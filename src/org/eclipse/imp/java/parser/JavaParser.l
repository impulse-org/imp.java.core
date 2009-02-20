
Predefined Macros:

    $rule_number
    $rule_text
    $rule_size
    $input_file
    $current_line
    $next_line
    $#identifier
    $symbol_declarations
    $num_rules
    $num_terminals
    $num_nonterminals
    $num_non_terminals
    $num_symbols
    $template
    $file_prefix
    $package
    $ast_package
    $ast_type
    $exp_type
    $prs_type
    $sym_type
    $action_type
    $visitor_type
    $prefix
    $suffix
    $entry_name
    $entry_marker



Defined Symbols:



    $Header
    /.
                    //
                    // Rule $rule_number:  $rule_text
                    //
                    ./


    $BeginAction
    /.$Header$case $rule_number: {./


    $EndAction
    /.                break;
                    }./


    $BeginJava
    /.$BeginAction
                        $symbol_declarations./


    $EndJava
    /.$EndAction./


    $NoAction
    /.$Header$case $rule_number:
                        break;./


    $BadAction
    /.$Header$case $rule_number:
                        throw new Error("No action specified for rule " + $rule_number);./


    $NullAction
    /.$Header$case $rule_number:
                        setResult(null);
                        break;./


    $BeginActions
    /.
            public void ruleAction(int ruleNumber)
            {
                switch (ruleNumber)
                {./


    $SplitActions
    /.
    	            default:
    	                ruleAction$rule_number(ruleNumber);
    	                break;
    	        }
    	        return;
    	    }
    	
    	    public void ruleAction$rule_number(int ruleNumber)
    	    {
    	        switch (ruleNumber)
    	        {./


    $EndActions
    /.
                    default:
                        break;
                }
                return;
            }./


    $entry_declarations
    /.
            public $ast_class parse$entry_name()
            {
                return parse$entry_name(null, 0);
            }
                
            public $ast_class parse$entry_name(Monitor monitor)
            {
                return parse$entry_name(monitor, 0);
            }
                
            public $ast_class parse$entry_name(int error_repair_count)
            {
                return parse$entry_name(null, error_repair_count);
            }
                
            public void resetParse$entry_name()
            {
                dtParser.resetParserEntry($sym_type.$entry_marker);
            }
            
            public $ast_class parse$entry_name(Monitor monitor, int error_repair_count)
            {
                dtParser.setMonitor(monitor);
                
                try
                {
                    return ($ast_class) dtParser.parseEntry($sym_type.$entry_marker);
                }
                catch (BadParseException e)
                {
                    prsStream.reset(e.error_token); // point to error token
    
                    DiagnoseParser diagnoseParser = new DiagnoseParser(prsStream, prsTable);
                    diagnoseParser.diagnoseEntry($sym_type.$entry_marker, e.error_token);
                }
    
                return null;
            }
        ./


    $additional_interfaces
    /., IParser, Parser./


    $ast_class
    /.Object./


    $unimplemented_symbols_warning
    /.false./


    $setSym1
    /. // macro setSym1 is deprecated. Use function setResult
                    getParser().setSym1./


    $setResult
    /. // macro setResult is deprecated. Use function setResult
                     getParser().setSym1./


    $getSym
    /. // macro getSym is deprecated. Use function getRhsSym
                  getParser().getSym./


    $getToken
    /. // macro getToken is deprecated. Use function getRhsTokenIndex
                    getParser().getToken./


    $getIToken
    /. // macro getIToken is deprecated. Use function getRhsIToken
                     prsStream.getIToken./


    $getLeftSpan
    /. // macro getLeftSpan is deprecated. Use function getLeftSpan
                       getParser().getFirstToken./


    $getRightSpan
    /. // macro getRightSpan is deprecated. Use function getRightSpan
                        getParser().getLastToken./


Aliases:

    ++ ::= PLUSPLUS
    '--' ::= MINUSMINUS
    == ::= EQEQ
    <= ::= LTEQ
    >= ::= GTEQ
    != ::= NOTEQ
    << ::= LSHIFT
    >> ::= RSHIFT
    >>> ::= URSHIFT
    += ::= PLUSEQ
    -= ::= MINUSEQ
    *= ::= MULTEQ
    /= ::= DIVEQ
    &= ::= ANDEQ
    |= ::= OREQ
    ^= ::= XOREQ
    '%=' ::= MODEQ
    <<= ::= LSHIFTEQ
    >>= ::= RSHIFTEQ
    >>>= ::= URSHIFTEQ
    || ::= OROR
    && ::= ANDAND
    + ::= PLUS
    - ::= MINUS
    ! ::= NOT
    '%' ::= MOD
    ^ ::= XOR
    & ::= AND
    * ::= MULT
    '|' ::= OR
    ~ ::= COMP
    / ::= DIV
    > ::= GT
    < ::= LT
    ( ::= LPAREN
    ) ::= RPAREN
    { ::= LBRACE
    } ::= RBRACE
    [ ::= LBRACK
    ] ::= RBRACK
    ; ::= SEMICOLON
    ? ::= QUESTION
    : ::= COLON
    , ::= COMMA
    . ::= DOT
    = ::= EQ


Start:

    goal

Eol:

    SEMICOLON

Eof:

    EOF_TOKEN

Error:

    ERROR_TOKEN

Terminals:

    PLUSPLUS
    MINUSMINUS
    EQEQ
    LTEQ
    GTEQ
    NOTEQ
    LSHIFT
    RSHIFT
    URSHIFT
    PLUSEQ
    MINUSEQ
    MULTEQ
    DIVEQ
    ANDEQ
    OREQ
    XOREQ
    MODEQ
    LSHIFTEQ
    RSHIFTEQ
    URSHIFTEQ
    OROR
    ANDAND
    PLUS
    MINUS
    NOT
    MOD
    XOR
    AND
    MULT
    OR
    COMP
    DIV
    GT
    LT
    LPAREN
    RPAREN
    LBRACE
    RBRACE
    LBRACK
    RBRACK
    SEMICOLON
    QUESTION
    COLON
    COMMA
    DOT
    EQ
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
    EOF_TOKEN
    SlComment
    MlComment
    DocComment
    IDENTIFIER
    INTEGER_LITERAL
    LONG_LITERAL
    DOUBLE_LITERAL
    FLOAT_LITERAL
    CHARACTER_LITERAL
    STRING_LITERAL
    INTEGER_LITERAL_BD
    LONG_LITERAL_BD
    error
    ASSERT
    ERROR_TOKEN


