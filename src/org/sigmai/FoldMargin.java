package org.sigmai;

import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

class FoldMargin extends Canvas {

	private TreeMap<Integer, FoldDot> foldDots;
	private TreeMap<Integer, FoldDot> foldedDots;
	
	FoldMargin(Composite parent) {
		super(parent, SWT.NO_BACKGROUND);
		
		foldDots   = new TreeMap<>();
		foldedDots = new TreeMap<>();
		addPaintListener(e->{
			drawDots(e.gc);
		});
	}
	
	void cleanDots() {
		checkWidget();
		foldDots.clear();
	}
	
	void addFoldDots(FoldDot dot) {
		checkWidget();
		foldDots.put(dot.startLine, dot);
	}

	private static final int base = 10;
	
	private void drawDots(GC gc) {
		checkWidget();
		Point size = getSize();
		gc.fillRectangle(0, 0, size.x, size.y);
		Set<Integer> dottedLines = foldDots.keySet();
		for (var l : dottedLines) {
			FoldDot dot = foldDots.get(l);
			int y = base + dot.startLine * inc - topPixel;
			drawDot(gc, y, dot);
		}
		for (var l : dottedLines) {
			FoldDot dot = foldDots.get(l);
			int y = base + dot.startLine * inc - topPixel;
			gc.fillRectangle(1, y, 10, 10);
			gc.drawRectangle(1, y, 10, 10);
			gc.drawLine(3, y+5, 8, y+5);
		}
	}

	private void drawDot(GC gc, int y, FoldDot dot) {
		checkWidget();
		if (dot.folded) {
			
		} else {
			int endY = base + dot.endLine * inc - topPixel;
			gc.drawLine(6, y+11, 6, endY);
			gc.drawLine(6, endY, 8, endY);
		}
	}
	
	private int inc 	 = 0;
	private int topPixel = 0;
	
	void setInc(int inc) {
		checkWidget();
		if (this.inc != inc) {
			this.inc = inc;
			this.redraw();
		}
	}
	
	void setTopPixel(int topPixel) {
		checkWidget();
		if (this.topPixel != topPixel) {
			this.topPixel = topPixel;
			this.redraw();
		}
	} 
}
