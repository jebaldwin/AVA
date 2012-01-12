#ifndef _WINSOCK_H_
#define _WINSOCK_H_

#pragma once

/**
* Defines a classes that wraps up the winsock API.
* These classes do not supports only TCP/IP.
*/ 

namespace openutils {

    /**
    * class CWinSock
    * Performs winsock initialization and cleanup
    * @since 1.0.0
    */
    class CWinSock {
    public:
        static void Initialize();/// WSAStartup
        static void Finalize();/// WSACleanup
    };
}

#endif // _WINSOCK_H_