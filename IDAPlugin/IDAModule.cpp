#define USE_DANGEROUS_FUNCTIONS

#include "IDAModule.h"
#include "PLWInterface.h"
#include "IdaSdk.h"
#include "Utils.h"
#include "SequenceDumper.h"
#include <stdio.h>
#include <auto.hpp>
#include <windows.h>
#include "idp.hpp"
#include <psapi.h>

// The plugin can be passed an integer argument from the plugins.cfg
// file. This can be useful when you want the one plug-in to do
// something different depending on the hot-key pressed or menu
// item selected. (Comment from the IDA plugin Tutorial - openrce.org)
 
//#undef _DEBUG

#ifdef _DEBUG
    const std::string gPluginName = "AVA Framework - Debug";
#else
    const std::string gPluginName = "AVA Framework";
#endif

#define MAX_FILENAME_LENGTH 256
const std::string gPluginHelp = "No help for now...";
const std::string gPluginHotKey = "alt-f5";

// Plugin's singleton unique instance.
static const CPLWInterface::singleton_initializer PluginMaker(new CIDAModule);

// Put all jar needed in the java application's classpath.
const std::string gClasspath = "ava.jar;javaPluginIDA.jar;idaComm.jar"; //Name of the java plugin project exported jar file.
const std::string gMainClass = "cs.uvic.ca.pluginName.app.PluginNameApp";
const std::string gJAR_DEFAULT_SUBFOLDER = "/plugins/java/";
const char gPATH_SEPARATOR = ';';

// Messages strings
const std::string gVersionWarning = "%s Warning - This plugin version has been designed for IDA pro %s to %s. You may experience problems.\n";
const std::string gCommStarted = "javaPlugin to IDA Pro communication started (Port: %u)\n";
const std::string gAnalysisRunning = "Please wait for the auto-analysis to complete before running this plugin.\n";
const std::string gPluginRunning = "%s is already running!\n";
const std::string gClassPathError = "An error occured when initializing CLASSPATH!\n";
const std::string gJavaAppClosed = "Java app has been closed. You could restart it at anytime with the short-cut.\n";

// Communication strings
const std::string gJavaNotResponding = "java does not respond";
const std::string gUpdateJavaServerPort = "updateJavaServerPort";
const std::string gBye = "bye\n";
const std::string pageBreak = "!jeb!";

//sequence diagram messages
const std::string gUpdateCursor = "updateCursor"; // Example specific.
const std::string gNavigateFunction = "navigateFunction"; // Navigation to specific function.
const std::string gPluginStarted = "pluginStarted"; // Process initial request.
const std::string gSetBreakpoint = "setBreakpoint"; // Set breakpoint.
const std::string gRetrieveInfo = "retrieve"; // Retrieve call info.
const std::string gRetrieveEntryInfo = "retrieveEntry"; // Retrieve call info.
const std::string gEnableTracing = "enableTracing"; // Retrieve call info.
const std::string gDisableTracing = "disableTracing"; // Retrieve call info.
const std::string gEnableInner = "enableInner"; 
const std::string gDisableInner = "disableInner";
const std::string gPrefCount = "prefCount";
const std::string gStopLoop = "stopLoop";
const std::string gDebugTraceStart = "startDebuggingWithTracing";

//state diagram messages
const std::string gRecordStart = "startRecording";
const std::string gRecordStop = "stopRecording";
const std::string gExecuteAction = "executeAction";
std::string stateMessages = "";

boolean trace = false;
boolean navigatedToExternal = false;
char currFunction[MAXSTR];
ea_t lastFunctionEA = NULL;
char currNavFunction[MAXSTR];
char lastLocFunction[MAXSTR];
ea_t lastEA = NULL;
char module_filename[MAX_FILENAME_LENGTH];
char renameNew[MAXSTR];
char renameOld[MAXSTR];
boolean traceInnerCalls = false;
boolean lastWasLocal = true;
boolean thatFunctionFound = false;
boolean firstSuspend = true;
string lastStackFunction = "";
list<string> ignoreList;
list<string> stopLoopAddr;
int loopCount = 0;
int prefCount = 2;
char lastFunction[MAXSTR];
ea_t jumpAddr = NULL;
int jumpCount = 1;
ea_t stopLoopAddrs[MAXSTR];
boolean noLoop = false;
int loopIndex = 0;
ea_t lastJumpAddr = BADADDR;
bool justStoppedTracing = false;


CIDAModule::CIDAModule(void)
    : m_LOWER_IDA_VERSION("5.1"),
	  m_HIGHER_IDA_VERSION("6.0"),
      m_notificationSuspendedTokens(1),
      m_lastDecimalEffectiveAddress(0),
      m_pluginStarted(false),
      m_bHooked(false),
      m_UILoaded(false),
      m_pJavaComm(NULL)
{
	// Initialize Call instructions vector
	ushort cIns[] = {
		NN_call,                // Call Procedure
		NN_callfi,              // Indirect Call Far Procedure
		NN_callni,              // Indirect Call Near Procedure
	};
	callIns = vector<ushort>(cIns, cIns+sizeof(cIns)/sizeof(ushort));

	// Initialize Jump instructions vector
	ushort jIns[] = {
		NN_ja,                  // Jump if Above (CF=0 & ZF=0)
		NN_jae,                 // Jump if Above or Equal (CF=0)
		NN_jb,                  // Jump if Below (CF=1)
		NN_jbe,                 // Jump if Below or Equal (CF=1 | ZF=1)
		NN_jc,                  // Jump if Carry (CF=1)
		NN_jcxz,                // Jump if CX is 0
		NN_jecxz,               // Jump if ECX is 0
		NN_jrcxz,               // Jump if RCX is 0
		NN_je,                  // Jump if Equal (ZF=1)
		NN_jg,                  // Jump if Greater (ZF=0 & SF=OF)
		NN_jge,                 // Jump if Greater or Equal (SF=OF)
		NN_jl,                  // Jump if Less (SF!=OF)
		NN_jle,                 // Jump if Less or Equal (ZF=1 | SF!=OF)
		NN_jna,                 // Jump if Not Above (CF=1 | ZF=1)
		NN_jnae,                // Jump if Not Above or Equal (CF=1)
		NN_jnb,                 // Jump if Not Below (CF=0)
		NN_jnbe,                // Jump if Not Below or Equal (CF=0 & ZF=0)
		NN_jnc,                 // Jump if Not Carry (CF=0)
		NN_jne,                 // Jump if Not Equal (ZF=0)
		NN_jng,                 // Jump if Not Greater (ZF=1 | SF!=OF)
		NN_jnge,                // Jump if Not Greater or Equal (ZF=1)
		NN_jnl,                 // Jump if Not Less (SF=OF)
		NN_jnle,                // Jump if Not Less or Equal (ZF=0 & SF=OF)
		NN_jno,                 // Jump if Not Overflow (OF=0)
		NN_jnp,                 // Jump if Not Parity (PF=0)
		NN_jns,                 // Jump if Not Sign (SF=0)
		NN_jnz,                 // Jump if Not Zero (ZF=0)
		NN_jo,                  // Jump if Overflow (OF=1)
		NN_jp,                  // Jump if Parity (PF=1)
		NN_jpe,                 // Jump if Parity Even (PF=1)
		NN_jpo,                 // Jump if Parity Odd  (PF=0)
		NN_js,                  // Jump if Sign (SF=1)
		NN_jz,                  // Jump if Zero (ZF=1)
		NN_jmp,                 // Jump
		NN_jmpfi,               // Indirect Far Jump
		NN_jmpni,               // Indirect Near Jump
		NN_jmpshort,            // Jump Short (not used)
	};
	jumpIns = vector<ushort>(jIns, jIns+sizeof(jIns)/sizeof(ushort));

	populateImports();
}

CIDAModule::~CIDAModule(void)
{
}

/**
 * \fn getIDAPluginInfo()
 * \brief IDA pro - init function.
 */
void CIDAModule::getIDAPluginInfo(int &FeatureFlags,
								  std::string &Name,
								  std::string &HotKey,
								  std::string &Comment,
								  std::string &Help ) const
{
    FeatureFlags = 0;
    Comment = gPluginName;
    Help =    gPluginHelp;
    HotKey =  gPluginHotKey;

    // The name of the plug-in displayed in the Edit->Plugins menu. It can
    // be overridden in the user's plugins.cfg file.
    Name =    gPluginName;
}

/**
 * \fn onInit()
 * \brief IDA pro - init function.
 */ 
int CIDAModule::onInit()
{
    // If the disassembled file is not a Portable Executable (PE).
    if( inf.filetype != f_PE ) 
    {
        // Returning this value signals that the plugin should not be loaded.
        return PLUGIN_SKIP;
    }

  	// Call to sdk api to get IDA's version.
    m_currentIDAVersion = CIdaSdk::getIDAVersionString();

    // If the IDA version is not supported show a warning in the console view.
	// Note: To support a new IDA version, add the new version number in onCallBackUI too.
    if ( m_currentIDAVersion.compare(m_LOWER_IDA_VERSION) < 0 || 
		 m_currentIDAVersion.compare(m_HIGHER_IDA_VERSION) > 0 ) 
    {
		msg(gVersionWarning.c_str(), CPLWInterface::getInstance().getIDAPlugin().wanted_name, m_LOWER_IDA_VERSION.c_str(), m_HIGHER_IDA_VERSION.c_str());
    }

    // Returning this value instructs IDA to make the plugin available for use
    // with the current database. Ida loads the plugin when the user activates 
    // the plugin using a menu action or a hotkey.
    // return PLUGIN_OK;
 
    // Returning this value instructs IDA to make the plugin available for use
    // with the current database and keep the plugin loaded in memory.
    return PLUGIN_KEEP;
}

