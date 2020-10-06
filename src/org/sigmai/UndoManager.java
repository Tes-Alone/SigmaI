package org.sigmai;

import java.util.Stack;

class UndoInfo {
	int pos;
	String newText;
	String replacedText;
	boolean isAdd;
	boolean isBoth;
	
	UndoInfo(int pos, String newText, String replacedText) {
		this.pos = pos;
		this.newText = newText;
		this.replacedText = replacedText;
		this.isAdd = this.newText!=null && this.replacedText==null;
		this.isBoth = this.newText!=null && this.replacedText!=null;
	}
	
	public String toString() {
		return "{" + pos + "," + newText + "," + replacedText + "," + isAdd + "," + isBoth + "}";
	}
}

interface UndoListener {
	void onUndo0(UndoInfo info);
	void onRedo0(UndoInfo info);
	
	void onUndo1(UndoInfo info);
	void onRedo1(UndoInfo info);
	
	void onUndo2(UndoInfo info);
	void onRedo2(UndoInfo info);
}

class UndoManager {

	private Stack<UndoInfo> undoStack;
	private Stack<UndoInfo> redoStack;
	
	private UndoListener undoListener;
	
	UndoManager() {
		undoStack = new Stack<>();
		redoStack = new Stack<>();
	}
	
	private int undoCount = 0;
	private int undoLimit = 1024;
	
	void setUndoListener(UndoListener listener) {
		this.undoListener = listener;
	}
	
	private boolean isUndoing;
	
	boolean canUndo() {
		return !undoStack.isEmpty();
	}
	
	void undo() {
		if (!undoStack.empty()) {
			isUndoing = true;
			UndoInfo info = undoStack.pop();
			if (info.isBoth) {
				undoListener.onUndo0(info);				
				redoStack.push(info);
			} else if (info.isAdd) {
				int len = info.newText.length();
				if (len != 1) {
					undoListener.onUndo1(info);
					redoStack.push(info);
				} else {
					boolean isAdjacent = true;
					while (len==1 && isAdjacent) {
						undoListener.onUndo1(info);
						redoStack.push(info);
						
						if (!undoStack.empty()) {
							info = undoStack.pop();
							if (info.isAdd) {
								len = info.newText.length();
								isAdjacent = redoStack.peek().pos==info.pos+1
												|| redoStack.peek().pos==info.pos;
							} else {
								break;
							}
						} else {
							break;
						}
					}
					
					if (len!=1 || !info.isAdd || !isAdjacent) {
						undoStack.push(info);
					}
				}
			} else {
				int len = info.replacedText.length();
				if (len != 1) {
					undoListener.onUndo2(info);
					redoStack.push(info);
				} else {
					boolean isAdjacent = true;
					while (len==1 && isAdjacent) {
						undoListener.onUndo2(info);
						redoStack.push(info);
						
						if (!undoStack.empty()) {
							info = undoStack.pop();
							if (!info.isAdd) {
								len = info.replacedText.length();
								isAdjacent = redoStack.peek().pos==info.pos-1
										|| redoStack.peek().pos==info.pos;
							} else {
								break;
							}
						} else {
							break;
						}
					}
					
					if (len!=1 || info.isAdd || !isAdjacent) {
						undoStack.push(info);
					}
				}
			}
			
			isUndoing = false;
			undoed = true;
		}
		//System.out.println("undo:" + undoStack);
		//System.out.println("redo:" + redoStack);
	}
	
	void redo() {
		if (!redoStack.empty()) {
			isRedoing = true;
			UndoInfo info = redoStack.pop();
			if (info.isBoth) {
				undoListener.onRedo0(info);				
				undoStack.push(info);
			} else if (info.isAdd) {
				int len = info.newText.length();
				if (len != 1) {
					undoListener.onRedo1(info);
					undoStack.push(info);
				} else {
					boolean isAdjacent = true;
					while (len==1 && isAdjacent) {
						undoListener.onRedo1(info);
						undoStack.push(info);
						
						if (!redoStack.empty()) {
							info = redoStack.pop();
							if (info.isAdd) {
								len = info.newText.length();
								isAdjacent = undoStack.peek().pos==info.pos
												|| undoStack.peek().pos==info.pos-1;
							} else {
								break;
							}
						} else {
							break;
						}
					}
					
					if (len!=1 || !info.isAdd || !isAdjacent) {
						redoStack.push(info);
					}
				}
			} else {
				int len = info.replacedText.length();
				if (len != 1) {
					undoListener.onRedo2(info);
					undoStack.push(info);
				} else {
					boolean isAdjacent = true;
					while (len==1 && isAdjacent) {
						undoListener.onRedo2(info);
						undoStack.push(info);
						
						if (!redoStack.empty()) {
							info = redoStack.pop();
							if (!info.isAdd) {
								len = info.replacedText.length();
								isAdjacent = undoStack.peek().pos==info.pos+1
										|| undoStack.peek().pos==info.pos;
							} else {
								break;
							}
						} else {
							break;
						}
					}
					
					if (len!=1 || info.isAdd || !isAdjacent) {
						redoStack.push(info);
					}
				}
			}
			
			isRedoing = false;
		}
		//System.out.println("undo:" + undoStack);
		//System.out.println("redo:" + redoStack);
	}

	private boolean isRedoing;
	
	boolean canRedo() {
		return !redoStack.isEmpty();
	}
	
	void cleanUndoInfo() {
		undoStack.clear();
		redoStack.clear();
		undoCount = 0;
	}
	
	void setUndoLimit(int limit) {
		if (limit < 0) limit = 0;
		this.undoLimit = limit;
	}
	
	private boolean undoed;
	
	void addUndoInfo(int start, String replacedText,
								String newText) {
		if (!isUndoing && !isRedoing) {
			undoCount++;
			if (undoCount > undoLimit) {
				cleanUndoInfo();
			} else {
				/*
				 * 如果曾经执行过 undo 操作, 那么在下一次输入需要清空 redo 栈
				 **/ 
				
				if (undoed) {
					undoed = false;
					redoStack.clear();
				}
				
				if (replacedText!=null && !replacedText.isEmpty() 
						&& newText!=null && !newText.isEmpty()) {
					UndoInfo info = new UndoInfo(start, newText, replacedText);
					undoStack.push(info);
				} else if (replacedText!=null && !replacedText.isEmpty()){
					//System.out.println(replacedText);
					UndoInfo info = new UndoInfo(start, null, replacedText);
					undoStack.push(info);
				} else if (newText!=null && !newText.isEmpty()) {
					UndoInfo info = new UndoInfo(start, newText, null);
					undoStack.push(info);
				}
			}
		}
	}	
	
	boolean isUndoing() {
		return isUndoing;
	}
	
	boolean isRedoing() {
		return isRedoing;
	}
}
