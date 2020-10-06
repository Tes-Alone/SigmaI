package org.sigmai.lexer;

import java.util.HashSet;

public abstract class BaseLexer extends Lexer {

	protected int tkKeyWord1 = CPPLexer.TK_CPP_KEYWORD1;
	protected int tkKeyWord2 = CPPLexer.TK_CPP_KEYWORD2;
	protected int tkID		 = CPPLexer.TK_CPP_ID;
	protected int tkNumber   = CPPLexer.TK_CPP_NUMBER;
	protected int tkString   = CPPLexer.TK_CPP_STRING;
	protected int tkChar     = CPPLexer.TK_CPP_CHAR;
	protected int tkOperator = CPPLexer.TK_CPP_OPERATOR;
	protected int tkComment  = CPPLexer.TK_CPP_COMMENT;
	
	protected BaseLexer() {
		operatorSet = getOperatorSet();
	}
	
	@Override
	protected void scan() {
		if (!isEnd() && isWordStart(current())) {
			addWord(offset());
		} else if (!isEnd(1) && isNumberStart(current(), charUntil(1))) {
			addNumber(offset());
		} else if (!isEnd() && isNormalStringStart(current(), '"')) {
			addNormalString(offset(), '"');
		} else if (!isEnd() && isNormalStringStart(current(), '\'')) {
			addNormalString(offset(), '\'');
		} else if (!isEnd() && isOperator(current())) {
			addOperator(offset());
		} else {
			advance();
		}
	}
	
	protected boolean isWordPart(char c) {
		return isUSLetterOrDigit(c) || c=='_';
	}

	protected boolean isWordStart(char c) {
		return isUSLetter(c) || c=='_';
	}
	
	protected void addWord(int offset) {
		advance();
		while (!isEnd() && isWordPart(current())) {
			advance();
		}
		String lexeme = subString(offset, offset());
		Token token = new Token();
		token.start  = offset;
		token.length = offset()-offset;
		
		if (isHighLightWord(lexeme)) {
			token.isHighLightWord = true;
		}
		
		if (isKeyWord1(lexeme)) {
			token.type   = tkKeyWord1;
		} else if (isKeyWord2(lexeme)) {
			token.type   = tkKeyWord2;
		} else {
			token.type   = tkID;
		}
		token.lexeme = lexeme;
		addToken(token);
		addWord(lexeme);
	}
	
	protected void addNumber(int offset) {
		advance();
		while (!isEnd() && isNumberPart(current())) {
			advance();
		}
		Token token = new Token();
		token.start  = offset;
		token.length = offset()-offset;
		token.type   = tkNumber;
		addToken(token);
	}

	protected boolean isNumberPart(char c) {
		return Character.isLetterOrDigit(c) || c=='.';
	}

	protected boolean isNumberStart(char c, char d) {
		return Character.isDigit(c)||(c=='.'&&Character.isDigit(d));
	}
	
	protected boolean isNormalStringStart(char c, char d) {
		return c == d;
	}
	
	protected boolean isNormalStringEnd(char c, char d) {
		return c==d || c=='\r' || c=='\n';
	}
	
	protected void addNormalString(int offset, char c) {
		advance();
		while (!isEnd() && !isNormalStringEnd(current(), c)) {
			if (!isEnd() && current()=='\\') {
				advance();
				if (!isEnd(1) && (current()=='\r' && charUntil(1)=='\n')) { // for windows
					advance(); advance();
				} else if (!isEnd() && (current()=='\r' || current()=='\n')) { // for Linux or MacOS
					advance();
				} else {
					advance();
				}
			} else {
				advance();
			}
		}
		
		if (!isEnd()) {
			advance();
		}
		
		Token token = new Token();
		token.start  = offset;
		token.length = offset() - offset;
		token.type   = c=='\'' ? tkChar : tkString;
		addToken(token);
		
		addRange(offset, offset()-1);
	}
	
	private HashSet<Character> operatorSet;
	
	protected abstract HashSet<Character> getOperatorSet();
	
	protected boolean isOperator(char c) {
		return operatorSet.contains(c);
	}
	
	protected void addOperator(int offset) {
		advance();
		Token token = new Token();
		token.start  = offset;
		token.length = 1;
		token.type   = tkOperator;
		token.lexeme = charAt(offset) + "";
		addToken(token);
	}

	protected Token collectHighLightWord(Token token, int tokenType) {
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
				token.type = tokenType;
				addToken(token);
				token = new Token();
				token.start = tmp;
				token.length = offset()-tmp;
				token.isHighLightWord = true;
				token.type = tokenType;
				addToken(token);
				token = new Token();
				token.start = offset();
			}
		} else {
			advance();
		}
		return token;
	}
	
	protected void addSingleComment0(int offset) {
		Token token = new Token();
		token.start = offset;
		while (!isEnd() && !isSingleCommentEnd(current())) {
			token = collectHighLightWord(token, tkComment);
		}
		token.length = offset() - token.start;
		token.isHighLightWord = false;
		token.type = tkComment;
		addToken(token);
	}
	
	protected boolean isSingleCommentEnd(char c) {
		return c=='\r' || c=='\n';
	}
}