/**
 * \fn onRun()
 * \brief IDA pro - Run function
 */
void CIDAModule::onRun(int arg)
{
	// Wait for the analyse to finish before running the plugin.
    if (autoIsOk() && autoEnabled){

        if (m_pluginStarted){
            // Run once functionality:****************
            if (!m_UILoaded){
    			startUI();
            }else{
                msg(gPluginRunning.c_str(), CPLWInterface::getInstance().getIDAPlugin().wanted_name);
            }
            // Run once functionality ****************
        } else {
            // Communication Step 1/7 - Instanciate the c++ server.
            m_pJavaComm = new CComm(this);

			// Communication Step 2/7 - Start socket server (Try port 40010 first, then upper port after)
            unsigned short javaServerPort = m_pJavaComm->initServer();
            msg(gCommStarted.c_str(), javaServerPort);

   			// Communication Step 3/7 - Send the cppServerPort to the java app by command line argument.
			startUI(); // DEBUG NOTE: Just comment this line to start your java app manually with eclipse.

   			// Communication Step 4/7 - Start java server and client (See Java code)
			
			m_pluginStarted = true;
			m_debugging = false;
        }
    } else {
		msg(gAnalysisRunning.c_str());
    }

}

int CIDAModule::onCallBackProcessor( void * user_data, int notif_code, va_list va )
{
	if(!m_debugging){
		if(notif_code == processor_t::rename){
			//about to be renamed
			ea_t ea = va_arg(va, ea_t);
			const char *new_name = va_arg(va, const char *);
			func_t *func = get_func(ea);

			if (func != NULL) {
				char funcName[MAXSTR];
				if(strcmp(new_name, "") != 0){
					if (get_func_name(func->startEA, funcName, MAXSTR) != NULL) {
						qstrncpy(renameNew, new_name, sizeof(new_name));
						qstrncpy(renameOld, funcName, sizeof(funcName));
					} 
				} 
			} 
		} else {
			if(notif_code == processor_t::renamed){
				ea_t ea = va_arg(va, ea_t);
				const char *new_name = va_arg(va, const char *);

				//check if was renamed
				if(strcmp(new_name, renameOld) != 0){
				//if(renameNew != NULL && renameOld != NULL){
					if(strcmp(new_name, "") != 0){
						//actually was renamed
						qsnprintf(tempChar, sizeof(tempChar)-1, "rename\t%s\t%s\t%s\n", renameOld, new_name, module_filename);
						m_pJavaComm->send(tempChar);
					}
				//}
				}
			} 
		} 
	} else {
		if(notif_code == processor_t::add_func){
			std::string prepend = "debugexpandcall";
			func_t *func = va_arg(va, func_t *);
			char funcName[MAXSTR];
			get_func_name(func->startEA, funcName, MAXSTR);
			
			regval_t eip;
			get_reg_val("eip", &eip);

			if(eip.ival == func->startEA){

				//if current function is unk_addr or loc_addr then rename event
				functionText = "";
				char outStr[256];
				char unkName[256];
				char locName[256];
				int inDec = eip.ival;
				qsnprintf(outStr, sizeof(outStr)-1, "%x", inDec);
				for (size_t i = 0; i < 256; ++i){
			        outStr[i] = toupper(outStr[i]);
			    }

				qsnprintf(unkName, sizeof(unkName)-1, "unk_%s", outStr);

				qsnprintf(locName, sizeof(locName)-1, "loc_%s", outStr);

				if(strcmp(currFunction, unkName) == 0){
					prepend = "";
					qsnprintf(tempChar, sizeof(tempChar)-1, "rename\t%s\t%s\t%s\n", unkName, funcName, module_filename);
				} else {
					if(strcmp(currFunction, locName) == 0){
						//send rename event
						prepend = "";
						qsnprintf(tempChar, sizeof(tempChar)-1, "rename\t%s\t%s\t%s\n", locName, funcName, module_filename);
					} else {		
						qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\n", 0, func->startEA, funcName, module_filename);
					}
				}
				functionText.append(tempChar);
				m_pJavaComm->send(prepend.append(functionText));
				qstrncpy(currFunction, funcName, MAXSTR);
				lastStackFunction = currFunction;
			}
		}
	}

	return 0;
}

/**
 * \fn CIDAModule::onCallBackUI()
 * \brief IDA pro - Handle events from the UI
 */
int CIDAModule::onCallBackUI( void * user_data, int notif_code, va_list va )
{
	if(!m_debugging && notif_code == ui_preprocess){
	//if(!m_debugging){
		const char *action_name = va_arg(va, const char *);
		//msg("action name: %s\n", action_name);
		
		if(stristr(action_name, "Jump") != NULL){
		try{
			// Handle events from the UI and filter event depending
			// on the current IDA pro version.
			// ui_setstate event is used with IDA pro 5.1
			// ui_idp_event event is used with later versions.
			if (( (m_currentIDAVersion.compare(m_LOWER_IDA_VERSION) == 0) && (notif_code == ui_setstate)  ) ||
				( (m_currentIDAVersion.compare(m_LOWER_IDA_VERSION)  > 0) && (notif_code == ui_idp_event) )) {

				ea_t ea;
				// Get the current cursor position, store it in addr
				callui(ui_screenea, &ea);

				//ea_t ea = get_screen_ea();
				//ea_t ea = va_arg(va, ea_t);
				/*func_t *func = va_arg(va, func_t *);
				char funcName[MAXSTR];
				get_func_name(func->startEA, funcName, MAXSTR);*/
				
				/*regval_t eip;
				get_reg_val("eip", &eip);
				ea_t ea = eip.ival;*/
		

				if (ea != BADADDR) { // Is it a bad address?
					if (ea != m_lastDecimalEffectiveAddress) { // Is it the same we just sent?
						if (m_notificationSuspendedTokens == 0){ // Example specific flag.
							m_lastDecimalEffectiveAddress = ea;

							if (m_UILoaded){ // No need to send the position if the java app is not running.
#ifdef _DEBUG
								msg("nav event: %a\n",ea);
#endif
								int state = get_process_state();

								if(state == DSTATE_NOTASK){

									//need to convert ea to function that was followed
									func_t *func = get_func(ea);
									
									char moduleName[256];
									segment_t *seg = getseg(ea);
									get_segm_name(seg, moduleName, sizeof(moduleName)-1);
									string mystring = string(moduleName);
									size_t found = mystring.find("_text");

									if (func != NULL && found != string::npos) {
										if(navigatedToExternal){
											navigatedToExternal = false;
										} else {
											// Buffer where the function name will be stored
											char funcName[MAXSTR];
											if (get_func_name(func->startEA, funcName, MAXSTR) != NULL) {
												
#ifdef _DEBUG
												msg("Current function %a, named %s\n", func->startEA, funcName);
#endif

												//check if we're in a new function
												if(strcmp(currNavFunction, funcName) != 0){
													
													boolean found = false;
													std::string prepend = "navigateexpandcall";

													if(lastEA != NULL){
														segment_t *seg = getseg(lastEA);
														if(seg->type != SEG_XTRN){
															func_t *f  = get_func(lastEA);
															ccc = 0;
															// Iterate over addresses from the start to end of the function
															for(ea_t addr = f->startEA; addr < f->endEA; addr++) {

																// Check if it's a call
																//if(matchesIns(addr, callIns)) {
																	// Check if the called address is stored in a register
																	if(cmd.Operands[0].type == o_reg) {
																		char buf[MAXSTR];
																		generate_disasm_line(addr, buf, sizeof(buf)-1);
																		// Get the actual address if possible statically
																		ea_t calledAddr = getAddressCalled(buf);
																		if(calledAddr != BADADDR) {
																			//dumpCall(addr, calledAddr);
																			segment_t *seg = getseg(calledAddr);
																			if(seg->type == SEG_CODE) {
																				char thisName[MAXSTR];
																				//char module_filename[256];
																				//get_root_filename(module_filename, 256);
																				if(get_func_name(calledAddr, thisName, MAXSTR) != NULL) {
																					if(strcmp(thisName, funcName) == 0){
																						found = true;
																						break;
																					}
																					//qfprintf(seq_file, "\t%d:\t%a\t%a\t%s\t%s\n", get_func_num(to), from, to, funcName, module_filename);
																				}
																			} //else if(seg->type == SEG_XTRN) {
																			//	dumpExternCall(from, to);
																			//}
																		}
																	} else {
																		// The address isn't in a register
																		// Using cross references to find the called address
																		xrefblk_t xb;
																		for(bool res = xb.first_from(addr, XREF_ALL); res; res = xb.next_from()) {
																			if(xb.iscode && ((xb.type == fl_CF) || (xb.type == fl_CN))) {
																				//dumpCall(addr, xb.to);
																				segment_t *seg = getseg(xb.to);
																				if(seg->type == SEG_CODE) {
																					char thisName[MAXSTR];
																					if(get_func_name(xb.to, thisName, MAXSTR) != NULL) {
																						if(strcmp(thisName, funcName) == 0){
																							found = true;
																							break;
																						}
																						//qfprintf(seq_file, "\t%d:\t%a\t%a\t%s\t%s\n", get_func_num(to), from, to, funcName, module_filename);
																					}
																				} //else if(seg->type == SEG_XTRN) {
																				//	dumpExternCall(from, to);
																				//}
																			}
																		}
																	}
															}
														} else {
															prepend.append("addtoroot");
														}

													}

													if(!found){
														prepend.append("addtoroot");
													}

													dumpFunction(func);
													//functionText = functionText.substr(0, functionText.length() - 2);
													//qsnprintf(tempChar, sizeof(tempChar)-1, "\t%a\n", inf.startIP);
													//send to Java diagram
													m_pJavaComm->send(prepend.append(functionText));
													//currNavFunction = funcName;
													qstrncpy(currNavFunction, funcName, sizeof(funcName));
													lastEA = ea;
												}
											}
											navigatedToExternal = false;
										}
									} else {

										//only log the first external call
										if(!navigatedToExternal){
												navigatedToExternal = true;
												char* line = (char *)qalloc(MAXSTR);

												//-4 seems to help  it be the correct line, who knows why!
												generate_disasm_line(ea - 4, line, MAXSTR - 1);

												//generate_disasm_line(xb.from, line, MAXSTR - 1);
												functionText = "";
												//char* line = (char *)qalloc(MAXSTR);
												//char* line2 = get_curline();
												//msg(line2);
												//msg("\n");
												//generate_disasm_line(ea, line, MAXSTR - 1);
												//msg(line);
												while(*line != 0 && *line != COLOR_ADDR)
														line++;
												
												if(*line == COLOR_ADDR) {
													line++;  // skip color code
											#ifdef __EA64__
													line += 16;  // skip 16 digits of 64-bit address
											#else
													line += 8;   // skip 8 digits of 32-bit address
											#endif
													char* funcname = line;  // Beginning of function name
													while(*line != COLOR_OFF)
														line++;
													*line = '\0';  // Null-terminate the string

													//check if we're in a new function
													if(strcmp(currNavFunction, funcname) != 0){
														//currNavFunction = funcname;
														qstrncpy(currNavFunction, funcname, MAXSTR);
														
														pair<string, string> imp = getImport(funcname);
														std::string prepend = "navigateexpandcall";

														//> 0:	1001630	sub_1001630
														if(imp.second.empty()) {
															qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\t%s\n", 0, ea, funcname, module_filename, "Unknown");
															functionText.append(tempChar);
															//qfprintf(seq_file, "\t-1:\t%a\t%a\t%s\t%s\n", from, to, funcname, "Unknown");
														} else {
															qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\t%s\n", 0, ea, funcname, module_filename, imp.second.c_str());
															functionText.append(tempChar);
															//qfprintf(seq_file, "\t-1:\t%a\t%a\t%s\t%s\n", from, to, imp.first.c_str(), imp.second.c_str());
														}

														m_pJavaComm->send(prepend.append(functionText));
													}
												}
											}
											//}
										//}
									}
								}
							}
						}
					}
				}
			}
		}catch (std::exception e){
			msg("An exception happened in onCallBackUI(): %s\n", e.what());
		}
		}
	}

    return 0;
}

