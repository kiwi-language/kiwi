parser grammar KiwiParser;

options {tokenVocab=KiwiLexer;}

compilationUnit
    : packageDeclaration? importDeclaration* topLevTypeDecl+
    ;

packageDeclaration: PACKAGE qualifiedName;

importDeclaration: IMPORT qualifiedName;

topLevTypeDecl: classOrInterfaceModifier* typeDeclaration;

typeDeclaration: (classDeclaration | enumDeclaration | interfaceDeclaration )
    ;

classDeclaration
    : annotation* CLASS identifier typeParameters?
      ( '(' (initParameter (',' initParameter)* ) ')')?
      (':' type arguments? (',' type)* )?
      classBody?
    ;

initParameter:  (modifier* fieldDeclaration) | formalParameter ;

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
    : annotation* ENUM identifier
     ( '(' (initParameter (',' initParameter)* ) ')')?
     (':' typeList)? '{' enumConstants? ','? enumBodyDeclarations? '}'
    ;

enumConstants
    : enumConstant (',' enumConstant)*
    ;

enumConstant
    : identifier arguments? classBody?
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
    : interfaceMethodModifier* FN identifier typeParameters? formalParameters ('->' typeOrVoid)? (THROWS qualifiedNameList)?
    ;

interfaceMethodModifier
    : PUB
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
    | block
    | typeDeclaration
//    | genericConstructorDeclaration
    ;

fieldDeclaration: (VAR | VAL) identifier (':' type)? ('=' expression)?;

methodDeclaration
    :  FN identifier typeParameters? formalParameters ('->' typeOrVoid)? methodBody?
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
    : WHILE parExpression statement
    | DO statement WHILE parExpression
    | FOR '(' forControl ')' statement
    | IF parExpression statement (ELSE statement)?
    | TRY block catchClause+
    | SWITCH '{' branchCase* '}'
    | RETURN expression?
    | THROW expression
    | BREAK identifier?
    | CONTINUE identifier?
    | identifier ':' statement
    | SEMI
    | statementExpression=expression
    | localVariableDeclaration
    | block
    ;

localVariableDeclaration: (VAR | VAL) identifier (':' type)? ('=' expression)?;

forControl: loopVariable IN expression;

loopVariable: identifier (':' type)?;

loopVariableDeclarators
    : loopVariableDeclarator (',' loopVariableDeclarator)*
    ;

loopVariableDeclarator
    : type identifier '=' expression
    ;

loopVariableUpdates: loopVariableUpdate (',' loopVariableUpdate)*;

loopVariableUpdate: identifier '=' expression;

anonClassExpr: NEW classType arguments classBody;

newArray: NEW type arrayKind arrayInitializer?;

arrayInitializer
    : '{' (variableInitializer (',' variableInitializer)* (',')? )? '}'
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

catchClause: CATCH '(' identifier ':' type ')' block;

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

conjunction: range ('&&' range)*;

range: bitor (ELLIPSIS bitor)?;

bitor: bitand ('|' bitand)*;

bitand: bitxor ('&' bitxor)*;

bitxor: equality ('^' equality)*;

equality: relational equalitySuffix*;

equalitySuffix: op=('=='|'!=') relational;

relational: isExpr relationalSuffix*;

relationalSuffix: op=('<'|'>'|'<='|'>'|'>=') isExpr;

isExpr: shift isSuffix*;

isSuffix: (IS (typePtn | type));

typePtn: type identifier;

shift: additive shiftSuffix*;

shiftSuffix: ('>' '>' |'<' '<' | '>' '>' '>')  additive;

additive: multiplicative additiveSuffix*;

additiveSuffix: op=('+'|'-') multiplicative;

multiplicative: asExpr multiplicativeSuffix*;

multiplicativeSuffix: op=('*'|'/'|'%') asExpr;

asExpr: prefixExpr ('as' type)*;

prefixExpr: prefixOp* postfixExpr;

prefixOp: op=('~'|'-'|'+'|'++'|'--'|'!');

postfixExpr: primaryExpr postfixSuffix*;

postfixSuffix: op=('++' | '--' | '!!') | callSuffix | '.' anonClassExpr | indexingSuffix | selectorSuffix | typeArguments;

callSuffix: arguments;

indexingSuffix: '[' expression ']';

selectorSuffix: '.' (identifier | THIS) ;

primaryExpr
    : '(' expression ')'
    | THIS
    | SUPER
    | anonClassExpr
    | newArray
    | literal
    | identifier
    | lambdaExpression
    ;

arguments
    : '(' expressionList? ')'
    ;

identifier: IDENTIFIER | VALUE | INIT | TEMP;

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

type: unionType;

unionType: intersectionType ('|' intersectionType)*;

intersectionType: postfixType ('&' postfixType)*;

postfixType: atomicType typeSuffix*;

typeSuffix: arrayKind | '?';

atomicType: classType | primitiveType | functionType | uncertainType;

primitiveType
    : BOOL
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
    | ANY
    | NEVER
    ;

functionType: '(' (type (',' type)*)? ')' '->' type;

uncertainType: '[' type ',' type ']';

arrayKind: R | RW;

classType: classTypePart ('.' classTypePart)*;

classTypePart: identifier typeArguments?;

typeArguments
    : '<' type (',' type)* '>'
    ;

modifier
    : classOrInterfaceModifier
    | NATIVE
    | DELETED
    ;

classOrInterfaceModifier
    : PUB
    | PROT
    | PRIV
    | STATIC
    | ABSTRACT
    | VALUE
    | TEMP
    ;

lambdaExpression
    : lambdaParameters '->' (typeOrVoid? lambdaBody | expression)
    ;

lambdaParameters
    : '(' lambdaParameterList? ')' | identifier
    ;

lambdaParameterList
    : lambdaParameter (',' lambdaParameter)*;

lambdaParameter: identifier (':' type)?;

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
