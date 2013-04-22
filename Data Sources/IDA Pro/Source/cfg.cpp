/* 
 * Plugin: CFG
 * Author: Dean Pucsek <dpucsek@uvic.ca>
 * Date: 11 January 2013
 *
 * Extract info to build a CFG
 *
 */

/* IDA */
#include <ida.hpp>
#include <idp.hpp>
#include <loader.hpp>
#include <kernwin.hpp>
#include <search.hpp>
#include <xref.hpp>
#include <entry.hpp>
#include <name.hpp>
#include <bytes.hpp>
#include <lines.hpp>

/* C++ */
#include <vector>
#include <map>

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#define CFG_DEBUG 1

#ifdef CFG_DEBUG
#define DPRINT(s) msg("[CFG] " s "\n")
#define DPRINTF(s, ...) msg("[CFG] " s "\n", __VA_ARGS__)
#else
#define DPRINT(s)
#define DPRINTF(s, ...)
#endif

/* Error printing */
#define EMSGF(s, ...) msg("[CFG] ERROR %s:%d: " s "\n", __FILE__, __LINE__, __VA_ARGS__)
#define EMSG(s) msg("[CFG] ERROR %s:%d: " s "\n", __FILE__, __LINE__)

#define BUF_LEN 128

/* -------------- Utility Functions -------------- */

func_t *find_function_by_name(const char *fn)
{
    size_t func_idx;
    func_t *func;
    char *fn_buf;
    
    fn_buf = (char *)calloc(BUF_LEN, sizeof(char));
    if(!fn_buf)
        return NULL;

    for(func_idx = 0; func_idx < get_func_qty(); func_idx++)
    {
        memset((void *)fn_buf, 0, BUF_LEN);

        func = getn_func(func_idx);
        if(!get_func_name(func->startEA, fn_buf, BUF_LEN))
            continue;

        if(strncmp(fn, fn_buf, BUF_LEN) == 0)
            return func;
    }

    return NULL;
}

/* -------------- CFG Generation -------------- */

void cfg_gen(const char *func_name)
{
    func_t *func;
    ea_t insn_ea;

    std::vector<ea_t> xrefs_to(0);
    bool is_bb_end;
    xrefblk_t xb;

    func = find_function_by_name(func_name);
    if(!func) {
        EMSGF("Unable to find function: %s", func_name);
        return;
    }

    insn_ea = func->startEA;
    while(insn_ea < func->endEA)
    {
        msg("insn: 0x%x\n", insn_ea);

        is_bb_end = false;
        xrefs_to.clear();

        for(bool ok=xb.first_from(insn_ea, XREF_ALL); ok; ok=xb.next_from())
        {
            if(xb.type == fl_JN)
                is_bb_end = true;

            xrefs_to.push_back(xb.to);
        }

        if(is_bb_end)
        {
            for(std::vector<ea_t>::iterator it = xrefs_to.begin(); it != xrefs_to.end(); ++it)
                msg("-> xref from 0x%x to 0x%x\n", insn_ea, *it);
        }

        insn_ea = find_code(insn_ea, SEARCH_DOWN);
    }
}

/* -------------- IDA Plugin Interface -------------- */

int idaapi init(void)
{
	return is_idaq() ? PLUGIN_OK : PLUGIN_SKIP;
}

void idaapi term(void)
{
}

void idaapi run(int arg __attribute__((unused)))
{
    char *func_name = askstr(HIST_IDENT, NULL, "Function name to print CFG of");
    DPRINTF("func_name length: %d", (int)strlen(func_name));
    cfg_gen(func_name);
    free(func_name);
}

const char *comment = "CFG Printer";
const char *help = "";
const char *wanted_name = "CFG";
const char *wanted_hotkey = "Alt-i";

plugin_t PLUGIN = {
	IDP_INTERFACE_VERSION,
	0,
	init,
	term,
	run,
	comment,
	help,
	wanted_name,
	wanted_hotkey
};
