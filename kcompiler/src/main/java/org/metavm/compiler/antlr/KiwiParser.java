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
		RULE_initParameter = 6, RULE_classBody = 7, RULE_typeList = 8, RULE_classBodyDeclaration = 9, 
		RULE_staticBlock = 10, RULE_enumDeclaration = 11, RULE_enumConstants = 12, 
		RULE_enumConstant = 13, RULE_enumBodyDeclarations = 14, RULE_interfaceDeclaration = 15, 
		RULE_interfaceBody = 16, RULE_interfaceBodyDeclaration = 17, RULE_interfaceMemberDeclaration = 18, 
		RULE_interfaceMethodDeclaration = 19, RULE_interfaceMethodModifier = 20, 
		RULE_memberDeclaration = 21, RULE_fieldDeclaration = 22, RULE_methodDeclaration = 23, 
		RULE_typeParameters = 24, RULE_qualifiedNameList = 25, RULE_qualifiedName = 26, 
		RULE_typeParameter = 27, RULE_formalParameters = 28, RULE_formalParameterList = 29, 
		RULE_formalParameter = 30, RULE_methodBody = 31, RULE_block = 32, RULE_statement = 33, 
		RULE_localVariableDeclaration = 34, RULE_forControl = 35, RULE_loopVariable = 36, 
		RULE_loopVariableDeclarators = 37, RULE_loopVariableDeclarator = 38, RULE_loopVariableUpdates = 39, 
		RULE_loopVariableUpdate = 40, RULE_anonClassExpr = 41, RULE_newArray = 42, 
		RULE_arrayInitializer = 43, RULE_variableInitializer = 44, RULE_catchClause = 45, 
		RULE_catchFields = 46, RULE_catchField = 47, RULE_catchValue = 48, RULE_branchCase = 49, 
		RULE_switchLabel = 50, RULE_parExpression = 51, RULE_expressionList = 52, 
		RULE_expression = 53, RULE_assignment = 54, RULE_assignmentSuffix = 55, 
		RULE_ternary = 56, RULE_disjunction = 57, RULE_conjunction = 58, RULE_range = 59, 
		RULE_bitor = 60, RULE_bitand = 61, RULE_bitxor = 62, RULE_equality = 63, 
		RULE_equalitySuffix = 64, RULE_relational = 65, RULE_relationalSuffix = 66, 
		RULE_isExpr = 67, RULE_isSuffix = 68, RULE_typePtn = 69, RULE_shift = 70, 
		RULE_shiftSuffix = 71, RULE_additive = 72, RULE_additiveSuffix = 73, RULE_multiplicative = 74, 
		RULE_multiplicativeSuffix = 75, RULE_asExpr = 76, RULE_prefixExpr = 77, 
		RULE_prefixOp = 78, RULE_postfixExpr = 79, RULE_postfixSuffix = 80, RULE_callSuffix = 81, 
		RULE_indexingSuffix = 82, RULE_selectorSuffix = 83, RULE_primaryExpr = 84, 
		RULE_arguments = 85, RULE_identifier = 86, RULE_literal = 87, RULE_integerLiteral = 88, 
		RULE_floatLiteral = 89, RULE_typeOrVoid = 90, RULE_type = 91, RULE_unionType = 92, 
		RULE_intersectionType = 93, RULE_postfixType = 94, RULE_typeSuffix = 95, 
		RULE_atomicType = 96, RULE_primitiveType = 97, RULE_functionType = 98, 
		RULE_uncertainType = 99, RULE_arrayKind = 100, RULE_classType = 101, RULE_classTypePart = 102, 
		RULE_typeArguments = 103, RULE_modifier = 104, RULE_classOrInterfaceModifier = 105, 
		RULE_lambdaExpression = 106, RULE_lambdaParameters = 107, RULE_lambdaParameterList = 108, 
		RULE_lambdaParameter = 109, RULE_lambdaBody = 110, RULE_annotation = 111, 
		RULE_elementValuePairs = 112, RULE_elementValuePair = 113;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "packageDeclaration", "importDeclaration", "topLevTypeDecl", 
			"typeDeclaration", "classDeclaration", "initParameter", "classBody", 
			"typeList", "classBodyDeclaration", "staticBlock", "enumDeclaration", 
			"enumConstants", "enumConstant", "enumBodyDeclarations", "interfaceDeclaration", 
			"interfaceBody", "interfaceBodyDeclaration", "interfaceMemberDeclaration", 
			"interfaceMethodDeclaration", "interfaceMethodModifier", "memberDeclaration", 
			"fieldDeclaration", "methodDeclaration", "typeParameters", "qualifiedNameList", 
			"qualifiedName", "typeParameter", "formalParameters", "formalParameterList", 
			"formalParameter", "methodBody", "block", "statement", "localVariableDeclaration", 
			"forControl", "loopVariable", "loopVariableDeclarators", "loopVariableDeclarator", 
			"loopVariableUpdates", "loopVariableUpdate", "anonClassExpr", "newArray", 
			"arrayInitializer", "variableInitializer", "catchClause", "catchFields", 
			"catchField", "catchValue", "branchCase", "switchLabel", "parExpression", 
			"expressionList", "expression", "assignment", "assignmentSuffix", "ternary", 
			"disjunction", "conjunction", "range", "bitor", "bitand", "bitxor", "equality", 
			"equalitySuffix", "relational", "relationalSuffix", "isExpr", "isSuffix", 
			"typePtn", "shift", "shiftSuffix", "additive", "additiveSuffix", "multiplicative", 
			"multiplicativeSuffix", "asExpr", "prefixExpr", "prefixOp", "postfixExpr", 
			"postfixSuffix", "callSuffix", "indexingSuffix", "selectorSuffix", "primaryExpr", 
			"arguments", "identifier", "literal", "integerLiteral", "floatLiteral", 
			"typeOrVoid", "type", "unionType", "intersectionType", "postfixType", 
			"typeSuffix", "atomicType", "primitiveType", "functionType", "uncertainType", 
			"arrayKind", "classType", "classTypePart", "typeArguments", "modifier", 
			"classOrInterfaceModifier", "lambdaExpression", "lambdaParameters", "lambdaParameterList", 
			"lambdaParameter", "lambdaBody", "annotation", "elementValuePairs", "elementValuePair"
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
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<InitParameterContext> initParameter() {
			return getRuleContexts(InitParameterContext.class);
		}
		public InitParameterContext initParameter(int i) {
			return getRuleContext(InitParameterContext.class,i);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
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

			setState(283);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(272);
				match(LPAREN);
				{
				setState(273);
				initParameter();
				setState(278);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(274);
					match(COMMA);
					setState(275);
					initParameter();
					}
					}
					setState(280);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				setState(281);
				match(RPAREN);
				}
			}

			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(285);
				match(COLON);
				setState(286);
				type();
				setState(288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(287);
					arguments();
					}
				}

				setState(294);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(290);
					match(COMMA);
					setState(291);
					type();
					}
					}
					setState(296);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(299);
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
	public static class InitParameterContext extends ParserRuleContext {
		public FieldDeclarationContext fieldDeclaration() {
			return getRuleContext(FieldDeclarationContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public FormalParameterContext formalParameter() {
			return getRuleContext(FormalParameterContext.class,0);
		}
		public InitParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_initParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).enterInitParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KiwiParserListener ) ((KiwiParserListener)listener).exitInitParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof KiwiParserVisitor ) return ((KiwiParserVisitor<? extends T>)visitor).visitInitParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InitParameterContext initParameter() throws RecognitionException {
		InitParameterContext _localctx = new InitParameterContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_initParameter);
		int _la;
		try {
			setState(309);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(304);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18102365680828418L) != 0)) {
					{
					{
					setState(301);
					modifier();
					}
					}
					setState(306);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(307);
				fieldDeclaration();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(308);
				formalParameter();
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
		enterRule(_localctx, 14, RULE_classBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			match(LBRACE);
			setState(315);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18559762551570562L) != 0) || _la==LBRACE || _la==AT) {
				{
				{
				setState(312);
				classBodyDeclaration();
				}
				}
				setState(317);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(318);
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
		enterRule(_localctx, 16, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			type();
			setState(325);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(321);
				match(COMMA);
				setState(322);
				type();
				}
				}
				setState(327);
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
		enterRule(_localctx, 18, RULE_classBodyDeclaration);
		int _la;
		try {
			setState(336);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(331);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18102365680828418L) != 0)) {
					{
					{
					setState(328);
					modifier();
					}
					}
					setState(333);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(334);
				memberDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(335);
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
		enterRule(_localctx, 20, RULE_staticBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			match(STATIC);
			setState(339);
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
		public TerminalNode LPAREN() { return getToken(KiwiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(KiwiParser.RPAREN, 0); }
		public TerminalNode COLON() { return getToken(KiwiParser.COLON, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(KiwiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KiwiParser.COMMA, i);
		}
		public EnumBodyDeclarationsContext enumBodyDeclarations() {
			return getRuleContext(EnumBodyDeclarationsContext.class,0);
		}
		public List<InitParameterContext> initParameter() {
			return getRuleContexts(InitParameterContext.class);
		}
		public InitParameterContext initParameter(int i) {
			return getRuleContext(InitParameterContext.class,i);
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
		enterRule(_localctx, 22, RULE_enumDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(344);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(341);
				annotation();
				}
				}
				setState(346);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(347);
			match(ENUM);
			setState(348);
			identifier();
			setState(360);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(349);
				match(LPAREN);
				{
				setState(350);
				initParameter();
				setState(355);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(351);
					match(COMMA);
					setState(352);
					initParameter();
					}
					}
					setState(357);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				setState(358);
				match(RPAREN);
				}
			}

			setState(364);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(362);
				match(COLON);
				setState(363);
				typeList();
				}
			}

			setState(366);
			match(LBRACE);
			setState(368);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
				{
				setState(367);
				enumConstants();
				}
			}

			setState(371);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(370);
				match(COMMA);
				}
			}

			setState(374);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(373);
				enumBodyDeclarations();
				}
			}

			setState(376);
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
		enterRule(_localctx, 24, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(378);
			enumConstant();
			setState(383);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(379);
					match(COMMA);
					setState(380);
					enumConstant();
					}
					} 
				}
				setState(385);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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
		enterRule(_localctx, 26, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			identifier();
			setState(388);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(387);
				arguments();
				}
			}

			setState(391);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBRACE) {
				{
				setState(390);
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
		enterRule(_localctx, 28, RULE_enumBodyDeclarations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			match(SEMI);
			setState(397);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18559762551570562L) != 0) || _la==LBRACE || _la==AT) {
				{
				{
				setState(394);
				classBodyDeclaration();
				}
				}
				setState(399);
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
		enterRule(_localctx, 30, RULE_interfaceDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(400);
				annotation();
				}
				}
				setState(405);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(406);
			match(INTERFACE);
			setState(407);
			identifier();
			setState(409);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(408);
				typeParameters();
				}
			}

			setState(413);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(411);
				match(COLON);
				setState(412);
				typeList();
				}
			}

			setState(415);
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
		enterRule(_localctx, 32, RULE_interfaceBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
			match(LBRACE);
			setState(421);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18137550052917266L) != 0)) {
				{
				{
				setState(418);
				interfaceBodyDeclaration();
				}
				}
				setState(423);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(424);
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
		enterRule(_localctx, 34, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(429);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(426);
					modifier();
					}
					} 
				}
				setState(431);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			}
			setState(432);
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
		enterRule(_localctx, 36, RULE_interfaceMemberDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
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
		enterRule(_localctx, 38, RULE_interfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(439);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 5368709138L) != 0)) {
				{
				{
				setState(436);
				interfaceMethodModifier();
				}
				}
				setState(441);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(442);
			match(FN);
			setState(443);
			identifier();
			setState(445);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(444);
				typeParameters();
				}
			}

			setState(447);
			formalParameters();
			setState(450);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(448);
				match(ARROW);
				setState(449);
				typeOrVoid();
				}
			}

			setState(454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(452);
				match(THROWS);
				setState(453);
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
		enterRule(_localctx, 40, RULE_interfaceMethodModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(456);
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
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
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
		enterRule(_localctx, 42, RULE_memberDeclaration);
		try {
			setState(462);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FN:
				enterOuterAlt(_localctx, 1);
				{
				setState(458);
				methodDeclaration();
				}
				break;
			case VAL:
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(459);
				fieldDeclaration();
				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(460);
				block();
				}
				break;
			case CLASS:
			case ENUM:
			case INTERFACE:
			case AT:
				enterOuterAlt(_localctx, 4);
				{
				setState(461);
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
		enterRule(_localctx, 44, RULE_fieldDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(464);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(465);
			identifier();
			setState(468);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(466);
				match(COLON);
				setState(467);
				type();
				}
			}

			setState(472);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(470);
				match(ASSIGN);
				setState(471);
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
		enterRule(_localctx, 46, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(474);
			match(FN);
			setState(475);
			identifier();
			setState(477);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(476);
				typeParameters();
				}
			}

			setState(479);
			formalParameters();
			setState(482);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ARROW) {
				{
				setState(480);
				match(ARROW);
				setState(481);
				typeOrVoid();
				}
			}

			setState(485);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				{
				setState(484);
				methodBody();
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
			setState(487);
			match(LT);
			setState(488);
			typeParameter();
			setState(493);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(489);
				match(COMMA);
				setState(490);
				typeParameter();
				}
				}
				setState(495);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(496);
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
			setState(498);
			qualifiedName();
			setState(503);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(499);
				match(COMMA);
				setState(500);
				qualifiedName();
				}
				}
				setState(505);
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
			setState(506);
			identifier();
			setState(511);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(507);
				match(DOT);
				setState(508);
				identifier();
				}
				}
				setState(513);
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
			setState(519);
			match(LPAREN);
			setState(521);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
				{
				setState(520);
				formalParameterList();
				}
			}

			setState(523);
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
			setState(525);
			formalParameter();
			setState(530);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(526);
				match(COMMA);
				setState(527);
				formalParameter();
				}
				}
				setState(532);
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
			setState(533);
			identifier();
			setState(534);
			match(COLON);
			setState(535);
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
			setState(537);
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
			setState(539);
			match(LBRACE);
			setState(543);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1474299263810142720L) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 87943895056399L) != 0)) {
				{
				{
				setState(540);
				statement();
				}
				}
				setState(545);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(546);
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
			setState(608);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(548);
				match(WHILE);
				setState(549);
				parExpression();
				setState(550);
				statement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(552);
				match(DO);
				setState(553);
				statement();
				setState(554);
				match(WHILE);
				setState(555);
				parExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(557);
				match(FOR);
				setState(558);
				match(LPAREN);
				setState(559);
				forControl();
				setState(560);
				match(RPAREN);
				setState(561);
				statement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(563);
				match(IF);
				setState(564);
				parExpression();
				setState(565);
				statement();
				setState(568);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
				case 1:
					{
					setState(566);
					match(ELSE);
					setState(567);
					statement();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(570);
				match(TRY);
				setState(571);
				block();
				setState(573); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(572);
					catchClause();
					}
					}
					setState(575); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==CATCH );
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(577);
				match(SWITCH);
				setState(578);
				match(LBRACE);
				setState(582);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(579);
					branchCase();
					}
					}
					setState(584);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(585);
				match(RBRACE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(586);
				match(RETURN);
				setState(588);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
				case 1:
					{
					setState(587);
					expression();
					}
					break;
				}
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(590);
				match(THROW);
				setState(591);
				expression();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(592);
				match(BREAK);
				setState(594);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
				case 1:
					{
					setState(593);
					identifier();
					}
					break;
				}
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(596);
				match(CONTINUE);
				setState(598);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
				case 1:
					{
					setState(597);
					identifier();
					}
					break;
				}
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(600);
				identifier();
				setState(601);
				match(COLON);
				setState(602);
				statement();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(604);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(605);
				((StatementContext)_localctx).statementExpression = expression();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(606);
				localVariableDeclaration();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(607);
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
			setState(610);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(611);
			identifier();
			setState(614);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(612);
				match(COLON);
				setState(613);
				type();
				}
			}

			setState(618);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASSIGN) {
				{
				setState(616);
				match(ASSIGN);
				setState(617);
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
			setState(620);
			loopVariable();
			setState(621);
			match(IN);
			setState(622);
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
			setState(624);
			identifier();
			setState(627);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(625);
				match(COLON);
				setState(626);
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
			setState(629);
			loopVariableDeclarator();
			setState(634);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(630);
				match(COMMA);
				setState(631);
				loopVariableDeclarator();
				}
				}
				setState(636);
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
			setState(637);
			type();
			setState(638);
			identifier();
			setState(639);
			match(ASSIGN);
			setState(640);
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
			setState(642);
			loopVariableUpdate();
			setState(647);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(643);
				match(COMMA);
				setState(644);
				loopVariableUpdate();
				}
				}
				setState(649);
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
			setState(650);
			identifier();
			setState(651);
			match(ASSIGN);
			setState(652);
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
			setState(654);
			match(NEW);
			setState(655);
			classType();
			setState(656);
			arguments();
			setState(657);
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
			setState(659);
			match(NEW);
			setState(660);
			type();
			setState(661);
			arrayKind();
			setState(663);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
			case 1:
				{
				setState(662);
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
			setState(665);
			match(LBRACE);
			setState(677);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1460362591002952192L) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 87943760838671L) != 0)) {
				{
				setState(666);
				variableInitializer();
				setState(671);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(667);
						match(COMMA);
						setState(668);
						variableInitializer();
						}
						} 
					}
					setState(673);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
				}
				setState(675);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(674);
					match(COMMA);
					}
				}

				}
			}

			setState(679);
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
			setState(683);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(681);
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
				setState(682);
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
			setState(685);
			match(CATCH);
			setState(686);
			match(LPAREN);
			setState(687);
			identifier();
			setState(688);
			match(COLON);
			setState(689);
			type();
			setState(690);
			match(RPAREN);
			setState(691);
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
			setState(693);
			catchField();
			setState(698);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(694);
				match(COMMA);
				setState(695);
				catchField();
				}
				}
				setState(700);
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
			setState(701);
			identifier();
			setState(702);
			match(COLON);
			setState(703);
			match(LBRACE);
			setState(709);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
				{
				{
				setState(704);
				catchValue();
				setState(705);
				match(COMMA);
				}
				}
				setState(711);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(712);
			match(DEFAULT);
			setState(713);
			match(COLON);
			setState(714);
			expression();
			setState(715);
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
			setState(717);
			identifier();
			setState(718);
			match(COLON);
			setState(719);
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
			setState(721);
			switchLabel();
			setState(722);
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
			setState(730);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(724);
				match(CASE);
				{
				setState(725);
				expression();
				}
				setState(726);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(728);
				match(DEFAULT);
				setState(729);
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
			setState(732);
			match(LPAREN);
			setState(733);
			expression();
			setState(734);
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
			setState(736);
			expression();
			setState(741);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(737);
				match(COMMA);
				setState(738);
				expression();
				}
				}
				setState(743);
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
			setState(744);
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
			setState(746);
			ternary();
			setState(750);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(747);
					assignmentSuffix();
					}
					} 
				}
				setState(752);
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
			setState(753);
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
			setState(754);
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
			setState(756);
			disjunction();
			setState(762);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				{
				setState(757);
				match(QUESTION);
				setState(758);
				disjunction();
				setState(759);
				match(COLON);
				setState(760);
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
			setState(764);
			conjunction();
			setState(769);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,74,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(765);
					match(OR);
					setState(766);
					conjunction();
					}
					} 
				}
				setState(771);
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
			setState(772);
			range();
			setState(777);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(773);
					match(AND);
					setState(774);
					range();
					}
					} 
				}
				setState(779);
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
			setState(780);
			bitor();
			setState(783);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				{
				setState(781);
				match(ELLIPSIS);
				setState(782);
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
			setState(785);
			bitand();
			setState(790);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(786);
					match(BITOR);
					setState(787);
					bitand();
					}
					} 
				}
				setState(792);
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
			setState(793);
			bitxor();
			setState(798);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,78,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(794);
					match(BITAND);
					setState(795);
					bitxor();
					}
					} 
				}
				setState(800);
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
			setState(801);
			equality();
			setState(806);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(802);
					match(CARET);
					setState(803);
					equality();
					}
					} 
				}
				setState(808);
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
			setState(809);
			relational();
			setState(813);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(810);
					equalitySuffix();
					}
					} 
				}
				setState(815);
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
			setState(816);
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
			setState(817);
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
			setState(819);
			isExpr();
			setState(823);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,81,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(820);
					relationalSuffix();
					}
					} 
				}
				setState(825);
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
			setState(826);
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
			setState(827);
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
			setState(829);
			shift();
			setState(833);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,82,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(830);
					isSuffix();
					}
					} 
				}
				setState(835);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,82,_ctx);
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
			setState(836);
			match(IS);
			setState(839);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				{
				setState(837);
				typePtn();
				}
				break;
			case 2:
				{
				setState(838);
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
			setState(841);
			type();
			setState(842);
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
			setState(844);
			additive();
			setState(848);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,84,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(845);
					shiftSuffix();
					}
					} 
				}
				setState(850);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,84,_ctx);
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
			setState(858);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				{
				setState(851);
				match(GT);
				setState(852);
				match(GT);
				}
				break;
			case 2:
				{
				setState(853);
				match(LT);
				setState(854);
				match(LT);
				}
				break;
			case 3:
				{
				setState(855);
				match(GT);
				setState(856);
				match(GT);
				setState(857);
				match(GT);
				}
				break;
			}
			setState(860);
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
			setState(862);
			multiplicative();
			setState(866);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(863);
					additiveSuffix();
					}
					} 
				}
				setState(868);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
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
			setState(869);
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
			setState(870);
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
			setState(872);
			asExpr();
			setState(876);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(873);
					multiplicativeSuffix();
					}
					} 
				}
				setState(878);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
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
			setState(879);
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
			setState(880);
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
			setState(882);
			prefixExpr();
			setState(887);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(883);
					match(AS);
					setState(884);
					type();
					}
					} 
				}
				setState(889);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
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
			setState(893);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 58)) & ~0x3f) == 0 && ((1L << (_la - 58)) & 30725L) != 0)) {
				{
				{
				setState(890);
				prefixOp();
				}
				}
				setState(895);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(896);
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
			setState(898);
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
			setState(900);
			primaryExpr();
			setState(904);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(901);
					postfixSuffix();
					}
					} 
				}
				setState(906);
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
			setState(914);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(907);
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
				setState(908);
				callSuffix();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(909);
				match(DOT);
				setState(910);
				anonClassExpr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(911);
				indexingSuffix();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(912);
				selectorSuffix();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(913);
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
			setState(916);
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
			setState(918);
			match(LBRACK);
			setState(919);
			expression();
			setState(920);
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
			setState(922);
			match(DOT);
			setState(925);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
			case INIT:
			case TEMP:
			case IDENTIFIER:
				{
				setState(923);
				identifier();
				}
				break;
			case THIS:
				{
				setState(924);
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
			setState(938);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(927);
				match(LPAREN);
				setState(928);
				expression();
				setState(929);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(931);
				match(THIS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(932);
				match(SUPER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(933);
				anonClassExpr();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(934);
				newArray();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(935);
				literal();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(936);
				identifier();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(937);
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
			setState(940);
			match(LPAREN);
			setState(942);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1460362591002952192L) != 0) || ((((_la - 69)) & ~0x3f) == 0 && ((1L << (_la - 69)) & 87943752450063L) != 0)) {
				{
				setState(941);
				expressionList();
				}
			}

			setState(944);
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
			setState(946);
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
			setState(955);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(948);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(949);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(950);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(951);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(952);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(953);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(954);
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
			setState(957);
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
			setState(959);
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
			setState(963);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(961);
				match(VOID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(962);
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
			setState(965);
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
			setState(967);
			intersectionType();
			setState(972);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,97,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(968);
					match(BITOR);
					setState(969);
					intersectionType();
					}
					} 
				}
				setState(974);
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
			setState(975);
			postfixType();
			setState(980);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,98,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(976);
					match(BITAND);
					setState(977);
					postfixType();
					}
					} 
				}
				setState(982);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,98,_ctx);
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
			setState(983);
			atomicType();
			setState(987);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(984);
					typeSuffix();
					}
					} 
				}
				setState(989);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
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
			setState(992);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case R:
			case RW:
				enterOuterAlt(_localctx, 1);
				{
				setState(990);
				arrayKind();
				}
				break;
			case QUESTION:
				enterOuterAlt(_localctx, 2);
				{
				setState(991);
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
			setState(998);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUE:
			case INIT:
			case TEMP:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(994);
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
				setState(995);
				primitiveType();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 3);
				{
				setState(996);
				functionType();
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 4);
				{
				setState(997);
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
			setState(1000);
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
			setState(1002);
			match(LPAREN);
			setState(1011);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19224411088368452L) != 0) || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 33554449L) != 0)) {
				{
				setState(1003);
				type();
				setState(1008);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1004);
					match(COMMA);
					setState(1005);
					type();
					}
					}
					setState(1010);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(1013);
			match(RPAREN);
			setState(1014);
			match(ARROW);
			setState(1015);
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
			setState(1017);
			match(LBRACK);
			setState(1018);
			type();
			setState(1019);
			match(COMMA);
			setState(1020);
			type();
			setState(1021);
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
			setState(1023);
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
			setState(1025);
			classTypePart();
			setState(1030);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1026);
					match(DOT);
					setState(1027);
					classTypePart();
					}
					} 
				}
				setState(1032);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
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
			setState(1033);
			identifier();
			setState(1035);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				{
				setState(1034);
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
			setState(1037);
			match(LT);
			setState(1038);
			type();
			setState(1043);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1039);
				match(COMMA);
				setState(1040);
				type();
				}
				}
				setState(1045);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1046);
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
			setState(1051);
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
				setState(1048);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1049);
				match(NATIVE);
				}
				break;
			case DELETED:
				enterOuterAlt(_localctx, 3);
				{
				setState(1050);
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
			setState(1053);
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
			setState(1055);
			lambdaParameters();
			setState(1056);
			match(ARROW);
			setState(1062);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
			case 1:
				{
				setState(1058);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19224411088368452L) != 0) || ((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 33554449L) != 0)) {
					{
					setState(1057);
					typeOrVoid();
					}
				}

				setState(1060);
				lambdaBody();
				}
				break;
			case 2:
				{
				setState(1061);
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
			setState(1070);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1064);
				match(LPAREN);
				setState(1066);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 19210667160502272L) != 0) || _la==IDENTIFIER) {
					{
					setState(1065);
					lambdaParameterList();
					}
				}

				setState(1068);
				match(RPAREN);
				}
				break;
			case VALUE:
			case INIT:
			case TEMP:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(1069);
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
			setState(1072);
			lambdaParameter();
			setState(1077);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1073);
				match(COMMA);
				setState(1074);
				lambdaParameter();
				}
				}
				setState(1079);
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
			setState(1080);
			identifier();
			setState(1083);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1081);
				match(COLON);
				setState(1082);
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
			setState(1085);
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
			setState(1087);
			match(AT);
			setState(1088);
			identifier();
			setState(1095);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1089);
				match(LPAREN);
				setState(1092);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
				case 1:
					{
					setState(1090);
					elementValuePairs();
					}
					break;
				case 2:
					{
					setState(1091);
					expression();
					}
					break;
				}
				setState(1094);
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
			setState(1097);
			elementValuePair();
			setState(1102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1098);
				match(COMMA);
				setState(1099);
				elementValuePair();
				}
				}
				setState(1104);
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
			setState(1105);
			identifier();
			setState(1106);
			match(ASSIGN);
			setState(1107);
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
		"\u0004\u0001v\u0456\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u0115\b\u0005\n"+
		"\u0005\f\u0005\u0118\t\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u011c"+
		"\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u0121\b\u0005"+
		"\u0001\u0005\u0001\u0005\u0005\u0005\u0125\b\u0005\n\u0005\f\u0005\u0128"+
		"\t\u0005\u0003\u0005\u012a\b\u0005\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0005\u0006\u012f\b\u0006\n\u0006\f\u0006\u0132\t\u0006\u0001\u0006\u0001"+
		"\u0006\u0003\u0006\u0136\b\u0006\u0001\u0007\u0001\u0007\u0005\u0007\u013a"+
		"\b\u0007\n\u0007\f\u0007\u013d\t\u0007\u0001\u0007\u0001\u0007\u0001\b"+
		"\u0001\b\u0001\b\u0005\b\u0144\b\b\n\b\f\b\u0147\t\b\u0001\t\u0005\t\u014a"+
		"\b\t\n\t\f\t\u014d\t\t\u0001\t\u0001\t\u0003\t\u0151\b\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0005\u000b\u0157\b\u000b\n\u000b\f\u000b\u015a"+
		"\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u0162\b\u000b\n\u000b\f\u000b\u0165\t\u000b\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u0169\b\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u016d\b\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0171\b\u000b\u0001"+
		"\u000b\u0003\u000b\u0174\b\u000b\u0001\u000b\u0003\u000b\u0177\b\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005\f\u017e\b\f\n\f"+
		"\f\f\u0181\t\f\u0001\r\u0001\r\u0003\r\u0185\b\r\u0001\r\u0003\r\u0188"+
		"\b\r\u0001\u000e\u0001\u000e\u0005\u000e\u018c\b\u000e\n\u000e\f\u000e"+
		"\u018f\t\u000e\u0001\u000f\u0005\u000f\u0192\b\u000f\n\u000f\f\u000f\u0195"+
		"\t\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u019a\b\u000f"+
		"\u0001\u000f\u0001\u000f\u0003\u000f\u019e\b\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0005\u0010\u01a4\b\u0010\n\u0010\f\u0010\u01a7"+
		"\t\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0005\u0011\u01ac\b\u0011"+
		"\n\u0011\f\u0011\u01af\t\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0005\u0013\u01b6\b\u0013\n\u0013\f\u0013\u01b9\t\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u01be\b\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u01c3\b\u0013\u0001\u0013\u0001\u0013"+
		"\u0003\u0013\u01c7\b\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0003\u0015\u01cf\b\u0015\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0003\u0016\u01d5\b\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u01d9\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017"+
		"\u01de\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u01e3\b"+
		"\u0017\u0001\u0017\u0003\u0017\u01e6\b\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0005\u0018\u01ec\b\u0018\n\u0018\f\u0018\u01ef\t\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019"+
		"\u01f6\b\u0019\n\u0019\f\u0019\u01f9\t\u0019\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0005\u001a\u01fe\b\u001a\n\u001a\f\u001a\u0201\t\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0003\u001b\u0206\b\u001b\u0001\u001c\u0001\u001c"+
		"\u0003\u001c\u020a\b\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0005\u001d\u0211\b\u001d\n\u001d\f\u001d\u0214\t\u001d\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f\u0001"+
		" \u0001 \u0005 \u021e\b \n \f \u0221\t \u0001 \u0001 \u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u0239\b!\u0001"+
		"!\u0001!\u0001!\u0004!\u023e\b!\u000b!\f!\u023f\u0001!\u0001!\u0001!\u0005"+
		"!\u0245\b!\n!\f!\u0248\t!\u0001!\u0001!\u0001!\u0003!\u024d\b!\u0001!"+
		"\u0001!\u0001!\u0001!\u0003!\u0253\b!\u0001!\u0001!\u0003!\u0257\b!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u0261\b!\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0003\"\u0267\b\"\u0001\"\u0001\"\u0003\"\u026b"+
		"\b\"\u0001#\u0001#\u0001#\u0001#\u0001$\u0001$\u0001$\u0003$\u0274\b$"+
		"\u0001%\u0001%\u0001%\u0005%\u0279\b%\n%\f%\u027c\t%\u0001&\u0001&\u0001"+
		"&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0005\'\u0286\b\'\n\'\f\'\u0289"+
		"\t\'\u0001(\u0001(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"+
		"*\u0001*\u0001*\u0001*\u0003*\u0298\b*\u0001+\u0001+\u0001+\u0001+\u0005"+
		"+\u029e\b+\n+\f+\u02a1\t+\u0001+\u0003+\u02a4\b+\u0003+\u02a6\b+\u0001"+
		"+\u0001+\u0001,\u0001,\u0003,\u02ac\b,\u0001-\u0001-\u0001-\u0001-\u0001"+
		"-\u0001-\u0001-\u0001-\u0001.\u0001.\u0001.\u0005.\u02b9\b.\n.\f.\u02bc"+
		"\t.\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u0005/\u02c4\b/\n/\f/\u02c7"+
		"\t/\u0001/\u0001/\u0001/\u0001/\u0001/\u00010\u00010\u00010\u00010\u0001"+
		"1\u00011\u00011\u00012\u00012\u00012\u00012\u00012\u00012\u00032\u02db"+
		"\b2\u00013\u00013\u00013\u00013\u00014\u00014\u00014\u00054\u02e4\b4\n"+
		"4\f4\u02e7\t4\u00015\u00015\u00016\u00016\u00056\u02ed\b6\n6\f6\u02f0"+
		"\t6\u00017\u00017\u00017\u00018\u00018\u00018\u00018\u00018\u00018\u0003"+
		"8\u02fb\b8\u00019\u00019\u00019\u00059\u0300\b9\n9\f9\u0303\t9\u0001:"+
		"\u0001:\u0001:\u0005:\u0308\b:\n:\f:\u030b\t:\u0001;\u0001;\u0001;\u0003"+
		";\u0310\b;\u0001<\u0001<\u0001<\u0005<\u0315\b<\n<\f<\u0318\t<\u0001="+
		"\u0001=\u0001=\u0005=\u031d\b=\n=\f=\u0320\t=\u0001>\u0001>\u0001>\u0005"+
		">\u0325\b>\n>\f>\u0328\t>\u0001?\u0001?\u0005?\u032c\b?\n?\f?\u032f\t"+
		"?\u0001@\u0001@\u0001@\u0001A\u0001A\u0005A\u0336\bA\nA\fA\u0339\tA\u0001"+
		"B\u0001B\u0001B\u0001C\u0001C\u0005C\u0340\bC\nC\fC\u0343\tC\u0001D\u0001"+
		"D\u0001D\u0003D\u0348\bD\u0001E\u0001E\u0001E\u0001F\u0001F\u0005F\u034f"+
		"\bF\nF\fF\u0352\tF\u0001G\u0001G\u0001G\u0001G\u0001G\u0001G\u0001G\u0003"+
		"G\u035b\bG\u0001G\u0001G\u0001H\u0001H\u0005H\u0361\bH\nH\fH\u0364\tH"+
		"\u0001I\u0001I\u0001I\u0001J\u0001J\u0005J\u036b\bJ\nJ\fJ\u036e\tJ\u0001"+
		"K\u0001K\u0001K\u0001L\u0001L\u0001L\u0005L\u0376\bL\nL\fL\u0379\tL\u0001"+
		"M\u0005M\u037c\bM\nM\fM\u037f\tM\u0001M\u0001M\u0001N\u0001N\u0001O\u0001"+
		"O\u0005O\u0387\bO\nO\fO\u038a\tO\u0001P\u0001P\u0001P\u0001P\u0001P\u0001"+
		"P\u0001P\u0003P\u0393\bP\u0001Q\u0001Q\u0001R\u0001R\u0001R\u0001R\u0001"+
		"S\u0001S\u0001S\u0003S\u039e\bS\u0001T\u0001T\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0001T\u0001T\u0001T\u0001T\u0003T\u03ab\bT\u0001U\u0001U\u0003"+
		"U\u03af\bU\u0001U\u0001U\u0001V\u0001V\u0001W\u0001W\u0001W\u0001W\u0001"+
		"W\u0001W\u0001W\u0003W\u03bc\bW\u0001X\u0001X\u0001Y\u0001Y\u0001Z\u0001"+
		"Z\u0003Z\u03c4\bZ\u0001[\u0001[\u0001\\\u0001\\\u0001\\\u0005\\\u03cb"+
		"\b\\\n\\\f\\\u03ce\t\\\u0001]\u0001]\u0001]\u0005]\u03d3\b]\n]\f]\u03d6"+
		"\t]\u0001^\u0001^\u0005^\u03da\b^\n^\f^\u03dd\t^\u0001_\u0001_\u0003_"+
		"\u03e1\b_\u0001`\u0001`\u0001`\u0001`\u0003`\u03e7\b`\u0001a\u0001a\u0001"+
		"b\u0001b\u0001b\u0001b\u0005b\u03ef\bb\nb\fb\u03f2\tb\u0003b\u03f4\bb"+
		"\u0001b\u0001b\u0001b\u0001b\u0001c\u0001c\u0001c\u0001c\u0001c\u0001"+
		"c\u0001d\u0001d\u0001e\u0001e\u0001e\u0005e\u0405\be\ne\fe\u0408\te\u0001"+
		"f\u0001f\u0003f\u040c\bf\u0001g\u0001g\u0001g\u0001g\u0005g\u0412\bg\n"+
		"g\fg\u0415\tg\u0001g\u0001g\u0001h\u0001h\u0001h\u0003h\u041c\bh\u0001"+
		"i\u0001i\u0001j\u0001j\u0001j\u0003j\u0423\bj\u0001j\u0001j\u0003j\u0427"+
		"\bj\u0001k\u0001k\u0003k\u042b\bk\u0001k\u0001k\u0003k\u042f\bk\u0001"+
		"l\u0001l\u0001l\u0005l\u0434\bl\nl\fl\u0437\tl\u0001m\u0001m\u0001m\u0003"+
		"m\u043c\bm\u0001n\u0001n\u0001o\u0001o\u0001o\u0001o\u0001o\u0003o\u0445"+
		"\bo\u0001o\u0003o\u0448\bo\u0001p\u0001p\u0001p\u0005p\u044d\bp\np\fp"+
		"\u0450\tp\u0001q\u0001q\u0001q\u0001q\u0001q\u0000\u0000r\u0000\u0002"+
		"\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e"+
		" \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086"+
		"\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e"+
		"\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6"+
		"\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce"+
		"\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u0000\u000f"+
		"\u0004\u0000\u0001\u0001\u0004\u0004\u001e\u001e  \u0001\u0000/0\u0003"+
		"\u000077OPSY\u0002\u0000??BB\u0002\u000089@A\u0001\u0000GH\u0002\u0000"+
		"IJNN\u0003\u0000::<<EH\u0002\u0000;;EF\u0004\u0000..2266ss\u0001\u0000"+
		"gj\u0001\u0000kl\u0007\u0000\u0002\u0002\u0006\u0006\b\t\f\r\u0014\u0018"+
		"\'\'*+\u0001\u0000qr\u0005\u0000\u0001\u0001\u001c\u001e  ..66\u047c\u0000"+
		"\u00e5\u0001\u0000\u0000\u0000\u0002\u00f2\u0001\u0000\u0000\u0000\u0004"+
		"\u00f5\u0001\u0000\u0000\u0000\u0006\u00fb\u0001\u0000\u0000\u0000\b\u0103"+
		"\u0001\u0000\u0000\u0000\n\u0108\u0001\u0000\u0000\u0000\f\u0135\u0001"+
		"\u0000\u0000\u0000\u000e\u0137\u0001\u0000\u0000\u0000\u0010\u0140\u0001"+
		"\u0000\u0000\u0000\u0012\u0150\u0001\u0000\u0000\u0000\u0014\u0152\u0001"+
		"\u0000\u0000\u0000\u0016\u0158\u0001\u0000\u0000\u0000\u0018\u017a\u0001"+
		"\u0000\u0000\u0000\u001a\u0182\u0001\u0000\u0000\u0000\u001c\u0189\u0001"+
		"\u0000\u0000\u0000\u001e\u0193\u0001\u0000\u0000\u0000 \u01a1\u0001\u0000"+
		"\u0000\u0000\"\u01ad\u0001\u0000\u0000\u0000$\u01b2\u0001\u0000\u0000"+
		"\u0000&\u01b7\u0001\u0000\u0000\u0000(\u01c8\u0001\u0000\u0000\u0000*"+
		"\u01ce\u0001\u0000\u0000\u0000,\u01d0\u0001\u0000\u0000\u0000.\u01da\u0001"+
		"\u0000\u0000\u00000\u01e7\u0001\u0000\u0000\u00002\u01f2\u0001\u0000\u0000"+
		"\u00004\u01fa\u0001\u0000\u0000\u00006\u0202\u0001\u0000\u0000\u00008"+
		"\u0207\u0001\u0000\u0000\u0000:\u020d\u0001\u0000\u0000\u0000<\u0215\u0001"+
		"\u0000\u0000\u0000>\u0219\u0001\u0000\u0000\u0000@\u021b\u0001\u0000\u0000"+
		"\u0000B\u0260\u0001\u0000\u0000\u0000D\u0262\u0001\u0000\u0000\u0000F"+
		"\u026c\u0001\u0000\u0000\u0000H\u0270\u0001\u0000\u0000\u0000J\u0275\u0001"+
		"\u0000\u0000\u0000L\u027d\u0001\u0000\u0000\u0000N\u0282\u0001\u0000\u0000"+
		"\u0000P\u028a\u0001\u0000\u0000\u0000R\u028e\u0001\u0000\u0000\u0000T"+
		"\u0293\u0001\u0000\u0000\u0000V\u0299\u0001\u0000\u0000\u0000X\u02ab\u0001"+
		"\u0000\u0000\u0000Z\u02ad\u0001\u0000\u0000\u0000\\\u02b5\u0001\u0000"+
		"\u0000\u0000^\u02bd\u0001\u0000\u0000\u0000`\u02cd\u0001\u0000\u0000\u0000"+
		"b\u02d1\u0001\u0000\u0000\u0000d\u02da\u0001\u0000\u0000\u0000f\u02dc"+
		"\u0001\u0000\u0000\u0000h\u02e0\u0001\u0000\u0000\u0000j\u02e8\u0001\u0000"+
		"\u0000\u0000l\u02ea\u0001\u0000\u0000\u0000n\u02f1\u0001\u0000\u0000\u0000"+
		"p\u02f4\u0001\u0000\u0000\u0000r\u02fc\u0001\u0000\u0000\u0000t\u0304"+
		"\u0001\u0000\u0000\u0000v\u030c\u0001\u0000\u0000\u0000x\u0311\u0001\u0000"+
		"\u0000\u0000z\u0319\u0001\u0000\u0000\u0000|\u0321\u0001\u0000\u0000\u0000"+
		"~\u0329\u0001\u0000\u0000\u0000\u0080\u0330\u0001\u0000\u0000\u0000\u0082"+
		"\u0333\u0001\u0000\u0000\u0000\u0084\u033a\u0001\u0000\u0000\u0000\u0086"+
		"\u033d\u0001\u0000\u0000\u0000\u0088\u0344\u0001\u0000\u0000\u0000\u008a"+
		"\u0349\u0001\u0000\u0000\u0000\u008c\u034c\u0001\u0000\u0000\u0000\u008e"+
		"\u035a\u0001\u0000\u0000\u0000\u0090\u035e\u0001\u0000\u0000\u0000\u0092"+
		"\u0365\u0001\u0000\u0000\u0000\u0094\u0368\u0001\u0000\u0000\u0000\u0096"+
		"\u036f\u0001\u0000\u0000\u0000\u0098\u0372\u0001\u0000\u0000\u0000\u009a"+
		"\u037d\u0001\u0000\u0000\u0000\u009c\u0382\u0001\u0000\u0000\u0000\u009e"+
		"\u0384\u0001\u0000\u0000\u0000\u00a0\u0392\u0001\u0000\u0000\u0000\u00a2"+
		"\u0394\u0001\u0000\u0000\u0000\u00a4\u0396\u0001\u0000\u0000\u0000\u00a6"+
		"\u039a\u0001\u0000\u0000\u0000\u00a8\u03aa\u0001\u0000\u0000\u0000\u00aa"+
		"\u03ac\u0001\u0000\u0000\u0000\u00ac\u03b2\u0001\u0000\u0000\u0000\u00ae"+
		"\u03bb\u0001\u0000\u0000\u0000\u00b0\u03bd\u0001\u0000\u0000\u0000\u00b2"+
		"\u03bf\u0001\u0000\u0000\u0000\u00b4\u03c3\u0001\u0000\u0000\u0000\u00b6"+
		"\u03c5\u0001\u0000\u0000\u0000\u00b8\u03c7\u0001\u0000\u0000\u0000\u00ba"+
		"\u03cf\u0001\u0000\u0000\u0000\u00bc\u03d7\u0001\u0000\u0000\u0000\u00be"+
		"\u03e0\u0001\u0000\u0000\u0000\u00c0\u03e6\u0001\u0000\u0000\u0000\u00c2"+
		"\u03e8\u0001\u0000\u0000\u0000\u00c4\u03ea\u0001\u0000\u0000\u0000\u00c6"+
		"\u03f9\u0001\u0000\u0000\u0000\u00c8\u03ff\u0001\u0000\u0000\u0000\u00ca"+
		"\u0401\u0001\u0000\u0000\u0000\u00cc\u0409\u0001\u0000\u0000\u0000\u00ce"+
		"\u040d\u0001\u0000\u0000\u0000\u00d0\u041b\u0001\u0000\u0000\u0000\u00d2"+
		"\u041d\u0001\u0000\u0000\u0000\u00d4\u041f\u0001\u0000\u0000\u0000\u00d6"+
		"\u042e\u0001\u0000\u0000\u0000\u00d8\u0430\u0001\u0000\u0000\u0000\u00da"+
		"\u0438\u0001\u0000\u0000\u0000\u00dc\u043d\u0001\u0000\u0000\u0000\u00de"+
		"\u043f\u0001\u0000\u0000\u0000\u00e0\u0449\u0001\u0000\u0000\u0000\u00e2"+
		"\u0451\u0001\u0000\u0000\u0000\u00e4\u00e6\u0003\u0002\u0001\u0000\u00e5"+
		"\u00e4\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001\u0000\u0000\u0000\u00e6"+
		"\u00ea\u0001\u0000\u0000\u0000\u00e7\u00e9\u0003\u0004\u0002\u0000\u00e8"+
		"\u00e7\u0001\u0000\u0000\u0000\u00e9\u00ec\u0001\u0000\u0000\u0000\u00ea"+
		"\u00e8\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb"+
		"\u00ee\u0001\u0000\u0000\u0000\u00ec\u00ea\u0001\u0000\u0000\u0000\u00ed"+
		"\u00ef\u0003\u0006\u0003\u0000\u00ee\u00ed\u0001\u0000\u0000\u0000\u00ef"+
		"\u00f0\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f0"+
		"\u00f1\u0001\u0000\u0000\u0000\u00f1\u0001\u0001\u0000\u0000\u0000\u00f2"+
		"\u00f3\u0005\n\u0000\u0000\u00f3\u00f4\u00034\u001a\u0000\u00f4\u0003"+
		"\u0001\u0000\u0000\u0000\u00f5\u00f6\u0005\u000b\u0000\u0000\u00f6\u00f7"+
		"\u00034\u001a\u0000\u00f7\u0005\u0001\u0000\u0000\u0000\u00f8\u00fa\u0003"+
		"\u00d2i\u0000\u00f9\u00f8\u0001\u0000\u0000\u0000\u00fa\u00fd\u0001\u0000"+
		"\u0000\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001\u0000"+
		"\u0000\u0000\u00fc\u00fe\u0001\u0000\u0000\u0000\u00fd\u00fb\u0001\u0000"+
		"\u0000\u0000\u00fe\u00ff\u0003\b\u0004\u0000\u00ff\u0007\u0001\u0000\u0000"+
		"\u0000\u0100\u0104\u0003\n\u0005\u0000\u0101\u0104\u0003\u0016\u000b\u0000"+
		"\u0102\u0104\u0003\u001e\u000f\u0000\u0103\u0100\u0001\u0000\u0000\u0000"+
		"\u0103\u0101\u0001\u0000\u0000\u0000\u0103\u0102\u0001\u0000\u0000\u0000"+
		"\u0104\t\u0001\u0000\u0000\u0000\u0105\u0107\u0003\u00deo\u0000\u0106"+
		"\u0105\u0001\u0000\u0000\u0000\u0107\u010a\u0001\u0000\u0000\u0000\u0108"+
		"\u0106\u0001\u0000\u0000\u0000\u0108\u0109\u0001\u0000\u0000\u0000\u0109"+
		"\u010b\u0001\u0000\u0000\u0000\u010a\u0108\u0001\u0000\u0000\u0000\u010b"+
		"\u010c\u0005\u0007\u0000\u0000\u010c\u010e\u0003\u00acV\u0000\u010d\u010f"+
		"\u00030\u0018\u0000\u010e\u010d\u0001\u0000\u0000\u0000\u010e\u010f\u0001"+
		"\u0000\u0000\u0000\u010f\u011b\u0001\u0000\u0000\u0000\u0110\u0111\u0005"+
		"Z\u0000\u0000\u0111\u0116\u0003\f\u0006\u0000\u0112\u0113\u0005a\u0000"+
		"\u0000\u0113\u0115\u0003\f\u0006\u0000\u0114\u0112\u0001\u0000\u0000\u0000"+
		"\u0115\u0118\u0001\u0000\u0000\u0000\u0116\u0114\u0001\u0000\u0000\u0000"+
		"\u0116\u0117\u0001\u0000\u0000\u0000\u0117\u0119\u0001\u0000\u0000\u0000"+
		"\u0118\u0116\u0001\u0000\u0000\u0000\u0119\u011a\u0005[\u0000\u0000\u011a"+
		"\u011c\u0001\u0000\u0000\u0000\u011b\u0110\u0001\u0000\u0000\u0000\u011b"+
		"\u011c\u0001\u0000\u0000\u0000\u011c\u0129\u0001\u0000\u0000\u0000\u011d"+
		"\u011e\u0005>\u0000\u0000\u011e\u0120\u0003\u00b6[\u0000\u011f\u0121\u0003"+
		"\u00aaU\u0000\u0120\u011f\u0001\u0000\u0000\u0000\u0120\u0121\u0001\u0000"+
		"\u0000\u0000\u0121\u0126\u0001\u0000\u0000\u0000\u0122\u0123\u0005a\u0000"+
		"\u0000\u0123\u0125\u0003\u00b6[\u0000\u0124\u0122\u0001\u0000\u0000\u0000"+
		"\u0125\u0128\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000"+
		"\u0126\u0127\u0001\u0000\u0000\u0000\u0127\u012a\u0001\u0000\u0000\u0000"+
		"\u0128\u0126\u0001\u0000\u0000\u0000\u0129\u011d\u0001\u0000\u0000\u0000"+
		"\u0129\u012a\u0001\u0000\u0000\u0000\u012a\u012b\u0001\u0000\u0000\u0000"+
		"\u012b\u012c\u0003\u000e\u0007\u0000\u012c\u000b\u0001\u0000\u0000\u0000"+
		"\u012d\u012f\u0003\u00d0h\u0000\u012e\u012d\u0001\u0000\u0000\u0000\u012f"+
		"\u0132\u0001\u0000\u0000\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0130"+
		"\u0131\u0001\u0000\u0000\u0000\u0131\u0133\u0001\u0000\u0000\u0000\u0132"+
		"\u0130\u0001\u0000\u0000\u0000\u0133\u0136\u0003,\u0016\u0000\u0134\u0136"+
		"\u0003<\u001e\u0000\u0135\u0130\u0001\u0000\u0000\u0000\u0135\u0134\u0001"+
		"\u0000\u0000\u0000\u0136\r\u0001\u0000\u0000\u0000\u0137\u013b\u0005\\"+
		"\u0000\u0000\u0138\u013a\u0003\u0012\t\u0000\u0139\u0138\u0001\u0000\u0000"+
		"\u0000\u013a\u013d\u0001\u0000\u0000\u0000\u013b\u0139\u0001\u0000\u0000"+
		"\u0000\u013b\u013c\u0001\u0000\u0000\u0000\u013c\u013e\u0001\u0000\u0000"+
		"\u0000\u013d\u013b\u0001\u0000\u0000\u0000\u013e\u013f\u0005]\u0000\u0000"+
		"\u013f\u000f\u0001\u0000\u0000\u0000\u0140\u0145\u0003\u00b6[\u0000\u0141"+
		"\u0142\u0005a\u0000\u0000\u0142\u0144\u0003\u00b6[\u0000\u0143\u0141\u0001"+
		"\u0000\u0000\u0000\u0144\u0147\u0001\u0000\u0000\u0000\u0145\u0143\u0001"+
		"\u0000\u0000\u0000\u0145\u0146\u0001\u0000\u0000\u0000\u0146\u0011\u0001"+
		"\u0000\u0000\u0000\u0147\u0145\u0001\u0000\u0000\u0000\u0148\u014a\u0003"+
		"\u00d0h\u0000\u0149\u0148\u0001\u0000\u0000\u0000\u014a\u014d\u0001\u0000"+
		"\u0000\u0000\u014b\u0149\u0001\u0000\u0000\u0000\u014b\u014c\u0001\u0000"+
		"\u0000\u0000\u014c\u014e\u0001\u0000\u0000\u0000\u014d\u014b\u0001\u0000"+
		"\u0000\u0000\u014e\u0151\u0003*\u0015\u0000\u014f\u0151\u0003\u0014\n"+
		"\u0000\u0150\u014b\u0001\u0000\u0000\u0000\u0150\u014f\u0001\u0000\u0000"+
		"\u0000\u0151\u0013\u0001\u0000\u0000\u0000\u0152\u0153\u0005 \u0000\u0000"+
		"\u0153\u0154\u0003@ \u0000\u0154\u0015\u0001\u0000\u0000\u0000\u0155\u0157"+
		"\u0003\u00deo\u0000\u0156\u0155\u0001\u0000\u0000\u0000\u0157\u015a\u0001"+
		"\u0000\u0000\u0000\u0158\u0156\u0001\u0000\u0000\u0000\u0158\u0159\u0001"+
		"\u0000\u0000\u0000\u0159\u015b\u0001\u0000\u0000\u0000\u015a\u0158\u0001"+
		"\u0000\u0000\u0000\u015b\u015c\u0005\u000f\u0000\u0000\u015c\u0168\u0003"+
		"\u00acV\u0000\u015d\u015e\u0005Z\u0000\u0000\u015e\u0163\u0003\f\u0006"+
		"\u0000\u015f\u0160\u0005a\u0000\u0000\u0160\u0162\u0003\f\u0006\u0000"+
		"\u0161\u015f\u0001\u0000\u0000\u0000\u0162\u0165\u0001\u0000\u0000\u0000"+
		"\u0163\u0161\u0001\u0000\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000"+
		"\u0164\u0166\u0001\u0000\u0000\u0000\u0165\u0163\u0001\u0000\u0000\u0000"+
		"\u0166\u0167\u0005[\u0000\u0000\u0167\u0169\u0001\u0000\u0000\u0000\u0168"+
		"\u015d\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169"+
		"\u016c\u0001\u0000\u0000\u0000\u016a\u016b\u0005>\u0000\u0000\u016b\u016d"+
		"\u0003\u0010\b\u0000\u016c\u016a\u0001\u0000\u0000\u0000\u016c\u016d\u0001"+
		"\u0000\u0000\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016e\u0170\u0005"+
		"\\\u0000\u0000\u016f\u0171\u0003\u0018\f\u0000\u0170\u016f\u0001\u0000"+
		"\u0000\u0000\u0170\u0171\u0001\u0000\u0000\u0000\u0171\u0173\u0001\u0000"+
		"\u0000\u0000\u0172\u0174\u0005a\u0000\u0000\u0173\u0172\u0001\u0000\u0000"+
		"\u0000\u0173\u0174\u0001\u0000\u0000\u0000\u0174\u0176\u0001\u0000\u0000"+
		"\u0000\u0175\u0177\u0003\u001c\u000e\u0000\u0176\u0175\u0001\u0000\u0000"+
		"\u0000\u0176\u0177\u0001\u0000\u0000\u0000\u0177\u0178\u0001\u0000\u0000"+
		"\u0000\u0178\u0179\u0005]\u0000\u0000\u0179\u0017\u0001\u0000\u0000\u0000"+
		"\u017a\u017f\u0003\u001a\r\u0000\u017b\u017c\u0005a\u0000\u0000\u017c"+
		"\u017e\u0003\u001a\r\u0000\u017d\u017b\u0001\u0000\u0000\u0000\u017e\u0181"+
		"\u0001\u0000\u0000\u0000\u017f\u017d\u0001\u0000\u0000\u0000\u017f\u0180"+
		"\u0001\u0000\u0000\u0000\u0180\u0019\u0001\u0000\u0000\u0000\u0181\u017f"+
		"\u0001\u0000\u0000\u0000\u0182\u0184\u0003\u00acV\u0000\u0183\u0185\u0003"+
		"\u00aaU\u0000\u0184\u0183\u0001\u0000\u0000\u0000\u0184\u0185\u0001\u0000"+
		"\u0000\u0000\u0185\u0187\u0001\u0000\u0000\u0000\u0186\u0188\u0003\u000e"+
		"\u0007\u0000\u0187\u0186\u0001\u0000\u0000\u0000\u0187\u0188\u0001\u0000"+
		"\u0000\u0000\u0188\u001b\u0001\u0000\u0000\u0000\u0189\u018d\u0005`\u0000"+
		"\u0000\u018a\u018c\u0003\u0012\t\u0000\u018b\u018a\u0001\u0000\u0000\u0000"+
		"\u018c\u018f\u0001\u0000\u0000\u0000\u018d\u018b\u0001\u0000\u0000\u0000"+
		"\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u001d\u0001\u0000\u0000\u0000"+
		"\u018f\u018d\u0001\u0000\u0000\u0000\u0190\u0192\u0003\u00deo\u0000\u0191"+
		"\u0190\u0001\u0000\u0000\u0000\u0192\u0195\u0001\u0000\u0000\u0000\u0193"+
		"\u0191\u0001\u0000\u0000\u0000\u0193\u0194\u0001\u0000\u0000\u0000\u0194"+
		"\u0196\u0001\u0000\u0000\u0000\u0195\u0193\u0001\u0000\u0000\u0000\u0196"+
		"\u0197\u0005\u0019\u0000\u0000\u0197\u0199\u0003\u00acV\u0000\u0198\u019a"+
		"\u00030\u0018\u0000\u0199\u0198\u0001\u0000\u0000\u0000\u0199\u019a\u0001"+
		"\u0000\u0000\u0000\u019a\u019d\u0001\u0000\u0000\u0000\u019b\u019c\u0005"+
		">\u0000\u0000\u019c\u019e\u0003\u0010\b\u0000\u019d\u019b\u0001\u0000"+
		"\u0000\u0000\u019d\u019e\u0001\u0000\u0000\u0000\u019e\u019f\u0001\u0000"+
		"\u0000\u0000\u019f\u01a0\u0003 \u0010\u0000\u01a0\u001f\u0001\u0000\u0000"+
		"\u0000\u01a1\u01a5\u0005\\\u0000\u0000\u01a2\u01a4\u0003\"\u0011\u0000"+
		"\u01a3\u01a2\u0001\u0000\u0000\u0000\u01a4\u01a7\u0001\u0000\u0000\u0000"+
		"\u01a5\u01a3\u0001\u0000\u0000\u0000\u01a5\u01a6\u0001\u0000\u0000\u0000"+
		"\u01a6\u01a8\u0001\u0000\u0000\u0000\u01a7\u01a5\u0001\u0000\u0000\u0000"+
		"\u01a8\u01a9\u0005]\u0000\u0000\u01a9!\u0001\u0000\u0000\u0000\u01aa\u01ac"+
		"\u0003\u00d0h\u0000\u01ab\u01aa\u0001\u0000\u0000\u0000\u01ac\u01af\u0001"+
		"\u0000\u0000\u0000\u01ad\u01ab\u0001\u0000\u0000\u0000\u01ad\u01ae\u0001"+
		"\u0000\u0000\u0000\u01ae\u01b0\u0001\u0000\u0000\u0000\u01af\u01ad\u0001"+
		"\u0000\u0000\u0000\u01b0\u01b1\u0003$\u0012\u0000\u01b1#\u0001\u0000\u0000"+
		"\u0000\u01b2\u01b3\u0003&\u0013\u0000\u01b3%\u0001\u0000\u0000\u0000\u01b4"+
		"\u01b6\u0003(\u0014\u0000\u01b5\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b9"+
		"\u0001\u0000\u0000\u0000\u01b7\u01b5\u0001\u0000\u0000\u0000\u01b7\u01b8"+
		"\u0001\u0000\u0000\u0000\u01b8\u01ba\u0001\u0000\u0000\u0000\u01b9\u01b7"+
		"\u0001\u0000\u0000\u0000\u01ba\u01bb\u0005-\u0000\u0000\u01bb\u01bd\u0003"+
		"\u00acV\u0000\u01bc\u01be\u00030\u0018\u0000\u01bd\u01bc\u0001\u0000\u0000"+
		"\u0000\u01bd\u01be\u0001\u0000\u0000\u0000\u01be\u01bf\u0001\u0000\u0000"+
		"\u0000\u01bf\u01c2\u00038\u001c\u0000\u01c0\u01c1\u0005c\u0000\u0000\u01c1"+
		"\u01c3\u0003\u00b4Z\u0000\u01c2\u01c0\u0001\u0000\u0000\u0000\u01c2\u01c3"+
		"\u0001\u0000\u0000\u0000\u01c3\u01c6\u0001\u0000\u0000\u0000\u01c4\u01c5"+
		"\u0005%\u0000\u0000\u01c5\u01c7\u00032\u0019\u0000\u01c6\u01c4\u0001\u0000"+
		"\u0000\u0000\u01c6\u01c7\u0001\u0000\u0000\u0000\u01c7\'\u0001\u0000\u0000"+
		"\u0000\u01c8\u01c9\u0007\u0000\u0000\u0000\u01c9)\u0001\u0000\u0000\u0000"+
		"\u01ca\u01cf\u0003.\u0017\u0000\u01cb\u01cf\u0003,\u0016\u0000\u01cc\u01cf"+
		"\u0003@ \u0000\u01cd\u01cf\u0003\b\u0004\u0000\u01ce\u01ca\u0001\u0000"+
		"\u0000\u0000\u01ce\u01cb\u0001\u0000\u0000\u0000\u01ce\u01cc\u0001\u0000"+
		"\u0000\u0000\u01ce\u01cd\u0001\u0000\u0000\u0000\u01cf+\u0001\u0000\u0000"+
		"\u0000\u01d0\u01d1\u0007\u0001\u0000\u0000\u01d1\u01d4\u0003\u00acV\u0000"+
		"\u01d2\u01d3\u0005>\u0000\u0000\u01d3\u01d5\u0003\u00b6[\u0000\u01d4\u01d2"+
		"\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001\u0000\u0000\u0000\u01d5\u01d8"+
		"\u0001\u0000\u0000\u0000\u01d6\u01d7\u00057\u0000\u0000\u01d7\u01d9\u0003"+
		"j5\u0000\u01d8\u01d6\u0001\u0000\u0000\u0000\u01d8\u01d9\u0001\u0000\u0000"+
		"\u0000\u01d9-\u0001\u0000\u0000\u0000\u01da\u01db\u0005-\u0000\u0000\u01db"+
		"\u01dd\u0003\u00acV\u0000\u01dc\u01de\u00030\u0018\u0000\u01dd\u01dc\u0001"+
		"\u0000\u0000\u0000\u01dd\u01de\u0001\u0000\u0000\u0000\u01de\u01df\u0001"+
		"\u0000\u0000\u0000\u01df\u01e2\u00038\u001c\u0000\u01e0\u01e1\u0005c\u0000"+
		"\u0000\u01e1\u01e3\u0003\u00b4Z\u0000\u01e2\u01e0\u0001\u0000\u0000\u0000"+
		"\u01e2\u01e3\u0001\u0000\u0000\u0000\u01e3\u01e5\u0001\u0000\u0000\u0000"+
		"\u01e4\u01e6\u0003>\u001f\u0000\u01e5\u01e4\u0001\u0000\u0000\u0000\u01e5"+
		"\u01e6\u0001\u0000\u0000\u0000\u01e6/\u0001\u0000\u0000\u0000\u01e7\u01e8"+
		"\u00059\u0000\u0000\u01e8\u01ed\u00036\u001b\u0000\u01e9\u01ea\u0005a"+
		"\u0000\u0000\u01ea\u01ec\u00036\u001b\u0000\u01eb\u01e9\u0001\u0000\u0000"+
		"\u0000\u01ec\u01ef\u0001\u0000\u0000\u0000\u01ed\u01eb\u0001\u0000\u0000"+
		"\u0000\u01ed\u01ee\u0001\u0000\u0000\u0000\u01ee\u01f0\u0001\u0000\u0000"+
		"\u0000\u01ef\u01ed\u0001\u0000\u0000\u0000\u01f0\u01f1\u00058\u0000\u0000"+
		"\u01f11\u0001\u0000\u0000\u0000\u01f2\u01f7\u00034\u001a\u0000\u01f3\u01f4"+
		"\u0005a\u0000\u0000\u01f4\u01f6\u00034\u001a\u0000\u01f5\u01f3\u0001\u0000"+
		"\u0000\u0000\u01f6\u01f9\u0001\u0000\u0000\u0000\u01f7\u01f5\u0001\u0000"+
		"\u0000\u0000\u01f7\u01f8\u0001\u0000\u0000\u0000\u01f83\u0001\u0000\u0000"+
		"\u0000\u01f9\u01f7\u0001\u0000\u0000\u0000\u01fa\u01ff\u0003\u00acV\u0000"+
		"\u01fb\u01fc\u0005b\u0000\u0000\u01fc\u01fe\u0003\u00acV\u0000\u01fd\u01fb"+
		"\u0001\u0000\u0000\u0000\u01fe\u0201\u0001\u0000\u0000\u0000\u01ff\u01fd"+
		"\u0001\u0000\u0000\u0000\u01ff\u0200\u0001\u0000\u0000\u0000\u02005\u0001"+
		"\u0000\u0000\u0000\u0201\u01ff\u0001\u0000\u0000\u0000\u0202\u0205\u0003"+
		"\u00acV\u0000\u0203\u0204\u0005>\u0000\u0000\u0204\u0206\u0003\u00b6["+
		"\u0000\u0205\u0203\u0001\u0000\u0000\u0000\u0205\u0206\u0001\u0000\u0000"+
		"\u0000\u02067\u0001\u0000\u0000\u0000\u0207\u0209\u0005Z\u0000\u0000\u0208"+
		"\u020a\u0003:\u001d\u0000\u0209\u0208\u0001\u0000\u0000\u0000\u0209\u020a"+
		"\u0001\u0000\u0000\u0000\u020a\u020b\u0001\u0000\u0000\u0000\u020b\u020c"+
		"\u0005[\u0000\u0000\u020c9\u0001\u0000\u0000\u0000\u020d\u0212\u0003<"+
		"\u001e\u0000\u020e\u020f\u0005a\u0000\u0000\u020f\u0211\u0003<\u001e\u0000"+
		"\u0210\u020e\u0001\u0000\u0000\u0000\u0211\u0214\u0001\u0000\u0000\u0000"+
		"\u0212\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000\u0000\u0000"+
		"\u0213;\u0001\u0000\u0000\u0000\u0214\u0212\u0001\u0000\u0000\u0000\u0215"+
		"\u0216\u0003\u00acV\u0000\u0216\u0217\u0005>\u0000\u0000\u0217\u0218\u0003"+
		"\u00b6[\u0000\u0218=\u0001\u0000\u0000\u0000\u0219\u021a\u0003@ \u0000"+
		"\u021a?\u0001\u0000\u0000\u0000\u021b\u021f\u0005\\\u0000\u0000\u021c"+
		"\u021e\u0003B!\u0000\u021d\u021c\u0001\u0000\u0000\u0000\u021e\u0221\u0001"+
		"\u0000\u0000\u0000\u021f\u021d\u0001\u0000\u0000\u0000\u021f\u0220\u0001"+
		"\u0000\u0000\u0000\u0220\u0222\u0001\u0000\u0000\u0000\u0221\u021f\u0001"+
		"\u0000\u0000\u0000\u0222\u0223\u0005]\u0000\u0000\u0223A\u0001\u0000\u0000"+
		"\u0000\u0224\u0225\u0005(\u0000\u0000\u0225\u0226\u0003f3\u0000\u0226"+
		"\u0227\u0003B!\u0000\u0227\u0261\u0001\u0000\u0000\u0000\u0228\u0229\u0005"+
		")\u0000\u0000\u0229\u022a\u0003B!\u0000\u022a\u022b\u0005(\u0000\u0000"+
		"\u022b\u022c\u0003f3\u0000\u022c\u0261\u0001\u0000\u0000\u0000\u022d\u022e"+
		"\u0005\u0011\u0000\u0000\u022e\u022f\u0005Z\u0000\u0000\u022f\u0230\u0003"+
		"F#\u0000\u0230\u0231\u0005[\u0000\u0000\u0231\u0232\u0003B!\u0000\u0232"+
		"\u0261\u0001\u0000\u0000\u0000\u0233\u0234\u0005\u0012\u0000\u0000\u0234"+
		"\u0235\u0003f3\u0000\u0235\u0238\u0003B!\u0000\u0236\u0237\u0005\u000e"+
		"\u0000\u0000\u0237\u0239\u0003B!\u0000\u0238\u0236\u0001\u0000\u0000\u0000"+
		"\u0238\u0239\u0001\u0000\u0000\u0000\u0239\u0261\u0001\u0000\u0000\u0000"+
		"\u023a\u023b\u0005&\u0000\u0000\u023b\u023d\u0003@ \u0000\u023c\u023e"+
		"\u0003Z-\u0000\u023d\u023c\u0001\u0000\u0000\u0000\u023e\u023f\u0001\u0000"+
		"\u0000\u0000\u023f\u023d\u0001\u0000\u0000\u0000\u023f\u0240\u0001\u0000"+
		"\u0000\u0000\u0240\u0261\u0001\u0000\u0000\u0000\u0241\u0242\u0005\"\u0000"+
		"\u0000\u0242\u0246\u0005\\\u0000\u0000\u0243\u0245\u0003b1\u0000\u0244"+
		"\u0243\u0001\u0000\u0000\u0000\u0245\u0248\u0001\u0000\u0000\u0000\u0246"+
		"\u0244\u0001\u0000\u0000\u0000\u0246\u0247\u0001\u0000\u0000\u0000\u0247"+
		"\u0249\u0001\u0000\u0000\u0000\u0248\u0246\u0001\u0000\u0000\u0000\u0249"+
		"\u0261\u0005]\u0000\u0000\u024a\u024c\u0005\u001f\u0000\u0000\u024b\u024d"+
		"\u0003j5\u0000\u024c\u024b\u0001\u0000\u0000\u0000\u024c\u024d\u0001\u0000"+
		"\u0000\u0000\u024d\u0261\u0001\u0000\u0000\u0000\u024e\u024f\u0005$\u0000"+
		"\u0000\u024f\u0261\u0003j5\u0000\u0250\u0252\u00055\u0000\u0000\u0251"+
		"\u0253\u0003\u00acV\u0000\u0252\u0251\u0001\u0000\u0000\u0000\u0252\u0253"+
		"\u0001\u0000\u0000\u0000\u0253\u0261\u0001\u0000\u0000\u0000\u0254\u0256"+
		"\u00054\u0000\u0000\u0255\u0257\u0003\u00acV\u0000\u0256\u0255\u0001\u0000"+
		"\u0000\u0000\u0256\u0257\u0001\u0000\u0000\u0000\u0257\u0261\u0001\u0000"+
		"\u0000\u0000\u0258\u0259\u0003\u00acV\u0000\u0259\u025a\u0005>\u0000\u0000"+
		"\u025a\u025b\u0003B!\u0000\u025b\u0261\u0001\u0000\u0000\u0000\u025c\u0261"+
		"\u0005`\u0000\u0000\u025d\u0261\u0003j5\u0000\u025e\u0261\u0003D\"\u0000"+
		"\u025f\u0261\u0003@ \u0000\u0260\u0224\u0001\u0000\u0000\u0000\u0260\u0228"+
		"\u0001\u0000\u0000\u0000\u0260\u022d\u0001\u0000\u0000\u0000\u0260\u0233"+
		"\u0001\u0000\u0000\u0000\u0260\u023a\u0001\u0000\u0000\u0000\u0260\u0241"+
		"\u0001\u0000\u0000\u0000\u0260\u024a\u0001\u0000\u0000\u0000\u0260\u024e"+
		"\u0001\u0000\u0000\u0000\u0260\u0250\u0001\u0000\u0000\u0000\u0260\u0254"+
		"\u0001\u0000\u0000\u0000\u0260\u0258\u0001\u0000\u0000\u0000\u0260\u025c"+
		"\u0001\u0000\u0000\u0000\u0260\u025d\u0001\u0000\u0000\u0000\u0260\u025e"+
		"\u0001\u0000\u0000\u0000\u0260\u025f\u0001\u0000\u0000\u0000\u0261C\u0001"+
		"\u0000\u0000\u0000\u0262\u0263\u0007\u0001\u0000\u0000\u0263\u0266\u0003"+
		"\u00acV\u0000\u0264\u0265\u0005>\u0000\u0000\u0265\u0267\u0003\u00b6["+
		"\u0000\u0266\u0264\u0001\u0000\u0000\u0000\u0266\u0267\u0001\u0000\u0000"+
		"\u0000\u0267\u026a\u0001\u0000\u0000\u0000\u0268\u0269\u00057\u0000\u0000"+
		"\u0269\u026b\u0003j5\u0000\u026a\u0268\u0001\u0000\u0000\u0000\u026a\u026b"+
		"\u0001\u0000\u0000\u0000\u026bE\u0001\u0000\u0000\u0000\u026c\u026d\u0003"+
		"H$\u0000\u026d\u026e\u00053\u0000\u0000\u026e\u026f\u0003j5\u0000\u026f"+
		"G\u0001\u0000\u0000\u0000\u0270\u0273\u0003\u00acV\u0000\u0271\u0272\u0005"+
		">\u0000\u0000\u0272\u0274\u0003\u00b6[\u0000\u0273\u0271\u0001\u0000\u0000"+
		"\u0000\u0273\u0274\u0001\u0000\u0000\u0000\u0274I\u0001\u0000\u0000\u0000"+
		"\u0275\u027a\u0003L&\u0000\u0276\u0277\u0005a\u0000\u0000\u0277\u0279"+
		"\u0003L&\u0000\u0278\u0276\u0001\u0000\u0000\u0000\u0279\u027c\u0001\u0000"+
		"\u0000\u0000\u027a\u0278\u0001\u0000\u0000\u0000\u027a\u027b\u0001\u0000"+
		"\u0000\u0000\u027bK\u0001\u0000\u0000\u0000\u027c\u027a\u0001\u0000\u0000"+
		"\u0000\u027d\u027e\u0003\u00b6[\u0000\u027e\u027f\u0003\u00acV\u0000\u027f"+
		"\u0280\u00057\u0000\u0000\u0280\u0281\u0003j5\u0000\u0281M\u0001\u0000"+
		"\u0000\u0000\u0282\u0287\u0003P(\u0000\u0283\u0284\u0005a\u0000\u0000"+
		"\u0284\u0286\u0003P(\u0000\u0285\u0283\u0001\u0000\u0000\u0000\u0286\u0289"+
		"\u0001\u0000\u0000\u0000\u0287\u0285\u0001\u0000\u0000\u0000\u0287\u0288"+
		"\u0001\u0000\u0000\u0000\u0288O\u0001\u0000\u0000\u0000\u0289\u0287\u0001"+
		"\u0000\u0000\u0000\u028a\u028b\u0003\u00acV\u0000\u028b\u028c\u00057\u0000"+
		"\u0000\u028c\u028d\u0003j5\u0000\u028dQ\u0001\u0000\u0000\u0000\u028e"+
		"\u028f\u0005\u001b\u0000\u0000\u028f\u0290\u0003\u00cae\u0000\u0290\u0291"+
		"\u0003\u00aaU\u0000\u0291\u0292\u0003\u000e\u0007\u0000\u0292S\u0001\u0000"+
		"\u0000\u0000\u0293\u0294\u0005\u001b\u0000\u0000\u0294\u0295\u0003\u00b6"+
		"[\u0000\u0295\u0297\u0003\u00c8d\u0000\u0296\u0298\u0003V+\u0000\u0297"+
		"\u0296\u0001\u0000\u0000\u0000\u0297\u0298\u0001\u0000\u0000\u0000\u0298"+
		"U\u0001\u0000\u0000\u0000\u0299\u02a5\u0005\\\u0000\u0000\u029a\u029f"+
		"\u0003X,\u0000\u029b\u029c\u0005a\u0000\u0000\u029c\u029e\u0003X,\u0000"+
		"\u029d\u029b\u0001\u0000\u0000\u0000\u029e\u02a1\u0001\u0000\u0000\u0000"+
		"\u029f\u029d\u0001\u0000\u0000\u0000\u029f\u02a0\u0001\u0000\u0000\u0000"+
		"\u02a0\u02a3\u0001\u0000\u0000\u0000\u02a1\u029f\u0001\u0000\u0000\u0000"+
		"\u02a2\u02a4\u0005a\u0000\u0000\u02a3\u02a2\u0001\u0000\u0000\u0000\u02a3"+
		"\u02a4\u0001\u0000\u0000\u0000\u02a4\u02a6\u0001\u0000\u0000\u0000\u02a5"+
		"\u029a\u0001\u0000\u0000\u0000\u02a5\u02a6\u0001\u0000\u0000\u0000\u02a6"+
		"\u02a7\u0001\u0000\u0000\u0000\u02a7\u02a8\u0005]\u0000\u0000\u02a8W\u0001"+
		"\u0000\u0000\u0000\u02a9\u02ac\u0003V+\u0000\u02aa\u02ac\u0003j5\u0000"+
		"\u02ab\u02a9\u0001\u0000\u0000\u0000\u02ab\u02aa\u0001\u0000\u0000\u0000"+
		"\u02acY\u0001\u0000\u0000\u0000\u02ad\u02ae\u0005\u0005\u0000\u0000\u02ae"+
		"\u02af\u0005Z\u0000\u0000\u02af\u02b0\u0003\u00acV\u0000\u02b0\u02b1\u0005"+
		">\u0000\u0000\u02b1\u02b2\u0003\u00b6[\u0000\u02b2\u02b3\u0005[\u0000"+
		"\u0000\u02b3\u02b4\u0003@ \u0000\u02b4[\u0001\u0000\u0000\u0000\u02b5"+
		"\u02ba\u0003^/\u0000\u02b6\u02b7\u0005a\u0000\u0000\u02b7\u02b9\u0003"+
		"^/\u0000\u02b8\u02b6\u0001\u0000\u0000\u0000\u02b9\u02bc\u0001\u0000\u0000"+
		"\u0000\u02ba\u02b8\u0001\u0000\u0000\u0000\u02ba\u02bb\u0001\u0000\u0000"+
		"\u0000\u02bb]\u0001\u0000\u0000\u0000\u02bc\u02ba\u0001\u0000\u0000\u0000"+
		"\u02bd\u02be\u0003\u00acV\u0000\u02be\u02bf\u0005>\u0000\u0000\u02bf\u02c5"+
		"\u0005\\\u0000\u0000\u02c0\u02c1\u0003`0\u0000\u02c1\u02c2\u0005a\u0000"+
		"\u0000\u02c2\u02c4\u0001\u0000\u0000\u0000\u02c3\u02c0\u0001\u0000\u0000"+
		"\u0000\u02c4\u02c7\u0001\u0000\u0000\u0000\u02c5\u02c3\u0001\u0000\u0000"+
		"\u0000\u02c5\u02c6\u0001\u0000\u0000\u0000\u02c6\u02c8\u0001\u0000\u0000"+
		"\u0000\u02c7\u02c5\u0001\u0000\u0000\u0000\u02c8\u02c9\u0005\u0004\u0000"+
		"\u0000\u02c9\u02ca\u0005>\u0000\u0000\u02ca\u02cb\u0003j5\u0000\u02cb"+
		"\u02cc\u0005]\u0000\u0000\u02cc_\u0001\u0000\u0000\u0000\u02cd\u02ce\u0003"+
		"\u00acV\u0000\u02ce\u02cf\u0005>\u0000\u0000\u02cf\u02d0\u0003j5\u0000"+
		"\u02d0a\u0001\u0000\u0000\u0000\u02d1\u02d2\u0003d2\u0000\u02d2\u02d3"+
		"\u0003@ \u0000\u02d3c\u0001\u0000\u0000\u0000\u02d4\u02d5\u0005\u0003"+
		"\u0000\u0000\u02d5\u02d6\u0003j5\u0000\u02d6\u02d7\u0005c\u0000\u0000"+
		"\u02d7\u02db\u0001\u0000\u0000\u0000\u02d8\u02d9\u0005\u0004\u0000\u0000"+
		"\u02d9\u02db\u0005c\u0000\u0000\u02da\u02d4\u0001\u0000\u0000\u0000\u02da"+
		"\u02d8\u0001\u0000\u0000\u0000\u02dbe\u0001\u0000\u0000\u0000\u02dc\u02dd"+
		"\u0005Z\u0000\u0000\u02dd\u02de\u0003j5\u0000\u02de\u02df\u0005[\u0000"+
		"\u0000\u02dfg\u0001\u0000\u0000\u0000\u02e0\u02e5\u0003j5\u0000\u02e1"+
		"\u02e2\u0005a\u0000\u0000\u02e2\u02e4\u0003j5\u0000\u02e3\u02e1\u0001"+
		"\u0000\u0000\u0000\u02e4\u02e7\u0001\u0000\u0000\u0000\u02e5\u02e3\u0001"+
		"\u0000\u0000\u0000\u02e5\u02e6\u0001\u0000\u0000\u0000\u02e6i\u0001\u0000"+
		"\u0000\u0000\u02e7\u02e5\u0001\u0000\u0000\u0000\u02e8\u02e9\u0003l6\u0000"+
		"\u02e9k\u0001\u0000\u0000\u0000\u02ea\u02ee\u0003p8\u0000\u02eb\u02ed"+
		"\u0003n7\u0000\u02ec\u02eb\u0001\u0000\u0000\u0000\u02ed\u02f0\u0001\u0000"+
		"\u0000\u0000\u02ee\u02ec\u0001\u0000\u0000\u0000\u02ee\u02ef\u0001\u0000"+
		"\u0000\u0000\u02efm\u0001\u0000\u0000\u0000\u02f0\u02ee\u0001\u0000\u0000"+
		"\u0000\u02f1\u02f2\u0007\u0002\u0000\u0000\u02f2\u02f3\u0003l6\u0000\u02f3"+
		"o\u0001\u0000\u0000\u0000\u02f4\u02fa\u0003r9\u0000\u02f5\u02f6\u0005"+
		"=\u0000\u0000\u02f6\u02f7\u0003r9\u0000\u02f7\u02f8\u0005>\u0000\u0000"+
		"\u02f8\u02f9\u0003p8\u0000\u02f9\u02fb\u0001\u0000\u0000\u0000\u02fa\u02f5"+
		"\u0001\u0000\u0000\u0000\u02fa\u02fb\u0001\u0000\u0000\u0000\u02fbq\u0001"+
		"\u0000\u0000\u0000\u02fc\u0301\u0003t:\u0000\u02fd\u02fe\u0005D\u0000"+
		"\u0000\u02fe\u0300\u0003t:\u0000\u02ff\u02fd\u0001\u0000\u0000\u0000\u0300"+
		"\u0303\u0001\u0000\u0000\u0000\u0301\u02ff\u0001\u0000\u0000\u0000\u0301"+
		"\u0302\u0001\u0000\u0000\u0000\u0302s\u0001\u0000\u0000\u0000\u0303\u0301"+
		"\u0001\u0000\u0000\u0000\u0304\u0309\u0003v;\u0000\u0305\u0306\u0005C"+
		"\u0000\u0000\u0306\u0308\u0003v;\u0000\u0307\u0305\u0001\u0000\u0000\u0000"+
		"\u0308\u030b\u0001\u0000\u0000\u0000\u0309\u0307\u0001\u0000\u0000\u0000"+
		"\u0309\u030a\u0001\u0000\u0000\u0000\u030au\u0001\u0000\u0000\u0000\u030b"+
		"\u0309\u0001\u0000\u0000\u0000\u030c\u030f\u0003x<\u0000\u030d\u030e\u0005"+
		"f\u0000\u0000\u030e\u0310\u0003x<\u0000\u030f\u030d\u0001\u0000\u0000"+
		"\u0000\u030f\u0310\u0001\u0000\u0000\u0000\u0310w\u0001\u0000\u0000\u0000"+
		"\u0311\u0316\u0003z=\u0000\u0312\u0313\u0005L\u0000\u0000\u0313\u0315"+
		"\u0003z=\u0000\u0314\u0312\u0001\u0000\u0000\u0000\u0315\u0318\u0001\u0000"+
		"\u0000\u0000\u0316\u0314\u0001\u0000\u0000\u0000\u0316\u0317\u0001\u0000"+
		"\u0000\u0000\u0317y\u0001\u0000\u0000\u0000\u0318\u0316\u0001\u0000\u0000"+
		"\u0000\u0319\u031e\u0003|>\u0000\u031a\u031b\u0005K\u0000\u0000\u031b"+
		"\u031d\u0003|>\u0000\u031c\u031a\u0001\u0000\u0000\u0000\u031d\u0320\u0001"+
		"\u0000\u0000\u0000\u031e\u031c\u0001\u0000\u0000\u0000\u031e\u031f\u0001"+
		"\u0000\u0000\u0000\u031f{\u0001\u0000\u0000\u0000\u0320\u031e\u0001\u0000"+
		"\u0000\u0000\u0321\u0326\u0003~?\u0000\u0322\u0323\u0005M\u0000\u0000"+
		"\u0323\u0325\u0003~?\u0000\u0324\u0322\u0001\u0000\u0000\u0000\u0325\u0328"+
		"\u0001\u0000\u0000\u0000\u0326\u0324\u0001\u0000\u0000\u0000\u0326\u0327"+
		"\u0001\u0000\u0000\u0000\u0327}\u0001\u0000\u0000\u0000\u0328\u0326\u0001"+
		"\u0000\u0000\u0000\u0329\u032d\u0003\u0082A\u0000\u032a\u032c\u0003\u0080"+
		"@\u0000\u032b\u032a\u0001\u0000\u0000\u0000\u032c\u032f\u0001\u0000\u0000"+
		"\u0000\u032d\u032b\u0001\u0000\u0000\u0000\u032d\u032e\u0001\u0000\u0000"+
		"\u0000\u032e\u007f\u0001\u0000\u0000\u0000\u032f\u032d\u0001\u0000\u0000"+
		"\u0000\u0330\u0331\u0007\u0003\u0000\u0000\u0331\u0332\u0003\u0082A\u0000"+
		"\u0332\u0081\u0001\u0000\u0000\u0000\u0333\u0337\u0003\u0086C\u0000\u0334"+
		"\u0336\u0003\u0084B\u0000\u0335\u0334\u0001\u0000\u0000\u0000\u0336\u0339"+
		"\u0001\u0000\u0000\u0000\u0337\u0335\u0001\u0000\u0000\u0000\u0337\u0338"+
		"\u0001\u0000\u0000\u0000\u0338\u0083\u0001\u0000\u0000\u0000\u0339\u0337"+
		"\u0001\u0000\u0000\u0000\u033a\u033b\u0007\u0004\u0000\u0000\u033b\u033c"+
		"\u0003\u0086C\u0000\u033c\u0085\u0001\u0000\u0000\u0000\u033d\u0341\u0003"+
		"\u008cF\u0000\u033e\u0340\u0003\u0088D\u0000\u033f\u033e\u0001\u0000\u0000"+
		"\u0000\u0340\u0343\u0001\u0000\u0000\u0000\u0341\u033f\u0001\u0000\u0000"+
		"\u0000\u0341\u0342\u0001\u0000\u0000\u0000\u0342\u0087\u0001\u0000\u0000"+
		"\u0000\u0343\u0341\u0001\u0000\u0000\u0000\u0344\u0347\u0005\u0013\u0000"+
		"\u0000\u0345\u0348\u0003\u008aE\u0000\u0346\u0348\u0003\u00b6[\u0000\u0347"+
		"\u0345\u0001\u0000\u0000\u0000\u0347\u0346\u0001\u0000\u0000\u0000\u0348"+
		"\u0089\u0001\u0000\u0000\u0000\u0349\u034a\u0003\u00b6[\u0000\u034a\u034b"+
		"\u0003\u00acV\u0000\u034b\u008b\u0001\u0000\u0000\u0000\u034c\u0350\u0003"+
		"\u0090H\u0000\u034d\u034f\u0003\u008eG\u0000\u034e\u034d\u0001\u0000\u0000"+
		"\u0000\u034f\u0352\u0001\u0000\u0000\u0000\u0350\u034e\u0001\u0000\u0000"+
		"\u0000\u0350\u0351\u0001\u0000\u0000\u0000\u0351\u008d\u0001\u0000\u0000"+
		"\u0000\u0352\u0350\u0001\u0000\u0000\u0000\u0353\u0354\u00058\u0000\u0000"+
		"\u0354\u035b\u00058\u0000\u0000\u0355\u0356\u00059\u0000\u0000\u0356\u035b"+
		"\u00059\u0000\u0000\u0357\u0358\u00058\u0000\u0000\u0358\u0359\u00058"+
		"\u0000\u0000\u0359\u035b\u00058\u0000\u0000\u035a\u0353\u0001\u0000\u0000"+
		"\u0000\u035a\u0355\u0001\u0000\u0000\u0000\u035a\u0357\u0001\u0000\u0000"+
		"\u0000\u035b\u035c\u0001\u0000\u0000\u0000\u035c\u035d\u0003\u0090H\u0000"+
		"\u035d\u008f\u0001\u0000\u0000\u0000\u035e\u0362\u0003\u0094J\u0000\u035f"+
		"\u0361\u0003\u0092I\u0000\u0360\u035f\u0001\u0000\u0000\u0000\u0361\u0364"+
		"\u0001\u0000\u0000\u0000\u0362\u0360\u0001\u0000\u0000\u0000\u0362\u0363"+
		"\u0001\u0000\u0000\u0000\u0363\u0091\u0001\u0000\u0000\u0000\u0364\u0362"+
		"\u0001\u0000\u0000\u0000\u0365\u0366\u0007\u0005\u0000\u0000\u0366\u0367"+
		"\u0003\u0094J\u0000\u0367\u0093\u0001\u0000\u0000\u0000\u0368\u036c\u0003"+
		"\u0098L\u0000\u0369\u036b\u0003\u0096K\u0000\u036a\u0369\u0001\u0000\u0000"+
		"\u0000\u036b\u036e\u0001\u0000\u0000\u0000\u036c\u036a\u0001\u0000\u0000"+
		"\u0000\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u0095\u0001\u0000\u0000"+
		"\u0000\u036e\u036c\u0001\u0000\u0000\u0000\u036f\u0370\u0007\u0006\u0000"+
		"\u0000\u0370\u0371\u0003\u0098L\u0000\u0371\u0097\u0001\u0000\u0000\u0000"+
		"\u0372\u0377\u0003\u009aM\u0000\u0373\u0374\u00051\u0000\u0000\u0374\u0376"+
		"\u0003\u00b6[\u0000\u0375\u0373\u0001\u0000\u0000\u0000\u0376\u0379\u0001"+
		"\u0000\u0000\u0000\u0377\u0375\u0001\u0000\u0000\u0000\u0377\u0378\u0001"+
		"\u0000\u0000\u0000\u0378\u0099\u0001\u0000\u0000\u0000\u0379\u0377\u0001"+
		"\u0000\u0000\u0000\u037a\u037c\u0003\u009cN\u0000\u037b\u037a\u0001\u0000"+
		"\u0000\u0000\u037c\u037f\u0001\u0000\u0000\u0000\u037d\u037b\u0001\u0000"+
		"\u0000\u0000\u037d\u037e\u0001\u0000\u0000\u0000\u037e\u0380\u0001\u0000"+
		"\u0000\u0000\u037f\u037d\u0001\u0000\u0000\u0000\u0380\u0381\u0003\u009e"+
		"O\u0000\u0381\u009b\u0001\u0000\u0000\u0000\u0382\u0383\u0007\u0007\u0000"+
		"\u0000\u0383\u009d\u0001\u0000\u0000\u0000\u0384\u0388\u0003\u00a8T\u0000"+
		"\u0385\u0387\u0003\u00a0P\u0000\u0386\u0385\u0001\u0000\u0000\u0000\u0387"+
		"\u038a\u0001\u0000\u0000\u0000\u0388\u0386\u0001\u0000\u0000\u0000\u0388"+
		"\u0389\u0001\u0000\u0000\u0000\u0389\u009f\u0001\u0000\u0000\u0000\u038a"+
		"\u0388\u0001\u0000\u0000\u0000\u038b\u0393\u0007\b\u0000\u0000\u038c\u0393"+
		"\u0003\u00a2Q\u0000\u038d\u038e\u0005b\u0000\u0000\u038e\u0393\u0003R"+
		")\u0000\u038f\u0393\u0003\u00a4R\u0000\u0390\u0393\u0003\u00a6S\u0000"+
		"\u0391\u0393\u0003\u00ceg\u0000\u0392\u038b\u0001\u0000\u0000\u0000\u0392"+
		"\u038c\u0001\u0000\u0000\u0000\u0392\u038d\u0001\u0000\u0000\u0000\u0392"+
		"\u038f\u0001\u0000\u0000\u0000\u0392\u0390\u0001\u0000\u0000\u0000\u0392"+
		"\u0391\u0001\u0000\u0000\u0000\u0393\u00a1\u0001\u0000\u0000\u0000\u0394"+
		"\u0395\u0003\u00aaU\u0000\u0395\u00a3\u0001\u0000\u0000\u0000\u0396\u0397"+
		"\u0005^\u0000\u0000\u0397\u0398\u0003j5\u0000\u0398\u0399\u0005_\u0000"+
		"\u0000\u0399\u00a5\u0001\u0000\u0000\u0000\u039a\u039d\u0005b\u0000\u0000"+
		"\u039b\u039e\u0003\u00acV\u0000\u039c\u039e\u0005#\u0000\u0000\u039d\u039b"+
		"\u0001\u0000\u0000\u0000\u039d\u039c\u0001\u0000\u0000\u0000\u039e\u00a7"+
		"\u0001\u0000\u0000\u0000\u039f\u03a0\u0005Z\u0000\u0000\u03a0\u03a1\u0003"+
		"j5\u0000\u03a1\u03a2\u0005[\u0000\u0000\u03a2\u03ab\u0001\u0000\u0000"+
		"\u0000\u03a3\u03ab\u0005#\u0000\u0000\u03a4\u03ab\u0005!\u0000\u0000\u03a5"+
		"\u03ab\u0003R)\u0000\u03a6\u03ab\u0003T*\u0000\u03a7\u03ab\u0003\u00ae"+
		"W\u0000\u03a8\u03ab\u0003\u00acV\u0000\u03a9\u03ab\u0003\u00d4j\u0000"+
		"\u03aa\u039f\u0001\u0000\u0000\u0000\u03aa\u03a3\u0001\u0000\u0000\u0000"+
		"\u03aa\u03a4\u0001\u0000\u0000\u0000\u03aa\u03a5\u0001\u0000\u0000\u0000"+
		"\u03aa\u03a6\u0001\u0000\u0000\u0000\u03aa\u03a7\u0001\u0000\u0000\u0000"+
		"\u03aa\u03a8\u0001\u0000\u0000\u0000\u03aa\u03a9\u0001\u0000\u0000\u0000"+
		"\u03ab\u00a9\u0001\u0000\u0000\u0000\u03ac\u03ae\u0005Z\u0000\u0000\u03ad"+
		"\u03af\u0003h4\u0000\u03ae\u03ad\u0001\u0000\u0000\u0000\u03ae\u03af\u0001"+
		"\u0000\u0000\u0000\u03af\u03b0\u0001\u0000\u0000\u0000\u03b0\u03b1\u0005"+
		"[\u0000\u0000\u03b1\u00ab\u0001\u0000\u0000\u0000\u03b2\u03b3\u0007\t"+
		"\u0000\u0000\u03b3\u00ad\u0001\u0000\u0000\u0000\u03b4\u03bc\u0003\u00b0"+
		"X\u0000\u03b5\u03bc\u0003\u00b2Y\u0000\u03b6\u03bc\u0005n\u0000\u0000"+
		"\u03b7\u03bc\u0005o\u0000\u0000\u03b8\u03bc\u0005m\u0000\u0000\u03b9\u03bc"+
		"\u0005\t\u0000\u0000\u03ba\u03bc\u0005p\u0000\u0000\u03bb\u03b4\u0001"+
		"\u0000\u0000\u0000\u03bb\u03b5\u0001\u0000\u0000\u0000\u03bb\u03b6\u0001"+
		"\u0000\u0000\u0000\u03bb\u03b7\u0001\u0000\u0000\u0000\u03bb\u03b8\u0001"+
		"\u0000\u0000\u0000\u03bb\u03b9\u0001\u0000\u0000\u0000\u03bb\u03ba\u0001"+
		"\u0000\u0000\u0000\u03bc\u00af\u0001\u0000\u0000\u0000\u03bd\u03be\u0007"+
		"\n\u0000\u0000\u03be\u00b1\u0001\u0000\u0000\u0000\u03bf\u03c0\u0007\u000b"+
		"\u0000\u0000\u03c0\u00b3\u0001\u0000\u0000\u0000\u03c1\u03c4\u0005\'\u0000"+
		"\u0000\u03c2\u03c4\u0003\u00b6[\u0000\u03c3\u03c1\u0001\u0000\u0000\u0000"+
		"\u03c3\u03c2\u0001\u0000\u0000\u0000\u03c4\u00b5\u0001\u0000\u0000\u0000"+
		"\u03c5\u03c6\u0003\u00b8\\\u0000\u03c6\u00b7\u0001\u0000\u0000\u0000\u03c7"+
		"\u03cc\u0003\u00ba]\u0000\u03c8\u03c9\u0005L\u0000\u0000\u03c9\u03cb\u0003"+
		"\u00ba]\u0000\u03ca\u03c8\u0001\u0000\u0000\u0000\u03cb\u03ce\u0001\u0000"+
		"\u0000\u0000\u03cc\u03ca\u0001\u0000\u0000\u0000\u03cc\u03cd\u0001\u0000"+
		"\u0000\u0000\u03cd\u00b9\u0001\u0000\u0000\u0000\u03ce\u03cc\u0001\u0000"+
		"\u0000\u0000\u03cf\u03d4\u0003\u00bc^\u0000\u03d0\u03d1\u0005K\u0000\u0000"+
		"\u03d1\u03d3\u0003\u00bc^\u0000\u03d2\u03d0\u0001\u0000\u0000\u0000\u03d3"+
		"\u03d6\u0001\u0000\u0000\u0000\u03d4\u03d2\u0001\u0000\u0000\u0000\u03d4"+
		"\u03d5\u0001\u0000\u0000\u0000\u03d5\u00bb\u0001\u0000\u0000\u0000\u03d6"+
		"\u03d4\u0001\u0000\u0000\u0000\u03d7\u03db\u0003\u00c0`\u0000\u03d8\u03da"+
		"\u0003\u00be_\u0000\u03d9\u03d8\u0001\u0000\u0000\u0000\u03da\u03dd\u0001"+
		"\u0000\u0000\u0000\u03db\u03d9\u0001\u0000\u0000\u0000\u03db\u03dc\u0001"+
		"\u0000\u0000\u0000\u03dc\u00bd\u0001\u0000\u0000\u0000\u03dd\u03db\u0001"+
		"\u0000\u0000\u0000\u03de\u03e1\u0003\u00c8d\u0000\u03df\u03e1\u0005=\u0000"+
		"\u0000\u03e0\u03de\u0001\u0000\u0000\u0000\u03e0\u03df\u0001\u0000\u0000"+
		"\u0000\u03e1\u00bf\u0001\u0000\u0000\u0000\u03e2\u03e7\u0003\u00cae\u0000"+
		"\u03e3\u03e7\u0003\u00c2a\u0000\u03e4\u03e7\u0003\u00c4b\u0000\u03e5\u03e7"+
		"\u0003\u00c6c\u0000\u03e6\u03e2\u0001\u0000\u0000\u0000\u03e6\u03e3\u0001"+
		"\u0000\u0000\u0000\u03e6\u03e4\u0001\u0000\u0000\u0000\u03e6\u03e5\u0001"+
		"\u0000\u0000\u0000\u03e7\u00c1\u0001\u0000\u0000\u0000\u03e8\u03e9\u0007"+
		"\f\u0000\u0000\u03e9\u00c3\u0001\u0000\u0000\u0000\u03ea\u03f3\u0005Z"+
		"\u0000\u0000\u03eb\u03f0\u0003\u00b6[\u0000\u03ec\u03ed\u0005a\u0000\u0000"+
		"\u03ed\u03ef\u0003\u00b6[\u0000\u03ee\u03ec\u0001\u0000\u0000\u0000\u03ef"+
		"\u03f2\u0001\u0000\u0000\u0000\u03f0\u03ee\u0001\u0000\u0000\u0000\u03f0"+
		"\u03f1\u0001\u0000\u0000\u0000\u03f1\u03f4\u0001\u0000\u0000\u0000\u03f2"+
		"\u03f0\u0001\u0000\u0000\u0000\u03f3\u03eb\u0001\u0000\u0000\u0000\u03f3"+
		"\u03f4\u0001\u0000\u0000\u0000\u03f4\u03f5\u0001\u0000\u0000\u0000\u03f5"+
		"\u03f6\u0005[\u0000\u0000\u03f6\u03f7\u0005c\u0000\u0000\u03f7\u03f8\u0003"+
		"\u00b6[\u0000\u03f8\u00c5\u0001\u0000\u0000\u0000\u03f9\u03fa\u0005^\u0000"+
		"\u0000\u03fa\u03fb\u0003\u00b6[\u0000\u03fb\u03fc\u0005a\u0000\u0000\u03fc"+
		"\u03fd\u0003\u00b6[\u0000\u03fd\u03fe\u0005_\u0000\u0000\u03fe\u00c7\u0001"+
		"\u0000\u0000\u0000\u03ff\u0400\u0007\r\u0000\u0000\u0400\u00c9\u0001\u0000"+
		"\u0000\u0000\u0401\u0406\u0003\u00ccf\u0000\u0402\u0403\u0005b\u0000\u0000"+
		"\u0403\u0405\u0003\u00ccf\u0000\u0404\u0402\u0001\u0000\u0000\u0000\u0405"+
		"\u0408\u0001\u0000\u0000\u0000\u0406\u0404\u0001\u0000\u0000\u0000\u0406"+
		"\u0407\u0001\u0000\u0000\u0000\u0407\u00cb\u0001\u0000\u0000\u0000\u0408"+
		"\u0406\u0001\u0000\u0000\u0000\u0409\u040b\u0003\u00acV\u0000\u040a\u040c"+
		"\u0003\u00ceg\u0000\u040b\u040a\u0001\u0000\u0000\u0000\u040b\u040c\u0001"+
		"\u0000\u0000\u0000\u040c\u00cd\u0001\u0000\u0000\u0000\u040d\u040e\u0005"+
		"9\u0000\u0000\u040e\u0413\u0003\u00b6[\u0000\u040f\u0410\u0005a\u0000"+
		"\u0000\u0410\u0412\u0003\u00b6[\u0000\u0411\u040f\u0001\u0000\u0000\u0000"+
		"\u0412\u0415\u0001\u0000\u0000\u0000\u0413\u0411\u0001\u0000\u0000\u0000"+
		"\u0413\u0414\u0001\u0000\u0000\u0000\u0414\u0416\u0001\u0000\u0000\u0000"+
		"\u0415\u0413\u0001\u0000\u0000\u0000\u0416\u0417\u00058\u0000\u0000\u0417"+
		"\u00cf\u0001\u0000\u0000\u0000\u0418\u041c\u0003\u00d2i\u0000\u0419\u041c"+
		"\u0005\u001a\u0000\u0000\u041a\u041c\u0005,\u0000\u0000\u041b\u0418\u0001"+
		"\u0000\u0000\u0000\u041b\u0419\u0001\u0000\u0000\u0000\u041b\u041a\u0001"+
		"\u0000\u0000\u0000\u041c\u00d1\u0001\u0000\u0000\u0000\u041d\u041e\u0007"+
		"\u000e\u0000\u0000\u041e\u00d3\u0001\u0000\u0000\u0000\u041f\u0420\u0003"+
		"\u00d6k\u0000\u0420\u0426\u0005c\u0000\u0000\u0421\u0423\u0003\u00b4Z"+
		"\u0000\u0422\u0421\u0001\u0000\u0000\u0000\u0422\u0423\u0001\u0000\u0000"+
		"\u0000\u0423\u0424\u0001\u0000\u0000\u0000\u0424\u0427\u0003\u00dcn\u0000"+
		"\u0425\u0427\u0003j5\u0000\u0426\u0422\u0001\u0000\u0000\u0000\u0426\u0425"+
		"\u0001\u0000\u0000\u0000\u0427\u00d5\u0001\u0000\u0000\u0000\u0428\u042a"+
		"\u0005Z\u0000\u0000\u0429\u042b\u0003\u00d8l\u0000\u042a\u0429\u0001\u0000"+
		"\u0000\u0000\u042a\u042b\u0001\u0000\u0000\u0000\u042b\u042c\u0001\u0000"+
		"\u0000\u0000\u042c\u042f\u0005[\u0000\u0000\u042d\u042f\u0003\u00acV\u0000"+
		"\u042e\u0428\u0001\u0000\u0000\u0000\u042e\u042d\u0001\u0000\u0000\u0000"+
		"\u042f\u00d7\u0001\u0000\u0000\u0000\u0430\u0435\u0003\u00dam\u0000\u0431"+
		"\u0432\u0005a\u0000\u0000\u0432\u0434\u0003\u00dam\u0000\u0433\u0431\u0001"+
		"\u0000\u0000\u0000\u0434\u0437\u0001\u0000\u0000\u0000\u0435\u0433\u0001"+
		"\u0000\u0000\u0000\u0435\u0436\u0001\u0000\u0000\u0000\u0436\u00d9\u0001"+
		"\u0000\u0000\u0000\u0437\u0435\u0001\u0000\u0000\u0000\u0438\u043b\u0003"+
		"\u00acV\u0000\u0439\u043a\u0005>\u0000\u0000\u043a\u043c\u0003\u00b6["+
		"\u0000\u043b\u0439\u0001\u0000\u0000\u0000\u043b\u043c\u0001\u0000\u0000"+
		"\u0000\u043c\u00db\u0001\u0000\u0000\u0000\u043d\u043e\u0003@ \u0000\u043e"+
		"\u00dd\u0001\u0000\u0000\u0000\u043f\u0440\u0005e\u0000\u0000\u0440\u0447"+
		"\u0003\u00acV\u0000\u0441\u0444\u0005Z\u0000\u0000\u0442\u0445\u0003\u00e0"+
		"p\u0000\u0443\u0445\u0003j5\u0000\u0444\u0442\u0001\u0000\u0000\u0000"+
		"\u0444\u0443\u0001\u0000\u0000\u0000\u0444\u0445\u0001\u0000\u0000\u0000"+
		"\u0445\u0446\u0001\u0000\u0000\u0000\u0446\u0448\u0005[\u0000\u0000\u0447"+
		"\u0441\u0001\u0000\u0000\u0000\u0447\u0448\u0001\u0000\u0000\u0000\u0448"+
		"\u00df\u0001\u0000\u0000\u0000\u0449\u044e\u0003\u00e2q\u0000\u044a\u044b"+
		"\u0005a\u0000\u0000\u044b\u044d\u0003\u00e2q\u0000\u044c\u044a\u0001\u0000"+
		"\u0000\u0000\u044d\u0450\u0001\u0000\u0000\u0000\u044e\u044c\u0001\u0000"+
		"\u0000\u0000\u044e\u044f\u0001\u0000\u0000\u0000\u044f\u00e1\u0001\u0000"+
		"\u0000\u0000\u0450\u044e\u0001\u0000\u0000\u0000\u0451\u0452\u0003\u00ac"+
		"V\u0000\u0452\u0453\u00057\u0000\u0000\u0453\u0454\u0003j5\u0000\u0454"+
		"\u00e3\u0001\u0000\u0000\u0000u\u00e5\u00ea\u00f0\u00fb\u0103\u0108\u010e"+
		"\u0116\u011b\u0120\u0126\u0129\u0130\u0135\u013b\u0145\u014b\u0150\u0158"+
		"\u0163\u0168\u016c\u0170\u0173\u0176\u017f\u0184\u0187\u018d\u0193\u0199"+
		"\u019d\u01a5\u01ad\u01b7\u01bd\u01c2\u01c6\u01ce\u01d4\u01d8\u01dd\u01e2"+
		"\u01e5\u01ed\u01f7\u01ff\u0205\u0209\u0212\u021f\u0238\u023f\u0246\u024c"+
		"\u0252\u0256\u0260\u0266\u026a\u0273\u027a\u0287\u0297\u029f\u02a3\u02a5"+
		"\u02ab\u02ba\u02c5\u02da\u02e5\u02ee\u02fa\u0301\u0309\u030f\u0316\u031e"+
		"\u0326\u032d\u0337\u0341\u0347\u0350\u035a\u0362\u036c\u0377\u037d\u0388"+
		"\u0392\u039d\u03aa\u03ae\u03bb\u03c3\u03cc\u03d4\u03db\u03e0\u03e6\u03f0"+
		"\u03f3\u0406\u040b\u0413\u041b\u0422\u0426\u042a\u042e\u0435\u043b\u0444"+
		"\u0447\u044e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}