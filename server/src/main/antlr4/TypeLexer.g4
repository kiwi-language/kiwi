lexer grammar TypeLexer;

// Keywords

BOOLEAN:            'boolean';
DOUBLE:             'double';
LONG:               'long';
VOID:               'void';
NULL:               'null';
TIME:               'time';
PASSWORD:           'password';
STRING:             'string';
ANY:                'any';
NEVER:              'never';
R:                  'r';
C:                  'c';


// Separators

LPAREN:             '(';
RPAREN:             ')';
LBRACK:             '[';
RBRACK:             ']';
COMMA:              ',';
DOT:                '.';
NUM:                '#';
COLON:              ':';

// Operators

GT:                 '>';
LT:                 '<';
BITAND:             '&';
BITOR:              '|';
QUESTION:           '?';

// Java 8 tokens

ARROW:              '->';

// Additional symbols not defined in the lexical specification

// Whitespace
WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);

// Identifiers

IDENTIFIER:         Letter LetterOrDigit*;

// Literal
DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;

// Fragment rules

fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;
