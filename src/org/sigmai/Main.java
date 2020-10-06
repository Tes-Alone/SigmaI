package org.sigmai;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.sigmai.lexer.CPPLexer;

public class Main {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.CENTER);
		shell.setLayout(new FillLayout());

		var editor = new SigmaI(shell, SigmaI.LINE_MARGIN | SigmaI.FOLD_MARGIN | SigmaI.WIN7);
		var lexer  = new CPPLexer();
		lexer.setKeyWord1(new String[]{"int", "short", "long"});
		//StylePalette.getInstance().setColor(CPPLexer.TK_KEYWORD1, new Color(display, 0xff, 0xff, 00));
		editor.setLexer(lexer);
		editor.setFont(new Font(display, "Courier New", 18, SWT.NORMAL));
		editor.getStyledText().setFocus();
		editor.setLineMarginBackground(display.getSystemColor(SWT.COLOR_BLUE));
		editor.setLineMarginForeground(display.getSystemColor(SWT.COLOR_WHITE));
		editor.setMatchBraceFlag(SigmaI.PAIR_BIG|SigmaI.PAIR_BRAC);
		editor.setHighLightWord(true);
		shell.setSize(1400, 768);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
