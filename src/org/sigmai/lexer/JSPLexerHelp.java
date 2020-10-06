package org.sigmai.lexer;

class JSPLexerHelp extends JavaLexer {
	
	@Override
	protected void scan() {
		if (!isEnd(3) && isComment1(current(), 
				charUntil(1), charUntil(2), charUntil(3))) {
			addComment1(offset());
		} else if (!isEnd(3) && isComment2(current(), 
				charUntil(1), charUntil(2), charUntil(3))) {
			addComment2(offset());
		} else {
			super.scan();
		}
	}
	
	private void addComment1(int offset) {
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
		token.type = TK_JAVA_COMMENT;
		addToken(token);
		//System.out.println(offset());
		
		addRange(offset, offset());
	}

	private boolean isComment1(char c, char d, char e, char f) {
		return c=='<' && d=='!' && e=='-' && f==e;
	}
	
	private boolean isComment2(char c, char d, char e, char f) {
		return c=='<' && d=='%' && e=='-' && f==e;
	}
	
	private void addComment2(int offset) {
		advance(); advance();advance(); advance();
		while (!isEnd(3) && (current()!='-'
								||charUntil(1)!='-'
									||charUntil(2)!='%'
										||charUntil(3)!='>')) {
			advance();
		}
		
		if (!isEnd(3)) {advance();}
		if (!isEnd(2)) {advance();}
		if (!isEnd0(1)) {advance();}
		if (!isEnd0(0)) {advance();}
		Token token = new Token();
		token.start = offset;
		token.length = offset() - offset;
		token.type = TK_JAVA_COMMENT;
		addToken(token);
		
		addRange(offset, offset());
	}
	
	private boolean isEnd0(int need) {
		return super.isEnd(need);
	}

	@Override
	protected boolean isEnd() {
		return (!super.isEnd(1) && current()=='%' && charUntil(1)=='>') ||
					super.isEnd();
	}
	
	@Override
	protected boolean isEnd(int need) {
		return (!super.isEnd(1) && current()=='%' && charUntil(1)=='>') ||
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
