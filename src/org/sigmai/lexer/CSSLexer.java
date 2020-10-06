package org.sigmai.lexer;

import java.util.HashSet;

public class CSSLexer extends Lexer {

	public static final int TK_CSS_COMMENT  = 0;
	public static final int TK_CSS_SYMBOL 	= 1;
	public static final int TK_CSS_PROPERTY = 2;
	public static final int TK_CSS_VALUE    = 3;
	public static final int TK_CSS_NUMBER 	= 4;
	public static final int TK_CSS_BRACE 	= 5;
	public static final int TK_CSS_SELECTOR = 6;
	
	protected int tkComment;
	protected int tkSelector;
	protected int tkProperty;
	protected int tkValue;
	protected int tkBrace;
	protected int tkSymbol;
	protected int tkNumber;
	
	{
		tkComment = TK_CSS_COMMENT;
		tkSelector = TK_CSS_SELECTOR;
		tkProperty = TK_CSS_PROPERTY;
		tkValue = TK_CSS_VALUE;
		tkBrace = TK_CSS_BRACE;
		tkSymbol = TK_CSS_SYMBOL;
		tkNumber = TK_CSS_NUMBER;
	}
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(7);
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	@Override
	public StylePalette getStylePalette() {
		return stylePalette;
	}

	private boolean passColon;
	private boolean inBrace;
	
	@Override
	protected void scan() {
		if (!isEnd(3) && isHTMLCommentStart(current(), charUntil(1), 
				charUntil(2), charUntil(3))) {
			addHTMLComment(offset());
		} else if (!isEnd(1) && isCommentStart(current(), charUntil(1))) {
			addComment(offset());
		} else if (!isEnd(1) && !passColon && isIllegalLineStart(current(), charUntil(1))) {
			addIllegalLine(offset());
		} else if (!isEnd(1) && isWordStart(current(), charUntil(1))) {
			addWord(offset());
		} else if (!isEnd(1) && inBrace && isNumberStart(current(), charUntil(1))) {
			addNumber(offset());
		} else if (!isEnd() && isSymbol(current())) {
			addSymbol(offset());
		} else if (!isEnd() && current()=='{') {
			addBrace(offset(), true); inBrace = true; passColon = false;
		} else if (!isEnd() && current()=='}') {
			addBrace(offset(), false); inBrace = false;
		} else {
			advance();
		}
	}
	
	private void addHTMLComment(int offset) {
		advance(); advance(); advance(); advance();
		while (!isEnd(2) && (current()!='-'||
				charUntil(1)!='-'||charUntil(2)!='>')) {
			advance();
		}
		
		if (!isEnd(2)) {advance();}
		if (!isEnd(1)) {advance();}
		if (!isEnd()) {advance();}
		Token token = new Token();
		token.start = offset;
		token.length = offset() - offset;
		token.type = TK_CSS_COMMENT;
		addToken(token);
		
		addRange(offset, offset());
	}

	private boolean isHTMLCommentStart(char c, char d, char e, char f) {
		return c=='<' && d=='!' && e=='-' && f=='-';
	}
	
	private void addIllegalLine(int offset) {
		advance();	advance();
		while (!isEnd() && !isIllegalLineEnd(current())) {
			advance();
		}
		Token token = new Token();
		token.start  = offset;
		token.length = offset()-offset;
		token.type   = tkComment;
		addToken(token);
		
		addRange(offset, offset());
	}

	private boolean isIllegalLineEnd(char c) {
		return c=='\r' || c=='\n' || c==';';
	}

	private boolean isIllegalLineStart(char c, char d) {
		return Character.isDigit(c) && (Character.isLetter(d)||d=='-'||d=='@'||d==':');
	}
	
	private void checkPassColonStatus(char c) {
		if (c==';' || c=='\r' || c=='\n') {
			passColon = false;
		} else if (c == ':') {
			passColon = true;
		}
	}

	private void addSymbol(int offset) {
		checkPassColonStatus(current());
		advance();
		Token token = new Token();
		token.start  = offset;
		token.length = 1;
		token.type   = tkSymbol;
		addToken(token);
	}
	
	private void addBrace(int offset, boolean isLeft) {
		advance();
		Token token  = new Token();
		token.start  = offset;
		token.length = 1;
		token.type   = tkBrace;
		addToken(token);
	}
	
	private static HashSet<Character> symbolSet;
	
