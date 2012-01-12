/** @file
 * @brief CStdCallBack header and implementation file.
 * @author Emmanuel Giasson
 */

#pragma once
#ifndef _CallBack_HPP_20080515_
#define _CallBack_HPP_20080515_

namespace idatools
{

/** @class CStdCallBack
 *
 * <H2>Overview</H2>
 *
 * Functor that can be used in most "stdcall" callback cases, namely
 * Win32 or IDA API, to bind the callback to an object method (and an object,
 * and optionally extra data) rather than a global function or static member
 * function. 
 *
 * Functions using callbacks typically take two arguments on top of all other
 * parameters :
 *      - callback function pointer (its prototype has to match the parameter
 *        type of the function receiving it)
 *      - "user data", so that the callback function knows the "context" of the
 *        callback. When using C callback from C++, this is often the address of
 *        an object, and the callback function simply cast the user data back 
 *        into the object pointer, and forward the callback to a method of the
 *        object. 
 *          
 * This class can only be used when the "user data" value is passed as the last
 * parameter to the callback function (but it doesn't matter which parameter it
 * is when calling the function that uses the callback).
 *
 * For example, ListView_SortItems() in Win32 API requires a callback function 
 * pointer to a function of type 
 *   int CALLBACK CompareFunc(LPARAM lParam1, LPARAM lParam2, LPARAM lParamSort); 
 *
 * The last parameter is the "user data" passed to ListView_SortItems() along 
 * with the callback function pointer and other parameters.
 *
 *
 * <H2>Usage</H2>
 *
 * In the typical usage scenario above, we first need to create a CStdCallBack
 * object. Its type is a bit awkward (make_call_back() can also be used although
 * it requires an additionnal template function to receive it and forward the
 * call to the called function). Given a class CBClass with a method callback(), 
 * the type would be :
@code
        CStdCallBack< CBClass, type of callback(), additional data type >
@endcode
 * "additional data type" is, as its name implies, the type of additional data
 * to be passed to the callback method. That method should then have the formal
 * arguments of the callback function, less the last "user data" parameter, 
 * followed by one additional argument of that type. If that is not required, 
 * "void" must be used and no additional argument is required in the method 
 * prototype.
 *
 * "type of callback()" is the awkward part of the type. It is easier to do a
 * class or local typedef. For example, let a function that wants a callback 
 * function pointer of type "bool function(int, int)", that we'd rather want to
 * call back a method of our object of class CBClass. The needed callback object
 * would be defined as 
@code
    typedef bool (CBClass::*method_ptr)( int, int );            // method pointer type
    typedef CStdCallBack< CBClass, method_ptr, void >   method_callback;    // callback object type
    method_callback MyCallBack( Object, &CBClass::method );     // callback object
    
    APIFunction( arg1, arg2, &method_callback::CBFunction, &MyCallBack );
@endcode
 *
 * where "Object" and "method" should replaced with appropriate identifiers.
 * The compiler will generate the appropriate callback function from 
 * method_callback::CBFunction function template. For now, up to 3 parameters in
 * are supported for CBFunction, not counting the "user data" parameter.
 *
 * This approach gets interesing when multiple callback use the same callback
 * object type or when several callback are registered through some API, such as :
 *
@code
    // ... in MyClass class declaration 
    typedef bool (MyClass::*reg_cb_method_ptr)( int );            
    typedef idatools::CStdCallBack< MyClass, reg_cb_method_ptr, void >   reg_method_callback
    
    std::vector< reg_method_callback >  mRegisteredCallbacks;

    ...
    bool method1( int SomeValue );
    bool method2( int SomeValue );
    bool method3( int SomeValue );


    //-------------------------

    // ... in class definition
    mRegisteredCallbacks.push_back( reg_method_callback( this, &MyClass::method1 ) );
    mRegisteredCallbacks.push_back( reg_method_callback( this, &MyClass::method2 ) );
    mRegisteredCallbacks.push_back( reg_method_callback( this, &MyClass::method3 ) );

    SomeRegistrationAPI( "foo", 1, &reg_method_callback::CBFunction, &mRegisteredCallbacks[0] );
    SomeRegistrationAPI( "bar", 63, &reg_method_callback::CBFunction, &mRegisteredCallbacks[1] );
    SomeRegistrationAPI( "woot", 433, &reg_method_callback::CBFunction, &mRegisteredCallbacks[2] );
@endcode
 *
 * make_call_back() function is handy when the callback object needs only to be 
 * created on the stack, passed on to a template function that will do 
 * something with it, and destroyed on that function return. For example :
 *
@code
   template < class callback_object_type >
   void doSomethingWithCB( callback_object_type & CBObject )
   {
        // we can do anything, such as 
        SomeRegistrationAPI( "foo", 1, &callback_object_type::CBFunction, &CBObject );
   }

   //... somewhere else in the code

   doSomethinWithCB( make_call_back( this, &MyClass::method3 ) );
@endcode
 * 
 * This removes the need for complex or ugly typedef (make_call_back takes care
 * of everything) at the expense of an additional intermediate function needed 
 * to split the static CBFunction from the callback object type, and the 
 * callback object itself.
 *
 * Functions requiring callbacks in an API can easily be wrapped once with an
 * intermediate function, making the whole callback object creation quite easy
 * (as long as we don't need to keep the callback object themselves, which would
 * require to know their type as in previous examples). 
 *
 *
 * <hr> <!--------------------------------------------------------------------->
 * @ingroup IDATools
 */

template< class    class_type, 
          typename class_method,
          typename extra_data_type >         // extra data to forward to the final callback method
struct CStdCallBack
{
    typedef CStdCallBack< class_type, 
                          class_method, 
                          extra_data_type > this_type;

