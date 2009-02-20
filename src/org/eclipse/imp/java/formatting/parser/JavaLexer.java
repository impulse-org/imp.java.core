
////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2007 IBM Corporation.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
//Contributors:
//    Philippe Charles (pcharles@us.ibm.com) - initial API and implementation

////////////////////////////////////////////////////////////////////////////////

package org.eclipse.imp.java.formatting.parser;

import lpg.runtime.*;

 import org.eclipse.imp.parser.*;
 
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
            // Rule 2:  Token ::= $ { I D E N T I F I E R IntegerOpt }
            //
            case 2: { 
                makeToken(JavaParsersym.TK_VAR_IDENTIFIER);
                  break;
            }
    
            //
            // Rule 3:  Token ::= $ { B l o c k S t a t e m e n t s IntegerOpt }
            //
            case 3: { 
                makeToken(JavaParsersym.TK_VAR_BlockStatements);
                  break;
            }
    
            //
            // Rule 4:  Token ::= $ { A r g u m e n t L i s t o p t IntegerOpt }
            //
            case 4: { 
                makeToken(JavaParsersym.TK_VAR_ArgumentListopt);
                  break;
            }
    
            //
            // Rule 5:  Token ::= $ { A r g u m e n t L i s t IntegerOpt }
            //
            case 5: { 
                makeToken(JavaParsersym.TK_VAR_ArgumentList);
                  break;
            }
    
            //
            // Rule 6:  Token ::= $ { S t a t e m e n t IntegerOpt }
            //
            case 6: { 
                makeToken(JavaParsersym.TK_VAR_Statement);
                  break;
            }
    
            //
            // Rule 7:  Token ::= $ { E x p r e s s i o n IntegerOpt }
            //
            case 7: { 
                makeToken(JavaParsersym.TK_VAR_Expression);
                  break;
            }
    
            //
            // Rule 8:  Token ::= $ { T y p e D e c l a r a t i o n IntegerOpt }
            //
            case 8: { 
                makeToken(JavaParsersym.TK_VAR_TypeDeclaration);
                  break;
            }
    
            //
            // Rule 9:  Token ::= $ { I m p o r t D e c l a r a t i o n IntegerOpt }
            //
            case 9: { 
                makeToken(JavaParsersym.TK_VAR_ImportDeclaration);
                  break;
            }
    
            //
            // Rule 10:  Token ::= $ { I m p o r t D e c l a r a t i o n s IntegerOpt }
            //
            case 10: { 
                makeToken(JavaParsersym.TK_VAR_ImportDeclarations);
                  break;
            }
    
            //
            // Rule 11:  Token ::= $ { C l a s s B o d y D e c l a r a t i o n IntegerOpt }
            //
            case 11: { 
                makeToken(JavaParsersym.TK_VAR_ClassBodyDeclaration);
                  break;
            }
    
            //
            // Rule 12:  Token ::= $ { P a c k a g e N a m e IntegerOpt }
            //
            case 12: { 
                makeToken(JavaParsersym.TK_VAR_PackageName);
                  break;
            }
    
            //
            // Rule 13:  Token ::= $ { T y p e N a m e IntegerOpt }
            //
            case 13: { 
                makeToken(JavaParsersym.TK_VAR_TypeName);
                  break;
            }
    
            //
            // Rule 14:  Token ::= $ { M e t h o d N a m e IntegerOpt }
            //
            case 14: { 
                makeToken(JavaParsersym.TK_VAR_MethodName);
                  break;
            }
    
            //
            // Rule 15:  Token ::= " SLBody "
            //
            case 15: { 
                makeToken(JavaParsersym.TK_StringLiteral);
                  break;
            }
    
            //
            // Rule 16:  Token ::= ' NotSQ '
            //
            case 16: { 
                makeToken(JavaParsersym.TK_CharacterLiteral);
                  break;
            }
    
            //
            // Rule 17:  Token ::= IntegerLiteral
            //
            case 17: { 
                makeToken(JavaParsersym.TK_IntegerLiteral);
                  break;
            }
    
            //
            // Rule 18:  Token ::= FloatingPointLiteral
            //
            case 18: { 
                makeToken(JavaParsersym.TK_FloatingPointLiteral);
                  break;
            }
    
            //
            // Rule 19:  Token ::= DoubleLiteral
            //
            case 19: { 
                makeToken(JavaParsersym.TK_DoubleLiteral);
                  break;
            }
    
            //
            // Rule 20:  Token ::= / * Inside Stars /
            //
            case 20: { 
                skipToken();
                  break;
            }
    
            //
            // Rule 21:  Token ::= SLC
            //
            case 21: { 
                skipToken();
                  break;
            }
    
            //
            // Rule 22:  Token ::= WS
            //
            case 22: { 
                skipToken();
                  break;
            }
    
            //
            // Rule 23:  Token ::= +
            //
            case 23: { 
                makeToken(JavaParsersym.TK_PLUS);
                  break;
            }
    
            //
            // Rule 24:  Token ::= -
            //
            case 24: { 
                makeToken(JavaParsersym.TK_MINUS);
                  break;
            }
    
            //
            // Rule 25:  Token ::= *
            //
            case 25: { 
                makeToken(JavaParsersym.TK_MULTIPLY);
                  break;
            }
    
            //
            // Rule 26:  Token ::= /
            //
            case 26: { 
                makeToken(JavaParsersym.TK_DIVIDE);
                  break;
            }
    
            //
            // Rule 27:  Token ::= (
            //
            case 27: { 
                makeToken(JavaParsersym.TK_LPAREN);
                  break;
            }
    
            //
            // Rule 28:  Token ::= )
            //
            case 28: { 
                makeToken(JavaParsersym.TK_RPAREN);
                  break;
            }
    
            //
            // Rule 29:  Token ::= =
            //
            case 29: { 
                makeToken(JavaParsersym.TK_EQUAL);
                  break;
            }
    
            //
            // Rule 30:  Token ::= ,
            //
            case 30: { 
                makeToken(JavaParsersym.TK_COMMA);
                  break;
            }
    
            //
            // Rule 31:  Token ::= :
            //
            case 31: { 
                makeToken(JavaParsersym.TK_COLON);
                  break;
            }
    
            //
            // Rule 32:  Token ::= ;
            //
            case 32: { 
                makeToken(JavaParsersym.TK_SEMICOLON);
                  break;
            }
    
            //
            // Rule 33:  Token ::= ^
            //
            case 33: { 
                makeToken(JavaParsersym.TK_XOR);
                  break;
            }
    
            //
            // Rule 34:  Token ::= %
            //
            case 34: { 
                makeToken(JavaParsersym.TK_REMAINDER);
                  break;
            }
    
            //
            // Rule 35:  Token ::= ~
            //
            case 35: { 
                makeToken(JavaParsersym.TK_TWIDDLE);
                  break;
            }
    
            //
            // Rule 36:  Token ::= |
            //
            case 36: { 
                makeToken(JavaParsersym.TK_OR);
                  break;
            }
    
            //
            // Rule 37:  Token ::= &
            //
            case 37: { 
                makeToken(JavaParsersym.TK_AND);
                  break;
            }
    
            //
            // Rule 38:  Token ::= <
            //
            case 38: { 
                makeToken(JavaParsersym.TK_LESS);
                  break;
            }
    
            //
            // Rule 39:  Token ::= >
            //
            case 39: { 
                makeToken(JavaParsersym.TK_GREATER);
                  break;
            }
    
            //
            // Rule 40:  Token ::= .
            //
            case 40: { 
                makeToken(JavaParsersym.TK_DOT);
                  break;
            }
    
            //
            // Rule 41:  Token ::= !
            //
            case 41: { 
                makeToken(JavaParsersym.TK_NOT);
                  break;
            }
    
            //
            // Rule 42:  Token ::= [
            //
            case 42: { 
                makeToken(JavaParsersym.TK_LBRACKET);
                  break;
            }
    
            //
            // Rule 43:  Token ::= ]
            //
            case 43: { 
                makeToken(JavaParsersym.TK_RBRACKET);
                  break;
            }
    
            //
            // Rule 44:  Token ::= {
            //
            case 44: { 
                makeToken(JavaParsersym.TK_LBRACE);
                  break;
            }
    
            //
            // Rule 45:  Token ::= }
            //
            case 45: { 
                makeToken(JavaParsersym.TK_RBRACE);
                  break;
            }
    
            //
            // Rule 46:  Token ::= ?
            //
            case 46: { 
                makeToken(JavaParsersym.TK_QUESTION);
                  break;
            }
    
            //
            // Rule 47:  Token ::= @
            //
            case 47: { 
                makeToken(JavaParsersym.TK_AT);
                  break;
            }
    
            //
            // Rule 48:  Token ::= + +
            //
            case 48: { 
                makeToken(JavaParsersym.TK_PLUS_PLUS);
                  break;
            }
    
            //
            // Rule 49:  Token ::= - -
            //
            case 49: { 
                makeToken(JavaParsersym.TK_MINUS_MINUS);
                  break;
            }
    
            //
            // Rule 50:  Token ::= = =
            //
            case 50: { 
                makeToken(JavaParsersym.TK_EQUAL_EQUAL);
                  break;
            }
    
            //
            // Rule 51:  Token ::= < =
            //
            case 51: { 
                makeToken(JavaParsersym.TK_LESS_EQUAL);
                  break;
            }
    
            //
            // Rule 52:  Token ::= ! =
            //
            case 52: { 
                makeToken(JavaParsersym.TK_NOT_EQUAL);
                  break;
            }
    
            //
            // Rule 53:  Token ::= < <
            //
            case 53: { 
                makeToken(JavaParsersym.TK_LEFT_SHIFT);
                  break;
            }
    
            //
            // Rule 54:  Token ::= + =
            //
            case 54: { 
                makeToken(JavaParsersym.TK_PLUS_EQUAL);
                  break;
            }
    
            //
            // Rule 55:  Token ::= - =
            //
            case 55: { 
                makeToken(JavaParsersym.TK_MINUS_EQUAL);
                  break;
            }
    
            //
            // Rule 56:  Token ::= * =
            //
            case 56: { 
                makeToken(JavaParsersym.TK_MULTIPLY_EQUAL);
                  break;
            }
    
            //
            // Rule 57:  Token ::= / =
            //
            case 57: { 
                makeToken(JavaParsersym.TK_DIVIDE_EQUAL);
                  break;
            }
    
            //
            // Rule 58:  Token ::= & =
            //
            case 58: { 
                makeToken(JavaParsersym.TK_AND_EQUAL);
                  break;
            }
    
            //
            // Rule 59:  Token ::= | =
            //
            case 59: { 
                makeToken(JavaParsersym.TK_OR_EQUAL);
                  break;
            }
    
            //
            // Rule 60:  Token ::= ^ =
            //
            case 60: { 
                makeToken(JavaParsersym.TK_XOR_EQUAL);
                  break;
            }
    
            //
            // Rule 61:  Token ::= % =
            //
            case 61: { 
                makeToken(JavaParsersym.TK_REMAINDER_EQUAL);
                  break;
            }
    
            //
            // Rule 62:  Token ::= < < =
            //
            case 62: { 
                makeToken(JavaParsersym.TK_LEFT_SHIFT_EQUAL);
                  break;
            }
    
            //
            // Rule 63:  Token ::= | |
            //
            case 63: { 
                makeToken(JavaParsersym.TK_OR_OR);
                  break;
            }
    
            //
            // Rule 64:  Token ::= & &
            //
            case 64: { 
                makeToken(JavaParsersym.TK_AND_AND);
                  break;
            }
    
            //
            // Rule 65:  Token ::= . . .
            //
            case 65: { 
                makeToken(JavaParsersym.TK_ELLIPSIS);
                  break;
            }
    
    
            default:
                break;
        }
        return;
    }
}