	static {
		symbolSet = new HashSet<Character>();
		symbolSet.add(':');
		symbolSet.add('@');
		symbolSet.add(';');
		symbolSet.add('*');
		symbolSet.add('.');
		symbolSet.add('>');
		symbolSet.add('#');
		symbolSet.add('%');
		symbolSet.add('-');
		symbolSet.add('+');
		symbolSet.add(',');
		symbolSet.add('(');
		symbolSet.add(')');
		symbolSet.add('\'');
		symbolSet.add('"');
	}

	private boolean isSymbol(char c) {
		return symbolSet.contains(c);
	}
	
	private boolean isCommentStart(char c, char d) {
		return c == '/' && d == '*';
	}
	
	private void addNumber(int offset) {
		advance();
		while (!isEnd() && isNumberPart(current())) {
			advance();
		}
		Token token  = new Token();
		token.start  = offset;
		token.length = offset()-offset;
		token.type   = tkNumber;
		addToken(token);
	}

	private boolean isNumberPart(char c) {
		return Character.isLetterOrDigit(c) || c=='.';
	}

	private boolean isNumberStart(char c, char d) {
		return c=='#'||Character.isDigit(c)||(c=='.'&&Character.isDigit(d));
	}
	
	private void addComment(int offset) {
		advance();
		advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd(1) && !isCommentEnd(current(), charUntil(1))) {
			token = collectSplitToken(token);
		}
		
		if (!isEnd(1)) {
			advance();
			advance();
		} else if (!isEnd()) {
			advance();
		}
		
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = tkComment;
		addToken(token);
		
		var commentTk = new MultiCommentToken();
		commentTk.start = offset;
		commentTk.end = offset();
		addMultiCommentToken(commentTk);
		
		addRange(offset, offset());
	}
	
	@Override
	public boolean isSupportMultiComment() {
		return true;
	}
	
	private Token collectSplitToken(Token token) {
		if (!isEnd(1) && isWordStart(current(), charUntil(1))) {
			int tmp = offset();
			while (!isEnd() && isWordPart(current())) {
				advance();
			}
			String lexeme = subString(tmp, offset());
			addWord(lexeme);
			if (isHighLightWord(lexeme)) {
				token.length = tmp - token.start;
				token.isHighLightWord = false;
				token.type = tkComment;
				addToken(token);
				token = new Token();
				token.start = tmp;
				token.length = offset()-tmp;
				token.isHighLightWord = true;
				token.type = tkComment;
				addToken(token);
				token = new Token();
				token.start = offset();
			}
		} else {
			advance();
		}
		return token;
	}
	
	private void addWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		String lexeme = subString(offset, offset());
		Token token = new Token();
		token.start  = offset;
		token.length = offset()-offset;
		
		if (isHighLightWord(lexeme)) {
			token.isHighLightWord = true;
		}
		
		if (isKeyWord1(lexeme) && !passColon && inBrace) {
			token.type   = tkProperty;
		} else 
			 // 这里使用 keyword1 判断, 通过 passColon 确定类型.
			 // 这样处理是方便关键字的提供.
			if (isKeyWord1(lexeme) && passColon && inBrace) {
			token.type   =  tkValue;
		} else {
			token.type   = tkSelector;
		}
		addToken(token);
		addWord(lexeme);
	}

	private boolean isCommentEnd(char c, char d) {
		return c=='*' && d=='/';
	}
	
	private boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || c=='-' || c=='_';
	}

	
	private boolean isWordStart(char c, char d) {
		return Character.isLetter(c) || (c=='-'&&Character.isLetter(d));
	}
	
	@Override
	protected boolean isEnd() {
		return (!super.isEnd(7) && subString(offset(), offset()+8).equalsIgnoreCase("</style>")) ||
					super.isEnd();
	}
	
	@Override
	protected boolean isEnd(int need) {
		return (!super.isEnd(7) && subString(offset(), offset()+8).equalsIgnoreCase("</style>")) ||
					super.isEnd(need);
	}
	
	@Override
	public void tokenization(int startOffset, String text, String highLightWord) {
		setOffset(startOffset);
		setText(text);
		setHighLightWord(highLightWord);
		reset();
		while (scanHelp()) {}
	}
	
	@Override
	protected void reset() {
		super.reset();
		passColon = false;
		inBrace = false;
	}
	
	private boolean scanHelp() {
		if (isEnd()) return false;
		else {
			scan();
			if (!isEnd()) return true;
			return false;
		}
	}

}
