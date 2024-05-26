parser grammar TypeParser;

options {tokenVocab = TypeLexer;}

type
    : classType
    | variableType
    | primitiveType
    | '(' typeList? ')' '->' type
    | type ('|' type)+
    | type ('&' type)+
    | '[' type ',' type ']'
    | elementType=type '[' arrayKind? ']'
    | NEVER
    | ANY
    | '#' qualifiedName
    ;

methodRef: classType '.' IDENTIFIER typeArguments?;

arrayKind: R | C;

classType: qualifiedName typeArguments? (':' DECIMAL_LITERAL)?;

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
