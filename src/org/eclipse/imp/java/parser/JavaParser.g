%options variables=nt
%options fp=JavaParser
%options ast_type=Node
%options package=org.eclipse.imp.java.parser
%options template=dtParserTemplateF.gi
%options import_terminals=JavaLexer.gi

------------------------------------------------------------------------
--                         J A V A   1.4
--
-- This is the Java 1.4 grammar from the Polyglot distribution, tweaked
-- (by rfuhrer@watson.ibm.com and pcharles@us.ibm.com) to adapt the CUP
-- grammar to LPG format, and to interface with the LPG parser and lexer
-- drivers.
------------------------------------------------------------------------
%Terminals
    PLUSPLUS      ::= '++'
    MINUSMINUS    ::= '--'
    EQEQ          ::= '=='
    LTEQ          ::= '<='
    GTEQ          ::= '>='
    NOTEQ         ::= '!='
    LSHIFT        ::= '<<'
    RSHIFT        ::= '>>'
    URSHIFT       ::= '>>>'
    PLUSEQ        ::= '+='
    MINUSEQ       ::= '-='
    MULTEQ        ::= '*='
    DIVEQ         ::= '/='
    ANDEQ         ::= '&='
    OREQ          ::= '|='
    XOREQ         ::= '^='
    MODEQ         ::= '%='
    LSHIFTEQ      ::= '<<='
    RSHIFTEQ      ::= '>>='
    URSHIFTEQ     ::= '>>>='
    OROR          ::= '||'
    ANDAND        ::= '&&'

    PLUS      ::= '+'
    MINUS     ::= '-'
    NOT       ::= '!'
    MOD       ::= '%'
    XOR       ::= '^'
    AND       ::= '&'
    MULT      ::= '*'
    OR        ::= '|'
    COMP      ::= '~'
    DIV       ::= '/'
    GT        ::= '>'
    LT        ::= '<'
    LPAREN    ::= '('
    RPAREN    ::= ')'
    LBRACE    ::= '{'
    RBRACE    ::= '}'
    LBRACK    ::= '['
    RBRACK    ::= ']'
    SEMICOLON ::= ';'
    QUESTION  ::= '?'
    COLON     ::= ':'
    COMMA     ::= ','
    DOT       ::= '.'
    EQ        ::= '='
%End

%Globals
    /.import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.*;
import polyglot.util.*;
import polyglot.parse.*;
import polyglot.types.*;
import polyglot.ast.Assert;

import polyglot.parse.Name;
import polyglot.frontend.FileSource;
import polyglot.frontend.Parser;

import org.eclipse.imp.parser.IParser;
    ./
%End

%Define
    $ast_class /.Object./
    $additional_interfaces /., IParser, Parser./
%End

%Headers
    /.
        private ErrorQueue eq;
        private TypeSystem ts;
        private NodeFactory nf;
        private FileSource source;
        private boolean unrecoverableSyntaxError= false;

        public $action_type(ILexStream lexStream, TypeSystem t, NodeFactory n, FileSource source, ErrorQueue q) {
            this(lexStream);
            this.ts= (TypeSystem) t;
            this.nf= (NodeFactory) n;
            this.source= source;
            this.eq= q;
        }

        // RMF 11/7/2005 - N.B. This class has to be serializable, since it shows up inside Type objects,
        // which Polyglot serializes to save processing when loading class files generated from source
        // by Polyglot itself.
        public static class JPGPosition extends Position {
            private static final long serialVersionUID= -1593187800129872262L;
            private final transient IToken leftIToken, rightIToken;

            public JPGPosition(String path, String filename, IToken leftToken, IToken rightToken) {
                super(path, filename,
                      leftToken.getLine(), leftToken.getColumn(),
                      rightToken.getEndLine(), rightToken.getEndColumn(),
                  leftToken.getStartOffset(), rightToken.getEndOffset());
                this.leftIToken= leftToken;
                this.rightIToken= rightToken;
            }

            public JPGPosition(JPGPosition left, JPGPosition right) {
                super(left.path(), left.file(),
                      left.getLeftIToken().getLine(), left.getLeftIToken().getColumn(),
                      right.getRightIToken().getEndLine(), right.getRightIToken().getEndColumn(),
                      left.getLeftIToken().getStartOffset(), right.getRightIToken().getEndOffset());
                this.leftIToken= left.getLeftIToken();
                this.rightIToken= right.getRightIToken();
            }

            public IToken getLeftIToken() { return leftIToken; }
            public IToken getRightIToken() { return rightIToken; }

            public String toText() {
                IPrsStream prsStream= leftIToken.getIPrsStream();
                return new String(prsStream.getInputChars(),
                                  leftIToken.getStartOffset(),
                                  rightIToken.getEndOffset() - leftIToken.getStartOffset() + 1);
            }
        }

        public JPGPosition pos() {
            return new JPGPosition("", prsStream.getFileName(),
                                   prsStream.getIToken(getLeftSpan()), prsStream.getIToken(getRightSpan()));
        }

        public JPGPosition pos(int i) {
            return new JPGPosition("", prsStream.getFileName(),
                                   prsStream.getIToken(i), prsStream.getIToken(i));
        }

        public JPGPosition pos(int i, int j) {
            return new JPGPosition("", prsStream.getFileName(),
                                   prsStream.getIToken(i), prsStream.getIToken(j));
        }

        public JPGPosition pos(Node n) {
            if (n == null) return null;
            return (JPGPosition) n.position();
        }

        public JPGPosition pos(IToken tok) {
            return new JPGPosition("", prsStream.getFileName(), tok, tok);
        }

        public JPGPosition pos(IToken left, IToken right) {
            return new JPGPosition("", prsStream.getFileName(), left, right);
        }

        /**
         * Return the source position of the Type.
         */
        public JPGPosition pos (Type n) {
            if (n == null) return null;
            return (JPGPosition) n.position();
        }

        /**
         * Return the source position of the first element in the list to the
         * last element in the list.
         */
        public JPGPosition pos (List l) {
            if (l == null || l.isEmpty ()) {
            	return null;
            }
            return pos(l.get(0), l.get(l.size()-1));
        }

        public JPGPosition pos (VarDeclarator n) {
            if (n == null) return null;
            return (JPGPosition) n.pos;
        }

        public JPGPosition pos(Object first, Object last){
            return pos(first, last, first);
        }

        public JPGPosition pos(Object first, Object last, Object noEndDefault){
            //System.out.println("first: "+first+" class: "+first.getClass()+" last: "+last+" class: "+last.getClass());
            JPGPosition fpos = posForObject(first);
            JPGPosition epos = posForObject(last);

            if (fpos != null && epos != null) {
                if (epos.endColumn() != Position.END_UNUSED) {
                    return new JPGPosition(fpos, epos);
                }        

                // the end line and column are not being used in this extension.
                // so return the default for that case.
                return posForObject(noEndDefault);
            }
            return null;
        }

        protected JPGPosition posForObject(Object o) {
            if (o instanceof Node) {
                return pos ((Node) o);
            } else if (o instanceof IToken) {
                return pos ((IToken) o);
            } else if (o instanceof Type) {
                return pos ((Type) o);
            } else if (o instanceof List) {
                return pos ((List) o);
            } else if (o instanceof VarDeclarator) {
                return pos ((VarDeclarator) o);
            } else {
                return null;
            }
        }

        public List variableDeclarators(TypeNode a, List b, Flags flags) {
            List l= new TypedList(new LinkedList(), LocalDecl.class, false);
            for(Iterator i= b.iterator(); i.hasNext(); ) {
                VarDeclarator d= (VarDeclarator) i.next();
                l.add(nf.LocalDecl(pos(d), flags, array(a, d.dims), d.name, d.init));
            }
            return l;
        }

        /**
         * Helper for exprToType.
         */
        protected QualifierNode prefixToQualifier(Prefix p) {
            if (p instanceof TypeNode)
                return typeToQualifier((TypeNode) p);

            if (p instanceof Expr)
                return exprToQualifier((Expr) p);

            if (p instanceof AmbReceiver) {
                AmbReceiver a= (AmbReceiver) p;

                if (a.prefix() != null)
                    return nf.AmbQualifierNode(pos(p), prefixToQualifier(a.prefix()), a.name());
                else
                    return nf.AmbQualifierNode(pos(p), a.name());
            }

            if (p instanceof AmbPrefix) {
                AmbPrefix a= (AmbPrefix) p;

                if (a.prefix() != null)
                    return nf.AmbQualifierNode(pos(p), prefixToQualifier(a.prefix()), a.name());
                else
                    return nf.AmbQualifierNode(pos(p), a.name());
            }
            die(pos(p));
            return null;
        }

        /**
         * Helper for exprToType.
         */
        protected QualifierNode typeToQualifier (TypeNode t) {
            if (t instanceof AmbTypeNode) {
                AmbTypeNode a= (AmbTypeNode) t;

                if (a.qualifier () != null)
      	            return nf.AmbQualifierNode (pos (t), a.qual (), a.name ());
                else
                    return nf.AmbQualifierNode (pos (t), a.name ());
            }
            die(pos(t));
            return null;
        }

        /**
         * Helper for exprToType.
         */
        protected QualifierNode exprToQualifier (Expr e) {
            if (e instanceof AmbExpr) {
                AmbExpr a= (AmbExpr) e;
                return nf.AmbQualifierNode(pos(e), a.name());
            }

            if (e instanceof Field) {
                Field f= (Field) e;
                Receiver r= f.target ();
                return nf.AmbQualifierNode(pos(e), prefixToQualifier(r), f.name());
            }
            die(pos(e));
            return null;
        }

        /**
         * Convert <code>e</code> into a type, yielding a <code>TypeNode</code>.
         * This is used by the cast_expression production.
         */
        public TypeNode exprToType(Expr e) {
            if (e instanceof AmbExpr) {
                AmbExpr a= (AmbExpr) e;
                return nf.AmbTypeNode(pos(e), a.name());
            }

            if (e instanceof Field) {
                Field f= (Field) e;
                Receiver r= f.target();
                return nf.AmbTypeNode(pos(e), prefixToQualifier(r), f.name());
            }

            die(pos(e));
            return null;
        }

        public void reportError(int errorCode, String msg, Position pos) {
            JPGPosition jpos= (JPGPosition) pos;
            eq.enqueue(ErrorInfo.SYNTAX_ERROR, msg + ": " + pos.toString(),
                       new JPGPosition("", prsStream.getFileName(),
                                       jpos.getLeftIToken(),
                                       jpos.getRightIToken()));
        }

        /**
         * Report a fatal error then abort parsing.
         */
        public void die(Position pos) {
            reportError(prsStream.ERROR_CODE, "Syntax error.", pos);
        }

        /**
         * Report a fatal error then abort parsing.
         */
        public void die() throws Exception {
            // done_parsing();
            throw new Exception();
        }

        public Node parse() {
            try {
                SourceFile sf= (SourceFile) parser();

                if ((!unrecoverableSyntaxError) && (sf != null))
                    return sf.source(source);

                eq.enqueue(ErrorInfo.SYNTAX_ERROR, "Unable to parse " + source.name() + ".");
            } catch (RuntimeException e) {
                // Let the Compiler catch and report it.
                throw e;
            } catch (Exception e) {
                // Used by cup to indicate a non-recoverable error.
                eq.enqueue(ErrorInfo.SYNTAX_ERROR, e.getMessage());
            }
            return null;
        }

        public TypeNode array(TypeNode n, int dims) {
          if (dims > 0) {
              if (n instanceof CanonicalTypeNode) {
                  Type t= ((CanonicalTypeNode) n).type ();
                  return nf.CanonicalTypeNode (pos (n), ts.arrayOf (t, dims));
              }
              return nf.ArrayTypeNode (pos (n), array (n, dims - 1));
          } else {
              return n;
          }
        }
     ./
