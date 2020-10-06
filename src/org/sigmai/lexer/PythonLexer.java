package org.sigmai.lexer;

import java.util.HashSet;

public class PythonLexer extends BaseLexer {
	
	public static final int TK_PYTHON_ID	   = 7;
	public static final int TK_PYTHON_KEYWORD2 = 6;
	public static final int TK_PYTHON_KEYWORD1 = 5;
	public static final int TK_PYTHON_NUMBER   = 4;
	public static final int TK_PYTHON_CHAR     = 3;
	public static final int TK_PYTHON_STRING   = 2;
	public static final int TK_PYTHON_OPERATOR = 1;
	public static final int TK_PYTHON_COMMENT  = 0;
	
	public static final int TK_PYTHON_RAWSTRING  = 8;
	public static final int TK_PYTHON_LONGSTRING = 9;
	
	private static StylePalette stylePalette;
	private static HashSet<Character> operatorSet;
	
	static {
		operatorSet  = new HashSet<>();
		stylePalette = new StylePalette(10);
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	{
		tkKeyWord1 = TK_PYTHON_KEYWORD1;
		tkKeyWord2 = TK_PYTHON_KEYWORD2;
		tkNumber   = TK_PYTHON_NUMBER;
		tkComment  = TK_PYTHON_COMMENT;
		tkID	   = TK_PYTHON_ID;
		tkString   = TK_PYTHON_STRING;
		tkChar     = TK_PYTHON_CHAR;
		tkOperator = TK_PYTHON_OPERATOR;
	}
	
	static {
		operatorSet.add('$');
		operatorSet.add('~');
		operatorSet.add('{');
		operatorSet.add('}');
		operatorSet.add('@');
		operatorSet.add('~');
		operatorSet.add('!');
		operatorSet.add('%');
		operatorSet.add('^');
		operatorSet.add('&');
		operatorSet.add('*');
		operatorSet.add('(');
		operatorSet.add(')');
		operatorSet.add('-');
		operatorSet.add('+');
		operatorSet.add('=');
		operatorSet.add('[');
		operatorSet.add(']');
		operatorSet.add(':');
		operatorSet.add(';');
		operatorSet.add('|');
		operatorSet.add('<');
		operatorSet.add('>');
		operatorSet.add('?');
		operatorSet.add('.');
		operatorSet.add(',');
		operatorSet.add('/');
	}
	
	@Override
	protected void scan() {
		if (!isEnd() && isCommentStart(current())) {
			addComment(offset());
		} else if (!isEnd(1) && isRawStringStart(current(), charUntil(1))) {
			addRawString(offset());
		} else if (!isEnd(2) && isLongStringStart(current(), 
							charUntil(1), charUntil(2))) {
			addLongString(offset());
		} else {
			super.scan();
		}
	}
	
	private void addRawString(int offset) {
		advance();
		advance();
		while (!isEnd() && (!isRawStringEnd(current(), '"')
						&&!isRawStringEnd(current(), '\''))) {
			advance();
		}
		
		if (!isEnd()) {
			advance();
		}
		
		Token token = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		token.type   = TK_PYTHON_RAWSTRING;
		addToken(token);
		
		addRange(offset, offset()-1);
	}
	
	private boolean isRawStringEnd(char c, char d) {
		return super.isNormalStringEnd(c, d);
	}

	@Override
	protected boolean isWordStart(char c) {
		return Character.isLetter(c) || c == '_';
	}
	
	@Override
	protected boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
	
	private void addComment(int offset) {
		advance();
		addSingleComment0(offset);
		
		addRange(offset, offset());
	}
	
	private void addLongString(int offset) {
		advance(); advance(); advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd(2) && !isLongStringEnd(current(),
								charUntil(1), charUntil(2))) {
			token = collectHighLightWord(token, TK_PYTHON_LONGSTRING);
		}
		
		if (!isEnd(2)) {
			advance();
			advance();
			advance();
		} else if (!isEnd(1)) {
			advance();
			advance();
		} else if (!isEnd()) {
			advance();
		}
		
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = TK_PYTHON_LONGSTRING;
		addToken(token);
		
		addRange(offset, offset()-1);
	}
	
	private boolean isRawStringStart(char c, char d) {
		return (c=='r'||c=='R')&&(d=='"'||d=='\'');
	}
	
	private boolean isCommentStart(char c) {
		return c == '#';
	}
	
	private boolean isLongStringStart(char c, char d, char e) {
		return (c=='"'||c=='\'') && d==c && e==c;
	}
	
	private boolean isLongStringEnd(char c, char d, char e) {
		return isLongStringStart(c, d, e);
	}
	
	@Override
	protected HashSet<Character> getOperatorSet() {
		return operatorSet;
	}
}
