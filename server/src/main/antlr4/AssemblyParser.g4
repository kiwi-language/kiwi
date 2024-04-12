parser grammar AssemblyParser;

options {tokenVocab=AssemblyLexer;}

compilationUnit
    : typeDeclaration+
    ;

typeDeclaration
    : classOrInterfaceModifier*
     (classDeclaration | enumDeclaration | interfaceDeclaration )
    | ';'
    ;

classDeclaration
    : (CLASS | STRUCT) IDENTIFIER typeParameters?
      (EXTENDS typeType)?
      (IMPLEMENTS typeList)?
      classBody
    ;

classBody
    : '{' classBodyDeclaration* '}'
    ;

typeList
    : typeType (',' typeType)*
    ;

classBodyDeclaration
    : modifier* memberDeclaration
    ;

enumDeclaration
    : ENUM IDENTIFIER (IMPLEMENTS typeList)? '{' enumConstants? ','? enumBodyDeclarations? '}'
    ;

enumConstants
    : enumConstant (',' enumConstant)*
    ;

enumConstant
    : IDENTIFIER arguments?
    ;

enumBodyDeclarations
    : ';' classBodyDeclaration*
    ;

interfaceDeclaration
    : INTERFACE IDENTIFIER typeParameters? (EXTENDS typeList)? interfaceBody
    ;

interfaceBody
    : '{' interfaceBodyDeclaration* '}'
    ;

interfaceBodyDeclaration
    : modifier* interfaceMemberDeclaration
    | ';'
    ;

interfaceMemberDeclaration
    : interfaceMethodDeclaration
    | genericInterfaceMethodDeclaration
    ;

interfaceMethodDeclaration
    : interfaceMethodModifier* interfaceCommonBodyDeclaration
    ;

interfaceMethodModifier
    : PUBLIC
    | ABSTRACT
    | DEFAULT
    | STATIC
    ;

genericInterfaceMethodDeclaration
    : interfaceMethodModifier* typeParameters interfaceCommonBodyDeclaration
    ;

interfaceCommonBodyDeclaration
    : typeTypeOrVoid IDENTIFIER formalParameters ('[' ']')* (THROWS qualifiedNameList)? ';'
    ;

memberDeclaration
    : methodDeclaration
    | genericMethodDeclaration
    | fieldDeclaration
    | constructorDeclaration
    | genericConstructorDeclaration
    ;

fieldDeclaration: typeType IDENTIFIER ';';

methodDeclaration
    : typeTypeOrVoid IDENTIFIER formalParameters methodBody
    ;

genericMethodDeclaration
    : typeParameters methodDeclaration
    ;

constructorDeclaration
    : IDENTIFIER formalParameters (THROWS qualifiedNameList)? constructorBody=block
    ;

genericConstructorDeclaration
    : typeParameters constructorDeclaration
    ;

typeParameters
    : '<' typeParameter (',' typeParameter)* '>'
    ;

qualifiedNameList
    : qualifiedName (',' qualifiedName)*
    ;

qualifiedName
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

typeParameter
    : IDENTIFIER (EXTENDS typeType)?
    ;


formalParameters
    : '(' formalParameterList? ')'
    ;

receiverParameter
    : typeType (IDENTIFIER '.')* THIS
    ;

formalParameterList
    : formalParameter (',' formalParameter)*
    ;

formalParameter
    : typeType IDENTIFIER
    ;

methodBody
    : block
    | ';'
    ;

block
    : '{' labeledStatement* '}'
    ;

labeledStatement: (IDENTIFIER ':')? statement;

statement
    : WHILE parExpression block
    | TRY block catchClause
    | SWITCH '{' branchCase* '}'
    | RETURN expression? ';'
    | THROW expression ';'
    | SEMI
    | statementExpression=expression ';'
    | methodCall ';'
    | NEW creator ';'
    | (THIS | IDENTIFIER) '.' IDENTIFIER
      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
      expression ';'
    | statementExpression=expression ';'
    ;

