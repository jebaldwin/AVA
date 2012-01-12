#ifndef _SOCKETEXCEPTION_H_
#define _SOCKETEXCEPTION_H_

#pragma once

#include <string>

/**
* Defines a classes that wraps up the winsock API.
* These classes do not supports only TCP/IP.
*/ 

namespace openutils {

    /**
    * class CSocketException
    * Thrown by all sock.h classes.
    * @since 1.0.0
    */
    class CSocketException {
    private:
        std::string m_strError; /// error message
        int m_nCode; /// Error code
    public:
        /**
        * Default constructor.
        * @param code Error code.
        * @param msg Error message
        * @since 1.0.0
        */
        CSocketException(int code,const char* msg) {
            m_nCode = code;
            m_strError = msg;
        }
        /**
        * returns the error code.
        * @return int
        * @since 1.0.0
        */
        inline int GetCode() { return m_nCode; }
        /**
        * returns the error message.
        * @return const char*
        * @since 1.0.0
        */
        inline const char* GetMessage() { return m_strError.c_str(); }
    };

}

#endif // _SOCKETEXCEPTION_H_
