package org.sigmai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.sigmai.event.AutoIndentEvent;
import org.sigmai.event.AutoIndentListener;
import org.sigmai.event.EnterCommentAreaEvent;
import org.sigmai.event.EnterCommentAreaListener;
import org.sigmai.event.InputModeChangedEvent;
import org.sigmai.event.InputModeChangedListener;
import org.sigmai.event.PairEvent;
import org.sigmai.event.PairListener;
import org.sigmai.event.UndoRedoEvent;
import org.sigmai.event.UndoRedoListener;
import org.sigmai.lexer.FoldToken;
import org.sigmai.lexer.Lexer;
import org.sigmai.lexer.MultiCommentToken;
import org.sigmai.lexer.RangeToken;
import org.sigmai.lexer.StylePalette;
import org.sigmai.lexer.Token;

/**
 * SigmaI 是一个小型的代码编辑组件.
 * <br>
 * SigmaI 建立在 SWT 的 StyledText 组件之上, 但并不隐藏 StyledText.<br>
 * SigmaI 是 StyledText 的装饰器.<br>
 * <br>
 * SigmaI 为 StyledText 添加了一些高级功能, 如 UndoRedo, 代码词法着色, 
 * 括号自动输入, 随笔提示以及函数调用提示等.
 * <br>
 * <dl>
 * <dt><b>风格:</b></dt>
 * <dd>WIN7, GNOME, LINE_MARGIN</dd>
 * </dl>
 * 其中, WIN7 和 GNOME 是互斥的.
 * @see StyledText　
 * */

/**
 * SigmaI is a tiny code edit control.
 * 
 * It builds upon StyledText of SWT, but not hidden the StyledText.
 * It is a decorator of StyledText.
 * 
 * SigmaI adds some functions of a code-edit control, like undo/redo,
 * token coloration, auto complement, brace-match.
 * 
 * */
 
public class SigmaI extends Composite {
	
	/**
	 *  风格值, 指定编辑器包含代码行旁注栏.
	 * */
	public static final int LINE_MARGIN = 1;
	
	public static final int FOLD_MARGIN = 1 << 1;
	
	/**
	 * 风格值, 指定编辑器在 Window 7 环境运行, 用于配置相关参数, 如滚动条宽度等.
	 * */
	public static final int WIN7  = 1 << 2;
	
	/**
	 * 风格值, 指定编辑器在 GNOME 环境运行, 用于配置相关参数, 如滚动条宽度等.
	 * */
	public static final int GNOME = 1 << 3;
	
	/**
	 * 符号对标签, 指定编辑器自动完成 "{}" 输入以及匹配.
	 * */
	public static final int PAIR_BRAC = 1 << 4;
	
	/**
	 *符号对标签, 指定编辑器自动完成 "[]" 输入以及匹配.
	 * */
	public static final int PAIR_SQUA = 1 << 5;
	
	/**
	 * 符号对标签, 指定编辑器自动完成 "<>" 输入以及匹配.
	 * */
	public static final int PAIR_BIG  = 1 << 6;
	
	/**
	 * 符号对标签, 指定编辑器自动完成 "()" 输入以及匹配.
	 * */
	public static final int PAIR_PARE = 1 << 7;
	
	/**
	 * 符号对标签,  指定编辑器自动完成 " \'\' " 输入.
	 * */
	public static final int PAIR_SINGLE_QUOT = 1 << 8;
	
	/**
	 * 符号对标签, 指定编辑器自动完成 ' \"\" ' 输入.
	 * */
	public static final int PAIR_DOUBLE_QUOT = 1 << 9;
	
	private StyledText editor;
	private LineMargin lineMargin;
	private FoldMargin foldMargin;
	private WordFixMargin wordFixMargin;
	
	private boolean hasLineMargin;
	private boolean hasFoldMargin;
	
	private Color highLight;
	private Caret normalCaret;
	private Caret overWriteCaret;
	private boolean isOverWriteMode;
	
	private int scrollBarWidth;
	
	private Lexer lexer;
	
	private UndoManager undoMan;
	
	private FunctionCallTip  callTip;
	private CompleteWordPane wordPane;
	
	/**
	 * 构造函数.
	 * 
	 * @param parent 父组件.
	 * @param style 风格.
	 * */
	public SigmaI(Composite parent, int style) {
		super(parent, style);
		checkStyle(style);
		initContent();
		initResources();
		addListeners();
	}
	
	/**
	 * 获取 内部使用的 StyledText.
	 * 
	 * @return 接受者的 StyledText 控件.
	 * */
	public StyledText getStyledText() {
		checkWidget();
		return editor;
	}
	
	/**
	 * 设置编辑区域前景色.
	 * 
	 * 如果 fore 为 null 或 fore 已被销毁, 方法什么都不做.
	 * 
	 * @param fore 指定的前景色.
	 * */
	public void setEditAreaForeground(Color fore) {
		checkWidget();
		if (fore!=null && !fore.isDisposed()) {
			editor.setForeground(fore);
			wordPane.setForeground(fore);
		}
	}
	
	/**
	 * 设置编辑区域背景色.
	 * 
	 * 如果 back 为 null 或 back 已被销毁, 方法什么都不做.
	 * 
	 * @param back 指定的背景色.
	 * */
	public void setEditAreaBakcground(Color back) {
		checkWidget();
		if (back!=null && !back.isDisposed()) {		
			editor.setBackground(back);
			wordPane.setBackground(back);
		}
	}
	
	/**
	 * 设置代码行旁注栏前景色.
	 * 
	 * 如果 fore 为 null 或 fore 已被销毁, 方法什么都不做.
	 * 
	 * @param fore 指定的前景色.
	 * */
	public void setLineMarginForeground(Color fore) {
		checkWidget();
		if (lineMargin!=null && fore!=null && !fore.isDisposed()) {		
			lineMargin.setForeground(fore);
		}
	}
	
	/**
	 * 设置代码行旁注栏背景色.
	 * 
	 * 如果 back 为 null 或 back 已被销毁, 方法什么都不做.
	 * 
	 * @param back 指定的背景色.
	 *
	 * */
	public void setLineMarginBackground(Color back) {
		checkWidget();
		if (lineMargin!=null && back!=null && !back.isDisposed()) {		
			lineMargin.setBackground(back);
		}
	}
	
	/**
	 * 设置单词定位旁注栏背景色.
	 * 
	 * 如果 back 为 null 或 back 已被销毁, 方法什么都不做.
	 * 
	 * @param back 指定的背景色.
	 *
	 * */
	public void setWordFixMarginBackground(Color back) {
		checkWidget();			
		if (back!=null && !back.isDisposed()) {		
			wordFixMargin.setBackground(back);
		}
	}
	
	/**
	 * 设置单词定位旁注栏前景色.
	 * 
	 * 如果 fore 为 null 或 fore 已被销毁, 方法什么都不做.
	 * 
	 * @param fore 指定的前景色.
	 * */
	public void setWordFixMarginForeground(Color fore) {
		checkWidget();			
		if (fore!=null && !fore.isDisposed()) {		
			wordFixMargin.setForeground(fore);
		}
	}
	
	/**
	 * 设置编辑器字体.
	 * 
	 * 函数会同时设置代码行旁注栏(如果存在)和编辑区域.
	 * 如果 font 为 null 或 font 已被销毁, 方法什么都不做.
	 * 
	 * @param font 指定的字体.
	 * */
	public void setFont(Font font) {
		checkWidget();
		if (font!=null && !font.isDisposed()) { 
			editor.setFont(font);
			int lineHeight = editor.getLineHeight();
			normalCaret.setSize(2, lineHeight);
			overWriteCaret.setSize(lineHeight>>1, lineHeight);
			editor.setCaret(editor.getCaret());
			if (lineMargin != null) {
				lineMargin.setFont(font);
			}
		}
	}
	
	/**
	 * 设置词法解析器.
	 * 
	 * 词法解析器用于解析接受者的当前文本内容, 用于辅助着色, 以及范围确定.
	 * 
	 * @param lexer 词法解析器, 可以为 null.
	 * @see Lexer
	 * */
	public void setLexer(Lexer lexer) {
		checkWidget();
		this.lexer = lexer;
	}
	
	/**
	 * 获取词法解析器.
	 * 
	 * @return 接受者的词法解析器, 可能为 null.
	 * */
	public Lexer getLexer() {
		checkWidget();
		return lexer;
	}
	
	private PairListener pairListener;
	
	/**
	 * 设置 PairListener.
	 * 
	 * PairListner 用于监听自动完成符号对({}, [] 等)操作.
	 * 
	 * @param listener
	 * @see PairListener
	 * */
	public void setPairListener(PairListener listener) {
		checkWidget();		
		this.pairListener = listener;
	}
	
	private UndoRedoListener undoListener;
	
	/**
	 * 设置 UndoRedoListener.
	 * 
	 * UndoRedoListner 用于监听Undo， Redo操作.
	 * 
	 * @param listener
	 * @see UndoRedoListener
	 * */
	public void setUndoRedoListener(UndoRedoListener listener) {
		checkWidget();
		this.undoListener = listener;
	}
	
	private boolean highLightCurrent = false;
	