%End

%EOL
    ;
%End

%Start
    goal
%End
   
%Notice
/.
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.
./
%End

%Rules
-- 19.2) The Syntactic Grammar
goal ::=
                    -- SourceFile
        compilation_unit$a
            /.$BeginJava if (eq.hasErrors()) setResult(null);
               else setResult(a); $EndJava ./

-- 19.3) Lexical Structure.
literal ::=
                    -- Lit
        INTEGER_LITERAL$a    
            /.$BeginJava
               String s= a.toString();
               if (s.charAt(0) == '0' && s.length() > 1 && (s.charAt(1) == 'x' || s.charAt(1) == 'X'))
                   setResult(nf.IntLit(pos(a), IntLit.INT, Integer.parseInt(s.substring(2), 16)));
               else if (s.charAt(0) == '0' && s.length() > 1)
                   setResult(nf.IntLit(pos(a), IntLit.INT, Integer.parseInt(s, 8)));
               else
                   setResult(nf.IntLit(pos(a), IntLit.INT, Integer.parseInt(s)));
               $EndJava ./ 
    |   LONG_LITERAL$a    
            /.$BeginJava setResult(nf.IntLit(pos(a), IntLit.LONG,
                     Long.parseLong(a.toString().substring(0, a.toString().length() - 1)))); $EndJava ./ 
    |   DOUBLE_LITERAL$a
            /.$BeginJava setResult(nf.FloatLit(pos(a), FloatLit.DOUBLE,
                                           Double.parseDouble(a.toString()))); $EndJava ./
    |   FLOAT_LITERAL$a
            /.$BeginJava setResult(nf.FloatLit(pos(a), FloatLit.FLOAT,
                                           Float.parseFloat(a.toString()))); $EndJava ./
    |   TRUE$a
            /.$BeginJava setResult(nf.BooleanLit(pos(a), true)); $EndJava ./
    |   FALSE$a
            /.$BeginJava setResult(nf.BooleanLit(pos(a), false)); $EndJava ./
    |   CHARACTER_LITERAL$a
            /.$BeginJava setResult(nf.CharLit(pos(a),
                      a.toString().charAt(0))); $EndJava ./
    |   STRING_LITERAL$a
            /.$BeginJava setResult(nf.StringLit(pos(a), a.toString())); $EndJava ./
    |   NULL$a
            /.$BeginJava setResult(nf.NullLit(pos(a))); $EndJava ./

boundary_literal ::=
                    -- Lit
        INTEGER_LITERAL_BD$a    
            /.$BeginJava setResult(nf.IntLit(pos(a), IntLit.INT,
                                     Integer.parseInt(a.toString()))); $EndJava ./ 
    |   LONG_LITERAL_BD$a    
            /.$BeginJava setResult(nf.IntLit(pos(a), IntLit.LONG,
                     Long.parseLong(a.toString()))); $EndJava ./ 



-- 19.4) Types, Values, and Variables
type ::=
                    -- TypeNode
        primitive_type$a
            /.$BeginJava setResult(a); $EndJava ./
    |   reference_type$a
            /.$BeginJava setResult(a); $EndJava ./

primitive_type ::=
                    -- TypeNode
        numeric_type$a
            /.$BeginJava setResult(a); $EndJava ./
    |   BOOLEAN$a
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a), ts.Boolean())); $EndJava ./

numeric_type ::=
                    -- TypeNode
        integral_type$a
            /.$BeginJava setResult(a); $EndJava ./
    |   floating_point_type$a
            /.$BeginJava setResult(a); $EndJava ./

integral_type ::=
                    -- TypeNode
        BYTE$a 
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a), ts.Byte())); $EndJava ./
    |   CHAR$a
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a), ts.Char())); $EndJava ./
    |   SHORT$a
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a), ts.Short())); $EndJava ./
    |   INT$a
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a), ts.Int())); $EndJava ./
    |   LONG$a
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a), ts.Long())); $EndJava ./

floating_point_type ::=
                    -- TypeNode
        FLOAT$a 
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a),
                       ts.Float())); $EndJava ./
    |   DOUBLE$a
            /.$BeginJava setResult(nf.CanonicalTypeNode(pos(a),
                       ts.Double())); $EndJava ./

reference_type ::=
                    -- TypeNode
        class_or_interface_type$a
            /.$BeginJava setResult(a); $EndJava ./
    |   array_type$a
            /.$BeginJava setResult(a); $EndJava ./

class_or_interface_type ::=
                    -- TypeNode
        name$a
            /.$BeginJava setResult(a.toType()); $EndJava ./

class_type ::=
                    -- TypeNode
        class_or_interface_type$a
            /.$BeginJava setResult(a); $EndJava ./

interface_type ::=
                    -- TypeNode
        class_or_interface_type$a
            /.$BeginJava setResult(a); $EndJava ./

