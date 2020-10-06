package org.sigmai.lexer;

public class JSPLexer extends HTMLLexer {

	protected void scan() {
		if (!isEnd(3) && isJspComment(current(), 
				charUntil(1), charUntil(2), charUntil(3))) {
			addJspComment(offset());
		} else if (!isEnd(1) && isJSPStart(current(), charUntil(1))) {
			addJSPStyle(offset());
		} else {
			super.scan();
		}
	}
	
	private void addJspComment(int offset) {
		advance(); advance();advance(); advance();
		while (!isEnd(3) && (current()!='-'
								||charUntil(1)!='-'
									||charUntil(2)!='%'
										||charUntil(3)!='>')) {
			advance();
		}
		
		if (!isEnd(3)) {advance();}
		if (!isEnd(2)) {advance();}
		if (!isEnd(1)) {advance();}
		if (!isEnd()) {advance();}
		Token token = new Token();
		token.start = offset;
		token.length = offset() - offset;
		token.type = TK_HTML_COMMENT;
		addToken(token);
		
		addRange(offset, offset());
	}

	private boolean isJspComment(char c, char d, char e, char f) {
		return c=='<' && d=='%' && e=='-' && f==e;
	}

	private boolean isJSPStart(char c, char d) {
		return c =='<' && d == '%';
	}
	
	private String[] jspWords1 = new String[0];
	private String[] jspWords2 = new String[0];
	
	public void setJspWords1(String[] words) {
		if (words == null)
			throw new NullPointerException();
		for (String word : words) {
			if (word == null) 
				throw new NullPointerException();
		}
		jspWords1 = words;
	}
	
	public void setJspWords2(String[] words) {
		if (words == null)
			throw new NullPointerException();
		for (String word : words) {
			if (word == null) 
				throw new NullPointerException();
		}
		jspWords2 = words;
	}
	
	/* 
	 * %> 
	 **/
	private void addEndSymbol() {
		Token token = new Token();
		token.start = offset();
		token.length = 2;
		token.type = TK_JS_OPERATOR;
		token.lexeme = subString(offset(), offset()+2);
		addToken(token);
		advance(); advance();	
	}
	
	private void addJSPStyle(int offset) {
		var assist = new JSPLexerHelp();
		assist.setKeyWord1(jspWords1);
		assist.setKeyWord2(jspWords2);
		assist.tkBrace = TK_JS_BRACE;
		assist.tkChar  = TK_JS_CHAR;
		assist.tkComment = TK_HTML_COMMENT;;
		assist.tkCommentDoc = TK_HTML_COMMENT;
		assist.tkID = TK_JS_ID;
		assist.tkKeyWord1 = TK_JS_KEYWORD1;
		assist.tkKeyWord2 = TK_JS_KEYWORD2;
		assist.tkNumber = TK_JS_NUMBER;
		assist.tkString = TK_JS_STRING;
		assist.tokenization(offset, getText(), getHighLightWord());
		addTokenList(assist.getTokens());
		addWordList(assist.getWordList());
		addMultiCommentTokenList(assist.getMultiCommentTokenList());
		addRangeTokenList(assist.getRangeTokenList());
		setOffset(assist.offset());
		if (offset()<getText().length()) {
			addEndSymbol();
		}
	}
}
