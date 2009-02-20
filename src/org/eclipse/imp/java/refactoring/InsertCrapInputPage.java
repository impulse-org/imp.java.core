/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.refactoring;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class InsertCrapInputPage extends UserInputWizardPage {
    public InsertCrapInputPage(String name) {
	super(name);
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
	Composite result= new Composite(parent, SWT.NONE);
	setControl(result);
	GridLayout layout= new GridLayout();
	layout.numColumns= 1;
	result.setLayout(layout);

	final Button deleteButton= new Button(result, SWT.CHECK);

	deleteButton.setText("Delete declarations after inlining");
	deleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	deleteButton.addSelectionListener(new SelectionListener() {
	    public void widgetSelected(SelectionEvent e) {
		// Set a parameter on the refactoring, e.g. getInsertCrapRefactoring().setDoDelete(deleteButton.getSelection());
	    }
	    public void widgetDefaultSelected(SelectionEvent e) { }
	});
    }

    private InsertCrapRefactoring getInsertCrapRefactoring() {
	return (InsertCrapRefactoring) getRefactoring();
    }
}
