package org.sigmai.lexer;

public class JavaLexer extends CPPLexer {
	
	public static final int TK_JAVA_COMMENTDOC = 9;
	public static final int TK_JAVA_BRACE    = 8;
	public static final int TK_JAVA_ID 	     = 7;
	public static final int TK_JAVA_KEYWORD2 = 6;
	public static final int TK_JAVA_KEYWORD1 = 5;
	public static final int TK_JAVA_NUMBER   = 4;
	public static final int TK_JAVA_CHAR     = 3;
	public static final int TK_JAVA_STRING   = 2;
	public static final int TK_JAVA_OPERATOR = 1;
	public static final int TK_JAVA_COMMENT  = 0;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(10);
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	{
		tkKeyWord1 = TK_JAVA_KEYWORD1;
		tkKeyWord2 = TK_JAVA_KEYWORD2;
		tkNumber   = TK_JAVA_NUMBER;
		tkComment  = TK_JAVA_COMMENT;
		tkID	   = TK_JAVA_ID;
		tkString   = TK_JAVA_STRING;
		tkChar     = TK_JAVA_CHAR;
		tkOperator = TK_JAVA_OPERATOR;
		tkCommentDoc = TK_JAVA_COMMENTDOC;
		tkBrace	   = TK_JAVA_BRACE;
	}
	
	static {
		operatorSet.add('@');
	}
	
	@Override
	protected boolean isMacroStart(char c) {
		return false;
	}
	
	@Override
	protected boolean isWideStringStart(char c, char d, char e) {
		return false;
	}
	
	@Override
	protected boolean isRawStringStart(char c, char d) {
		return false;
	}
	
	@Override
	protected boolean isWordStart(char c) {
		return Character.isLetter(c) || c == '_';
	}
	
	@Override
	protected boolean isWordPart(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
	
	@Override
	protected boolean isNumberPart(char c) {
		return super.isNumberPart(c) || c=='_';
	}
}
