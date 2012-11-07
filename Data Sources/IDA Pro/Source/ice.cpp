/* 
 * Plugin: ICE
 * Author: Dean Pucsek <dpucsek@uvic.ca>
 * Date: 16 October 2012
 *
 * Provide communication between IDA Pro and the ICE application.
 *
 */

/* Windows API 
 *
 * IMPORTANT: The Windows API MUST be included before anything else.
 */

#include <winsock2.h>
#include <windows.h>
#include <ws2tcpip.h>

/* IDA Includes */
#pragma warning(disable : 4996)
#pragma warning(disable : 4267)
#include <ida.hpp>
#include <idp.hpp>
#include <loader.hpp>
#include <kernwin.hpp>
#include <search.hpp>
#include <xref.hpp>
#include <entry.hpp>

/* JSON Library */
#include <jansson.h>

/* C++ */
#include <vector>
#include <map>

#define COMM_PORT       4040
#define COMM_HOST       "127.0.0.1"
#define COMM_HEADER_LEN 4

#define SMALL_BUF 256

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#define ICE_DEBUG 1

#ifdef ICE_DEBUG
#define DPRINT(s) msg("[ICE] " s "\n")
#define DPRINTF(s, ...) msg("[ICE] " s "\n", __VA_ARGS__)
#else
#define DPRINT(s)
#define DPRINTF(s, ...)
#endif

/* -------------- Communication -------------- */

void commSend(SOCKET socket, char *msgStr)
{
	int msgLen = strlen(msgStr);

	char *sendBuf = (char *)calloc(1, msgLen+COMM_HEADER_LEN);
	if(!sendBuf)
		return;

	*((short *)sendBuf) = (short)msgLen;
	*(sendBuf + 2) = 0x0d; /* CR */
	*(sendBuf + 3) = 0x0a; /* LF */

	memcpy(sendBuf+COMM_HEADER_LEN, msgStr, msgLen);

	//DPRINTF("<commSend> msg length: %x", *(short *)sendBuf);
	//DPRINTF("<commSend> msg: %s", sendBuf+COMM_HEADER_LEN);

	send(socket, sendBuf, msgLen+COMM_HEADER_LEN, 0);

	free(sendBuf);
}

/* -------------- ICE Requests -------------- */

void handle_request_calls(SOCKET commSock)
{
	msg("Warning: unable to handle call requests (not implemented yet)\n");
}

void __send_sync(SOCKET commSock)
{
	json_t *root;
	char *fn_buf;
	char *sync_msg;
	DWORD pid;

	root = json_object();
	
	pid = GetCurrentProcessId();
	json_object_set_new(root, "instance_id", json_integer(pid));

	fn_buf = (char *)calloc(1, SMALL_BUF);
	get_root_filename(fn_buf, SMALL_BUF);
	json_object_set_new(root, "origin", json_string(fn_buf));
	free((void *)fn_buf);

	json_object_set_new(root, "action", json_string("response"));
	json_object_set_new(root, "actionType", json_string("sync"));

	sync_msg = json_dumps(root, 0);

	commSend(commSock, sync_msg);

	DPRINT("sync msg sent");
}

void __send_calls(SOCKET commSock, ea_t func_start, std::vector<std::pair<ea_t, ea_t>> calls)
{
	json_t *root, *call;
	char *data_str, *call_msg;
	DWORD pid;
	std::vector<std::pair<ea_t, ea_t>>::iterator call_iterator;

	root = json_object();

	pid = GetCurrentProcessId();
	json_object_set_new(root, "instance_id", json_integer(pid));

	char *fn_buf = (char *)calloc(1, SMALL_BUF);
	get_root_filename(fn_buf, SMALL_BUF);
	json_object_set_new(root, "origin", json_string(fn_buf));
	free((void *)fn_buf);

	json_object_set_new(root, "action", json_string("response"));
	json_object_set_new(root, "actionType", json_string("calls"));

	for(call_iterator=calls.begin(); call_iterator < calls.end(); call_iterator++)
	{
		call = json_object();
		json_object_set_new(call, "callee", json_integer(func_start));
		json_object_set_new(call, "from", json_integer((*call_iterator).first));
		json_object_set_new(call, "to", json_integer((*call_iterator).second));
		
		data_str = json_dumps(call, 0);
		json_object_set_new(root, "data", json_string(data_str));

		call_msg = json_dumps(root, 0);

		/* send data */
		commSend(commSock, call_msg);
	}
}

