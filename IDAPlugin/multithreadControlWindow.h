#ifndef __MULTITHREADCONTROL_H__
#define __MULTITHREADCONTROL_H__

#include <ida.hpp>
#include <diskio.hpp>
#include "..\SocketModule\ServerSocket.h"

/**
 * The receive function will be implemented somewhere else because the only receiving 
 * side knows what to do with the received message.
 */
class IReceive
{
public:
    virtual void receive( const std::string & buffer ) = 0;
};

/**
 * Use of the event pool of an invisible windows within the plugin IDA to catch
 * messages from socket communication and avoid conflicts with IDA's single thread.
 */
class CMultithreadControl
{
public:
    CMultithreadControl( IReceive * commCallBack, int ListenPort );
    ~CMultithreadControl() {}

    bool ServerRunning();
    bool DestroyControlWindow();

    static long FAR PASCAL WndProc( HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam );

    void Accept( SOCKET hSocket, int nEvent, int nError );
    void Read  ( SOCKET hSocket, int nEvent, int nError );

    // Get the ListenerPort. It may changes if the default port is already used.
    int getListenerPort(){
        return m_ListenPort;
    }

private:
    BOOL CreateHiddenWindow();    
    BOOL CreateListenerSocket();

    HWND        m_hWnd;
    WNDCLASSEX  m_wndclass;
    openutils::CSocket       * m_hListen;
    openutils::CSocket       * m_hAccept;
    openutils::CServerSocket * ListenerServer;
    int         m_ListenPort;

    IReceive  * m_commCallBack;
};

#endif	/*__MULTITHREADCONTROL_H__*/