array_type ::=
                    -- TypeNode
        primitive_type$a dims$b
            /.$BeginJava setResult(array(a, b.intValue())); $EndJava ./
    |   name$a dims$b
            /.$BeginJava setResult(array(a.toType(), b.intValue())); $EndJava ./

-- 19.5) Names
name    ::=
                    -- Name
        simple_name$a
            /.$BeginJava setResult(a); $EndJava ./
    |   qualified_name$a
            /.$BeginJava setResult(a); $EndJava ./

simple_name ::=
                    -- Name
        IDENTIFIER$a
            /.$BeginJava setResult(new Name(nf, ts, pos(a), nf.Id(pos(a), a.toString()))); $EndJava ./

qualified_name ::=
                    -- Name
        name$a DOT IDENTIFIER$b
            /.$BeginJava setResult(new Name(nf, ts, pos(((JPGPosition) a.pos).getLeftIToken(), b), a, nf.Id(pos(b), b.toString()))); $EndJava ./

-- 19.6) Packages
compilation_unit ::=
                    -- SourceFile
        package_declaration_opt$a
        import_declarations_opt$b
        type_declarations_opt$c
            /.$BeginJava setResult(nf.SourceFile(pos(getLeftSpan(), getRightSpan()),
					     a, b, c));
	    $EndJava ./
    |   error
        type_declarations_opt$c
            /.$BeginJava setResult(nf.SourceFile(pos(getLeftSpan(), getRightSpan()),
					     null, Collections.EMPTY_LIST, c));
	    $EndJava ./

package_declaration_opt ::=
                    -- PackageNode
        package_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |
            /.$BeginJava setResult(null); $EndJava ./

import_declarations_opt ::=
                    -- List of Import
        import_declarations$a 
            /.$BeginJava setResult(a); $EndJava ./ 
    |
            /.$BeginJava setResult(new TypedList(new LinkedList(), Import.class, false)); $EndJava ./

type_declarations_opt   ::=
                    -- List of TopLevelDecl
        type_declarations$a 
            /.$BeginJava setResult(a); $EndJava ./ 
    |   
            /.$BeginJava setResult(new TypedList(new LinkedList(), TopLevelDecl.class, false)); $EndJava ./

import_declarations ::=
                    -- List of Import
        import_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Import.class, false); 
               l.add(a);
               setResult(l); $EndJava ./
    |   import_declarations$a import_declaration$b
            /.$BeginJava setResult(a); 
               a.add(b); $EndJava ./

type_declarations ::=
                    -- List of TopLevelDecl
        type_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), TopLevelDecl.class, false); 
               if (a != null)
                   l.add(a);
               setResult(l); $EndJava ./
    |   type_declarations$a type_declaration$b
            /.$BeginJava setResult(a);
               if (b != null)
                   a.add(b); $EndJava ./

package_declaration ::=
                    -- PackageNode
        PACKAGE name$a SEMICOLON
            /.$BeginJava setResult(a.toPackage()); $EndJava ./

import_declaration ::=
                    -- Import
        single_type_import_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |   type_import_on_demand_declaration$a
            /.$BeginJava setResult(a); $EndJava ./

single_type_import_declaration ::=
                    -- Import
        IMPORT$a qualified_name$b SEMICOLON$c
            /.$BeginJava setResult(nf.Import(pos(a, c), Import.CLASS, b.toString())); $EndJava ./

type_import_on_demand_declaration ::=
                    -- Import
        IMPORT$a name$b DOT MULT SEMICOLON$c
            /.$BeginJava setResult(nf.Import(pos(a, c), Import.PACKAGE, b.toString())); $EndJava ./

type_declaration ::=
                    -- ClassDecl
        class_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |   interface_declaration$a
            /.$BeginJava setResult(a); $EndJava ./    
    |   SEMICOLON
            /.$BeginJava setResult(null); $EndJava ./


-- 19.7) Productions used only in the LALR(1) grammar
modifiers_opt ::=
                    -- Flags
            /.$BeginJava setResult(Flags.NONE); $EndJava ./
    |   modifiers$a
            /.$BeginJava setResult(a); $EndJava ./

modifiers ::=
                    -- Flags
        modifier$a 
            /.$BeginJava setResult(a); $EndJava ./
    |   modifiers$a modifier$b
            /.$BeginJava if (a.intersects(b)) eq.enqueue(0, "Duplicate modifiers", pos());
               setResult(a.set(b)); $EndJava ./

modifier ::=
                    -- Flags
        PUBLIC
            /.$BeginJava setResult(Flags.PUBLIC); $EndJava ./
    |   PROTECTED
            /.$BeginJava setResult(Flags.PROTECTED); $EndJava ./
    |   PRIVATE
            /.$BeginJava setResult(Flags.PRIVATE); $EndJava ./
    |   STATIC
            /.$BeginJava setResult(Flags.STATIC); $EndJava ./
    |   ABSTRACT
            /.$BeginJava setResult(Flags.ABSTRACT); $EndJava ./
    |   FINAL
            /.$BeginJava setResult(Flags.FINAL); $EndJava ./
    |   NATIVE
            /.$BeginJava setResult(Flags.NATIVE); $EndJava ./
    |   SYNCHRONIZED
            /.$BeginJava setResult(Flags.SYNCHRONIZED); $EndJava ./
    |   TRANSIENT
            /.$BeginJava setResult(Flags.TRANSIENT); $EndJava ./
    |   VOLATILE
            /.$BeginJava setResult(Flags.VOLATILE); $EndJava ./
    |   STRICTFP
            /.$BeginJava setResult(Flags.STRICTFP); $EndJava ./

-- 19.8) Classes

-- 19.8.1) Class Declarations
class_declaration ::=
                    -- ClassDecl
        modifiers_opt$a CLASS$n IDENTIFIER$b 
                super_opt$c interfaces_opt$d class_body$e
            /.$BeginJava setResult(nf.ClassDecl(pos(n, e),
                a, b.toString(), c, d, e)); $EndJava ./

super ::=
                    -- TypeNode
        EXTENDS class_type$a 
            /.$BeginJava setResult(a); $EndJava ./        

super_opt ::=
                    -- TypeNode
            /.$BeginJava setResult(null); $EndJava./
    |   super$a
            /.$BeginJava setResult(a); $EndJava ./

interfaces ::=
                    -- List of TypeNode
        IMPLEMENTS interface_type_list$a
            /.$BeginJava setResult(a); $EndJava ./

interfaces_opt ::=
                    -- List of TypeNode
            /.$BeginJava setResult(new TypedList(new LinkedList(), TypeNode.class, false)); $EndJava ./
    |   interfaces$a
            /.$BeginJava setResult(a); $EndJava ./ 

interface_type_list ::=
                    -- List of TypeNode
        interface_type$a 
            /.$BeginJava List l = new TypedList(new LinkedList(), TypeNode.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   interface_type_list$a COMMA interface_type$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./

class_body ::=
                    -- ClassBody
        LBRACE$n class_body_declarations_opt$a RBRACE$b
            /.$BeginJava setResult(nf.ClassBody(pos(n, b), a)); $EndJava ./

class_body_declarations_opt ::=
                    -- List of ClassMember
            /.$BeginJava setResult(new TypedList(new LinkedList(), ClassMember.class, false)); $EndJava ./
    |   class_body_declarations$a
            /.$BeginJava setResult(a); $EndJava ./

class_body_declarations ::=
                    -- List of ClassMember
        class_body_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |   class_body_declarations$a class_body_declaration$b
            /.$BeginJava setResult(a);
               a.addAll(b); $EndJava ./

class_body_declaration ::=
                    -- List of ClassMember
        class_member_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |   static_initializer$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(nf.Initializer(pos(a), Flags.STATIC, a));
               setResult(l); $EndJava ./
    |   constructor_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   block$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(nf.Initializer(pos(a), Flags.NONE, a));
               setResult(l); $EndJava ./
    |   SEMICOLON
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               setResult(l); $EndJava ./
    |   error SEMICOLON
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               setResult(l); $EndJava ./
    |   error LBRACE
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               setResult(l); $EndJava ./

class_member_declaration ::=
                    -- List of ClassMember
        field_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |   method_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(a);
               setResult(l); $EndJava ./
        -- repeat the prod for 'class_declaration' here:
    |   modifiers_opt$a CLASS$n IDENTIFIER$b
                    super_opt$c interfaces_opt$d class_body$e
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(nf.ClassDecl(pos(n, e),
                        a, b.toString(), c, d, e));
               setResult(l); $EndJava ./
    |   interface_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(a);
               setResult(l); $EndJava ./


-- 19.8.2) Field Declarations
field_declaration ::=
                    -- List of ClassMember
        modifiers_opt$a type$b variable_declarators$c SEMICOLON$e
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               for (Iterator i = c.iterator(); i.hasNext(); ) {
                   VarDeclarator d = (VarDeclarator) i.next();
                   l.add(nf.FieldDecl(pos(b, e),
                                             a, array(b, d.dims),
                                             d.name, d.init));
               }
               setResult(l); $EndJava ./

