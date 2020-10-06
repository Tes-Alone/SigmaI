package org.sigmai.lexer;

import java.util.HashSet;

public class MakefileLexer extends Lexer {	
	
	public static final int TK_MF_TARGET   = 4;
	public static final int TK_MF_VARIABLE = 3;
	public static final int TK_MF_NONE	   = 2;
	public static final int TK_MF_SYMBOL   = 1;
	public static final int TK_MF_COMMENT  = 0;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(5);
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	private boolean isNewLine;
	
	private static final HashSet<Character> symbolSet;
	
	static {
		symbolSet = new HashSet<Character>();
		symbolSet.add('$');
		symbolSet.add('(');
		symbolSet.add(')');
		symbolSet.add('{');
		symbolSet.add('}');
		symbolSet.add('^');
		symbolSet.add('@');
		symbolSet.add('%');
		//symbolSet.add('+');
		symbolSet.add('*');
		symbolSet.add('<');
		symbolSet.add('"');
		symbolSet.add('\'');
		symbolSet.add(',');
		symbolSet.add('|');
		symbolSet.add(';');
		//symbolSet.add(':');
		//symbolSet.add('=');
		//symbolSet.add('?');
	}
	
	@Override
	protected void scan() {
		if (!isEnd() && isLineEnd(current())) {
			isNewLine = true;  filterNewLine();
		} else if (!isEnd() && isNewLine && current()=='\t') {
			isNewLine = false; addRecipe(offset());
		} else if (!isEnd() && isNewLine && current()=='#') {
			isNewLine = false; addComment(offset());
		} else if (!isEnd() && isNewLine) {
			isNewLine = false; addTargetOrVariable(offset());
		} else if (!isEnd() && isWordStart(current())) {
			addWord(offset());
		} else if (!isEnd() && isSymbol(current())) {
			addSymbol(offset());
		} else if (!isEnd() && current()=='\\') {
			crossNewLine();
		} else {
			advance();
		}
	}
	
	private void crossNewLine() {
		advance();
		if (!isEnd(1) && current()=='\r'&&charUntil(1)=='\n') {
			advance();
			advance();
		} else if (!isEnd() && isLineEnd(current())) {
			advance();
		}
	}

	private void filterNewLine() {
		if(!isEnd(1) && current()=='\r' && charUntil(1)=='\n') {
			advance(); advance();
		} else {
			advance();
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
		token.type = TK_MF_COMMENT;
		addToken(token);
		
		addRange(offset, offset());
	}

	private boolean isCommentEnd(char c) {
		return c=='\r' || c=='\n';
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
				token.type = TK_MF_COMMENT;
				addToken(token);
				token = new Token();
				token.start = tmp;
				token.length = offset()-tmp;
				token.isHighLightWord = true;
				token.type = TK_MF_COMMENT;
				addToken(token);
				token = new Token();
				token.start = offset();
			}
		} else {
			advance();
		}
		return token;
	}

	private void addSymbol(int offset) {
		boolean isDollar = current()=='$';
		advance();
		Token token = new Token();
		
		if (!isEnd() && isDollar && (current()=='+'||current()=='?')) { 
			//fix 当 rule 中 含有 + 时, 如果 symbolSet 中含有 +, 则 g++ 的 + 会被视为 symbol.
			advance();
			token.length = 2;
		} else {
			token.length = 1;
		}
		token.start = offset;
		token.type = TK_MF_SYMBOL;
		addToken(token);
	}

	private boolean isSymbol(char c) {
		return symbolSet.contains(c);
	}

	private void addWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		String lexeme = subString(offset, offset());
		//System.out.println(lexeme);
		if (isHighLightWord(lexeme)) {
			Token token = new Token();
			token.type   = TK_MF_NONE; 
			token.start  = offset;
			token.length = offset()-offset;
			token.isHighLightWord = true;
			addToken(token);
		}
		addWord(lexeme);
	}

	private void addTargetOrVariable(int offset) {
		while (!isEnd() && current()!=':'&&current()!='='
						&&current()!='?'&&current()!='+'&&!isLineEnd(current())) {
			if (!isEnd() && isWordStart(current())) {
				addWord(offset());
			} else {
				advance();
			}
		}
		Token token = new Token();
		if ((!isEnd(2)&&current()==':'&&charUntil(1)==':'&&charUntil(2)=='=')||
				(!isEnd(1)&&current()=='+'&&charUntil(1)=='=') || 
					(!isEnd(1)&&current()==':'&&charUntil(1)=='=') ||
						(!isEnd(1)&&current()=='?'&&charUntil(1)=='=') ||
							(!isEnd() && current()=='=')) {
			token.type = TK_MF_VARIABLE;
		} else if (!isEnd() && current()==':') {
			token.type = TK_MF_TARGET;
		} else {
			token.type = TK_MF_NONE;
		}
		token.start  = offset;
		token.length = offset() - offset;
		addToken(token);
	}

	private void addRecipe(int offset) {
		advance();
		while (!isEnd() && !isLineEnd(current())) {
			if (!isEnd() && isWordStart(current())) {
				int tmp = offset();
				while (!isEnd() && isWordPart(current())) {
					advance();
				}
				String lexeme = subString(tmp, offset());
				addWord(lexeme);
				if (isHighLightWord(lexeme)) {
					var token = new Token();
					token.start = tmp;
					token.length = offset()-tmp;
					token.isHighLightWord = true;
					token.type = TK_MF_NONE;
					addToken(token);
				}
			} else if (current() == '\\') {
				advance();
				if (!isEnd(1) && (current()=='\r' && charUntil(1)=='\n')) { // for windows
					advance(); advance();
				} else if (!isEnd() && (current()=='\r' || current()=='\n')) { // for Linux or MacOS
					advance();
				}
			} else if (isSymbol(current())) {
				int tmp = offset();
				boolean isDollar = current()=='$';
				advance();
				var token = new Token();
				token.start = tmp;
				
				if (!isEnd() && isDollar && (current()=='+'||current()=='?')) {
					//fix 当 rule 中 含有 + 时, 如果 symbolSet 中含有 +, 则 g++ 的 + 会被视为 symbol.
					advance();
					token.length = 2;
				} else {
					token.length = 1;
				}
				
				token.type = TK_MF_SYMBOL;
				addToken(token);
				token = new Token();
				token.start = offset();
			} else {
				advance();
			}
		}
	}

	private boolean isWordStart(char c) {
		return Character.isLetter(c) || c=='_';
	}
	
	private boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || c=='_';
	}

	private boolean isLineEnd(char c) {
		return c=='\r' || c=='\n';
	}

	@Override
	protected void reset() { 
		super.reset();
		isNewLine = true;
	}
}
