
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.

package org.eclipse.imp.java.parser;

import lpg.runtime.*;
import java.util.Collections;
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

public class JavaParser implements RuleAction, IParser, Parser
{
    private PrsStream prsStream = null;
    
    private boolean unimplementedSymbolsWarning = false;

    private static ParseTable prsTable = new JavaParserprs();
    public ParseTable getParseTable() { return prsTable; }

    private DeterministicParser dtParser = null;
    public DeterministicParser getParser() { return dtParser; }

    private void setResult(Object object) { dtParser.setSym1(object); }
    public Object getRhsSym(int i) { return dtParser.getSym(i); }

    public int getRhsTokenIndex(int i) { return dtParser.getToken(i); }
    public IToken getRhsIToken(int i) { return prsStream.getIToken(getRhsTokenIndex(i)); }
    
    public int getRhsFirstTokenIndex(int i) { return dtParser.getFirstToken(i); }
    public IToken getRhsFirstIToken(int i) { return prsStream.getIToken(getRhsFirstTokenIndex(i)); }

    public int getRhsLastTokenIndex(int i) { return dtParser.getLastToken(i); }
    public IToken getRhsLastIToken(int i) { return prsStream.getIToken(getRhsLastTokenIndex(i)); }

    public int getLeftSpan() { return dtParser.getFirstToken(); }
    public IToken getLeftIToken()  { return prsStream.getIToken(getLeftSpan()); }

    public int getRightSpan() { return dtParser.getLastToken(); }
    public IToken getRightIToken() { return prsStream.getIToken(getRightSpan()); }

    public int getRhsErrorTokenIndex(int i)
    {
        int index = dtParser.getToken(i);
        IToken err = prsStream.getIToken(index);
        return (err instanceof ErrorToken ? index : 0);
    }
    public ErrorToken getRhsErrorIToken(int i)
    {
        int index = dtParser.getToken(i);
        IToken err = prsStream.getIToken(index);
        return (ErrorToken) (err instanceof ErrorToken ? err : null);
    }

    public void reset(ILexStream lexStream)
    {
        prsStream = new PrsStream(lexStream);
        dtParser.reset(prsStream);

        try
        {
            prsStream.remapTerminalSymbols(orderedTerminalSymbols(), prsTable.getEoftSymbol());
        }
        catch(NullExportedSymbolsException e) {
        }
        catch(NullTerminalSymbolsException e) {
        }
        catch(UnimplementedTerminalsException e)
        {
            if (unimplementedSymbolsWarning) {
                java.util.ArrayList unimplemented_symbols = e.getSymbols();
                System.out.println("The Lexer will not scan the following token(s):");
                for (int i = 0; i < unimplemented_symbols.size(); i++)
                {
                    Integer id = (Integer) unimplemented_symbols.get(i);
                    System.out.println("    " + JavaParsersym.orderedTerminalSymbols[id.intValue()]);               
                }
                System.out.println();
            }
        }
        catch(UndefinedEofSymbolException e)
        {
            throw new Error(new UndefinedEofSymbolException
                                ("The Lexer does not implement the Eof symbol " +
                                 JavaParsersym.orderedTerminalSymbols[prsTable.getEoftSymbol()]));
        }
    }
    
    public JavaParser()
    {
        try
        {
            dtParser = new DeterministicParser(prsStream, prsTable, (RuleAction) this);
        }
        catch (NotDeterministicParseTableException e)
        {
            throw new Error(new NotDeterministicParseTableException
                                ("Regenerate JavaParserprs.java with -NOBACKTRACK option"));
        }
        catch (BadParseSymFileException e)
        {
            throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- JavaParsersym.java. Regenerate JavaParserprs.java"));
        }
    }

    public JavaParser(ILexStream lexStream)
    {
        this();
        reset(lexStream);
    }

    public int numTokenKinds() { return JavaParsersym.numTokenKinds; }
    public String[] orderedTerminalSymbols() { return JavaParsersym.orderedTerminalSymbols; }
    public String getTokenKindName(int kind) { return JavaParsersym.orderedTerminalSymbols[kind]; }            
    public int getEOFTokenKind() { return prsTable.getEoftSymbol(); }
    public IPrsStream getIPrsStream() { return prsStream; }

    /**
     * @deprecated replaced by {@link #getIPrsStream()}
     *
     */
    public PrsStream getPrsStream() { return prsStream; }

    /**
     * @deprecated replaced by {@link #getIPrsStream()}
     *
     */
    public PrsStream getParseStream() { return prsStream; }

    public Object parser()
    {
        return parser(null, 0);
    }
        
    public Object parser(Monitor monitor)
    {
        return parser(monitor, 0);
    }
        
    public Object parser(int error_repair_count)
    {
        return parser(null, error_repair_count);
    }
        
    public Object parser(Monitor monitor, int error_repair_count)
    {
        dtParser.setMonitor(monitor);

        try
        {
            return (Object) dtParser.parse();
        }
        catch (BadParseException e)
        {
            prsStream.reset(e.error_token); // point to error token

            DiagnoseParser diagnoseParser = new DiagnoseParser(prsStream, prsTable);
            diagnoseParser.diagnose(e.error_token);
        }

        return null;
    }

    //
    // Additional entry points, if any
    //
    

    private ErrorQueue eq;
    private TypeSystem ts;
    private NodeFactory nf;
    private FileSource source;
    private boolean unrecoverableSyntaxError= false;

