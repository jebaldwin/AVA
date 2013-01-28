package rcpapp.cartographer;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instruction;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class CFGFrame {
	private JFrame frame;
	private Function func;
	
	public CFGFrame(Function f) {
		this.frame = new JFrame(f.getName());
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.func = f;
	}
	
	public void show() {
	    CFG cfg = new CFG(this.func);
	    Layout<Instruction, Flow> layout = new CircleLayout(cfg.graph());
	    layout.setSize(new Dimension(300,300));
	    BasicVisualizationServer<Instruction, Flow> vv = new BasicVisualizationServer<Instruction, Flow>(layout);
	    vv.setPreferredSize(new Dimension(350,350));
	
		this.frame.pack();
		this.frame.setVisible(true);
	}
	
	private class CFG {
		private SparseMultigraph<Instruction, Flow> g;
		
		public CFG(Function f) {
			g = new SparseMultigraph<Instruction, Flow>();
			
			Collection<Instruction> instrs = f.getInstructions();
			Iterator<Instruction> instr_iter = instrs.iterator();
			while(instr_iter.hasNext()) {
				Instruction instr = instr_iter.next();
				Iterator<Long> next_instr = instr.getNextAddresses().iterator();
				while(next_instr.hasNext()) {
					Long ni = next_instr.next();
					Flow fl = new Flow(instr, f.getInstruction(ni), FlowType.FLOW_NORMAL);
					g.addEdge(fl, instr, f.getInstruction(ni));
				}
			}
		}
		
		public Graph<Instruction, Flow> graph() {
			return this.g;
		}
	}
	
	private class Flow {
		private Instruction start;
		private Instruction end;
		private FlowType type;
		
		public Flow(Instruction s, Instruction e, FlowType ft) {
			this.start = s;
			this.end = e;
			this.type = ft;
		}
	}
	
	private enum FlowType {
		FLOW_NORMAL, FLOW_JUMP, FLOW_CALL
	}
}