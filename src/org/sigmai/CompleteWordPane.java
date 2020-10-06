package org.sigmai;

import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

class CompleteWordPane {
	
	private Shell shell;
	private List  list;
	private Font  font;
	
	CompleteWordPane(Shell parent) {
		initContent(parent);
	}
	
	private String result;
	
	void resetWordList(TreeSet<String> wordList) {
		list.removeAll();
		for (String word : wordList) {
			list.add(word);
		}
		list.setSelection(0);
		result = list.getSelection()[0];
	}

	private void initContent(Shell parent) {
		shell = new Shell(parent, SWT.RESIZE | SWT.MODELESS);
		shell.setLayout(new FillLayout());
		shell.setSize(380, 270);
		list = new List(shell, SWT.V_SCROLL|SWT.H_SCROLL|SWT.SINGLE);
		font = new Font(shell.getDisplay(), "Courier New", 15, SWT.NORMAL);
		list.setFont(font);
	}
	
	Point getSize() {
		return shell.getSize();
	}
	
	void setBackground(Color color) {
		list.setBackground(color);
	}
	
	void setForeground(Color color) {
		list.setForeground(color);
	}
	
	void setMouseListener(MouseListener listener) {
		list.addMouseListener(listener);
	}
	
	String getSelection() {
		return list.getSelection()[0];
	}

	void open(int x, int y) {
		shell.setLocation(x, y);
		shell.setVisible(true);
	}
	
	void hide() {
		result = null;
		shell.setVisible(false);
	}
	
	String getWord() {
		return result;
	}
	
	void setWord(String word) {
		this.result = word;
	}
	
	boolean isVisible() {
		return shell.isVisible();
	}
	
	void dispose() {
		shell.dispose();
		font.dispose();
	}
	
	
	void selectionUp() {
		int selectIndex = list.getSelectionIndex();
		if (selectIndex > 0) {
			list.setSelection(selectIndex-1);
		} else {
			list.setSelection(list.getItemCount()-1);
		}
		try {
			result = list.getSelection()[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			result = null;
		}
	}	
	
	void selectionDown() {
		int selectIndex = list.getSelectionIndex();
		if (selectIndex < list.getItemCount()-1) {
			list.setSelection(selectIndex+1);
		} else {
			list.setSelection(0);
		}
		try {
			result = list.getSelection()[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			result = null;
		}
	}
}