Soft Keywords:



Rules:

0      '$accept' ::= goal

1      goal ::= compilation_unit $a
2      literal ::= INTEGER_LITERAL $a
3                | LONG_LITERAL $a
4                | DOUBLE_LITERAL $a
5                | FLOAT_LITERAL $a
6                | TRUE $a
7                | FALSE $a
8                | CHARACTER_LITERAL $a
9                | STRING_LITERAL $a
10               | NULL $a
11     boundary_literal ::= INTEGER_LITERAL_BD $a
12                        | LONG_LITERAL_BD $a
13     type ::= primitive_type $a
14            | reference_type $a
15     primitive_type ::= numeric_type $a
16                      | BOOLEAN $a
17     numeric_type ::= integral_type $a
18                    | floating_point_type $a
19     integral_type ::= BYTE $a
20                     | CHAR $a
21                     | SHORT $a
22                     | INT $a
23                     | LONG $a
24     floating_point_type ::= FLOAT $a
25                           | DOUBLE $a
26     reference_type ::= class_or_interface_type $a
27                      | array_type $a
28     class_or_interface_type ::= name $a
29     class_type ::= class_or_interface_type $a
30     interface_type ::= class_or_interface_type $a
31     array_type ::= primitive_type $a dims $b
32                  | name $a dims $b
33     name ::= simple_name $a
34            | qualified_name $a
35     simple_name ::= IDENTIFIER $a
36     qualified_name ::= name $a DOT IDENTIFIER $b
37     compilation_unit ::= package_declaration_opt $a import_declarations_opt $b type_declarations_opt $c
38                        | error type_declarations_opt $c
39     package_declaration_opt ::= package_declaration $a
40                               |
41     import_declarations_opt ::= import_declarations $a
42                               |
43     type_declarations_opt ::= type_declarations $a
44                             |
45     import_declarations ::= import_declaration $a
46                           | import_declarations $a import_declaration $b
47     type_declarations ::= type_declaration $a
48                         | type_declarations $a type_declaration $b
49     package_declaration ::= PACKAGE name $a SEMICOLON
50     import_declaration ::= single_type_import_declaration $a
51                          | type_import_on_demand_declaration $a
52     single_type_import_declaration ::= IMPORT $a qualified_name $b SEMICOLON $c
53     type_import_on_demand_declaration ::= IMPORT $a name $b DOT MULT SEMICOLON $c
54     type_declaration ::= class_declaration $a
55                        | interface_declaration $a
56                        | SEMICOLON
57     modifiers_opt ::=
58                     | modifiers $a
59     modifiers ::= modifier $a
60                 | modifiers $a modifier $b
61     modifier ::= PUBLIC
62                | PROTECTED
63                | PRIVATE
64                | STATIC
65                | ABSTRACT
66                | FINAL
67                | NATIVE
68                | SYNCHRONIZED
69                | TRANSIENT
70                | VOLATILE
71                | STRICTFP
72     class_declaration ::= modifiers_opt $a CLASS $n IDENTIFIER $b super_opt $c interfaces_opt $d class_body $e
73     super ::= EXTENDS class_type $a
74     super_opt ::=
75                 | super $a
76     interfaces ::= IMPLEMENTS interface_type_list $a
77     interfaces_opt ::=
78                      | interfaces $a
79     interface_type_list ::= interface_type $a
80                           | interface_type_list $a COMMA interface_type $b
81     class_body ::= LBRACE $n class_body_declarations_opt $a RBRACE $b
82     class_body_declarations_opt ::=
83                                   | class_body_declarations $a
84     class_body_declarations ::= class_body_declaration $a
85                               | class_body_declarations $a class_body_declaration $b
86     class_body_declaration ::= class_member_declaration $a
87                              | static_initializer $a
88                              | constructor_declaration $a
89                              | block $a
90                              | SEMICOLON
91                              | error SEMICOLON
92                              | error LBRACE
93     class_member_declaration ::= field_declaration $a
94                                | method_declaration $a
95                                | modifiers_opt $a CLASS $n IDENTIFIER $b super_opt $c interfaces_opt $d class_body $e
96                                | interface_declaration $a
97     field_declaration ::= modifiers_opt $a type $b variable_declarators $c SEMICOLON $e
98     variable_declarators ::= variable_declarator $a
99                            | variable_declarators $a COMMA variable_declarator $b
100    variable_declarator ::= variable_declarator_id $a
101                          | variable_declarator_id $a EQ variable_initializer $b
102    variable_declarator_id ::= IDENTIFIER $a
103                             | variable_declarator_id $a LBRACK RBRACK
104    variable_initializer ::= expression $a
105                           | array_initializer $a
106    method_declaration ::= method_header $a method_body $b
107    method_header ::= modifiers_opt $a type $b IDENTIFIER $c LPAREN formal_parameter_list_opt $d RPAREN $g dims_opt $e throws_opt $f
108                    | modifiers_opt $a VOID $b IDENTIFIER $c LPAREN formal_parameter_list_opt $d RPAREN $g throws_opt $f
109    formal_parameter_list_opt ::=
110                                | formal_parameter_list $a
111    formal_parameter_list ::= formal_parameter $a
112                            | formal_parameter_list $a COMMA formal_parameter $b
113    formal_parameter ::= type $a variable_declarator_id $b
114                       | FINAL type $a variable_declarator_id $b
115    throws_opt ::=
116                 | throws $a
117    throws ::= THROWS class_type_list $a
118    class_type_list ::= class_type $a
119                      | class_type_list $a COMMA class_type $b
120    method_body ::= block $a
121                  | SEMICOLON
122    static_initializer ::= STATIC block $a
123    constructor_declaration ::= modifiers_opt $m simple_name $a LPAREN formal_parameter_list_opt $b RPAREN throws_opt $c constructor_body $d
124    constructor_body ::= LBRACE $n explicit_constructor_invocation $a block_statements $b RBRACE $d
125                       | LBRACE $n explicit_constructor_invocation $a RBRACE $d
126                       | LBRACE $n block_statements $a RBRACE $d
127                       | LBRACE $n RBRACE $d
128    explicit_constructor_invocation ::= THIS $a LPAREN argument_list_opt $b RPAREN SEMICOLON $c
129                                      | SUPER $a LPAREN argument_list_opt $b RPAREN SEMICOLON $c
130                                      | primary $a DOT THIS $n LPAREN argument_list_opt $b RPAREN SEMICOLON $c
131                                      | primary $a DOT SUPER $n LPAREN argument_list_opt $b RPAREN SEMICOLON $c
132    interface_declaration ::= modifiers_opt $a INTERFACE $n IDENTIFIER $b extends_interfaces_opt $c interface_body $d
133    extends_interfaces_opt ::=
134                             | extends_interfaces $a
135    extends_interfaces ::= EXTENDS interface_type $a
136                         | extends_interfaces $a COMMA interface_type $b
137    interface_body ::= LBRACE $n interface_member_declarations_opt $a RBRACE $d
138    interface_member_declarations_opt ::=
139                                        | interface_member_declarations $a
140    interface_member_declarations ::= interface_member_declaration $a
141                                    | interface_member_declarations $a interface_member_declaration $b
142    interface_member_declaration ::= constant_declaration $a
143                                   | abstract_method_declaration $a
144                                   | class_declaration $a
145                                   | interface_declaration $a
146                                   | SEMICOLON
147    constant_declaration ::= field_declaration $a
148    abstract_method_declaration ::= method_header $a SEMICOLON
149    array_initializer ::= LBRACE $n variable_initializers $a COMMA RBRACE $d
150                        | LBRACE $n variable_initializers $a RBRACE $d
151                        | LBRACE $n COMMA RBRACE $d
152                        | LBRACE $n RBRACE $d
153    variable_initializers ::= variable_initializer $a
154                            | variable_initializers $a COMMA variable_initializer $b
155    block ::= LBRACE $n block_statements_opt $a RBRACE $d
156            | error RBRACE $d
157    block_statements_opt ::=
158                           | block_statements $a
159    block_statements ::= block_statement $a
160                       | block_statements $a block_statement $b
161    block_statement ::= local_variable_declaration_statement $a
162                      | statement $a
163                      | class_declaration $a
164    local_variable_declaration_statement ::= local_variable_declaration $a SEMICOLON
165    local_variable_declaration ::= type $a variable_declarators $b
166                                 | FINAL type $a variable_declarators $b
167    statement ::= statement_without_trailing_substatement $a
168                | labeled_statement $a
169                | if_then_statement $a
170                | if_then_else_statement $a
171                | while_statement $a
172                | for_statement $a
173                | error SEMICOLON $a
174    statement_no_short_if ::= statement_without_trailing_substatement $a
175                            | labeled_statement_no_short_if $a
176                            | if_then_else_statement_no_short_if $a
177                            | while_statement_no_short_if $a
178                            | for_statement_no_short_if $a
179    statement_without_trailing_substatement ::= block $a
180                                              | empty_statement $a
181                                              | expression_statement $a
182                                              | switch_statement $a
183                                              | do_statement $a
184                                              | break_statement $a
185                                              | continue_statement $a
186                                              | return_statement $a
187                                              | synchronized_statement $a
188                                              | throw_statement $a
189                                              | try_statement $a
190                                              | assert_statement $a
191    empty_statement ::= SEMICOLON $a
192    labeled_statement ::= IDENTIFIER $a COLON statement $b
193    labeled_statement_no_short_if ::= IDENTIFIER $a COLON statement_no_short_if $b
194    expression_statement ::= statement_expression $a SEMICOLON $d
195    statement_expression ::= assignment $a
196                           | preincrement_expression $a
197                           | predecrement_expression $a
198                           | postincrement_expression $a
199                           | postdecrement_expression $a
200                           | method_invocation $a
201                           | class_instance_creation_expression $a
202    if_then_statement ::= IF $n LPAREN expression $a RPAREN statement $b
203    if_then_else_statement ::= IF $n LPAREN expression $a RPAREN statement_no_short_if $b ELSE statement $c
204    if_then_else_statement_no_short_if ::= IF $n LPAREN expression $a RPAREN statement_no_short_if $b ELSE statement_no_short_if $c
205    switch_statement ::= SWITCH $n LPAREN expression $a RPAREN switch_block $b
206    switch_block ::= LBRACE switch_block_statement_groups $a switch_labels $b RBRACE
207                   | LBRACE switch_block_statement_groups $a RBRACE
208                   | LBRACE switch_labels $a RBRACE
209                   | LBRACE RBRACE
210    switch_block_statement_groups ::= switch_block_statement_group $a
211                                    | switch_block_statement_groups $a switch_block_statement_group $b
212    switch_block_statement_group ::= switch_labels $a block_statements $b
213    switch_labels ::= switch_label $a
214                    | switch_labels $a switch_label $b
215    switch_label ::= CASE $n constant_expression $a COLON $d
216                   | DEFAULT $n COLON $d
217    while_statement ::= WHILE $n LPAREN expression $a RPAREN statement $b
218    while_statement_no_short_if ::= WHILE $n LPAREN expression $a RPAREN statement_no_short_if $b
219    do_statement ::= DO $n statement $a WHILE LPAREN expression $b RPAREN SEMICOLON $d
220    for_statement ::= FOR $n LPAREN for_init_opt $a SEMICOLON expression_opt $b SEMICOLON $e for_update_opt $c RPAREN statement $d
221    for_statement_no_short_if ::= FOR $n LPAREN for_init_opt $a SEMICOLON expression_opt $b SEMICOLON $e for_update_opt $c RPAREN statement_no_short_if $d
222    for_init_opt ::=
223                   | for_init $a
224    for_init ::= statement_expression_list $a
225               | local_variable_declaration $a
226    for_update_opt ::=
227                     | for_update $a
228    for_update ::= statement_expression_list $a
229    statement_expression_list ::= statement_expression $a
230                                | statement_expression_list $a COMMA statement_expression $b
231    identifier_opt ::=
232                     | IDENTIFIER $a
233    break_statement ::= BREAK $n identifier_opt $a SEMICOLON $d
234    continue_statement ::= CONTINUE $n identifier_opt $a SEMICOLON $d
235    return_statement ::= RETURN $n expression_opt $a SEMICOLON $d
236    throw_statement ::= THROW $n expression $a SEMICOLON $d
237    synchronized_statement ::= SYNCHRONIZED $n LPAREN expression $a RPAREN block $b
238    try_statement ::= TRY $n block $a catches $b
239                    | TRY $n block $a catches_opt $b finally $c
240    catches_opt ::=
241                  | catches $a
242    catches ::= catch_clause $a
243              | catches $a catch_clause $b
244    catch_clause ::= CATCH $n LPAREN formal_parameter $a RPAREN block $b
245    finally ::= FINALLY block $a
246    assert_statement ::= ASSERT $x expression $a SEMICOLON $d
247                       | ASSERT $x expression $a COLON expression $b SEMICOLON $d
248    primary ::= primary_no_new_array $a
249              | array_creation_expression $a
250    primary_no_new_array ::= literal $a
251                           | THIS $a
252                           | LPAREN expression $a RPAREN
253                           | class_instance_creation_expression $a
254                           | field_access $a
255                           | method_invocation $a
256                           | array_access $a
257                           | primitive_type $a DOT CLASS $n
258                           | VOID $a DOT CLASS $n
259                           | array_type $a DOT CLASS $n
260                           | name $a DOT CLASS $n
261                           | name $a DOT THIS $n
262    class_instance_creation_expression ::= NEW $n class_type $a LPAREN argument_list_opt $b RPAREN $d
263                                         | NEW $n class_type $a LPAREN argument_list_opt $b RPAREN class_body $c
264                                         | primary $a DOT NEW simple_name $b LPAREN argument_list_opt $c RPAREN $d
265                                         | primary $a DOT NEW simple_name $b LPAREN argument_list_opt $c RPAREN class_body $d
266                                         | name $a DOT NEW simple_name $b LPAREN argument_list_opt $c RPAREN $d
267                                         | name $a DOT NEW simple_name $b LPAREN argument_list_opt $c RPAREN class_body $d
268    argument_list_opt ::=
269                        | argument_list $a
270    argument_list ::= expression $a
271                    | argument_list $a COMMA expression $b
272    array_creation_expression ::= NEW $n primitive_type $a dim_exprs $b dims_opt $c
273                                | NEW $n class_or_interface_type $a dim_exprs $b dims_opt $c
274                                | NEW $n primitive_type $a dims $b array_initializer $c
275                                | NEW $n class_or_interface_type $a dims $b array_initializer $c
276    dim_exprs ::= dim_expr $a
277                | dim_exprs $a dim_expr $b
278    dim_expr ::= LBRACK $x expression $a RBRACK $y
279    dims_opt ::=
280               | dims $a
281    dims ::= LBRACK RBRACK
282           | dims $a LBRACK RBRACK
283    field_access ::= primary $a DOT IDENTIFIER $b
284                   | SUPER $n DOT IDENTIFIER $a
285                   | name $a DOT SUPER $n DOT IDENTIFIER $b
286    method_invocation ::= name $a LPAREN argument_list_opt $b RPAREN $d
287                        | primary $a DOT IDENTIFIER $b LPAREN argument_list_opt $c RPAREN $d
288                        | SUPER $a DOT IDENTIFIER $b LPAREN argument_list_opt $c RPAREN $d
289                        | name $a DOT SUPER $n DOT IDENTIFIER $b LPAREN argument_list_opt $c RPAREN $d
290    array_access ::= name $a LBRACK expression $b RBRACK $d
291                   | primary_no_new_array $a LBRACK expression $b RBRACK $d
292    postfix_expression ::= primary $a
293                         | name $a
294                         | postincrement_expression $a
295                         | postdecrement_expression $a
296    postincrement_expression ::= postfix_expression $a PLUSPLUS $b
297    postdecrement_expression ::= postfix_expression $a MINUSMINUS $b
298    unary_expression ::= preincrement_expression $a
299                       | predecrement_expression $a
300                       | PLUS $b unary_expression $a
301                       | MINUS $b unary_expression $a
302                       | MINUS $b boundary_literal $a
303                       | unary_expression_not_plus_minus $a
304    preincrement_expression ::= PLUSPLUS $b unary_expression $a
305    predecrement_expression ::= MINUSMINUS $b unary_expression $a
306    unary_expression_not_plus_minus ::= postfix_expression $a
307                                      | COMP $b unary_expression $a
308                                      | NOT $b unary_expression $a
309                                      | cast_expression $a
310    cast_expression ::= LPAREN $p primitive_type $a dims_opt $b RPAREN unary_expression $c
311                      | LPAREN $p expression $a RPAREN unary_expression_not_plus_minus $b
312                      | LPAREN $p name $a dims $b RPAREN unary_expression_not_plus_minus $c
313    multiplicative_expression ::= unary_expression $a
314                                | multiplicative_expression $a MULT unary_expression $b
315                                | multiplicative_expression $a DIV unary_expression $b
316                                | multiplicative_expression $a MOD unary_expression $b
317    additive_expression ::= multiplicative_expression $a
318                          | additive_expression $a PLUS multiplicative_expression $b
319                          | additive_expression $a MINUS multiplicative_expression $b
320    shift_expression ::= additive_expression $a
321                       | shift_expression $a LSHIFT additive_expression $b
322                       | shift_expression $a RSHIFT additive_expression $b
323                       | shift_expression $a URSHIFT additive_expression $b
324    relational_expression ::= shift_expression $a
325                            | relational_expression $a LT shift_expression $b
326                            | relational_expression $a GT shift_expression $b
327                            | relational_expression $a LTEQ shift_expression $b
328                            | relational_expression $a GTEQ shift_expression $b
329                            | relational_expression $a INSTANCEOF reference_type $b
330    equality_expression ::= relational_expression $a
331                          | equality_expression $a EQEQ relational_expression $b
332                          | equality_expression $a NOTEQ relational_expression $b
333    and_expression ::= equality_expression $a
334                     | and_expression $a AND equality_expression $b
335    exclusive_or_expression ::= and_expression $a
336                              | exclusive_or_expression $a XOR and_expression $b
337    inclusive_or_expression ::= exclusive_or_expression $a
338                              | inclusive_or_expression $a OR exclusive_or_expression $b
339    conditional_and_expression ::= inclusive_or_expression $a
340                                 | conditional_and_expression $a ANDAND inclusive_or_expression $b
341    conditional_or_expression ::= conditional_and_expression $a
342                                | conditional_or_expression $a OROR conditional_and_expression $b
343    conditional_expression ::= conditional_or_expression $a
344                             | conditional_or_expression $a QUESTION expression $b COLON conditional_expression $c
345    assignment_expression ::= conditional_expression $a
346                            | assignment $a
347    assignment ::= left_hand_side $a assignment_operator $b assignment_expression $c
348    left_hand_side ::= name $a
349                     | field_access $a
350                     | array_access $a
351    assignment_operator ::= EQ
352                          | MULTEQ
353                          | DIVEQ
354                          | MODEQ
355                          | PLUSEQ
356                          | MINUSEQ
357                          | LSHIFTEQ
358                          | RSHIFTEQ
359                          | URSHIFTEQ
360                          | ANDEQ
361                          | XOREQ
362                          | OREQ
363    expression_opt ::=
364                     | expression $a
365    expression ::= assignment_expression $a
366    constant_expression ::= expression $a


