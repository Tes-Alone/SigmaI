package org.sigmai.lexer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class StylePalette {
	
	public static class Style {
		public Color color;
		public int   fontStyle;
	}
	
	private Style[] styles;
	private static Style defaultStyle;
	
	static {
		defaultStyle = new Style();
		defaultStyle.color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
		defaultStyle.fontStyle = SWT.NORMAL;
	}
	
	public StylePalette(int size) {
		styles = new Style[size];
		for (int i=0; i<size; i++) {
			styles[i] = defaultStyle;
		}
	}
	
	public Style getStyle(int tkType) {
		if (tkType<0 || tkType>=styles.length) {
			throw new SWTError(SWT.ERROR_INVALID_RANGE);
		}
		return styles[tkType];
	}
	
	public void setStyle(int tkType, Style style) {
		if (tkType<0 || tkType>=styles.length) {
			throw new SWTError(SWT.ERROR_INVALID_RANGE);
		}
		if (style == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		styles[tkType] = style;
	}
	
	public void setDefaultStyle(Style style) {
		if (style == null) {
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		}
		defaultStyle = style;
	}
	
	public Style getDefaultStyle() {
		return defaultStyle;
	}
	
	public int size() {
		return styles.length;
	}
}
