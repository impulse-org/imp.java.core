/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.tokenColorer;

import lpg.runtime.IToken;

import org.eclipse.imp.java.parser.JavaParsersym;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.imp.services.base.TokenColorerBase;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class JavaTokenColorer extends TokenColorerBase implements JavaParsersym, ITokenColorer {
    TextAttribute commentAttribute, keywordAttribute, stringAttribute, numberAttribute, identifierAttribute;

    public TextAttribute getColoring(IParseController controller, Object o) {
        IToken token= (IToken) o;
	switch (token.getKind()) {
	case TK_SlComment:
	case TK_MlComment:
	    return commentAttribute;
	case TK_IDENTIFIER:
	    return identifierAttribute;
	case TK_INTEGER_LITERAL:
	case TK_LONG_LITERAL:
	    return numberAttribute;
	case TK_STRING_LITERAL:
	    return stringAttribute;
	default:
	    if (((SimpleLPGParseController) controller).isKeyword(token.getKind()))
		return keywordAttribute;
	    else
		return null;
	}
    }

    public JavaTokenColorer() {
	super();
	Display display= Display.getDefault();
	commentAttribute= new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_RED), null, SWT.ITALIC);
	stringAttribute= new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_BLUE), null, SWT.BOLD);
	identifierAttribute= new TextAttribute(display.getSystemColor(SWT.COLOR_BLACK), null, SWT.NORMAL);
	numberAttribute= new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_YELLOW), null, SWT.BOLD);
	keywordAttribute= new TextAttribute(display.getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD);
    }

    public void setLanguage(String language) {}

    public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
        return seed;
    }
}
