lexer grammar KiwiLexer;

// keywords

ABSTRACT:           'abstract';
BOOL:               'bool';
CASE:               'case';
DEFAULT:            'default';
CATCH:              'catch';
STRING:             'string';
CLASS:              'class';
BEAN:               'bean';
NULL:               'null';
PACKAGE:            'package';
IMPORT:             'import';
PASSWORD:           'password';
FLOAT:              'float';
DOUBLE:             'double';
ELSE:               'else';
ENUM:               'enum';
FINALLY:            'finally';
FOR:                'for';
IF:                 'if';
IS:                 'is';
BYTE:               'byte';
SHORT:              'short';
INT:                'int';
LONG:               'long';
CHAR:               'char';
INTERFACE:          'interface';
NATIVE:             'native';
NEW:                'new';
PRIV:               'priv';
PROT:               'prot';
PUB:                'pub';
RETURN:             'return';
STATIC:             'static';
SUPER:              'super';
SWITCH:             'switch';
THIS:               'this';
THROW:              'throw';
TIME:               'time';
THROWS:             'throws';
TRY:                'try';
VOID:               'void';
WHILE:              'while';
DO:                 'do';
ANY:                'any';
NEVER:              'never';
DELETED:            'deleted';
FN:                 'fn';
VALUE:              'value';
VAL:                'val';
VAR:                'var';
AS:                 'as';
IN:                 'in';
CONTINUE:           'continue';
BREAK:              'break';
TEMP:               'temp';

// Operators

ASSIGN:             '=';
GT:                 '>';
LT:                 '<';
BANG:               '!';
BANGBANG:           '!!';
TILDE:              '~';
QUESTION:           '?';
COLON:              ':';
EQUAL:              '==';
LE:                 '<=';
GE:                 '>=';
NOTEQUAL:           '!=';
AND:                '&&';
OR:                 '||';
INC:                '++';
DEC:                '--';
ADD:                '+';
SUB:                '-';
MUL:                '*';
DIV:                '/';
BITAND:             '&';
BITOR:              '|';
CARET:              '^';
MOD:                '%';
ADD_ASSIGN:         '+=';
SUB_ASSIGN:         '-=';
MUL_ASSIGN:         '*=';
DIV_ASSIGN:         '/=';
AND_ASSIGN:         '&=';
OR_ASSIGN:          '|=';
XOR_ASSIGN:         '^=';
MOD_ASSIGN:         '%=';
LSHIFT_ASSIGN:      '<<=';
RSHIFT_ASSIGN:      '>>=';
URSHIFT_ASSIGN:     '>>>=';

// Separators

LPAREN:             '(';
RPAREN:             ')';
LBRACE:             '{';
RBRACE:             '}';
LBRACK:             '[';
RBRACK:             ']';
SEMI:               ';';
COMMA:              ',';
DOT:                '.';

// Java 8 tokens

ARROW:              '->';
COLONCOLON:         '::';

AT:                 '@';
ELLIPSIS:           '...';

// Literals

DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
HEX_LITERAL:        '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?;
OCT_LITERAL:        '0' '_'* [0-7] ([0-7_]* [0-7])? [lL]?;
BINARY_LITERAL:     '0' [bB] [01] ([01_]* [01])? [lL]?;

FLOAT_LITERAL:      Digits '.' Digits ExponentPart? [fFdD]?
             |       Digits (ExponentPart [fFdD]? | [fFdD])
             ;

HEX_FLOAT_LITERAL:  '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [fFdD]?;

BOOL_LITERAL:       'true'
            |       'false'
            ;

CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';

STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';

TEXT_BLOCK:         '"""' [ \t]* [\r\n] (. | EscapeSequence)*? '"""';


// array kinds

R: '[r]';
RW: '[]';

// Identifiers

IDENTIFIER:         Letter LetterOrDigit*;

// fragments


fragment ExponentPart
    : [eE] [+-]? Digits
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

// Whitespace and comments


WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);