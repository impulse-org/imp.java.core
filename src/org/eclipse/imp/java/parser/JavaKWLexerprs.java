
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.

package org.eclipse.imp.java.parser;

public class JavaKWLexerprs implements lpg.runtime.ParseTable, JavaKWLexersym {
    public final static int ERROR_SYMBOL = 0;
    public final int getErrorSymbol() { return ERROR_SYMBOL; }

    public final static int SCOPE_UBOUND = 0;
    public final int getScopeUbound() { return SCOPE_UBOUND; }

    public final static int SCOPE_SIZE = 0;
    public final int getScopeSize() { return SCOPE_SIZE; }

    public final static int MAX_NAME_LENGTH = 0;
    public final int getMaxNameLength() { return MAX_NAME_LENGTH; }

    public final static int NUM_STATES = 189;
    public final int getNumStates() { return NUM_STATES; }

    public final static int NT_OFFSET = 30;
    public final int getNtOffset() { return NT_OFFSET; }

    public final static int LA_STATE_OFFSET = 295;
    public final int getLaStateOffset() { return LA_STATE_OFFSET; }

    public final static int MAX_LA = 1;
    public final int getMaxLa() { return MAX_LA; }

    public final static int NUM_RULES = 51;
    public final int getNumRules() { return NUM_RULES; }

    public final static int NUM_NONTERMINALS = 2;
    public final int getNumNonterminals() { return NUM_NONTERMINALS; }

    public final static int NUM_SYMBOLS = 32;
    public final int getNumSymbols() { return NUM_SYMBOLS; }

    public final static int SEGMENT_SIZE = 8192;
    public final int getSegmentSize() { return SEGMENT_SIZE; }

    public final static int START_STATE = 52;
    public final int getStartState() { return START_STATE; }

    public final static int IDENTIFIER_SYMBOL = 0;
    public final int getIdentifier_SYMBOL() { return IDENTIFIER_SYMBOL; }

    public final static int EOFT_SYMBOL = 25;
    public final int getEoftSymbol() { return EOFT_SYMBOL; }

    public final static int EOLT_SYMBOL = 31;
    public final int getEoltSymbol() { return EOLT_SYMBOL; }

    public final static int ACCEPT_ACTION = 243;
    public final int getAcceptAction() { return ACCEPT_ACTION; }

    public final static int ERROR_ACTION = 244;
    public final int getErrorAction() { return ERROR_ACTION; }

    public final static boolean BACKTRACK = false;
    public final boolean getBacktrack() { return BACKTRACK; }

    public final int getStartSymbol() { return lhs(0); }
    public final boolean isValidForParser() { return JavaKWLexersym.isValidForParser; }


    public interface IsNullable {
        public final static byte isNullable[] = {0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0
        };
    };
    public final static byte isNullable[] = IsNullable.isNullable;
    public final boolean isNullable(int index) { return isNullable[index] != 0; }

    public interface ProsthesesIndex {
        public final static byte prosthesesIndex[] = {0,
            2,1
        };
    };
    public final static byte prosthesesIndex[] = ProsthesesIndex.prosthesesIndex;
    public final int prosthesesIndex(int index) { return prosthesesIndex[index]; }

    public interface IsKeyword {
        public final static byte isKeyword[] = {0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0
        };
    };
    public final static byte isKeyword[] = IsKeyword.isKeyword;
    public final boolean isKeyword(int index) { return isKeyword[index] != 0; }

    public interface BaseCheck {
        public final static byte baseCheck[] = {0,
            8,7,5,4,4,5,4,5,5,8,
            7,2,6,4,7,5,5,7,5,3,
            4,2,10,6,10,3,9,4,6,3,
            4,7,7,9,6,6,5,6,8,5,
            6,12,4,5,6,9,4,3,4,8,
            5
        };
    };
    public final static byte baseCheck[] = BaseCheck.baseCheck;
    public final int baseCheck(int index) { return baseCheck[index]; }
    public final static byte rhs[] = baseCheck;
    public final int rhs(int index) { return rhs[index]; };

