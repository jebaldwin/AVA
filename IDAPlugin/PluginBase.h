#pragma once
#ifndef _PluginBase_H_20080529_
#define _PluginBase_H_20080529_

//#include <windows.h>
#include <string>
#include <map>

#include "CallBack.hpp"

// IDA includes
#include <pro.h>
#include <kernwin.hpp>
//---

class CPluginBase
{
    //-- Types
private:
    class IDeletable
    {
    public:
        virtual ~IDeletable()
        {}
    };

    template< class base_call_back_type >  
    class CDeletableCallBack : public base_call_back_type
                             , public IDeletable
    {
    public:
        CDeletableCallBack( const base_call_back_type & CB )
            : base_call_back_type( CB )
        {}
    };

public:
    typedef const IDeletable *  RegisteredCallBackPtr;

protected:


    //---- cTor / dTor
protected:
    CPluginBase();

public:
    virtual ~CPluginBase();


    //---- Methods

public:
    virtual int onInit();
    virtual void onRun( int Arg );
    virtual void onTerm();

    virtual int onCallBackProcessor( void * user_data, int notification_code, va_list va );
    virtual int onCallBackUI( void * user_data, int notification_code, va_list va );
    virtual int onCallBackDebug( void * user_data, int notification_code, va_list va );
    virtual int onCallBackDatabase( void * user_data, int notification_code, va_list va );

    virtual void getIDAPluginInfo( int & FeatureFlags,
                                   std::string & Name,
                                   std::string & HotKey,
                                   std::string & Comment,
                                   std::string & Help ) const;

    const char * getName() const;

    // see kernwin.hpp : add_menu_item() in IDA SDK
    template< class    class_type, 
              typename class_method >  
    RegisteredCallBackPtr addMenuItem( class_type * Object, 
                                       class_method Method,
                                       const char * MenuPath,
                                       const char *name,
                                       const char *hotkey = NULL,
                                       int flags = SETMENU_APP ); // by default, after menupath rather than before

    template< class    class_type, 
              typename class_method,
              typename user_data >  
    RegisteredCallBackPtr addMenuItem( const idatools::CStdCallBack< class_type, class_method, user_data > & CallBack, 
                                       const char * MenuPath,
                                       const char * Name,
                                       const char * HotKey = NULL,
                                       int Flags = SETMENU_APP ); // by default, after menupath rather than before 

    bool delMenuItem( RegisteredCallBackPtr MenuItem );
    bool delMenuItem( const std::string & MenuPath );
    bool delAllMenuItems();

protected:

private:
    // note : this method could exist somewhere else, along with other utility 
    // methods
    static const std::string computeFinalMenuPath( const char * ExistingMenuPath,
                                                   const char * NewItemName );

    //---- Data members
private:

    std::map< std::string, IDeletable * > mMenuCallbacks;
};


//-----------------------------------------------------
// inline methods


/***
 * see kernwin.hpp : add_menu_item() in IDA SDK for parameter descriptions
 * (other than Object and Method describred below)
 *
 * @param Object   
 *  Any object
 *
 * @param Method
 *  Any method on that object that has the following prototype :
 *      bool method()
 *
 * @return NULL on failure, or a valid pointer to a ICallBack object on success
 * 
 * @remark Only CPluginBase should use this pointer to delete the pointed 
 *         object. This is why it is returned as a const * (later on, it may
 *         also be used as an ID to remove menu items, instead of the menu path)
 *
 * @warning Do not add menu items with a '/' or '\' in the name. It will confuse
 *          IDA when the item is removed/
 */
template< class    class_type, 
          typename class_method >  
__forceinline CPluginBase::RegisteredCallBackPtr
    CPluginBase::addMenuItem( class_type * Object, 
                              class_method Method,
                              const char * MenuPath,
                              const char * Name,
                              const char * HotKey /* = NULL */,
                              int Flags/* = SETMENU_APP*/ )
{
    return addMenuItem( idatools::make_call_back( Object, Method ), 
                        MenuPath, Name, HotKey, Flags  );
}


/***
 * see kernwin.hpp : add_menu_item() in IDA SDK for parameter descriptions
 * (other than Object and Method describred below)
 *
 * @param CallBack
 *  A properly initialized call-back object to associate to a menu item. 
 *  A copy of that object will be registered.
 *
 * @return NULL on failure, or a valid pointer to a ICallBack object on success
 * 
 * @remark Only CPluginBase should use this pointer to delete the pointed 
 *         object. This is why it is returned as a const * (later on, it may
 *         also be used as an ID to remove menu items, instead of the menu path)
 *
 * @warning Do not add menu items with a '/' or '\' in the name. It will confuse
 *          IDA when the item is removed/
 */
template< class    class_type, 
          typename class_method,
          typename user_data >  
CPluginBase::RegisteredCallBackPtr
    CPluginBase::addMenuItem( const idatools::CStdCallBack< class_type, class_method, user_data > & CallBack, 
                              const char * MenuPath,
                              const char * Name,
                              const char * HotKey /* = NULL */,
                              int Flags/* = SETMENU_APP*/ )
{
    typedef idatools::CStdCallBack< class_type, class_method, user_data >    cb_type;

    CDeletableCallBack<cb_type> * DelCB =
        new CDeletableCallBack<cb_type>( CallBack );

    if ( add_menu_item( MenuPath, Name, 
                        HotKey, Flags, 
                        &cb_type::CBFunction, 
                        (cb_type *)DelCB ) ){
        mMenuCallbacks[ computeFinalMenuPath(MenuPath, Name ) ] = DelCB;
    }else{
        delete DelCB;
        DelCB = NULL;
    }

    return DelCB;
}


#endif // include guard