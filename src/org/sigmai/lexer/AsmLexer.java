package org.sigmai.lexer;

import java.util.HashSet;

public class AsmLexer extends BaseLexer {
	
	public static final int TK_ASM_COMMENT  = 0;
	public static final int TK_ASM_OPERATOR = 1;
	public static final int TK_ASM_STRING   = 2;
	public static final int TK_ASM_BASEINSTR  = 3;
	public static final int TK_ASM_NUMBER     = 4;
	public static final int TK_ASM_SYSINSTR   = 5;
	public static final int TK_ASM_MEDIAINSTR = 6;
	public static final int TK_ASM_ID		  = 7;
	public static final int TK_ASM_REGISTER1  = 8;
	public static final int TK_ASM_MACRO	  = 9;
	public static final int TK_ASM_DIRECTIVE  = 10;
	public static final int TK_ASM_PSEINSTR   = 11;
	public static final int TK_ASM_REGISTER2  = 12;
	
	{
		tkComment = TK_ASM_COMMENT;
		tkNumber  = TK_ASM_NUMBER;
		tkString  = TK_ASM_STRING;
		tkChar    = TK_ASM_STRING;
	}

	private static StylePalette stylePalette;
	
	@Override
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	private static HashSet<String> baseInstr;
	private static HashSet<String> systemInstr;
	private static HashSet<String> mediaInstr;
	private static HashSet<String> registers1;
	private static HashSet<String> registers2;
	private static HashSet<String> directives;
	private static HashSet<String> pseudoInstr;
	
	private static HashSet<Character> operatorSet;
	
	static {
		baseInstr = new HashSet<String>();
		systemInstr = new HashSet<String>();
		mediaInstr = new HashSet<String>();
		registers1 = new HashSet<String>();
		registers2 = new HashSet<String>();
		directives = new HashSet<String>();;
		pseudoInstr = new HashSet<String>();
		
		stylePalette = new StylePalette(13);
		
		operatorSet = new HashSet<>();
		operatorSet.add('[');
		operatorSet.add(']');
		operatorSet.add(',');
		operatorSet.add('+');
		operatorSet.add('-');
		operatorSet.add('*');
		operatorSet.add('/');
		operatorSet.add('>');
		operatorSet.add('<');
		operatorSet.add('!');
		operatorSet.add('~');
		operatorSet.add('^');
		operatorSet.add('%');
		operatorSet.add('|');
		operatorSet.add('&');
		operatorSet.add('(');
		operatorSet.add(')');
		operatorSet.add('$');
		operatorSet.add('.');
		operatorSet.add(':');
	}
	
	@Override
	protected void scan() {
		if (!isEnd() && isMacroStart(current())) {
			addMacro(offset());
		} else if (!isEnd() && isCommentStart(current())) {
			addComment(offset());
		} else {
			super.scan();
		}
	}
	
	public void setBaseInstrList(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				baseInstr.add(w);
			}
		}
	}
	
	public void setSystemInstrList(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				systemInstr.add(w);
			}
		}
	}
	
	public void setMediaInstrList(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				mediaInstr.add(w);
			}
		}
	}
	
	public void setRegister1List(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				registers1.add(w);
			}
		}
	}
	
	public void setRegister2List(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				registers2.add(w);
			}
		}
	}
	
	public void setDirectiveList(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				directives.add(w);
			}
		}
	}
	
	public void setPseudoInstrList(String[] words) {
		if (words == null) {
			throw new NullPointerException();
		}
		for (var w : words) {
			if (w!=null && !w.isEmpty()) {
				pseudoInstr.add(w);
			}
		}
	}
	
	private boolean isMacroStart(char c) {
		return c == '%';
	}
	
	private void addMacro(int offset) {
		advance();
		Token token = new Token();
		token.start = offset;
		while (!isEnd(1) && !isMacroEnd(current())) {
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
					token.type =TK_ASM_MACRO;
					addToken(token);
					token = new Token();
					token.start = tmp;
					token.length = offset()-tmp;
					token.isHighLightWord = true;
					token.type = TK_ASM_MACRO;
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
		token.type = TK_ASM_MACRO;
		addToken(token);
		
		addRange(offset, offset());
	}

	protected boolean isMacroEnd(char c) {
		return c=='\n' || c=='\r' || c==';';
	}
	
	private void addComment(int offset) {
		advance();
		addSingleComment0(offset);
		
		addRange(offset, offset());
	}
	
	private boolean isCommentStart(char c) {
		return c == ';';
	}
	
	@Override
	protected boolean isNumberPart(char c) {
		return super.isNumberPart(c) || c=='_'; 
	}
	
	@Override
	protected void addWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		boolean isSingleWord = isSingleWord(offset, offset());
		String lexeme = subString(offset, offset());
		String word   = subString(offset, offset()).toLowerCase();
		if (isSingleWord && baseInstr.contains(word)) {
			addToken(new Token(offset, 
							lexeme.length(), TK_ASM_BASEINSTR, isHighLightWord(lexeme), null));
		} else if (isSingleWord && systemInstr.contains(word)) {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_SYSINSTR, isHighLightWord(lexeme), null));
		} else if (isSingleWord && mediaInstr.contains(word)) {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_MEDIAINSTR, isHighLightWord(lexeme), null));
		} else if (isSingleWord && registers1.contains(word)) {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_REGISTER1, isHighLightWord(lexeme), null));
		} else if (isSingleWord && registers2.contains(word)) {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_REGISTER2, isHighLightWord(lexeme), null));
		} else if (isSingleWord && directives.contains(word)) {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_DIRECTIVE, isHighLightWord(lexeme), null));
		} else if (isSingleWord && pseudoInstr.contains(word)) {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_PSEINSTR, isHighLightWord(lexeme), null));
		} else {
			addToken(new Token(offset, 
					lexeme.length(), TK_ASM_ID, isHighLightWord(lexeme), null));
		}
		addWord(lexeme);
	}

	private boolean isSingleWord(int i, int j) {
		String left  = " ";
		String right = " ";
		try {
			left = subString(i-1, i);
		} catch (StringIndexOutOfBoundsException e) {}
		try {
			right = subString(j, j+1);
		} catch (StringIndexOutOfBoundsException e) {}
		return !(isIDPart(left.charAt(0)) || isIDPart(right.charAt(0)));
	}

	private boolean isIDPart(char c) {
		return c=='.' || c=='$' || c=='#' || c=='@' || c=='~';
	}

	@Override
	protected HashSet<Character> getOperatorSet() {
		return operatorSet;
	}
}
