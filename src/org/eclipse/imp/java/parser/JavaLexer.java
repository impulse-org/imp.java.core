
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.

package org.eclipse.imp.java.parser;

import lpg.runtime.*;
import org.eclipse.imp.parser.ILexer;

public class JavaLexer implements RuleAction, ILexer
{
    private JavaLexerLpgLexStream lexStream;
    
    private static ParseTable prs = new JavaLexerprs();
    public ParseTable getParseTable() { return prs; }

    private LexParser lexParser = new LexParser();
    public LexParser getParser() { return lexParser; }

    public int getToken(int i) { return lexParser.getToken(i); }
    public int getRhsFirstTokenIndex(int i) { return lexParser.getFirstToken(i); }
    public int getRhsLastTokenIndex(int i) { return lexParser.getLastToken(i); }

    public int getLeftSpan() { return lexParser.getToken(1); }
    public int getRightSpan() { return lexParser.getLastToken(); }

    public void resetKeywordLexer()
    {
        if (kwLexer == null)
              this.kwLexer = new JavaKWLexer(lexStream.getInputChars(), JavaParsersym.TK_IDENTIFIER);
        else this.kwLexer.setInputChars(lexStream.getInputChars());
    }

    public void reset(String filename, int tab) throws java.io.IOException
    {
        lexStream = new JavaLexerLpgLexStream(filename, tab);
        lexParser.reset((ILexStream) lexStream, prs, (RuleAction) this);
        resetKeywordLexer();
    }

    public void reset(char[] input_chars, String filename)
    {
        reset(input_chars, filename, 1);
    }
    
    public void reset(char[] input_chars, String filename, int tab)
    {
        lexStream = new JavaLexerLpgLexStream(input_chars, filename, tab);
        lexParser.reset((ILexStream) lexStream, prs, (RuleAction) this);
        resetKeywordLexer();
    }
    
    public JavaLexer(String filename, int tab) throws java.io.IOException 
    {
        reset(filename, tab);
    }

    public JavaLexer(char[] input_chars, String filename, int tab)
    {
        reset(input_chars, filename, tab);
    }

    public JavaLexer(char[] input_chars, String filename)
    {
        reset(input_chars, filename, 1);
    }

    public JavaLexer() {}

    public ILexStream getILexStream() { return lexStream; }

    /**
     * @deprecated replaced by {@link #getILexStream()}
     */
    public ILexStream getLexStream() { return lexStream; }

    private void initializeLexer(IPrsStream prsStream, int start_offset, int end_offset)
    {
        if (lexStream.getInputChars() == null)
            throw new NullPointerException("LexStream was not initialized");
        lexStream.setPrsStream(prsStream);
        prsStream.makeToken(start_offset, end_offset, 0); // Token list must start with a bad token
    }

    private void addEOF(IPrsStream prsStream, int end_offset)
    {
        prsStream.makeToken(end_offset, end_offset, JavaParsersym.TK_EOF_TOKEN); // and end with the end of file token
        prsStream.setStreamLength(prsStream.getSize());
    }

    public void lexer(IPrsStream prsStream)
    {
        lexer(null, prsStream);
    }
    
    public void lexer(Monitor monitor, IPrsStream prsStream)
    {
        initializeLexer(prsStream, 0, -1);
        lexParser.parseCharacters(monitor);  // Lex the input characters
        addEOF(prsStream, lexStream.getStreamIndex());
    }

    public void lexer(IPrsStream prsStream, int start_offset, int end_offset)
    {
        lexer(null, prsStream, start_offset, end_offset);
    }
    
    public void lexer(Monitor monitor, IPrsStream prsStream, int start_offset, int end_offset)
    {
        if (start_offset <= 1)
             initializeLexer(prsStream, 0, -1);
        else initializeLexer(prsStream, start_offset - 1, start_offset - 1);

        lexParser.parseCharacters(monitor, start_offset, end_offset);

        addEOF(prsStream, (end_offset >= lexStream.getStreamIndex() ? lexStream.getStreamIndex() : end_offset + 1));
    }

    /**
     * If a parse stream was not passed to this Lexical analyser then we
     * simply report a lexical error. Otherwise, we produce a bad token.
     */
    public void reportLexicalError(int startLoc, int endLoc) {
        IPrsStream prs_stream = lexStream.getPrsStream();
        if (prs_stream == null)
            lexStream.reportLexicalError(startLoc, endLoc);
        else {
            //
            // Remove any token that may have been processed that fall in the
            // range of the lexical error... then add one error token that spans
            // the error range.
            //
            for (int i = prs_stream.getSize() - 1; i > 0; i--) {
                if (prs_stream.getStartOffset(i) >= startLoc)
                     prs_stream.removeLastToken();
                else break;
            }
            prs_stream.makeToken(startLoc, endLoc, 0); // add an error token to the prsStream
        }        
    }

