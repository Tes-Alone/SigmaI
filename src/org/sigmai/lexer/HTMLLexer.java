package org.sigmai.lexer;

public class HTMLLexer extends Lexer {

	public static final int TK_HTML_TAGNAME   = 21;
	public static final int TK_HTML_ATTRNAME  = 20;
	public static final int TK_HTML_ATTRVALUE = 19;
	public static final int TK_HTML_SYMBOL 	 = 18;
	public static final int TK_HTML_CONTENT  = 17;
	public static final int TK_HTML_KEYWORDTAG  = 16;
	public static final int TK_HTML_KEYWORDATTR = 15;
	
	public static final int TK_CSS_SELECTOR = 14;
	public static final int TK_CSS_PROPERTY = 13;
	public static final int TK_CSS_VALUE    = 12;
	public static final int TK_CSS_SYMBOL 	= 11;
	public static final int TK_CSS_BRACE 	= 10;
	public static final int TK_CSS_NUMBER 	= 9;
	
	public static final int TK_JS_BRACE    = 8;
	public static final int TK_JS_ID	   = 7;
	public static final int TK_JS_KEYWORD2 = 6;
	public static final int TK_JS_KEYWORD1 = 5;
	public static final int TK_JS_NUMBER   = 4;
	public static final int TK_JS_CHAR     = 3;
	public static final int TK_JS_STRING   = 2;
	public static final int TK_JS_OPERATOR = 1;
	public static final int TK_HTML_COMMENT = 0;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(22);
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	@Override
	protected void scan() {		
		if (!isEnd(1) && isOpenTagStart(current(), charUntil(1))) {
			startAttrName = true;
			addOpenTagStyle(offset());
		} else if (!isEnd(2) && isCloseTagStart(current(), charUntil(1), charUntil(2))) {
			addCloseTagStyle(offset());
		} else if (!isEnd() && isAttrNameStart(current())) {
			addAttrNameStyle(offset());
		} else if (!isEnd(1) && isSelfCloseSymbol(current(), charUntil(1))) {
			startAttrName = false; 
			addSymbolStyle(offset());
			addSymbolStyle(offset());
		} else if (!isEnd() && isAttrValueStart(current(), '\'')) { //is attrValue
			addAttrValueStyle(offset(), '\'');
		} else if (!isEnd() && isAttrValueStart(current(), '"')) { //is attrValue
			addAttrValueStyle(offset(), '"');
		} else if (!isEnd() && current()=='>') { // big
			startAttrName = false;
			styleStart  = styleHalfStart;
			scriptStart = scriptHalfStart;
			addSymbolStyle(offset());
		} else if (!isEnd() && isContentStart(current())) { // 对 content 的判断要放在 big 之后.
			addContentStyle(offset());
		} else if (!isEnd() && current()=='=') { // equ
			addSymbolStyle(offset());
		} else if (!isEnd(3) && isCommentStart(current(), charUntil(1), charUntil(2), charUntil(3))) {
			addComment(offset());
		} else {
			advance();
		}
	}

	@Override
	public boolean isSupportMultiComment() {
		return true;
	}

	private boolean isSelfCloseSymbol(char c, char d) {
		return c=='/' && d=='>';
	}

	private String[] jsWords1 = new String[0];
	
	public void setJavaScriptWords1(String[] words) {
		if (words == null)
			throw new NullPointerException();
		for (String word : words) {
			if (word == null) 
				throw new NullPointerException();
		}
		jsWords1 = words;
	}
	
	private String[] jsWords2 = new String[0];
	
	public void setJavaScriptWords2(String[] words) {
		if (words == null)
			throw new NullPointerException();
		for (String word : words) {
			if (word == null) 
				throw new NullPointerException();
		}
		jsWords2 = words;
	}
	
	private String[] cssWords1 = new String[0];
	
	public void setCSSWords1(String[] words) {
		if (words == null)
			throw new NullPointerException();
		for (String word : words) {
			if (word == null) 
				throw new NullPointerException();
		}
		cssWords1 = words;
	}
	
	private String[] cssWords2 = new String[0];
	
