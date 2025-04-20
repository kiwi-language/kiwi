// Generated from KiwiParser.g4 by ANTLR 4.13.2
package org.metavm.compiler.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class KiwiParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ABSTRACT=1, BOOL=2, CASE=3, DEFAULT=4, CATCH=5, STRING=6, CLASS=7, TIME=8, 
		NULL=9, PACKAGE=10, IMPORT=11, PASSWORD=12, DOUBLE=13, ELSE=14, ENUM=15, 
		FINALLY=16, FOR=17, IF=18, IS=19, BYTE=20, SHORT=21, INT=22, LONG=23, 
		CHAR=24, INTERFACE=25, NATIVE=26, NEW=27, PRIV=28, PROT=29, PUB=30, RETURN=31, 
		STATIC=32, SUPER=33, SWITCH=34, THIS=35, THROW=36, THROWS=37, TRY=38, 
		VOID=39, WHILE=40, DO=41, ANY=42, NEVER=43, DELETED=44, FN=45, VALUE=46, 
		VAL=47, VAR=48, AS=49, INIT=50, IN=51, CONTINUE=52, BREAK=53, TEMP=54, 
		ASSIGN=55, GT=56, LT=57, BANG=58, BANGBANG=59, TILDE=60, QUESTION=61, 
		COLON=62, EQUAL=63, LE=64, GE=65, NOTEQUAL=66, AND=67, OR=68, INC=69, 
		DEC=70, ADD=71, SUB=72, MUL=73, DIV=74, BITAND=75, BITOR=76, CARET=77, 
		MOD=78, ADD_ASSIGN=79, SUB_ASSIGN=80, MUL_ASSIGN=81, DIV_ASSIGN=82, AND_ASSIGN=83, 
		OR_ASSIGN=84, XOR_ASSIGN=85, MOD_ASSIGN=86, LSHIFT_ASSIGN=87, RSHIFT_ASSIGN=88, 
		URSHIFT_ASSIGN=89, LPAREN=90, RPAREN=91, LBRACE=92, RBRACE=93, LBRACK=94, 
		RBRACK=95, SEMI=96, COMMA=97, DOT=98, ARROW=99, COLONCOLON=100, AT=101, 
		ELLIPSIS=102, DECIMAL_LITERAL=103, HEX_LITERAL=104, OCT_LITERAL=105, BINARY_LITERAL=106, 
		FLOAT_LITERAL=107, HEX_FLOAT_LITERAL=108, BOOL_LITERAL=109, CHAR_LITERAL=110, 
		STRING_LITERAL=111, TEXT_BLOCK=112, R=113, RW=114, IDENTIFIER=115, WS=116, 
		COMMENT=117, LINE_COMMENT=118;
	public static final int
		RULE_compilationUnit = 0, RULE_packageDeclaration = 1, RULE_importDeclaration = 2, 
		RULE_topLevTypeDecl = 3, RULE_typeDeclaration = 4, RULE_classDeclaration = 5, 
		RULE_classBody = 6, RULE_typeList = 7, RULE_classBodyDeclaration = 8, 
		RULE_staticBlock = 9, RULE_enumDeclaration = 10, RULE_enumConstants = 11, 
		RULE_enumConstant = 12, RULE_enumBodyDeclarations = 13, RULE_interfaceDeclaration = 14, 
		RULE_interfaceBody = 15, RULE_interfaceBodyDeclaration = 16, RULE_interfaceMemberDeclaration = 17, 
		RULE_interfaceMethodDeclaration = 18, RULE_interfaceMethodModifier = 19, 
		RULE_memberDeclaration = 20, RULE_fieldDeclaration = 21, RULE_methodDeclaration = 22, 
		RULE_constructorDeclaration = 23, RULE_typeParameters = 24, RULE_qualifiedNameList = 25, 
		RULE_qualifiedName = 26, RULE_typeParameter = 27, RULE_formalParameters = 28, 
		RULE_formalParameterList = 29, RULE_formalParameter = 30, RULE_methodBody = 31, 
		RULE_block = 32, RULE_statement = 33, RULE_localVariableDeclaration = 34, 
		RULE_forControl = 35, RULE_loopVariable = 36, RULE_loopVariableDeclarators = 37, 
		RULE_loopVariableDeclarator = 38, RULE_loopVariableUpdates = 39, RULE_loopVariableUpdate = 40, 
		RULE_anonClassExpr = 41, RULE_newArray = 42, RULE_arrayInitializer = 43, 
		RULE_variableInitializer = 44, RULE_catchClause = 45, RULE_catchFields = 46, 
		RULE_catchField = 47, RULE_catchValue = 48, RULE_branchCase = 49, RULE_switchLabel = 50, 
		RULE_parExpression = 51, RULE_expressionList = 52, RULE_expression = 53, 
		RULE_assignment = 54, RULE_assignmentSuffix = 55, RULE_ternary = 56, RULE_disjunction = 57, 
		RULE_conjunction = 58, RULE_range = 59, RULE_bitor = 60, RULE_bitand = 61, 
		RULE_bitxor = 62, RULE_equality = 63, RULE_equalitySuffix = 64, RULE_relational = 65, 
		RULE_relationalSuffix = 66, RULE_isExpr = 67, RULE_isSuffix = 68, RULE_typePtn = 69, 
		RULE_shift = 70, RULE_shiftSuffix = 71, RULE_additive = 72, RULE_additiveSuffix = 73, 
		RULE_multiplicative = 74, RULE_multiplicativeSuffix = 75, RULE_asExpr = 76, 
		RULE_prefixExpr = 77, RULE_prefixOp = 78, RULE_postfixExpr = 79, RULE_postfixSuffix = 80, 
		RULE_callSuffix = 81, RULE_indexingSuffix = 82, RULE_selectorSuffix = 83, 
		RULE_primaryExpr = 84, RULE_arguments = 85, RULE_identifier = 86, RULE_literal = 87, 
		RULE_integerLiteral = 88, RULE_floatLiteral = 89, RULE_typeOrVoid = 90, 
		RULE_type = 91, RULE_unionType = 92, RULE_intersectionType = 93, RULE_postfixType = 94, 
		RULE_typeSuffix = 95, RULE_atomicType = 96, RULE_primitiveType = 97, RULE_functionType = 98, 
		RULE_uncertainType = 99, RULE_arrayKind = 100, RULE_classType = 101, RULE_classTypePart = 102, 
		RULE_typeArguments = 103, RULE_modifier = 104, RULE_classOrInterfaceModifier = 105, 
		RULE_lambdaExpression = 106, RULE_lambdaParameters = 107, RULE_lambdaParameterList = 108, 
		RULE_lambdaParameter = 109, RULE_lambdaBody = 110, RULE_annotation = 111, 
		RULE_elementValuePairs = 112, RULE_elementValuePair = 113;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "packageDeclaration", "importDeclaration", "topLevTypeDecl", 
			"typeDeclaration", "classDeclaration", "classBody", "typeList", "classBodyDeclaration", 
			"staticBlock", "enumDeclaration", "enumConstants", "enumConstant", "enumBodyDeclarations", 
			"interfaceDeclaration", "interfaceBody", "interfaceBodyDeclaration", 
			"interfaceMemberDeclaration", "interfaceMethodDeclaration", "interfaceMethodModifier", 
			"memberDeclaration", "fieldDeclaration", "methodDeclaration", "constructorDeclaration", 
			"typeParameters", "qualifiedNameList", "qualifiedName", "typeParameter", 
			"formalParameters", "formalParameterList", "formalParameter", "methodBody", 
			"block", "statement", "localVariableDeclaration", "forControl", "loopVariable", 
			"loopVariableDeclarators", "loopVariableDeclarator", "loopVariableUpdates", 
			"loopVariableUpdate", "anonClassExpr", "newArray", "arrayInitializer", 
			"variableInitializer", "catchClause", "catchFields", "catchField", "catchValue", 
			"branchCase", "switchLabel", "parExpression", "expressionList", "expression", 
			"assignment", "assignmentSuffix", "ternary", "disjunction", "conjunction", 
			"range", "bitor", "bitand", "bitxor", "equality", "equalitySuffix", "relational", 
			"relationalSuffix", "isExpr", "isSuffix", "typePtn", "shift", "shiftSuffix", 
			"additive", "additiveSuffix", "multiplicative", "multiplicativeSuffix", 
			"asExpr", "prefixExpr", "prefixOp", "postfixExpr", "postfixSuffix", "callSuffix", 
			"indexingSuffix", "selectorSuffix", "primaryExpr", "arguments", "identifier", 
			"literal", "integerLiteral", "floatLiteral", "typeOrVoid", "type", "unionType", 
			"intersectionType", "postfixType", "typeSuffix", "atomicType", "primitiveType", 
			"functionType", "uncertainType", "arrayKind", "classType", "classTypePart", 
			"typeArguments", "modifier", "classOrInterfaceModifier", "lambdaExpression", 
			"lambdaParameters", "lambdaParameterList", "lambdaParameter", "lambdaBody", 
			"annotation", "elementValuePairs", "elementValuePair"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'bool'", "'case'", "'default'", "'catch'", "'string'", 
			"'class'", "'time'", "'null'", "'package'", "'import'", "'password'", 
			"'double'", "'else'", "'enum'", "'finally'", "'for'", "'if'", "'is'", 
			"'byte'", "'short'", "'int'", "'long'", "'char'", "'interface'", "'native'", 
			"'new'", "'priv'", "'prot'", "'pub'", "'return'", "'static'", "'super'", 
			"'switch'", "'this'", "'throw'", "'throws'", "'try'", "'void'", "'while'", 
			"'do'", "'any'", "'never'", "'deleted'", "'fn'", "'value'", "'val'", 
			"'var'", "'as'", "'init'", "'in'", "'continue'", "'break'", "'temp'", 
			"'='", "'>'", "'<'", "'!'", "'!!'", "'~'", "'?'", "':'", "'=='", "'<='", 
			"'>='", "'!='", "'&&'", "'||'", "'++'", "'--'", "'+'", "'-'", "'*'", 
			"'/'", "'&'", "'|'", "'^'", "'%'", "'+='", "'-='", "'*='", "'/='", "'&='", 
			"'|='", "'^='", "'%='", "'<<='", "'>>='", "'>>>='", "'('", "')'", "'{'", 
			"'}'", "'['", "']'", "';'", "','", "'.'", "'->'", "'::'", "'@'", "'...'", 
			null, null, null, null, null, null, null, null, null, null, "'[r]'", 
			"'[]'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSTRACT", "BOOL", "CASE", "DEFAULT", "CATCH", "STRING", "CLASS", 
			"TIME", "NULL", "PACKAGE", "IMPORT", "PASSWORD", "DOUBLE", "ELSE", "ENUM", 
			"FINALLY", "FOR", "IF", "IS", "BYTE", "SHORT", "INT", "LONG", "CHAR", 
			"INTERFACE", "NATIVE", "NEW", "PRIV", "PROT", "PUB", "RETURN", "STATIC", 
			"SUPER", "SWITCH", "THIS", "THROW", "THROWS", "TRY", "VOID", "WHILE", 
			"DO", "ANY", "NEVER", "DELETED", "FN", "VALUE", "VAL", "VAR", "AS", "INIT", 
			"IN", "CONTINUE", "BREAK", "TEMP", "ASSIGN", "GT", "LT", "BANG", "BANGBANG", 
			"TILDE", "QUESTION", "COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", 
			"OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", 
			"MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", 
			"OR_ASSIGN", "XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", 
			"URSHIFT_ASSIGN", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", 
			"SEMI", "COMMA", "DOT", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "DECIMAL_LITERAL", 
			"HEX_LITERAL", "OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", 
			"BOOL_LITERAL", "CHAR_LITERAL", "STRING_LITERAL", "TEXT_BLOCK", "R", 
			"RW", "IDENTIFIER", "WS", "COMMENT", "LINE_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "KiwiParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public KiwiParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public PackageDeclarationContext packageDeclaration() {
			return getRuleContext(PackageDeclarationContext.class,0);
		}
		public List<ImportDeclarationContext> importDeclaration() {
			return getRuleContexts(ImportDeclarationContext.class);
		}
		public ImportDeclarationContext importDeclaration(int i) {
			return getRuleContext(ImportDeclarationContext.class,i);
		}
		public List<TopLevTypeDeclContext> topLevTypeDecl() {
			return getRuleContexts(TopLevTypeDeclContext.class);
		}
		public TopLevTypeDeclContext topLevTypeDecl(int i) {
			return getRuleContext(TopLevTypeDeclContext.class,i);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PACKAGE) {
				{
				setState(228);
				packageDeclaration();
				}
			}

			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(231);
				importDeclaration();
				}
				}
				setState(236);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(238); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(237);
				topLevTypeDecl();
				}
				}
				setState(240); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 18084773461262466L) != 0) || _la==AT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PackageDeclarationContext extends ParserRuleContext {
		public TerminalNode PACKAGE() { return getToken(KiwiParser.PACKAGE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public PackageDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPackageDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPackageDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPackageDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PackageDeclarationContext packageDeclaration() throws RecognitionException {
		PackageDeclarationContext _localctx = new PackageDeclarationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_packageDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
			match(PACKAGE);
			setState(243);
			qualifiedName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportDeclarationContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(KiwiParser.IMPORT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterImportDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitImportDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitImportDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportDeclarationContext importDeclaration() throws RecognitionException {
		ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			match(IMPORT);
			setState(246);
			qualifiedName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TopLevTypeDeclContext extends ParserRuleContext {
		public TypeDeclarationContext typeDeclaration() {
			return getRuleContext(TypeDeclarationContext.class,0);
		}
		public List<ClassOrInterfaceModifierContext> classOrInterfaceModifier() {
			return getRuleContexts(ClassOrInterfaceModifierContext.class);
		}
		public ClassOrInterfaceModifierContext classOrInterfaceModifier(int i) {
			return getRuleContext(ClassOrInterfaceModifierContext.class,i);
		}
		public TopLevTypeDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_topLevTypeDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTopLevTypeDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTopLevTypeDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTopLevTypeDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TopLevTypeDeclContext topLevTypeDecl() throws RecognitionException {
		TopLevTypeDeclContext _localctx = new TopLevTypeDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_topLevTypeDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18084773427675138L) != 0)) {
				{
				{
				setState(248);
				classOrInterfaceModifier();
				}
				}
				setState(253);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(254);
			typeDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeDeclarationContext extends ParserRuleContext {
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public EnumDeclarationContext enumDeclaration() {
			return getRuleContext(EnumDeclarationContext.class,0);
		}
		public InterfaceDeclarationContext interfaceDeclaration() {
			return getRuleContext(InterfaceDeclarationContext.class,0);
		}
		public TypeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeDeclarationContext typeDeclaration() throws RecognitionException {
		TypeDeclarationContext _localctx = new TypeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_typeDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(256);
				classDeclaration();
				}
				break;
			case 2:
				{
				setState(257);
				enumDeclaration();
				}
				break;
			case 3:
				{
				setState(258);
				interfaceDeclaration();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassDeclarationContext extends ParserRuleContext {
		public TerminalNode CLASS() { return getToken(KiwiParser.CLASS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public ClassDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassDeclarationContext classDeclaration() throws RecognitionException {
		ClassDeclarationContext _localctx = new ClassDeclarationContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_classDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(261);
				annotation();
				}
				}
				setState(266);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(267);
			match(CLASS);
			setState(268);
			identifier();
			setState(270);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(269);
				typeParameters();
				}
			}

			setState(274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(272);
				match(COLON);
				setState(273);
				typeList();
				}
			}

			setState(276);
			classBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassBodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public ClassBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassBodyContext classBody() throws RecognitionException {
		ClassBodyContext _localctx = new ClassBodyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_classBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
			match(LBRACE);
			setState(282);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19685662458413186L) != 0) || _la==AT) {
				{
				{
				setState(279);
				classBodyDeclaration();
				}
				}
				setState(284);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(285);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeListContext extends ParserRuleContext {
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(287);
			type();
			setState(292);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(288);
				match(COMMA);
				setState(289);
				type();
				}
				}
				setState(294);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassBodyDeclarationContext extends ParserRuleContext {
		public MemberDeclarationContext memberDeclaration() {
			return getRuleContext(MemberDeclarationContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public StaticBlockContext staticBlock() {
			return getRuleContext(StaticBlockContext.class,0);
		}
		public ClassBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classBodyDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassBodyDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassBodyDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassBodyDeclarationContext classBodyDeclaration() throws RecognitionException {
		ClassBodyDeclarationContext _localctx = new ClassBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_classBodyDeclaration);
		int _la;
		try {
			setState(303);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(298);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18102365680828418L) != 0)) {
					{
					{
					setState(295);
					modifier();
					}
					}
					setState(300);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(301);
				memberDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(302);
				staticBlock();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StaticBlockContext extends ParserRuleContext {
		public TerminalNode STATIC() { return getToken(KiwiParser.STATIC, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public StaticBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_staticBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterStaticBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitStaticBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitStaticBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StaticBlockContext staticBlock() throws RecognitionException {
		StaticBlockContext _localctx = new StaticBlockContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_staticBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(305);
			match(STATIC);
			setState(306);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumDeclarationContext extends ParserRuleContext {
		public TerminalNode ENUM() { return getToken(KiwiParser.ENUM, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(KiwiParser.COMMA, 0); }
		public EnumBodyDeclarationsContext enumBodyDeclarations() {
			return getRuleContext(EnumBodyDeclarationsContext.class,0);
		}
		public EnumDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterEnumDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitEnumDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitEnumDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumDeclarationContext enumDeclaration() throws RecognitionException {
		EnumDeclarationContext _localctx = new EnumDeclarationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_enumDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(308);
				annotation();
				}
				}
				setState(313);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(314);
			match(ENUM);
			setState(315);
			identifier();
			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(316);
				match(COLON);
				setState(317);
				typeList();
				}
			}

			setState(320);
			match(LBRACE);
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
				{
				setState(321);
				enumConstants();
				}
			}

			setState(325);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(324);
				match(COMMA);
				}
			}

			setState(328);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(327);
				enumBodyDeclarations();
				}
			}

			setState(330);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumConstantsContext extends ParserRuleContext {
		public List<EnumConstantContext> enumConstant() {
			return getRuleContexts(EnumConstantContext.class);
		}
		public EnumConstantContext enumConstant(int i) {
			return getRuleContext(EnumConstantContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public EnumConstantsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstants; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterEnumConstants(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitEnumConstants(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitEnumConstants(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumConstantsContext enumConstants() throws RecognitionException {
		EnumConstantsContext _localctx = new EnumConstantsContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(332);
			enumConstant();
			setState(337);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(333);
					match(COMMA);
					setState(334);
					enumConstant();
					}
					} 
				}
				setState(339);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumConstantContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public EnumConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterEnumConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitEnumConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitEnumConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumConstantContext enumConstant() throws RecognitionException {
		EnumConstantContext _localctx = new EnumConstantContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(340);
			identifier();
			setState(342);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(341);
				arguments();
				}
			}

			setState(345);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBRACE) {
				{
				setState(344);
				classBody();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumBodyDeclarationsContext extends ParserRuleContext {
		public TerminalNode SEMI() { return getToken(KiwiParser.SEMI, 0); }
		public List<ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public EnumBodyDeclarationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumBodyDeclarations; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterEnumBodyDeclarations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitEnumBodyDeclarations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitEnumBodyDeclarations(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumBodyDeclarationsContext enumBodyDeclarations() throws RecognitionException {
		EnumBodyDeclarationsContext _localctx = new EnumBodyDeclarationsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_enumBodyDeclarations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(347);
			match(SEMI);
			setState(351);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19685662458413186L) != 0) || _la==AT) {
				{
				{
				setState(348);
				classBodyDeclaration();
				}
				}
				setState(353);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceDeclarationContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(KiwiParser.INTERFACE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public InterfaceBodyContext interfaceBody() {
			return getRuleContext(InterfaceBodyContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public InterfaceDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInterfaceDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInterfaceDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInterfaceDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceDeclarationContext interfaceDeclaration() throws RecognitionException {
		InterfaceDeclarationContext _localctx = new InterfaceDeclarationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_interfaceDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(357);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(354);
				annotation();
				}
				}
				setState(359);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(360);
			match(INTERFACE);
			setState(361);
			identifier();
			setState(363);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(362);
				typeParameters();
				}
			}

			setState(367);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(365);
				match(COLON);
				setState(366);
				typeList();
				}
			}

			setState(369);
			interfaceBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceBodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<InterfaceBodyDeclarationContext> interfaceBodyDeclaration() {
			return getRuleContexts(InterfaceBodyDeclarationContext.class);
		}
		public InterfaceBodyDeclarationContext interfaceBodyDeclaration(int i) {
			return getRuleContext(InterfaceBodyDeclarationContext.class,i);
		}
		public InterfaceBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInterfaceBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInterfaceBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInterfaceBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceBodyContext interfaceBody() throws RecognitionException {
		InterfaceBodyContext _localctx = new InterfaceBodyContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_interfaceBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			match(LBRACE);
			setState(375);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18137550052917266L) != 0)) {
				{
				{
				setState(372);
				interfaceBodyDeclaration();
				}
				}
				setState(377);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(378);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceBodyDeclarationContext extends ParserRuleContext {
		public InterfaceMemberDeclarationContext interfaceMemberDeclaration() {
			return getRuleContext(InterfaceMemberDeclarationContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public InterfaceBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceBodyDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInterfaceBodyDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInterfaceBodyDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInterfaceBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceBodyDeclarationContext interfaceBodyDeclaration() throws RecognitionException {
		InterfaceBodyDeclarationContext _localctx = new InterfaceBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(383);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(380);
					modifier();
					}
					} 
				}
				setState(385);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			}
			setState(386);
			interfaceMemberDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceMemberDeclarationContext extends ParserRuleContext {
		public InterfaceMethodDeclarationContext interfaceMethodDeclaration() {
			return getRuleContext(InterfaceMethodDeclarationContext.class,0);
		}
		public InterfaceMemberDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMemberDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInterfaceMemberDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInterfaceMemberDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInterfaceMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMemberDeclarationContext interfaceMemberDeclaration() throws RecognitionException {
		InterfaceMemberDeclarationContext _localctx = new InterfaceMemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_interfaceMemberDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			interfaceMethodDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceMethodDeclarationContext extends ParserRuleContext {
		public TerminalNode FN() { return getToken(KiwiParser.FN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public List<InterfaceMethodModifierContext> interfaceMethodModifier() {
			return getRuleContexts(InterfaceMethodModifierContext.class);
		}
		public InterfaceMethodModifierContext interfaceMethodModifier(int i) {
			return getRuleContext(InterfaceMethodModifierContext.class,i);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(KiwiParser.ARROW, 0); }
		public TypeOrVoidContext typeOrVoid() {
			return getRuleContext(TypeOrVoidContext.class,0);
		}
		public TerminalNode THROWS() { return getToken(KiwiParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public InterfaceMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMethodDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInterfaceMethodDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInterfaceMethodDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInterfaceMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMethodDeclarationContext interfaceMethodDeclaration() throws RecognitionException {
		InterfaceMethodDeclarationContext _localctx = new InterfaceMethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_interfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 5368709138L) != 0)) {
				{
				{
				setState(390);
				interfaceMethodModifier();
				}
				}
				setState(395);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(396);
			match(FN);
			setState(397);
			identifier();
			setState(399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(398);
				typeParameters();
				}
			}

			setState(401);
			formalParameters();
			setState(404);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(402);
				match(ARROW);
				setState(403);
				typeOrVoid();
				}
			}

			setState(408);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(406);
				match(THROWS);
				setState(407);
				qualifiedNameList();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceMethodModifierContext extends ParserRuleContext {
		public TerminalNode PUB() { return getToken(KiwiParser.PUB, 0); }
		public TerminalNode ABSTRACT() { return getToken(KiwiParser.ABSTRACT, 0); }
		public TerminalNode DEFAULT() { return getToken(KiwiParser.DEFAULT, 0); }
		public TerminalNode STATIC() { return getToken(KiwiParser.STATIC, 0); }
		public InterfaceMethodModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMethodModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInterfaceMethodModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInterfaceMethodModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInterfaceMethodModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMethodModifierContext interfaceMethodModifier() throws RecognitionException {
		InterfaceMethodModifierContext _localctx = new InterfaceMethodModifierContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_interfaceMethodModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 5368709138L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberDeclarationContext extends ParserRuleContext {
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public FieldDeclarationContext fieldDeclaration() {
			return getRuleContext(FieldDeclarationContext.class,0);
		}
		public ConstructorDeclarationContext constructorDeclaration() {
			return getRuleContext(ConstructorDeclarationContext.class,0);
		}
		public TypeDeclarationContext typeDeclaration() {
			return getRuleContext(TypeDeclarationContext.class,0);
		}
		public MemberDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterMemberDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitMemberDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclarationContext memberDeclaration() throws RecognitionException {
		MemberDeclarationContext _localctx = new MemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_memberDeclaration);
		try {
			setState(416);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FN:
				enterOuterAlt(_localctx, 1);
				{
				setState(412);
				methodDeclaration();
				}
				break;
			case VAL:
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(413);
				fieldDeclaration();
				}
				break;
			case INIT:
				enterOuterAlt(_localctx, 3);
				{
				setState(414);
				constructorDeclaration();
				}
				break;
			case CLASS:
			case ENUM:
			case INTERFACE:
			case AT:
				enterOuterAlt(_localctx, 4);
				{
				setState(415);
				typeDeclaration();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldDeclarationContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode VAR() { return getToken(KiwiParser.VAR, 0); }
		public TerminalNode VAL() { return getToken(KiwiParser.VAL, 0); }
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(KiwiParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FieldDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterFieldDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitFieldDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitFieldDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldDeclarationContext fieldDeclaration() throws RecognitionException {
		FieldDeclarationContext _localctx = new FieldDeclarationContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_fieldDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(418);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(419);
			identifier();
			setState(422);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(420);
				match(COLON);
				setState(421);
				type();
				}
			}

			setState(426);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(424);
				match(ASSIGN);
				setState(425);
				expression();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodDeclarationContext extends ParserRuleContext {
		public TerminalNode FN() { return getToken(KiwiParser.FN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(KiwiParser.ARROW, 0); }
		public TypeOrVoidContext typeOrVoid() {
			return getRuleContext(TypeOrVoidContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public MethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterMethodDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitMethodDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodDeclarationContext methodDeclaration() throws RecognitionException {
		MethodDeclarationContext _localctx = new MethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
			match(FN);
			setState(429);
			identifier();
			setState(431);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(430);
				typeParameters();
				}
			}

			setState(433);
			formalParameters();
			setState(436);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(434);
				match(ARROW);
				setState(435);
				typeOrVoid();
				}
			}

			setState(439);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBRACE) {
				{
				setState(438);
				methodBody();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstructorDeclarationContext extends ParserRuleContext {
		public BlockContext constructorBody;
		public TerminalNode INIT() { return getToken(KiwiParser.INIT, 0); }
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode THROWS() { return getToken(KiwiParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public ConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterConstructorDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitConstructorDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitConstructorDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstructorDeclarationContext constructorDeclaration() throws RecognitionException {
		ConstructorDeclarationContext _localctx = new ConstructorDeclarationContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_constructorDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
			match(INIT);
			setState(442);
			formalParameters();
			setState(445);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(443);
				match(THROWS);
				setState(444);
				qualifiedNameList();
				}
			}

			setState(447);
			((ConstructorDeclarationContext)_localctx).constructorBody = block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeParametersContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(KiwiParser.LT, 0); }
		public List<TypeParameterContext> typeParameter() {
			return getRuleContexts(TypeParameterContext.class);
		}
		public TypeParameterContext typeParameter(int i) {
			return getRuleContext(TypeParameterContext.class,i);
		}
		public TerminalNode GT() { return getToken(KiwiParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public TypeParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParametersContext typeParameters() throws RecognitionException {
		TypeParametersContext _localctx = new TypeParametersContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_typeParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
			match(LT);
			setState(450);
			typeParameter();
			setState(455);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(451);
				match(COMMA);
				setState(452);
				typeParameter();
				}
				}
				setState(457);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(458);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameListContext extends ParserRuleContext {
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public QualifiedNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterQualifiedNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitQualifiedNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitQualifiedNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameListContext qualifiedNameList() throws RecognitionException {
		QualifiedNameListContext _localctx = new QualifiedNameListContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(460);
			qualifiedName();
			setState(465);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(461);
				match(COMMA);
				setState(462);
				qualifiedName();
				}
				}
				setState(467);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(KiwiParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(KiwiParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_qualifiedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(468);
			identifier();
			setState(473);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(469);
				match(DOT);
				setState(470);
				identifier();
				}
				}
				setState(475);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeParameterContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TypeParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParameterContext typeParameter() throws RecognitionException {
		TypeParameterContext _localctx = new TypeParameterContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_typeParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(476);
			identifier();
			setState(479);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(477);
				match(COLON);
				setState(478);
				type();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FormalParametersContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
		}
		public FormalParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterFormalParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitFormalParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitFormalParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParametersContext formalParameters() throws RecognitionException {
		FormalParametersContext _localctx = new FormalParametersContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(481);
			match(LPAREN);
			setState(483);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
				{
				setState(482);
				formalParameterList();
				}
			}

			setState(485);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FormalParameterListContext extends ParserRuleContext {
		public List<FormalParameterContext> formalParameter() {
			return getRuleContexts(FormalParameterContext.class);
		}
		public FormalParameterContext formalParameter(int i) {
			return getRuleContext(FormalParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public FormalParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterFormalParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitFormalParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitFormalParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterListContext formalParameterList() throws RecognitionException {
		FormalParameterListContext _localctx = new FormalParameterListContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(487);
			formalParameter();
			setState(492);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(488);
				match(COMMA);
				setState(489);
				formalParameter();
				}
				}
				setState(494);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FormalParameterContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public FormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterFormalParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitFormalParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterContext formalParameter() throws RecognitionException {
		FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			identifier();
			setState(496);
			match(COLON);
			setState(497);
			type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodBodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public MethodBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterMethodBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitMethodBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitMethodBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodBodyContext methodBody() throws RecognitionException {
		MethodBodyContext _localctx = new MethodBodyContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(499);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(501);
			match(LBRACE);
			setState(505);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1474299263810142720L) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 87943895056399L) != 0)) {
				{
				{
				setState(502);
				statement();
				}
				}
				setState(507);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(508);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public ExpressionContext statementExpression;
		public TerminalNode WHILE() { return getToken(KiwiParser.WHILE, 0); }
		public ParExpressionContext parExpression() {
			return getRuleContext(ParExpressionContext.class,0);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode DO() { return getToken(KiwiParser.DO, 0); }
		public TerminalNode FOR() { return getToken(KiwiParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public ForControlContext forControl() {
			return getRuleContext(ForControlContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode IF() { return getToken(KiwiParser.IF, 0); }
		public TerminalNode ELSE() { return getToken(KiwiParser.ELSE, 0); }
		public TerminalNode TRY() { return getToken(KiwiParser.TRY, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<CatchClauseContext> catchClause() {
			return getRuleContexts(CatchClauseContext.class);
		}
		public CatchClauseContext catchClause(int i) {
			return getRuleContext(CatchClauseContext.class,i);
		}
		public TerminalNode SWITCH() { return getToken(KiwiParser.SWITCH, 0); }
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<BranchCaseContext> branchCase() {
			return getRuleContexts(BranchCaseContext.class);
		}
		public BranchCaseContext branchCase(int i) {
			return getRuleContext(BranchCaseContext.class,i);
		}
		public TerminalNode RETURN() { return getToken(KiwiParser.RETURN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode THROW() { return getToken(KiwiParser.THROW, 0); }
		public TerminalNode BREAK() { return getToken(KiwiParser.BREAK, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode CONTINUE() { return getToken(KiwiParser.CONTINUE, 0); }
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TerminalNode SEMI() { return getToken(KiwiParser.SEMI, 0); }
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_statement);
		int _la;
		try {
			setState(570);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(510);
				match(WHILE);
				setState(511);
				parExpression();
				setState(512);
				statement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(514);
				match(DO);
				setState(515);
				statement();
				setState(516);
				match(WHILE);
				setState(517);
				parExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(519);
				match(FOR);
				setState(520);
				match(LPAREN);
				setState(521);
				forControl();
				setState(522);
				match(RPAREN);
				setState(523);
				statement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(525);
				match(IF);
				setState(526);
				parExpression();
				setState(527);
				statement();
				setState(530);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(528);
					match(ELSE);
					setState(529);
					statement();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(532);
				match(TRY);
				setState(533);
				block();
				setState(535); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(534);
					catchClause();
					}
					}
					setState(537); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==CATCH );
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(539);
				match(SWITCH);
				setState(540);
				match(LBRACE);
				setState(544);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(541);
					branchCase();
					}
					}
					setState(546);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(547);
				match(RBRACE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(548);
				match(RETURN);
				setState(550);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
				case 1:
					{
					setState(549);
					expression();
					}
					break;
				}
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(552);
				match(THROW);
				setState(553);
				expression();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(554);
				match(BREAK);
				setState(556);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
				case 1:
					{
					setState(555);
					identifier();
					}
					break;
				}
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(558);
				match(CONTINUE);
				setState(560);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
				case 1:
					{
					setState(559);
					identifier();
					}
					break;
				}
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(562);
				identifier();
				setState(563);
				match(COLON);
				setState(564);
				statement();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(566);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(567);
				((StatementContext)_localctx).statementExpression = expression();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(568);
				localVariableDeclaration();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(569);
				block();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LocalVariableDeclarationContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode VAR() { return getToken(KiwiParser.VAR, 0); }
		public TerminalNode VAL() { return getToken(KiwiParser.VAL, 0); }
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(KiwiParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LocalVariableDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localVariableDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLocalVariableDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLocalVariableDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLocalVariableDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocalVariableDeclarationContext localVariableDeclaration() throws RecognitionException {
		LocalVariableDeclarationContext _localctx = new LocalVariableDeclarationContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_localVariableDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(572);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(573);
			identifier();
			setState(576);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(574);
				match(COLON);
				setState(575);
				type();
				}
			}

			setState(580);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(578);
				match(ASSIGN);
				setState(579);
				expression();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForControlContext extends ParserRuleContext {
		public LoopVariableContext loopVariable() {
			return getRuleContext(LoopVariableContext.class,0);
		}
		public TerminalNode IN() { return getToken(KiwiParser.IN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forControl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterForControl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitForControl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForControlContext forControl() throws RecognitionException {
		ForControlContext _localctx = new ForControlContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_forControl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(582);
			loopVariable();
			setState(583);
			match(IN);
			setState(584);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopVariableContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public LoopVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLoopVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLoopVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLoopVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableContext loopVariable() throws RecognitionException {
		LoopVariableContext _localctx = new LoopVariableContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_loopVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(586);
			identifier();
			setState(589);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(587);
				match(COLON);
				setState(588);
				type();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopVariableDeclaratorsContext extends ParserRuleContext {
		public List<LoopVariableDeclaratorContext> loopVariableDeclarator() {
			return getRuleContexts(LoopVariableDeclaratorContext.class);
		}
		public LoopVariableDeclaratorContext loopVariableDeclarator(int i) {
			return getRuleContext(LoopVariableDeclaratorContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public LoopVariableDeclaratorsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableDeclarators; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLoopVariableDeclarators(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLoopVariableDeclarators(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLoopVariableDeclarators(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableDeclaratorsContext loopVariableDeclarators() throws RecognitionException {
		LoopVariableDeclaratorsContext _localctx = new LoopVariableDeclaratorsContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_loopVariableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(591);
			loopVariableDeclarator();
			setState(596);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(592);
				match(COMMA);
				setState(593);
				loopVariableDeclarator();
				}
				}
				setState(598);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopVariableDeclaratorContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(KiwiParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LoopVariableDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLoopVariableDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLoopVariableDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLoopVariableDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableDeclaratorContext loopVariableDeclarator() throws RecognitionException {
		LoopVariableDeclaratorContext _localctx = new LoopVariableDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_loopVariableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(599);
			type();
			setState(600);
			identifier();
			setState(601);
			match(ASSIGN);
			setState(602);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopVariableUpdatesContext extends ParserRuleContext {
		public List<LoopVariableUpdateContext> loopVariableUpdate() {
			return getRuleContexts(LoopVariableUpdateContext.class);
		}
		public LoopVariableUpdateContext loopVariableUpdate(int i) {
			return getRuleContext(LoopVariableUpdateContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public LoopVariableUpdatesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableUpdates; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLoopVariableUpdates(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLoopVariableUpdates(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLoopVariableUpdates(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableUpdatesContext loopVariableUpdates() throws RecognitionException {
		LoopVariableUpdatesContext _localctx = new LoopVariableUpdatesContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_loopVariableUpdates);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(604);
			loopVariableUpdate();
			setState(609);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(605);
				match(COMMA);
				setState(606);
				loopVariableUpdate();
				}
				}
				setState(611);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopVariableUpdateContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(KiwiParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LoopVariableUpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableUpdate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLoopVariableUpdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLoopVariableUpdate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLoopVariableUpdate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableUpdateContext loopVariableUpdate() throws RecognitionException {
		LoopVariableUpdateContext _localctx = new LoopVariableUpdateContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_loopVariableUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(612);
			identifier();
			setState(613);
			match(ASSIGN);
			setState(614);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnonClassExprContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(KiwiParser.NEW, 0); }
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public AnonClassExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anonClassExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAnonClassExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAnonClassExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAnonClassExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnonClassExprContext anonClassExpr() throws RecognitionException {
		AnonClassExprContext _localctx = new AnonClassExprContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_anonClassExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(616);
			match(NEW);
			setState(617);
			classType();
			setState(618);
			arguments();
			setState(619);
			classBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NewArrayContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(KiwiParser.NEW, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public NewArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_newArray; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterNewArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitNewArray(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitNewArray(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NewArrayContext newArray() throws RecognitionException {
		NewArrayContext _localctx = new NewArrayContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_newArray);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(621);
			match(NEW);
			setState(622);
			type();
			setState(623);
			arrayKind();
			setState(625);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				{
				setState(624);
				arrayInitializer();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayInitializerContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitArrayInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayInitializerContext arrayInitializer() throws RecognitionException {
		ArrayInitializerContext _localctx = new ArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			match(LBRACE);
			setState(639);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1460362591002952192L) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 87943760838671L) != 0)) {
				{
				setState(628);
				variableInitializer();
				setState(633);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,57,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(629);
						match(COMMA);
						setState(630);
						variableInitializer();
						}
						} 
					}
					setState(635);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,57,_ctx);
				}
				setState(637);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(636);
					match(COMMA);
					}
				}

				}
			}

			setState(641);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableInitializerContext extends ParserRuleContext {
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public VariableInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterVariableInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitVariableInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitVariableInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableInitializerContext variableInitializer() throws RecognitionException {
		VariableInitializerContext _localctx = new VariableInitializerContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_variableInitializer);
		try {
			setState(645);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(643);
				arrayInitializer();
				}
				break;
			case NULL:
			case NEW:
			case SUPER:
			case THIS:
			case VALUE:
			case INIT:
			case TEMP:
			case BANG:
			case TILDE:
			case INC:
			case DEC:
			case ADD:
			case SUB:
			case LPAREN:
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
			case BOOL_LITERAL:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case TEXT_BLOCK:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(644);
				expression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CatchClauseContext extends ParserRuleContext {
		public TerminalNode CATCH() { return getToken(KiwiParser.CATCH, 0); }
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public CatchClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterCatchClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitCatchClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitCatchClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchClauseContext catchClause() throws RecognitionException {
		CatchClauseContext _localctx = new CatchClauseContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_catchClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			match(CATCH);
			setState(648);
			match(LPAREN);
			setState(649);
			identifier();
			setState(650);
			match(COLON);
			setState(651);
			type();
			setState(652);
			match(RPAREN);
			setState(653);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CatchFieldsContext extends ParserRuleContext {
		public List<CatchFieldContext> catchField() {
			return getRuleContexts(CatchFieldContext.class);
		}
		public CatchFieldContext catchField(int i) {
			return getRuleContext(CatchFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public CatchFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterCatchFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitCatchFields(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitCatchFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchFieldsContext catchFields() throws RecognitionException {
		CatchFieldsContext _localctx = new CatchFieldsContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_catchFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(655);
			catchField();
			setState(660);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(656);
				match(COMMA);
				setState(657);
				catchField();
				}
				}
				setState(662);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CatchFieldContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TerminalNode> COLON() { return getTokens(KiwiParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(KiwiParser.COLON, i);
		}
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode DEFAULT() { return getToken(KiwiParser.DEFAULT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public List<CatchValueContext> catchValue() {
			return getRuleContexts(CatchValueContext.class);
		}
		public CatchValueContext catchValue(int i) {
			return getRuleContext(CatchValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public CatchFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterCatchField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitCatchField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitCatchField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchFieldContext catchField() throws RecognitionException {
		CatchFieldContext _localctx = new CatchFieldContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_catchField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(663);
			identifier();
			setState(664);
			match(COLON);
			setState(665);
			match(LBRACE);
			setState(671);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
				{
				{
				setState(666);
				catchValue();
				setState(667);
				match(COMMA);
				}
				}
				setState(673);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(674);
			match(DEFAULT);
			setState(675);
			match(COLON);
			setState(676);
			expression();
			setState(677);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CatchValueContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CatchValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterCatchValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitCatchValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitCatchValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchValueContext catchValue() throws RecognitionException {
		CatchValueContext _localctx = new CatchValueContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_catchValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(679);
			identifier();
			setState(680);
			match(COLON);
			setState(681);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BranchCaseContext extends ParserRuleContext {
		public SwitchLabelContext switchLabel() {
			return getRuleContext(SwitchLabelContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public BranchCaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_branchCase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterBranchCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitBranchCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitBranchCase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BranchCaseContext branchCase() throws RecognitionException {
		BranchCaseContext _localctx = new BranchCaseContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_branchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(683);
			switchLabel();
			setState(684);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchLabelContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(KiwiParser.CASE, 0); }
		public TerminalNode ARROW() { return getToken(KiwiParser.ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(KiwiParser.DEFAULT, 0); }
		public SwitchLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterSwitchLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitSwitchLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitSwitchLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchLabelContext switchLabel() throws RecognitionException {
		SwitchLabelContext _localctx = new SwitchLabelContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_switchLabel);
		try {
			setState(692);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(686);
				match(CASE);
				{
				setState(687);
				expression();
				}
				setState(688);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(690);
				match(DEFAULT);
				setState(691);
				match(ARROW);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParExpressionContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public ParExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterParExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitParExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitParExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParExpressionContext parExpression() throws RecognitionException {
		ParExpressionContext _localctx = new ParExpressionContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(694);
			match(LPAREN);
			setState(695);
			expression();
			setState(696);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(698);
			expression();
			setState(703);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(699);
				match(COMMA);
				setState(700);
				expression();
				}
				}
				setState(705);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(706);
			assignment();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentContext extends ParserRuleContext {
		public TernaryContext ternary() {
			return getRuleContext(TernaryContext.class,0);
		}
		public List<AssignmentSuffixContext> assignmentSuffix() {
			return getRuleContexts(AssignmentSuffixContext.class);
		}
		public AssignmentSuffixContext assignmentSuffix(int i) {
			return getRuleContext(AssignmentSuffixContext.class,i);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_assignment);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(708);
			ternary();
			setState(712);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(709);
					assignmentSuffix();
					}
					} 
				}
				setState(714);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentSuffixContext extends ParserRuleContext {
		public Token op;
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(KiwiParser.ASSIGN, 0); }
		public TerminalNode ADD_ASSIGN() { return getToken(KiwiParser.ADD_ASSIGN, 0); }
		public TerminalNode SUB_ASSIGN() { return getToken(KiwiParser.SUB_ASSIGN, 0); }
		public TerminalNode MOD_ASSIGN() { return getToken(KiwiParser.MOD_ASSIGN, 0); }
		public TerminalNode RSHIFT_ASSIGN() { return getToken(KiwiParser.RSHIFT_ASSIGN, 0); }
		public TerminalNode URSHIFT_ASSIGN() { return getToken(KiwiParser.URSHIFT_ASSIGN, 0); }
		public TerminalNode LSHIFT_ASSIGN() { return getToken(KiwiParser.LSHIFT_ASSIGN, 0); }
		public TerminalNode AND_ASSIGN() { return getToken(KiwiParser.AND_ASSIGN, 0); }
		public TerminalNode OR_ASSIGN() { return getToken(KiwiParser.OR_ASSIGN, 0); }
		public TerminalNode XOR_ASSIGN() { return getToken(KiwiParser.XOR_ASSIGN, 0); }
		public AssignmentSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAssignmentSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAssignmentSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAssignmentSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentSuffixContext assignmentSuffix() throws RecognitionException {
		AssignmentSuffixContext _localctx = new AssignmentSuffixContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_assignmentSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(715);
			((AssignmentSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 55)) & ~0x3f) == 0 && ((1L << (_la - 55)) & 34141634561L) != 0)) ) {
				((AssignmentSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(716);
			assignment();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TernaryContext extends ParserRuleContext {
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}
		public DisjunctionContext disjunction(int i) {
			return getRuleContext(DisjunctionContext.class,i);
		}
		public TerminalNode QUESTION() { return getToken(KiwiParser.QUESTION, 0); }
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TernaryContext ternary() {
			return getRuleContext(TernaryContext.class,0);
		}
		public TernaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ternary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTernary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTernary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTernary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TernaryContext ternary() throws RecognitionException {
		TernaryContext _localctx = new TernaryContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_ternary);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(718);
			disjunction();
			setState(724);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				{
				setState(719);
				match(QUESTION);
				setState(720);
				disjunction();
				setState(721);
				match(COLON);
				setState(722);
				ternary();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DisjunctionContext extends ParserRuleContext {
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}
		public ConjunctionContext conjunction(int i) {
			return getRuleContext(ConjunctionContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(KiwiParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(KiwiParser.OR, i);
		}
		public DisjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitDisjunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitDisjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DisjunctionContext disjunction() throws RecognitionException {
		DisjunctionContext _localctx = new DisjunctionContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_disjunction);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(726);
			conjunction();
			setState(731);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(727);
					match(OR);
					setState(728);
					conjunction();
					}
					} 
				}
				setState(733);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,67,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConjunctionContext extends ParserRuleContext {
		public List<RangeContext> range() {
			return getRuleContexts(RangeContext.class);
		}
		public RangeContext range(int i) {
			return getRuleContext(RangeContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(KiwiParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(KiwiParser.AND, i);
		}
		public ConjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitConjunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitConjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConjunctionContext conjunction() throws RecognitionException {
		ConjunctionContext _localctx = new ConjunctionContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_conjunction);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(734);
			range();
			setState(739);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(735);
					match(AND);
					setState(736);
					range();
					}
					} 
				}
				setState(741);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RangeContext extends ParserRuleContext {
		public List<BitorContext> bitor() {
			return getRuleContexts(BitorContext.class);
		}
		public BitorContext bitor(int i) {
			return getRuleContext(BitorContext.class,i);
		}
		public TerminalNode ELLIPSIS() { return getToken(KiwiParser.ELLIPSIS, 0); }
		public RangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeContext range() throws RecognitionException {
		RangeContext _localctx = new RangeContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(742);
			bitor();
			setState(745);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(743);
				match(ELLIPSIS);
				setState(744);
				bitor();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BitorContext extends ParserRuleContext {
		public List<BitandContext> bitand() {
			return getRuleContexts(BitandContext.class);
		}
		public BitandContext bitand(int i) {
			return getRuleContext(BitandContext.class,i);
		}
		public List<TerminalNode> BITOR() { return getTokens(KiwiParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(KiwiParser.BITOR, i);
		}
		public BitorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterBitor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitBitor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitBitor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitorContext bitor() throws RecognitionException {
		BitorContext _localctx = new BitorContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_bitor);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(747);
			bitand();
			setState(752);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(748);
					match(BITOR);
					setState(749);
					bitand();
					}
					} 
				}
				setState(754);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,70,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BitandContext extends ParserRuleContext {
		public List<BitxorContext> bitxor() {
			return getRuleContexts(BitxorContext.class);
		}
		public BitxorContext bitxor(int i) {
			return getRuleContext(BitxorContext.class,i);
		}
		public List<TerminalNode> BITAND() { return getTokens(KiwiParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(KiwiParser.BITAND, i);
		}
		public BitandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterBitand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitBitand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitBitand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitandContext bitand() throws RecognitionException {
		BitandContext _localctx = new BitandContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_bitand);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(755);
			bitxor();
			setState(760);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(756);
					match(BITAND);
					setState(757);
					bitxor();
					}
					} 
				}
				setState(762);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BitxorContext extends ParserRuleContext {
		public List<EqualityContext> equality() {
			return getRuleContexts(EqualityContext.class);
		}
		public EqualityContext equality(int i) {
			return getRuleContext(EqualityContext.class,i);
		}
		public List<TerminalNode> CARET() { return getTokens(KiwiParser.CARET); }
		public TerminalNode CARET(int i) {
			return getToken(KiwiParser.CARET, i);
		}
		public BitxorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bitxor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterBitxor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitBitxor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitBitxor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BitxorContext bitxor() throws RecognitionException {
		BitxorContext _localctx = new BitxorContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_bitxor);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(763);
			equality();
			setState(768);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(764);
					match(CARET);
					setState(765);
					equality();
					}
					} 
				}
				setState(770);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EqualityContext extends ParserRuleContext {
		public RelationalContext relational() {
			return getRuleContext(RelationalContext.class,0);
		}
		public List<EqualitySuffixContext> equalitySuffix() {
			return getRuleContexts(EqualitySuffixContext.class);
		}
		public EqualitySuffixContext equalitySuffix(int i) {
			return getRuleContext(EqualitySuffixContext.class,i);
		}
		public EqualityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equality; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterEquality(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitEquality(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitEquality(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualityContext equality() throws RecognitionException {
		EqualityContext _localctx = new EqualityContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_equality);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(771);
			relational();
			setState(775);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(772);
					equalitySuffix();
					}
					} 
				}
				setState(777);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EqualitySuffixContext extends ParserRuleContext {
		public Token op;
		public RelationalContext relational() {
			return getRuleContext(RelationalContext.class,0);
		}
		public TerminalNode EQUAL() { return getToken(KiwiParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(KiwiParser.NOTEQUAL, 0); }
		public EqualitySuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equalitySuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterEqualitySuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitEqualitySuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitEqualitySuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualitySuffixContext equalitySuffix() throws RecognitionException {
		EqualitySuffixContext _localctx = new EqualitySuffixContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_equalitySuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(778);
			((EqualitySuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==EQUAL || _la==NOTEQUAL) ) {
				((EqualitySuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(779);
			relational();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationalContext extends ParserRuleContext {
		public IsExprContext isExpr() {
			return getRuleContext(IsExprContext.class,0);
		}
		public List<RelationalSuffixContext> relationalSuffix() {
			return getRuleContexts(RelationalSuffixContext.class);
		}
		public RelationalSuffixContext relationalSuffix(int i) {
			return getRuleContext(RelationalSuffixContext.class,i);
		}
		public RelationalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relational; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterRelational(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitRelational(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitRelational(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalContext relational() throws RecognitionException {
		RelationalContext _localctx = new RelationalContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_relational);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(781);
			isExpr();
			setState(785);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(782);
					relationalSuffix();
					}
					} 
				}
				setState(787);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationalSuffixContext extends ParserRuleContext {
		public Token op;
		public IsExprContext isExpr() {
			return getRuleContext(IsExprContext.class,0);
		}
		public TerminalNode LT() { return getToken(KiwiParser.LT, 0); }
		public TerminalNode GT() { return getToken(KiwiParser.GT, 0); }
		public TerminalNode LE() { return getToken(KiwiParser.LE, 0); }
		public TerminalNode GE() { return getToken(KiwiParser.GE, 0); }
		public RelationalSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterRelationalSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitRelationalSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitRelationalSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalSuffixContext relationalSuffix() throws RecognitionException {
		RelationalSuffixContext _localctx = new RelationalSuffixContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_relationalSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(788);
			((RelationalSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 56)) & ~0x3f) == 0 && ((1L << (_la - 56)) & 771L) != 0)) ) {
				((RelationalSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(789);
			isExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IsExprContext extends ParserRuleContext {
		public ShiftContext shift() {
			return getRuleContext(ShiftContext.class,0);
		}
		public List<IsSuffixContext> isSuffix() {
			return getRuleContexts(IsSuffixContext.class);
		}
		public IsSuffixContext isSuffix(int i) {
			return getRuleContext(IsSuffixContext.class,i);
		}
		public IsExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterIsExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitIsExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitIsExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IsExprContext isExpr() throws RecognitionException {
		IsExprContext _localctx = new IsExprContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_isExpr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(791);
			shift();
			setState(795);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(792);
					isSuffix();
					}
					} 
				}
				setState(797);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IsSuffixContext extends ParserRuleContext {
		public TerminalNode IS() { return getToken(KiwiParser.IS, 0); }
		public TypePtnContext typePtn() {
			return getRuleContext(TypePtnContext.class,0);
		}
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public IsSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterIsSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitIsSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitIsSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IsSuffixContext isSuffix() throws RecognitionException {
		IsSuffixContext _localctx = new IsSuffixContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_isSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(798);
			match(IS);
			setState(801);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				{
				setState(799);
				typePtn();
				}
				break;
			case 2:
				{
				setState(800);
				type();
				}
				break;
			}
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypePtnContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypePtnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typePtn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypePtn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypePtn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypePtn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypePtnContext typePtn() throws RecognitionException {
		TypePtnContext _localctx = new TypePtnContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_typePtn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(803);
			type();
			setState(804);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ShiftContext extends ParserRuleContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public List<ShiftSuffixContext> shiftSuffix() {
			return getRuleContexts(ShiftSuffixContext.class);
		}
		public ShiftSuffixContext shiftSuffix(int i) {
			return getRuleContext(ShiftSuffixContext.class,i);
		}
		public ShiftContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shift; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterShift(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitShift(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitShift(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShiftContext shift() throws RecognitionException {
		ShiftContext _localctx = new ShiftContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_shift);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(806);
			additive();
			setState(810);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(807);
					shiftSuffix();
					}
					} 
				}
				setState(812);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ShiftSuffixContext extends ParserRuleContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public List<TerminalNode> GT() { return getTokens(KiwiParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(KiwiParser.GT, i);
		}
		public List<TerminalNode> LT() { return getTokens(KiwiParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(KiwiParser.LT, i);
		}
		public ShiftSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shiftSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterShiftSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitShiftSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitShiftSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShiftSuffixContext shiftSuffix() throws RecognitionException {
		ShiftSuffixContext _localctx = new ShiftSuffixContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_shiftSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(820);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				{
				setState(813);
				match(GT);
				setState(814);
				match(GT);
				}
				break;
			case 2:
				{
				setState(815);
				match(LT);
				setState(816);
				match(LT);
				}
				break;
			case 3:
				{
				setState(817);
				match(GT);
				setState(818);
				match(GT);
				setState(819);
				match(GT);
				}
				break;
			}
			setState(822);
			additive();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveContext extends ParserRuleContext {
		public MultiplicativeContext multiplicative() {
			return getRuleContext(MultiplicativeContext.class,0);
		}
		public List<AdditiveSuffixContext> additiveSuffix() {
			return getRuleContexts(AdditiveSuffixContext.class);
		}
		public AdditiveSuffixContext additiveSuffix(int i) {
			return getRuleContext(AdditiveSuffixContext.class,i);
		}
		public AdditiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAdditive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAdditive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAdditive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveContext additive() throws RecognitionException {
		AdditiveContext _localctx = new AdditiveContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_additive);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(824);
			multiplicative();
			setState(828);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(825);
					additiveSuffix();
					}
					} 
				}
				setState(830);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveSuffixContext extends ParserRuleContext {
		public Token op;
		public MultiplicativeContext multiplicative() {
			return getRuleContext(MultiplicativeContext.class,0);
		}
		public TerminalNode ADD() { return getToken(KiwiParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(KiwiParser.SUB, 0); }
		public AdditiveSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additiveSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAdditiveSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAdditiveSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAdditiveSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveSuffixContext additiveSuffix() throws RecognitionException {
		AdditiveSuffixContext _localctx = new AdditiveSuffixContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_additiveSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			((AdditiveSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==ADD || _la==SUB) ) {
				((AdditiveSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(832);
			multiplicative();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeContext extends ParserRuleContext {
		public AsExprContext asExpr() {
			return getRuleContext(AsExprContext.class,0);
		}
		public List<MultiplicativeSuffixContext> multiplicativeSuffix() {
			return getRuleContexts(MultiplicativeSuffixContext.class);
		}
		public MultiplicativeSuffixContext multiplicativeSuffix(int i) {
			return getRuleContext(MultiplicativeSuffixContext.class,i);
		}
		public MultiplicativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicative; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterMultiplicative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitMultiplicative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitMultiplicative(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicativeContext multiplicative() throws RecognitionException {
		MultiplicativeContext _localctx = new MultiplicativeContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_multiplicative);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(834);
			asExpr();
			setState(838);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(835);
					multiplicativeSuffix();
					}
					} 
				}
				setState(840);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeSuffixContext extends ParserRuleContext {
		public Token op;
		public AsExprContext asExpr() {
			return getRuleContext(AsExprContext.class,0);
		}
		public TerminalNode MUL() { return getToken(KiwiParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(KiwiParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(KiwiParser.MOD, 0); }
		public MultiplicativeSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicativeSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterMultiplicativeSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitMultiplicativeSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitMultiplicativeSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicativeSuffixContext multiplicativeSuffix() throws RecognitionException {
		MultiplicativeSuffixContext _localctx = new MultiplicativeSuffixContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_multiplicativeSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(841);
			((MultiplicativeSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 35L) != 0)) ) {
				((MultiplicativeSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(842);
			asExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AsExprContext extends ParserRuleContext {
		public PrefixExprContext prefixExpr() {
			return getRuleContext(PrefixExprContext.class,0);
		}
		public List<TerminalNode> AS() { return getTokens(KiwiParser.AS); }
		public TerminalNode AS(int i) {
			return getToken(KiwiParser.AS, i);
		}
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public AsExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAsExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAsExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAsExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsExprContext asExpr() throws RecognitionException {
		AsExprContext _localctx = new AsExprContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_asExpr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(844);
			prefixExpr();
			setState(849);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,81,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(845);
					match(AS);
					setState(846);
					type();
					}
					} 
				}
				setState(851);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,81,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrefixExprContext extends ParserRuleContext {
		public PostfixExprContext postfixExpr() {
			return getRuleContext(PostfixExprContext.class,0);
		}
		public List<PrefixOpContext> prefixOp() {
			return getRuleContexts(PrefixOpContext.class);
		}
		public PrefixOpContext prefixOp(int i) {
			return getRuleContext(PrefixOpContext.class,i);
		}
		public PrefixExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPrefixExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPrefixExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPrefixExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixExprContext prefixExpr() throws RecognitionException {
		PrefixExprContext _localctx = new PrefixExprContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_prefixExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(855);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 58)) & ~0x3f) == 0 && ((1L << (_la - 58)) & 30725L) != 0)) {
				{
				{
				setState(852);
				prefixOp();
				}
				}
				setState(857);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(858);
			postfixExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrefixOpContext extends ParserRuleContext {
		public Token op;
		public TerminalNode TILDE() { return getToken(KiwiParser.TILDE, 0); }
		public TerminalNode SUB() { return getToken(KiwiParser.SUB, 0); }
		public TerminalNode ADD() { return getToken(KiwiParser.ADD, 0); }
		public TerminalNode INC() { return getToken(KiwiParser.INC, 0); }
		public TerminalNode DEC() { return getToken(KiwiParser.DEC, 0); }
		public TerminalNode BANG() { return getToken(KiwiParser.BANG, 0); }
		public PrefixOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPrefixOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPrefixOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPrefixOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrefixOpContext prefixOp() throws RecognitionException {
		PrefixOpContext _localctx = new PrefixOpContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_prefixOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(860);
			((PrefixOpContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 58)) & ~0x3f) == 0 && ((1L << (_la - 58)) & 30725L) != 0)) ) {
				((PrefixOpContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixExprContext extends ParserRuleContext {
		public PrimaryExprContext primaryExpr() {
			return getRuleContext(PrimaryExprContext.class,0);
		}
		public List<PostfixSuffixContext> postfixSuffix() {
			return getRuleContexts(PostfixSuffixContext.class);
		}
		public PostfixSuffixContext postfixSuffix(int i) {
			return getRuleContext(PostfixSuffixContext.class,i);
		}
		public PostfixExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPostfixExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPostfixExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPostfixExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixExprContext postfixExpr() throws RecognitionException {
		PostfixExprContext _localctx = new PostfixExprContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_postfixExpr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(862);
			primaryExpr();
			setState(866);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(863);
					postfixSuffix();
					}
					} 
				}
				setState(868);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixSuffixContext extends ParserRuleContext {
		public Token op;
		public TerminalNode INC() { return getToken(KiwiParser.INC, 0); }
		public TerminalNode DEC() { return getToken(KiwiParser.DEC, 0); }
		public TerminalNode BANGBANG() { return getToken(KiwiParser.BANGBANG, 0); }
		public CallSuffixContext callSuffix() {
			return getRuleContext(CallSuffixContext.class,0);
		}
		public TerminalNode DOT() { return getToken(KiwiParser.DOT, 0); }
		public AnonClassExprContext anonClassExpr() {
			return getRuleContext(AnonClassExprContext.class,0);
		}
		public IndexingSuffixContext indexingSuffix() {
			return getRuleContext(IndexingSuffixContext.class,0);
		}
		public SelectorSuffixContext selectorSuffix() {
			return getRuleContext(SelectorSuffixContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public PostfixSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPostfixSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPostfixSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPostfixSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixSuffixContext postfixSuffix() throws RecognitionException {
		PostfixSuffixContext _localctx = new PostfixSuffixContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_postfixSuffix);
		int _la;
		try {
			setState(876);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(869);
				((PostfixSuffixContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 59)) & ~0x3f) == 0 && ((1L << (_la - 59)) & 3073L) != 0)) ) {
					((PostfixSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(870);
				callSuffix();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(871);
				match(DOT);
				setState(872);
				anonClassExpr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(873);
				indexingSuffix();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(874);
				selectorSuffix();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(875);
				typeArguments();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallSuffixContext extends ParserRuleContext {
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public CallSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterCallSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitCallSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitCallSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallSuffixContext callSuffix() throws RecognitionException {
		CallSuffixContext _localctx = new CallSuffixContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_callSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(878);
			arguments();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IndexingSuffixContext extends ParserRuleContext {
		public TerminalNode LBRACK() { return getToken(KiwiParser.LBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(KiwiParser.RBRACK, 0); }
		public IndexingSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexingSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterIndexingSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitIndexingSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitIndexingSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexingSuffixContext indexingSuffix() throws RecognitionException {
		IndexingSuffixContext _localctx = new IndexingSuffixContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_indexingSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(880);
			match(LBRACK);
			setState(881);
			expression();
			setState(882);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectorSuffixContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(KiwiParser.DOT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode THIS() { return getToken(KiwiParser.THIS, 0); }
		public SelectorSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectorSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterSelectorSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitSelectorSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitSelectorSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectorSuffixContext selectorSuffix() throws RecognitionException {
		SelectorSuffixContext _localctx = new SelectorSuffixContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_selectorSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(884);
			match(DOT);
			setState(887);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
			case INIT:
			case TEMP:
			case IDENTIFIER:
				{
				setState(885);
				identifier();
				}
				break;
			case THIS:
				{
				setState(886);
				match(THIS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExprContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode THIS() { return getToken(KiwiParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(KiwiParser.SUPER, 0); }
		public AnonClassExprContext anonClassExpr() {
			return getRuleContext(AnonClassExprContext.class,0);
		}
		public NewArrayContext newArray() {
			return getRuleContext(NewArrayContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LambdaExpressionContext lambdaExpression() {
			return getRuleContext(LambdaExpressionContext.class,0);
		}
		public PrimaryExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPrimaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPrimaryExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPrimaryExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExprContext primaryExpr() throws RecognitionException {
		PrimaryExprContext _localctx = new PrimaryExprContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_primaryExpr);
		try {
			setState(900);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(889);
				match(LPAREN);
				setState(890);
				expression();
				setState(891);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(893);
				match(THIS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(894);
				match(SUPER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(895);
				anonClassExpr();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(896);
				newArray();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(897);
				literal();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(898);
				identifier();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(899);
				lambdaExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(902);
			match(LPAREN);
			setState(904);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1460362591002952192L) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 87943752450063L) != 0)) {
				{
				setState(903);
				expressionList();
				}
			}

			setState(906);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(KiwiParser.IDENTIFIER, 0); }
		public TerminalNode VALUE() { return getToken(KiwiParser.VALUE, 0); }
		public TerminalNode INIT() { return getToken(KiwiParser.INIT, 0); }
		public TerminalNode TEMP() { return getToken(KiwiParser.TEMP, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(908);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public FloatLiteralContext floatLiteral() {
			return getRuleContext(FloatLiteralContext.class,0);
		}
		public TerminalNode CHAR_LITERAL() { return getToken(KiwiParser.CHAR_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(KiwiParser.STRING_LITERAL, 0); }
		public TerminalNode BOOL_LITERAL() { return getToken(KiwiParser.BOOL_LITERAL, 0); }
		public TerminalNode NULL() { return getToken(KiwiParser.NULL, 0); }
		public TerminalNode TEXT_BLOCK() { return getToken(KiwiParser.TEXT_BLOCK, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_literal);
		try {
			setState(917);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(910);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(911);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(912);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(913);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(914);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(915);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(916);
				match(TEXT_BLOCK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntegerLiteralContext extends ParserRuleContext {
		public TerminalNode DECIMAL_LITERAL() { return getToken(KiwiParser.DECIMAL_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(KiwiParser.HEX_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(KiwiParser.OCT_LITERAL, 0); }
		public TerminalNode BINARY_LITERAL() { return getToken(KiwiParser.BINARY_LITERAL, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(919);
			_la = _input.LA(1);
			if ( !(((((_la - 103)) & ~0x3f) == 0 && ((1L << (_la - 103)) & 15L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FloatLiteralContext extends ParserRuleContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(KiwiParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(KiwiParser.HEX_FLOAT_LITERAL, 0); }
		public FloatLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitFloatLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FloatLiteralContext floatLiteral() throws RecognitionException {
		FloatLiteralContext _localctx = new FloatLiteralContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			_la = _input.LA(1);
			if ( !(_la==FLOAT_LITERAL || _la==HEX_FLOAT_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeOrVoidContext extends ParserRuleContext {
		public TerminalNode VOID() { return getToken(KiwiParser.VOID, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TypeOrVoidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeOrVoid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeOrVoid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeOrVoid(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeOrVoid(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeOrVoidContext typeOrVoid() throws RecognitionException {
		TypeOrVoidContext _localctx = new TypeOrVoidContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_typeOrVoid);
		try {
			setState(925);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(923);
				match(VOID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(924);
				type();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public UnionTypeContext unionType() {
			return getRuleContext(UnionTypeContext.class,0);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(927);
			unionType();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnionTypeContext extends ParserRuleContext {
		public List<IntersectionTypeContext> intersectionType() {
			return getRuleContexts(IntersectionTypeContext.class);
		}
		public IntersectionTypeContext intersectionType(int i) {
			return getRuleContext(IntersectionTypeContext.class,i);
		}
		public List<TerminalNode> BITOR() { return getTokens(KiwiParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(KiwiParser.BITOR, i);
		}
		public UnionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unionType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterUnionType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitUnionType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitUnionType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnionTypeContext unionType() throws RecognitionException {
		UnionTypeContext _localctx = new UnionTypeContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_unionType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(929);
			intersectionType();
			setState(934);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(930);
					match(BITOR);
					setState(931);
					intersectionType();
					}
					} 
				}
				setState(936);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntersectionTypeContext extends ParserRuleContext {
		public List<PostfixTypeContext> postfixType() {
			return getRuleContexts(PostfixTypeContext.class);
		}
		public PostfixTypeContext postfixType(int i) {
			return getRuleContext(PostfixTypeContext.class,i);
		}
		public List<TerminalNode> BITAND() { return getTokens(KiwiParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(KiwiParser.BITAND, i);
		}
		public IntersectionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intersectionType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterIntersectionType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitIntersectionType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitIntersectionType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntersectionTypeContext intersectionType() throws RecognitionException {
		IntersectionTypeContext _localctx = new IntersectionTypeContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_intersectionType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(937);
			postfixType();
			setState(942);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,91,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(938);
					match(BITAND);
					setState(939);
					postfixType();
					}
					} 
				}
				setState(944);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,91,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PostfixTypeContext extends ParserRuleContext {
		public AtomicTypeContext atomicType() {
			return getRuleContext(AtomicTypeContext.class,0);
		}
		public List<TypeSuffixContext> typeSuffix() {
			return getRuleContexts(TypeSuffixContext.class);
		}
		public TypeSuffixContext typeSuffix(int i) {
			return getRuleContext(TypeSuffixContext.class,i);
		}
		public PostfixTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPostfixType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPostfixType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPostfixType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixTypeContext postfixType() throws RecognitionException {
		PostfixTypeContext _localctx = new PostfixTypeContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_postfixType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(945);
			atomicType();
			setState(949);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,92,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(946);
					typeSuffix();
					}
					} 
				}
				setState(951);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,92,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeSuffixContext extends ParserRuleContext {
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
		}
		public TerminalNode QUESTION() { return getToken(KiwiParser.QUESTION, 0); }
		public TypeSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeSuffixContext typeSuffix() throws RecognitionException {
		TypeSuffixContext _localctx = new TypeSuffixContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_typeSuffix);
		try {
			setState(954);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case R:
			case RW:
				enterOuterAlt(_localctx, 1);
				{
				setState(952);
				arrayKind();
				}
				break;
			case QUESTION:
				enterOuterAlt(_localctx, 2);
				{
				setState(953);
				match(QUESTION);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AtomicTypeContext extends ParserRuleContext {
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public FunctionTypeContext functionType() {
			return getRuleContext(FunctionTypeContext.class,0);
		}
		public UncertainTypeContext uncertainType() {
			return getRuleContext(UncertainTypeContext.class,0);
		}
		public AtomicTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomicType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAtomicType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAtomicType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAtomicType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomicTypeContext atomicType() throws RecognitionException {
		AtomicTypeContext _localctx = new AtomicTypeContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_atomicType);
		try {
			setState(960);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
			case INIT:
			case TEMP:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(956);
				classType();
				}
				break;
			case BOOL:
			case STRING:
			case TIME:
			case NULL:
			case PASSWORD:
			case DOUBLE:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case CHAR:
			case VOID:
			case ANY:
			case NEVER:
				enterOuterAlt(_localctx, 2);
				{
				setState(957);
				primitiveType();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 3);
				{
				setState(958);
				functionType();
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 4);
				{
				setState(959);
				uncertainType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveTypeContext extends ParserRuleContext {
		public TerminalNode BOOL() { return getToken(KiwiParser.BOOL, 0); }
		public TerminalNode BYTE() { return getToken(KiwiParser.BYTE, 0); }
		public TerminalNode SHORT() { return getToken(KiwiParser.SHORT, 0); }
		public TerminalNode INT() { return getToken(KiwiParser.INT, 0); }
		public TerminalNode LONG() { return getToken(KiwiParser.LONG, 0); }
		public TerminalNode DOUBLE() { return getToken(KiwiParser.DOUBLE, 0); }
		public TerminalNode STRING() { return getToken(KiwiParser.STRING, 0); }
		public TerminalNode PASSWORD() { return getToken(KiwiParser.PASSWORD, 0); }
		public TerminalNode TIME() { return getToken(KiwiParser.TIME, 0); }
		public TerminalNode NULL() { return getToken(KiwiParser.NULL, 0); }
		public TerminalNode VOID() { return getToken(KiwiParser.VOID, 0); }
		public TerminalNode CHAR() { return getToken(KiwiParser.CHAR, 0); }
		public TerminalNode ANY() { return getToken(KiwiParser.ANY, 0); }
		public TerminalNode NEVER() { return getToken(KiwiParser.NEVER, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterPrimitiveType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitPrimitiveType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(962);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 13743927866180L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionTypeContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(KiwiParser.ARROW, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public FunctionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterFunctionType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitFunctionType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitFunctionType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTypeContext functionType() throws RecognitionException {
		FunctionTypeContext _localctx = new FunctionTypeContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_functionType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(964);
			match(LPAREN);
			setState(973);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19224411088368452L) != 0) || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 33554449L) != 0)) {
				{
				setState(965);
				type();
				setState(970);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(966);
					match(COMMA);
					setState(967);
					type();
					}
					}
					setState(972);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(975);
			match(RPAREN);
			setState(976);
			match(ARROW);
			setState(977);
			type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UncertainTypeContext extends ParserRuleContext {
		public TerminalNode LBRACK() { return getToken(KiwiParser.LBRACK, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(KiwiParser.COMMA, 0); }
		public TerminalNode RBRACK() { return getToken(KiwiParser.RBRACK, 0); }
		public UncertainTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uncertainType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterUncertainType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitUncertainType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitUncertainType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UncertainTypeContext uncertainType() throws RecognitionException {
		UncertainTypeContext _localctx = new UncertainTypeContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_uncertainType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(979);
			match(LBRACK);
			setState(980);
			type();
			setState(981);
			match(COMMA);
			setState(982);
			type();
			setState(983);
			match(RBRACK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayKindContext extends ParserRuleContext {
		public TerminalNode R() { return getToken(KiwiParser.R, 0); }
		public TerminalNode RW() { return getToken(KiwiParser.RW, 0); }
		public ArrayKindContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayKind; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterArrayKind(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitArrayKind(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitArrayKind(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayKindContext arrayKind() throws RecognitionException {
		ArrayKindContext _localctx = new ArrayKindContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(985);
			_la = _input.LA(1);
			if ( !(_la==R || _la==RW) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassTypeContext extends ParserRuleContext {
		public List<ClassTypePartContext> classTypePart() {
			return getRuleContexts(ClassTypePartContext.class);
		}
		public ClassTypePartContext classTypePart(int i) {
			return getRuleContext(ClassTypePartContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(KiwiParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(KiwiParser.DOT, i);
		}
		public ClassTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassTypeContext classType() throws RecognitionException {
		ClassTypeContext _localctx = new ClassTypeContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_classType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(987);
			classTypePart();
			setState(992);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,97,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(988);
					match(DOT);
					setState(989);
					classTypePart();
					}
					} 
				}
				setState(994);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,97,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassTypePartContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ClassTypePartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classTypePart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassTypePart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassTypePart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassTypePart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassTypePartContext classTypePart() throws RecognitionException {
		ClassTypePartContext _localctx = new ClassTypePartContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_classTypePart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(995);
			identifier();
			setState(997);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(996);
				typeArguments();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeArgumentsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(KiwiParser.LT, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(KiwiParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitTypeArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(999);
			match(LT);
			setState(1000);
			type();
			setState(1005);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1001);
				match(COMMA);
				setState(1002);
				type();
				}
				}
				setState(1007);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1008);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModifierContext extends ParserRuleContext {
		public ClassOrInterfaceModifierContext classOrInterfaceModifier() {
			return getRuleContext(ClassOrInterfaceModifierContext.class,0);
		}
		public TerminalNode NATIVE() { return getToken(KiwiParser.NATIVE, 0); }
		public TerminalNode DELETED() { return getToken(KiwiParser.DELETED, 0); }
		public ModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModifierContext modifier() throws RecognitionException {
		ModifierContext _localctx = new ModifierContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_modifier);
		try {
			setState(1013);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIV:
			case PROT:
			case PUB:
			case STATIC:
			case VALUE:
			case TEMP:
				enterOuterAlt(_localctx, 1);
				{
				setState(1010);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1011);
				match(NATIVE);
				}
				break;
			case DELETED:
				enterOuterAlt(_localctx, 3);
				{
				setState(1012);
				match(DELETED);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassOrInterfaceModifierContext extends ParserRuleContext {
		public TerminalNode PUB() { return getToken(KiwiParser.PUB, 0); }
		public TerminalNode PROT() { return getToken(KiwiParser.PROT, 0); }
		public TerminalNode PRIV() { return getToken(KiwiParser.PRIV, 0); }
		public TerminalNode STATIC() { return getToken(KiwiParser.STATIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(KiwiParser.ABSTRACT, 0); }
		public TerminalNode VALUE() { return getToken(KiwiParser.VALUE, 0); }
		public TerminalNode TEMP() { return getToken(KiwiParser.TEMP, 0); }
		public ClassOrInterfaceModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassOrInterfaceModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassOrInterfaceModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassOrInterfaceModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceModifierContext classOrInterfaceModifier() throws RecognitionException {
		ClassOrInterfaceModifierContext _localctx = new ClassOrInterfaceModifierContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_classOrInterfaceModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1015);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 18084773427675138L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaExpressionContext extends ParserRuleContext {
		public LambdaParametersContext lambdaParameters() {
			return getRuleContext(LambdaParametersContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(KiwiParser.ARROW, 0); }
		public LambdaBodyContext lambdaBody() {
			return getRuleContext(LambdaBodyContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TypeOrVoidContext typeOrVoid() {
			return getRuleContext(TypeOrVoidContext.class,0);
		}
		public LambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLambdaExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLambdaExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaExpressionContext lambdaExpression() throws RecognitionException {
		LambdaExpressionContext _localctx = new LambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_lambdaExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1017);
			lambdaParameters();
			setState(1018);
			match(ARROW);
			setState(1024);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
			case 1:
				{
				setState(1020);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19224411088368452L) != 0) || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 33554449L) != 0)) {
					{
					setState(1019);
					typeOrVoid();
					}
				}

				setState(1022);
				lambdaBody();
				}
				break;
			case 2:
				{
				setState(1023);
				expression();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaParametersContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public LambdaParameterListContext lambdaParameterList() {
			return getRuleContext(LambdaParameterListContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LambdaParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLambdaParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLambdaParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLambdaParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaParametersContext lambdaParameters() throws RecognitionException {
		LambdaParametersContext _localctx = new LambdaParametersContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_lambdaParameters);
		int _la;
		try {
			setState(1032);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1026);
				match(LPAREN);
				setState(1028);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
					{
					setState(1027);
					lambdaParameterList();
					}
				}

				setState(1030);
				match(RPAREN);
				}
				break;
			case VALUE:
			case INIT:
			case TEMP:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(1031);
				identifier();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaParameterListContext extends ParserRuleContext {
		public List<LambdaParameterContext> lambdaParameter() {
			return getRuleContexts(LambdaParameterContext.class);
		}
		public LambdaParameterContext lambdaParameter(int i) {
			return getRuleContext(LambdaParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public LambdaParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaParameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLambdaParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLambdaParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLambdaParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaParameterListContext lambdaParameterList() throws RecognitionException {
		LambdaParameterListContext _localctx = new LambdaParameterListContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_lambdaParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1034);
			lambdaParameter();
			setState(1039);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1035);
				match(COMMA);
				setState(1036);
				lambdaParameter();
				}
				}
				setState(1041);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaParameterContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public LambdaParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLambdaParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLambdaParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLambdaParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaParameterContext lambdaParameter() throws RecognitionException {
		LambdaParameterContext _localctx = new LambdaParameterContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_lambdaParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1042);
			identifier();
			setState(1045);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1043);
				match(COLON);
				setState(1044);
				type();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaBodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public LambdaBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterLambdaBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitLambdaBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitLambdaBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaBodyContext lambdaBody() throws RecognitionException {
		LambdaBodyContext _localctx = new LambdaBodyContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_lambdaBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1047);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(KiwiParser.AT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public ElementValuePairsContext elementValuePairs() {
			return getRuleContext(ElementValuePairsContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitAnnotation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1049);
			match(AT);
			setState(1050);
			identifier();
			setState(1057);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1051);
				match(LPAREN);
				setState(1054);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,107,_ctx) ) {
				case 1:
					{
					setState(1052);
					elementValuePairs();
					}
					break;
				case 2:
					{
					setState(1053);
					expression();
					}
					break;
				}
				setState(1056);
				match(RPAREN);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementValuePairsContext extends ParserRuleContext {
		public List<ElementValuePairContext> elementValuePair() {
			return getRuleContexts(ElementValuePairContext.class);
		}
		public ElementValuePairContext elementValuePair(int i) {
			return getRuleContext(ElementValuePairContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public ElementValuePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePairs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterElementValuePairs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitElementValuePairs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitElementValuePairs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValuePairsContext elementValuePairs() throws RecognitionException {
		ElementValuePairsContext _localctx = new ElementValuePairsContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1059);
			elementValuePair();
			setState(1064);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1060);
				match(COMMA);
				setState(1061);
				elementValuePair();
				}
				}
				setState(1066);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementValuePairContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(KiwiParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ElementValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterElementValuePair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitElementValuePair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitElementValuePair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValuePairContext elementValuePair() throws RecognitionException {
		ElementValuePairContext _localctx = new ElementValuePairContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1067);
			identifier();
			setState(1068);
			match(ASSIGN);
			setState(1069);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001v\u0430\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u00076\u0002"+
		"7\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007;\u0002"+
		"<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007@\u0002"+
		"A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007E\u0002"+
		"F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007J\u0002"+
		"K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007O\u0002"+
		"P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007T\u0002"+
		"U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007Y\u0002"+
		"Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007^\u0002"+
		"_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007c\u0002"+
		"d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007h\u0002"+
		"i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007m\u0002"+
		"n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0001\u0000\u0003\u0000"+
		"\u00e6\b\u0000\u0001\u0000\u0005\u0000\u00e9\b\u0000\n\u0000\f\u0000\u00ec"+
		"\t\u0000\u0001\u0000\u0004\u0000\u00ef\b\u0000\u000b\u0000\f\u0000\u00f0"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0005\u0003\u00fa\b\u0003\n\u0003\f\u0003\u00fd\t\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0104"+
		"\b\u0004\u0001\u0005\u0005\u0005\u0107\b\u0005\n\u0005\f\u0005\u010a\t"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u010f\b\u0005\u0001"+
		"\u0005\u0001\u0005\u0003\u0005\u0113\b\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0006\u0001\u0006\u0005\u0006\u0119\b\u0006\n\u0006\f\u0006\u011c\t\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007"+
		"\u0123\b\u0007\n\u0007\f\u0007\u0126\t\u0007\u0001\b\u0005\b\u0129\b\b"+
		"\n\b\f\b\u012c\t\b\u0001\b\u0001\b\u0003\b\u0130\b\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\n\u0005\n\u0136\b\n\n\n\f\n\u0139\t\n\u0001\n\u0001\n\u0001\n"+
		"\u0001\n\u0003\n\u013f\b\n\u0001\n\u0001\n\u0003\n\u0143\b\n\u0001\n\u0003"+
		"\n\u0146\b\n\u0001\n\u0003\n\u0149\b\n\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u0150\b\u000b\n\u000b\f\u000b\u0153\t\u000b"+
		"\u0001\f\u0001\f\u0003\f\u0157\b\f\u0001\f\u0003\f\u015a\b\f\u0001\r\u0001"+
		"\r\u0005\r\u015e\b\r\n\r\f\r\u0161\t\r\u0001\u000e\u0005\u000e\u0164\b"+
		"\u000e\n\u000e\f\u000e\u0167\t\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u016c\b\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0170\b"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0005\u000f\u0176"+
		"\b\u000f\n\u000f\f\u000f\u0179\t\u000f\u0001\u000f\u0001\u000f\u0001\u0010"+
		"\u0005\u0010\u017e\b\u0010\n\u0010\f\u0010\u0181\t\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0005\u0012\u0188\b\u0012\n"+
		"\u0012\f\u0012\u018b\t\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003"+
		"\u0012\u0190\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0195"+
		"\b\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0199\b\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u01a1\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015"+
		"\u01a7\b\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u01ab\b\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u01b0\b\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u01b5\b\u0016\u0001\u0016\u0003\u0016\u01b8"+
		"\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u01be"+
		"\b\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0005\u0018\u01c6\b\u0018\n\u0018\f\u0018\u01c9\t\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u01d0\b\u0019"+
		"\n\u0019\f\u0019\u01d3\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005"+
		"\u001a\u01d8\b\u001a\n\u001a\f\u001a\u01db\t\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0003\u001b\u01e0\b\u001b\u0001\u001c\u0001\u001c\u0003\u001c"+
		"\u01e4\b\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0005\u001d\u01eb\b\u001d\n\u001d\f\u001d\u01ee\t\u001d\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f\u0001 \u0001 \u0005"+
		" \u01f8\b \n \f \u01fb\t \u0001 \u0001 \u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u0213\b!\u0001!\u0001!\u0001"+
		"!\u0004!\u0218\b!\u000b!\f!\u0219\u0001!\u0001!\u0001!\u0005!\u021f\b"+
		"!\n!\f!\u0222\t!\u0001!\u0001!\u0001!\u0003!\u0227\b!\u0001!\u0001!\u0001"+
		"!\u0001!\u0003!\u022d\b!\u0001!\u0001!\u0003!\u0231\b!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u023b\b!\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0003\"\u0241\b\"\u0001\"\u0001\"\u0003\"\u0245\b\"\u0001#"+
		"\u0001#\u0001#\u0001#\u0001$\u0001$\u0001$\u0003$\u024e\b$\u0001%\u0001"+
		"%\u0001%\u0005%\u0253\b%\n%\f%\u0256\t%\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0001\'\u0001\'\u0001\'\u0005\'\u0260\b\'\n\'\f\'\u0263\t\'\u0001(\u0001"+
		"(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001*\u0001*\u0001"+
		"*\u0001*\u0003*\u0272\b*\u0001+\u0001+\u0001+\u0001+\u0005+\u0278\b+\n"+
		"+\f+\u027b\t+\u0001+\u0003+\u027e\b+\u0003+\u0280\b+\u0001+\u0001+\u0001"+
		",\u0001,\u0003,\u0286\b,\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001"+
		"-\u0001-\u0001.\u0001.\u0001.\u0005.\u0293\b.\n.\f.\u0296\t.\u0001/\u0001"+
		"/\u0001/\u0001/\u0001/\u0001/\u0005/\u029e\b/\n/\f/\u02a1\t/\u0001/\u0001"+
		"/\u0001/\u0001/\u0001/\u00010\u00010\u00010\u00010\u00011\u00011\u0001"+
		"1\u00012\u00012\u00012\u00012\u00012\u00012\u00032\u02b5\b2\u00013\u0001"+
		"3\u00013\u00013\u00014\u00014\u00014\u00054\u02be\b4\n4\f4\u02c1\t4\u0001"+
		"5\u00015\u00016\u00016\u00056\u02c7\b6\n6\f6\u02ca\t6\u00017\u00017\u0001"+
		"7\u00018\u00018\u00018\u00018\u00018\u00018\u00038\u02d5\b8\u00019\u0001"+
		"9\u00019\u00059\u02da\b9\n9\f9\u02dd\t9\u0001:\u0001:\u0001:\u0005:\u02e2"+
		"\b:\n:\f:\u02e5\t:\u0001;\u0001;\u0001;\u0003;\u02ea\b;\u0001<\u0001<"+
		"\u0001<\u0005<\u02ef\b<\n<\f<\u02f2\t<\u0001=\u0001=\u0001=\u0005=\u02f7"+
		"\b=\n=\f=\u02fa\t=\u0001>\u0001>\u0001>\u0005>\u02ff\b>\n>\f>\u0302\t"+
		">\u0001?\u0001?\u0005?\u0306\b?\n?\f?\u0309\t?\u0001@\u0001@\u0001@\u0001"+
		"A\u0001A\u0005A\u0310\bA\nA\fA\u0313\tA\u0001B\u0001B\u0001B\u0001C\u0001"+
		"C\u0005C\u031a\bC\nC\fC\u031d\tC\u0001D\u0001D\u0001D\u0003D\u0322\bD"+
		"\u0001E\u0001E\u0001E\u0001F\u0001F\u0005F\u0329\bF\nF\fF\u032c\tF\u0001"+
		"G\u0001G\u0001G\u0001G\u0001G\u0001G\u0001G\u0003G\u0335\bG\u0001G\u0001"+
		"G\u0001H\u0001H\u0005H\u033b\bH\nH\fH\u033e\tH\u0001I\u0001I\u0001I\u0001"+
		"J\u0001J\u0005J\u0345\bJ\nJ\fJ\u0348\tJ\u0001K\u0001K\u0001K\u0001L\u0001"+
		"L\u0001L\u0005L\u0350\bL\nL\fL\u0353\tL\u0001M\u0005M\u0356\bM\nM\fM\u0359"+
		"\tM\u0001M\u0001M\u0001N\u0001N\u0001O\u0001O\u0005O\u0361\bO\nO\fO\u0364"+
		"\tO\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0003P\u036d\bP\u0001"+
		"Q\u0001Q\u0001R\u0001R\u0001R\u0001R\u0001S\u0001S\u0001S\u0003S\u0378"+
		"\bS\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0003T\u0385\bT\u0001U\u0001U\u0003U\u0389\bU\u0001U\u0001U\u0001"+
		"V\u0001V\u0001W\u0001W\u0001W\u0001W\u0001W\u0001W\u0001W\u0003W\u0396"+
		"\bW\u0001X\u0001X\u0001Y\u0001Y\u0001Z\u0001Z\u0003Z\u039e\bZ\u0001[\u0001"+
		"[\u0001\\\u0001\\\u0001\\\u0005\\\u03a5\b\\\n\\\f\\\u03a8\t\\\u0001]\u0001"+
		"]\u0001]\u0005]\u03ad\b]\n]\f]\u03b0\t]\u0001^\u0001^\u0005^\u03b4\b^"+
		"\n^\f^\u03b7\t^\u0001_\u0001_\u0003_\u03bb\b_\u0001`\u0001`\u0001`\u0001"+
		"`\u0003`\u03c1\b`\u0001a\u0001a\u0001b\u0001b\u0001b\u0001b\u0005b\u03c9"+
		"\bb\nb\fb\u03cc\tb\u0003b\u03ce\bb\u0001b\u0001b\u0001b\u0001b\u0001c"+
		"\u0001c\u0001c\u0001c\u0001c\u0001c\u0001d\u0001d\u0001e\u0001e\u0001"+
		"e\u0005e\u03df\be\ne\fe\u03e2\te\u0001f\u0001f\u0003f\u03e6\bf\u0001g"+
		"\u0001g\u0001g\u0001g\u0005g\u03ec\bg\ng\fg\u03ef\tg\u0001g\u0001g\u0001"+
		"h\u0001h\u0001h\u0003h\u03f6\bh\u0001i\u0001i\u0001j\u0001j\u0001j\u0003"+
		"j\u03fd\bj\u0001j\u0001j\u0003j\u0401\bj\u0001k\u0001k\u0003k\u0405\b"+
		"k\u0001k\u0001k\u0003k\u0409\bk\u0001l\u0001l\u0001l\u0005l\u040e\bl\n"+
		"l\fl\u0411\tl\u0001m\u0001m\u0001m\u0003m\u0416\bm\u0001n\u0001n\u0001"+
		"o\u0001o\u0001o\u0001o\u0001o\u0003o\u041f\bo\u0001o\u0003o\u0422\bo\u0001"+
		"p\u0001p\u0001p\u0005p\u0427\bp\np\fp\u042a\tp\u0001q\u0001q\u0001q\u0001"+
		"q\u0001q\u0000\u0000r\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0"+
		"\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8"+
		"\u00da\u00dc\u00de\u00e0\u00e2\u0000\u000f\u0004\u0000\u0001\u0001\u0004"+
		"\u0004\u001e\u001e  \u0001\u0000/0\u0003\u000077OPSY\u0002\u0000??BB\u0002"+
		"\u000089@A\u0001\u0000GH\u0002\u0000IJNN\u0003\u0000::<<EH\u0002\u0000"+
		";;EF\u0004\u0000..2266ss\u0001\u0000gj\u0001\u0000kl\u0007\u0000\u0002"+
		"\u0002\u0006\u0006\b\t\f\r\u0014\u0018\'\'*+\u0001\u0000qr\u0005\u0000"+
		"\u0001\u0001\u001c\u001e  ..66\u044f\u0000\u00e5\u0001\u0000\u0000\u0000"+
		"\u0002\u00f2\u0001\u0000\u0000\u0000\u0004\u00f5\u0001\u0000\u0000\u0000"+
		"\u0006\u00fb\u0001\u0000\u0000\u0000\b\u0103\u0001\u0000\u0000\u0000\n"+
		"\u0108\u0001\u0000\u0000\u0000\f\u0116\u0001\u0000\u0000\u0000\u000e\u011f"+
		"\u0001\u0000\u0000\u0000\u0010\u012f\u0001\u0000\u0000\u0000\u0012\u0131"+
		"\u0001\u0000\u0000\u0000\u0014\u0137\u0001\u0000\u0000\u0000\u0016\u014c"+
		"\u0001\u0000\u0000\u0000\u0018\u0154\u0001\u0000\u0000\u0000\u001a\u015b"+
		"\u0001\u0000\u0000\u0000\u001c\u0165\u0001\u0000\u0000\u0000\u001e\u0173"+
		"\u0001\u0000\u0000\u0000 \u017f\u0001\u0000\u0000\u0000\"\u0184\u0001"+
		"\u0000\u0000\u0000$\u0189\u0001\u0000\u0000\u0000&\u019a\u0001\u0000\u0000"+
		"\u0000(\u01a0\u0001\u0000\u0000\u0000*\u01a2\u0001\u0000\u0000\u0000,"+
		"\u01ac\u0001\u0000\u0000\u0000.\u01b9\u0001\u0000\u0000\u00000\u01c1\u0001"+
		"\u0000\u0000\u00002\u01cc\u0001\u0000\u0000\u00004\u01d4\u0001\u0000\u0000"+
		"\u00006\u01dc\u0001\u0000\u0000\u00008\u01e1\u0001\u0000\u0000\u0000:"+
		"\u01e7\u0001\u0000\u0000\u0000<\u01ef\u0001\u0000\u0000\u0000>\u01f3\u0001"+
		"\u0000\u0000\u0000@\u01f5\u0001\u0000\u0000\u0000B\u023a\u0001\u0000\u0000"+
		"\u0000D\u023c\u0001\u0000\u0000\u0000F\u0246\u0001\u0000\u0000\u0000H"+
		"\u024a\u0001\u0000\u0000\u0000J\u024f\u0001\u0000\u0000\u0000L\u0257\u0001"+
		"\u0000\u0000\u0000N\u025c\u0001\u0000\u0000\u0000P\u0264\u0001\u0000\u0000"+
		"\u0000R\u0268\u0001\u0000\u0000\u0000T\u026d\u0001\u0000\u0000\u0000V"+
		"\u0273\u0001\u0000\u0000\u0000X\u0285\u0001\u0000\u0000\u0000Z\u0287\u0001"+
		"\u0000\u0000\u0000\\\u028f\u0001\u0000\u0000\u0000^\u0297\u0001\u0000"+
		"\u0000\u0000`\u02a7\u0001\u0000\u0000\u0000b\u02ab\u0001\u0000\u0000\u0000"+
		"d\u02b4\u0001\u0000\u0000\u0000f\u02b6\u0001\u0000\u0000\u0000h\u02ba"+
		"\u0001\u0000\u0000\u0000j\u02c2\u0001\u0000\u0000\u0000l\u02c4\u0001\u0000"+
		"\u0000\u0000n\u02cb\u0001\u0000\u0000\u0000p\u02ce\u0001\u0000\u0000\u0000"+
		"r\u02d6\u0001\u0000\u0000\u0000t\u02de\u0001\u0000\u0000\u0000v\u02e6"+
		"\u0001\u0000\u0000\u0000x\u02eb\u0001\u0000\u0000\u0000z\u02f3\u0001\u0000"+
		"\u0000\u0000|\u02fb\u0001\u0000\u0000\u0000~\u0303\u0001\u0000\u0000\u0000"+
		"\u0080\u030a\u0001\u0000\u0000\u0000\u0082\u030d\u0001\u0000\u0000\u0000"+
		"\u0084\u0314\u0001\u0000\u0000\u0000\u0086\u0317\u0001\u0000\u0000\u0000"+
		"\u0088\u031e\u0001\u0000\u0000\u0000\u008a\u0323\u0001\u0000\u0000\u0000"+
		"\u008c\u0326\u0001\u0000\u0000\u0000\u008e\u0334\u0001\u0000\u0000\u0000"+
		"\u0090\u0338\u0001\u0000\u0000\u0000\u0092\u033f\u0001\u0000\u0000\u0000"+
		"\u0094\u0342\u0001\u0000\u0000\u0000\u0096\u0349\u0001\u0000\u0000\u0000"+
		"\u0098\u034c\u0001\u0000\u0000\u0000\u009a\u0357\u0001\u0000\u0000\u0000"+
		"\u009c\u035c\u0001\u0000\u0000\u0000\u009e\u035e\u0001\u0000\u0000\u0000"+
		"\u00a0\u036c\u0001\u0000\u0000\u0000\u00a2\u036e\u0001\u0000\u0000\u0000"+
		"\u00a4\u0370\u0001\u0000\u0000\u0000\u00a6\u0374\u0001\u0000\u0000\u0000"+
		"\u00a8\u0384\u0001\u0000\u0000\u0000\u00aa\u0386\u0001\u0000\u0000\u0000"+
		"\u00ac\u038c\u0001\u0000\u0000\u0000\u00ae\u0395\u0001\u0000\u0000\u0000"+
		"\u00b0\u0397\u0001\u0000\u0000\u0000\u00b2\u0399\u0001\u0000\u0000\u0000"+
		"\u00b4\u039d\u0001\u0000\u0000\u0000\u00b6\u039f\u0001\u0000\u0000\u0000"+
		"\u00b8\u03a1\u0001\u0000\u0000\u0000\u00ba\u03a9\u0001\u0000\u0000\u0000"+
		"\u00bc\u03b1\u0001\u0000\u0000\u0000\u00be\u03ba\u0001\u0000\u0000\u0000"+
		"\u00c0\u03c0\u0001\u0000\u0000\u0000\u00c2\u03c2\u0001\u0000\u0000\u0000"+
		"\u00c4\u03c4\u0001\u0000\u0000\u0000\u00c6\u03d3\u0001\u0000\u0000\u0000"+
		"\u00c8\u03d9\u0001\u0000\u0000\u0000\u00ca\u03db\u0001\u0000\u0000\u0000"+
		"\u00cc\u03e3\u0001\u0000\u0000\u0000\u00ce\u03e7\u0001\u0000\u0000\u0000"+
		"\u00d0\u03f5\u0001\u0000\u0000\u0000\u00d2\u03f7\u0001\u0000\u0000\u0000"+
		"\u00d4\u03f9\u0001\u0000\u0000\u0000\u00d6\u0408\u0001\u0000\u0000\u0000"+
		"\u00d8\u040a\u0001\u0000\u0000\u0000\u00da\u0412\u0001\u0000\u0000\u0000"+
		"\u00dc\u0417\u0001\u0000\u0000\u0000\u00de\u0419\u0001\u0000\u0000\u0000"+
		"\u00e0\u0423\u0001\u0000\u0000\u0000\u00e2\u042b\u0001\u0000\u0000\u0000"+
		"\u00e4\u00e6\u0003\u0002\u0001\u0000\u00e5\u00e4\u0001\u0000\u0000\u0000"+
		"\u00e5\u00e6\u0001\u0000\u0000\u0000\u00e6\u00ea\u0001\u0000\u0000\u0000"+
		"\u00e7\u00e9\u0003\u0004\u0002\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000"+
		"\u00e9\u00ec\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000"+
		"\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb\u00ee\u0001\u0000\u0000\u0000"+
		"\u00ec\u00ea\u0001\u0000\u0000\u0000\u00ed\u00ef\u0003\u0006\u0003\u0000"+
		"\u00ee\u00ed\u0001\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000"+
		"\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000"+
		"\u00f1\u0001\u0001\u0000\u0000\u0000\u00f2\u00f3\u0005\n\u0000\u0000\u00f3"+
		"\u00f4\u00034\u001a\u0000\u00f4\u0003\u0001\u0000\u0000\u0000\u00f5\u00f6"+
		"\u0005\u000b\u0000\u0000\u00f6\u00f7\u00034\u001a\u0000\u00f7\u0005\u0001"+
		"\u0000\u0000\u0000\u00f8\u00fa\u0003\u00d2i\u0000\u00f9\u00f8\u0001\u0000"+
		"\u0000\u0000\u00fa\u00fd\u0001\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000"+
		"\u0000\u0000\u00fb\u00fc\u0001\u0000\u0000\u0000\u00fc\u00fe\u0001\u0000"+
		"\u0000\u0000\u00fd\u00fb\u0001\u0000\u0000\u0000\u00fe\u00ff\u0003\b\u0004"+
		"\u0000\u00ff\u0007\u0001\u0000\u0000\u0000\u0100\u0104\u0003\n\u0005\u0000"+
		"\u0101\u0104\u0003\u0014\n\u0000\u0102\u0104\u0003\u001c\u000e\u0000\u0103"+
		"\u0100\u0001\u0000\u0000\u0000\u0103\u0101\u0001\u0000\u0000\u0000\u0103"+
		"\u0102\u0001\u0000\u0000\u0000\u0104\t\u0001\u0000\u0000\u0000\u0105\u0107"+
		"\u0003\u00deo\u0000\u0106\u0105\u0001\u0000\u0000\u0000\u0107\u010a\u0001"+
		"\u0000\u0000\u0000\u0108\u0106\u0001\u0000\u0000\u0000\u0108\u0109\u0001"+
		"\u0000\u0000\u0000\u0109\u010b\u0001\u0000\u0000\u0000\u010a\u0108\u0001"+
		"\u0000\u0000\u0000\u010b\u010c\u0005\u0007\u0000\u0000\u010c\u010e\u0003"+
		"\u00acV\u0000\u010d\u010f\u00030\u0018\u0000\u010e\u010d\u0001\u0000\u0000"+
		"\u0000\u010e\u010f\u0001\u0000\u0000\u0000\u010f\u0112\u0001\u0000\u0000"+
		"\u0000\u0110\u0111\u0005>\u0000\u0000\u0111\u0113\u0003\u000e\u0007\u0000"+
		"\u0112\u0110\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000\u0000"+
		"\u0113\u0114\u0001\u0000\u0000\u0000\u0114\u0115\u0003\f\u0006\u0000\u0115"+
		"\u000b\u0001\u0000\u0000\u0000\u0116\u011a\u0005\\\u0000\u0000\u0117\u0119"+
		"\u0003\u0010\b\u0000\u0118\u0117\u0001\u0000\u0000\u0000\u0119\u011c\u0001"+
		"\u0000\u0000\u0000\u011a\u0118\u0001\u0000\u0000\u0000\u011a\u011b\u0001"+
		"\u0000\u0000\u0000\u011b\u011d\u0001\u0000\u0000\u0000\u011c\u011a\u0001"+
		"\u0000\u0000\u0000\u011d\u011e\u0005]\u0000\u0000\u011e\r\u0001\u0000"+
		"\u0000\u0000\u011f\u0124\u0003\u00b6[\u0000\u0120\u0121\u0005a\u0000\u0000"+
		"\u0121\u0123\u0003\u00b6[\u0000\u0122\u0120\u0001\u0000\u0000\u0000\u0123"+
		"\u0126\u0001\u0000\u0000\u0000\u0124\u0122\u0001\u0000\u0000\u0000\u0124"+
		"\u0125\u0001\u0000\u0000\u0000\u0125\u000f\u0001\u0000\u0000\u0000\u0126"+
		"\u0124\u0001\u0000\u0000\u0000\u0127\u0129\u0003\u00d0h\u0000\u0128\u0127"+
		"\u0001\u0000\u0000\u0000\u0129\u012c\u0001\u0000\u0000\u0000\u012a\u0128"+
		"\u0001\u0000\u0000\u0000\u012a\u012b\u0001\u0000\u0000\u0000\u012b\u012d"+
		"\u0001\u0000\u0000\u0000\u012c\u012a\u0001\u0000\u0000\u0000\u012d\u0130"+
		"\u0003(\u0014\u0000\u012e\u0130\u0003\u0012\t\u0000\u012f\u012a\u0001"+
		"\u0000\u0000\u0000\u012f\u012e\u0001\u0000\u0000\u0000\u0130\u0011\u0001"+
		"\u0000\u0000\u0000\u0131\u0132\u0005 \u0000\u0000\u0132\u0133\u0003@ "+
		"\u0000\u0133\u0013\u0001\u0000\u0000\u0000\u0134\u0136\u0003\u00deo\u0000"+
		"\u0135\u0134\u0001\u0000\u0000\u0000\u0136\u0139\u0001\u0000\u0000\u0000"+
		"\u0137\u0135\u0001\u0000\u0000\u0000\u0137\u0138\u0001\u0000\u0000\u0000"+
		"\u0138\u013a\u0001\u0000\u0000\u0000\u0139\u0137\u0001\u0000\u0000\u0000"+
		"\u013a\u013b\u0005\u000f\u0000\u0000\u013b\u013e\u0003\u00acV\u0000\u013c"+
		"\u013d\u0005>\u0000\u0000\u013d\u013f\u0003\u000e\u0007\u0000\u013e\u013c"+
		"\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0140"+
		"\u0001\u0000\u0000\u0000\u0140\u0142\u0005\\\u0000\u0000\u0141\u0143\u0003"+
		"\u0016\u000b\u0000\u0142\u0141\u0001\u0000\u0000\u0000\u0142\u0143\u0001"+
		"\u0000\u0000\u0000\u0143\u0145\u0001\u0000\u0000\u0000\u0144\u0146\u0005"+
		"a\u0000\u0000\u0145\u0144\u0001\u0000\u0000\u0000\u0145\u0146\u0001\u0000"+
		"\u0000\u0000\u0146\u0148\u0001\u0000\u0000\u0000\u0147\u0149\u0003\u001a"+
		"\r\u0000\u0148\u0147\u0001\u0000\u0000\u0000\u0148\u0149\u0001\u0000\u0000"+
		"\u0000\u0149\u014a\u0001\u0000\u0000\u0000\u014a\u014b\u0005]\u0000\u0000"+
		"\u014b\u0015\u0001\u0000\u0000\u0000\u014c\u0151\u0003\u0018\f\u0000\u014d"+
		"\u014e\u0005a\u0000\u0000\u014e\u0150\u0003\u0018\f\u0000\u014f\u014d"+
		"\u0001\u0000\u0000\u0000\u0150\u0153\u0001\u0000\u0000\u0000\u0151\u014f"+
		"\u0001\u0000\u0000\u0000\u0151\u0152\u0001\u0000\u0000\u0000\u0152\u0017"+
		"\u0001\u0000\u0000\u0000\u0153\u0151\u0001\u0000\u0000\u0000\u0154\u0156"+
		"\u0003\u00acV\u0000\u0155\u0157\u0003\u00aaU\u0000\u0156\u0155\u0001\u0000"+
		"\u0000\u0000\u0156\u0157\u0001\u0000\u0000\u0000\u0157\u0159\u0001\u0000"+
		"\u0000\u0000\u0158\u015a\u0003\f\u0006\u0000\u0159\u0158\u0001\u0000\u0000"+
		"\u0000\u0159\u015a\u0001\u0000\u0000\u0000\u015a\u0019\u0001\u0000\u0000"+
		"\u0000\u015b\u015f\u0005`\u0000\u0000\u015c\u015e\u0003\u0010\b\u0000"+
		"\u015d\u015c\u0001\u0000\u0000\u0000\u015e\u0161\u0001\u0000\u0000\u0000"+
		"\u015f\u015d\u0001\u0000\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000"+
		"\u0160\u001b\u0001\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000\u0000"+
		"\u0162\u0164\u0003\u00deo\u0000\u0163\u0162\u0001\u0000\u0000\u0000\u0164"+
		"\u0167\u0001\u0000\u0000\u0000\u0165\u0163\u0001\u0000\u0000\u0000\u0165"+
		"\u0166\u0001\u0000\u0000\u0000\u0166\u0168\u0001\u0000\u0000\u0000\u0167"+
		"\u0165\u0001\u0000\u0000\u0000\u0168\u0169\u0005\u0019\u0000\u0000\u0169"+
		"\u016b\u0003\u00acV\u0000\u016a\u016c\u00030\u0018\u0000\u016b\u016a\u0001"+
		"\u0000\u0000\u0000\u016b\u016c\u0001\u0000\u0000\u0000\u016c\u016f\u0001"+
		"\u0000\u0000\u0000\u016d\u016e\u0005>\u0000\u0000\u016e\u0170\u0003\u000e"+
		"\u0007\u0000\u016f\u016d\u0001\u0000\u0000\u0000\u016f\u0170\u0001\u0000"+
		"\u0000\u0000\u0170\u0171\u0001\u0000\u0000\u0000\u0171\u0172\u0003\u001e"+
		"\u000f\u0000\u0172\u001d\u0001\u0000\u0000\u0000\u0173\u0177\u0005\\\u0000"+
		"\u0000\u0174\u0176\u0003 \u0010\u0000\u0175\u0174\u0001\u0000\u0000\u0000"+
		"\u0176\u0179\u0001\u0000\u0000\u0000\u0177\u0175\u0001\u0000\u0000\u0000"+
		"\u0177\u0178\u0001\u0000\u0000\u0000\u0178\u017a\u0001\u0000\u0000\u0000"+
		"\u0179\u0177\u0001\u0000\u0000\u0000\u017a\u017b\u0005]\u0000\u0000\u017b"+
		"\u001f\u0001\u0000\u0000\u0000\u017c\u017e\u0003\u00d0h\u0000\u017d\u017c"+
		"\u0001\u0000\u0000\u0000\u017e\u0181\u0001\u0000\u0000\u0000\u017f\u017d"+
		"\u0001\u0000\u0000\u0000\u017f\u0180\u0001\u0000\u0000\u0000\u0180\u0182"+
		"\u0001\u0000\u0000\u0000\u0181\u017f\u0001\u0000\u0000\u0000\u0182\u0183"+
		"\u0003\"\u0011\u0000\u0183!\u0001\u0000\u0000\u0000\u0184\u0185\u0003"+
		"$\u0012\u0000\u0185#\u0001\u0000\u0000\u0000\u0186\u0188\u0003&\u0013"+
		"\u0000\u0187\u0186\u0001\u0000\u0000\u0000\u0188\u018b\u0001\u0000\u0000"+
		"\u0000\u0189\u0187\u0001\u0000\u0000\u0000\u0189\u018a\u0001\u0000\u0000"+
		"\u0000\u018a\u018c\u0001\u0000\u0000\u0000\u018b\u0189\u0001\u0000\u0000"+
		"\u0000\u018c\u018d\u0005-\u0000\u0000\u018d\u018f\u0003\u00acV\u0000\u018e"+
		"\u0190\u00030\u0018\u0000\u018f\u018e\u0001\u0000\u0000\u0000\u018f\u0190"+
		"\u0001\u0000\u0000\u0000\u0190\u0191\u0001\u0000\u0000\u0000\u0191\u0194"+
		"\u00038\u001c\u0000\u0192\u0193\u0005c\u0000\u0000\u0193\u0195\u0003\u00b4"+
		"Z\u0000\u0194\u0192\u0001\u0000\u0000\u0000\u0194\u0195\u0001\u0000\u0000"+
		"\u0000\u0195\u0198\u0001\u0000\u0000\u0000\u0196\u0197\u0005%\u0000\u0000"+
		"\u0197\u0199\u00032\u0019\u0000\u0198\u0196\u0001\u0000\u0000\u0000\u0198"+
		"\u0199\u0001\u0000\u0000\u0000\u0199%\u0001\u0000\u0000\u0000\u019a\u019b"+
		"\u0007\u0000\u0000\u0000\u019b\'\u0001\u0000\u0000\u0000\u019c\u01a1\u0003"+
		",\u0016\u0000\u019d\u01a1\u0003*\u0015\u0000\u019e\u01a1\u0003.\u0017"+
		"\u0000\u019f\u01a1\u0003\b\u0004\u0000\u01a0\u019c\u0001\u0000\u0000\u0000"+
		"\u01a0\u019d\u0001\u0000\u0000\u0000\u01a0\u019e\u0001\u0000\u0000\u0000"+
		"\u01a0\u019f\u0001\u0000\u0000\u0000\u01a1)\u0001\u0000\u0000\u0000\u01a2"+
		"\u01a3\u0007\u0001\u0000\u0000\u01a3\u01a6\u0003\u00acV\u0000\u01a4\u01a5"+
		"\u0005>\u0000\u0000\u01a5\u01a7\u0003\u00b6[\u0000\u01a6\u01a4\u0001\u0000"+
		"\u0000\u0000\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7\u01aa\u0001\u0000"+
		"\u0000\u0000\u01a8\u01a9\u00057\u0000\u0000\u01a9\u01ab\u0003j5\u0000"+
		"\u01aa\u01a8\u0001\u0000\u0000\u0000\u01aa\u01ab\u0001\u0000\u0000\u0000"+
		"\u01ab+\u0001\u0000\u0000\u0000\u01ac\u01ad\u0005-\u0000\u0000\u01ad\u01af"+
		"\u0003\u00acV\u0000\u01ae\u01b0\u00030\u0018\u0000\u01af\u01ae\u0001\u0000"+
		"\u0000\u0000\u01af\u01b0\u0001\u0000\u0000\u0000\u01b0\u01b1\u0001\u0000"+
		"\u0000\u0000\u01b1\u01b4\u00038\u001c\u0000\u01b2\u01b3\u0005c\u0000\u0000"+
		"\u01b3\u01b5\u0003\u00b4Z\u0000\u01b4\u01b2\u0001\u0000\u0000\u0000\u01b4"+
		"\u01b5\u0001\u0000\u0000\u0000\u01b5\u01b7\u0001\u0000\u0000\u0000\u01b6"+
		"\u01b8\u0003>\u001f\u0000\u01b7\u01b6\u0001\u0000\u0000\u0000\u01b7\u01b8"+
		"\u0001\u0000\u0000\u0000\u01b8-\u0001\u0000\u0000\u0000\u01b9\u01ba\u0005"+
		"2\u0000\u0000\u01ba\u01bd\u00038\u001c\u0000\u01bb\u01bc\u0005%\u0000"+
		"\u0000\u01bc\u01be\u00032\u0019\u0000\u01bd\u01bb\u0001\u0000\u0000\u0000"+
		"\u01bd\u01be\u0001\u0000\u0000\u0000\u01be\u01bf\u0001\u0000\u0000\u0000"+
		"\u01bf\u01c0\u0003@ \u0000\u01c0/\u0001\u0000\u0000\u0000\u01c1\u01c2"+
		"\u00059\u0000\u0000\u01c2\u01c7\u00036\u001b\u0000\u01c3\u01c4\u0005a"+
		"\u0000\u0000\u01c4\u01c6\u00036\u001b\u0000\u01c5\u01c3\u0001\u0000\u0000"+
		"\u0000\u01c6\u01c9\u0001\u0000\u0000\u0000\u01c7\u01c5\u0001\u0000\u0000"+
		"\u0000\u01c7\u01c8\u0001\u0000\u0000\u0000\u01c8\u01ca\u0001\u0000\u0000"+
		"\u0000\u01c9\u01c7\u0001\u0000\u0000\u0000\u01ca\u01cb\u00058\u0000\u0000"+
		"\u01cb1\u0001\u0000\u0000\u0000\u01cc\u01d1\u00034\u001a\u0000\u01cd\u01ce"+
		"\u0005a\u0000\u0000\u01ce\u01d0\u00034\u001a\u0000\u01cf\u01cd\u0001\u0000"+
		"\u0000\u0000\u01d0\u01d3\u0001\u0000\u0000\u0000\u01d1\u01cf\u0001\u0000"+
		"\u0000\u0000\u01d1\u01d2\u0001\u0000\u0000\u0000\u01d23\u0001\u0000\u0000"+
		"\u0000\u01d3\u01d1\u0001\u0000\u0000\u0000\u01d4\u01d9\u0003\u00acV\u0000"+
		"\u01d5\u01d6\u0005b\u0000\u0000\u01d6\u01d8\u0003\u00acV\u0000\u01d7\u01d5"+
		"\u0001\u0000\u0000\u0000\u01d8\u01db\u0001\u0000\u0000\u0000\u01d9\u01d7"+
		"\u0001\u0000\u0000\u0000\u01d9\u01da\u0001\u0000\u0000\u0000\u01da5\u0001"+
		"\u0000\u0000\u0000\u01db\u01d9\u0001\u0000\u0000\u0000\u01dc\u01df\u0003"+
		"\u00acV\u0000\u01dd\u01de\u0005>\u0000\u0000\u01de\u01e0\u0003\u00b6["+
		"\u0000\u01df\u01dd\u0001\u0000\u0000\u0000\u01df\u01e0\u0001\u0000\u0000"+
		"\u0000\u01e07\u0001\u0000\u0000\u0000\u01e1\u01e3\u0005Z\u0000\u0000\u01e2"+
		"\u01e4\u0003:\u001d\u0000\u01e3\u01e2\u0001\u0000\u0000\u0000\u01e3\u01e4"+
		"\u0001\u0000\u0000\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5\u01e6"+
		"\u0005[\u0000\u0000\u01e69\u0001\u0000\u0000\u0000\u01e7\u01ec\u0003<"+
		"\u001e\u0000\u01e8\u01e9\u0005a\u0000\u0000\u01e9\u01eb\u0003<\u001e\u0000"+
		"\u01ea\u01e8\u0001\u0000\u0000\u0000\u01eb\u01ee\u0001\u0000\u0000\u0000"+
		"\u01ec\u01ea\u0001\u0000\u0000\u0000\u01ec\u01ed\u0001\u0000\u0000\u0000"+
		"\u01ed;\u0001\u0000\u0000\u0000\u01ee\u01ec\u0001\u0000\u0000\u0000\u01ef"+
		"\u01f0\u0003\u00acV\u0000\u01f0\u01f1\u0005>\u0000\u0000\u01f1\u01f2\u0003"+
		"\u00b6[\u0000\u01f2=\u0001\u0000\u0000\u0000\u01f3\u01f4\u0003@ \u0000"+
		"\u01f4?\u0001\u0000\u0000\u0000\u01f5\u01f9\u0005\\\u0000\u0000\u01f6"+
		"\u01f8\u0003B!\u0000\u01f7\u01f6\u0001\u0000\u0000\u0000\u01f8\u01fb\u0001"+
		"\u0000\u0000\u0000\u01f9\u01f7\u0001\u0000\u0000\u0000\u01f9\u01fa\u0001"+
		"\u0000\u0000\u0000\u01fa\u01fc\u0001\u0000\u0000\u0000\u01fb\u01f9\u0001"+
		"\u0000\u0000\u0000\u01fc\u01fd\u0005]\u0000\u0000\u01fdA\u0001\u0000\u0000"+
		"\u0000\u01fe\u01ff\u0005(\u0000\u0000\u01ff\u0200\u0003f3\u0000\u0200"+
		"\u0201\u0003B!\u0000\u0201\u023b\u0001\u0000\u0000\u0000\u0202\u0203\u0005"+
		")\u0000\u0000\u0203\u0204\u0003B!\u0000\u0204\u0205\u0005(\u0000\u0000"+
		"\u0205\u0206\u0003f3\u0000\u0206\u023b\u0001\u0000\u0000\u0000\u0207\u0208"+
		"\u0005\u0011\u0000\u0000\u0208\u0209\u0005Z\u0000\u0000\u0209\u020a\u0003"+
		"F#\u0000\u020a\u020b\u0005[\u0000\u0000\u020b\u020c\u0003B!\u0000\u020c"+
		"\u023b\u0001\u0000\u0000\u0000\u020d\u020e\u0005\u0012\u0000\u0000\u020e"+
		"\u020f\u0003f3\u0000\u020f\u0212\u0003B!\u0000\u0210\u0211\u0005\u000e"+
		"\u0000\u0000\u0211\u0213\u0003B!\u0000\u0212\u0210\u0001\u0000\u0000\u0000"+
		"\u0212\u0213\u0001\u0000\u0000\u0000\u0213\u023b\u0001\u0000\u0000\u0000"+
		"\u0214\u0215\u0005&\u0000\u0000\u0215\u0217\u0003@ \u0000\u0216\u0218"+
		"\u0003Z-\u0000\u0217\u0216\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000"+
		"\u0000\u0000\u0219\u0217\u0001\u0000\u0000\u0000\u0219\u021a\u0001\u0000"+
		"\u0000\u0000\u021a\u023b\u0001\u0000\u0000\u0000\u021b\u021c\u0005\"\u0000"+
		"\u0000\u021c\u0220\u0005\\\u0000\u0000\u021d\u021f\u0003b1\u0000\u021e"+
		"\u021d\u0001\u0000\u0000\u0000\u021f\u0222\u0001\u0000\u0000\u0000\u0220"+
		"\u021e\u0001\u0000\u0000\u0000\u0220\u0221\u0001\u0000\u0000\u0000\u0221"+
		"\u0223\u0001\u0000\u0000\u0000\u0222\u0220\u0001\u0000\u0000\u0000\u0223"+
		"\u023b\u0005]\u0000\u0000\u0224\u0226\u0005\u001f\u0000\u0000\u0225\u0227"+
		"\u0003j5\u0000\u0226\u0225\u0001\u0000\u0000\u0000\u0226\u0227\u0001\u0000"+
		"\u0000\u0000\u0227\u023b\u0001\u0000\u0000\u0000\u0228\u0229\u0005$\u0000"+
		"\u0000\u0229\u023b\u0003j5\u0000\u022a\u022c\u00055\u0000\u0000\u022b"+
		"\u022d\u0003\u00acV\u0000\u022c\u022b\u0001\u0000\u0000\u0000\u022c\u022d"+
		"\u0001\u0000\u0000\u0000\u022d\u023b\u0001\u0000\u0000\u0000\u022e\u0230"+
		"\u00054\u0000\u0000\u022f\u0231\u0003\u00acV\u0000\u0230\u022f\u0001\u0000"+
		"\u0000\u0000\u0230\u0231\u0001\u0000\u0000\u0000\u0231\u023b\u0001\u0000"+
		"\u0000\u0000\u0232\u0233\u0003\u00acV\u0000\u0233\u0234\u0005>\u0000\u0000"+
		"\u0234\u0235\u0003B!\u0000\u0235\u023b\u0001\u0000\u0000\u0000\u0236\u023b"+
		"\u0005`\u0000\u0000\u0237\u023b\u0003j5\u0000\u0238\u023b\u0003D\"\u0000"+
		"\u0239\u023b\u0003@ \u0000\u023a\u01fe\u0001\u0000\u0000\u0000\u023a\u0202"+
		"\u0001\u0000\u0000\u0000\u023a\u0207\u0001\u0000\u0000\u0000\u023a\u020d"+
		"\u0001\u0000\u0000\u0000\u023a\u0214\u0001\u0000\u0000\u0000\u023a\u021b"+
		"\u0001\u0000\u0000\u0000\u023a\u0224\u0001\u0000\u0000\u0000\u023a\u0228"+
		"\u0001\u0000\u0000\u0000\u023a\u022a\u0001\u0000\u0000\u0000\u023a\u022e"+
		"\u0001\u0000\u0000\u0000\u023a\u0232\u0001\u0000\u0000\u0000\u023a\u0236"+
		"\u0001\u0000\u0000\u0000\u023a\u0237\u0001\u0000\u0000\u0000\u023a\u0238"+
		"\u0001\u0000\u0000\u0000\u023a\u0239\u0001\u0000\u0000\u0000\u023bC\u0001"+
		"\u0000\u0000\u0000\u023c\u023d\u0007\u0001\u0000\u0000\u023d\u0240\u0003"+
		"\u00acV\u0000\u023e\u023f\u0005>\u0000\u0000\u023f\u0241\u0003\u00b6["+
		"\u0000\u0240\u023e\u0001\u0000\u0000\u0000\u0240\u0241\u0001\u0000\u0000"+
		"\u0000\u0241\u0244\u0001\u0000\u0000\u0000\u0242\u0243\u00057\u0000\u0000"+
		"\u0243\u0245\u0003j5\u0000\u0244\u0242\u0001\u0000\u0000\u0000\u0244\u0245"+
		"\u0001\u0000\u0000\u0000\u0245E\u0001\u0000\u0000\u0000\u0246\u0247\u0003"+
		"H$\u0000\u0247\u0248\u00053\u0000\u0000\u0248\u0249\u0003j5\u0000\u0249"+
		"G\u0001\u0000\u0000\u0000\u024a\u024d\u0003\u00acV\u0000\u024b\u024c\u0005"+
		">\u0000\u0000\u024c\u024e\u0003\u00b6[\u0000\u024d\u024b\u0001\u0000\u0000"+
		"\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024eI\u0001\u0000\u0000\u0000"+
		"\u024f\u0254\u0003L&\u0000\u0250\u0251\u0005a\u0000\u0000\u0251\u0253"+
		"\u0003L&\u0000\u0252\u0250\u0001\u0000\u0000\u0000\u0253\u0256\u0001\u0000"+
		"\u0000\u0000\u0254\u0252\u0001\u0000\u0000\u0000\u0254\u0255\u0001\u0000"+
		"\u0000\u0000\u0255K\u0001\u0000\u0000\u0000\u0256\u0254\u0001\u0000\u0000"+
		"\u0000\u0257\u0258\u0003\u00b6[\u0000\u0258\u0259\u0003\u00acV\u0000\u0259"+
		"\u025a\u00057\u0000\u0000\u025a\u025b\u0003j5\u0000\u025bM\u0001\u0000"+
		"\u0000\u0000\u025c\u0261\u0003P(\u0000\u025d\u025e\u0005a\u0000\u0000"+
		"\u025e\u0260\u0003P(\u0000\u025f\u025d\u0001\u0000\u0000\u0000\u0260\u0263"+
		"\u0001\u0000\u0000\u0000\u0261\u025f\u0001\u0000\u0000\u0000\u0261\u0262"+
		"\u0001\u0000\u0000\u0000\u0262O\u0001\u0000\u0000\u0000\u0263\u0261\u0001"+
		"\u0000\u0000\u0000\u0264\u0265\u0003\u00acV\u0000\u0265\u0266\u00057\u0000"+
		"\u0000\u0266\u0267\u0003j5\u0000\u0267Q\u0001\u0000\u0000\u0000\u0268"+
		"\u0269\u0005\u001b\u0000\u0000\u0269\u026a\u0003\u00cae\u0000\u026a\u026b"+
		"\u0003\u00aaU\u0000\u026b\u026c\u0003\f\u0006\u0000\u026cS\u0001\u0000"+
		"\u0000\u0000\u026d\u026e\u0005\u001b\u0000\u0000\u026e\u026f\u0003\u00b6"+
		"[\u0000\u026f\u0271\u0003\u00c8d\u0000\u0270\u0272\u0003V+\u0000\u0271"+
		"\u0270\u0001\u0000\u0000\u0000\u0271\u0272\u0001\u0000\u0000\u0000\u0272"+
		"U\u0001\u0000\u0000\u0000\u0273\u027f\u0005\\\u0000\u0000\u0274\u0279"+
		"\u0003X,\u0000\u0275\u0276\u0005a\u0000\u0000\u0276\u0278\u0003X,\u0000"+
		"\u0277\u0275\u0001\u0000\u0000\u0000\u0278\u027b\u0001\u0000\u0000\u0000"+
		"\u0279\u0277\u0001\u0000\u0000\u0000\u0279\u027a\u0001\u0000\u0000\u0000"+
		"\u027a\u027d\u0001\u0000\u0000\u0000\u027b\u0279\u0001\u0000\u0000\u0000"+
		"\u027c\u027e\u0005a\u0000\u0000\u027d\u027c\u0001\u0000\u0000\u0000\u027d"+
		"\u027e\u0001\u0000\u0000\u0000\u027e\u0280\u0001\u0000\u0000\u0000\u027f"+
		"\u0274\u0001\u0000\u0000\u0000\u027f\u0280\u0001\u0000\u0000\u0000\u0280"+
		"\u0281\u0001\u0000\u0000\u0000\u0281\u0282\u0005]\u0000\u0000\u0282W\u0001"+
		"\u0000\u0000\u0000\u0283\u0286\u0003V+\u0000\u0284\u0286\u0003j5\u0000"+
		"\u0285\u0283\u0001\u0000\u0000\u0000\u0285\u0284\u0001\u0000\u0000\u0000"+
		"\u0286Y\u0001\u0000\u0000\u0000\u0287\u0288\u0005\u0005\u0000\u0000\u0288"+
		"\u0289\u0005Z\u0000\u0000\u0289\u028a\u0003\u00acV\u0000\u028a\u028b\u0005"+
		">\u0000\u0000\u028b\u028c\u0003\u00b6[\u0000\u028c\u028d\u0005[\u0000"+
		"\u0000\u028d\u028e\u0003@ \u0000\u028e[\u0001\u0000\u0000\u0000\u028f"+
		"\u0294\u0003^/\u0000\u0290\u0291\u0005a\u0000\u0000\u0291\u0293\u0003"+
		"^/\u0000\u0292\u0290\u0001\u0000\u0000\u0000\u0293\u0296\u0001\u0000\u0000"+
		"\u0000\u0294\u0292\u0001\u0000\u0000\u0000\u0294\u0295\u0001\u0000\u0000"+
		"\u0000\u0295]\u0001\u0000\u0000\u0000\u0296\u0294\u0001\u0000\u0000\u0000"+
		"\u0297\u0298\u0003\u00acV\u0000\u0298\u0299\u0005>\u0000\u0000\u0299\u029f"+
		"\u0005\\\u0000\u0000\u029a\u029b\u0003`0\u0000\u029b\u029c\u0005a\u0000"+
		"\u0000\u029c\u029e\u0001\u0000\u0000\u0000\u029d\u029a\u0001\u0000\u0000"+
		"\u0000\u029e\u02a1\u0001\u0000\u0000\u0000\u029f\u029d\u0001\u0000\u0000"+
		"\u0000\u029f\u02a0\u0001\u0000\u0000\u0000\u02a0\u02a2\u0001\u0000\u0000"+
		"\u0000\u02a1\u029f\u0001\u0000\u0000\u0000\u02a2\u02a3\u0005\u0004\u0000"+
		"\u0000\u02a3\u02a4\u0005>\u0000\u0000\u02a4\u02a5\u0003j5\u0000\u02a5"+
		"\u02a6\u0005]\u0000\u0000\u02a6_\u0001\u0000\u0000\u0000\u02a7\u02a8\u0003"+
		"\u00acV\u0000\u02a8\u02a9\u0005>\u0000\u0000\u02a9\u02aa\u0003j5\u0000"+
		"\u02aaa\u0001\u0000\u0000\u0000\u02ab\u02ac\u0003d2\u0000\u02ac\u02ad"+
		"\u0003@ \u0000\u02adc\u0001\u0000\u0000\u0000\u02ae\u02af\u0005\u0003"+
		"\u0000\u0000\u02af\u02b0\u0003j5\u0000\u02b0\u02b1\u0005c\u0000\u0000"+
		"\u02b1\u02b5\u0001\u0000\u0000\u0000\u02b2\u02b3\u0005\u0004\u0000\u0000"+
		"\u02b3\u02b5\u0005c\u0000\u0000\u02b4\u02ae\u0001\u0000\u0000\u0000\u02b4"+
		"\u02b2\u0001\u0000\u0000\u0000\u02b5e\u0001\u0000\u0000\u0000\u02b6\u02b7"+
		"\u0005Z\u0000\u0000\u02b7\u02b8\u0003j5\u0000\u02b8\u02b9\u0005[\u0000"+
		"\u0000\u02b9g\u0001\u0000\u0000\u0000\u02ba\u02bf\u0003j5\u0000\u02bb"+
		"\u02bc\u0005a\u0000\u0000\u02bc\u02be\u0003j5\u0000\u02bd\u02bb\u0001"+
		"\u0000\u0000\u0000\u02be\u02c1\u0001\u0000\u0000\u0000\u02bf\u02bd\u0001"+
		"\u0000\u0000\u0000\u02bf\u02c0\u0001\u0000\u0000\u0000\u02c0i\u0001\u0000"+
		"\u0000\u0000\u02c1\u02bf\u0001\u0000\u0000\u0000\u02c2\u02c3\u0003l6\u0000"+
		"\u02c3k\u0001\u0000\u0000\u0000\u02c4\u02c8\u0003p8\u0000\u02c5\u02c7"+
		"\u0003n7\u0000\u02c6\u02c5\u0001\u0000\u0000\u0000\u02c7\u02ca\u0001\u0000"+
		"\u0000\u0000\u02c8\u02c6\u0001\u0000\u0000\u0000\u02c8\u02c9\u0001\u0000"+
		"\u0000\u0000\u02c9m\u0001\u0000\u0000\u0000\u02ca\u02c8\u0001\u0000\u0000"+
		"\u0000\u02cb\u02cc\u0007\u0002\u0000\u0000\u02cc\u02cd\u0003l6\u0000\u02cd"+
		"o\u0001\u0000\u0000\u0000\u02ce\u02d4\u0003r9\u0000\u02cf\u02d0\u0005"+
		"=\u0000\u0000\u02d0\u02d1\u0003r9\u0000\u02d1\u02d2\u0005>\u0000\u0000"+
		"\u02d2\u02d3\u0003p8\u0000\u02d3\u02d5\u0001\u0000\u0000\u0000\u02d4\u02cf"+
		"\u0001\u0000\u0000\u0000\u02d4\u02d5\u0001\u0000\u0000\u0000\u02d5q\u0001"+
		"\u0000\u0000\u0000\u02d6\u02db\u0003t:\u0000\u02d7\u02d8\u0005D\u0000"+
		"\u0000\u02d8\u02da\u0003t:\u0000\u02d9\u02d7\u0001\u0000\u0000\u0000\u02da"+
		"\u02dd\u0001\u0000\u0000\u0000\u02db\u02d9\u0001\u0000\u0000\u0000\u02db"+
		"\u02dc\u0001\u0000\u0000\u0000\u02dcs\u0001\u0000\u0000\u0000\u02dd\u02db"+
		"\u0001\u0000\u0000\u0000\u02de\u02e3\u0003v;\u0000\u02df\u02e0\u0005C"+
		"\u0000\u0000\u02e0\u02e2\u0003v;\u0000\u02e1\u02df\u0001\u0000\u0000\u0000"+
		"\u02e2\u02e5\u0001\u0000\u0000\u0000\u02e3\u02e1\u0001\u0000\u0000\u0000"+
		"\u02e3\u02e4\u0001\u0000\u0000\u0000\u02e4u\u0001\u0000\u0000\u0000\u02e5"+
		"\u02e3\u0001\u0000\u0000\u0000\u02e6\u02e9\u0003x<\u0000\u02e7\u02e8\u0005"+
		"f\u0000\u0000\u02e8\u02ea\u0003x<\u0000\u02e9\u02e7\u0001\u0000\u0000"+
		"\u0000\u02e9\u02ea\u0001\u0000\u0000\u0000\u02eaw\u0001\u0000\u0000\u0000"+
		"\u02eb\u02f0\u0003z=\u0000\u02ec\u02ed\u0005L\u0000\u0000\u02ed\u02ef"+
		"\u0003z=\u0000\u02ee\u02ec\u0001\u0000\u0000\u0000\u02ef\u02f2\u0001\u0000"+
		"\u0000\u0000\u02f0\u02ee\u0001\u0000\u0000\u0000\u02f0\u02f1\u0001\u0000"+
		"\u0000\u0000\u02f1y\u0001\u0000\u0000\u0000\u02f2\u02f0\u0001\u0000\u0000"+
		"\u0000\u02f3\u02f8\u0003|>\u0000\u02f4\u02f5\u0005K\u0000\u0000\u02f5"+
		"\u02f7\u0003|>\u0000\u02f6\u02f4\u0001\u0000\u0000\u0000\u02f7\u02fa\u0001"+
		"\u0000\u0000\u0000\u02f8\u02f6\u0001\u0000\u0000\u0000\u02f8\u02f9\u0001"+
		"\u0000\u0000\u0000\u02f9{\u0001\u0000\u0000\u0000\u02fa\u02f8\u0001\u0000"+
		"\u0000\u0000\u02fb\u0300\u0003~?\u0000\u02fc\u02fd\u0005M\u0000\u0000"+
		"\u02fd\u02ff\u0003~?\u0000\u02fe\u02fc\u0001\u0000\u0000\u0000\u02ff\u0302"+
		"\u0001\u0000\u0000\u0000\u0300\u02fe\u0001\u0000\u0000\u0000\u0300\u0301"+
		"\u0001\u0000\u0000\u0000\u0301}\u0001\u0000\u0000\u0000\u0302\u0300\u0001"+
		"\u0000\u0000\u0000\u0303\u0307\u0003\u0082A\u0000\u0304\u0306\u0003\u0080"+
		"@\u0000\u0305\u0304\u0001\u0000\u0000\u0000\u0306\u0309\u0001\u0000\u0000"+
		"\u0000\u0307\u0305\u0001\u0000\u0000\u0000\u0307\u0308\u0001\u0000\u0000"+
		"\u0000\u0308\u007f\u0001\u0000\u0000\u0000\u0309\u0307\u0001\u0000\u0000"+
		"\u0000\u030a\u030b\u0007\u0003\u0000\u0000\u030b\u030c\u0003\u0082A\u0000"+
		"\u030c\u0081\u0001\u0000\u0000\u0000\u030d\u0311\u0003\u0086C\u0000\u030e"+
		"\u0310\u0003\u0084B\u0000\u030f\u030e\u0001\u0000\u0000\u0000\u0310\u0313"+
		"\u0001\u0000\u0000\u0000\u0311\u030f\u0001\u0000\u0000\u0000\u0311\u0312"+
		"\u0001\u0000\u0000\u0000\u0312\u0083\u0001\u0000\u0000\u0000\u0313\u0311"+
		"\u0001\u0000\u0000\u0000\u0314\u0315\u0007\u0004\u0000\u0000\u0315\u0316"+
		"\u0003\u0086C\u0000\u0316\u0085\u0001\u0000\u0000\u0000\u0317\u031b\u0003"+
		"\u008cF\u0000\u0318\u031a\u0003\u0088D\u0000\u0319\u0318\u0001\u0000\u0000"+
		"\u0000\u031a\u031d\u0001\u0000\u0000\u0000\u031b\u0319\u0001\u0000\u0000"+
		"\u0000\u031b\u031c\u0001\u0000\u0000\u0000\u031c\u0087\u0001\u0000\u0000"+
		"\u0000\u031d\u031b\u0001\u0000\u0000\u0000\u031e\u0321\u0005\u0013\u0000"+
		"\u0000\u031f\u0322\u0003\u008aE\u0000\u0320\u0322\u0003\u00b6[\u0000\u0321"+
		"\u031f\u0001\u0000\u0000\u0000\u0321\u0320\u0001\u0000\u0000\u0000\u0322"+
		"\u0089\u0001\u0000\u0000\u0000\u0323\u0324\u0003\u00b6[\u0000\u0324\u0325"+
		"\u0003\u00acV\u0000\u0325\u008b\u0001\u0000\u0000\u0000\u0326\u032a\u0003"+
		"\u0090H\u0000\u0327\u0329\u0003\u008eG\u0000\u0328\u0327\u0001\u0000\u0000"+
		"\u0000\u0329\u032c\u0001\u0000\u0000\u0000\u032a\u0328\u0001\u0000\u0000"+
		"\u0000\u032a\u032b\u0001\u0000\u0000\u0000\u032b\u008d\u0001\u0000\u0000"+
		"\u0000\u032c\u032a\u0001\u0000\u0000\u0000\u032d\u032e\u00058\u0000\u0000"+
		"\u032e\u0335\u00058\u0000\u0000\u032f\u0330\u00059\u0000\u0000\u0330\u0335"+
		"\u00059\u0000\u0000\u0331\u0332\u00058\u0000\u0000\u0332\u0333\u00058"+
		"\u0000\u0000\u0333\u0335\u00058\u0000\u0000\u0334\u032d\u0001\u0000\u0000"+
		"\u0000\u0334\u032f\u0001\u0000\u0000\u0000\u0334\u0331\u0001\u0000\u0000"+
		"\u0000\u0335\u0336\u0001\u0000\u0000\u0000\u0336\u0337\u0003\u0090H\u0000"+
		"\u0337\u008f\u0001\u0000\u0000\u0000\u0338\u033c\u0003\u0094J\u0000\u0339"+
		"\u033b\u0003\u0092I\u0000\u033a\u0339\u0001\u0000\u0000\u0000\u033b\u033e"+
		"\u0001\u0000\u0000\u0000\u033c\u033a\u0001\u0000\u0000\u0000\u033c\u033d"+
		"\u0001\u0000\u0000\u0000\u033d\u0091\u0001\u0000\u0000\u0000\u033e\u033c"+
		"\u0001\u0000\u0000\u0000\u033f\u0340\u0007\u0005\u0000\u0000\u0340\u0341"+
		"\u0003\u0094J\u0000\u0341\u0093\u0001\u0000\u0000\u0000\u0342\u0346\u0003"+
		"\u0098L\u0000\u0343\u0345\u0003\u0096K\u0000\u0344\u0343\u0001\u0000\u0000"+
		"\u0000\u0345\u0348\u0001\u0000\u0000\u0000\u0346\u0344\u0001\u0000\u0000"+
		"\u0000\u0346\u0347\u0001\u0000\u0000\u0000\u0347\u0095\u0001\u0000\u0000"+
		"\u0000\u0348\u0346\u0001\u0000\u0000\u0000\u0349\u034a\u0007\u0006\u0000"+
		"\u0000\u034a\u034b\u0003\u0098L\u0000\u034b\u0097\u0001\u0000\u0000\u0000"+
		"\u034c\u0351\u0003\u009aM\u0000\u034d\u034e\u00051\u0000\u0000\u034e\u0350"+
		"\u0003\u00b6[\u0000\u034f\u034d\u0001\u0000\u0000\u0000\u0350\u0353\u0001"+
		"\u0000\u0000\u0000\u0351\u034f\u0001\u0000\u0000\u0000\u0351\u0352\u0001"+
		"\u0000\u0000\u0000\u0352\u0099\u0001\u0000\u0000\u0000\u0353\u0351\u0001"+
		"\u0000\u0000\u0000\u0354\u0356\u0003\u009cN\u0000\u0355\u0354\u0001\u0000"+
		"\u0000\u0000\u0356\u0359\u0001\u0000\u0000\u0000\u0357\u0355\u0001\u0000"+
		"\u0000\u0000\u0357\u0358\u0001\u0000\u0000\u0000\u0358\u035a\u0001\u0000"+
		"\u0000\u0000\u0359\u0357\u0001\u0000\u0000\u0000\u035a\u035b\u0003\u009e"+
		"O\u0000\u035b\u009b\u0001\u0000\u0000\u0000\u035c\u035d\u0007\u0007\u0000"+
		"\u0000\u035d\u009d\u0001\u0000\u0000\u0000\u035e\u0362\u0003\u00a8T\u0000"+
		"\u035f\u0361\u0003\u00a0P\u0000\u0360\u035f\u0001\u0000\u0000\u0000\u0361"+
		"\u0364\u0001\u0000\u0000\u0000\u0362\u0360\u0001\u0000\u0000\u0000\u0362"+
		"\u0363\u0001\u0000\u0000\u0000\u0363\u009f\u0001\u0000\u0000\u0000\u0364"+
		"\u0362\u0001\u0000\u0000\u0000\u0365\u036d\u0007\b\u0000\u0000\u0366\u036d"+
		"\u0003\u00a2Q\u0000\u0367\u0368\u0005b\u0000\u0000\u0368\u036d\u0003R"+
		")\u0000\u0369\u036d\u0003\u00a4R\u0000\u036a\u036d\u0003\u00a6S\u0000"+
		"\u036b\u036d\u0003\u00ceg\u0000\u036c\u0365\u0001\u0000\u0000\u0000\u036c"+
		"\u0366\u0001\u0000\u0000\u0000\u036c\u0367\u0001\u0000\u0000\u0000\u036c"+
		"\u0369\u0001\u0000\u0000\u0000\u036c\u036a\u0001\u0000\u0000\u0000\u036c"+
		"\u036b\u0001\u0000\u0000\u0000\u036d\u00a1\u0001\u0000\u0000\u0000\u036e"+
		"\u036f\u0003\u00aaU\u0000\u036f\u00a3\u0001\u0000\u0000\u0000\u0370\u0371"+
		"\u0005^\u0000\u0000\u0371\u0372\u0003j5\u0000\u0372\u0373\u0005_\u0000"+
		"\u0000\u0373\u00a5\u0001\u0000\u0000\u0000\u0374\u0377\u0005b\u0000\u0000"+
		"\u0375\u0378\u0003\u00acV\u0000\u0376\u0378\u0005#\u0000\u0000\u0377\u0375"+
		"\u0001\u0000\u0000\u0000\u0377\u0376\u0001\u0000\u0000\u0000\u0378\u00a7"+
		"\u0001\u0000\u0000\u0000\u0379\u037a\u0005Z\u0000\u0000\u037a\u037b\u0003"+
		"j5\u0000\u037b\u037c\u0005[\u0000\u0000\u037c\u0385\u0001\u0000\u0000"+
		"\u0000\u037d\u0385\u0005#\u0000\u0000\u037e\u0385\u0005!\u0000\u0000\u037f"+
		"\u0385\u0003R)\u0000\u0380\u0385\u0003T*\u0000\u0381\u0385\u0003\u00ae"+
		"W\u0000\u0382\u0385\u0003\u00acV\u0000\u0383\u0385\u0003\u00d4j\u0000"+
		"\u0384\u0379\u0001\u0000\u0000\u0000\u0384\u037d\u0001\u0000\u0000\u0000"+
		"\u0384\u037e\u0001\u0000\u0000\u0000\u0384\u037f\u0001\u0000\u0000\u0000"+
		"\u0384\u0380\u0001\u0000\u0000\u0000\u0384\u0381\u0001\u0000\u0000\u0000"+
		"\u0384\u0382\u0001\u0000\u0000\u0000\u0384\u0383\u0001\u0000\u0000\u0000"+
		"\u0385\u00a9\u0001\u0000\u0000\u0000\u0386\u0388\u0005Z\u0000\u0000\u0387"+
		"\u0389\u0003h4\u0000\u0388\u0387\u0001\u0000\u0000\u0000\u0388\u0389\u0001"+
		"\u0000\u0000\u0000\u0389\u038a\u0001\u0000\u0000\u0000\u038a\u038b\u0005"+
		"[\u0000\u0000\u038b\u00ab\u0001\u0000\u0000\u0000\u038c\u038d\u0007\t"+
		"\u0000\u0000\u038d\u00ad\u0001\u0000\u0000\u0000\u038e\u0396\u0003\u00b0"+
		"X\u0000\u038f\u0396\u0003\u00b2Y\u0000\u0390\u0396\u0005n\u0000\u0000"+
		"\u0391\u0396\u0005o\u0000\u0000\u0392\u0396\u0005m\u0000\u0000\u0393\u0396"+
		"\u0005\t\u0000\u0000\u0394\u0396\u0005p\u0000\u0000\u0395\u038e\u0001"+
		"\u0000\u0000\u0000\u0395\u038f\u0001\u0000\u0000\u0000\u0395\u0390\u0001"+
		"\u0000\u0000\u0000\u0395\u0391\u0001\u0000\u0000\u0000\u0395\u0392\u0001"+
		"\u0000\u0000\u0000\u0395\u0393\u0001\u0000\u0000\u0000\u0395\u0394\u0001"+
		"\u0000\u0000\u0000\u0396\u00af\u0001\u0000\u0000\u0000\u0397\u0398\u0007"+
		"\n\u0000\u0000\u0398\u00b1\u0001\u0000\u0000\u0000\u0399\u039a\u0007\u000b"+
		"\u0000\u0000\u039a\u00b3\u0001\u0000\u0000\u0000\u039b\u039e\u0005\'\u0000"+
		"\u0000\u039c\u039e\u0003\u00b6[\u0000\u039d\u039b\u0001\u0000\u0000\u0000"+
		"\u039d\u039c\u0001\u0000\u0000\u0000\u039e\u00b5\u0001\u0000\u0000\u0000"+
		"\u039f\u03a0\u0003\u00b8\\\u0000\u03a0\u00b7\u0001\u0000\u0000\u0000\u03a1"+
		"\u03a6\u0003\u00ba]\u0000\u03a2\u03a3\u0005L\u0000\u0000\u03a3\u03a5\u0003"+
		"\u00ba]\u0000\u03a4\u03a2\u0001\u0000\u0000\u0000\u03a5\u03a8\u0001\u0000"+
		"\u0000\u0000\u03a6\u03a4\u0001\u0000\u0000\u0000\u03a6\u03a7\u0001\u0000"+
		"\u0000\u0000\u03a7\u00b9\u0001\u0000\u0000\u0000\u03a8\u03a6\u0001\u0000"+
		"\u0000\u0000\u03a9\u03ae\u0003\u00bc^\u0000\u03aa\u03ab\u0005K\u0000\u0000"+
		"\u03ab\u03ad\u0003\u00bc^\u0000\u03ac\u03aa\u0001\u0000\u0000\u0000\u03ad"+
		"\u03b0\u0001\u0000\u0000\u0000\u03ae\u03ac\u0001\u0000\u0000\u0000\u03ae"+
		"\u03af\u0001\u0000\u0000\u0000\u03af\u00bb\u0001\u0000\u0000\u0000\u03b0"+
		"\u03ae\u0001\u0000\u0000\u0000\u03b1\u03b5\u0003\u00c0`\u0000\u03b2\u03b4"+
		"\u0003\u00be_\u0000\u03b3\u03b2\u0001\u0000\u0000\u0000\u03b4\u03b7\u0001"+
		"\u0000\u0000\u0000\u03b5\u03b3\u0001\u0000\u0000\u0000\u03b5\u03b6\u0001"+
		"\u0000\u0000\u0000\u03b6\u00bd\u0001\u0000\u0000\u0000\u03b7\u03b5\u0001"+
		"\u0000\u0000\u0000\u03b8\u03bb\u0003\u00c8d\u0000\u03b9\u03bb\u0005=\u0000"+
		"\u0000\u03ba\u03b8\u0001\u0000\u0000\u0000\u03ba\u03b9\u0001\u0000\u0000"+
		"\u0000\u03bb\u00bf\u0001\u0000\u0000\u0000\u03bc\u03c1\u0003\u00cae\u0000"+
		"\u03bd\u03c1\u0003\u00c2a\u0000\u03be\u03c1\u0003\u00c4b\u0000\u03bf\u03c1"+
		"\u0003\u00c6c\u0000\u03c0\u03bc\u0001\u0000\u0000\u0000\u03c0\u03bd\u0001"+
		"\u0000\u0000\u0000\u03c0\u03be\u0001\u0000\u0000\u0000\u03c0\u03bf\u0001"+
		"\u0000\u0000\u0000\u03c1\u00c1\u0001\u0000\u0000\u0000\u03c2\u03c3\u0007"+
		"\f\u0000\u0000\u03c3\u00c3\u0001\u0000\u0000\u0000\u03c4\u03cd\u0005Z"+
		"\u0000\u0000\u03c5\u03ca\u0003\u00b6[\u0000\u03c6\u03c7\u0005a\u0000\u0000"+
		"\u03c7\u03c9\u0003\u00b6[\u0000\u03c8\u03c6\u0001\u0000\u0000\u0000\u03c9"+
		"\u03cc\u0001\u0000\u0000\u0000\u03ca\u03c8\u0001\u0000\u0000\u0000\u03ca"+
		"\u03cb\u0001\u0000\u0000\u0000\u03cb\u03ce\u0001\u0000\u0000\u0000\u03cc"+
		"\u03ca\u0001\u0000\u0000\u0000\u03cd\u03c5\u0001\u0000\u0000\u0000\u03cd"+
		"\u03ce\u0001\u0000\u0000\u0000\u03ce\u03cf\u0001\u0000\u0000\u0000\u03cf"+
		"\u03d0\u0005[\u0000\u0000\u03d0\u03d1\u0005c\u0000\u0000\u03d1\u03d2\u0003"+
		"\u00b6[\u0000\u03d2\u00c5\u0001\u0000\u0000\u0000\u03d3\u03d4\u0005^\u0000"+
		"\u0000\u03d4\u03d5\u0003\u00b6[\u0000\u03d5\u03d6\u0005a\u0000\u0000\u03d6"+
		"\u03d7\u0003\u00b6[\u0000\u03d7\u03d8\u0005_\u0000\u0000\u03d8\u00c7\u0001"+
		"\u0000\u0000\u0000\u03d9\u03da\u0007\r\u0000\u0000\u03da\u00c9\u0001\u0000"+
		"\u0000\u0000\u03db\u03e0\u0003\u00ccf\u0000\u03dc\u03dd\u0005b\u0000\u0000"+
		"\u03dd\u03df\u0003\u00ccf\u0000\u03de\u03dc\u0001\u0000\u0000\u0000\u03df"+
		"\u03e2\u0001\u0000\u0000\u0000\u03e0\u03de\u0001\u0000\u0000\u0000\u03e0"+
		"\u03e1\u0001\u0000\u0000\u0000\u03e1\u00cb\u0001\u0000\u0000\u0000\u03e2"+
		"\u03e0\u0001\u0000\u0000\u0000\u03e3\u03e5\u0003\u00acV\u0000\u03e4\u03e6"+
		"\u0003\u00ceg\u0000\u03e5\u03e4\u0001\u0000\u0000\u0000\u03e5\u03e6\u0001"+
		"\u0000\u0000\u0000\u03e6\u00cd\u0001\u0000\u0000\u0000\u03e7\u03e8\u0005"+
		"9\u0000\u0000\u03e8\u03ed\u0003\u00b6[\u0000\u03e9\u03ea\u0005a\u0000"+
		"\u0000\u03ea\u03ec\u0003\u00b6[\u0000\u03eb\u03e9\u0001\u0000\u0000\u0000"+
		"\u03ec\u03ef\u0001\u0000\u0000\u0000\u03ed\u03eb\u0001\u0000\u0000\u0000"+
		"\u03ed\u03ee\u0001\u0000\u0000\u0000\u03ee\u03f0\u0001\u0000\u0000\u0000"+
		"\u03ef\u03ed\u0001\u0000\u0000\u0000\u03f0\u03f1\u00058\u0000\u0000\u03f1"+
		"\u00cf\u0001\u0000\u0000\u0000\u03f2\u03f6\u0003\u00d2i\u0000\u03f3\u03f6"+
		"\u0005\u001a\u0000\u0000\u03f4\u03f6\u0005,\u0000\u0000\u03f5\u03f2\u0001"+
		"\u0000\u0000\u0000\u03f5\u03f3\u0001\u0000\u0000\u0000\u03f5\u03f4\u0001"+
		"\u0000\u0000\u0000\u03f6\u00d1\u0001\u0000\u0000\u0000\u03f7\u03f8\u0007"+
		"\u000e\u0000\u0000\u03f8\u00d3\u0001\u0000\u0000\u0000\u03f9\u03fa\u0003"+
		"\u00d6k\u0000\u03fa\u0400\u0005c\u0000\u0000\u03fb\u03fd\u0003\u00b4Z"+
		"\u0000\u03fc\u03fb\u0001\u0000\u0000\u0000\u03fc\u03fd\u0001\u0000\u0000"+
		"\u0000\u03fd\u03fe\u0001\u0000\u0000\u0000\u03fe\u0401\u0003\u00dcn\u0000"+
		"\u03ff\u0401\u0003j5\u0000\u0400\u03fc\u0001\u0000\u0000\u0000\u0400\u03ff"+
		"\u0001\u0000\u0000\u0000\u0401\u00d5\u0001\u0000\u0000\u0000\u0402\u0404"+
		"\u0005Z\u0000\u0000\u0403\u0405\u0003\u00d8l\u0000\u0404\u0403\u0001\u0000"+
		"\u0000\u0000\u0404\u0405\u0001\u0000\u0000\u0000\u0405\u0406\u0001\u0000"+
		"\u0000\u0000\u0406\u0409\u0005[\u0000\u0000\u0407\u0409\u0003\u00acV\u0000"+
		"\u0408\u0402\u0001\u0000\u0000\u0000\u0408\u0407\u0001\u0000\u0000\u0000"+
		"\u0409\u00d7\u0001\u0000\u0000\u0000\u040a\u040f\u0003\u00dam\u0000\u040b"+
		"\u040c\u0005a\u0000\u0000\u040c\u040e\u0003\u00dam\u0000\u040d\u040b\u0001"+
		"\u0000\u0000\u0000\u040e\u0411\u0001\u0000\u0000\u0000\u040f\u040d\u0001"+
		"\u0000\u0000\u0000\u040f\u0410\u0001\u0000\u0000\u0000\u0410\u00d9\u0001"+
		"\u0000\u0000\u0000\u0411\u040f\u0001\u0000\u0000\u0000\u0412\u0415\u0003"+
		"\u00acV\u0000\u0413\u0414\u0005>\u0000\u0000\u0414\u0416\u0003\u00b6["+
		"\u0000\u0415\u0413\u0001\u0000\u0000\u0000\u0415\u0416\u0001\u0000\u0000"+
		"\u0000\u0416\u00db\u0001\u0000\u0000\u0000\u0417\u0418\u0003@ \u0000\u0418"+
		"\u00dd\u0001\u0000\u0000\u0000\u0419\u041a\u0005e\u0000\u0000\u041a\u0421"+
		"\u0003\u00acV\u0000\u041b\u041e\u0005Z\u0000\u0000\u041c\u041f\u0003\u00e0"+
		"p\u0000\u041d\u041f\u0003j5\u0000\u041e\u041c\u0001\u0000\u0000\u0000"+
		"\u041e\u041d\u0001\u0000\u0000\u0000\u041e\u041f\u0001\u0000\u0000\u0000"+
		"\u041f\u0420\u0001\u0000\u0000\u0000\u0420\u0422\u0005[\u0000\u0000\u0421"+
		"\u041b\u0001\u0000\u0000\u0000\u0421\u0422\u0001\u0000\u0000\u0000\u0422"+
		"\u00df\u0001\u0000\u0000\u0000\u0423\u0428\u0003\u00e2q\u0000\u0424\u0425"+
		"\u0005a\u0000\u0000\u0425\u0427\u0003\u00e2q\u0000\u0426\u0424\u0001\u0000"+
		"\u0000\u0000\u0427\u042a\u0001\u0000\u0000\u0000\u0428\u0426\u0001\u0000"+
		"\u0000\u0000\u0428\u0429\u0001\u0000\u0000\u0000\u0429\u00e1\u0001\u0000"+
		"\u0000\u0000\u042a\u0428\u0001\u0000\u0000\u0000\u042b\u042c\u0003\u00ac"+
		"V\u0000\u042c\u042d\u00057\u0000\u0000\u042d\u042e\u0003j5\u0000\u042e"+
		"\u00e3\u0001\u0000\u0000\u0000n\u00e5\u00ea\u00f0\u00fb\u0103\u0108\u010e"+
		"\u0112\u011a\u0124\u012a\u012f\u0137\u013e\u0142\u0145\u0148\u0151\u0156"+
		"\u0159\u015f\u0165\u016b\u016f\u0177\u017f\u0189\u018f\u0194\u0198\u01a0"+
		"\u01a6\u01aa\u01af\u01b4\u01b7\u01bd\u01c7\u01d1\u01d9\u01df\u01e3\u01ec"+
		"\u01f9\u0212\u0219\u0220\u0226\u022c\u0230\u023a\u0240\u0244\u024d\u0254"+
		"\u0261\u0271\u0279\u027d\u027f\u0285\u0294\u029f\u02b4\u02bf\u02c8\u02d4"+
		"\u02db\u02e3\u02e9\u02f0\u02f8\u0300\u0307\u0311\u031b\u0321\u032a\u0334"+
		"\u033c\u0346\u0351\u0357\u0362\u036c\u0377\u0384\u0388\u0395\u039d\u03a6"+
		"\u03ae\u03b5\u03ba\u03c0\u03ca\u03cd\u03e0\u03e5\u03ed\u03f5\u03fc\u0400"+
		"\u0404\u0408\u040f\u0415\u041e\u0421\u0428";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}