variable_declarators ::=
                    -- List of VarDeclarator
        variable_declarator$a
            /.$BeginJava List l = new TypedList(new LinkedList(), VarDeclarator.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   variable_declarators$a COMMA variable_declarator$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./

variable_declarator ::=
                -- VarDeclarator
        variable_declarator_id$a
            /.$BeginJava setResult(a); $EndJava ./
    |   variable_declarator_id$a EQ variable_initializer$b
            /.$BeginJava setResult(a);
               a.init = b; $EndJava ./

variable_declarator_id ::=
                -- VarDeclarator
        IDENTIFIER$a
            /.$BeginJava setResult(new VarDeclarator(pos(a),
                            nf.Id(pos(a), a.toString()))); $EndJava ./
    |   variable_declarator_id$a LBRACK RBRACK
            /.$BeginJava setResult(a);
               a.dims++; $EndJava ./

variable_initializer ::=
                    -- Expr
        expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   array_initializer$a
            /.$BeginJava setResult(a); $EndJava ./


-- 19.8.3) Method Declarations
method_declaration ::=
                    -- MethodDecl
        method_header$a method_body$b
            /.$BeginJava setResult((MethodDecl) a.body(b)); $EndJava ./

method_header ::=
                    -- MethodDecl
        modifiers_opt$a type$b IDENTIFIER$c LPAREN 
                formal_parameter_list_opt$d RPAREN$g dims_opt$e throws_opt$f
            /.$BeginJava setResult(nf.MethodDecl(pos(b, g, c), a,
                array(b, e.intValue()), c.toString(),
                d, f, null)); $EndJava ./
    |   modifiers_opt$a VOID$b IDENTIFIER$c LPAREN
                formal_parameter_list_opt$d RPAREN$g throws_opt$f
            /.$BeginJava setResult(nf.MethodDecl(pos(b, g, c), a,
                nf.CanonicalTypeNode(pos(b),
                ts.Void()), c.toString(), d, f, null)); $EndJava ./

formal_parameter_list_opt ::=        
                    -- List of Formal
            /.$BeginJava setResult(new TypedList(new LinkedList(), Formal.class, false)); $EndJava ./ 
    |   formal_parameter_list$a
            /.$BeginJava setResult(a); $EndJava ./

formal_parameter_list ::=
                    -- List of Formal
        formal_parameter$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Formal.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   formal_parameter_list$a COMMA formal_parameter$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./    

formal_parameter ::=
                    -- Formal
        type$a variable_declarator_id$b
            /.$BeginJava setResult(nf.Formal(pos(a, b, b), Flags.NONE,
                                         array(a, b.dims), b.name)); $EndJava ./
    |   FINAL type$a variable_declarator_id$b
            /.$BeginJava setResult(nf.Formal(pos(a, b, b), Flags.FINAL,
                     array(a, b.dims), b.name)); $EndJava ./

throws_opt ::=                
                    -- List of TypeNode
            /.$BeginJava setResult(new TypedList(new LinkedList(), TypeNode.class, false)); $EndJava ./
    |   throws$a
            /.$BeginJava setResult(a); $EndJava ./

throws ::=                
                    -- List of TypeNode
        THROWS class_type_list$a
            /.$BeginJava setResult(a); $EndJava ./

class_type_list ::=
                    -- List of TypeNode
        class_type$a
            /.$BeginJava List l = new TypedList(new LinkedList(), TypeNode.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   class_type_list$a COMMA class_type$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./    

method_body ::=
                    -- Block
        block$a
            /.$BeginJava setResult(a); $EndJava ./
    |   SEMICOLON
            /.$BeginJava setResult(null); $EndJava ./


-- 19.8.4) Static Initializers
static_initializer ::=
                    -- Block
        STATIC block$a
            /.$BeginJava setResult(a); $EndJava ./


-- 19.8.5) Constructor Declarations
constructor_declaration ::=
                    -- ConstructorDecl
        modifiers_opt$m simple_name$a LPAREN formal_parameter_list_opt$b RPAREN
            throws_opt$c constructor_body$d
            /.$BeginJava setResult(nf.ConstructorDecl(pos(getRhsIToken(1), d), m, a.toString(), b,
                c, d)); $EndJava ./

constructor_body ::=
                    -- Block
        LBRACE$n explicit_constructor_invocation$a block_statements$b RBRACE$d
            /.$BeginJava List l = new TypedList(new LinkedList(), Stmt.class, false);
               l.add(a);
               l.addAll(b);
               setResult(nf.Block(pos(n, d), l)); $EndJava ./
    |   LBRACE$n explicit_constructor_invocation$a RBRACE$d
            /.$BeginJava setResult(nf.Block(pos(n, d), a)); $EndJava ./
    |   LBRACE$n block_statements$a RBRACE$d
            /.$BeginJava a.add(0, nf.SuperCall(pos(n, d), 
                Collections.EMPTY_LIST));
               setResult(nf.Block(pos(n, d), a)); $EndJava ./
    |   LBRACE$n RBRACE$d
            /.$BeginJava setResult(nf.Block(pos(n, d),
                nf.SuperCall(pos(n, d),
                Collections.EMPTY_LIST))); $EndJava ./

explicit_constructor_invocation ::=
                    -- ConstructorCall
        THIS$a LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            /.$BeginJava setResult(nf.ThisCall(pos(a, c), b)); $EndJava ./
    |   SUPER$a LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            /.$BeginJava setResult(nf.SuperCall(pos(a, c), b)); $EndJava ./
    |   primary$a DOT THIS$n LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            /.$BeginJava setResult(nf.ThisCall(pos(a, c, n), a, b)); $EndJava ./
    |   primary$a DOT SUPER$n LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            /.$BeginJava setResult(nf.SuperCall(pos(a, c, n), a, b)); $EndJava ./


-- 19.9) Interfaces

-- 19.9.1) Interface Declarations
interface_declaration ::=
                    -- ClassDecl
        modifiers_opt$a INTERFACE$n IDENTIFIER$b
                extends_interfaces_opt$c interface_body$d
            /.$BeginJava setResult(nf.ClassDecl(
                    pos(n, d), a.Interface(),
                        b.toString(), null, c, d)); $EndJava ./

extends_interfaces_opt ::=
                    -- List of TypeNode
            /.$BeginJava setResult(new TypedList(new LinkedList(), TypeNode.class, false)); $EndJava ./
    |   extends_interfaces$a
            /.$BeginJava setResult(a); $EndJava ./

