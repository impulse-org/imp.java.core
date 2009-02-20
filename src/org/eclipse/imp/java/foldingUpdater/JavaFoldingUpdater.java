/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.foldingUpdater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lpg.runtime.Adjunct;
import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.imp.java.parser.JavaParseController;
import org.eclipse.imp.java.parser.JavaParser.JPGPosition;
import org.eclipse.imp.services.base.FolderBase;

import polyglot.ast.Block;
import polyglot.ast.Catch;
import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.If;
import polyglot.ast.Initializer;
import polyglot.ast.Loop;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.SourceFile;
import polyglot.ast.Stmt;
import polyglot.ast.Switch;
import polyglot.ast.SwitchBlock;
import polyglot.ast.Try;
import polyglot.visit.NodeVisitor;

public class JavaFoldingUpdater extends FolderBase {
    protected IPrsStream prsStream;
    protected ILexStream lexStream;

    //
    // Use this version of makeAnnotation when you have a range of 
    // tokens to fold.
    //
    public void makeAnnotation(IToken first_token, IToken last_token) {
        if (last_token.getEndLine() > first_token.getLine()) {
            IToken next_token = prsStream.getIToken(prsStream.getNext(last_token.getTokenIndex()));
            IToken[] adjuncts = next_token.getPrecedingAdjuncts();
            IToken gate_token = adjuncts.length == 0 ? next_token : adjuncts[0];
            makeAnnotation(first_token.getStartOffset(),
                           gate_token.getLine() > last_token.getEndLine()
                                                ? lexStream.getLineOffset(gate_token.getLine() - 1)
                                                : last_token.getEndOffset());
        }
    }

    public class FoldingVisitor extends NodeVisitor {
	public NodeVisitor enter(Node n) {
            if (n instanceof ClassDecl || n instanceof Initializer || n instanceof ConstructorDecl ||
        	n instanceof MethodDecl || n instanceof Switch || n instanceof SwitchBlock ||
        	n instanceof Loop) { // includes For, ForLoop, While, do
        	makeAnnotation(n);
            } else if (n instanceof If) {
                Stmt true_part = ((If) n).consequent();
                Stmt else_part = ((If) n).alternative();
                IToken last_true_part_token = ((JPGPosition) true_part.position()).getRightIToken();
                makeAnnotation(((JPGPosition) n.position()).getLeftIToken(), last_true_part_token);
                if (else_part != null && (! (else_part instanceof If))) {
                    makeAnnotation(prsStream.getIToken(prsStream.getNext(last_true_part_token.getTokenIndex())),
                                   ((JPGPosition) else_part.position()).getRightIToken());
                }
            } else if (n instanceof Try) {
                Block try_block = ((Try) n).tryBlock();
                List catch_blocks = ((Try) n).catchBlocks();
                Block finally_block = ((Try) n).finallyBlock();

                IToken last_token = ((JPGPosition) try_block.position()).getRightIToken();
                makeAnnotation(((JPGPosition) n.position()).getLeftIToken(), last_token);
                for (int i = 0; i < catch_blocks.size(); i++) {
                    Catch catch_block = (Catch) catch_blocks.get(i);
                    IToken first_token = prsStream.getIToken(prsStream.getNext(last_token.getTokenIndex()));
                    last_token = ((JPGPosition) catch_block.position()).getRightIToken();
                    makeAnnotation(first_token, last_token);
                }

                if (finally_block != null) {
                    IToken first_token = prsStream.getIToken(prsStream.getNext(last_token.getTokenIndex()));
                    last_token = ((JPGPosition) finally_block.position()).getRightIToken();
                    makeAnnotation(first_token, last_token);
                }
            }
	    return this;
	}
    }

    protected NodeVisitor getVisitor() {
	return new FoldingVisitor();
    }

    public void sendVisitorToAST(HashMap newAnnotations, List annotations, Object ast) {
        prsStream = ((JavaParseController) parseController).getParser().getParseStream();
        lexStream = prsStream.getLexStream();

        Node theAST= (Node) ast;
	NodeVisitor abstractVisitor= getVisitor();

	theAST.visit(abstractVisitor);
        foldImports(ast);
        foldMultilineComments();
    }

    private void foldMultilineComments() {
	//
        // Fold comments that span multiple lines.
        //
        ArrayList adjuncts = prsStream.getAdjuncts();
        for (int i = 0; i < adjuncts.size(); ) {
            Adjunct adjunct = (Adjunct) adjuncts.get(i);

            IToken previous_token = prsStream.getIToken(adjunct.getTokenIndex()),
                   next_token = prsStream.getIToken(prsStream.getNext(previous_token.getTokenIndex())),
                   comments[] = previous_token.getFollowingAdjuncts();

            for (int k = 0; k < comments.length; k++) {
                Adjunct comment = (Adjunct) comments[k];
                if (comment.getEndLine() > comment.getLine()) {
                    IToken gate_token = k + 1 < comments.length ? comments[k + 1] : next_token;
                    makeAnnotation(comment.getStartOffset(),
                                   gate_token.getLine() > comment.getEndLine()
                                       ? lexStream.getLineOffset(gate_token.getLine() - 1)
                                       : comment.getEndOffset());
                }
            }
            i += comments.length;
        }
    }

    private void foldImports(Object ast) {
	//            
        // If the program contains more than one import declaration
        // that span multiple lines, fold them.
        //            
        List imports = ((SourceFile) ast).imports();
        if (imports.size() > 0) {
            Node first_import= (Node) imports.get(0),
                 last_import= (Node) imports.get(imports.size() - 1);

            final int offset= first_import.position().offset();
	    makeAnnotation(offset, last_import.position().endOffset() - offset + 3);
        }
    }
}
