// Generated from AssemblyParser.g4 by ANTLR 4.13.2
package org.metavm.asm.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class AssemblyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ABSTRACT=1, BOOLEAN=2, CASE=3, DEFAULT=4, CATCH=5, STRING=6, CLASS=7, 
		RECORD=8, INDEX=9, UNIQUE=10, STRUCT=11, TIME=12, NULL=13, PACKAGE=14, 
		IMPORT=15, PASSWORD=16, DOUBLE=17, ELSE=18, ENUM=19, EXTENDS=20, READONLY=21, 
		CHILD=22, TITLE=23, FINALLY=24, FOR=25, IF=26, IMPLEMENTS=27, INSTANCEOF=28, 
		BYTE=29, SHORT=30, INT=31, LONG=32, CHAR=33, INTERFACE=34, NATIVE=35, 
		ENEW=36, UNEW=37, NEW=38, ALLOCATE=39, PRIVATE=40, PROTECTED=41, PUBLIC=42, 
		RETURN=43, STATIC=44, SUPER=45, SWITCH=46, THIS=47, THROW=48, THROWS=49, 
		TRY=50, VOID=51, WHILE=52, ANY=53, NEVER=54, SELECT=55, SELECT_FIRST=56, 
		DELETED=57, VAR=58, ASSIGN=59, GT=60, LT=61, BANG=62, BANGBANG=63, TILDE=64, 
		QUESTION=65, COLON=66, EQUAL=67, LE=68, GE=69, NOTEQUAL=70, AND=71, OR=72, 
		INC=73, DEC=74, ADD=75, SUB=76, MUL=77, DIV=78, BITAND=79, BITOR=80, CARET=81, 
		MOD=82, ADD_ASSIGN=83, SUB_ASSIGN=84, MUL_ASSIGN=85, DIV_ASSIGN=86, AND_ASSIGN=87, 
		OR_ASSIGN=88, XOR_ASSIGN=89, MOD_ASSIGN=90, LSHIFT_ASSIGN=91, RSHIFT_ASSIGN=92, 
		URSHIFT_ASSIGN=93, LPAREN=94, RPAREN=95, LBRACE=96, RBRACE=97, LBRACK=98, 
		RBRACK=99, SEMI=100, COMMA=101, DOT=102, ARROW=103, COLONCOLON=104, AT=105, 
		ELLIPSIS=106, DECIMAL_LITERAL=107, HEX_LITERAL=108, OCT_LITERAL=109, BINARY_LITERAL=110, 
		FLOAT_LITERAL=111, HEX_FLOAT_LITERAL=112, BOOL_LITERAL=113, CHAR_LITERAL=114, 
		STRING_LITERAL=115, TEXT_BLOCK=116, R=117, RW=118, C=119, V=120, IDENTIFIER=121, 
		WS=122, COMMENT=123, LINE_COMMENT=124;
	public static final int
		RULE_compilationUnit = 0, RULE_packageDeclaration = 1, RULE_importDeclaration = 2, 
		RULE_typeDeclaration = 3, RULE_classDeclaration = 4, RULE_classBody = 5, 
		RULE_typeList = 6, RULE_classBodyDeclaration = 7, RULE_staticBlock = 8, 
		RULE_enumDeclaration = 9, RULE_enumConstants = 10, RULE_enumConstant = 11, 
		RULE_enumBodyDeclarations = 12, RULE_interfaceDeclaration = 13, RULE_interfaceBody = 14, 
		RULE_interfaceBodyDeclaration = 15, RULE_interfaceMemberDeclaration = 16, 
		RULE_interfaceMethodDeclaration = 17, RULE_interfaceMethodModifier = 18, 
		RULE_interfaceCommonBodyDeclaration = 19, RULE_memberDeclaration = 20, 
		RULE_fieldDeclaration = 21, RULE_methodDeclaration = 22, RULE_constructorDeclaration = 23, 
		RULE_typeParameters = 24, RULE_qualifiedNameList = 25, RULE_qualifiedName = 26, 
		RULE_typeParameter = 27, RULE_formalParameters = 28, RULE_receiverParameter = 29, 
		RULE_formalParameterList = 30, RULE_formalParameter = 31, RULE_methodBody = 32, 
		RULE_block = 33, RULE_statement = 34, RULE_localVariableDeclaration = 35, 
		RULE_allocator = 36, RULE_allocatorFieldList = 37, RULE_allocatorField = 38, 
		RULE_select = 39, RULE_forControl = 40, RULE_loopVariableDeclarators = 41, 
		RULE_loopVariableDeclarator = 42, RULE_loopVariableUpdates = 43, RULE_loopVariableUpdate = 44, 
		RULE_qualifiedFieldName = 45, RULE_creator = 46, RULE_arrayCreatorRest = 47, 
		RULE_arrayInitializer = 48, RULE_variableInitializer = 49, RULE_createdName = 50, 
		RULE_classCreatorRest = 51, RULE_catchClause = 52, RULE_catchFields = 53, 
		RULE_catchField = 54, RULE_catchValue = 55, RULE_branchCase = 56, RULE_switchLabel = 57, 
		RULE_parExpression = 58, RULE_expressionList = 59, RULE_expression = 60, 
		RULE_primary = 61, RULE_explicitGenericInvocation = 62, RULE_explicitGenericInvocationSuffix = 63, 
		RULE_superSuffix = 64, RULE_arguments = 65, RULE_classType = 66, RULE_methodCall = 67, 
		RULE_literal = 68, RULE_integerLiteral = 69, RULE_floatLiteral = 70, RULE_typeTypeOrVoid = 71, 
		RULE_typeType = 72, RULE_arrayKind = 73, RULE_classOrInterfaceType = 74, 
		RULE_typeArguments = 75, RULE_primitiveType = 76, RULE_modifier = 77, 
		RULE_classOrInterfaceModifier = 78, RULE_lambdaExpression = 79, RULE_lambdaParameters = 80, 
		RULE_lambdaBody = 81, RULE_indexDeclaration = 82, RULE_indexField = 83, 
		RULE_annotation = 84;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "packageDeclaration", "importDeclaration", "typeDeclaration", 
			"classDeclaration", "classBody", "typeList", "classBodyDeclaration", 
			"staticBlock", "enumDeclaration", "enumConstants", "enumConstant", "enumBodyDeclarations", 
			"interfaceDeclaration", "interfaceBody", "interfaceBodyDeclaration", 
			"interfaceMemberDeclaration", "interfaceMethodDeclaration", "interfaceMethodModifier", 
			"interfaceCommonBodyDeclaration", "memberDeclaration", "fieldDeclaration", 
			"methodDeclaration", "constructorDeclaration", "typeParameters", "qualifiedNameList", 
			"qualifiedName", "typeParameter", "formalParameters", "receiverParameter", 
			"formalParameterList", "formalParameter", "methodBody", "block", "statement", 
			"localVariableDeclaration", "allocator", "allocatorFieldList", "allocatorField", 
			"select", "forControl", "loopVariableDeclarators", "loopVariableDeclarator", 
			"loopVariableUpdates", "loopVariableUpdate", "qualifiedFieldName", "creator", 
			"arrayCreatorRest", "arrayInitializer", "variableInitializer", "createdName", 
			"classCreatorRest", "catchClause", "catchFields", "catchField", "catchValue", 
			"branchCase", "switchLabel", "parExpression", "expressionList", "expression", 
			"primary", "explicitGenericInvocation", "explicitGenericInvocationSuffix", 
			"superSuffix", "arguments", "classType", "methodCall", "literal", "integerLiteral", 
			"floatLiteral", "typeTypeOrVoid", "typeType", "arrayKind", "classOrInterfaceType", 
			"typeArguments", "primitiveType", "modifier", "classOrInterfaceModifier", 
			"lambdaExpression", "lambdaParameters", "lambdaBody", "indexDeclaration", 
			"indexField", "annotation"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'boolean'", "'case'", "'default'", "'catch'", "'string'", 
			"'class'", "'record'", "'index'", "'unique'", "'struct'", "'time'", "'null'", 
			"'package'", "'import'", "'password'", "'double'", "'else'", "'enum'", 
			"'extends'", "'readonly'", "'child'", "'title'", "'finally'", "'for'", 
			"'if'", "'implements'", "'instanceof'", "'byte'", "'short'", "'int'", 
			"'long'", "'char'", "'interface'", "'native'", "'enew'", "'unew'", "'new'", 
			"'allocate'", "'private'", "'protected'", "'public'", "'return'", "'static'", 
			"'super'", "'switch'", "'this'", "'throw'", "'throws'", "'try'", "'void'", 
			"'while'", "'any'", "'never'", "'select'", "'selectFirst'", "'deleted'", 
			"'var'", "'='", "'>'", "'<'", "'!'", "'!!'", "'~'", "'?'", "':'", "'=='", 
			"'<='", "'>='", "'!='", "'&&'", "'||'", "'++'", "'--'", "'+'", "'-'", 
			"'*'", "'/'", "'&'", "'|'", "'^'", "'%'", "'+='", "'-='", "'*='", "'/='", 
			"'&='", "'|='", "'^='", "'%='", "'<<='", "'>>='", "'>>>='", "'('", "')'", 
			"'{'", "'}'", "'['", "']'", "';'", "','", "'.'", "'->'", "'::'", "'@'", 
			"'...'", null, null, null, null, null, null, null, null, null, null, 
			"'[r]'", "'[rw]'", "'[c]'", "'[v]'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSTRACT", "BOOLEAN", "CASE", "DEFAULT", "CATCH", "STRING", "CLASS", 
			"RECORD", "INDEX", "UNIQUE", "STRUCT", "TIME", "NULL", "PACKAGE", "IMPORT", 
			"PASSWORD", "DOUBLE", "ELSE", "ENUM", "EXTENDS", "READONLY", "CHILD", 
			"TITLE", "FINALLY", "FOR", "IF", "IMPLEMENTS", "INSTANCEOF", "BYTE", 
			"SHORT", "INT", "LONG", "CHAR", "INTERFACE", "NATIVE", "ENEW", "UNEW", 
			"NEW", "ALLOCATE", "PRIVATE", "PROTECTED", "PUBLIC", "RETURN", "STATIC", 
			"SUPER", "SWITCH", "THIS", "THROW", "THROWS", "TRY", "VOID", "WHILE", 
			"ANY", "NEVER", "SELECT", "SELECT_FIRST", "DELETED", "VAR", "ASSIGN", 
			"GT", "LT", "BANG", "BANGBANG", "TILDE", "QUESTION", "COLON", "EQUAL", 
			"LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", "ADD", "SUB", "MUL", 
			"DIV", "BITAND", "BITOR", "CARET", "MOD", "ADD_ASSIGN", "SUB_ASSIGN", 
			"MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", "XOR_ASSIGN", 
			"MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", "LPAREN", 
			"RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", 
			"ARROW", "COLONCOLON", "AT", "ELLIPSIS", "DECIMAL_LITERAL", "HEX_LITERAL", 
			"OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", 
			"BOOL_LITERAL", "CHAR_LITERAL", "STRING_LITERAL", "TEXT_BLOCK", "R", 
			"RW", "C", "V", "IDENTIFIER", "WS", "COMMENT", "LINE_COMMENT"
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
	public String getGrammarFileName() { return "AssemblyParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public AssemblyParser(TokenStream input) {
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
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
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PACKAGE) {
				{
				setState(170);
				packageDeclaration();
				}
			}

			setState(176);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(173);
				importDeclaration();
				}
				}
				setState(178);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(180); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(179);
				typeDeclaration();
				}
				}
				setState(182); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 25305947834754L) != 0) || _la==SEMI || _la==AT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode PACKAGE() { return getToken(AssemblyParser.PACKAGE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public PackageDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterPackageDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitPackageDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitPackageDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PackageDeclarationContext packageDeclaration() throws RecognitionException {
		PackageDeclarationContext _localctx = new PackageDeclarationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_packageDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(PACKAGE);
			setState(185);
			qualifiedName();
			setState(186);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode IMPORT() { return getToken(AssemblyParser.IMPORT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterImportDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitImportDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitImportDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportDeclarationContext importDeclaration() throws RecognitionException {
		ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			match(IMPORT);
			setState(189);
			qualifiedName();
			setState(190);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public TypeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeDeclarationContext typeDeclaration() throws RecognitionException {
		TypeDeclarationContext _localctx = new TypeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_typeDeclaration);
		int _la;
		try {
			setState(204);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case CLASS:
			case RECORD:
			case STRUCT:
			case ENUM:
			case INTERFACE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 25288767438850L) != 0)) {
					{
					{
					setState(192);
					classOrInterfaceModifier();
					}
					}
					setState(197);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(201);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(198);
					classDeclaration();
					}
					break;
				case 2:
					{
					setState(199);
					enumDeclaration();
					}
					break;
				case 3:
					{
					setState(200);
					interfaceDeclaration();
					}
					break;
				}
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(203);
				match(SEMI);
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
	public static class ClassDeclarationContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public TerminalNode CLASS() { return getToken(AssemblyParser.CLASS, 0); }
		public TerminalNode STRUCT() { return getToken(AssemblyParser.STRUCT, 0); }
		public TerminalNode RECORD() { return getToken(AssemblyParser.RECORD, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(AssemblyParser.EXTENDS, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode IMPLEMENTS() { return getToken(AssemblyParser.IMPLEMENTS, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public ClassDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassDeclaration(this);
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
			setState(209);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(206);
				annotation();
				}
				}
				setState(211);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(212);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2432L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(213);
			match(IDENTIFIER);
			setState(215);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(214);
				typeParameters();
				}
			}

			setState(219);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(217);
				match(EXTENDS);
				setState(218);
				typeType(0);
				}
			}

			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(221);
				match(IMPLEMENTS);
				setState(222);
				typeList();
				}
			}

			setState(225);
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
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassBody(this);
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
			setState(227);
			match(LBRACE);
			setState(231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2479256934652524102L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217745L) != 0)) {
				{
				{
				setState(228);
				classBodyDeclaration();
				}
				}
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(234);
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
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeList(this);
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
			setState(236);
			typeType(0);
			setState(241);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(237);
				match(COMMA);
				setState(238);
				typeType(0);
				}
				}
				setState(243);
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassBodyDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassBodyDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassBodyDeclarationContext classBodyDeclaration() throws RecognitionException {
		ClassBodyDeclarationContext _localctx = new ClassBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_classBodyDeclaration);
		int _la;
		try {
			setState(252);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(247);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 144140511217714178L) != 0)) {
					{
					{
					setState(244);
					modifier();
					}
					}
					setState(249);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(250);
				memberDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(251);
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
		public TerminalNode STATIC() { return getToken(AssemblyParser.STATIC, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public StaticBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_staticBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterStaticBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitStaticBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitStaticBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StaticBlockContext staticBlock() throws RecognitionException {
		StaticBlockContext _localctx = new StaticBlockContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_staticBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(254);
			match(STATIC);
			setState(255);
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
		public TerminalNode ENUM() { return getToken(AssemblyParser.ENUM, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode IMPLEMENTS() { return getToken(AssemblyParser.IMPLEMENTS, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(AssemblyParser.COMMA, 0); }
		public EnumBodyDeclarationsContext enumBodyDeclarations() {
			return getRuleContext(EnumBodyDeclarationsContext.class,0);
		}
		public EnumDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterEnumDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitEnumDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitEnumDeclaration(this);
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
			setState(260);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(257);
				annotation();
				}
				}
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(263);
			match(ENUM);
			setState(264);
			match(IDENTIFIER);
			setState(267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(265);
				match(IMPLEMENTS);
				setState(266);
				typeList();
				}
			}

			setState(269);
			match(LBRACE);
			setState(271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(270);
				enumConstants();
				}
			}

			setState(274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(273);
				match(COMMA);
				}
			}

			setState(277);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(276);
				enumBodyDeclarations();
				}
			}

			setState(279);
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public EnumConstantsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstants; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterEnumConstants(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitEnumConstants(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitEnumConstants(this);
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
			setState(281);
			enumConstant();
			setState(286);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(282);
					match(COMMA);
					setState(283);
					enumConstant();
					}
					} 
				}
				setState(288);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
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
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public EnumConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterEnumConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitEnumConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitEnumConstant(this);
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
			setState(289);
			match(IDENTIFIER);
			setState(291);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(290);
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
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterEnumBodyDeclarations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitEnumBodyDeclarations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitEnumBodyDeclarations(this);
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
			setState(293);
			match(SEMI);
			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2479256934652524102L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217745L) != 0)) {
				{
				{
				setState(294);
				classBodyDeclaration();
				}
				}
				setState(299);
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
		public TerminalNode INTERFACE() { return getToken(AssemblyParser.INTERFACE, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
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
		public TerminalNode EXTENDS() { return getToken(AssemblyParser.EXTENDS, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public InterfaceDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceDeclaration(this);
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
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(300);
				annotation();
				}
				}
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(306);
			match(INTERFACE);
			setState(307);
			match(IDENTIFIER);
			setState(309);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(308);
				typeParameters();
				}
			}

			setState(313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(311);
				match(EXTENDS);
				setState(312);
				typeList();
				}
			}

			setState(315);
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
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceBody(this);
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
			setState(317);
			match(LBRACE);
			setState(321);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2479256934652523606L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217809L) != 0)) {
				{
				{
				setState(318);
				interfaceBodyDeclaration();
				}
				}
				setState(323);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(324);
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
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public InterfaceBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceBodyDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceBodyDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceBodyDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceBodyDeclarationContext interfaceBodyDeclaration() throws RecognitionException {
		InterfaceBodyDeclarationContext _localctx = new InterfaceBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			setState(334);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case BOOLEAN:
			case DEFAULT:
			case STRING:
			case UNIQUE:
			case TIME:
			case NULL:
			case PASSWORD:
			case DOUBLE:
			case READONLY:
			case CHILD:
			case TITLE:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case CHAR:
			case NATIVE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case VOID:
			case ANY:
			case NEVER:
			case DELETED:
			case LT:
			case LPAREN:
			case LBRACK:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(329);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(326);
						modifier();
						}
						} 
					}
					setState(331);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
				}
				setState(332);
				interfaceMemberDeclaration();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(333);
				match(SEMI);
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceMemberDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceMemberDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMemberDeclarationContext interfaceMemberDeclaration() throws RecognitionException {
		InterfaceMemberDeclarationContext _localctx = new InterfaceMemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_interfaceMemberDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
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
		public InterfaceCommonBodyDeclarationContext interfaceCommonBodyDeclaration() {
			return getRuleContext(InterfaceCommonBodyDeclarationContext.class,0);
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
		public InterfaceMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMethodDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceMethodDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceMethodDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceMethodDeclaration(this);
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
			setState(341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 21990232555538L) != 0)) {
				{
				{
				setState(338);
				interfaceMethodModifier();
				}
				}
				setState(343);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(345);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(344);
				typeParameters();
				}
			}

			setState(347);
			interfaceCommonBodyDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode PUBLIC() { return getToken(AssemblyParser.PUBLIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(AssemblyParser.ABSTRACT, 0); }
		public TerminalNode DEFAULT() { return getToken(AssemblyParser.DEFAULT, 0); }
		public TerminalNode STATIC() { return getToken(AssemblyParser.STATIC, 0); }
		public InterfaceMethodModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMethodModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceMethodModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceMethodModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceMethodModifier(this);
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
			setState(349);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 21990232555538L) != 0)) ) {
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
	public static class InterfaceCommonBodyDeclarationContext extends ParserRuleContext {
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public List<TerminalNode> LBRACK() { return getTokens(AssemblyParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(AssemblyParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(AssemblyParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(AssemblyParser.RBRACK, i);
		}
		public TerminalNode THROWS() { return getToken(AssemblyParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public InterfaceCommonBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceCommonBodyDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterInterfaceCommonBodyDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitInterfaceCommonBodyDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitInterfaceCommonBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceCommonBodyDeclarationContext interfaceCommonBodyDeclaration() throws RecognitionException {
		InterfaceCommonBodyDeclarationContext _localctx = new InterfaceCommonBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_interfaceCommonBodyDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(351);
			typeTypeOrVoid();
			setState(352);
			match(IDENTIFIER);
			setState(353);
			formalParameters();
			setState(358);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(354);
				match(LBRACK);
				setState(355);
				match(RBRACK);
				}
				}
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(363);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(361);
				match(THROWS);
				setState(362);
				qualifiedNameList();
				}
			}

			setState(365);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public IndexDeclarationContext indexDeclaration() {
			return getRuleContext(IndexDeclarationContext.class,0);
		}
		public MemberDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterMemberDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitMemberDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclarationContext memberDeclaration() throws RecognitionException {
		MemberDeclarationContext _localctx = new MemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_memberDeclaration);
		try {
			setState(371);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				methodDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(368);
				fieldDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(369);
				constructorDeclaration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(370);
				indexDeclaration();
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
	public static class FieldDeclarationContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public FieldDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterFieldDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitFieldDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitFieldDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldDeclarationContext fieldDeclaration() throws RecognitionException {
		FieldDeclarationContext _localctx = new FieldDeclarationContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(373);
			typeType(0);
			setState(374);
			match(IDENTIFIER);
			setState(375);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public MethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterMethodDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitMethodDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitMethodDeclaration(this);
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
			setState(378);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(377);
				typeParameters();
				}
			}

			setState(380);
			typeTypeOrVoid();
			setState(381);
			match(IDENTIFIER);
			setState(382);
			formalParameters();
			setState(383);
			methodBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode THROWS() { return getToken(AssemblyParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public ConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterConstructorDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitConstructorDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitConstructorDeclaration(this);
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
			setState(386);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(385);
				typeParameters();
				}
			}

			setState(388);
			match(IDENTIFIER);
			setState(389);
			formalParameters();
			setState(392);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(390);
				match(THROWS);
				setState(391);
				qualifiedNameList();
				}
			}

			setState(394);
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
		public TerminalNode LT() { return getToken(AssemblyParser.LT, 0); }
		public List<TypeParameterContext> typeParameter() {
			return getRuleContexts(TypeParameterContext.class);
		}
		public TypeParameterContext typeParameter(int i) {
			return getRuleContext(TypeParameterContext.class,i);
		}
		public TerminalNode GT() { return getToken(AssemblyParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public TypeParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeParameters(this);
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
			setState(396);
			match(LT);
			setState(397);
			typeParameter();
			setState(402);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(398);
				match(COMMA);
				setState(399);
				typeParameter();
				}
				}
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(405);
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public QualifiedNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterQualifiedNameList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitQualifiedNameList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitQualifiedNameList(this);
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
			setState(407);
			qualifiedName();
			setState(412);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(408);
				match(COMMA);
				setState(409);
				qualifiedName();
				}
				}
				setState(414);
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
		public List<TerminalNode> IDENTIFIER() { return getTokens(AssemblyParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(AssemblyParser.IDENTIFIER, i);
		}
		public List<TerminalNode> DOT() { return getTokens(AssemblyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(AssemblyParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(415);
			match(IDENTIFIER);
			setState(420);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(416);
					match(DOT);
					setState(417);
					match(IDENTIFIER);
					}
					} 
				}
				setState(422);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
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
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode EXTENDS() { return getToken(AssemblyParser.EXTENDS, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TypeParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeParameter(this);
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
			setState(423);
			match(IDENTIFIER);
			setState(426);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(424);
				match(EXTENDS);
				setState(425);
				typeType(0);
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
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
		}
		public FormalParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterFormalParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitFormalParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitFormalParameters(this);
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
			setState(428);
			match(LPAREN);
			setState(430);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29273414221115460L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217745L) != 0)) {
				{
				setState(429);
				formalParameterList();
				}
			}

			setState(432);
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
	public static class ReceiverParameterContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode THIS() { return getToken(AssemblyParser.THIS, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(AssemblyParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(AssemblyParser.IDENTIFIER, i);
		}
		public List<TerminalNode> DOT() { return getTokens(AssemblyParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(AssemblyParser.DOT, i);
		}
		public ReceiverParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_receiverParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterReceiverParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitReceiverParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitReceiverParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReceiverParameterContext receiverParameter() throws RecognitionException {
		ReceiverParameterContext _localctx = new ReceiverParameterContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_receiverParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
			typeType(0);
			setState(439);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(435);
				match(IDENTIFIER);
				setState(436);
				match(DOT);
				}
				}
				setState(441);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(442);
			match(THIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public FormalParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterFormalParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitFormalParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitFormalParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterListContext formalParameterList() throws RecognitionException {
		FormalParameterListContext _localctx = new FormalParameterListContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(444);
			formalParameter();
			setState(449);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(445);
				match(COMMA);
				setState(446);
				formalParameter();
				}
				}
				setState(451);
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
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public FormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterFormalParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitFormalParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterContext formalParameter() throws RecognitionException {
		FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(452);
			typeType(0);
			setState(453);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public MethodBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterMethodBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitMethodBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitMethodBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodBodyContext methodBody() throws RecognitionException {
		MethodBodyContext _localctx = new MethodBodyContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_methodBody);
		try {
			setState(457);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(455);
				block();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(456);
				match(SEMI);
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
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(459);
			match(LBRACE);
			setState(463);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 5043443291958489156L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113678210670081L) != 0)) {
				{
				{
				setState(460);
				statement();
				}
				}
				setState(465);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(466);
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
		public TerminalNode WHILE() { return getToken(AssemblyParser.WHILE, 0); }
		public ParExpressionContext parExpression() {
			return getRuleContext(ParExpressionContext.class,0);
		}
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public TerminalNode FOR() { return getToken(AssemblyParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public ForControlContext forControl() {
			return getRuleContext(ForControlContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public TerminalNode IF() { return getToken(AssemblyParser.IF, 0); }
		public TerminalNode ELSE() { return getToken(AssemblyParser.ELSE, 0); }
		public TerminalNode TRY() { return getToken(AssemblyParser.TRY, 0); }
		public CatchClauseContext catchClause() {
			return getRuleContext(CatchClauseContext.class,0);
		}
		public TerminalNode SWITCH() { return getToken(AssemblyParser.SWITCH, 0); }
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public List<BranchCaseContext> branchCase() {
			return getRuleContexts(BranchCaseContext.class);
		}
		public BranchCaseContext branchCase(int i) {
			return getRuleContext(BranchCaseContext.class,i);
		}
		public TerminalNode RETURN() { return getToken(AssemblyParser.RETURN, 0); }
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode THROW() { return getToken(AssemblyParser.THROW, 0); }
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_statement);
		int _la;
		try {
			setState(514);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(468);
				match(WHILE);
				setState(469);
				parExpression();
				setState(470);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(472);
				match(FOR);
				setState(473);
				match(LPAREN);
				setState(474);
				forControl();
				setState(475);
				match(RPAREN);
				setState(476);
				block();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(478);
				match(IF);
				setState(479);
				parExpression();
				setState(480);
				block();
				setState(483);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(481);
					match(ELSE);
					setState(482);
					block();
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(485);
				match(TRY);
				setState(486);
				block();
				setState(487);
				catchClause();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(489);
				match(SWITCH);
				setState(490);
				match(LBRACE);
				setState(494);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(491);
					branchCase();
					}
					}
					setState(496);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(497);
				match(RBRACE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(498);
				match(RETURN);
				setState(500);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113592311324161L) != 0)) {
					{
					setState(499);
					expression(0);
					}
				}

				setState(502);
				match(SEMI);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(503);
				match(THROW);
				setState(504);
				expression(0);
				setState(505);
				match(SEMI);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(507);
				match(SEMI);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(508);
				((StatementContext)_localctx).statementExpression = expression(0);
				setState(509);
				match(SEMI);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(511);
				localVariableDeclaration();
				setState(512);
				match(SEMI);
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
		public TerminalNode VAR() { return getToken(AssemblyParser.VAR, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode ASSIGN() { return getToken(AssemblyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public LocalVariableDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localVariableDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLocalVariableDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLocalVariableDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLocalVariableDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocalVariableDeclarationContext localVariableDeclaration() throws RecognitionException {
		LocalVariableDeclarationContext _localctx = new LocalVariableDeclarationContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_localVariableDeclaration);
		int _la;
		try {
			setState(526);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(516);
				match(VAR);
				setState(517);
				match(IDENTIFIER);
				setState(518);
				match(ASSIGN);
				setState(519);
				expression(0);
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
			case ANY:
			case NEVER:
			case LPAREN:
			case LBRACK:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(520);
				typeType(0);
				setState(521);
				match(IDENTIFIER);
				setState(524);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ASSIGN) {
					{
					setState(522);
					match(ASSIGN);
					setState(523);
					expression(0);
					}
				}

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
	public static class AllocatorContext extends ParserRuleContext {
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public AllocatorFieldListContext allocatorFieldList() {
			return getRuleContext(AllocatorFieldListContext.class,0);
		}
		public AllocatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allocator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterAllocator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitAllocator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitAllocator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllocatorContext allocator() throws RecognitionException {
		AllocatorContext _localctx = new AllocatorContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_allocator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528);
			classOrInterfaceType();
			setState(529);
			match(LBRACE);
			setState(531);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(530);
				allocatorFieldList();
				}
			}

			setState(533);
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
	public static class AllocatorFieldListContext extends ParserRuleContext {
		public List<AllocatorFieldContext> allocatorField() {
			return getRuleContexts(AllocatorFieldContext.class);
		}
		public AllocatorFieldContext allocatorField(int i) {
			return getRuleContext(AllocatorFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public AllocatorFieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allocatorFieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterAllocatorFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitAllocatorFieldList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitAllocatorFieldList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllocatorFieldListContext allocatorFieldList() throws RecognitionException {
		AllocatorFieldListContext _localctx = new AllocatorFieldListContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_allocatorFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(535);
			allocatorField();
			setState(540);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(536);
				match(COMMA);
				setState(537);
				allocatorField();
				}
				}
				setState(542);
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
	public static class AllocatorFieldContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode COLON() { return getToken(AssemblyParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AllocatorFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allocatorField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterAllocatorField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitAllocatorField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitAllocatorField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllocatorFieldContext allocatorField() throws RecognitionException {
		AllocatorFieldContext _localctx = new AllocatorFieldContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_allocatorField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			match(IDENTIFIER);
			setState(544);
			match(COLON);
			setState(545);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public TerminalNode SELECT() { return getToken(AssemblyParser.SELECT, 0); }
		public TerminalNode SELECT_FIRST() { return getToken(AssemblyParser.SELECT_FIRST, 0); }
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public SelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterSelect(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitSelect(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitSelect(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectContext select() throws RecognitionException {
		SelectContext _localctx = new SelectContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_select);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			_la = _input.LA(1);
			if ( !(_la==SELECT || _la==SELECT_FIRST) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(548);
			match(LPAREN);
			setState(549);
			qualifiedName();
			setState(550);
			match(DOT);
			setState(551);
			match(IDENTIFIER);
			setState(556);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(552);
				match(COMMA);
				setState(553);
				expression(0);
				}
				}
				setState(558);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(559);
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
	public static class ForControlContext extends ParserRuleContext {
		public LoopVariableUpdatesContext forUpdate;
		public List<TerminalNode> SEMI() { return getTokens(AssemblyParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(AssemblyParser.SEMI, i);
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterForControl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitForControl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForControlContext forControl() throws RecognitionException {
		ForControlContext _localctx = new ForControlContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_forControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(562);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29273414221115460L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217745L) != 0)) {
				{
				setState(561);
				loopVariableDeclarators();
				}
			}

			setState(564);
			match(SEMI);
			setState(566);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113592311324161L) != 0)) {
				{
				setState(565);
				expression(0);
				}
			}

			setState(568);
			match(SEMI);
			setState(570);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(569);
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public LoopVariableDeclaratorsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableDeclarators; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLoopVariableDeclarators(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLoopVariableDeclarators(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLoopVariableDeclarators(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableDeclaratorsContext loopVariableDeclarators() throws RecognitionException {
		LoopVariableDeclaratorsContext _localctx = new LoopVariableDeclaratorsContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_loopVariableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(572);
			loopVariableDeclarator();
			setState(577);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(573);
				match(COMMA);
				setState(574);
				loopVariableDeclarator();
				}
				}
				setState(579);
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
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode ASSIGN() { return getToken(AssemblyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LoopVariableDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLoopVariableDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLoopVariableDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLoopVariableDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableDeclaratorContext loopVariableDeclarator() throws RecognitionException {
		LoopVariableDeclaratorContext _localctx = new LoopVariableDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_loopVariableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(580);
			typeType(0);
			setState(581);
			match(IDENTIFIER);
			setState(582);
			match(ASSIGN);
			setState(583);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public LoopVariableUpdatesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableUpdates; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLoopVariableUpdates(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLoopVariableUpdates(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLoopVariableUpdates(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableUpdatesContext loopVariableUpdates() throws RecognitionException {
		LoopVariableUpdatesContext _localctx = new LoopVariableUpdatesContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_loopVariableUpdates);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(585);
			loopVariableUpdate();
			setState(590);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(586);
				match(COMMA);
				setState(587);
				loopVariableUpdate();
				}
				}
				setState(592);
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
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode ASSIGN() { return getToken(AssemblyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LoopVariableUpdateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopVariableUpdate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLoopVariableUpdate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLoopVariableUpdate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLoopVariableUpdate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopVariableUpdateContext loopVariableUpdate() throws RecognitionException {
		LoopVariableUpdateContext _localctx = new LoopVariableUpdateContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_loopVariableUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(593);
			match(IDENTIFIER);
			setState(594);
			match(ASSIGN);
			setState(595);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedFieldNameContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public QualifiedFieldNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedFieldName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterQualifiedFieldName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitQualifiedFieldName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitQualifiedFieldName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedFieldNameContext qualifiedFieldName() throws RecognitionException {
		QualifiedFieldNameContext _localctx = new QualifiedFieldNameContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_qualifiedFieldName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			qualifiedName();
			setState(598);
			match(DOT);
			setState(599);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreatorContext extends ParserRuleContext {
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
		}
		public CreatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_creator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCreator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCreator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCreator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreatorContext creator() throws RecognitionException {
		CreatorContext _localctx = new CreatorContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_creator);
		int _la;
		try {
			setState(610);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(602);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(601);
					typeArguments();
					}
				}

				setState(604);
				classOrInterfaceType();
				setState(605);
				arguments();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(607);
				typeType(0);
				setState(608);
				arrayKind();
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
	public static class ArrayCreatorRestContext extends ParserRuleContext {
		public List<TerminalNode> LBRACK() { return getTokens(AssemblyParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(AssemblyParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(AssemblyParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(AssemblyParser.RBRACK, i);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public ArrayCreatorRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayCreatorRest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterArrayCreatorRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitArrayCreatorRest(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitArrayCreatorRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayCreatorRestContext arrayCreatorRest() throws RecognitionException {
		ArrayCreatorRestContext _localctx = new ArrayCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_arrayCreatorRest);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(612);
			match(LBRACK);
			setState(613);
			match(RBRACK);
			setState(618);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(614);
				match(LBRACK);
				setState(615);
				match(RBRACK);
				}
				}
				setState(620);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(621);
			arrayInitializer();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public List<VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitArrayInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayInitializerContext arrayInitializer() throws RecognitionException {
		ArrayInitializerContext _localctx = new ArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(623);
			match(LBRACE);
			setState(635);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113596606291457L) != 0)) {
				{
				setState(624);
				variableInitializer();
				setState(629);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(625);
						match(COMMA);
						setState(626);
						variableInitializer();
						}
						} 
					}
					setState(631);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
				}
				setState(633);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(632);
					match(COMMA);
					}
				}

				}
			}

			setState(637);
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterVariableInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitVariableInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitVariableInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableInitializerContext variableInitializer() throws RecognitionException {
		VariableInitializerContext _localctx = new VariableInitializerContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_variableInitializer);
		try {
			setState(641);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(639);
				arrayInitializer();
				}
				break;
			case NULL:
			case ENEW:
			case UNEW:
			case NEW:
			case ALLOCATE:
			case SUPER:
			case THIS:
			case SELECT:
			case SELECT_FIRST:
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
				setState(640);
				expression(0);
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
	public static class CreatedNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public CreatedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createdName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCreatedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCreatedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCreatedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreatedNameContext createdName() throws RecognitionException {
		CreatedNameContext _localctx = new CreatedNameContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_createdName);
		int _la;
		try {
			setState(648);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(643);
				match(IDENTIFIER);
				setState(645);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(644);
					typeArguments();
					}
				}

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
				enterOuterAlt(_localctx, 2);
				{
				setState(647);
				primitiveType();
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
	public static class ClassCreatorRestContext extends ParserRuleContext {
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ClassCreatorRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classCreatorRest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassCreatorRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassCreatorRest(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassCreatorRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassCreatorRestContext classCreatorRest() throws RecognitionException {
		ClassCreatorRestContext _localctx = new ClassCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(650);
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
	public static class CatchClauseContext extends ParserRuleContext {
		public TerminalNode CATCH() { return getToken(AssemblyParser.CATCH, 0); }
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public CatchFieldsContext catchFields() {
			return getRuleContext(CatchFieldsContext.class,0);
		}
		public CatchClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCatchClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCatchClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCatchClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchClauseContext catchClause() throws RecognitionException {
		CatchClauseContext _localctx = new CatchClauseContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_catchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(652);
			match(CATCH);
			setState(653);
			match(LBRACE);
			setState(655);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(654);
				catchFields();
				}
			}

			setState(657);
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public CatchFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCatchFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCatchFields(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCatchFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchFieldsContext catchFields() throws RecognitionException {
		CatchFieldsContext _localctx = new CatchFieldsContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_catchFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(659);
			catchField();
			setState(664);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(660);
				match(COMMA);
				setState(661);
				catchField();
				}
				}
				setState(666);
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
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public List<TerminalNode> COLON() { return getTokens(AssemblyParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(AssemblyParser.COLON, i);
		}
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode DEFAULT() { return getToken(AssemblyParser.DEFAULT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public List<CatchValueContext> catchValue() {
			return getRuleContexts(CatchValueContext.class);
		}
		public CatchValueContext catchValue(int i) {
			return getRuleContext(CatchValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public CatchFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCatchField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCatchField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCatchField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchFieldContext catchField() throws RecognitionException {
		CatchFieldContext _localctx = new CatchFieldContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_catchField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(667);
			match(IDENTIFIER);
			setState(668);
			match(COLON);
			setState(669);
			match(LBRACE);
			setState(675);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(670);
				catchValue();
				setState(671);
				match(COMMA);
				}
				}
				setState(677);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(678);
			match(DEFAULT);
			setState(679);
			match(COLON);
			setState(680);
			expression(0);
			setState(681);
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
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode COLON() { return getToken(AssemblyParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CatchValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterCatchValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitCatchValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitCatchValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchValueContext catchValue() throws RecognitionException {
		CatchValueContext _localctx = new CatchValueContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_catchValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(683);
			match(IDENTIFIER);
			setState(684);
			match(COLON);
			setState(685);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterBranchCase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitBranchCase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitBranchCase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BranchCaseContext branchCase() throws RecognitionException {
		BranchCaseContext _localctx = new BranchCaseContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_branchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(687);
			switchLabel();
			setState(688);
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
		public TerminalNode CASE() { return getToken(AssemblyParser.CASE, 0); }
		public TerminalNode ARROW() { return getToken(AssemblyParser.ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(AssemblyParser.DEFAULT, 0); }
		public SwitchLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterSwitchLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitSwitchLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitSwitchLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchLabelContext switchLabel() throws RecognitionException {
		SwitchLabelContext _localctx = new SwitchLabelContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_switchLabel);
		try {
			setState(696);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(690);
				match(CASE);
				{
				setState(691);
				expression(0);
				}
				setState(692);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(694);
				match(DEFAULT);
				setState(695);
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
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public ParExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterParExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitParExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitParExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParExpressionContext parExpression() throws RecognitionException {
		ParExpressionContext _localctx = new ParExpressionContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(698);
			match(LPAREN);
			setState(699);
			expression(0);
			setState(700);
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
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(702);
			expression(0);
			setState(707);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(703);
				match(COMMA);
				setState(704);
				expression(0);
				}
				}
				setState(709);
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
		public TypeTypeContext castType;
		public Token prefix;
		public Token bop;
		public Token postfix;
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public TerminalNode THIS() { return getToken(AssemblyParser.THIS, 0); }
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode SUPER() { return getToken(AssemblyParser.SUPER, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public TerminalNode NEW() { return getToken(AssemblyParser.NEW, 0); }
		public TerminalNode UNEW() { return getToken(AssemblyParser.UNEW, 0); }
		public TerminalNode ENEW() { return getToken(AssemblyParser.ENEW, 0); }
		public TerminalNode ALLOCATE() { return getToken(AssemblyParser.ALLOCATE, 0); }
		public AllocatorContext allocator() {
			return getRuleContext(AllocatorContext.class,0);
		}
		public SelectContext select() {
			return getRuleContext(SelectContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode ADD() { return getToken(AssemblyParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(AssemblyParser.SUB, 0); }
		public TerminalNode INC() { return getToken(AssemblyParser.INC, 0); }
		public TerminalNode DEC() { return getToken(AssemblyParser.DEC, 0); }
		public TerminalNode TILDE() { return getToken(AssemblyParser.TILDE, 0); }
		public TerminalNode BANG() { return getToken(AssemblyParser.BANG, 0); }
		public LambdaExpressionContext lambdaExpression() {
			return getRuleContext(LambdaExpressionContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public TerminalNode MUL() { return getToken(AssemblyParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(AssemblyParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(AssemblyParser.MOD, 0); }
		public List<TerminalNode> LT() { return getTokens(AssemblyParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(AssemblyParser.LT, i);
		}
		public List<TerminalNode> GT() { return getTokens(AssemblyParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(AssemblyParser.GT, i);
		}
		public TerminalNode LE() { return getToken(AssemblyParser.LE, 0); }
		public TerminalNode GE() { return getToken(AssemblyParser.GE, 0); }
		public TerminalNode EQUAL() { return getToken(AssemblyParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(AssemblyParser.NOTEQUAL, 0); }
		public TerminalNode BITAND() { return getToken(AssemblyParser.BITAND, 0); }
		public TerminalNode CARET() { return getToken(AssemblyParser.CARET, 0); }
		public TerminalNode BITOR() { return getToken(AssemblyParser.BITOR, 0); }
		public TerminalNode AND() { return getToken(AssemblyParser.AND, 0); }
		public TerminalNode OR() { return getToken(AssemblyParser.OR, 0); }
		public TerminalNode COLON() { return getToken(AssemblyParser.COLON, 0); }
		public TerminalNode QUESTION() { return getToken(AssemblyParser.QUESTION, 0); }
		public TerminalNode ASSIGN() { return getToken(AssemblyParser.ASSIGN, 0); }
		public TerminalNode ADD_ASSIGN() { return getToken(AssemblyParser.ADD_ASSIGN, 0); }
		public TerminalNode SUB_ASSIGN() { return getToken(AssemblyParser.SUB_ASSIGN, 0); }
		public TerminalNode MUL_ASSIGN() { return getToken(AssemblyParser.MUL_ASSIGN, 0); }
		public TerminalNode DIV_ASSIGN() { return getToken(AssemblyParser.DIV_ASSIGN, 0); }
		public TerminalNode AND_ASSIGN() { return getToken(AssemblyParser.AND_ASSIGN, 0); }
		public TerminalNode OR_ASSIGN() { return getToken(AssemblyParser.OR_ASSIGN, 0); }
		public TerminalNode XOR_ASSIGN() { return getToken(AssemblyParser.XOR_ASSIGN, 0); }
		public TerminalNode RSHIFT_ASSIGN() { return getToken(AssemblyParser.RSHIFT_ASSIGN, 0); }
		public TerminalNode URSHIFT_ASSIGN() { return getToken(AssemblyParser.URSHIFT_ASSIGN, 0); }
		public TerminalNode LSHIFT_ASSIGN() { return getToken(AssemblyParser.LSHIFT_ASSIGN, 0); }
		public TerminalNode MOD_ASSIGN() { return getToken(AssemblyParser.MOD_ASSIGN, 0); }
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(AssemblyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(AssemblyParser.RBRACK, 0); }
		public TerminalNode BANGBANG() { return getToken(AssemblyParser.BANGBANG, 0); }
		public TerminalNode INSTANCEOF() { return getToken(AssemblyParser.INSTANCEOF, 0); }
		public TerminalNode COLONCOLON() { return getToken(AssemblyParser.COLONCOLON, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 120;
		enterRecursionRule(_localctx, 120, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(741);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				{
				setState(711);
				primary();
				}
				break;
			case 2:
				{
				setState(712);
				match(THIS);
				setState(713);
				match(LPAREN);
				setState(715);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113592311324161L) != 0)) {
					{
					setState(714);
					expressionList();
					}
				}

				setState(717);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(718);
				match(SUPER);
				setState(719);
				match(LPAREN);
				setState(721);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113592311324161L) != 0)) {
					{
					setState(720);
					expressionList();
					}
				}

				setState(723);
				match(RPAREN);
				}
				break;
			case 4:
				{
				setState(724);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 481036337152L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(725);
				creator();
				}
				break;
			case 5:
				{
				setState(726);
				match(ALLOCATE);
				setState(727);
				allocator();
				}
				break;
			case 6:
				{
				setState(728);
				select();
				}
				break;
			case 7:
				{
				setState(729);
				match(LPAREN);
				setState(730);
				((ExpressionContext)_localctx).castType = typeType(0);
				setState(731);
				match(RPAREN);
				setState(732);
				expression(20);
				}
				break;
			case 8:
				{
				setState(734);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 15L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(735);
				expression(18);
				}
				break;
			case 9:
				{
				setState(736);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==BANG || _la==TILDE) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(737);
				expression(17);
				}
				break;
			case 10:
				{
				setState(738);
				lambdaExpression();
				}
				break;
			case 11:
				{
				setState(739);
				match(IDENTIFIER);
				setState(740);
				arguments();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(815);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(813);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(743);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(744);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 35L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(745);
						expression(17);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(746);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(747);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==ADD || _la==SUB) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(748);
						expression(16);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(749);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(757);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
						case 1:
							{
							setState(750);
							match(LT);
							setState(751);
							match(LT);
							}
							break;
						case 2:
							{
							setState(752);
							match(GT);
							setState(753);
							match(GT);
							setState(754);
							match(GT);
							}
							break;
						case 3:
							{
							setState(755);
							match(GT);
							setState(756);
							match(GT);
							}
							break;
						}
						setState(759);
						expression(15);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(760);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(761);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 60)) & ~0x3f) == 0 && ((1L << (_la - 60)) & 771L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(762);
						expression(14);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(763);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(764);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EQUAL || _la==NOTEQUAL) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(765);
						expression(12);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(766);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(767);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(768);
						expression(11);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(769);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(770);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(771);
						expression(10);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(772);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(773);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(774);
						expression(9);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(775);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(776);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(777);
						expression(8);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(778);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(779);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(780);
						expression(7);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(781);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(782);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(783);
						expression(0);
						setState(784);
						match(COLON);
						setState(785);
						expression(5);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(787);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(788);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 59)) & ~0x3f) == 0 && ((1L << (_la - 59)) & 34342961153L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(789);
						expression(4);
						}
						break;
					case 13:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(790);
						if (!(precpred(_ctx, 27))) throw new FailedPredicateException(this, "precpred(_ctx, 27)");
						setState(791);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(795);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
						case 1:
							{
							setState(792);
							match(IDENTIFIER);
							}
							break;
						case 2:
							{
							setState(793);
							match(THIS);
							}
							break;
						case 3:
							{
							setState(794);
							methodCall();
							}
							break;
						}
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(797);
						if (!(precpred(_ctx, 24))) throw new FailedPredicateException(this, "precpred(_ctx, 24)");
						setState(798);
						match(LBRACK);
						setState(799);
						expression(0);
						setState(800);
						match(RBRACK);
						}
						break;
					case 15:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(802);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(803);
						((ExpressionContext)_localctx).postfix = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 63)) & ~0x3f) == 0 && ((1L << (_la - 63)) & 3073L) != 0)) ) {
							((ExpressionContext)_localctx).postfix = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 16:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(804);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(805);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(806);
						typeType(0);
						}
						break;
					case 17:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(807);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(808);
						match(COLONCOLON);
						setState(810);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(809);
							typeArguments();
							}
						}

						setState(812);
						match(IDENTIFIER);
						}
						break;
					}
					} 
				}
				setState(817);
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
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public TerminalNode THIS() { return getToken(AssemblyParser.THIS, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_primary);
		try {
			setState(825);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(818);
				match(LPAREN);
				setState(819);
				expression(0);
				setState(820);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(822);
				match(THIS);
				}
				break;
			case NULL:
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
				enterOuterAlt(_localctx, 3);
				{
				setState(823);
				literal();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 4);
				{
				setState(824);
				match(IDENTIFIER);
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
	public static class ExplicitGenericInvocationContext extends ParserRuleContext {
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() {
			return getRuleContext(ExplicitGenericInvocationSuffixContext.class,0);
		}
		public ExplicitGenericInvocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitGenericInvocation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterExplicitGenericInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitExplicitGenericInvocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitExplicitGenericInvocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitGenericInvocationContext explicitGenericInvocation() throws RecognitionException {
		ExplicitGenericInvocationContext _localctx = new ExplicitGenericInvocationContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(827);
			typeArguments();
			setState(828);
			explicitGenericInvocationSuffix();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExplicitGenericInvocationSuffixContext extends ParserRuleContext {
		public TerminalNode SUPER() { return getToken(AssemblyParser.SUPER, 0); }
		public SuperSuffixContext superSuffix() {
			return getRuleContext(SuperSuffixContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitGenericInvocationSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterExplicitGenericInvocationSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitExplicitGenericInvocationSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitExplicitGenericInvocationSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() throws RecognitionException {
		ExplicitGenericInvocationSuffixContext _localctx = new ExplicitGenericInvocationSuffixContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_explicitGenericInvocationSuffix);
		try {
			setState(834);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(830);
				match(SUPER);
				setState(831);
				superSuffix();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(832);
				match(IDENTIFIER);
				setState(833);
				arguments();
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
	public static class SuperSuffixContext extends ParserRuleContext {
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public SuperSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_superSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterSuperSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitSuperSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitSuperSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SuperSuffixContext superSuffix() throws RecognitionException {
		SuperSuffixContext _localctx = new SuperSuffixContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_superSuffix);
		int _la;
		try {
			setState(845);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(836);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(837);
				match(DOT);
				setState(839);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(838);
					typeArguments();
					}
				}

				setState(841);
				match(IDENTIFIER);
				setState(843);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(842);
					arguments();
					}
				}

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
	public static class ArgumentsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(847);
			match(LPAREN);
			setState(849);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113592311324161L) != 0)) {
				{
				setState(848);
				expressionList();
				}
			}

			setState(851);
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
	public static class ClassTypeContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ClassTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassTypeContext classType() throws RecognitionException {
		ClassTypeContext _localctx = new ClassTypeContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_classType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(853);
			match(IDENTIFIER);
			setState(855);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(854);
				typeArguments();
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
	public static class MethodCallContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_methodCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(858);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(857);
				typeArguments();
				}
			}

			setState(860);
			match(IDENTIFIER);
			setState(861);
			match(LPAREN);
			setState(863);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4719949362136883200L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 153113592311324161L) != 0)) {
				{
				setState(862);
				expressionList();
				}
			}

			setState(865);
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
		public TerminalNode CHAR_LITERAL() { return getToken(AssemblyParser.CHAR_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(AssemblyParser.STRING_LITERAL, 0); }
		public TerminalNode BOOL_LITERAL() { return getToken(AssemblyParser.BOOL_LITERAL, 0); }
		public TerminalNode NULL() { return getToken(AssemblyParser.NULL, 0); }
		public TerminalNode TEXT_BLOCK() { return getToken(AssemblyParser.TEXT_BLOCK, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_literal);
		try {
			setState(874);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(867);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(868);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(869);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(870);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(871);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(872);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(873);
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
		public TerminalNode DECIMAL_LITERAL() { return getToken(AssemblyParser.DECIMAL_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(AssemblyParser.HEX_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(AssemblyParser.OCT_LITERAL, 0); }
		public TerminalNode BINARY_LITERAL() { return getToken(AssemblyParser.BINARY_LITERAL, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(876);
			_la = _input.LA(1);
			if ( !(((((_la - 107)) & ~0x3f) == 0 && ((1L << (_la - 107)) & 15L) != 0)) ) {
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
		public TerminalNode FLOAT_LITERAL() { return getToken(AssemblyParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(AssemblyParser.HEX_FLOAT_LITERAL, 0); }
		public FloatLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitFloatLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FloatLiteralContext floatLiteral() throws RecognitionException {
		FloatLiteralContext _localctx = new FloatLiteralContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(878);
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
	public static class TypeTypeOrVoidContext extends ParserRuleContext {
		public TerminalNode VOID() { return getToken(AssemblyParser.VOID, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TypeTypeOrVoidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeTypeOrVoid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeTypeOrVoid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeTypeOrVoid(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeTypeOrVoid(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeTypeOrVoidContext typeTypeOrVoid() throws RecognitionException {
		TypeTypeOrVoidContext _localctx = new TypeTypeOrVoidContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_typeTypeOrVoid);
		try {
			setState(882);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(880);
				match(VOID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(881);
				typeType(0);
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
	public static class TypeTypeContext extends ParserRuleContext {
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public TerminalNode ANY() { return getToken(AssemblyParser.ANY, 0); }
		public TerminalNode NEVER() { return getToken(AssemblyParser.NEVER, 0); }
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(AssemblyParser.ARROW, 0); }
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public TerminalNode LBRACK() { return getToken(AssemblyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(AssemblyParser.RBRACK, 0); }
		public List<TerminalNode> BITOR() { return getTokens(AssemblyParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(AssemblyParser.BITOR, i);
		}
		public List<TerminalNode> BITAND() { return getTokens(AssemblyParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(AssemblyParser.BITAND, i);
		}
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
		}
		public TypeTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeTypeContext typeType() throws RecognitionException {
		return typeType(0);
	}

	private TypeTypeContext typeType(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TypeTypeContext _localctx = new TypeTypeContext(_ctx, _parentState);
		TypeTypeContext _prevctx = _localctx;
		int _startState = 144;
		enterRecursionRule(_localctx, 144, RULE_typeType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(909);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(885);
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
				setState(886);
				primitiveType();
				}
				break;
			case ANY:
				{
				setState(887);
				match(ANY);
				}
				break;
			case NEVER:
				{
				setState(888);
				match(NEVER);
				}
				break;
			case LPAREN:
				{
				setState(889);
				match(LPAREN);
				setState(898);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29273414221115460L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217745L) != 0)) {
					{
					setState(890);
					typeType(0);
					setState(895);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(891);
						match(COMMA);
						setState(892);
						typeType(0);
						}
						}
						setState(897);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(900);
				match(RPAREN);
				setState(901);
				match(ARROW);
				setState(902);
				typeType(2);
				}
				break;
			case LBRACK:
				{
				setState(903);
				match(LBRACK);
				setState(904);
				typeType(0);
				setState(905);
				match(COMMA);
				setState(906);
				typeType(0);
				setState(907);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(929);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,98,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(927);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
					case 1:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(911);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(914); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(912);
								match(BITOR);
								setState(913);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(916); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(918);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(921); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(919);
								match(BITAND);
								setState(920);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(923); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(925);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(926);
						arrayKind();
						}
						break;
					}
					} 
				}
				setState(931);
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
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayKindContext extends ParserRuleContext {
		public TerminalNode R() { return getToken(AssemblyParser.R, 0); }
		public TerminalNode RW() { return getToken(AssemblyParser.RW, 0); }
		public TerminalNode C() { return getToken(AssemblyParser.C, 0); }
		public TerminalNode V() { return getToken(AssemblyParser.V, 0); }
		public ArrayKindContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayKind; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterArrayKind(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitArrayKind(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitArrayKind(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayKindContext arrayKind() throws RecognitionException {
		ArrayKindContext _localctx = new ArrayKindContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(932);
			_la = _input.LA(1);
			if ( !(((((_la - 117)) & ~0x3f) == 0 && ((1L << (_la - 117)) & 15L) != 0)) ) {
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassOrInterfaceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassOrInterfaceType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassOrInterfaceType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceTypeContext classOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(934);
			qualifiedName();
			setState(936);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(935);
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
		public TerminalNode LT() { return getToken(AssemblyParser.LT, 0); }
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(AssemblyParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(AssemblyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(AssemblyParser.COMMA, i);
		}
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitTypeArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(938);
			match(LT);
			setState(939);
			typeType(0);
			setState(944);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(940);
				match(COMMA);
				setState(941);
				typeType(0);
				}
				}
				setState(946);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(947);
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
		public TerminalNode BOOLEAN() { return getToken(AssemblyParser.BOOLEAN, 0); }
		public TerminalNode BYTE() { return getToken(AssemblyParser.BYTE, 0); }
		public TerminalNode SHORT() { return getToken(AssemblyParser.SHORT, 0); }
		public TerminalNode INT() { return getToken(AssemblyParser.INT, 0); }
		public TerminalNode LONG() { return getToken(AssemblyParser.LONG, 0); }
		public TerminalNode DOUBLE() { return getToken(AssemblyParser.DOUBLE, 0); }
		public TerminalNode STRING() { return getToken(AssemblyParser.STRING, 0); }
		public TerminalNode PASSWORD() { return getToken(AssemblyParser.PASSWORD, 0); }
		public TerminalNode TIME() { return getToken(AssemblyParser.TIME, 0); }
		public TerminalNode NULL() { return getToken(AssemblyParser.NULL, 0); }
		public TerminalNode VOID() { return getToken(AssemblyParser.VOID, 0); }
		public TerminalNode CHAR() { return getToken(AssemblyParser.CHAR, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterPrimitiveType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitPrimitiveType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(949);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2251816456892484L) != 0)) ) {
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
		public TerminalNode NATIVE() { return getToken(AssemblyParser.NATIVE, 0); }
		public TerminalNode READONLY() { return getToken(AssemblyParser.READONLY, 0); }
		public TerminalNode CHILD() { return getToken(AssemblyParser.CHILD, 0); }
		public TerminalNode TITLE() { return getToken(AssemblyParser.TITLE, 0); }
		public TerminalNode UNIQUE() { return getToken(AssemblyParser.UNIQUE, 0); }
		public TerminalNode DELETED() { return getToken(AssemblyParser.DELETED, 0); }
		public ModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModifierContext modifier() throws RecognitionException {
		ModifierContext _localctx = new ModifierContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_modifier);
		try {
			setState(958);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
				enterOuterAlt(_localctx, 1);
				{
				setState(951);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(952);
				match(NATIVE);
				}
				break;
			case READONLY:
				enterOuterAlt(_localctx, 3);
				{
				setState(953);
				match(READONLY);
				}
				break;
			case CHILD:
				enterOuterAlt(_localctx, 4);
				{
				setState(954);
				match(CHILD);
				}
				break;
			case TITLE:
				enterOuterAlt(_localctx, 5);
				{
				setState(955);
				match(TITLE);
				}
				break;
			case UNIQUE:
				enterOuterAlt(_localctx, 6);
				{
				setState(956);
				match(UNIQUE);
				}
				break;
			case DELETED:
				enterOuterAlt(_localctx, 7);
				{
				setState(957);
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
		public TerminalNode PUBLIC() { return getToken(AssemblyParser.PUBLIC, 0); }
		public TerminalNode PROTECTED() { return getToken(AssemblyParser.PROTECTED, 0); }
		public TerminalNode PRIVATE() { return getToken(AssemblyParser.PRIVATE, 0); }
		public TerminalNode STATIC() { return getToken(AssemblyParser.STATIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(AssemblyParser.ABSTRACT, 0); }
		public ClassOrInterfaceModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterClassOrInterfaceModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitClassOrInterfaceModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitClassOrInterfaceModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceModifierContext classOrInterfaceModifier() throws RecognitionException {
		ClassOrInterfaceModifierContext _localctx = new ClassOrInterfaceModifierContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_classOrInterfaceModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(960);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 25288767438850L) != 0)) ) {
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
		public TerminalNode COLON() { return getToken(AssemblyParser.COLON, 0); }
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(AssemblyParser.ARROW, 0); }
		public LambdaBodyContext lambdaBody() {
			return getRuleContext(LambdaBodyContext.class,0);
		}
		public LambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLambdaExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLambdaExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaExpressionContext lambdaExpression() throws RecognitionException {
		LambdaExpressionContext _localctx = new LambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(962);
			lambdaParameters();
			setState(963);
			match(COLON);
			setState(964);
			typeTypeOrVoid();
			setState(965);
			match(ARROW);
			setState(966);
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
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
		}
		public LambdaParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLambdaParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLambdaParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLambdaParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaParametersContext lambdaParameters() throws RecognitionException {
		LambdaParametersContext _localctx = new LambdaParametersContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_lambdaParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(968);
			match(LPAREN);
			setState(970);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 29273414221115460L) != 0) || ((((_la - 94)) & ~0x3f) == 0 && ((1L << (_la - 94)) & 134217745L) != 0)) {
				{
				setState(969);
				formalParameterList();
				}
			}

			setState(972);
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
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLambdaBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLambdaBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLambdaBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaBodyContext lambdaBody() throws RecognitionException {
		LambdaBodyContext _localctx = new LambdaBodyContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_lambdaBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(974);
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
	public static class IndexDeclarationContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(AssemblyParser.INDEX, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode LBRACE() { return getToken(AssemblyParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(AssemblyParser.RBRACE, 0); }
		public List<IndexFieldContext> indexField() {
			return getRuleContexts(IndexFieldContext.class);
		}
		public IndexFieldContext indexField(int i) {
			return getRuleContext(IndexFieldContext.class,i);
		}
		public IndexDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterIndexDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitIndexDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitIndexDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexDeclarationContext indexDeclaration() throws RecognitionException {
		IndexDeclarationContext _localctx = new IndexDeclarationContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_indexDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(976);
			match(INDEX);
			setState(977);
			match(IDENTIFIER);
			setState(978);
			match(LBRACE);
			setState(982);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(979);
				indexField();
				}
				}
				setState(984);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(985);
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
	public static class IndexFieldContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode COLON() { return getToken(AssemblyParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(AssemblyParser.SEMI, 0); }
		public IndexFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterIndexField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitIndexField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitIndexField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexFieldContext indexField() throws RecognitionException {
		IndexFieldContext _localctx = new IndexFieldContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_indexField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(987);
			match(IDENTIFIER);
			setState(988);
			match(COLON);
			setState(989);
			expression(0);
			setState(990);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TerminalNode AT() { return getToken(AssemblyParser.AT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitAnnotation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(992);
			match(AT);
			setState(993);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
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
		case 60:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 72:
			return typeType_sempred((TypeTypeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 16);
		case 1:
			return precpred(_ctx, 15);
		case 2:
			return precpred(_ctx, 14);
		case 3:
			return precpred(_ctx, 13);
		case 4:
			return precpred(_ctx, 11);
		case 5:
			return precpred(_ctx, 10);
		case 6:
			return precpred(_ctx, 9);
		case 7:
			return precpred(_ctx, 8);
		case 8:
			return precpred(_ctx, 7);
		case 9:
			return precpred(_ctx, 6);
		case 10:
			return precpred(_ctx, 5);
		case 11:
			return precpred(_ctx, 4);
		case 12:
			return precpred(_ctx, 27);
		case 13:
			return precpred(_ctx, 24);
		case 14:
			return precpred(_ctx, 19);
		case 15:
			return precpred(_ctx, 12);
		case 16:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean typeType_sempred(TypeTypeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 17:
			return precpred(_ctx, 5);
		case 18:
			return precpred(_ctx, 4);
		case 19:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001|\u03e4\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007T\u0001"+
		"\u0000\u0003\u0000\u00ac\b\u0000\u0001\u0000\u0005\u0000\u00af\b\u0000"+
		"\n\u0000\f\u0000\u00b2\t\u0000\u0001\u0000\u0004\u0000\u00b5\b\u0000\u000b"+
		"\u0000\f\u0000\u00b6\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0005\u0003\u00c2"+
		"\b\u0003\n\u0003\f\u0003\u00c5\t\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u0003\u00ca\b\u0003\u0001\u0003\u0003\u0003\u00cd\b\u0003\u0001"+
		"\u0004\u0005\u0004\u00d0\b\u0004\n\u0004\f\u0004\u00d3\t\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u0004\u00d8\b\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u0004\u00dc\b\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00e0\b"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0005\u0005\u00e6"+
		"\b\u0005\n\u0005\f\u0005\u00e9\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0005\u0006\u00f0\b\u0006\n\u0006\f\u0006\u00f3"+
		"\t\u0006\u0001\u0007\u0005\u0007\u00f6\b\u0007\n\u0007\f\u0007\u00f9\t"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00fd\b\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\t\u0005\t\u0103\b\t\n\t\f\t\u0106\t\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0003\t\u010c\b\t\u0001\t\u0001\t\u0003\t\u0110\b\t\u0001"+
		"\t\u0003\t\u0113\b\t\u0001\t\u0003\t\u0116\b\t\u0001\t\u0001\t\u0001\n"+
		"\u0001\n\u0001\n\u0005\n\u011d\b\n\n\n\f\n\u0120\t\n\u0001\u000b\u0001"+
		"\u000b\u0003\u000b\u0124\b\u000b\u0001\f\u0001\f\u0005\f\u0128\b\f\n\f"+
		"\f\f\u012b\t\f\u0001\r\u0005\r\u012e\b\r\n\r\f\r\u0131\t\r\u0001\r\u0001"+
		"\r\u0001\r\u0003\r\u0136\b\r\u0001\r\u0001\r\u0003\r\u013a\b\r\u0001\r"+
		"\u0001\r\u0001\u000e\u0001\u000e\u0005\u000e\u0140\b\u000e\n\u000e\f\u000e"+
		"\u0143\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0005\u000f\u0148\b"+
		"\u000f\n\u000f\f\u000f\u014b\t\u000f\u0001\u000f\u0001\u000f\u0003\u000f"+
		"\u014f\b\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0005\u0011\u0154\b"+
		"\u0011\n\u0011\f\u0011\u0157\t\u0011\u0001\u0011\u0003\u0011\u015a\b\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0165\b\u0013\n\u0013"+
		"\f\u0013\u0168\t\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u016c\b\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0003\u0014\u0174\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0016\u0003\u0016\u017b\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0017\u0003\u0017\u0183\b\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0189\b\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018"+
		"\u0191\b\u0018\n\u0018\f\u0018\u0194\t\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u019b\b\u0019\n\u0019\f\u0019"+
		"\u019e\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u01a3\b"+
		"\u001a\n\u001a\f\u001a\u01a6\t\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0003\u001b\u01ab\b\u001b\u0001\u001c\u0001\u001c\u0003\u001c\u01af\b"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005"+
		"\u001d\u01b6\b\u001d\n\u001d\f\u001d\u01b9\t\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u01c0\b\u001e\n\u001e"+
		"\f\u001e\u01c3\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001 \u0001"+
		" \u0003 \u01ca\b \u0001!\u0001!\u0005!\u01ce\b!\n!\f!\u01d1\t!\u0001!"+
		"\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\"\u01e4"+
		"\b\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u01ed"+
		"\b\"\n\"\f\"\u01f0\t\"\u0001\"\u0001\"\u0001\"\u0003\"\u01f5\b\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0003\"\u0203\b\"\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0003#\u020d\b#\u0003#\u020f\b#\u0001$\u0001$\u0001"+
		"$\u0003$\u0214\b$\u0001$\u0001$\u0001%\u0001%\u0001%\u0005%\u021b\b%\n"+
		"%\f%\u021e\t%\u0001&\u0001&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0005\'\u022b\b\'\n\'\f\'\u022e\t\'\u0001\'"+
		"\u0001\'\u0001(\u0003(\u0233\b(\u0001(\u0001(\u0003(\u0237\b(\u0001(\u0001"+
		"(\u0003(\u023b\b(\u0001)\u0001)\u0001)\u0005)\u0240\b)\n)\f)\u0243\t)"+
		"\u0001*\u0001*\u0001*\u0001*\u0001*\u0001+\u0001+\u0001+\u0005+\u024d"+
		"\b+\n+\f+\u0250\t+\u0001,\u0001,\u0001,\u0001,\u0001-\u0001-\u0001-\u0001"+
		"-\u0001.\u0003.\u025b\b.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0003"+
		".\u0263\b.\u0001/\u0001/\u0001/\u0001/\u0005/\u0269\b/\n/\f/\u026c\t/"+
		"\u0001/\u0001/\u00010\u00010\u00010\u00010\u00050\u0274\b0\n0\f0\u0277"+
		"\t0\u00010\u00030\u027a\b0\u00030\u027c\b0\u00010\u00010\u00011\u0001"+
		"1\u00031\u0282\b1\u00012\u00012\u00032\u0286\b2\u00012\u00032\u0289\b"+
		"2\u00013\u00013\u00014\u00014\u00014\u00034\u0290\b4\u00014\u00014\u0001"+
		"5\u00015\u00015\u00055\u0297\b5\n5\f5\u029a\t5\u00016\u00016\u00016\u0001"+
		"6\u00016\u00016\u00056\u02a2\b6\n6\f6\u02a5\t6\u00016\u00016\u00016\u0001"+
		"6\u00016\u00017\u00017\u00017\u00017\u00018\u00018\u00018\u00019\u0001"+
		"9\u00019\u00019\u00019\u00019\u00039\u02b9\b9\u0001:\u0001:\u0001:\u0001"+
		":\u0001;\u0001;\u0001;\u0005;\u02c2\b;\n;\f;\u02c5\t;\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0003<\u02cc\b<\u0001<\u0001<\u0001<\u0001<\u0003<\u02d2"+
		"\b<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<\u02e6"+
		"\b<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0003<\u02f6\b<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0003<\u031c\b<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<\u032b\b<\u0001"+
		"<\u0005<\u032e\b<\n<\f<\u0331\t<\u0001=\u0001=\u0001=\u0001=\u0001=\u0001"+
		"=\u0001=\u0003=\u033a\b=\u0001>\u0001>\u0001>\u0001?\u0001?\u0001?\u0001"+
		"?\u0003?\u0343\b?\u0001@\u0001@\u0001@\u0003@\u0348\b@\u0001@\u0001@\u0003"+
		"@\u034c\b@\u0003@\u034e\b@\u0001A\u0001A\u0003A\u0352\bA\u0001A\u0001"+
		"A\u0001B\u0001B\u0003B\u0358\bB\u0001C\u0003C\u035b\bC\u0001C\u0001C\u0001"+
		"C\u0003C\u0360\bC\u0001C\u0001C\u0001D\u0001D\u0001D\u0001D\u0001D\u0001"+
		"D\u0001D\u0003D\u036b\bD\u0001E\u0001E\u0001F\u0001F\u0001G\u0001G\u0003"+
		"G\u0373\bG\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001"+
		"H\u0005H\u037e\bH\nH\fH\u0381\tH\u0003H\u0383\bH\u0001H\u0001H\u0001H"+
		"\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0003H\u038e\bH\u0001H\u0001"+
		"H\u0001H\u0004H\u0393\bH\u000bH\fH\u0394\u0001H\u0001H\u0001H\u0004H\u039a"+
		"\bH\u000bH\fH\u039b\u0001H\u0001H\u0005H\u03a0\bH\nH\fH\u03a3\tH\u0001"+
		"I\u0001I\u0001J\u0001J\u0003J\u03a9\bJ\u0001K\u0001K\u0001K\u0001K\u0005"+
		"K\u03af\bK\nK\fK\u03b2\tK\u0001K\u0001K\u0001L\u0001L\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0003M\u03bf\bM\u0001N\u0001N\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001P\u0001P\u0003P\u03cb\bP\u0001P\u0001"+
		"P\u0001Q\u0001Q\u0001R\u0001R\u0001R\u0001R\u0005R\u03d5\bR\nR\fR\u03d8"+
		"\tR\u0001R\u0001R\u0001S\u0001S\u0001S\u0001S\u0001S\u0001T\u0001T\u0001"+
		"T\u0001T\u0000\u0002x\u0090U\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPR"+
		"TVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e"+
		"\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6"+
		"\u00a8\u0000\u0011\u0002\u0000\u0007\b\u000b\u000b\u0004\u0000\u0001\u0001"+
		"\u0004\u0004**,,\u0001\u000078\u0001\u0000$&\u0001\u0000IL\u0002\u0000"+
		">>@@\u0002\u0000MNRR\u0001\u0000KL\u0002\u0000<=DE\u0002\u0000CCFF\u0002"+
		"\u0000;;S]\u0002\u0000??IJ\u0001\u0000kn\u0001\u0000op\u0001\u0000ux\u0006"+
		"\u0000\u0002\u0002\u0006\u0006\f\r\u0010\u0011\u001d!33\u0003\u0000\u0001"+
		"\u0001(*,,\u042c\u0000\u00ab\u0001\u0000\u0000\u0000\u0002\u00b8\u0001"+
		"\u0000\u0000\u0000\u0004\u00bc\u0001\u0000\u0000\u0000\u0006\u00cc\u0001"+
		"\u0000\u0000\u0000\b\u00d1\u0001\u0000\u0000\u0000\n\u00e3\u0001\u0000"+
		"\u0000\u0000\f\u00ec\u0001\u0000\u0000\u0000\u000e\u00fc\u0001\u0000\u0000"+
		"\u0000\u0010\u00fe\u0001\u0000\u0000\u0000\u0012\u0104\u0001\u0000\u0000"+
		"\u0000\u0014\u0119\u0001\u0000\u0000\u0000\u0016\u0121\u0001\u0000\u0000"+
		"\u0000\u0018\u0125\u0001\u0000\u0000\u0000\u001a\u012f\u0001\u0000\u0000"+
		"\u0000\u001c\u013d\u0001\u0000\u0000\u0000\u001e\u014e\u0001\u0000\u0000"+
		"\u0000 \u0150\u0001\u0000\u0000\u0000\"\u0155\u0001\u0000\u0000\u0000"+
		"$\u015d\u0001\u0000\u0000\u0000&\u015f\u0001\u0000\u0000\u0000(\u0173"+
		"\u0001\u0000\u0000\u0000*\u0175\u0001\u0000\u0000\u0000,\u017a\u0001\u0000"+
		"\u0000\u0000.\u0182\u0001\u0000\u0000\u00000\u018c\u0001\u0000\u0000\u0000"+
		"2\u0197\u0001\u0000\u0000\u00004\u019f\u0001\u0000\u0000\u00006\u01a7"+
		"\u0001\u0000\u0000\u00008\u01ac\u0001\u0000\u0000\u0000:\u01b2\u0001\u0000"+
		"\u0000\u0000<\u01bc\u0001\u0000\u0000\u0000>\u01c4\u0001\u0000\u0000\u0000"+
		"@\u01c9\u0001\u0000\u0000\u0000B\u01cb\u0001\u0000\u0000\u0000D\u0202"+
		"\u0001\u0000\u0000\u0000F\u020e\u0001\u0000\u0000\u0000H\u0210\u0001\u0000"+
		"\u0000\u0000J\u0217\u0001\u0000\u0000\u0000L\u021f\u0001\u0000\u0000\u0000"+
		"N\u0223\u0001\u0000\u0000\u0000P\u0232\u0001\u0000\u0000\u0000R\u023c"+
		"\u0001\u0000\u0000\u0000T\u0244\u0001\u0000\u0000\u0000V\u0249\u0001\u0000"+
		"\u0000\u0000X\u0251\u0001\u0000\u0000\u0000Z\u0255\u0001\u0000\u0000\u0000"+
		"\\\u0262\u0001\u0000\u0000\u0000^\u0264\u0001\u0000\u0000\u0000`\u026f"+
		"\u0001\u0000\u0000\u0000b\u0281\u0001\u0000\u0000\u0000d\u0288\u0001\u0000"+
		"\u0000\u0000f\u028a\u0001\u0000\u0000\u0000h\u028c\u0001\u0000\u0000\u0000"+
		"j\u0293\u0001\u0000\u0000\u0000l\u029b\u0001\u0000\u0000\u0000n\u02ab"+
		"\u0001\u0000\u0000\u0000p\u02af\u0001\u0000\u0000\u0000r\u02b8\u0001\u0000"+
		"\u0000\u0000t\u02ba\u0001\u0000\u0000\u0000v\u02be\u0001\u0000\u0000\u0000"+
		"x\u02e5\u0001\u0000\u0000\u0000z\u0339\u0001\u0000\u0000\u0000|\u033b"+
		"\u0001\u0000\u0000\u0000~\u0342\u0001\u0000\u0000\u0000\u0080\u034d\u0001"+
		"\u0000\u0000\u0000\u0082\u034f\u0001\u0000\u0000\u0000\u0084\u0355\u0001"+
		"\u0000\u0000\u0000\u0086\u035a\u0001\u0000\u0000\u0000\u0088\u036a\u0001"+
		"\u0000\u0000\u0000\u008a\u036c\u0001\u0000\u0000\u0000\u008c\u036e\u0001"+
		"\u0000\u0000\u0000\u008e\u0372\u0001\u0000\u0000\u0000\u0090\u038d\u0001"+
		"\u0000\u0000\u0000\u0092\u03a4\u0001\u0000\u0000\u0000\u0094\u03a6\u0001"+
		"\u0000\u0000\u0000\u0096\u03aa\u0001\u0000\u0000\u0000\u0098\u03b5\u0001"+
		"\u0000\u0000\u0000\u009a\u03be\u0001\u0000\u0000\u0000\u009c\u03c0\u0001"+
		"\u0000\u0000\u0000\u009e\u03c2\u0001\u0000\u0000\u0000\u00a0\u03c8\u0001"+
		"\u0000\u0000\u0000\u00a2\u03ce\u0001\u0000\u0000\u0000\u00a4\u03d0\u0001"+
		"\u0000\u0000\u0000\u00a6\u03db\u0001\u0000\u0000\u0000\u00a8\u03e0\u0001"+
		"\u0000\u0000\u0000\u00aa\u00ac\u0003\u0002\u0001\u0000\u00ab\u00aa\u0001"+
		"\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000\u0000\u0000\u00ac\u00b0\u0001"+
		"\u0000\u0000\u0000\u00ad\u00af\u0003\u0004\u0002\u0000\u00ae\u00ad\u0001"+
		"\u0000\u0000\u0000\u00af\u00b2\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001"+
		"\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000\u00b1\u00b4\u0001"+
		"\u0000\u0000\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b3\u00b5\u0003"+
		"\u0006\u0003\u0000\u00b4\u00b3\u0001\u0000\u0000\u0000\u00b5\u00b6\u0001"+
		"\u0000\u0000\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001"+
		"\u0000\u0000\u0000\u00b7\u0001\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005"+
		"\u000e\u0000\u0000\u00b9\u00ba\u00034\u001a\u0000\u00ba\u00bb\u0005d\u0000"+
		"\u0000\u00bb\u0003\u0001\u0000\u0000\u0000\u00bc\u00bd\u0005\u000f\u0000"+
		"\u0000\u00bd\u00be\u00034\u001a\u0000\u00be\u00bf\u0005d\u0000\u0000\u00bf"+
		"\u0005\u0001\u0000\u0000\u0000\u00c0\u00c2\u0003\u009cN\u0000\u00c1\u00c0"+
		"\u0001\u0000\u0000\u0000\u00c2\u00c5\u0001\u0000\u0000\u0000\u00c3\u00c1"+
		"\u0001\u0000\u0000\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c9"+
		"\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000\u0000\u0000\u00c6\u00ca"+
		"\u0003\b\u0004\u0000\u00c7\u00ca\u0003\u0012\t\u0000\u00c8\u00ca\u0003"+
		"\u001a\r\u0000\u00c9\u00c6\u0001\u0000\u0000\u0000\u00c9\u00c7\u0001\u0000"+
		"\u0000\u0000\u00c9\u00c8\u0001\u0000\u0000\u0000\u00ca\u00cd\u0001\u0000"+
		"\u0000\u0000\u00cb\u00cd\u0005d\u0000\u0000\u00cc\u00c3\u0001\u0000\u0000"+
		"\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cd\u0007\u0001\u0000\u0000"+
		"\u0000\u00ce\u00d0\u0003\u00a8T\u0000\u00cf\u00ce\u0001\u0000\u0000\u0000"+
		"\u00d0\u00d3\u0001\u0000\u0000\u0000\u00d1\u00cf\u0001\u0000\u0000\u0000"+
		"\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d5\u0007\u0000\u0000\u0000"+
		"\u00d5\u00d7\u0005y\u0000\u0000\u00d6\u00d8\u00030\u0018\u0000\u00d7\u00d6"+
		"\u0001\u0000\u0000\u0000\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8\u00db"+
		"\u0001\u0000\u0000\u0000\u00d9\u00da\u0005\u0014\u0000\u0000\u00da\u00dc"+
		"\u0003\u0090H\u0000\u00db\u00d9\u0001\u0000\u0000\u0000\u00db\u00dc\u0001"+
		"\u0000\u0000\u0000\u00dc\u00df\u0001\u0000\u0000\u0000\u00dd\u00de\u0005"+
		"\u001b\u0000\u0000\u00de\u00e0\u0003\f\u0006\u0000\u00df\u00dd\u0001\u0000"+
		"\u0000\u0000\u00df\u00e0\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000"+
		"\u0000\u0000\u00e1\u00e2\u0003\n\u0005\u0000\u00e2\t\u0001\u0000\u0000"+
		"\u0000\u00e3\u00e7\u0005`\u0000\u0000\u00e4\u00e6\u0003\u000e\u0007\u0000"+
		"\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e6\u00e9\u0001\u0000\u0000\u0000"+
		"\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000"+
		"\u00e8\u00ea\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000"+
		"\u00ea\u00eb\u0005a\u0000\u0000\u00eb\u000b\u0001\u0000\u0000\u0000\u00ec"+
		"\u00f1\u0003\u0090H\u0000\u00ed\u00ee\u0005e\u0000\u0000\u00ee\u00f0\u0003"+
		"\u0090H\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001\u0000"+
		"\u0000\u0000\u00f2\r\u0001\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f4\u00f6\u0003\u009aM\u0000\u00f5\u00f4\u0001\u0000\u0000\u0000"+
		"\u00f6\u00f9\u0001\u0000\u0000\u0000\u00f7\u00f5\u0001\u0000\u0000\u0000"+
		"\u00f7\u00f8\u0001\u0000\u0000\u0000\u00f8\u00fa\u0001\u0000\u0000\u0000"+
		"\u00f9\u00f7\u0001\u0000\u0000\u0000\u00fa\u00fd\u0003(\u0014\u0000\u00fb"+
		"\u00fd\u0003\u0010\b\u0000\u00fc\u00f7\u0001\u0000\u0000\u0000\u00fc\u00fb"+
		"\u0001\u0000\u0000\u0000\u00fd\u000f\u0001\u0000\u0000\u0000\u00fe\u00ff"+
		"\u0005,\u0000\u0000\u00ff\u0100\u0003B!\u0000\u0100\u0011\u0001\u0000"+
		"\u0000\u0000\u0101\u0103\u0003\u00a8T\u0000\u0102\u0101\u0001\u0000\u0000"+
		"\u0000\u0103\u0106\u0001\u0000\u0000\u0000\u0104\u0102\u0001\u0000\u0000"+
		"\u0000\u0104\u0105\u0001\u0000\u0000\u0000\u0105\u0107\u0001\u0000\u0000"+
		"\u0000\u0106\u0104\u0001\u0000\u0000\u0000\u0107\u0108\u0005\u0013\u0000"+
		"\u0000\u0108\u010b\u0005y\u0000\u0000\u0109\u010a\u0005\u001b\u0000\u0000"+
		"\u010a\u010c\u0003\f\u0006\u0000\u010b\u0109\u0001\u0000\u0000\u0000\u010b"+
		"\u010c\u0001\u0000\u0000\u0000\u010c\u010d\u0001\u0000\u0000\u0000\u010d"+
		"\u010f\u0005`\u0000\u0000\u010e\u0110\u0003\u0014\n\u0000\u010f\u010e"+
		"\u0001\u0000\u0000\u0000\u010f\u0110\u0001\u0000\u0000\u0000\u0110\u0112"+
		"\u0001\u0000\u0000\u0000\u0111\u0113\u0005e\u0000\u0000\u0112\u0111\u0001"+
		"\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000\u0000\u0113\u0115\u0001"+
		"\u0000\u0000\u0000\u0114\u0116\u0003\u0018\f\u0000\u0115\u0114\u0001\u0000"+
		"\u0000\u0000\u0115\u0116\u0001\u0000\u0000\u0000\u0116\u0117\u0001\u0000"+
		"\u0000\u0000\u0117\u0118\u0005a\u0000\u0000\u0118\u0013\u0001\u0000\u0000"+
		"\u0000\u0119\u011e\u0003\u0016\u000b\u0000\u011a\u011b\u0005e\u0000\u0000"+
		"\u011b\u011d\u0003\u0016\u000b\u0000\u011c\u011a\u0001\u0000\u0000\u0000"+
		"\u011d\u0120\u0001\u0000\u0000\u0000\u011e\u011c\u0001\u0000\u0000\u0000"+
		"\u011e\u011f\u0001\u0000\u0000\u0000\u011f\u0015\u0001\u0000\u0000\u0000"+
		"\u0120\u011e\u0001\u0000\u0000\u0000\u0121\u0123\u0005y\u0000\u0000\u0122"+
		"\u0124\u0003\u0082A\u0000\u0123\u0122\u0001\u0000\u0000\u0000\u0123\u0124"+
		"\u0001\u0000\u0000\u0000\u0124\u0017\u0001\u0000\u0000\u0000\u0125\u0129"+
		"\u0005d\u0000\u0000\u0126\u0128\u0003\u000e\u0007\u0000\u0127\u0126\u0001"+
		"\u0000\u0000\u0000\u0128\u012b\u0001\u0000\u0000\u0000\u0129\u0127\u0001"+
		"\u0000\u0000\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a\u0019\u0001"+
		"\u0000\u0000\u0000\u012b\u0129\u0001\u0000\u0000\u0000\u012c\u012e\u0003"+
		"\u00a8T\u0000\u012d\u012c\u0001\u0000\u0000\u0000\u012e\u0131\u0001\u0000"+
		"\u0000\u0000\u012f\u012d\u0001\u0000\u0000\u0000\u012f\u0130\u0001\u0000"+
		"\u0000\u0000\u0130\u0132\u0001\u0000\u0000\u0000\u0131\u012f\u0001\u0000"+
		"\u0000\u0000\u0132\u0133\u0005\"\u0000\u0000\u0133\u0135\u0005y\u0000"+
		"\u0000\u0134\u0136\u00030\u0018\u0000\u0135\u0134\u0001\u0000\u0000\u0000"+
		"\u0135\u0136\u0001\u0000\u0000\u0000\u0136\u0139\u0001\u0000\u0000\u0000"+
		"\u0137\u0138\u0005\u0014\u0000\u0000\u0138\u013a\u0003\f\u0006\u0000\u0139"+
		"\u0137\u0001\u0000\u0000\u0000\u0139\u013a\u0001\u0000\u0000\u0000\u013a"+
		"\u013b\u0001\u0000\u0000\u0000\u013b\u013c\u0003\u001c\u000e\u0000\u013c"+
		"\u001b\u0001\u0000\u0000\u0000\u013d\u0141\u0005`\u0000\u0000\u013e\u0140"+
		"\u0003\u001e\u000f\u0000\u013f\u013e\u0001\u0000\u0000\u0000\u0140\u0143"+
		"\u0001\u0000\u0000\u0000\u0141\u013f\u0001\u0000\u0000\u0000\u0141\u0142"+
		"\u0001\u0000\u0000\u0000\u0142\u0144\u0001\u0000\u0000\u0000\u0143\u0141"+
		"\u0001\u0000\u0000\u0000\u0144\u0145\u0005a\u0000\u0000\u0145\u001d\u0001"+
		"\u0000\u0000\u0000\u0146\u0148\u0003\u009aM\u0000\u0147\u0146\u0001\u0000"+
		"\u0000\u0000\u0148\u014b\u0001\u0000\u0000\u0000\u0149\u0147\u0001\u0000"+
		"\u0000\u0000\u0149\u014a\u0001\u0000\u0000\u0000\u014a\u014c\u0001\u0000"+
		"\u0000\u0000\u014b\u0149\u0001\u0000\u0000\u0000\u014c\u014f\u0003 \u0010"+
		"\u0000\u014d\u014f\u0005d\u0000\u0000\u014e\u0149\u0001\u0000\u0000\u0000"+
		"\u014e\u014d\u0001\u0000\u0000\u0000\u014f\u001f\u0001\u0000\u0000\u0000"+
		"\u0150\u0151\u0003\"\u0011\u0000\u0151!\u0001\u0000\u0000\u0000\u0152"+
		"\u0154\u0003$\u0012\u0000\u0153\u0152\u0001\u0000\u0000\u0000\u0154\u0157"+
		"\u0001\u0000\u0000\u0000\u0155\u0153\u0001\u0000\u0000\u0000\u0155\u0156"+
		"\u0001\u0000\u0000\u0000\u0156\u0159\u0001\u0000\u0000\u0000\u0157\u0155"+
		"\u0001\u0000\u0000\u0000\u0158\u015a\u00030\u0018\u0000\u0159\u0158\u0001"+
		"\u0000\u0000\u0000\u0159\u015a\u0001\u0000\u0000\u0000\u015a\u015b\u0001"+
		"\u0000\u0000\u0000\u015b\u015c\u0003&\u0013\u0000\u015c#\u0001\u0000\u0000"+
		"\u0000\u015d\u015e\u0007\u0001\u0000\u0000\u015e%\u0001\u0000\u0000\u0000"+
		"\u015f\u0160\u0003\u008eG\u0000\u0160\u0161\u0005y\u0000\u0000\u0161\u0166"+
		"\u00038\u001c\u0000\u0162\u0163\u0005b\u0000\u0000\u0163\u0165\u0005c"+
		"\u0000\u0000\u0164\u0162\u0001\u0000\u0000\u0000\u0165\u0168\u0001\u0000"+
		"\u0000\u0000\u0166\u0164\u0001\u0000\u0000\u0000\u0166\u0167\u0001\u0000"+
		"\u0000\u0000\u0167\u016b\u0001\u0000\u0000\u0000\u0168\u0166\u0001\u0000"+
		"\u0000\u0000\u0169\u016a\u00051\u0000\u0000\u016a\u016c\u00032\u0019\u0000"+
		"\u016b\u0169\u0001\u0000\u0000\u0000\u016b\u016c\u0001\u0000\u0000\u0000"+
		"\u016c\u016d\u0001\u0000\u0000\u0000\u016d\u016e\u0005d\u0000\u0000\u016e"+
		"\'\u0001\u0000\u0000\u0000\u016f\u0174\u0003,\u0016\u0000\u0170\u0174"+
		"\u0003*\u0015\u0000\u0171\u0174\u0003.\u0017\u0000\u0172\u0174\u0003\u00a4"+
		"R\u0000\u0173\u016f\u0001\u0000\u0000\u0000\u0173\u0170\u0001\u0000\u0000"+
		"\u0000\u0173\u0171\u0001\u0000\u0000\u0000\u0173\u0172\u0001\u0000\u0000"+
		"\u0000\u0174)\u0001\u0000\u0000\u0000\u0175\u0176\u0003\u0090H\u0000\u0176"+
		"\u0177\u0005y\u0000\u0000\u0177\u0178\u0005d\u0000\u0000\u0178+\u0001"+
		"\u0000\u0000\u0000\u0179\u017b\u00030\u0018\u0000\u017a\u0179\u0001\u0000"+
		"\u0000\u0000\u017a\u017b\u0001\u0000\u0000\u0000\u017b\u017c\u0001\u0000"+
		"\u0000\u0000\u017c\u017d\u0003\u008eG\u0000\u017d\u017e\u0005y\u0000\u0000"+
		"\u017e\u017f\u00038\u001c\u0000\u017f\u0180\u0003@ \u0000\u0180-\u0001"+
		"\u0000\u0000\u0000\u0181\u0183\u00030\u0018\u0000\u0182\u0181\u0001\u0000"+
		"\u0000\u0000\u0182\u0183\u0001\u0000\u0000\u0000\u0183\u0184\u0001\u0000"+
		"\u0000\u0000\u0184\u0185\u0005y\u0000\u0000\u0185\u0188\u00038\u001c\u0000"+
		"\u0186\u0187\u00051\u0000\u0000\u0187\u0189\u00032\u0019\u0000\u0188\u0186"+
		"\u0001\u0000\u0000\u0000\u0188\u0189\u0001\u0000\u0000\u0000\u0189\u018a"+
		"\u0001\u0000\u0000\u0000\u018a\u018b\u0003B!\u0000\u018b/\u0001\u0000"+
		"\u0000\u0000\u018c\u018d\u0005=\u0000\u0000\u018d\u0192\u00036\u001b\u0000"+
		"\u018e\u018f\u0005e\u0000\u0000\u018f\u0191\u00036\u001b\u0000\u0190\u018e"+
		"\u0001\u0000\u0000\u0000\u0191\u0194\u0001\u0000\u0000\u0000\u0192\u0190"+
		"\u0001\u0000\u0000\u0000\u0192\u0193\u0001\u0000\u0000\u0000\u0193\u0195"+
		"\u0001\u0000\u0000\u0000\u0194\u0192\u0001\u0000\u0000\u0000\u0195\u0196"+
		"\u0005<\u0000\u0000\u01961\u0001\u0000\u0000\u0000\u0197\u019c\u00034"+
		"\u001a\u0000\u0198\u0199\u0005e\u0000\u0000\u0199\u019b\u00034\u001a\u0000"+
		"\u019a\u0198\u0001\u0000\u0000\u0000\u019b\u019e\u0001\u0000\u0000\u0000"+
		"\u019c\u019a\u0001\u0000\u0000\u0000\u019c\u019d\u0001\u0000\u0000\u0000"+
		"\u019d3\u0001\u0000\u0000\u0000\u019e\u019c\u0001\u0000\u0000\u0000\u019f"+
		"\u01a4\u0005y\u0000\u0000\u01a0\u01a1\u0005f\u0000\u0000\u01a1\u01a3\u0005"+
		"y\u0000\u0000\u01a2\u01a0\u0001\u0000\u0000\u0000\u01a3\u01a6\u0001\u0000"+
		"\u0000\u0000\u01a4\u01a2\u0001\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000"+
		"\u0000\u0000\u01a55\u0001\u0000\u0000\u0000\u01a6\u01a4\u0001\u0000\u0000"+
		"\u0000\u01a7\u01aa\u0005y\u0000\u0000\u01a8\u01a9\u0005\u0014\u0000\u0000"+
		"\u01a9\u01ab\u0003\u0090H\u0000\u01aa\u01a8\u0001\u0000\u0000\u0000\u01aa"+
		"\u01ab\u0001\u0000\u0000\u0000\u01ab7\u0001\u0000\u0000\u0000\u01ac\u01ae"+
		"\u0005^\u0000\u0000\u01ad\u01af\u0003<\u001e\u0000\u01ae\u01ad\u0001\u0000"+
		"\u0000\u0000\u01ae\u01af\u0001\u0000\u0000\u0000\u01af\u01b0\u0001\u0000"+
		"\u0000\u0000\u01b0\u01b1\u0005_\u0000\u0000\u01b19\u0001\u0000\u0000\u0000"+
		"\u01b2\u01b7\u0003\u0090H\u0000\u01b3\u01b4\u0005y\u0000\u0000\u01b4\u01b6"+
		"\u0005f\u0000\u0000\u01b5\u01b3\u0001\u0000\u0000\u0000\u01b6\u01b9\u0001"+
		"\u0000\u0000\u0000\u01b7\u01b5\u0001\u0000\u0000\u0000\u01b7\u01b8\u0001"+
		"\u0000\u0000\u0000\u01b8\u01ba\u0001\u0000\u0000\u0000\u01b9\u01b7\u0001"+
		"\u0000\u0000\u0000\u01ba\u01bb\u0005/\u0000\u0000\u01bb;\u0001\u0000\u0000"+
		"\u0000\u01bc\u01c1\u0003>\u001f\u0000\u01bd\u01be\u0005e\u0000\u0000\u01be"+
		"\u01c0\u0003>\u001f\u0000\u01bf\u01bd\u0001\u0000\u0000\u0000\u01c0\u01c3"+
		"\u0001\u0000\u0000\u0000\u01c1\u01bf\u0001\u0000\u0000\u0000\u01c1\u01c2"+
		"\u0001\u0000\u0000\u0000\u01c2=\u0001\u0000\u0000\u0000\u01c3\u01c1\u0001"+
		"\u0000\u0000\u0000\u01c4\u01c5\u0003\u0090H\u0000\u01c5\u01c6\u0005y\u0000"+
		"\u0000\u01c6?\u0001\u0000\u0000\u0000\u01c7\u01ca\u0003B!\u0000\u01c8"+
		"\u01ca\u0005d\u0000\u0000\u01c9\u01c7\u0001\u0000\u0000\u0000\u01c9\u01c8"+
		"\u0001\u0000\u0000\u0000\u01caA\u0001\u0000\u0000\u0000\u01cb\u01cf\u0005"+
		"`\u0000\u0000\u01cc\u01ce\u0003D\"\u0000\u01cd\u01cc\u0001\u0000\u0000"+
		"\u0000\u01ce\u01d1\u0001\u0000\u0000\u0000\u01cf\u01cd\u0001\u0000\u0000"+
		"\u0000\u01cf\u01d0\u0001\u0000\u0000\u0000\u01d0\u01d2\u0001\u0000\u0000"+
		"\u0000\u01d1\u01cf\u0001\u0000\u0000\u0000\u01d2\u01d3\u0005a\u0000\u0000"+
		"\u01d3C\u0001\u0000\u0000\u0000\u01d4\u01d5\u00054\u0000\u0000\u01d5\u01d6"+
		"\u0003t:\u0000\u01d6\u01d7\u0003B!\u0000\u01d7\u0203\u0001\u0000\u0000"+
		"\u0000\u01d8\u01d9\u0005\u0019\u0000\u0000\u01d9\u01da\u0005^\u0000\u0000"+
		"\u01da\u01db\u0003P(\u0000\u01db\u01dc\u0005_\u0000\u0000\u01dc\u01dd"+
		"\u0003B!\u0000\u01dd\u0203\u0001\u0000\u0000\u0000\u01de\u01df\u0005\u001a"+
		"\u0000\u0000\u01df\u01e0\u0003t:\u0000\u01e0\u01e3\u0003B!\u0000\u01e1"+
		"\u01e2\u0005\u0012\u0000\u0000\u01e2\u01e4\u0003B!\u0000\u01e3\u01e1\u0001"+
		"\u0000\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000\u0000\u01e4\u0203\u0001"+
		"\u0000\u0000\u0000\u01e5\u01e6\u00052\u0000\u0000\u01e6\u01e7\u0003B!"+
		"\u0000\u01e7\u01e8\u0003h4\u0000\u01e8\u0203\u0001\u0000\u0000\u0000\u01e9"+
		"\u01ea\u0005.\u0000\u0000\u01ea\u01ee\u0005`\u0000\u0000\u01eb\u01ed\u0003"+
		"p8\u0000\u01ec\u01eb\u0001\u0000\u0000\u0000\u01ed\u01f0\u0001\u0000\u0000"+
		"\u0000\u01ee\u01ec\u0001\u0000\u0000\u0000\u01ee\u01ef\u0001\u0000\u0000"+
		"\u0000\u01ef\u01f1\u0001\u0000\u0000\u0000\u01f0\u01ee\u0001\u0000\u0000"+
		"\u0000\u01f1\u0203\u0005a\u0000\u0000\u01f2\u01f4\u0005+\u0000\u0000\u01f3"+
		"\u01f5\u0003x<\u0000\u01f4\u01f3\u0001\u0000\u0000\u0000\u01f4\u01f5\u0001"+
		"\u0000\u0000\u0000\u01f5\u01f6\u0001\u0000\u0000\u0000\u01f6\u0203\u0005"+
		"d\u0000\u0000\u01f7\u01f8\u00050\u0000\u0000\u01f8\u01f9\u0003x<\u0000"+
		"\u01f9\u01fa\u0005d\u0000\u0000\u01fa\u0203\u0001\u0000\u0000\u0000\u01fb"+
		"\u0203\u0005d\u0000\u0000\u01fc\u01fd\u0003x<\u0000\u01fd\u01fe\u0005"+
		"d\u0000\u0000\u01fe\u0203\u0001\u0000\u0000\u0000\u01ff\u0200\u0003F#"+
		"\u0000\u0200\u0201\u0005d\u0000\u0000\u0201\u0203\u0001\u0000\u0000\u0000"+
		"\u0202\u01d4\u0001\u0000\u0000\u0000\u0202\u01d8\u0001\u0000\u0000\u0000"+
		"\u0202\u01de\u0001\u0000\u0000\u0000\u0202\u01e5\u0001\u0000\u0000\u0000"+
		"\u0202\u01e9\u0001\u0000\u0000\u0000\u0202\u01f2\u0001\u0000\u0000\u0000"+
		"\u0202\u01f7\u0001\u0000\u0000\u0000\u0202\u01fb\u0001\u0000\u0000\u0000"+
		"\u0202\u01fc\u0001\u0000\u0000\u0000\u0202\u01ff\u0001\u0000\u0000\u0000"+
		"\u0203E\u0001\u0000\u0000\u0000\u0204\u0205\u0005:\u0000\u0000\u0205\u0206"+
		"\u0005y\u0000\u0000\u0206\u0207\u0005;\u0000\u0000\u0207\u020f\u0003x"+
		"<\u0000\u0208\u0209\u0003\u0090H\u0000\u0209\u020c\u0005y\u0000\u0000"+
		"\u020a\u020b\u0005;\u0000\u0000\u020b\u020d\u0003x<\u0000\u020c\u020a"+
		"\u0001\u0000\u0000\u0000\u020c\u020d\u0001\u0000\u0000\u0000\u020d\u020f"+
		"\u0001\u0000\u0000\u0000\u020e\u0204\u0001\u0000\u0000\u0000\u020e\u0208"+
		"\u0001\u0000\u0000\u0000\u020fG\u0001\u0000\u0000\u0000\u0210\u0211\u0003"+
		"\u0094J\u0000\u0211\u0213\u0005`\u0000\u0000\u0212\u0214\u0003J%\u0000"+
		"\u0213\u0212\u0001\u0000\u0000\u0000\u0213\u0214\u0001\u0000\u0000\u0000"+
		"\u0214\u0215\u0001\u0000\u0000\u0000\u0215\u0216\u0005a\u0000\u0000\u0216"+
		"I\u0001\u0000\u0000\u0000\u0217\u021c\u0003L&\u0000\u0218\u0219\u0005"+
		"e\u0000\u0000\u0219\u021b\u0003L&\u0000\u021a\u0218\u0001\u0000\u0000"+
		"\u0000\u021b\u021e\u0001\u0000\u0000\u0000\u021c\u021a\u0001\u0000\u0000"+
		"\u0000\u021c\u021d\u0001\u0000\u0000\u0000\u021dK\u0001\u0000\u0000\u0000"+
		"\u021e\u021c\u0001\u0000\u0000\u0000\u021f\u0220\u0005y\u0000\u0000\u0220"+
		"\u0221\u0005B\u0000\u0000\u0221\u0222\u0003x<\u0000\u0222M\u0001\u0000"+
		"\u0000\u0000\u0223\u0224\u0007\u0002\u0000\u0000\u0224\u0225\u0005^\u0000"+
		"\u0000\u0225\u0226\u00034\u001a\u0000\u0226\u0227\u0005f\u0000\u0000\u0227"+
		"\u022c\u0005y\u0000\u0000\u0228\u0229\u0005e\u0000\u0000\u0229\u022b\u0003"+
		"x<\u0000\u022a\u0228\u0001\u0000\u0000\u0000\u022b\u022e\u0001\u0000\u0000"+
		"\u0000\u022c\u022a\u0001\u0000\u0000\u0000\u022c\u022d\u0001\u0000\u0000"+
		"\u0000\u022d\u022f\u0001\u0000\u0000\u0000\u022e\u022c\u0001\u0000\u0000"+
		"\u0000\u022f\u0230\u0005_\u0000\u0000\u0230O\u0001\u0000\u0000\u0000\u0231"+
		"\u0233\u0003R)\u0000\u0232\u0231\u0001\u0000\u0000\u0000\u0232\u0233\u0001"+
		"\u0000\u0000\u0000\u0233\u0234\u0001\u0000\u0000\u0000\u0234\u0236\u0005"+
		"d\u0000\u0000\u0235\u0237\u0003x<\u0000\u0236\u0235\u0001\u0000\u0000"+
		"\u0000\u0236\u0237\u0001\u0000\u0000\u0000\u0237\u0238\u0001\u0000\u0000"+
		"\u0000\u0238\u023a\u0005d\u0000\u0000\u0239\u023b\u0003V+\u0000\u023a"+
		"\u0239\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000\u023b"+
		"Q\u0001\u0000\u0000\u0000\u023c\u0241\u0003T*\u0000\u023d\u023e\u0005"+
		"e\u0000\u0000\u023e\u0240\u0003T*\u0000\u023f\u023d\u0001\u0000\u0000"+
		"\u0000\u0240\u0243\u0001\u0000\u0000\u0000\u0241\u023f\u0001\u0000\u0000"+
		"\u0000\u0241\u0242\u0001\u0000\u0000\u0000\u0242S\u0001\u0000\u0000\u0000"+
		"\u0243\u0241\u0001\u0000\u0000\u0000\u0244\u0245\u0003\u0090H\u0000\u0245"+
		"\u0246\u0005y\u0000\u0000\u0246\u0247\u0005;\u0000\u0000\u0247\u0248\u0003"+
		"x<\u0000\u0248U\u0001\u0000\u0000\u0000\u0249\u024e\u0003X,\u0000\u024a"+
		"\u024b\u0005e\u0000\u0000\u024b\u024d\u0003X,\u0000\u024c\u024a\u0001"+
		"\u0000\u0000\u0000\u024d\u0250\u0001\u0000\u0000\u0000\u024e\u024c\u0001"+
		"\u0000\u0000\u0000\u024e\u024f\u0001\u0000\u0000\u0000\u024fW\u0001\u0000"+
		"\u0000\u0000\u0250\u024e\u0001\u0000\u0000\u0000\u0251\u0252\u0005y\u0000"+
		"\u0000\u0252\u0253\u0005;\u0000\u0000\u0253\u0254\u0003x<\u0000\u0254"+
		"Y\u0001\u0000\u0000\u0000\u0255\u0256\u00034\u001a\u0000\u0256\u0257\u0005"+
		"f\u0000\u0000\u0257\u0258\u0005y\u0000\u0000\u0258[\u0001\u0000\u0000"+
		"\u0000\u0259\u025b\u0003\u0096K\u0000\u025a\u0259\u0001\u0000\u0000\u0000"+
		"\u025a\u025b\u0001\u0000\u0000\u0000\u025b\u025c\u0001\u0000\u0000\u0000"+
		"\u025c\u025d\u0003\u0094J\u0000\u025d\u025e\u0003\u0082A\u0000\u025e\u0263"+
		"\u0001\u0000\u0000\u0000\u025f\u0260\u0003\u0090H\u0000\u0260\u0261\u0003"+
		"\u0092I\u0000\u0261\u0263\u0001\u0000\u0000\u0000\u0262\u025a\u0001\u0000"+
		"\u0000\u0000\u0262\u025f\u0001\u0000\u0000\u0000\u0263]\u0001\u0000\u0000"+
		"\u0000\u0264\u0265\u0005b\u0000\u0000\u0265\u026a\u0005c\u0000\u0000\u0266"+
		"\u0267\u0005b\u0000\u0000\u0267\u0269\u0005c\u0000\u0000\u0268\u0266\u0001"+
		"\u0000\u0000\u0000\u0269\u026c\u0001\u0000\u0000\u0000\u026a\u0268\u0001"+
		"\u0000\u0000\u0000\u026a\u026b\u0001\u0000\u0000\u0000\u026b\u026d\u0001"+
		"\u0000\u0000\u0000\u026c\u026a\u0001\u0000\u0000\u0000\u026d\u026e\u0003"+
		"`0\u0000\u026e_\u0001\u0000\u0000\u0000\u026f\u027b\u0005`\u0000\u0000"+
		"\u0270\u0275\u0003b1\u0000\u0271\u0272\u0005e\u0000\u0000\u0272\u0274"+
		"\u0003b1\u0000\u0273\u0271\u0001\u0000\u0000\u0000\u0274\u0277\u0001\u0000"+
		"\u0000\u0000\u0275\u0273\u0001\u0000\u0000\u0000\u0275\u0276\u0001\u0000"+
		"\u0000\u0000\u0276\u0279\u0001\u0000\u0000\u0000\u0277\u0275\u0001\u0000"+
		"\u0000\u0000\u0278\u027a\u0005e\u0000\u0000\u0279\u0278\u0001\u0000\u0000"+
		"\u0000\u0279\u027a\u0001\u0000\u0000\u0000\u027a\u027c\u0001\u0000\u0000"+
		"\u0000\u027b\u0270\u0001\u0000\u0000\u0000\u027b\u027c\u0001\u0000\u0000"+
		"\u0000\u027c\u027d\u0001\u0000\u0000\u0000\u027d\u027e\u0005a\u0000\u0000"+
		"\u027ea\u0001\u0000\u0000\u0000\u027f\u0282\u0003`0\u0000\u0280\u0282"+
		"\u0003x<\u0000\u0281\u027f\u0001\u0000\u0000\u0000\u0281\u0280\u0001\u0000"+
		"\u0000\u0000\u0282c\u0001\u0000\u0000\u0000\u0283\u0285\u0005y\u0000\u0000"+
		"\u0284\u0286\u0003\u0096K\u0000\u0285\u0284\u0001\u0000\u0000\u0000\u0285"+
		"\u0286\u0001\u0000\u0000\u0000\u0286\u0289\u0001\u0000\u0000\u0000\u0287"+
		"\u0289\u0003\u0098L\u0000\u0288\u0283\u0001\u0000\u0000\u0000\u0288\u0287"+
		"\u0001\u0000\u0000\u0000\u0289e\u0001\u0000\u0000\u0000\u028a\u028b\u0003"+
		"\u0082A\u0000\u028bg\u0001\u0000\u0000\u0000\u028c\u028d\u0005\u0005\u0000"+
		"\u0000\u028d\u028f\u0005`\u0000\u0000\u028e\u0290\u0003j5\u0000\u028f"+
		"\u028e\u0001\u0000\u0000\u0000\u028f\u0290\u0001\u0000\u0000\u0000\u0290"+
		"\u0291\u0001\u0000\u0000\u0000\u0291\u0292\u0005a\u0000\u0000\u0292i\u0001"+
		"\u0000\u0000\u0000\u0293\u0298\u0003l6\u0000\u0294\u0295\u0005e\u0000"+
		"\u0000\u0295\u0297\u0003l6\u0000\u0296\u0294\u0001\u0000\u0000\u0000\u0297"+
		"\u029a\u0001\u0000\u0000\u0000\u0298\u0296\u0001\u0000\u0000\u0000\u0298"+
		"\u0299\u0001\u0000\u0000\u0000\u0299k\u0001\u0000\u0000\u0000\u029a\u0298"+
		"\u0001\u0000\u0000\u0000\u029b\u029c\u0005y\u0000\u0000\u029c\u029d\u0005"+
		"B\u0000\u0000\u029d\u02a3\u0005`\u0000\u0000\u029e\u029f\u0003n7\u0000"+
		"\u029f\u02a0\u0005e\u0000\u0000\u02a0\u02a2\u0001\u0000\u0000\u0000\u02a1"+
		"\u029e\u0001\u0000\u0000\u0000\u02a2\u02a5\u0001\u0000\u0000\u0000\u02a3"+
		"\u02a1\u0001\u0000\u0000\u0000\u02a3\u02a4\u0001\u0000\u0000\u0000\u02a4"+
		"\u02a6\u0001\u0000\u0000\u0000\u02a5\u02a3\u0001\u0000\u0000\u0000\u02a6"+
		"\u02a7\u0005\u0004\u0000\u0000\u02a7\u02a8\u0005B\u0000\u0000\u02a8\u02a9"+
		"\u0003x<\u0000\u02a9\u02aa\u0005a\u0000\u0000\u02aam\u0001\u0000\u0000"+
		"\u0000\u02ab\u02ac\u0005y\u0000\u0000\u02ac\u02ad\u0005B\u0000\u0000\u02ad"+
		"\u02ae\u0003x<\u0000\u02aeo\u0001\u0000\u0000\u0000\u02af\u02b0\u0003"+
		"r9\u0000\u02b0\u02b1\u0003B!\u0000\u02b1q\u0001\u0000\u0000\u0000\u02b2"+
		"\u02b3\u0005\u0003\u0000\u0000\u02b3\u02b4\u0003x<\u0000\u02b4\u02b5\u0005"+
		"g\u0000\u0000\u02b5\u02b9\u0001\u0000\u0000\u0000\u02b6\u02b7\u0005\u0004"+
		"\u0000\u0000\u02b7\u02b9\u0005g\u0000\u0000\u02b8\u02b2\u0001\u0000\u0000"+
		"\u0000\u02b8\u02b6\u0001\u0000\u0000\u0000\u02b9s\u0001\u0000\u0000\u0000"+
		"\u02ba\u02bb\u0005^\u0000\u0000\u02bb\u02bc\u0003x<\u0000\u02bc\u02bd"+
		"\u0005_\u0000\u0000\u02bdu\u0001\u0000\u0000\u0000\u02be\u02c3\u0003x"+
		"<\u0000\u02bf\u02c0\u0005e\u0000\u0000\u02c0\u02c2\u0003x<\u0000\u02c1"+
		"\u02bf\u0001\u0000\u0000\u0000\u02c2\u02c5\u0001\u0000\u0000\u0000\u02c3"+
		"\u02c1\u0001\u0000\u0000\u0000\u02c3\u02c4\u0001\u0000\u0000\u0000\u02c4"+
		"w\u0001\u0000\u0000\u0000\u02c5\u02c3\u0001\u0000\u0000\u0000\u02c6\u02c7"+
		"\u0006<\uffff\uffff\u0000\u02c7\u02e6\u0003z=\u0000\u02c8\u02c9\u0005"+
		"/\u0000\u0000\u02c9\u02cb\u0005^\u0000\u0000\u02ca\u02cc\u0003v;\u0000"+
		"\u02cb\u02ca\u0001\u0000\u0000\u0000\u02cb\u02cc\u0001\u0000\u0000\u0000"+
		"\u02cc\u02cd\u0001\u0000\u0000\u0000\u02cd\u02e6\u0005_\u0000\u0000\u02ce"+
		"\u02cf\u0005-\u0000\u0000\u02cf\u02d1\u0005^\u0000\u0000\u02d0\u02d2\u0003"+
		"v;\u0000\u02d1\u02d0\u0001\u0000\u0000\u0000\u02d1\u02d2\u0001\u0000\u0000"+
		"\u0000\u02d2\u02d3\u0001\u0000\u0000\u0000\u02d3\u02e6\u0005_\u0000\u0000"+
		"\u02d4\u02d5\u0007\u0003\u0000\u0000\u02d5\u02e6\u0003\\.\u0000\u02d6"+
		"\u02d7\u0005\'\u0000\u0000\u02d7\u02e6\u0003H$\u0000\u02d8\u02e6\u0003"+
		"N\'\u0000\u02d9\u02da\u0005^\u0000\u0000\u02da\u02db\u0003\u0090H\u0000"+
		"\u02db\u02dc\u0005_\u0000\u0000\u02dc\u02dd\u0003x<\u0014\u02dd\u02e6"+
		"\u0001\u0000\u0000\u0000\u02de\u02df\u0007\u0004\u0000\u0000\u02df\u02e6"+
		"\u0003x<\u0012\u02e0\u02e1\u0007\u0005\u0000\u0000\u02e1\u02e6\u0003x"+
		"<\u0011\u02e2\u02e6\u0003\u009eO\u0000\u02e3\u02e4\u0005y\u0000\u0000"+
		"\u02e4\u02e6\u0003\u0082A\u0000\u02e5\u02c6\u0001\u0000\u0000\u0000\u02e5"+
		"\u02c8\u0001\u0000\u0000\u0000\u02e5\u02ce\u0001\u0000\u0000\u0000\u02e5"+
		"\u02d4\u0001\u0000\u0000\u0000\u02e5\u02d6\u0001\u0000\u0000\u0000\u02e5"+
		"\u02d8\u0001\u0000\u0000\u0000\u02e5\u02d9\u0001\u0000\u0000\u0000\u02e5"+
		"\u02de\u0001\u0000\u0000\u0000\u02e5\u02e0\u0001\u0000\u0000\u0000\u02e5"+
		"\u02e2\u0001\u0000\u0000\u0000\u02e5\u02e3\u0001\u0000\u0000\u0000\u02e6"+
		"\u032f\u0001\u0000\u0000\u0000\u02e7\u02e8\n\u0010\u0000\u0000\u02e8\u02e9"+
		"\u0007\u0006\u0000\u0000\u02e9\u032e\u0003x<\u0011\u02ea\u02eb\n\u000f"+
		"\u0000\u0000\u02eb\u02ec\u0007\u0007\u0000\u0000\u02ec\u032e\u0003x<\u0010"+
		"\u02ed\u02f5\n\u000e\u0000\u0000\u02ee\u02ef\u0005=\u0000\u0000\u02ef"+
		"\u02f6\u0005=\u0000\u0000\u02f0\u02f1\u0005<\u0000\u0000\u02f1\u02f2\u0005"+
		"<\u0000\u0000\u02f2\u02f6\u0005<\u0000\u0000\u02f3\u02f4\u0005<\u0000"+
		"\u0000\u02f4\u02f6\u0005<\u0000\u0000\u02f5\u02ee\u0001\u0000\u0000\u0000"+
		"\u02f5\u02f0\u0001\u0000\u0000\u0000\u02f5\u02f3\u0001\u0000\u0000\u0000"+
		"\u02f6\u02f7\u0001\u0000\u0000\u0000\u02f7\u032e\u0003x<\u000f\u02f8\u02f9"+
		"\n\r\u0000\u0000\u02f9\u02fa\u0007\b\u0000\u0000\u02fa\u032e\u0003x<\u000e"+
		"\u02fb\u02fc\n\u000b\u0000\u0000\u02fc\u02fd\u0007\t\u0000\u0000\u02fd"+
		"\u032e\u0003x<\f\u02fe\u02ff\n\n\u0000\u0000\u02ff\u0300\u0005O\u0000"+
		"\u0000\u0300\u032e\u0003x<\u000b\u0301\u0302\n\t\u0000\u0000\u0302\u0303"+
		"\u0005Q\u0000\u0000\u0303\u032e\u0003x<\n\u0304\u0305\n\b\u0000\u0000"+
		"\u0305\u0306\u0005P\u0000\u0000\u0306\u032e\u0003x<\t\u0307\u0308\n\u0007"+
		"\u0000\u0000\u0308\u0309\u0005G\u0000\u0000\u0309\u032e\u0003x<\b\u030a"+
		"\u030b\n\u0006\u0000\u0000\u030b\u030c\u0005H\u0000\u0000\u030c\u032e"+
		"\u0003x<\u0007\u030d\u030e\n\u0005\u0000\u0000\u030e\u030f\u0005A\u0000"+
		"\u0000\u030f\u0310\u0003x<\u0000\u0310\u0311\u0005B\u0000\u0000\u0311"+
		"\u0312\u0003x<\u0005\u0312\u032e\u0001\u0000\u0000\u0000\u0313\u0314\n"+
		"\u0004\u0000\u0000\u0314\u0315\u0007\n\u0000\u0000\u0315\u032e\u0003x"+
		"<\u0004\u0316\u0317\n\u001b\u0000\u0000\u0317\u031b\u0005f\u0000\u0000"+
		"\u0318\u031c\u0005y\u0000\u0000\u0319\u031c\u0005/\u0000\u0000\u031a\u031c"+
		"\u0003\u0086C\u0000\u031b\u0318\u0001\u0000\u0000\u0000\u031b\u0319\u0001"+
		"\u0000\u0000\u0000\u031b\u031a\u0001\u0000\u0000\u0000\u031c\u032e\u0001"+
		"\u0000\u0000\u0000\u031d\u031e\n\u0018\u0000\u0000\u031e\u031f\u0005b"+
		"\u0000\u0000\u031f\u0320\u0003x<\u0000\u0320\u0321\u0005c\u0000\u0000"+
		"\u0321\u032e\u0001\u0000\u0000\u0000\u0322\u0323\n\u0013\u0000\u0000\u0323"+
		"\u032e\u0007\u000b\u0000\u0000\u0324\u0325\n\f\u0000\u0000\u0325\u0326"+
		"\u0005\u001c\u0000\u0000\u0326\u032e\u0003\u0090H\u0000\u0327\u0328\n"+
		"\u0002\u0000\u0000\u0328\u032a\u0005h\u0000\u0000\u0329\u032b\u0003\u0096"+
		"K\u0000\u032a\u0329\u0001\u0000\u0000\u0000\u032a\u032b\u0001\u0000\u0000"+
		"\u0000\u032b\u032c\u0001\u0000\u0000\u0000\u032c\u032e\u0005y\u0000\u0000"+
		"\u032d\u02e7\u0001\u0000\u0000\u0000\u032d\u02ea\u0001\u0000\u0000\u0000"+
		"\u032d\u02ed\u0001\u0000\u0000\u0000\u032d\u02f8\u0001\u0000\u0000\u0000"+
		"\u032d\u02fb\u0001\u0000\u0000\u0000\u032d\u02fe\u0001\u0000\u0000\u0000"+
		"\u032d\u0301\u0001\u0000\u0000\u0000\u032d\u0304\u0001\u0000\u0000\u0000"+
		"\u032d\u0307\u0001\u0000\u0000\u0000\u032d\u030a\u0001\u0000\u0000\u0000"+
		"\u032d\u030d\u0001\u0000\u0000\u0000\u032d\u0313\u0001\u0000\u0000\u0000"+
		"\u032d\u0316\u0001\u0000\u0000\u0000\u032d\u031d\u0001\u0000\u0000\u0000"+
		"\u032d\u0322\u0001\u0000\u0000\u0000\u032d\u0324\u0001\u0000\u0000\u0000"+
		"\u032d\u0327\u0001\u0000\u0000\u0000\u032e\u0331\u0001\u0000\u0000\u0000"+
		"\u032f\u032d\u0001\u0000\u0000\u0000\u032f\u0330\u0001\u0000\u0000\u0000"+
		"\u0330y\u0001\u0000\u0000\u0000\u0331\u032f\u0001\u0000\u0000\u0000\u0332"+
		"\u0333\u0005^\u0000\u0000\u0333\u0334\u0003x<\u0000\u0334\u0335\u0005"+
		"_\u0000\u0000\u0335\u033a\u0001\u0000\u0000\u0000\u0336\u033a\u0005/\u0000"+
		"\u0000\u0337\u033a\u0003\u0088D\u0000\u0338\u033a\u0005y\u0000\u0000\u0339"+
		"\u0332\u0001\u0000\u0000\u0000\u0339\u0336\u0001\u0000\u0000\u0000\u0339"+
		"\u0337\u0001\u0000\u0000\u0000\u0339\u0338\u0001\u0000\u0000\u0000\u033a"+
		"{\u0001\u0000\u0000\u0000\u033b\u033c\u0003\u0096K\u0000\u033c\u033d\u0003"+
		"~?\u0000\u033d}\u0001\u0000\u0000\u0000\u033e\u033f\u0005-\u0000\u0000"+
		"\u033f\u0343\u0003\u0080@\u0000\u0340\u0341\u0005y\u0000\u0000\u0341\u0343"+
		"\u0003\u0082A\u0000\u0342\u033e\u0001\u0000\u0000\u0000\u0342\u0340\u0001"+
		"\u0000\u0000\u0000\u0343\u007f\u0001\u0000\u0000\u0000\u0344\u034e\u0003"+
		"\u0082A\u0000\u0345\u0347\u0005f\u0000\u0000\u0346\u0348\u0003\u0096K"+
		"\u0000\u0347\u0346\u0001\u0000\u0000\u0000\u0347\u0348\u0001\u0000\u0000"+
		"\u0000\u0348\u0349\u0001\u0000\u0000\u0000\u0349\u034b\u0005y\u0000\u0000"+
		"\u034a\u034c\u0003\u0082A\u0000\u034b\u034a\u0001\u0000\u0000\u0000\u034b"+
		"\u034c\u0001\u0000\u0000\u0000\u034c\u034e\u0001\u0000\u0000\u0000\u034d"+
		"\u0344\u0001\u0000\u0000\u0000\u034d\u0345\u0001\u0000\u0000\u0000\u034e"+
		"\u0081\u0001\u0000\u0000\u0000\u034f\u0351\u0005^\u0000\u0000\u0350\u0352"+
		"\u0003v;\u0000\u0351\u0350\u0001\u0000\u0000\u0000\u0351\u0352\u0001\u0000"+
		"\u0000\u0000\u0352\u0353\u0001\u0000\u0000\u0000\u0353\u0354\u0005_\u0000"+
		"\u0000\u0354\u0083\u0001\u0000\u0000\u0000\u0355\u0357\u0005y\u0000\u0000"+
		"\u0356\u0358\u0003\u0096K\u0000\u0357\u0356\u0001\u0000\u0000\u0000\u0357"+
		"\u0358\u0001\u0000\u0000\u0000\u0358\u0085\u0001\u0000\u0000\u0000\u0359"+
		"\u035b\u0003\u0096K\u0000\u035a\u0359\u0001\u0000\u0000\u0000\u035a\u035b"+
		"\u0001\u0000\u0000\u0000\u035b\u035c\u0001\u0000\u0000\u0000\u035c\u035d"+
		"\u0005y\u0000\u0000\u035d\u035f\u0005^\u0000\u0000\u035e\u0360\u0003v"+
		";\u0000\u035f\u035e\u0001\u0000\u0000\u0000\u035f\u0360\u0001\u0000\u0000"+
		"\u0000\u0360\u0361\u0001\u0000\u0000\u0000\u0361\u0362\u0005_\u0000\u0000"+
		"\u0362\u0087\u0001\u0000\u0000\u0000\u0363\u036b\u0003\u008aE\u0000\u0364"+
		"\u036b\u0003\u008cF\u0000\u0365\u036b\u0005r\u0000\u0000\u0366\u036b\u0005"+
		"s\u0000\u0000\u0367\u036b\u0005q\u0000\u0000\u0368\u036b\u0005\r\u0000"+
		"\u0000\u0369\u036b\u0005t\u0000\u0000\u036a\u0363\u0001\u0000\u0000\u0000"+
		"\u036a\u0364\u0001\u0000\u0000\u0000\u036a\u0365\u0001\u0000\u0000\u0000"+
		"\u036a\u0366\u0001\u0000\u0000\u0000\u036a\u0367\u0001\u0000\u0000\u0000"+
		"\u036a\u0368\u0001\u0000\u0000\u0000\u036a\u0369\u0001\u0000\u0000\u0000"+
		"\u036b\u0089\u0001\u0000\u0000\u0000\u036c\u036d\u0007\f\u0000\u0000\u036d"+
		"\u008b\u0001\u0000\u0000\u0000\u036e\u036f\u0007\r\u0000\u0000\u036f\u008d"+
		"\u0001\u0000\u0000\u0000\u0370\u0373\u00053\u0000\u0000\u0371\u0373\u0003"+
		"\u0090H\u0000\u0372\u0370\u0001\u0000\u0000\u0000\u0372\u0371\u0001\u0000"+
		"\u0000\u0000\u0373\u008f\u0001\u0000\u0000\u0000\u0374\u0375\u0006H\uffff"+
		"\uffff\u0000\u0375\u038e\u0003\u0094J\u0000\u0376\u038e\u0003\u0098L\u0000"+
		"\u0377\u038e\u00055\u0000\u0000\u0378\u038e\u00056\u0000\u0000\u0379\u0382"+
		"\u0005^\u0000\u0000\u037a\u037f\u0003\u0090H\u0000\u037b\u037c\u0005e"+
		"\u0000\u0000\u037c\u037e\u0003\u0090H\u0000\u037d\u037b\u0001\u0000\u0000"+
		"\u0000\u037e\u0381\u0001\u0000\u0000\u0000\u037f\u037d\u0001\u0000\u0000"+
		"\u0000\u037f\u0380\u0001\u0000\u0000\u0000\u0380\u0383\u0001\u0000\u0000"+
		"\u0000\u0381\u037f\u0001\u0000\u0000\u0000\u0382\u037a\u0001\u0000\u0000"+
		"\u0000\u0382\u0383\u0001\u0000\u0000\u0000\u0383\u0384\u0001\u0000\u0000"+
		"\u0000\u0384\u0385\u0005_\u0000\u0000\u0385\u0386\u0005g\u0000\u0000\u0386"+
		"\u038e\u0003\u0090H\u0002\u0387\u0388\u0005b\u0000\u0000\u0388\u0389\u0003"+
		"\u0090H\u0000\u0389\u038a\u0005e\u0000\u0000\u038a\u038b\u0003\u0090H"+
		"\u0000\u038b\u038c\u0005c\u0000\u0000\u038c\u038e\u0001\u0000\u0000\u0000"+
		"\u038d\u0374\u0001\u0000\u0000\u0000\u038d\u0376\u0001\u0000\u0000\u0000"+
		"\u038d\u0377\u0001\u0000\u0000\u0000\u038d\u0378\u0001\u0000\u0000\u0000"+
		"\u038d\u0379\u0001\u0000\u0000\u0000\u038d\u0387\u0001\u0000\u0000\u0000"+
		"\u038e\u03a1\u0001\u0000\u0000\u0000\u038f\u0392\n\u0005\u0000\u0000\u0390"+
		"\u0391\u0005P\u0000\u0000\u0391\u0393\u0003\u0090H\u0000\u0392\u0390\u0001"+
		"\u0000\u0000\u0000\u0393\u0394\u0001\u0000\u0000\u0000\u0394\u0392\u0001"+
		"\u0000\u0000\u0000\u0394\u0395\u0001\u0000\u0000\u0000\u0395\u03a0\u0001"+
		"\u0000\u0000\u0000\u0396\u0399\n\u0004\u0000\u0000\u0397\u0398\u0005O"+
		"\u0000\u0000\u0398\u039a\u0003\u0090H\u0000\u0399\u0397\u0001\u0000\u0000"+
		"\u0000\u039a\u039b\u0001\u0000\u0000\u0000\u039b\u0399\u0001\u0000\u0000"+
		"\u0000\u039b\u039c\u0001\u0000\u0000\u0000\u039c\u03a0\u0001\u0000\u0000"+
		"\u0000\u039d\u039e\n\u0003\u0000\u0000\u039e\u03a0\u0003\u0092I\u0000"+
		"\u039f\u038f\u0001\u0000\u0000\u0000\u039f\u0396\u0001\u0000\u0000\u0000"+
		"\u039f\u039d\u0001\u0000\u0000\u0000\u03a0\u03a3\u0001\u0000\u0000\u0000"+
		"\u03a1\u039f\u0001\u0000\u0000\u0000\u03a1\u03a2\u0001\u0000\u0000\u0000"+
		"\u03a2\u0091\u0001\u0000\u0000\u0000\u03a3\u03a1\u0001\u0000\u0000\u0000"+
		"\u03a4\u03a5\u0007\u000e\u0000\u0000\u03a5\u0093\u0001\u0000\u0000\u0000"+
		"\u03a6\u03a8\u00034\u001a\u0000\u03a7\u03a9\u0003\u0096K\u0000\u03a8\u03a7"+
		"\u0001\u0000\u0000\u0000\u03a8\u03a9\u0001\u0000\u0000\u0000\u03a9\u0095"+
		"\u0001\u0000\u0000\u0000\u03aa\u03ab\u0005=\u0000\u0000\u03ab\u03b0\u0003"+
		"\u0090H\u0000\u03ac\u03ad\u0005e\u0000\u0000\u03ad\u03af\u0003\u0090H"+
		"\u0000\u03ae\u03ac\u0001\u0000\u0000\u0000\u03af\u03b2\u0001\u0000\u0000"+
		"\u0000\u03b0\u03ae\u0001\u0000\u0000\u0000\u03b0\u03b1\u0001\u0000\u0000"+
		"\u0000\u03b1\u03b3\u0001\u0000\u0000\u0000\u03b2\u03b0\u0001\u0000\u0000"+
		"\u0000\u03b3\u03b4\u0005<\u0000\u0000\u03b4\u0097\u0001\u0000\u0000\u0000"+
		"\u03b5\u03b6\u0007\u000f\u0000\u0000\u03b6\u0099\u0001\u0000\u0000\u0000"+
		"\u03b7\u03bf\u0003\u009cN\u0000\u03b8\u03bf\u0005#\u0000\u0000\u03b9\u03bf"+
		"\u0005\u0015\u0000\u0000\u03ba\u03bf\u0005\u0016\u0000\u0000\u03bb\u03bf"+
		"\u0005\u0017\u0000\u0000\u03bc\u03bf\u0005\n\u0000\u0000\u03bd\u03bf\u0005"+
		"9\u0000\u0000\u03be\u03b7\u0001\u0000\u0000\u0000\u03be\u03b8\u0001\u0000"+
		"\u0000\u0000\u03be\u03b9\u0001\u0000\u0000\u0000\u03be\u03ba\u0001\u0000"+
		"\u0000\u0000\u03be\u03bb\u0001\u0000\u0000\u0000\u03be\u03bc\u0001\u0000"+
		"\u0000\u0000\u03be\u03bd\u0001\u0000\u0000\u0000\u03bf\u009b\u0001\u0000"+
		"\u0000\u0000\u03c0\u03c1\u0007\u0010\u0000\u0000\u03c1\u009d\u0001\u0000"+
		"\u0000\u0000\u03c2\u03c3\u0003\u00a0P\u0000\u03c3\u03c4\u0005B\u0000\u0000"+
		"\u03c4\u03c5\u0003\u008eG\u0000\u03c5\u03c6\u0005g\u0000\u0000\u03c6\u03c7"+
		"\u0003\u00a2Q\u0000\u03c7\u009f\u0001\u0000\u0000\u0000\u03c8\u03ca\u0005"+
		"^\u0000\u0000\u03c9\u03cb\u0003<\u001e\u0000\u03ca\u03c9\u0001\u0000\u0000"+
		"\u0000\u03ca\u03cb\u0001\u0000\u0000\u0000\u03cb\u03cc\u0001\u0000\u0000"+
		"\u0000\u03cc\u03cd\u0005_\u0000\u0000\u03cd\u00a1\u0001\u0000\u0000\u0000"+
		"\u03ce\u03cf\u0003B!\u0000\u03cf\u00a3\u0001\u0000\u0000\u0000\u03d0\u03d1"+
		"\u0005\t\u0000\u0000\u03d1\u03d2\u0005y\u0000\u0000\u03d2\u03d6\u0005"+
		"`\u0000\u0000\u03d3\u03d5\u0003\u00a6S\u0000\u03d4\u03d3\u0001\u0000\u0000"+
		"\u0000\u03d5\u03d8\u0001\u0000\u0000\u0000\u03d6\u03d4\u0001\u0000\u0000"+
		"\u0000\u03d6\u03d7\u0001\u0000\u0000\u0000\u03d7\u03d9\u0001\u0000\u0000"+
		"\u0000\u03d8\u03d6\u0001\u0000\u0000\u0000\u03d9\u03da\u0005a\u0000\u0000"+
		"\u03da\u00a5\u0001\u0000\u0000\u0000\u03db\u03dc\u0005y\u0000\u0000\u03dc"+
		"\u03dd\u0005B\u0000\u0000\u03dd\u03de\u0003x<\u0000\u03de\u03df\u0005"+
		"d\u0000\u0000\u03df\u00a7\u0001\u0000\u0000\u0000\u03e0\u03e1\u0005i\u0000"+
		"\u0000\u03e1\u03e2\u0005y\u0000\u0000\u03e2\u00a9\u0001\u0000\u0000\u0000"+
		"h\u00ab\u00b0\u00b6\u00c3\u00c9\u00cc\u00d1\u00d7\u00db\u00df\u00e7\u00f1"+
		"\u00f7\u00fc\u0104\u010b\u010f\u0112\u0115\u011e\u0123\u0129\u012f\u0135"+
		"\u0139\u0141\u0149\u014e\u0155\u0159\u0166\u016b\u0173\u017a\u0182\u0188"+
		"\u0192\u019c\u01a4\u01aa\u01ae\u01b7\u01c1\u01c9\u01cf\u01e3\u01ee\u01f4"+
		"\u0202\u020c\u020e\u0213\u021c\u022c\u0232\u0236\u023a\u0241\u024e\u025a"+
		"\u0262\u026a\u0275\u0279\u027b\u0281\u0285\u0288\u028f\u0298\u02a3\u02b8"+
		"\u02c3\u02cb\u02d1\u02e5\u02f5\u031b\u032a\u032d\u032f\u0339\u0342\u0347"+
		"\u034b\u034d\u0351\u0357\u035a\u035f\u036a\u0372\u037f\u0382\u038d\u0394"+
		"\u039b\u039f\u03a1\u03a8\u03b0\u03be\u03ca\u03d6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}