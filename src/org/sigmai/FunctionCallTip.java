package org.sigmai;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolTip;

class FunctionCallTip {
	
	private ToolTip toolTip;
	
	FunctionCallTip() {
		toolTip = new ToolTip(Display.getCurrent().getShells()[0], SWT.NONE);
		toolTip.setAutoHide(false);
	}
	
	void showTip(String text, int x, int y) {
		toolTip.setLocation(x, y);
		toolTip.setMessage(text);
		toolTip.setVisible(true);
	}
	
	void hideTip() {
		if (toolTip.isVisible())
			toolTip.setVisible(false);
	}
	
	void dispose() {
		toolTip.dispose();
	}
}