Types:

    SourceFile ::= goal
    polyglot.ast.Lit ::= literal
    polyglot.ast.Lit ::= boundary_literal
    TypeNode ::= type
               | primitive_type
               | numeric_type
    TypeNode ::= integral_type
               | floating_point_type
    TypeNode ::= reference_type
    TypeNode ::= class_or_interface_type
    TypeNode ::= class_type
               | interface_type
    TypeNode ::= array_type
    Name ::= simple_name
           | name
           | qualified_name
    SourceFile ::= compilation_unit
    PackageNode ::= package_declaration_opt
                  | package_declaration
    List ::= import_declarations_opt
           | import_declarations
    List ::= type_declarations_opt
           | type_declarations
    Import ::= import_declaration
    Import ::= single_type_import_declaration
    Import ::= type_import_on_demand_declaration
    ClassDecl ::= type_declaration
    Flags ::= modifiers_opt
            | modifiers
            | modifier
    ClassDecl ::= class_declaration
    TypeNode ::= super
               | super_opt
    List ::= interfaces
           | interfaces_opt
           | interface_type_list
    ClassBody ::= class_body
    List ::= class_body_declarations
           | class_body_declarations_opt
    List ::= class_body_declaration
           | class_member_declaration
    List ::= field_declaration
    List ::= variable_declarators
    VarDeclarator ::= variable_declarator
    VarDeclarator ::= variable_declarator_id
    Expr ::= variable_initializer
    MethodDecl ::= method_declaration
                 | method_header
    List ::= formal_parameter_list_opt
           | formal_parameter_list
    Formal ::= formal_parameter
    List ::= throws_opt
           | throws
    List ::= class_type_list
    Block ::= method_body
    Block ::= static_initializer
    ConstructorDecl ::= constructor_declaration
    Block ::= constructor_body
    ConstructorCall ::= explicit_constructor_invocation
    ClassDecl ::= interface_declaration
    List ::= extends_interfaces_opt
           | extends_interfaces
    ClassBody ::= interface_body
    List ::= interface_member_declarations_opt
           | interface_member_declarations
    List ::= interface_member_declaration
    List ::= constant_declaration
    MethodDecl ::= abstract_method_declaration
    ArrayInit ::= array_initializer
    List ::= variable_initializers
    Block ::= block
    List ::= block_statements_opt
           | block_statements
    List ::= block_statement
    List ::= local_variable_declaration_statement
    List ::= local_variable_declaration
    Stmt ::= statement
           | statement_no_short_if
    Stmt ::= statement_without_trailing_substatement
    Empty ::= empty_statement
    Labeled ::= labeled_statement
              | labeled_statement_no_short_if
    Stmt ::= expression_statement
    Expr ::= statement_expression
    If ::= if_then_statement
    If ::= if_then_else_statement
         | if_then_else_statement_no_short_if
    Switch ::= switch_statement
    List ::= switch_block
           | switch_block_statement_groups
    List ::= switch_block_statement_group
           | switch_labels
    Case ::= switch_label
    While ::= while_statement
            | while_statement_no_short_if
    Do ::= do_statement
    For ::= for_statement
          | for_statement_no_short_if
    List ::= for_init_opt
           | for_init
    List ::= for_update_opt
           | for_update
    List ::= statement_expression_list
    Name ::= identifier_opt
    Branch ::= break_statement
             | continue_statement
    Return ::= return_statement
    Throw ::= throw_statement
    Synchronized ::= synchronized_statement
    Try ::= try_statement
    List ::= catches_opt
           | catches
    Catch ::= catch_clause
    Block ::= finally
    Assert ::= assert_statement
    Expr ::= primary
           | primary_no_new_array
    Expr ::= class_instance_creation_expression
    List ::= argument_list_opt
           | argument_list
    NewArray ::= array_creation_expression
    List ::= dim_exprs
    Expr ::= dim_expr
    Integer ::= dims_opt
              | dims
    Field ::= field_access
    Call ::= method_invocation
    ArrayAccess ::= array_access
    Expr ::= postfix_expression
    Unary ::= postincrement_expression
            | postdecrement_expression
    Expr ::= unary_expression
           | unary_expression_not_plus_minus
    Unary ::= preincrement_expression
            | predecrement_expression
    Cast ::= cast_expression
    Expr ::= multiplicative_expression
           | additive_expression
    Expr ::= shift_expression
           | relational_expression
           | equality_expression
    Expr ::= and_expression
           | exclusive_or_expression
           | inclusive_or_expression
    Expr ::= conditional_and_expression
           | conditional_or_expression
    Expr ::= conditional_expression
           | assignment_expression
    Expr ::= assignment
    Expr ::= left_hand_side
    Assign.Operator ::= assignment_operator
    Expr ::= expression_opt
           | expression
    Expr ::= constant_expression

