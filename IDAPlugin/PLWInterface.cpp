
#ifdef _AFXDLL
    #include <afxwin.h>
#endif

#include "PLWInterface.h"
#include "PluginBase.h"

#include <idp.hpp>

#include <assert.h>

using namespace std;


plugin_t PLUGIN;

#ifdef _AFXDLL
    CWinApp NeededForMFCApp;
#else
    #define AFX_MANAGE_STATE( p )   ;
#endif


CPLWInterface::CPLWInterface(void)
    : mPluginSingleton( NULL )
{
    // Initialize call backs function (IDA side) and methods (plug-in side)
    // pointers

    mCallBacks[HT_IDP].mHookFunction = IDACallBack<HT_IDP>;
    mCallBacks[HT_UI].mHookFunction  = IDACallBack<HT_UI>;
    mCallBacks[HT_DBG].mHookFunction = IDACallBack<HT_DBG>;
    mCallBacks[HT_IDB].mHookFunction = IDACallBack<HT_IDB>;

    mCallBacks[HT_IDP].mPluginMethod = &CPluginBase::onCallBackProcessor;
    mCallBacks[HT_UI].mPluginMethod  = &CPluginBase::onCallBackUI;
    mCallBacks[HT_DBG].mPluginMethod = &CPluginBase::onCallBackDebug;
    mCallBacks[HT_IDB].mPluginMethod = &CPluginBase::onCallBackDatabase;
}

CPLWInterface::~CPLWInterface(void)
{
    // safety clean up
    uninstallAllCallBacks( NULL );

    delete mPluginSingleton;
    mPluginSingleton = NULL;
}


CPLWInterface & 
    CPLWInterface::getInstance()
{
    return *manageInstance( false );
}

plugin_t &
     CPLWInterface::getIDAPlugin() 
{
    return PLUGIN;
}

/**
 * Create, reuse or delete the singleton. 
 *
 * @param Destroy
 *  true to delete the singleton, false to create or reuse it.
 */
// static
CPLWInterface *
    CPLWInterface::manageInstance( bool Destroy ) 
{
    static CPLWInterface  * SingleInstance = NULL;  

    if ( Destroy )
    {
        delete SingleInstance;
        SingleInstance = NULL;
    }
    else
    {
        if ( SingleInstance == NULL )
        {
            SingleInstance = new CPLWInterface;
        }
    }

    return SingleInstance;
}


void
    CPLWInterface::setPluginSingleton( CPluginBase * PluginSingleton )
{
    delete mPluginSingleton;
    mPluginSingleton = PluginSingleton;

    int PluginFeatures = 0;

    if ( mPluginSingleton != NULL )
    {
        mPluginSingleton->getIDAPluginInfo( PluginFeatures,
                                            mNameCopy,
                                            mHotKeyCopy,
                                            mCommentCopy,
                                            mHelpCopy );
    }
    else
    {
        mCommentCopy = "No plug-in was loaded";
        mHelpCopy = "No help available";
        mNameCopy = "Unknown plug-in";
        mHotKeyCopy = "";
    }

    PLUGIN.version = IDP_INTERFACE_VERSION;
    PLUGIN.flags = PluginFeatures;
    PLUGIN.init = IDAP_init;
    PLUGIN.term = IDAP_term;
    PLUGIN.run = IDAP_run;
    PLUGIN.comment = const_cast< char * >( mCommentCopy.c_str() );
    PLUGIN.help = const_cast< char * >( mHelpCopy.c_str() );
    PLUGIN.wanted_name = const_cast< char * >( mNameCopy.c_str() );
    PLUGIN.wanted_hotkey = const_cast< char * >( mHotKeyCopy.c_str() );
}

int 
    CPLWInterface::IDAP_init( void )
{
    AFX_MANAGE_STATE( AfxGetStaticModuleState() )

    CPluginBase * Plugin = getInstance().mPluginSingleton;

    if ( Plugin != NULL ) 
    {
        return Plugin->onInit();
    }
    else
    {
        return PLUGIN_SKIP;
    }
}

void 
    CPLWInterface::IDAP_run( int arg )
{
    AFX_MANAGE_STATE( AfxGetStaticModuleState() )

    CPluginBase * Plugin = getInstance().mPluginSingleton;

    if ( Plugin != NULL ) 
    {
        Plugin->onRun( arg );
    }
}

void 
    CPLWInterface::IDAP_term( void )
{
    AFX_MANAGE_STATE( AfxGetStaticModuleState() )

    CPluginBase * Plugin = getInstance().mPluginSingleton;

    if ( Plugin != NULL ) 
    {
        Plugin->onTerm();
    }
}


/**
 * Install a plug-in call-back for a given hook type (see 
 * hook_to_notification_point in the eIDA SDK documentation). The callback 
 * function is provided by CPLWInterface and will redirect the call to one
 * of the onCallBack methods of the plug-in. Overload these methods to process
 * IDA notifications. Several callbacks can be installed on the same hook type,
 * provided they have their own unique UserData value. 
 *
 * @param HookType 
 *  One of the predefined IDA hook types (HT_IDP, HT_UI, HT_DBG, HT_IDB)
 *
 * @param UserData
 *  Custom value. It will be passed back to the callback function on each call.
 *  If only one callback is installed for any given type, a unique address (such
 *  as the adress of a plug-in singleton) is typically used. 
 */
