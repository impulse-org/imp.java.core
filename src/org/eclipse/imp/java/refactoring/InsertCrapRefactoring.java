/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.imp.analysis.search.PolyglotSourceFinder;
import org.eclipse.imp.analysis.type.constraints.BasicPolyglotConstraintCreator;
import org.eclipse.imp.analysis.type.constraints.ITypeVariableFactory;
import org.eclipse.imp.analysis.type.constraints.PolyglotConstraintCreator;
import org.eclipse.imp.analysis.type.constraints.TypeConstraintCollector;
import org.eclipse.imp.analysis.type.constraints.TypeConstraintFactory;
import org.eclipse.imp.analysis.type.constraints.bindings.BindingKeyFactory;
import org.eclipse.imp.analysis.type.constraints.bindings.PolyglotBindingKeyFactory;
import org.eclipse.imp.analysis.type.constraints.variables.TypeVariableFactory;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import polyglot.ast.Node;

public class InsertCrapRefactoring extends Refactoring {
    private final IFile fSourceFile;
    private final Node fNode;

    public InsertCrapRefactoring(UniversalEditor editor) {
	super();

	IEditorInput input= editor.getEditorInput();

	if (input instanceof IFileEditorInput) {
	    IFileEditorInput fileInput= (IFileEditorInput) input;

	    fSourceFile= fileInput.getFile();
	    fNode= findNode(editor);
	} else {
	    fSourceFile= null;
	    fNode= null;
	}
    }

    private Node findNode(UniversalEditor editor) {
	Point sel= editor.getSelection();
	IParseController parseController= editor.getParseController();
	Node root= (Node) parseController.getCurrentAst();
	ISourcePositionLocator locator= parseController.getNodeLocator();

	return (Node) locator.findNode(root, sel.x);
    }

    public String getName() {
	return "Insert Crap";
    }

    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
	// Check parameters retrieved from editor context
	return new RefactoringStatus();
    }

    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        TypeConstraintFactory constraintFactory= new TypeConstraintFactory();
        BindingKeyFactory keyFactory= new PolyglotBindingKeyFactory();
        ITypeVariableFactory variableFactory= new TypeVariableFactory(keyFactory);
        PolyglotConstraintCreator creator= new BasicPolyglotConstraintCreator(constraintFactory, variableFactory);
        TypeConstraintCollector collector= new TypeConstraintCollector(creator);

        try {
            ISourceProject srcProject= ModelFactory.open(fSourceFile.getProject());

            fSourceFile.getProject().accept(new PolyglotSourceFinder(new TextFileDocumentProvider(), srcProject, collector, collector.getVisitor(), LanguageRegistry.findLanguage("java")));
        } catch (ModelException e) {
            e.printStackTrace();
        }
	return new RefactoringStatus();
    }

    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
	TextFileChange tfc= new TextFileChange("Insert Crap", fSourceFile);

	tfc.setEdit(new MultiTextEdit());

	int startOffset= 0;
	int endOffset= 5;

	// START HERE
	tfc.addEdit(new ReplaceEdit(startOffset, endOffset - startOffset + 1, "Boo!"));

	return tfc;
    }
}
