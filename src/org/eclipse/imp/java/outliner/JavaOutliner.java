/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.outliner;

import org.eclipse.imp.services.base.OutlinerBase;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.swt.graphics.Image;

import polyglot.ast.ClassDecl;
import polyglot.ast.ConstructorDecl;
import polyglot.ast.FieldDecl;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.visit.NodeVisitor;

public class JavaOutliner extends OutlinerBase {
    public static Image _DESC_ELCL_VIEW_MENU = JavaPluginImages.DESC_ELCL_VIEW_MENU.createImage();

    public static Image _DESC_FIELD_DEFAULT = JavaPluginImages.DESC_FIELD_DEFAULT.createImage();
    public static Image _DESC_FIELD_PRIVATE = JavaPluginImages.DESC_FIELD_PRIVATE.createImage();
    public static Image _DESC_FIELD_PROTECTED = JavaPluginImages.DESC_FIELD_PROTECTED.createImage();
    public static Image _DESC_FIELD_PUBLIC = JavaPluginImages.DESC_FIELD_PUBLIC.createImage();

    public static Image[] FIELD_DESCS= {
	_DESC_FIELD_DEFAULT, _DESC_FIELD_PRIVATE, _DESC_FIELD_PROTECTED, _DESC_FIELD_PUBLIC
    };

    public static Image _DESC_MISC_DEFAULT = JavaPluginImages.DESC_MISC_DEFAULT.createImage();
    public static Image _DESC_MISC_PRIVATE = JavaPluginImages.DESC_MISC_PRIVATE.createImage();
    public static Image _DESC_MISC_PROTECTED = JavaPluginImages.DESC_MISC_PROTECTED.createImage();
    public static Image _DESC_MISC_PUBLIC = JavaPluginImages.DESC_MISC_PUBLIC.createImage();

    public static Image[] MISC_DESCS= {
	_DESC_MISC_DEFAULT, _DESC_MISC_PRIVATE, _DESC_MISC_PROTECTED, _DESC_MISC_PUBLIC
    };

    public static Image _DESC_OBJS_CFILECLASS = JavaPluginImages.DESC_OBJS_CFILECLASS.createImage();
    public static Image _DESC_OBJS_CFILEINT = JavaPluginImages.DESC_OBJS_CFILEINT.createImage();

    public static Image _DESC_OBJS_INNER_CLASS_DEFAULT = JavaPluginImages.DESC_OBJS_INNER_CLASS_DEFAULT.createImage();
    public static Image _DESC_OBJS_INNER_CLASS_PRIVATE = JavaPluginImages.DESC_OBJS_INNER_CLASS_PRIVATE.createImage();
    public static Image _DESC_OBJS_INNER_CLASS_PROTECTED = JavaPluginImages.DESC_OBJS_INNER_CLASS_PROTECTED.createImage();
    public static Image _DESC_OBJS_INNER_CLASS_PUBLIC = JavaPluginImages.DESC_OBJS_INNER_CLASS_PUBLIC.createImage();

    public static Image[] INNER_CLASS_DESCS= {
	_DESC_OBJS_INNER_CLASS_DEFAULT, _DESC_OBJS_INNER_CLASS_PRIVATE, _DESC_OBJS_INNER_CLASS_PROTECTED, _DESC_OBJS_INNER_CLASS_PUBLIC
    };

    public static Image _DESC_OBJS_INNER_INTERFACE_DEFAULT = JavaPluginImages.DESC_OBJS_INNER_INTERFACE_DEFAULT.createImage();
    public static Image _DESC_OBJS_INNER_INTERFACE_PRIVATE = JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PRIVATE.createImage();
    public static Image _DESC_OBJS_INNER_INTERFACE_PROTECTED = JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PROTECTED.createImage();
    public static Image _DESC_OBJS_INNER_INTERFACE_PUBLIC = JavaPluginImages.DESC_OBJS_INNER_INTERFACE_PUBLIC.createImage();

    public static Image[] INNER_INTF_DESCS= {
	_DESC_OBJS_INNER_INTERFACE_DEFAULT, _DESC_OBJS_INNER_INTERFACE_PRIVATE, _DESC_OBJS_INNER_INTERFACE_PROTECTED, _DESC_OBJS_INNER_INTERFACE_PUBLIC
    };

    public static Image _DESC_OBJS_PACKDECL = JavaPluginImages.DESC_OBJS_PACKDECL.createImage();

    protected String filter(String name) {
        return name.replaceAll("\n", "").replaceAll("\\{amb\\}", "");
    }

    protected class OutlineVisitor extends NodeVisitor {
	public NodeVisitor enter(Node n) {
	    if (n instanceof MethodDecl) {
	    	pushSubItem(((MethodDecl) n).name() + "()", n);
	    } else if (n instanceof ConstructorDecl) {
	    	// SMS 20 Apr 2007:  push, don't add (especially if you want to pop)
	    	/*addSubItem*/ pushSubItem(((ConstructorDecl) n).name() + "()", n);
	    } else if (n instanceof FieldDecl) {
	    	addSubItem(((FieldDecl) n).name(), n);
	    } else if (n instanceof ClassDecl) {
	    	pushTopItem(((ClassDecl) n).name(), n);
	    }
	    return this;
	}
	public Node leave(Node old, Node n, NodeVisitor v) {
	    if (n instanceof ClassDecl || n instanceof MethodDecl || n instanceof ConstructorDecl) {
		popSubItem();
	    } // SMS 20 Apr 2007
	    return n;
	}
    }

    protected void sendVisitorToAST(Object node) {
    	Node root= (Node) node;
    	root.visit(new OutlineVisitor());
    }
}
