#ifndef __SOCKETCOMM_H__
#define __SOCKETCOMM_H__
/////////////////////////////////////////////////////////////////////////////////
#include "..\SocketModule\SocketClient.h"
#include "multithreadControlWindow.h"

class CSocketComm : public IReceive
{
public:
    CSocketComm();
    ~CSocketComm();
    void closeAndDeleteComm();

    unsigned short initServer();
    void initClient();

    void setJavaServerPort(unsigned short port){
        m_javaServerPort = port;
    }

    unsigned short getCPPServerPort(){
        return m_cppServerPort;
    }

  	virtual void receive(const std::string &buffer) = 0;
    std::string send(const std::string &message);

private:
    // The IP address of the Java server for use in socket connections
    const std::string JAVA_SERVER_HOST_IP;

    // The port of the Java server for use in socket connections
    unsigned short m_javaServerPort;
    unsigned short m_cppServerPort;

private:
    CSocketClient * m_pSocketClient;
    CMultithreadControl * m_pMultithreadControler;
};

///////////////////////////////////////////////////////////////////////////////
#endif // __SOCKETCOMM_H__
