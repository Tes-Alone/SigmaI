package org.sigmai.lexer;

public class JavaScriptLexer extends JavaLexer {

	public static final int TK_JS_BRACE    = 8;
	public static final int TK_JS_ID	   = 7;
	public static final int TK_JS_KEYWORD2 = 6;
	public static final int TK_JS_KEYWORD1 = 5;
	public static final int TK_JS_NUMBER   = 4;
	public static final int TK_JS_CHAR     = 3;
	public static final int TK_JS_STRING   = 2;
	public static final int TK_JS_OPERATOR = 1;
	public static final int TK_JS_COMMENT  = 0;
	
	private static StylePalette stylePalette;
	
	static {
		stylePalette = new StylePalette(9);
	}
	
	public StylePalette getStylePalette() {
		return stylePalette;
	}
	
	public static StylePalette getStylePaletteStatic() {
		return stylePalette;
	}
	
	{
		tkKeyWord1 = TK_JS_KEYWORD1;
		tkKeyWord2 = TK_JS_KEYWORD2;
		tkNumber   = TK_JS_NUMBER;
		tkComment  = TK_JS_COMMENT;
		tkID	   = TK_JS_ID;
		tkString   = TK_JS_STRING;
		tkChar     = TK_JS_CHAR;
		tkOperator = TK_JS_OPERATOR;
		tkBrace	   = TK_JS_BRACE;
	}
	
	static {
		operatorSet.add('$');
	}
	
	@Override
	protected void scan() {
		if (!isEnd(3) && isCommentStart(current(), charUntil(1), 
						charUntil(2), charUntil(3))) {
			addComment(offset());
		} else {
			super.scan();
		}
	}
	
	@Override
	protected boolean isCommentDocStart(char c, char d, char e, char f) {
		return false;
	}
	
	private boolean isCommentStart(char c, char d, char e, char f) {
		return c=='<' && d=='!' && e=='-' && f=='-';
	}
	
	private void addComment(int offset) {
		advance();advance();advance();advance();
		addSingleComment0(offset);
		
		addRange(offset, offset());
	}
	
	@Override
	protected boolean isEnd() {
		return (!super.isEnd(8) && subString(offset(), offset()+9).equalsIgnoreCase("</script>")) ||
					super.isEnd();
	}
	
	@Override
	protected boolean isEnd(int need) {
		return (!super.isEnd(8) && subString(offset(), offset()+9).equalsIgnoreCase("</script>")) ||
					super.isEnd(need);
	}
	
	@Override
	public void tokenization(int startOffset, String text, String highLightWord) {
		setOffset(startOffset);
		setText(text);
		setHighLightWord(highLightWord);
		super.reset();
		while (scanHelp()) {}
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
