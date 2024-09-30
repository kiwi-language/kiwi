parser grammar MetaVMParser;

options { tokenVocab=MetaVMLexer; }

primary
    : '(' expression ')'
    | THIS
    | SUPER
    | literal
    | identifier
    | typeTypeOrVoid '.' CLASS
    | nonWildcardTypeArguments (explicitGenericInvocationSuffix | THIS arguments)
    ;

expression
    : primary
    | typeType bop='.' identifier
    | expression bop='.'
      (
         identifier
       | methodCall
       | THIS
       | NEW nonWildcardTypeArguments? innerCreator
       | SUPER superSuffix
       | explicitGenericInvocation
      )
    | expression '[' expression ']'
    | list
    | allMatch
    | expression AS IDENTIFIER
    | methodCall
    | NEW creator
    | '(' annotation* typeType ('&' typeType)* ')' expression
    | expression postfix=('++' | '--')
    | prefix=('+'|'-'|'++'|'--') expression
    | prefix=('~'|'!') expression
    | expression bop=('*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=INSTANCEOF (typeType | pattern)
    | expression bop=('=' | '!=') expression
    | expression bop='&' expression
    | expression bop='^' expression
    | expression bop='|' expression
    | expression bop=AND expression
    | expression bop=OR expression
    | <assoc=right> expression bop='?' expression ':' expression
    ;

allMatch: ALL_MATCH '(' expression ',' expression ')';

list: '[' expressionList? ']';

// Java17
pattern
    : variableModifier* typeType annotation* identifier
    ;


variableModifier
    : FINAL
    | annotation
    ;


innerCreator
    : identifier nonWildcardTypeArgumentsOrDiamond? classCreatorRest
    ;

creator
    : nonWildcardTypeArguments createdName classCreatorRest
    | createdName (arrayCreatorRest | classCreatorRest)
    ;


nonWildcardTypeArgumentsOrDiamond
    : '<' '>'
    | nonWildcardTypeArguments
    ;

explicitGenericInvocation
    : nonWildcardTypeArguments explicitGenericInvocationSuffix
    ;

classCreatorRest
    : arguments
    ;


arrayCreatorRest
    : '[' (']' ('[' ']')* arrayInitializer | expression ']' ('[' expression ']')* ('[' ']')*)
    ;


arrayInitializer
    : '{' (variableInitializer (',' variableInitializer)* (',')? )? '}'
    ;


variableInitializer
    : arrayInitializer
    | expression
    ;


createdName
    : identifier typeArgumentsOrDiamond? ('.' identifier typeArgumentsOrDiamond?)*
    | primitiveType
    ;


typeArgumentsOrDiamond
    : '<' '>'
    | typeArguments
    ;


methodCall
    : identifier '(' expressionList? ')'
    | THIS '(' expressionList? ')'
    | SUPER '(' expressionList? ')'
    ;


expressionList
    : expression (',' expression)*
    ;

explicitGenericInvocationSuffix
    : SUPER superSuffix
    | identifier arguments
    ;


superSuffix
    : arguments
    | '.' typeArguments? identifier arguments?
    ;

arguments
    : '(' expressionList? ')'
    ;

elementValueArrayInitializer
    : '{' (elementValue (',' elementValue)*)? (',')? '}'
    ;

literal
    : integerLiteral
    | floatLiteral
    | CHAR_LITERAL
    | STRING_LITERAL
    | BOOL_LITERAL
    | NULL_LITERAL
    | TEXT_BLOCK // Java17
    ;


integerLiteral
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | OCT_LITERAL
    | BINARY_LITERAL
    ;

floatLiteral
    : FLOAT_LITERAL
    | HEX_FLOAT_LITERAL
    ;

identifier
    : IDENTIFIER
    | MODULE
    | OPEN
    | REQUIRES
    | EXPORTS
    | OPENS
    | TO
    | USES
    | PROVIDES
    | WITH
    | TRANSITIVE
    | YIELD
    | SEALED
    | PERMITS
    | RECORD
    | VAR
    ;

typeIdentifier  // Identifiers that are not restricted for type declarations
    : IDENTIFIER
    | MODULE
    | OPEN
    | REQUIRES
    | EXPORTS
    | OPENS
    | TO
    | USES
    | PROVIDES
    | WITH
    | TRANSITIVE
    | SEALED
    | PERMITS
    | RECORD
    ;

typeTypeOrVoid
    : typeType
    | VOID
    ;

elementValuePairs
    : elementValuePair (',' elementValuePair)*
    ;

elementValuePair
    : identifier '=' elementValue
    ;

elementValue
    : expression
    | annotation
    | elementValueArrayInitializer
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

altAnnotationQualifiedName
    : (identifier DOT)* '@' identifier
    ;

annotation
    : ('@' qualifiedName | altAnnotationQualifiedName) ('(' ( elementValuePairs | elementValue )? ')')?
    ;

typeArguments
    : '<' typeType (',' typeType)* '>'
    ;

classOrInterfaceType
    : qualifiedName typeArguments?
    ;

typeType
    : NEVER
    | ANY
    | primitiveType
    | parType
//    | variableType
//    | '#' qualifiedName
    | typeType ('|' typeType)+
    | typeType ('&' typeType)+
    | '[' typeType ',' typeType ']'
    | '(' typeList? ')' '->' typeType
    | typeType arrayKind
    | classOrInterfaceType
    ;

parType: '(' typeType ')';

arrayKind: R | '[' ']' | C | V;

primitiveType
    : BOOLEAN
    | STRING
    | LONG
    | CHAR
    | DOUBLE
    | TIME
    | PASSWORD
    | VOID
    | NULL_LITERAL
    ;

nonWildcardTypeArguments
    : '<' typeList '>'
    ;

typeList
    : typeType (',' typeType)*
    ;