#ifndef _SOCK_H_
#define _SOCK_H_

#pragma once

#include <windows.h>
#include "SocketAddress.h"

/**
* Defines a classes that wraps up the winsock API.
* These classes do not supports only TCP/IP.
*/ 

namespace openutils {

    /**
    * class CSocket
    * Wraps up a raw SOCKET
    * @since 1.0.0
    */
    class CSocket {
    private:
        SOCKET m_socket; /// SOCKET for communication
        CSocketAddress *m_clientAddr; /// Address details of this socket.
    public:
        CSocket(); /// Default constructor
        void SetSocket(SOCKET sock); /// Sets the SOCKET
        SOCKET GetSocket(); /// Gets the SOCKET //Added by Gab
        void SetClientAddr(SOCKADDR_IN addr); /// Sets address details
        void Connect() throw (CSocketException); /// Connects to a server
        void Connect(int port) throw (CSocketException);
        void Connect(const char* host_name, int port) throw (CSocketException); /// Connects to host
        CSocketAddress* GetAddress() { return m_clientAddr; } /// Returns the client address
        int Send(const char* data) throw (CSocketException); /// Writes data to the socket
        int Send(const char* data, const int nb) throw (CSocketException);
        int Read(char* buffer,int len) throw (CSocketException); /// Reads data from the socket
        void Close(); /// Closes the socket
        ~CSocket(); /// Destructor
    };
}

#endif // _SOCK_H_