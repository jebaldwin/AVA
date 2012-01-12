#ifndef _SOCKADDR_H_
#define _SOCKADDR_H_

#pragma once

#include <windows.h>
#include <vector>

#include "SocketException.h"

//warning C4290: C++ exception specification ignored except to indicate a function is not __declspec(nothrow)
#pragma warning( disable : 4290 ) 

/**
* Defines a classes that wraps up the winsock API.
* These classes do not supports only TCP/IP.
*/ 

namespace openutils {

    /**
    * class CSocketAddress
    * Wraps up an internet address object.
    * @since 1.0.0
    */
    class CSocketAddress {
    private:
        SOCKADDR_IN m_sockAddrIn;   /// server info
        LPHOSTENT m_lpHostEnt;      /// used to obtain address by name
        std::string m_strHostName;  /// host name
        int m_nPort;                /// port 
    public:
        CSocketAddress(const char* host,int port);              /// default constructor		
        CSocketAddress(SOCKADDR_IN sockAddr);                   /// Constructor initialized by a SOCKADDR_IN
        const char* GetIP();                                    /// Returns the IP address
        const char* GetName();                                  /// Returns the official address
        int GetPort() { return m_nPort; }                       /// Returns the port
        void GetAliases(std::vector<std::string>* ret);         /// Returns aliases
        SOCKADDR_IN GetSockAddrIn() throw (CSocketException);   /// returns the sockaddr_in
        void operator = (CSocketAddress addr);                  /// Assignment operation
        ~CSocketAddress();                                      /// Destructor
    };

}

#endif // _SOCKADDR_H_