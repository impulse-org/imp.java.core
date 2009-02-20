--
-- The Java Lexer
--
%Options fp=JavaLexer
%options single-productions
%options package=org.eclipse.imp.java.parser
%options template=LexerTemplateF.gi
%options filter=JavaKWLexer.gi

%Globals
    /.import org.eclipse.imp.parser.ILexer;./
%End

%Define
    $additional_interfaces /., ILexer./
    $kw_lexer_class /.$JavaKWLexer./
%End

%Include
    LexerBasicMapF.gi
%End

%Export
    SlComment
    MlComment
    DocComment

    IDENTIFIER

    INTEGER_LITERAL  LONG_LITERAL  DOUBLE_LITERAL  FLOAT_LITERAL  
    CHARACTER_LITERAL  STRING_LITERAL
    INTEGER_LITERAL_BD LONG_LITERAL_BD  error

    ASSERT
    LBRACK RBRACK LBRACE RBRACE LPAREN RPAREN COMMA COLON SEMICOLON DOT QUESTION
    PLUS MINUS MULT DIV  MOD
    EQ XOR AND OR NOT
    PLUSPLUS MINUSMINUS COMP LSHIFT RSHIFT URSHIFT LT GT LTEQ  
    GTEQ EQEQ  NOTEQ  ANDAND  OROR  MULTEQ  DIVEQ  MODEQ  PLUSEQ  MINUSEQ  
    LSHIFTEQ RSHIFTEQ  URSHIFTEQ  ANDEQ  XOREQ  OREQ
%End

%Terminals
    CtlCharNotWS

    LF   CR   HT   FF

    a    b    c    d    e    f    g    h    i    j    k    l    m
    n    o    p    q    r    s    t    u    v    w    x    y    z
    _

    A    B    C    D    E    F    G    H    I    J    K    L    M
    N    O    P    Q    R    S    T    U    V    W    X    Y    Z

    0    1    2    3    4    5    6    7    8    9

    AfterASCII   ::= '\u0080..\ufffe'
    Space        ::= ' '
    LF           ::= NewLine
    CR           ::= Return
    HT           ::= HorizontalTab
    FF           ::= FormFeed
    DoubleQuote  ::= '"'
    SingleQuote  ::= "'"
    Percent      ::= '%'
    VerticalBar  ::= '|'
    Exclamation  ::= '!'
    AtSign       ::= '@'
    BackQuote    ::= '`'
    Tilde        ::= '~'
    Sharp        ::= '#'
    DollarSign   ::= '$'
    Ampersand    ::= '&'
    Caret        ::= '^'
    Colon        ::= ':'
    SemiColon    ::= ';'
    BackSlash    ::= '\'
    LeftBrace    ::= '{'
    RightBrace   ::= '}'
    LeftBracket  ::= '['
    RightBracket ::= ']'
    QuestionMark ::= '?'
    Comma        ::= ','
    Dot          ::= '.'
    LessThan     ::= '<'
    GreaterThan  ::= '>'
    Plus         ::= '+'
    Minus        ::= '-'
    Slash        ::= '/'
    Star         ::= '*'
    LeftParen    ::= '('
    RightParen   ::= ')'
    Equal        ::= '='
%End

%Start
    Token
%End

%Notice
/.
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.
./
%End