qualifiedFieldName: qualifiedName '.' IDENTIFIER;

creator: classOrInterfaceType arguments
//    : typeArguments createdName classCreatorRest
//    : createdName (arrayCreatorRest | classCreatorRest)
    ;

arrayCreatorRest
    : '[' ']' ('[' ']')* arrayInitializer
    ;

arrayInitializer
    : '{' (variableInitializer (',' variableInitializer)* (',')? )? '}'
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

createdName
    : IDENTIFIER typeArguments?
    | primitiveType
    ;

classCreatorRest
    : arguments
    ;


catchClause: CATCH '{' catchFields? '}';

catchFields: catchField (',' catchField)*;

catchField: IDENTIFIER ':' '{' (catchValue ',')* DEFAULT ':' expression '}';

catchValue: IDENTIFIER ':' expression;

branchCase
    : switchLabel block
    ;

switchLabel
    : CASE (expression) '->'
    | DEFAULT '->'
    ;

parExpression
    : '(' expression ')'
    ;

expressionList
    : expression (',' expression)*
    ;

expression
    : primary
    | expression bop='.'
      (
         IDENTIFIER
       | THIS
       | SUPER superSuffix
//       | explicitGenericInvocation
      )
    | expression '[' expression ']'
    | '(' typeType ')' expression
    | expression postfix=('++' | '--')
    | prefix=('+'|'-'|'++'|'--') expression
    | prefix=('~'|'!') expression
    | expression bop=('*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=INSTANCEOF typeType
    | expression bop=('==' | '!=') expression
    | expression bop='&' expression
    | expression bop='^' expression
    | expression bop='|' expression
    | expression bop='&&' expression
    | expression bop='||' expression
    | <assoc=right> expression bop='?' expression ':' expression
//    | <assoc=right> expression
//      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
//      expression
    // Java 8 methodReference
    | expression '::' typeArguments? IDENTIFIER
//    | typeType '::' (typeArguments? IDENTIFIER | NEW)
//    | classType '::' typeArguments? NEW
    ;

primary
    : '(' expression ')'
    | THIS
    | literal
    | IDENTIFIER
//    | typeArguments (explicitGenericInvocationSuffix | THIS arguments)
    ;

explicitGenericInvocation
    : typeArguments explicitGenericInvocationSuffix
    ;

explicitGenericInvocationSuffix
    : SUPER superSuffix
    | IDENTIFIER arguments
    ;

superSuffix
    : arguments
    | '.' typeArguments? IDENTIFIER arguments?
    ;

arguments
    : '(' expressionList? ')'
    ;

classType
    : IDENTIFIER typeArguments?
    ;

methodCall
    : expression '.' IDENTIFIER '(' expressionList? ')'
    | THIS '(' expressionList? ')'
    | SUPER '(' expressionList? ')'
    ;

literal
    : integerLiteral
    | floatLiteral
    | CHAR_LITERAL
    | STRING_LITERAL
    | BOOL_LITERAL
    | NULL
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

typeTypeOrVoid
    : typeType
    | VOID
    ;

typeType
    : classOrInterfaceType
    | primitiveType
    | typeType ('|' typeType)+
    | typeType ('&' typeType)+
    | typeType '[' arrayKind ']'
    | '(' (typeType (',' typeType)*)? ')' '->' typeType
    | '[' typeType ',' typeType ']'
    ;

arrayKind: R | RW | C;

classOrInterfaceType: qualifiedName typeArguments?;

typeArguments
    : '<' typeType (',' typeType)* '>'
    ;

primitiveType
    : BOOLEAN
    | INT
    | DOUBLE
    | STRING
    | PASSWORD
    | TIME
    | NULL
    | VOID
    ;

modifier
    : classOrInterfaceModifier
    | NATIVE
    | READONLY
    | CHILD
    | TITLE
    ;

classOrInterfaceModifier
    : PUBLIC
    | PROTECTED
    | PRIVATE
    | STATIC
    | ABSTRACT
    ;