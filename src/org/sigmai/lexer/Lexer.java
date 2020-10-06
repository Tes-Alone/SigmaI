package org.sigmai.lexer;

import java.util.HashSet;
import java.util.TreeSet;

/**
 * 词法解析器基类.
 * 
 * 本类是 SigmaI 的词法解析器基类. 它提供一些词法解析子类所需的函数, 以及一些与 SigmaI 交互的接口.
 * 词法解析器负责搜集语言定义的各种词法 Token, 用于代码着色以及范围确定.
 * */
public abstract class Lexer {
	
	private TreeSet<Token> tokens;
	
	private HashSet<String> keyWord1;
	private HashSet<String> keyWord2;
	
	private TreeSet<RangeToken> rangeTokenSet;
	private TreeSet<FoldToken> foldTokenSet;
	
	/**
	 * 构造函数.
	 * 
	 * 初始化内部状态.
	 * */
	public Lexer() {
		tokens   = new TreeSet<>();
		keyWord1 = new HashSet<>();
		keyWord2 = new HashSet<>();
		multiCommentTokenSet = new TreeSet<>();
		rangeTokenSet = new TreeSet<>();
		foldTokenSet  = new TreeSet<>();
	}
	
	/**
	 * 判断当前 Lexer 是否支持多行注释.
	 * 
	 * 具体的子类可根据情况复写此方法.
	 * 此方法默认返回假.
	 * 
	 * @return 结果.
	 * */
	public boolean isSupportMultiComment() {
		return false;
	}
	
	/**
	 * 获取风格调配器.
	 * 
	 * 子类需复写此方法, 提供自己的 StylePalette 对象.
	 * 
	 * @return 风格调配器.
	 * */
	public abstract StylePalette getStylePalette();
	
	/**
	 * 设置第 1 组关键字.
	 * 
	 * 方法会过滤无效参数, 但不抛出异常.
	 * 
	 * @param keyWords 关键字列表.
	 * */
	public void setKeyWord1(String[] keyWords) {
		if (keyWords != null) {
			for (String word : keyWords) {
				if (word!=null && !word.isEmpty())
					keyWord1.add(word);
			}
		}
	}
	
	/**
	 * 设置第 2 组关键字.
	 * 
	 * 方法会过滤无效参数, 但不抛出异常.
	 * 
	 * @param keyWords 关键字列表.
	 * */
	public void setKeyWord2(String[] keyWords) {
		if (keyWords != null) {
			for (String word : keyWords)
				keyWord2.add(word);
		}
	}
	
	/**
	 * 判断单词是否属于第 1 组关键字.
	 * 
	 * @param word 待判断单词.
	 * @return 如果 word 是关键字1, 返回真; 否则返回假.
	 * */
	protected boolean isKeyWord1(String word) {
		return keyWord1.contains(word);
	}
	
	/**
	 * 判断单词是否属于第 2 组关键字.
	 * 
	 * @param word 待判断单词.
	 * @return 如果 word 是关键字2, 返回真; 否则返回假.
	 * */
	protected boolean isKeyWord2(String word) {
		return keyWord2.contains(word);
	}
	
	private String text = "";
	private int index   = 0;
	private String highLightWord = null;
	
	/**
	 * 解析文本.
	 * <br>
	 * 获取文本中的各种 Token.
	 * <br>
	 * startOffset 用于 嵌套语言, 如 HTML 中的 javascript, 指定 javascript 的开始位置.
	 * 一般使用将 startOffset 置 0. 
	 * 
	 * @param startOffset 解析开始位置.
	 * @param text 被解析文本.
	 * @param highLightWord 需要高亮的单词.
	 * */
	public void tokenization(int startOffset, String text, String highLightWord) {
		reset();
		this.index = startOffset;
		this.text = text;
		this.highLightWord = highLightWord;
		
		while (!isEnd()) {
			scan();
		}
	}
	
	public TreeSet<FoldToken> getFoldTokens() {
		return foldTokenSet;
	}
	
