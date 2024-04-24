parser grammar TypeParser;

options {tokenVocab = TypeLexer;}

type
    : classType
    | variableType
    | primitiveType
    | '(' typeList ')' '->' type
    | type ('|' type)+
    | type ('&' type)+
    | '[' type ',' type ']'
    | elementType=type '[' arrayKind? ']'
    | NEVER
    | ANY
    | '#' qualifiedName
    ;

arrayKind: R | C;

classType: qualifiedName typeArguments?;

variableType: '?' IDENTIFIER;

typeArguments: '<' typeList '>';

primitiveType
    : LONG
    | DOUBLE
    | TIME
    | STRING
    | PASSWORD
    | NULL
    | VOID
    | BOOLEAN
    ;

typeList: type (',' type) *;

qualifiedName: IDENTIFIER ('.' IDENTIFIER)*;
