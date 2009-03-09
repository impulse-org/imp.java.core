/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.java.matching;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.imp.java.parser.JavaParser.JPGPosition;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.xform.pattern.matching.MatchResult;
import org.eclipse.imp.xform.pattern.matching.Matcher;
import org.eclipse.imp.xform.pattern.parser.ASTAdapterBase;

import polyglot.ast.AmbExpr;
import polyglot.ast.ClassDecl;
import polyglot.ast.Expr;
import polyglot.ast.Field;
import polyglot.ast.FieldDecl;
import polyglot.ast.Local;
import polyglot.ast.MethodDecl;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ast.NodeFactory_c;
import polyglot.ast.ProcedureDecl;
import polyglot.ast.TypeNode;
import polyglot.types.ArrayType;
import polyglot.types.ClassType;
import polyglot.types.SemanticException;
import polyglot.types.Type;
import polyglot.types.TypeSystem;
import polyglot.types.TypeSystem_c;
import polyglot.visit.HaltingVisitor;
import polyglot.visit.NodeVisitor;

public class PolyglotASTAdapter extends ASTAdapterBase implements ILanguageService {
    private final TypeSystem fTypeSystem;
    private final NodeFactory fNodeFactory;

    public PolyglotASTAdapter() {
	this(new TypeSystem_c(), new NodeFactory_c());
    }

    public PolyglotASTAdapter(TypeSystem typeSystem, NodeFactory nf) {
        fTypeSystem= typeSystem;
        fNodeFactory= nf;
    }

    protected Object getTargetType(Object astNode) {
	Node node= (Node) astNode;
	if (node instanceof Expr) {
	    Expr expr= (Expr) node;
	
	    return typeNameOf(expr.type());
	} else if (node instanceof Local) {
	    return typeNameOf(((Local) node).type());
	} else if (node instanceof FieldDecl) {
	    return typeNameOf(((FieldDecl) node).type().type());
	} else if (node instanceof MethodDecl) {
	    return typeNameOf(((MethodDecl) node).returnType().type());
	}
	return "<unknown>";
    }

    protected String getName(Object astNode) {
	Node node= (Node) astNode;
	if (node instanceof ClassDecl)
	    return ((ClassDecl) node).name();
	if (node instanceof FieldDecl)
	    return ((FieldDecl) node).name();
	if (node instanceof ProcedureDecl)
	    return ((ProcedureDecl) node).name();
	if (node instanceof Field)
	    return ((Field) node).name();
	if (node instanceof Local)
	    return ((Local) node).name();
	if (node instanceof TypeNode)
	    return typeNameOf(((TypeNode) node).type());
	if (node instanceof AmbExpr)
	    return ((AmbExpr) node).name();
	// DON'T throw an exception - it is reasonable for a pattern to constrain
	// via an attribute that not all concrete nodes have. E.g.:
	//    [Expr e { name = 'x' }]
	return null;
    }

