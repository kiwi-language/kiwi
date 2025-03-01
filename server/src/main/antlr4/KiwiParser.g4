parser grammar KiwiParser;

options {tokenVocab=KiwiLexer;}

compilationUnit
    : packageDeclaration? importDeclaration* typeDeclaration+
    ;

packageDeclaration: PACKAGE qualifiedName;

importDeclaration: IMPORT qualifiedName;

typeDeclaration
    : classOrInterfaceModifier*
     (classDeclaration | enumDeclaration | interfaceDeclaration )
    ;

classDeclaration
    : annotation* CLASS identifier typeParameters?
      (':' typeList)?
      classBody
    ;

classBody
    : '{' classBodyDeclaration* '}'
    ;

typeList
    : type (',' type)*
    ;

classBodyDeclaration
    : modifier* memberDeclaration
    | staticBlock
    ;

staticBlock: STATIC block;

enumDeclaration
    : annotation* ENUM identifier (':' typeList)? '{' enumConstants? ','? enumBodyDeclarations? '}'
    ;

enumConstants
    : enumConstant (',' enumConstant)*
    ;

enumConstant
    : identifier arguments?
    ;

enumBodyDeclarations
    : ';' classBodyDeclaration*
    ;

interfaceDeclaration
    : annotation* INTERFACE identifier typeParameters? (':' typeList)? interfaceBody
    ;

interfaceBody
    : '{' interfaceBodyDeclaration* '}'
    ;

interfaceBodyDeclaration
    : modifier* interfaceMemberDeclaration
    ;

interfaceMemberDeclaration
    : interfaceMethodDeclaration
//    | genericInterfaceMethodDeclaration
    ;

interfaceMethodDeclaration
    : interfaceMethodModifier* FUNC identifier typeParameters? formalParameters ('->' typeOrVoid)? (THROWS qualifiedNameList)?
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

memberDeclaration
    : methodDeclaration
//    | genericMethodDeclaration
    | fieldDeclaration
    | constructorDeclaration
    | classDeclaration
//    | genericConstructorDeclaration
    ;

fieldDeclaration: (VAR | VAL) identifier ':' type;

methodDeclaration
    :  FUNC identifier typeParameters? formalParameters ('->' typeOrVoid)? methodBody?
    ;

constructorDeclaration
    :  INIT formalParameters (THROWS qualifiedNameList)? constructorBody=block
    ;

typeParameters
    : '<' typeParameter (',' typeParameter)* '>'
    ;

qualifiedNameList
    : qualifiedName (',' qualifiedName)*
    ;

qualifiedName
    : identifier ('.' identifier)*
    ;

typeParameter
    : identifier (':' type)?
    ;


formalParameters
    : '(' formalParameterList? ')'
    ;

formalParameterList
    : formalParameter (',' formalParameter)*
    ;

formalParameter
    : identifier ':' type
    ;

methodBody: block
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
    | RETURN expression?
    | THROW expression
    | SEMI
    | statementExpression=expression
    | localVariableDeclaration
    ;

localVariableDeclaration: (VAR | VAL) identifier (':' type)? ('=' expression)?;

forControl
    : loopVariableDeclarators? ';' expression? ';' forUpdate=loopVariableUpdates?
    ;

loopVariableDeclarators
    : loopVariableDeclarator (',' loopVariableDeclarator)*
    ;

loopVariableDeclarator
    : type identifier '=' expression
    ;

loopVariableUpdates: loopVariableUpdate (',' loopVariableUpdate)*;

loopVariableUpdate: identifier '=' expression;

newExpr: NEW typeArguments? classOrInterfaceType arguments;

newArray: NEW type arrayKind;

arrayInitializer
    : '{' (variableInitializer (',' variableInitializer)* (',')? )? '}'
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

catchClause: CATCH '{' catchFields? '}';

catchFields: catchField (',' catchField)*;

catchField: identifier ':' '{' (catchValue ',')* DEFAULT ':' expression '}';

catchValue: identifier ':' expression;

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

