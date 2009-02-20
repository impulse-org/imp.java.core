package org.eclipse.imp.java.formatting.parser;

import java.util.Map;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

import org.eclipse.imp.java.formatting.parser.JavaParser.AbstractAstList;
import org.eclipse.imp.services.IASTAdapter;

public class ASTAdapter implements IASTAdapter, JavaParsersym {
	public boolean isList(Object astNode) {
		return astNode instanceof AbstractAstList;
	}

	public Object[] getChildren(Object astNode) {
		return ((IAst) astNode).getChildren().toArray();
	}

	public int getOffset(Object astNode) {
		return ((IAst) astNode).getLeftIToken().getStartOffset();
	}

	public int getLength(Object astNode) {
		IAst ast = (IAst) astNode;
		IToken left = ast.getLeftIToken();
		IToken right = ast.getRightIToken();

		// special case for epsilon trees
		if (left.getTokenIndex() > right.getTokenIndex()) {
			return 0;
		} else {
			int start = left.getStartOffset();
			int end = right.getEndOffset();
			return end - start + 1;
		}
	}

	public String getTypeOf(Object astNode) {
		return astNode.getClass().getName();
	}

	public boolean isMetaVariable(Object astNode) {
		IAst ast = (IAst) astNode;
		if (ast.getChildren().size() == 0) {
			int k = ast.getLeftIToken().getKind();
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

	public Object construct(String qualName, Object[] children) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object construct(String qualName, Object[] children, Map attribs) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getChildAtPosition(int pos, Object astNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getChildRoleAtPosition(int pos, String qualNodeType) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFile(Object astNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMetaVariableName(Object astNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPositionOfChildRole(String roleName, String qualNodeType) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getValue(String attributeName, Object astNode) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isInstanceOfType(Object astNode, String typeName) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSubTypeOf(String maybeSuper, String maybeSub) {
		// TODO Auto-generated method stub
		return false;
	}

	public String lookupSimpleNodeType(String simpleName) {
		// TODO Auto-generated method stub
		return null;
	}
}
