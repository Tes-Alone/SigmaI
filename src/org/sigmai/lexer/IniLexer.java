package org.sigmai.lexer;

public class IniLexer extends Lexer {

	public static final int TK_INI_COMMENT		= 0;
	public static final int TK_INI_SYMBOL		= 1;
	public static final int TK_INI_SECTION_HEAD = 2;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(3);
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	@Override
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	private boolean isEquLeftPart;
	
	@Override
	protected void scan() {
		if (!isEnd() && (current()=='\n'||current()=='\r')) {
			isEquLeftPart = true; advance();
		} else if (!isEnd() && isSectionHead(current())) {
			addSectionHead(offset());
		} else if (!isEnd() && isSingleCommentStart(current())) {
			addSingleComment(offset());
			isEquLeftPart = true;
		} else if (!isEnd() && isSymbol(current())) {
			addSymbol(offset());
		} else if (!isEnd() && isWordStart(current())) {
			addWord(offset());
		} else {
			advance();
		}
	}
	
	private boolean isSymbol(char c) {
		return  c=='=' || c=='"' || c=='\'' || c=='{' 
					|| c=='}' || c=='(' || c==')' || c=='<' || c=='>';
	}

	private void addWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		String lexeme = subString(offset, offset());
		if (isHighLightWord(lexeme)) {
			Token token = new Token();
			token.type   = TK_INI_SECTION_HEAD;
			token.start  = offset;
			token.length = offset()-offset;
			token.isHighLightWord = true;
			addToken(token);
		}

		addWord(lexeme);
	}

	private boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || c=='_';
	}

	private boolean isWordStart(char c) {
		return Character.isLetter(c) || c=='_';
	}

	private void addSymbol(int offset) {
		isEquLeftPart = false;
		Token token = new Token();
		token.start  = offset;
		token.length = 1;
		token.isHighLightWord = false;
		token.type = TK_INI_SYMBOL;
		addToken(token);
		advance();
	}

	private void addSectionHead(int offset) {
		advance();
		while (!isEnd() && !isSectionHeadEnd(current())) {
			advance();
		}
		
		if (!isEnd() && current()==']') {
			advance();
		}
		
		Token token = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		token.isHighLightWord = false;
		token.type = TK_INI_SECTION_HEAD;
		addToken(token);
	}

	private boolean isSectionHeadEnd(char c) {
		return c=='\r'||c=='\n'||c==']';
	}

	@Override
	protected void reset() {
		super.reset();
		isEquLeftPart = true;
	}

	private boolean isSectionHead(char c) {
		return isEquLeftPart && c=='[';
	}
	
	private boolean isSingleCommentStart(char c) {
		return isEquLeftPart && c==';';
	}
	
	private void addSingleComment(int offset) {
		advance();
		while (!isEnd() && !isSingleCommentEnd(current())) {
			advance();
		}
		Token token  = new Token();
		token.start  = offset;
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = TK_INI_COMMENT;
		addToken(token);
	}
	
	private boolean isSingleCommentEnd(char c) {
		return c=='\r' || c=='\n';
	}
}
