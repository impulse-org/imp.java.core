
// (C) Copyright IBM Corporation 2007
// 
// This file is part of the Eclipse IMP.

package org.eclipse.imp.java.parser;

public interface JavaKWLexersym {
    public final static int
      Char_DollarSign = 26,
      Char_Percent = 27,
      Char__ = 28,
      Char_a = 3,
      Char_b = 15,
      Char_c = 10,
      Char_d = 14,
      Char_e = 1,
      Char_f = 13,
      Char_g = 19,
      Char_h = 12,
      Char_i = 6,
      Char_j = 29,
      Char_k = 21,
      Char_l = 4,
      Char_m = 22,
      Char_n = 7,
      Char_o = 5,
      Char_p = 16,
      Char_q = 30,
      Char_r = 8,
      Char_s = 9,
      Char_t = 2,
      Char_u = 11,
      Char_v = 20,
      Char_w = 17,
      Char_x = 23,
      Char_y = 18,
      Char_z = 24,
      Char_EOF = 25;

    public final static String orderedTerminalSymbols[] = {
                 "",
                 "e",
                 "t",
                 "a",
                 "l",
                 "o",
                 "i",
                 "n",
                 "r",
                 "s",
                 "c",
                 "u",
                 "h",
                 "f",
                 "d",
                 "b",
                 "p",
                 "w",
                 "y",
                 "g",
                 "v",
                 "k",
                 "m",
                 "x",
                 "z",
                 "EOF",
                 "DollarSign",
                 "Percent",
                 "_",
                 "j",
                 "q"
             };

    public final static int numTokenKinds = orderedTerminalSymbols.length;
    public final static boolean isValidForParser = true;
}