    public interface BaseAction {
        public final static char baseAction[] = {
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,54,78,96,97,62,22,12,
            38,42,100,47,101,23,52,71,32,50,
            83,104,57,48,73,107,109,111,77,112,
            65,113,25,116,118,119,121,124,82,125,
            130,129,133,135,139,144,140,134,6,147,
            148,150,86,155,158,156,160,162,164,163,
            170,165,168,174,175,176,179,181,182,186,
            188,189,190,191,195,196,197,198,201,204,
            89,205,209,215,213,207,219,214,221,90,
            223,225,226,227,230,234,235,238,241,243,
            244,246,247,248,249,250,255,252,256,262,
            265,267,269,251,273,274,275,278,280,284,
            285,287,289,290,291,296,295,299,292,302,
            300,306,309,305,310,307,320,316,317,321,
            323,325,328,329,332,334,335,336,338,339,
            343,346,348,349,351,356,361,359,357,363,
            366,368,371,372,375,373,377,378,379,381,
            383,384,387,389,392,397,395,399,402,403,
            410,411,401,406,414,416,417,418,422,423,
            424,426,244,244
        };
    };
    public final static char baseAction[] = BaseAction.baseAction;
    public final int baseAction(int index) { return baseAction[index]; }
    public final static char lhs[] = baseAction;
    public final int lhs(int index) { return lhs[index]; };

    public interface TermCheck {
        public final static byte termCheck[] = {0,
            0,1,2,3,4,0,6,7,8,9,
            10,0,1,13,14,15,16,17,13,19,
            20,0,0,2,0,3,4,5,6,5,
            6,0,11,12,3,4,5,0,17,18,
            3,0,1,12,3,8,0,0,11,0,
            3,0,11,7,5,4,0,8,11,13,
            4,0,6,0,0,18,2,18,22,8,
            0,1,0,12,23,5,0,0,6,3,
            8,0,0,2,8,0,0,2,0,0,
            9,2,4,5,9,0,0,15,9,0,
            0,5,25,0,5,5,0,12,0,6,
            0,0,0,7,6,0,5,0,0,0,
            0,4,2,0,0,10,16,15,0,0,
            7,2,0,0,0,17,8,5,0,0,
            16,7,4,0,11,2,0,0,9,0,
            3,0,3,7,0,0,2,0,1,0,
            5,0,0,0,0,4,3,0,9,0,
            1,7,5,0,0,0,14,2,0,1,
            0,0,9,2,10,0,6,0,0,0,
            0,2,4,8,0,0,0,0,11,4,
            0,1,6,0,0,2,0,1,0,5,
            20,3,0,0,0,21,19,3,0,1,
            0,9,0,3,0,0,0,1,15,0,
            1,9,8,0,0,10,3,0,4,2,
            0,1,0,0,2,0,0,0,0,0,
            0,0,9,2,0,0,8,10,12,10,
            6,0,17,8,0,1,0,6,0,3,
            20,3,0,0,0,0,3,0,1,0,
            8,2,8,0,0,1,0,4,0,0,
            0,0,4,7,0,0,6,2,0,0,
            11,0,1,9,0,0,0,8,0,0,
            12,6,21,9,6,0,0,8,12,0,
            0,2,0,7,0,10,2,0,0,1,
            10,0,10,0,0,0,2,0,0,0,
            7,4,0,1,13,0,19,0,0,4,
            0,3,14,3,7,0,0,22,0,4,
            0,1,0,5,2,0,1,0,1,13,
            0,0,0,3,0,1,0,0,0,2,
            0,10,0,0,1,9,0,7,0,11,
            18,0,10,7,0,7,0,1,0,1,
            0,0,0,1,10,0,6,16,7,0,
            0,2,2,0,1,0,0,0,2,14,
            5,0,0,0,1,0,0,0,0,0,
            0,9,0,0,13,0,0,0,0,14,
            0,24,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0
        };
    };
    public final static byte termCheck[] = TermCheck.termCheck;
    public final int termCheck(int index) { return termCheck[index]; }