/**
 * \author Jennifer Baldwin
 */
int CIDAModule::onCallBackDebug(void *udata, int event_id, va_list va)
{
	//char buffer [33];
	//itoa (event_id,buffer,10);
	//msg(buffer);
	// Only for the dbg_bpt event notification
	if (event_id == dbg_bpt) {
		// Get the Thread ID
		thread_id_t tid = va_arg(va, thread_id_t);

		// Get the address of where the breakpoint was hit
		ea_t addr = va_arg(va, ea_t);

		if(justStoppedTracing){
			justStoppedTracing = false;
			//run_to(get_screen_ea());
			//msg("contine process\n");
			continue_process();
			return 0;
		}

//#ifdef _DEBUG
//			msg("Breakpoint hit at: %a, in Thread: %d\n", addr, tid);
//#endif

		//get current function
		//ea_t addr = get_screen_ea();
		func_t *func = get_func(addr);
		if (func != NULL) {
			// Buffer where the function name will be stored
			char funcName[MAXSTR];
			if (get_func_name(func->startEA, funcName, MAXSTR) != NULL) {
				
#ifdef _DEBUG
				msg("Current function %a, named %s\n", func->startEA, funcName);
#endif

				//check if we're in a new function
				if(strcmp(currFunction, funcName) !=  0){
					//currFunction = funcName;
					qstrncpy(currFunction, funcName, sizeof(funcName));
#ifdef _DEBUG
					msg("new function");
#endif

					std::string prepend = "debugaddtoroot";

					if(!trace){
						prepend = "debugaddtorootexpandcall";
					}else {
						prepend = "debugexpandcall";
					}

					//dump function
					dumpFunction(func);

					//send to Java diagram
					m_pJavaComm->send(prepend.append(functionText));
				}
			}
		} else {
			if(func == NULL && !trace && firstSuspend){
				char funcName[MAXSTR];
				char *res = get_name(BADADDR, inf.startIP, funcName, sizeof(funcName)-1);
				std::string prepend = "debugexpandcall";
				qstrncpy(lastLocFunction, funcName, MAXSTR);
				qsnprintf(tempChar, sizeof(tempChar)-1, "> -1:\t%a\t%s\t%s\n", inf.startIP, funcName, module_filename);
				functionText = "";
				functionText.append(tempChar);
				m_pJavaComm->send(prepend.append(functionText));	
				firstSuspend = false;
				return 0;
			}
		}
		
		if(trace){
			enable_step_trace();
		}
	}
	if(event_id == dbg_suspend_process){
		if(justStoppedTracing){
			justStoppedTracing = false;
			//run_to(get_screen_ea());
			continue_process();
			return 0;
		}

		//check whether we should ignore
		//if(checkLoop(get_screen_ea())){
		//	return 0;
		//}

		if(checkJump(get_screen_ea())){
			return 0;
		}

		//get current function
		if(trace){
			const debug_event_t *evt = get_debug_event();
			ea_t entryPointAddress = inf.startIP;
			ea_t thisAddress = evt->ea;
		
			if(thisAddress == entryPointAddress){

				//enable_func_trace();
				enable_step_trace();
				//enable_insn_trace();
				module_info_t minfo;

				for ( bool ok=get_first_module(&minfo); ok; ok=get_next_module(&minfo) ){
					size_t found;
					string mystring = string(minfo.name);
					found = mystring.find("\\IDA\\plugins\\");

					if (found != string::npos){
						for (int i=0; i < mystring.length(); i++){
							mystring[i] = tolower(mystring[i]);
						}

						string::size_type loc = mystring.find_last_of("\\");
						mystring = mystring.substr(loc + 1);

						size_t j = mystring.find(".dll");
						mystring = mystring.replace(j, 4, "_dll");

						ignoreList.push_back(mystring);
					}
				}
			}
		}

		ea_t addr = get_screen_ea();
		func_t *func = get_func(addr);

		if(func == NULL && !trace && firstSuspend){
			char funcName[MAXSTR];
			char *res = get_name(BADADDR, inf.startIP, funcName, sizeof(funcName)-1);
			std::string prepend = "debugexpandcall";
			qstrncpy(lastLocFunction, funcName, MAXSTR);
			qsnprintf(tempChar, sizeof(tempChar)-1, "> -1:\t%a\t%s\t%s\n", inf.startIP, funcName, module_filename);
			functionText = "";
			functionText.append(tempChar);
			m_pJavaComm->send(prepend.append(functionText));	
			firstSuspend = false;
			return 0;
		}

		firstSuspend = false;

		if (func != NULL) {

			char moduleName[256];
			segment_t *seg = getseg(addr);
			get_segm_name(seg, moduleName, sizeof(moduleName)-1);
			string mystring = string(moduleName);
			size_t found = mystring.find("_text");

			if (found != string::npos){
				// Buffer where the function name will be stored
				char funcName[MAXSTR];
				if (get_func_name(func->startEA, funcName, MAXSTR) != NULL) {

					//check if we're in a new function
					if(strcmp(currFunction, funcName) != 0){
						char buf[MAXSTR];

						lastWasLocal = true;
						
						if(checkLoop(addr)){
							return 0;
						}

						std::string prepend = "debug";
						lastStackFunction = "";
						//if(!trace){
							prepend = "debugexpandcall";
						//}

						//dump function
						dumpFunction(func);

						//send to Java diagram
						m_pJavaComm->send(prepend.append(functionText));//currFunction = funcName;
						qstrncpy(currFunction, funcName, sizeof(funcName));
						qstrncpy(lastLocFunction, funcName, MAXSTR);
					}
				}
			} else {
				const debug_event_t *evt = get_debug_event();
				logExternal(addr, true, false, evt, true);
			}
		} else {
			const debug_event_t *evt = get_debug_event();
			logExternal(addr, true, false, evt, true);
		}
	} 
	if(event_id == dbg_trace){
		regval_t esp, eip;

		// Get ESP register value
		get_reg_val("esp", &esp);
		// Get EIP register value
		get_reg_val("eip", &eip);

		if(checkJump(eip.ival)){
			return 0;
		}

		if(eip.ival != BADADDR){
			//if (eip.ival > start_ea && eip.ival < end_ea){
			//msg("EIP = %a\n", eip.ival);
			func_t *func = get_func(eip.ival);
			if (func != NULL) {
				// Buffer where the function name will be stored
				char funcName[MAXSTR];

				char moduleName[256];
				segment_t *seg = getseg(eip.ival);
				get_segm_name(seg, moduleName, sizeof(moduleName)-1);
				string mystring = string(moduleName);
				size_t found = mystring.find("_text");

				if (found != string::npos){
					if (get_func_name(func->startEA, funcName, MAXSTR) != NULL) {
						//check if we're in a new function

						if(strcmp(currFunction, funcName) != 0){
							lastWasLocal = true;
							qstrncpy(currFunction, funcName, sizeof(funcName));
							qstrncpy(lastLocFunction, funcName, MAXSTR);

							//use add to root only after a continue with no step tracing
							//std::string prepend = "debug!jeb!add to root!jeb!";
							if(checkLoop(eip.ival)){
								return 0;
							}

							std::string prepend = "debug";
							lastStackFunction = "";
							//dump function
							dumpFunction(func);

							//send to Java diagram
							m_pJavaComm->send(prepend.append(functionText));
						}
					}
				} else {
					const debug_event_t *evt = get_debug_event();
					logExternal(eip.ival, false, false, evt, false);
				}
			} else {
				//external function
				const debug_event_t *evt = get_debug_event();
				logExternal(eip.ival, false, false, evt, false);
			}
		} 
	}
	if(event_id == dbg_process_start){

		m_debugging = true;
		firstSuspend = true;

		if(trace){
		//	enable_func_trace();
			msg("Sending trace data to AVA, this may take a few moments, please be patient...");
		}
	}
	if(event_id == dbg_process_exit){
		
		//request_enable_step_trace(false);
		if(trace){
			//enable_func_trace(false);
			enable_step_trace(false);
			//enable_insn_trace(false);
		}
		
		m_debugging = false;

		//currFunction = "";

		// Loop through all trace events
		/*for (int i = 0; i < get_tev_qty(); i++) {
			tev_info_t tev;
			// Get the trace event information
			get_tev_info(i, &tev);
			// Display the address the event took place
			msg("Trace event occurred at %a\n", tev.ea);
		}*/
	}
	if(event_id == dbg_run_to){
		//command
		//address
		ea_t addr = va_arg(va, ea_t);
		regval_t esp, eip;

		// Get ESP register value
		get_reg_val("esp", &esp);
		// Get EIP register value
		get_reg_val("eip", &eip);

		//stateMessages += "runto " + addr + " \n";
		qsnprintf(tempChar, sizeof(tempChar)-1, "runto\t%a\n", eip.ival);
		stateMessages.append(tempChar);
		msg(tempChar);
	}
	return 0;
}