void handle_request_functions(SOCKET commSock)
{
	int func_idx, entry_idx, isEntry;
	func_t *func;
	ea_t start, end, entry_addr;
	char *name_buf, *func_msg;
	char *data_str, *modname_buf;
	char *en_buf;
	uval_t ord;
	json_t *root, *func_data;

	name_buf = (char *)calloc(1, SMALL_BUF);
	if(!name_buf)
		msg("Error: calloc name_buf\n");

	modname_buf = (char *)calloc(1, SMALL_BUF);
	if(!modname_buf)
		msg("Error: calloc modname_buf\n");

	get_root_filename(modname_buf, SMALL_BUF);

	en_buf = (char *)calloc(1, SMALL_BUF);
	if(!en_buf)
		msg("Error: calloc en_buf\n");

	/* get exported symbols */
	std::map<ea_t, std::string> entry_map;
	for(entry_idx = 0; entry_idx < get_entry_qty(); entry_idx++)
	{
		ord = get_entry_ordinal(entry_idx);
		entry_addr = get_entry(ord);
		get_entry_name(ord, en_buf, SMALL_BUF);

		entry_map.insert(std::pair<ea_t, std::string>(entry_addr, std::string(en_buf)));

		memset(en_buf, 0, SMALL_BUF);
	}

	/* process functions */
	for(func_idx = 0; func_idx < get_func_qty(); func_idx++)
	{
		/* basic info */
		func = getn_func(func_idx);
		get_func_name(func->startEA, name_buf, SMALL_BUF);

		start = func->startEA;
		end = func->endEA;	

		isEntry = entry_map.count(start);

		/* find all call instructions */
		ea_t code_ea;
		std::vector<std::pair<ea_t,ea_t>> calls;

		code_ea = func->startEA;
		while((code_ea = find_code(code_ea, SEARCH_DOWN | SEARCH_NEXT)) <= func->endEA)
		{
			if(is_call_insn(code_ea))
			{
				xrefblk_t xb;
				for(bool ok=xb.first_from(code_ea, XREF_ALL); ok; ok=xb.next_from())
				{
					if(xb.iscode)
					{
						if(xb.type == fl_CN || xb.type == fl_CF)
						{
							calls.push_back(std::make_pair(xb.from,xb.to));
						}
					}
				}
			}
		}

		/* build JSON string */
		root = json_object();
		func_data = json_object();

		json_object_set_new(func_data, "name", json_string(name_buf));
		json_object_set_new(func_data, "start", json_integer(start));
		json_object_set_new(func_data, "end", json_integer(end));
		json_object_set_new(func_data, "entryPoint", json_boolean(isEntry));
		json_object_set_new(func_data, "index", json_integer(get_func_num(start)));
		json_object_set_new(func_data, "module", json_string(modname_buf));

		DWORD pid = GetCurrentProcessId();
		json_object_set_new(root, "instance_id", json_integer(pid));

		char *fn_buf = (char *)calloc(1, SMALL_BUF);
		get_root_filename(fn_buf, SMALL_BUF);
		json_object_set_new(root, "origin", json_string(fn_buf));
		free((void *)fn_buf);

		json_object_set_new(root, "action", json_string("response"));
		json_object_set_new(root, "actionType", json_string("functions"));

		data_str = json_dumps(func_data, 0);
		json_object_set_new(root, "data", json_string(data_str));

		func_msg = json_dumps(root, 0);

		/* send data */
		//DPRINTF("func_msg [%d] (%d) :: %s", func_idx, strlen(func_msg), func_msg);
		commSend(commSock, func_msg);

		__send_calls(commSock, start, calls);

		memset(name_buf, 0, SMALL_BUF);
	}

	free(name_buf);
	free(modname_buf);

	__send_sync(commSock);
}

int handle_request(SOCKET commSock, json_t *req)
{
	const char *reqType;
	json_t *val;

	val = json_object_get(req, "actionType");
	reqType = json_string_value(val);
	DPRINTF("Request type: %s\n", reqType);

	if(strcmp("functions", reqType) == 0)
	{
		handle_request_functions(commSock);
	} 
	else if(strcmp("calls", reqType) == 0) 
	{
		handle_request_calls(commSock);
	}

	return 0;
}