    CStdCallBack( class_type * This, 
                  class_method Method,
                  extra_data_type ExtraData )  // warning : a COPY of the user data is used
        : mThis( This )
        , mMethod( Method )
        , mExtraData( ExtraData )
    {}

    /**
     * Callback without any parameter and only an application-defined value (this)
     * (and the extra data)
     */
    template< typename return_type,
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall
        CBFunction( cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( CBObject.mExtraData );
    }


    /**
     * Callback with one parameter and one application-defined value (this)
     */
    template< typename return_type,
              typename param1_type, 
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( param1_type Param1, 
                    cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( Param1, CBObject.mExtraData );
    }

    /**
     * Callback with two parameters and one application-defined value (this)
     */
    template< typename return_type,
              typename param1_type, 
              typename param2_type, 
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( param1_type Param1, param2_type Param2, 
                    cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( Param1, Param2, CBObject.mExtraData );
    }

    /**
     * Callback with three parameters and one application-defined value (this)
     */
    template< typename return_type,
              typename param1_type, 
              typename param2_type, 
              typename param3_type, 
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( param1_type Param1, param2_type Param2, param3_type Param3, 
                    cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( Param1, Param2, Param3, CBObject.mExtraData );
    }

    class_type *   mThis;
    class_method   mMethod;
    extra_data_type mExtraData;
};


/// Partial template specialization, when no additional user data is needed for the callback method
template< class    class_type, 
          typename class_method >         // real user data type used in final callback method
struct CStdCallBack< class_type, class_method, void > 
{
    typedef CStdCallBack< class_type, 
                          class_method, 
                          void > this_type;

    CStdCallBack( class_type * This, 
                  class_method Method )
        : mThis( This )
        , mMethod( Method )
    {}

    /**
     * Callback without any parameter and only an application-defined value (this)
     * (and the extra data)
     */
    template< typename return_type,
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))();
    }

    /**
     * Callback with one parameter and one application-defined value (this)
     */
    template< typename return_type,
              typename param1_type, 
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( param1_type Param1, 
                    cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( Param1 );
    }

    /**
     * Callback with two parameters and one application-defined value (this)
     */
    template< typename return_type,
              typename param1_type, 
              typename param2_type, 
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( param1_type Param1, param2_type Param2, 
                    cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( Param1, Param2 );
    }

    /**
     * Callback with three parameters and one application-defined value (this)
     */
    template< typename return_type,
              typename param1_type, 
              typename param2_type, 
              typename param3_type, 
              typename cbobject_ptr_as_custom_value_type >
	static return_type __stdcall 
        CBFunction( param1_type Param1, param2_type Param2, param3_type Param3, 
                    cbobject_ptr_as_custom_value_type CustomValue )
    {
        this_type & CBObject = *reinterpret_cast< this_type *>( CustomValue );

        return ((CBObject.mThis)->*(CBObject.mMethod))( Param1, Param2, Param3 );
    }

    class_type *     mThis;
    class_method     mMethod;
};


// Inline methods/functions
//------------------------------------------------------------------------------

/**
 * Shortcut function to easily create a call-back object (with an extra data. 
 * See also the other version without an extra data)
 * 
 * @param Object   
 *  Any object to call back
 *
 * @param Method
 *  Any method on that object, that takes the expected arguments expected by
 *  the caller (which calls the call-back function), but with the last argument
 *  (often called "user data") replaced by an additionnal argument of type 
 *  extra_data_type (must be the last argument).
 *
 * @param ExtraData
 *  Extra data to pass on to the call back function. This data is COPIED into
 *  the call-back object, unless extra_data_type is explicitely specified as a
 *  reference (note : this hasn't been tested yet. a wrapper such as boost::ref
 *  might come handy for that)
 *
 * @return The call-back object (from which the CBFunction static method can be
 *         passed on as the callback function)
 */
template< class    class_type, 
          typename class_method,
          typename extra_data_type >         // extra data to forward to the final callback method
__forceinline CStdCallBack< class_type, class_method, extra_data_type >
    make_call_back( class_type * This, 
                    class_method Method,
                    extra_data_type ExtraData )  // warning : a COPY of the user data is used
                                                 // in CStdCallBack constructor
{   
    return CStdCallBack< class_type, class_method, extra_data_type >( This, Method, ExtraData );
}

/**
 * Shortcut function to easily create a call-back object (without an extra 
 * data. See also the other version that has an extra data)
 * 
 * @param Object   
 *  Any object to call back
 *
 * @param Method
 *  Any method on that object, that takes the expected arguments expected by
 *  the caller (which calls the call-back function), lass the last argument
 *  (often called "user data").
 *
 * @return The call-back object (from which the CBFunction static method can be
 *         passed on as the callback function)
 */
template< class    class_type, 
          typename class_method >      
__forceinline CStdCallBack< class_type, class_method, void >
    make_call_back( class_type * This, 
                    class_method Method ) 
                                                
{   
    return CStdCallBack< class_type, class_method, void >( This, Method );
}


} // namespace idatools

#endif // include guard