bool
    CPLWInterface::installCallBack( hook_type_t HookType,
                                       void * UserData )
{
    assert( HookType >= 0 && HookType < HT_LAST );

    bool Success = hook_to_notification_point( HookType,
                                               mCallBacks[ HookType ].mHookFunction,
                                               UserData );

    if ( Success )
    {
        mCallBacks[ HookType ].mInstalled.insert( UserData );
    }

    return Success;
}

/**
 * Uninstall a previouly installed call-back (via installCallBack only).
 *
 * @param HookType 
 *  One of the predefined IDA hook types (HT_IDP, HT_UI, HT_DBG, HT_IDB)
 *
 * @param UserData
 *  The same value that was provided to installCallBack(), or NULL to 
 *  uninstall all callbacks for the given type.
 *
 * @warning 
 *  A value of NULL (0) for UserData will uninstall ALL callbacks of the given
 *  type. This is an IDA behaviour. Therefore, a callback installed with 0 as 
 *  its UserData can not be uninstalled separately. 
 */
void
    CPLWInterface::uninstallCallBack( hook_type_t HookType,
                                         void * UserData )
{
    assert( HookType >= 0 && HookType < HT_LAST );

    set< void * > & CallBacks = mCallBacks[ HookType ].mInstalled;
    if ( UserData == NULL || CallBacks.find( UserData ) != CallBacks.end() )
    {
        // mimic IDA behaviour
        if ( UserData == NULL )
        {
            CallBacks.clear();
        }
        else
        {
            CallBacks.erase( UserData );
        }

        unhook_from_notification_point( HookType, 
                                        mCallBacks[ HookType ].mHookFunction,
                                        UserData );
    }
}

/**
 * Uninstall all call-back callback installed (via installCallBack only) with 
 * the same UserData value.
 *
 * @param UserData
 *  The same value that was provided to installCallBack(), or NULL to 
 *  uninstall all callbacks for all types.
 *
 * @warning 
 *  A value of NULL (0) for UserData will uninstall ALL callbacks of the given
 *  type. This is an IDA behaviour. Therefore, a callback installed with 0 as 
 *  its UserData can not be uninstalled separately. 
 */
void
    CPLWInterface::uninstallAllCallBacks( void * UserData )
{
    for( int i = 0; i < HT_LAST; ++i )
    {
        uninstallCallBack( hook_type_t(i), UserData );
    }
}


/**
 * Suspend callback handling for a given hook type, or for all types. 
 * resumeCallBackHandling() must be called as many times as 
 * suspendCallBackHandling() for a given hook type to resume callback handling.
 * It is often easier to use a CSuspendCB object. 
 * 
 * @param HookType
 *  Hook type to suspend callback handling of, or HT_LAST for all types
 */
void
    CPLWInterface::suspendCallBackHandling( 
        hook_type_t HookType /* = HT_LAST*/ )
{
    if ( HookType == HT_LAST )
    {
        for( size_t i = 0; i < HT_LAST; ++i )
        {
            ++mCallBacks[i].mSuspended;
        }
    }
    else
    {
        ++mCallBacks[ HookType ].mSuspended;
    }
}


/**
 * Resume callback handling for a given hook type, or for all types. 
 * resumeCallBackHandling() must be called as many times as 
 * suspendCallBackHandling() for a given hook type to resume callback handling.
 * It is often easier to use a CSuspendCB object. 
 * 
 * @param HookType
 *  Hook type to resume callback handling of, or HT_LAST for all types
 */
void
    CPLWInterface::resumeCallBackHandling( 
        hook_type_t HookType /* = HT_LAST*/ )
{
    if ( HookType == HT_LAST )
    {
        for( size_t i = 0; i < HT_LAST; ++i )
        {
            --mCallBacks[i].mSuspended;
            assert( mCallBacks[i].mSuspended >= 0 );
        }
    }
    else
    {
        --mCallBacks[ HookType ].mSuspended;
        assert( mCallBacks[ HookType ].mSuspended >= 0 );
    }
}


/** 
 * @return true if callback associated with HookType is suspended. If HookType
 *         is HT_LAST, it returns true if and only if ALL callbacks are 
 *         suspended.
 */
bool
    CPLWInterface::isCallBackHandlingSuspended( 
        hook_type_t HookType /* = HT_LAST*/ ) const
{
    bool Suspended = true;

    if ( HookType == HT_LAST )
    {
        for( int i = 0; i < HT_LAST; ++i )
        {
            if ( mCallBacks[ i ].mSuspended == 0 )
            {
                Suspended = false;
                break;
            }
        }
    }
    else
    {
        Suspended = mCallBacks[ HookType ].mSuspended > 0; 
    }

    return Suspended;
}


/** 
 * IDA callback functions. One function is instanciated for each callback type.
 * It forwards the call to the appropriate plug-in virtual method, if and only
 * if there is such a plug-in and callback handling is not suspended.
 */
template< int ida_hook_type >
int idaapi 
    CPLWInterface::IDACallBack(void *user_data, int notif_code, va_list va )
{
    int ReturnValue = 0;

    if ( !getInstance().isCallBackHandlingSuspended( (hook_type_t)ida_hook_type ) )
    {
        CPluginBase * Plugin = getInstance().mPluginSingleton;

        if ( Plugin != NULL ) 
        {
            AFX_MANAGE_STATE( AfxGetStaticModuleState() )

            plug_in_call_back   CBMethod = 
                getInstance().mCallBacks[ ida_hook_type ].mPluginMethod;

            ReturnValue = (Plugin->*CBMethod)( user_data, notif_code, va );
        }
    }

    return ReturnValue;
}