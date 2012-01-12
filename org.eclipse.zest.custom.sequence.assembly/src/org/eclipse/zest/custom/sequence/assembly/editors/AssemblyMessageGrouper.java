package org.eclipse.zest.custom.sequence.assembly.editors;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.custom.sequence.widgets.Activation;
import org.eclipse.zest.custom.sequence.widgets.MessageGroup;
import org.eclipse.zest.custom.sequence.widgets.UMLItem;
import org.eclipse.zest.custom.uml.viewers.IMessageGrouper;
import org.eclipse.zest.custom.uml.viewers.IMessageGrouping;
import org.eclipse.zest.custom.uml.viewers.MessageGrouping;
import org.eclipse.zest.custom.uml.viewers.UMLSequenceViewer;

/**
 * Uses the AST of the java model to discover groups for the passed messages.
 */

public class AssemblyMessageGrouper implements IMessageGrouper {

	private enum Colors {
		Green, Blue, Red, LightGreen, LightBlue, LightRed;
		Color c;

		Color getColor() {
			switch (this) {
			case Green:
				if (c == null) {
					c = new Color(Display.getCurrent(), 0, 100, 0);
				}
				return c;

			case Red:
				if (c == null) {
					c = new Color(Display.getCurrent(), 100, 0, 0);
				}
				return c;
			case Blue:
				if (c == null) {
					c = new Color(Display.getCurrent(), 0, 0, 100);
				}
				return c;
			case LightGreen:
				if (c == null) {
					c = new Color(Display.getCurrent(), 225, 255, 225);
				}
				return c;

			case LightRed:
				if (c == null) {
					c = new Color(Display.getCurrent(), 255, 225, 225);
				}
				return c;
			case LightBlue:
				if (c == null) {
					c = new Color(Display.getCurrent(), 225, 225, 255);
				}
				return c;
			}

			return null;
		}

		void dispose() {
			if (c != null && !c.isDisposed()) {
				c.dispose();
			}
			c = null;
		}
	}

	private static class MappedMessageGrouping extends MessageGrouping {

		private Object key;

		/**
		 * @param activationElement
		 * @param offset
		 * @param length
		 * @param name
		 */
		public MappedMessageGrouping(Object activationElement, int offset, int length, String name, Object key) {
			super(activationElement, offset, length, name);
			this.key = key;
		}

		public Object getKey() {
			return key;
		}

	}

