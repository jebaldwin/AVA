//
// snaptrace2.cpp
//

#include <ida.hpp>
#include <idp.hpp>
#include <loader.hpp>
#include <dbg.hpp>
#include <name.hpp>

ea_t start_ea = 0;
ea_t end_ea = 0;

// Handler for HT_DBG events
int idaapi trace_handler(void *udata, int dbg_event_id, va_list va) {
	regval_t esp, eip;

	// Get ESP register value
	get_reg_val("esp", &esp);
	// Get EIP register value
	get_reg_val("eip", &eip);

	if(dbg_event_id == dbg_exception){
		msg("exception \n");
	}

	// We'll also receive debug events unrelated to tracing,
	// make sure those are filtered out
	if (dbg_event_id == dbg_trace) {
		if(eip.ival != BADADDR){
			func_t *func = get_func(eip.ival);
			if (func == NULL) {
				//external function
				msg("%a \n", eip.ival);

				char name[MAXSTR];
				//char *res = get_name(BADADDR, eip.ival, name, sizeof(name)-1);
				char *res = get_name(BADADDR, eip.ival, name, sizeof(name)-1);

				if (res != NULL) {
					msg("Name: %s\n", name);			
				}
			}
		}
	}

  return 0;
}

int idaapi IDAP_init(void)
{
  // Receive debug event notifications
  hook_to_notification_point(HT_DBG, trace_handler, NULL);
  return PLUGIN_KEEP;
}

void idaapi IDAP_term(void)
{
  // Unhook from the notification point on exit
  unhook_from_notification_point(HT_DBG, trace_handler, NULL);
  return;
}

void idaapi IDAP_run(int arg) 
{  
    //Run to the binary entry point
    request_run_to(inf.startIP);
    request_enable_step_trace();
    run_requests();
}

char IDAP_comment[] = "Snap Tracer 2";
char IDAP_help[] = "Allow tracing only between user "
                   "specified addresses\n";


char IDAP_name[] = "Snap Tracer 2";
char IDAP_hotkey[] = "Alt-I";

plugin_t PLUGIN =
{
  IDP_INTERFACE_VERSION,
  0,
  IDAP_init,
  IDAP_term,
  IDAP_run,
  IDAP_comment,
  IDAP_help,
  IDAP_name,
  IDAP_hotkey
};
