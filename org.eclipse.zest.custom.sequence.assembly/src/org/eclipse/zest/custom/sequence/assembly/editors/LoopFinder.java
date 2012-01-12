package org.eclipse.zest.custom.sequence.assembly.editors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Finds tandem repeats, and contained tandem repeats, in an array of objects.
 * Simple O(n2) algorithm. 
 * @author Del Myers, Jennifer Baldwin
 */

public class LoopFinder {

	static HashMap<String, String> lengths = new HashMap<String, String>();
	static int actualLoopLength = -99;
	
	/**
	 * Defines a region in the original array that contains a loop. If a region starts at
	 * <i>i</i> and has length <i>l</i> and iterations <i>t</i>, then sub-"strings" in
	 * the original array [i, i+l-1], [i+l, i+l+l-1],...,[i+tl, i+2tl-1] are all equal.
	 * If a loop region contains sub-regions, then those sub regions are also mirrored in
	 * the equal substrings, offset by i+lt.
	 * @author Del Myers
	 */
	public static class LoopRegion implements Comparable<LoopRegion>{
		public final int offset;
		public final int length;
		public final LoopRegion parent;
		public final int iterations;
		private final TreeSet<LoopRegion> children;
		public int callLength = -1;
		final int looplength = -99;
		
		protected LoopRegion(LoopRegion parent, int offset, int length, final int iterations) {
			children = new TreeSet<LoopRegion>();
			this.offset = offset;
			this.length = length;
			this.parent = parent;
			this.iterations = iterations;
			if (parent != null) {
				parent.addChild(this);
			}
		}
		private void addChild(LoopRegion child) {
			this.children.add(child);
		}
		public int compareTo(LoopRegion that) {
			int diff = this.offset - that.offset;
			if (diff == 0) {
				diff = this.length - that.length;
			}
			return diff;
		}
		public SortedSet<LoopRegion> getChildren() {
			return children;
		}
		@Override
		public String toString() {
			return "[" + offset + "," +(offset+length-1)+"]:" + iterations; 
		}
	}
	
	
	public static<T> List<LoopRegion> findLoops(T[] array) {
		return findLoops(array, 0, array.length, null);
	}
	
	public static<T> List<LoopRegion> findLoops(T[] array, int offset, int length, LoopRegion parent) {
		int end = offset+length-1;
		//set the head of the search to the beginning of the list
		List<LoopRegion> regions = new LinkedList<LoopRegion>();
		for (int left = offset; left <= end; left++) {
			
			//search backwards from the middle of the list to the front, looking for loops.
			for (int right = (end - left + 1)/2 + left; right > left; right--) {
				
				if (array[left].equals(array[right])) {
					//Found the beginning of a match. A full loop must be the
					//same length as the difference from left to right.
					int loopLength = right-left;
					
					//check successive substrings of size "loopLength" to see
					//if they match the substring of size loopLength starting
					//at "left".
					int iteration = 1;
					boolean loopBreak = false; //for when the pattern is broken.
					
					//TODO this while loop is fucked
					while (!loopBreak) {
						for (int loopIndex = 0; loopIndex < loopLength; loopIndex++) {
							int rightCheck = left+loopLength*iteration+loopIndex;
							if (rightCheck > end) {
								loopBreak = true;
								break;
							}
							if (!array[left + loopIndex].equals(array[rightCheck])) {
								loopBreak = true;
								break;
							}
						}
						if (!loopBreak) {
							iteration++;
						}
					}
					
					//System.out.println("iteration " + iteration);
					//System.out.println("length " + loopLength);
					
					if (iteration > 1) {
						//we found a loop
						//check for the degenerate case in which the entire loop is just
						//a repetition. Otherwise, recurse.
						boolean single = true; 
						for (int loopIndex = left+1; loopIndex < left+loopLength && single; loopIndex++) {
							if (!array[loopIndex].equals(array[loopIndex-1])){
								single = false;
							}
						}
						if (single) {
							//System.out.println("base case " + loopLength);
							if (loopLength == 1) {
								//just add one region which is the number of iterations
								regions.add(new LoopRegion(parent, left, 1, iteration));
							} else {
								boolean addone = false;
								if ((left+loopLength*iteration <= end)) {
									//check to see if we need to add one more.
									addone = array[loopLength*iteration+left].equals(array[left]);
								}
								//just add a region of length one that is 2* the loop length
								regions.add(new LoopRegion(parent, left, 1, loopLength*2 + ((addone) ? 1 : 0)));
							}
						} else {
							//add one region, and recurse on it. There is no reason to add
							//the sub-regions to the region list, as it will be added to the parent.
							LoopRegion region = new LoopRegion(parent, left, loopLength, iteration);
							//System.out.println(loopLength);
							//System.out.println("recurse");
							actualLoopLength = loopLength;
							
							System.out.println("offset " + region.offset + " length " + loopLength);
							
							if(!lengths.containsKey("" + region.offset)){
								lengths.put("" + region.offset, "" + loopLength);
							}
							findLoops(array, left, loopLength, region);
							regions.add(region);
						}
						//check this region with the last one to see if it is equal
						//if it is, double the last one.
						if (regions.size() > 1) {
							//System.out.println("double");
							LoopRegion thisRegion = regions.get(regions.size()-1);
							LoopRegion lastRegion = regions.get(regions.size()-2);
							if (thisRegion.length == lastRegion.length && (thisRegion.offset==lastRegion.offset+(lastRegion.length*lastRegion.iterations))) {
								boolean equal = true;
								for (int i = 0; i < thisRegion.length && equal; i++) {
									T thisMethod = array[thisRegion.offset+i];
									T lastMethod = array[lastRegion.offset+i];
									if (!thisMethod.equals(lastMethod)) {
										equal = false;
									}
								}
								if (equal) {
									regions.remove(regions.size()-1);
									regions.remove(regions.size()-1);
									regions.add(new LoopRegion(parent, lastRegion.offset, lastRegion.length, lastRegion.iterations+1));
								}
							}
						} 
						//don't need to re-check the region again.
						left = left + loopLength*iteration-1;
					}
				}
			}
		}
		//System.out.println(length);
		/*for (int i = 0; i < regions.size(); i++) {
			String off = "" + regions.get(i).offset;
			String len = "" + (length/2);
			System.out.println(" len " + len);
			if(!lengths.containsKey(off)){
				System.out.println("put in at " + off + " the value " + len);
				lengths.put(off, len);
			}
		}
		System.out.println(lengths);*/
		return regions;
	}
	
	
}
