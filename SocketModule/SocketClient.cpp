#include "SocketClient.h"

using namespace openutils;

CSocketClient::CSocketClient(std::string host, unsigned short connectionPort)
{
    m_hostName = host;
    m_connectionPort = connectionPort;
    m_connected = false;
    m_client = new CSocket;
}

CSocketClient::~CSocketClient(void) {
    delete(m_client);
}

void CSocketClient::Connect(){
    m_client->Connect(m_hostName.c_str(), m_connectionPort);
    m_connected = true;
}

void CSocketClient::Close() {
    if (m_client != NULL)
        m_client->Close();
    m_connected = false;
}

void CSocketClient::Send(std::string message) {
    try {
        if (m_connected)
            m_client->Send(message.c_str());
        else
            Connect();
    }
    catch (CSocketException e) {
        Close();
		throw;
    }
}

std::string CSocketClient::Receive() {
    try {
        std::string receivedBuffer;

        char buffer[1000];
        int cb = 0;
        if (m_connected) {
            cb = m_client->Read(buffer, 1000);

            receivedBuffer = buffer;
            receivedBuffer = receivedBuffer.substr(0, cb);
        }
        return receivedBuffer;
    }
    catch (CSocketException e) {
        Close();
        throw;
    }
}
 