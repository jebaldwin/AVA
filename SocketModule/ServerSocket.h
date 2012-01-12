#ifndef _SERVERSOCK_H_
#define _SERVERSOCK_H_

#pragma once

#include "SocketAddress.h"
#include "Socket.h"


/**
* Defines a classes that wraps up the winsock API.
* These classes do not supports only TCP/IP.
*/ 
namespace openutils {

    /**
    * class CServerSocket.
    * Wraps up win32 calls for creating a server.
    * @since 1.0.0
    */
    class CServerSocket {
    private:		
        SOCKET m_socket; /// listening socket
        SOCKADDR_IN m_sockAddrIn; /// default socket settings
        int m_nPort; /// server port
        int m_nQueue; /// number of clients that can be in queue waiting for acceptance.
        CSocketAddress *m_sockAddr; /// Address to which this server is attached.
        bool m_bBound; /// true if bound to port.
        bool m_bListening; /// true if listening
    public:
        CServerSocket();  /// default constructor
        CServerSocket(int port); /// overloaded constructor
        CServerSocket(int port, int queue); /// overloaded constructor
        ~CServerSocket(); /// default destructor
        void CloseSocketNextBindThenAccept(CSocketAddress *sock_addr);/// Binds the server to the given address.
        CSocket* Bind() throw (CSocketException); /// Bind a connection.
        CSocket* BindAndListen() throw (CSocketException); /// Bind and listen for a connection.
        CSocket* Accept() throw (CSocketException);/// Accepts a client connection.
        void Close(); /// Closes the Socket.	
        bool IsListening(); /// returns the listening flag

        void SetPort(int port); /// Sets the port
        void SetQueue(int q); /// Sets the queue size
        int GetPort(); /// returns the port
        int GetQueue(); /// returns the queue size
        SOCKET GetServerSocket();
        CSocketAddress* GetSocketAddress(); /// Returns the socket address
    private:
        void Init(); /// default initialization
    };

}

#endif // _SERVERSOCK_H_