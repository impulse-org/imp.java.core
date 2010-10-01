package org.eclipse.imp.java.parser;

import org.eclipse.imp.services.base.LanguageSyntaxPropertiesBase;

public class JavaSyntaxProperties extends LanguageSyntaxPropertiesBase {
    public String getBlockCommentStart() {
        return "/*";
    }

    public String getBlockCommentEnd() {
        return "*/";
    }

    public String getSingleLineCommentPrefix() {
        return "//";
    }

    public String getBlockCommentContinuation() {
        return "*";
    }
}
