package org.sigmai;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

interface FixWidthListener {
	void fix(int newWidth);
}

interface ClickListener {
	void clicked(int line);
}

class LineMargin extends Canvas {
	
	private int maxLineCount = 1;
	private int topPixel     = 0;
	private int inc          = 0;
	private static final int base = 2;
	
	private FixWidthListener fixListener;
	private ClickListener   clickListener;
	
	LineMargin(Composite parent) {
		super(parent, SWT.NO_BACKGROUND);
		addListeners();
	}
	
	void setFixWidthListener(FixWidthListener fixListener) {
		checkWidget();
		this.fixListener = fixListener;
	}
	
	void setClickListener(ClickListener clickListener) {
		checkWidget();
		this.clickListener = clickListener;
	}

	private void addListeners() {
		checkWidget();
		this.addPaintListener(e->{
			drawLines(e.gc);
		});
		
		this.addDisposeListener(e->{
			
		});
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				clickListener.clicked((e.y + topPixel - base) / inc);
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
	}
	
	void setInc(int inc) {
		checkWidget();
		if (this.inc != inc) {
			this.inc = inc;
			this.redraw();
		}
	}
	
	void setMaxLineCount(int max) {
		checkWidget();
		if (this.maxLineCount != max) {
			this.maxLineCount = max;
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
	
	private void drawLines(GC gc) {
		checkWidget();
		gc.setFont(this.getFont());
		fixListener.fix(gc.stringExtent(maxLineCount+"").x + 20); 
		Point size = getSize();
		//gc.setBackground(this.getBackground());
		//gc.setForeground(this.getForeground());
		gc.fillRectangle(0, 0, size.x, size.y);
		
		int topLine = (-20 - base + topPixel) / inc;
		int bottomLine = (size.y - base + topPixel)/inc + 1;
		if (bottomLine > maxLineCount) {
			bottomLine = maxLineCount;
		}
		for (int i=topLine; i<bottomLine; i++) { 
			int y = (i * inc + base) - topPixel;  
			String line = Integer.toString(i+1); 
			int lineWidth = gc.stringExtent(line).x; 
			int x = (size.x - lineWidth) >> 1;
			gc.drawString(line, x, y);
		}
		gc.drawLine(size.x-1, 0, size.x-1, 2000);
	}
}