//expression
//    : primary
//    | expression bop='.'
//      (
//         identifier
//       | THIS
//       | methodCall
//       | creator
//      )
//    | THIS '(' expressionList? ')'
//    | SUPER '(' expressionList? ')'
//    | expression '[' expression ']'
//    | creator
//    | expression AS castType=typeType
//    | expression postfix=('++' | '--' | '!!')
//    | prefix=('+'|'-'|'++'|'--') expression
//    | prefix=('~'|'!') expression
//    | expression bop=('*'|'/'|'%') expression
//    | expression bop=('+'|'-') expression
//    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression
//    | expression bop=('<=' | '>=' | '>' | '<') expression
//    | expression bop=INSTANCEOF typeType
//    | expression bop=('==' | '!=') expression
//    | expression bop='&' expression
//    | expression bop='^' expression
//    | expression bop='|' expression
//    | expression bop='&&' expression
//    | expression bop='||' expression
//    | <assoc=right> expression bop='?' expression ':' expression
//    | <assoc=right> expression
//      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
//      expression
//    | lambdaExpression
//    | identifier arguments
//;

//primary
//    : '(' expression ')'
//    | THIS
//    | literal
//    | identifier
//    ;

expression: assignment;

assignment: ternary assignmentSuffix*;

assignmentSuffix: op=('='|'+='|'-='|'%='|'>>='|'>>>='|'<<='|'&='|'|='|'^=') assignment;

ternary: disjunction ('?' disjunction ':' ternary)?;

disjunction: conjunction ('||' conjunction)*;

conjunction: bitor ('&&' bitor)*;

bitor: bitand ('|' bitand)*;

bitand: bitxor ('&' bitxor)*;

bitxor: equality ('^' equality)*;

equality: relational equalitySuffix*;

equalitySuffix: op=('=='|'!=') relational;

relational: isExpr relationalSuffix*;

relationalSuffix: op=('<'|'>'|'<='|'>') isExpr;

isExpr: shift (IS type)*;

shift: additive shiftSuffix*;

shiftSuffix: '>' '>' |'<' '<' | '>' '>' '>'  additive;

additive: multiplicative additiveSuffix*;

additiveSuffix: op=('+'|'-') multiplicative;

multiplicative: asExpr multiplicativeSuffix*;

multiplicativeSuffix: op=('*'|'/'|'%') asExpr;

asExpr: prefixExpr ('as' type)*;

prefixExpr: prefixOp* postfixExpr;

prefixOp: op=('~'|'-'|'+'|'++'|'--'|'!');

postfixExpr: primaryExpr postfixSuffix*;

postfixSuffix: op=('++' | '--' | '!!') | callSuffix | '.' newExpr | indexingSuffix | selectorSuffix;

callSuffix: typeArguments? arguments;

indexingSuffix: '[' expression ']';

selectorSuffix: '.' identifier ;

primaryExpr
    : '(' expression ')'
    | THIS
    | SUPER
    | newExpr
    | newArray
    | literal
    | identifier
    | lambdaExpression
    | NEW
    ;

arguments
    : '(' expressionList? ')'
    ;

identifier: IDENTIFIER | VALUE;

methodCall
    : identifier typeArguments? '(' expressionList? ')'
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

typeOrVoid: VOID | type;

type
    : classOrInterfaceType
    | primitiveType
    | ANY
    | NEVER
    | type ('|' type)+
    | type ('&' type)+
    | type arrayKind
    | '(' (type (',' type)*)? ')' '->' type
    | '[' type ',' type ']'
    ;

arrayKind: R | RW;

classOrInterfaceType: qualifiedName typeArguments?;

typeArguments
    : '<' type (',' type)* '>'
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
    | DELETED
    ;

classOrInterfaceModifier
    : PUBLIC
    | PROTECTED
    | PRIVATE
    | STATIC
    | ABSTRACT
    | VALUE
    ;

lambdaExpression
    : lambdaParameters '->' typeOrVoid lambdaBody
    ;

// Java8
lambdaParameters
    : '(' formalParameterList? ')'
    ;

// Java8
lambdaBody
    : block
    ;

annotation: '@' identifier ('(' (elementValuePairs | expression)? ')')?;

elementValuePairs
    : elementValuePair (',' elementValuePair)*
    ;

elementValuePair
    : identifier '=' expression
    ;
