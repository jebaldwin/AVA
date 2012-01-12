#include "WinSock.h"

#include <windows.h> 

using namespace openutils;

/**
* Defines a classes that wraps up the winsock API.These classes
* do not supports only TCP/IP.
*/ 

/**
* WSAStartup
* @since 1.0.0
*/
void CWinSock::Initialize() {
	WORD ver = MAKEWORD(2, 2);
	WSADATA wsadata;
	WSAStartup(ver, &wsadata);
}

/**
* WSACleanup
* @since 1.0.0
*/
void CWinSock::Finalize() {
	WSACleanup();
}