%Rules
    ---------------------  Rules for Scanned Tokens --------------------------------
    -- The lexer creates an array list of tokens which is defined in the PrsStream class.
    -- A token has three attributes: a start offset, an end offset and a kind.
    -- 
    -- Only rules that produce complete tokens have actions to create token objects.
    -- When making a token, calls to the methods, $getToken(1) and $getRightSpan(), 
    -- provide the offsets (i.e. the span) of a rule's right hand side (rhs) and thus of the token.
    -- For a rule of the form A ::= A1 A2 ... An, the start offset of the rhs of A is given by
    -- $getToken(1) or by $getLeftSpan() and the end offset by $getRightSpan().
    --  
    -- Regarding rules for parsing in general, note that for a rhs symbol Ai, the 
    -- method $getToken(i) returns the location of the leftmost character derived from Ai.  
    -- The method $getLeftSpan(i) returns the same location unless Ai produces $empty in which case
    -- it returns the location of the last character derived before reducing Ai to $empty. 
    -- The method $getRightSpan(i) returns the location of the rightmost character derived from Ai 
    -- unless Ai produces $empty in which case it returns the location of the last character 
    -- derived before reducing Ai to $empty.
    --------------------------------------------------------------------------------
    Token ::= Identifier
        /.$BeginAction
                    checkForKeyWord();
          $EndAction
        ./
    Token ::= '"' SLBody '"'
        /.$BeginAction
                    makeToken($_STRING_LITERAL);
          $EndAction
        ./
    Token ::= "'" NotSQ "'"
        /.$BeginAction
                    makeToken($_CHARACTER_LITERAL);
          $EndAction
        ./
    Token ::= IntegerLiteral
        /.$BeginAction
                    makeToken($_INTEGER_LITERAL);
          $EndAction
        ./
    Token ::= LongLiteral
        /.$BeginAction
                    makeToken($_LONG_LITERAL);
          $EndAction
        ./
    Token ::= FloatingPointLiteral
        /.$BeginAction
                    makeToken($_FLOAT_LITERAL);
          $EndAction
        ./
    Token ::= DoubleLiteral
        /.$BeginAction
                    makeToken($_DOUBLE_LITERAL);
          $EndAction
        ./
    Token ::= '/' '*' Inside Stars '/'
        /.$BeginAction
                    if (lexStream.getKind(getLeftSpan()) == JavaLexersym.Char_Star)
                         makeComment($_DocComment);
                    else makeComment($_MlComment);
          $EndAction
        ./
    Token ::= SLC
        /.$BeginAction
                    makeComment($_SlComment);
          $EndAction
        ./
    Token ::= WS -- White Space is scanned but not added to output vector
        /.$BeginAction
                    skipToken();
          $EndAction
        ./
    Token ::= '+'
        /.$BeginAction
                    makeToken($_PLUS);
          $EndAction
        ./
    Token ::= '-'
        /.$BeginAction
                    makeToken($_MINUS);
          $EndAction
        ./

    Token ::= '*'
        /.$BeginAction
                    makeToken($_MULT);
          $EndAction
        ./

    Token ::= '/'
        /.$BeginAction
                    makeToken($_DIV);
          $EndAction
        ./

    Token ::= '('
        /.$BeginAction
                    makeToken($_LPAREN);
          $EndAction
        ./

    Token ::= ')'
        /.$BeginAction
                    makeToken($_RPAREN);
          $EndAction
        ./

    Token ::= '='
        /.$BeginAction
                    makeToken($_EQ);
          $EndAction
        ./

    Token ::= ','
        /.$BeginAction
                    makeToken($_COMMA);
          $EndAction
        ./

    Token ::= ':'
        /.$BeginAction
                    makeToken($_COLON);
          $EndAction
        ./

    Token ::= ';'
        /.$BeginAction
                    makeToken($_SEMICOLON);
          $EndAction
        ./

    Token ::= '^'
        /.$BeginAction
                    makeToken($_XOR);
          $EndAction
        ./

    Token ::= '%'
        /.$BeginAction
                    makeToken($_MOD);
          $EndAction
        ./

    Token ::= '~'
        /.$BeginAction
                    makeToken($_COMP);
          $EndAction
        ./

    Token ::= '|'
        /.$BeginAction
                    makeToken($_OR);
          $EndAction
        ./

    Token ::= '&'
        /.$BeginAction
                    makeToken($_AND);
          $EndAction
        ./

    Token ::= '<'
        /.$BeginAction
                    makeToken($_LT);
          $EndAction
        ./

    Token ::= '>'
        /.$BeginAction
                    makeToken($_GT);
          $EndAction
        ./

    Token ::= '.'
        /.$BeginAction
                    makeToken($_DOT);
          $EndAction
        ./

    Token ::= '!'
        /.$BeginAction
                    makeToken($_NOT);
          $EndAction
        ./

    Token ::= '['
        /.$BeginAction
                    makeToken($_LBRACK);
          $EndAction
        ./

    Token ::= ']'
        /.$BeginAction
                    makeToken($_RBRACK);
          $EndAction
        ./

    Token ::= '{'
        /.$BeginAction
                    makeToken($_LBRACE);
          $EndAction
        ./

    Token ::= '}'
        /.$BeginAction
                    makeToken($_RBRACE);
          $EndAction
        ./

    Token ::= '?'
        /.$BeginAction
                    makeToken($_QUESTION);
          $EndAction
        ./

    Token ::= '+' '+'
        /.$BeginAction
                    makeToken($_PLUSPLUS);
          $EndAction
        ./

    Token ::= '-' '-'
        /.$BeginAction
                    makeToken($_MINUSMINUS);
          $EndAction
        ./

    Token ::= '=' '='
        /.$BeginAction
                    makeToken($_EQEQ);
          $EndAction
        ./

    Token ::= '<' '='
        /.$BeginAction
                    makeToken($_LTEQ);
          $EndAction
        ./

    Token ::= '>' '='
        /.$BeginAction
                    makeToken($_GTEQ);
          $EndAction
        ./

    Token ::= '!' '='
        /.$BeginAction
                    makeToken($_NOTEQ);
          $EndAction
        ./

    Token ::= '<' '<'
        /.$BeginAction
                    makeToken($_LSHIFT);
          $EndAction
        ./

    Token ::= '>' '>'
        /.$BeginAction
                    makeToken($_RSHIFT);
          $EndAction
        ./

    Token ::= '>' '>' '>'
        /.$BeginAction
                    makeToken($_URSHIFT);
          $EndAction
        ./

    Token ::= '+' '='
        /.$BeginAction
                    makeToken($_PLUSEQ);
          $EndAction
        ./

    Token ::= '-' '='
        /.$BeginAction
                    makeToken($_MINUSEQ);
          $EndAction
        ./

    Token ::= '*' '='
        /.$BeginAction
                    makeToken($_MULTEQ);
          $EndAction
        ./

    Token ::= '/' '='
        /.$BeginAction
                    makeToken($_DIVEQ);
          $EndAction
        ./

    Token ::= '&' '='
        /.$BeginAction
                    makeToken($_ANDEQ);
          $EndAction
        ./

    Token ::= '|' '='
        /.$BeginAction
                    makeToken($_OREQ);
          $EndAction
        ./

    Token ::= '^' '='
        /.$BeginAction
                    makeToken($_XOREQ);
          $EndAction
        ./

    Token ::= '%' '='
        /.$BeginAction
                    makeToken($_MODEQ);
          $EndAction
        ./

    Token ::= '<' '<' '='
        /.$BeginAction
                    makeToken($_LSHIFTEQ);
          $EndAction
        ./

    Token ::= '>' '>' '='
        /.$BeginAction
                    makeToken($_RSHIFTEQ);
          $EndAction
        ./

    Token ::= '>' '>' '>' '='
        /.$BeginAction
                    makeToken($_URSHIFTEQ);
          $EndAction
        ./

    Token ::= '|' '|'
        /.$BeginAction
                    makeToken($_OROR);
          $EndAction
        ./

    Token ::= '&' '&'
        /.$BeginAction
                    makeToken($_ANDAND);
          $EndAction
        ./

    IntegerLiteral -> Integer
                    | '0' LetterXx HexDigits

    LongLiteral -> Integer LetterLl
                 | '0' LetterXx HexDigits LetterLl

    DoubleLiteral -> Decimal
                   | Decimal LetterForD
                   | Decimal Exponent
                   | Decimal Exponent LetterForD
                   | Integer Exponent
                   | Integer Exponent LetterForD
                   | Integer LetterForD

    FloatingPointLiteral -> Decimal LetterForF
                          | Decimal Exponent LetterForF
                          | Integer Exponent LetterForF
                          | Integer LetterForF

    Inside ::= Inside Stars NotSlashOrStar
             | Inside '/'
             | Inside NotSlashOrStar
             | %empty

    Stars -> '*'
           | Stars '*'

    SLC ::= '/' '/'
          | SLC NotEol

    SLBody ::= %empty
             | SLBody NotDQ

    Integer -> Digit
             | Integer Digit

    HexDigits -> HexDigit
               | HexDigits HexDigit

    Decimal ::= '.' Integer
              | Integer '.'
              | Integer '.' Integer

    Exponent ::= LetterEe Integer
               | LetterEe '-' Integer
               | LetterEe '+' Integer

    WSChar -> Space
            | LF
            | CR
            | HT
            | FF

    Letter -> LowerCaseLetter
            | UpperCaseLetter
            | _
            | '$'
            | '\u0080..\ufffe'

    LowerCaseLetter -> a | b | c | d | e | f | g | h | i | j | k | l | m |
                       n | o | p | q | r | s | t | u | v | w | x | y | z

    UpperCaseLetter -> A | B | C | D | E | F | G | H | I | J | K | L | M |
                       N | O | P | Q | R | S | T | U | V | W | X | Y | Z

    Digit -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

    OctalDigit -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7

    a..f -> a | b | c | d | e | f | A | B | C | D | E | F

    HexDigit -> Digit
              | a..f

    OctalDigits3 -> OctalDigit
                  | OctalDigit OctalDigit
                  | OctalDigit OctalDigit OctalDigit

    LetterForD -> 'D'
                | 'd'

    LetterForF -> 'F'
                | 'f'

    LetterLl -> 'L'
              | 'l'

    LetterEe -> 'E'
              | 'e'

    LetterXx -> 'X'
              | 'x'


    WS -> WSChar
        | WS WSChar

    Identifier -> Letter
                | Identifier Letter
                | Identifier Digit

    SpecialNotStar -> '+' | '-' | '/' | '(' | ')' | '"' | '!' | '@' | '`' | '~' |
                      '%' | '&' | '^' | ':' | ';' | "'" | '\' | '|' | '{' | '}' |
                      '[' | ']' | '?' | ',' | '.' | '<' | '>' | '=' | '#'

    SpecialNotSlash -> '+' | '-' | -- exclude the star as well
                       '(' | ')' | '"' | '!' | '@' | '`' | '~' |
                       '%' | '&' | '^' | ':' | ';' | "'" | '\' | '|' | '{' | '}' |
                       '[' | ']' | '?' | ',' | '.' | '<' | '>' | '=' | '#'

    SpecialNotDQ -> '+' | '-' | '/' | '(' | ')' | '*' | '!' | '@' | '`' | '~' |
                    '%' | '&' | '^' | ':' | ';' | "'" | '|' | '{' | '}' |
                    '[' | ']' | '?' | ',' | '.' | '<' | '>' | '=' | '#'

    SpecialNotSQ -> '+' | '-' | '*' | '(' | ')' | '"' | '!' | '@' | '`' | '~' |
                    '%' | '&' | '^' | ':' | ';' | '/' | '|' | '{' | '}' |
                    '[' | ']' | '?' | ',' | '.' | '<' | '>' | '=' | '#'

    NotSlashOrStar -> Letter
                    | Digit
                    | SpecialNotSlash
                    | WSChar

    NotEol -> Letter
            | Digit
            | Space
            | '*'
            | SpecialNotStar
            | HT
            | FF
            | CtlCharNotWS

    NotDQ -> Letter
           | Digit
           | SpecialNotDQ
           | Space
           | HT
           | FF
           | EscapeSequence
           | '\' u HexDigit HexDigit HexDigit HexDigit
           | '\' OctalDigit

    NotSQ -> Letter
           | Digit
           | SpecialNotSQ
           | Space
           | HT
           | FF
           | EscapeSequence
           | '\' u HexDigit HexDigit HexDigit HexDigit
           | '\' OctalDigits3

    EscapeSequence ::= '\' b
                     | '\' t
                     | '\' n
                     | '\' f
                     | '\' r
                     | '\' '"'
                     | '\' "'"
                     | '\' '\'
%End