    //
    // The Lexer contains an array of characters as the input stream to be parsed.
    // There are methods to retrieve and classify characters.
    // The lexparser "token" is implemented simply as the index of the next character in the array.
    // The Lexer extends the abstract class LpgLexStream with an implementation of the abstract
    // method getKind.  The template defines the Lexer class and the lexer() method.
    // A driver creates the action class, "Lexer", passing an Option object to the constructor.
    //
    JavaKWLexer kwLexer;
    boolean printTokens;
    private final static int ECLIPSE_TAB_VALUE = 4;

    public int [] getKeywordKinds() { return kwLexer.getKeywordKinds(); }

    public JavaLexer(String filename) throws java.io.IOException
    {
        this(filename, ECLIPSE_TAB_VALUE);
        this.kwLexer = new JavaKWLexer(lexStream.getInputChars(), JavaParsersym.TK_IDENTIFIER);
    }

    /**
     * @deprecated function replaced by {@link #reset(char [] content, String filename)}
     */
    public void initialize(char [] content, String filename)
    {
        reset(content, filename);
    }
    
    final void makeToken(int left_token, int right_token, int kind)
    {
        lexStream.makeToken(left_token, right_token, kind);
    }
    
    final void makeToken(int kind)
    {
        int startOffset = getLeftSpan(),
            endOffset = getRightSpan();
        lexStream.makeToken(startOffset, endOffset, kind);
        if (printTokens) printValue(startOffset, endOffset);
    }

    final void makeComment(int kind)
    {
        int startOffset = getLeftSpan(),
            endOffset = getRightSpan();
        lexStream.getIPrsStream().makeAdjunct(startOffset, endOffset, kind);
    }

    final void skipToken()
    {
        if (printTokens) printValue(getLeftSpan(), getRightSpan());
    }
    
    final void checkForKeyWord()
    {
        int startOffset = getLeftSpan(),
            endOffset = getRightSpan(),
            kwKind = kwLexer.lexer(startOffset, endOffset);
        lexStream.makeToken(startOffset, endOffset, kwKind);
        if (printTokens) printValue(startOffset, endOffset);
    }
    
    //
    // This flavor of checkForKeyWord is necessary when the default kind
    // (which is returned when the keyword filter doesn't match) is something
    // other than _IDENTIFIER.
    //
    final void checkForKeyWord(int defaultKind)
    {
        int startOffset = getLeftSpan(),
            endOffset = getRightSpan(),
            kwKind = kwLexer.lexer(startOffset, endOffset);
        if (kwKind == JavaParsersym.TK_IDENTIFIER)
            kwKind = defaultKind;
        lexStream.makeToken(startOffset, endOffset, kwKind);
        if (printTokens) printValue(startOffset, endOffset);
    }
    
    final void printValue(int startOffset, int endOffset)
    {
        String s = new String(lexStream.getInputChars(), startOffset, endOffset - startOffset + 1);
        System.out.print(s);
    }

