// Generated from AssemblyParser.g4 by ANTLR 4.13.2
package org.metavm.asm.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

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
		INT=29, CHAR=30, INTERFACE=31, NATIVE=32, ENEW=33, UNEW=34, NEW=35, ALLOCATE=36, 
		PRIVATE=37, PROTECTED=38, PUBLIC=39, RETURN=40, STATIC=41, SUPER=42, SWITCH=43, 
		THIS=44, THROW=45, THROWS=46, TRY=47, VOID=48, WHILE=49, ANY=50, NEVER=51, 
		SELECT=52, SELECT_FIRST=53, DELETED=54, ASSIGN=55, GT=56, LT=57, BANG=58, 
		TILDE=59, QUESTION=60, COLON=61, EQUAL=62, LE=63, GE=64, NOTEQUAL=65, 
		AND=66, OR=67, INC=68, DEC=69, ADD=70, SUB=71, MUL=72, DIV=73, BITAND=74, 
		BITOR=75, CARET=76, MOD=77, ADD_ASSIGN=78, SUB_ASSIGN=79, MUL_ASSIGN=80, 
		DIV_ASSIGN=81, AND_ASSIGN=82, OR_ASSIGN=83, XOR_ASSIGN=84, MOD_ASSIGN=85, 
		LSHIFT_ASSIGN=86, RSHIFT_ASSIGN=87, URSHIFT_ASSIGN=88, LPAREN=89, RPAREN=90, 
		LBRACE=91, RBRACE=92, LBRACK=93, RBRACK=94, SEMI=95, COMMA=96, DOT=97, 
		ARROW=98, COLONCOLON=99, AT=100, ELLIPSIS=101, DECIMAL_LITERAL=102, HEX_LITERAL=103, 
		OCT_LITERAL=104, BINARY_LITERAL=105, FLOAT_LITERAL=106, HEX_FLOAT_LITERAL=107, 
		BOOL_LITERAL=108, CHAR_LITERAL=109, STRING_LITERAL=110, TEXT_BLOCK=111, 
		R=112, RW=113, C=114, V=115, IDENTIFIER=116, WS=117, COMMENT=118, LINE_COMMENT=119;
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
		RULE_block = 33, RULE_labeledStatement = 34, RULE_statement = 35, RULE_allocator = 36, 
		RULE_allocatorFieldList = 37, RULE_allocatorField = 38, RULE_select = 39, 
		RULE_forControl = 40, RULE_loopVariableDeclarators = 41, RULE_loopVariableDeclarator = 42, 
		RULE_loopVariableUpdates = 43, RULE_loopVariableUpdate = 44, RULE_qualifiedFieldName = 45, 
		RULE_creator = 46, RULE_arrayCreatorRest = 47, RULE_arrayInitializer = 48, 
		RULE_variableInitializer = 49, RULE_createdName = 50, RULE_classCreatorRest = 51, 
		RULE_catchClause = 52, RULE_catchFields = 53, RULE_catchField = 54, RULE_catchValue = 55, 
		RULE_branchCase = 56, RULE_switchLabel = 57, RULE_parExpression = 58, 
		RULE_expressionList = 59, RULE_expression = 60, RULE_primary = 61, RULE_explicitGenericInvocation = 62, 
		RULE_explicitGenericInvocationSuffix = 63, RULE_superSuffix = 64, RULE_arguments = 65, 
		RULE_classType = 66, RULE_methodCall = 67, RULE_functionCall = 68, RULE_literal = 69, 
		RULE_integerLiteral = 70, RULE_floatLiteral = 71, RULE_typeTypeOrVoid = 72, 
		RULE_typeType = 73, RULE_arrayKind = 74, RULE_classOrInterfaceType = 75, 
		RULE_typeArguments = 76, RULE_primitiveType = 77, RULE_modifier = 78, 
		RULE_classOrInterfaceModifier = 79, RULE_lambda = 80, RULE_lambdaParameters = 81, 
		RULE_lambdaBody = 82, RULE_indexDeclaration = 83, RULE_indexField = 84, 
		RULE_annotation = 85;
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
			"formalParameterList", "formalParameter", "methodBody", "block", "labeledStatement", 
			"statement", "allocator", "allocatorFieldList", "allocatorField", "select", 
			"forControl", "loopVariableDeclarators", "loopVariableDeclarator", "loopVariableUpdates", 
			"loopVariableUpdate", "qualifiedFieldName", "creator", "arrayCreatorRest", 
			"arrayInitializer", "variableInitializer", "createdName", "classCreatorRest", 
			"catchClause", "catchFields", "catchField", "catchValue", "branchCase", 
			"switchLabel", "parExpression", "expressionList", "expression", "primary", 
			"explicitGenericInvocation", "explicitGenericInvocationSuffix", "superSuffix", 
			"arguments", "classType", "methodCall", "functionCall", "literal", "integerLiteral", 
			"floatLiteral", "typeTypeOrVoid", "typeType", "arrayKind", "classOrInterfaceType", 
			"typeArguments", "primitiveType", "modifier", "classOrInterfaceModifier", 
			"lambda", "lambdaParameters", "lambdaBody", "indexDeclaration", "indexField", 
			"annotation"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'boolean'", "'case'", "'default'", "'catch'", "'string'", 
			"'class'", "'record'", "'index'", "'unique'", "'struct'", "'time'", "'null'", 
			"'package'", "'import'", "'password'", "'double'", "'else'", "'enum'", 
			"'extends'", "'readonly'", "'child'", "'title'", "'finally'", "'for'", 
			"'if'", "'implements'", "'instanceof'", "'int'", "'char'", "'interface'", 
			"'native'", "'enew'", "'unew'", "'new'", "'allocate'", "'private'", "'protected'", 
			"'public'", "'return'", "'static'", "'super'", "'switch'", "'this'", 
			"'throw'", "'throws'", "'try'", "'void'", "'while'", "'any'", "'never'", 
			"'select'", "'selectFirst'", "'deleted'", "'='", "'>'", "'<'", "'!'", 
			"'~'", "'?'", "':'", "'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", 
			"'++'", "'--'", "'+'", "'-'", "'*'", "'/'", "'&'", "'|'", "'^'", "'%'", 
			"'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", 
			"'>>='", "'>>>='", "'('", "')'", "'{'", "'}'", "'['", "']'", "';'", "','", 
			"'.'", "'->'", "'::'", "'@'", "'...'", null, null, null, null, null, 
			null, null, null, null, null, "'[r]'", "'[rw]'", "'[c]'", "'[v]'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSTRACT", "BOOLEAN", "CASE", "DEFAULT", "CATCH", "STRING", "CLASS", 
			"RECORD", "INDEX", "UNIQUE", "STRUCT", "TIME", "NULL", "PACKAGE", "IMPORT", 
			"PASSWORD", "DOUBLE", "ELSE", "ENUM", "EXTENDS", "READONLY", "CHILD", 
			"TITLE", "FINALLY", "FOR", "IF", "IMPLEMENTS", "INSTANCEOF", "INT", "CHAR", 
			"INTERFACE", "NATIVE", "ENEW", "UNEW", "NEW", "ALLOCATE", "PRIVATE", 
			"PROTECTED", "PUBLIC", "RETURN", "STATIC", "SUPER", "SWITCH", "THIS", 
			"THROW", "THROWS", "TRY", "VOID", "WHILE", "ANY", "NEVER", "SELECT", 
			"SELECT_FIRST", "DELETED", "ASSIGN", "GT", "LT", "BANG", "TILDE", "QUESTION", 
			"COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", 
			"ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", "MOD", "ADD_ASSIGN", 
			"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", 
			"XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", 
			"LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", 
			"DOT", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "DECIMAL_LITERAL", "HEX_LITERAL", 
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
			setState(173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PACKAGE) {
				{
				setState(172);
				packageDeclaration();
				}
			}

			setState(178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(175);
				importDeclaration();
				}
				}
				setState(180);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(182); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(181);
				typeDeclaration();
				}
				}
				setState(184); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 3163243940226L) != 0) || _la==SEMI || _la==AT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
			setState(186);
			match(PACKAGE);
			setState(187);
			qualifiedName();
			setState(188);
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
			setState(190);
			match(IMPORT);
			setState(191);
			qualifiedName();
			setState(192);
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
			setState(206);
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
				setState(197);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3161095929858L) != 0)) {
					{
					{
					setState(194);
					classOrInterfaceModifier();
					}
					}
					setState(199);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(203);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
				case 1:
					{
					setState(200);
					classDeclaration();
					}
					break;
				case 2:
					{
					setState(201);
					enumDeclaration();
					}
					break;
				case 3:
					{
					setState(202);
					interfaceDeclaration();
					}
					break;
				}
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(205);
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
			setState(211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(208);
				annotation();
				}
				}
				setState(213);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(214);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2432L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(215);
			match(IDENTIFIER);
			setState(217);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(216);
				typeParameters();
				}
			}

			setState(221);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(219);
				match(EXTENDS);
				setState(220);
				typeType(0);
				}
			}

			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(223);
				match(IMPLEMENTS);
				setState(224);
				typeList();
				}
			}

			setState(227);
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
			setState(229);
			match(LBRACE);
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 165791928298976838L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217745L) != 0)) {
				{
				{
				setState(230);
				classBodyDeclaration();
				}
				}
				setState(235);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(236);
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
			setState(238);
			typeType(0);
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(239);
				match(COMMA);
				setState(240);
				typeType(0);
				}
				}
				setState(245);
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
			setState(254);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(249);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18017563915060226L) != 0)) {
					{
					{
					setState(246);
					modifier();
					}
					}
					setState(251);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(252);
				memberDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
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
			setState(256);
			match(STATIC);
			setState(257);
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
			setState(262);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(259);
				annotation();
				}
				}
				setState(264);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(265);
			match(ENUM);
			setState(266);
			match(IDENTIFIER);
			setState(269);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(267);
				match(IMPLEMENTS);
				setState(268);
				typeList();
				}
			}

			setState(271);
			match(LBRACE);
			setState(273);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(272);
				enumConstants();
				}
			}

			setState(276);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(275);
				match(COMMA);
				}
			}

			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(278);
				enumBodyDeclarations();
				}
			}

			setState(281);
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
			setState(283);
			enumConstant();
			setState(288);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(284);
					match(COMMA);
					setState(285);
					enumConstant();
					}
					} 
				}
				setState(290);
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
			setState(291);
			match(IDENTIFIER);
			setState(293);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(292);
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
			setState(295);
			match(SEMI);
			setState(299);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 165791928298976838L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217745L) != 0)) {
				{
				{
				setState(296);
				classBodyDeclaration();
				}
				}
				setState(301);
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
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(302);
				annotation();
				}
				}
				setState(307);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(308);
			match(INTERFACE);
			setState(309);
			match(IDENTIFIER);
			setState(311);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(310);
				typeParameters();
				}
			}

			setState(315);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(313);
				match(EXTENDS);
				setState(314);
				typeList();
				}
			}

			setState(317);
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
			setState(319);
			match(LBRACE);
			setState(323);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 165791928298976342L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217809L) != 0)) {
				{
				{
				setState(320);
				interfaceBodyDeclaration();
				}
				}
				setState(325);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(326);
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
			setState(336);
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
			case INT:
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
				setState(331);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(328);
						modifier();
						}
						} 
					}
					setState(333);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
				}
				setState(334);
				interfaceMemberDeclaration();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(335);
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
			setState(338);
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
			setState(343);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2748779069458L) != 0)) {
				{
				{
				setState(340);
				interfaceMethodModifier();
				}
				}
				setState(345);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(347);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(346);
				typeParameters();
				}
			}

			setState(349);
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
			setState(351);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2748779069458L) != 0)) ) {
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
			setState(353);
			typeTypeOrVoid();
			setState(354);
			match(IDENTIFIER);
			setState(355);
			formalParameters();
			setState(360);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(356);
				match(LBRACK);
				setState(357);
				match(RBRACK);
				}
				}
				setState(362);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(365);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(363);
				match(THROWS);
				setState(364);
				qualifiedNameList();
				}
			}

			setState(367);
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
			setState(373);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(369);
				methodDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(370);
				fieldDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(371);
				constructorDeclaration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(372);
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
			setState(375);
			typeType(0);
			setState(376);
			match(IDENTIFIER);
			setState(377);
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
			setState(380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(379);
				typeParameters();
				}
			}

			setState(382);
			typeTypeOrVoid();
			setState(383);
			match(IDENTIFIER);
			setState(384);
			formalParameters();
			setState(385);
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
			setState(388);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(387);
				typeParameters();
				}
			}

			setState(390);
			match(IDENTIFIER);
			setState(391);
			formalParameters();
			setState(394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(392);
				match(THROWS);
				setState(393);
				qualifiedNameList();
				}
			}

			setState(396);
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
			setState(398);
			match(LT);
			setState(399);
			typeParameter();
			setState(404);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(400);
				match(COMMA);
				setState(401);
				typeParameter();
				}
				}
				setState(406);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(407);
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
			setState(409);
			qualifiedName();
			setState(414);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(410);
				match(COMMA);
				setState(411);
				qualifiedName();
				}
				}
				setState(416);
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
			setState(417);
			match(IDENTIFIER);
			setState(422);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(418);
					match(DOT);
					setState(419);
					match(IDENTIFIER);
					}
					} 
				}
				setState(424);
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
			setState(425);
			match(IDENTIFIER);
			setState(428);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(426);
				match(EXTENDS);
				setState(427);
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
			setState(430);
			match(LPAREN);
			setState(432);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3659176308060228L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217745L) != 0)) {
				{
				setState(431);
				formalParameterList();
				}
			}

			setState(434);
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
			setState(436);
			typeType(0);
			setState(441);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(437);
				match(IDENTIFIER);
				setState(438);
				match(DOT);
				}
				}
				setState(443);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(444);
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
			setState(446);
			formalParameter();
			setState(451);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(447);
				match(COMMA);
				setState(448);
				formalParameter();
				}
				}
				setState(453);
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
			setState(454);
			typeType(0);
			setState(455);
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
			setState(459);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(457);
				block();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(458);
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
		public List<LabeledStatementContext> labeledStatement() {
			return getRuleContexts(LabeledStatementContext.class);
		}
		public LabeledStatementContext labeledStatement(int i) {
			return getRuleContext(LabeledStatementContext.class,i);
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
			setState(461);
			match(LBRACE);
			setState(465);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 878972813938008064L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299050119200783L) != 0)) {
				{
				{
				setState(462);
				labeledStatement();
				}
				}
				setState(467);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(468);
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
	public static class LabeledStatementContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode COLON() { return getToken(AssemblyParser.COLON, 0); }
		public LabeledStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_labeledStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLabeledStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLabeledStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLabeledStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabeledStatementContext labeledStatement() throws RecognitionException {
		LabeledStatementContext _localctx = new LabeledStatementContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_labeledStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(472);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				{
				setState(470);
				match(IDENTIFIER);
				setState(471);
				match(COLON);
				}
				break;
			}
			setState(474);
			statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TypeTypeContext castType;
		public Token bop;
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
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public TerminalNode BANG() { return getToken(AssemblyParser.BANG, 0); }
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
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
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public List<TerminalNode> IDENTIFIER() { return getTokens(AssemblyParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(AssemblyParser.IDENTIFIER, i);
		}
		public TerminalNode THIS() { return getToken(AssemblyParser.THIS, 0); }
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
		public LambdaContext lambda() {
			return getRuleContext(LambdaContext.class,0);
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
		enterRule(_localctx, 70, RULE_statement);
		int _la;
		try {
			setState(550);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(476);
				match(WHILE);
				setState(477);
				parExpression();
				setState(478);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(480);
				match(FOR);
				setState(481);
				match(LPAREN);
				setState(482);
				forControl();
				setState(483);
				match(RPAREN);
				setState(484);
				block();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(486);
				match(IF);
				setState(487);
				parExpression();
				setState(488);
				block();
				setState(491);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(489);
					match(ELSE);
					setState(490);
					block();
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(493);
				match(TRY);
				setState(494);
				block();
				setState(495);
				catchClause();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(497);
				match(SWITCH);
				setState(498);
				match(LBRACE);
				setState(502);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(499);
					branchCase();
					}
					}
					setState(504);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(505);
				match(RBRACE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(506);
				match(RETURN);
				setState(508);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
					{
					setState(507);
					expression(0);
					}
				}

				setState(510);
				match(SEMI);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(511);
				match(THROW);
				setState(512);
				expression(0);
				setState(513);
				match(SEMI);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(515);
				match(SEMI);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(516);
				methodCall();
				setState(518);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BANG) {
					{
					setState(517);
					match(BANG);
					}
				}

				setState(520);
				match(SEMI);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(522);
				functionCall();
				setState(523);
				match(SEMI);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(525);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 60129542144L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(526);
				creator();
				setState(527);
				match(SEMI);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(529);
				match(ALLOCATE);
				setState(530);
				allocator();
				setState(531);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(533);
				select();
				setState(534);
				match(SEMI);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(536);
				match(LPAREN);
				setState(537);
				((StatementContext)_localctx).castType = typeType(0);
				setState(538);
				match(RPAREN);
				setState(539);
				expression(0);
				setState(540);
				match(SEMI);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(542);
				_la = _input.LA(1);
				if ( !(_la==THIS || _la==IDENTIFIER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(543);
				match(DOT);
				setState(544);
				match(IDENTIFIER);
				setState(545);
				((StatementContext)_localctx).bop = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 55)) & ~0x3f) == 0 && ((1L << (_la - 55)) & 17171480577L) != 0)) ) {
					((StatementContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(546);
				expression(0);
				setState(547);
				match(SEMI);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(549);
				lambda();
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
			setState(552);
			classOrInterfaceType();
			setState(553);
			match(LBRACE);
			setState(555);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(554);
				allocatorFieldList();
				}
			}

			setState(557);
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
			setState(559);
			allocatorField();
			setState(564);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(560);
				match(COMMA);
				setState(561);
				allocatorField();
				}
				}
				setState(566);
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
			setState(567);
			match(IDENTIFIER);
			setState(568);
			match(COLON);
			setState(569);
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
			setState(571);
			_la = _input.LA(1);
			if ( !(_la==SELECT || _la==SELECT_FIRST) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(572);
			match(LPAREN);
			setState(573);
			qualifiedName();
			setState(574);
			match(DOT);
			setState(575);
			match(IDENTIFIER);
			setState(580);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(576);
				match(COMMA);
				setState(577);
				expression(0);
				}
				}
				setState(582);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(583);
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
			setState(586);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3659176308060228L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217745L) != 0)) {
				{
				setState(585);
				loopVariableDeclarators();
				}
			}

			setState(588);
			match(SEMI);
			setState(590);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
				{
				setState(589);
				expression(0);
				}
			}

			setState(592);
			match(SEMI);
			setState(594);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(593);
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
			setState(596);
			loopVariableDeclarator();
			setState(601);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(597);
				match(COMMA);
				setState(598);
				loopVariableDeclarator();
				}
				}
				setState(603);
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
			setState(604);
			typeType(0);
			setState(605);
			match(IDENTIFIER);
			setState(606);
			match(ASSIGN);
			setState(607);
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
			setState(609);
			loopVariableUpdate();
			setState(614);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(610);
				match(COMMA);
				setState(611);
				loopVariableUpdate();
				}
				}
				setState(616);
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
			setState(617);
			match(IDENTIFIER);
			setState(618);
			match(ASSIGN);
			setState(619);
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
			setState(621);
			qualifiedName();
			setState(622);
			match(DOT);
			setState(623);
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
			setState(634);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(626);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(625);
					typeArguments();
					}
				}

				setState(628);
				classOrInterfaceType();
				setState(629);
				arguments();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(631);
				typeType(0);
				setState(632);
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
			setState(636);
			match(LBRACK);
			setState(637);
			match(RBRACK);
			setState(642);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(638);
				match(LBRACK);
				setState(639);
				match(RBRACK);
				}
				}
				setState(644);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(645);
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
			setState(647);
			match(LBRACE);
			setState(659);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049993371663L) != 0)) {
				{
				setState(648);
				variableInitializer();
				setState(653);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(649);
						match(COMMA);
						setState(650);
						variableInitializer();
						}
						} 
					}
					setState(655);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
				}
				setState(657);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(656);
					match(COMMA);
					}
				}

				}
			}

			setState(661);
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
			setState(665);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(663);
				arrayInitializer();
				}
				break;
			case NULL:
			case THIS:
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
				setState(664);
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
			setState(672);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(667);
				match(IDENTIFIER);
				setState(669);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(668);
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
			case INT:
			case CHAR:
			case VOID:
				enterOuterAlt(_localctx, 2);
				{
				setState(671);
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
			setState(674);
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
			setState(676);
			match(CATCH);
			setState(677);
			match(LBRACE);
			setState(679);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(678);
				catchFields();
				}
			}

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
			setState(683);
			catchField();
			setState(688);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(684);
				match(COMMA);
				setState(685);
				catchField();
				}
				}
				setState(690);
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
			setState(691);
			match(IDENTIFIER);
			setState(692);
			match(COLON);
			setState(693);
			match(LBRACE);
			setState(699);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(694);
				catchValue();
				setState(695);
				match(COMMA);
				}
				}
				setState(701);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(702);
			match(DEFAULT);
			setState(703);
			match(COLON);
			setState(704);
			expression(0);
			setState(705);
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
			setState(707);
			match(IDENTIFIER);
			setState(708);
			match(COLON);
			setState(709);
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
			setState(711);
			switchLabel();
			setState(712);
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
			setState(720);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(714);
				match(CASE);
				{
				setState(715);
				expression(0);
				}
				setState(716);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(718);
				match(DEFAULT);
				setState(719);
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
			setState(722);
			match(LPAREN);
			setState(723);
			expression(0);
			setState(724);
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
			setState(726);
			expression(0);
			setState(731);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(727);
				match(COMMA);
				setState(728);
				expression(0);
				}
				}
				setState(733);
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
		public Token prefix;
		public Token bop;
		public Token postfix;
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ADD() { return getToken(AssemblyParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(AssemblyParser.SUB, 0); }
		public TerminalNode INC() { return getToken(AssemblyParser.INC, 0); }
		public TerminalNode DEC() { return getToken(AssemblyParser.DEC, 0); }
		public TerminalNode TILDE() { return getToken(AssemblyParser.TILDE, 0); }
		public TerminalNode BANG() { return getToken(AssemblyParser.BANG, 0); }
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
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public TerminalNode THIS() { return getToken(AssemblyParser.THIS, 0); }
		public TerminalNode LBRACK() { return getToken(AssemblyParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(AssemblyParser.RBRACK, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
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
			setState(742);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				{
				setState(735);
				primary();
				}
				break;
			case 2:
				{
				setState(736);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 15L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(737);
				expression(16);
				}
				break;
			case 3:
				{
				setState(738);
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
				setState(739);
				expression(15);
				}
				break;
			case 4:
				{
				setState(740);
				match(IDENTIFIER);
				setState(741);
				arguments();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(809);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(807);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(744);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(745);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 72)) & ~0x3f) == 0 && ((1L << (_la - 72)) & 35L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(746);
						expression(15);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(747);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(748);
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
						setState(749);
						expression(14);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(750);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(758);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
						case 1:
							{
							setState(751);
							match(LT);
							setState(752);
							match(LT);
							}
							break;
						case 2:
							{
							setState(753);
							match(GT);
							setState(754);
							match(GT);
							setState(755);
							match(GT);
							}
							break;
						case 3:
							{
							setState(756);
							match(GT);
							setState(757);
							match(GT);
							}
							break;
						}
						setState(760);
						expression(13);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(761);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(762);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 56)) & ~0x3f) == 0 && ((1L << (_la - 56)) & 387L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(763);
						expression(12);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(764);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(765);
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
						setState(766);
						expression(10);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(767);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(768);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(769);
						expression(9);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(770);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(771);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(772);
						expression(8);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(773);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(774);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(775);
						expression(7);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(776);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(777);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(778);
						expression(6);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(779);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(780);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(781);
						expression(5);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(782);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(783);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(784);
						expression(0);
						setState(785);
						match(COLON);
						setState(786);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(788);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(789);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(790);
						_la = _input.LA(1);
						if ( !(_la==THIS || _la==IDENTIFIER) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 13:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(791);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(792);
						match(LBRACK);
						setState(793);
						expression(0);
						setState(794);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(796);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(797);
						((ExpressionContext)_localctx).postfix = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==INC || _la==DEC) ) {
							((ExpressionContext)_localctx).postfix = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 15:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(798);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(799);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(800);
						typeType(0);
						}
						break;
					case 16:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(801);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(802);
						match(COLONCOLON);
						setState(804);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(803);
							typeArguments();
							}
						}

						setState(806);
						match(IDENTIFIER);
						}
						break;
					}
					} 
				}
				setState(811);
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
			setState(819);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(812);
				match(LPAREN);
				setState(813);
				expression(0);
				setState(814);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(816);
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
				setState(817);
				literal();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 4);
				{
				setState(818);
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
			setState(821);
			typeArguments();
			setState(822);
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
			setState(828);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(824);
				match(SUPER);
				setState(825);
				superSuffix();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(826);
				match(IDENTIFIER);
				setState(827);
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
			setState(839);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(830);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(831);
				match(DOT);
				setState(833);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(832);
					typeArguments();
					}
				}

				setState(835);
				match(IDENTIFIER);
				setState(837);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(836);
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
			setState(841);
			match(LPAREN);
			setState(843);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
				{
				setState(842);
				expressionList();
				}
			}

			setState(845);
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
			setState(847);
			match(IDENTIFIER);
			setState(849);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(848);
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
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(AssemblyParser.DOT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(AssemblyParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode THIS() { return getToken(AssemblyParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(AssemblyParser.SUPER, 0); }
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
			setState(875);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(851);
				expression(0);
				setState(852);
				match(DOT);
				setState(854);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(853);
					typeArguments();
					}
				}

				setState(856);
				match(IDENTIFIER);
				setState(857);
				match(LPAREN);
				setState(859);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
					{
					setState(858);
					expressionList();
					}
				}

				setState(861);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(863);
				match(THIS);
				setState(864);
				match(LPAREN);
				setState(866);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
					{
					setState(865);
					expressionList();
					}
				}

				setState(868);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(869);
				match(SUPER);
				setState(870);
				match(LPAREN);
				setState(872);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
					{
					setState(871);
					expressionList();
					}
				}

				setState(874);
				match(RPAREN);
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
	public static class FunctionCallContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(877);
			expression(0);
			setState(878);
			match(LPAREN);
			setState(880);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 864708720641187840L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 299049984983055L) != 0)) {
				{
				setState(879);
				expressionList();
				}
			}

			setState(882);
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
		enterRule(_localctx, 138, RULE_literal);
		try {
			setState(891);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(884);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(885);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(886);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(887);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(888);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(889);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(890);
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
		enterRule(_localctx, 140, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(893);
			_la = _input.LA(1);
			if ( !(((((_la - 102)) & ~0x3f) == 0 && ((1L << (_la - 102)) & 15L) != 0)) ) {
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
		enterRule(_localctx, 142, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(895);
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
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode VOID() { return getToken(AssemblyParser.VOID, 0); }
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
		enterRule(_localctx, 144, RULE_typeTypeOrVoid);
		try {
			setState(899);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(897);
				typeType(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(898);
				match(VOID);
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
		int _startState = 146;
		enterRecursionRule(_localctx, 146, RULE_typeType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(926);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(902);
				classOrInterfaceType();
				}
				break;
			case BOOLEAN:
			case STRING:
			case TIME:
			case NULL:
			case PASSWORD:
			case DOUBLE:
			case INT:
			case CHAR:
			case VOID:
				{
				setState(903);
				primitiveType();
				}
				break;
			case ANY:
				{
				setState(904);
				match(ANY);
				}
				break;
			case NEVER:
				{
				setState(905);
				match(NEVER);
				}
				break;
			case LPAREN:
				{
				setState(906);
				match(LPAREN);
				setState(915);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3659176308060228L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217745L) != 0)) {
					{
					setState(907);
					typeType(0);
					setState(912);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(908);
						match(COMMA);
						setState(909);
						typeType(0);
						}
						}
						setState(914);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(917);
				match(RPAREN);
				setState(918);
				match(ARROW);
				setState(919);
				typeType(2);
				}
				break;
			case LBRACK:
				{
				setState(920);
				match(LBRACK);
				setState(921);
				typeType(0);
				setState(922);
				match(COMMA);
				setState(923);
				typeType(0);
				setState(924);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(946);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(944);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
					case 1:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(928);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(931); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(929);
								match(BITOR);
								setState(930);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(933); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(935);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(938); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(936);
								match(BITAND);
								setState(937);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(940); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,97,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(942);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(943);
						arrayKind();
						}
						break;
					}
					} 
				}
				setState(948);
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
		enterRule(_localctx, 148, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(949);
			_la = _input.LA(1);
			if ( !(((((_la - 112)) & ~0x3f) == 0 && ((1L << (_la - 112)) & 15L) != 0)) ) {
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
		enterRule(_localctx, 150, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(951);
			qualifiedName();
			setState(953);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				{
				setState(952);
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
		enterRule(_localctx, 152, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(955);
			match(LT);
			setState(956);
			typeType(0);
			setState(961);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(957);
				match(COMMA);
				setState(958);
				typeType(0);
				}
				}
				setState(963);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(964);
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
		public TerminalNode INT() { return getToken(AssemblyParser.INT, 0); }
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
		enterRule(_localctx, 154, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(966);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 281476587532356L) != 0)) ) {
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
		enterRule(_localctx, 156, RULE_modifier);
		try {
			setState(975);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
				enterOuterAlt(_localctx, 1);
				{
				setState(968);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(969);
				match(NATIVE);
				}
				break;
			case READONLY:
				enterOuterAlt(_localctx, 3);
				{
				setState(970);
				match(READONLY);
				}
				break;
			case CHILD:
				enterOuterAlt(_localctx, 4);
				{
				setState(971);
				match(CHILD);
				}
				break;
			case TITLE:
				enterOuterAlt(_localctx, 5);
				{
				setState(972);
				match(TITLE);
				}
				break;
			case UNIQUE:
				enterOuterAlt(_localctx, 6);
				{
				setState(973);
				match(UNIQUE);
				}
				break;
			case DELETED:
				enterOuterAlt(_localctx, 7);
				{
				setState(974);
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
		enterRule(_localctx, 158, RULE_classOrInterfaceModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(977);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3161095929858L) != 0)) ) {
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
	public static class LambdaContext extends ParserRuleContext {
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
		public LambdaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambda; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterLambda(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitLambda(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaContext lambda() throws RecognitionException {
		LambdaContext _localctx = new LambdaContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_lambda);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(979);
			lambdaParameters();
			setState(980);
			match(COLON);
			setState(981);
			typeTypeOrVoid();
			setState(982);
			match(ARROW);
			setState(983);
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
		enterRule(_localctx, 162, RULE_lambdaParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(985);
			match(LPAREN);
			setState(987);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3659176308060228L) != 0) || ((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 134217745L) != 0)) {
				{
				setState(986);
				formalParameterList();
				}
			}

			setState(989);
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
		enterRule(_localctx, 164, RULE_lambdaBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(991);
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
		enterRule(_localctx, 166, RULE_indexDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(993);
			match(INDEX);
			setState(994);
			match(IDENTIFIER);
			setState(995);
			match(LBRACE);
			setState(999);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(996);
				indexField();
				}
				}
				setState(1001);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1002);
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
		enterRule(_localctx, 168, RULE_indexField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1004);
			match(IDENTIFIER);
			setState(1005);
			match(COLON);
			setState(1006);
			expression(0);
			setState(1007);
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
		enterRule(_localctx, 170, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1009);
			match(AT);
			setState(1010);
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
		case 73:
			return typeType_sempred((TypeTypeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 14);
		case 1:
			return precpred(_ctx, 13);
		case 2:
			return precpred(_ctx, 12);
		case 3:
			return precpred(_ctx, 11);
		case 4:
			return precpred(_ctx, 9);
		case 5:
			return precpred(_ctx, 8);
		case 6:
			return precpred(_ctx, 7);
		case 7:
			return precpred(_ctx, 6);
		case 8:
			return precpred(_ctx, 5);
		case 9:
			return precpred(_ctx, 4);
		case 10:
			return precpred(_ctx, 3);
		case 11:
			return precpred(_ctx, 19);
		case 12:
			return precpred(_ctx, 18);
		case 13:
			return precpred(_ctx, 17);
		case 14:
			return precpred(_ctx, 10);
		case 15:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean typeType_sempred(TypeTypeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 16:
			return precpred(_ctx, 5);
		case 17:
			return precpred(_ctx, 4);
		case 18:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001w\u03f5\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"U\u0007U\u0001\u0000\u0003\u0000\u00ae\b\u0000\u0001\u0000\u0005\u0000"+
		"\u00b1\b\u0000\n\u0000\f\u0000\u00b4\t\u0000\u0001\u0000\u0004\u0000\u00b7"+
		"\b\u0000\u000b\u0000\f\u0000\u00b8\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0005\u0003\u00c4\b\u0003\n\u0003\f\u0003\u00c7\t\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003\u00cc\b\u0003\u0001\u0003\u0003\u0003\u00cf"+
		"\b\u0003\u0001\u0004\u0005\u0004\u00d2\b\u0004\n\u0004\f\u0004\u00d5\t"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00da\b\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004\u00de\b\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004\u00e2\b\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0005"+
		"\u0005\u00e8\b\u0005\n\u0005\f\u0005\u00eb\t\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u00f2\b\u0006\n\u0006"+
		"\f\u0006\u00f5\t\u0006\u0001\u0007\u0005\u0007\u00f8\b\u0007\n\u0007\f"+
		"\u0007\u00fb\t\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00ff\b\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\t\u0005\t\u0105\b\t\n\t\f\t\u0108\t\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\t\u010e\b\t\u0001\t\u0001\t\u0003\t\u0112"+
		"\b\t\u0001\t\u0003\t\u0115\b\t\u0001\t\u0003\t\u0118\b\t\u0001\t\u0001"+
		"\t\u0001\n\u0001\n\u0001\n\u0005\n\u011f\b\n\n\n\f\n\u0122\t\n\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u0126\b\u000b\u0001\f\u0001\f\u0005\f\u012a\b"+
		"\f\n\f\f\f\u012d\t\f\u0001\r\u0005\r\u0130\b\r\n\r\f\r\u0133\t\r\u0001"+
		"\r\u0001\r\u0001\r\u0003\r\u0138\b\r\u0001\r\u0001\r\u0003\r\u013c\b\r"+
		"\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0005\u000e\u0142\b\u000e\n\u000e"+
		"\f\u000e\u0145\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0005\u000f"+
		"\u014a\b\u000f\n\u000f\f\u000f\u014d\t\u000f\u0001\u000f\u0001\u000f\u0003"+
		"\u000f\u0151\b\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0005\u0011\u0156"+
		"\b\u0011\n\u0011\f\u0011\u0159\t\u0011\u0001\u0011\u0003\u0011\u015c\b"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0167\b\u0013\n"+
		"\u0013\f\u0013\u016a\t\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u016e"+
		"\b\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u0176\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0016\u0003\u0016\u017d\b\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0003\u0017\u0185\b\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u018b\b\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005"+
		"\u0018\u0193\b\u0018\n\u0018\f\u0018\u0196\t\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u019d\b\u0019\n\u0019"+
		"\f\u0019\u01a0\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a"+
		"\u01a5\b\u001a\n\u001a\f\u001a\u01a8\t\u001a\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0003\u001b\u01ad\b\u001b\u0001\u001c\u0001\u001c\u0003\u001c\u01b1"+
		"\b\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005"+
		"\u001d\u01b8\b\u001d\n\u001d\f\u001d\u01bb\t\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u01c2\b\u001e\n\u001e"+
		"\f\u001e\u01c5\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001 \u0001"+
		" \u0003 \u01cc\b \u0001!\u0001!\u0005!\u01d0\b!\n!\f!\u01d3\t!\u0001!"+
		"\u0001!\u0001\"\u0001\"\u0003\"\u01d9\b\"\u0001\"\u0001\"\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0003#\u01ec\b#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0005#\u01f5\b#\n#\f#\u01f8\t#\u0001#\u0001#\u0001#\u0003"+
		"#\u01fd\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003"+
		"#\u0207\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0003#\u0227\b#\u0001$\u0001$\u0001$\u0003$\u022c\b$\u0001$\u0001"+
		"$\u0001%\u0001%\u0001%\u0005%\u0233\b%\n%\f%\u0236\t%\u0001&\u0001&\u0001"+
		"&\u0001&\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0005"+
		"\'\u0243\b\'\n\'\f\'\u0246\t\'\u0001\'\u0001\'\u0001(\u0003(\u024b\b("+
		"\u0001(\u0001(\u0003(\u024f\b(\u0001(\u0001(\u0003(\u0253\b(\u0001)\u0001"+
		")\u0001)\u0005)\u0258\b)\n)\f)\u025b\t)\u0001*\u0001*\u0001*\u0001*\u0001"+
		"*\u0001+\u0001+\u0001+\u0005+\u0265\b+\n+\f+\u0268\t+\u0001,\u0001,\u0001"+
		",\u0001,\u0001-\u0001-\u0001-\u0001-\u0001.\u0003.\u0273\b.\u0001.\u0001"+
		".\u0001.\u0001.\u0001.\u0001.\u0003.\u027b\b.\u0001/\u0001/\u0001/\u0001"+
		"/\u0005/\u0281\b/\n/\f/\u0284\t/\u0001/\u0001/\u00010\u00010\u00010\u0001"+
		"0\u00050\u028c\b0\n0\f0\u028f\t0\u00010\u00030\u0292\b0\u00030\u0294\b"+
		"0\u00010\u00010\u00011\u00011\u00031\u029a\b1\u00012\u00012\u00032\u029e"+
		"\b2\u00012\u00032\u02a1\b2\u00013\u00013\u00014\u00014\u00014\u00034\u02a8"+
		"\b4\u00014\u00014\u00015\u00015\u00015\u00055\u02af\b5\n5\f5\u02b2\t5"+
		"\u00016\u00016\u00016\u00016\u00016\u00016\u00056\u02ba\b6\n6\f6\u02bd"+
		"\t6\u00016\u00016\u00016\u00016\u00016\u00017\u00017\u00017\u00017\u0001"+
		"8\u00018\u00018\u00019\u00019\u00019\u00019\u00019\u00019\u00039\u02d1"+
		"\b9\u0001:\u0001:\u0001:\u0001:\u0001;\u0001;\u0001;\u0005;\u02da\b;\n"+
		";\f;\u02dd\t;\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<"+
		"\u0003<\u02e7\b<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<\u02f7\b<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0001<\u0001<\u0003<\u0325\b<\u0001<\u0005<\u0328\b<\n<\f<\u032b\t<"+
		"\u0001=\u0001=\u0001=\u0001=\u0001=\u0001=\u0001=\u0003=\u0334\b=\u0001"+
		">\u0001>\u0001>\u0001?\u0001?\u0001?\u0001?\u0003?\u033d\b?\u0001@\u0001"+
		"@\u0001@\u0003@\u0342\b@\u0001@\u0001@\u0003@\u0346\b@\u0003@\u0348\b"+
		"@\u0001A\u0001A\u0003A\u034c\bA\u0001A\u0001A\u0001B\u0001B\u0003B\u0352"+
		"\bB\u0001C\u0001C\u0001C\u0003C\u0357\bC\u0001C\u0001C\u0001C\u0003C\u035c"+
		"\bC\u0001C\u0001C\u0001C\u0001C\u0001C\u0003C\u0363\bC\u0001C\u0001C\u0001"+
		"C\u0001C\u0003C\u0369\bC\u0001C\u0003C\u036c\bC\u0001D\u0001D\u0001D\u0003"+
		"D\u0371\bD\u0001D\u0001D\u0001E\u0001E\u0001E\u0001E\u0001E\u0001E\u0001"+
		"E\u0003E\u037c\bE\u0001F\u0001F\u0001G\u0001G\u0001H\u0001H\u0003H\u0384"+
		"\bH\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0005"+
		"I\u038f\bI\nI\fI\u0392\tI\u0003I\u0394\bI\u0001I\u0001I\u0001I\u0001I"+
		"\u0001I\u0001I\u0001I\u0001I\u0001I\u0003I\u039f\bI\u0001I\u0001I\u0001"+
		"I\u0004I\u03a4\bI\u000bI\fI\u03a5\u0001I\u0001I\u0001I\u0004I\u03ab\b"+
		"I\u000bI\fI\u03ac\u0001I\u0001I\u0005I\u03b1\bI\nI\fI\u03b4\tI\u0001J"+
		"\u0001J\u0001K\u0001K\u0003K\u03ba\bK\u0001L\u0001L\u0001L\u0001L\u0005"+
		"L\u03c0\bL\nL\fL\u03c3\tL\u0001L\u0001L\u0001M\u0001M\u0001N\u0001N\u0001"+
		"N\u0001N\u0001N\u0001N\u0001N\u0003N\u03d0\bN\u0001O\u0001O\u0001P\u0001"+
		"P\u0001P\u0001P\u0001P\u0001P\u0001Q\u0001Q\u0003Q\u03dc\bQ\u0001Q\u0001"+
		"Q\u0001R\u0001R\u0001S\u0001S\u0001S\u0001S\u0005S\u03e6\bS\nS\fS\u03e9"+
		"\tS\u0001S\u0001S\u0001T\u0001T\u0001T\u0001T\u0001T\u0001U\u0001U\u0001"+
		"U\u0001U\u0000\u0002x\u0092V\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPR"+
		"TVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e"+
		"\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6"+
		"\u00a8\u00aa\u0000\u0012\u0002\u0000\u0007\b\u000b\u000b\u0004\u0000\u0001"+
		"\u0001\u0004\u0004\'\'))\u0001\u0000!#\u0002\u0000,,tt\u0002\u000077N"+
		"X\u0001\u000045\u0001\u0000DG\u0001\u0000:;\u0002\u0000HIMM\u0001\u0000"+
		"FG\u0002\u000089?@\u0002\u0000>>AA\u0001\u0000DE\u0001\u0000fi\u0001\u0000"+
		"jk\u0001\u0000ps\u0006\u0000\u0002\u0002\u0006\u0006\f\r\u0010\u0011\u001d"+
		"\u001e00\u0003\u0000\u0001\u0001%\'))\u043b\u0000\u00ad\u0001\u0000\u0000"+
		"\u0000\u0002\u00ba\u0001\u0000\u0000\u0000\u0004\u00be\u0001\u0000\u0000"+
		"\u0000\u0006\u00ce\u0001\u0000\u0000\u0000\b\u00d3\u0001\u0000\u0000\u0000"+
		"\n\u00e5\u0001\u0000\u0000\u0000\f\u00ee\u0001\u0000\u0000\u0000\u000e"+
		"\u00fe\u0001\u0000\u0000\u0000\u0010\u0100\u0001\u0000\u0000\u0000\u0012"+
		"\u0106\u0001\u0000\u0000\u0000\u0014\u011b\u0001\u0000\u0000\u0000\u0016"+
		"\u0123\u0001\u0000\u0000\u0000\u0018\u0127\u0001\u0000\u0000\u0000\u001a"+
		"\u0131\u0001\u0000\u0000\u0000\u001c\u013f\u0001\u0000\u0000\u0000\u001e"+
		"\u0150\u0001\u0000\u0000\u0000 \u0152\u0001\u0000\u0000\u0000\"\u0157"+
		"\u0001\u0000\u0000\u0000$\u015f\u0001\u0000\u0000\u0000&\u0161\u0001\u0000"+
		"\u0000\u0000(\u0175\u0001\u0000\u0000\u0000*\u0177\u0001\u0000\u0000\u0000"+
		",\u017c\u0001\u0000\u0000\u0000.\u0184\u0001\u0000\u0000\u00000\u018e"+
		"\u0001\u0000\u0000\u00002\u0199\u0001\u0000\u0000\u00004\u01a1\u0001\u0000"+
		"\u0000\u00006\u01a9\u0001\u0000\u0000\u00008\u01ae\u0001\u0000\u0000\u0000"+
		":\u01b4\u0001\u0000\u0000\u0000<\u01be\u0001\u0000\u0000\u0000>\u01c6"+
		"\u0001\u0000\u0000\u0000@\u01cb\u0001\u0000\u0000\u0000B\u01cd\u0001\u0000"+
		"\u0000\u0000D\u01d8\u0001\u0000\u0000\u0000F\u0226\u0001\u0000\u0000\u0000"+
		"H\u0228\u0001\u0000\u0000\u0000J\u022f\u0001\u0000\u0000\u0000L\u0237"+
		"\u0001\u0000\u0000\u0000N\u023b\u0001\u0000\u0000\u0000P\u024a\u0001\u0000"+
		"\u0000\u0000R\u0254\u0001\u0000\u0000\u0000T\u025c\u0001\u0000\u0000\u0000"+
		"V\u0261\u0001\u0000\u0000\u0000X\u0269\u0001\u0000\u0000\u0000Z\u026d"+
		"\u0001\u0000\u0000\u0000\\\u027a\u0001\u0000\u0000\u0000^\u027c\u0001"+
		"\u0000\u0000\u0000`\u0287\u0001\u0000\u0000\u0000b\u0299\u0001\u0000\u0000"+
		"\u0000d\u02a0\u0001\u0000\u0000\u0000f\u02a2\u0001\u0000\u0000\u0000h"+
		"\u02a4\u0001\u0000\u0000\u0000j\u02ab\u0001\u0000\u0000\u0000l\u02b3\u0001"+
		"\u0000\u0000\u0000n\u02c3\u0001\u0000\u0000\u0000p\u02c7\u0001\u0000\u0000"+
		"\u0000r\u02d0\u0001\u0000\u0000\u0000t\u02d2\u0001\u0000\u0000\u0000v"+
		"\u02d6\u0001\u0000\u0000\u0000x\u02e6\u0001\u0000\u0000\u0000z\u0333\u0001"+
		"\u0000\u0000\u0000|\u0335\u0001\u0000\u0000\u0000~\u033c\u0001\u0000\u0000"+
		"\u0000\u0080\u0347\u0001\u0000\u0000\u0000\u0082\u0349\u0001\u0000\u0000"+
		"\u0000\u0084\u034f\u0001\u0000\u0000\u0000\u0086\u036b\u0001\u0000\u0000"+
		"\u0000\u0088\u036d\u0001\u0000\u0000\u0000\u008a\u037b\u0001\u0000\u0000"+
		"\u0000\u008c\u037d\u0001\u0000\u0000\u0000\u008e\u037f\u0001\u0000\u0000"+
		"\u0000\u0090\u0383\u0001\u0000\u0000\u0000\u0092\u039e\u0001\u0000\u0000"+
		"\u0000\u0094\u03b5\u0001\u0000\u0000\u0000\u0096\u03b7\u0001\u0000\u0000"+
		"\u0000\u0098\u03bb\u0001\u0000\u0000\u0000\u009a\u03c6\u0001\u0000\u0000"+
		"\u0000\u009c\u03cf\u0001\u0000\u0000\u0000\u009e\u03d1\u0001\u0000\u0000"+
		"\u0000\u00a0\u03d3\u0001\u0000\u0000\u0000\u00a2\u03d9\u0001\u0000\u0000"+
		"\u0000\u00a4\u03df\u0001\u0000\u0000\u0000\u00a6\u03e1\u0001\u0000\u0000"+
		"\u0000\u00a8\u03ec\u0001\u0000\u0000\u0000\u00aa\u03f1\u0001\u0000\u0000"+
		"\u0000\u00ac\u00ae\u0003\u0002\u0001\u0000\u00ad\u00ac\u0001\u0000\u0000"+
		"\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00b2\u0001\u0000\u0000"+
		"\u0000\u00af\u00b1\u0003\u0004\u0002\u0000\u00b0\u00af\u0001\u0000\u0000"+
		"\u0000\u00b1\u00b4\u0001\u0000\u0000\u0000\u00b2\u00b0\u0001\u0000\u0000"+
		"\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000\u00b3\u00b6\u0001\u0000\u0000"+
		"\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b5\u00b7\u0003\u0006\u0003"+
		"\u0000\u00b6\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000"+
		"\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000"+
		"\u0000\u00b9\u0001\u0001\u0000\u0000\u0000\u00ba\u00bb\u0005\u000e\u0000"+
		"\u0000\u00bb\u00bc\u00034\u001a\u0000\u00bc\u00bd\u0005_\u0000\u0000\u00bd"+
		"\u0003\u0001\u0000\u0000\u0000\u00be\u00bf\u0005\u000f\u0000\u0000\u00bf"+
		"\u00c0\u00034\u001a\u0000\u00c0\u00c1\u0005_\u0000\u0000\u00c1\u0005\u0001"+
		"\u0000\u0000\u0000\u00c2\u00c4\u0003\u009eO\u0000\u00c3\u00c2\u0001\u0000"+
		"\u0000\u0000\u00c4\u00c7\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c6\u0001\u0000\u0000\u0000\u00c6\u00cb\u0001\u0000"+
		"\u0000\u0000\u00c7\u00c5\u0001\u0000\u0000\u0000\u00c8\u00cc\u0003\b\u0004"+
		"\u0000\u00c9\u00cc\u0003\u0012\t\u0000\u00ca\u00cc\u0003\u001a\r\u0000"+
		"\u00cb\u00c8\u0001\u0000\u0000\u0000\u00cb\u00c9\u0001\u0000\u0000\u0000"+
		"\u00cb\u00ca\u0001\u0000\u0000\u0000\u00cc\u00cf\u0001\u0000\u0000\u0000"+
		"\u00cd\u00cf\u0005_\u0000\u0000\u00ce\u00c5\u0001\u0000\u0000\u0000\u00ce"+
		"\u00cd\u0001\u0000\u0000\u0000\u00cf\u0007\u0001\u0000\u0000\u0000\u00d0"+
		"\u00d2\u0003\u00aaU\u0000\u00d1\u00d0\u0001\u0000\u0000\u0000\u00d2\u00d5"+
		"\u0001\u0000\u0000\u0000\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d3\u00d4"+
		"\u0001\u0000\u0000\u0000\u00d4\u00d6\u0001\u0000\u0000\u0000\u00d5\u00d3"+
		"\u0001\u0000\u0000\u0000\u00d6\u00d7\u0007\u0000\u0000\u0000\u00d7\u00d9"+
		"\u0005t\u0000\u0000\u00d8\u00da\u00030\u0018\u0000\u00d9\u00d8\u0001\u0000"+
		"\u0000\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u00dd\u0001\u0000"+
		"\u0000\u0000\u00db\u00dc\u0005\u0014\u0000\u0000\u00dc\u00de\u0003\u0092"+
		"I\u0000\u00dd\u00db\u0001\u0000\u0000\u0000\u00dd\u00de\u0001\u0000\u0000"+
		"\u0000\u00de\u00e1\u0001\u0000\u0000\u0000\u00df\u00e0\u0005\u001b\u0000"+
		"\u0000\u00e0\u00e2\u0003\f\u0006\u0000\u00e1\u00df\u0001\u0000\u0000\u0000"+
		"\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000"+
		"\u00e3\u00e4\u0003\n\u0005\u0000\u00e4\t\u0001\u0000\u0000\u0000\u00e5"+
		"\u00e9\u0005[\u0000\u0000\u00e6\u00e8\u0003\u000e\u0007\u0000\u00e7\u00e6"+
		"\u0001\u0000\u0000\u0000\u00e8\u00eb\u0001\u0000\u0000\u0000\u00e9\u00e7"+
		"\u0001\u0000\u0000\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ec"+
		"\u0001\u0000\u0000\u0000\u00eb\u00e9\u0001\u0000\u0000\u0000\u00ec\u00ed"+
		"\u0005\\\u0000\u0000\u00ed\u000b\u0001\u0000\u0000\u0000\u00ee\u00f3\u0003"+
		"\u0092I\u0000\u00ef\u00f0\u0005`\u0000\u0000\u00f0\u00f2\u0003\u0092I"+
		"\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001\u0000\u0000"+
		"\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001\u0000\u0000"+
		"\u0000\u00f4\r\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000"+
		"\u00f6\u00f8\u0003\u009cN\u0000\u00f7\u00f6\u0001\u0000\u0000\u0000\u00f8"+
		"\u00fb\u0001\u0000\u0000\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00f9"+
		"\u00fa\u0001\u0000\u0000\u0000\u00fa\u00fc\u0001\u0000\u0000\u0000\u00fb"+
		"\u00f9\u0001\u0000\u0000\u0000\u00fc\u00ff\u0003(\u0014\u0000\u00fd\u00ff"+
		"\u0003\u0010\b\u0000\u00fe\u00f9\u0001\u0000\u0000\u0000\u00fe\u00fd\u0001"+
		"\u0000\u0000\u0000\u00ff\u000f\u0001\u0000\u0000\u0000\u0100\u0101\u0005"+
		")\u0000\u0000\u0101\u0102\u0003B!\u0000\u0102\u0011\u0001\u0000\u0000"+
		"\u0000\u0103\u0105\u0003\u00aaU\u0000\u0104\u0103\u0001\u0000\u0000\u0000"+
		"\u0105\u0108\u0001\u0000\u0000\u0000\u0106\u0104\u0001\u0000\u0000\u0000"+
		"\u0106\u0107\u0001\u0000\u0000\u0000\u0107\u0109\u0001\u0000\u0000\u0000"+
		"\u0108\u0106\u0001\u0000\u0000\u0000\u0109\u010a\u0005\u0013\u0000\u0000"+
		"\u010a\u010d\u0005t\u0000\u0000\u010b\u010c\u0005\u001b\u0000\u0000\u010c"+
		"\u010e\u0003\f\u0006\u0000\u010d\u010b\u0001\u0000\u0000\u0000\u010d\u010e"+
		"\u0001\u0000\u0000\u0000\u010e\u010f\u0001\u0000\u0000\u0000\u010f\u0111"+
		"\u0005[\u0000\u0000\u0110\u0112\u0003\u0014\n\u0000\u0111\u0110\u0001"+
		"\u0000\u0000\u0000\u0111\u0112\u0001\u0000\u0000\u0000\u0112\u0114\u0001"+
		"\u0000\u0000\u0000\u0113\u0115\u0005`\u0000\u0000\u0114\u0113\u0001\u0000"+
		"\u0000\u0000\u0114\u0115\u0001\u0000\u0000\u0000\u0115\u0117\u0001\u0000"+
		"\u0000\u0000\u0116\u0118\u0003\u0018\f\u0000\u0117\u0116\u0001\u0000\u0000"+
		"\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000\u0000"+
		"\u0000\u0119\u011a\u0005\\\u0000\u0000\u011a\u0013\u0001\u0000\u0000\u0000"+
		"\u011b\u0120\u0003\u0016\u000b\u0000\u011c\u011d\u0005`\u0000\u0000\u011d"+
		"\u011f\u0003\u0016\u000b\u0000\u011e\u011c\u0001\u0000\u0000\u0000\u011f"+
		"\u0122\u0001\u0000\u0000\u0000\u0120\u011e\u0001\u0000\u0000\u0000\u0120"+
		"\u0121\u0001\u0000\u0000\u0000\u0121\u0015\u0001\u0000\u0000\u0000\u0122"+
		"\u0120\u0001\u0000\u0000\u0000\u0123\u0125\u0005t\u0000\u0000\u0124\u0126"+
		"\u0003\u0082A\u0000\u0125\u0124\u0001\u0000\u0000\u0000\u0125\u0126\u0001"+
		"\u0000\u0000\u0000\u0126\u0017\u0001\u0000\u0000\u0000\u0127\u012b\u0005"+
		"_\u0000\u0000\u0128\u012a\u0003\u000e\u0007\u0000\u0129\u0128\u0001\u0000"+
		"\u0000\u0000\u012a\u012d\u0001\u0000\u0000\u0000\u012b\u0129\u0001\u0000"+
		"\u0000\u0000\u012b\u012c\u0001\u0000\u0000\u0000\u012c\u0019\u0001\u0000"+
		"\u0000\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012e\u0130\u0003\u00aa"+
		"U\u0000\u012f\u012e\u0001\u0000\u0000\u0000\u0130\u0133\u0001\u0000\u0000"+
		"\u0000\u0131\u012f\u0001\u0000\u0000\u0000\u0131\u0132\u0001\u0000\u0000"+
		"\u0000\u0132\u0134\u0001\u0000\u0000\u0000\u0133\u0131\u0001\u0000\u0000"+
		"\u0000\u0134\u0135\u0005\u001f\u0000\u0000\u0135\u0137\u0005t\u0000\u0000"+
		"\u0136\u0138\u00030\u0018\u0000\u0137\u0136\u0001\u0000\u0000\u0000\u0137"+
		"\u0138\u0001\u0000\u0000\u0000\u0138\u013b\u0001\u0000\u0000\u0000\u0139"+
		"\u013a\u0005\u0014\u0000\u0000\u013a\u013c\u0003\f\u0006\u0000\u013b\u0139"+
		"\u0001\u0000\u0000\u0000\u013b\u013c\u0001\u0000\u0000\u0000\u013c\u013d"+
		"\u0001\u0000\u0000\u0000\u013d\u013e\u0003\u001c\u000e\u0000\u013e\u001b"+
		"\u0001\u0000\u0000\u0000\u013f\u0143\u0005[\u0000\u0000\u0140\u0142\u0003"+
		"\u001e\u000f\u0000\u0141\u0140\u0001\u0000\u0000\u0000\u0142\u0145\u0001"+
		"\u0000\u0000\u0000\u0143\u0141\u0001\u0000\u0000\u0000\u0143\u0144\u0001"+
		"\u0000\u0000\u0000\u0144\u0146\u0001\u0000\u0000\u0000\u0145\u0143\u0001"+
		"\u0000\u0000\u0000\u0146\u0147\u0005\\\u0000\u0000\u0147\u001d\u0001\u0000"+
		"\u0000\u0000\u0148\u014a\u0003\u009cN\u0000\u0149\u0148\u0001\u0000\u0000"+
		"\u0000\u014a\u014d\u0001\u0000\u0000\u0000\u014b\u0149\u0001\u0000\u0000"+
		"\u0000\u014b\u014c\u0001\u0000\u0000\u0000\u014c\u014e\u0001\u0000\u0000"+
		"\u0000\u014d\u014b\u0001\u0000\u0000\u0000\u014e\u0151\u0003 \u0010\u0000"+
		"\u014f\u0151\u0005_\u0000\u0000\u0150\u014b\u0001\u0000\u0000\u0000\u0150"+
		"\u014f\u0001\u0000\u0000\u0000\u0151\u001f\u0001\u0000\u0000\u0000\u0152"+
		"\u0153\u0003\"\u0011\u0000\u0153!\u0001\u0000\u0000\u0000\u0154\u0156"+
		"\u0003$\u0012\u0000\u0155\u0154\u0001\u0000\u0000\u0000\u0156\u0159\u0001"+
		"\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000\u0000\u0157\u0158\u0001"+
		"\u0000\u0000\u0000\u0158\u015b\u0001\u0000\u0000\u0000\u0159\u0157\u0001"+
		"\u0000\u0000\u0000\u015a\u015c\u00030\u0018\u0000\u015b\u015a\u0001\u0000"+
		"\u0000\u0000\u015b\u015c\u0001\u0000\u0000\u0000\u015c\u015d\u0001\u0000"+
		"\u0000\u0000\u015d\u015e\u0003&\u0013\u0000\u015e#\u0001\u0000\u0000\u0000"+
		"\u015f\u0160\u0007\u0001\u0000\u0000\u0160%\u0001\u0000\u0000\u0000\u0161"+
		"\u0162\u0003\u0090H\u0000\u0162\u0163\u0005t\u0000\u0000\u0163\u0168\u0003"+
		"8\u001c\u0000\u0164\u0165\u0005]\u0000\u0000\u0165\u0167\u0005^\u0000"+
		"\u0000\u0166\u0164\u0001\u0000\u0000\u0000\u0167\u016a\u0001\u0000\u0000"+
		"\u0000\u0168\u0166\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000"+
		"\u0000\u0169\u016d\u0001\u0000\u0000\u0000\u016a\u0168\u0001\u0000\u0000"+
		"\u0000\u016b\u016c\u0005.\u0000\u0000\u016c\u016e\u00032\u0019\u0000\u016d"+
		"\u016b\u0001\u0000\u0000\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016e"+
		"\u016f\u0001\u0000\u0000\u0000\u016f\u0170\u0005_\u0000\u0000\u0170\'"+
		"\u0001\u0000\u0000\u0000\u0171\u0176\u0003,\u0016\u0000\u0172\u0176\u0003"+
		"*\u0015\u0000\u0173\u0176\u0003.\u0017\u0000\u0174\u0176\u0003\u00a6S"+
		"\u0000\u0175\u0171\u0001\u0000\u0000\u0000\u0175\u0172\u0001\u0000\u0000"+
		"\u0000\u0175\u0173\u0001\u0000\u0000\u0000\u0175\u0174\u0001\u0000\u0000"+
		"\u0000\u0176)\u0001\u0000\u0000\u0000\u0177\u0178\u0003\u0092I\u0000\u0178"+
		"\u0179\u0005t\u0000\u0000\u0179\u017a\u0005_\u0000\u0000\u017a+\u0001"+
		"\u0000\u0000\u0000\u017b\u017d\u00030\u0018\u0000\u017c\u017b\u0001\u0000"+
		"\u0000\u0000\u017c\u017d\u0001\u0000\u0000\u0000\u017d\u017e\u0001\u0000"+
		"\u0000\u0000\u017e\u017f\u0003\u0090H\u0000\u017f\u0180\u0005t\u0000\u0000"+
		"\u0180\u0181\u00038\u001c\u0000\u0181\u0182\u0003@ \u0000\u0182-\u0001"+
		"\u0000\u0000\u0000\u0183\u0185\u00030\u0018\u0000\u0184\u0183\u0001\u0000"+
		"\u0000\u0000\u0184\u0185\u0001\u0000\u0000\u0000\u0185\u0186\u0001\u0000"+
		"\u0000\u0000\u0186\u0187\u0005t\u0000\u0000\u0187\u018a\u00038\u001c\u0000"+
		"\u0188\u0189\u0005.\u0000\u0000\u0189\u018b\u00032\u0019\u0000\u018a\u0188"+
		"\u0001\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000\u018b\u018c"+
		"\u0001\u0000\u0000\u0000\u018c\u018d\u0003B!\u0000\u018d/\u0001\u0000"+
		"\u0000\u0000\u018e\u018f\u00059\u0000\u0000\u018f\u0194\u00036\u001b\u0000"+
		"\u0190\u0191\u0005`\u0000\u0000\u0191\u0193\u00036\u001b\u0000\u0192\u0190"+
		"\u0001\u0000\u0000\u0000\u0193\u0196\u0001\u0000\u0000\u0000\u0194\u0192"+
		"\u0001\u0000\u0000\u0000\u0194\u0195\u0001\u0000\u0000\u0000\u0195\u0197"+
		"\u0001\u0000\u0000\u0000\u0196\u0194\u0001\u0000\u0000\u0000\u0197\u0198"+
		"\u00058\u0000\u0000\u01981\u0001\u0000\u0000\u0000\u0199\u019e\u00034"+
		"\u001a\u0000\u019a\u019b\u0005`\u0000\u0000\u019b\u019d\u00034\u001a\u0000"+
		"\u019c\u019a\u0001\u0000\u0000\u0000\u019d\u01a0\u0001\u0000\u0000\u0000"+
		"\u019e\u019c\u0001\u0000\u0000\u0000\u019e\u019f\u0001\u0000\u0000\u0000"+
		"\u019f3\u0001\u0000\u0000\u0000\u01a0\u019e\u0001\u0000\u0000\u0000\u01a1"+
		"\u01a6\u0005t\u0000\u0000\u01a2\u01a3\u0005a\u0000\u0000\u01a3\u01a5\u0005"+
		"t\u0000\u0000\u01a4\u01a2\u0001\u0000\u0000\u0000\u01a5\u01a8\u0001\u0000"+
		"\u0000\u0000\u01a6\u01a4\u0001\u0000\u0000\u0000\u01a6\u01a7\u0001\u0000"+
		"\u0000\u0000\u01a75\u0001\u0000\u0000\u0000\u01a8\u01a6\u0001\u0000\u0000"+
		"\u0000\u01a9\u01ac\u0005t\u0000\u0000\u01aa\u01ab\u0005\u0014\u0000\u0000"+
		"\u01ab\u01ad\u0003\u0092I\u0000\u01ac\u01aa\u0001\u0000\u0000\u0000\u01ac"+
		"\u01ad\u0001\u0000\u0000\u0000\u01ad7\u0001\u0000\u0000\u0000\u01ae\u01b0"+
		"\u0005Y\u0000\u0000\u01af\u01b1\u0003<\u001e\u0000\u01b0\u01af\u0001\u0000"+
		"\u0000\u0000\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1\u01b2\u0001\u0000"+
		"\u0000\u0000\u01b2\u01b3\u0005Z\u0000\u0000\u01b39\u0001\u0000\u0000\u0000"+
		"\u01b4\u01b9\u0003\u0092I\u0000\u01b5\u01b6\u0005t\u0000\u0000\u01b6\u01b8"+
		"\u0005a\u0000\u0000\u01b7\u01b5\u0001\u0000\u0000\u0000\u01b8\u01bb\u0001"+
		"\u0000\u0000\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001"+
		"\u0000\u0000\u0000\u01ba\u01bc\u0001\u0000\u0000\u0000\u01bb\u01b9\u0001"+
		"\u0000\u0000\u0000\u01bc\u01bd\u0005,\u0000\u0000\u01bd;\u0001\u0000\u0000"+
		"\u0000\u01be\u01c3\u0003>\u001f\u0000\u01bf\u01c0\u0005`\u0000\u0000\u01c0"+
		"\u01c2\u0003>\u001f\u0000\u01c1\u01bf\u0001\u0000\u0000\u0000\u01c2\u01c5"+
		"\u0001\u0000\u0000\u0000\u01c3\u01c1\u0001\u0000\u0000\u0000\u01c3\u01c4"+
		"\u0001\u0000\u0000\u0000\u01c4=\u0001\u0000\u0000\u0000\u01c5\u01c3\u0001"+
		"\u0000\u0000\u0000\u01c6\u01c7\u0003\u0092I\u0000\u01c7\u01c8\u0005t\u0000"+
		"\u0000\u01c8?\u0001\u0000\u0000\u0000\u01c9\u01cc\u0003B!\u0000\u01ca"+
		"\u01cc\u0005_\u0000\u0000\u01cb\u01c9\u0001\u0000\u0000\u0000\u01cb\u01ca"+
		"\u0001\u0000\u0000\u0000\u01ccA\u0001\u0000\u0000\u0000\u01cd\u01d1\u0005"+
		"[\u0000\u0000\u01ce\u01d0\u0003D\"\u0000\u01cf\u01ce\u0001\u0000\u0000"+
		"\u0000\u01d0\u01d3\u0001\u0000\u0000\u0000\u01d1\u01cf\u0001\u0000\u0000"+
		"\u0000\u01d1\u01d2\u0001\u0000\u0000\u0000\u01d2\u01d4\u0001\u0000\u0000"+
		"\u0000\u01d3\u01d1\u0001\u0000\u0000\u0000\u01d4\u01d5\u0005\\\u0000\u0000"+
		"\u01d5C\u0001\u0000\u0000\u0000\u01d6\u01d7\u0005t\u0000\u0000\u01d7\u01d9"+
		"\u0005=\u0000\u0000\u01d8\u01d6\u0001\u0000\u0000\u0000\u01d8\u01d9\u0001"+
		"\u0000\u0000\u0000\u01d9\u01da\u0001\u0000\u0000\u0000\u01da\u01db\u0003"+
		"F#\u0000\u01dbE\u0001\u0000\u0000\u0000\u01dc\u01dd\u00051\u0000\u0000"+
		"\u01dd\u01de\u0003t:\u0000\u01de\u01df\u0003B!\u0000\u01df\u0227\u0001"+
		"\u0000\u0000\u0000\u01e0\u01e1\u0005\u0019\u0000\u0000\u01e1\u01e2\u0005"+
		"Y\u0000\u0000\u01e2\u01e3\u0003P(\u0000\u01e3\u01e4\u0005Z\u0000\u0000"+
		"\u01e4\u01e5\u0003B!\u0000\u01e5\u0227\u0001\u0000\u0000\u0000\u01e6\u01e7"+
		"\u0005\u001a\u0000\u0000\u01e7\u01e8\u0003t:\u0000\u01e8\u01eb\u0003B"+
		"!\u0000\u01e9\u01ea\u0005\u0012\u0000\u0000\u01ea\u01ec\u0003B!\u0000"+
		"\u01eb\u01e9\u0001\u0000\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000"+
		"\u01ec\u0227\u0001\u0000\u0000\u0000\u01ed\u01ee\u0005/\u0000\u0000\u01ee"+
		"\u01ef\u0003B!\u0000\u01ef\u01f0\u0003h4\u0000\u01f0\u0227\u0001\u0000"+
		"\u0000\u0000\u01f1\u01f2\u0005+\u0000\u0000\u01f2\u01f6\u0005[\u0000\u0000"+
		"\u01f3\u01f5\u0003p8\u0000\u01f4\u01f3\u0001\u0000\u0000\u0000\u01f5\u01f8"+
		"\u0001\u0000\u0000\u0000\u01f6\u01f4\u0001\u0000\u0000\u0000\u01f6\u01f7"+
		"\u0001\u0000\u0000\u0000\u01f7\u01f9\u0001\u0000\u0000\u0000\u01f8\u01f6"+
		"\u0001\u0000\u0000\u0000\u01f9\u0227\u0005\\\u0000\u0000\u01fa\u01fc\u0005"+
		"(\u0000\u0000\u01fb\u01fd\u0003x<\u0000\u01fc\u01fb\u0001\u0000\u0000"+
		"\u0000\u01fc\u01fd\u0001\u0000\u0000\u0000\u01fd\u01fe\u0001\u0000\u0000"+
		"\u0000\u01fe\u0227\u0005_\u0000\u0000\u01ff\u0200\u0005-\u0000\u0000\u0200"+
		"\u0201\u0003x<\u0000\u0201\u0202\u0005_\u0000\u0000\u0202\u0227\u0001"+
		"\u0000\u0000\u0000\u0203\u0227\u0005_\u0000\u0000\u0204\u0206\u0003\u0086"+
		"C\u0000\u0205\u0207\u0005:\u0000\u0000\u0206\u0205\u0001\u0000\u0000\u0000"+
		"\u0206\u0207\u0001\u0000\u0000\u0000\u0207\u0208\u0001\u0000\u0000\u0000"+
		"\u0208\u0209\u0005_\u0000\u0000\u0209\u0227\u0001\u0000\u0000\u0000\u020a"+
		"\u020b\u0003\u0088D\u0000\u020b\u020c\u0005_\u0000\u0000\u020c\u0227\u0001"+
		"\u0000\u0000\u0000\u020d\u020e\u0007\u0002\u0000\u0000\u020e\u020f\u0003"+
		"\\.\u0000\u020f\u0210\u0005_\u0000\u0000\u0210\u0227\u0001\u0000\u0000"+
		"\u0000\u0211\u0212\u0005$\u0000\u0000\u0212\u0213\u0003H$\u0000\u0213"+
		"\u0214\u0005_\u0000\u0000\u0214\u0227\u0001\u0000\u0000\u0000\u0215\u0216"+
		"\u0003N\'\u0000\u0216\u0217\u0005_\u0000\u0000\u0217\u0227\u0001\u0000"+
		"\u0000\u0000\u0218\u0219\u0005Y\u0000\u0000\u0219\u021a\u0003\u0092I\u0000"+
		"\u021a\u021b\u0005Z\u0000\u0000\u021b\u021c\u0003x<\u0000\u021c\u021d"+
		"\u0005_\u0000\u0000\u021d\u0227\u0001\u0000\u0000\u0000\u021e\u021f\u0007"+
		"\u0003\u0000\u0000\u021f\u0220\u0005a\u0000\u0000\u0220\u0221\u0005t\u0000"+
		"\u0000\u0221\u0222\u0007\u0004\u0000\u0000\u0222\u0223\u0003x<\u0000\u0223"+
		"\u0224\u0005_\u0000\u0000\u0224\u0227\u0001\u0000\u0000\u0000\u0225\u0227"+
		"\u0003\u00a0P\u0000\u0226\u01dc\u0001\u0000\u0000\u0000\u0226\u01e0\u0001"+
		"\u0000\u0000\u0000\u0226\u01e6\u0001\u0000\u0000\u0000\u0226\u01ed\u0001"+
		"\u0000\u0000\u0000\u0226\u01f1\u0001\u0000\u0000\u0000\u0226\u01fa\u0001"+
		"\u0000\u0000\u0000\u0226\u01ff\u0001\u0000\u0000\u0000\u0226\u0203\u0001"+
		"\u0000\u0000\u0000\u0226\u0204\u0001\u0000\u0000\u0000\u0226\u020a\u0001"+
		"\u0000\u0000\u0000\u0226\u020d\u0001\u0000\u0000\u0000\u0226\u0211\u0001"+
		"\u0000\u0000\u0000\u0226\u0215\u0001\u0000\u0000\u0000\u0226\u0218\u0001"+
		"\u0000\u0000\u0000\u0226\u021e\u0001\u0000\u0000\u0000\u0226\u0225\u0001"+
		"\u0000\u0000\u0000\u0227G\u0001\u0000\u0000\u0000\u0228\u0229\u0003\u0096"+
		"K\u0000\u0229\u022b\u0005[\u0000\u0000\u022a\u022c\u0003J%\u0000\u022b"+
		"\u022a\u0001\u0000\u0000\u0000\u022b\u022c\u0001\u0000\u0000\u0000\u022c"+
		"\u022d\u0001\u0000\u0000\u0000\u022d\u022e\u0005\\\u0000\u0000\u022eI"+
		"\u0001\u0000\u0000\u0000\u022f\u0234\u0003L&\u0000\u0230\u0231\u0005`"+
		"\u0000\u0000\u0231\u0233\u0003L&\u0000\u0232\u0230\u0001\u0000\u0000\u0000"+
		"\u0233\u0236\u0001\u0000\u0000\u0000\u0234\u0232\u0001\u0000\u0000\u0000"+
		"\u0234\u0235\u0001\u0000\u0000\u0000\u0235K\u0001\u0000\u0000\u0000\u0236"+
		"\u0234\u0001\u0000\u0000\u0000\u0237\u0238\u0005t\u0000\u0000\u0238\u0239"+
		"\u0005=\u0000\u0000\u0239\u023a\u0003x<\u0000\u023aM\u0001\u0000\u0000"+
		"\u0000\u023b\u023c\u0007\u0005\u0000\u0000\u023c\u023d\u0005Y\u0000\u0000"+
		"\u023d\u023e\u00034\u001a\u0000\u023e\u023f\u0005a\u0000\u0000\u023f\u0244"+
		"\u0005t\u0000\u0000\u0240\u0241\u0005`\u0000\u0000\u0241\u0243\u0003x"+
		"<\u0000\u0242\u0240\u0001\u0000\u0000\u0000\u0243\u0246\u0001\u0000\u0000"+
		"\u0000\u0244\u0242\u0001\u0000\u0000\u0000\u0244\u0245\u0001\u0000\u0000"+
		"\u0000\u0245\u0247\u0001\u0000\u0000\u0000\u0246\u0244\u0001\u0000\u0000"+
		"\u0000\u0247\u0248\u0005Z\u0000\u0000\u0248O\u0001\u0000\u0000\u0000\u0249"+
		"\u024b\u0003R)\u0000\u024a\u0249\u0001\u0000\u0000\u0000\u024a\u024b\u0001"+
		"\u0000\u0000\u0000\u024b\u024c\u0001\u0000\u0000\u0000\u024c\u024e\u0005"+
		"_\u0000\u0000\u024d\u024f\u0003x<\u0000\u024e\u024d\u0001\u0000\u0000"+
		"\u0000\u024e\u024f\u0001\u0000\u0000\u0000\u024f\u0250\u0001\u0000\u0000"+
		"\u0000\u0250\u0252\u0005_\u0000\u0000\u0251\u0253\u0003V+\u0000\u0252"+
		"\u0251\u0001\u0000\u0000\u0000\u0252\u0253\u0001\u0000\u0000\u0000\u0253"+
		"Q\u0001\u0000\u0000\u0000\u0254\u0259\u0003T*\u0000\u0255\u0256\u0005"+
		"`\u0000\u0000\u0256\u0258\u0003T*\u0000\u0257\u0255\u0001\u0000\u0000"+
		"\u0000\u0258\u025b\u0001\u0000\u0000\u0000\u0259\u0257\u0001\u0000\u0000"+
		"\u0000\u0259\u025a\u0001\u0000\u0000\u0000\u025aS\u0001\u0000\u0000\u0000"+
		"\u025b\u0259\u0001\u0000\u0000\u0000\u025c\u025d\u0003\u0092I\u0000\u025d"+
		"\u025e\u0005t\u0000\u0000\u025e\u025f\u00057\u0000\u0000\u025f\u0260\u0003"+
		"x<\u0000\u0260U\u0001\u0000\u0000\u0000\u0261\u0266\u0003X,\u0000\u0262"+
		"\u0263\u0005`\u0000\u0000\u0263\u0265\u0003X,\u0000\u0264\u0262\u0001"+
		"\u0000\u0000\u0000\u0265\u0268\u0001\u0000\u0000\u0000\u0266\u0264\u0001"+
		"\u0000\u0000\u0000\u0266\u0267\u0001\u0000\u0000\u0000\u0267W\u0001\u0000"+
		"\u0000\u0000\u0268\u0266\u0001\u0000\u0000\u0000\u0269\u026a\u0005t\u0000"+
		"\u0000\u026a\u026b\u00057\u0000\u0000\u026b\u026c\u0003x<\u0000\u026c"+
		"Y\u0001\u0000\u0000\u0000\u026d\u026e\u00034\u001a\u0000\u026e\u026f\u0005"+
		"a\u0000\u0000\u026f\u0270\u0005t\u0000\u0000\u0270[\u0001\u0000\u0000"+
		"\u0000\u0271\u0273\u0003\u0098L\u0000\u0272\u0271\u0001\u0000\u0000\u0000"+
		"\u0272\u0273\u0001\u0000\u0000\u0000\u0273\u0274\u0001\u0000\u0000\u0000"+
		"\u0274\u0275\u0003\u0096K\u0000\u0275\u0276\u0003\u0082A\u0000\u0276\u027b"+
		"\u0001\u0000\u0000\u0000\u0277\u0278\u0003\u0092I\u0000\u0278\u0279\u0003"+
		"\u0094J\u0000\u0279\u027b\u0001\u0000\u0000\u0000\u027a\u0272\u0001\u0000"+
		"\u0000\u0000\u027a\u0277\u0001\u0000\u0000\u0000\u027b]\u0001\u0000\u0000"+
		"\u0000\u027c\u027d\u0005]\u0000\u0000\u027d\u0282\u0005^\u0000\u0000\u027e"+
		"\u027f\u0005]\u0000\u0000\u027f\u0281\u0005^\u0000\u0000\u0280\u027e\u0001"+
		"\u0000\u0000\u0000\u0281\u0284\u0001\u0000\u0000\u0000\u0282\u0280\u0001"+
		"\u0000\u0000\u0000\u0282\u0283\u0001\u0000\u0000\u0000\u0283\u0285\u0001"+
		"\u0000\u0000\u0000\u0284\u0282\u0001\u0000\u0000\u0000\u0285\u0286\u0003"+
		"`0\u0000\u0286_\u0001\u0000\u0000\u0000\u0287\u0293\u0005[\u0000\u0000"+
		"\u0288\u028d\u0003b1\u0000\u0289\u028a\u0005`\u0000\u0000\u028a\u028c"+
		"\u0003b1\u0000\u028b\u0289\u0001\u0000\u0000\u0000\u028c\u028f\u0001\u0000"+
		"\u0000\u0000\u028d\u028b\u0001\u0000\u0000\u0000\u028d\u028e\u0001\u0000"+
		"\u0000\u0000\u028e\u0291\u0001\u0000\u0000\u0000\u028f\u028d\u0001\u0000"+
		"\u0000\u0000\u0290\u0292\u0005`\u0000\u0000\u0291\u0290\u0001\u0000\u0000"+
		"\u0000\u0291\u0292\u0001\u0000\u0000\u0000\u0292\u0294\u0001\u0000\u0000"+
		"\u0000\u0293\u0288\u0001\u0000\u0000\u0000\u0293\u0294\u0001\u0000\u0000"+
		"\u0000\u0294\u0295\u0001\u0000\u0000\u0000\u0295\u0296\u0005\\\u0000\u0000"+
		"\u0296a\u0001\u0000\u0000\u0000\u0297\u029a\u0003`0\u0000\u0298\u029a"+
		"\u0003x<\u0000\u0299\u0297\u0001\u0000\u0000\u0000\u0299\u0298\u0001\u0000"+
		"\u0000\u0000\u029ac\u0001\u0000\u0000\u0000\u029b\u029d\u0005t\u0000\u0000"+
		"\u029c\u029e\u0003\u0098L\u0000\u029d\u029c\u0001\u0000\u0000\u0000\u029d"+
		"\u029e\u0001\u0000\u0000\u0000\u029e\u02a1\u0001\u0000\u0000\u0000\u029f"+
		"\u02a1\u0003\u009aM\u0000\u02a0\u029b\u0001\u0000\u0000\u0000\u02a0\u029f"+
		"\u0001\u0000\u0000\u0000\u02a1e\u0001\u0000\u0000\u0000\u02a2\u02a3\u0003"+
		"\u0082A\u0000\u02a3g\u0001\u0000\u0000\u0000\u02a4\u02a5\u0005\u0005\u0000"+
		"\u0000\u02a5\u02a7\u0005[\u0000\u0000\u02a6\u02a8\u0003j5\u0000\u02a7"+
		"\u02a6\u0001\u0000\u0000\u0000\u02a7\u02a8\u0001\u0000\u0000\u0000\u02a8"+
		"\u02a9\u0001\u0000\u0000\u0000\u02a9\u02aa\u0005\\\u0000\u0000\u02aai"+
		"\u0001\u0000\u0000\u0000\u02ab\u02b0\u0003l6\u0000\u02ac\u02ad\u0005`"+
		"\u0000\u0000\u02ad\u02af\u0003l6\u0000\u02ae\u02ac\u0001\u0000\u0000\u0000"+
		"\u02af\u02b2\u0001\u0000\u0000\u0000\u02b0\u02ae\u0001\u0000\u0000\u0000"+
		"\u02b0\u02b1\u0001\u0000\u0000\u0000\u02b1k\u0001\u0000\u0000\u0000\u02b2"+
		"\u02b0\u0001\u0000\u0000\u0000\u02b3\u02b4\u0005t\u0000\u0000\u02b4\u02b5"+
		"\u0005=\u0000\u0000\u02b5\u02bb\u0005[\u0000\u0000\u02b6\u02b7\u0003n"+
		"7\u0000\u02b7\u02b8\u0005`\u0000\u0000\u02b8\u02ba\u0001\u0000\u0000\u0000"+
		"\u02b9\u02b6\u0001\u0000\u0000\u0000\u02ba\u02bd\u0001\u0000\u0000\u0000"+
		"\u02bb\u02b9\u0001\u0000\u0000\u0000\u02bb\u02bc\u0001\u0000\u0000\u0000"+
		"\u02bc\u02be\u0001\u0000\u0000\u0000\u02bd\u02bb\u0001\u0000\u0000\u0000"+
		"\u02be\u02bf\u0005\u0004\u0000\u0000\u02bf\u02c0\u0005=\u0000\u0000\u02c0"+
		"\u02c1\u0003x<\u0000\u02c1\u02c2\u0005\\\u0000\u0000\u02c2m\u0001\u0000"+
		"\u0000\u0000\u02c3\u02c4\u0005t\u0000\u0000\u02c4\u02c5\u0005=\u0000\u0000"+
		"\u02c5\u02c6\u0003x<\u0000\u02c6o\u0001\u0000\u0000\u0000\u02c7\u02c8"+
		"\u0003r9\u0000\u02c8\u02c9\u0003B!\u0000\u02c9q\u0001\u0000\u0000\u0000"+
		"\u02ca\u02cb\u0005\u0003\u0000\u0000\u02cb\u02cc\u0003x<\u0000\u02cc\u02cd"+
		"\u0005b\u0000\u0000\u02cd\u02d1\u0001\u0000\u0000\u0000\u02ce\u02cf\u0005"+
		"\u0004\u0000\u0000\u02cf\u02d1\u0005b\u0000\u0000\u02d0\u02ca\u0001\u0000"+
		"\u0000\u0000\u02d0\u02ce\u0001\u0000\u0000\u0000\u02d1s\u0001\u0000\u0000"+
		"\u0000\u02d2\u02d3\u0005Y\u0000\u0000\u02d3\u02d4\u0003x<\u0000\u02d4"+
		"\u02d5\u0005Z\u0000\u0000\u02d5u\u0001\u0000\u0000\u0000\u02d6\u02db\u0003"+
		"x<\u0000\u02d7\u02d8\u0005`\u0000\u0000\u02d8\u02da\u0003x<\u0000\u02d9"+
		"\u02d7\u0001\u0000\u0000\u0000\u02da\u02dd\u0001\u0000\u0000\u0000\u02db"+
		"\u02d9\u0001\u0000\u0000\u0000\u02db\u02dc\u0001\u0000\u0000\u0000\u02dc"+
		"w\u0001\u0000\u0000\u0000\u02dd\u02db\u0001\u0000\u0000\u0000\u02de\u02df"+
		"\u0006<\uffff\uffff\u0000\u02df\u02e7\u0003z=\u0000\u02e0\u02e1\u0007"+
		"\u0006\u0000\u0000\u02e1\u02e7\u0003x<\u0010\u02e2\u02e3\u0007\u0007\u0000"+
		"\u0000\u02e3\u02e7\u0003x<\u000f\u02e4\u02e5\u0005t\u0000\u0000\u02e5"+
		"\u02e7\u0003\u0082A\u0000\u02e6\u02de\u0001\u0000\u0000\u0000\u02e6\u02e0"+
		"\u0001\u0000\u0000\u0000\u02e6\u02e2\u0001\u0000\u0000\u0000\u02e6\u02e4"+
		"\u0001\u0000\u0000\u0000\u02e7\u0329\u0001\u0000\u0000\u0000\u02e8\u02e9"+
		"\n\u000e\u0000\u0000\u02e9\u02ea\u0007\b\u0000\u0000\u02ea\u0328\u0003"+
		"x<\u000f\u02eb\u02ec\n\r\u0000\u0000\u02ec\u02ed\u0007\t\u0000\u0000\u02ed"+
		"\u0328\u0003x<\u000e\u02ee\u02f6\n\f\u0000\u0000\u02ef\u02f0\u00059\u0000"+
		"\u0000\u02f0\u02f7\u00059\u0000\u0000\u02f1\u02f2\u00058\u0000\u0000\u02f2"+
		"\u02f3\u00058\u0000\u0000\u02f3\u02f7\u00058\u0000\u0000\u02f4\u02f5\u0005"+
		"8\u0000\u0000\u02f5\u02f7\u00058\u0000\u0000\u02f6\u02ef\u0001\u0000\u0000"+
		"\u0000\u02f6\u02f1\u0001\u0000\u0000\u0000\u02f6\u02f4\u0001\u0000\u0000"+
		"\u0000\u02f7\u02f8\u0001\u0000\u0000\u0000\u02f8\u0328\u0003x<\r\u02f9"+
		"\u02fa\n\u000b\u0000\u0000\u02fa\u02fb\u0007\n\u0000\u0000\u02fb\u0328"+
		"\u0003x<\f\u02fc\u02fd\n\t\u0000\u0000\u02fd\u02fe\u0007\u000b\u0000\u0000"+
		"\u02fe\u0328\u0003x<\n\u02ff\u0300\n\b\u0000\u0000\u0300\u0301\u0005J"+
		"\u0000\u0000\u0301\u0328\u0003x<\t\u0302\u0303\n\u0007\u0000\u0000\u0303"+
		"\u0304\u0005L\u0000\u0000\u0304\u0328\u0003x<\b\u0305\u0306\n\u0006\u0000"+
		"\u0000\u0306\u0307\u0005K\u0000\u0000\u0307\u0328\u0003x<\u0007\u0308"+
		"\u0309\n\u0005\u0000\u0000\u0309\u030a\u0005B\u0000\u0000\u030a\u0328"+
		"\u0003x<\u0006\u030b\u030c\n\u0004\u0000\u0000\u030c\u030d\u0005C\u0000"+
		"\u0000\u030d\u0328\u0003x<\u0005\u030e\u030f\n\u0003\u0000\u0000\u030f"+
		"\u0310\u0005<\u0000\u0000\u0310\u0311\u0003x<\u0000\u0311\u0312\u0005"+
		"=\u0000\u0000\u0312\u0313\u0003x<\u0003\u0313\u0328\u0001\u0000\u0000"+
		"\u0000\u0314\u0315\n\u0013\u0000\u0000\u0315\u0316\u0005a\u0000\u0000"+
		"\u0316\u0328\u0007\u0003\u0000\u0000\u0317\u0318\n\u0012\u0000\u0000\u0318"+
		"\u0319\u0005]\u0000\u0000\u0319\u031a\u0003x<\u0000\u031a\u031b\u0005"+
		"^\u0000\u0000\u031b\u0328\u0001\u0000\u0000\u0000\u031c\u031d\n\u0011"+
		"\u0000\u0000\u031d\u0328\u0007\f\u0000\u0000\u031e\u031f\n\n\u0000\u0000"+
		"\u031f\u0320\u0005\u001c\u0000\u0000\u0320\u0328\u0003\u0092I\u0000\u0321"+
		"\u0322\n\u0002\u0000\u0000\u0322\u0324\u0005c\u0000\u0000\u0323\u0325"+
		"\u0003\u0098L\u0000\u0324\u0323\u0001\u0000\u0000\u0000\u0324\u0325\u0001"+
		"\u0000\u0000\u0000\u0325\u0326\u0001\u0000\u0000\u0000\u0326\u0328\u0005"+
		"t\u0000\u0000\u0327\u02e8\u0001\u0000\u0000\u0000\u0327\u02eb\u0001\u0000"+
		"\u0000\u0000\u0327\u02ee\u0001\u0000\u0000\u0000\u0327\u02f9\u0001\u0000"+
		"\u0000\u0000\u0327\u02fc\u0001\u0000\u0000\u0000\u0327\u02ff\u0001\u0000"+
		"\u0000\u0000\u0327\u0302\u0001\u0000\u0000\u0000\u0327\u0305\u0001\u0000"+
		"\u0000\u0000\u0327\u0308\u0001\u0000\u0000\u0000\u0327\u030b\u0001\u0000"+
		"\u0000\u0000\u0327\u030e\u0001\u0000\u0000\u0000\u0327\u0314\u0001\u0000"+
		"\u0000\u0000\u0327\u0317\u0001\u0000\u0000\u0000\u0327\u031c\u0001\u0000"+
		"\u0000\u0000\u0327\u031e\u0001\u0000\u0000\u0000\u0327\u0321\u0001\u0000"+
		"\u0000\u0000\u0328\u032b\u0001\u0000\u0000\u0000\u0329\u0327\u0001\u0000"+
		"\u0000\u0000\u0329\u032a\u0001\u0000\u0000\u0000\u032ay\u0001\u0000\u0000"+
		"\u0000\u032b\u0329\u0001\u0000\u0000\u0000\u032c\u032d\u0005Y\u0000\u0000"+
		"\u032d\u032e\u0003x<\u0000\u032e\u032f\u0005Z\u0000\u0000\u032f\u0334"+
		"\u0001\u0000\u0000\u0000\u0330\u0334\u0005,\u0000\u0000\u0331\u0334\u0003"+
		"\u008aE\u0000\u0332\u0334\u0005t\u0000\u0000\u0333\u032c\u0001\u0000\u0000"+
		"\u0000\u0333\u0330\u0001\u0000\u0000\u0000\u0333\u0331\u0001\u0000\u0000"+
		"\u0000\u0333\u0332\u0001\u0000\u0000\u0000\u0334{\u0001\u0000\u0000\u0000"+
		"\u0335\u0336\u0003\u0098L\u0000\u0336\u0337\u0003~?\u0000\u0337}\u0001"+
		"\u0000\u0000\u0000\u0338\u0339\u0005*\u0000\u0000\u0339\u033d\u0003\u0080"+
		"@\u0000\u033a\u033b\u0005t\u0000\u0000\u033b\u033d\u0003\u0082A\u0000"+
		"\u033c\u0338\u0001\u0000\u0000\u0000\u033c\u033a\u0001\u0000\u0000\u0000"+
		"\u033d\u007f\u0001\u0000\u0000\u0000\u033e\u0348\u0003\u0082A\u0000\u033f"+
		"\u0341\u0005a\u0000\u0000\u0340\u0342\u0003\u0098L\u0000\u0341\u0340\u0001"+
		"\u0000\u0000\u0000\u0341\u0342\u0001\u0000\u0000\u0000\u0342\u0343\u0001"+
		"\u0000\u0000\u0000\u0343\u0345\u0005t\u0000\u0000\u0344\u0346\u0003\u0082"+
		"A\u0000\u0345\u0344\u0001\u0000\u0000\u0000\u0345\u0346\u0001\u0000\u0000"+
		"\u0000\u0346\u0348\u0001\u0000\u0000\u0000\u0347\u033e\u0001\u0000\u0000"+
		"\u0000\u0347\u033f\u0001\u0000\u0000\u0000\u0348\u0081\u0001\u0000\u0000"+
		"\u0000\u0349\u034b\u0005Y\u0000\u0000\u034a\u034c\u0003v;\u0000\u034b"+
		"\u034a\u0001\u0000\u0000\u0000\u034b\u034c\u0001\u0000\u0000\u0000\u034c"+
		"\u034d\u0001\u0000\u0000\u0000\u034d\u034e\u0005Z\u0000\u0000\u034e\u0083"+
		"\u0001\u0000\u0000\u0000\u034f\u0351\u0005t\u0000\u0000\u0350\u0352\u0003"+
		"\u0098L\u0000\u0351\u0350\u0001\u0000\u0000\u0000\u0351\u0352\u0001\u0000"+
		"\u0000\u0000\u0352\u0085\u0001\u0000\u0000\u0000\u0353\u0354\u0003x<\u0000"+
		"\u0354\u0356\u0005a\u0000\u0000\u0355\u0357\u0003\u0098L\u0000\u0356\u0355"+
		"\u0001\u0000\u0000\u0000\u0356\u0357\u0001\u0000\u0000\u0000\u0357\u0358"+
		"\u0001\u0000\u0000\u0000\u0358\u0359\u0005t\u0000\u0000\u0359\u035b\u0005"+
		"Y\u0000\u0000\u035a\u035c\u0003v;\u0000\u035b\u035a\u0001\u0000\u0000"+
		"\u0000\u035b\u035c\u0001\u0000\u0000\u0000\u035c\u035d\u0001\u0000\u0000"+
		"\u0000\u035d\u035e\u0005Z\u0000\u0000\u035e\u036c\u0001\u0000\u0000\u0000"+
		"\u035f\u0360\u0005,\u0000\u0000\u0360\u0362\u0005Y\u0000\u0000\u0361\u0363"+
		"\u0003v;\u0000\u0362\u0361\u0001\u0000\u0000\u0000\u0362\u0363\u0001\u0000"+
		"\u0000\u0000\u0363\u0364\u0001\u0000\u0000\u0000\u0364\u036c\u0005Z\u0000"+
		"\u0000\u0365\u0366\u0005*\u0000\u0000\u0366\u0368\u0005Y\u0000\u0000\u0367"+
		"\u0369\u0003v;\u0000\u0368\u0367\u0001\u0000\u0000\u0000\u0368\u0369\u0001"+
		"\u0000\u0000\u0000\u0369\u036a\u0001\u0000\u0000\u0000\u036a\u036c\u0005"+
		"Z\u0000\u0000\u036b\u0353\u0001\u0000\u0000\u0000\u036b\u035f\u0001\u0000"+
		"\u0000\u0000\u036b\u0365\u0001\u0000\u0000\u0000\u036c\u0087\u0001\u0000"+
		"\u0000\u0000\u036d\u036e\u0003x<\u0000\u036e\u0370\u0005Y\u0000\u0000"+
		"\u036f\u0371\u0003v;\u0000\u0370\u036f\u0001\u0000\u0000\u0000\u0370\u0371"+
		"\u0001\u0000\u0000\u0000\u0371\u0372\u0001\u0000\u0000\u0000\u0372\u0373"+
		"\u0005Z\u0000\u0000\u0373\u0089\u0001\u0000\u0000\u0000\u0374\u037c\u0003"+
		"\u008cF\u0000\u0375\u037c\u0003\u008eG\u0000\u0376\u037c\u0005m\u0000"+
		"\u0000\u0377\u037c\u0005n\u0000\u0000\u0378\u037c\u0005l\u0000\u0000\u0379"+
		"\u037c\u0005\r\u0000\u0000\u037a\u037c\u0005o\u0000\u0000\u037b\u0374"+
		"\u0001\u0000\u0000\u0000\u037b\u0375\u0001\u0000\u0000\u0000\u037b\u0376"+
		"\u0001\u0000\u0000\u0000\u037b\u0377\u0001\u0000\u0000\u0000\u037b\u0378"+
		"\u0001\u0000\u0000\u0000\u037b\u0379\u0001\u0000\u0000\u0000\u037b\u037a"+
		"\u0001\u0000\u0000\u0000\u037c\u008b\u0001\u0000\u0000\u0000\u037d\u037e"+
		"\u0007\r\u0000\u0000\u037e\u008d\u0001\u0000\u0000\u0000\u037f\u0380\u0007"+
		"\u000e\u0000\u0000\u0380\u008f\u0001\u0000\u0000\u0000\u0381\u0384\u0003"+
		"\u0092I\u0000\u0382\u0384\u00050\u0000\u0000\u0383\u0381\u0001\u0000\u0000"+
		"\u0000\u0383\u0382\u0001\u0000\u0000\u0000\u0384\u0091\u0001\u0000\u0000"+
		"\u0000\u0385\u0386\u0006I\uffff\uffff\u0000\u0386\u039f\u0003\u0096K\u0000"+
		"\u0387\u039f\u0003\u009aM\u0000\u0388\u039f\u00052\u0000\u0000\u0389\u039f"+
		"\u00053\u0000\u0000\u038a\u0393\u0005Y\u0000\u0000\u038b\u0390\u0003\u0092"+
		"I\u0000\u038c\u038d\u0005`\u0000\u0000\u038d\u038f\u0003\u0092I\u0000"+
		"\u038e\u038c\u0001\u0000\u0000\u0000\u038f\u0392\u0001\u0000\u0000\u0000"+
		"\u0390\u038e\u0001\u0000\u0000\u0000\u0390\u0391\u0001\u0000\u0000\u0000"+
		"\u0391\u0394\u0001\u0000\u0000\u0000\u0392\u0390\u0001\u0000\u0000\u0000"+
		"\u0393\u038b\u0001\u0000\u0000\u0000\u0393\u0394\u0001\u0000\u0000\u0000"+
		"\u0394\u0395\u0001\u0000\u0000\u0000\u0395\u0396\u0005Z\u0000\u0000\u0396"+
		"\u0397\u0005b\u0000\u0000\u0397\u039f\u0003\u0092I\u0002\u0398\u0399\u0005"+
		"]\u0000\u0000\u0399\u039a\u0003\u0092I\u0000\u039a\u039b\u0005`\u0000"+
		"\u0000\u039b\u039c\u0003\u0092I\u0000\u039c\u039d\u0005^\u0000\u0000\u039d"+
		"\u039f\u0001\u0000\u0000\u0000\u039e\u0385\u0001\u0000\u0000\u0000\u039e"+
		"\u0387\u0001\u0000\u0000\u0000\u039e\u0388\u0001\u0000\u0000\u0000\u039e"+
		"\u0389\u0001\u0000\u0000\u0000\u039e\u038a\u0001\u0000\u0000\u0000\u039e"+
		"\u0398\u0001\u0000\u0000\u0000\u039f\u03b2\u0001\u0000\u0000\u0000\u03a0"+
		"\u03a3\n\u0005\u0000\u0000\u03a1\u03a2\u0005K\u0000\u0000\u03a2\u03a4"+
		"\u0003\u0092I\u0000\u03a3\u03a1\u0001\u0000\u0000\u0000\u03a4\u03a5\u0001"+
		"\u0000\u0000\u0000\u03a5\u03a3\u0001\u0000\u0000\u0000\u03a5\u03a6\u0001"+
		"\u0000\u0000\u0000\u03a6\u03b1\u0001\u0000\u0000\u0000\u03a7\u03aa\n\u0004"+
		"\u0000\u0000\u03a8\u03a9\u0005J\u0000\u0000\u03a9\u03ab\u0003\u0092I\u0000"+
		"\u03aa\u03a8\u0001\u0000\u0000\u0000\u03ab\u03ac\u0001\u0000\u0000\u0000"+
		"\u03ac\u03aa\u0001\u0000\u0000\u0000\u03ac\u03ad\u0001\u0000\u0000\u0000"+
		"\u03ad\u03b1\u0001\u0000\u0000\u0000\u03ae\u03af\n\u0003\u0000\u0000\u03af"+
		"\u03b1\u0003\u0094J\u0000\u03b0\u03a0\u0001\u0000\u0000\u0000\u03b0\u03a7"+
		"\u0001\u0000\u0000\u0000\u03b0\u03ae\u0001\u0000\u0000\u0000\u03b1\u03b4"+
		"\u0001\u0000\u0000\u0000\u03b2\u03b0\u0001\u0000\u0000\u0000\u03b2\u03b3"+
		"\u0001\u0000\u0000\u0000\u03b3\u0093\u0001\u0000\u0000\u0000\u03b4\u03b2"+
		"\u0001\u0000\u0000\u0000\u03b5\u03b6\u0007\u000f\u0000\u0000\u03b6\u0095"+
		"\u0001\u0000\u0000\u0000\u03b7\u03b9\u00034\u001a\u0000\u03b8\u03ba\u0003"+
		"\u0098L\u0000\u03b9\u03b8\u0001\u0000\u0000\u0000\u03b9\u03ba\u0001\u0000"+
		"\u0000\u0000\u03ba\u0097\u0001\u0000\u0000\u0000\u03bb\u03bc\u00059\u0000"+
		"\u0000\u03bc\u03c1\u0003\u0092I\u0000\u03bd\u03be\u0005`\u0000\u0000\u03be"+
		"\u03c0\u0003\u0092I\u0000\u03bf\u03bd\u0001\u0000\u0000\u0000\u03c0\u03c3"+
		"\u0001\u0000\u0000\u0000\u03c1\u03bf\u0001\u0000\u0000\u0000\u03c1\u03c2"+
		"\u0001\u0000\u0000\u0000\u03c2\u03c4\u0001\u0000\u0000\u0000\u03c3\u03c1"+
		"\u0001\u0000\u0000\u0000\u03c4\u03c5\u00058\u0000\u0000\u03c5\u0099\u0001"+
		"\u0000\u0000\u0000\u03c6\u03c7\u0007\u0010\u0000\u0000\u03c7\u009b\u0001"+
		"\u0000\u0000\u0000\u03c8\u03d0\u0003\u009eO\u0000\u03c9\u03d0\u0005 \u0000"+
		"\u0000\u03ca\u03d0\u0005\u0015\u0000\u0000\u03cb\u03d0\u0005\u0016\u0000"+
		"\u0000\u03cc\u03d0\u0005\u0017\u0000\u0000\u03cd\u03d0\u0005\n\u0000\u0000"+
		"\u03ce\u03d0\u00056\u0000\u0000\u03cf\u03c8\u0001\u0000\u0000\u0000\u03cf"+
		"\u03c9\u0001\u0000\u0000\u0000\u03cf\u03ca\u0001\u0000\u0000\u0000\u03cf"+
		"\u03cb\u0001\u0000\u0000\u0000\u03cf\u03cc\u0001\u0000\u0000\u0000\u03cf"+
		"\u03cd\u0001\u0000\u0000\u0000\u03cf\u03ce\u0001\u0000\u0000\u0000\u03d0"+
		"\u009d\u0001\u0000\u0000\u0000\u03d1\u03d2\u0007\u0011\u0000\u0000\u03d2"+
		"\u009f\u0001\u0000\u0000\u0000\u03d3\u03d4\u0003\u00a2Q\u0000\u03d4\u03d5"+
		"\u0005=\u0000\u0000\u03d5\u03d6\u0003\u0090H\u0000\u03d6\u03d7\u0005b"+
		"\u0000\u0000\u03d7\u03d8\u0003\u00a4R\u0000\u03d8\u00a1\u0001\u0000\u0000"+
		"\u0000\u03d9\u03db\u0005Y\u0000\u0000\u03da\u03dc\u0003<\u001e\u0000\u03db"+
		"\u03da\u0001\u0000\u0000\u0000\u03db\u03dc\u0001\u0000\u0000\u0000\u03dc"+
		"\u03dd\u0001\u0000\u0000\u0000\u03dd\u03de\u0005Z\u0000\u0000\u03de\u00a3"+
		"\u0001\u0000\u0000\u0000\u03df\u03e0\u0003B!\u0000\u03e0\u00a5\u0001\u0000"+
		"\u0000\u0000\u03e1\u03e2\u0005\t\u0000\u0000\u03e2\u03e3\u0005t\u0000"+
		"\u0000\u03e3\u03e7\u0005[\u0000\u0000\u03e4\u03e6\u0003\u00a8T\u0000\u03e5"+
		"\u03e4\u0001\u0000\u0000\u0000\u03e6\u03e9\u0001\u0000\u0000\u0000\u03e7"+
		"\u03e5\u0001\u0000\u0000\u0000\u03e7\u03e8\u0001\u0000\u0000\u0000\u03e8"+
		"\u03ea\u0001\u0000\u0000\u0000\u03e9\u03e7\u0001\u0000\u0000\u0000\u03ea"+
		"\u03eb\u0005\\\u0000\u0000\u03eb\u00a7\u0001\u0000\u0000\u0000\u03ec\u03ed"+
		"\u0005t\u0000\u0000\u03ed\u03ee\u0005=\u0000\u0000\u03ee\u03ef\u0003x"+
		"<\u0000\u03ef\u03f0\u0005_\u0000\u0000\u03f0\u00a9\u0001\u0000\u0000\u0000"+
		"\u03f1\u03f2\u0005d\u0000\u0000\u03f2\u03f3\u0005t\u0000\u0000\u03f3\u00ab"+
		"\u0001\u0000\u0000\u0000i\u00ad\u00b2\u00b8\u00c5\u00cb\u00ce\u00d3\u00d9"+
		"\u00dd\u00e1\u00e9\u00f3\u00f9\u00fe\u0106\u010d\u0111\u0114\u0117\u0120"+
		"\u0125\u012b\u0131\u0137\u013b\u0143\u014b\u0150\u0157\u015b\u0168\u016d"+
		"\u0175\u017c\u0184\u018a\u0194\u019e\u01a6\u01ac\u01b0\u01b9\u01c3\u01cb"+
		"\u01d1\u01d8\u01eb\u01f6\u01fc\u0206\u0226\u022b\u0234\u0244\u024a\u024e"+
		"\u0252\u0259\u0266\u0272\u027a\u0282\u028d\u0291\u0293\u0299\u029d\u02a0"+
		"\u02a7\u02b0\u02bb\u02d0\u02db\u02e6\u02f6\u0324\u0327\u0329\u0333\u033c"+
		"\u0341\u0345\u0347\u034b\u0351\u0356\u035b\u0362\u0368\u036b\u0370\u037b"+
		"\u0383\u0390\u0393\u039e\u03a5\u03ac\u03b0\u03b2\u03b9\u03c1\u03cf\u03db"+
		"\u03e7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}