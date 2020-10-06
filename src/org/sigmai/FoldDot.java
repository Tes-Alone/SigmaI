package org.sigmai;

class FoldDot implements Comparable<FoldDot> {
	boolean folded;
	int startOffset;
	int endOffset;
	int startLine;
	int endLine;
	String content;
	
	@Override
	public int compareTo(FoldDot o) {
		return Integer.compare(startLine, o.startLine);
	}
}
