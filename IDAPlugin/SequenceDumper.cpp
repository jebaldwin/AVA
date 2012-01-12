#include "SequenceDumper.h"

#undef _DEBUG

SequenceDumper::SequenceDumper(char *fname) {
	iii = 0;
	// Open sequence file
	seq_file = fopenWT(fname);

	// Initialize Call instructions vector
	ushort cIns[] = {
		NN_call,                // Call Procedure
		NN_callfi,              // Indirect Call Far Procedure
		NN_callni,              // Indirect Call Near Procedure
	};
	callIns = vector<ushort>(cIns, cIns+sizeof(cIns)/sizeof(ushort));

	// Initialize Jump instructions vector
	ushort jIns[] = {
		NN_ja,                  // Jump if Above (CF=0 & ZF=0)
		NN_jae,                 // Jump if Above or Equal (CF=0)
		NN_jb,                  // Jump if Below (CF=1)
		NN_jbe,                 // Jump if Below or Equal (CF=1 | ZF=1)
		NN_jc,                  // Jump if Carry (CF=1)
		NN_jcxz,                // Jump if CX is 0
		NN_jecxz,               // Jump if ECX is 0
		NN_jrcxz,               // Jump if RCX is 0
		NN_je,                  // Jump if Equal (ZF=1)
		NN_jg,                  // Jump if Greater (ZF=0 & SF=OF)
		NN_jge,                 // Jump if Greater or Equal (SF=OF)
		NN_jl,                  // Jump if Less (SF!=OF)
		NN_jle,                 // Jump if Less or Equal (ZF=1 | SF!=OF)
		NN_jna,                 // Jump if Not Above (CF=1 | ZF=1)
		NN_jnae,                // Jump if Not Above or Equal (CF=1)
		NN_jnb,                 // Jump if Not Below (CF=0)
		NN_jnbe,                // Jump if Not Below or Equal (CF=0 & ZF=0)
		NN_jnc,                 // Jump if Not Carry (CF=0)
		NN_jne,                 // Jump if Not Equal (ZF=0)
		NN_jng,                 // Jump if Not Greater (ZF=1 | SF!=OF)
		NN_jnge,                // Jump if Not Greater or Equal (ZF=1)
		NN_jnl,                 // Jump if Not Less (SF=OF)
		NN_jnle,                // Jump if Not Less or Equal (ZF=0 & SF=OF)
		NN_jno,                 // Jump if Not Overflow (OF=0)
		NN_jnp,                 // Jump if Not Parity (PF=0)
		NN_jns,                 // Jump if Not Sign (SF=0)
		NN_jnz,                 // Jump if Not Zero (ZF=0)
		NN_jo,                  // Jump if Overflow (OF=1)
		NN_jp,                  // Jump if Parity (PF=1)
		NN_jpe,                 // Jump if Parity Even (PF=1)
		NN_jpo,                 // Jump if Parity Odd  (PF=0)
		NN_js,                  // Jump if Sign (SF=1)
		NN_jz,                  // Jump if Zero (ZF=1)
		NN_jmp,                 // Jump
		NN_jmpfi,               // Indirect Far Jump
		NN_jmpni,               // Indirect Near Jump
		NN_jmpshort,            // Jump Short (not used)
	};
	jumpIns = vector<ushort>(jIns, jIns+sizeof(jIns)/sizeof(ushort));
}

SequenceDumper::~SequenceDumper() {
	eclose(seq_file);
}

void SequenceDumper::setFilename(char* fname) {
	filename = string(fname);
}

void SequenceDumper::dump(){
	populateImports();
	dumpEntryPoints();
	// Iterate over every function in the module
	for(size_t i = 0; i < get_func_qty(); i++) {
		func_t *f = getn_func(i);
		dumpFunction(f);
	}
}

