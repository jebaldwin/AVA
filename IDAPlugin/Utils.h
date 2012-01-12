#ifndef __UTILS_H__
#define __UTILS_H__
/////////////////////////////////////////////////////////////////////////////////
#pragma once

#include <sstream>

/**
 * Static class containing useful functions not related to IDA plugins. 
 */
class CUtils
{
public:
    /**
     * \fn toString()
     * \brief Generic conversion to std::string
     *
     * \param const T& :
     * \return 
     */
    template <class T> static std::string toString(const T& t)
    {
        std::stringstream ss;
        ss << t;
        return ss.str();
    }
};

///////////////////////////////////////////////////////////////////////////////
#endif // __UTILS_H__