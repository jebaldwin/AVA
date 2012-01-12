#include <iostream>
#include <fstream>
#include <sstream>

#include "SocketComm.h"
#include "multithreadControlWindow.h"

/**
 * \fn CSocketComm::CSocketComm() 
 * \brief Initialize constants.
 */
CSocketComm::CSocketComm() 
    : JAVA_SERVER_HOST_IP("Localhost"),
      m_javaServerPort(0),
      m_cppServerPort(40010),
      m_pSocketClient(NULL),
      m_pMultithreadControler(NULL)
{
}

/**
 * \fn CSocketComm::initClient() 
 * \brief Initialize the C++ socket client.
 */
void CSocketComm::initClient() {
    if (m_pSocketClient == NULL)
        m_pSocketClient = new CSocketClient(JAVA_SERVER_HOST_IP, m_javaServerPort);
}

/**
 * \fn CSocketComm::initServer() 
 * \brief Initialize the C++ socket server.
 *
 * \return the CPP server listening port to which the java client will have to connect.
 */
unsigned short CSocketComm::initServer() {
    m_pMultithreadControler = new CMultithreadControl(this, m_cppServerPort);
    m_cppServerPort = m_pMultithreadControler->getListenerPort();
    return m_cppServerPort;
}

void CSocketComm::closeAndDeleteComm()
{
    // Close client connection to the java socket server.
    if (m_pSocketClient != NULL){
        m_pSocketClient->Close();
        delete(m_pSocketClient);
        m_pSocketClient = NULL;
    }
}

CSocketComm::~CSocketComm() 
{
    closeAndDeleteComm();

    // Stop the Socket server.
    if (m_pMultithreadControler != NULL)
    {
        if (m_pMultithreadControler->ServerRunning()) {
	        m_pMultithreadControler->DestroyControlWindow();
        }
        delete m_pMultithreadControler;
        m_pMultithreadControler = NULL;
    }
}

/**
 * \fn CSocketComm::send()
 * \brief Send out a position and return any reply. (Simple Atomic Data Exchanges)
 *
 * \param const std::string &message: Message to send.
 * \return std::string: Response received from java.
 */
std::string CSocketComm::send(const std::string &message) {
    
	std::string responseStr;
    
	try {
        if (m_pSocketClient != NULL){
		    m_pSocketClient->Send(message);
		    responseStr = m_pSocketClient->Receive();

		    std::basic_string <char>::size_type indexChEndl;
		    indexChEndl = responseStr.rfind ( "\r\n" );
		    if (indexChEndl != std::string::npos)
			    responseStr.erase(indexChEndl);
        } else {
            responseStr = "java does not respond";
        }
	}
	catch (openutils::CSocketException e) {
		responseStr = "java does not respond";
        
        //Close client connection to the java socket server.
        closeAndDeleteComm();
    }

    return responseStr;
}