	/**
	 * 获取解析后的 Token 集合.
	 * 
	 * @return token 集合.
	 * */
	public TreeSet<Token> getTokens() {
		return tokens;
	}
	
	/**
	 * 获取被高亮单词.
	 * 
	 * @return 被高亮单词.
	 * */
	protected String getHighLightWord() {
		return highLightWord;
	}
	
	/**
	 * 判断单词是否被高亮单词.
	 * 
	 * @param word 被检测单词.
	 * @return 如果 word 是被高亮单词, 返回真; 否则返回假.
	 * */
	protected boolean isHighLightWord(String word) {
		return word.equals(highLightWord);
	}
	
	/**
	 * 设置当前解析的偏移位置.
	 * 
	 * 方法不对参数进行检测.
	 * 
	 * @param offset 解析的新偏移位置.
	 * */
	protected void setOffset(int offset) {
		this.index = offset;
	}
	
	/**
	 * 设置解析的文本.
	 * 
	 * 方法不对参数进行检测.
	 * 
	 * @param text 待解析文本.
	 * */
	protected void setText(String text) {
		this.text = text;
	}
	
	/**
	 * 设置高亮单词.
	 * 
	 * 方法不对参数进行检测.
	 * 
	 * @param word 被高亮单词.
	 * */
	protected void setHighLightWord(String word) {
		this.highLightWord = word;
	}
	
	/**
	 * 判断是否解析到文本结尾.
	 * 
	 * @return 结果.
	 * */
	protected boolean isEnd() {
		return index >= text.length();
	}
	
	/**
	 * 判断是否解析到文本结尾.
	 * 
	 * 方法不对参数进行检测.
	 * 
	 * @param need 文本结尾之前的位置.
	 * @return 结果.
	 * */
	protected boolean isEnd(int need) {
		return index+need >= text.length();
	}
	
	/**
	 * 移动解析偏移到下一位置.
	 * */
	protected void advance() {
		index++;
	}
	
	/**
	 * 获取当前解析偏移位置.
	 * 
	 * @return 当前解析偏移位置.
	 * */
	protected int offset() {
		return index;
	}
	
	/**
	 * 获取当前字符.
	 * 
	 * 方法不检测是否到文本尾.
	 * 
	 * @return 当前字符.
	 * */
	protected char current() {
		return text.charAt(index);
	}
	
	/**
	 * 获取位置 i 的字符.
	 * 
	 * 方法不检测参数是否越界.
	 * 
	 * @param i 位置.
	 * @return 被解析文本的第 i 个字符.
	 * */
	protected char charAt(int i) {
		return text.charAt(i);
	}
	
	/**
	 * 获取被解析文本.
	 * 
	 * @return 被解析文本.
	 * */
	protected String getText() {
		return text;
	}
	
	/**
	 * 获取被解析文本子串.
	 * 
	 * 方法不检测参数是否越界.
	 * 
	 * @param start 子串开始位置, 包括.
	 * @param end 子串结束位置, 不包括.
	 * @return 子串.
	 * */
	protected String subString(int start, int end) {
		return text.substring(start, end);
	}
	
	/**
	 * 获取当前偏移之后的第 until 个字符.
	 * 
	 * @param until.
	 * @return 结果.
	 * */
	protected char charUntil(int until) {
		return text.charAt(index+until);
	}
	
	/**
	 * 判断给定字符是否是英文字母.
	 * 
	 * 不区分大小写.
	 * 
	 * @param c.
	 * @return 结果.
	 * */
	protected boolean isUSLetter(char c) {
		return (c>='a'&&c<='z') || (c>='A'&&c<='Z');
	}
	
	/**
	 * 判断给定字符是否是阿拉伯数字.
	 * 
	 * @param c.
	 * @return 结果.
	 * */
	protected boolean isUSDigit(char c) {
		return (c>='0' && c<='9');
	}
	
	/**
	 * 判断给定字符是否是英文字母或阿拉伯数字.
	 * 
	 * 不区分大小写.
	 * 
	 * @param c.
	 * @return 结果.
	 * */
	protected boolean isUSLetterOrDigit(char c) {
		return isUSLetter(c) || isUSDigit(c);
	}
	