	public void setCSSWords2(String[] words) {
		if (words == null)
			throw new NullPointerException();
		for (String word : words) {
			if (word == null) 
				throw new NullPointerException();
		}
		cssWords2 = words;
	}
	
	private void addCloseTagStyle(int offset) {
		addSymbolStyle(offset);
		addSymbolStyle(offset());
		offset = offset();
		advance();
		while (!isEnd(0) && isTagNamePart(current())) {
			advance();
		}
		
		String lexeme = subString(offset, offset());
		Token  token  = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		
		if (isKeyWord1(lexeme)) {
			token.type = TK_HTML_KEYWORDTAG;
		} else {
			token.type = TK_HTML_TAGNAME;
		}
		
		if (isHighLightWord(lexeme)) {
			token.isHighLightWord = true;
		}
		token.lexeme = lexeme;
		addToken(token);
		addWord(lexeme);
		
		if (lexeme.equalsIgnoreCase("script")) {
			scriptHalfStart = false;
			scriptStart = false;
		} else if (lexeme.equalsIgnoreCase("style")) {
			styleHalfStart = false;
			styleStart = false;
		}
	}

	private boolean isCloseTagStart(char c, char d, char e) {
		return c=='<' && d=='/' && (Character.isLetter(e)||e=='_');
	}
	
	private boolean isOpenTagStart(char c, char d) {
		return c=='<' && (Character.isLetter(d) || d=='_');
	}
	
	private boolean isTagNamePart(char c) {
		return Character.isLetterOrDigit(c) || c=='_'	|| c=='-' || c==':';
	}
	
	private boolean startAttrName;
	
	private boolean isAttrNameStart(char c) {
		return startAttrName && (Character.isLetter(c) || c=='_');
	}
	
	private boolean isAttributeNamePart(char c) {
		return Character.isLetterOrDigit(c) || c=='_' || c=='-';
	}
	
	private boolean isAttrValueStart(char c, char d) {
		return c == d;
	} 
	
	private boolean isContentStart(char c) {
		return !startAttrName && c!='<';
	}
	
	private static final char CONTENT_END = '<';
	
	private void addContentStyle(int offset) {
		if (scriptStart) {
			var assist = new JavaScriptLexer();
			assist.tkBrace = TK_JS_BRACE;
			assist.tkChar  = TK_JS_CHAR;
			assist.tkComment = TK_HTML_COMMENT;;
			assist.tkCommentDoc = TK_HTML_COMMENT;
			assist.tkID = TK_JS_ID;
			assist.tkKeyWord1 = TK_JS_KEYWORD1;
			assist.tkKeyWord2 = TK_JS_KEYWORD2;
			assist.tkNumber = TK_JS_NUMBER;
			assist.tkString = TK_JS_STRING;
			assist.setKeyWord1(jsWords1);
			assist.setKeyWord2(jsWords2);
			assist.tokenization(offset, getText(), getHighLightWord());
			addTokenList(assist.getTokens());
			addWordList(assist.getWordList());
			addMultiCommentTokenList(assist.getMultiCommentTokenList());
			addRangeTokenList(assist.getRangeTokenList());
			setOffset(assist.offset());
		} else if (styleStart) {
			var assist = new CSSLexer();
			assist.tkSelector = TK_CSS_SELECTOR;
			assist.tkProperty = TK_CSS_PROPERTY;
			assist.tkValue = TK_CSS_VALUE;
			assist.tkSymbol = TK_CSS_SYMBOL;
			assist.tkBrace = TK_CSS_BRACE;
			assist.tkComment = TK_HTML_COMMENT;
			assist.tkNumber = TK_CSS_NUMBER;
			assist.setKeyWord1(cssWords1);
			assist.setKeyWord1(cssWords2);
			assist.tokenization(offset, getText(), getHighLightWord());
			addTokenList(assist.getTokens());
			addWordList(assist.getWordList());
			addMultiCommentTokenList(assist.getMultiCommentTokenList());
			addRangeTokenList(assist.getRangeTokenList());
			setOffset(assist.offset());
		} else {
			while (!isEnd() && current()!=CONTENT_END) {
				if (!isEnd() && isWordStart(current())) {
					addWord(offset());
				} else {
					advance();
				}
			}
		}
	}

