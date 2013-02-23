package rcpapp.cartographer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import cs.uvic.ca.ice.model.Function;
import cs.uvic.ca.ice.model.Instruction;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
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
	private JToolBar toolbar;
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
	    
	    layout.setSize(new Dimension(2048,2048));
	    
	    VisualizationViewer<Instruction, Flow> vv = new VisualizationViewer<Instruction, Flow>(layout);
  
	    vv.setVertexToolTipTransformer(new ToStringLabeller());
	    //vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
	    //vv.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);	    
	    
	    //vv.setPreferredSize(new Dimension(350,350));
        vv.setPreferredSize(null);
	    
        DefaultModalGraphMouse<Instruction, Flow> gm = new DefaultModalGraphMouse<Instruction, Flow>();
        gm.setMode(DefaultModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        vv.addKeyListener(gm.getModeKeyListener());
              
		this.toolbar = new JToolBar("Views", JToolBar.HORIZONTAL);
		
		JButton filterButton = new JButton("Joins");
		filterButton.addActionListener(new FilterJoinAction(vv));		
		this.toolbar.add(filterButton);
		
		JButton callButton = new JButton("Calls");
		callButton.addActionListener(new FilterCallAction(vv));
		this.toolbar.add(callButton);
		
		JButton loopButton = new JButton("Loops");
		loopButton.addActionListener(new FilterLoopAction(vv));
		this.toolbar.add(loopButton);
		
		this.frame.getContentPane().add(this.toolbar,BorderLayout.NORTH);
        		
	    this.frame.getContentPane().add(vv);
		this.frame.pack();
	}
	
	public void show() {
		this.frame.setVisible(true);
	}
	
	private class FilterLoopAction extends AbstractAction {
		private VisualizationViewer<Instruction, Flow> vv;
		
		public FilterLoopAction(VisualizationViewer<Instruction, Flow> vv) {
			super();
			
			this.vv = vv;
		}

		public void actionPerformed(ActionEvent e) {
			TarjanSCC looper = new TarjanSCC(vv.getGraphLayout().getGraph());
			final Collection<Collection<Instruction>> cci = looper.identify();
		
		    Transformer<Instruction,Paint> vertexPainter = new Transformer<Instruction,Paint>() {
	            public Paint transform(Instruction i) {
	            	for(Collection<Instruction> c : cci)
	            		if(c.contains(i))
	            			return Color.BLUE;
	            	
	            	return Color.LIGHT_GRAY;
	            }
	        };
		    
		    vv.getRenderContext().setVertexFillPaintTransformer(vertexPainter);
			vv.repaint();
		}
	}
	
	private class FilterCallAction extends AbstractAction {
		private VisualizationViewer<Instruction, Flow> vv;
		
		public FilterCallAction(VisualizationViewer<Instruction, Flow> vv) {
			super();
			
			this.vv = vv;
		}
				
		public void actionPerformed(ActionEvent arg0) {
		    Transformer<Instruction,Paint> vertexPainter = new Transformer<Instruction,Paint>() {
	            public Paint transform(Instruction i) {
	            	if(i.isCall())
	            		return Color.GREEN;
	            	
	            	return Color.LIGHT_GRAY;
	            }
	        };
		    
		    vv.getRenderContext().setVertexFillPaintTransformer(vertexPainter);
		    vv.repaint();
		}
	}
	
	private class FilterJoinAction extends AbstractAction {
		private VisualizationViewer<Instruction, Flow> vv;
		
		public FilterJoinAction(VisualizationViewer<Instruction, Flow> vv) {
			super();
				
			this.vv = vv;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			final Graph<Instruction, Flow> graph = vv.getGraphLayout().getGraph();
			
			VertexPredicateFilter<Instruction, Flow> vpf = new VertexPredicateFilter<Instruction, Flow>(new Predicate<Instruction>() {
				public boolean evaluate(Instruction arg0) {
					if(graph.getNeighborCount(arg0) > 2)
						return true;
					
					return false;
				}
			});
			final Graph<Instruction, Flow> g = vpf.transform(graph);
			
		    Transformer<Instruction,Paint> vertexPainter = new Transformer<Instruction,Paint>() {
	            public Paint transform(Instruction i) {
	            	if(g.containsVertex(i))
	            		return Color.RED;
	            	
	            	return Color.LIGHT_GRAY;
	            }
	        };
		    
		    vv.getRenderContext().setVertexFillPaintTransformer(vertexPainter);
			vv.repaint();
		}
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
	
	public enum FlowType {
		FLOW_NORMAL, FLOW_JUMP, FLOW_CALL
	}
}