boolean CIDAModule::checkJump(ea_t addr){
	ua_ana0(addr);
	
	if ( cmd.itype == NN_ja
		|| cmd.itype == NN_jae
		|| cmd.itype == NN_jb
		|| cmd.itype == NN_jbe
		|| cmd.itype == NN_jc
		|| cmd.itype == NN_jcxz
		|| cmd.itype == NN_jecxz
		|| cmd.itype == NN_jrcxz
		|| cmd.itype == NN_je
		|| cmd.itype == NN_jg
		|| cmd.itype == NN_jge
		|| cmd.itype == NN_jl
		|| cmd.itype == NN_jle
		|| cmd.itype == NN_jna
		|| cmd.itype == NN_jnae
		|| cmd.itype == NN_jnb
		|| cmd.itype == NN_jnbe
		|| cmd.itype == NN_jnc
		|| cmd.itype == NN_jne
		|| cmd.itype == NN_jng
		|| cmd.itype == NN_jnge
		|| cmd.itype == NN_jnl
		|| cmd.itype == NN_jnle
		|| cmd.itype == NN_jno
		|| cmd.itype == NN_jnp
		|| cmd.itype == NN_jns
		|| cmd.itype == NN_jnz
		|| cmd.itype == NN_jo
		|| cmd.itype == NN_jp
		|| cmd.itype == NN_jpe
		|| cmd.itype == NN_jpo
		|| cmd.itype == NN_js
		|| cmd.itype == NN_jz
		|| cmd.itype == NN_jmp
		|| cmd.itype == NN_jmpfi
		|| cmd.itype == NN_jmpni
		|| cmd.itype == NN_jmpshort){


		if(lastJumpAddr == cmd.Operands[0].addr){
			if(jumpCount > prefCount){
				return false;
			}
			
			if(jumpCount == prefCount){
				std::string prepend = "innerloop ";

				func_t* currFunc = get_func(lastJumpAddr);
				func_t* thisFunc = get_func(lastJumpAddr);

				if(currFunc != NULL && thisFunc != NULL){
					char funcName[MAXSTR];
					get_name(BADADDR, lastJumpAddr, funcName, sizeof(funcName)-1);

					if(strcmp(funcName, "") == 0){
						return false;
					}

					char funcName2[MAXSTR];
					get_name(BADADDR, addr, funcName2, sizeof(funcName2)-1);

					if(strcmp(funcName, funcName2) == 0){
						if(trace){
							justStoppedTracing = true;
							enable_step_trace(false);
							//continue_process();
							//run_to_(sectolast);	
						}

						qsnprintf(tempChar, sizeof(tempChar)-1, "%s %s %s\n", funcName, module_filename, module_filename);
						prepend.append(tempChar);
						m_pJavaComm->send(prepend);
						jumpCount++;

						return true;
					}
				} else {
					//external or stack
					char name[200];
					get_name(BADADDR, lastJumpAddr, name, sizeof(name)-1);
					
					if(strcmp(name, "") == 0){
						return false;
					}
					char name2[200];
					get_name(BADADDR, addr, name2, sizeof(name2)-1);
					string nametwo = string(name2);
					
					if(strcmp(name, name2) == 0 || nametwo == ""){
						if(trace){
							justStoppedTracing = true;
							enable_step_trace(false);
							//continue_process();
							//run_to_(sectolast);	
						}

						qsnprintf(tempChar, sizeof(tempChar)-1, "%s %s %s\n", name, module_filename, module_filename);
						prepend.append(tempChar);
						m_pJavaComm->send(prepend);	
						jumpCount++;
						return true;
					}
				}
			}
			jumpCount++;
		} else {
			jumpCount = 0;
			lastJumpAddr = cmd.Operands[0].addr;
		}
	} 
	return false;
}

boolean CIDAModule::checkLoop(ea_t addr){

		//check whether we should ignore
		if(noLoop){

			ea_t expectAddr = stopLoopAddrs[loopCount];

			//need to loop back to start
			if(expectAddr == 0 || expectAddr == BADADDR){
				loopCount = 0;
				expectAddr = stopLoopAddrs[loopCount];
			}

			if(addr == expectAddr){
				//then we're still in the loop
				loopCount++;
				return true;
			} else {
				//not in the loop
				loopCount = 0;
				noLoop = false;
			}
		}
		return false;
}