/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaKWLexer.gi:21:5:21:9:278:282: Informative: The terminal CONST is useless.
/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaKWLexer.gi:33:5:33:8:402:405: Informative: The terminal GOTO is useless.
/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaLexer.gi:24:5:24:13:389:397: Informative: The terminal SlComment is useless.
/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaLexer.gi:25:5:25:13:403:411: Informative: The terminal MlComment is useless.
/Users/rmfuhrer/eclipse/workspaces/imp-3.3-release/org.eclipse.imp.java.core/src/org/eclipse/imp/java/parser/JavaLexer.gi:26:5:26:14:417:426: Informative: The terminal DocComment is useless.


 IBM LALR Parser Generator              2.0.13 (May 27 2008)


Name map:

'$empty' ::= '$empty'
PLUSPLUS ::= ++
MINUSMINUS ::= --
EQEQ ::= ==
LTEQ ::= <=
GTEQ ::= >=
NOTEQ ::= !=
LSHIFT ::= <<
RSHIFT ::= >>
URSHIFT ::= >>>
PLUSEQ ::= +=
MINUSEQ ::= -=
MULTEQ ::= *=
DIVEQ ::= /=
ANDEQ ::= &=
OREQ ::= |=
XOREQ ::= ^=
MODEQ ::= %=
LSHIFTEQ ::= <<=
RSHIFTEQ ::= >>=
URSHIFTEQ ::= >>>=
OROR ::= ||
ANDAND ::= &&
PLUS ::= +
MINUS ::= -
NOT ::= !
MOD ::= %
XOR ::= ^
AND ::= &
MULT ::= *
OR ::= '|'
COMP ::= ~
DIV ::= /
GT ::= >
LT ::= <
LPAREN ::= (
RPAREN ::= )
LBRACE ::= {
RBRACE ::= }
LBRACK ::= [
RBRACK ::= ]
SEMICOLON ::= ;
QUESTION ::= ?
COLON ::= :
COMMA ::= ,
DOT ::= .
EQ ::= =
ABSTRACT ::= ABSTRACT
BOOLEAN ::= BOOLEAN
BREAK ::= BREAK
BYTE ::= BYTE
CASE ::= CASE
CATCH ::= CATCH
CHAR ::= CHAR
CLASS ::= CLASS
CONST ::= CONST
CONTINUE ::= CONTINUE
DEFAULT ::= DEFAULT
DO ::= DO
DOUBLE ::= DOUBLE
ELSE ::= ELSE
EXTENDS ::= EXTENDS
FALSE ::= FALSE
FINAL ::= FINAL
FINALLY ::= FINALLY
FLOAT ::= FLOAT
FOR ::= FOR
GOTO ::= GOTO
IF ::= IF
IMPLEMENTS ::= IMPLEMENTS
IMPORT ::= IMPORT
INSTANCEOF ::= INSTANCEOF
INT ::= INT
INTERFACE ::= INTERFACE
LONG ::= LONG
NATIVE ::= NATIVE
NEW ::= NEW
NULL ::= NULL
PACKAGE ::= PACKAGE
PRIVATE ::= PRIVATE
PROTECTED ::= PROTECTED
PUBLIC ::= PUBLIC
RETURN ::= RETURN
SHORT ::= SHORT
STATIC ::= STATIC
STRICTFP ::= STRICTFP
SUPER ::= SUPER
SWITCH ::= SWITCH
SYNCHRONIZED ::= SYNCHRONIZED
THIS ::= THIS
THROW ::= THROW
THROWS ::= THROWS
TRANSIENT ::= TRANSIENT
TRUE ::= TRUE
TRY ::= TRY
VOID ::= VOID
VOLATILE ::= VOLATILE
WHILE ::= WHILE
EOF_TOKEN ::= EOF_TOKEN
SlComment ::= SlComment
MlComment ::= MlComment
DocComment ::= DocComment
IDENTIFIER ::= IDENTIFIER
INTEGER_LITERAL ::= INTEGER_LITERAL
LONG_LITERAL ::= LONG_LITERAL
DOUBLE_LITERAL ::= DOUBLE_LITERAL
FLOAT_LITERAL ::= FLOAT_LITERAL
CHARACTER_LITERAL ::= CHARACTER_LITERAL
STRING_LITERAL ::= STRING_LITERAL
INTEGER_LITERAL_BD ::= INTEGER_LITERAL_BD
LONG_LITERAL_BD ::= LONG_LITERAL_BD
error ::= error
ASSERT ::= ASSERT
ERROR_TOKEN ::= ERROR_TOKEN
boundary_literal ::= boundary_literal
type ::= type
primitive_type ::= primitive_type
reference_type ::= reference_type
class_or_interface_type ::= class_or_interface_type
array_type ::= array_type
name ::= name
class_type ::= class_type
interface_type ::= interface_type
dims ::= dims
simple_name ::= simple_name
qualified_name ::= qualified_name
import_declarations ::= import_declarations
type_declarations ::= type_declarations
import_declaration ::= import_declaration
type_declaration ::= type_declaration
modifiers ::= modifiers
modifier ::= modifier
class_body ::= class_body
interface_type_list ::= interface_type_list
class_body_declarations ::= class_body_declarations
class_body_declaration ::= class_body_declaration
block ::= block
variable_declarators ::= variable_declarators
variable_declarator ::= variable_declarator
variable_declarator_id ::= variable_declarator_id
variable_initializer ::= variable_initializer
expression ::= expression
array_initializer ::= array_initializer
method_header ::= method_header
method_body ::= method_body
formal_parameter_list ::= formal_parameter_list
formal_parameter ::= formal_parameter
class_type_list ::= class_type_list
constructor_body ::= constructor_body
explicit_constructor_invocation ::= explicit_constructor_invocation
block_statements ::= block_statements
primary ::= primary
interface_body ::= interface_body
extends_interfaces ::= extends_interfaces
interface_member_declarations ::= interface_member_declarations
interface_member_declaration ::= interface_member_declaration
variable_initializers ::= variable_initializers
block_statement ::= block_statement
statement ::= statement
local_variable_declaration ::= local_variable_declaration
statement_without_trailing_substatement ::= 
    statement_without_trailing_substatement