extends_interfaces ::=
                    -- List of TypeNode
        EXTENDS interface_type$a
            /.$BeginJava List l = new TypedList(new LinkedList(), TypeNode.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   extends_interfaces$a COMMA interface_type$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./    

interface_body ::=
                    -- ClassBody
        LBRACE$n interface_member_declarations_opt$a RBRACE$d
            /.$BeginJava setResult(nf.ClassBody(pos(n, d), a)); $EndJava ./

interface_member_declarations_opt ::=
                    -- List of ClassMember
            /.$BeginJava setResult(new TypedList(new LinkedList(), ClassMember.class, false)); $EndJava ./
    |   interface_member_declarations$a
            /.$BeginJava setResult(a); $EndJava ./

interface_member_declarations ::=
                    -- List of ClassMember
        interface_member_declaration$a
            /.$BeginJava setResult(a); $EndJava ./        
    |   interface_member_declarations$a interface_member_declaration$b
            /.$BeginJava setResult(a);
               a.addAll(b); $EndJava ./

interface_member_declaration ::=
                    -- List of ClassMember
        constant_declaration$a
            /.$BeginJava setResult(a); $EndJava ./
    |   abstract_method_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   class_declaration$a 
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   interface_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ClassMember.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   SEMICOLON
            /.$BeginJava setResult(Collections.EMPTY_LIST); $EndJava ./

constant_declaration ::=
                    -- List of ClassMember
        field_declaration$a
            /.$BeginJava setResult(a); $EndJava ./

abstract_method_declaration ::=
                    -- MethodDecl
        method_header$a SEMICOLON
            /.$BeginJava setResult(a); $EndJava ./


-- 19.10) Arrays
array_initializer ::=
                    -- ArrayInit
        LBRACE$n variable_initializers$a COMMA RBRACE$d
            /.$BeginJava setResult(nf.ArrayInit(pos(n, d), a)); $EndJava ./
    |   LBRACE$n variable_initializers$a RBRACE$d
            /.$BeginJava setResult(nf.ArrayInit(pos(n, d), a)); $EndJava ./
    |   LBRACE$n COMMA RBRACE$d
            /.$BeginJava setResult(nf.ArrayInit(pos(n, d))); $EndJava ./
    |   LBRACE$n RBRACE$d
            /.$BeginJava setResult(nf.ArrayInit(pos(n, d))); $EndJava ./

variable_initializers ::=
                    -- List of Expr
        variable_initializer$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Expr.class, false);
           l.add(a);
           setResult(l); $EndJava ./
    |   variable_initializers$a COMMA variable_initializer$b
            /.$BeginJava setResult(a); a.add(b); $EndJava ./    


-- 19.11) Blocks and Statements
block ::=
                    -- Block
        LBRACE$n block_statements_opt$a RBRACE$d
            /.$BeginJava setResult(nf.Block(pos(n, d), a)); $EndJava ./
    |
        error RBRACE$d
            /.$BeginJava setResult(nf.Block(pos(d),
                                        Collections.EMPTY_LIST)); $EndJava ./

block_statements_opt ::=
                    -- List of Stmt
            /.$BeginJava setResult(new TypedList(new LinkedList(), Stmt.class, false)); $EndJava ./
    |   block_statements$a
            /.$BeginJava setResult(a); $EndJava ./

block_statements ::=
                    -- List of Stmt
        block_statement$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Stmt.class, false);
               l.addAll(a);
               setResult(l); $EndJava ./
    |   block_statements$a block_statement$b
            /.$BeginJava setResult(a);
               a.addAll(b); $EndJava ./

block_statement ::=
                    -- List of Stmt
        local_variable_declaration_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   statement$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Stmt.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   class_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Stmt.class, false);
               l.add(nf.LocalClassDecl(pos(a), a));
               setResult(l); $EndJava ./

local_variable_declaration_statement ::=
                    -- List of LocalDecl
        local_variable_declaration$a SEMICOLON
            /.$BeginJava setResult(a); $EndJava ./

local_variable_declaration ::=
                    -- List of LocalDecl
        type$a variable_declarators$b
            /.$BeginJava setResult(variableDeclarators(a, b, Flags.NONE)); $EndJava ./
    |   FINAL type$a variable_declarators$b
            /.$BeginJava setResult(variableDeclarators(a, b, Flags.FINAL)); $EndJava ./

statement ::=
                    -- Stmt
        statement_without_trailing_substatement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   labeled_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   if_then_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   if_then_else_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   while_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   for_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   error SEMICOLON$a
            /.$BeginJava setResult(nf.Empty(pos(a))); $EndJava ./

statement_no_short_if ::=
                    -- Stmt
        statement_without_trailing_substatement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   labeled_statement_no_short_if$a
            /.$BeginJava setResult(a); $EndJava ./
    |   if_then_else_statement_no_short_if$a
            /.$BeginJava setResult(a); $EndJava ./
    |   while_statement_no_short_if$a
            /.$BeginJava setResult(a); $EndJava ./
    |   for_statement_no_short_if$a
            /.$BeginJava setResult(a); $EndJava ./

statement_without_trailing_substatement ::=
                    -- Stmt 
        block$a
            /.$BeginJava setResult(a); $EndJava ./
    |   empty_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   expression_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   switch_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   do_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   break_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   continue_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   return_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   synchronized_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   throw_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   try_statement$a
            /.$BeginJava setResult(a); $EndJava ./
    |   assert_statement$a
            /.$BeginJava setResult(a); $EndJava ./

empty_statement ::=
                    -- Empty
        SEMICOLON$a
            /.$BeginJava setResult(nf.Empty(pos(a))); $EndJava ./

labeled_statement ::=
                    -- Labeled
        IDENTIFIER$a COLON statement$b
            /.$BeginJava setResult(nf.Labeled(pos(a, b),
                                      a.toString(), b)); $EndJava ./

labeled_statement_no_short_if ::=
                    -- Labeled
        IDENTIFIER$a COLON statement_no_short_if$b
            /.$BeginJava setResult(nf.Labeled(pos(a, b),
                                      a.toString(), b)); $EndJava ./

expression_statement ::=
                    -- Stmt
        statement_expression$a SEMICOLON$d
            /.$BeginJava setResult(nf.Eval(pos(a, d), a)); $EndJava ./

statement_expression ::=
                    -- Expr
        assignment$a
            /.$BeginJava setResult(a); $EndJava ./
    |   preincrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   predecrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   postincrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   postdecrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   method_invocation$a
            /.$BeginJava setResult(a); $EndJava ./
    |   class_instance_creation_expression$a
            /.$BeginJava setResult(a); $EndJava ./

if_then_statement ::=
                    -- If
        IF$n LPAREN expression$a RPAREN statement$b
            /.$BeginJava setResult(nf.If(pos(n, b), a, b)); $EndJava ./

if_then_else_statement ::=
                    -- If
        IF$n LPAREN expression$a RPAREN statement_no_short_if$b 
            ELSE statement$c
            /.$BeginJava setResult(nf.If(pos(n, c), a, b, c)); $EndJava ./

if_then_else_statement_no_short_if ::=
                    -- If
        IF$n LPAREN expression$a RPAREN statement_no_short_if$b
            ELSE statement_no_short_if$c
            /.$BeginJava setResult(nf.If(pos(n, c), a, b, c)); $EndJava ./

switch_statement ::=
                    -- Switch
        SWITCH$n LPAREN expression$a RPAREN switch_block$b
            /.$BeginJava setResult(nf.Switch(pos(n, b), a, b)); $EndJava ./

switch_block ::=
                    -- List of SwitchElement
        LBRACE switch_block_statement_groups$a switch_labels$b RBRACE
            /.$BeginJava setResult(a);
               a.addAll(b); $EndJava ./
    |   LBRACE switch_block_statement_groups$a RBRACE
            /.$BeginJava setResult(a); $EndJava ./
    |   LBRACE switch_labels$a RBRACE
            /.$BeginJava setResult(a); $EndJava ./
    |   LBRACE RBRACE
            /.$BeginJava setResult(new TypedList(new LinkedList(), SwitchElement.class, false)); $EndJava ./

switch_block_statement_groups ::=
                    -- List of SwitchElement
        switch_block_statement_group$a
            /.$BeginJava setResult(a); $EndJava ./        
    |   switch_block_statement_groups$a switch_block_statement_group$b
            /.$BeginJava setResult(a);
               a.addAll(b); $EndJava ./

switch_block_statement_group ::=
                    -- List of SwitchElement
        switch_labels$a block_statements$b
            /.$BeginJava List l = new TypedList(new LinkedList(), SwitchElement.class, false);
               l.addAll(a); 
               l.add(nf.SwitchBlock(pos(a, b), b));
               setResult(l); $EndJava ./

switch_labels ::=
                    -- List of Case
        switch_label$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Case.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   switch_labels$a switch_label$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./

switch_label ::=
                    -- Case
        CASE$n constant_expression$a COLON$d
            /.$BeginJava setResult(nf.Case(pos(n, d), a)); $EndJava ./
    |   DEFAULT$n COLON$d
            /.$BeginJava setResult(nf.Default(pos(n, d))); $EndJava ./


while_statement ::=
                    -- While
        WHILE$n LPAREN expression$a RPAREN statement$b
            /.$BeginJava setResult(nf.While(pos(n, b), a, b)); $EndJava ./

