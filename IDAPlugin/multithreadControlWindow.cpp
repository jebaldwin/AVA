#include <sstream>

#include "multithreadControlWindow.h"
#include "..\SocketModule\Winsock.h"
#include "idasdk.h"

#define	WM_NETEVENT WM_USER + 1
#undef _DEBUG

using namespace openutils;

/**
* CMultithreadControl::CMultithreadControl
* @param Port to Listen to.
*/
CMultithreadControl::CMultithreadControl( IReceive * commCallBack, int ListenPort )
    : m_hWnd(NULL)
{
    m_commCallBack = commCallBack;
    m_ListenPort = ListenPort;

    // initializes winsock
	CWinSock::Initialize();

    if (CreateHiddenWindow())
        CreateListenerSocket();
}

/**
* CMultithreadControl::CreateHiddenWindow
* Create the control window that will be used to communicate
* to the single threaded IDA SDK.
*/
BOOL CMultithreadControl::CreateHiddenWindow()
{
    static BOOL s_fRegistered = FALSE;

    // Create a unique identifier for the hidden windows class name
    // to avoid conflict when registering the class name for many plugins
    // running in the same IDA pro and using the socket communication module.
    std::ostringstream oss;
    oss << "SocketComm_";                               // Socket Comm ID
    oss << GetCurrentProcessId() << "_";                // Processus ID
    oss << (ea_t)(void *)CMultithreadControl::WndProc;  // WndProc "ID"
    // Create a copy of the copy returned by str() because we keep 
    // a pointer to it and this last copy is temporary so the pointer would
    // be pointing at garbage.
    std::string classname = oss.str();

    if( !s_fRegistered )
    {
        m_wndclass.cbSize = sizeof(WNDCLASSEX);
        m_wndclass.style = CS_HREDRAW | CS_VREDRAW | CS_DBLCLKS;
        m_wndclass.lpfnWndProc = CMultithreadControl::WndProc;
        m_wndclass.cbClsExtra = 0;
        m_wndclass.cbWndExtra = 0;
        m_wndclass.hInstance = GetModuleHandle(NULL);
        m_wndclass.hIcon = NULL;
        m_wndclass.hIconSm = NULL;
        m_wndclass.hCursor = NULL;
        m_wndclass.hbrBackground = NULL;
        m_wndclass.lpszMenuName = NULL;
        m_wndclass.lpszClassName = classname.c_str();

        if( RegisterClassEx (&m_wndclass) == 0 ){
            DWORD errCode = GetLastError();
            msg("Socket communication queue window hasn't been able to register the class %s successfully. Error: %d\n", classname.c_str(), errCode);
            return false;
        }

        s_fRegistered = TRUE;
    }

    //Get IDA pro main window handle.
    HWND hwndIDA = CIdaSdk::getIDAWindowsHandle();
    m_hWnd = CreateWindow(  m_wndclass.lpszClassName,
                            "",
                            WS_OVERLAPPEDWINDOW, //WS_POPUP,
                            0,
                            0,
                            0,
                            0,
                            hwndIDA, //NULL,
                            NULL,
                            GetModuleHandle(NULL),
                            (LPVOID)this );

    if (m_hWnd == NULL){
        DWORD errCode = GetLastError();
        msg("Socket communication queue window hasn't been created successfully. Error: %d\n", errCode);
    }

    // Invisible window
    ShowWindow( m_hWnd, /*SW_NORMAL*/ SW_HIDE );
    UpdateWindow( m_hWnd );
    return true;
}

bool CMultithreadControl::ServerRunning()
{
	return m_hWnd != NULL;
}

bool CMultithreadControl::DestroyControlWindow()
{
    int retr = false;
    int ret2 = false;
	if( DestroyWindow(m_hWnd) ) // Windows will then send a WM_DESTROY message.
    {
	    m_hWnd = NULL;
        retr = true;
    }

    ret2 = UnregisterClass(m_wndclass.lpszClassName, m_wndclass.hInstance);
	return (retr && ret2);
}

/**
* PASCAL CMultithreadControl::WndProc
*/
long FAR PASCAL CMultithreadControl::WndProc( HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam )
{
    CMultithreadControl * pObj;
    SOCKET hSocket = (SOCKET)wParam;
    int nError = WSAGETSELECTERROR(lParam);
    int nEvent = WSAGETSELECTEVENT(lParam);

    // From MSDN:
    // The WSAAsyncSelect function is used to request that WS2_32.DLL should send a message to the window
    // hWnd when it detects any network event specified by the lEvent parameter. The message that should be
    // sent is specified by the wMsg parameter. The socket for which notification is required is identified
    // by the s parameter.
    // The WSAAsyncSelect function automatically sets socket s to nonblocking mode, regardless of the value
    // of lEvent. To set socket s back to blocking mode, it is first necessary to clear the event record
    // associated with socket s via a call to WSAAsyncSelect with lEvent set to zero.

    switch( message )  
    {
    case WM_CREATE :
        {
            // On met l'objet CMultithreadControl ds la fenetre (GWL_USERDATA)
            LPCREATESTRUCT lpcs = (LPCREATESTRUCT)lParam;
            pObj = (CMultithreadControl *)lpcs->lpCreateParams;
            SetWindowLong( hWnd, GWL_USERDATA, (LONG)pObj );
            return 0;
        }
    break;
    case WM_NETEVENT:
        {
            pObj = (CMultithreadControl *)GetWindowLong( hWnd, GWL_USERDATA );

            switch( nEvent )
            {
            case FD_ACCEPT:
                pObj->Accept(hSocket, nEvent, nError );
                break;
            case FD_READ:
                pObj->Read(hSocket, nEvent, nError );
                break;
            }
            break;
        }
    }
    return DefWindowProc( hWnd, message, wParam, lParam );
}

