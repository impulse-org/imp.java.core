
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.

package org.eclipse.imp.java.parser;

import lpg.runtime.*;

public class JavaKWLexer extends JavaKWLexerprs
{
    private char[] inputChars;
    private final int keywordKind[] = new int[51 + 1];

    public int[] getKeywordKinds() { return keywordKind; }

    public int lexer(int curtok, int lasttok)
    {
        int current_kind = getKind(inputChars[curtok]),
            act;

        for (act = tAction(START_STATE, current_kind);
             act > NUM_RULES && act < ACCEPT_ACTION;
             act = tAction(act, current_kind))
        {
            curtok++;
            current_kind = (curtok > lasttok
                                   ? JavaKWLexersym.Char_EOF
                                   : getKind(inputChars[curtok]));
        }

        if (act > ERROR_ACTION)
        {
            curtok++;
            act -= ERROR_ACTION;
        }

        return keywordKind[act == ERROR_ACTION  || curtok <= lasttok ? 0 : act];
    }

    public void setInputChars(char[] inputChars) { this.inputChars = inputChars; }


    final static int tokenKind[] = new int[128];
    static
    {
        tokenKind['$'] = JavaKWLexersym.Char_DollarSign;
        tokenKind['%'] = JavaKWLexersym.Char_Percent;
        tokenKind['_'] = JavaKWLexersym.Char__;
        
        tokenKind['a'] = JavaKWLexersym.Char_a;
        tokenKind['b'] = JavaKWLexersym.Char_b;
        tokenKind['c'] = JavaKWLexersym.Char_c;
        tokenKind['d'] = JavaKWLexersym.Char_d;
        tokenKind['e'] = JavaKWLexersym.Char_e;
        tokenKind['f'] = JavaKWLexersym.Char_f;
        tokenKind['g'] = JavaKWLexersym.Char_g;
        tokenKind['h'] = JavaKWLexersym.Char_h;
        tokenKind['i'] = JavaKWLexersym.Char_i;
        tokenKind['j'] = JavaKWLexersym.Char_j;
        tokenKind['k'] = JavaKWLexersym.Char_k;
        tokenKind['l'] = JavaKWLexersym.Char_l;
        tokenKind['m'] = JavaKWLexersym.Char_m;
        tokenKind['n'] = JavaKWLexersym.Char_n;
        tokenKind['o'] = JavaKWLexersym.Char_o;
        tokenKind['p'] = JavaKWLexersym.Char_p;
        tokenKind['q'] = JavaKWLexersym.Char_q;
        tokenKind['r'] = JavaKWLexersym.Char_r;
        tokenKind['s'] = JavaKWLexersym.Char_s;
        tokenKind['t'] = JavaKWLexersym.Char_t;
        tokenKind['u'] = JavaKWLexersym.Char_u;
        tokenKind['v'] = JavaKWLexersym.Char_v;
        tokenKind['w'] = JavaKWLexersym.Char_w;
        tokenKind['x'] = JavaKWLexersym.Char_x;
        tokenKind['y'] = JavaKWLexersym.Char_y;
        tokenKind['z'] = JavaKWLexersym.Char_z;
    };

    final int getKind(int c)
    {
        return ((c & 0xFFFFFF80) == 0 /* 0 <= c < 128? */ ? tokenKind[c] : 0);
    }


