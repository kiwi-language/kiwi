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
		ABSTRACT=1, BOOLEAN=2, CASE=3, DEFAULT=4, CATCH=5, STRING=6, CLASS=7, 
		TIME=8, NULL=9, PACKAGE=10, IMPORT=11, PASSWORD=12, DOUBLE=13, ELSE=14, 
		ENUM=15, FINALLY=16, FOR=17, IF=18, IS=19, BYTE=20, SHORT=21, INT=22, 
		LONG=23, CHAR=24, INTERFACE=25, NATIVE=26, NEW=27, PRIVATE=28, PROTECTED=29, 
		PUBLIC=30, RETURN=31, STATIC=32, SUPER=33, SWITCH=34, THIS=35, THROW=36, 
		THROWS=37, TRY=38, VOID=39, WHILE=40, ANY=41, NEVER=42, DELETED=43, FUNC=44, 
		VALUE=45, VAL=46, VAR=47, AS=48, INIT=49, ASSIGN=50, GT=51, LT=52, BANG=53, 
		BANGBANG=54, TILDE=55, QUESTION=56, COLON=57, EQUAL=58, LE=59, GE=60, 
		NOTEQUAL=61, AND=62, OR=63, INC=64, DEC=65, ADD=66, SUB=67, MUL=68, DIV=69, 
		BITAND=70, BITOR=71, CARET=72, MOD=73, ADD_ASSIGN=74, SUB_ASSIGN=75, MUL_ASSIGN=76, 
		DIV_ASSIGN=77, AND_ASSIGN=78, OR_ASSIGN=79, XOR_ASSIGN=80, MOD_ASSIGN=81, 
		LSHIFT_ASSIGN=82, RSHIFT_ASSIGN=83, URSHIFT_ASSIGN=84, LPAREN=85, RPAREN=86, 
		LBRACE=87, RBRACE=88, LBRACK=89, RBRACK=90, SEMI=91, COMMA=92, DOT=93, 
		ARROW=94, COLONCOLON=95, AT=96, ELLIPSIS=97, DECIMAL_LITERAL=98, HEX_LITERAL=99, 
		OCT_LITERAL=100, BINARY_LITERAL=101, FLOAT_LITERAL=102, HEX_FLOAT_LITERAL=103, 
		BOOL_LITERAL=104, CHAR_LITERAL=105, STRING_LITERAL=106, TEXT_BLOCK=107, 
		R=108, RW=109, IDENTIFIER=110, WS=111, COMMENT=112, LINE_COMMENT=113;
	public static final int
		RULE_compilationUnit = 0, RULE_packageDeclaration = 1, RULE_importDeclaration = 2, 
		RULE_typeDeclaration = 3, RULE_classDeclaration = 4, RULE_classBody = 5, 
		RULE_typeList = 6, RULE_classBodyDeclaration = 7, RULE_staticBlock = 8, 
		RULE_enumDeclaration = 9, RULE_enumConstants = 10, RULE_enumConstant = 11, 
		RULE_enumBodyDeclarations = 12, RULE_interfaceDeclaration = 13, RULE_interfaceBody = 14, 
		RULE_interfaceBodyDeclaration = 15, RULE_interfaceMemberDeclaration = 16, 
		RULE_interfaceMethodDeclaration = 17, RULE_interfaceMethodModifier = 18, 
		RULE_memberDeclaration = 19, RULE_fieldDeclaration = 20, RULE_methodDeclaration = 21, 
		RULE_constructorDeclaration = 22, RULE_typeParameters = 23, RULE_qualifiedNameList = 24, 
		RULE_qualifiedName = 25, RULE_typeParameter = 26, RULE_formalParameters = 27, 
		RULE_formalParameterList = 28, RULE_formalParameter = 29, RULE_methodBody = 30, 
		RULE_block = 31, RULE_statement = 32, RULE_localVariableDeclaration = 33, 
		RULE_forControl = 34, RULE_loopVariableDeclarators = 35, RULE_loopVariableDeclarator = 36, 
		RULE_loopVariableUpdates = 37, RULE_loopVariableUpdate = 38, RULE_newExpr = 39, 
		RULE_newArray = 40, RULE_arrayInitializer = 41, RULE_variableInitializer = 42, 
		RULE_catchClause = 43, RULE_catchFields = 44, RULE_catchField = 45, RULE_catchValue = 46, 
		RULE_branchCase = 47, RULE_switchLabel = 48, RULE_parExpression = 49, 
		RULE_expressionList = 50, RULE_expression = 51, RULE_assignment = 52, 
		RULE_assignmentSuffix = 53, RULE_ternary = 54, RULE_disjunction = 55, 
		RULE_conjunction = 56, RULE_bitor = 57, RULE_bitand = 58, RULE_bitxor = 59, 
		RULE_equality = 60, RULE_equalitySuffix = 61, RULE_relational = 62, RULE_relationalSuffix = 63, 
		RULE_isExpr = 64, RULE_shift = 65, RULE_shiftSuffix = 66, RULE_additive = 67, 
		RULE_additiveSuffix = 68, RULE_multiplicative = 69, RULE_multiplicativeSuffix = 70, 
		RULE_asExpr = 71, RULE_prefixExpr = 72, RULE_prefixOp = 73, RULE_postfixExpr = 74, 
		RULE_postfixSuffix = 75, RULE_callSuffix = 76, RULE_indexingSuffix = 77, 
		RULE_selectorSuffix = 78, RULE_primaryExpr = 79, RULE_arguments = 80, 
		RULE_identifier = 81, RULE_methodCall = 82, RULE_literal = 83, RULE_integerLiteral = 84, 
		RULE_floatLiteral = 85, RULE_typeOrVoid = 86, RULE_type = 87, RULE_arrayKind = 88, 
		RULE_classOrInterfaceType = 89, RULE_typeArguments = 90, RULE_primitiveType = 91, 
		RULE_modifier = 92, RULE_classOrInterfaceModifier = 93, RULE_lambdaExpression = 94, 
		RULE_lambdaParameters = 95, RULE_lambdaBody = 96, RULE_annotation = 97, 
		RULE_elementValuePairs = 98, RULE_elementValuePair = 99;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "packageDeclaration", "importDeclaration", "typeDeclaration", 
			"classDeclaration", "classBody", "typeList", "classBodyDeclaration", 
			"staticBlock", "enumDeclaration", "enumConstants", "enumConstant", "enumBodyDeclarations", 
			"interfaceDeclaration", "interfaceBody", "interfaceBodyDeclaration", 
			"interfaceMemberDeclaration", "interfaceMethodDeclaration", "interfaceMethodModifier", 
			"memberDeclaration", "fieldDeclaration", "methodDeclaration", "constructorDeclaration", 
			"typeParameters", "qualifiedNameList", "qualifiedName", "typeParameter", 
			"formalParameters", "formalParameterList", "formalParameter", "methodBody", 
			"block", "statement", "localVariableDeclaration", "forControl", "loopVariableDeclarators", 
			"loopVariableDeclarator", "loopVariableUpdates", "loopVariableUpdate", 
			"newExpr", "newArray", "arrayInitializer", "variableInitializer", "catchClause", 
			"catchFields", "catchField", "catchValue", "branchCase", "switchLabel", 
			"parExpression", "expressionList", "expression", "assignment", "assignmentSuffix", 
			"ternary", "disjunction", "conjunction", "bitor", "bitand", "bitxor", 
			"equality", "equalitySuffix", "relational", "relationalSuffix", "isExpr", 
			"shift", "shiftSuffix", "additive", "additiveSuffix", "multiplicative", 
			"multiplicativeSuffix", "asExpr", "prefixExpr", "prefixOp", "postfixExpr", 
			"postfixSuffix", "callSuffix", "indexingSuffix", "selectorSuffix", "primaryExpr", 
			"arguments", "identifier", "methodCall", "literal", "integerLiteral", 
			"floatLiteral", "typeOrVoid", "type", "arrayKind", "classOrInterfaceType", 
			"typeArguments", "primitiveType", "modifier", "classOrInterfaceModifier", 
			"lambdaExpression", "lambdaParameters", "lambdaBody", "annotation", "elementValuePairs", 
			"elementValuePair"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'boolean'", "'case'", "'default'", "'catch'", "'string'", 
			"'class'", "'time'", "'null'", "'package'", "'import'", "'password'", 
			"'double'", "'else'", "'enum'", "'finally'", "'for'", "'if'", "'is'", 
			"'byte'", "'short'", "'int'", "'long'", "'char'", "'interface'", "'native'", 
			"'new'", "'private'", "'protected'", "'public'", "'return'", "'static'", 
			"'super'", "'switch'", "'this'", "'throw'", "'throws'", "'try'", "'void'", 
			"'while'", "'any'", "'never'", "'deleted'", "'func'", "'value'", "'val'", 
			"'var'", "'as'", "'init'", "'='", "'>'", "'<'", "'!'", "'!!'", "'~'", 
			"'?'", "':'", "'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", "'++'", 
			"'--'", "'+'", "'-'", "'*'", "'/'", "'&'", "'|'", "'^'", "'%'", "'+='", 
			"'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", "'>>='", 
			"'>>>='", "'('", "')'", "'{'", "'}'", "'['", "']'", "';'", "','", "'.'", 
			"'->'", "'::'", "'@'", "'...'", null, null, null, null, null, null, null, 
			null, null, null, "'[r]'", "'[]'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSTRACT", "BOOLEAN", "CASE", "DEFAULT", "CATCH", "STRING", "CLASS", 
			"TIME", "NULL", "PACKAGE", "IMPORT", "PASSWORD", "DOUBLE", "ELSE", "ENUM", 
			"FINALLY", "FOR", "IF", "IS", "BYTE", "SHORT", "INT", "LONG", "CHAR", 
			"INTERFACE", "NATIVE", "NEW", "PRIVATE", "PROTECTED", "PUBLIC", "RETURN", 
			"STATIC", "SUPER", "SWITCH", "THIS", "THROW", "THROWS", "TRY", "VOID", 
			"WHILE", "ANY", "NEVER", "DELETED", "FUNC", "VALUE", "VAL", "VAR", "AS", 
			"INIT", "ASSIGN", "GT", "LT", "BANG", "BANGBANG", "TILDE", "QUESTION", 
			"COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", 
			"ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", "MOD", "ADD_ASSIGN", 
			"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", 
			"XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", 
			"LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", 
			"DOT", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "DECIMAL_LITERAL", "HEX_LITERAL", 
			"OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", 
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
		public List<TypeDeclarationContext> typeDeclaration() {
			return getRuleContexts(TypeDeclarationContext.class);
		}
		public TypeDeclarationContext typeDeclaration(int i) {
			return getRuleContext(TypeDeclarationContext.class,i);
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
			setState(201);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PACKAGE) {
				{
				setState(200);
				packageDeclaration();
				}
			}

			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(203);
				importDeclaration();
				}
				}
				setState(208);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(210); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(209);
				typeDeclaration();
				}
				}
				setState(212); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 35190579691650L) != 0) || _la==AT );
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
			setState(214);
			match(PACKAGE);
			setState(215);
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
			setState(217);
			match(IMPORT);
			setState(218);
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
		public List<ClassOrInterfaceModifierContext> classOrInterfaceModifier() {
			return getRuleContexts(ClassOrInterfaceModifierContext.class);
		}
		public ClassOrInterfaceModifierContext classOrInterfaceModifier(int i) {
			return getRuleContext(ClassOrInterfaceModifierContext.class,i);
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
		enterRule(_localctx, 6, RULE_typeDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 35190546104322L) != 0)) {
				{
				{
				setState(220);
				classOrInterfaceModifier();
				}
				}
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(229);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(226);
				classDeclaration();
				}
				break;
			case 2:
				{
				setState(227);
				enumDeclaration();
				}
				break;
			case 3:
				{
				setState(228);
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
		enterRule(_localctx, 8, RULE_classDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(231);
				annotation();
				}
				}
				setState(236);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(237);
			match(CLASS);
			setState(238);
			identifier();
			setState(240);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(239);
				typeParameters();
				}
			}

			setState(244);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(242);
				match(COLON);
				setState(243);
				typeList();
				}
			}

			setState(246);
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
		enterRule(_localctx, 10, RULE_classBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			match(LBRACE);
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 835635078234242L) != 0) || _la==AT) {
				{
				{
				setState(249);
				classBodyDeclaration();
				}
				}
				setState(254);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(255);
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
		enterRule(_localctx, 12, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
			type(0);
			setState(262);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(258);
				match(COMMA);
				setState(259);
				type(0);
				}
				}
				setState(264);
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
		enterRule(_localctx, 14, RULE_classBodyDeclaration);
		int _la;
		try {
			setState(273);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(268);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 43986706235394L) != 0)) {
					{
					{
					setState(265);
					modifier();
					}
					}
					setState(270);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(271);
				memberDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(272);
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
		enterRule(_localctx, 16, RULE_staticBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(275);
			match(STATIC);
			setState(276);
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
		enterRule(_localctx, 18, RULE_enumDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(278);
				annotation();
				}
				}
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(284);
			match(ENUM);
			setState(285);
			identifier();
			setState(288);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(286);
				match(COLON);
				setState(287);
				typeList();
				}
			}

			setState(290);
			match(LBRACE);
			setState(292);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUE || _la==IDENTIFIER) {
				{
				setState(291);
				enumConstants();
				}
			}

			setState(295);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(294);
				match(COMMA);
				}
			}

			setState(298);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(297);
				enumBodyDeclarations();
				}
			}

			setState(300);
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
		enterRule(_localctx, 20, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(302);
			enumConstant();
			setState(307);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(303);
					match(COMMA);
					setState(304);
					enumConstant();
					}
					} 
				}
				setState(309);
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
		enterRule(_localctx, 22, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(310);
			identifier();
			setState(312);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(311);
				arguments();
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
		enterRule(_localctx, 24, RULE_enumBodyDeclarations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(314);
			match(SEMI);
			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 835635078234242L) != 0) || _la==AT) {
				{
				{
				setState(315);
				classBodyDeclaration();
				}
				}
				setState(320);
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
		enterRule(_localctx, 26, RULE_interfaceDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(321);
				annotation();
				}
				}
				setState(326);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(327);
			match(INTERFACE);
			setState(328);
			identifier();
			setState(330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(329);
				typeParameters();
				}
			}

			setState(334);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(332);
				match(COLON);
				setState(333);
				typeList();
				}
			}

			setState(336);
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
		enterRule(_localctx, 28, RULE_interfaceBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			match(LBRACE);
			setState(342);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 61578892279826L) != 0)) {
				{
				{
				setState(339);
				interfaceBodyDeclaration();
				}
				}
				setState(344);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(345);
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
		enterRule(_localctx, 30, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(350);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(347);
					modifier();
					}
					} 
				}
				setState(352);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			setState(353);
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
		enterRule(_localctx, 32, RULE_interfaceMemberDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(355);
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
		public TerminalNode FUNC() { return getToken(KiwiParser.FUNC, 0); }
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
		enterRule(_localctx, 34, RULE_interfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(360);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 5368709138L) != 0)) {
				{
				{
				setState(357);
				interfaceMethodModifier();
				}
				}
				setState(362);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(363);
			match(FUNC);
			setState(364);
			identifier();
			setState(366);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(365);
				typeParameters();
				}
			}

			setState(368);
			formalParameters();
			setState(371);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(369);
				match(ARROW);
				setState(370);
				typeOrVoid();
				}
			}

			setState(375);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(373);
				match(THROWS);
				setState(374);
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
		public TerminalNode PUBLIC() { return getToken(KiwiParser.PUBLIC, 0); }
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
		enterRule(_localctx, 36, RULE_interfaceMethodModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
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
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
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
		enterRule(_localctx, 38, RULE_memberDeclaration);
		try {
			setState(383);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FUNC:
				enterOuterAlt(_localctx, 1);
				{
				setState(379);
				methodDeclaration();
				}
				break;
			case VAL:
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(380);
				fieldDeclaration();
				}
				break;
			case INIT:
				enterOuterAlt(_localctx, 3);
				{
				setState(381);
				constructorDeclaration();
				}
				break;
			case CLASS:
			case AT:
				enterOuterAlt(_localctx, 4);
				{
				setState(382);
				classDeclaration();
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
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode VAR() { return getToken(KiwiParser.VAR, 0); }
		public TerminalNode VAL() { return getToken(KiwiParser.VAL, 0); }
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
		enterRule(_localctx, 40, RULE_fieldDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(385);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(386);
			identifier();
			setState(387);
			match(COLON);
			setState(388);
			type(0);
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
		public TerminalNode FUNC() { return getToken(KiwiParser.FUNC, 0); }
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
		enterRule(_localctx, 42, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(390);
			match(FUNC);
			setState(391);
			identifier();
			setState(393);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(392);
				typeParameters();
				}
			}

			setState(395);
			formalParameters();
			setState(398);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(396);
				match(ARROW);
				setState(397);
				typeOrVoid();
				}
			}

			setState(401);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBRACE) {
				{
				setState(400);
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
		enterRule(_localctx, 44, RULE_constructorDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			match(INIT);
			setState(404);
			formalParameters();
			setState(407);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(405);
				match(THROWS);
				setState(406);
				qualifiedNameList();
				}
			}

			setState(409);
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
		enterRule(_localctx, 46, RULE_typeParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(411);
			match(LT);
			setState(412);
			typeParameter();
			setState(417);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(413);
				match(COMMA);
				setState(414);
				typeParameter();
				}
				}
				setState(419);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(420);
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
		enterRule(_localctx, 48, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(422);
			qualifiedName();
			setState(427);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(423);
				match(COMMA);
				setState(424);
				qualifiedName();
				}
				}
				setState(429);
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
		enterRule(_localctx, 50, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			identifier();
			setState(435);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(431);
					match(DOT);
					setState(432);
					identifier();
					}
					} 
				}
				setState(437);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
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
		enterRule(_localctx, 52, RULE_typeParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			identifier();
			setState(441);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(439);
				match(COLON);
				setState(440);
				type(0);
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
		enterRule(_localctx, 54, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
			match(LPAREN);
			setState(445);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUE || _la==IDENTIFIER) {
				{
				setState(444);
				formalParameterList();
				}
			}

			setState(447);
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
		enterRule(_localctx, 56, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
			formalParameter();
			setState(454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(450);
				match(COMMA);
				setState(451);
				formalParameter();
				}
				}
				setState(456);
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
		enterRule(_localctx, 58, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(457);
			identifier();
			setState(458);
			match(COLON);
			setState(459);
			type(0);
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
		enterRule(_localctx, 60, RULE_methodBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(461);
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
		enterRule(_localctx, 62, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
			match(LBRACE);
			setState(467);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 45283792398975488L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 87943886667791L) != 0)) {
				{
				{
				setState(464);
				statement();
				}
				}
				setState(469);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(470);
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
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public TerminalNode FOR() { return getToken(KiwiParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public ForControlContext forControl() {
			return getRuleContext(ForControlContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode IF() { return getToken(KiwiParser.IF, 0); }
		public TerminalNode ELSE() { return getToken(KiwiParser.ELSE, 0); }
		public TerminalNode TRY() { return getToken(KiwiParser.TRY, 0); }
		public CatchClauseContext catchClause() {
			return getRuleContext(CatchClauseContext.class,0);
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
		enterRule(_localctx, 64, RULE_statement);
		int _la;
		try {
			setState(511);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case WHILE:
				enterOuterAlt(_localctx, 1);
				{
				setState(472);
				match(WHILE);
				setState(473);
				parExpression();
				setState(474);
				block();
				}
				break;
			case FOR:
				enterOuterAlt(_localctx, 2);
				{
				setState(476);
				match(FOR);
				setState(477);
				match(LPAREN);
				setState(478);
				forControl();
				setState(479);
				match(RPAREN);
				setState(480);
				block();
				}
				break;
			case IF:
				enterOuterAlt(_localctx, 3);
				{
				setState(482);
				match(IF);
				setState(483);
				parExpression();
				setState(484);
				block();
				setState(487);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(485);
					match(ELSE);
					setState(486);
					block();
					}
				}

				}
				break;
			case TRY:
				enterOuterAlt(_localctx, 4);
				{
				setState(489);
				match(TRY);
				setState(490);
				block();
				setState(491);
				catchClause();
				}
				break;
			case SWITCH:
				enterOuterAlt(_localctx, 5);
				{
				setState(493);
				match(SWITCH);
				setState(494);
				match(LBRACE);
				setState(498);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(495);
					branchCase();
					}
					}
					setState(500);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(501);
				match(RBRACE);
				}
				break;
			case RETURN:
				enterOuterAlt(_localctx, 6);
				{
				setState(502);
				match(RETURN);
				setState(504);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
				case 1:
					{
					setState(503);
					expression();
					}
					break;
				}
				}
				break;
			case THROW:
				enterOuterAlt(_localctx, 7);
				{
				setState(506);
				match(THROW);
				setState(507);
				expression();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 8);
				{
				setState(508);
				match(SEMI);
				}
				break;
			case NULL:
			case NEW:
			case SUPER:
			case THIS:
			case VALUE:
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
				enterOuterAlt(_localctx, 9);
				{
				setState(509);
				((StatementContext)_localctx).statementExpression = expression();
				}
				break;
			case VAL:
			case VAR:
				enterOuterAlt(_localctx, 10);
				{
				setState(510);
				localVariableDeclaration();
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
		enterRule(_localctx, 66, RULE_localVariableDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(513);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(514);
			identifier();
			setState(517);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(515);
				match(COLON);
				setState(516);
				type(0);
				}
			}

			setState(521);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(519);
				match(ASSIGN);
				setState(520);
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
		public LoopVariableUpdatesContext forUpdate;
		public List<TerminalNode> SEMI() { return getTokens(KiwiParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(KiwiParser.SEMI, i);
		}
		public LoopVariableDeclaratorsContext loopVariableDeclarators() {
			return getRuleContext(LoopVariableDeclaratorsContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LoopVariableUpdatesContext loopVariableUpdates() {
			return getRuleContext(LoopVariableUpdatesContext.class,0);
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
		enterRule(_localctx, 68, RULE_forControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 42331230188356L) != 0) || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 33554449L) != 0)) {
				{
				setState(523);
				loopVariableDeclarators();
				}
			}

			setState(526);
			match(SEMI);
			setState(528);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 45071223729684992L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 87943752450063L) != 0)) {
				{
				setState(527);
				expression();
				}
			}

			setState(530);
			match(SEMI);
			setState(532);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUE || _la==IDENTIFIER) {
				{
				setState(531);
				((ForControlContext)_localctx).forUpdate = loopVariableUpdates();
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
		enterRule(_localctx, 70, RULE_loopVariableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534);
			loopVariableDeclarator();
			setState(539);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(535);
				match(COMMA);
				setState(536);
				loopVariableDeclarator();
				}
				}
				setState(541);
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
		enterRule(_localctx, 72, RULE_loopVariableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(542);
			type(0);
			setState(543);
			identifier();
			setState(544);
			match(ASSIGN);
			setState(545);
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
		enterRule(_localctx, 74, RULE_loopVariableUpdates);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			loopVariableUpdate();
			setState(552);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(548);
				match(COMMA);
				setState(549);
				loopVariableUpdate();
				}
				}
				setState(554);
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
		enterRule(_localctx, 76, RULE_loopVariableUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(555);
			identifier();
			setState(556);
			match(ASSIGN);
			setState(557);
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
	public static class NewExprContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(KiwiParser.NEW, 0); }
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public NewExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_newExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterNewExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitNewExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitNewExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NewExprContext newExpr() throws RecognitionException {
		NewExprContext _localctx = new NewExprContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_newExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(559);
			match(NEW);
			setState(561);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(560);
				typeArguments();
				}
			}

			setState(563);
			classOrInterfaceType();
			setState(564);
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
	public static class NewArrayContext extends ParserRuleContext {
		public TerminalNode NEW() { return getToken(KiwiParser.NEW, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
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
		enterRule(_localctx, 80, RULE_newArray);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(566);
			match(NEW);
			setState(567);
			type(0);
			setState(568);
			arrayKind();
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
		enterRule(_localctx, 82, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(570);
			match(LBRACE);
			setState(582);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 45071223729684992L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 87943760838671L) != 0)) {
				{
				setState(571);
				variableInitializer();
				setState(576);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(572);
						match(COMMA);
						setState(573);
						variableInitializer();
						}
						} 
					}
					setState(578);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
				}
				setState(580);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(579);
					match(COMMA);
					}
				}

				}
			}

			setState(584);
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
		enterRule(_localctx, 84, RULE_variableInitializer);
		try {
			setState(588);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(586);
				arrayInitializer();
				}
				break;
			case NULL:
			case NEW:
			case SUPER:
			case THIS:
			case VALUE:
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
				setState(587);
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
		public TerminalNode LBRACE() { return getToken(KiwiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(KiwiParser.RBRACE, 0); }
		public CatchFieldsContext catchFields() {
			return getRuleContext(CatchFieldsContext.class,0);
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
		enterRule(_localctx, 86, RULE_catchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(590);
			match(CATCH);
			setState(591);
			match(LBRACE);
			setState(593);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUE || _la==IDENTIFIER) {
				{
				setState(592);
				catchFields();
				}
			}

			setState(595);
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
		enterRule(_localctx, 88, RULE_catchFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			catchField();
			setState(602);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(598);
				match(COMMA);
				setState(599);
				catchField();
				}
				}
				setState(604);
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
		enterRule(_localctx, 90, RULE_catchField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(605);
			identifier();
			setState(606);
			match(COLON);
			setState(607);
			match(LBRACE);
			setState(613);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==VALUE || _la==IDENTIFIER) {
				{
				{
				setState(608);
				catchValue();
				setState(609);
				match(COMMA);
				}
				}
				setState(615);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(616);
			match(DEFAULT);
			setState(617);
			match(COLON);
			setState(618);
			expression();
			setState(619);
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
		enterRule(_localctx, 92, RULE_catchValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(621);
			identifier();
			setState(622);
			match(COLON);
			setState(623);
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
		enterRule(_localctx, 94, RULE_branchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(625);
			switchLabel();
			setState(626);
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
		enterRule(_localctx, 96, RULE_switchLabel);
		try {
			setState(634);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(628);
				match(CASE);
				{
				setState(629);
				expression();
				}
				setState(630);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(632);
				match(DEFAULT);
				setState(633);
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
		enterRule(_localctx, 98, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(636);
			match(LPAREN);
			setState(637);
			expression();
			setState(638);
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
		enterRule(_localctx, 100, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(640);
			expression();
			setState(645);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(641);
				match(COMMA);
				setState(642);
				expression();
				}
				}
				setState(647);
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
		enterRule(_localctx, 102, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(648);
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
		enterRule(_localctx, 104, RULE_assignment);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(650);
			ternary();
			setState(654);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(651);
					assignmentSuffix();
					}
					} 
				}
				setState(656);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
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
		enterRule(_localctx, 106, RULE_assignmentSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(657);
			((AssignmentSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 50)) & ~0x3f) == 0 && ((1L << (_la - 50)) & 34141634561L) != 0)) ) {
				((AssignmentSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(658);
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
		enterRule(_localctx, 108, RULE_ternary);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(660);
			disjunction();
			setState(666);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==QUESTION) {
				{
				setState(661);
				match(QUESTION);
				setState(662);
				disjunction();
				setState(663);
				match(COLON);
				setState(664);
				ternary();
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
		enterRule(_localctx, 110, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(668);
			conjunction();
			setState(673);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(669);
				match(OR);
				setState(670);
				conjunction();
				}
				}
				setState(675);
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
	public static class ConjunctionContext extends ParserRuleContext {
		public List<BitorContext> bitor() {
			return getRuleContexts(BitorContext.class);
		}
		public BitorContext bitor(int i) {
			return getRuleContext(BitorContext.class,i);
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
		enterRule(_localctx, 112, RULE_conjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(676);
			bitor();
			setState(681);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(677);
				match(AND);
				setState(678);
				bitor();
				}
				}
				setState(683);
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
		enterRule(_localctx, 114, RULE_bitor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(684);
			bitand();
			setState(689);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITOR) {
				{
				{
				setState(685);
				match(BITOR);
				setState(686);
				bitand();
				}
				}
				setState(691);
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
		enterRule(_localctx, 116, RULE_bitand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(692);
			bitxor();
			setState(697);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(693);
				match(BITAND);
				setState(694);
				bitxor();
				}
				}
				setState(699);
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
		enterRule(_localctx, 118, RULE_bitxor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(700);
			equality();
			setState(705);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CARET) {
				{
				{
				setState(701);
				match(CARET);
				setState(702);
				equality();
				}
				}
				setState(707);
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
		enterRule(_localctx, 120, RULE_equality);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(708);
			relational();
			setState(712);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==EQUAL || _la==NOTEQUAL) {
				{
				{
				setState(709);
				equalitySuffix();
				}
				}
				setState(714);
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
		enterRule(_localctx, 122, RULE_equalitySuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(715);
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
			setState(716);
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
		enterRule(_localctx, 124, RULE_relational);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(718);
			isExpr();
			setState(722);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 583216151744479232L) != 0)) {
				{
				{
				setState(719);
				relationalSuffix();
				}
				}
				setState(724);
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
	public static class RelationalSuffixContext extends ParserRuleContext {
		public Token op;
		public IsExprContext isExpr() {
			return getRuleContext(IsExprContext.class,0);
		}
		public TerminalNode LT() { return getToken(KiwiParser.LT, 0); }
		public TerminalNode GT() { return getToken(KiwiParser.GT, 0); }
		public TerminalNode LE() { return getToken(KiwiParser.LE, 0); }
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
		enterRule(_localctx, 126, RULE_relationalSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(725);
			((RelationalSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 583216151744479232L) != 0)) ) {
				((RelationalSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(726);
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
		public List<TerminalNode> IS() { return getTokens(KiwiParser.IS); }
		public TerminalNode IS(int i) {
			return getToken(KiwiParser.IS, i);
		}
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
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
		enterRule(_localctx, 128, RULE_isExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(728);
			shift();
			setState(733);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IS) {
				{
				{
				setState(729);
				match(IS);
				setState(730);
				type(0);
				}
				}
				setState(735);
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
		enterRule(_localctx, 130, RULE_shift);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(736);
			additive();
			setState(740);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(737);
					shiftSuffix();
					}
					} 
				}
				setState(742);
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
	public static class ShiftSuffixContext extends ParserRuleContext {
		public List<TerminalNode> GT() { return getTokens(KiwiParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(KiwiParser.GT, i);
		}
		public List<TerminalNode> LT() { return getTokens(KiwiParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(KiwiParser.LT, i);
		}
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
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
		enterRule(_localctx, 132, RULE_shiftSuffix);
		try {
			setState(751);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(743);
				match(GT);
				setState(744);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(745);
				match(LT);
				setState(746);
				match(LT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(747);
				match(GT);
				setState(748);
				match(GT);
				setState(749);
				match(GT);
				setState(750);
				additive();
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
		enterRule(_localctx, 134, RULE_additive);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(753);
			multiplicative();
			setState(757);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(754);
					additiveSuffix();
					}
					} 
				}
				setState(759);
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
		enterRule(_localctx, 136, RULE_additiveSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(760);
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
			setState(761);
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
		enterRule(_localctx, 138, RULE_multiplicative);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(763);
			asExpr();
			setState(767);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 35L) != 0)) {
				{
				{
				setState(764);
				multiplicativeSuffix();
				}
				}
				setState(769);
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
		enterRule(_localctx, 140, RULE_multiplicativeSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(770);
			((MultiplicativeSuffixContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 35L) != 0)) ) {
				((MultiplicativeSuffixContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(771);
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
		enterRule(_localctx, 142, RULE_asExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(773);
			prefixExpr();
			setState(778);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AS) {
				{
				{
				setState(774);
				match(AS);
				setState(775);
				type(0);
				}
				}
				setState(780);
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
		enterRule(_localctx, 144, RULE_prefixExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(784);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 30725L) != 0)) {
				{
				{
				setState(781);
				prefixOp();
				}
				}
				setState(786);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(787);
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
		enterRule(_localctx, 146, RULE_prefixOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(789);
			((PrefixOpContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 30725L) != 0)) ) {
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
		enterRule(_localctx, 148, RULE_postfixExpr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(791);
			primaryExpr();
			setState(795);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,78,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(792);
					postfixSuffix();
					}
					} 
				}
				setState(797);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,78,_ctx);
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
		public NewExprContext newExpr() {
			return getRuleContext(NewExprContext.class,0);
		}
		public IndexingSuffixContext indexingSuffix() {
			return getRuleContext(IndexingSuffixContext.class,0);
		}
		public SelectorSuffixContext selectorSuffix() {
			return getRuleContext(SelectorSuffixContext.class,0);
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
		enterRule(_localctx, 150, RULE_postfixSuffix);
		int _la;
		try {
			setState(804);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(798);
				((PostfixSuffixContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 54)) & ~0x3f) == 0 && ((1L << (_la - 54)) & 3073L) != 0)) ) {
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
				setState(799);
				callSuffix();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(800);
				match(DOT);
				setState(801);
				newExpr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(802);
				indexingSuffix();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(803);
				selectorSuffix();
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
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
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
		enterRule(_localctx, 152, RULE_callSuffix);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(807);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(806);
				typeArguments();
				}
			}

			setState(809);
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
		enterRule(_localctx, 154, RULE_indexingSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(811);
			match(LBRACK);
			setState(812);
			expression();
			setState(813);
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
		enterRule(_localctx, 156, RULE_selectorSuffix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(815);
			match(DOT);
			setState(816);
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
	public static class PrimaryExprContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode THIS() { return getToken(KiwiParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(KiwiParser.SUPER, 0); }
		public NewExprContext newExpr() {
			return getRuleContext(NewExprContext.class,0);
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
		public TerminalNode NEW() { return getToken(KiwiParser.NEW, 0); }
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
		enterRule(_localctx, 158, RULE_primaryExpr);
		try {
			setState(830);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(818);
				match(LPAREN);
				setState(819);
				expression();
				setState(820);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(822);
				match(THIS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(823);
				match(SUPER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(824);
				newExpr();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(825);
				newArray();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(826);
				literal();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(827);
				identifier();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(828);
				lambdaExpression();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(829);
				match(NEW);
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
		enterRule(_localctx, 160, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(832);
			match(LPAREN);
			setState(834);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 45071223729684992L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 87943752450063L) != 0)) {
				{
				setState(833);
				expressionList();
				}
			}

			setState(836);
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
		enterRule(_localctx, 162, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(838);
			_la = _input.LA(1);
			if ( !(_la==VALUE || _la==IDENTIFIER) ) {
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
	public static class MethodCallContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public MethodCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_methodCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(840);
			identifier();
			setState(842);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(841);
				typeArguments();
				}
			}

			setState(844);
			match(LPAREN);
			setState(846);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 45071223729684992L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 87943752450063L) != 0)) {
				{
				setState(845);
				expressionList();
				}
			}

			setState(848);
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
		enterRule(_localctx, 166, RULE_literal);
		try {
			setState(857);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(850);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(851);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(852);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(853);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(854);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(855);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(856);
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
		enterRule(_localctx, 168, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(859);
			_la = _input.LA(1);
			if ( !(((((_la - 98)) & ~0x3f) == 0 && ((1L << (_la - 98)) & 15L) != 0)) ) {
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
		enterRule(_localctx, 170, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(861);
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
		enterRule(_localctx, 172, RULE_typeOrVoid);
		try {
			setState(865);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(863);
				match(VOID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(864);
				type(0);
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
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public TerminalNode ANY() { return getToken(KiwiParser.ANY, 0); }
		public TerminalNode NEVER() { return getToken(KiwiParser.NEVER, 0); }
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
		public TerminalNode LBRACK() { return getToken(KiwiParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(KiwiParser.RBRACK, 0); }
		public List<TerminalNode> BITOR() { return getTokens(KiwiParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(KiwiParser.BITOR, i);
		}
		public List<TerminalNode> BITAND() { return getTokens(KiwiParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(KiwiParser.BITAND, i);
		}
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
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
		return type(0);
	}

	private TypeContext type(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TypeContext _localctx = new TypeContext(_ctx, _parentState);
		TypeContext _prevctx = _localctx;
		int _startState = 174;
		enterRecursionRule(_localctx, 174, RULE_type, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(892);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
			case IDENTIFIER:
				{
				setState(868);
				classOrInterfaceType();
				}
				break;
			case BOOLEAN:
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
				{
				setState(869);
				primitiveType();
				}
				break;
			case ANY:
				{
				setState(870);
				match(ANY);
				}
				break;
			case NEVER:
				{
				setState(871);
				match(NEVER);
				}
				break;
			case LPAREN:
				{
				setState(872);
				match(LPAREN);
				setState(881);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 42331230188356L) != 0) || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 33554449L) != 0)) {
					{
					setState(873);
					type(0);
					setState(878);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(874);
						match(COMMA);
						setState(875);
						type(0);
						}
						}
						setState(880);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(883);
				match(RPAREN);
				setState(884);
				match(ARROW);
				setState(885);
				type(2);
				}
				break;
			case LBRACK:
				{
				setState(886);
				match(LBRACK);
				setState(887);
				type(0);
				setState(888);
				match(COMMA);
				setState(889);
				type(0);
				setState(890);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(912);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(910);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
					case 1:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(894);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(897); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(895);
								match(BITOR);
								setState(896);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(899); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(901);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(904); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(902);
								match(BITAND);
								setState(903);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(906); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,91,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(908);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(909);
						arrayKind();
						}
						break;
					}
					} 
				}
				setState(914);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
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
		enterRule(_localctx, 176, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(915);
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
	public static class ClassOrInterfaceTypeContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ClassOrInterfaceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterClassOrInterfaceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitClassOrInterfaceType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitClassOrInterfaceType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceTypeContext classOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(917);
			qualifiedName();
			setState(919);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				{
				setState(918);
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
		enterRule(_localctx, 180, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			match(LT);
			setState(922);
			type(0);
			setState(927);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(923);
				match(COMMA);
				setState(924);
				type(0);
				}
				}
				setState(929);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(930);
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
	public static class PrimitiveTypeContext extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(KiwiParser.BOOLEAN, 0); }
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
		enterRule(_localctx, 182, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(932);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 549788332868L) != 0)) ) {
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
		enterRule(_localctx, 184, RULE_modifier);
		try {
			setState(937);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case VALUE:
				enterOuterAlt(_localctx, 1);
				{
				setState(934);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(935);
				match(NATIVE);
				}
				break;
			case DELETED:
				enterOuterAlt(_localctx, 3);
				{
				setState(936);
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
		public TerminalNode PUBLIC() { return getToken(KiwiParser.PUBLIC, 0); }
		public TerminalNode PROTECTED() { return getToken(KiwiParser.PROTECTED, 0); }
		public TerminalNode PRIVATE() { return getToken(KiwiParser.PRIVATE, 0); }
		public TerminalNode STATIC() { return getToken(KiwiParser.STATIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(KiwiParser.ABSTRACT, 0); }
		public TerminalNode VALUE() { return getToken(KiwiParser.VALUE, 0); }
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
		enterRule(_localctx, 186, RULE_classOrInterfaceModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(939);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 35190546104322L) != 0)) ) {
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
		public TypeOrVoidContext typeOrVoid() {
			return getRuleContext(TypeOrVoidContext.class,0);
		}
		public LambdaBodyContext lambdaBody() {
			return getRuleContext(LambdaBodyContext.class,0);
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
		enterRule(_localctx, 188, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(941);
			lambdaParameters();
			setState(942);
			match(ARROW);
			setState(943);
			typeOrVoid();
			setState(944);
			lambdaBody();
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
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
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
		enterRule(_localctx, 190, RULE_lambdaParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(946);
			match(LPAREN);
			setState(948);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VALUE || _la==IDENTIFIER) {
				{
				setState(947);
				formalParameterList();
				}
			}

			setState(950);
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
		enterRule(_localctx, 192, RULE_lambdaBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(952);
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
		enterRule(_localctx, 194, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(954);
			match(AT);
			setState(955);
			identifier();
			setState(962);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(956);
				match(LPAREN);
				setState(959);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
				case 1:
					{
					setState(957);
					elementValuePairs();
					}
					break;
				case 2:
					{
					setState(958);
					expression();
					}
					break;
				}
				setState(961);
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
		enterRule(_localctx, 196, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(964);
			elementValuePair();
			setState(969);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(965);
				match(COMMA);
				setState(966);
				elementValuePair();
				}
				}
				setState(971);
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
		enterRule(_localctx, 198, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(972);
			identifier();
			setState(973);
			match(ASSIGN);
			setState(974);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 87:
			return type_sempred((TypeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean type_sempred(TypeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		case 2:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001q\u03d1\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007c\u0001"+
		"\u0000\u0003\u0000\u00ca\b\u0000\u0001\u0000\u0005\u0000\u00cd\b\u0000"+
		"\n\u0000\f\u0000\u00d0\t\u0000\u0001\u0000\u0004\u0000\u00d3\b\u0000\u000b"+
		"\u0000\f\u0000\u00d4\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0005\u0003\u00de\b\u0003\n\u0003\f\u0003"+
		"\u00e1\t\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u00e6\b"+
		"\u0003\u0001\u0004\u0005\u0004\u00e9\b\u0004\n\u0004\f\u0004\u00ec\t\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00f1\b\u0004\u0001\u0004"+
		"\u0001\u0004\u0003\u0004\u00f5\b\u0004\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0005\u0005\u00fb\b\u0005\n\u0005\f\u0005\u00fe\t\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u0105"+
		"\b\u0006\n\u0006\f\u0006\u0108\t\u0006\u0001\u0007\u0005\u0007\u010b\b"+
		"\u0007\n\u0007\f\u0007\u010e\t\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u0112\b\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0005\t\u0118\b\t\n\t\f"+
		"\t\u011b\t\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0121\b\t\u0001\t"+
		"\u0001\t\u0003\t\u0125\b\t\u0001\t\u0003\t\u0128\b\t\u0001\t\u0003\t\u012b"+
		"\b\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0005\n\u0132\b\n\n\n\f\n"+
		"\u0135\t\n\u0001\u000b\u0001\u000b\u0003\u000b\u0139\b\u000b\u0001\f\u0001"+
		"\f\u0005\f\u013d\b\f\n\f\f\f\u0140\t\f\u0001\r\u0005\r\u0143\b\r\n\r\f"+
		"\r\u0146\t\r\u0001\r\u0001\r\u0001\r\u0003\r\u014b\b\r\u0001\r\u0001\r"+
		"\u0003\r\u014f\b\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0005\u000e"+
		"\u0155\b\u000e\n\u000e\f\u000e\u0158\t\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000f\u0005\u000f\u015d\b\u000f\n\u000f\f\u000f\u0160\t\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0005\u0011\u0167\b\u0011"+
		"\n\u0011\f\u0011\u016a\t\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u016f\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0174"+
		"\b\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0178\b\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013"+
		"\u0180\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u018a\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0003\u0015\u018f\b\u0015\u0001\u0015\u0003\u0015"+
		"\u0192\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016"+
		"\u0198\b\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0005\u0017\u01a0\b\u0017\n\u0017\f\u0017\u01a3\t\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u01aa"+
		"\b\u0018\n\u0018\f\u0018\u01ad\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0005\u0019\u01b2\b\u0019\n\u0019\f\u0019\u01b5\t\u0019\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0003\u001a\u01ba\b\u001a\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u01be\b\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0005\u001c\u01c5\b\u001c\n\u001c\f\u001c\u01c8\t\u001c\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001f"+
		"\u0001\u001f\u0005\u001f\u01d2\b\u001f\n\u001f\f\u001f\u01d5\t\u001f\u0001"+
		"\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u01e8\b \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0005 \u01f1\b \n \f \u01f4"+
		"\t \u0001 \u0001 \u0001 \u0003 \u01f9\b \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0003 \u0200\b \u0001!\u0001!\u0001!\u0001!\u0003!\u0206\b!\u0001!\u0001"+
		"!\u0003!\u020a\b!\u0001\"\u0003\"\u020d\b\"\u0001\"\u0001\"\u0003\"\u0211"+
		"\b\"\u0001\"\u0001\"\u0003\"\u0215\b\"\u0001#\u0001#\u0001#\u0005#\u021a"+
		"\b#\n#\f#\u021d\t#\u0001$\u0001$\u0001$\u0001$\u0001$\u0001%\u0001%\u0001"+
		"%\u0005%\u0227\b%\n%\f%\u022a\t%\u0001&\u0001&\u0001&\u0001&\u0001\'\u0001"+
		"\'\u0003\'\u0232\b\'\u0001\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0001"+
		"(\u0001)\u0001)\u0001)\u0001)\u0005)\u023f\b)\n)\f)\u0242\t)\u0001)\u0003"+
		")\u0245\b)\u0003)\u0247\b)\u0001)\u0001)\u0001*\u0001*\u0003*\u024d\b"+
		"*\u0001+\u0001+\u0001+\u0003+\u0252\b+\u0001+\u0001+\u0001,\u0001,\u0001"+
		",\u0005,\u0259\b,\n,\f,\u025c\t,\u0001-\u0001-\u0001-\u0001-\u0001-\u0001"+
		"-\u0005-\u0264\b-\n-\f-\u0267\t-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001"+
		".\u0001.\u0001.\u0001.\u0001/\u0001/\u0001/\u00010\u00010\u00010\u0001"+
		"0\u00010\u00010\u00030\u027b\b0\u00011\u00011\u00011\u00011\u00012\u0001"+
		"2\u00012\u00052\u0284\b2\n2\f2\u0287\t2\u00013\u00013\u00014\u00014\u0005"+
		"4\u028d\b4\n4\f4\u0290\t4\u00015\u00015\u00015\u00016\u00016\u00016\u0001"+
		"6\u00016\u00016\u00036\u029b\b6\u00017\u00017\u00017\u00057\u02a0\b7\n"+
		"7\f7\u02a3\t7\u00018\u00018\u00018\u00058\u02a8\b8\n8\f8\u02ab\t8\u0001"+
		"9\u00019\u00019\u00059\u02b0\b9\n9\f9\u02b3\t9\u0001:\u0001:\u0001:\u0005"+
		":\u02b8\b:\n:\f:\u02bb\t:\u0001;\u0001;\u0001;\u0005;\u02c0\b;\n;\f;\u02c3"+
		"\t;\u0001<\u0001<\u0005<\u02c7\b<\n<\f<\u02ca\t<\u0001=\u0001=\u0001="+
		"\u0001>\u0001>\u0005>\u02d1\b>\n>\f>\u02d4\t>\u0001?\u0001?\u0001?\u0001"+
		"@\u0001@\u0001@\u0005@\u02dc\b@\n@\f@\u02df\t@\u0001A\u0001A\u0005A\u02e3"+
		"\bA\nA\fA\u02e6\tA\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001"+
		"B\u0003B\u02f0\bB\u0001C\u0001C\u0005C\u02f4\bC\nC\fC\u02f7\tC\u0001D"+
		"\u0001D\u0001D\u0001E\u0001E\u0005E\u02fe\bE\nE\fE\u0301\tE\u0001F\u0001"+
		"F\u0001F\u0001G\u0001G\u0001G\u0005G\u0309\bG\nG\fG\u030c\tG\u0001H\u0005"+
		"H\u030f\bH\nH\fH\u0312\tH\u0001H\u0001H\u0001I\u0001I\u0001J\u0001J\u0005"+
		"J\u031a\bJ\nJ\fJ\u031d\tJ\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0003"+
		"K\u0325\bK\u0001L\u0003L\u0328\bL\u0001L\u0001L\u0001M\u0001M\u0001M\u0001"+
		"M\u0001N\u0001N\u0001N\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0003O\u033f\bO\u0001P\u0001P\u0003"+
		"P\u0343\bP\u0001P\u0001P\u0001Q\u0001Q\u0001R\u0001R\u0003R\u034b\bR\u0001"+
		"R\u0001R\u0003R\u034f\bR\u0001R\u0001R\u0001S\u0001S\u0001S\u0001S\u0001"+
		"S\u0001S\u0001S\u0003S\u035a\bS\u0001T\u0001T\u0001U\u0001U\u0001V\u0001"+
		"V\u0003V\u0362\bV\u0001W\u0001W\u0001W\u0001W\u0001W\u0001W\u0001W\u0001"+
		"W\u0001W\u0005W\u036d\bW\nW\fW\u0370\tW\u0003W\u0372\bW\u0001W\u0001W"+
		"\u0001W\u0001W\u0001W\u0001W\u0001W\u0001W\u0001W\u0003W\u037d\bW\u0001"+
		"W\u0001W\u0001W\u0004W\u0382\bW\u000bW\fW\u0383\u0001W\u0001W\u0001W\u0004"+
		"W\u0389\bW\u000bW\fW\u038a\u0001W\u0001W\u0005W\u038f\bW\nW\fW\u0392\t"+
		"W\u0001X\u0001X\u0001Y\u0001Y\u0003Y\u0398\bY\u0001Z\u0001Z\u0001Z\u0001"+
		"Z\u0005Z\u039e\bZ\nZ\fZ\u03a1\tZ\u0001Z\u0001Z\u0001[\u0001[\u0001\\\u0001"+
		"\\\u0001\\\u0003\\\u03aa\b\\\u0001]\u0001]\u0001^\u0001^\u0001^\u0001"+
		"^\u0001^\u0001_\u0001_\u0003_\u03b5\b_\u0001_\u0001_\u0001`\u0001`\u0001"+
		"a\u0001a\u0001a\u0001a\u0001a\u0003a\u03c0\ba\u0001a\u0003a\u03c3\ba\u0001"+
		"b\u0001b\u0001b\u0005b\u03c8\bb\nb\fb\u03cb\tb\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0000\u0001\u00aed\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPR"+
		"TVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e"+
		"\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6"+
		"\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be"+
		"\u00c0\u00c2\u00c4\u00c6\u0000\u000f\u0004\u0000\u0001\u0001\u0004\u0004"+
		"\u001e\u001e  \u0001\u0000./\u0003\u000022JKNT\u0002\u0000::==\u0002\u0000"+
		"34;;\u0001\u0000BC\u0002\u0000DEII\u0003\u00005577@C\u0002\u000066@A\u0002"+
		"\u0000--nn\u0001\u0000be\u0001\u0000fg\u0001\u0000lm\u0006\u0000\u0002"+
		"\u0002\u0006\u0006\b\t\f\r\u0014\u0018\'\'\u0004\u0000\u0001\u0001\u001c"+
		"\u001e  --\u03f3\u0000\u00c9\u0001\u0000\u0000\u0000\u0002\u00d6\u0001"+
		"\u0000\u0000\u0000\u0004\u00d9\u0001\u0000\u0000\u0000\u0006\u00df\u0001"+
		"\u0000\u0000\u0000\b\u00ea\u0001\u0000\u0000\u0000\n\u00f8\u0001\u0000"+
		"\u0000\u0000\f\u0101\u0001\u0000\u0000\u0000\u000e\u0111\u0001\u0000\u0000"+
		"\u0000\u0010\u0113\u0001\u0000\u0000\u0000\u0012\u0119\u0001\u0000\u0000"+
		"\u0000\u0014\u012e\u0001\u0000\u0000\u0000\u0016\u0136\u0001\u0000\u0000"+
		"\u0000\u0018\u013a\u0001\u0000\u0000\u0000\u001a\u0144\u0001\u0000\u0000"+
		"\u0000\u001c\u0152\u0001\u0000\u0000\u0000\u001e\u015e\u0001\u0000\u0000"+
		"\u0000 \u0163\u0001\u0000\u0000\u0000\"\u0168\u0001\u0000\u0000\u0000"+
		"$\u0179\u0001\u0000\u0000\u0000&\u017f\u0001\u0000\u0000\u0000(\u0181"+
		"\u0001\u0000\u0000\u0000*\u0186\u0001\u0000\u0000\u0000,\u0193\u0001\u0000"+
		"\u0000\u0000.\u019b\u0001\u0000\u0000\u00000\u01a6\u0001\u0000\u0000\u0000"+
		"2\u01ae\u0001\u0000\u0000\u00004\u01b6\u0001\u0000\u0000\u00006\u01bb"+
		"\u0001\u0000\u0000\u00008\u01c1\u0001\u0000\u0000\u0000:\u01c9\u0001\u0000"+
		"\u0000\u0000<\u01cd\u0001\u0000\u0000\u0000>\u01cf\u0001\u0000\u0000\u0000"+
		"@\u01ff\u0001\u0000\u0000\u0000B\u0201\u0001\u0000\u0000\u0000D\u020c"+
		"\u0001\u0000\u0000\u0000F\u0216\u0001\u0000\u0000\u0000H\u021e\u0001\u0000"+
		"\u0000\u0000J\u0223\u0001\u0000\u0000\u0000L\u022b\u0001\u0000\u0000\u0000"+
		"N\u022f\u0001\u0000\u0000\u0000P\u0236\u0001\u0000\u0000\u0000R\u023a"+
		"\u0001\u0000\u0000\u0000T\u024c\u0001\u0000\u0000\u0000V\u024e\u0001\u0000"+
		"\u0000\u0000X\u0255\u0001\u0000\u0000\u0000Z\u025d\u0001\u0000\u0000\u0000"+
		"\\\u026d\u0001\u0000\u0000\u0000^\u0271\u0001\u0000\u0000\u0000`\u027a"+
		"\u0001\u0000\u0000\u0000b\u027c\u0001\u0000\u0000\u0000d\u0280\u0001\u0000"+
		"\u0000\u0000f\u0288\u0001\u0000\u0000\u0000h\u028a\u0001\u0000\u0000\u0000"+
		"j\u0291\u0001\u0000\u0000\u0000l\u0294\u0001\u0000\u0000\u0000n\u029c"+
		"\u0001\u0000\u0000\u0000p\u02a4\u0001\u0000\u0000\u0000r\u02ac\u0001\u0000"+
		"\u0000\u0000t\u02b4\u0001\u0000\u0000\u0000v\u02bc\u0001\u0000\u0000\u0000"+
		"x\u02c4\u0001\u0000\u0000\u0000z\u02cb\u0001\u0000\u0000\u0000|\u02ce"+
		"\u0001\u0000\u0000\u0000~\u02d5\u0001\u0000\u0000\u0000\u0080\u02d8\u0001"+
		"\u0000\u0000\u0000\u0082\u02e0\u0001\u0000\u0000\u0000\u0084\u02ef\u0001"+
		"\u0000\u0000\u0000\u0086\u02f1\u0001\u0000\u0000\u0000\u0088\u02f8\u0001"+
		"\u0000\u0000\u0000\u008a\u02fb\u0001\u0000\u0000\u0000\u008c\u0302\u0001"+
		"\u0000\u0000\u0000\u008e\u0305\u0001\u0000\u0000\u0000\u0090\u0310\u0001"+
		"\u0000\u0000\u0000\u0092\u0315\u0001\u0000\u0000\u0000\u0094\u0317\u0001"+
		"\u0000\u0000\u0000\u0096\u0324\u0001\u0000\u0000\u0000\u0098\u0327\u0001"+
		"\u0000\u0000\u0000\u009a\u032b\u0001\u0000\u0000\u0000\u009c\u032f\u0001"+
		"\u0000\u0000\u0000\u009e\u033e\u0001\u0000\u0000\u0000\u00a0\u0340\u0001"+
		"\u0000\u0000\u0000\u00a2\u0346\u0001\u0000\u0000\u0000\u00a4\u0348\u0001"+
		"\u0000\u0000\u0000\u00a6\u0359\u0001\u0000\u0000\u0000\u00a8\u035b\u0001"+
		"\u0000\u0000\u0000\u00aa\u035d\u0001\u0000\u0000\u0000\u00ac\u0361\u0001"+
		"\u0000\u0000\u0000\u00ae\u037c\u0001\u0000\u0000\u0000\u00b0\u0393\u0001"+
		"\u0000\u0000\u0000\u00b2\u0395\u0001\u0000\u0000\u0000\u00b4\u0399\u0001"+
		"\u0000\u0000\u0000\u00b6\u03a4\u0001\u0000\u0000\u0000\u00b8\u03a9\u0001"+
		"\u0000\u0000\u0000\u00ba\u03ab\u0001\u0000\u0000\u0000\u00bc\u03ad\u0001"+
		"\u0000\u0000\u0000\u00be\u03b2\u0001\u0000\u0000\u0000\u00c0\u03b8\u0001"+
		"\u0000\u0000\u0000\u00c2\u03ba\u0001\u0000\u0000\u0000\u00c4\u03c4\u0001"+
		"\u0000\u0000\u0000\u00c6\u03cc\u0001\u0000\u0000\u0000\u00c8\u00ca\u0003"+
		"\u0002\u0001\u0000\u00c9\u00c8\u0001\u0000\u0000\u0000\u00c9\u00ca\u0001"+
		"\u0000\u0000\u0000\u00ca\u00ce\u0001\u0000\u0000\u0000\u00cb\u00cd\u0003"+
		"\u0004\u0002\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cd\u00d0\u0001"+
		"\u0000\u0000\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001"+
		"\u0000\u0000\u0000\u00cf\u00d2\u0001\u0000\u0000\u0000\u00d0\u00ce\u0001"+
		"\u0000\u0000\u0000\u00d1\u00d3\u0003\u0006\u0003\u0000\u00d2\u00d1\u0001"+
		"\u0000\u0000\u0000\u00d3\u00d4\u0001\u0000\u0000\u0000\u00d4\u00d2\u0001"+
		"\u0000\u0000\u0000\u00d4\u00d5\u0001\u0000\u0000\u0000\u00d5\u0001\u0001"+
		"\u0000\u0000\u0000\u00d6\u00d7\u0005\n\u0000\u0000\u00d7\u00d8\u00032"+
		"\u0019\u0000\u00d8\u0003\u0001\u0000\u0000\u0000\u00d9\u00da\u0005\u000b"+
		"\u0000\u0000\u00da\u00db\u00032\u0019\u0000\u00db\u0005\u0001\u0000\u0000"+
		"\u0000\u00dc\u00de\u0003\u00ba]\u0000\u00dd\u00dc\u0001\u0000\u0000\u0000"+
		"\u00de\u00e1\u0001\u0000\u0000\u0000\u00df\u00dd\u0001\u0000\u0000\u0000"+
		"\u00df\u00e0\u0001\u0000\u0000\u0000\u00e0\u00e5\u0001\u0000\u0000\u0000"+
		"\u00e1\u00df\u0001\u0000\u0000\u0000\u00e2\u00e6\u0003\b\u0004\u0000\u00e3"+
		"\u00e6\u0003\u0012\t\u0000\u00e4\u00e6\u0003\u001a\r\u0000\u00e5\u00e2"+
		"\u0001\u0000\u0000\u0000\u00e5\u00e3\u0001\u0000\u0000\u0000\u00e5\u00e4"+
		"\u0001\u0000\u0000\u0000\u00e6\u0007\u0001\u0000\u0000\u0000\u00e7\u00e9"+
		"\u0003\u00c2a\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000\u00e9\u00ec\u0001"+
		"\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001"+
		"\u0000\u0000\u0000\u00eb\u00ed\u0001\u0000\u0000\u0000\u00ec\u00ea\u0001"+
		"\u0000\u0000\u0000\u00ed\u00ee\u0005\u0007\u0000\u0000\u00ee\u00f0\u0003"+
		"\u00a2Q\u0000\u00ef\u00f1\u0003.\u0017\u0000\u00f0\u00ef\u0001\u0000\u0000"+
		"\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000\u0000"+
		"\u0000\u00f2\u00f3\u00059\u0000\u0000\u00f3\u00f5\u0003\f\u0006\u0000"+
		"\u00f4\u00f2\u0001\u0000\u0000\u0000\u00f4\u00f5\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f7\u0003\n\u0005\u0000\u00f7"+
		"\t\u0001\u0000\u0000\u0000\u00f8\u00fc\u0005W\u0000\u0000\u00f9\u00fb"+
		"\u0003\u000e\u0007\u0000\u00fa\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fe"+
		"\u0001\u0000\u0000\u0000\u00fc\u00fa\u0001\u0000\u0000\u0000\u00fc\u00fd"+
		"\u0001\u0000\u0000\u0000\u00fd\u00ff\u0001\u0000\u0000\u0000\u00fe\u00fc"+
		"\u0001\u0000\u0000\u0000\u00ff\u0100\u0005X\u0000\u0000\u0100\u000b\u0001"+
		"\u0000\u0000\u0000\u0101\u0106\u0003\u00aeW\u0000\u0102\u0103\u0005\\"+
		"\u0000\u0000\u0103\u0105\u0003\u00aeW\u0000\u0104\u0102\u0001\u0000\u0000"+
		"\u0000\u0105\u0108\u0001\u0000\u0000\u0000\u0106\u0104\u0001\u0000\u0000"+
		"\u0000\u0106\u0107\u0001\u0000\u0000\u0000\u0107\r\u0001\u0000\u0000\u0000"+
		"\u0108\u0106\u0001\u0000\u0000\u0000\u0109\u010b\u0003\u00b8\\\u0000\u010a"+
		"\u0109\u0001\u0000\u0000\u0000\u010b\u010e\u0001\u0000\u0000\u0000\u010c"+
		"\u010a\u0001\u0000\u0000\u0000\u010c\u010d\u0001\u0000\u0000\u0000\u010d"+
		"\u010f\u0001\u0000\u0000\u0000\u010e\u010c\u0001\u0000\u0000\u0000\u010f"+
		"\u0112\u0003&\u0013\u0000\u0110\u0112\u0003\u0010\b\u0000\u0111\u010c"+
		"\u0001\u0000\u0000\u0000\u0111\u0110\u0001\u0000\u0000\u0000\u0112\u000f"+
		"\u0001\u0000\u0000\u0000\u0113\u0114\u0005 \u0000\u0000\u0114\u0115\u0003"+
		">\u001f\u0000\u0115\u0011\u0001\u0000\u0000\u0000\u0116\u0118\u0003\u00c2"+
		"a\u0000\u0117\u0116\u0001\u0000\u0000\u0000\u0118\u011b\u0001\u0000\u0000"+
		"\u0000\u0119\u0117\u0001\u0000\u0000\u0000\u0119\u011a\u0001\u0000\u0000"+
		"\u0000\u011a\u011c\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000"+
		"\u0000\u011c\u011d\u0005\u000f\u0000\u0000\u011d\u0120\u0003\u00a2Q\u0000"+
		"\u011e\u011f\u00059\u0000\u0000\u011f\u0121\u0003\f\u0006\u0000\u0120"+
		"\u011e\u0001\u0000\u0000\u0000\u0120\u0121\u0001\u0000\u0000\u0000\u0121"+
		"\u0122\u0001\u0000\u0000\u0000\u0122\u0124\u0005W\u0000\u0000\u0123\u0125"+
		"\u0003\u0014\n\u0000\u0124\u0123\u0001\u0000\u0000\u0000\u0124\u0125\u0001"+
		"\u0000\u0000\u0000\u0125\u0127\u0001\u0000\u0000\u0000\u0126\u0128\u0005"+
		"\\\u0000\u0000\u0127\u0126\u0001\u0000\u0000\u0000\u0127\u0128\u0001\u0000"+
		"\u0000\u0000\u0128\u012a\u0001\u0000\u0000\u0000\u0129\u012b\u0003\u0018"+
		"\f\u0000\u012a\u0129\u0001\u0000\u0000\u0000\u012a\u012b\u0001\u0000\u0000"+
		"\u0000\u012b\u012c\u0001\u0000\u0000\u0000\u012c\u012d\u0005X\u0000\u0000"+
		"\u012d\u0013\u0001\u0000\u0000\u0000\u012e\u0133\u0003\u0016\u000b\u0000"+
		"\u012f\u0130\u0005\\\u0000\u0000\u0130\u0132\u0003\u0016\u000b\u0000\u0131"+
		"\u012f\u0001\u0000\u0000\u0000\u0132\u0135\u0001\u0000\u0000\u0000\u0133"+
		"\u0131\u0001\u0000\u0000\u0000\u0133\u0134\u0001\u0000\u0000\u0000\u0134"+
		"\u0015\u0001\u0000\u0000\u0000\u0135\u0133\u0001\u0000\u0000\u0000\u0136"+
		"\u0138\u0003\u00a2Q\u0000\u0137\u0139\u0003\u00a0P\u0000\u0138\u0137\u0001"+
		"\u0000\u0000\u0000\u0138\u0139\u0001\u0000\u0000\u0000\u0139\u0017\u0001"+
		"\u0000\u0000\u0000\u013a\u013e\u0005[\u0000\u0000\u013b\u013d\u0003\u000e"+
		"\u0007\u0000\u013c\u013b\u0001\u0000\u0000\u0000\u013d\u0140\u0001\u0000"+
		"\u0000\u0000\u013e\u013c\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000"+
		"\u0000\u0000\u013f\u0019\u0001\u0000\u0000\u0000\u0140\u013e\u0001\u0000"+
		"\u0000\u0000\u0141\u0143\u0003\u00c2a\u0000\u0142\u0141\u0001\u0000\u0000"+
		"\u0000\u0143\u0146\u0001\u0000\u0000\u0000\u0144\u0142\u0001\u0000\u0000"+
		"\u0000\u0144\u0145\u0001\u0000\u0000\u0000\u0145\u0147\u0001\u0000\u0000"+
		"\u0000\u0146\u0144\u0001\u0000\u0000\u0000\u0147\u0148\u0005\u0019\u0000"+
		"\u0000\u0148\u014a\u0003\u00a2Q\u0000\u0149\u014b\u0003.\u0017\u0000\u014a"+
		"\u0149\u0001\u0000\u0000\u0000\u014a\u014b\u0001\u0000\u0000\u0000\u014b"+
		"\u014e\u0001\u0000\u0000\u0000\u014c\u014d\u00059\u0000\u0000\u014d\u014f"+
		"\u0003\f\u0006\u0000\u014e\u014c\u0001\u0000\u0000\u0000\u014e\u014f\u0001"+
		"\u0000\u0000\u0000\u014f\u0150\u0001\u0000\u0000\u0000\u0150\u0151\u0003"+
		"\u001c\u000e\u0000\u0151\u001b\u0001\u0000\u0000\u0000\u0152\u0156\u0005"+
		"W\u0000\u0000\u0153\u0155\u0003\u001e\u000f\u0000\u0154\u0153\u0001\u0000"+
		"\u0000\u0000\u0155\u0158\u0001\u0000\u0000\u0000\u0156\u0154\u0001\u0000"+
		"\u0000\u0000\u0156\u0157\u0001\u0000\u0000\u0000\u0157\u0159\u0001\u0000"+
		"\u0000\u0000\u0158\u0156\u0001\u0000\u0000\u0000\u0159\u015a\u0005X\u0000"+
		"\u0000\u015a\u001d\u0001\u0000\u0000\u0000\u015b\u015d\u0003\u00b8\\\u0000"+
		"\u015c\u015b\u0001\u0000\u0000\u0000\u015d\u0160\u0001\u0000\u0000\u0000"+
		"\u015e\u015c\u0001\u0000\u0000\u0000\u015e\u015f\u0001\u0000\u0000\u0000"+
		"\u015f\u0161\u0001\u0000\u0000\u0000\u0160\u015e\u0001\u0000\u0000\u0000"+
		"\u0161\u0162\u0003 \u0010\u0000\u0162\u001f\u0001\u0000\u0000\u0000\u0163"+
		"\u0164\u0003\"\u0011\u0000\u0164!\u0001\u0000\u0000\u0000\u0165\u0167"+
		"\u0003$\u0012\u0000\u0166\u0165\u0001\u0000\u0000\u0000\u0167\u016a\u0001"+
		"\u0000\u0000\u0000\u0168\u0166\u0001\u0000\u0000\u0000\u0168\u0169\u0001"+
		"\u0000\u0000\u0000\u0169\u016b\u0001\u0000\u0000\u0000\u016a\u0168\u0001"+
		"\u0000\u0000\u0000\u016b\u016c\u0005,\u0000\u0000\u016c\u016e\u0003\u00a2"+
		"Q\u0000\u016d\u016f\u0003.\u0017\u0000\u016e\u016d\u0001\u0000\u0000\u0000"+
		"\u016e\u016f\u0001\u0000\u0000\u0000\u016f\u0170\u0001\u0000\u0000\u0000"+
		"\u0170\u0173\u00036\u001b\u0000\u0171\u0172\u0005^\u0000\u0000\u0172\u0174"+
		"\u0003\u00acV\u0000\u0173\u0171\u0001\u0000\u0000\u0000\u0173\u0174\u0001"+
		"\u0000\u0000\u0000\u0174\u0177\u0001\u0000\u0000\u0000\u0175\u0176\u0005"+
		"%\u0000\u0000\u0176\u0178\u00030\u0018\u0000\u0177\u0175\u0001\u0000\u0000"+
		"\u0000\u0177\u0178\u0001\u0000\u0000\u0000\u0178#\u0001\u0000\u0000\u0000"+
		"\u0179\u017a\u0007\u0000\u0000\u0000\u017a%\u0001\u0000\u0000\u0000\u017b"+
		"\u0180\u0003*\u0015\u0000\u017c\u0180\u0003(\u0014\u0000\u017d\u0180\u0003"+
		",\u0016\u0000\u017e\u0180\u0003\b\u0004\u0000\u017f\u017b\u0001\u0000"+
		"\u0000\u0000\u017f\u017c\u0001\u0000\u0000\u0000\u017f\u017d\u0001\u0000"+
		"\u0000\u0000\u017f\u017e\u0001\u0000\u0000\u0000\u0180\'\u0001\u0000\u0000"+
		"\u0000\u0181\u0182\u0007\u0001\u0000\u0000\u0182\u0183\u0003\u00a2Q\u0000"+
		"\u0183\u0184\u00059\u0000\u0000\u0184\u0185\u0003\u00aeW\u0000\u0185)"+
		"\u0001\u0000\u0000\u0000\u0186\u0187\u0005,\u0000\u0000\u0187\u0189\u0003"+
		"\u00a2Q\u0000\u0188\u018a\u0003.\u0017\u0000\u0189\u0188\u0001\u0000\u0000"+
		"\u0000\u0189\u018a\u0001\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000"+
		"\u0000\u018b\u018e\u00036\u001b\u0000\u018c\u018d\u0005^\u0000\u0000\u018d"+
		"\u018f\u0003\u00acV\u0000\u018e\u018c\u0001\u0000\u0000\u0000\u018e\u018f"+
		"\u0001\u0000\u0000\u0000\u018f\u0191\u0001\u0000\u0000\u0000\u0190\u0192"+
		"\u0003<\u001e\u0000\u0191\u0190\u0001\u0000\u0000\u0000\u0191\u0192\u0001"+
		"\u0000\u0000\u0000\u0192+\u0001\u0000\u0000\u0000\u0193\u0194\u00051\u0000"+
		"\u0000\u0194\u0197\u00036\u001b\u0000\u0195\u0196\u0005%\u0000\u0000\u0196"+
		"\u0198\u00030\u0018\u0000\u0197\u0195\u0001\u0000\u0000\u0000\u0197\u0198"+
		"\u0001\u0000\u0000\u0000\u0198\u0199\u0001\u0000\u0000\u0000\u0199\u019a"+
		"\u0003>\u001f\u0000\u019a-\u0001\u0000\u0000\u0000\u019b\u019c\u00054"+
		"\u0000\u0000\u019c\u01a1\u00034\u001a\u0000\u019d\u019e\u0005\\\u0000"+
		"\u0000\u019e\u01a0\u00034\u001a\u0000\u019f\u019d\u0001\u0000\u0000\u0000"+
		"\u01a0\u01a3\u0001\u0000\u0000\u0000\u01a1\u019f\u0001\u0000\u0000\u0000"+
		"\u01a1\u01a2\u0001\u0000\u0000\u0000\u01a2\u01a4\u0001\u0000\u0000\u0000"+
		"\u01a3\u01a1\u0001\u0000\u0000\u0000\u01a4\u01a5\u00053\u0000\u0000\u01a5"+
		"/\u0001\u0000\u0000\u0000\u01a6\u01ab\u00032\u0019\u0000\u01a7\u01a8\u0005"+
		"\\\u0000\u0000\u01a8\u01aa\u00032\u0019\u0000\u01a9\u01a7\u0001\u0000"+
		"\u0000\u0000\u01aa\u01ad\u0001\u0000\u0000\u0000\u01ab\u01a9\u0001\u0000"+
		"\u0000\u0000\u01ab\u01ac\u0001\u0000\u0000\u0000\u01ac1\u0001\u0000\u0000"+
		"\u0000\u01ad\u01ab\u0001\u0000\u0000\u0000\u01ae\u01b3\u0003\u00a2Q\u0000"+
		"\u01af\u01b0\u0005]\u0000\u0000\u01b0\u01b2\u0003\u00a2Q\u0000\u01b1\u01af"+
		"\u0001\u0000\u0000\u0000\u01b2\u01b5\u0001\u0000\u0000\u0000\u01b3\u01b1"+
		"\u0001\u0000\u0000\u0000\u01b3\u01b4\u0001\u0000\u0000\u0000\u01b43\u0001"+
		"\u0000\u0000\u0000\u01b5\u01b3\u0001\u0000\u0000\u0000\u01b6\u01b9\u0003"+
		"\u00a2Q\u0000\u01b7\u01b8\u00059\u0000\u0000\u01b8\u01ba\u0003\u00aeW"+
		"\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001\u0000\u0000"+
		"\u0000\u01ba5\u0001\u0000\u0000\u0000\u01bb\u01bd\u0005U\u0000\u0000\u01bc"+
		"\u01be\u00038\u001c\u0000\u01bd\u01bc\u0001\u0000\u0000\u0000\u01bd\u01be"+
		"\u0001\u0000\u0000\u0000\u01be\u01bf\u0001\u0000\u0000\u0000\u01bf\u01c0"+
		"\u0005V\u0000\u0000\u01c07\u0001\u0000\u0000\u0000\u01c1\u01c6\u0003:"+
		"\u001d\u0000\u01c2\u01c3\u0005\\\u0000\u0000\u01c3\u01c5\u0003:\u001d"+
		"\u0000\u01c4\u01c2\u0001\u0000\u0000\u0000\u01c5\u01c8\u0001\u0000\u0000"+
		"\u0000\u01c6\u01c4\u0001\u0000\u0000\u0000\u01c6\u01c7\u0001\u0000\u0000"+
		"\u0000\u01c79\u0001\u0000\u0000\u0000\u01c8\u01c6\u0001\u0000\u0000\u0000"+
		"\u01c9\u01ca\u0003\u00a2Q\u0000\u01ca\u01cb\u00059\u0000\u0000\u01cb\u01cc"+
		"\u0003\u00aeW\u0000\u01cc;\u0001\u0000\u0000\u0000\u01cd\u01ce\u0003>"+
		"\u001f\u0000\u01ce=\u0001\u0000\u0000\u0000\u01cf\u01d3\u0005W\u0000\u0000"+
		"\u01d0\u01d2\u0003@ \u0000\u01d1\u01d0\u0001\u0000\u0000\u0000\u01d2\u01d5"+
		"\u0001\u0000\u0000\u0000\u01d3\u01d1\u0001\u0000\u0000\u0000\u01d3\u01d4"+
		"\u0001\u0000\u0000\u0000\u01d4\u01d6\u0001\u0000\u0000\u0000\u01d5\u01d3"+
		"\u0001\u0000\u0000\u0000\u01d6\u01d7\u0005X\u0000\u0000\u01d7?\u0001\u0000"+
		"\u0000\u0000\u01d8\u01d9\u0005(\u0000\u0000\u01d9\u01da\u0003b1\u0000"+
		"\u01da\u01db\u0003>\u001f\u0000\u01db\u0200\u0001\u0000\u0000\u0000\u01dc"+
		"\u01dd\u0005\u0011\u0000\u0000\u01dd\u01de\u0005U\u0000\u0000\u01de\u01df"+
		"\u0003D\"\u0000\u01df\u01e0\u0005V\u0000\u0000\u01e0\u01e1\u0003>\u001f"+
		"\u0000\u01e1\u0200\u0001\u0000\u0000\u0000\u01e2\u01e3\u0005\u0012\u0000"+
		"\u0000\u01e3\u01e4\u0003b1\u0000\u01e4\u01e7\u0003>\u001f\u0000\u01e5"+
		"\u01e6\u0005\u000e\u0000\u0000\u01e6\u01e8\u0003>\u001f\u0000\u01e7\u01e5"+
		"\u0001\u0000\u0000\u0000\u01e7\u01e8\u0001\u0000\u0000\u0000\u01e8\u0200"+
		"\u0001\u0000\u0000\u0000\u01e9\u01ea\u0005&\u0000\u0000\u01ea\u01eb\u0003"+
		">\u001f\u0000\u01eb\u01ec\u0003V+\u0000\u01ec\u0200\u0001\u0000\u0000"+
		"\u0000\u01ed\u01ee\u0005\"\u0000\u0000\u01ee\u01f2\u0005W\u0000\u0000"+
		"\u01ef\u01f1\u0003^/\u0000\u01f0\u01ef\u0001\u0000\u0000\u0000\u01f1\u01f4"+
		"\u0001\u0000\u0000\u0000\u01f2\u01f0\u0001\u0000\u0000\u0000\u01f2\u01f3"+
		"\u0001\u0000\u0000\u0000\u01f3\u01f5\u0001\u0000\u0000\u0000\u01f4\u01f2"+
		"\u0001\u0000\u0000\u0000\u01f5\u0200\u0005X\u0000\u0000\u01f6\u01f8\u0005"+
		"\u001f\u0000\u0000\u01f7\u01f9\u0003f3\u0000\u01f8\u01f7\u0001\u0000\u0000"+
		"\u0000\u01f8\u01f9\u0001\u0000\u0000\u0000\u01f9\u0200\u0001\u0000\u0000"+
		"\u0000\u01fa\u01fb\u0005$\u0000\u0000\u01fb\u0200\u0003f3\u0000\u01fc"+
		"\u0200\u0005[\u0000\u0000\u01fd\u0200\u0003f3\u0000\u01fe\u0200\u0003"+
		"B!\u0000\u01ff\u01d8\u0001\u0000\u0000\u0000\u01ff\u01dc\u0001\u0000\u0000"+
		"\u0000\u01ff\u01e2\u0001\u0000\u0000\u0000\u01ff\u01e9\u0001\u0000\u0000"+
		"\u0000\u01ff\u01ed\u0001\u0000\u0000\u0000\u01ff\u01f6\u0001\u0000\u0000"+
		"\u0000\u01ff\u01fa\u0001\u0000\u0000\u0000\u01ff\u01fc\u0001\u0000\u0000"+
		"\u0000\u01ff\u01fd\u0001\u0000\u0000\u0000\u01ff\u01fe\u0001\u0000\u0000"+
		"\u0000\u0200A\u0001\u0000\u0000\u0000\u0201\u0202\u0007\u0001\u0000\u0000"+
		"\u0202\u0205\u0003\u00a2Q\u0000\u0203\u0204\u00059\u0000\u0000\u0204\u0206"+
		"\u0003\u00aeW\u0000\u0205\u0203\u0001\u0000\u0000\u0000\u0205\u0206\u0001"+
		"\u0000\u0000\u0000\u0206\u0209\u0001\u0000\u0000\u0000\u0207\u0208\u0005"+
		"2\u0000\u0000\u0208\u020a\u0003f3\u0000\u0209\u0207\u0001\u0000\u0000"+
		"\u0000\u0209\u020a\u0001\u0000\u0000\u0000\u020aC\u0001\u0000\u0000\u0000"+
		"\u020b\u020d\u0003F#\u0000\u020c\u020b\u0001\u0000\u0000\u0000\u020c\u020d"+
		"\u0001\u0000\u0000\u0000\u020d\u020e\u0001\u0000\u0000\u0000\u020e\u0210"+
		"\u0005[\u0000\u0000\u020f\u0211\u0003f3\u0000\u0210\u020f\u0001\u0000"+
		"\u0000\u0000\u0210\u0211\u0001\u0000\u0000\u0000\u0211\u0212\u0001\u0000"+
		"\u0000\u0000\u0212\u0214\u0005[\u0000\u0000\u0213\u0215\u0003J%\u0000"+
		"\u0214\u0213\u0001\u0000\u0000\u0000\u0214\u0215\u0001\u0000\u0000\u0000"+
		"\u0215E\u0001\u0000\u0000\u0000\u0216\u021b\u0003H$\u0000\u0217\u0218"+
		"\u0005\\\u0000\u0000\u0218\u021a\u0003H$\u0000\u0219\u0217\u0001\u0000"+
		"\u0000\u0000\u021a\u021d\u0001\u0000\u0000\u0000\u021b\u0219\u0001\u0000"+
		"\u0000\u0000\u021b\u021c\u0001\u0000\u0000\u0000\u021cG\u0001\u0000\u0000"+
		"\u0000\u021d\u021b\u0001\u0000\u0000\u0000\u021e\u021f\u0003\u00aeW\u0000"+
		"\u021f\u0220\u0003\u00a2Q\u0000\u0220\u0221\u00052\u0000\u0000\u0221\u0222"+
		"\u0003f3\u0000\u0222I\u0001\u0000\u0000\u0000\u0223\u0228\u0003L&\u0000"+
		"\u0224\u0225\u0005\\\u0000\u0000\u0225\u0227\u0003L&\u0000\u0226\u0224"+
		"\u0001\u0000\u0000\u0000\u0227\u022a\u0001\u0000\u0000\u0000\u0228\u0226"+
		"\u0001\u0000\u0000\u0000\u0228\u0229\u0001\u0000\u0000\u0000\u0229K\u0001"+
		"\u0000\u0000\u0000\u022a\u0228\u0001\u0000\u0000\u0000\u022b\u022c\u0003"+
		"\u00a2Q\u0000\u022c\u022d\u00052\u0000\u0000\u022d\u022e\u0003f3\u0000"+
		"\u022eM\u0001\u0000\u0000\u0000\u022f\u0231\u0005\u001b\u0000\u0000\u0230"+
		"\u0232\u0003\u00b4Z\u0000\u0231\u0230\u0001\u0000\u0000\u0000\u0231\u0232"+
		"\u0001\u0000\u0000\u0000\u0232\u0233\u0001\u0000\u0000\u0000\u0233\u0234"+
		"\u0003\u00b2Y\u0000\u0234\u0235\u0003\u00a0P\u0000\u0235O\u0001\u0000"+
		"\u0000\u0000\u0236\u0237\u0005\u001b\u0000\u0000\u0237\u0238\u0003\u00ae"+
		"W\u0000\u0238\u0239\u0003\u00b0X\u0000\u0239Q\u0001\u0000\u0000\u0000"+
		"\u023a\u0246\u0005W\u0000\u0000\u023b\u0240\u0003T*\u0000\u023c\u023d"+
		"\u0005\\\u0000\u0000\u023d\u023f\u0003T*\u0000\u023e\u023c\u0001\u0000"+
		"\u0000\u0000\u023f\u0242\u0001\u0000\u0000\u0000\u0240\u023e\u0001\u0000"+
		"\u0000\u0000\u0240\u0241\u0001\u0000\u0000\u0000\u0241\u0244\u0001\u0000"+
		"\u0000\u0000\u0242\u0240\u0001\u0000\u0000\u0000\u0243\u0245\u0005\\\u0000"+
		"\u0000\u0244\u0243\u0001\u0000\u0000\u0000\u0244\u0245\u0001\u0000\u0000"+
		"\u0000\u0245\u0247\u0001\u0000\u0000\u0000\u0246\u023b\u0001\u0000\u0000"+
		"\u0000\u0246\u0247\u0001\u0000\u0000\u0000\u0247\u0248\u0001\u0000\u0000"+
		"\u0000\u0248\u0249\u0005X\u0000\u0000\u0249S\u0001\u0000\u0000\u0000\u024a"+
		"\u024d\u0003R)\u0000\u024b\u024d\u0003f3\u0000\u024c\u024a\u0001\u0000"+
		"\u0000\u0000\u024c\u024b\u0001\u0000\u0000\u0000\u024dU\u0001\u0000\u0000"+
		"\u0000\u024e\u024f\u0005\u0005\u0000\u0000\u024f\u0251\u0005W\u0000\u0000"+
		"\u0250\u0252\u0003X,\u0000\u0251\u0250\u0001\u0000\u0000\u0000\u0251\u0252"+
		"\u0001\u0000\u0000\u0000\u0252\u0253\u0001\u0000\u0000\u0000\u0253\u0254"+
		"\u0005X\u0000\u0000\u0254W\u0001\u0000\u0000\u0000\u0255\u025a\u0003Z"+
		"-\u0000\u0256\u0257\u0005\\\u0000\u0000\u0257\u0259\u0003Z-\u0000\u0258"+
		"\u0256\u0001\u0000\u0000\u0000\u0259\u025c\u0001\u0000\u0000\u0000\u025a"+
		"\u0258\u0001\u0000\u0000\u0000\u025a\u025b\u0001\u0000\u0000\u0000\u025b"+
		"Y\u0001\u0000\u0000\u0000\u025c\u025a\u0001\u0000\u0000\u0000\u025d\u025e"+
		"\u0003\u00a2Q\u0000\u025e\u025f\u00059\u0000\u0000\u025f\u0265\u0005W"+
		"\u0000\u0000\u0260\u0261\u0003\\.\u0000\u0261\u0262\u0005\\\u0000\u0000"+
		"\u0262\u0264\u0001\u0000\u0000\u0000\u0263\u0260\u0001\u0000\u0000\u0000"+
		"\u0264\u0267\u0001\u0000\u0000\u0000\u0265\u0263\u0001\u0000\u0000\u0000"+
		"\u0265\u0266\u0001\u0000\u0000\u0000\u0266\u0268\u0001\u0000\u0000\u0000"+
		"\u0267\u0265\u0001\u0000\u0000\u0000\u0268\u0269\u0005\u0004\u0000\u0000"+
		"\u0269\u026a\u00059\u0000\u0000\u026a\u026b\u0003f3\u0000\u026b\u026c"+
		"\u0005X\u0000\u0000\u026c[\u0001\u0000\u0000\u0000\u026d\u026e\u0003\u00a2"+
		"Q\u0000\u026e\u026f\u00059\u0000\u0000\u026f\u0270\u0003f3\u0000\u0270"+
		"]\u0001\u0000\u0000\u0000\u0271\u0272\u0003`0\u0000\u0272\u0273\u0003"+
		">\u001f\u0000\u0273_\u0001\u0000\u0000\u0000\u0274\u0275\u0005\u0003\u0000"+
		"\u0000\u0275\u0276\u0003f3\u0000\u0276\u0277\u0005^\u0000\u0000\u0277"+
		"\u027b\u0001\u0000\u0000\u0000\u0278\u0279\u0005\u0004\u0000\u0000\u0279"+
		"\u027b\u0005^\u0000\u0000\u027a\u0274\u0001\u0000\u0000\u0000\u027a\u0278"+
		"\u0001\u0000\u0000\u0000\u027ba\u0001\u0000\u0000\u0000\u027c\u027d\u0005"+
		"U\u0000\u0000\u027d\u027e\u0003f3\u0000\u027e\u027f\u0005V\u0000\u0000"+
		"\u027fc\u0001\u0000\u0000\u0000\u0280\u0285\u0003f3\u0000\u0281\u0282"+
		"\u0005\\\u0000\u0000\u0282\u0284\u0003f3\u0000\u0283\u0281\u0001\u0000"+
		"\u0000\u0000\u0284\u0287\u0001\u0000\u0000\u0000\u0285\u0283\u0001\u0000"+
		"\u0000\u0000\u0285\u0286\u0001\u0000\u0000\u0000\u0286e\u0001\u0000\u0000"+
		"\u0000\u0287\u0285\u0001\u0000\u0000\u0000\u0288\u0289\u0003h4\u0000\u0289"+
		"g\u0001\u0000\u0000\u0000\u028a\u028e\u0003l6\u0000\u028b\u028d\u0003"+
		"j5\u0000\u028c\u028b\u0001\u0000\u0000\u0000\u028d\u0290\u0001\u0000\u0000"+
		"\u0000\u028e\u028c\u0001\u0000\u0000\u0000\u028e\u028f\u0001\u0000\u0000"+
		"\u0000\u028fi\u0001\u0000\u0000\u0000\u0290\u028e\u0001\u0000\u0000\u0000"+
		"\u0291\u0292\u0007\u0002\u0000\u0000\u0292\u0293\u0003h4\u0000\u0293k"+
		"\u0001\u0000\u0000\u0000\u0294\u029a\u0003n7\u0000\u0295\u0296\u00058"+
		"\u0000\u0000\u0296\u0297\u0003n7\u0000\u0297\u0298\u00059\u0000\u0000"+
		"\u0298\u0299\u0003l6\u0000\u0299\u029b\u0001\u0000\u0000\u0000\u029a\u0295"+
		"\u0001\u0000\u0000\u0000\u029a\u029b\u0001\u0000\u0000\u0000\u029bm\u0001"+
		"\u0000\u0000\u0000\u029c\u02a1\u0003p8\u0000\u029d\u029e\u0005?\u0000"+
		"\u0000\u029e\u02a0\u0003p8\u0000\u029f\u029d\u0001\u0000\u0000\u0000\u02a0"+
		"\u02a3\u0001\u0000\u0000\u0000\u02a1\u029f\u0001\u0000\u0000\u0000\u02a1"+
		"\u02a2\u0001\u0000\u0000\u0000\u02a2o\u0001\u0000\u0000\u0000\u02a3\u02a1"+
		"\u0001\u0000\u0000\u0000\u02a4\u02a9\u0003r9\u0000\u02a5\u02a6\u0005>"+
		"\u0000\u0000\u02a6\u02a8\u0003r9\u0000\u02a7\u02a5\u0001\u0000\u0000\u0000"+
		"\u02a8\u02ab\u0001\u0000\u0000\u0000\u02a9\u02a7\u0001\u0000\u0000\u0000"+
		"\u02a9\u02aa\u0001\u0000\u0000\u0000\u02aaq\u0001\u0000\u0000\u0000\u02ab"+
		"\u02a9\u0001\u0000\u0000\u0000\u02ac\u02b1\u0003t:\u0000\u02ad\u02ae\u0005"+
		"G\u0000\u0000\u02ae\u02b0\u0003t:\u0000\u02af\u02ad\u0001\u0000\u0000"+
		"\u0000\u02b0\u02b3\u0001\u0000\u0000\u0000\u02b1\u02af\u0001\u0000\u0000"+
		"\u0000\u02b1\u02b2\u0001\u0000\u0000\u0000\u02b2s\u0001\u0000\u0000\u0000"+
		"\u02b3\u02b1\u0001\u0000\u0000\u0000\u02b4\u02b9\u0003v;\u0000\u02b5\u02b6"+
		"\u0005F\u0000\u0000\u02b6\u02b8\u0003v;\u0000\u02b7\u02b5\u0001\u0000"+
		"\u0000\u0000\u02b8\u02bb\u0001\u0000\u0000\u0000\u02b9\u02b7\u0001\u0000"+
		"\u0000\u0000\u02b9\u02ba\u0001\u0000\u0000\u0000\u02bau\u0001\u0000\u0000"+
		"\u0000\u02bb\u02b9\u0001\u0000\u0000\u0000\u02bc\u02c1\u0003x<\u0000\u02bd"+
		"\u02be\u0005H\u0000\u0000\u02be\u02c0\u0003x<\u0000\u02bf\u02bd\u0001"+
		"\u0000\u0000\u0000\u02c0\u02c3\u0001\u0000\u0000\u0000\u02c1\u02bf\u0001"+
		"\u0000\u0000\u0000\u02c1\u02c2\u0001\u0000\u0000\u0000\u02c2w\u0001\u0000"+
		"\u0000\u0000\u02c3\u02c1\u0001\u0000\u0000\u0000\u02c4\u02c8\u0003|>\u0000"+
		"\u02c5\u02c7\u0003z=\u0000\u02c6\u02c5\u0001\u0000\u0000\u0000\u02c7\u02ca"+
		"\u0001\u0000\u0000\u0000\u02c8\u02c6\u0001\u0000\u0000\u0000\u02c8\u02c9"+
		"\u0001\u0000\u0000\u0000\u02c9y\u0001\u0000\u0000\u0000\u02ca\u02c8\u0001"+
		"\u0000\u0000\u0000\u02cb\u02cc\u0007\u0003\u0000\u0000\u02cc\u02cd\u0003"+
		"|>\u0000\u02cd{\u0001\u0000\u0000\u0000\u02ce\u02d2\u0003\u0080@\u0000"+
		"\u02cf\u02d1\u0003~?\u0000\u02d0\u02cf\u0001\u0000\u0000\u0000\u02d1\u02d4"+
		"\u0001\u0000\u0000\u0000\u02d2\u02d0\u0001\u0000\u0000\u0000\u02d2\u02d3"+
		"\u0001\u0000\u0000\u0000\u02d3}\u0001\u0000\u0000\u0000\u02d4\u02d2\u0001"+
		"\u0000\u0000\u0000\u02d5\u02d6\u0007\u0004\u0000\u0000\u02d6\u02d7\u0003"+
		"\u0080@\u0000\u02d7\u007f\u0001\u0000\u0000\u0000\u02d8\u02dd\u0003\u0082"+
		"A\u0000\u02d9\u02da\u0005\u0013\u0000\u0000\u02da\u02dc\u0003\u00aeW\u0000"+
		"\u02db\u02d9\u0001\u0000\u0000\u0000\u02dc\u02df\u0001\u0000\u0000\u0000"+
		"\u02dd\u02db\u0001\u0000\u0000\u0000\u02dd\u02de\u0001\u0000\u0000\u0000"+
		"\u02de\u0081\u0001\u0000\u0000\u0000\u02df\u02dd\u0001\u0000\u0000\u0000"+
		"\u02e0\u02e4\u0003\u0086C\u0000\u02e1\u02e3\u0003\u0084B\u0000\u02e2\u02e1"+
		"\u0001\u0000\u0000\u0000\u02e3\u02e6\u0001\u0000\u0000\u0000\u02e4\u02e2"+
		"\u0001\u0000\u0000\u0000\u02e4\u02e5\u0001\u0000\u0000\u0000\u02e5\u0083"+
		"\u0001\u0000\u0000\u0000\u02e6\u02e4\u0001\u0000\u0000\u0000\u02e7\u02e8"+
		"\u00053\u0000\u0000\u02e8\u02f0\u00053\u0000\u0000\u02e9\u02ea\u00054"+
		"\u0000\u0000\u02ea\u02f0\u00054\u0000\u0000\u02eb\u02ec\u00053\u0000\u0000"+
		"\u02ec\u02ed\u00053\u0000\u0000\u02ed\u02ee\u00053\u0000\u0000\u02ee\u02f0"+
		"\u0003\u0086C\u0000\u02ef\u02e7\u0001\u0000\u0000\u0000\u02ef\u02e9\u0001"+
		"\u0000\u0000\u0000\u02ef\u02eb\u0001\u0000\u0000\u0000\u02f0\u0085\u0001"+
		"\u0000\u0000\u0000\u02f1\u02f5\u0003\u008aE\u0000\u02f2\u02f4\u0003\u0088"+
		"D\u0000\u02f3\u02f2\u0001\u0000\u0000\u0000\u02f4\u02f7\u0001\u0000\u0000"+
		"\u0000\u02f5\u02f3\u0001\u0000\u0000\u0000\u02f5\u02f6\u0001\u0000\u0000"+
		"\u0000\u02f6\u0087\u0001\u0000\u0000\u0000\u02f7\u02f5\u0001\u0000\u0000"+
		"\u0000\u02f8\u02f9\u0007\u0005\u0000\u0000\u02f9\u02fa\u0003\u008aE\u0000"+
		"\u02fa\u0089\u0001\u0000\u0000\u0000\u02fb\u02ff\u0003\u008eG\u0000\u02fc"+
		"\u02fe\u0003\u008cF\u0000\u02fd\u02fc\u0001\u0000\u0000\u0000\u02fe\u0301"+
		"\u0001\u0000\u0000\u0000\u02ff\u02fd\u0001\u0000\u0000\u0000\u02ff\u0300"+
		"\u0001\u0000\u0000\u0000\u0300\u008b\u0001\u0000\u0000\u0000\u0301\u02ff"+
		"\u0001\u0000\u0000\u0000\u0302\u0303\u0007\u0006\u0000\u0000\u0303\u0304"+
		"\u0003\u008eG\u0000\u0304\u008d\u0001\u0000\u0000\u0000\u0305\u030a\u0003"+
		"\u0090H\u0000\u0306\u0307\u00050\u0000\u0000\u0307\u0309\u0003\u00aeW"+
		"\u0000\u0308\u0306\u0001\u0000\u0000\u0000\u0309\u030c\u0001\u0000\u0000"+
		"\u0000\u030a\u0308\u0001\u0000\u0000\u0000\u030a\u030b\u0001\u0000\u0000"+
		"\u0000\u030b\u008f\u0001\u0000\u0000\u0000\u030c\u030a\u0001\u0000\u0000"+
		"\u0000\u030d\u030f\u0003\u0092I\u0000\u030e\u030d\u0001\u0000\u0000\u0000"+
		"\u030f\u0312\u0001\u0000\u0000\u0000\u0310\u030e\u0001\u0000\u0000\u0000"+
		"\u0310\u0311\u0001\u0000\u0000\u0000\u0311\u0313\u0001\u0000\u0000\u0000"+
		"\u0312\u0310\u0001\u0000\u0000\u0000\u0313\u0314\u0003\u0094J\u0000\u0314"+
		"\u0091\u0001\u0000\u0000\u0000\u0315\u0316\u0007\u0007\u0000\u0000\u0316"+
		"\u0093\u0001\u0000\u0000\u0000\u0317\u031b\u0003\u009eO\u0000\u0318\u031a"+
		"\u0003\u0096K\u0000\u0319\u0318\u0001\u0000\u0000\u0000\u031a\u031d\u0001"+
		"\u0000\u0000\u0000\u031b\u0319\u0001\u0000\u0000\u0000\u031b\u031c\u0001"+
		"\u0000\u0000\u0000\u031c\u0095\u0001\u0000\u0000\u0000\u031d\u031b\u0001"+
		"\u0000\u0000\u0000\u031e\u0325\u0007\b\u0000\u0000\u031f\u0325\u0003\u0098"+
		"L\u0000\u0320\u0321\u0005]\u0000\u0000\u0321\u0325\u0003N\'\u0000\u0322"+
		"\u0325\u0003\u009aM\u0000\u0323\u0325\u0003\u009cN\u0000\u0324\u031e\u0001"+
		"\u0000\u0000\u0000\u0324\u031f\u0001\u0000\u0000\u0000\u0324\u0320\u0001"+
		"\u0000\u0000\u0000\u0324\u0322\u0001\u0000\u0000\u0000\u0324\u0323\u0001"+
		"\u0000\u0000\u0000\u0325\u0097\u0001\u0000\u0000\u0000\u0326\u0328\u0003"+
		"\u00b4Z\u0000\u0327\u0326\u0001\u0000\u0000\u0000\u0327\u0328\u0001\u0000"+
		"\u0000\u0000\u0328\u0329\u0001\u0000\u0000\u0000\u0329\u032a\u0003\u00a0"+
		"P\u0000\u032a\u0099\u0001\u0000\u0000\u0000\u032b\u032c\u0005Y\u0000\u0000"+
		"\u032c\u032d\u0003f3\u0000\u032d\u032e\u0005Z\u0000\u0000\u032e\u009b"+
		"\u0001\u0000\u0000\u0000\u032f\u0330\u0005]\u0000\u0000\u0330\u0331\u0003"+
		"\u00a2Q\u0000\u0331\u009d\u0001\u0000\u0000\u0000\u0332\u0333\u0005U\u0000"+
		"\u0000\u0333\u0334\u0003f3\u0000\u0334\u0335\u0005V\u0000\u0000\u0335"+
		"\u033f\u0001\u0000\u0000\u0000\u0336\u033f\u0005#\u0000\u0000\u0337\u033f"+
		"\u0005!\u0000\u0000\u0338\u033f\u0003N\'\u0000\u0339\u033f\u0003P(\u0000"+
		"\u033a\u033f\u0003\u00a6S\u0000\u033b\u033f\u0003\u00a2Q\u0000\u033c\u033f"+
		"\u0003\u00bc^\u0000\u033d\u033f\u0005\u001b\u0000\u0000\u033e\u0332\u0001"+
		"\u0000\u0000\u0000\u033e\u0336\u0001\u0000\u0000\u0000\u033e\u0337\u0001"+
		"\u0000\u0000\u0000\u033e\u0338\u0001\u0000\u0000\u0000\u033e\u0339\u0001"+
		"\u0000\u0000\u0000\u033e\u033a\u0001\u0000\u0000\u0000\u033e\u033b\u0001"+
		"\u0000\u0000\u0000\u033e\u033c\u0001\u0000\u0000\u0000\u033e\u033d\u0001"+
		"\u0000\u0000\u0000\u033f\u009f\u0001\u0000\u0000\u0000\u0340\u0342\u0005"+
		"U\u0000\u0000\u0341\u0343\u0003d2\u0000\u0342\u0341\u0001\u0000\u0000"+
		"\u0000\u0342\u0343\u0001\u0000\u0000\u0000\u0343\u0344\u0001\u0000\u0000"+
		"\u0000\u0344\u0345\u0005V\u0000\u0000\u0345\u00a1\u0001\u0000\u0000\u0000"+
		"\u0346\u0347\u0007\t\u0000\u0000\u0347\u00a3\u0001\u0000\u0000\u0000\u0348"+
		"\u034a\u0003\u00a2Q\u0000\u0349\u034b\u0003\u00b4Z\u0000\u034a\u0349\u0001"+
		"\u0000\u0000\u0000\u034a\u034b\u0001\u0000\u0000\u0000\u034b\u034c\u0001"+
		"\u0000\u0000\u0000\u034c\u034e\u0005U\u0000\u0000\u034d\u034f\u0003d2"+
		"\u0000\u034e\u034d\u0001\u0000\u0000\u0000\u034e\u034f\u0001\u0000\u0000"+
		"\u0000\u034f\u0350\u0001\u0000\u0000\u0000\u0350\u0351\u0005V\u0000\u0000"+
		"\u0351\u00a5\u0001\u0000\u0000\u0000\u0352\u035a\u0003\u00a8T\u0000\u0353"+
		"\u035a\u0003\u00aaU\u0000\u0354\u035a\u0005i\u0000\u0000\u0355\u035a\u0005"+
		"j\u0000\u0000\u0356\u035a\u0005h\u0000\u0000\u0357\u035a\u0005\t\u0000"+
		"\u0000\u0358\u035a\u0005k\u0000\u0000\u0359\u0352\u0001\u0000\u0000\u0000"+
		"\u0359\u0353\u0001\u0000\u0000\u0000\u0359\u0354\u0001\u0000\u0000\u0000"+
		"\u0359\u0355\u0001\u0000\u0000\u0000\u0359\u0356\u0001\u0000\u0000\u0000"+
		"\u0359\u0357\u0001\u0000\u0000\u0000\u0359\u0358\u0001\u0000\u0000\u0000"+
		"\u035a\u00a7\u0001\u0000\u0000\u0000\u035b\u035c\u0007\n\u0000\u0000\u035c"+
		"\u00a9\u0001\u0000\u0000\u0000\u035d\u035e\u0007\u000b\u0000\u0000\u035e"+
		"\u00ab\u0001\u0000\u0000\u0000\u035f\u0362\u0005\'\u0000\u0000\u0360\u0362"+
		"\u0003\u00aeW\u0000\u0361\u035f\u0001\u0000\u0000\u0000\u0361\u0360\u0001"+
		"\u0000\u0000\u0000\u0362\u00ad\u0001\u0000\u0000\u0000\u0363\u0364\u0006"+
		"W\uffff\uffff\u0000\u0364\u037d\u0003\u00b2Y\u0000\u0365\u037d\u0003\u00b6"+
		"[\u0000\u0366\u037d\u0005)\u0000\u0000\u0367\u037d\u0005*\u0000\u0000"+
		"\u0368\u0371\u0005U\u0000\u0000\u0369\u036e\u0003\u00aeW\u0000\u036a\u036b"+
		"\u0005\\\u0000\u0000\u036b\u036d\u0003\u00aeW\u0000\u036c\u036a\u0001"+
		"\u0000\u0000\u0000\u036d\u0370\u0001\u0000\u0000\u0000\u036e\u036c\u0001"+
		"\u0000\u0000\u0000\u036e\u036f\u0001\u0000\u0000\u0000\u036f\u0372\u0001"+
		"\u0000\u0000\u0000\u0370\u036e\u0001\u0000\u0000\u0000\u0371\u0369\u0001"+
		"\u0000\u0000\u0000\u0371\u0372\u0001\u0000\u0000\u0000\u0372\u0373\u0001"+
		"\u0000\u0000\u0000\u0373\u0374\u0005V\u0000\u0000\u0374\u0375\u0005^\u0000"+
		"\u0000\u0375\u037d\u0003\u00aeW\u0002\u0376\u0377\u0005Y\u0000\u0000\u0377"+
		"\u0378\u0003\u00aeW\u0000\u0378\u0379\u0005\\\u0000\u0000\u0379\u037a"+
		"\u0003\u00aeW\u0000\u037a\u037b\u0005Z\u0000\u0000\u037b\u037d\u0001\u0000"+
		"\u0000\u0000\u037c\u0363\u0001\u0000\u0000\u0000\u037c\u0365\u0001\u0000"+
		"\u0000\u0000\u037c\u0366\u0001\u0000\u0000\u0000\u037c\u0367\u0001\u0000"+
		"\u0000\u0000\u037c\u0368\u0001\u0000\u0000\u0000\u037c\u0376\u0001\u0000"+
		"\u0000\u0000\u037d\u0390\u0001\u0000\u0000\u0000\u037e\u0381\n\u0005\u0000"+
		"\u0000\u037f\u0380\u0005G\u0000\u0000\u0380\u0382\u0003\u00aeW\u0000\u0381"+
		"\u037f\u0001\u0000\u0000\u0000\u0382\u0383\u0001\u0000\u0000\u0000\u0383"+
		"\u0381\u0001\u0000\u0000\u0000\u0383\u0384\u0001\u0000\u0000\u0000\u0384"+
		"\u038f\u0001\u0000\u0000\u0000\u0385\u0388\n\u0004\u0000\u0000\u0386\u0387"+
		"\u0005F\u0000\u0000\u0387\u0389\u0003\u00aeW\u0000\u0388\u0386\u0001\u0000"+
		"\u0000\u0000\u0389\u038a\u0001\u0000\u0000\u0000\u038a\u0388\u0001\u0000"+
		"\u0000\u0000\u038a\u038b\u0001\u0000\u0000\u0000\u038b\u038f\u0001\u0000"+
		"\u0000\u0000\u038c\u038d\n\u0003\u0000\u0000\u038d\u038f\u0003\u00b0X"+
		"\u0000\u038e\u037e\u0001\u0000\u0000\u0000\u038e\u0385\u0001\u0000\u0000"+
		"\u0000\u038e\u038c\u0001\u0000\u0000\u0000\u038f\u0392\u0001\u0000\u0000"+
		"\u0000\u0390\u038e\u0001\u0000\u0000\u0000\u0390\u0391\u0001\u0000\u0000"+
		"\u0000\u0391\u00af\u0001\u0000\u0000\u0000\u0392\u0390\u0001\u0000\u0000"+
		"\u0000\u0393\u0394\u0007\f\u0000\u0000\u0394\u00b1\u0001\u0000\u0000\u0000"+
		"\u0395\u0397\u00032\u0019\u0000\u0396\u0398\u0003\u00b4Z\u0000\u0397\u0396"+
		"\u0001\u0000\u0000\u0000\u0397\u0398\u0001\u0000\u0000\u0000\u0398\u00b3"+
		"\u0001\u0000\u0000\u0000\u0399\u039a\u00054\u0000\u0000\u039a\u039f\u0003"+
		"\u00aeW\u0000\u039b\u039c\u0005\\\u0000\u0000\u039c\u039e\u0003\u00ae"+
		"W\u0000\u039d\u039b\u0001\u0000\u0000\u0000\u039e\u03a1\u0001\u0000\u0000"+
		"\u0000\u039f\u039d\u0001\u0000\u0000\u0000\u039f\u03a0\u0001\u0000\u0000"+
		"\u0000\u03a0\u03a2\u0001\u0000\u0000\u0000\u03a1\u039f\u0001\u0000\u0000"+
		"\u0000\u03a2\u03a3\u00053\u0000\u0000\u03a3\u00b5\u0001\u0000\u0000\u0000"+
		"\u03a4\u03a5\u0007\r\u0000\u0000\u03a5\u00b7\u0001\u0000\u0000\u0000\u03a6"+
		"\u03aa\u0003\u00ba]\u0000\u03a7\u03aa\u0005\u001a\u0000\u0000\u03a8\u03aa"+
		"\u0005+\u0000\u0000\u03a9\u03a6\u0001\u0000\u0000\u0000\u03a9\u03a7\u0001"+
		"\u0000\u0000\u0000\u03a9\u03a8\u0001\u0000\u0000\u0000\u03aa\u00b9\u0001"+
		"\u0000\u0000\u0000\u03ab\u03ac\u0007\u000e\u0000\u0000\u03ac\u00bb\u0001"+
		"\u0000\u0000\u0000\u03ad\u03ae\u0003\u00be_\u0000\u03ae\u03af\u0005^\u0000"+
		"\u0000\u03af\u03b0\u0003\u00acV\u0000\u03b0\u03b1\u0003\u00c0`\u0000\u03b1"+
		"\u00bd\u0001\u0000\u0000\u0000\u03b2\u03b4\u0005U\u0000\u0000\u03b3\u03b5"+
		"\u00038\u001c\u0000\u03b4\u03b3\u0001\u0000\u0000\u0000\u03b4\u03b5\u0001"+
		"\u0000\u0000\u0000\u03b5\u03b6\u0001\u0000\u0000\u0000\u03b6\u03b7\u0005"+
		"V\u0000\u0000\u03b7\u00bf\u0001\u0000\u0000\u0000\u03b8\u03b9\u0003>\u001f"+
		"\u0000\u03b9\u00c1\u0001\u0000\u0000\u0000\u03ba\u03bb\u0005`\u0000\u0000"+
		"\u03bb\u03c2\u0003\u00a2Q\u0000\u03bc\u03bf\u0005U\u0000\u0000\u03bd\u03c0"+
		"\u0003\u00c4b\u0000\u03be\u03c0\u0003f3\u0000\u03bf\u03bd\u0001\u0000"+
		"\u0000\u0000\u03bf\u03be\u0001\u0000\u0000\u0000\u03bf\u03c0\u0001\u0000"+
		"\u0000\u0000\u03c0\u03c1\u0001\u0000\u0000\u0000\u03c1\u03c3\u0005V\u0000"+
		"\u0000\u03c2\u03bc\u0001\u0000\u0000\u0000\u03c2\u03c3\u0001\u0000\u0000"+
		"\u0000\u03c3\u00c3\u0001\u0000\u0000\u0000\u03c4\u03c9\u0003\u00c6c\u0000"+
		"\u03c5\u03c6\u0005\\\u0000\u0000\u03c6\u03c8\u0003\u00c6c\u0000\u03c7"+
		"\u03c5\u0001\u0000\u0000\u0000\u03c8\u03cb\u0001\u0000\u0000\u0000\u03c9"+
		"\u03c7\u0001\u0000\u0000\u0000\u03c9\u03ca\u0001\u0000\u0000\u0000\u03ca"+
		"\u00c5\u0001\u0000\u0000\u0000\u03cb\u03c9\u0001\u0000\u0000\u0000\u03cc"+
		"\u03cd\u0003\u00a2Q\u0000\u03cd\u03ce\u00052\u0000\u0000\u03ce\u03cf\u0003"+
		"f3\u0000\u03cf\u00c7\u0001\u0000\u0000\u0000e\u00c9\u00ce\u00d4\u00df"+
		"\u00e5\u00ea\u00f0\u00f4\u00fc\u0106\u010c\u0111\u0119\u0120\u0124\u0127"+
		"\u012a\u0133\u0138\u013e\u0144\u014a\u014e\u0156\u015e\u0168\u016e\u0173"+
		"\u0177\u017f\u0189\u018e\u0191\u0197\u01a1\u01ab\u01b3\u01b9\u01bd\u01c6"+
		"\u01d3\u01e7\u01f2\u01f8\u01ff\u0205\u0209\u020c\u0210\u0214\u021b\u0228"+
		"\u0231\u0240\u0244\u0246\u024c\u0251\u025a\u0265\u027a\u0285\u028e\u029a"+
		"\u02a1\u02a9\u02b1\u02b9\u02c1\u02c8\u02d2\u02dd\u02e4\u02ef\u02f5\u02ff"+
		"\u030a\u0310\u031b\u0324\u0327\u033e\u0342\u034a\u034e\u0359\u0361\u036e"+
		"\u0371\u037c\u0383\u038a\u038e\u0390\u0397\u039f\u03a9\u03b4\u03bf\u03c2"+
		"\u03c9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}