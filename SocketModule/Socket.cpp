#include "Socket.h"

using namespace openutils;

/**
* Default constructor
* @since 1.0.0
*/
CSocket::CSocket() 
    : m_clientAddr(NULL),
      m_socket(INVALID_SOCKET)
{
}

/**
* Sets the SOCKET
* @param sock SOCKET
* @since 1.0.0
*/
void CSocket::SetSocket(SOCKET sock) {
	m_socket = sock;
}

/**
* Gets the SOCKET
* @param sock SOCKET
* @since 1.0.0
*/
SOCKET CSocket::GetSocket() {
	return m_socket;
}
/**
* Sets address details
* @param addr SOCKADDR_IN
* @since 1.0.0
*/
void CSocket::SetClientAddr(SOCKADDR_IN addr) {
	if(m_clientAddr != NULL) delete m_clientAddr;
	m_clientAddr = new CSocketAddress(addr);
}

/**
* Connects to a server
* @since 1.0.0
*/
void CSocket::Connect() throw (CSocketException) {
	if(m_clientAddr == NULL)
		throw CSocketException(-1,"Cannot connect to NULL host");
	Connect(m_clientAddr->GetName(), m_clientAddr->GetPort());
}

/**
* Connects to a server
* @param port Port to connect
* @since 1.0.0
*/
void CSocket::Connect(int port) throw (CSocketException) {
	if(m_clientAddr == NULL)
		throw CSocketException(-1,"Cannot connect to NULL host");
	Connect(m_clientAddr->GetName(), port);
}

/**
* Connects to a server
* @param host_name Server name
* @param port Port to connect
* @since 1.0.0
*/
void CSocket::Connect(const char* host_name, int port) throw (CSocketException) {	
	LPHOSTENT hostEntry;
	hostEntry = gethostbyname(host_name);
	if (!hostEntry) {
		throw CSocketException(WSAGetLastError(), "Failed to resolve host");		
	}
	
    if (m_socket == INVALID_SOCKET) {
        //Socket Creation here
        m_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
        if (m_socket == INVALID_SOCKET) {
            throw CSocketException(WSAGetLastError(), "Failed to create client socket");		
        }
    }
	
	SOCKADDR_IN serverInfo;
	serverInfo.sin_family = AF_INET;
	serverInfo.sin_addr = *((LPIN_ADDR)*hostEntry->h_addr_list);
	serverInfo.sin_port = htons(port);
    // The connect function establishes a connection to a specified socket.
    //Parameters
    // s 
    //  Descriptor identifying an unconnected socket.
    // name 
    //  Name of the socket in the sockaddr structure to which the connection should be established.
    // namelen 
    //  Length of name, in bytes.
	int nret = connect(m_socket,(LPSOCKADDR)&serverInfo, sizeof(struct sockaddr));
	if (nret == SOCKET_ERROR) {
		throw CSocketException(WSAGetLastError(), "Connect failed.");		
	}
}

/**
* Writes data to the socket. Returns number of bytes written
* @param data data to write
* @return int
* @throw CSocketException
* @since 1.0.0
*/
int CSocket::Send(const char* data) throw (CSocketException) {
	int nret = send(m_socket, data, strlen(data),0);
	if(nret == SOCKET_ERROR) {
		throw CSocketException(WSAGetLastError(), "Network failure: Send()");		
	}
	return nret;
}

/**
* Writes only the number of byte specified to the socket. Returns number of bytes written.
* @param data data to write
* @return int
* @throw CSocketException
* @since 1.0.0
*/
int CSocket::Send(const char* data, const int nb) throw (CSocketException) {
	int nret = send(m_socket, data, nb, 0);
	if(nret == SOCKET_ERROR) {
		throw CSocketException(WSAGetLastError(), "Network failure: Send()");		
	}
	return nret;
}

/**
* Reads data from the socket.Returns number of bytes actually read.
* @param buffer Data buffer
* @param len Number of bytes to read
* @throw CSocketException
* @since 1.0.0
*/
int CSocket::Read(char* buffer,int len) throw (CSocketException) {
	int nret = 0;	
	nret = recv(m_socket,buffer,len,0);
	if(nret == SOCKET_ERROR) {
		throw CSocketException(WSAGetLastError(), "Network failure: Read()");		
	}
	buffer[nret] = '\0';
	return nret;	
}

/**
* Closes the socket
* @since 1.0.0
*/
void CSocket::Close() {
    if (m_socket != INVALID_SOCKET){
        if (closesocket(m_socket) != 0){
            throw CSocketException(WSAGetLastError(), "Can't close socket!");
        }
        m_socket = INVALID_SOCKET;
    }
    delete(m_clientAddr);
}

/**
* Destructor
* @since 1.0.0
*/
CSocket::~CSocket() {
	Close();
}