void CIDAModule::logExternal(ea_t addr, bool expand, bool addtoroot, const debug_event_t *evt, bool makename){
	//if(traceInnerCalls || lastWasLocal){
		char name[200];
		char *res = get_name(BADADDR, addr, name, sizeof(name)-1);
		char moduleName[MAXSTR];
		segment_t *seg = getseg(addr);
		get_segm_name(seg, moduleName, sizeof(moduleName)-1);
		bool exists = false;
		functionText = "";

		/*if(res == NULL){
			msg("at address %a\n", addr);
			exists = get_debug_name(&addr, DEBNAME_EXACT, name, sizeof(name)-1);
			msg("exists? %d\n", exists);
			msg("debug name %s\n", name);
		}*/

		if (res != NULL || exists) {
			string namestring(name, 200);

			//need to split name by first _ into the import and func name
			string::size_type pos = namestring.find_first_of("_");
			string import = namestring.substr(0, pos);
			string fname = namestring.substr(pos + 1, 200);
					
			if(fname != "" && import != ""){// && import != "unk"){

				if(strcmp(currFunction, namestring.c_str()) != 0){
					//msg("name %s\n", name);
					//msg("segment module name %s\n", moduleName);
					//msg("eid %i pid %i tid %i \n", evt->eid, evt->pid, evt->tid);

					//currFunction = fname.c_str();
					
					if(import == "loc" || import == "locret" || import == "unk"){
						//need to use last known function, loc is part of the function name
						//if(import == "loc")
						//	fname = "loc_" + fname;
						//else 
						//	fname = "locret_" + fname;
						if(import == "unk"){
							import = "loc";
						}
						fname = import + "_" + fname;
					}/* else {
						msg(import.c_str());
						//lastKnownGoodModule = import;
						lastKnownGoodModule = new char[import.length() + 1];
						//qstrncpy(lastKnownGoodModule, import.c_str(), MAX_FILENAME_LENGTH + sizeof(import));
						qstrncpy(lastKnownGoodModule, import.c_str(), sizeof(import)); // No name
						msg(lastKnownGoodModule);
					}*/

					std::string prepend = "debug";

					if(expand){
						prepend = "debugexpandcall";
					}
					if(addtoroot){
						prepend = "debugexpandcalladdtoroot";
					}
					functionText = "";
					size_t found;
					string mystring = string(moduleName);
					found = mystring.find("Stack_");
					if (found != string::npos){
						lastStackFunction = mystring;
					} else {
						lastStackFunction = "";
					}
					size_t foundText = mystring.find("_text");
					
					if (found != string::npos || foundText != string::npos){
						//msg("%d %d\n", lastFunctionEA, addr);
						//msg("was stack or text %s\n", mystring.c_str());
						if(lastFunctionEA != addr){

							//loop logic

							if(checkLoop(addr)){
								return;
							}

							qsnprintf(tempChar, sizeof(tempChar)-1, "> -1:\t%a\t%s\t%s\n", addr, fname.c_str(), module_filename);
							functionText.append(tempChar);
							m_pJavaComm->send(prepend.append(functionText));
							qstrncpy(lastFunction, currFunction, MAXSTR);
							qstrncpy(currFunction, namestring.c_str(), MAXSTR);

							found = mystring.find("Stack_");
							if (found != string::npos){
								//msg("stack putting in lastLocFunction %s \n", fname.c_str());
								qstrncpy(lastLocFunction, fname.c_str(), MAXSTR);
							}
							
							lastWasLocal = true;
							//}
						} 
					} else {
						if(traceInnerCalls || lastWasLocal){
							//msg("was external %s\n", mystring.c_str());
							list<string>::iterator ili;
							boolean local = true;
							string module = string(moduleName); //hidedebugger_dll
							string thisname = "";

							for(ili = ignoreList.begin(); ili != ignoreList.end(); ++ili){
								
								thisname = (string)*ili;

								if(strcmp(module.c_str(), thisname.c_str()) == 0){
									local = false;
									break;
								} 
							}

							if(local){

								if(checkLoop(addr)){
									return;
								}

								found = mystring.find("debug");
								if (found != string::npos){
									qsnprintf(tempChar, sizeof(tempChar)-1, "> -1:\t%a\t%s\t%s\t%s\n", addr, fname.c_str(), module_filename, module_filename);
								} else {
									qsnprintf(tempChar, sizeof(tempChar)-1, "> -1:\t%a\t%s\t%s\t%s\n", addr, fname.c_str(), module_filename, moduleName);
								}
								functionText.append(tempChar);
								m_pJavaComm->send(prepend.append(functionText));
								qstrncpy(currFunction, namestring.c_str(), MAXSTR);
								//msg("putting in lastLocFunction %s \n", fname.c_str());
								//qstrncpy(lastLocFunction, fname.c_str(), MAXSTR);
							}
						}
						lastWasLocal = false;
					}

					lastFunctionEA = addr;
				} else {
					//msg("last name string was equal\n");
					if(checkLoop(addr)){
						return;
					}
				}
			} 
		} else {
			size_t found;
			string mystring = string(moduleName);

			found = mystring.find("Stack_");
			if (found != string::npos){
				//msg("was stack! %s\n", mystring.c_str());
				if(strcmp(currFunction,"Stack") != 0){
					if(strcmp(lastStackFunction.c_str(), "") == 0){
						std::string prepend = "debug";
						if(expand){
							prepend = "debugexpandcall";
						}
						if(addtoroot){
							prepend = "debugexpandcalladdtoroot";
						}
						/*if(checkLoop(addr)){
							return;
						}*/

						functionText = "";
						//qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\n", 0, addr, moduleName, module_filename);
						//qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\n", 0, addr, currFunction, module_filename);
						qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\n", 0, addr, lastLocFunction, module_filename);
						functionText.append(tempChar);
						m_pJavaComm->send(prepend.append(functionText));
						qstrncpy(currFunction, "Stack", MAXSTR);
						lastWasLocal = true;

					}
				}
			}

			found = mystring.find("debug");
			if (found != string::npos){
				if(strcmp(currFunction, moduleName) != 0){
					std::string prepend = "debug";
					if(expand){
						prepend = "debugexpandcall";
					}
					if(addtoroot){
						prepend = "debugexpandcalladdtoroot";
					}
					functionText = "";
					qsnprintf(tempChar, sizeof(tempChar)-1, "> -1:\tFFFFFF\ttempname\t%s\n", module_filename);
					functionText.append(tempChar);
					m_pJavaComm->send(prepend.append(functionText));
					qstrncpy(currFunction, moduleName, MAXSTR);
				}
				lastWasLocal = true;
			}

			//need to fix
			/*if(!trace && makename){
				found = mystring.find("_text");
				if (found != string::npos){
					std::string prepend = "debug";
					if(expand){
						prepend = "debugexpandcall";
					}
					if(addtoroot){
						prepend = "debugexpandcalladdtoroot";
					}
					if(checkLoop(addr)){
							return;
						}

					functionText = "";
					string newname = "loc_";
					qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s%a\t%s\n", 0, addr, newname.c_str(), addr, module_filename);
					functionText.append(tempChar);
					m_pJavaComm->send(prepend.append(functionText));
					qsnprintf(tempChar, sizeof(tempChar)-1, "%s%a", newname.c_str(), addr);
					qstrncpy(currFunction, tempChar, MAXSTR);
				}
			}*/
		}
	//}
}

/**
 * \fn CIDAModule::onTerm()
 * \brief IDA pro - Term function
 */
void CIDAModule::onTerm()
{
    // Stuff to do when exiting, generally you'd put any sort
	// of clean-up jobs here.

	if (m_bHooked){
		CPLWInterface::getInstance().uninstallAllCallBacks(NULL);
		m_bHooked = false;
	}

    // Communication Step 7/7 - Send a "socket communication closed" message to java app.
    if (m_pJavaComm != NULL){
        m_pJavaComm->send(gBye);
        delete(m_pJavaComm);
        m_pJavaComm = NULL;
    }

	return;
}

void CIDAModule::onJavaInitialize()
{
    // Communication Step 7/7 - Send a "socket communication closed" message to java app.
    if (m_pJavaComm != NULL){
		std::string gHello = "hello ";
		
		// Get the name of the module being analyzed
		ssize_t val = get_root_filename(module_filename, MAX_FILENAME_LENGTH);
		char def_filename[MAX_FILENAME_LENGTH + 4];
		qstpncpy(def_filename, module_filename, MAX_FILENAME_LENGTH + 4);
		qstrncat(def_filename, ".ose", MAX_FILENAME_LENGTH + 4);
		std::string filename = CIdaSdk::getIDAProPath() + "/plugins/java/ava/" + def_filename;

		char *sz;
		sz = new char[filename.length() + 1];
		qstrncpy(sz, filename.c_str(), MAX_FILENAME_LENGTH + sizeof(filename));
		
		char newfilename[MAXSTR];
//		newfilename = filename.append(".ose");
		qstpncpy(newfilename, filename.c_str(), MAXSTR);
		qstrncat(newfilename, ".ose", MAXSTR);

		SequenceDumper *mysequencedumper = new SequenceDumper(sz);
		mysequencedumper->setFilename(newfilename);

		// Generate the call dump
		mysequencedumper->dump();

		delete mysequencedumper;
		
		gHello = "hello ";
		m_pJavaComm->send(gHello.append(filename + "\n").c_str());

		m_notificationSuspendedTokens--;
	}
}

/**
 * Hook to IDA events. Events will be unhook automatically when quiting.
 */
void CIDAModule::installCallBack()
{
    // Hook UI callback.
	m_bHooked = CPLWInterface::getInstance().installCallBack(HT_UI, this);
	m_bHooked = CPLWInterface::getInstance().installCallBack(HT_DBG, this);
	m_bHooked = CPLWInterface::getInstance().installCallBack(HT_IDP, this);
	// add other hook here...
}

/**
 * Give the plugin's reference to the comm class to be able to call plugin's function.
 */
CIDAModule::CComm::CComm(CIDAModule * plugin)
    : CSocketComm()
{
    m_plugin = plugin;
}

/**
 * Entry point for all message coming from the socket communication module.
 * This is the implementation of the virtual function receive.
 */