	/**
	 * 开启或关闭高亮当前行功能.
	 * 
	 * @param  enable 参数.
	 * */
	public void setHighLightCurrent(boolean enable) {
		checkWidget();
		highLightCurrent = enable;
	}
	
	/**
	 * 设置当前高亮行的颜色.
	 * 
	 * 如果 color 为 null 或 color 已被销毁, 函数什么都不做.
	 * 如果未开启高亮行功能, 函数什么都不做.
	 * 
	 * @param color 指定的字体.
	 * */
	public void setHighLightColor(Color color) {
		checkWidget();
		if (color == null)return;
		if (color.isDisposed()) return;
		if (!highLightCurrent) return;
		highLight = color;
		highLightCurrent();
	}
	
	private void highLightCurrent() {
		checkWidget();
		if (highLightCurrent) {
			int line = editor.getLineAtOffset(editor.getCaretOffset());
			int lineCount = editor.getLineCount();
			
			if (lineCount>1 && line<lineCount) {
				editor.setLineBackground(0, line, null);
				editor.setLineBackground(line+1, lineCount-line-1, null);
			}
			//System.out.println(line);
			//FIXME
			try {
				editor.setLineBackground(line, 1, highLight);
			} catch (IndexOutOfBoundsException e) {
				editor.setLineBackground(0, 1, highLight);
			}
		}
	}
	
	private boolean autoIndent = false;
	
	/**
	 * 开启或关闭自动缩进功能.
	 * 
	 * @param enable 参数.
	 * */
	public void setAutoIndent(boolean enable) {
		checkWidget();
		autoIndent = enable;
	}
	
	private AutoIndentListener autoIndentListener;
	
	/**
	 * 设置 AutonIndentListener.
	 * 
	 * AutoIndentListener 监听自动缩进操作.
	 * 
	 * @param listener 
	 * */
	public void setAutoIndentListener(AutoIndentListener listener) {
		checkWidget();
		this.autoIndentListener = listener;
	}
	
	private void autoIndent(VerifyEvent event) {
		checkWidget();
		if (autoIndent && !undoMan.isUndoing() && !undoMan.isRedoing()) {
			try {
				int caretOffset = editor.getCaretOffset();
				int lineIndex   = editor.getLineAtOffset(caretOffset);
				int lineOffset  = editor.getOffsetAtLine(lineIndex);
				int caretOffsetAtLine = caretOffset-lineOffset; 
				String      line = editor.getLine(lineIndex);
				StringBuffer tab = new StringBuffer();
				for (int i=0; i<caretOffsetAtLine; i++) {
					char c = line.charAt(i);
					if (c=='\t' || c==' ') {
						tab.append(c);
					} else {
						break;
					}
				}
				
				String lineBeforeCaret = line.substring(0, caretOffsetAtLine);
				String lineAfterCaret  = line.substring(caretOffsetAtLine);
				
				AutoIndentEvent e = new AutoIndentEvent();
				e.sigmai = this;
				e.indentString = event.text + tab.toString();
				
				//FIXME 暂时将注释的自动补充放在这里.
				boolean starOrSlashStart = lineBeforeCaret.trim().startsWith("/*") 
											|| lineBeforeCaret.trim().startsWith("*");
				
				if (isInComment && starOrSlashStart 
									&& e.indentString.equals(event.text)) {
					e.indentString += " * ";
				} else if (isInComment && starOrSlashStart) {
					e.indentString += "* ";
				}
				
				//  D nest comment
				boolean starOrSlashStartForD = lineBeforeCaret.trim().startsWith("/+") 
						|| lineBeforeCaret.trim().startsWith("+");

				if (isInComment && starOrSlashStartForD 
								&& e.indentString.equals(event.text)) {
					e.indentString += " + ";
				} else if (isInComment && starOrSlashStartForD) {
					e.indentString += "+ ";
				}
				//---------------------------
				
				e.lineBeforeCaret = lineBeforeCaret;
				e.lineAfterCaret  = lineAfterCaret;
				if (autoIndentListener != null) {
					autoIndentListener.indenting(e);
				}
				
				event.text = e.indentString;
			} catch (Exception e) {}
		}
	}
	
	private EnterCommentAreaListener enterCommentAreaListener;
	
	/**
	 * 设置 EnterCommentAreaListener.
	 * 
	 * EnterCommentAreaListener 监听 caret 进入注释区域.
	 * 
	 * @param listener 
	 * */
	public void setEnterCommentAreaListener(EnterCommentAreaListener listener) {
		checkWidget();
		this.enterCommentAreaListener = listener;
	}
	
	private void handleEnterCommentAreaEvent(String text) {
		checkWidget();
		if (isInComment && enterCommentAreaListener!=null) {
			EnterCommentAreaEvent e = new EnterCommentAreaEvent();
			e.sigmai = this;
			e.text   = text;
			enterCommentAreaListener.enterCommnetArea(e);
		}
	}
	
	private boolean[] pairs = new boolean[6]; 
	private int pairFlag;
	
	/**
	 * 开启或关闭自动输入符号对功能.
	 * 
	 * pairFlag 包含某符号对标签, 代表开启该符号对功能, 否则为关闭.
	 * pairFlag 必须是本类定义的符号对标签值的位或值, 其它值会被忽略.
	 * 
	 * @param pairFlag 符号对标签.
	 * */
	public void setAutoCompletePair(int pairFlag) {
		checkWidget();
		this.pairFlag = pairFlag;
		if ((pairFlag&PAIR_BRAC)!=0)pairs[0]=true;else pairs[0]=false;
		if ((pairFlag&PAIR_SQUA)!=0)pairs[1]=true;else pairs[1]=false;
		if ((pairFlag&PAIR_PARE)!=0)pairs[2]=true;else pairs[2]=false;
		if ((pairFlag&PAIR_BIG)!=0)pairs[3]=true;else pairs[3]=false;
		if ((pairFlag&PAIR_SINGLE_QUOT)!=0)pairs[4]=true;else pairs[4]=false;
		if ((pairFlag&PAIR_DOUBLE_QUOT)!=0)pairs[5]=true;else pairs[5]=false;
	}
	
	/**
	 * 获取自动输入符号对标签.
	 * 
	 * @return 符号对标签.
	 * */
	public int getPairFlag() {
		checkWidget();
		return pairFlag;
	}
	
	private boolean matchBrace[] = new boolean[4];
	private int matchBraceFlag;
	
	/**
	 * 设置匹配括号标签.
	 * 
	 * matchBraceFlag 包含某符号对标签, 代表开启该符号对功能, 否则为关闭.
	 * matchBraceFlag 必须是本类定义的符号对标签值的位或值, 其它值会被忽略.
	 * matchBraceFlag 不能是 PAIR_DOUBLE_QUOT 和 PAIR_SINGLE_QUOT.
	 * 
	 * @param matchBraceFlag.
	 * */
	public void setMatchBraceFlag(int matchBraceFlag) {
		checkWidget();
		this.matchBraceFlag = matchBraceFlag;
		if ((matchBraceFlag&PAIR_BRAC)!=0)matchBrace[0]=true;else matchBrace[0]=false;
		if ((matchBraceFlag&PAIR_SQUA)!=0)matchBrace[1]=true;else matchBrace[1]=false;
		if ((matchBraceFlag&PAIR_PARE)!=0)matchBrace[2]=true;else matchBrace[2]=false;
		if ((matchBraceFlag&PAIR_BIG)!=0)matchBrace[3]=true;else matchBrace[3]=false;
	}
	
	/**
	 * 获取匹配括号标签.
	 * 
	 * @return 接受者的括号匹配标签.
	 * */
	public int getMatchBraceFlag() {
		checkWidget();
		return matchBraceFlag;
	}
	
	private StyleRange boxedPairStyle;
	//private int boxedPairOffset = -1;
	
	private void matchRightBrace(int offset, char half) {
		checkWidget();
		unBoxHalf();
		if (half=='{' && matchBrace[0]) {
			int rightOffset = fixRightOffset(offset,'{','}');
			if (rightOffset == -1) return;
			rightOffset += offset;
			cacheStyle(rightOffset);
			boxHalf(rightOffset);
		} else if (half=='[' && matchBrace[1]) {
			int rightOffset = fixRightOffset(offset,'[',']');
			if (rightOffset == -1) return;
			rightOffset += offset;
			cacheStyle(rightOffset);
			boxHalf(rightOffset);
		} else if (half=='(' && matchBrace[2]) {
			int rightOffset = fixRightOffset(offset,'(',')');
			if (rightOffset == -1) return;
			rightOffset += offset;
			cacheStyle(rightOffset);
			boxHalf(rightOffset);
		} else if (half =='<' && matchBrace[3]) {
			int rightOffset = fixRightOffset(offset,'<','>');
			if (rightOffset == -1) return;
			rightOffset += offset;
			cacheStyle(rightOffset);
			boxHalf(rightOffset);
		}
	}
	
	private void cacheStyle(int offset) {
		checkWidget();
		boxedPairStyle  = editor.getStyleRangeAtOffset(offset);
		//boxedPairOffset = offset;
	}

