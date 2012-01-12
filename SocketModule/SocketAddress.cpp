#include "SocketAddress.h"

using namespace openutils;

/**
* Default constructor
* @param host Host name.
* @param port Host port
* @since 1.0.0
*/
CSocketAddress::CSocketAddress(const char* host, int port) {
	m_sockAddrIn.sin_family = AF_INET; 
	m_sockAddrIn.sin_addr.s_addr = INADDR_ANY; // initialized only in GetSockAddrIn()
	m_sockAddrIn.sin_port = htons(port);
	m_strHostName = host;
	m_nPort = port;
}

/**
* Constructor initialized by a SOCKADDR_IN
* @param sockAddr Address information
* @since 1.0.0
*/
CSocketAddress::CSocketAddress(SOCKADDR_IN sockAddr) {
	m_sockAddrIn.sin_family = sockAddr.sin_family;
	m_sockAddrIn.sin_addr.s_addr = sockAddr.sin_addr.s_addr;
	m_sockAddrIn.sin_port = sockAddr.sin_port;
	m_strHostName = inet_ntoa(m_sockAddrIn.sin_addr);
	m_nPort = sockAddr.sin_port;;
}

/**
* Returns the IP address
* @return const char*
* @since 1.0.0
*/
const char* CSocketAddress::GetIP() {
	return (const char*)inet_ntoa(m_sockAddrIn.sin_addr);
}

/**
* Returns the official address
* @return const char*
* @since 1.0.0
*/
const char* CSocketAddress::GetName() {
	HOSTENT *lpHost = gethostbyname(GetIP());
	if(lpHost == NULL) return NULL;
	return lpHost->h_name;
}

/**
* Returns aliases
* @param vector<string>* values are returned here
* @since 1.0.0
*/
void CSocketAddress::GetAliases(std::vector<std::string>* ret) {
	HOSTENT *lpHost = gethostbyname(GetIP());
	if(lpHost == NULL) return;
	char** tmp = (char**)lpHost->h_aliases;	
	if(tmp == NULL) return;
	else {
		int i = 0;
		while(true) {
			if(tmp[i] == NULL) break;
			else ret->push_back(tmp[i]);
		}
	}
}

/**
* Returns the sockaddr_in. tries to bind with the server.
* throws CSocketException on failure.
* @return SOCKADDR_IN
* @since 1.0.0
*/
SOCKADDR_IN CSocketAddress::GetSockAddrIn() throw (CSocketException) {
	m_lpHostEnt = gethostbyname(m_strHostName.c_str());
	if (!m_lpHostEnt) {
		throw CSocketException(WSAGetLastError(), "Failed to resolve host:gethostbyname()");
	}
	m_sockAddrIn.sin_addr = *((LPIN_ADDR)*m_lpHostEnt->h_addr_list);
	return m_sockAddrIn;
}

/**
* Assignment operation
* @param addr Object is copied into this.
* @since 1.0.0
*/
void CSocketAddress::operator = (CSocketAddress addr) {
	m_sockAddrIn = addr.m_sockAddrIn;
	m_strHostName = addr.m_strHostName;
	m_nPort = addr.m_nPort;
	m_lpHostEnt = addr.m_lpHostEnt;
}

/**
* Destructor
* @since 1.0.0
*/
CSocketAddress::~CSocketAddress() {
}