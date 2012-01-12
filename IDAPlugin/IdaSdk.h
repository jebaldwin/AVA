#ifndef __IDASDK_H__
#define __IDASDK_H__
/////////////////////////////////////////////////////////////////////////////////
#pragma once

#include "windows.h"
#include <string>

#include <ida.hpp>
#include <idp.hpp>

/**
 * This class contains ida sdk useful function.
 */
class CIdaSdk
{
public:
    static std::string getFunctionName(ea_t ea);
    static bool isFunction(ea_t ea);

    static std::string getIDADisassembledFileName();
    static std::string getIDADisassembledFilePath();
    static std::string getIDAVersionString();
    static std::string getIDADatabaseFilePath();
    static std::string getIDAProPath();

    static HWND getIDAWindowsHandle();
};

///////////////////////////////////////////////////////////////////////////////
#endif // __IDASDK_H__