void CIDAModule::CComm::receive(const std::string &message){

#ifdef _DEBUG
		msg("%s \n",message.c_str());
#endif

    // Implement what need to be done when we receive a message from Java.
    if (strstr(message.c_str(), gUpdateJavaServerPort.c_str()) != NULL) {

        //Communication Step 5/7 - Start the cppSocketClient and connect to the java server on the port we just received.
        // The substr(20) is to remove the "updateJavaServerPort" in the message received and keep only the port.
        setJavaServerPort((unsigned short)strtoul(message.substr(20).c_str(), NULL, 10));
	    initClient();

		//As we received new from the java side, we presume java app is loaded.
		m_plugin->setUILoaded(true);

        //Hook to IDA events from now on.
		m_plugin->installCallBack();

		//sending first ack message to unblock the java server.\n");
		std::string gHello = "ack\n";
		//std::string filename = CIdaSdk::getIDADisassembledFileName();

		m_plugin->m_pJavaComm->send(gHello);
		//m_plugin->m_pJavaComm->send(filename + "\n");

		m_plugin->onJavaInitialize();

		
    }else if(message.compare("bye") == 0) {

		//Close socket communication.
        closeAndDeleteComm();

        // Tell user java app has been closed and how it could be restarted.
        msg(gJavaAppClosed.c_str());

        m_plugin->setUILoaded(false);

	}else if (strstr(message.c_str(), gUpdateCursor.c_str()) != NULL) {
		//check module id
		/*char *tmp = strdup(message.c_str());
		char * pch = pch = strtok(tmp," ");
		char * module;

		while (pch != NULL){
			pch = strtok (NULL, " ");

			if(pch != NULL){
				module = strdup(pch);
			}
		}*/

		//if(strcmp(module, module_filename) == 0){
			m_plugin->updateIDACursorPosition(message.c_str()+(gUpdateCursor.length())+1);
		//}
	}else if (strstr(message.c_str(), gNavigateFunction.c_str()) != NULL) {
		//check module id
		/*char *tmp = strdup(message.c_str());
		char * pch = pch = strtok(tmp," ");
		char * module;

		while (pch != NULL){
			pch = strtok (NULL, " ");

			if(pch != NULL){
				module = strdup(pch);
			}
		}*/

		//if(strcmp(module, module_filename) == 0){
			m_plugin->navigateIDAFunction(message.c_str()+(gNavigateFunction.length())+1);
		//}
	}else if (strstr(message.c_str(), gSetBreakpoint.c_str()) != NULL) {
		//check module id
		/*char *tmp = strdup(message.c_str());
		char * pch = pch = strtok(tmp," ");
		char * module;

		while (pch != NULL){
			pch = strtok (NULL, " ");

			if(pch != NULL){
				module = strdup(pch);
			}
		}*/

		//if(strcmp(module, module_filename) == 0){
			// get cursor position for function
			const char * functionAddr = message.c_str()+(gUpdateCursor.length())+1;
			ea_t requestedCursorPosition = 0;
			requestedCursorPosition = strtoul(functionAddr, NULL, 10);
			
			// Add a software breakpoint at the cursor position
			if (add_bpt(requestedCursorPosition, 0, BPT_SOFT)){

			}
		//}
	}else if (strstr(message.c_str(), gRetrieveInfo.c_str()) != NULL) {
		//check module id
		/*char *tmp = strdup(message.c_str());
		char * pch = pch = strtok(tmp," ");
		char * module;

		while (pch != NULL){
			pch = strtok (NULL, " ");

			if(pch != NULL){
				module = strdup(pch);
			}
		}*/

		//if(strcmp(module, module_filename) == 0){
			const char * functionAddr = message.c_str()+(gRetrieveInfo.length())+1;
			ea_t requestedCursorPosition = 0;
			requestedCursorPosition = strtoul(functionAddr, NULL, 10);
			std::string prepend = "response!jeb!";
			func_t *func = get_func(requestedCursorPosition);

			if(func != NULL){
				m_plugin->dumpEntireFunction(func);
			
				//send functionText
				m_plugin->m_pJavaComm->send(prepend.append(m_plugin->functionText));
			} else {
				//need to send a response to unblock
				std::string text = "nofunc";
				m_plugin->m_pJavaComm->send(prepend.append(text));
			}
		//}
	}else if (strstr(message.c_str(), gRetrieveEntryInfo.c_str()) != NULL){
		//check module id
		/*char *tmp = strdup(message.c_str());
		char * pch = pch = strtok(tmp," ");
		char * module;

		while (pch != NULL){
			pch = strtok (NULL, " ");

			if(pch != NULL){
				module = strdup(pch);
			}
		}*/

		//if(strcmp(module, module_filename) == 0){
			const char * functionAddr = message.c_str()+(gRetrieveInfo.length())+1;
			ea_t requestedCursorPosition = 0;
			requestedCursorPosition = strtoul(functionAddr, NULL, 10);
			std::string prepend = "response!jeb!root!jeb!";
			char funcName[MAXSTR];
			char tempChar[MAXSTR];
			get_func_name(requestedCursorPosition, funcName, MAXSTR);

			if(funcName != NULL){		
				//send functionText
				qsnprintf(tempChar, sizeof(tempChar)-1, "%s%s\n", prepend, funcName);
				//msg(tempChar);
				m_plugin->m_pJavaComm->send(tempChar);
			} else {
				//need to send a response to unblock
				std::string text = "nofunc";
				m_plugin->m_pJavaComm->send(prepend.append(text));
			}
		//}
	}else if (strstr(message.c_str(), gEnableTracing.c_str()) != NULL) {

		set_debugger_options(DOPT_ENTRY_BPT);
		trace = true;
		// Enable step tracing
		//request_enable_func_trace();
		request_enable_step_trace();
		//request_enable_insn_trace();
	
		// Run queued requests
		run_requests();
	}else if (strstr(message.c_str(), gDisableTracing.c_str()) != NULL) {
	
		set_debugger_options(NULL);
		trace = false;
		// Enable step tracing
		//request_enable_func_trace(false);
		request_enable_step_trace(false);
		//request_enable_insn_trace(false);
	
		// Run queued requests
		run_requests();
	}else if (strstr(message.c_str(), gDebugTraceStart.c_str()) != NULL) {
		// Run to the binary entry point
		request_run_to(inf.startIP);

		// Enable step tracing
		//request_enable_func_trace();
		request_enable_step_trace();
		//request_enable_insn_trace();
	
		// Run queued requests
		run_requests();
	}else if (strstr(message.c_str(), gEnableInner.c_str()) != NULL) {
		traceInnerCalls = true;
	}else if (strstr(message.c_str(), gDisableInner.c_str()) != NULL) {
		traceInnerCalls = false;
	}else if (strstr(message.c_str(), gPrefCount.c_str()) != NULL) {
		prefCount = (int)strtoul(message.substr(10).c_str(), NULL, 10);
	}else if (strstr(message.c_str(), gStopLoop.c_str()) != NULL) {
		//get addresses
		char *tmp = strdup(message.c_str());
		char *pch = strtok(tmp," ");
		char *module;

		noLoop = true;
		loopIndex = 0;

		//stopLoopAddr.clear();
		int index = 0;
		ea_t addr = 0;

		while (pch != NULL){
			pch = strtok (NULL, " ");
			//msg("%s\n",pch);

			if(pch != NULL){
				//stopLoopAddr.push_back(pch);
				
				addr = strtoul(pch, NULL, 16);
				stopLoopAddrs[index] = addr;
			}
			index++;
		}

		stopLoopAddrs[index] = BADADDR;

		//ea_t sectolast = stopLoopAddrs[index - 2] + 1;
		//msg("%a\n",sectolast);
		//if (add_bpt(sectolast + 1, 0, BPT_SOFT)){
		//	msg("breakpoint set\n");
		//}

		//TODO enable_step_trace stops execution
		if(trace){
			//msg("setting step tracing to false \n");
			justStoppedTracing = true;
			enable_step_trace(false);
			//continue_process();
			//run_to_(sectolast);	
		}
		
		
		//request_run_to(sectolast);
		//run_requests();
	}else if (strstr(message.c_str(), gRecordStart.c_str()) != NULL) {
		//record debugging actions
		msg("record start");
		stateMessages = "";
	}else if (strstr(message.c_str(), gRecordStop.c_str()) != NULL) {
		//stop recording debugging actions and send to eclipse
		msg("record stop");
		m_plugin->m_pJavaComm->send("outputActions \n" + stateMessages + " \n");
	}else if (strstr(message.c_str(), gExecuteAction.c_str()) != NULL) {
		//execute action in message
		msg("execute action");

	}
}


/**
 * \fn initializeClassPathFromJarList() 
 * \brief Initialize classpath
 */
std::string CIDAModule::initializeClassPathFromJarList(const std::string &JAR_List){

	unsigned int cptEnd   = 0;
	unsigned int cptBegin = 0;

    //Get IDA Pro's installed directory:
    m_IDAPath = CIdaSdk::getIDAProPath();

	std::string classpath = "\""; // Enclose each path element between '"' in case there is a space in the path.
	classpath.append(m_IDAPath);

    //Add /plugin/java/ section.
    classpath.append(gJAR_DEFAULT_SUBFOLDER);
	classpath.append("\"");
    classpath.operator+=(gPATH_SEPARATOR);

	cptEnd = JAR_List.find(gPATH_SEPARATOR);
	while (cptEnd < std::string::npos)
	{
		// Add first '"'
		classpath.append("\"");

		//Add IdaPro path.
		classpath.append(m_IDAPath);

        //Add /plugin/java section.
        classpath.append(gJAR_DEFAULT_SUBFOLDER);

		//Add last relative section.
		classpath.append(JAR_List.substr(cptBegin, cptEnd-cptBegin));

		// Add second '"'
		classpath.append("\";");

		//Beginning counter is initialized to end counter + 1.
		cptBegin = cptEnd+1;

		//Ending counter is intialized to the next ; position.
		cptEnd = JAR_List.find(gPATH_SEPARATOR, cptEnd+1);
	}

    // Add the last file in jar_list if not followed by ";"
	if ((cptEnd = std::string::npos) && (JAR_List.length() > cptBegin))
	{
		// Add first '"'
		classpath.append("\"");

		//Add IdaPro path.
		classpath.append(m_IDAPath);

        //Add /plugin/java section.
        classpath.append(gJAR_DEFAULT_SUBFOLDER);

		//Add last relative section.
        classpath.append(JAR_List.substr(cptBegin, JAR_List.length()));

		// Add second '"'
		classpath.append("\"");
	}

    return classpath;
}