    public JavaKWLexer(char[] inputChars, int identifierKind)
    {
        this.inputChars = inputChars;
        keywordKind[0] = identifierKind;

        //
        // Rule 1:  KeyWord ::= a b s t r a c t
        //
        
        keywordKind[1] = (JavaParsersym.TK_ABSTRACT);
      
    
        //
        // Rule 2:  KeyWord ::= b o o l e a n
        //
        
        keywordKind[2] = (JavaParsersym.TK_BOOLEAN);
      
    
        //
        // Rule 3:  KeyWord ::= b r e a k
        //
        
        keywordKind[3] = (JavaParsersym.TK_BREAK);
      
    
        //
        // Rule 4:  KeyWord ::= b y t e
        //
        
        keywordKind[4] = (JavaParsersym.TK_BYTE);
      
    
        //
        // Rule 5:  KeyWord ::= c a s e
        //
        
        keywordKind[5] = (JavaParsersym.TK_CASE);
      
    
        //
        // Rule 6:  KeyWord ::= c a t c h
        //
        
        keywordKind[6] = (JavaParsersym.TK_CATCH);
      
    
        //
        // Rule 7:  KeyWord ::= c h a r
        //
        
        keywordKind[7] = (JavaParsersym.TK_CHAR);
      
    
        //
        // Rule 8:  KeyWord ::= c l a s s
        //
        
        keywordKind[8] = (JavaParsersym.TK_CLASS);
      
    
        //
        // Rule 9:  KeyWord ::= c o n s t
        //
        
        keywordKind[9] = (JavaParsersym.TK_CONST);
      
    
        //
        // Rule 10:  KeyWord ::= c o n t i n u e
        //
        
        keywordKind[10] = (JavaParsersym.TK_CONTINUE);
      
    
        //
        // Rule 11:  KeyWord ::= d e f a u l t
        //
        
        keywordKind[11] = (JavaParsersym.TK_DEFAULT);
      
    
        //
        // Rule 12:  KeyWord ::= d o
        //
        
        keywordKind[12] = (JavaParsersym.TK_DO);
      
    
        //
        // Rule 13:  KeyWord ::= d o u b l e
        //
        
        keywordKind[13] = (JavaParsersym.TK_DOUBLE);
      
    
        //
        // Rule 14:  KeyWord ::= e l s e
        //
        
        keywordKind[14] = (JavaParsersym.TK_ELSE);
      
    
        //
        // Rule 15:  KeyWord ::= e x t e n d s
        //
        
        keywordKind[15] = (JavaParsersym.TK_EXTENDS);
      
    
        //
        // Rule 16:  KeyWord ::= f a l s e
        //
        
        keywordKind[16] = (JavaParsersym.TK_FALSE);
      
    
        //
        // Rule 17:  KeyWord ::= f i n a l
        //
        
        keywordKind[17] = (JavaParsersym.TK_FINAL);
      
    
        //
        // Rule 18:  KeyWord ::= f i n a l l y
        //
        
        keywordKind[18] = (JavaParsersym.TK_FINALLY);
      
    
        //
        // Rule 19:  KeyWord ::= f l o a t
        //
        
        keywordKind[19] = (JavaParsersym.TK_FLOAT);
      
    
        //
        // Rule 20:  KeyWord ::= f o r
        //
        
        keywordKind[20] = (JavaParsersym.TK_FOR);
      
    
        //
        // Rule 21:  KeyWord ::= g o t o
        //
        
        keywordKind[21] = (JavaParsersym.TK_GOTO);
      
    
        //
        // Rule 22:  KeyWord ::= i f
        //
        
        keywordKind[22] = (JavaParsersym.TK_IF);
      
    
        //
        // Rule 23:  KeyWord ::= i m p l e m e n t s
        //
        
        keywordKind[23] = (JavaParsersym.TK_IMPLEMENTS);
      
    
        //
        // Rule 24:  KeyWord ::= i m p o r t
        //
        
        keywordKind[24] = (JavaParsersym.TK_IMPORT);
      
    
        //
        // Rule 25:  KeyWord ::= i n s t a n c e o f
        //
        
        keywordKind[25] = (JavaParsersym.TK_INSTANCEOF);
      
    
        //
        // Rule 26:  KeyWord ::= i n t
        //
        
        keywordKind[26] = (JavaParsersym.TK_INT);
      
    
        //
        // Rule 27:  KeyWord ::= i n t e r f a c e
        //
        
        keywordKind[27] = (JavaParsersym.TK_INTERFACE);
      
    
        //
        // Rule 28:  KeyWord ::= l o n g
        //
        
        keywordKind[28] = (JavaParsersym.TK_LONG);
      
    
        //
        // Rule 29:  KeyWord ::= n a t i v e
        //
        
        keywordKind[29] = (JavaParsersym.TK_NATIVE);
      
    
        //
        // Rule 30:  KeyWord ::= n e w
        //
        
        keywordKind[30] = (JavaParsersym.TK_NEW);
      
    
        //
        // Rule 31:  KeyWord ::= n u l l
        //
        
        keywordKind[31] = (JavaParsersym.TK_NULL);
      
    
        //
        // Rule 32:  KeyWord ::= p a c k a g e
        //
        
        keywordKind[32] = (JavaParsersym.TK_PACKAGE);
      
    
        //
        // Rule 33:  KeyWord ::= p r i v a t e
        //
        
        keywordKind[33] = (JavaParsersym.TK_PRIVATE);
      
    
        //
        // Rule 34:  KeyWord ::= p r o t e c t e d
        //
        
        keywordKind[34] = (JavaParsersym.TK_PROTECTED);
      
    
        //
        // Rule 35:  KeyWord ::= p u b l i c
        //
        
        keywordKind[35] = (JavaParsersym.TK_PUBLIC);
      
    
        //
        // Rule 36:  KeyWord ::= r e t u r n
        //
        
        keywordKind[36] = (JavaParsersym.TK_RETURN);
      
    
        //
        // Rule 37:  KeyWord ::= s h o r t
        //
        
        keywordKind[37] = (JavaParsersym.TK_SHORT);
      
    
        //
        // Rule 38:  KeyWord ::= s t a t i c
        //
        
        keywordKind[38] = (JavaParsersym.TK_STATIC);
      
    
        //
        // Rule 39:  KeyWord ::= s t r i c t f p
        //
        
        keywordKind[39] = (JavaParsersym.TK_STRICTFP);
      
    
        //
        // Rule 40:  KeyWord ::= s u p e r
        //
        
        keywordKind[40] = (JavaParsersym.TK_SUPER);
      
    
        //
        // Rule 41:  KeyWord ::= s w i t c h
        //
        
        keywordKind[41] = (JavaParsersym.TK_SWITCH);
      
    
        //
        // Rule 42:  KeyWord ::= s y n c h r o n i z e d
        //
        
        keywordKind[42] = (JavaParsersym.TK_SYNCHRONIZED);
      
    
        //
        // Rule 43:  KeyWord ::= t h i s
        //
        
        keywordKind[43] = (JavaParsersym.TK_THIS);
      
    
        //
        // Rule 44:  KeyWord ::= t h r o w
        //
        
        keywordKind[44] = (JavaParsersym.TK_THROW);
      
    
        //
        // Rule 45:  KeyWord ::= t h r o w s
        //
        
        keywordKind[45] = (JavaParsersym.TK_THROWS);
      
    
        //
        // Rule 46:  KeyWord ::= t r a n s i e n t
        //
        
        keywordKind[46] = (JavaParsersym.TK_TRANSIENT);
      
    
        //
        // Rule 47:  KeyWord ::= t r u e
        //
        
        keywordKind[47] = (JavaParsersym.TK_TRUE);
      
    
        //
        // Rule 48:  KeyWord ::= t r y
        //
        
        keywordKind[48] = (JavaParsersym.TK_TRY);
      
    
        //
        // Rule 49:  KeyWord ::= v o i d
        //
        
        keywordKind[49] = (JavaParsersym.TK_VOID);
      
    
        //
        // Rule 50:  KeyWord ::= v o l a t i l e
        //
        
        keywordKind[50] = (JavaParsersym.TK_VOLATILE);
      
    
        //
        // Rule 51:  KeyWord ::= w h i l e
        //
        
        keywordKind[51] = (JavaParsersym.TK_WHILE);
      
    
        for (int i = 0; i < keywordKind.length; i++)
        {
            if (keywordKind[i] == 0)
                keywordKind[i] = identifierKind;
        }
    }
}

