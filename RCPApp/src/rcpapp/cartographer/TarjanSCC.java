package rcpapp.cartographer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

import edu.uci.ics.jung.graph.Graph;

import cs.uvic.ca.ice.model.Instruction;

public class TarjanSCC {
	private Graph<Instruction, Flow> graph;
	
	private int index;
	private Stack<Instruction> stack;
	
	public TarjanSCC(Graph<Instruction, Flow> g) {
		this.graph = g;
		this.index = 0;
		this.stack = new Stack<Instruction>();
	}
	
	public Collection<Collection<Instruction>> identify() {
		Vector<Collection<Instruction>> comps = new Vector<Collection<Instruction>>();
		for(Instruction i : this.graph.getVertices()) {
			if(i.index == -1) {
				Collection<Instruction> c = this.strongConnect(i);
				if(c != null)
					comps.add(c);
			}
		}
		
		return comps;
	}
	
	private Collection<Instruction> strongConnect(Instruction i) {
	    // Set the depth index for v to the smallest unused index
		i.index = this.index;
		i.lowlink = this.index;
		this.index++;
			
		this.stack.push(i);

		// Consider successors of v
		for(Flow f : this.graph.getEdges()) {
			Instruction v = f.origin();
			Instruction w = f.destination();
			
			if(w.index == -1) {
				this.strongConnect(w);
				v.lowlink = Math.min(v.lowlink, w.lowlink);
			} else if(this.stack.contains(w)) {
				v.lowlink = Math.min(v.lowlink, w.index);
			}
		}
			
		// If v is a root node, pop the stack and generate an SCC
		if(i.lowlink == i.index) {
			ArrayList<Instruction> strongComponent = new ArrayList<Instruction>();
			Instruction w = this.stack.pop();
		
			while(w != i) {
				strongComponent.add(w);
				w = this.stack.pop();
			}
				
			return strongComponent;
		}
			
		return null;
	}
}