/* -------------- ICE Thread -------------- */

DWORD WINAPI ICE_ThreadProc(LPVOID lpParameter)
{
	SOCKET commSock;
	struct sockaddr_in saddr;
	json_error_t json_error;
	json_t *root;
	fd_set readfds;
	int ret;

	commSock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	DPRINT("created socket\n");

	DPRINTF("Host: 0x%lx Port: %d", inet_addr(COMM_HOST), COMM_PORT);
	saddr.sin_family = AF_INET;
	saddr.sin_addr.s_addr = inet_addr(COMM_HOST);
	saddr.sin_port = htons(COMM_PORT);

	ret = connect(commSock, (const sockaddr *)&saddr, sizeof(saddr));
	if(ret == SOCKET_ERROR)
	{
		msg("Error: connect failed\n");
		return 0;
	}

	/* send hello msg */
	root = json_object();

	DWORD pid = GetCurrentProcessId();
	json_object_set_new(root, "instance_id", json_integer(pid));

	char *fn_buf = (char *)calloc(1, SMALL_BUF);
	get_root_filename(fn_buf, SMALL_BUF);
	json_object_set_new(root, "origin", json_string(fn_buf));
	free((void *)fn_buf);

	json_object_set_new(root, "action", json_string("hello"));
	json_object_set_new(root, "data", json_null());

	char *hello_msg = json_dumps(root, 0);
				
	DPRINTF("sending: %s", hello_msg);
	DPRINTF("length: %d", strlen(hello_msg));
				
	commSend(commSock, hello_msg);

	timeval select_interval;
	select_interval.tv_sec = 1;
	select_interval.tv_usec = 0;

	while(1)
	{
		FD_ZERO(&readfds);
		FD_SET(commSock, &readfds);

		ret = select(0, &readfds, NULL, NULL, (const timeval *)&select_interval);
		if(ret == SOCKET_ERROR)
		{
			msg("Erorr: select failed\n");
			break;
		}

		/* reading */
		if(FD_ISSET(commSock, &readfds))
		{
			char *recv_buf = (char *)calloc(1, SMALL_BUF);
			if(!recv_buf)
				msg("failed to calloc\n");

			recv(commSock, recv_buf, SMALL_BUF, 0);
			DPRINTF("received: %s", recv_buf);
			
			if(recv_buf[0] != '{')
				continue;

			int found_json = FALSE;
			int i = 0;
			for(i = 0; i < SMALL_BUF; i++)
			{
				if(found_json)
					recv_buf[i] = 0;
				else if(recv_buf[i] == '}')
					found_json = TRUE;
			}

			root = json_loads(recv_buf, 0, &json_error);
			if(root == NULL) {
				msg("Error: failed to load JSON string\n");
				msg("\\tJSON Error: %s\n", json_error.text);
			}

			free(recv_buf);

			json_t *value;

			if(!json_is_object(root))
				msg("Error: not a JSON object\n");

			value = json_object_get(root, "action");
			if(strcmp("request", json_string_value(value)) == 0)
				handle_request(commSock, root);
			else
				msg("Action is: %s\n", json_string_value(value));
		}
	}

	return 0;
}

/* -------------- IDA Plugin Interface -------------- */

int idaapi init(void)
{
	return is_idaq() ? PLUGIN_OK : PLUGIN_SKIP;
}

void idaapi term(void)
{
}

void idaapi run(int arg)
{
	CreateThread(NULL				/* lpThreadAttributes	*/
				, 0					/* dwStackSize			*/
				, ICE_ThreadProc	/* lpStartAddress		*/
				, NULL				/* lpParameter			*/
				, 0					/* dwCreationFlags		*/
				, NULL				/* lpThreadId			*/
				);
}

const char *comment = "Communication with ICE";
const char *help = "";
const char *wanted_name = "ICE";
const char *wanted_hotkey = "Alt-i";

plugin_t PLUGIN = {
	IDP_INTERFACE_VERSION,
	0,
	init,
	term,
	run,
	comment,
	help,
	wanted_name,
	wanted_hotkey
};