	private void unBoxHalf() {
		checkWidget();
		if (boxedPairStyle != null) {
			boxedPairStyle.borderStyle = SWT.NONE;
			editor.setStyleRange(boxedPairStyle);
			boxedPairStyle = null;
		}/* else {
			if (boxedPairOffset != -1) {
				StyleRange style = new StyleRange();
				style.start  = boxedPairOffset;
				style.length = 1;
				style.borderStyle = SWT.NONE;
				editor.setStyleRange(style);
				boxedPairOffset = -1;
			}
		}*/
	}

	private void boxHalf(int offset) {
		checkWidget();
		StyleRange style = new StyleRange();
		style.start  = offset;
		style.length = 1;
		style.borderStyle = SWT.BORDER_SOLID;
		editor.setStyleRange(style);
	}

	private void matchBraces() {
		checkWidget();
		String text = editor.getText();
		int caretOffset = editor.getCaretOffset();
		char half = 0;
		
		if (isInRangeToken(caretOffset)) {
			return;
		}
		
		try {
			half = text.charAt(caretOffset);
		} catch (Exception e) {}

		try {
			matchRightBrace(caretOffset+1, half);
		} catch(Exception e) {}
		
		try {
			matchLeftBrace(caretOffset-1, half);
		} catch (Exception e) {}
	}
	
	private int fixRightOffset(int offset, char leftHalf, char rightHalf) {
		checkWidget();
		String text = editor.getText(offset, editor.getCharCount()-1);
		int stack = 1;
		int len   = text.length();
		for (int i=0; i<len; i++) {
			if  (isInRangeToken(i+offset)) {
				continue;
			}
			char c = text.charAt(i); 
			if (c == rightHalf) {
				stack--;
				if (stack == 0) {
					return i;
				}
			} else if (c == leftHalf) {
				stack++;
			}
		}
		return -1;
	}

	private void matchLeftBrace(int offset, char half) {
		checkWidget();
		if (half=='}' && matchBrace[0]) {
			int leftOffset = fixLeftOffset(offset,'{','}');
			if (leftOffset == -1) return;
			cacheStyle(leftOffset);
			boxHalf(leftOffset);
		} else if (half==']' && matchBrace[1]) {
			int leftOffset = fixLeftOffset(offset,'[',']');
			if (leftOffset == -1) return;
			cacheStyle(leftOffset);
			boxHalf(leftOffset);
		} else if (half==')' && matchBrace[2]) {
			int leftOffset = fixLeftOffset(offset,'(',')');
			if (leftOffset == -1) return;
			cacheStyle(leftOffset);
			boxHalf(leftOffset);
		} else if (half =='>' && matchBrace[3]) {
			int leftOffset = fixLeftOffset(offset,'<','>');
			if (leftOffset == -1) return;
			cacheStyle(leftOffset);
			boxHalf(leftOffset);
		}
	}
	
	private int fixLeftOffset(int offset, char leftHalf, char rightHalf) {
		checkWidget();
		String text = editor.getText(0, offset);
		int stack = 1;
		int len   = text.length();
		for (int i=len-1; i>=0; i--) {
			if (isInRangeToken(i)) {
				continue;
			}
			char c = text.charAt(i);
			if (c == leftHalf) {
				stack--;
				if (stack == 0) {
					return i;
				}
			} else if (c == rightHalf) {
				stack++;
			}
		}
		return -1;
	}
	
	private boolean isInRangeToken(int offset) {
		checkWidget();
		if (lexer == null) {
			return false;
		}
		TreeSet<RangeToken> rangeTokens = lexer.getRangeTokenList();
		for (var tk : rangeTokens) {
			if (offset>=tk.start && offset<=tk.end) {
				return true;
			}
		}
		return false;
	}
	
	private boolean highLightWord;
	private Color   highLightWordColor;
	
	/**
	 * 开启或关闭高亮单词.
	 * 
	 * @param highLight 参数.
	 * */
	public void setHighLightWord(boolean highLight) {
		checkWidget();
		highLightWord = highLight;
	}
	
	/**
	 * 设置单词高亮颜色.
	 * 
	 * 如果 color 为 null 或 color 已被销毁, 方法什么都不做.
	 * 方法不检查是否开启高亮单词功能.
	 * 
	 * @param color .
	 * */
	public void setHighLightWordColor(Color color) {
		checkWidget();
		if (color == null) return;
		if (color.isDisposed()) return;			
		this.highLightWordColor = color;
	}
	
	private int wordStart;
	
	/**
	 * 获取指定位置的单词.
	 * 
	 * 获取接受者目前文本中  offset 处的单词, 单词包括 Unicode 字符和数字以及 '_'.
	 * offset 可能位于单词左边或右边, 也可以位于单词中间.
	 * 
	 * @param offset 指定的位置.
	 * @return 单词.
	 * */
	public String getWordAtOffset(int offset) {
		checkWidget();
		String text = editor.getText();
		int len     = text.length();
		StringBuffer leftHalf = new StringBuffer();
		StringBuffer rightHalf = new StringBuffer();
		
		for (int i=offset; i<len; i++) {
			char c = 0;
			try {		
				c = text.charAt(i);
			} catch (Exception e) {
				c = '\0';
			} 
			if (Character.isLetterOrDigit(c) || c=='_') {
				rightHalf.append(c);
			} else {
				break;
			}
		}
		
		for (wordStart=offset-1; wordStart>=0; wordStart--) {
			char c = 0;
			try {		
				c = text.charAt(wordStart);
			} catch (Exception e) {
				c = '\0';
			}
			if (Character.isLetterOrDigit(c) || c=='_') {
				leftHalf.append(c);
			} else {
				break;
			}
		}
		wordStart++ ;
		
		return leftHalf.reverse().append(rightHalf).toString();
	}
	
	/**
	 * 获取单词开始位置.
	 * 
	 * 这里的开始位置是通过 getWordAtOffset() 方法计算出的, 所以此方法
	 * 必须在 getWordAtOffset() 之后调用. 否则, 返回值无意义.
	 * 
	 * @return 单词开始位置.
	 * @see getWordAtOffset
	 * */
	public int getWordStart() {
		checkWidget();
		return wordStart;
	}
	
	private void highLightWord() {
		checkWidget();
		if (highLightWord) {
			String word = getWordAtOffset(editor.getCaretOffset());
			//System.out.println(word);
			if (!word.isEmpty()) {
				try {
					this.paintLexeme(word);
				} catch(Exception e) {
					//e.printStackTrace();
				}
			}
		}
	}
	
	private boolean autoCompleteWord;
	
	/**
	 * 开启或关闭随笔提示功能.
	 * 
	 * @param b 参数.
	 * */
	public void setAutoCompleteWord(boolean b) {
		checkWidget();
		autoCompleteWord = b;
		wordCompleter.enableWordCompleter(b);
	}
	