/**
 * \fn startUI() 
 * \brief Start the java application from the command line.
 *
 * \note You need to add all jar file needed by the java app in the gClasspath.
 */
bool CIDAModule::startUI()
{
	if(!isRunning("ava.exe")){
		std::string classpath = initializeClassPathFromJarList(gClasspath);
		std::string commandLine = "";

	#if _DEBUG
		//Also show the console.
		//commandLine.append("java -ea -classpath "); 
	#else
		//Only show java app frame.
		//commandLine.append("javaw -ea -classpath "); 
	#endif

		//Send the cppServerPort to the java side when running java app.
		//commandLine.append(classpath + " " + gMainClass + " -p:" + CUtils::toString(m_pJavaComm->getCPPServerPort()));
		commandLine.append(CIdaSdk::getIDAProPath() + "\\plugins\\java\\ava\\ava.exe");
		
		LPSTR szExe = _strdup(commandLine.c_str());
		STARTUPINFO          si = { sizeof(si) };
		PROCESS_INFORMATION  pi;

		//Start the child process. 
		//To run a batch file, you must start the command interpreter; 
		//set lpApplicationName to cmd.exe and set lpCommandLine to the 
		//following arguments: /c plus the name of the batch file.
		if( !CreateProcess( NULL,   // refers to command line (next argument) when NULL
							szExe,  // Command line
							NULL,   // Process handle not inheritable
							NULL,   // Thread handle not inheritable
							FALSE,  // Set handle inheritance to FALSE
							0,      // No creation flags
							NULL,   // Use parent's environment block
							NULL,   // Use parent's starting directory 
							&si,    // Pointer to STARTUPINFO structure
							&pi )) {// Pointer to PROCESS_INFORMATION structure

			DWORD errCode = GetLastError();
			if (errCode == 2){

	#if _DEBUG
				msg( "Unable to find ava. (System err: %d).\n", errCode );
	#else
				msg( "Unable to find ava. (System err: %d).\n", errCode );
	#endif
			}
			else{
				msg( "CreateProcess failed Error:(%d)\n", errCode );
			}

			free(szExe);
			return false;
		}
		else
		{
			// Give time to the java app window to appear.
			WaitForInputIdle(pi.hProcess, INFINITE);

			free(szExe);
			CloseHandle(pi.hProcess);
			CloseHandle(pi.hThread);
		}
	} else {
		msg("AVA is already running, messaging is now enabled.\n");
	}

    return true;
}

/**
 * \fn updateCursorPosition() 
 * \brief Update IDA position. (This is a function specific to this example.)
 *
 * \param char *newPosition : The new IDA's position in decimal format.
 */
void CIDAModule::updateIDACursorPosition(const char *newPosition) {

	ea_t requestedCursorPosition = 0;
	requestedCursorPosition = strtoul(newPosition, NULL, 10);

    //If the string address received from Java conversion has been succesfully performed, we jump to the address.
	if (requestedCursorPosition != 0){

		m_notificationSuspendedTokens++;
        // Check if the address is valid.
        bool validAddress = (get_func(requestedCursorPosition) != NULL);
        if (validAddress){
		    jumpto(requestedCursorPosition);
        }
		m_notificationSuspendedTokens--;

        if (validAddress)
		    m_lastDecimalEffectiveAddress = requestedCursorPosition;
	}
}

/**
 * 
 *
 * \author Jennifer Baldwin
 */
void CIDAModule::navigateIDAFunction(const char *newPosition) {

	ea_t requestedCursorPosition = 0;
	requestedCursorPosition = strtoul(newPosition, NULL, 10);

    //If the string address received from Java conversion has been succesfully performed, we jump to the address.
	if (requestedCursorPosition != 0){

		m_notificationSuspendedTokens++;
        // Check if the address is valid.
        bool validAddress = (get_func(requestedCursorPosition) != NULL);
        if (validAddress){
		    jumpto(requestedCursorPosition);

			//JB added to see if this is how you get the function name
			func_t *func = get_func(requestedCursorPosition);
			if (func != NULL) {
				// Buffer where the function name will be stored
				char funcName[MAXSTR];
				if (get_func_name(func->startEA, funcName, MAXSTR) != NULL) {
#ifdef _DEBUG
						msg("Current function %a, named %s\n", func->startEA, funcName); 
#endif
				}
			}

			//dump_function(f) from OSE plugin
			func_t *f = get_func(requestedCursorPosition);
			dumpFunction(f);
        }
		m_notificationSuspendedTokens--;

        if (validAddress)
		    m_lastDecimalEffectiveAddress = requestedCursorPosition;
	}
}

void CIDAModule::dumpFunction(func_t *f) {
	char fname[MAXSTR];
	get_func_name(f->startEA, fname, sizeof(fname));
	int ord = get_func_num(f->startEA);
	functionText = "";
	//string externalFile = "";

	pair<string, string> imp = getImport(fname);

	char module_filename[MAX_FILENAME_LENGTH];
	ssize_t val = get_root_filename(module_filename, MAX_FILENAME_LENGTH);

	segment_t *seg = getseg(f->startEA);
	char fname2[MAXSTR];
	get_func_name(f->startEA, fname2, sizeof(fname2));

	qsnprintf(tempChar, sizeof(tempChar)-1, "");

	if(seg->type == SEG_XTRN) {
		qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\t%s\n", ord, f->startEA, fname, module_filename, imp.second.c_str());
	} else {
		qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\n", ord, f->startEA, fname, module_filename);
	}

	//old
	/*if(seg->type == SEG_CODE) {
		//msg("local");
		qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\n", ord, f->startEA, fname, module_filename);
		//msg("seg_code %s\n",tempChar.c_str());
	} else if(seg->type == SEG_XTRN) {
		//msg("external");
		qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s\t%s\n", ord, f->startEA, fname, module_filename, imp.second.c_str());
		//msg("seg_xtern %s\n",tempChar.c_str());
		//externalFile = getExternName(f->startEA);
	}*/

	//msg(tempChar);
	//string temp(fname);
	//functionText.append(temp + "\n");
	functionText.append(tempChar);

	//dumpCalls(f);
	//qsnprintf(tempChar, sizeof(tempChar)-1, "< %d\n", ord);
	//functionText.append(tempChar);
	//msg(functionText.c_str());
}

string CIDAModule::getExternName(ea_t to) {
	//char* line = (char *)qalloc(MAXSTR);
	//generate_disasm_line(to, line, MAXSTR - 1);

	char linebuf[MAXSTR];
	char *line = linebuf;
	generate_disasm_line(to, line, MAXSTR - 1);


	while(*line != 0 && *line != COLOR_ADDR)
		line++;

	if(*line == COLOR_ADDR) {
		line++;  // skip color code
#ifdef __EA64__
		line += 16;  // skip 16 digits of 64-bit address
#else
		line += 8;   // skip 8 digits of 32-bit address
#endif
		char* funcname = line;  // Beginning of function name
		while(*line != COLOR_OFF)
			line++;
		*line = '\0';  // Null-terminate the string
//		qfprintf(seq_file, "\t-1:\t%a\t%s\n", addr, funcname);
		pair<string, string> imp = getImport(funcname);

		/*std::stringstream ss;
		ss << get_func_num(to);
		string funcNum = ss.str();
		ss << from;
		string fromString = ss.str();
		ss << to;
		string toString = ss.str();*/

		if(imp.second.empty()) {
			//qfree(line);
			return "Unknown";
		} else {
			//qfree(line);
			return imp.second;
		}
	}
	return "Unknown";
}

void CIDAModule::dumpEntireFunction(func_t *f) {
	char fname[MAXSTR];
	get_func_name(f->startEA, fname, sizeof(fname));
	int ord = get_func_num(f->startEA);
	functionText = "";
	//qfprintf(seq_file, "> %d:\t%a\t%s\n", ord, f->startEA, fname);
	//functionText = "> " + ordString + ":\t" + addressString + "\t" + fname + "\n";
	qsnprintf(tempChar, sizeof(tempChar)-1, "> %d:\t%a\t%s\t%s!jeb!", ord, f->startEA, fname, module_filename);
	//string temp(fname);
	//functionText.append(temp + "\n");
	functionText.append(tempChar);
	dumpCalls(f);
	qsnprintf(tempChar, sizeof(tempChar)-1, "< %d\n", ord);
	functionText.append(tempChar);
}

