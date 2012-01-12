#include "IdaSdk.h"

#include <ida.hpp>
#include <idp.hpp>
#include <loader.hpp> // for database_idb
#include <diskio.hpp> // for idadir

/**
 * \fn getIDADisassembledFileName()
 * \brief get ida pro disassembled file name. 
 *
 * \return The disassembled file name as a string.
 */
std::string CIdaSdk::getIDADisassembledFileName()
{
    char disassembledFileName[255];
    std::string idaDisassembledFileName;

    if ( get_root_filename(disassembledFileName, sizeof(disassembledFileName)-1) )
        idaDisassembledFileName = disassembledFileName;
    else
        idaDisassembledFileName = "Unknown disassembled file name";

    return idaDisassembledFileName;
}

/**
 * \fn getIDADisassembledFilePath()
 * \brief get ida pro disassembled file path. 
 *
 * \return The disassembled file path saved in the database.
 * \note: If the .idb file is moved after creation, the path will not be updated!
 */
std::string CIdaSdk::getIDADisassembledFilePath()
{
    char disassembledFileName[255];
    std::string idaDisassembledFileName;

    if ( get_input_file_path(disassembledFileName, sizeof(disassembledFileName)-1) )
        idaDisassembledFileName = disassembledFileName;
    else
        idaDisassembledFileName = "Unknown disassembled file name";

    return idaDisassembledFileName;
}

/**
 * Tell if the address as argument is the beginning of a function in IDA.
 */
bool CIdaSdk::isFunction(ea_t ea){
    func_t * function = get_func(ea);
    if (function != NULL){
        return (function->startEA == ea);
    }
    return false;
}

/**
 * \fn getFunctionName()
 * \brief get the ida pro function name corresponding to the effective address. 
 *
 * \return The ida pro function name.
 */
std::string CIdaSdk::getFunctionName(ea_t ea){
    char buf[MAXSTR];
    get_func_name(ea, buf, sizeof(buf)-1);
    std::string functionName = buf;
    return functionName;
}

/**
 * \fn getIDAVersionString()
 * \brief get ida pro version number. 
 *
 * \return The ida pro version number as a string.
 */
std::string CIdaSdk::getIDAVersionString()
{
    char idaVersion[10];
    std::string idaVersionString;

    if ( get_kernel_version(idaVersion, sizeof(idaVersion)-1) )
        idaVersionString = idaVersion;
    else
        idaVersionString = "Unknown IDA version";

    return idaVersionString;
}

/**
 * \fn getIDADatabaseFilePath()
 * \brief get ida pro current .idb file path. 
 *
 * \return The .idb path.
 */
std::string CIdaSdk::getIDADatabaseFilePath()
{
    std::string idaDisassembledFileName;
    idaDisassembledFileName = database_idb;
    return idaDisassembledFileName;
}

/**
 * \fn getIDAWindowsHandle()
 * \brief get ida pro windows handle.
 *
 * \return The ida pro windows handle.
 */
HWND CIdaSdk::getIDAWindowsHandle(){
    return (HWND) callui(ui_get_hwnd).vptr;
}

/**
 * \fn CIdaSdk::getIDAProPath()
 * \brief Get IDA pro's path.
 *
 * \return A std::string containing IDA path.
 */
std::string CIdaSdk::getIDAProPath()
{
	std::string idaStrPath(idadir(NULL));

	//Replace every \ by / in the path.
	size_t cpt = idaStrPath.find("\\");
	while (cpt < std::string::npos)
	{
		idaStrPath.replace(cpt, 1, "/");
		cpt = idaStrPath.find("\\", cpt+1);
	}

	return idaStrPath;
}