package rcpapp.cartographer;

import cs.uvic.ca.ice.model.Function;

public class StackFrame {
	private Integer fn;
	private Function f;
		
	public StackFrame(Integer frame_number, Function f) {
		this.fn = frame_number;
		this.f = f;
	}
		
	public Integer getFrameNumber() {
		return this.fn;
	}
		
	public Function getFunction() {
		return this.f;
	}
}
