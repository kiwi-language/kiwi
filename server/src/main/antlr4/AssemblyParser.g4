parser grammar AssemblyParser;

options {tokenVocab=AssemblyLexer;}

compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration+
    ;

packageDeclaration: PACKAGE qualifiedName ';';

importDeclaration: IMPORT qualifiedName ';';

typeDeclaration
    : classOrInterfaceModifier*
     (classDeclaration | enumDeclaration | interfaceDeclaration )
    | ';'
    ;

classDeclaration
    : annotation* (CLASS | STRUCT | RECORD) IDENTIFIER typeParameters?
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
    | staticBlock
    ;

staticBlock: STATIC block;

enumDeclaration
    : annotation* ENUM IDENTIFIER (IMPLEMENTS typeList)? '{' enumConstants? ','? enumBodyDeclarations? '}'
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
    : annotation* INTERFACE IDENTIFIER typeParameters? (EXTENDS typeList)? interfaceBody
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
//    | genericInterfaceMethodDeclaration
    ;

interfaceMethodDeclaration
    : interfaceMethodModifier* typeParameters? interfaceCommonBodyDeclaration
    ;

interfaceMethodModifier
    : PUBLIC
    | ABSTRACT
    | DEFAULT
    | STATIC
    ;

//genericInterfaceMethodDeclaration
//    : interfaceMethodModifier* typeParameters interfaceCommonBodyDeclaration
//    ;

interfaceCommonBodyDeclaration
    : typeTypeOrVoid IDENTIFIER formalParameters ('[' ']')* (THROWS qualifiedNameList)? ';'
    ;

memberDeclaration
    : methodDeclaration
//    | genericMethodDeclaration
    | fieldDeclaration
    | constructorDeclaration
    | classDeclaration
//    | genericConstructorDeclaration
    ;

fieldDeclaration: typeType IDENTIFIER ';';

methodDeclaration
    : typeParameters? typeTypeOrVoid IDENTIFIER formalParameters methodBody
    ;

//genericMethodDeclaration
//    : typeParameters methodDeclaration
//    ;

constructorDeclaration
    : typeParameters? IDENTIFIER formalParameters (THROWS qualifiedNameList)? constructorBody=block
    ;

//genericConstructorDeclaration
//    : typeParameters constructorDeclaration
//    ;

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
    : '{' statement* '}'
    ;

statement
    : WHILE parExpression block
    | FOR '(' forControl ')'  block
    | IF parExpression block (ELSE block)?
    | TRY block catchClause
    | SWITCH '{' branchCase* '}'
    | RETURN expression? ';'
    | THROW expression ';'
    | SEMI
    | statementExpression=expression ';'
    | localVariableDeclaration ';'
//    | statementExpression=expression ';'
//    | methodCall '!'? ';'
//    | functionCall ';'
    ;

localVariableDeclaration: VAR IDENTIFIER '=' expression | typeType IDENTIFIER ('=' expression)?;

allocator: classOrInterfaceType '{' allocatorFieldList? '}';

allocatorFieldList: allocatorField (',' allocatorField)*;

allocatorField: IDENTIFIER ':' expression;

forControl
    : loopVariableDeclarators? ';' expression? ';' forUpdate=loopVariableUpdates?
    ;

loopVariableDeclarators
    : loopVariableDeclarator (',' loopVariableDeclarator)*
    ;

loopVariableDeclarator
    : typeType IDENTIFIER '=' expression
    ;

loopVariableUpdates: loopVariableUpdate (',' loopVariableUpdate)*;

loopVariableUpdate: IDENTIFIER '=' expression;

qualifiedFieldName: qualifiedName '.' IDENTIFIER;

creator: typeArguments? classOrInterfaceType arguments
//    : typeArguments createdName classCreatorRest
//    : createdName (arrayCreatorRest | classCreatorRest)
      | typeType arrayKind
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
       | methodCall
       | NEW creator
//       | SUPER superSuffix
//       | explicitGenericInvocation
      )
    | THIS '(' expressionList? ')'
    | SUPER '(' expressionList? ')'
    | expression '[' expression ']'
    | NEW creator
    | '(' castType=typeType ')' expression
    | expression postfix=('++' | '--' | '!!')
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
    | <assoc=right> expression
      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
      expression
    | lambdaExpression
    // Java 8 methodReference
    | expression '::' typeArguments? IDENTIFIER
    | IDENTIFIER arguments
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
    : typeArguments? IDENTIFIER '(' expressionList? ')'
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

typeTypeOrVoid: VOID | typeType;

typeType
    : classOrInterfaceType
    | primitiveType
    | ANY
    | NEVER
    | typeType ('|' typeType)+
    | typeType ('&' typeType)+
    | typeType arrayKind
    | '(' (typeType (',' typeType)*)? ')' '->' typeType
    | '[' typeType ',' typeType ']'
    ;

arrayKind: R | RW;

classOrInterfaceType: qualifiedName typeArguments?;

typeArguments
    : '<' typeType (',' typeType)* '>'
    ;

primitiveType
    : BOOLEAN
    | BYTE
    | SHORT
    | INT
    | LONG
    | DOUBLE
    | STRING
    | PASSWORD
    | TIME
    | NULL
    | VOID
    | CHAR
    ;

modifier
    : classOrInterfaceModifier
    | NATIVE
    | READONLY
    | TITLE
    | UNIQUE
    | DELETED
    ;

classOrInterfaceModifier
    : PUBLIC
    | PROTECTED
    | PRIVATE
    | STATIC
    | ABSTRACT
    ;

lambdaExpression
    : lambdaParameters ':' typeTypeOrVoid '->' lambdaBody
    ;

// Java8
lambdaParameters
    : '(' formalParameterList? ')'
    ;

// Java8
lambdaBody
    : block
    ;

indexField: IDENTIFIER ':' expression ';';

annotation: '@' IDENTIFIER ('(' (elementValuePairs | expression)? ')')?;

elementValuePairs
    : elementValuePair (',' elementValuePair)*
    ;

elementValuePair
    : IDENTIFIER '=' expression
    ;
