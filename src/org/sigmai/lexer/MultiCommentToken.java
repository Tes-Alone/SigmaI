package org.sigmai.lexer;

public class MultiCommentToken implements Comparable<MultiCommentToken> {
	
	public int start;
	public int end;
	
	@Override
	public int compareTo(MultiCommentToken other) {
		return Integer.compare(start, other.start);
	}
}
