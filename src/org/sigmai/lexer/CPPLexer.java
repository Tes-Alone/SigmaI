package org.sigmai.lexer;

import java.util.HashSet;

public class CPPLexer extends BaseLexer {
	public static final int TK_CPP_BRACE    = 8;
	public static final int TK_CPP_ID	    = 7;
	public static final int TK_CPP_KEYWORD2 = 6;
	public static final int TK_CPP_KEYWORD1 = 5;
	public static final int TK_CPP_NUMBER   = 4;
	public static final int TK_CPP_CHAR     = 3;
	public static final int TK_CPP_STRING   = 2;
	public static final int TK_CPP_OPERATOR = 1;
	public static final int TK_CPP_COMMENT  = 0;
	
	public static final int TK_CPP_COMMENTDOC = 9;
	public static final int TK_CPP_RAWSTRING  = 10;
	public static final int TK_CPP_WIDESTRING = 11;
	public static final int TK_CPP_MACRO      = 12;
	
	private static StylePalette stylePalette;
	protected static HashSet<Character> operatorSet;
	
	static {
		stylePalette = new StylePalette(13);
		operatorSet = new HashSet<>();
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	protected int tkRawString = TK_CPP_RAWSTRING;
	protected int tkMacro     = TK_CPP_MACRO;
	protected int tkCommentDoc = TK_CPP_COMMENTDOC;
	protected int tkBrace	  = TK_CPP_BRACE;
	
	{
		tkComment = TK_CPP_COMMENT;
		//foldTokenStack = new Stack<>();
	}
	
	@Override
	protected void scan() {
		if (!isEnd(1) && isWideStringStart(current(), charUntil(1), '\'')) {
			addWideString(offset(), '\'');
		} else if (!isEnd(1) && isWideStringStart(current(), charUntil(1), '"')) {
			addWideString(offset(), '"');
		} else if (!isEnd(1) && isRawStringStart(current(), charUntil(1))) {
			addRawString(offset());
		} else  if(!isEnd() && isMacroStart(current())) {
			addMacro(offset());
		} else if (!isEnd(3) && isCommentDocStart(
						current(), charUntil(1), charUntil(2), charUntil(3))) {
			addCommentDoc(offset());
		} else if (!isEnd(1) && isSingleCommentStart(current(), charUntil(1))) {
			addSingleComment(offset());
		} else if (!isEnd(1) && isMultiCommentStart(current(), charUntil(1))) {
			addMultiComment(offset());
		} else if (!isEnd() && current()=='{') {
			addBrace(offset(), true);
		} else if (!isEnd() && current()=='}') {
			addBrace(offset(), false);
		} else {
			super.scan();
		}
	}

	private void addCommentDoc(int offset) {
		advance();advance();advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd(1) && !isMultiCommentEnd(current(), charUntil(1))) {
			token = collectHighLightWord(token, tkCommentDoc);
		}
		
		if (!isEnd(1)) {
			advance();
			advance();
		} else if (!isEnd()) {
			advance();
		}
		
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = tkCommentDoc;
		addToken(token);
		
		var commentTk = new MultiCommentToken();
		commentTk.start = offset;
		commentTk.end = offset();
		addMultiCommentToken(commentTk);
		
		addRange(offset, offset());
	}

	protected boolean isCommentDocStart(char c, char d, char e, char f) {
		return c=='/' && d=='*' && e=='*' && f!='/';
	}

	private void addMultiComment(int offset) {
		advance();
		advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd(1) && !isMultiCommentEnd(current(), charUntil(1))) {
			token = collectHighLightWord(token, tkComment);
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

	protected boolean isMultiCommentStart(char c, char d) {
		return c=='/' && d=='*';
	}

	private boolean isMultiCommentEnd(char c, char d) {
		return c=='*' && d=='/';
	}
	
	private void addSingleComment(int offset) {
		advance(); advance();
		addSingleComment0(offset);
		
		addRange(offset, offset());
	}

	protected boolean isSingleCommentStart(char c, char d) {
		return c=='/' && d=='/';
	}
	
	//private Stack<FoldToken> foldTokenStack;

	private void addBrace(int offset, boolean isLeft) {
		advance();
		Token token = new Token();
		token.start  = offset;
		token.length = 1;
		token.type   = tkBrace;
		addToken(token);
		/*
		if (isLeft) {
			FoldToken ft = new FoldToken(offset, 0);
			foldTokenStack.push(ft);
		} else {
			FoldToken ft = foldTokenStack.pop();
			ft.end = offset;
			addFoldToken(ft);
		}*/
		
	}

	private void addRawString(int offset) {
		advance();
		advance();
		while (!isEnd() && !isRawStringEnd(current(), '"')) {
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

	protected boolean isRawStringEnd(char c, char d) {
		return super.isNormalStringEnd(c, d);
	}

	protected boolean isRawStringStart(char c, char d) {
		return c == 'R' && d == '"';
	}
	
	@Override
	public boolean isSupportMultiComment() {
		return true;
	}
	
	static {
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

	private void addMacro(int offset) {
		advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd(1) && !isMacroEnd(current(), charUntil(1))) {
			if (!isEnd() && isWordStart(current())) {
				int tmp = offset();
				while (!isEnd() && isWordPart(current())) {
					advance();
				}
				String lexeme = subString(tmp, offset());
				addWord(lexeme);
				if (isHighLightWord(lexeme)) {
					token.length = tmp - token.start;
					token.isHighLightWord = false;
					token.type = tkMacro;
					addToken(token);
					token = new Token();
					token.start = tmp;
					token.length = offset()-tmp;
					token.isHighLightWord = true;
					token.type = tkMacro;
					addToken(token);
					token = new Token();
					token.start = offset();
				}
			} else if (current() == '\\') {
				advance();
				if (!isEnd(1) && (current()=='\r' && charUntil(1)=='\n')) { // for windows
					advance(); advance();
				} else if (!isEnd() && (current()=='\r' || current()=='\n')) { // for Linux or MacOS
					advance();
				}
			} else {
				advance();
			}
		}
		if (isEnd(1) && !isEnd()) {
			advance();
		}
		
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = tkMacro;
		addToken(token);
		
		addRange(offset, offset());
	}

	protected boolean isMacroEnd(char c, char d) {
		return c=='\n' || c=='\r' || (c=='/'&&(d=='*'||d=='/'));
	}

	protected boolean isMacroStart(char c) {
		return c == '#';
	}

	private void addWideString(int offset, char c) {
		advance();
		addNormalString(offset, c);
	}

	protected boolean isWideStringStart(char c, char d, char e) {
		return (c=='L'||c=='U'||c=='u') && d == e;
	}

	@Override
	protected HashSet<Character> getOperatorSet() {
		return operatorSet;
	}
}