    //
    //
    //
    static class JavaLexerLpgLexStream extends LpgLexStream
    {
    public final static int tokenKind[] =
    {
        JavaLexersym.Char_CtlCharNotWS,    // 000    0x00
        JavaLexersym.Char_CtlCharNotWS,    // 001    0x01
        JavaLexersym.Char_CtlCharNotWS,    // 002    0x02
        JavaLexersym.Char_CtlCharNotWS,    // 003    0x03
        JavaLexersym.Char_CtlCharNotWS,    // 004    0x04
        JavaLexersym.Char_CtlCharNotWS,    // 005    0x05
        JavaLexersym.Char_CtlCharNotWS,    // 006    0x06
        JavaLexersym.Char_CtlCharNotWS,    // 007    0x07
        JavaLexersym.Char_CtlCharNotWS,    // 008    0x08
        JavaLexersym.Char_HT,              // 009    0x09
        JavaLexersym.Char_LF,              // 010    0x0A
        JavaLexersym.Char_CtlCharNotWS,    // 011    0x0B
        JavaLexersym.Char_FF,              // 012    0x0C
        JavaLexersym.Char_CR,              // 013    0x0D
        JavaLexersym.Char_CtlCharNotWS,    // 014    0x0E
        JavaLexersym.Char_CtlCharNotWS,    // 015    0x0F
        JavaLexersym.Char_CtlCharNotWS,    // 016    0x10
        JavaLexersym.Char_CtlCharNotWS,    // 017    0x11
        JavaLexersym.Char_CtlCharNotWS,    // 018    0x12
        JavaLexersym.Char_CtlCharNotWS,    // 019    0x13
        JavaLexersym.Char_CtlCharNotWS,    // 020    0x14
        JavaLexersym.Char_CtlCharNotWS,    // 021    0x15
        JavaLexersym.Char_CtlCharNotWS,    // 022    0x16
        JavaLexersym.Char_CtlCharNotWS,    // 023    0x17
        JavaLexersym.Char_CtlCharNotWS,    // 024    0x18
        JavaLexersym.Char_CtlCharNotWS,    // 025    0x19
        JavaLexersym.Char_CtlCharNotWS,    // 026    0x1A
        JavaLexersym.Char_CtlCharNotWS,    // 027    0x1B
        JavaLexersym.Char_CtlCharNotWS,    // 028    0x1C
        JavaLexersym.Char_CtlCharNotWS,    // 029    0x1D
        JavaLexersym.Char_CtlCharNotWS,    // 030    0x1E
        JavaLexersym.Char_CtlCharNotWS,    // 031    0x1F
        JavaLexersym.Char_Space,           // 032    0x20
        JavaLexersym.Char_Exclamation,     // 033    0x21
        JavaLexersym.Char_DoubleQuote,     // 034    0x22
        JavaLexersym.Char_Sharp,           // 035    0x23
        JavaLexersym.Char_DollarSign,      // 036    0x24
        JavaLexersym.Char_Percent,         // 037    0x25
        JavaLexersym.Char_Ampersand,       // 038    0x26
        JavaLexersym.Char_SingleQuote,     // 039    0x27
        JavaLexersym.Char_LeftParen,       // 040    0x28
        JavaLexersym.Char_RightParen,      // 041    0x29
        JavaLexersym.Char_Star,            // 042    0x2A
        JavaLexersym.Char_Plus,            // 043    0x2B
        JavaLexersym.Char_Comma,           // 044    0x2C
        JavaLexersym.Char_Minus,           // 045    0x2D
        JavaLexersym.Char_Dot,             // 046    0x2E
        JavaLexersym.Char_Slash,           // 047    0x2F
        JavaLexersym.Char_0,               // 048    0x30
        JavaLexersym.Char_1,               // 049    0x31
        JavaLexersym.Char_2,               // 050    0x32
        JavaLexersym.Char_3,               // 051    0x33
        JavaLexersym.Char_4,               // 052    0x34
        JavaLexersym.Char_5,               // 053    0x35
        JavaLexersym.Char_6,               // 054    0x36
        JavaLexersym.Char_7,               // 055    0x37
        JavaLexersym.Char_8,               // 056    0x38
        JavaLexersym.Char_9,               // 057    0x39
        JavaLexersym.Char_Colon,           // 058    0x3A
        JavaLexersym.Char_SemiColon,       // 059    0x3B
        JavaLexersym.Char_LessThan,        // 060    0x3C
        JavaLexersym.Char_Equal,           // 061    0x3D
        JavaLexersym.Char_GreaterThan,     // 062    0x3E
        JavaLexersym.Char_QuestionMark,    // 063    0x3F
        JavaLexersym.Char_AtSign,          // 064    0x40
        JavaLexersym.Char_A,               // 065    0x41
        JavaLexersym.Char_B,               // 066    0x42
        JavaLexersym.Char_C,               // 067    0x43
        JavaLexersym.Char_D,               // 068    0x44
        JavaLexersym.Char_E,               // 069    0x45
        JavaLexersym.Char_F,               // 070    0x46
        JavaLexersym.Char_G,               // 071    0x47
        JavaLexersym.Char_H,               // 072    0x48
        JavaLexersym.Char_I,               // 073    0x49
        JavaLexersym.Char_J,               // 074    0x4A
        JavaLexersym.Char_K,               // 075    0x4B
        JavaLexersym.Char_L,               // 076    0x4C
        JavaLexersym.Char_M,               // 077    0x4D
        JavaLexersym.Char_N,               // 078    0x4E
        JavaLexersym.Char_O,               // 079    0x4F
        JavaLexersym.Char_P,               // 080    0x50
        JavaLexersym.Char_Q,               // 081    0x51
        JavaLexersym.Char_R,               // 082    0x52
        JavaLexersym.Char_S,               // 083    0x53
        JavaLexersym.Char_T,               // 084    0x54
        JavaLexersym.Char_U,               // 085    0x55
        JavaLexersym.Char_V,               // 086    0x56
        JavaLexersym.Char_W,               // 087    0x57
        JavaLexersym.Char_X,               // 088    0x58
        JavaLexersym.Char_Y,               // 089    0x59
        JavaLexersym.Char_Z,               // 090    0x5A
        JavaLexersym.Char_LeftBracket,     // 091    0x5B
        JavaLexersym.Char_BackSlash,       // 092    0x5C
        JavaLexersym.Char_RightBracket,    // 093    0x5D
        JavaLexersym.Char_Caret,           // 094    0x5E
        JavaLexersym.Char__,               // 095    0x5F
        JavaLexersym.Char_BackQuote,       // 096    0x60
        JavaLexersym.Char_a,               // 097    0x61
        JavaLexersym.Char_b,               // 098    0x62
        JavaLexersym.Char_c,               // 099    0x63
        JavaLexersym.Char_d,               // 100    0x64
        JavaLexersym.Char_e,               // 101    0x65
        JavaLexersym.Char_f,               // 102    0x66
        JavaLexersym.Char_g,               // 103    0x67
        JavaLexersym.Char_h,               // 104    0x68
        JavaLexersym.Char_i,               // 105    0x69
        JavaLexersym.Char_j,               // 106    0x6A
        JavaLexersym.Char_k,               // 107    0x6B
        JavaLexersym.Char_l,               // 108    0x6C
        JavaLexersym.Char_m,               // 109    0x6D
        JavaLexersym.Char_n,               // 110    0x6E
        JavaLexersym.Char_o,               // 111    0x6F
        JavaLexersym.Char_p,               // 112    0x70
        JavaLexersym.Char_q,               // 113    0x71
        JavaLexersym.Char_r,               // 114    0x72
        JavaLexersym.Char_s,               // 115    0x73
        JavaLexersym.Char_t,               // 116    0x74
        JavaLexersym.Char_u,               // 117    0x75
        JavaLexersym.Char_v,               // 118    0x76
        JavaLexersym.Char_w,               // 119    0x77
        JavaLexersym.Char_x,               // 120    0x78
        JavaLexersym.Char_y,               // 121    0x79
        JavaLexersym.Char_z,               // 122    0x7A
        JavaLexersym.Char_LeftBrace,       // 123    0x7B
        JavaLexersym.Char_VerticalBar,     // 124    0x7C
        JavaLexersym.Char_RightBrace,      // 125    0x7D
        JavaLexersym.Char_Tilde,           // 126    0x7E

        JavaLexersym.Char_AfterASCII,      // for all chars in range 128..65534
        JavaLexersym.Char_EOF              // for '\uffff' or 65535 
    };
            
