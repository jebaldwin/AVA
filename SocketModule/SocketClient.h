#ifndef __SOCKETCLIENT_H__
#define __SOCKETCLIENT_H__
///////////////////////////////////////////////////////////////////////////////
#pragma once

#include "Socket.h"

class CSocketClient
{
public:
    CSocketClient(std::string, unsigned short);
    ~CSocketClient(void);

    void Connect();
    void Close();

    bool isConnected(){return m_connected;}
    void Send(std::string message);
    std::string Receive();
private:
    openutils::CSocket * m_client;
    bool m_connected;
    std::string m_hostName;
    unsigned short m_connectionPort;
};

///////////////////////////////////////////////////////////////////////////////
#endif // __SOCKETCLIENT_H__
