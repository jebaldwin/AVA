#pragma once
#ifndef _PLWInterface_H_20080529_
#define _PLWInterface_H_20080529_

// IDA includes
#include <stdio.h>
#include <ida.hpp>
#include <loader.hpp>
//-----

#include <set>

class CPluginBase;

class CPLWInterface
{
    //---- Types
public: 

    /**
     * Scoped calls to suspendCallBackHandling() and resumeCallBackHandling().
     * Usage : Simply instanciate a CSuspendCB object to suspend callback
     * notification handling for the remaining of the scope. Callback handling
     * will resume upon exiting scope (this includes the cas where an exception
     * is thrown from that scope)
     */
    class CSuspendCB
    {
    public:
        /// Suspend one type of notification, or all types (HT_LAST)
        CSuspendCB( hook_type_t HookType = HT_LAST )
            : mSuspendedType( HookType ){
            CPLWInterface::getInstance().suspendCallBackHandling( HookType );
        }

        /// Destructor : resume suspended notifications
        ~CSuspendCB(){
            CPLWInterface::getInstance().resumeCallBackHandling( mSuspendedType );
        }

        const hook_type_t mSuspendedType;
    };

    class singleton_initializer
    {
    public:
        singleton_initializer( CPluginBase * PluginSingleton ){
            CPLWInterface::getInstance().setPluginSingleton( PluginSingleton );
        }

        /// Destructor
        ~singleton_initializer(){
            CPLWInterface::manageInstance( true );
        }
    };

    friend class singleton_initializer;

private:    // types
    typedef int (CPluginBase::* plug_in_call_back)(void *, int, va_list);

    /// Callback descriptor and infos
    struct callback_desc
    {
        callback_desc()
            : mHookFunction( NULL )
            , mPluginMethod( (plug_in_call_back)0 )
            , mInstalled()
            , mSuspended( false )
        {}

        /// CPLWInterface function called from IDA
        hook_cb_t *         mHookFunction;

        /// CPluginBase-derived class (plug-in) method, called from CPLWInterface
        plug_in_call_back   mPluginMethod;

        /// List of all callbacks installed for a given hook type with different
        /// "user data"
        std::set< void * >  mInstalled;

        /// 0 : normal callback handling  
        /// > 0 : suspended
        int                 mSuspended;
    };

    //---- cTor / dTor

private:
    CPLWInterface();

public:
    ~CPLWInterface();

    //---- Methods

public:
    static CPLWInterface & getInstance();

    plugin_t & getIDAPlugin();
    CPluginBase * getPlugin(); 

    bool installCallBack( hook_type_t HookType, void * UserData );
    void uninstallCallBack( hook_type_t HookType, void * UserData );
    void uninstallAllCallBacks( void * UserData );
    void suspendCallBackHandling( hook_type_t HookType = HT_LAST );
    void resumeCallBackHandling( hook_type_t HookType = HT_LAST );
    bool isCallBackHandlingSuspended( hook_type_t HookType = HT_LAST ) const;

private:
    void setPluginSingleton( CPluginBase * PluginSingleton );

    static CPLWInterface * manageInstance( bool Destroy );

    static int idaapi IDAP_init( void );
    static void idaapi IDAP_run( int arg );
    static void idaapi IDAP_term( void );

    template< int ida_hook_type >
    static int idaapi IDACallBack(void *user_data, int notif_code, va_list va);

    //---- Data members
private:
    CPluginBase * mPluginSingleton;

    std::string   mCommentCopy;
    std::string   mHelpCopy;
    std::string   mNameCopy;
    std::string   mHotKeyCopy;

    callback_desc mCallBacks[ HT_LAST ];
};


#endif // include guard