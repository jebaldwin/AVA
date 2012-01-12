#ifndef __IDAMODULE_H__
#define __IDAMODULE_H__
///////////////////////////////////////////////////////////////////////////////

#pragma once

#include "PlugInBase.h"
#include "SocketComm.h"
#include "dbg.hpp"
#include <ida.hpp>
//#include <idp.hpp>
//#include <allins.hpp>
#include <diskio.hpp>
#include <funcs.hpp>
#include <fpro.h>
#include <ua.hpp>
#include <name.hpp>
#include <entry.hpp>
#include <vector>
#include <string>
#include <hash_map>

using namespace std;
using namespace stdext;

class CIDAModule : public CPluginBase
{
public:
    CIDAModule();
    virtual ~CIDAModule();

    virtual int  onInit();
    virtual void onRun(int Arg);
    virtual void onTerm();
    virtual void getIDAPluginInfo(int & FeatureFlags,
                                  std::string & Name,
                                  std::string & HotKey,
                                  std::string & Comment,
                                  std::string & Help ) const;
protected:
    virtual int onCallBackUI(void * user_data, int notification_code, va_list va);
	virtual int onCallBackDebug(void * user_data, int notification_code, va_list va);
	virtual int onCallBackProcessor(void * user_data, int notification_code, va_list va);


private:
  	void installCallBack();

	//Constants:
	const std::string m_LOWER_IDA_VERSION;
	const std::string m_HIGHER_IDA_VERSION;

	//IDA pro directory
    std::string m_IDAPath;
    //IDA pro version
    std::string m_currentIDAVersion;

    /**
     * This little class implements the CSocketComm's receive method because it
     * is specific to the plugin.
     */
    class CComm : public CSocketComm
    {
    private:
        CIDAModule * m_plugin;
    public:
        CComm(CIDAModule * plugin);
  	    virtual void receive(const std::string &buffer);
    };

    // Pointer to the communication module.
    CComm * m_pJavaComm;

    // Start java application
	bool startUI();

    // Prepare the command line arguments before running the java plugin.
    // The jar list contains the plugin's jar and libraries needed to run.
	std::string initializeClassPathFromJarList(const std::string &JAR_List);

    // Retrieve IDA pro path
	std::string getIDAProPath();

    //Last decimal address sent.
    unsigned long m_lastDecimalEffectiveAddress;

    // Flag telling we hooked to IDA events. (Will automatically unhook when closing plugin)
    bool m_bHooked;

	// Flag to tell if we are in a debugging state, used to seperate from  navigation events
	bool m_debugging;

    // Flag telling the plugin is running.
    bool m_pluginStarted;

    // Flag telling if the java plugin has been loaded.
    bool m_UILoaded;
    // Get/Set function for UILoaded flag
    bool getUILoaded() const {return m_UILoaded;}
    void setUILoaded(const bool loaded){m_UILoaded = loaded;}

    // Example specific functions to jump to a new position in IDA Pro.******************
    void updateIDACursorPosition(const char *newPosition);
	
	//JB added
	hash_map<string, string> importsMap;
	int iii;
	int ccc;
	string filename;
	char tempChar[MAXSTR];
	string functionText;
	void dump();
	void setFilename(char *fname);
	void CIDAModule::navigateIDAFunction(const char *newPosition);
	void dumpEntryPoints();
	bool IDAPython_Menu_Callback(void *ud);
	void dumpFunction(func_t *f);
	void dumpCalls(func_t *f);
	void dumpCall(ea_t from, ea_t to);
	void dumpLine(char* line);
	void dumpExternCall(ea_t from, ea_t to);
	void populateImports();
	void logExternal(ea_t addr);
	ea_t getAddressCalled(char* line);
	pair<string, string> getImport(char* funcName);
	bool matchesIns(ea_t addr, vector<ushort> &ins);
	bool CIDAModule::isRunning(string pName);
	vector<ushort> callIns;
	vector<ushort> jumpIns;
	void CIDAModule::onJavaInitialize();
	string CIDAModule::getExternName(ea_t to);
	boolean CIDAModule::checkLoop(ea_t addr);
	boolean CIDAModule::checkJump(ea_t addr);
	void CIDAModule::dumpEntireFunction(func_t *f);
	void CIDAModule::logExternal(ea_t addr, bool expand, bool addtoroot, const debug_event_t *evt, bool makename);


	// This is used to suspend event notification when we receive a position from
    // the java app. This way we can jumpto() a position without caring about events
    // that will be generated. (We don't want to send the position again to the java app.)
    // If the suspend token == 0, IDA notification are allowed.
    int m_notificationSuspendedTokens;
};

///////////////////////////////////////////////////////////////////////////////
#endif // __IDAMODULE_H__
