/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.resolution;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IReferenceResolver;

import polyglot.ast.Ambiguous;
import polyglot.ast.Call;
import polyglot.ast.Field;
import polyglot.ast.Local;
import polyglot.ast.LocalDecl;
import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.FieldInstance;
import polyglot.types.LocalInstance;
import polyglot.types.MethodInstance;
import polyglot.visit.NodeVisitor;

public class JavaReferenceResolver implements IReferenceResolver, ILanguageService {
    public JavaReferenceResolver() {}

    /**
     * Get the target for a given source node in the AST represented by a
     * given Parse Controller.
     */
    public Object getLinkTarget(Object node, IParseController parseController) {
	if (node instanceof Ambiguous) {
	    return null;
	}
	if (node instanceof TypeNode) {
	    TypeNode typeNode= (TypeNode) node;
	    return typeNode.type();
	} else if (node instanceof Call) {
	    Call call= (Call) node;
	    MethodInstance mi= call.methodInstance();
	    if (mi != null)
		return mi.declaration();
	} else if (node instanceof Field) {
	    Field field= (Field) node;
	    FieldInstance fi= field.fieldInstance();

	    if (fi != null)
		return fi.declaration();
	} else if (node instanceof Local) {
	    Local local= (Local) node;
	    LocalInstance li= local.localInstance();
	    
	    if (li != null)
		return li.declaration();
	}
	return null;
    }

    /**
     * Get the text associated with a given node for use in a link
     * from (or to) that node
     */
    public String getLinkText(Object node) {
	return node.toString();
    }

    public Node findVarDefinition(Local local, Node ast) {
	final LocalInstance li= local.localInstance();
	final LocalDecl ld[]= new LocalDecl[1];
	NodeVisitor finder= new NodeVisitor() {
	    public NodeVisitor enter(Node n) {
		if (n instanceof LocalDecl) {
		    LocalDecl thisLD= (LocalDecl) n;
		    if (thisLD.localInstance() == li)
			ld[0]= thisLD;
		}
		return super.enter(n);
	    }
	};
	ast.visit(finder);
	return ld[0];
    }
}