void SequenceDumper::populateImports() {
	for(uval_t idx = import_node.alt1st(); idx != BADNODE; idx = import_node.altnxt(idx)) {
		// Get the module name
		char modName[MAXSTR + 4];
		if(import_node.supstr(idx, modName, sizeof(modName)) <= 0)
			qstrncpy(modName, "Unknown", sizeof(modName)); // No name
		else
			qstrncat(modName, ".dll", MAXSTR + 4);
		netnode modNode = import_node.altval(idx);
		char nodeName[MAXNAMESIZE];
		modNode.name(nodeName, MAXNAMESIZE);
#ifdef _DEBUG
		msg("modNode idx: %x\n       name: ", idx);
#endif
		for(int i = 0; nodeName[i] != 0; i++) {
#ifdef _DEBUG
			msg("%02x ", nodeName[i]);
#endif
		}

#ifdef _DEBUG
		msg("\n");
#endif

		// For all imported by ORDINAL functions
		for(uval_t ord = modNode.alt1st(); ord != BADNODE; ord = modNode.altnxt(ord)) {
			ea_t ea = modNode.altval(ord);
			char funcName[MAXSTR];
			if(modNode.supstr(ea, funcName, sizeof(funcName)) <= 0)
				funcName[0] = '\0'; // Import by ordinal, no name
			else 
				importsMap[string(funcName)] = string(modName);

		}

		// For all imported by NAME functions
		for(ea_t ea = modNode.sup1st(); ea != BADADDR; ea = modNode.supnxt(ea)) {
			char funcName[MAXSTR];
			if(modNode.supstr(ea, funcName, sizeof(funcName)) <= 0)
				funcName[0] = '\0'; // No name
			else
				importsMap[string(funcName)] = string(modName);
		}
	}
}

void SequenceDumper::dumpEntryPoints() {
	char epname[MAXSTR];
	ea_t epaddr;
	// Iterate through the entry points
	for(size_t e = 0; e < get_entry_qty(); e++) {
		epaddr = get_entry(get_entry_ordinal(e));
		get_func_name(epaddr, epname, sizeof(epname));
		char module_filename[256];
		get_root_filename(module_filename, 256);
		qfprintf(seq_file, ">> %d:\t%a\t%s\t%s\n", get_func_num(epaddr), epaddr, epname, module_filename);
	}
}


void SequenceDumper::dumpFunction(func_t *f) {
	char fname[MAXSTR];
	get_func_name(f->startEA, fname, sizeof(fname));
	int ord = get_func_num(f->startEA);
	char module_filename[256];
	get_root_filename(module_filename, 256);
	qfprintf(seq_file, "> %d:\t%a\t%s\t%s\n", ord, f->startEA, fname, module_filename);
	dumpCalls(f);
	qfprintf(seq_file, "< %d\n", ord);
}

void SequenceDumper::dumpCalls(func_t *f) {
	ccc = 0;

	// Iterate over addresses from the start to end of the function
	for(ea_t addr = f->startEA; addr < f->endEA; addr++) {
		// Check if it's a call
		if(matchesIns(addr, callIns)) {
			// Check if the called address is stored in a register
			if(cmd.Operands[0].type == o_reg) {
				char buf[MAXSTR];
				generate_disasm_line(addr, buf, sizeof(buf)-1);
				// Get the actual address if possible statically
				ea_t calledAddr = getAddressCalled(buf);
				if(calledAddr != BADADDR) {
					dumpCall(addr, calledAddr);
				}
			} else {
				// The address isn't in a register
				// Using cross references to find the called address
				xrefblk_t xb;
				for(bool res = xb.first_from(addr, XREF_ALL); res; res = xb.next_from()) {
					if(xb.iscode && ((xb.type == fl_CF) || (xb.type == fl_CN))) {
						dumpCall(addr, xb.to);
					}
				}
			}
		}
		// Check if it's a jump
		if(matchesIns(addr, jumpIns)) {
			// Using cross references to check if the jump is to an address
			// in the data segement
			xrefblk_t xb;
			for(bool res = xb.first_from(addr, XREF_DATA); res; res = xb.next_from()) {
				segment_t *seg = getseg(xb.to);
				
				// The segment type SEG_XTRN is only created if the 'Create Import Segment' option
				// is selected when disassembling
				if(seg != NULL){
					if(seg->type == SEG_XTRN) {
						dumpExternCall(addr, xb.to);
					}
				}
			}
		}
	}
}