    public JavaParser(ILexStream lexStream, TypeSystem t, NodeFactory n, FileSource source, ErrorQueue q) {
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
 
    public void ruleAction(int ruleNumber)
    {
        switch (ruleNumber)
        {

            //
            // Rule 1:  goal ::= compilation_unit$a
            //
            case 1: {
                //#line 401 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                SourceFile a = (SourceFile) getRhsSym(1); if (eq.hasErrors()) setResult(null);
           else setResult(a);                 break;
            } 
            //
            // Rule 2:  literal ::= INTEGER_LITERAL$a
            //
            case 2: {
                //#line 408 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
           String s= a.toString();
           if (s.charAt(0) == '0' && s.length() > 1 && (s.charAt(1) == 'x' || s.charAt(1) == 'X'))
               setResult(nf.IntLit(pos(a), IntLit.INT, Integer.parseInt(s.substring(2), 16)));
           else if (s.charAt(0) == '0' && s.length() > 1)
               setResult(nf.IntLit(pos(a), IntLit.INT, Integer.parseInt(s, 8)));
           else
               setResult(nf.IntLit(pos(a), IntLit.INT, Integer.parseInt(s)));
                           break;
            } 
            //
            // Rule 3:  literal ::= LONG_LITERAL$a
            //
            case 3: {
                //#line 418 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.IntLit(pos(a), IntLit.LONG,
                 Long.parseLong(a.toString().substring(0, a.toString().length() - 1))));                 break;
            } 
            //
            // Rule 4:  literal ::= DOUBLE_LITERAL$a
            //
            case 4: {
                //#line 421 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.FloatLit(pos(a), FloatLit.DOUBLE,
                                       Double.parseDouble(a.toString())));                 break;
            } 
            //
            // Rule 5:  literal ::= FLOAT_LITERAL$a
            //
            case 5: {
                //#line 424 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.FloatLit(pos(a), FloatLit.FLOAT,
                                       Float.parseFloat(a.toString())));                 break;
            } 
            //
            // Rule 6:  literal ::= TRUE$a
            //
            case 6: {
                //#line 427 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.BooleanLit(pos(a), true));                 break;
            } 

            //
            // Rule 7:  literal ::= FALSE$a
            //
            case 7: {
                //#line 429 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.BooleanLit(pos(a), false));                 break;
            } 

            //
            // Rule 8:  literal ::= CHARACTER_LITERAL$a
            //
            case 8: {
                //#line 431 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CharLit(pos(a),
                  a.toString().charAt(0)));                 break;
            } 
            //
            // Rule 9:  literal ::= STRING_LITERAL$a
            //
            case 9: {
                //#line 434 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.StringLit(pos(a), a.toString()));                 break;
            } 

            //
            // Rule 10:  literal ::= NULL$a
            //
            case 10: {
                //#line 436 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.NullLit(pos(a)));                 break;
            } 

            //
            // Rule 11:  boundary_literal ::= INTEGER_LITERAL_BD$a
            //
            case 11: {
                //#line 441 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.IntLit(pos(a), IntLit.INT,
                                 Integer.parseInt(a.toString())));                 break;
            } 
            //
            // Rule 12:  boundary_literal ::= LONG_LITERAL_BD$a
            //
            case 12: {
                //#line 444 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.IntLit(pos(a), IntLit.LONG,
                 Long.parseLong(a.toString())));                 break;
            } 
            //
            // Rule 13:  type ::= primitive_type$a
            //
            case 13: {
                //#line 453 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 14:  type ::= reference_type$a
            //
            case 14: {
                //#line 455 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 15:  primitive_type ::= numeric_type$a
            //
            case 15: {
                //#line 460 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 16:  primitive_type ::= BOOLEAN$a
            //
            case 16: {
                //#line 462 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a), ts.Boolean()));                 break;
            } 

            //
            // Rule 17:  numeric_type ::= integral_type$a
            //
            case 17: {
                //#line 467 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 18:  numeric_type ::= floating_point_type$a
            //
            case 18: {
                //#line 469 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 19:  integral_type ::= BYTE$a
            //
            case 19: {
                //#line 474 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a), ts.Byte()));                 break;
            } 

            //
            // Rule 20:  integral_type ::= CHAR$a
            //
            case 20: {
                //#line 476 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a), ts.Char()));                 break;
            } 

            //
            // Rule 21:  integral_type ::= SHORT$a
            //
            case 21: {
                //#line 478 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a), ts.Short()));                 break;
            } 

            //
            // Rule 22:  integral_type ::= INT$a
            //
            case 22: {
                //#line 480 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a), ts.Int()));                 break;
            } 

            //
            // Rule 23:  integral_type ::= LONG$a
            //
            case 23: {
                //#line 482 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a), ts.Long()));                 break;
            } 

            //
            // Rule 24:  floating_point_type ::= FLOAT$a
            //
            case 24: {
                //#line 487 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a),
                   ts.Float()));                 break;
            } 
            //
            // Rule 25:  floating_point_type ::= DOUBLE$a
            //
            case 25: {
                //#line 490 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.CanonicalTypeNode(pos(a),
                   ts.Double()));                 break;
            } 
            //
            // Rule 26:  reference_type ::= class_or_interface_type$a
            //
            case 26: {
                //#line 496 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 27:  reference_type ::= array_type$a
            //
            case 27: {
                //#line 498 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 28:  class_or_interface_type ::= name$a
            //
            case 28: {
                //#line 503 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1); setResult(a.toType());                 break;
            } 

            //
            // Rule 29:  class_type ::= class_or_interface_type$a
            //
            case 29: {
                //#line 508 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 30:  interface_type ::= class_or_interface_type$a
            //
            case 30: {
                //#line 513 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 31:  array_type ::= primitive_type$a dims$b
            //
            case 31: {
                //#line 518 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1);
                //#line 518 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer b = (Integer) getRhsSym(2); setResult(array(a, b.intValue()));                 break;
            } 

            //
            // Rule 32:  array_type ::= name$a dims$b
            //
            case 32: {
                //#line 520 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 520 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer b = (Integer) getRhsSym(2); setResult(array(a.toType(), b.intValue()));                 break;
            } 

            //
            // Rule 33:  name ::= simple_name$a
            //
            case 33: {
                //#line 526 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 34:  name ::= qualified_name$a
            //
            case 34: {
                //#line 528 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 35:  simple_name ::= IDENTIFIER$a
            //
            case 35: {
                //#line 533 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(new Name(nf, ts, pos(a), nf.Id(pos(a), a.toString())));                 break;
            } 

            //
            // Rule 36:  qualified_name ::= name$a DOT IDENTIFIER$b
            //
            case 36: {
                //#line 538 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 538 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3); setResult(new Name(nf, ts, pos(((JPGPosition) a.pos).getLeftIToken(), b), a, nf.Id(pos(b), b.toString())));                 break;
            } 

            //
            // Rule 37:  compilation_unit ::= package_declaration_opt$a import_declarations_opt$b type_declarations_opt$c
            //
            case 37: {
                //#line 544 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                PackageNode a = (PackageNode) getRhsSym(1);
                //#line 545 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2);
                //#line 546 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(3); setResult(nf.SourceFile(pos(getLeftSpan(), getRightSpan()),
				     a, b, c));
                    break;
            } 
            //
            // Rule 38:  compilation_unit ::= error type_declarations_opt$c
            //
            case 38: {
                //#line 551 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(2); setResult(nf.SourceFile(pos(getLeftSpan(), getRightSpan()),
				     null, Collections.EMPTY_LIST, c));
                    break;
            } 
            //
            // Rule 39:  package_declaration_opt ::= package_declaration$a
            //
            case 39: {
                //#line 558 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                PackageNode a = (PackageNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 40:  package_declaration_opt ::=
            //
            case 40: {
                 setResult(null);                 break;
            } 

            //
            // Rule 41:  import_declarations_opt ::= import_declarations$a
            //
            case 41: {
                //#line 565 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 42:  import_declarations_opt ::=
            //
            case 42: {
                 setResult(new TypedList(new LinkedList(), Import.class, false));                 break;
            } 

            //
            // Rule 43:  type_declarations_opt ::= type_declarations$a
            //
            case 43: {
                //#line 572 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 44:  type_declarations_opt ::=
            //
            case 44: {
                 setResult(new TypedList(new LinkedList(), TopLevelDecl.class, false));                 break;
            } 

            //
            // Rule 45:  import_declarations ::= import_declaration$a
            //
            case 45: {
                //#line 579 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Import a = (Import) getRhsSym(1); List l = new TypedList(new LinkedList(), Import.class, false); 
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 46:  import_declarations ::= import_declarations$a import_declaration$b
            //
            case 46: {
                //#line 583 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 583 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Import b = (Import) getRhsSym(2); setResult(a); 
           a.add(b);                 break;
            } 
            //
            // Rule 47:  type_declarations ::= type_declaration$a
            //
            case 47: {
                //#line 589 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), TopLevelDecl.class, false); 
           if (a != null)
               l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 48:  type_declarations ::= type_declarations$a type_declaration$b
            //
            case 48: {
                //#line 594 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 594 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl b = (ClassDecl) getRhsSym(2); setResult(a);
           if (b != null)
               a.add(b);                 break;
            } 
            //
            // Rule 49:  package_declaration ::= PACKAGE name$a SEMICOLON
            //
            case 49: {
                //#line 601 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(2); setResult(a.toPackage());                 break;
            } 

            //
            // Rule 50:  import_declaration ::= single_type_import_declaration$a
            //
            case 50: {
                //#line 606 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Import a = (Import) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 51:  import_declaration ::= type_import_on_demand_declaration$a
            //
            case 51: {
                //#line 608 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Import a = (Import) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 52:  single_type_import_declaration ::= IMPORT$a qualified_name$b SEMICOLON$c
            //
            case 52: {
                //#line 613 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 613 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name b = (Name) getRhsSym(2);
                //#line 613 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(3); setResult(nf.Import(pos(a, c), Import.CLASS, b.toString()));                 break;
            } 

            //
            // Rule 53:  type_import_on_demand_declaration ::= IMPORT$a name$b DOT MULT SEMICOLON$c
            //
            case 53: {
                //#line 618 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 618 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name b = (Name) getRhsSym(2);
                //#line 618 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(5); setResult(nf.Import(pos(a, c), Import.PACKAGE, b.toString()));                 break;
            } 

            //
            // Rule 54:  type_declaration ::= class_declaration$a
            //
            case 54: {
                //#line 623 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 55:  type_declaration ::= interface_declaration$a
            //
            case 55: {
                //#line 625 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 56:  type_declaration ::= SEMICOLON
            //
            case 56: {
                 setResult(null);                 break;
            } 

            //
            // Rule 57:  modifiers_opt ::=
            //
            case 57: {
                 setResult(Flags.NONE);                 break;
            } 

            //
            // Rule 58:  modifiers_opt ::= modifiers$a
            //
            case 58: {
                //#line 635 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 59:  modifiers ::= modifier$a
            //
            case 59: {
                //#line 640 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 60:  modifiers ::= modifiers$a modifier$b
            //
            case 60: {
                //#line 642 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 642 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags b = (Flags) getRhsSym(2); if (a.intersects(b)) eq.enqueue(0, "Duplicate modifiers", pos());
           setResult(a.set(b));                 break;
            } 
            //
            // Rule 61:  modifier ::= PUBLIC
            //
            case 61: {
                 setResult(Flags.PUBLIC);                 break;
            } 

            //
            // Rule 62:  modifier ::= PROTECTED
            //
            case 62: {
                 setResult(Flags.PROTECTED);                 break;
            } 

            //
            // Rule 63:  modifier ::= PRIVATE
            //
            case 63: {
                 setResult(Flags.PRIVATE);                 break;
            } 

            //
            // Rule 64:  modifier ::= STATIC
            //
            case 64: {
                 setResult(Flags.STATIC);                 break;
            } 

            //
            // Rule 65:  modifier ::= ABSTRACT
            //
            case 65: {
                 setResult(Flags.ABSTRACT);                 break;
            } 

            //
            // Rule 66:  modifier ::= FINAL
            //
            case 66: {
                 setResult(Flags.FINAL);                 break;
            } 

            //
            // Rule 67:  modifier ::= NATIVE
            //
            case 67: {
                 setResult(Flags.NATIVE);                 break;
            } 

            //
            // Rule 68:  modifier ::= SYNCHRONIZED
            //
            case 68: {
                 setResult(Flags.SYNCHRONIZED);                 break;
            } 

            //
            // Rule 69:  modifier ::= TRANSIENT
            //
            case 69: {
                 setResult(Flags.TRANSIENT);                 break;
            } 

            //
            // Rule 70:  modifier ::= VOLATILE
            //
            case 70: {
                 setResult(Flags.VOLATILE);                 break;
            } 

            //
            // Rule 71:  modifier ::= STRICTFP
            //
            case 71: {
                 setResult(Flags.STRICTFP);                 break;
            } 

            //
            // Rule 72:  class_declaration ::= modifiers_opt$a CLASS$n IDENTIFIER$b super_opt$c interfaces_opt$d class_body$e
            //
            case 72: {
                //#line 676 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 676 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(2);
                //#line 676 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3);
                //#line 677 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode c = (TypeNode) getRhsSym(4);
                //#line 677 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List d = (List) getRhsSym(5);
                //#line 677 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassBody e = (ClassBody) getRhsSym(6); setResult(nf.ClassDecl(pos(n, e),
            a, b.toString(), c, d, e));                 break;
            } 
            //
            // Rule 73:  super ::= EXTENDS class_type$a
            //
            case 73: {
                //#line 683 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 74:  super_opt ::=
            //
            case 74: {
                 setResult(null);                 break;
            }

            //
            // Rule 75:  super_opt ::= super$a
            //
            case 75: {
                //#line 689 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 76:  interfaces ::= IMPLEMENTS interface_type_list$a
            //
            case 76: {
                //#line 694 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 77:  interfaces_opt ::=
            //
            case 77: {
                 setResult(new TypedList(new LinkedList(), TypeNode.class, false));                 break;
            } 

            //
            // Rule 78:  interfaces_opt ::= interfaces$a
            //
            case 78: {
                //#line 700 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 79:  interface_type_list ::= interface_type$a
            //
            case 79: {
                //#line 705 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); List l = new TypedList(new LinkedList(), TypeNode.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 80:  interface_type_list ::= interface_type_list$a COMMA interface_type$b
            //
            case 80: {
                //#line 709 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 709 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode b = (TypeNode) getRhsSym(3); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 81:  class_body ::= LBRACE$n class_body_declarations_opt$a RBRACE$b
            //
            case 81: {
                //#line 715 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 715 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 715 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3); setResult(nf.ClassBody(pos(n, b), a));                 break;
            } 

            //
            // Rule 82:  class_body_declarations_opt ::=
            //
            case 82: {
                 setResult(new TypedList(new LinkedList(), ClassMember.class, false));                 break;
            } 

            //
            // Rule 83:  class_body_declarations_opt ::= class_body_declarations$a
            //
            case 83: {
                //#line 721 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 84:  class_body_declarations ::= class_body_declaration$a
            //
            case 84: {
                //#line 726 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 85:  class_body_declarations ::= class_body_declarations$a class_body_declaration$b
            //
            case 85: {
                //#line 728 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 728 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2); setResult(a);
           a.addAll(b);                 break;
            } 
            //
            // Rule 86:  class_body_declaration ::= class_member_declaration$a
            //
            case 86: {
                //#line 734 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 87:  class_body_declaration ::= static_initializer$a
            //
            case 87: {
                //#line 736 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(nf.Initializer(pos(a), Flags.STATIC, a));
           setResult(l);                 break;
            } 
            //
            // Rule 88:  class_body_declaration ::= constructor_declaration$a
            //
            case 88: {
                //#line 740 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ConstructorDecl a = (ConstructorDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 89:  class_body_declaration ::= block$a
            //
            case 89: {
                //#line 744 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(nf.Initializer(pos(a), Flags.NONE, a));
           setResult(l);                 break;
            } 
            //
            // Rule 90:  class_body_declaration ::= SEMICOLON
            //
            case 90: {
                 List l = new TypedList(new LinkedList(), ClassMember.class, false);
           setResult(l);                 break;
            } 
            //
            // Rule 91:  class_body_declaration ::= error SEMICOLON
            //
            case 91: {
                 List l = new TypedList(new LinkedList(), ClassMember.class, false);
           setResult(l);                 break;
            } 
            //
            // Rule 92:  class_body_declaration ::= error LBRACE
            //
            case 92: {
                 List l = new TypedList(new LinkedList(), ClassMember.class, false);
           setResult(l);                 break;
            } 
            //
            // Rule 93:  class_member_declaration ::= field_declaration$a
            //
            case 93: {
                //#line 760 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 94:  class_member_declaration ::= method_declaration$a
            //
            case 94: {
                //#line 762 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                MethodDecl a = (MethodDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 95:  class_member_declaration ::= modifiers_opt$a CLASS$n IDENTIFIER$b super_opt$c interfaces_opt$d class_body$e
            //
            case 95: {
                //#line 767 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 767 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(2);
                //#line 767 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3);
                //#line 768 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode c = (TypeNode) getRhsSym(4);
                //#line 768 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List d = (List) getRhsSym(5);
                //#line 768 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassBody e = (ClassBody) getRhsSym(6); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(nf.ClassDecl(pos(n, e),
                    a, b.toString(), c, d, e));
           setResult(l);                 break;
            } 
            //
            // Rule 96:  class_member_declaration ::= interface_declaration$a
            //
            case 96: {
                //#line 773 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 97:  field_declaration ::= modifiers_opt$a type$b variable_declarators$c SEMICOLON$e
            //
            case 97: {
                //#line 782 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 782 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode b = (TypeNode) getRhsSym(2);
                //#line 782 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(3);
                //#line 782 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken e = (IToken) getRhsIToken(4); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           for (Iterator i = c.iterator(); i.hasNext(); ) {
               VarDeclarator d = (VarDeclarator) i.next();
               l.add(nf.FieldDecl(pos(b, e),
                                         a, array(b, d.dims),
                                         d.name, d.init));
           }
           setResult(l);                 break;
            } 
            //
            // Rule 98:  variable_declarators ::= variable_declarator$a
            //
            case 98: {
                //#line 794 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator a = (VarDeclarator) getRhsSym(1); List l = new TypedList(new LinkedList(), VarDeclarator.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 99:  variable_declarators ::= variable_declarators$a COMMA variable_declarator$b
            //
            case 99: {
                //#line 798 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 798 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator b = (VarDeclarator) getRhsSym(3); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 100:  variable_declarator ::= variable_declarator_id$a
            //
            case 100: {
                //#line 804 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator a = (VarDeclarator) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 101:  variable_declarator ::= variable_declarator_id$a EQ variable_initializer$b
            //
            case 101: {
                //#line 806 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator a = (VarDeclarator) getRhsSym(1);
                //#line 806 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(a);
           a.init = b;                 break;
            } 
            //
            // Rule 102:  variable_declarator_id ::= IDENTIFIER$a
            //
            case 102: {
                //#line 812 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(new VarDeclarator(pos(a),
                        nf.Id(pos(a), a.toString())));                 break;
            } 
            //
            // Rule 103:  variable_declarator_id ::= variable_declarator_id$a LBRACK RBRACK
            //
            case 103: {
                //#line 815 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator a = (VarDeclarator) getRhsSym(1); setResult(a);
           a.dims++;                 break;
            } 
            //
            // Rule 104:  variable_initializer ::= expression$a
            //
            case 104: {
                //#line 821 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 105:  variable_initializer ::= array_initializer$a
            //
            case 105: {
                //#line 823 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ArrayInit a = (ArrayInit) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 106:  method_declaration ::= method_header$a method_body$b
            //
            case 106: {
                //#line 830 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                MethodDecl a = (MethodDecl) getRhsSym(1);
                //#line 830 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block b = (Block) getRhsSym(2); setResult((MethodDecl) a.body(b));                 break;
            } 

            //
            // Rule 107:  method_header ::= modifiers_opt$a type$b IDENTIFIER$c LPAREN formal_parameter_list_opt$d RPAREN$g dims_opt$e throws_opt$f
            //
            case 107: {
                //#line 835 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 835 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode b = (TypeNode) getRhsSym(2);
                //#line 835 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(3);
                //#line 836 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List d = (List) getRhsSym(5);
                //#line 836 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken g = (IToken) getRhsIToken(6);
                //#line 836 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer e = (Integer) getRhsSym(7);
                //#line 836 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List f = (List) getRhsSym(8); setResult(nf.MethodDecl(pos(b, g, c), a,
            array(b, e.intValue()), c.toString(),
            d, f, null));                 break;
            } 
            //
            // Rule 108:  method_header ::= modifiers_opt$a VOID$b IDENTIFIER$c LPAREN formal_parameter_list_opt$d RPAREN$g throws_opt$f
            //
            case 108: {
                //#line 840 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 840 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(2);
                //#line 840 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(3);
                //#line 841 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List d = (List) getRhsSym(5);
                //#line 841 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken g = (IToken) getRhsIToken(6);
                //#line 841 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List f = (List) getRhsSym(7); setResult(nf.MethodDecl(pos(b, g, c), a,
            nf.CanonicalTypeNode(pos(b),
            ts.Void()), c.toString(), d, f, null));                 break;
            } 
            //
            // Rule 109:  formal_parameter_list_opt ::=
            //
            case 109: {
                 setResult(new TypedList(new LinkedList(), Formal.class, false));                 break;
            } 

            //
            // Rule 110:  formal_parameter_list_opt ::= formal_parameter_list$a
            //
            case 110: {
                //#line 849 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 111:  formal_parameter_list ::= formal_parameter$a
            //
            case 111: {
                //#line 854 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Formal a = (Formal) getRhsSym(1); List l = new TypedList(new LinkedList(), Formal.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 112:  formal_parameter_list ::= formal_parameter_list$a COMMA formal_parameter$b
            //
            case 112: {
                //#line 858 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 858 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Formal b = (Formal) getRhsSym(3); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 113:  formal_parameter ::= type$a variable_declarator_id$b
            //
            case 113: {
                //#line 864 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1);
                //#line 864 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator b = (VarDeclarator) getRhsSym(2); setResult(nf.Formal(pos(a, b, b), Flags.NONE,
                                     array(a, b.dims), b.name));                 break;
            } 
            //
            // Rule 114:  formal_parameter ::= FINAL type$a variable_declarator_id$b
            //
            case 114: {
                //#line 867 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 867 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                VarDeclarator b = (VarDeclarator) getRhsSym(3); setResult(nf.Formal(pos(a, b, b), Flags.FINAL,
                 array(a, b.dims), b.name));                 break;
            } 
            //
            // Rule 115:  throws_opt ::=
            //
            case 115: {
                 setResult(new TypedList(new LinkedList(), TypeNode.class, false));                 break;
            } 

            //
            // Rule 116:  throws_opt ::= throws$a
            //
            case 116: {
                //#line 874 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 117:  throws ::= THROWS class_type_list$a
            //
            case 117: {
                //#line 879 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 118:  class_type_list ::= class_type$a
            //
            case 118: {
                //#line 884 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1); List l = new TypedList(new LinkedList(), TypeNode.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 119:  class_type_list ::= class_type_list$a COMMA class_type$b
            //
            case 119: {
                //#line 888 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 888 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode b = (TypeNode) getRhsSym(3); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 120:  method_body ::= block$a
            //
            case 120: {
                //#line 894 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 121:  method_body ::= SEMICOLON
            //
            case 121: {
                 setResult(null);                 break;
            } 

            //
            // Rule 122:  static_initializer ::= STATIC block$a
            //
            case 122: {
                //#line 903 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 123:  constructor_declaration ::= modifiers_opt$m simple_name$a LPAREN formal_parameter_list_opt$b RPAREN throws_opt$c constructor_body$d
            //
            case 123: {
                //#line 910 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags m = (Flags) getRhsSym(1);
                //#line 910 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(2);
                //#line 910 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(4);
                //#line 911 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(6);
                //#line 911 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block d = (Block) getRhsSym(7); setResult(nf.ConstructorDecl(pos(getRhsIToken(1), d), m, a.toString(), b,
            c, d));                 break;
            } 
            //
            // Rule 124:  constructor_body ::= LBRACE$n explicit_constructor_invocation$a block_statements$b RBRACE$d
            //
            case 124: {
                //#line 917 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 917 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ConstructorCall a = (ConstructorCall) getRhsSym(2);
                //#line 917 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 917 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(4); List l = new TypedList(new LinkedList(), Stmt.class, false);
           l.add(a);
           l.addAll(b);
           setResult(nf.Block(pos(n, d), l));                 break;
            } 
            //
            // Rule 125:  constructor_body ::= LBRACE$n explicit_constructor_invocation$a RBRACE$d
            //
            case 125: {
                //#line 922 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 922 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ConstructorCall a = (ConstructorCall) getRhsSym(2);
                //#line 922 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.Block(pos(n, d), a));                 break;
            } 

            //
            // Rule 126:  constructor_body ::= LBRACE$n block_statements$a RBRACE$d
            //
            case 126: {
                //#line 924 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 924 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 924 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); a.add(0, nf.SuperCall(pos(n, d), 
            Collections.EMPTY_LIST));
           setResult(nf.Block(pos(n, d), a));                 break;
            } 
            //
            // Rule 127:  constructor_body ::= LBRACE$n RBRACE$d
            //
            case 127: {
                //#line 928 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 928 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(2); setResult(nf.Block(pos(n, d),
            nf.SuperCall(pos(n, d),
            Collections.EMPTY_LIST)));                 break;
            } 
            //
            // Rule 128:  explicit_constructor_invocation ::= THIS$a LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            //
            case 128: {
                //#line 935 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 935 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 935 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(5); setResult(nf.ThisCall(pos(a, c), b));                 break;
            } 

            //
            // Rule 129:  explicit_constructor_invocation ::= SUPER$a LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            //
            case 129: {
                //#line 937 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 937 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 937 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(5); setResult(nf.SuperCall(pos(a, c), b));                 break;
            } 

            //
            // Rule 130:  explicit_constructor_invocation ::= primary$a DOT THIS$n LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            //
            case 130: {
                //#line 939 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 939 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3);
                //#line 939 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(5);
                //#line 939 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(7); setResult(nf.ThisCall(pos(a, c, n), a, b));                 break;
            } 

            //
            // Rule 131:  explicit_constructor_invocation ::= primary$a DOT SUPER$n LPAREN argument_list_opt$b RPAREN SEMICOLON$c
            //
            case 131: {
                //#line 941 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 941 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3);
                //#line 941 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(5);
                //#line 941 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken c = (IToken) getRhsIToken(7); setResult(nf.SuperCall(pos(a, c, n), a, b));                 break;
            } 

            //
            // Rule 132:  interface_declaration ::= modifiers_opt$a INTERFACE$n IDENTIFIER$b extends_interfaces_opt$c interface_body$d
            //
            case 132: {
                //#line 950 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Flags a = (Flags) getRhsSym(1);
                //#line 950 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(2);
                //#line 950 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3);
                //#line 951 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(4);
                //#line 951 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassBody d = (ClassBody) getRhsSym(5); setResult(nf.ClassDecl(
                pos(n, d), a.Interface(),
                    b.toString(), null, c, d));                 break;
            } 
            //
            // Rule 133:  extends_interfaces_opt ::=
            //
            case 133: {
                 setResult(new TypedList(new LinkedList(), TypeNode.class, false));                 break;
            } 

            //
            // Rule 134:  extends_interfaces_opt ::= extends_interfaces$a
            //
            case 134: {
                //#line 959 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 135:  extends_interfaces ::= EXTENDS interface_type$a
            //
            case 135: {
                //#line 964 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2); List l = new TypedList(new LinkedList(), TypeNode.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 136:  extends_interfaces ::= extends_interfaces$a COMMA interface_type$b
            //
            case 136: {
                //#line 968 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 968 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode b = (TypeNode) getRhsSym(3); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 137:  interface_body ::= LBRACE$n interface_member_declarations_opt$a RBRACE$d
            //
            case 137: {
                //#line 974 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 974 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 974 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.ClassBody(pos(n, d), a));                 break;
            } 

            //
            // Rule 138:  interface_member_declarations_opt ::=
            //
            case 138: {
                 setResult(new TypedList(new LinkedList(), ClassMember.class, false));                 break;
            } 

            //
            // Rule 139:  interface_member_declarations_opt ::= interface_member_declarations$a
            //
            case 139: {
                //#line 980 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 140:  interface_member_declarations ::= interface_member_declaration$a
            //
            case 140: {
                //#line 985 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 141:  interface_member_declarations ::= interface_member_declarations$a interface_member_declaration$b
            //
            case 141: {
                //#line 987 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 987 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2); setResult(a);
           a.addAll(b);                 break;
            } 
            //
            // Rule 142:  interface_member_declaration ::= constant_declaration$a
            //
            case 142: {
                //#line 993 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 143:  interface_member_declaration ::= abstract_method_declaration$a
            //
            case 143: {
                //#line 995 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                MethodDecl a = (MethodDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 144:  interface_member_declaration ::= class_declaration$a
            //
            case 144: {
                //#line 999 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 145:  interface_member_declaration ::= interface_declaration$a
            //
            case 145: {
                //#line 1003 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), ClassMember.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 146:  interface_member_declaration ::= SEMICOLON
            //
            case 146: {
                 setResult(Collections.EMPTY_LIST);                 break;
            } 

            //
            // Rule 147:  constant_declaration ::= field_declaration$a
            //
            case 147: {
                //#line 1012 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 148:  abstract_method_declaration ::= method_header$a SEMICOLON
            //
            case 148: {
                //#line 1017 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                MethodDecl a = (MethodDecl) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 149:  array_initializer ::= LBRACE$n variable_initializers$a COMMA RBRACE$d
            //
            case 149: {
                //#line 1024 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1024 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 1024 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(4); setResult(nf.ArrayInit(pos(n, d), a));                 break;
            } 

            //
            // Rule 150:  array_initializer ::= LBRACE$n variable_initializers$a RBRACE$d
            //
            case 150: {
                //#line 1026 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1026 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 1026 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.ArrayInit(pos(n, d), a));                 break;
            } 

            //
            // Rule 151:  array_initializer ::= LBRACE$n COMMA RBRACE$d
            //
            case 151: {
                //#line 1028 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1028 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.ArrayInit(pos(n, d)));                 break;
            } 

            //
            // Rule 152:  array_initializer ::= LBRACE$n RBRACE$d
            //
            case 152: {
                //#line 1030 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1030 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(2); setResult(nf.ArrayInit(pos(n, d)));                 break;
            } 

            //
            // Rule 153:  variable_initializers ::= variable_initializer$a
            //
            case 153: {
                //#line 1035 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); List l = new TypedList(new LinkedList(), Expr.class, false);
       l.add(a);
       setResult(l);                 break;
            } 
            //
            // Rule 154:  variable_initializers ::= variable_initializers$a COMMA variable_initializer$b
            //
            case 154: {
                //#line 1039 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1039 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(a); a.add(b);                 break;
            } 

            //
            // Rule 155:  block ::= LBRACE$n block_statements_opt$a RBRACE$d
            //
            case 155: {
                //#line 1046 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1046 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 1046 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.Block(pos(n, d), a));                 break;
            } 

            //
            // Rule 156:  block ::= error RBRACE$d
            //
            case 156: {
                //#line 1049 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(2); setResult(nf.Block(pos(d),
                                    Collections.EMPTY_LIST));                 break;
            } 
            //
            // Rule 157:  block_statements_opt ::=
            //
            case 157: {
                 setResult(new TypedList(new LinkedList(), Stmt.class, false));                 break;
            } 

            //
            // Rule 158:  block_statements_opt ::= block_statements$a
            //
            case 158: {
                //#line 1056 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 159:  block_statements ::= block_statement$a
            //
            case 159: {
                //#line 1061 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); List l = new TypedList(new LinkedList(), Stmt.class, false);
           l.addAll(a);
           setResult(l);                 break;
            } 
            //
            // Rule 160:  block_statements ::= block_statements$a block_statement$b
            //
            case 160: {
                //#line 1065 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1065 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2); setResult(a);
           a.addAll(b);                 break;
            } 
            //
            // Rule 161:  block_statement ::= local_variable_declaration_statement$a
            //
            case 161: {
                //#line 1071 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 162:  block_statement ::= statement$a
            //
            case 162: {
                //#line 1073 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt a = (Stmt) getRhsSym(1); List l = new TypedList(new LinkedList(), Stmt.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 163:  block_statement ::= class_declaration$a
            //
            case 163: {
                //#line 1077 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassDecl a = (ClassDecl) getRhsSym(1); List l = new TypedList(new LinkedList(), Stmt.class, false);
           l.add(nf.LocalClassDecl(pos(a), a));
           setResult(l);                 break;
            } 
            //
            // Rule 164:  local_variable_declaration_statement ::= local_variable_declaration$a SEMICOLON
            //
            case 164: {
                //#line 1084 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 165:  local_variable_declaration ::= type$a variable_declarators$b
            //
            case 165: {
                //#line 1089 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1);
                //#line 1089 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2); setResult(variableDeclarators(a, b, Flags.NONE));                 break;
            } 

            //
            // Rule 166:  local_variable_declaration ::= FINAL type$a variable_declarators$b
            //
            case 166: {
                //#line 1091 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1091 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3); setResult(variableDeclarators(a, b, Flags.FINAL));                 break;
            } 

            //
            // Rule 167:  statement ::= statement_without_trailing_substatement$a
            //
            case 167: {
                //#line 1096 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt a = (Stmt) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 168:  statement ::= labeled_statement$a
            //
            case 168: {
                //#line 1098 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Labeled a = (Labeled) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 169:  statement ::= if_then_statement$a
            //
            case 169: {
                //#line 1100 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                If a = (If) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 170:  statement ::= if_then_else_statement$a
            //
            case 170: {
                //#line 1102 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                If a = (If) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 171:  statement ::= while_statement$a
            //
            case 171: {
                //#line 1104 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                While a = (While) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 172:  statement ::= for_statement$a
            //
            case 172: {
                //#line 1106 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                For a = (For) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 173:  statement ::= error SEMICOLON$a
            //
            case 173: {
                //#line 1108 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(2); setResult(nf.Empty(pos(a)));                 break;
            } 

            //
            // Rule 174:  statement_no_short_if ::= statement_without_trailing_substatement$a
            //
            case 174: {
                //#line 1113 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt a = (Stmt) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 175:  statement_no_short_if ::= labeled_statement_no_short_if$a
            //
            case 175: {
                //#line 1115 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Labeled a = (Labeled) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 176:  statement_no_short_if ::= if_then_else_statement_no_short_if$a
            //
            case 176: {
                //#line 1117 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                If a = (If) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 177:  statement_no_short_if ::= while_statement_no_short_if$a
            //
            case 177: {
                //#line 1119 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                While a = (While) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 178:  statement_no_short_if ::= for_statement_no_short_if$a
            //
            case 178: {
                //#line 1121 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                For a = (For) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 179:  statement_without_trailing_substatement ::= block$a
            //
            case 179: {
                //#line 1126 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 180:  statement_without_trailing_substatement ::= empty_statement$a
            //
            case 180: {
                //#line 1128 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Empty a = (Empty) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 181:  statement_without_trailing_substatement ::= expression_statement$a
            //
            case 181: {
                //#line 1130 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt a = (Stmt) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 182:  statement_without_trailing_substatement ::= switch_statement$a
            //
            case 182: {
                //#line 1132 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Switch a = (Switch) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 183:  statement_without_trailing_substatement ::= do_statement$a
            //
            case 183: {
                //#line 1134 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Do a = (Do) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 184:  statement_without_trailing_substatement ::= break_statement$a
            //
            case 184: {
                //#line 1136 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Branch a = (Branch) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 185:  statement_without_trailing_substatement ::= continue_statement$a
            //
            case 185: {
                //#line 1138 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Branch a = (Branch) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 186:  statement_without_trailing_substatement ::= return_statement$a
            //
            case 186: {
                //#line 1140 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Return a = (Return) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 187:  statement_without_trailing_substatement ::= synchronized_statement$a
            //
            case 187: {
                //#line 1142 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Synchronized a = (Synchronized) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 188:  statement_without_trailing_substatement ::= throw_statement$a
            //
            case 188: {
                //#line 1144 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Throw a = (Throw) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 189:  statement_without_trailing_substatement ::= try_statement$a
            //
            case 189: {
                //#line 1146 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Try a = (Try) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 190:  statement_without_trailing_substatement ::= assert_statement$a
            //
            case 190: {
                //#line 1148 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Assert a = (Assert) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 191:  empty_statement ::= SEMICOLON$a
            //
            case 191: {
                //#line 1153 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.Empty(pos(a)));                 break;
            } 

            //
            // Rule 192:  labeled_statement ::= IDENTIFIER$a COLON statement$b
            //
            case 192: {
                //#line 1158 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 1158 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(3); setResult(nf.Labeled(pos(a, b),
                                  a.toString(), b));                 break;
            } 
            //
            // Rule 193:  labeled_statement_no_short_if ::= IDENTIFIER$a COLON statement_no_short_if$b
            //
            case 193: {
                //#line 1164 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 1164 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(3); setResult(nf.Labeled(pos(a, b),
                                  a.toString(), b));                 break;
            } 
            //
            // Rule 194:  expression_statement ::= statement_expression$a SEMICOLON$d
            //
            case 194: {
                //#line 1170 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1170 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(2); setResult(nf.Eval(pos(a, d), a));                 break;
            } 

            //
            // Rule 195:  statement_expression ::= assignment$a
            //
            case 195: {
                //#line 1175 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 196:  statement_expression ::= preincrement_expression$a
            //
            case 196: {
                //#line 1177 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 197:  statement_expression ::= predecrement_expression$a
            //
            case 197: {
                //#line 1179 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 198:  statement_expression ::= postincrement_expression$a
            //
            case 198: {
                //#line 1181 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 199:  statement_expression ::= postdecrement_expression$a
            //
            case 199: {
                //#line 1183 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 200:  statement_expression ::= method_invocation$a
            //
            case 200: {
                //#line 1185 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Call a = (Call) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 201:  statement_expression ::= class_instance_creation_expression$a
            //
            case 201: {
                //#line 1187 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 202:  if_then_statement ::= IF$n LPAREN expression$a RPAREN statement$b
            //
            case 202: {
                //#line 1192 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1192 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1192 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(5); setResult(nf.If(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 203:  if_then_else_statement ::= IF$n LPAREN expression$a RPAREN statement_no_short_if$b ELSE statement$c
            //
            case 203: {
                //#line 1197 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1197 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1197 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(5);
                //#line 1198 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt c = (Stmt) getRhsSym(7); setResult(nf.If(pos(n, c), a, b, c));                 break;
            } 

            //
            // Rule 204:  if_then_else_statement_no_short_if ::= IF$n LPAREN expression$a RPAREN statement_no_short_if$b ELSE statement_no_short_if$c
            //
            case 204: {
                //#line 1203 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1203 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1203 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(5);
                //#line 1204 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt c = (Stmt) getRhsSym(7); setResult(nf.If(pos(n, c), a, b, c));                 break;
            } 

            //
            // Rule 205:  switch_statement ::= SWITCH$n LPAREN expression$a RPAREN switch_block$b
            //
            case 205: {
                //#line 1209 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1209 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1209 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(5); setResult(nf.Switch(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 206:  switch_block ::= LBRACE switch_block_statement_groups$a switch_labels$b RBRACE
            //
            case 206: {
                //#line 1214 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2);
                //#line 1214 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3); setResult(a);
           a.addAll(b);                 break;
            } 
            //
            // Rule 207:  switch_block ::= LBRACE switch_block_statement_groups$a RBRACE
            //
            case 207: {
                //#line 1217 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 208:  switch_block ::= LBRACE switch_labels$a RBRACE
            //
            case 208: {
                //#line 1219 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 209:  switch_block ::= LBRACE RBRACE
            //
            case 209: {
                 setResult(new TypedList(new LinkedList(), SwitchElement.class, false));                 break;
            } 

            //
            // Rule 210:  switch_block_statement_groups ::= switch_block_statement_group$a
            //
            case 210: {
                //#line 1226 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 211:  switch_block_statement_groups ::= switch_block_statement_groups$a switch_block_statement_group$b
            //
            case 211: {
                //#line 1228 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1228 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2); setResult(a);
           a.addAll(b);                 break;
            } 
            //
            // Rule 212:  switch_block_statement_group ::= switch_labels$a block_statements$b
            //
            case 212: {
                //#line 1234 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1234 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(2); List l = new TypedList(new LinkedList(), SwitchElement.class, false);
           l.addAll(a); 
           l.add(nf.SwitchBlock(pos(a, b), b));
           setResult(l);                 break;
            } 
            //
            // Rule 213:  switch_labels ::= switch_label$a
            //
            case 213: {
                //#line 1242 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Case a = (Case) getRhsSym(1); List l = new TypedList(new LinkedList(), Case.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 214:  switch_labels ::= switch_labels$a switch_label$b
            //
            case 214: {
                //#line 1246 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1246 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Case b = (Case) getRhsSym(2); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 215:  switch_label ::= CASE$n constant_expression$a COLON$d
            //
            case 215: {
                //#line 1252 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1252 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1252 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.Case(pos(n, d), a));                 break;
            } 

            //
            // Rule 216:  switch_label ::= DEFAULT$n COLON$d
            //
            case 216: {
                //#line 1254 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1254 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(2); setResult(nf.Default(pos(n, d)));                 break;
            } 

            //
            // Rule 217:  while_statement ::= WHILE$n LPAREN expression$a RPAREN statement$b
            //
            case 217: {
                //#line 1260 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1260 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1260 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(5); setResult(nf.While(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 218:  while_statement_no_short_if ::= WHILE$n LPAREN expression$a RPAREN statement_no_short_if$b
            //
            case 218: {
                //#line 1265 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1265 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1265 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt b = (Stmt) getRhsSym(5); setResult(nf.While(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 219:  do_statement ::= DO$n statement$a WHILE LPAREN expression$b RPAREN SEMICOLON$d
            //
            case 219: {
                //#line 1270 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1270 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt a = (Stmt) getRhsSym(2);
                //#line 1270 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(5);
                //#line 1270 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(7); setResult(nf.Do(pos(n, d), a, b));                 break;
            } 

            //
            // Rule 220:  for_statement ::= FOR$n LPAREN for_init_opt$a SEMICOLON expression_opt$b SEMICOLON$e for_update_opt$c RPAREN statement$d
            //
            case 220: {
                //#line 1275 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1275 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(3);
                //#line 1275 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(5);
                //#line 1275 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken e = (IToken) getRhsIToken(6);
                //#line 1276 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(7);
                //#line 1276 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt d = (Stmt) getRhsSym(9); setResult(nf.For(pos(n, e), a, b, c, d));                 break;
            } 

            //
            // Rule 221:  for_statement_no_short_if ::= FOR$n LPAREN for_init_opt$a SEMICOLON expression_opt$b SEMICOLON$e for_update_opt$c RPAREN statement_no_short_if$d
            //
            case 221: {
                //#line 1281 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1281 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(3);
                //#line 1281 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(5);
                //#line 1281 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken e = (IToken) getRhsIToken(6);
                //#line 1282 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(7);
                //#line 1282 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Stmt d = (Stmt) getRhsSym(9); setResult(nf.For(pos(n, e), a, b, c, d));                 break;
            } 

            //
            // Rule 222:  for_init_opt ::=
            //
            case 222: {
                 setResult(new TypedList(new LinkedList(), ForInit.class, false));                 break;
            } 

            //
            // Rule 223:  for_init_opt ::= for_init$a
            //
            case 223: {
                //#line 1288 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 224:  for_init ::= statement_expression_list$a
            //
            case 224: {
                //#line 1293 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 225:  for_init ::= local_variable_declaration$a
            //
            case 225: {
                //#line 1295 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); List l = new TypedList(new LinkedList(), ForInit.class, false);
           l.addAll(a);
           setResult(l);                 break;
            } 
            //
            // Rule 226:  for_update_opt ::=
            //
            case 226: {
                 setResult(new TypedList(new LinkedList(), ForUpdate.class, false));                 break;
            } 

            //
            // Rule 227:  for_update_opt ::= for_update$a
            //
            case 227: {
                //#line 1303 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 228:  for_update ::= statement_expression_list$a
            //
            case 228: {
                //#line 1308 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 229:  statement_expression_list ::= statement_expression$a
            //
            case 229: {
                //#line 1313 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); List l = new TypedList(new LinkedList(), Eval.class, false);
           l.add(nf.Eval(pos(a), a));
           setResult(l);                 break;
            } 
            //
            // Rule 230:  statement_expression_list ::= statement_expression_list$a COMMA statement_expression$b
            //
            case 230: {
                //#line 1317 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1317 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(a);
           a.add(nf.Eval(pos(a, b, b), b));                 break;
            } 
            //
            // Rule 231:  identifier_opt ::=
            //
            case 231: {
                 setResult(null);                 break;
            } 

            //
            // Rule 232:  identifier_opt ::= IDENTIFIER$a
            //
            case 232: {
                //#line 1325 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(new Name(nf, ts, pos(a), 
            nf.Id(pos(a), a.toString())));                 break;
            } 
            //
            // Rule 233:  break_statement ::= BREAK$n identifier_opt$a SEMICOLON$d
            //
            case 233: {
                //#line 1332 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1332 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(2);
                //#line 1332 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); if (a == null)
               setResult(nf.Break(pos(n, d)));
           else
               setResult(nf.Break(pos(n, d), a.toString()));                 break;
            } 
            //
            // Rule 234:  continue_statement ::= CONTINUE$n identifier_opt$a SEMICOLON$d
            //
            case 234: {
                //#line 1341 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1341 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(2);
                //#line 1341 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); if (a == null)
               setResult(nf.Continue(pos(n, d)));
           else
               setResult(nf.Continue(pos(n, d), a.toString()));                 break;
            } 
            //
            // Rule 235:  return_statement ::= RETURN$n expression_opt$a SEMICOLON$d
            //
            case 235: {
                //#line 1349 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1349 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1349 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.Return(pos(n, d), a));                 break;
            } 

            //
            // Rule 236:  throw_statement ::= THROW$n expression$a SEMICOLON$d
            //
            case 236: {
                //#line 1354 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1354 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1354 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.Throw(pos(n, d), a));                 break;
            } 

            //
            // Rule 237:  synchronized_statement ::= SYNCHRONIZED$n LPAREN expression$a RPAREN block$b
            //
            case 237: {
                //#line 1359 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1359 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(3);
                //#line 1359 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block b = (Block) getRhsSym(5); setResult(nf.Synchronized(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 238:  try_statement ::= TRY$n block$a catches$b
            //
            case 238: {
                //#line 1364 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1364 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(2);
                //#line 1364 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3); setResult(nf.Try(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 239:  try_statement ::= TRY$n block$a catches_opt$b finally$c
            //
            case 239: {
                //#line 1366 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1366 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(2);
                //#line 1366 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 1366 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block c = (Block) getRhsSym(4); setResult(nf.Try(pos(n, c), a, b, c));                 break;
            } 

            //
            // Rule 240:  catches_opt ::=
            //
            case 240: {
                 setResult(new TypedList(new LinkedList(), Catch.class, false));                 break;
            } 

            //
            // Rule 241:  catches_opt ::= catches$a
            //
            case 241: {
                //#line 1372 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 242:  catches ::= catch_clause$a
            //
            case 242: {
                //#line 1377 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Catch a = (Catch) getRhsSym(1); List l = new TypedList(new LinkedList(), Catch.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 243:  catches ::= catches$a catch_clause$b
            //
            case 243: {
                //#line 1381 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1381 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Catch b = (Catch) getRhsSym(2); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 244:  catch_clause ::= CATCH$n LPAREN formal_parameter$a RPAREN block$b
            //
            case 244: {
                //#line 1387 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1387 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Formal a = (Formal) getRhsSym(3);
                //#line 1387 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block b = (Block) getRhsSym(5); setResult(nf.Catch(pos(n, b), a, b));                 break;
            } 

            //
            // Rule 245:  finally ::= FINALLY block$a
            //
            case 245: {
                //#line 1392 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Block a = (Block) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 246:  assert_statement ::= ASSERT$x expression$a SEMICOLON$d
            //
            case 246: {
                //#line 1398 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken x = (IToken) getRhsIToken(1);
                //#line 1398 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1398 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(3); setResult(nf.Assert(pos(x, d), a));                 break;
            } 

            //
            // Rule 247:  assert_statement ::= ASSERT$x expression$a COLON expression$b SEMICOLON$d
            //
            case 247: {
                //#line 1400 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken x = (IToken) getRhsIToken(1);
                //#line 1400 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1400 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(4);
                //#line 1400 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(5); setResult(nf.Assert(pos(x, d), a, b));                 break;
            } 

            //
            // Rule 248:  primary ::= primary_no_new_array$a
            //
            case 248: {
                //#line 1407 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 249:  primary ::= array_creation_expression$a
            //
            case 249: {
                //#line 1409 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                NewArray a = (NewArray) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 250:  primary_no_new_array ::= literal$a
            //
            case 250: {
                //#line 1414 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                polyglot.ast.Lit a = (polyglot.ast.Lit) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 251:  primary_no_new_array ::= THIS$a
            //
            case 251: {
                //#line 1416 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1); setResult(nf.This(pos(a)));                 break;
            } 

            //
            // Rule 252:  primary_no_new_array ::= LPAREN expression$a RPAREN
            //
            case 252: {
                //#line 1418 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(a);                 break;
            } 

            //
            // Rule 253:  primary_no_new_array ::= class_instance_creation_expression$a
            //
            case 253: {
                //#line 1420 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 254:  primary_no_new_array ::= field_access$a
            //
            case 254: {
                //#line 1422 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Field a = (Field) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 255:  primary_no_new_array ::= method_invocation$a
            //
            case 255: {
                //#line 1424 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Call a = (Call) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 256:  primary_no_new_array ::= array_access$a
            //
            case 256: {
                //#line 1426 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ArrayAccess a = (ArrayAccess) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 257:  primary_no_new_array ::= primitive_type$a DOT CLASS$n
            //
            case 257: {
                //#line 1428 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1);
                //#line 1428 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3); setResult(nf.ClassLit(pos(a, n, n), a));                 break;
            } 

            //
            // Rule 258:  primary_no_new_array ::= VOID$a DOT CLASS$n
            //
            case 258: {
                //#line 1430 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 1430 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3); setResult(nf.ClassLit(pos(a, n, n), 
            nf.CanonicalTypeNode(pos(a),
                                        ts.Void())));                 break;
            } 
            //
            // Rule 259:  primary_no_new_array ::= array_type$a DOT CLASS$n
            //
            case 259: {
                //#line 1434 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(1);
                //#line 1434 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3); setResult(nf.ClassLit(pos(a, n, n), a));                 break;
            } 

            //
            // Rule 260:  primary_no_new_array ::= name$a DOT CLASS$n
            //
            case 260: {
                //#line 1436 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1436 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3); setResult(nf.ClassLit(pos(getRhsIToken(1), n, n), a.toType()));                 break;
            } 

            //
            // Rule 261:  primary_no_new_array ::= name$a DOT THIS$n
            //
            case 261: {
                //#line 1438 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1438 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3); setResult(nf.This(pos(getRhsIToken(1), n, n), a.toType()));                 break;
            } 

            //
            // Rule 262:  class_instance_creation_expression ::= NEW$n class_type$a LPAREN argument_list_opt$b RPAREN$d
            //
            case 262: {
                //#line 1443 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1443 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1443 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(4);
                //#line 1443 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(5); setResult(nf.New(pos(n, d), a, b));                 break;
            } 

            //
            // Rule 263:  class_instance_creation_expression ::= NEW$n class_type$a LPAREN argument_list_opt$b RPAREN class_body$c
            //
            case 263: {
                //#line 1445 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1445 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1445 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(4);
                //#line 1445 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassBody c = (ClassBody) getRhsSym(6); setResult(nf.New(pos(n, c), a, b, c));                 break;
            } 

            //
            // Rule 264:  class_instance_creation_expression ::= primary$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN$d
            //
            case 264: {
                //#line 1447 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1447 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name b = (Name) getRhsSym(4);
                //#line 1447 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(6);
                //#line 1447 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(7); setResult(nf.New(pos(a, d), a,
			      b.toType(), c));                 break;
            } 
            //
            // Rule 265:  class_instance_creation_expression ::= primary$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN class_body$d
            //
            case 265: {
                //#line 1450 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1450 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name b = (Name) getRhsSym(4);
                //#line 1450 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(6);
                //#line 1450 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassBody d = (ClassBody) getRhsSym(8); setResult(nf.New(pos(a, d), a,
			      b.toType(), c, d));                 break;
            } 
            //
            // Rule 266:  class_instance_creation_expression ::= name$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN$d
            //
            case 266: {
                //#line 1453 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1453 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name b = (Name) getRhsSym(4);
                //#line 1453 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(6);
                //#line 1453 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(7); setResult(nf.New(pos(a, d), a.toExpr(),
			      b.toType(), c));                 break;
            } 
            //
            // Rule 267:  class_instance_creation_expression ::= name$a DOT NEW simple_name$b LPAREN argument_list_opt$c RPAREN class_body$d
            //
            case 267: {
                //#line 1456 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1456 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name b = (Name) getRhsSym(4);
                //#line 1456 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(6);
                //#line 1456 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ClassBody d = (ClassBody) getRhsSym(8); setResult(nf.New(pos(a, d), a.toExpr(),
			      b.toType(), c, d));                 break;
            } 
            //
            // Rule 268:  argument_list_opt ::=
            //
            case 268: {
                 setResult(new TypedList(new LinkedList(), Expr.class, false));                 break;
            } 

            //
            // Rule 269:  argument_list_opt ::= argument_list$a
            //
            case 269: {
                //#line 1463 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 270:  argument_list ::= expression$a
            //
            case 270: {
                //#line 1468 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); List l = new TypedList(new LinkedList(), Expr.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 271:  argument_list ::= argument_list$a COMMA expression$b
            //
            case 271: {
                //#line 1472 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1472 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 272:  array_creation_expression ::= NEW$n primitive_type$a dim_exprs$b dims_opt$c
            //
            case 272: {
                //#line 1478 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1478 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1478 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 1478 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer c = (Integer) getRhsSym(4); setResult(nf.NewArray(pos(n, b), a, b,
            c.intValue()));                 break;
            } 
            //
            // Rule 273:  array_creation_expression ::= NEW$n class_or_interface_type$a dim_exprs$b dims_opt$c
            //
            case 273: {
                //#line 1481 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1481 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1481 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 1481 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer c = (Integer) getRhsSym(4); setResult(nf.NewArray(pos(n, b), a, b, 
            c.intValue()));                 break;
            } 
            //
            // Rule 274:  array_creation_expression ::= NEW$n primitive_type$a dims$b array_initializer$c
            //
            case 274: {
                //#line 1484 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1484 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1484 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer b = (Integer) getRhsSym(3);
                //#line 1484 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ArrayInit c = (ArrayInit) getRhsSym(4); setResult(nf.NewArray(pos(n, c), a,
            b.intValue(), c));                 break;
            } 
            //
            // Rule 275:  array_creation_expression ::= NEW$n class_or_interface_type$a dims$b array_initializer$c
            //
            case 275: {
                //#line 1487 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1487 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1487 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer b = (Integer) getRhsSym(3);
                //#line 1487 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ArrayInit c = (ArrayInit) getRhsSym(4); setResult(nf.NewArray(pos(n, c), a,
            b.intValue(), c));                 break;
            } 
            //
            // Rule 276:  dim_exprs ::= dim_expr$a
            //
            case 276: {
                //#line 1493 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); List l = new TypedList(new LinkedList(), Expr.class, false);
           l.add(a);
           setResult(l);                 break;
            } 
            //
            // Rule 277:  dim_exprs ::= dim_exprs$a dim_expr$b
            //
            case 277: {
                //#line 1497 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List a = (List) getRhsSym(1);
                //#line 1497 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(2); setResult(a);
           a.add(b);                 break;
            } 
            //
            // Rule 278:  dim_expr ::= LBRACK$x expression$a RBRACK$y
            //
            case 278: {
                //#line 1503 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken x = (IToken) getRhsIToken(1);
                //#line 1503 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1503 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken y = (IToken) getRhsIToken(3); setResult((Expr)a.position(pos(x,y,a)));                 break;
            } 

            //
            // Rule 279:  dims_opt ::=
            //
            case 279: {
                 setResult(new Integer(0));                 break;
            } 

            //
            // Rule 280:  dims_opt ::= dims$a
            //
            case 280: {
                //#line 1509 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer a = (Integer) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 281:  dims ::= LBRACK RBRACK
            //
            case 281: {
                 setResult(new Integer(1));                 break;
            } 

            //
            // Rule 282:  dims ::= dims$a LBRACK RBRACK
            //
            case 282: {
                //#line 1516 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer a = (Integer) getRhsSym(1); setResult(new Integer(a.intValue() + 1));                 break;
            } 

            //
            // Rule 283:  field_access ::= primary$a DOT IDENTIFIER$b
            //
            case 283: {
                //#line 1521 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1521 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3); setResult(nf.Field(pos(a, b, b), a,
            b.toString()));                 break;
            } 
            //
            // Rule 284:  field_access ::= SUPER$n DOT IDENTIFIER$a
            //
            case 284: {
                //#line 1524 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(1);
                //#line 1524 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(3); setResult(nf.Field(pos(a),
            nf.Super(pos(n)),
            a.toString()));                 break;
            } 
            //
            // Rule 285:  field_access ::= name$a DOT SUPER$n DOT IDENTIFIER$b
            //
            case 285: {
                //#line 1528 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1528 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3);
                //#line 1528 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(5); setResult(nf.Field(pos(b),
            nf.Super(pos(n), a.toType()),
            b.toString()));                 break;
            } 
            //
            // Rule 286:  method_invocation ::= name$a LPAREN argument_list_opt$b RPAREN$d
            //
            case 286: {
                //#line 1535 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1535 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List b = (List) getRhsSym(3);
                //#line 1535 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(4); setResult(nf.Call(pos(getRhsIToken(1),d),
            a.prefix == null ? null : a.prefix.toReceiver(),
            a.name, b));                 break;
            } 
            //
            // Rule 287:  method_invocation ::= primary$a DOT IDENTIFIER$b LPAREN argument_list_opt$c RPAREN$d
            //
            case 287: {
                //#line 1539 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1539 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3);
                //#line 1539 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(5);
                //#line 1539 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(6); setResult(nf.Call(pos(b,d), a,
            b.toString(), c));                 break;
            } 
            //
            // Rule 288:  method_invocation ::= SUPER$a DOT IDENTIFIER$b LPAREN argument_list_opt$c RPAREN$d
            //
            case 288: {
                //#line 1542 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken a = (IToken) getRhsIToken(1);
                //#line 1542 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(3);
                //#line 1542 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(5);
                //#line 1542 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(6); setResult(nf.Call(pos(a,d, b),
            nf.Super(pos(a)),
            b.toString(), c));                 break;
            } 
            //
            // Rule 289:  method_invocation ::= name$a DOT SUPER$n DOT IDENTIFIER$b LPAREN argument_list_opt$c RPAREN$d
            //
            case 289: {
                //#line 1546 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1546 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken n = (IToken) getRhsIToken(3);
                //#line 1546 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(5);
                //#line 1546 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                List c = (List) getRhsSym(7);
                //#line 1546 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(8); setResult(nf.Call(pos(b,d),
            nf.Super(pos(n), a.toType()),
            b.toString(), c));                 break;
            } 
            //
            // Rule 290:  array_access ::= name$a LBRACK expression$b RBRACK$d
            //
            case 290: {
                //#line 1553 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1);
                //#line 1553 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3);
                //#line 1553 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(4); setResult(nf.ArrayAccess(pos(a, d), a.toExpr(), b));                 break;
            } 

            //
            // Rule 291:  array_access ::= primary_no_new_array$a LBRACK expression$b RBRACK$d
            //
            case 291: {
                //#line 1555 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1555 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3);
                //#line 1555 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken d = (IToken) getRhsIToken(4); setResult(nf.ArrayAccess(pos(a, d), a, b));                 break;
            } 

            //
            // Rule 292:  postfix_expression ::= primary$a
            //
            case 292: {
                //#line 1560 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 293:  postfix_expression ::= name$a
            //
            case 293: {
                //#line 1562 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1); setResult(a.toExpr());                 break;
            } 

            //
            // Rule 294:  postfix_expression ::= postincrement_expression$a
            //
            case 294: {
                //#line 1564 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 295:  postfix_expression ::= postdecrement_expression$a
            //
            case 295: {
                //#line 1566 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 296:  postincrement_expression ::= postfix_expression$a PLUSPLUS$b
            //
            case 296: {
                //#line 1571 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1571 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(2); setResult(nf.Unary(pos(a,b), a, Unary.POST_INC));                 break;
            } 

            //
            // Rule 297:  postdecrement_expression ::= postfix_expression$a MINUSMINUS$b
            //
            case 297: {
                //#line 1576 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1576 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(2); setResult(nf.Unary(pos(a,b), a, Unary.POST_DEC));                 break;
            } 

            //
            // Rule 298:  unary_expression ::= preincrement_expression$a
            //
            case 298: {
                //#line 1581 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 299:  unary_expression ::= predecrement_expression$a
            //
            case 299: {
                //#line 1583 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Unary a = (Unary) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 300:  unary_expression ::= PLUS$b unary_expression$a
            //
            case 300: {
                //#line 1585 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1585 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.POS, a));                 break;
            } 

            //
            // Rule 301:  unary_expression ::= MINUS$b unary_expression$a
            //
            case 301: {
                //#line 1587 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1587 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.NEG, a));                 break;
            } 

            //
            // Rule 302:  unary_expression ::= MINUS$b boundary_literal$a
            //
            case 302: {
                //#line 1589 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1589 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                polyglot.ast.Lit a = (polyglot.ast.Lit) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.NEG, a));                 break;
            } 

            //
            // Rule 303:  unary_expression ::= unary_expression_not_plus_minus$a
            //
            case 303: {
                //#line 1591 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 304:  preincrement_expression ::= PLUSPLUS$b unary_expression$a
            //
            case 304: {
                //#line 1596 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1596 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.PRE_INC, a));                 break;
            } 

            //
            // Rule 305:  predecrement_expression ::= MINUSMINUS$b unary_expression$a
            //
            case 305: {
                //#line 1601 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1601 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.PRE_DEC, a));                 break;
            } 

            //
            // Rule 306:  unary_expression_not_plus_minus ::= postfix_expression$a
            //
            case 306: {
                //#line 1606 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 307:  unary_expression_not_plus_minus ::= COMP$b unary_expression$a
            //
            case 307: {
                //#line 1608 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1608 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.BIT_NOT, a));                 break;
            } 

            //
            // Rule 308:  unary_expression_not_plus_minus ::= NOT$b unary_expression$a
            //
            case 308: {
                //#line 1610 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken b = (IToken) getRhsIToken(1);
                //#line 1610 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2); setResult(nf.Unary(pos(b,a,a), Unary.NOT, a));                 break;
            } 

            //
            // Rule 309:  unary_expression_not_plus_minus ::= cast_expression$a
            //
            case 309: {
                //#line 1612 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Cast a = (Cast) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 310:  cast_expression ::= LPAREN$p primitive_type$a dims_opt$b RPAREN unary_expression$c
            //
            case 310: {
                //#line 1617 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken p = (IToken) getRhsIToken(1);
                //#line 1617 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode a = (TypeNode) getRhsSym(2);
                //#line 1617 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer b = (Integer) getRhsSym(3);
                //#line 1617 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr c = (Expr) getRhsSym(5); setResult(nf.Cast(pos(p, c,a),
            array(a, b.intValue()), c));                 break;
            } 
            //
            // Rule 311:  cast_expression ::= LPAREN$p expression$a RPAREN unary_expression_not_plus_minus$b
            //
            case 311: {
                //#line 1620 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken p = (IToken) getRhsIToken(1);
                //#line 1620 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(2);
                //#line 1620 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(4); setResult(nf.Cast(pos(p, b,a),
            exprToType(a), b));                 break;
            } 
            //
            // Rule 312:  cast_expression ::= LPAREN$p name$a dims$b RPAREN unary_expression_not_plus_minus$c
            //
            case 312: {
                //#line 1623 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                IToken p = (IToken) getRhsIToken(1);
                //#line 1623 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(2);
                //#line 1623 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Integer b = (Integer) getRhsSym(3);
                //#line 1623 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr c = (Expr) getRhsSym(5); setResult(nf.Cast(pos(p, c,a),
            array(a.toType(), b.intValue()), c));                 break;
            } 
            //
            // Rule 313:  multiplicative_expression ::= unary_expression$a
            //
            case 313: {
                //#line 1629 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 314:  multiplicative_expression ::= multiplicative_expression$a MULT unary_expression$b
            //
            case 314: {
                //#line 1631 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1631 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.MUL, b));                 break;
            } 
            //
            // Rule 315:  multiplicative_expression ::= multiplicative_expression$a DIV unary_expression$b
            //
            case 315: {
                //#line 1634 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1634 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.DIV, b));                 break;
            } 
            //
            // Rule 316:  multiplicative_expression ::= multiplicative_expression$a MOD unary_expression$b
            //
            case 316: {
                //#line 1637 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1637 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.MOD, b));                 break;
            } 
            //
            // Rule 317:  additive_expression ::= multiplicative_expression$a
            //
            case 317: {
                //#line 1643 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 318:  additive_expression ::= additive_expression$a PLUS multiplicative_expression$b
            //
            case 318: {
                //#line 1645 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1645 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.ADD, b));                 break;
            } 
            //
            // Rule 319:  additive_expression ::= additive_expression$a MINUS multiplicative_expression$b
            //
            case 319: {
                //#line 1648 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1648 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.SUB, b));                 break;
            } 
            //
            // Rule 320:  shift_expression ::= additive_expression$a
            //
            case 320: {
                //#line 1654 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 321:  shift_expression ::= shift_expression$a LSHIFT additive_expression$b
            //
            case 321: {
                //#line 1656 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1656 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.SHL, b));                 break;
            } 
            //
            // Rule 322:  shift_expression ::= shift_expression$a RSHIFT additive_expression$b
            //
            case 322: {
                //#line 1659 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1659 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.SHR, b));                 break;
            } 
            //
            // Rule 323:  shift_expression ::= shift_expression$a URSHIFT additive_expression$b
            //
            case 323: {
                //#line 1662 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1662 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.USHR, b));                 break;
            } 
            //
            // Rule 324:  relational_expression ::= shift_expression$a
            //
            case 324: {
                //#line 1668 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 325:  relational_expression ::= relational_expression$a LT shift_expression$b
            //
            case 325: {
                //#line 1670 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1670 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.LT, b));                 break;
            } 
            //
            // Rule 326:  relational_expression ::= relational_expression$a GT shift_expression$b
            //
            case 326: {
                //#line 1673 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1673 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.GT, b));                 break;
            } 
            //
            // Rule 327:  relational_expression ::= relational_expression$a LTEQ shift_expression$b
            //
            case 327: {
                //#line 1676 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1676 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.LE, b));                 break;
            } 
            //
            // Rule 328:  relational_expression ::= relational_expression$a GTEQ shift_expression$b
            //
            case 328: {
                //#line 1679 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1679 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.GE, b));                 break;
            } 
            //
            // Rule 329:  relational_expression ::= relational_expression$a INSTANCEOF reference_type$b
            //
            case 329: {
                //#line 1682 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1682 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                TypeNode b = (TypeNode) getRhsSym(3); setResult(nf.Instanceof(pos(a, b), a, b));                 break;
            } 

            //
            // Rule 330:  equality_expression ::= relational_expression$a
            //
            case 330: {
                //#line 1688 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 331:  equality_expression ::= equality_expression$a EQEQ relational_expression$b
            //
            case 331: {
                //#line 1690 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1690 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.EQ, b));                 break;
            } 
            //
            // Rule 332:  equality_expression ::= equality_expression$a NOTEQ relational_expression$b
            //
            case 332: {
                //#line 1693 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1693 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.NE, b));                 break;
            } 
            //
            // Rule 333:  and_expression ::= equality_expression$a
            //
            case 333: {
                //#line 1699 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 334:  and_expression ::= and_expression$a AND equality_expression$b
            //
            case 334: {
                //#line 1701 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1701 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.BIT_AND, b));                 break;
            } 
            //
            // Rule 335:  exclusive_or_expression ::= and_expression$a
            //
            case 335: {
                //#line 1707 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 336:  exclusive_or_expression ::= exclusive_or_expression$a XOR and_expression$b
            //
            case 336: {
                //#line 1709 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1709 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.BIT_XOR, b));                 break;
            } 
            //
            // Rule 337:  inclusive_or_expression ::= exclusive_or_expression$a
            //
            case 337: {
                //#line 1715 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 338:  inclusive_or_expression ::= inclusive_or_expression$a OR exclusive_or_expression$b
            //
            case 338: {
                //#line 1717 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1717 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.BIT_OR, b));                 break;
            } 
            //
            // Rule 339:  conditional_and_expression ::= inclusive_or_expression$a
            //
            case 339: {
                //#line 1723 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 340:  conditional_and_expression ::= conditional_and_expression$a ANDAND inclusive_or_expression$b
            //
            case 340: {
                //#line 1725 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1725 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.COND_AND, b));                 break;
            } 
            //
            // Rule 341:  conditional_or_expression ::= conditional_and_expression$a
            //
            case 341: {
                //#line 1731 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 342:  conditional_or_expression ::= conditional_or_expression$a OROR conditional_and_expression$b
            //
            case 342: {
                //#line 1733 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1733 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3); setResult(nf.Binary(pos(a, b), a, 
            Binary.COND_OR, b));                 break;
            } 
            //
            // Rule 343:  conditional_expression ::= conditional_or_expression$a
            //
            case 343: {
                //#line 1739 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 344:  conditional_expression ::= conditional_or_expression$a QUESTION expression$b COLON conditional_expression$c
            //
            case 344: {
                //#line 1741 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1741 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr b = (Expr) getRhsSym(3);
                //#line 1742 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr c = (Expr) getRhsSym(5); setResult(nf.Conditional(pos(a, c), a, 
            b, c));                 break;
            } 
            //
            // Rule 345:  assignment_expression ::= conditional_expression$a
            //
            case 345: {
                //#line 1748 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 346:  assignment_expression ::= assignment$a
            //
            case 346: {
                //#line 1750 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 347:  assignment ::= left_hand_side$a assignment_operator$b assignment_expression$c
            //
            case 347: {
                //#line 1755 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1);
                //#line 1755 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Assign.Operator b = (Assign.Operator) getRhsSym(2);
                //#line 1755 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr c = (Expr) getRhsSym(3); setResult(nf.Assign(pos(a, c), a, b, c));                 break;
            } 

            //
            // Rule 348:  left_hand_side ::= name$a
            //
            case 348: {
                //#line 1760 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Name a = (Name) getRhsSym(1); setResult(a.toExpr());                 break;
            } 

            //
            // Rule 349:  left_hand_side ::= field_access$a
            //
            case 349: {
                //#line 1762 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Field a = (Field) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 350:  left_hand_side ::= array_access$a
            //
            case 350: {
                //#line 1764 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                ArrayAccess a = (ArrayAccess) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 351:  assignment_operator ::= EQ
            //
            case 351: {
                 setResult(Assign.ASSIGN);                 break;
            } 

            //
            // Rule 352:  assignment_operator ::= MULTEQ
            //
            case 352: {
                 setResult(Assign.MUL_ASSIGN);                 break;
            } 

            //
            // Rule 353:  assignment_operator ::= DIVEQ
            //
            case 353: {
                 setResult(Assign.DIV_ASSIGN);                 break;
            } 

            //
            // Rule 354:  assignment_operator ::= MODEQ
            //
            case 354: {
                 setResult(Assign.MOD_ASSIGN);                 break;
            } 

            //
            // Rule 355:  assignment_operator ::= PLUSEQ
            //
            case 355: {
                 setResult(Assign.ADD_ASSIGN);                 break;
            } 

            //
            // Rule 356:  assignment_operator ::= MINUSEQ
            //
            case 356: {
                 setResult(Assign.SUB_ASSIGN);                 break;
            } 

            //
            // Rule 357:  assignment_operator ::= LSHIFTEQ
            //
            case 357: {
                 setResult(Assign.SHL_ASSIGN);                 break;
            } 

            //
            // Rule 358:  assignment_operator ::= RSHIFTEQ
            //
            case 358: {
                 setResult(Assign.SHR_ASSIGN);                 break;
            } 

            //
            // Rule 359:  assignment_operator ::= URSHIFTEQ
            //
            case 359: {
                 setResult(Assign.USHR_ASSIGN);                 break;
            } 

            //
            // Rule 360:  assignment_operator ::= ANDEQ
            //
            case 360: {
                 setResult(Assign.BIT_AND_ASSIGN);                 break;
            } 

            //
            // Rule 361:  assignment_operator ::= XOREQ
            //
            case 361: {
                 setResult(Assign.BIT_XOR_ASSIGN);                 break;
            } 

            //
            // Rule 362:  assignment_operator ::= OREQ
            //
            case 362: {
                 setResult(Assign.BIT_OR_ASSIGN);                 break;
            } 

            //
            // Rule 363:  expression_opt ::=
            //
            case 363: {
                 setResult(null);                 break;
            } 

            //
            // Rule 364:  expression_opt ::= expression$a
            //
            case 364: {
                //#line 1797 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 365:  expression ::= assignment_expression$a
            //
            case 365: {
                //#line 1802 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

            //
            // Rule 366:  constant_expression ::= expression$a
            //
            case 366: {
                //#line 1807 "/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaParser.g"
                Expr a = (Expr) getRhsSym(1); setResult(a);                 break;
            } 

    
            default:
                break;
        }
        return;
    }
}

