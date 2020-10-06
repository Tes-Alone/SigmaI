package org.sigmai;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

class WordFixMargin extends Canvas {

	private List<Integer> fixList;
	private int maxLine;
	
	WordFixMargin(Composite parent) {
		super(parent, SWT.NONE);
		fixList = new ArrayList<>();
		
		this.addPaintListener(e->{
			for (int fix : fixList) {
				int y = (fix * getSize().y) / maxLine;
				e.gc.drawRectangle(1, y, 17, 3);
			}
		});
	}
	
	void setMaxLine(int maxLine) {
		this.maxLine = maxLine;
	}
	
	void setFixList(List<Integer> fixList) {
		this.fixList = fixList;
	}
}
