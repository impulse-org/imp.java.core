package org.eclipse.imp.java.parser;

import org.eclipse.imp.services.ILanguageSyntaxProperties;

public class JavaSyntaxProperties implements ILanguageSyntaxProperties {

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

    public String[][] getFences() {
        // TODO Auto-generated method stub
        return null;
    }

    public int[] getIdentifierComponents(String ident) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getIdentifierConstituentChars() {
        // TODO Auto-generated method stub
        return null;
    }

}
