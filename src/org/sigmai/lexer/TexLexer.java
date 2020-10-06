package org.sigmai.lexer;

public class TexLexer extends Lexer {

	public static final int TK_TEX_COMMENT 	= 0;
	public static final int TK_TEX_OPERATOR	= 1;
	public static final int TK_TEX_OPTION   = 2;
	public static final int TK_TEX_ARGUMENT = 3;
	public static final int TK_TEX_COMMAND  = 4;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(5);
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	@Override
	public StylePalette getStylePalette() {
		return stylePalette;
	}

	@Override
	protected void scan() {
		if (!isEnd() && isCommandStart(current())) {
			addCommand(offset());
		} else if (!isEnd() && isArgumentStart(current())) {
			addArgument(offset());
		} else if (!isEnd() && isOptionStart(current())) {
			addOption(offset());
		} else if (!isEnd() && isSymbol(current())) {
			addSymbol(offset());
		} else if (!isEnd() && isCommentStart(current())) {
			addComment(offset());
		} else if (!isEnd() && Character.isWhitespace(current())) {
			advance();
		} else {
			collectHighLightWord(offset());
		}
	}
	
	private void addComment(int offset) {
		advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd() && !isCommentEnd(current())) {
			token = collectSplitToken(token);
		}
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = TK_TEX_COMMENT;
		addToken(token);
		
		addRange(offset, offset());
	}
	
	private Token collectSplitToken(Token token) {
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
				token.type = TK_TEX_COMMENT;
				addToken(token);
				token = new Token();
				token.start = tmp;
				token.length = offset()-tmp;
				token.isHighLightWord = true;
				token.type = TK_TEX_COMMENT;
				addToken(token);
				token = new Token();
				token.start = offset();
			}
		} else {
			advance();
		}
		return token;
	}

	private boolean isCommentEnd(char c) {
		return c=='\r' || c=='\n';
	}

	private boolean isCommentStart(char c) {
		return c == '%';
	}

	private boolean isSymbol(char c) {
		return c=='}' || c==']' || c=='*' || c=='$';
	}

	private void collectHighLightWord(int offset) {
		while (!isEnd() && !isContentEnd(current())) {
			if (!isEnd() && isWordStart(current())) {
				addHighLightWord(offset());
			} else {
				advance();
			}
		}
	}

	private void addHighLightWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		
		String lexeme = subString(offset, offset());
		if (isHighLightWord(lexeme)) {
			Token token = new Token();
			token.start  = offset;
			token.length = offset()-offset;
			token.isHighLightWord = true;
			addToken(token);
		}
		addWord(lexeme);
	}

	private boolean isWordPart(char c) {
		return isWordStart(c);
	}

	private boolean isWordStart(char c) {
		return Character.isLetterOrDigit(c);
	}

	private boolean isContentEnd(char c) {
		return c=='\\' || c=='[' || c=='{' || c=='%';
	}

	private void addOption(int offset) {
		addSymbol(offset);
		offset = offset();
		while (!isEnd() && !isOptionEnd(current())) {
			advance();
		}
		int len = offset() - offset;
		if (len != 0) {
			Token token = new Token();
			token.start = offset;
			token.length = len;
			token.type = TK_TEX_OPTION;
			addToken(token);
		}
	}

	private boolean isOptionEnd(char c) {
		return c == ']';
	}

	private boolean isOptionStart(char c) {
		return c == '[';
	}

	private void addSymbol(int offset) {
		advance();
		Token token = new Token();
		token.start = offset;
		token.length = 1;
		token.type = TK_TEX_OPERATOR;
		addToken(token);
	}

	private void addArgument(int offset) {
		addSymbol(offset);
		offset = offset();
		while (!isEnd() && !isArgumentEnd(current())) {
			advance();
		}
		int len = offset() - offset;
		if (len != 0) {
			Token token = new Token();
			token.start = offset;
			token.length = len;
			token.type = TK_TEX_ARGUMENT;
			addToken(token);
		}
	}

	private boolean isArgumentEnd(char c) {
		return c == '}' || c == '{';
	}

	private boolean isArgumentStart(char c) {
		return c == '{';
	}

	private void addCommand(int offset) {
		advance();
		while (!isEnd() && isCommandPart(current())) {
			advance();
		}
		Token token = new Token();
		token.start = offset;
		token.length = offset() - offset;
		token.type = TK_TEX_COMMAND;
		addToken(token);
	}

	private boolean isCommandPart(char c) {
		return Character.isLetter(c);
	}

	private boolean isCommandStart(char c) {
		return c == '\\';
	}

}
