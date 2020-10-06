package org.sigmai.lexer;

public class FoldToken implements Comparable<FoldToken> {
	public int start;
	public int end;
	
	public FoldToken(int start, int end) {
		this.start = start;
		this.end   = end;
	}

	@Override
	public int compareTo(FoldToken o) {
		return Integer.compare(start, o.start);
	}
	
	@Override
	public String toString() {
		return "" + start + ":" + end;
	}
}
