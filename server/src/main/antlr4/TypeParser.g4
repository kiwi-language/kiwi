parser grammar TypeParser;

options {tokenVocab = TypeLexer;}

unit: type EOF;

type
    : NEVER
    | ANY
    | primitiveType
    | variableType
    | '#' qualifiedName
    | classType
    | parType
    | elementType=type arrayKind
    | type ('|' type)+
    | type ('&' type)+
    | '[' type ',' type ']'
    | '(' typeList? ')' '->' type
    ;

parType: '(' type ')';

genericDeclarationRef: classType | methodRef | functionRef;

methodRef: classType '::' IDENTIFIER typeArguments?;

functionRef: FUNC IDENTIFIER typeArguments?;

simpleMethodRef: IDENTIFIER typeArguments?;

arrayKind: R | C | V | '[' ']';

classType: qualifiedName typeArguments? (':' DECIMAL_LITERAL)?;

variableType: '@' IDENTIFIER;

typeArguments: '<' typeList '>';

primitiveType
    : LONG
    | INT
    | DOUBLE
    | CHAR
    | SHORT
    | BYTE
    | TIME
    | STRING
    | PASSWORD
    | NULL
    | VOID
    | BOOLEAN
    | FLOAT
    ;

typeList: type (',' type) *;

qualifiedName: IDENTIFIER ('.' IDENTIFIER)*;

functionSignature: type IDENTIFIER typeParameterList? '(' parameterList? ')';

parameterList: parameter (',' parameter)*;

parameter: type IDENTIFIER;

typeParameterList: '<' typeParameter (',' typeParameter)* '>';

typeParameter: IDENTIFIER;