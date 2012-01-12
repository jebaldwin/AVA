#ifndef __SEQUENCEDUMPER_H
#define __SEQUENCEDUMPER_H

#include <ida.hpp>
#include <idp.hpp>
#include <allins.hpp>
#include <diskio.hpp>
#include <funcs.hpp>
#include <fpro.h>
#include <ua.hpp>
#include <name.hpp>
#include <entry.hpp>
#include <vector>
#include <string>
#include <hash_map>

using namespace std;
using namespace stdext;

class SequenceDumper {
public:
	SequenceDumper(char *fname);
	~SequenceDumper();

	void dump();
	void setFilename(char *fname);

private:
	FILE *seq_file;
	hash_map<string, string> importsMap;
	string filename;
	int iii;
	int ccc;

	void dumpEntryPoints();
	void dumpFunction(func_t *f);
	void dumpCalls(func_t *f);
	void dumpCall(ea_t from, ea_t to);
	void dumpLine(char* line);
	void dumpExternCall(ea_t from, ea_t to);
	void populateImports();
	ea_t getAddressCalled(char* line);
	pair<string, string> getImport(char* funcName);
	bool matchesIns(ea_t addr, vector<ushort> &ins);
	vector<ushort> callIns;
	vector<ushort> jumpIns;
};

#endif