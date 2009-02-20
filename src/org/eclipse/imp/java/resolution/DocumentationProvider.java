/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Mar 18, 2007
 */
package org.eclipse.imp.java.resolution;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IDocumentationProvider;

public class DocumentationProvider implements ILanguageService, IDocumentationProvider {
    public String getDocumentation(Object target, IParseController parseController) {
	return target.toString();
    }
}