while_statement_no_short_if ::=
                    -- While
        WHILE$n LPAREN expression$a RPAREN statement_no_short_if$b
            /.$BeginJava setResult(nf.While(pos(n, b), a, b)); $EndJava ./

do_statement ::=            
                    -- Do
        DO$n statement$a WHILE LPAREN expression$b RPAREN SEMICOLON$d
            /.$BeginJava setResult(nf.Do(pos(n, d), a, b)); $EndJava ./

for_statement ::=
                    -- For 
        FOR$n LPAREN for_init_opt$a SEMICOLON expression_opt$b SEMICOLON$e
            for_update_opt$c RPAREN statement$d
            /.$BeginJava setResult(nf.For(pos(n, e), a, b, c, d)); $EndJava ./

for_statement_no_short_if ::=
                    -- For
        FOR$n LPAREN for_init_opt$a SEMICOLON expression_opt$b SEMICOLON$e
            for_update_opt$c RPAREN statement_no_short_if$d
            /.$BeginJava setResult(nf.For(pos(n, e), a, b, c, d)); $EndJava ./

for_init_opt ::=
                    -- List of ForInit
            /.$BeginJava setResult(new TypedList(new LinkedList(), ForInit.class, false)); $EndJava ./
    |   for_init$a
            /.$BeginJava setResult(a); $EndJava ./

for_init ::=
                    -- List of ForInit
        statement_expression_list$a
            /.$BeginJava setResult(a); $EndJava ./
    |   local_variable_declaration$a
            /.$BeginJava List l = new TypedList(new LinkedList(), ForInit.class, false);
               l.addAll(a);
               setResult(l); $EndJava ./

for_update_opt ::=
                    -- List of ForUpdate
            /.$BeginJava setResult(new TypedList(new LinkedList(), ForUpdate.class, false)); $EndJava ./
    |   for_update$a
            /.$BeginJava setResult(a); $EndJava ./

for_update ::=
                    -- List of ForUpdate
        statement_expression_list$a
            /.$BeginJava setResult(a); $EndJava ./

statement_expression_list ::=
                    -- List of Stmt
        statement_expression$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Eval.class, false);
               l.add(nf.Eval(pos(a), a));
               setResult(l); $EndJava ./
    |   statement_expression_list$a COMMA statement_expression$b
            /.$BeginJava setResult(a);
               a.add(nf.Eval(pos(a, b, b), b)); $EndJava ./


identifier_opt ::=
                    -- Name
            /.$BeginJava setResult(null); $EndJava ./
    |   IDENTIFIER$a
            /.$BeginJava setResult(new Name(nf, ts, pos(a), 
                nf.Id(pos(a), a.toString()))); $EndJava ./


break_statement ::=
                    -- Branch
        BREAK$n identifier_opt$a SEMICOLON$d
            /.$BeginJava if (a == null)
                   setResult(nf.Break(pos(n, d)));
               else
                   setResult(nf.Break(pos(n, d), a.toString())); $EndJava ./


continue_statement ::=
                    -- Branch
        CONTINUE$n identifier_opt$a SEMICOLON$d
            /.$BeginJava if (a == null)
                   setResult(nf.Continue(pos(n, d)));
               else
                   setResult(nf.Continue(pos(n, d), a.toString())); $EndJava ./

return_statement ::=
                    -- Return
        RETURN$n expression_opt$a SEMICOLON$d
            /.$BeginJava setResult(nf.Return(pos(n, d), a)); $EndJava ./

throw_statement ::=
                    -- Throw
        THROW$n expression$a SEMICOLON$d
            /.$BeginJava setResult(nf.Throw(pos(n, d), a)); $EndJava ./

synchronized_statement ::=
                    -- Synchronized
        SYNCHRONIZED$n LPAREN expression$a RPAREN block$b
            /.$BeginJava setResult(nf.Synchronized(pos(n, b), a, b)); $EndJava ./

try_statement ::=
                    -- Try
        TRY$n block$a catches$b
            /.$BeginJava setResult(nf.Try(pos(n, b), a, b)); $EndJava ./
    |   TRY$n block$a catches_opt$b finally$c
            /.$BeginJava setResult(nf.Try(pos(n, c), a, b, c)); $EndJava ./

catches_opt ::=
                    -- List of Catch
            /.$BeginJava setResult(new TypedList(new LinkedList(), Catch.class, false)); $EndJava ./
    |   catches$a
            /.$BeginJava setResult(a); $EndJava ./

catches ::=
                    -- List of Catch
        catch_clause$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Catch.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   catches$a catch_clause$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./

catch_clause ::=
                    -- Catch
        CATCH$n LPAREN formal_parameter$a RPAREN block$b
            /.$BeginJava setResult(nf.Catch(pos(n, b), a, b)); $EndJava ./

finally ::=
                    -- Block
        FINALLY block$a
            /.$BeginJava setResult(a); $EndJava ./


assert_statement ::=
                    -- Assert
        ASSERT$x expression$a SEMICOLON$d
                /.$BeginJava setResult(nf.Assert(pos(x, d), a)); $EndJava ./
    |   ASSERT$x expression$a COLON expression$b SEMICOLON$d
                /.$BeginJava setResult(nf.Assert(pos(x, d), a, b)); $EndJava ./


-- 19.12) Expressions
primary ::=
                    -- Expr
        primary_no_new_array$a
            /.$BeginJava setResult(a); $EndJava ./
    |   array_creation_expression$a
            /.$BeginJava setResult(a); $EndJava ./

primary_no_new_array ::=
                    -- Expr
        literal$a
            /.$BeginJava setResult(a); $EndJava ./
    |   THIS$a
            /.$BeginJava setResult(nf.This(pos(a))); $EndJava ./
    |   LPAREN expression$a RPAREN
            /.$BeginJava setResult(a); $EndJava ./
    |   class_instance_creation_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   field_access$a
            /.$BeginJava setResult(a); $EndJava ./
    |   method_invocation$a
            /.$BeginJava setResult(a); $EndJava ./
    |   array_access$a
            /.$BeginJava setResult(a); $EndJava ./
    |   primitive_type$a DOT CLASS$n
            /.$BeginJava setResult(nf.ClassLit(pos(a, n, n), a)); $EndJava ./
    |   VOID$a DOT CLASS$n
            /.$BeginJava setResult(nf.ClassLit(pos(a, n, n), 
                nf.CanonicalTypeNode(pos(a),
                                            ts.Void()))); $EndJava ./
    |   array_type$a DOT CLASS$n
            /.$BeginJava setResult(nf.ClassLit(pos(a, n, n), a)); $EndJava ./
    |   name$a DOT CLASS$n
            /.$BeginJava setResult(nf.ClassLit(pos(getRhsIToken(1), n, n), a.toType())); $EndJava ./
    |   name$a DOT THIS$n
            /.$BeginJava setResult(nf.This(pos(getRhsIToken(1), n, n), a.toType())); $EndJava ./

class_instance_creation_expression ::=
                    -- Expr
        NEW$n class_type$a LPAREN argument_list_opt$b RPAREN$d
            /.$BeginJava setResult(nf.New(pos(n, d), a, b)); $EndJava ./
    |   NEW$n class_type$a LPAREN argument_list_opt$b RPAREN class_body$c
            /.$BeginJava setResult(nf.New(pos(n, c), a, b, c)); $EndJava ./
    |   primary$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN$d
            /.$BeginJava setResult(nf.New(pos(a, d), a,
				      b.toType(), c)); $EndJava ./
    |   primary$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN class_body$d
            /.$BeginJava setResult(nf.New(pos(a, d), a,
				      b.toType(), c, d)); $EndJava ./
    |   name$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN$d
            /.$BeginJava setResult(nf.New(pos(a, d), a.toExpr(),
				      b.toType(), c)); $EndJava ./
    |   name$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN class_body$d
            /.$BeginJava setResult(nf.New(pos(a, d), a.toExpr(),
				      b.toType(), c, d)); $EndJava ./

argument_list_opt ::=
                    -- List of Expr
            /.$BeginJava setResult(new TypedList(new LinkedList(), Expr.class, false)); $EndJava ./
    |   argument_list$a
            /.$BeginJava setResult(a); $EndJava ./

argument_list ::=
                    -- List of Expr
        expression$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Expr.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   argument_list$a COMMA expression$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./