statement_no_short_if ::= statement_no_short_if
statement_expression ::= statement_expression
postincrement_expression ::= postincrement_expression
postdecrement_expression ::= postdecrement_expression
method_invocation ::= method_invocation
class_instance_creation_expression ::= class_instance_creation_expression
switch_block ::= switch_block
switch_block_statement_groups ::= switch_block_statement_groups
switch_labels ::= switch_labels
switch_block_statement_group ::= switch_block_statement_group
switch_label ::= switch_label
constant_expression ::= constant_expression
statement_expression_list ::= statement_expression_list
catches ::= catches
finally ::= finally
catch_clause ::= catch_clause
primary_no_new_array ::= primary_no_new_array
field_access ::= field_access
array_access ::= array_access
argument_list ::= argument_list
dim_exprs ::= dim_exprs
dim_expr ::= dim_expr
postfix_expression ::= postfix_expression
unary_expression ::= unary_expression
unary_expression_not_plus_minus ::= unary_expression_not_plus_minus
multiplicative_expression ::= multiplicative_expression
additive_expression ::= additive_expression
shift_expression ::= shift_expression
relational_expression ::= relational_expression
equality_expression ::= equality_expression
and_expression ::= and_expression
exclusive_or_expression ::= exclusive_or_expression
inclusive_or_expression ::= inclusive_or_expression
conditional_and_expression ::= conditional_and_expression
conditional_or_expression ::= conditional_or_expression
conditional_expression ::= conditional_expression
assignment_expression ::= assignment_expression
left_hand_side ::= left_hand_side
assignment_operator ::= assignment_operator
goal ::= 
compilation_unit ::= 
literal ::= 
numeric_type ::= 
integral_type ::= 
floating_point_type ::= 
package_declaration_opt ::= 
import_declarations_opt ::= 
type_declarations_opt ::= 
package_declaration ::= 
single_type_import_declaration ::= 
type_import_on_demand_declaration ::= 
class_declaration ::= 
interface_declaration ::= 
modifiers_opt ::= 
super_opt ::= 
interfaces_opt ::= 
super ::= 
interfaces ::= 
class_body_declarations_opt ::= 
class_member_declaration ::= 
static_initializer ::= 
constructor_declaration ::= 
field_declaration ::= 
method_declaration ::= 
formal_parameter_list_opt ::= 
dims_opt ::= 
throws_opt ::= 
throws ::= 
argument_list_opt ::= 
extends_interfaces_opt ::= 
interface_member_declarations_opt ::= 
constant_declaration ::= 
abstract_method_declaration ::= 
block_statements_opt ::= 
local_variable_declaration_statement ::= 
labeled_statement ::= 
if_then_statement ::= 
if_then_else_statement ::= 
while_statement ::= 
for_statement ::= 
labeled_statement_no_short_if ::= 
if_then_else_statement_no_short_if ::= 
while_statement_no_short_if ::= 
for_statement_no_short_if ::= 
empty_statement ::= 
expression_statement ::= 
switch_statement ::= 
do_statement ::= 
break_statement ::= 
continue_statement ::= 
return_statement ::= 
synchronized_statement ::= 
throw_statement ::= 
try_statement ::= 
assert_statement ::= 
assignment ::= 
preincrement_expression ::= 
predecrement_expression ::= 
for_init_opt ::= 
expression_opt ::= 
for_update_opt ::= 
for_init ::= 
for_update ::= 
identifier_opt ::= 
catches_opt ::= 
array_creation_expression ::= 
cast_expression ::= 

 IBM LALR Parser Generator              2.0.13 (May 27 2008)