    public Object getTypeByName(String typeName) {
        try {
            return fTypeSystem.typeForName(typeName);
        } catch (SemanticException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String typeNameOf(Type type) {
        // Would be better to turn the type name in the pattern to a Polyglot Type
        // object *ONCE*, store it in the pattern, and then compare that to the types
        // in the various concrete AST nodes as matching proceeds...
        if (type == null) return "<unknown>";
        if (type.isPrimitive()) {
            if (type.isBoolean()) return "boolean";
            if (type.isByte()) return "byte";
            if (type.isChar()) return "char";
            if (type.isDouble()) return "double";
            if (type.isFloat()) return "float";
            if (type.isInt()) return "int";
            if (type.isLong()) return "long";
            if (type.isShort()) return "short";
            if (type.isVoid()) return "void";
            return "<unknown>";
        } else if (type.isArray()) {
            ArrayType arrayType= (ArrayType) type;
            return typeNameOf(arrayType.base()) + "[]";
        } else if (type.isClass()) {
            ClassType classType= (ClassType) type;
            return classType.fullName();
        } else
            return "<unknown>";
    }

    public Object[] getChildren(Object astNode) {
	Node node= (Node) astNode;
	final List children= new ArrayList();
	final int level[]= new int[1];

	level[0]= 0;
	NodeVisitor v= new NodeVisitor() {
	    public NodeVisitor enter(Node n) {
		if (level[0] == 1)
		    children.add(n);
		// Would be nice to truncate traversal, but not easy with Polyglot visitor API...
		level[0]++;
	        return super.enter(n);
	    }
	    public Node leave(Node old, Node n, NodeVisitor v) {
		level[0]--;
	        return super.leave(old, n, v);
	    }
	};
	
	node.visit(v);
        return children.toArray();
    }

    public boolean isInstanceOfType(Object astNode, String typeName) {
        try {
            return Class.forName("polyglot.ast." + typeName).isInstance(astNode);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MatchResult findNextMatch(final Matcher matcher, Object astRoot, final int matchStartPos) {
        final MatchResult[] result= new MatchResult[1];
        Node root= (Node) astRoot;

        root.visit(new HaltingVisitor() {
            /* (non-Javadoc)
             * @see polyglot.visit.NodeVisitor#enter(polyglot.ast.Node)
             */
            public NodeVisitor enter(Node n) {
                if (result[0] != null)
                    bypass(n);
                else {
                    JPGPosition pos= (JPGPosition) n.position();

                    // Hmmm... some nodes (like Call_c) occasionally have a null position()...
                    if (pos == null || matchStartPos < pos.getLeftIToken().getOffset()) {
                	MatchResult m= matcher.match(n);

                	if (m != null) {
                	    result[0]= m;
                	    bypass(n);
                	}
                    }
                }
                return this;
            }
        });
        return result[0];
    }

    public Set/*<MatchContext>*/ findAllMatches(final Matcher matcher, Object astRoot) {
        final Set/*<MatchContext>*/ result= new HashSet();

        Node root= (Node) astRoot;

        root.visit(new NodeVisitor() {
            /* (non-Javadoc)
             * @see polyglot.visit.NodeVisitor#enter(polyglot.ast.Node)
             */
            public NodeVisitor enter(Node n) {
                MatchResult m= matcher.match(n);

                if (m != null)
                    result.add(m);
                return this;
            }
        });
        return result;
    }


    public String getFile(Object astNode) {
	Node node= (Node) astNode;

	return node.position().file();
    }

    public int getOffset(Object astNode) {
	Node node= (Node) astNode;

	return ((JPGPosition) node.position()).getLeftIToken().getOffset();
    }

    public int getLength(Object astNode) {
	Node node= (Node) astNode;

	return ((JPGPosition) node.position()).getRightIToken().getEndOffset() - ((JPGPosition) node.position()).getLeftIToken().getOffset() + 1;
    }

    public String lookupSimpleNodeType(String simpleName) {
	return "polyglot.ext.jl.ast." + simpleName + "_c";
    }

    public String getTypeOf(Object astNode) {
	Node node= (Node) astNode;

	return node.getClass().getCanonicalName();
    }

    static final String jlPkg= "polyglot.ext.jl.ast.";

    public Object construct(String qualName, Object[] children) throws IllegalArgumentException {
	if (!qualName.startsWith(jlPkg))
	    return null;

	qualName= qualName.substring(jlPkg.length());
	Class[] argTypes= new Class[children.length];

	for(int i= 0; i < argTypes.length; i++) {
	    argTypes[i]= children[i].getClass();
	}
	try {
	    Method m= fNodeFactory.getClass().getMethod(qualName, argTypes);

	    return m.invoke(fNodeFactory, children);
	} catch (SecurityException e) {
	    throw new IllegalArgumentException(e);
	} catch (NoSuchMethodException e) {
	    throw new IllegalArgumentException(e);
	} catch (IllegalAccessException e) {
	    throw new IllegalArgumentException(e);
	} catch (InvocationTargetException e) {
	    throw new IllegalArgumentException(e.getCause());
	}
    }

    public Object construct(String qualName, Object[] children, Map attribs) throws IllegalArgumentException {
	return construct(qualName, children);
    }
}
