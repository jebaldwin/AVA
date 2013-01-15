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
#include <name.hpp>

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

	//DPRINTF("<commSend> msg length: 0x%x", *(short *)sendBuf);
	//DPRINTF("<commSend> msg: %s", sendBuf+COMM_HEADER_LEN);

	send(socket, sendBuf, msgLen+COMM_HEADER_LEN, 0);

	free(sendBuf);
}


/* -------------- Utility Functions -------------- */

func_t *find_function_by_name(const char *fn)
{
    size_t func_idx;
    func_t *func;
    char *fn_buf;
    
    fn_buf = (char *)calloc(SMALL_BUF, sizeof(char));
    if(!fn_buf)
        return NULL;

    for(func_idx = 0; func_idx < get_func_qty(); func_idx++)
    {
        memset((void *)fn_buf, 0, SMALL_BUF);

        func = getn_func(func_idx);
        if(!get_func_name(func->startEA, fn_buf, SMALL_BUF))
            continue;

        if(strncmp(fn, fn_buf, SMALL_BUF) == 0)
            return func;
    }

    return NULL;
}

/* -------------- CFG Generation -------------- */

void cfg_gen(SOCKET commSock, const char *func_name)
{
    func_t *func;
    ea_t insn_ea;

    std::vector<ea_t> xrefs_to(0);
    bool is_bb_end;
    xrefblk_t xb;

    func = find_function_by_name(func_name);
    if(!func) {
        DPRINTF("Unable to find function: %s", func_name);
        return;
    }

    insn_ea = func->startEA;
    while(insn_ea < func->endEA)
    {
        msg("insn: 0x%x\n", insn_ea);

        is_bb_end = false;
        xrefs_to.clear();

        for(bool ok=xb.first_from(insn_ea, XREF_ALL); ok; ok=xb.next_from())
        {
            if(xb.type == fl_JN)
                is_bb_end = true;

			
            xrefs_to.push_back(xb.to);
        }

		/* action: response
		 * type: instruction
		 * data: { address: <instr-addr>
		 *		   next: [<instr-addr>, ...]
		 */

		json_t *root, *instr_data, *next_array;
		char *fn_buf;
		char *instr_msg;
		DWORD pid;

		root = json_object();
		instr_data = json_object();
	
		json_object_set_new(instr_data, "address", json_integer(insn_ea));
		json_object_set_new(instr_data, "containing", json_integer(func->startEA));

		next_array = json_array();
		for(std::vector<ea_t>::iterator it = xrefs_to.begin(); it != xrefs_to.end(); ++it)
			json_array_append(next_array, json_integer(*it));

		json_object_set_new(instr_data, "next", next_array);

		pid = GetCurrentProcessId();
		json_object_set_new(root, "instance_id", json_integer(pid));

		fn_buf = (char *)calloc(1, SMALL_BUF);
		get_root_filename(fn_buf, SMALL_BUF);
		json_object_set_new(root, "origin", json_string(fn_buf));
		free((void *)fn_buf);

		json_object_set_new(root, "action", json_string("response"));
		json_object_set_new(root, "actionType", json_string("instructions"));

		json_object_set_new(root, "data", json_string(json_dumps(instr_data, 0)));

		instr_msg = json_dumps(root, 0);

		commSend(commSock, instr_msg);
		DPRINTF("send instruction: %s", instr_msg);

        if(is_bb_end)
        {
            for(std::vector<ea_t>::iterator it = xrefs_to.begin(); it != xrefs_to.end(); ++it)
                msg("-> xref from 0x%x to 0x%x\n", insn_ea, *it);
        }

        insn_ea = find_code(insn_ea, SEARCH_DOWN);
    }
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

struct import_enum_param_t {
	int mod_index;
	SOCKET sock;
};

int __stdcall import_callback(ea_t addr, const char *name, uval_t ord, void *param)
{
	json_t *root, *func_data;
	char *modname_buf, *data_str;
	char *func_msg;
	struct import_enum_param_t *params = (struct import_enum_param_t *)param;

	modname_buf = (char *)calloc(1, SMALL_BUF);
	if(modname_buf == NULL) {
		msg("calloc failed\n");
	}

	get_import_module_name(params->mod_index, modname_buf, SMALL_BUF);

	/* build JSON string */
	root = json_object();
	func_data = json_object();

	json_object_set_new(func_data, "name", json_string(name));
	json_object_set_new(func_data, "start", json_integer(addr));
	json_object_set_new(func_data, "end", json_integer(addr));
	json_object_set_new(func_data, "entryPoint", json_boolean(false));
	json_object_set_new(func_data, "index", json_integer(-1));
	json_object_set_new(func_data, "module", json_string(modname_buf));

	free(modname_buf);

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
	commSend(params->sock, func_msg);

	return 1;
}

void handle_request_functions(SOCKET commSock)
{
	int func_idx, entry_idx, isEntry, mod_idx;
	func_t *func;
	ea_t start, end, entry_addr;
	char *name_buf, *func_msg;
	char *data_str, *modname_buf;
	char *func_cmt;
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

	/* process imports */
	for(mod_idx = 0; mod_idx < get_import_module_qty(); mod_idx++)
	{
		struct import_enum_param_t params;
		params.mod_index = mod_idx;
		params.sock = commSock;
		enum_import_names(mod_idx, (import_enum_cb_t *)import_callback, &params);
	}

	msg("---> going to process functions\n");

	/* process functions */
	for(func_idx = 0; func_idx < get_func_qty(); func_idx++)
	{
		/* basic info */
		func = getn_func(func_idx);
		get_func_name(func->startEA, name_buf, SMALL_BUF);

		start = func->startEA;
		end = func->endEA;	

		isEntry = entry_map.count(start);

		/* get any associated comment */
		func_cmt = get_func_cmt(func, true);

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
		json_object_set_new(func_data, "comment", json_string(func_cmt));
		qfree(func_cmt);

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
		commSend(commSock, func_msg);

		__send_calls(commSock, start, calls);

		memset(name_buf, 0, SMALL_BUF);
	}

	free(name_buf);
	free(modname_buf);

	__send_sync(commSock);
}

int handle_request_updateCursor(const char *new_ea_str) {
	unsigned long new_ea = strtoul(new_ea_str, NULL, 10);
	if(new_ea != 0)
		jumpto(new_ea);
	
	return 0;
}

int handle_request_setComment(const char *cmt, json_int_t json_ea)
{
	int ea = (int)json_ea;
	func_t *fn = get_func(ea);
	set_func_cmt(fn, cmt, true);
	return 0;
}

int handle_request_rename(const char *new_name, json_int_t json_ea)
{
	int ea = (int)json_ea;
	set_name(ea, new_name, SN_CHECK);
	return 0;
}

int handle_request_cfg(SOCKET commSock, const char *json_enc_obj)
{
	const char *func_name;
	json_error_t json_error;
	json_t *val, *root;

	root = json_loads(json_enc_obj, 0, &json_error);
	if(root == NULL) {
				msg("Error: failed to load JSON string\n");
				msg("\tJSON Error: %s\n", json_error.text);
			}

	val = json_object_get(root, "name");
	func_name = json_string_value(val);
	DPRINTF("Function name: %s\n", func_name);
	cfg_gen(commSock, func_name);

	return 0;
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
	else if(strcmp("updateCursor", reqType) == 0)
	{
		val = json_object_get(req, "data");
		handle_request_updateCursor(json_string_value(val));
	}
	else if(strcmp("setComment", reqType) == 0)
	{
		val = json_object_get(req, "data");
		val = json_loads(json_string_value(val), 0, NULL);

		handle_request_setComment(
			json_string_value(json_object_get(val, "item")),
			json_integer_value(json_object_get(val, "address")));
	}
	else if(strcmp("rename", reqType) == 0)
	{
		const char *json_item;
		val = json_object_get(req, "data");
		val = json_loads(json_string_value(val), 0, NULL);

		json_item = json_string_value(json_object_get(val, "item"));

		msg("json_item: (%p) %s\n", json_item, json_item);
		handle_request_rename(
			json_item,
			json_integer_value(json_object_get(val, "address")));

		//free(new_item);
	}
	else if(strcmp("cfg", reqType) == 0)
	{
		val = json_object_get(req, "data");
		handle_request_cfg(commSock, json_string_value(val));
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
			int i = 0, c = 0;
			for(i = 0; i < SMALL_BUF; i++)
			{
				if(found_json)
					recv_buf[i] = 0;
				else if(recv_buf[i] == '{')
					c++;
				else if(recv_buf[i] == '}')
					c--;

				if(c == 0)
					found_json = TRUE;
			}

			root = json_loads(recv_buf, 0, &json_error);
			if(root == NULL) {
				msg("Error: failed to load JSON string\n");
				msg("\tJSON Error: %s\n", json_error.text);
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