void CIDAModule::dumpCalls(func_t *f) {
	ccc = 0;
	// Iterate over addresses from the start to end of the function
	for(ea_t addr = f->startEA; addr < f->endEA; addr++) {
		// Check if it's a call
		if(matchesIns(addr, callIns)) {
			// Check if the called address is stored in a register
			if(cmd.Operands[0].type == o_reg) {
				char buf[MAXSTR];
				generate_disasm_line(addr, buf, sizeof(buf)-1);
				// Get the actual address if possible statically
				ea_t calledAddr = getAddressCalled(buf);
				if(calledAddr != BADADDR) {
					dumpCall(addr, calledAddr);
				}
			} else {
				// The address isn't in a register
				// Using cross references to find the called address
				xrefblk_t xb;
				for(bool res = xb.first_from(addr, XREF_ALL); res; res = xb.next_from()) {
					if(xb.iscode && ((xb.type == fl_CF) || (xb.type == fl_CN))) {
						dumpCall(addr, xb.to);
					}
				}
			}
		}
		// Check if it's a jump
		if(matchesIns(addr, jumpIns)) {
			// Using cross references to check if the jump is to an address
			// in the data segement
			xrefblk_t xb;
			for(bool res = xb.first_from(addr, XREF_DATA); res; res = xb.next_from()) {
				segment_t *seg = getseg(xb.to);
				// The segment type SEG_XTRN is only created if the 'Create Import Segment' option
				// is selected when disassembling
				if(seg->type == SEG_XTRN) {
					dumpExternCall(addr, xb.to);
				}
			}
		}
	}
}

void CIDAModule::dumpCall(ea_t from, ea_t to) {
	segment_t *seg = getseg(to);
	if(seg->type == SEG_CODE) {
		char funcName[MAXSTR];
		if(get_func_name(to, funcName, MAXSTR) != NULL) {
			//qfprintf(seq_file, "\t%d:\t%a\t%a\t%s\n", get_func_num(to), from, to, funcName);
			qsnprintf(tempChar, sizeof(tempChar)-1, "\t%d:\t%a\t%a\t%s!jeb!", get_func_num(to), from, to, funcName);
			functionText.append(tempChar);
			//msg("\t%d:\t%a\t%a\t%s\n", get_func_num(to), from, to, funcName);
		}
	} else if(seg->type == SEG_XTRN) {
		dumpExternCall(from, to);
	}
}

void CIDAModule::dumpExternCall(ea_t from, ea_t to) {
	//char* line = (char *)qalloc(MAXSTR);
	//generate_disasm_line(to, line, MAXSTR - 1);

	char linebuf[MAXSTR];
	char *line = linebuf;
	generate_disasm_line(to, line, MAXSTR - 1);


	while(*line != 0 && *line != COLOR_ADDR)
		line++;
	if(*line == COLOR_ADDR) {
		line++;  // skip color code
#ifdef __EA64__
		line += 16;  // skip 16 digits of 64-bit address
#else
		line += 8;   // skip 8 digits of 32-bit address
#endif
		char* funcname = line;  // Beginning of function name
		while(*line != COLOR_OFF)
			line++;
		*line = '\0';  // Null-terminate the string
//		qfprintf(seq_file, "\t-1:\t%a\t%s\n", addr, funcname);
		pair<string, string> imp = getImport(funcname);

		std::stringstream ss;
		ss << get_func_num(to);
		string funcNum = ss.str();
		ss << from;
		string fromString = ss.str();
		ss << to;
		string toString = ss.str();

		if(imp.second.empty()) {
			//qfprintf(seq_file, "\t-1:\t%a\t%a\t%s\t%s\n", from, to, funcname, "Unknown");
			qsnprintf(tempChar, sizeof(tempChar)-1, "\t-1:\t%a\t%a\t%s\t%s!jeb!", from, to, funcname, "Unknown");
			functionText.append(tempChar);
			//msg("\t-1:\t%a\t%a\t%s\t%s\n", from, to, funcname, "Unknown");
		} else {
			//qfprintf(seq_file, "\t-1:\t%a\t%a\t%s\t%s\n", from, to, imp.first.c_str(), imp.second.c_str());
			qsnprintf(tempChar, sizeof(tempChar)-1, "\t-1:\t%a\t%a\t%s\t%s!jeb!", from, to, imp.first.c_str(), imp.second.c_str());
			functionText.append(tempChar);
			//msg("\t-1:\t%a\t%a\t%s\t%s\n", from, to, imp.first.c_str(), imp.second.c_str());
		}
	}
	//qfree(line);
}

ea_t CIDAModule::getAddressCalled(char* line) {
	ea_t addr = BADADDR; 
	while(*line != COLOR_ADDR && *line != 0) 
		line++;
	if(*line == COLOR_ADDR) {
		line++;
#ifdef __EA64__
#define ADDRLEN 16
#else
#define ADDRLEN 8
#endif
		char addrstr[ADDRLEN+1];
		qstrncpy(addrstr, line, sizeof(addrstr));
		str2ea(addrstr, &addr, 0);
	}
	return addr;
}

pair<string, string> CIDAModule::getImport(char* funcName) {
	string modStr = importsMap[string(funcName)];
	if(modStr.empty()) {
		funcName += 6; // skip a possible '__imp_' prefix to function name
		modStr = importsMap[string(funcName)];
	}
	return pair<string, string>(string(funcName), modStr);
}

bool CIDAModule::matchesIns(ea_t addr, vector<ushort> &ins) {
	if(ua_ana0(addr) > 0) {
		vector<ushort>::iterator insIter;
		for(insIter = ins.begin(); insIter != ins.end(); insIter++)
			if(cmd.itype == *insIter)
				return true;
	}
	return false;
}

void CIDAModule::populateImports() {
	for(uval_t idx = import_node.alt1st(); idx != BADNODE; idx = import_node.altnxt(idx)) {
		// Get the module name
		char modName[MAXSTR + 4];
		if(import_node.supstr(idx, modName, sizeof(modName)) <= 0)
			qstrncpy(modName, "Unknown", sizeof(modName)); // No name
		else
			qstrncat(modName, ".dll", MAXSTR + 4);
		netnode modNode = import_node.altval(idx);
		char nodeName[MAXNAMESIZE];
		modNode.name(nodeName, MAXNAMESIZE);

#ifdef _DEBUG
		msg("modNode idx: %x\n       name: ", idx);
#endif

		for(int i = 0; nodeName[i] != 0; i++) {
#ifdef _DEBUG
		msg("%02x ", nodeName[i]);
#endif
		}
#ifdef _DEBUG
		msg("\n");
#endif

		// For all imported by ORDINAL functions
		for(uval_t ord = modNode.alt1st(); ord != BADNODE; ord = modNode.altnxt(ord)) {
			ea_t ea = modNode.altval(ord);
			char funcName[MAXSTR];
			if(modNode.supstr(ea, funcName, sizeof(funcName)) <= 0)
				funcName[0] = '\0'; // Import by ordinal, no name
			else 
				importsMap[string(funcName)] = string(modName);

		}

		// For all imported by NAME functions
		for(ea_t ea = modNode.sup1st(); ea != BADADDR; ea = modNode.supnxt(ea)) {
			char funcName[MAXSTR];
			if(modNode.supstr(ea, funcName, sizeof(funcName)) <= 0)
				funcName[0] = '\0'; // No name
			else
				importsMap[string(funcName)] = string(modName);
		}
	}
}

void CIDAModule::dumpEntryPoints() {
	char epname[MAXSTR];
	ea_t epaddr;
	// Iterate through the entry points
	for(size_t e = 0; e < get_entry_qty(); e++) {
		epaddr = get_entry(get_entry_ordinal(e));
		get_func_name(epaddr, epname, sizeof(epname));
		//qfprintf(seq_file, ">> %d:\t%a\t%s\t%s\n", get_func_num(epaddr), epaddr, epname, filename.c_str());
	}
}

void CIDAModule::setFilename(char* fname) {
	filename = string(fname);
}

void CIDAModule::dumpLine(char *line) {
	for(int i = 0; line[i]; i++) {
		//qfprintf(seq_file, "%2x", line[i]);
#ifdef _DEBUG
		msg("%2x", line[i]);
#endif
	}
	//qfprintf(seq_file, "\n");
#ifdef _DEBUG
	msg("\n");
#endif
}

void CIDAModule::dump() {
	populateImports();
	dumpEntryPoints();
	// Iterate over every function in the module
	for(size_t i = 0; i < get_func_qty(); i++) {
		func_t *f = getn_func(i);
		dumpFunction(f);
	}
}

bool CIDAModule::isRunning(string pName)
{
	unsigned long aProcesses[1024], cbNeeded, cProcesses;
	if(!EnumProcesses(aProcesses, sizeof(aProcesses), &cbNeeded)){
		return false;
	}

	cProcesses = cbNeeded / sizeof(unsigned long);
	for(unsigned int i = 0; i < cProcesses; i++)
	{
		if(aProcesses[i] == 0)
			continue;

		HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, 0, aProcesses[i]);
		char buffer[50];
		GetModuleBaseName(hProcess, 0, buffer, 50);
		CloseHandle(hProcess);
		if(pName == string(buffer)){
			return true;
		}
	}

	return false;
}

