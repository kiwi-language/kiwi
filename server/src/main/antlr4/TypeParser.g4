parser grammar TypeParser;

options {tokenVocab = TypeLexer;}

type
    : NEVER
    | ANY
    | primitiveType
    | variableType
    | '#' qualifiedName
    | classType
    | elementType=type '[' arrayKind? ']'
    | type ('|' type)+
    | type ('&' type)+
    | '[' type ',' type ']'
    | '(' typeList? ')' '->' type
    ;

methodRef: classType '.' IDENTIFIER typeArguments?;

simpleMethodRef: IDENTIFIER typeArguments?;

arrayKind: R | C | V;

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

functionSignature: type IDENTIFIER typeParameterList? '(' parameterList? ')';

parameterList: parameter (',' parameter)*;

parameter: type IDENTIFIER;

typeParameterList: '<' typeParameter (',' typeParameter)* '>';

typeParameter: IDENTIFIER;