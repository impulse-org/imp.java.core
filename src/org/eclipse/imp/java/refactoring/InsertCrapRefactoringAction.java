/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.refactoring;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.refactoring.RefactoringStarter;
import org.eclipse.ui.texteditor.TextEditorAction;

public class InsertCrapRefactoringAction extends TextEditorAction {
//    private final UniversalEditor fEditor;

    public InsertCrapRefactoringAction(UniversalEditor editor) {
	super(RefactoringMessages.ResBundle, "InsertCrap.", editor);
//	fEditor= editor;
    }

    public void run() {
	final InsertCrapRefactoring refactoring= new InsertCrapRefactoring((UniversalEditor) this.getTextEditor());

	if (refactoring != null)
		new RefactoringStarter().activate(refactoring, new InsertCrapWizard(refactoring, "Insert Crap"), this.getTextEditor().getSite().getShell(), "Insert Crap", false);
    }
}