	/**
	 * 添加随笔提示单词.
	 * 
	 * 方法不检查是否开启随笔提示功能.
	 * 
	 * @param baseWords
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 baseWords 为 null.</li>
	 * </ul>
	 * */
	public void addBaseCompleteWords(String[] baseWords) {
		checkWidget();
		if (baseWords == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		wordCompleter.addBaseWords(baseWords);
	}
	
	/**
	 * 设置基本随笔提示词汇.
	 * 
	 * 方法不检查是否开启随笔提示功能.
	 * 
	 * @param vocabulary
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 vocabulary 为 null.</li>
	 * </ul>
	 * */
	public void setVocabulary(String[] vocabulary) {
		if (vocabulary == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		wordCompleter.setVocabulary(vocabulary);
	}

	/**
	 * 显示随笔提示面板.
	 * 
	 * 如果未开启随笔提示功能, 方法不会显示随笔提示面板.
	 * 
	 * @param wordList 单词列表.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 wordList 为 null.</li>
	 * </ul>
	 * */
	void showCompleteWordList(TreeSet<String> wordList) {
		checkWidget();
		if (wordList == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		if (autoCompleteWord) {	
			Point inputPoint  = editor.getLocationAtOffset(editor.getCaretOffset());
			Point displayPoint = editor.toDisplay(inputPoint);
			Point editorSize   = editor.getSize();
			Point wordPaneSize = wordPane.getSize();
			wordPane.resetWordList(wordList);
			if (inputPoint.y + wordPaneSize.y > editorSize.y) {
				Point fix = new Point(inputPoint.x, inputPoint.y-wordPaneSize.y);
				displayPoint = editor.toDisplay(fix);
			} else {
				displayPoint.y += editor.getLineHeight();
			}
			wordPane.open(displayPoint.x, displayPoint.y);
		}
	}
	
	/**
	 * 获取单词列表.
	 * 
	 * 获取 lexer 解析的单词列表, 如果 lexer 为 null, 返回 null.
	 * @return lexer 解析的单词列表.
	 * */
	TreeSet<String> getWordList() {
		checkWidget();
		if (lexer != null)
			return lexer.getWordList();
		return null;
	}
	
	/**
	 * 获取从随笔提示面板中选择的单词.
	 * 
	 * @return 单词.
	 * */
	String getCompleteWord() {
		checkWidget();
		return wordPane.getWord();
	}
	
	/**
	 * 隐藏随笔提示面板.
	 * */
	public void hideCompleteWordPane() {
		checkWidget();
		wordPane.hide();
	}
	
	/**
	 * 显示函数调用提示.
	 * 
	 * @param tip 提示.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 tip 为 null.</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 tip 为空串.</li>
	 * </ul>
	 * */
	public void showCallTip(String tip) {
		checkWidget();
		if (tip == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		if (tip.isEmpty())
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		Point tmp = editor.getLocationAtOffset(editor.getCaretOffset());
		Point pos = editor.toDisplay(tmp);
		callTip.showTip(tip, pos.x, pos.y+editor.getLineHeight());
	}
	
	/**
	 * 跳转到指定行.
	 * 
	 * 编辑区域会滚动到适当位置.
	 * 
	 * @param line 指定的行.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_INVALID_RANGE - 如果 line<0 || line>=行数 </li>
	 * </ul>
	 * */
	public void gotoLine(int line) {
		checkWidget();
		if (line<0 || line>=editor.getLineCount())
			throw new SWTError(SWT.ERROR_INVALID_RANGE);
		editor.setSelection(editor.getOffsetAtLine(line));
	}
	
	private int findCount = 0;
	private int findPrevCount = 0;
	
	/**
	 * 在全部文本中寻找.
	 * 
	 * 在接受者的全部文本范围向后查找字符串, 如果找到匹配子串, 匹配子串会被选择.
	 * 
	 * @param word 查找的单词.
	 * @param caseSensitive 区分大小写选项.
	 * @param wholeWord 作为整体选项.
	 * @param wrap 回卷选项.
	 * @return 如果查找到, 返回真, 否则返回假.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 word 为 null</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 word 为 空串</li>
	 * </ul>
	 * */
	public boolean findNextInAll(String word, boolean caseSensitive, boolean wholeWord, boolean wrap) {
		checkWidget();
		if (word == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		if (word.isEmpty()) {
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		}
		
		String text = editor.getText();
		
		if (!caseSensitive) {
			text = text.toLowerCase();
			word = word.toLowerCase();
		}
		
		if (!text.contains(word)) {
			return false;
		}
		
		while (true) {
			int index = text.indexOf(word, editor.getCaretOffset());
			if (wholeWord) {
				while (index!=-1 && fixIndexForWholeWord(index, word.length())) {
					index = text.indexOf(word, index+word.length());
				}
			}
			
			if (index == -1) {
				if (wrap) {
					findCount++;
					if (findCount == 2) {
						findCount = 0;
						return false;
					}
					editor.setCaretOffset(0);
					continue;
				} else {
					return false;
				}
			}
			
			this.gotoLine(editor.getLineAtOffset(index));
			editor.setSelectionRange(index, word.length());
			return true;
		}
	}
	
	private int indexForFindPrev;
	private boolean indexInited;
	
	
	/**
	 * 在全部文本中寻找.
	 * 
	 * 在接受者的全部文本范围向前查找字符串, 如果找到匹配子串, 匹配子串会被选择.
	 * 
	 * @param word 查找的单词.
	 * @param caseSensitive 区分大小写选项.
	 * @param wholeWord 作为整体选项.
	 * @param wrap 回卷选项.
	 * @return 如果查找到, 返回真, 否则返回假.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 word 为 null</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 word 为 空串</li>
	 * </ul>
	 * */
	public boolean findPrevInAll(String word, boolean caseSensitive, boolean wholeWord, boolean wrap) {
		checkWidget();
		if (word == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		if (word.isEmpty()) {
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		}
		
		String text = editor.getText();
		
		if (!caseSensitive) {
			text = text.toLowerCase();
			word = word.toLowerCase();
		}
		
		if (!indexInited) {
			indexForFindPrev = editor.getCaretOffset();
			indexInited = true;
		}
		
		if (!text.contains(word)) {
			return false;
		}
			
		while (true) {
			indexForFindPrev = text.lastIndexOf(word, indexForFindPrev-1);
			if (wholeWord) {
				while (indexForFindPrev!=-1 && fixIndexForWholeWord(indexForFindPrev, word.length())) {
					indexForFindPrev = text.lastIndexOf(word, indexForFindPrev-1);
				}
			}
			
			if (indexForFindPrev == -1) {
				if (wrap) {
					findPrevCount++;
					if (findPrevCount == 2) {
						findPrevCount = 0;
						return false;
					}
					indexForFindPrev = editor.getCharCount();
					continue;
				} else {
					return false;
				}
			}
			
			this.gotoLine(editor.getLineAtOffset(indexForFindPrev));
			editor.setSelectionRange(indexForFindPrev, word.length());
			indexForFindPrev--;
			return true;
		}
	}
	
	/**
	 * 在全部文本中替换.
	 * 
	 * 替换当前被选择的文本, 被选择的文本不必是 findNextInAll() 查找到的文本. 
	 * 
	 * @param newWord 替换用的单词.
	 * @param word 被替换的单词.
	 * @param caseSensitive 区分大小写选项.
	 * @param wholeWord 作为整体选项.
	 * @param wrap 回卷选项.
	 * @return 如果当前无选择文本, 返回 -1; 如过在替换当前之后, 后续文本没有与 word 匹配的部分, 返回 -2, 其他返回 0.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 word 或 newWord 为 null</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 word 为 空串</li>
	 * </ul>
	 * */
	public int replaceNextInAll(String newWord, String word, boolean caseSensitive, boolean wholeWord, boolean wrap) {
		checkWidget();
		if (word==null || newWord==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		if (word.isEmpty()) {
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		}
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return -1;
		this.insert(newWord);
		return this.findNextInAll(word, caseSensitive, wholeWord, wrap)?0:-2;
	}
	
	/**
	 * 在全部文本中替换.
	 * 
	 * 替换当前被选择的文本, 被选择的文本不必是 findPrevInAll() 查找到的文本. 
	 * 
	 * @param newWord 替换用的单词.
	 * @param word 被替换的单词.
	 * @param caseSensitive 区分大小写选项.
	 * @param wholeWord 作为整体选项.
	 * @param wrap 回卷选项.
	 * @return 如果当前无选择文本, 返回 -1; 如过在替换当前之后, 前面的文本没有与 word 匹配的部分, 返回 -2, 其他返回 0.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 word 或 newWord 为 null</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 word 为 空串</li>
	 * </ul>
	 * */
	public int replacePrevInAll(String newWord, String word, boolean caseSensitive, boolean wholeWord, boolean wrap) {
		checkWidget();
		if (word==null || newWord==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		if (word.isEmpty()) {
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		}
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return -1;
		this.insert(newWord);
		return this.findPrevInAll(word, caseSensitive, wholeWord, wrap)?0:-2;
	}
	
	/**
	 * 在全部文本中替换.
	 * 
	 * 向前替换所有匹配的单词.
	 * 
	 * @param newWord 替换用的单词.
	 * @param word 被替换的单词.
	 * @param caseSensitive 区分大小写选项.
	 * @param wholeWord 作为整体选项.
	 * @param wrap 回卷选项.
	 * @return 被替换的次数.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 word 或 newWord 为 null</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 word 为 空串</li>
	 * </ul>
	 * */
	public int replacePrevAll(String newWord, String word, boolean caseSensitive, boolean wholeWord, boolean wrap) {
		checkWidget();
		if (word==null || newWord==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		if (word.isEmpty()) {
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		}
		int replaceCount = 0;
		if (findPrevInAll(word, caseSensitive, wholeWord, wrap)) {
			while (replacePrevInAll(newWord, word, caseSensitive, wholeWord, wrap)>-1) {
				replaceCount++;
			}
			replaceCount++;
		}
		return replaceCount;
	}

	/**
	 * 在全部文本中替换.
	 * 
	 * 向后替换所有匹配的单词.
	 * 
	 * @param newWord 替换用的单词.
	 * @param word 被替换的单词.
	 * @param caseSensitive 区分大小写选项.
	 * @param wholeWord 作为整体选项.
	 * @param wrap 回卷选项.
	 * @return 被替换的次数.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 word 或 newWord 为 null</li>
	 * <li>SWT.ERROR_INVALID_ARGUMENT - 如果 word 为 空串</li>
	 * </ul>
	 * */	
	public int replaceNextAll(String newWord, String word, boolean caseSensitive, boolean wholeWord, boolean wrap) {
		checkWidget();
		if (word==null || newWord==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		if (word.isEmpty()) {
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		}
		int replaceCount = 0;
		if (findNextInAll(word, caseSensitive, wholeWord, wrap)) {
			while (replaceNextInAll(newWord, word, caseSensitive, wholeWord, wrap)>-1) {
				replaceCount++;
			}
			replaceCount++;
		}
		return replaceCount;
	}
	
	private boolean fixIndexForWholeWord(int index, int wordLen) {
		checkWidget();
		char left  = 0;
		char right = 0;
		try {
			left = editor.getTextRange(index-1, 1).charAt(0);
		} catch (Exception e) {
			left = 0;
		}
		
		try {
			right = editor.getTextRange(index+wordLen, 1).charAt(0);
		} catch (Exception e) {
			right = 0;
		}
		
		if ((Character.isLetterOrDigit(left)||left=='_') 
				|| (Character.isLetterOrDigit(right)||right=='_')) {
			return true;
		}
		return false;
	}

	private int regexFindIndex;
	
	/**
	 * 全部文本内查找模式.
	 * 
	 * @param regex 查找的模式.
	 * @param caseSensitive 区分大小写选项.
	 * @param wrap 回卷选项.
	 * @param dotAll dot 表示所有选项.
	 * @return 如果查找到匹配子串, 返回真, 否则返回假.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 regex 为空指针.</li>
	 * </ul>
	 * */
	public boolean findRegex(String regex, boolean caseSensitive, 
						boolean wrap, boolean dotAll) {
		checkWidget();
		if (regex == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		int flag = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
		flag |= dotAll ? Pattern.DOTALL : 0; 
		Pattern pattern = Pattern.compile(regex, flag);
		Matcher matcher = pattern.matcher(editor.getText());
		while (true) {
			if (matcher.find(regexFindIndex)) {
				regexFindIndex = matcher.end();
				this.gotoLine(editor.getLineAtOffset(regexFindIndex));
				editor.setSelection(matcher.start(), matcher.end());
				return true;
			}
			
			if (regexFindIndex == 0) {
				return false;
			} else {
				if (wrap) {
					regexFindIndex = 0;
				} else {
					return false;
				}
			}
		}
	}
	
	
	/**
	 * 全部文本内替换模式.
	 * 
	 * @param regex 查找的模式.
	 * @param replacement 替换用的文本.
	 * @param caseSensitive 区分大小写选项.
	 * @param wrap 回卷选项.
	 * @param dotAll dot 表示所有选项.
	 * @return 如果当前无选择文本, 返回 -1; 如过在替换当前之后, 前面的文本没有与 regex 匹配的部分, 返回 -2, 其他返回 0.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 regex 或 replacement 为空指针.</li>
	 * </ul>
	 * */
	public int replaceRegex(String regex, String replacement, 
				boolean caseSensitive, boolean wrap, boolean dotAll) {
		checkWidget();
		if (regex==null || replacement==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return -1;
		
		int flag = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
		flag |= dotAll ? Pattern.DOTALL : 0; 
		Pattern pattern = Pattern.compile(regex, flag);
		Matcher matcher = pattern.matcher(editor.getSelectionText());
		String  result  = matcher.replaceAll(fixEscape(replacement));
		this.insert(result);
		regexFindIndex = selection.x + result.length();
		return this.findRegex(regex, caseSensitive, wrap, dotAll)?0:-2;
	}
	
	private String fixEscape(String replacement) {
		checkWidget();
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<replacement.length(); i++ ) {
			char c = replacement.charAt(i);
			if (c == '\\') {
				i++;
				if (i<replacement.length()) {
					c = replacement.charAt(i);
					switch (c) {
					case '\\':
						sb.append(c);
						break;
					case 't':
						sb.append('\t');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 'n':
						sb.append('\n');
						break;
					default:
						sb.append('\\');
						sb.append(c);	
					}
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 全部文本内替换模式.
	 * 
	 * 一次替换全部匹配子串.
	 * 
	 * @param regex 查找的模式.
	 * @param replacement 替换用的文本.
	 * @param caseSensitive 区分大小写选项.
	 * @param dotAll dot 表示所有选项.
	 * @exception SWTError <ul>
	 * <li>SWT.ERROR_NULL_ARGUMENT - 如果 regex 或 replacement 为空指针.</li>
	 * </ul>
	 * */
	public void replaceAllRegex(String regex, String replacement, 
					boolean caseSensitive, boolean dotAll) {
		checkWidget();
		if (regex==null || replacement==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		int flag = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
		flag |= dotAll ? Pattern.DOTALL : 0; 
		Pattern pattern = Pattern.compile(regex, flag);
		Matcher matcher = pattern.matcher(editor.getText());
		String  result  = matcher.replaceAll(fixEscape(replacement)); 
		editor.selectAll();
		this.insert(result);
	}
	
	private boolean isReading;
	
	/**
	 * 设置编辑区域文本.
	 * 
	 * @param text 文本.
	 * */
	public void setText(String text) {
		checkWidget();
		isReading = true;
		editor.setText(text);
		isReading = false;
	}
	
	/**
	 * 获取 Caret 右边的字符.
	 * 
	 * 如果 Caret 右边没有字符, 返回0.
	 * 
	 * @return caret 右边的字符.
	 * */
	public char rightCharOfCaret() {
		checkWidget();
		String text = editor.getText();
		try {
			return text.charAt(editor.getCaretOffset());
		} catch (Exception e) {
			return 0;	
		}
	}

	/**
	 * 获取 Caret 左边的字符.
	 * 
	 * 如果 Caret 左边没有字符, 返回0.
	 * 
	 * @return caret 左边的字符.
	 * */
	public char leftCharOfCaret() {
		checkWidget();
		String text = editor.getText();
		try {
			return text.charAt(editor.getCaretOffset()-2);
		} catch (Exception e) {
			return 0;	
		}
	}
	
	/**
	 * 粘贴.
	 * */
	public void paste() {
		checkWidget();
		/*
		 * 当有选择文本时, paste() 会有几率出现 bug.
		 * */
		Point selection = editor.getSelection();
		editor.setSelection(selection.y, selection.x);
		// -----
		editor.paste();
	}
	
	/**
	 * 替换或插入文本.
	 * 
	 * 如果当前有被选择文本, 被选择文本会被替换为 text; 否则, 在当前 caret 位置插入 text.
	 * 
	 * @param text 插入的文本.
	 * */
	public void insert(String text) {
		checkWidget();
		// FIXME ----
		/*
		 * 当有选择文本时, insert() 会有几率出现 bug.
		 * */
		Point selection = editor.getSelection();
		editor.setSelection(selection.y, selection.x);
		// -----
		
		editor.insert(text);
	}
	
	/**
	 * 撤销操作.
	 * */
	public void undo() {
		checkWidget();
		if (undoMan.canUndo()) {
			wordCompleter.enableWordCompleter(false);
			undoMan.undo();
			if (undoListener != null) {
				UndoRedoEvent e = new UndoRedoEvent();
				e.sigmai = this;
				e.isUndo = true;
				undoListener.undoRedo(e);
			}
			wordCompleter.enableWordCompleter(true);
		}
	}
	
	/**
	 * 判断当前是否有可撤销操作.
	 * 
	 * @return 结果. 
	 * */
	public boolean canUndo() {
		checkWidget();
		return undoMan.canUndo();
	}
	
	/**
	   * 恢复撤销.
	 * */
	public void redo() {
		checkWidget();
		if (undoMan.canRedo()) {
			wordCompleter.enableWordCompleter(false);
			undoMan.redo();
			if (undoListener != null) {
				UndoRedoEvent e = new UndoRedoEvent();
				e.sigmai = this;
				e.isUndo = false;
				undoListener.undoRedo(e);
			}
			wordCompleter.enableWordCompleter(true);
		}
	}
	
	/**
	 * 判读接受者当前是否有可重做的被撤销操作.
	 * 
	 * @return 结果.
	 * */
	public boolean canRedo() {
		checkWidget();
		return undoMan.canRedo();
	}
	
	/**
	 * 设置最大允许可撤销操作的次数.
	 * 
	 * 默认为 1024.
	 * 
	 * @param limit 指定的值.
	 * */
	public void setUndoLimit(int limit) {
		checkWidget();
		undoMan.setUndoLimit(limit);
	}
	
	/**
	 * 设置获取焦点.
	 * 
	 *  如果开启高亮当前行功能, 此方法会高亮光标所在行.
	 * 
	 * @return 如果返回真表示接受者成功获取焦点, 否则返回假.
	 * */
	@Override
	public boolean setFocus() {
		checkWidget();
		boolean r = editor.setFocus();
		if (r)
			this.highLightCurrent();
		return r;
	}
	
	private void autoCompletePair(char c) {
		checkWidget();
		if (pairListener == null) {
			insertRightHalf(c);
		} else {
			PairEvent e = new PairEvent();
			e.sigmai    = this;
			e.character = c;
			pairListener.typedLeftHalf(e);
			if (!e.doit) return;
			insertRightHalf(c);
		}
	}
	
	private void insertRightHalf(char c) {
		checkWidget();
		switch (c) {
		case '{':
			if (pairs[0])
			this.insert("}");
			break;
		case '[':
			if (pairs[1])
			this.insert("]");
			break;
		case '(':
			if (pairs[2])
			this.insert(")");
			break;
		case '<':
			if (pairs[3])
			this.insert(">");
			break;
		case '\'':
			if (pairs[4])
			this.insert("'");
			break;
		case '"':
			if (pairs[5])
			this.insert("\"");
			break;
		}
	}
	
	private void updateMargin() {
		checkWidget();
		if (lineMargin != null) {
			lineMargin.setTopPixel(editor.getTopPixel());
			lineMargin.setMaxLineCount(editor.getLineCount());
			lineMargin.setInc(editor.getLineHeight());
		}
		if (foldMargin != null) {
			foldMargin.setTopPixel(editor.getTopPixel());
			foldMargin.setInc(editor.getLineHeight());
		}
	}
	
	
	private Set<MultiCommentToken> cacheCommentRanges;
	
	/**
	 * 进行词法高亮.
	 * */
	private void paintLexeme(String highLightWord) {
		checkWidget();
		if (lexer != null) {
			cacheCommentRanges = null;
			lexer.tokenization(0, editor.getText(), highLightWord);
			Iterator<Token> styleTokens = lexer.getTokens().iterator();
			List<Integer> wordFixList = new ArrayList<>();
			StylePalette stylePalette = lexer.getStylePalette();
			//System.out.println(styleTokens+"#");
			StyleRange[] styles = new StyleRange[lexer.getTokens().size()];
			for (int i=0; i<styles.length; i++) {
				Token token = styleTokens.next();	
				styles[i] = new StyleRange();
				styles[i].start  = token.start;
				styles[i].length = token.length;
				if (stylePalette != null) {
					StylePalette.Style style = stylePalette.getStyle(token.type);
					styles[i].foreground = style.color;
					styles[i].fontStyle  = style.fontStyle;
				}
				if (token.isHighLightWord) {
					styles[i].background = highLightWordColor;
					wordFixList.add(editor.getLineAtOffset(styles[i].start));
				}
			}
			editor.replaceStyleRanges(0, editor.getCharCount(), styles);
			editor.redraw();
			
			if (lexer.isSupportMultiComment()) {
				cacheCommentRanges = lexer.getMultiCommentTokenList();
			}
			
			if (foldMargin != null) {
				foldMargin.cleanDots();
				TreeSet<FoldToken> foldTokens = lexer.getFoldTokens();
				for (var ft : foldTokens) {
					FoldDot dot = new FoldDot();
					dot.startOffset = ft.start;
					dot.endOffset = ft.end;
					dot.startLine = editor.getLineAtOffset(ft.start);
					dot.endLine   = editor.getLineAtOffset(ft.end);
					foldMargin.addFoldDots(dot);
				}
				foldMargin.redraw();
			}
			
			wordFixMargin.setFixList(wordFixList);
			wordFixMargin.setMaxLine(editor.getLineCount());
			wordFixMargin.redraw();
		}
	}
	
	private String newText;
	
	private boolean isInComment;
	
	/*
	public boolean isInComment() {
		checkWidget();
		return isInComment;
	}*/
	
	private void checkInComment(int offset) {
		checkWidget();
		isInComment = false;
		if (cacheCommentRanges != null) {
			for (var p : cacheCommentRanges) {
				if (offset>=p.start && offset<p.end) {
					isInComment = true;
					break;
				}
			}
		}
		if (isInComment) {
			// FIXME 在注释区域才使用英语单词.
			wordCompleter.showVocabulary = true;
		} else {
			wordCompleter.showVocabulary = false;
		}
	}	
	
	private void addListeners() {
		checkWidget();
		this.addDisposeListener(e->{
			callTip.dispose();
			normalCaret.dispose();
			overWriteCaret.dispose();
			wordPane.dispose();
		});
		
		editor.addModifyListener(e->{
			this.highLightCurrent();
			try {
				this.paintLexeme("FIXME");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			checkInComment(editor.getCaretOffset());
		});
		
		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				SigmaI.this.highLightWord();
				indexInited = false;
			}
		});
		
		editor.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.stateMask&SWT.ALT)!=0 && e.keyCode==SWT.INSERT) {
					
				} else if ((e.stateMask&SWT.SHIFT)!=0 && e.keyCode==SWT.INSERT) {
					
				} else if ((e.stateMask&SWT.CTRL)!=0 && e.keyCode==SWT.INSERT) {
					//do nothing
				} else if (e.keyCode == SWT.INSERT) {
					updateInputMode();
				} else {
					autoCompletePair(e.character);						
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		editor.addVerifyKeyListener(e->{
			if (e.keyCode == SWT.ARROW_DOWN) {
				updateCompleteWordPane(e, true);
			} else if (e.keyCode == SWT.ARROW_UP) {
				updateCompleteWordPane(e, false);
			} else if (e.keyCode==SWT.HOME && (e.stateMask&SWT.SHIFT)!=0) {
				fixHomeSelection();
				e.doit = false;
			} else if (e.keyCode == SWT.HOME) {
				fixHome();
				e.doit = false;
			} else if (e.character == SWT.TAB) {
				if (fixTab()) {
					e.doit = false;
				}
			} else if (e.character == SWT.BS) {
				if (fixBS()) {
					e.doit = false;
				}
			}
		});
		
		editor.addPaintListener(e->{
			updateMargin();
			showVerticalEdge(e.gc);
		});
		
		editor.addCaretListener(e->{
			highLightCurrent();
			matchBraces();
			checkInComment(e.caretOffset);
		});
		
		editor.getContent().addTextChangeListener(wordCompleter);
		editor.addKeyListener(wordCompleter);
		editor.addMouseListener(wordCompleter);
		editor.addVerifyKeyListener(wordCompleter);
		editor.addKeyListener(wordCompleter);
		editor.addMouseListener(wordCompleter);
		editor.addModifyListener(wordCompleter);
		editor.addVerifyListener(wordCompleter);
		
		if (lineMargin != null) {
			lineMargin.setFixWidthListener(e->{
				FormData data = (FormData)lineMargin.getLayoutData();
				data.right = new FormAttachment(0, e);
				if (foldMargin !=null) {
					data = (FormData)foldMargin.getLayoutData();
					data.right = new FormAttachment(0, e+13);
				}
				this.layout();
			});
			
			lineMargin.setClickListener(e->{
				try {
					editor.setFocus();
					gotoLine(e);
				} catch (Throwable ex) {
					gotoLine(editor.getLineCount()-1);
				}
			});
		}
		
		undoMan.setUndoListener(new UndoListener() {

			@Override
			public void onUndo0(UndoInfo info) {
				editor.setSelectionRange(info.pos, info.newText.length());
				SigmaI.this.insert(info.replacedText);
				editor.setSelectionRange(info.pos, info.replacedText.length());
			}

			@Override
			public void onRedo0(UndoInfo info) {
				editor.setSelectionRange(info.pos, info.replacedText.length());
				SigmaI.this.insert(info.newText);
				editor.setSelectionRange(info.pos, info.newText.length());
			}

			@Override
			public void onUndo1(UndoInfo info) {
				editor.setSelectionRange(info.pos, info.newText.length());
				SigmaI.this.insert("");
				editor.setSelection(info.pos);
			}

			@Override
			public void onRedo1(UndoInfo info) {
				editor.setSelection(info.pos);
				SigmaI.this.insert(info.newText);
				editor.setSelectionRange(info.pos, info.newText.length());
			}

			@Override
			public void onUndo2(UndoInfo info) {
				editor.setSelection(info.pos);
				SigmaI.this.insert(info.replacedText);
				editor.setSelectionRange(info.pos, info.replacedText.length());
			}

			@Override
			public void onRedo2(UndoInfo info) {
				editor.setSelectionRange(info.pos, info.replacedText.length());
				SigmaI.this.insert("");
				editor.setSelection(info.pos);
			}
		});
		
		editor.getContent().addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChanging(TextChangingEvent event) {
				newText = event.newText;				
			}
			@Override
			public void textChanged(TextChangedEvent event) {}
			@Override
			public void textSet(TextChangedEvent event) {}
			
		});
		
		editor.addExtendedModifyListener(e->{
			if (!isReading) {
				undoMan.addUndoInfo(e.start, e.replacedText, newText);
			}
		});
		
		editor.getIME().addListener(SWT.ImeComposition, e->{
			/*
			 * FIXME
			 * 
			 * SWT StyledText 组件特性, 当在 StyledText 有选择文本时使用
			 * IME 输入文本, 被选择文本会被替换, 但 StyledText 的ExtendedModifyListener,
			 *    无法获取被替换的文本, 会导致 UndoManager 出现错误.
			 * */
			int    start 		= editor.getSelectionRange().x; // 这里必须将 start 设为选择文本的开始.
			String replacedText = editor.getSelectionText();
			undoMan.addUndoInfo(start, replacedText, null);
		});
		
		editor.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				if (e.text.equals(editor.getLineDelimiter())) {
					autoIndent(e);
				}
				handleEnterCommentAreaEvent(e.text);
			}
		});
		
		wordPane.setMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				String inputWord = getWordAtOffset(editor.getCaretOffset());
				int wordStart = getWordStart();
				editor.setSelectionRange(wordStart, inputWord.length());
				insert(wordPane.getSelection());
				hideCompleteWordPane();
			}
		});
	}
	

	private boolean fixBS() {
		checkWidget();
		if (editor.getSelectionCount() != 0) {
			return false;
		}
		int caretOffset = editor.getCaretOffset();
		int lineIndex   = editor.getLineAtOffset(caretOffset);
		int lineOffset  = editor.getOffsetAtLine(lineIndex);
		int tabOffset   = caretOffset - lineOffset;
		String text = editor.getLine(lineIndex);
		if (text.isEmpty()) {
			return false;
		}
		for (int i=0; i<tabOffset; i++) {
			if (text.charAt(i)!='\t' && text.charAt(i)!=' ') {
				return false;
			}
		}
		try {
			String prevLine = editor.getLine(lineIndex-1);
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<prevLine.length(); i++ ) {
				char c = prevLine.charAt(i); 
				if (c=='\t' || c==' ') {
					sb.append(c);
				} else {
					break;
				}
			}
			if (sb.length()==prevLine.length() || tabOffset<=sb.length()) {
				return false;
			}
			editor.setSelectionRange(lineOffset, text.length());
			this.insert(sb.toString() + text.trim());
			editor.setSelection(lineOffset + sb.length());
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	private boolean fixTab() {
		checkWidget();
		if (editor.getSelectionCount() != 0) {
			return false;
		}
		int caretOffset = editor.getCaretOffset();
		int lineIndex   = editor.getLineAtOffset(caretOffset);
		int lineOffset  = editor.getOffsetAtLine(lineIndex);
		int tabOffset   = caretOffset - lineOffset;
		String text = editor.getLine(lineIndex);
		if (text.isEmpty()) {
			return false;
		}
		for (int i=0; i<tabOffset; i++) {
			if (text.charAt(i)!='\t' && text.charAt(i)!=' ') {
				return false;
			}
		}
		try {
			String prevLine = editor.getLine(lineIndex-1);
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<prevLine.length(); i++ ) {
				char c = prevLine.charAt(i); 
				if (c=='\t' || c==' ') {
					sb.append(c);
				} else {
					break;
				}
			}
			if (sb.length()==prevLine.length() || tabOffset>=sb.length()) {
				return false;
			}
			editor.setSelectionRange(lineOffset, text.length());
			this.insert(sb.toString() + text.trim());
			editor.setSelection(lineOffset + sb.length());
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	private void fixHomeSelection() {
		checkWidget();
		int caretOffset = editor.getCaretOffset();
		int lineIndex = editor.getLineAtOffset(caretOffset);
		int lineOffset = editor.getOffsetAtLine(lineIndex);
		int offsetInLine = caretOffset - lineOffset;
		String line = editor.getLine(lineIndex);
		int spaceCount = 0;
		for (int i=0; i<line.length(); i++) {
			if (Character.isWhitespace(line.charAt(i))) {
				spaceCount++;
			} else {
				break;
			}
		}
		
		if (offsetInLine == 0) {
			Point selection = editor.getSelection();
			if (selection.x != selection.y) { // has selection
				editor.setSelection(selection.y, lineOffset+spaceCount);
			} else {						  // has no selection
				editor.setSelection(0, spaceCount);
			}
		} else if (offsetInLine<=spaceCount) {
			Point selection = editor.getSelection();
			if (selection.x != selection.y) { // has selection
				editor.setSelection(selection.y, lineOffset);
			} else {  // has no selection
				editor.setSelection(lineOffset+offsetInLine, lineOffset);
			}
		} else {
			Point selection = editor.getSelection();
			if (selection.x != selection.y) { // has selection
				editor.setSelection(selection.y, lineOffset+spaceCount);
			} else { // has no selection
				editor.setSelection(lineOffset+offsetInLine, lineOffset+spaceCount);
			}
		}
	}

	private void fixHome() {
		checkWidget();
		int caretOffset = editor.getCaretOffset();
		int lineIndex = editor.getLineAtOffset(caretOffset);
		int lineOffset = editor.getOffsetAtLine(lineIndex);
		int offsetInLine = caretOffset - lineOffset;
		String line   = editor.getLine(lineIndex);
		int spaceCount = 0;
		for (int i=0; i<line.length(); i++) {
			if (Character.isWhitespace(line.charAt(i))) {
				spaceCount++;
			} else {
				break;
			}
		}
		if (offsetInLine == 0) {
			editor.setSelection(lineOffset+spaceCount);
		} else if (offsetInLine<=spaceCount) {
			editor.setSelection(lineOffset);
		} else {
			//System.out.println(lineOffset+spaceCount);
			editor.setSelection(lineOffset+spaceCount);
		}
	}
	
	private void updateCompleteWordPane(VerifyEvent e, boolean down) {
		checkWidget();
		if (wordPane.isVisible()) {
			if (down) {
				wordPane.selectionDown();
			} else {
				wordPane.selectionUp();
			}
			e.doit = false;
		}
	}

	private void showVerticalEdge(GC gc) {
		checkWidget();
		if (verticalEdgeBound > 0) {
			int pos = gc.getAdvanceWidth(' ') * verticalEdgeBound;
			int y   = editor.getSize().y;
			int x   = pos-editor.getHorizontalPixel();
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_CYAN));
			gc.drawLine(x, 0, x, y);
		}
	}
	
	private int verticalEdgeBound = 80;
	
	/**
	 * 设置垂直线边界.
	 * 
	 * @param bound 边界, 如果 bound <= 0, 表示不显示垂直线.
	 * */
	public void setVerticalEdgeBound(int bound) {
		checkWidget();
		this.verticalEdgeBound = bound;
	}
	
	/**
	 * 判断当前输入模式是否是覆盖模式.
	 * 
	 * @return 结果.
	 * */
	public boolean isOverWriteMode() {
		checkWidget();
		return isOverWriteMode;
	}
	
	private InputModeChangedListener inputModeChangedListener;
	
	/**
	 * 设置 InputModeChangedListener.
	 * 
	 * InputModeChangedListener 监听输入模式改变动作.
	 * 
	 * @param listener 
	 * */
	public void setInputModeChangedListener(InputModeChangedListener listener) {
		checkWidget();
		this.inputModeChangedListener = listener;
	}
	
	private void updateInputMode() {
		checkWidget();
		if (!isOverWriteMode) {
			editor.setCaret(overWriteCaret);
			isOverWriteMode = true;
		} else {
			editor.setCaret(normalCaret);
			isOverWriteMode = false;
		}
		
		if (inputModeChangedListener != null) {
			InputModeChangedEvent e = new InputModeChangedEvent();
			e.sigmai = this;
			e.isOverwirteMode = isOverWriteMode;
			inputModeChangedListener.inputModeChanged(e);
		}
		
	}

	/**
	 * 隐藏函数调用提示.
	 * */
	public void hideCallTip() {
		checkWidget();
		callTip.hideTip();
	}

	private WordCompleter wordCompleter;
	
	private void initResources() {
		checkWidget();
		highLight = getDisplay().getSystemColor(SWT.COLOR_GRAY);
		highLightWordColor = getDisplay().getSystemColor(SWT.COLOR_GREEN);
		undoMan   = new UndoManager();
		callTip   = new FunctionCallTip();
		wordPane  = new CompleteWordPane(editor.getShell());
		normalCaret = new Caret(editor, SWT.NONE);
		overWriteCaret = new Caret(editor, SWT.NONE);
		int lineHeight = editor.getLineHeight();
		normalCaret.setSize(2, lineHeight);
		overWriteCaret.setSize(lineHeight>>1, lineHeight);
		editor.setCaret(normalCaret);
		wordCompleter = new WordCompleter();
		wordCompleter.setSigmaI(this);
	}

	private void checkStyle(int style) {
		checkWidget();
		if ((style&LINE_MARGIN)!=0) {
			hasLineMargin = true;
		}
		if ((style&FOLD_MARGIN)!=0) {
			hasFoldMargin = true;
		}

		if ((style&WIN7)!=0) {
			scrollBarWidth = 17;
		}
		if ((style&GNOME)!=0) {
			scrollBarWidth = 1;
		}
	}

	private void initContent() {
		checkWidget();
		this.setLayout(new FormLayout());
		createLineMargin();
		createFoldMargin();
		createEditor();
	}

	private void createLineMargin() {
		checkWidget();
		if (hasLineMargin) {
			lineMargin = new LineMargin(this);
			FormData data = new FormData();
			data.top  = new FormAttachment(0);
			data.left = new FormAttachment(0);
			data.bottom = new FormAttachment(100, -scrollBarWidth);
			data.right  = new FormAttachment(0, 10);
			lineMargin.setLayoutData(data);
		}
	}
	
	private void createFoldMargin() {
		checkWidget();
		if (hasFoldMargin) {
			foldMargin = new FoldMargin(this);
			FormData data = new FormData();
			data.top  = new FormAttachment(0);
			if (hasLineMargin)
				data.left = new FormAttachment(lineMargin);
			else
				data.left = new FormAttachment(0);
			data.bottom = new FormAttachment(100, -scrollBarWidth);
			data.right  = new FormAttachment(0, 0);
			foldMargin.setLayoutData(data);
		}
	}

	private void createEditor() {
		checkWidget();
		editor = new StyledText(this, SWT.V_SCROLL|SWT.H_SCROLL|SWT.FULL_SELECTION);
		FormData data = new FormData();
		data.top      = new FormAttachment(0);
		
		if (hasFoldMargin) {
			data.left = new FormAttachment(foldMargin);
			foldMargin.setInc(editor.getLineHeight());
		} else if (hasLineMargin) {
			data.left = new FormAttachment(lineMargin);
		}
		if (hasLineMargin) {
			lineMargin.setInc(editor.getLineHeight());
		}
		data.bottom = new FormAttachment(100);
		data.right  = new FormAttachment(100, -20);
		editor.setLayoutData(data);
		editor.setLeftMargin(3);
		editor.setTopMargin(3);
		
		wordFixMargin = new WordFixMargin(this);
		data = new FormData();
		data.top = new FormAttachment(0);
		data.bottom = new FormAttachment(100, -scrollBarWidth);
		data.left   = new FormAttachment(editor);
		data.right  = new FormAttachment(100);
		wordFixMargin.setLayoutData(data);
	}
	
	/**
	 * 在选择文本中替换.
	 * 
	 * 将选择文本中匹配 regex 的子串用 replacement 替换.
	 * 
	 * @param regex 配替换的模式.
	 * @param replacement 替换用的文本.
	 * @param dotAll dot 表示所有选项.
	 * @param caseSensitive 区分大小写选项.
	 * */
	public void replaceInSelection(String regex, String replacement,
								boolean dotAll, boolean caseSensitive) {
		checkWidget();
		if (regex==null || replacement==null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		int flag = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
		flag |= dotAll ? Pattern.DOTALL : 0; 
		Pattern pattern = Pattern.compile(regex, flag);
		Matcher matcher = pattern.matcher(editor.getSelectionText());
		int selectionStart = editor.getSelection().x;
		String  result  = matcher.replaceAll(fixEscape(replacement)); 
		this.insert(result);
		editor.setSelectionRange(selectionStart, result.length());
	}
	
	/**
	 * 获取当前光标当前行和列.
	 * 
	 * 获取光标位置, 如果 tabAsOne 为真, 函数将 tab 视为一个字符; 否则被视为当前 tab 宽度.
	 * 返回结果, Point.x 为行, Point.y 为列, 计数从 0 开始.
	 * 
	 * @param tabAsOne 是否将 tab 视为 1 个字符.
	 * @return 光标所处位置.
	 * */
	public Point getCurrentRowAndCol(boolean tabAsOne) {
		checkWidget();
		int offset = editor.getCaretOffset();
		int row    = editor.getLineAtOffset(offset);
		int colTabAsOne = offset - editor.getOffsetAtLine(row);
		if (tabAsOne) return new Point(row, colTabAsOne);
		char[] line = editor.getLine(row).toCharArray();
		int col = 0; 
		for (int i=0; i<colTabAsOne; i++) {
			if (line[i] == '\t') {
				if ((col % editor.getTabs())!=0) {
					col += (editor.getTabs() - (col % editor.getTabs()));
				} else {
					col += editor.getTabs();
				}
			} else {
				col++;
			}
		}
		return new Point(row, col);
	}
	
	/**
	 * 复制当前行.
	 * */
	public void copyLine() {
		checkWidget();
		int oldCaretOffset = editor.getCaretOffset();
		int lineIndex = editor.getLineAtOffset(oldCaretOffset);
		String line   = editor.getLine(lineIndex);
		int offset    = editor.getOffsetAtLine(lineIndex)+line.length();
		editor.setSelection(offset);
		this.insert(editor.getLineDelimiter()+line);
		editor.setSelection(oldCaretOffset);
	}
	
	/**
	 * 删除当前行.
	 * */
	public void deleteLine() {
		checkWidget();
		int lineIndex = editor.getLineAtOffset(editor.getCaretOffset());
		int startOffset = editor.getOffsetAtLine(lineIndex);
		int length = editor.getLine(lineIndex).length();
		if (lineIndex < editor.getLineCount()-1)
			length = editor.getLine(lineIndex).length() + editor.getLineDelimiter().length();
		editor.setSelectionRange(startOffset, length);
		this.insert("");
	}
	
	/**
	 * 排序被选择行. 
	 * */
	public void sortLines() {
		checkWidget();
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return; // no selection.
		int startLine = editor.getLineAtOffset(selection.x);
		int endLine   = editor.getLineAtOffset(selection.y);
		List<String> lines = new ArrayList<>();
		for (int i=startLine; i<=endLine; i++) {
			lines.add(editor.getLine(i));
		}
		int fixEndOffset = lines.get(lines.size()-1).length(); // before sort.
		lines.sort(null);
		StringBuffer sb = new StringBuffer();
		String endLineText = lines.get(lines.size()-1);
		for (int i=0; i<lines.size()-1; i++) {
			sb.append(lines.get(i) + editor.getLineDelimiter());
		}
		sb.append(endLineText);
		int startOffset = editor.getOffsetAtLine(startLine);
		int endOffset   = editor.getOffsetAtLine(endLine) + fixEndOffset;
		editor.setSelection(startOffset, endOffset);
		this.insert(sb.toString());
		editor.setSelection(startOffset, endOffset);
	}
	
	/**
	 * 反转被选择行.
	 * */
	public void revolveLines() {
		checkWidget();
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return; // no selection.
		int startLine = editor.getLineAtOffset(selection.x);
		int endLine   = editor.getLineAtOffset(selection.y);
		Stack<String> lines = new Stack<>();
		for (int i=startLine; i<=endLine; i++) {
			lines.push(editor.getLine(i));
		}
		int fixEndOffset = lines.peek().length(); // before sort.
		StringBuffer sb = new StringBuffer();
		String endLineText = lines.get(0);
		while (lines.size()>1) {
			sb.append(lines.pop() + editor.getLineDelimiter());
		}
		sb.append(endLineText);
		int startOffset = editor.getOffsetAtLine(startLine);
		int endOffset   = editor.getOffsetAtLine(endLine) + fixEndOffset;
		editor.setSelection(startOffset, endOffset);
		this.insert(sb.toString());
		editor.setSelection(startOffset, endOffset);
	}
	
	/**
	 * 分割被选择行.
	 * */
	public void splitLines() {
		checkWidget();
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return; // no selection.
		int startLine = editor.getLineAtOffset(selection.x);
		int endLine   = editor.getLineAtOffset(selection.y);
		StringBuffer tabs = new StringBuffer();
		int indentLength = 0;
		StringBuffer sb = new StringBuffer();
		String firstLine = editor.getLine(startLine);
		// get indent length
		for (int i=0; i<firstLine.length(); i++) {
			char c = firstLine.charAt(i);
			if (c == ' ') {
				tabs.append(c);
				indentLength++;
			} else if (c == '\t') {
				tabs.append(c);
				indentLength += editor.getTabs();
			} else {
				break;
			}
		}
		//get all text
		sb.append(firstLine.trim());
		if (startLine != endLine) {
			sb.append(editor.getLineDelimiter());
		}
		for (int i=startLine+1; i<endLine; i++) {
			String line = editor.getLine(i);
			sb.append(line.trim() + editor.getLineDelimiter());
		}
		if (startLine != endLine) {
			sb.append(editor.getLine(endLine).trim());
		}
		// split lines
		final int maxColumn = 70 - indentLength;
		boolean passMaxColumn = false;
		StringBuffer resultLines = new StringBuffer(tabs);
		for (int i=0, j=0; i<sb.length(); i++) {
			char c = sb.charAt(i);
			if (i<sb.length()-1 && c=='\r'&&sb.charAt(i+1)=='\n') {
				resultLines.append("\r\n" + tabs);
				j = 0;
				i++;
				passMaxColumn = false;
			} else if (c=='\r' || c=='\n') {
				resultLines.append(c).append(tabs);
				j = 0;
				passMaxColumn = false;
			} else if (j < maxColumn) {
				resultLines.append(sb.charAt(i));
				j++;
			} else if (j == maxColumn) {
				passMaxColumn = true;
				resultLines.append(sb.charAt(i));
				j++;
			} else if (passMaxColumn) {
				while (i<sb.length() 
						&& Character.isLetter(sb.charAt(i))
							&& j<80) {
					resultLines.append(sb.charAt(i));
					i++; j++;
				}
				if (i < sb.length()) {
					resultLines.append(sb.charAt(i));
				}
				resultLines.append(editor.getLineDelimiter()).append(tabs);
				j = 0;
				passMaxColumn = false;
			}
		}
		String endLineStr = editor.getLine(endLine);
		int startOffset = editor.getOffsetAtLine(startLine);
		int endOffset   = editor.getOffsetAtLine(endLine) + endLineStr.length();
		editor.setSelection(startOffset, endOffset);
		this.insert(resultLines.toString());
		editor.setSelectionRange(startOffset, resultLines.length());
	}
	
	/**
	 * 连接被选择行.
	 * */
	public void joinLines() {
		checkWidget();
		Point selection = editor.getSelection();
		if (selection.x == selection.y) return; // no selection.
		int startLine = editor.getLineAtOffset(selection.x);
		int endLine   = editor.getLineAtOffset(selection.y);
		StringBuffer lines = new StringBuffer();
		int endLineLength = 0;
		String firstLine = editor.getLine(startLine);
		if (startLine == endLine) {
			endLineLength = firstLine.length();
			lines.append(firstLine);
		} else {
			for (int i=startLine; i<endLine; i++) {
				lines.append(editor.getLine(i) + " ");
			}
			String endLineString = editor.getLine(endLine);
			endLineLength = endLineString.length();
			lines.append(endLineString);
		}
		int startOffset = editor.getOffsetAtLine(startLine);
		int endOffset   = editor.getOffsetAtLine(endLine) + endLineLength;
		editor.setSelection(startOffset, endOffset);
		this.insert(lines.toString());
		editor.setSelectionRange(startOffset, lines.length());
	}
}
