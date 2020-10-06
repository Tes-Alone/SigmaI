package org.sigmai.lexer;

public class DLexer extends JavaLexer {
	
	public static final int TK_D_MACRO     = 10;
	public static final int TK_D_RAWSTRING = 11;
	public static final int TK_D_COMMENTDOC = 9;
	
	public static final int TK_D_BRACE    = 8;
	public static final int TK_D_ID 	  = 7;
	public static final int TK_D_KEYWORD2 = 6;
	public static final int TK_D_KEYWORD1 = 5;
	public static final int TK_D_NUMBER   = 4;
	public static final int TK_D_CHAR     = 3;
	public static final int TK_D_STRING   = 2;
	public static final int TK_D_OPERATOR = 1;
	public static final int TK_D_COMMENT  = 0;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(12);
		operatorSet.add('$');
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	{
		tkKeyWord1 = TK_D_KEYWORD1;
		tkKeyWord2 = TK_D_KEYWORD2;
		tkNumber   = TK_D_NUMBER;
		tkComment  = TK_D_COMMENT;
		tkID	   = TK_D_ID;
		tkString   = TK_D_STRING;
		tkChar     = TK_D_CHAR;
		tkOperator = TK_D_OPERATOR;
		tkMacro	   = TK_D_MACRO;
		tkBrace	   = TK_D_BRACE;
		tkRawString = TK_D_RAWSTRING;
	}
	
	@Override
	protected void scan() {
		if (!isEnd(1) && isNestCommentStart(current(), charUntil(1))) {
			addNestComment(offset());
		} else if (!isEnd(1) && isStringStart(current(), charUntil(1))) {
			addString(offset());
		} else if (!isEnd() && isRawStringStart2(current())) {
			addRawString2(offset());
		} else {
			super.scan();
		}
	}

	private boolean isNestCommentStart(char c, char d) {
		return c=='/' && d=='+';
	}
	
	private void addString(int offset) {
		if (current()=='q') advance();
		advance();
		while (!isEnd(1) && !isStringEnd(current(), charUntil(1))) {
			if (!isEnd(1) && current()=='\\') {
				advance(); advance();
			} else if (!isEnd()) {
				advance();
			}
		}
		
		if (!isEnd(1) && (charUntil(1)=='c'
							||charUntil(1)=='d'
								||charUntil(1)=='w')) {
			advance(); advance();
		} else if (!isEnd()) {
			advance();
		}
		
		Token token = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		token.type   = TK_D_STRING;
		addToken(token);
		
		addRange(offset, offset()-1);
	}

	private boolean isStringEnd(char c, char d) {
		return c=='"' || (c=='"'&&(d=='c'||d=='w'||d=='d'));
	}
	
	private boolean isStringStart(char c, char d) {
		return c=='"' || (c=='q'&&d=='"');
	}
	
	private boolean isNestCommentEnd(char c, char d) {
		return c == '+' && d == '/';
	}

	private void addNestComment(int offset) {
		advance(); advance();
		int stack = 1;
		Token token = new Token();
		token.start = offset;
		while (!isEnd()) {
			if (!isEnd(1) && isNestCommentEnd(current(), charUntil(1))) {
				stack--;
				if (stack == 0) {
					break;
				}
			} else if (!isEnd(1) && isNestCommentStart(current(), charUntil(1))) {
				stack++;
			}
			token = collectHighLightWord(token, TK_D_COMMENT);
		}
		
		if (!isEnd(1)) {
			advance();
			advance();
		} else if (!isEnd()) {
			advance();
		}
		
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = TK_D_COMMENT;
		addToken(token);
		
		MultiCommentToken mcToken = new MultiCommentToken();
		mcToken.start = offset;
		mcToken.end = offset();
		addMultiCommentToken(mcToken);
		
		addRange(offset, offset());
	}

	@Override
	protected boolean isRawStringStart(char c, char d) {
		return c=='r' && d=='"';
	}
	
	@Override
	protected boolean isRawStringEnd(char c, char d) {
		return isStringEnd(c, d);
	}
	
	private boolean isRawStringStart2(char c) {
		return c =='`';
	}
	
	private boolean isRawStringEnd2(char c) {
		return c == '`';
	}
	
	private void addRawString2(int offset) {
		advance();
		while (!isEnd() && !isRawStringEnd2(current())) {
			advance();
		}
		
		if (!isEnd()) {
			advance();
		}
		
		Token token = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		token.type   = tkRawString;
		addToken(token);
		
		addRange(offset, offset()-1);
	}
	
	@Override
	protected boolean isMacroEnd(char c, char d) {
		return c=='\n' || c=='\r' || (c=='/'&&(d=='*'||d=='/'||d=='+'));
	}
	
	@Override
	protected boolean isMacroStart(char c) {
		return c == '#';
	}
	
	@Override
	protected boolean isNumberPart(char c) {
		return super.isNumberPart(c) || c=='_';
	}
	
	@Override
	protected void addNumber(int offset) {
		advance();
		while (!isEnd() && isNumberPart(current())) {
			if (!isEnd(1) && isSliceDots(current(), charUntil(1))) {
				addToken(new Token(offset, offset()-offset, tkNumber, false, null));
				addSliceDots();
				return;
			} else {
				advance();
			}
		}
		addToken(new Token(offset, offset()-offset, tkNumber, false, null));
	}

	private void addSliceDots() {
		addOperator(offset());
		addOperator(offset());
	}

	private boolean isSliceDots(char c, char d) {
		return c=='.' && d=='.';
	}
}