Scopes:

method_invocation ::= name DOT SUPER DOT IDENTIFIER LPAREN argument_list_opt  .
                      RPAREN 
for_statement_no_short_if ::= FOR LPAREN for_init_opt SEMICOLON 
                              expression_opt SEMICOLON for_update_opt  .RPAREN 
                              statement_no_short_if 
for_statement ::= FOR LPAREN for_init_opt SEMICOLON expression_opt SEMICOLON 
                  for_update_opt  .RPAREN statement 
class_instance_creation_expression ::= name DOT NEW simple_name LPAREN 
                                       argument_list_opt  .RPAREN 
class_instance_creation_expression ::= primary DOT NEW simple_name LPAREN 
                                       argument_list_opt  .RPAREN 
class_instance_creation_expression ::= name DOT NEW simple_name LPAREN 
                                       argument_list_opt  .RPAREN class_body 
class_instance_creation_expression ::= primary DOT NEW simple_name LPAREN 
                                       argument_list_opt  .RPAREN class_body 
method_invocation ::= SUPER DOT IDENTIFIER LPAREN argument_list_opt  .RPAREN 
method_invocation ::= primary DOT IDENTIFIER LPAREN argument_list_opt  .RPAREN 
do_statement ::= DO statement WHILE LPAREN expression  .RPAREN SEMICOLON 
if_then_else_statement_no_short_if ::= IF LPAREN expression RPAREN 
                                       statement_no_short_if  .ELSE 
                                       statement_no_short_if 