	/**
	 * 添加普通 Token.
	 * 
	 * 添加普通 Token 到接受者解析的 Token 集合. 普通 Token 用于辅助代码着色.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param token.
	 * @see Token
	 * */
	protected void addToken(Token token) {
		tokens.add(token);
	}
	
	protected void addFoldToken(FoldToken token) {
		foldTokenSet.add(token);
	}
	
	private TreeSet<MultiCommentToken> multiCommentTokenSet;
	
	/**
	 * 添加 MultiCommentToken.
	 * 
	 * 添加 MultiCommentToken 到接受者解析的 MultiCommentToken 集合, MutliCommentToken 
	 * 用于处理注释内输入.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param token.
	 * @see MultiCommentToken
	 * */
	protected void addMultiCommentToken(MultiCommentToken token) {
		multiCommentTokenSet.add(token);
	}
	
	/**
	 * 添加 RangeToken.
	 * 
	 * 添加 RangeToken 到接受者解析的 RangeToken 集合. RangeToken 用于确定词法单元的范围.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param start 范围开始, 包括.
	 * @param end 范围结束, 包括.
	 * */
	protected void addRange(int start, int end) {
		rangeTokenSet.add(new RangeToken(start, end));
	}
	
	private TreeSet<String> wordList = new TreeSet<String>();
	
	/**
	 * 添加单词.
	 * 
	 * 添加单词到接受者解析的单词集合. 单词用于随笔提示.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param word.
	 * */
	protected void addWord(String word) {
		wordList.add(word);
	}
	
	/**
	 * 获取单词集合.
	 * 
	 * 单词用于随笔提示.
	 * 
	 * @return 接受者解析的单词.
	 * */
	public TreeSet<String> getWordList() {
		return wordList;
	}
	
	/**
	 * 获取 MultiCommentToken 集合.
	 * 
	 * @return 接受者解析的 MultiCommentToken 集合.
	 * */
	public TreeSet<MultiCommentToken> getMultiCommentTokenList() {
		return multiCommentTokenSet;
	}
	
	/**
	 * 获取 RangeToken 集合.
	 * 
	 * @return 接受者解析的 RangeToken 集合.
	 * */
	public TreeSet<RangeToken> getRangeTokenList() {
		return rangeTokenSet;
	}
	
	/**
	 * 解析.
	 * 
	 * 子类需复写此方法, 进行实际的解析.
	 * */
	protected abstract void scan();
	
	/**
	 * 重置状态.
	 * 
	 * 此方法在解析之前调用.
	 * 子类可按需复用或复写此方法.
	 * */
	protected void reset() {
		tokens.clear();
		wordList.clear();
		multiCommentTokenSet.clear();
		rangeTokenSet.clear();
		foldTokenSet.clear();
	}
	
	/**
	 * 添加 Token 列表.
	 * 
	 * 向接受者添加 Token 列表.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param tokens token 列表.
	 * */
	protected void addTokenList(TreeSet<Token> tokens) {
		this.tokens.addAll(tokens);
	}
	
	/**
	 * 添加单词列表.
	 * 
	 * 向接受者添加单词列表.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param words 单词列表.
	 * */
	protected void addWordList(TreeSet<String> words) {
		this.wordList.addAll(words);
	}
	
	/**
	 * 添加 MultiCommentToken 列表.
	 * 
	 * 向接受者添加 MultiCommentToken 列表.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param tokens MultiCommentToken 列表.
	 * */
	protected void addMultiCommentTokenList(TreeSet<MultiCommentToken> tokens) {
		this.multiCommentTokenSet.addAll(tokens);
	}
	
	/**
	 * 添加 RangeToken 列表.
	 * 
	 * 向接受者添加 RangeToken 列表.
	 * <br>
	 * 方法不对参数进行检测.
	 * 
	 * @param tokens RangeToken 列表.
	 * */
	protected void addRangeTokenList(TreeSet<RangeToken> tokens) {
		this.rangeTokenSet.addAll(tokens);
	}
}
