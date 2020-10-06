package org.sigmai;

import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

class WordCompleter implements KeyListener, 
								MouseListener, TextChangeListener, 
									VerifyKeyListener, ModifyListener, VerifyListener{


	private boolean enableWordCompleter;
	
	private SigmaI sigmai;
	private StyledText editor;
	
	private TreeSet<String> selectedWordList = new TreeSet<String>();
	private int changeOffset;
	
	void setSigmaI(SigmaI sigmai) {
		this.sigmai = sigmai;
		this.editor = sigmai.getStyledText();
	}
	
	void enableWordCompleter(boolean enable) {
		enableWordCompleter = enable;
	}
	
	@Override
	public void textChanging(TextChangingEvent event) {
		if (enableWordCompleter) {
			changeOffset = event.start;
		}
	}

	private TreeSet<String> allWords = new TreeSet<String>();
	private TreeSet<String> baseWords  = new TreeSet<String>();
	private String[] vocabulary = new String[0];
	private String inputWord;
	
	void addBaseWords(String[] baseWords) {
		for (String w : baseWords) {
			if (w!=null && !w.isEmpty()) {
				this.baseWords.add(w);
			}
		}
	}
	
	void setVocabulary(String[] vocabulary) {
		this.vocabulary = vocabulary;
	}
	
	boolean showVocabulary;
	
	private void setAllWordList(TreeSet<String> wordList) {
		allWords = wordList;
	}
	
	private void setAllWordList() {
		allWords.clear();
		String text = editor.getText();
		int index = 0;
		int len   = text.length();
		while (index < len) {
			char c = text.charAt(index);
			if (index<len && (Character.isLetter(c)||c=='_')) {
				int i = index;
				c = text.charAt(index);
				index++;
				while (index<len && 
						(Character.isLetterOrDigit(text.charAt(index))||text.charAt(index)=='_')) {
					index++;
				}
				allWords.add(text.substring(i, index));
			} else {
				index++;
			}
		}
	}
	
	private boolean isWordPartInput;
	
	@Override
	public void textChanged(TextChangedEvent event) {
		if (!enableWordCompleter) return;
		if (enterTyped) return;
		inputWord = sigmai.getWordAtOffset(changeOffset);
		//System.out.println(inputWord);
		if (inputWord.length() >= 1) {
			for (String w : allWords) {
				if (w.startsWith(inputWord)) {
					selectedWordList.add(w);
				}
			}
			for (String w : baseWords) {
				if (w!=null && w.startsWith(inputWord)) {
					selectedWordList.add(w);
				}
			}
			if (showVocabulary) {
				for (String w : vocabulary) {
					if (w!=null && w.startsWith(inputWord)) {
						selectedWordList.add(w);
					}
				}
			}
			//editor.setFocus();
			if (!selectedWordList.isEmpty() && isWordPartInput) {
				sigmai.showCompleteWordList(selectedWordList);
				selectedWordList.clear();
			}
		} else {
			selectedWordList.clear();
			sigmai.hideCompleteWordPane();
		}
	}

	@Override
	public void textSet(TextChangedEvent event) {
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (!enableWordCompleter) return;
		sigmai.hideCompleteWordPane();
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}
	
	private boolean isCtrlPressing;

	@Override
	public void keyPressed(KeyEvent e) {
		if (!enableWordCompleter) return;
		if (e.keyCode==' ' || e.keyCode=='\t') {
			sigmai.hideCompleteWordPane();
		}
		
		if (e.keyCode==SWT.CTRL || (e.stateMask&SWT.CTRL)!=0) {
			isCtrlPressing = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!enableWordCompleter) return;
		if (e.keyCode==' ' || e.keyCode=='\t') {
			sigmai.hideCompleteWordPane();
		}
		if (e.keyCode==SWT.CTRL || (e.stateMask&SWT.CTRL)!=0) {
			isCtrlPressing = false;
		}
	}

	private boolean enterTyped;
	
	@Override
	public void verifyKey(VerifyEvent e) {
		if (!enableWordCompleter) return;
		if (e.keyCode == SWT.CR) {
			enterTyped = true;
			if (inputWord != null) {
				try {
					String word = sigmai.getCompleteWord();
					if (word != null) {
						int wordStart = sigmai.getWordStart();
						editor.setSelectionRange(wordStart, inputWord.length());
						//System.out.println(wordStart+"@"+inputWord+"#"+word+"%");
						sigmai.insert(word);
						sigmai.hideCompleteWordPane();
						e.doit = false;
						sigmai.setAutoIndent(false);
					} else {
						sigmai.setAutoIndent(true);
						sigmai.hideCompleteWordPane();
					}
				} catch (Exception ex) {
					sigmai.hideCompleteWordPane();
				}
			}
		} else {
			enterTyped = false;
			if (e.keyCode!=SWT.ARROW_DOWN && e.keyCode!=SWT.ARROW_UP)
				sigmai.hideCompleteWordPane();
		}
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (!enableWordCompleter) return;
		if (sigmai.getLexer() == null) {
			this.setAllWordList();
		} else {
			this.setAllWordList(sigmai.getWordList());
		}
	}

	@Override
	public void verifyText(VerifyEvent e) {
		if (!isCtrlPressing && enableWordCompleter 
				&& e.text.matches("(?U)\\w")) {
			isWordPartInput = true;
		} else {
			isWordPartInput = false;
		}
	}
}