    public final int getKind(int i)  // Classify character at ith location
    {
        int c = (i >= getStreamLength() ? '\uffff' : getCharValue(i));
        return (c < 128 // ASCII Character
                  ? tokenKind[c]
                  : c == '\uffff'
                       ? JavaLexersym.Char_EOF
                       : JavaLexersym.Char_AfterASCII);
    }

    public String[] orderedExportedSymbols() { return JavaParsersym.orderedTerminalSymbols; }

    public JavaLexerLpgLexStream(String filename, int tab) throws java.io.IOException
    {
        super(filename, tab);
    }

    public JavaLexerLpgLexStream(char[] input_chars, String filename, int tab)
    {
        super(input_chars, filename, tab);
    }

    public JavaLexerLpgLexStream(char[] input_chars, String filename)
    {
        super(input_chars, filename, 1);
    }
    }

    public void ruleAction(int ruleNumber)
    {
        switch(ruleNumber)
        {

            //
            // Rule 1:  Token ::= Identifier
            //
            case 1: { 
                checkForKeyWord();
                  break;
            }
    
            //
            // Rule 2:  Token ::= " SLBody "
            //
            case 2: { 
                makeToken(JavaParsersym.TK_STRING_LITERAL);
                  break;
            }
    
            //
            // Rule 3:  Token ::= ' NotSQ '
            //
            case 3: { 
                makeToken(JavaParsersym.TK_CHARACTER_LITERAL);
                  break;
            }
    
            //
            // Rule 4:  Token ::= IntegerLiteral
            //
            case 4: { 
                makeToken(JavaParsersym.TK_INTEGER_LITERAL);
                  break;
            }
    
            //
            // Rule 5:  Token ::= LongLiteral
            //
            case 5: { 
                makeToken(JavaParsersym.TK_LONG_LITERAL);
                  break;
            }
    
            //
            // Rule 6:  Token ::= FloatingPointLiteral
            //
            case 6: { 
                makeToken(JavaParsersym.TK_FLOAT_LITERAL);
                  break;
            }
    
            //
            // Rule 7:  Token ::= DoubleLiteral
            //
            case 7: { 
                makeToken(JavaParsersym.TK_DOUBLE_LITERAL);
                  break;
            }
    
            //
            // Rule 8:  Token ::= / * Inside Stars /
            //
            case 8: { 
                if (lexStream.getKind(getLeftSpan()) == JavaLexersym.Char_Star)
                     makeComment(JavaParsersym.TK_DocComment);
                else makeComment(JavaParsersym.TK_MlComment);
                  break;
            }
    
            //
            // Rule 9:  Token ::= SLC
            //
            case 9: { 
                makeComment(JavaParsersym.TK_SlComment);
                  break;
            }
    
            //
            // Rule 10:  Token ::= WS
            //
            case 10: { 
                skipToken();
                  break;
            }
    
            //
            // Rule 11:  Token ::= +
            //
            case 11: { 
                makeToken(JavaParsersym.TK_PLUS);
                  break;
            }
    
            //
            // Rule 12:  Token ::= -
            //
            case 12: { 
                makeToken(JavaParsersym.TK_MINUS);
                  break;
            }
    
            //
            // Rule 13:  Token ::= *
            //
            case 13: { 
                makeToken(JavaParsersym.TK_MULT);
                  break;
            }
    
            //
            // Rule 14:  Token ::= /
            //
            case 14: { 
                makeToken(JavaParsersym.TK_DIV);
                  break;
            }
    
            //
            // Rule 15:  Token ::= (
            //
            case 15: { 
                makeToken(JavaParsersym.TK_LPAREN);
                  break;
            }
    
            //
            // Rule 16:  Token ::= )
            //
            case 16: { 
                makeToken(JavaParsersym.TK_RPAREN);
                  break;
            }
    
            //
            // Rule 17:  Token ::= =
            //
            case 17: { 
                makeToken(JavaParsersym.TK_EQ);
                  break;
            }
    
            //
            // Rule 18:  Token ::= ,
            //
            case 18: { 
                makeToken(JavaParsersym.TK_COMMA);
                  break;
            }
    
            //
            // Rule 19:  Token ::= :
            //
            case 19: { 
                makeToken(JavaParsersym.TK_COLON);
                  break;
            }
    
            //
            // Rule 20:  Token ::= ;
            //
            case 20: { 
                makeToken(JavaParsersym.TK_SEMICOLON);
                  break;
            }
    
            //
            // Rule 21:  Token ::= ^
            //
            case 21: { 
                makeToken(JavaParsersym.TK_XOR);
                  break;
            }
    
            //
            // Rule 22:  Token ::= %
            //
            case 22: { 
                makeToken(JavaParsersym.TK_MOD);
                  break;
            }
    
            //
            // Rule 23:  Token ::= ~
            //
            case 23: { 
                makeToken(JavaParsersym.TK_COMP);
                  break;
            }
    
            //
            // Rule 24:  Token ::= |
            //
            case 24: { 
                makeToken(JavaParsersym.TK_OR);
                  break;
            }
    
            //
            // Rule 25:  Token ::= &
            //
            case 25: { 
                makeToken(JavaParsersym.TK_AND);
                  break;
            }
    
            //
            // Rule 26:  Token ::= <
            //
            case 26: { 
                makeToken(JavaParsersym.TK_LT);
                  break;
            }
    
            //
            // Rule 27:  Token ::= >
            //
            case 27: { 
                makeToken(JavaParsersym.TK_GT);
                  break;
            }
    
            //
            // Rule 28:  Token ::= .
            //
            case 28: { 
                makeToken(JavaParsersym.TK_DOT);
                  break;
            }
    
            //
            // Rule 29:  Token ::= !
            //
            case 29: { 
                makeToken(JavaParsersym.TK_NOT);
                  break;
            }
    
            //
            // Rule 30:  Token ::= [
            //
            case 30: { 
                makeToken(JavaParsersym.TK_LBRACK);
                  break;
            }
    
            //
            // Rule 31:  Token ::= ]
            //
            case 31: { 
                makeToken(JavaParsersym.TK_RBRACK);
                  break;
            }
    
            //
            // Rule 32:  Token ::= {
            //
            case 32: { 
                makeToken(JavaParsersym.TK_LBRACE);
                  break;
            }
    
            //
            // Rule 33:  Token ::= }
            //
            case 33: { 
                makeToken(JavaParsersym.TK_RBRACE);
                  break;
            }
    
            //
            // Rule 34:  Token ::= ?
            //
            case 34: { 
                makeToken(JavaParsersym.TK_QUESTION);
                  break;
            }
    
            //
            // Rule 35:  Token ::= + +
            //
            case 35: { 
                makeToken(JavaParsersym.TK_PLUSPLUS);
                  break;
            }
    
            //
            // Rule 36:  Token ::= - -
            //
            case 36: { 
                makeToken(JavaParsersym.TK_MINUSMINUS);
                  break;
            }
    
            //
            // Rule 37:  Token ::= = =
            //
            case 37: { 
                makeToken(JavaParsersym.TK_EQEQ);
                  break;
            }
    
            //
            // Rule 38:  Token ::= < =
            //
            case 38: { 
                makeToken(JavaParsersym.TK_LTEQ);
                  break;
            }
    
            //
            // Rule 39:  Token ::= > =
            //
            case 39: { 
                makeToken(JavaParsersym.TK_GTEQ);
                  break;
            }
    
            //
            // Rule 40:  Token ::= ! =
            //
            case 40: { 
                makeToken(JavaParsersym.TK_NOTEQ);
                  break;
            }
    
            //
            // Rule 41:  Token ::= < <
            //
            case 41: { 
                makeToken(JavaParsersym.TK_LSHIFT);
                  break;
            }
    
            //
            // Rule 42:  Token ::= > >
            //
            case 42: { 
                makeToken(JavaParsersym.TK_RSHIFT);
                  break;
            }
    
            //
            // Rule 43:  Token ::= > > >
            //
            case 43: { 
                makeToken(JavaParsersym.TK_URSHIFT);
                  break;
            }
    
            //
            // Rule 44:  Token ::= + =
            //
            case 44: { 
                makeToken(JavaParsersym.TK_PLUSEQ);
                  break;
            }
    
            //
            // Rule 45:  Token ::= - =
            //
            case 45: { 
                makeToken(JavaParsersym.TK_MINUSEQ);
                  break;
            }
    
            //
            // Rule 46:  Token ::= * =
            //
            case 46: { 
                makeToken(JavaParsersym.TK_MULTEQ);
                  break;
            }
    
            //
            // Rule 47:  Token ::= / =
            //
            case 47: { 
                makeToken(JavaParsersym.TK_DIVEQ);
                  break;
            }
    
            //
            // Rule 48:  Token ::= & =
            //
            case 48: { 
                makeToken(JavaParsersym.TK_ANDEQ);
                  break;
            }
    
            //
            // Rule 49:  Token ::= | =
            //
            case 49: { 
                makeToken(JavaParsersym.TK_OREQ);
                  break;
            }
    
            //
            // Rule 50:  Token ::= ^ =
            //
            case 50: { 
                makeToken(JavaParsersym.TK_XOREQ);
                  break;
            }
    
            //
            // Rule 51:  Token ::= % =
            //
            case 51: { 
                makeToken(JavaParsersym.TK_MODEQ);
                  break;
            }
    
            //
            // Rule 52:  Token ::= < < =
            //
            case 52: { 
                makeToken(JavaParsersym.TK_LSHIFTEQ);
                  break;
            }
    
            //
            // Rule 53:  Token ::= > > =
            //
            case 53: { 
                makeToken(JavaParsersym.TK_RSHIFTEQ);
                  break;
            }
    
            //
            // Rule 54:  Token ::= > > > =
            //
            case 54: { 
                makeToken(JavaParsersym.TK_URSHIFTEQ);
                  break;
            }
    
            //
            // Rule 55:  Token ::= | |
            //
            case 55: { 
                makeToken(JavaParsersym.TK_OROR);
                  break;
            }
    
            //
            // Rule 56:  Token ::= & &
            //
            case 56: { 
                makeToken(JavaParsersym.TK_ANDAND);
                  break;
            }
    
    
            default:
                break;
        }
        return;
    }
}

