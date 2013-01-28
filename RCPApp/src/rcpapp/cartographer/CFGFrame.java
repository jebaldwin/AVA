package rcpapp.cartographer;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instruction;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class CFGFrame {
	private JFrame frame;
	private Function func;
	private CFG cfg;
	
	public CFGFrame(Function f) {
		this.frame = new JFrame(f.getName());
		this.func = f;
		
	    cfg = new CFG(this.func);
	    
	    Layout<Instruction, Flow> layout = null;
	    try{
	    	layout = new ISOMLayout<Instruction, Flow>(cfg.graph());
	    } catch(Exception e) {
	    	System.out.println("layout: " + e);
	    }
	    
	    layout.setSize(new Dimension(2500,1250));
	    
	    VisualizationViewer<Instruction, Flow> vv = new VisualizationViewer<Instruction, Flow>(layout);

	    //vv.setPreferredSize(new Dimension(350,350));
	    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);	    
	    
        DefaultModalGraphMouse<Instruction, Flow> gm = new DefaultModalGraphMouse<Instruction, Flow>();
        gm.setMode(DefaultModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        
	    this.frame.getContentPane().add(vv);
		this.frame.pack();
	}
	
	public void show() {
		this.frame.setVisible(true);
		System.out.println("frame.setVisible(true)");
		cfg.printGraph();
	}
	
	private class CFG {
		private SparseMultigraph<Instruction, Flow> g;
		
		public CFG(Function f) {
			g = new SparseMultigraph<Instruction, Flow>();
			
			Collection<Instruction> instrs = f.getInstructions();
			Iterator<Instruction> instr_iter = instrs.iterator();
			while(instr_iter.hasNext()) {
				Instruction si = instr_iter.next();
				Iterator<Long> next_instr = si.getNextAddresses().iterator();
				while(next_instr.hasNext()) {
					Long ni = next_instr.next();
					Instruction di = f.getInstruction(ni);
					
					if(si == null || di == null)
						continue;
					
					Flow fl = new Flow(si, di, FlowType.FLOW_NORMAL);
					
					try {
						g.addEdge(fl,  si, di, EdgeType.DIRECTED);
					} catch(Exception e) {
						System.out.println("g.addEdge: " + e);
					}
				}
			}
		}
			
		public Graph<Instruction, Flow> graph() {
			return this.g;
		}
		
		public void printGraph() {
			System.out.println("Edge count: " + g.getEdgeCount());
			System.out.println("Vertex count: " + g.getVertexCount());
			
			System.out.println("----- Edges -----");
			Collection<Flow> edges = g.getEdges();
			Iterator<Flow> ei = edges.iterator();
			while(ei.hasNext()) {
				System.out.println("Edge: " + ei.next());
			}
			
			System.out.println("----- Vertices -----");
			Collection<Instruction> vertices = g.getVertices();
			Iterator<Instruction> vi = vertices.iterator();
			while(vi.hasNext()) {
				System.out.println("Vertex: " + vi.next());
			}

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