/**
* CMultithreadControl::CreateListenerSocket
*/
BOOL CMultithreadControl::CreateListenerSocket()
{
    ListenerServer = new CServerSocket(m_ListenPort);

    for (int i=0; i<9; i++){
        try {
            m_hListen = ListenerServer->BindAndListen();
			i = 9; //I don't know why break doesn't work for now :$
        }
        catch (CSocketException e) {
            // 10048 -> WSAEADDRINUSE
            if (i == 9){ // try 10 port before giving up.
                msg( "CMultithreadControl::CreateListenSocket() : Can't bind. %s (%d)\n", e.GetMessage(), e.GetCode() );
                return false;
            }
            else{
                m_ListenPort++;
                ListenerServer->SetPortA(m_ListenPort);
				char module_filename[256];
				ssize_t val = get_root_filename(module_filename, 256);
				std::string commandLine = "";
				commandLine.append(CIdaSdk::getIDAProPath() + "\\plugins\\java\\ava\\ports.txt");
				FILE *seq_file = fopenWT(commandLine.c_str());
				qfprintf(seq_file, "%d\t%s\n", m_ListenPort, module_filename);
				eclose(seq_file);
            }
        }
    }

#if _DEBUG
    msg( "m_hListen (Socket %d) is listening on the port %d for a socket connection\n", m_hListen->GetSocket(), m_ListenPort); 
#endif

    // FD_ACCEPT means we wants to receive notification of incoming connections.
    if (WSAAsyncSelect( m_hListen->GetSocket(), m_hWnd, WM_NETEVENT, FD_ACCEPT ) == SOCKET_ERROR) 
    {
        DWORD errCode = WSAGetLastError();
        msg( "CMultithreadControl::CreateListenSocket() : Can't WSAAsyncSelect(Error Number: %d)\n", errCode );
	    return false;
    }

#if _DEBUG
    msg("m_hListen (Socket %d) registered to the network event FD_ACCEPT\n", m_hListen->GetSocket());
#endif

    return true;
}

/**
* CMultithreadControl::Accept
* @param SOCKET hSocket : 
* @param int nEvent :
* @param int nError :
*/
void CMultithreadControl::Accept( SOCKET hSocket, int nEvent, int nError )
{
    if( nError != 0 ) {
        msg( "CMultithreadControl::Accept() : nError = %d\n", nError );
        return;
    }

    // Only one socket is associated with WM_NETEVENT message.
    if( hSocket != m_hListen->GetSocket()) {
        msg( "CMultithreadControl::Accept() : hSocket != m_hListen -> Application error !\n" );
        return;
    }

    m_hAccept = ListenerServer->Accept();

    if ( WSAAsyncSelect( m_hAccept->GetSocket(), m_hWnd, WM_NETEVENT, FD_READ ) == SOCKET_ERROR ) {
        DWORD errCode = WSAGetLastError();
        msg( "CMultithreadControl::Connect() : Can't WSAAsyncSelect(Error Number: %d)\n", errCode );
        return;
    }
#if _DEBUG
    else {
        msg("m_hAccept (Socket %d) registered to the network event FD_READ\n", m_hAccept->GetSocket());
    }
#endif
}

/**
* CMultithreadControl::Read
* @param SOCKET hSocket : 
* @param int nEvent :
* @param int nError :
* @return void
*/
void CMultithreadControl::Read( SOCKET hSocket, int nEvent, int nError )
{
    char Buffer[1000]; 
    int cb;

    if( nError != 0 ) {
        msg( "CMultithreadControl::Read() : nError = %d\n", nError );
        return;
    }
     
    if( hSocket != m_hAccept->GetSocket() ) {
        msg( "CMultithreadControl::Read() : hSocket != m_hAccept -> Application error !\n" );
        return;
    }

    try {
        cb = m_hAccept->Read(Buffer, 1000);
    } catch (CSocketException e) {
        msg( "Can't Read. %s (%d)\n", e.GetMessage(), e.GetCode() );
        return;
    }

    std::string receivedBuffer = Buffer;
    receivedBuffer = receivedBuffer.substr(0, cb);
    m_commCallBack->receive(receivedBuffer);

#if _DEBUG
    msg( "Message received on socket %d : %s\n", m_hAccept->GetSocket(), Buffer );
#endif
}