if_then_else_statement ::= IF LPAREN expression RPAREN statement_no_short_if  .
                           ELSE statement 
explicit_constructor_invocation ::= primary DOT SUPER LPAREN 
                                    argument_list_opt  .RPAREN SEMICOLON 
explicit_constructor_invocation ::= primary DOT THIS LPAREN 
                                    argument_list_opt  .RPAREN SEMICOLON 
for_statement_no_short_if ::= FOR LPAREN for_init_opt SEMICOLON 
                              expression_opt  .SEMICOLON for_update_opt RPAREN 
                              statement_no_short_if 
for_statement ::= FOR LPAREN for_init_opt SEMICOLON expression_opt  .SEMICOLON 
                  for_update_opt RPAREN statement 
class_instance_creation_expression ::= NEW class_type LPAREN 
                                       argument_list_opt  .RPAREN 
assert_statement ::= ASSERT expression COLON expression  .SEMICOLON 
class_instance_creation_expression ::= NEW class_type LPAREN 
                                       argument_list_opt  .RPAREN class_body 
array_access ::= primary_no_new_array LBRACK expression  .RBRACK 
array_access ::= name LBRACK expression  .RBRACK 
method_invocation ::= name LPAREN argument_list_opt  .RPAREN 
try_statement ::= TRY block catches_opt  .finally 
switch_block ::= LBRACE switch_block_statement_groups switch_labels  .RBRACE 
constructor_body ::= LBRACE explicit_constructor_invocation block_statements  .
                     RBRACE 
