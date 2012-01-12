#include "ServerSocket.h"

using namespace openutils;

/**
* Default constructor
* @since 1.0.0
*/
CServerSocket::CServerSocket() 
    : m_nPort(80),
	  m_nQueue(1),
      m_socket(INVALID_SOCKET)
{
	Init();
}

/**
* Overloaded constructor.
* @param port Port on which the server listens
* @since 1.0.0
*/
CServerSocket::CServerSocket(int port) 
    : m_nQueue(1),
      m_socket(INVALID_SOCKET)
{
	m_nPort = port;
	Init();	
}

/**
* Overloaded constructor.
* @param port Port on which the server listens
* @param queue Number of clients that will wait for accept()
* @since 1.0.0
*/
CServerSocket::CServerSocket(int port, int queue)
    : m_socket(INVALID_SOCKET)
{
	m_nPort = port;
	m_nQueue = queue;
	Init();	
}

/**
* Binds the server to the given address. 
* @param *sock_addr Socket address to bind to.
* @since 1.0.0
*/
void CServerSocket::CloseSocketNextBindThenAccept(CSocketAddress *sock_addr) {
	m_bBound = false;
	Close();
	m_sockAddr = sock_addr;
	Accept();
}

CSocket* CServerSocket::Bind() throw (CSocketException) {
	if(m_sockAddr != NULL) 
		m_sockAddrIn = m_sockAddr->GetSockAddrIn();

	if(!m_bBound) {	
        if (m_socket == INVALID_SOCKET) {
		    m_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        }
            // The Bind method binds the socket to the server.
		if (bind(m_socket, (LPSOCKADDR)&m_sockAddrIn, sizeof(struct sockaddr)) == SOCKET_ERROR) {
			throw CSocketException(WSAGetLastError(), "Failed to bind: Bind()");
		}
		m_bBound = true;
	}	 

	CSocket *sock = new CSocket();
	sock->SetSocket(m_socket);
	sock->SetClientAddr(m_sockAddrIn);		
	return sock;
}

CSocket* CServerSocket::BindAndListen() throw (CSocketException) {
	if(m_sockAddr != NULL) 
		m_sockAddrIn = m_sockAddr->GetSockAddrIn();

	if(!m_bBound) {	
        if (m_socket == INVALID_SOCKET) {
		    m_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        }
        // The Bind method binds the socket to the server.
		if (bind(m_socket, (LPSOCKADDR)&m_sockAddrIn, sizeof(struct sockaddr)) == SOCKET_ERROR) {
			throw CSocketException(WSAGetLastError(), "Failed to bind: BindAndListen()");
		}
		m_bBound = true;
	}	

    // The listen function places a socket in a state in which it is listening for an incoming connection.
	if (listen(m_socket, m_nQueue) == SOCKET_ERROR) {   
		throw CSocketException(WSAGetLastError(), "Failed to listen: BindAndListen()");
	}

	CSocket *sock = new CSocket();
	sock->SetSocket(m_socket);
	sock->SetClientAddr(m_sockAddrIn);		
	return sock;
}

/**
* Listens and accepts a client. Returns the accepted connection.
* @return CSocket*.
* @throw CSocketException
* @since 1.0.0
*/
CSocket* CServerSocket::Accept() throw (CSocketException) {
	if(m_sockAddr != NULL) 
		m_sockAddrIn = m_sockAddr->GetSockAddrIn();

	if(!m_bBound) {	
        if (m_socket == INVALID_SOCKET) {
		    m_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        }
        // The Bind method binds the socket to the server.
		int nret = bind(m_socket, (LPSOCKADDR)&m_sockAddrIn, sizeof(struct sockaddr));
		if (nret == SOCKET_ERROR) {
			throw CSocketException(WSAGetLastError(), "Failed to bind: Accept()");
		}
		m_bBound = true;
	}	

    // The listen function places a socket in a state in which it is listening for an incoming connection.
	if (listen(m_socket, m_nQueue) == SOCKET_ERROR) {
		throw CSocketException(WSAGetLastError(), "Failed to listen: Accept()");
	}

	SOCKET theClient = INVALID_SOCKET;
	SOCKADDR_IN clientAddr;
	int ssz = sizeof(struct sockaddr);

    // The accept function permits an incoming connection attempt on a socket.
	theClient = accept(m_socket,(LPSOCKADDR)&clientAddr, &ssz);
	if (theClient == INVALID_SOCKET) {
		throw CSocketException(WSAGetLastError(), "Invalid client socket: Accept()");		
	}

	CSocket *sockClient = new CSocket();
	sockClient->SetSocket(theClient);
	sockClient->SetClientAddr(clientAddr);		
	return sockClient;
}


/**
* Closes the socket.
* @throw CSocketException
* @since 1.0.0
*/
void CServerSocket::Close() {
	closesocket(m_socket);
	m_sockAddr = NULL;	
	m_bBound = false;
	m_bListening = false;
}


/**
* default initialization
* @throw CSocketException
* @since 1.0.0
*/
void CServerSocket::Init() {
	m_sockAddrIn.sin_family = AF_INET;
	m_sockAddrIn.sin_addr.s_addr = INADDR_ANY;	
	m_sockAddrIn.sin_port = htons(m_nPort);
	
	m_sockAddr = NULL; // bind the same machine
	m_bBound = false;
	m_bListening = true;
}

/**
* Destructor. Releases all resource.
* @since 1.0.0
*/
CServerSocket::~CServerSocket() {
	Close();
}

/**
* Sets the port
* @param port Value of port
* @since 1.0.0
*/
void CServerSocket::SetPort(int port) {
	m_nPort = port;
	Init();
}

/**
* Sets the queue size
* @param port Value of port
* @since 1.0.0
*/
void CServerSocket::SetQueue(int q) {
	m_nQueue = q;	
}

/**
* Returns the port
* @return int
* @since 1.0.0
*/
int CServerSocket::GetPort() {
	return m_nPort;
}

/**
* Returns the queue size
* @return int
* @since 1.0.0
*/
int CServerSocket::GetQueue() {
	return m_nQueue;
}

/**
* Returns the socket address
* @return CSocketAddress*
* @since 1.0.0
*/
CSocketAddress* CServerSocket::GetSocketAddress() {
	return m_sockAddr;
}

// GAB
SOCKET CServerSocket::GetServerSocket() {
    return m_socket;
}

/**
* Returns the listening flag
* @return bool
* @since 1.0.0
*/
bool CServerSocket::IsListening() {
	return m_bListening;
}