array_creation_expression ::=
                    -- NewArray
        NEW$n primitive_type$a dim_exprs$b dims_opt$c
            /.$BeginJava setResult(nf.NewArray(pos(n, b), a, b,
                c.intValue())); $EndJava ./
    |   NEW$n class_or_interface_type$a dim_exprs$b dims_opt$c
            /.$BeginJava setResult(nf.NewArray(pos(n, b), a, b, 
                c.intValue())); $EndJava ./
    |   NEW$n primitive_type$a dims$b array_initializer$c
            /.$BeginJava setResult(nf.NewArray(pos(n, c), a,
                b.intValue(), c)); $EndJava ./
    |   NEW$n class_or_interface_type$a dims$b array_initializer$c
            /.$BeginJava setResult(nf.NewArray(pos(n, c), a,
                b.intValue(), c)); $EndJava ./

dim_exprs ::=
                    -- List of Expr
        dim_expr$a
            /.$BeginJava List l = new TypedList(new LinkedList(), Expr.class, false);
               l.add(a);
               setResult(l); $EndJava ./
    |   dim_exprs$a dim_expr$b
            /.$BeginJava setResult(a);
               a.add(b); $EndJava ./

dim_expr ::=
                    -- Expr
        LBRACK$x expression$a RBRACK$y
            /.$BeginJava setResult((Expr)a.position(pos(x,y,a))); $EndJava ./

dims_opt ::=
                    -- Integer
            /.$BeginJava setResult(new Integer(0)); $EndJava ./
    |   dims$a
            /.$BeginJava setResult(a); $EndJava ./

dims ::=
                    -- Integer
        LBRACK RBRACK
            /.$BeginJava setResult(new Integer(1)); $EndJava ./
    |   dims$a LBRACK RBRACK
            /.$BeginJava setResult(new Integer(a.intValue() + 1)); $EndJava ./

field_access ::=
                    -- Field
        primary$a DOT IDENTIFIER$b
            /.$BeginJava setResult(nf.Field(pos(a, b, b), a,
                b.toString())); $EndJava ./
    |   SUPER$n DOT IDENTIFIER$a
            /.$BeginJava setResult(nf.Field(pos(a),
                nf.Super(pos(n)),
                a.toString())); $EndJava ./
    |   name$a DOT SUPER$n DOT IDENTIFIER$b
            /.$BeginJava setResult(nf.Field(pos(b),
                nf.Super(pos(n), a.toType()),
                b.toString())); $EndJava ./

method_invocation ::=
                    -- Call
        name$a LPAREN argument_list_opt$b RPAREN$d
            /.$BeginJava setResult(nf.Call(pos(getRhsIToken(1),d),
                a.prefix == null ? null : a.prefix.toReceiver(),
                a.name, b)); $EndJava ./
    |   primary$a DOT IDENTIFIER$b LPAREN argument_list_opt$c RPAREN$d
            /.$BeginJava setResult(nf.Call(pos(b,d), a,
                b.toString(), c)); $EndJava ./
    |   SUPER$a DOT IDENTIFIER$b LPAREN argument_list_opt$c RPAREN$d
            /.$BeginJava setResult(nf.Call(pos(a,d, b),
                nf.Super(pos(a)),
                b.toString(), c)); $EndJava ./
    |   name$a DOT SUPER$n DOT IDENTIFIER$b LPAREN argument_list_opt$c RPAREN$d
            /.$BeginJava setResult(nf.Call(pos(b,d),
                nf.Super(pos(n), a.toType()),
                b.toString(), c)); $EndJava ./

array_access ::=
                    -- ArrayAccess
        name$a LBRACK expression$b RBRACK$d
            /.$BeginJava setResult(nf.ArrayAccess(pos(a, d), a.toExpr(), b)); $EndJava ./
    |   primary_no_new_array$a LBRACK expression$b RBRACK$d
            /.$BeginJava setResult(nf.ArrayAccess(pos(a, d), a, b)); $EndJava ./

postfix_expression ::=
                    -- Expr
        primary$a
            /.$BeginJava setResult(a); $EndJava ./
    |   name$a
            /.$BeginJava setResult(a.toExpr()); $EndJava ./
    |   postincrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   postdecrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./

postincrement_expression ::=
                    -- Unary
        postfix_expression$a PLUSPLUS$b
            /.$BeginJava setResult(nf.Unary(pos(a,b), a, Unary.POST_INC)); $EndJava ./

postdecrement_expression ::=
                    -- Unary
        postfix_expression$a MINUSMINUS$b
            /.$BeginJava setResult(nf.Unary(pos(a,b), a, Unary.POST_DEC)); $EndJava ./

unary_expression ::=
                    -- Expr
        preincrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   predecrement_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   PLUS$b unary_expression$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.POS, a)); $EndJava ./
    |   MINUS$b unary_expression$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.NEG, a)); $EndJava ./
    |   MINUS$b boundary_literal$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.NEG, a)); $EndJava ./
    |   unary_expression_not_plus_minus$a
            /.$BeginJava setResult(a); $EndJava ./

preincrement_expression ::=
                    -- Unary
        PLUSPLUS$b unary_expression$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.PRE_INC, a)); $EndJava ./

predecrement_expression ::=
                    -- Unary
        MINUSMINUS$b unary_expression$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.PRE_DEC, a)); $EndJava ./

unary_expression_not_plus_minus ::=
                    -- Expr
        postfix_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   COMP$b unary_expression$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.BIT_NOT, a)); $EndJava ./
    |   NOT$b unary_expression$a
            /.$BeginJava setResult(nf.Unary(pos(b,a,a), Unary.NOT, a)); $EndJava ./
    |   cast_expression$a
            /.$BeginJava setResult(a); $EndJava ./

cast_expression ::=
                    -- Cast
        LPAREN$p primitive_type$a dims_opt$b RPAREN unary_expression$c
            /.$BeginJava setResult(nf.Cast(pos(p, c,a),
                array(a, b.intValue()), c)); $EndJava ./
    |   LPAREN$p expression$a RPAREN unary_expression_not_plus_minus$b
            /.$BeginJava setResult(nf.Cast(pos(p, b,a),
                exprToType(a), b)); $EndJava ./
    |   LPAREN$p name$a dims$b RPAREN unary_expression_not_plus_minus$c
            /.$BeginJava setResult(nf.Cast(pos(p, c,a),
                array(a.toType(), b.intValue()), c)); $EndJava ./

multiplicative_expression ::=
                    -- Expr
        unary_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   multiplicative_expression$a MULT unary_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.MUL, b)); $EndJava ./
    |   multiplicative_expression$a DIV unary_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.DIV, b)); $EndJava ./
    |   multiplicative_expression$a MOD unary_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.MOD, b)); $EndJava ./

additive_expression ::=
                    -- Expr
        multiplicative_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   additive_expression$a PLUS multiplicative_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.ADD, b)); $EndJava ./
    |   additive_expression$a MINUS multiplicative_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.SUB, b)); $EndJava ./

shift_expression ::=
                    -- Expr
        additive_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   shift_expression$a LSHIFT additive_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.SHL, b)); $EndJava ./
    |   shift_expression$a RSHIFT additive_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.SHR, b)); $EndJava ./
    |   shift_expression$a URSHIFT additive_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.USHR, b)); $EndJava ./

relational_expression ::=
                    -- Expr
        shift_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   relational_expression$a LT shift_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.LT, b)); $EndJava ./
    |   relational_expression$a GT shift_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.GT, b)); $EndJava ./
    |   relational_expression$a LTEQ shift_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.LE, b)); $EndJava ./
    |   relational_expression$a GTEQ shift_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.GE, b)); $EndJava ./
    |   relational_expression$a INSTANCEOF reference_type$b
            /.$BeginJava setResult(nf.Instanceof(pos(a, b), a, b)); $EndJava ./

    
equality_expression ::=
                    -- Expr
        relational_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   equality_expression$a EQEQ relational_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.EQ, b)); $EndJava ./
    |   equality_expression$a NOTEQ relational_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.NE, b)); $EndJava ./

and_expression ::=
                    -- Expr
        equality_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   and_expression$a AND equality_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.BIT_AND, b)); $EndJava ./

exclusive_or_expression ::=
                    -- Expr
        and_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   exclusive_or_expression$a XOR and_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.BIT_XOR, b)); $EndJava ./