	private void addWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		
		String lexeme = subString(offset, offset());
		Token token = new Token();
		token.start = offset;
		token.length = offset() - offset;
		token.type = TK_HTML_CONTENT;
		if (isHighLightWord(lexeme)) {
			token.isHighLightWord = true;
		}
		addToken(token);
		addWord(lexeme);
	}

	private boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || c=='_';
	}

	private boolean isWordStart(char c) {
		return Character.isLetter(c) || c=='_';
	}
	
	private boolean scriptStart;
	private boolean styleStart;

	private void addSymbolStyle(int offset) {
		advance();
		Token token = new Token();
		token.start  = offset;
		token.length = 1;
		token.type   = TK_HTML_SYMBOL;
		token.lexeme = charAt(offset) + "";
		addToken(token);
	}

	private void addAttrValueStyle(int offset, char quot) {
		advance(); 
		while (!isEnd() && (current()!=quot)) {
			if (!isEnd(3) && (isAmp(current(), charUntil(1), 
									charUntil(2), charUntil(3)))) {
				advance(); advance(); advance();
			} else if (!isEnd(2) && (isBigOrLess(current(), 
									charUntil(1), charUntil(2)))) {
				advance(); advance();
			} else {
				advance();
			}
		}
		
		if (!isEnd(0)) advance();
		
		Token token = new Token();
		token.start = offset;
		token.length = offset()-offset;
		token.type   = TK_HTML_ATTRVALUE;
		addToken(token);
	}

	private boolean isBigOrLess(char c, char d, char e) {
		return c=='&' && (d=='g'||d=='l') && e=='t';
	}
	
	private boolean isAmp(char c, char d, char e, char f) {
		return c=='&' && d=='a' && e=='m' && f=='p';
	}

	private	void addAttrNameStyle(int offset) {
		advance(); 
		while (!isEnd(0) && isAttributeNamePart(current())) {
			advance();
		}
		
		String lexeme = subString(offset, offset());
		Token  token = new Token();
		token.start = offset;
		token.length = offset() - offset;
		
		if (isKeyWord2(lexeme)) {
			token.type = TK_HTML_KEYWORDATTR;
		} else {
			token.type = TK_HTML_ATTRNAME;
		}
		
		if (isHighLightWord(lexeme)) {
			token.isHighLightWord = true;
		}
		addToken(token);
		addWord(lexeme);
	}
	
	private boolean scriptHalfStart;
	private boolean styleHalfStart;

	private void addOpenTagStyle(int offset) {
		addSymbolStyle(offset);
		offset = offset();
		advance();
		while (!isEnd() && isTagNamePart(current())) {
			advance();
		}
		
		String lexeme = subString(offset, offset());
		Token  token = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		
		if (isKeyWord1(lexeme)) {
			token.type = TK_HTML_KEYWORDTAG;
		} else {
			token.type = TK_HTML_TAGNAME;
		}
		
		if (isHighLightWord(lexeme)) {
			token.isHighLightWord = true;
		}
		token.lexeme = lexeme;
		addToken(token);
		addWord(lexeme);
		
		if (lexeme.equalsIgnoreCase("script")) {
			scriptHalfStart = true;
		} else {
			scriptHalfStart = false;
		}
		
		if (lexeme.equalsIgnoreCase("style")) {
			styleHalfStart = true;
		} else {
			styleHalfStart = false;
		}
	}
	
	private void addComment(int offset) {
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
		token.type = TK_HTML_COMMENT;
		addToken(token);
		
		addRange(offset, offset());
	}

	private boolean isCommentStart(char c, char d, char e, char f) {
		return c=='<' && d=='!' && e=='-' && f=='-';
	}


	@Override
	protected void reset() {
		super.reset();
		startAttrName = false;
		scriptHalfStart = false;
		styleHalfStart = false;
		scriptStart = false;
		styleStart = false;
	}
}