void SequenceDumper::dumpLine(char *line) {
	for(int i = 0; line[i]; i++) {
		qfprintf(seq_file, "%2x", line[i]);
	}
	qfprintf(seq_file, "\n");
}

void SequenceDumper::dumpCall(ea_t from, ea_t to) {
	segment_t *seg = getseg(to);
	if(seg->type == SEG_CODE) {
		char funcName[MAXSTR];
		char module_filename[256];
		get_root_filename(module_filename, 256);
		if(get_func_name(to, funcName, MAXSTR) != NULL) {
			qfprintf(seq_file, "\t%d:\t%a\t%a\t%s\t%s\n", get_func_num(to), from, to, funcName, module_filename);
		}
	} else if(seg->type == SEG_XTRN) {
		dumpExternCall(from, to);
	}
}

void SequenceDumper::dumpExternCall(ea_t from, ea_t to) {
	//char* line = (char *)qalloc(MAXSTR);
	//generate_disasm_line(to, line, MAXSTR - 1);
 
	char linebuf[MAXSTR];
	char *line = linebuf;
	generate_disasm_line(to, line, MAXSTR - 1);


	while(*line != 0 && *line != COLOR_ADDR)
		line++;
	if(*line == COLOR_ADDR) {
		line++;  // skip color code
#ifdef __EA64__
		line += 16;  // skip 16 digits of 64-bit address
#else
		line += 8;   // skip 8 digits of 32-bit address
#endif
		char* funcname = line;  // Beginning of function name
		while(*line != COLOR_OFF)
			line++;
		*line = '\0';  // Null-terminate the string
//		qfprintf(seq_file, "\t-1:\t%a\t%s\n", addr, funcname);
		pair<string, string> imp = getImport(funcname);
		char module_filename[256];
		get_root_filename(module_filename, 256);
		if(imp.second.empty()) {
			qfprintf(seq_file, "\t-1:\t%a\t%a\t%s\t%s\n", from, to, funcname, "Unknown", module_filename);
		} else {
			qfprintf(seq_file, "\t-1:\t%a\t%a\t%s\t%s\n", from, to, imp.first.c_str(), imp.second.c_str(), module_filename);
		}
	}
	//qfree(line);
}

pair<string, string> SequenceDumper::getImport(char* funcName) {
	string modStr = importsMap[string(funcName)];
	if(modStr.empty()) {
		funcName += 6; // skip a possible '__imp_' prefix to function name
		modStr = importsMap[string(funcName)];
	}
	return pair<string, string>(string(funcName), modStr);
}

ea_t SequenceDumper::getAddressCalled(char* line) {
	ea_t addr = BADADDR; 
	while(*line != COLOR_ADDR && *line != 0) 
		line++;
	if(*line == COLOR_ADDR) {
		line++;
#ifdef __EA64__
#define ADDRLEN 16
#else
#define ADDRLEN 8
#endif
		char addrstr[ADDRLEN+1];
		qstrncpy(addrstr, line, sizeof(addrstr));
		str2ea(addrstr, &addr, 0);
	}
	return addr;
}

bool SequenceDumper::matchesIns(ea_t addr, vector<ushort> &ins) {
	if(ua_ana0(addr) > 0) {
		vector<ushort>::iterator insIter;
		for(insIter = ins.begin(); insIter != ins.end(); insIter++)
			if(cmd.itype == *insIter)
				return true;
	}
	return false;
}
