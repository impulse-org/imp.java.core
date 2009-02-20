--
-- The Java KeyWord Lexer
--
%Options fp=JavaKWLexer
%options package=org.eclipse.imp.java.parser
%options template=KeywordTemplateF.gi

$Include
    KWLexerLowerCaseMapF.gi
$End

$Export
    ABSTRACT
    BOOLEAN
    BREAK
    BYTE
    CASE
    CATCH
    CHAR
    CLASS
    CONST
    CONTINUE
    DEFAULT
    DO
    DOUBLE
    ELSE
    EXTENDS
    FALSE
    FINAL
    FINALLY
    FLOAT
    FOR
    GOTO
    IF
    IMPLEMENTS
    IMPORT
    INSTANCEOF
    INT
    INTERFACE
    LONG
    NATIVE
    NEW
    NULL
    PACKAGE
    PRIVATE
    PROTECTED
    PUBLIC
    RETURN
    SHORT
    STATIC
    STRICTFP
    SUPER
    SWITCH
    SYNCHRONIZED
    THIS
    THROW
    THROWS
    TRANSIENT
    TRUE
    TRY
    VOID
    VOLATILE
    WHILE
$End

$Terminals
    a    b    c    d    e    f    g    h    i    j    k    l    m
    n    o    p    q    r    s    t    u    v    w    x    y    z
$End

$Start
    KeyWord
$End

%Notice
/.
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.
./
%End

$Rules
-- The Goal for the parser is a single Keyword

    KeyWord ::= a b s t r a c t
        /.$BeginAction
            $setResult($_ABSTRACT);
          $EndAction
        ./

              | b o o l e a n
        /.$BeginAction
            $setResult($_BOOLEAN);
          $EndAction
        ./

              | b r e a k
        /.$BeginAction
            $setResult($_BREAK);
          $EndAction
        ./

              | b y t e
        /.$BeginAction
            $setResult($_BYTE);
          $EndAction
        ./

              | c a s e
        /.$BeginAction
            $setResult($_CASE);
          $EndAction
        ./

              | c a t c h
        /.$BeginAction
            $setResult($_CATCH);
          $EndAction
        ./

              | c h a r
        /.$BeginAction
            $setResult($_CHAR);
          $EndAction
        ./

              | c l a s s
        /.$BeginAction
            $setResult($_CLASS);
          $EndAction
        ./

              | c o n s t
        /.$BeginAction
            $setResult($_CONST);
          $EndAction
        ./

              | c o n t i n u e
        /.$BeginAction
            $setResult($_CONTINUE);
          $EndAction
        ./

              | d e f a u l t
        /.$BeginAction
            $setResult($_DEFAULT);
          $EndAction
        ./

              | d o
        /.$BeginAction
            $setResult($_DO);
          $EndAction
        ./

              | d o u b l e
        /.$BeginAction
            $setResult($_DOUBLE);
          $EndAction
        ./

              | e l s e
        /.$BeginAction
            $setResult($_ELSE);
          $EndAction
        ./

              | e x t e n d s
        /.$BeginAction
            $setResult($_EXTENDS);
          $EndAction
        ./

              | f a l s e
        /.$BeginAction
            $setResult($_FALSE);
          $EndAction
        ./

              | f i n a l
        /.$BeginAction
            $setResult($_FINAL);
          $EndAction
        ./

              | f i n a l l y
        /.$BeginAction
            $setResult($_FINALLY);
          $EndAction
        ./

              | f l o a t
        /.$BeginAction
            $setResult($_FLOAT);
          $EndAction
        ./

              | f o r
        /.$BeginAction
            $setResult($_FOR);
          $EndAction
        ./

              | g o t o
        /.$BeginAction
            $setResult($_GOTO);
          $EndAction
        ./

              | i f
        /.$BeginAction
            $setResult($_IF);
          $EndAction
        ./

              | i m p l e m e n t s
        /.$BeginAction
            $setResult($_IMPLEMENTS);
          $EndAction
        ./

              | i m p o r t
        /.$BeginAction
            $setResult($_IMPORT);
          $EndAction
        ./

              | i n s t a n c e o f
        /.$BeginAction
            $setResult($_INSTANCEOF);
          $EndAction
        ./

              | i n t
        /.$BeginAction
            $setResult($_INT);
          $EndAction
        ./

              | i n t e r f a c e
        /.$BeginAction
            $setResult($_INTERFACE);
          $EndAction
        ./

              | l o n g
        /.$BeginAction
            $setResult($_LONG);
          $EndAction
        ./

              | n a t i v e
        /.$BeginAction
            $setResult($_NATIVE);
          $EndAction
        ./

              | n e w
        /.$BeginAction
            $setResult($_NEW);
          $EndAction
        ./

              | n u l l
        /.$BeginAction
            $setResult($_NULL);
          $EndAction
        ./

              | p a c k a g e
        /.$BeginAction
            $setResult($_PACKAGE);
          $EndAction
        ./

              | p r i v a t e
        /.$BeginAction
            $setResult($_PRIVATE);
          $EndAction
        ./

              | p r o t e c t e d
        /.$BeginAction
            $setResult($_PROTECTED);
          $EndAction
        ./

              | p u b l i c
        /.$BeginAction
            $setResult($_PUBLIC);
          $EndAction
        ./

              | r e t u r n
        /.$BeginAction
            $setResult($_RETURN);
          $EndAction
        ./

              | s h o r t
        /.$BeginAction
            $setResult($_SHORT);
          $EndAction
        ./

              | s t a t i c
        /.$BeginAction
            $setResult($_STATIC);
          $EndAction
        ./

              | s t r i c t f p
        /.$BeginAction
            $setResult($_STRICTFP);
          $EndAction
        ./

              | s u p e r
        /.$BeginAction
            $setResult($_SUPER);
          $EndAction
        ./

              | s w i t c h
        /.$BeginAction
            $setResult($_SWITCH);
          $EndAction
        ./

              | s y n c h r o n i z e d
        /.$BeginAction
            $setResult($_SYNCHRONIZED);
          $EndAction
        ./

              | t h i s
        /.$BeginAction
            $setResult($_THIS);
          $EndAction
        ./

              | t h r o w
        /.$BeginAction
            $setResult($_THROW);
          $EndAction
        ./

              | t h r o w s
        /.$BeginAction
            $setResult($_THROWS);
          $EndAction
        ./

              | t r a n s i e n t
        /.$BeginAction
            $setResult($_TRANSIENT);
          $EndAction
        ./

              | t r u e
        /.$BeginAction
            $setResult($_TRUE);
          $EndAction
        ./

              | t r y
        /.$BeginAction
            $setResult($_TRY);
          $EndAction
        ./

              | v o i d
        /.$BeginAction
            $setResult($_VOID);
          $EndAction
        ./

              | v o l a t i l e
        /.$BeginAction
            $setResult($_VOLATILE);
          $EndAction
        ./

              | w h i l e
        /.$BeginAction
            $setResult($_WHILE);
          $EndAction
        ./
$End
