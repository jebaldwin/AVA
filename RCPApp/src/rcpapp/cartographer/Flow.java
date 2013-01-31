package rcpapp.cartographer;

import rcpapp.cartographer.CFGFrame.FlowType;
import cs.uvic.ca.ice.model.Instruction;

public class Flow {
	private Instruction start;
	private Instruction end;
	private FlowType type;
	
	public Flow(Instruction s, Instruction e, FlowType ft) {
		this.start = s;
		this.end = e;
		this.type = ft;
	}
	
	public Instruction origin() {
		return this.start;
	}
	
	public Instruction destination() {
		return this.end;
	}
}