	public IMessageGrouping[] calculateGroups(UMLSequenceViewer viewer, Object activationElement, Object[] children) {

		/*
		 * HashMap<ASTNode, MappedMessageGrouping> groups = new HashMap<ASTNode,
		 * MappedMessageGrouping>(); if (!(activationElement instanceof
		 * IAdaptable)) { return new IMessageGrouping[0]; } ASTNode
		 * activationNode =
		 * (ASTNode)((IAdaptable)activationElement).getAdapter(ASTNode.class);
		 * if (!(activationNode instanceof MethodDeclaration)) { return new
		 * IMessageGrouping[0]; } for (int i = 0; i < children.length; i++) { if
		 * (children[i] instanceof IAdaptable) { ASTNode messageNode =
		 * (ASTNode)((IAdaptable)children[i]).getAdapter(ASTNode.class); if
		 * (messageNode != null) { ASTNode currentParent =
		 * messageNode.getParent(); while (currentParent != null &&
		 * currentParent != activationNode) { ASTNode block = null; String text
		 * = null; Color c = null; Color bc = null; String expressionString =
		 * ""; switch (currentParent.getNodeType()) { case ASTNode.IF_STATEMENT:
		 * block = checkIfSide((IfStatement)currentParent, messageNode); if
		 * (block != null && block ==
		 * ((IfStatement)currentParent).getElseStatement()) { text = "else"; }
		 * else if (block == ((IfStatement)currentParent).getThenStatement()) {
		 * text = "if (" +
		 * ((IfStatement)currentParent).getExpression().toString() + ")"; } c =
		 * Colors.Blue.getColor(); bc = Colors.LightBlue.getColor(); break; case
		 * ASTNode.WHILE_STATEMENT: if
		 * (((WhileStatement)currentParent).getExpression() != null) {
		 * expressionString =
		 * ((WhileStatement)currentParent).getExpression().toString(); } text =
		 * "while (" +expressionString + ")"; block = currentParent; c =
		 * Colors.Green.getColor(); bc = Colors.LightGreen.getColor(); break;
		 * case ASTNode.FOR_STATEMENT: if
		 * (((ForStatement)currentParent).getExpression() != null) {
		 * expressionString =
		 * ((ForStatement)currentParent).getExpression().toString(); } else {
		 * expressionString = ";;"; } text = "for (" + expressionString + ")";
		 * block = currentParent; c = Colors.Green.getColor(); bc =
		 * Colors.LightGreen.getColor(); break; case ASTNode.TRY_STATEMENT: text
		 * = "try"; block = currentParent; c = Colors.Red.getColor(); bc =
		 * Colors.LightRed.getColor(); break; case ASTNode.CATCH_CLAUSE: text =
		 * "catch (" +((CatchClause)currentParent).getException().toString()
		 * +")"; block = currentParent; c = Colors.Red.getColor(); bc =
		 * Colors.LightRed.getColor(); break; case ASTNode.DO_STATEMENT: text =
		 * "do while (" +
		 * ((DoStatement)currentParent).getExpression().toString() + ")"; block
		 * = currentParent; c = Colors.Green.getColor(); bc =
		 * Colors.LightGreen.getColor(); break; } if (text != null) {
		 * MappedMessageGrouping grouping = groups.get(block); if (grouping ==
		 * null) { grouping = new MappedMessageGrouping(activationElement, i, 1,
		 * text, block); grouping.setBackground(bc); grouping.setForeground(c);
		 * groups.put(block, grouping); } else { int length = (i -
		 * grouping.getOffset()) + 1; grouping.setLength(length); } }
		 * currentParent = currentParent.getParent(); } } } }
		 */
		if (activationElement instanceof NodeProxy) {
			NodeProxy np = (NodeProxy) activationElement;
			if (np.loopOffset > -1) {
				HashMap<String, MessageGroup> groups = new HashMap<String, MessageGroup>();
				/*
				 * MappedMessageGrouping map = new
				 * MappedMessageGrouping(activationElement, np.loopOffset,
				 * np.loopLength, "", Integer.toString(np.loopOffset));
				 * groups.put(Integer.toString(np.loopOffset), map);
				 */

				MessageGroup group = new MessageGroup(viewer.getChart());
				
				group.setText("");
				group.setData("");

				UMLItem[] items = viewer.getChart().getItems();
				Activation current = viewer.getChart().getRootActivation();
				
				for (int j = 0; j < items.length; j++) {
					if (items[j] instanceof Activation) {
						Activation act = (Activation) items[j];
						System.out.println(act.getLifeline().getText());
						if (act.getLifeline().getText().equals(np.targetName) && act.getLifeline().getParent().getText().equals(np.module)){
							int actIndex = 0;
							Activation[] acts = act.getLifeline().getActivations();
							Activation act2 = acts[Integer.parseInt(np.act)];
						
							//if(Integer.toString(actIndex).equals(np.act)) {			
								System.out.println(actIndex);
								current = act2;
								break;
							//}
						}
					}
				}

				group.setRange(current, 0, np.loopLength);
				groups.put(Integer.toString(np.loopOffset), group);

				ArrayList<MessageGroup> groupList = new ArrayList<MessageGroup>(groups.values());
				return groupList.toArray(new IMessageGrouping[groupList.size()]);
			}
		}
		ArrayList<MappedMessageGrouping> groupList = new ArrayList<MappedMessageGrouping>();// groups.values());
		/*
		 * Collections.sort(groupList, new Comparator<MappedMessageGrouping>(){
		 * public int compare(MappedMessageGrouping o1, MappedMessageGrouping
		 * o2) { ASTNode n1 = (ASTNode) o1.getKey(); ASTNode n2 = (ASTNode)
		 * o2.getKey(); return n1.getStartPosition() - n2.getStartPosition(); }
		 * });
		 */
		return groupList.toArray(new IMessageGrouping[groupList.size()]);
	}

	/**
	 * @param currentParent
	 * @param messageNode
	 * @return
	 */
	/*
	 * private ASTNode checkIfSide(IfStatement ifStatement, ASTNode messageNode)
	 * { ASTNode currentParent = messageNode.getParent(); ASTNode thenStatement
	 * = ifStatement.getThenStatement(); ASTNode elseStatement =
	 * ifStatement.getElseStatement(); while (currentParent != null &&
	 * currentParent != ifStatement) { if (currentParent == thenStatement) {
	 * return thenStatement; } else if (currentParent == elseStatement) { return
	 * elseStatement; } currentParent = currentParent.getParent(); } return
	 * null;
	 * 
	 * }
	 */

	public void dispose() {
		Colors.Red.dispose();
		Colors.Green.dispose();
		Colors.Blue.dispose();
		Colors.LightRed.dispose();
		Colors.LightGreen.dispose();
		Colors.LightBlue.dispose();
	}

}
