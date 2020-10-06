package org.sigmai.lexer;

public class RangeToken implements Comparable<RangeToken> {
	public int start;
	public int end;
	
	public RangeToken(int start, int end) {
		this.start = start;
		this.end   = end;
	}

	@Override
	public int compareTo(RangeToken other) {
		return Integer.compare(start, other.start);
	}
}