field_declaration ::= modifiers_opt type variable_declarators  .SEMICOLON 
conditional_expression ::= conditional_or_expression QUESTION expression  .
                           COLON conditional_expression 
synchronized_statement ::= SYNCHRONIZED LPAREN expression  .RPAREN block 
while_statement_no_short_if ::= WHILE LPAREN expression  .RPAREN 
                                statement_no_short_if 
while_statement ::= WHILE LPAREN expression  .RPAREN statement 
switch_statement ::= SWITCH LPAREN expression  .RPAREN switch_block 
if_then_statement ::= IF LPAREN expression  .RPAREN statement 
explicit_constructor_invocation ::= SUPER LPAREN argument_list_opt  .RPAREN 
                                    SEMICOLON 
explicit_constructor_invocation ::= THIS LPAREN argument_list_opt  .RPAREN 
                                    SEMICOLON 
if_then_else_statement_no_short_if ::= IF LPAREN expression  .RPAREN 
                                       statement_no_short_if ELSE 
                                       statement_no_short_if 
if_then_else_statement ::= IF LPAREN expression  .RPAREN statement_no_short_if 
                           ELSE statement 
for_statement_no_short_if ::= FOR LPAREN for_init_opt  .SEMICOLON 
                              expression_opt SEMICOLON for_update_opt RPAREN 
                              statement_no_short_if 
for_statement ::= FOR LPAREN for_init_opt  .SEMICOLON expression_opt SEMICOLON 
                  for_update_opt RPAREN statement 
dim_expr ::= LBRACK expression  .RBRACK 
primary_no_new_array ::= LPAREN expression  .RPAREN 
assert_statement ::= ASSERT expression  .SEMICOLON 
try_statement ::= TRY block  .catches 
throw_statement ::= THROW expression  .SEMICOLON 
return_statement ::= RETURN expression_opt  .SEMICOLON 
switch_label ::= CASE constant_expression  .COLON 
switch_block ::= LBRACE switch_labels  .RBRACE 
switch_block ::= LBRACE switch_block_statement_groups  .RBRACE 
block ::= LBRACE block_statements_opt  .RBRACE 
array_initializer ::= LBRACE variable_initializers  .RBRACE 
interface_body ::= LBRACE interface_member_declarations_opt  .RBRACE 
constructor_body ::= LBRACE block_statements  .RBRACE 
constructor_body ::= LBRACE explicit_constructor_invocation  .RBRACE 
class_body ::= LBRACE class_body_declarations_opt  .RBRACE 
cast_expression ::= LPAREN expression  .RPAREN unary_expression_not_plus_minus 
switch_block ::= LBRACE switch_block_statement_groups  .switch_labels RBRACE 
array_initializer ::= LBRACE variable_initializers  .COMMA RBRACE 
constructor_body ::= LBRACE explicit_constructor_invocation  .block_statements 
                     RBRACE 
assert_statement ::= ASSERT expression  .COLON expression SEMICOLON 
do_statement ::= DO statement  .WHILE LPAREN expression RPAREN SEMICOLON 
switch_block_statement_group ::= switch_labels  .block_statements 
expression_statement ::= statement_expression  .SEMICOLON 
local_variable_declaration_statement ::= local_variable_declaration  .SEMICOLON 
assignment ::= left_hand_side  .assignment_operator assignment_expression 
explicit_constructor_invocation ::= primary  .DOT SUPER LPAREN 
                                    argument_list_opt RPAREN SEMICOLON 
explicit_constructor_invocation ::= primary  .DOT THIS LPAREN argument_list_opt 
                                    RPAREN SEMICOLON 

