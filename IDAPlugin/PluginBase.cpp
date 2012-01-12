#include "PluginBase.h"
#include "PLWInterface.h"

#include <assert.h>

using namespace std;


CPluginBase::CPluginBase(void)
{
}

CPluginBase::~CPluginBase(void)
{
}


int
    CPluginBase::onInit()
{
    return PLUGIN_SKIP;
}

void
    CPluginBase::onRun( int arg )
{
    arg;
    return;
}

void
    CPluginBase::onTerm()
{
    return;
}


/**
 * Provides all plug-in dependent information to IDA. This information can later
 * be accessed through CPluginInterface::getInstance().getIDAPlugin(). There 
 * isn't any default value, so all values should be filled properly. See IDA 
 * SDK for more information (struct plugin_t).
 *
 * @param FeatureFlags
 *  All plug-in feature flags, as defined for plugin_t::flags.
 *
 * @param Name
 *  Plug-in name, as it will appear in IDA menus.
 *
 * @param HotKey
 *  Desired hot key, as a string, such as "Alt-1".
 *  
 * @param Comment
 *  Long comment about the plugin. It could appear in the status line or as a
 *  hint.
 *
 * @param Help
 *  Multiline help about the plugin
 */
void
    CPluginBase::getIDAPluginInfo( 
        int & FeatureFlags,
        std::string & Name,
        std::string & HotKey,
        std::string & Comment,
        std::string & Help ) const
{
    FeatureFlags = 0;
    Name = "Untitled plug-in";
    HotKey = "";
    Comment = "No Comment";
    Help = "No help available";
}


int
    CPluginBase::onCallBackProcessor( void * user_data, int notification_code, va_list va )
{
    return 0;
}

int
    CPluginBase::onCallBackUI( void * user_data, int notification_code, va_list va )
{
    return 0;
}

int
    CPluginBase::onCallBackDebug( void * user_data, int notification_code, va_list va )
{
    return 0;
}


int
    CPluginBase::onCallBackDatabase( void * user_data, int notification_code, va_list va )
{
    return 0;
}


const char *
    CPluginBase::getName() const    
{
    return CPLWInterface::getInstance().getIDAPlugin().wanted_name;
}


const string
    CPluginBase::computeFinalMenuPath( const char * ExistingMenuPath,
                                       const char * NewItemName )
{
    string Existing( ExistingMenuPath );
    string::size_type Index = Existing.find_last_of( "\\/" );
    if ( Index == string::npos )
    {
        Index = 0;
    }
    else
    {
        ++Index;
    }

    return Existing.substr( 0, Index ) + NewItemName;
}

/**
 * Delete all menu items added through addMenuItem(), but not those added
 * directly through add_menu_item (IDA SDK)
 *
 * @return true if all items were properly removed, false if at least one of
 *         them could not be removed (may not exist, or name contains '/' or '\'
 *         which confuses IDA...)
 */
bool
    CPluginBase::delAllMenuItems()
{
    bool AllOk = true;

    for( map< string, IDeletable * >::iterator Iter = mMenuCallbacks.begin();
         Iter != mMenuCallbacks.end(); ++Iter ){
        delete Iter->second;
        Iter->second = NULL;
        AllOk = del_menu_item( Iter->first.c_str() ) && AllOk;
    }

    mMenuCallbacks.clear();

    return AllOk;
}

/**
 * Delete an item from IDA menu, identified by its path. 
 * See del_menu_item in kernwin.hpp (IDA SDK) for more information.
 */
bool
    CPluginBase::delMenuItem( const string & MenuPath )
{
    delete mMenuCallbacks[MenuPath];
    mMenuCallbacks[MenuPath] = NULL;
    mMenuCallbacks.erase( MenuPath );
    return del_menu_item( MenuPath.c_str() );
}


/**
 * Delete an item from IDA menu, identified by its "call back object
 * pointer", returned by addMenuItem()
 *
 * @param MenuItem 
 *  The value returned by addMenuItem() for that menu item
 *
 * @return
 *  bool if it was properly removed, false otherwise
 */
bool
    CPluginBase::delMenuItem( RegisteredCallBackPtr MenuItem )
{
    bool Ok = false;

    // search by value (instead of key as usual)
    for( map< string, IDeletable * >::iterator Iter = mMenuCallbacks.begin();
         Iter != mMenuCallbacks.end(); ++Iter )
    {   
        if ( Iter->second == MenuItem )
        {
            Ok = del_menu_item( Iter->first.c_str() );
            delete Iter->second;
            Iter->second = NULL;
            mMenuCallbacks.erase( Iter );
            break;
        }
    }
    return Ok;
}
