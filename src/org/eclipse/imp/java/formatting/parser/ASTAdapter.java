package org.eclipse.imp.java.formatting.parser;

import lpg.runtime.IAst;

import org.eclipse.imp.java.formatting.parser.JavaParser.AbstractAstList;
import org.eclipse.imp.services.base.LPGASTMatchAdapterBase;

public class ASTAdapter extends LPGASTMatchAdapterBase implements JavaParsersym {
    public ASTAdapter() {
        super(AbstractAstList.class);
    }

	public boolean isMetaVariable(Object astNode) {
		IAst ast = (IAst) astNode;
		if (ast.getChildren().size() == 0) {
			String tokStr = ast.getLeftIToken().toString();

			if (tokStr.startsWith("${") && tokStr.endsWith("}")) {
				return true;
			}
//			switch (k) {
//			case TK_VAR_Statement:
//			case TK_VAR_Expression:
//			case TK_VAR_ArgumentList:
//			case TK_VAR_ArgumentListopt:
//			case TK_VAR_TypeDeclaration:
//			case TK_VAR_BlockStatements:
//			case TK_VAR_PackageName:
//			case TK_VAR_TypeName:
//			case TK_VAR_MethodName:
//			case TK_VAR_ClassBodyDeclaration:
//			case TK_VAR_ImportDeclaration:
//			case TK_VAR_ImportDeclarations:
//			case TK_VAR_IDENTIFIER:
//				return true;
//			default:
//				return false;
//			}
		}
		return false;
	}
}
