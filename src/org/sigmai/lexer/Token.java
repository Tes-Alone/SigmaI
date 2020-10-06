package org.sigmai.lexer;

public class Token implements Comparable<Token> {
	public int type;
	public int start;
	public int length;
	public boolean isHighLightWord;
	public String lexeme;
	
	public Token() {}
	
	public Token(int start, int length, int type, 
							boolean isHighLightWord, String lexeme) {
		this.type   = type;
		this.start  = start;
		this.length = length;
		this.lexeme = lexeme;
		this.isHighLightWord = isHighLightWord;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Token) {
			Token other = (Token)obj;
			return this.type == other.type 
					&& this.start == other.start
						&& this.length == other.length
							&& this.lexeme.equals(lexeme)
								&& this.isHighLightWord == other.isHighLightWord;
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(Token other) {
		return Integer.compare(start, other.start);
	}
}