inclusive_or_expression ::=
                    -- Expr
        exclusive_or_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   inclusive_or_expression$a OR exclusive_or_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.BIT_OR, b)); $EndJava ./

conditional_and_expression ::=
                    -- Expr
        inclusive_or_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   conditional_and_expression$a ANDAND inclusive_or_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.COND_AND, b)); $EndJava ./

conditional_or_expression ::=
                    -- Expr
        conditional_and_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   conditional_or_expression$a OROR conditional_and_expression$b
            /.$BeginJava setResult(nf.Binary(pos(a, b), a, 
                Binary.COND_OR, b)); $EndJava ./

conditional_expression ::=
                    -- Expr
        conditional_or_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   conditional_or_expression$a QUESTION expression$b 
            COLON conditional_expression$c
            /.$BeginJava setResult(nf.Conditional(pos(a, c), a, 
                b, c)); $EndJava ./

assignment_expression ::=
                    -- Expr
        conditional_expression$a
            /.$BeginJava setResult(a); $EndJava ./
    |   assignment$a
            /.$BeginJava setResult(a); $EndJava ./

assignment ::=
                    -- Expr
        left_hand_side$a assignment_operator$b assignment_expression$c
            /.$BeginJava setResult(nf.Assign(pos(a, c), a, b, c)); $EndJava ./

left_hand_side ::=
                    -- Expr
        name$a
            /.$BeginJava setResult(a.toExpr()); $EndJava ./
    |   field_access$a
            /.$BeginJava setResult(a); $EndJava ./
    |   array_access$a
            /.$BeginJava setResult(a); $EndJava ./

assignment_operator ::=
                    -- Assign.Operator
        EQ
            /.$BeginJava setResult(Assign.ASSIGN); $EndJava ./
    |   MULTEQ
            /.$BeginJava setResult(Assign.MUL_ASSIGN); $EndJava ./
    |   DIVEQ
            /.$BeginJava setResult(Assign.DIV_ASSIGN); $EndJava ./
    |   MODEQ
            /.$BeginJava setResult(Assign.MOD_ASSIGN); $EndJava ./
    |   PLUSEQ
            /.$BeginJava setResult(Assign.ADD_ASSIGN); $EndJava ./
    |   MINUSEQ
            /.$BeginJava setResult(Assign.SUB_ASSIGN); $EndJava ./
    |   LSHIFTEQ
            /.$BeginJava setResult(Assign.SHL_ASSIGN); $EndJava ./
    |   RSHIFTEQ
            /.$BeginJava setResult(Assign.SHR_ASSIGN); $EndJava ./
    |   URSHIFTEQ
            /.$BeginJava setResult(Assign.USHR_ASSIGN); $EndJava ./
    |   ANDEQ
            /.$BeginJava setResult(Assign.BIT_AND_ASSIGN); $EndJava ./
    |   XOREQ
            /.$BeginJava setResult(Assign.BIT_XOR_ASSIGN); $EndJava ./
    |   OREQ
            /.$BeginJava setResult(Assign.BIT_OR_ASSIGN); $EndJava ./

expression_opt ::=
                    -- Expr
            /.$BeginJava setResult(null); $EndJava ./
    |   expression$a
            /.$BeginJava setResult(a); $EndJava ./

expression ::=
                    -- Expr
        assignment_expression$a
            /.$BeginJava setResult(a); $EndJava ./

constant_expression ::=
                    -- Expr
        expression$a
            /.$BeginJava setResult(a); $EndJava ./
%End

%Types
-- 19.2) The Syntactic Grammar
SourceFile ::= goal
-- 19.3) Lexical Structure
polyglot.ast.Lit ::= literal
polyglot.ast.Lit ::= boundary_literal
-- 19.4) Types, Values, and Variables
TypeNode ::= type | primitive_type | numeric_type
TypeNode ::= integral_type | floating_point_type
TypeNode ::= reference_type
TypeNode ::= class_or_interface_type
TypeNode ::= class_type | interface_type
TypeNode ::= array_type
-- 19.5) Names
Name ::= simple_name | name | qualified_name
-- 19.6) Packages
SourceFile ::= compilation_unit
PackageNode ::= package_declaration_opt | package_declaration
List ::= import_declarations_opt | import_declarations
List ::= type_declarations_opt | type_declarations
Import ::= import_declaration
Import ::= single_type_import_declaration
Import ::= type_import_on_demand_declaration
ClassDecl ::= type_declaration
-- 19.7) Productions used only in the LALR(1) grammar
Flags ::= modifiers_opt | modifiers | modifier
-- 19.8.1) Class Declaration
ClassDecl ::= class_declaration
TypeNode ::= super | super_opt
List ::= interfaces | interfaces_opt | interface_type_list
ClassBody ::= class_body
List ::= class_body_declarations | class_body_declarations_opt
List ::= class_body_declaration | class_member_declaration
-- 19.8.2) Field Declarations
List ::= field_declaration
List ::= variable_declarators
VarDeclarator ::= variable_declarator
VarDeclarator ::= variable_declarator_id
Expr ::= variable_initializer
-- 19.8.3) Method Declarations
MethodDecl ::= method_declaration | method_header
List ::= formal_parameter_list_opt | formal_parameter_list
Formal ::= formal_parameter
List ::= throws_opt | throws
List ::= class_type_list
Block ::= method_body
-- 19.8.4) Static Initializers
Block ::= static_initializer
-- 19.8.5) Constructor Declarations
ConstructorDecl ::= constructor_declaration
Block ::= constructor_body
ConstructorCall ::= explicit_constructor_invocation
-- 19.9.1) Interface Declarations
ClassDecl ::= interface_declaration
List ::= extends_interfaces_opt | extends_interfaces
ClassBody ::= interface_body
List ::= interface_member_declarations_opt | interface_member_declarations
List ::= interface_member_declaration
List ::= constant_declaration
MethodDecl ::= abstract_method_declaration
-- 19.10) Arrays
ArrayInit ::= array_initializer
List ::= variable_initializers
-- 19.11) Blocks and Statements
Block ::= block
List ::= block_statements_opt | block_statements
List ::= block_statement
List ::= local_variable_declaration_statement
List ::= local_variable_declaration
Stmt ::= statement | statement_no_short_if
Stmt ::= statement_without_trailing_substatement
Empty ::= empty_statement
Labeled ::= labeled_statement | labeled_statement_no_short_if
Stmt ::= expression_statement
Expr ::= statement_expression
If ::= if_then_statement
If ::= if_then_else_statement | if_then_else_statement_no_short_if
Switch ::= switch_statement
List ::= switch_block | switch_block_statement_groups
List ::= switch_block_statement_group | switch_labels
Case ::= switch_label
While ::= while_statement | while_statement_no_short_if
Do ::= do_statement
For ::= for_statement | for_statement_no_short_if
List ::= for_init_opt | for_init
List ::= for_update_opt | for_update
List ::= statement_expression_list
Name ::= identifier_opt
Branch ::= break_statement | continue_statement
Return ::= return_statement
Throw ::= throw_statement
Synchronized ::= synchronized_statement
Try ::= try_statement
List ::= catches_opt | catches
Catch ::= catch_clause
Block ::= finally
Assert ::= assert_statement
-- 19.12) Expressions
Expr ::= primary | primary_no_new_array
Expr ::= class_instance_creation_expression
List ::= argument_list_opt | argument_list
NewArray ::= array_creation_expression
List ::= dim_exprs
Expr ::= dim_expr
Integer ::= dims_opt | dims
Field ::= field_access
Call ::= method_invocation
ArrayAccess ::= array_access
Expr ::= postfix_expression
Unary ::= postincrement_expression | postdecrement_expression
Expr ::= unary_expression | unary_expression_not_plus_minus
Unary ::= preincrement_expression | predecrement_expression
Cast ::= cast_expression
Expr ::= multiplicative_expression | additive_expression
Expr ::= shift_expression | relational_expression | equality_expression
Expr ::= and_expression | exclusive_or_expression | inclusive_or_expression
Expr ::= conditional_and_expression | conditional_or_expression
Expr ::= conditional_expression | assignment_expression
Expr ::= assignment
Expr ::= left_hand_side
Assign.Operator ::= assignment_operator
Expr ::= expression_opt | expression
Expr ::= constant_expression
%End