    public interface TermAction {
        public final static char termAction[] = {0,
            244,66,57,70,62,244,63,61,59,58,
            68,244,80,65,67,69,60,55,138,64,
            56,244,244,78,244,94,92,91,93,122,
            123,244,77,79,102,100,99,244,76,75,
            83,244,85,101,86,82,244,244,81,244,
            111,244,84,88,105,96,244,104,110,266,
            108,244,109,244,244,292,120,103,89,73,
            244,98,244,74,95,97,244,244,113,118,
            112,244,244,128,117,244,244,142,244,244,
            129,174,167,166,143,244,244,106,175,244,
            244,72,243,244,87,90,244,71,244,107,
            244,244,244,114,115,244,119,244,244,244,
            244,125,126,244,244,124,116,121,244,244,
            127,131,244,12,244,274,264,132,244,244,
            130,133,134,244,137,135,244,244,136,244,
            140,244,141,139,244,244,144,244,145,244,
            146,244,244,244,244,148,149,244,147,244,
            291,150,151,244,244,244,293,153,244,154,
            244,244,287,156,152,244,155,244,244,244,
            244,160,159,157,244,244,244,244,158,275,
            26,164,163,244,244,165,244,171,244,265,
            161,168,244,244,244,162,272,169,244,258,
            244,170,244,173,244,244,244,249,172,244,
            248,176,251,244,244,177,178,244,179,180,
            244,295,244,244,181,244,244,244,244,244,
            244,244,182,281,244,244,284,185,184,186,
            187,244,183,188,244,190,244,189,244,191,
            193,192,244,244,244,244,195,244,197,244,
            194,263,196,244,244,260,244,198,244,244,
            244,244,200,199,244,244,202,253,244,244,
            201,244,203,252,44,244,244,204,244,244,
            250,205,247,289,206,244,244,207,285,244,
            244,208,244,280,244,282,210,244,244,273,
            279,244,209,244,244,244,268,17,244,244,
            213,215,244,257,212,244,211,244,244,217,
            244,219,216,220,218,244,244,214,244,221,
            244,222,244,223,225,244,277,244,276,224,
            244,244,244,226,244,228,244,244,244,255,
            244,227,244,244,294,259,244,246,244,229,
            262,244,230,231,244,232,244,233,244,235,
            244,244,244,254,234,244,237,283,236,244,
            244,245,290,244,271,244,244,244,239,278,
            238,244,244,244,241,244,244,244,244,244,
            244,267,244,244,269,244,244,244,244,286,
            244,240
        };
    };
    public final static char termAction[] = TermAction.termAction;
    public final int termAction(int index) { return termAction[index]; }
    public final int asb(int index) { return 0; }
    public final int asr(int index) { return 0; }
    public final int nasb(int index) { return 0; }
    public final int nasr(int index) { return 0; }
    public final int terminalIndex(int index) { return 0; }
    public final int nonterminalIndex(int index) { return 0; }
    public final int scopePrefix(int index) { return 0;}
    public final int scopeSuffix(int index) { return 0;}
    public final int scopeLhs(int index) { return 0;}
    public final int scopeLa(int index) { return 0;}
    public final int scopeStateSet(int index) { return 0;}
    public final int scopeRhs(int index) { return 0;}
    public final int scopeState(int index) { return 0;}
    public final int inSymb(int index) { return 0;}
    public final String name(int index) { return null; }
    public final int originalState(int state) { return 0; }
    public final int asi(int state) { return 0; }
    public final int nasi(int state) { return 0; }
    public final int inSymbol(int state) { return 0; }

    /**
     * assert(! goto_default);
     */
    public final int ntAction(int state, int sym) {
        return baseAction[state + sym];
    }

    /**
     * assert(! shift_default);
     */
    public final int tAction(int state, int sym) {
        int i = baseAction[state],
            k = i + sym;
        return termAction[termCheck[k] == sym ? k : i];
    }
    public final int lookAhead(int la_state, int sym) {
        int k = la_state + sym;
        return termAction[termCheck[k] == sym ? k : la_state];
    }
}
