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
		INT=29, INTERFACE=30, NATIVE=31, ENEW=32, UNEW=33, NEW=34, ALLOCATE=35, 
		PRIVATE=36, PROTECTED=37, PUBLIC=38, RETURN=39, STATIC=40, SUPER=41, SWITCH=42, 
		THIS=43, THROW=44, THROWS=45, TRY=46, VOID=47, WHILE=48, ANY=49, NEVER=50, 
		SELECT=51, SELECT_FIRST=52, DELETED=53, ASSIGN=54, GT=55, LT=56, BANG=57, 
		TILDE=58, QUESTION=59, COLON=60, EQUAL=61, LE=62, GE=63, NOTEQUAL=64, 
		AND=65, OR=66, INC=67, DEC=68, ADD=69, SUB=70, MUL=71, DIV=72, BITAND=73, 
		BITOR=74, CARET=75, MOD=76, ADD_ASSIGN=77, SUB_ASSIGN=78, MUL_ASSIGN=79, 
		DIV_ASSIGN=80, AND_ASSIGN=81, OR_ASSIGN=82, XOR_ASSIGN=83, MOD_ASSIGN=84, 
		LSHIFT_ASSIGN=85, RSHIFT_ASSIGN=86, URSHIFT_ASSIGN=87, LPAREN=88, RPAREN=89, 
		LBRACE=90, RBRACE=91, LBRACK=92, RBRACK=93, SEMI=94, COMMA=95, DOT=96, 
		ARROW=97, COLONCOLON=98, AT=99, ELLIPSIS=100, DECIMAL_LITERAL=101, HEX_LITERAL=102, 
		OCT_LITERAL=103, BINARY_LITERAL=104, FLOAT_LITERAL=105, HEX_FLOAT_LITERAL=106, 
		BOOL_LITERAL=107, CHAR_LITERAL=108, STRING_LITERAL=109, TEXT_BLOCK=110, 
		R=111, RW=112, C=113, V=114, IDENTIFIER=115, WS=116, COMMENT=117, LINE_COMMENT=118;
	public static final int
		RULE_compilationUnit = 0, RULE_packageDeclaration = 1, RULE_importDeclaration = 2, 
		RULE_typeDeclaration = 3, RULE_classDeclaration = 4, RULE_classBody = 5, 
		RULE_typeList = 6, RULE_classBodyDeclaration = 7, RULE_enumDeclaration = 8, 
		RULE_enumConstants = 9, RULE_enumConstant = 10, RULE_enumBodyDeclarations = 11, 
		RULE_interfaceDeclaration = 12, RULE_interfaceBody = 13, RULE_interfaceBodyDeclaration = 14, 
		RULE_interfaceMemberDeclaration = 15, RULE_interfaceMethodDeclaration = 16, 
		RULE_interfaceMethodModifier = 17, RULE_interfaceCommonBodyDeclaration = 18, 
		RULE_memberDeclaration = 19, RULE_fieldDeclaration = 20, RULE_methodDeclaration = 21, 
		RULE_constructorDeclaration = 22, RULE_typeParameters = 23, RULE_qualifiedNameList = 24, 
		RULE_qualifiedName = 25, RULE_typeParameter = 26, RULE_formalParameters = 27, 
		RULE_receiverParameter = 28, RULE_formalParameterList = 29, RULE_formalParameter = 30, 
		RULE_methodBody = 31, RULE_block = 32, RULE_labeledStatement = 33, RULE_statement = 34, 
		RULE_allocator = 35, RULE_allocatorFieldList = 36, RULE_allocatorField = 37, 
		RULE_select = 38, RULE_forControl = 39, RULE_loopVariableDeclarators = 40, 
		RULE_loopVariableDeclarator = 41, RULE_loopVariableUpdates = 42, RULE_loopVariableUpdate = 43, 
		RULE_qualifiedFieldName = 44, RULE_creator = 45, RULE_arrayCreatorRest = 46, 
		RULE_arrayInitializer = 47, RULE_variableInitializer = 48, RULE_createdName = 49, 
		RULE_classCreatorRest = 50, RULE_catchClause = 51, RULE_catchFields = 52, 
		RULE_catchField = 53, RULE_catchValue = 54, RULE_branchCase = 55, RULE_switchLabel = 56, 
		RULE_parExpression = 57, RULE_expressionList = 58, RULE_expression = 59, 
		RULE_primary = 60, RULE_explicitGenericInvocation = 61, RULE_explicitGenericInvocationSuffix = 62, 
		RULE_superSuffix = 63, RULE_arguments = 64, RULE_classType = 65, RULE_methodCall = 66, 
		RULE_functionCall = 67, RULE_literal = 68, RULE_integerLiteral = 69, RULE_floatLiteral = 70, 
		RULE_typeTypeOrVoid = 71, RULE_typeType = 72, RULE_arrayKind = 73, RULE_classOrInterfaceType = 74, 
		RULE_typeArguments = 75, RULE_primitiveType = 76, RULE_modifier = 77, 
		RULE_classOrInterfaceModifier = 78, RULE_lambda = 79, RULE_lambdaParameters = 80, 
		RULE_lambdaBody = 81, RULE_indexDeclaration = 82, RULE_indexField = 83;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "packageDeclaration", "importDeclaration", "typeDeclaration", 
			"classDeclaration", "classBody", "typeList", "classBodyDeclaration", 
			"enumDeclaration", "enumConstants", "enumConstant", "enumBodyDeclarations", 
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
			"lambda", "lambdaParameters", "lambdaBody", "indexDeclaration", "indexField"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'boolean'", "'case'", "'default'", "'catch'", "'string'", 
			"'class'", "'record'", "'index'", "'unique'", "'struct'", "'time'", "'null'", 
			"'package'", "'import'", "'password'", "'double'", "'else'", "'enum'", 
			"'extends'", "'readonly'", "'child'", "'title'", "'finally'", "'for'", 
			"'if'", "'implements'", "'instanceof'", "'int'", "'interface'", "'native'", 
			"'enew'", "'unew'", "'new'", "'allocate'", "'private'", "'protected'", 
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
			"TITLE", "FINALLY", "FOR", "IF", "IMPLEMENTS", "INSTANCEOF", "INT", "INTERFACE", 
			"NATIVE", "ENEW", "UNEW", "NEW", "ALLOCATE", "PRIVATE", "PROTECTED", 
			"PUBLIC", "RETURN", "STATIC", "SUPER", "SWITCH", "THIS", "THROW", "THROWS", 
			"TRY", "VOID", "WHILE", "ANY", "NEVER", "SELECT", "SELECT_FIRST", "DELETED", 
			"ASSIGN", "GT", "LT", "BANG", "TILDE", "QUESTION", "COLON", "EQUAL", 
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
			setState(169);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PACKAGE) {
				{
				setState(168);
				packageDeclaration();
				}
			}

			setState(174);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(171);
				importDeclaration();
				}
				}
				setState(176);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(178); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(177);
				typeDeclaration();
				}
				}
				setState(180); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 1581622233474L) != 0) || _la==SEMI );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
			setState(182);
			match(PACKAGE);
			setState(183);
			qualifiedName();
			setState(184);
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
			setState(186);
			match(IMPORT);
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
			setState(202);
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
				enterOuterAlt(_localctx, 1);
				{
				setState(193);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1580547964930L) != 0)) {
					{
					{
					setState(190);
					classOrInterfaceModifier();
					}
					}
					setState(195);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(199);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLASS:
				case RECORD:
				case STRUCT:
					{
					setState(196);
					classDeclaration();
					}
					break;
				case ENUM:
					{
					setState(197);
					enumDeclaration();
					}
					break;
				case INTERFACE:
					{
					setState(198);
					interfaceDeclaration();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(201);
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
			setState(204);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2432L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(205);
			match(IDENTIFIER);
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(206);
				typeParameters();
				}
			}

			setState(211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(209);
				match(EXTENDS);
				setState(210);
				typeType(0);
				}
			}

			setState(215);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(213);
				match(IMPLEMENTS);
				setState(214);
				typeList();
				}
			}

			setState(217);
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
			setState(219);
			match(LBRACE);
			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 82895963888498246L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217745L) != 0)) {
				{
				{
				setState(220);
				classBodyDeclaration();
				}
				}
				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(226);
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
			setState(228);
			typeType(0);
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(229);
				match(COMMA);
				setState(230);
				typeType(0);
				}
				}
				setState(235);
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
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9008781964870658L) != 0)) {
				{
				{
				setState(236);
				modifier();
				}
				}
				setState(241);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(242);
			memberDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		enterRule(_localctx, 16, RULE_enumDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			match(ENUM);
			setState(245);
			match(IDENTIFIER);
			setState(248);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(246);
				match(IMPLEMENTS);
				setState(247);
				typeList();
				}
			}

			setState(250);
			match(LBRACE);
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(251);
				enumConstants();
				}
			}

			setState(255);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(254);
				match(COMMA);
				}
			}

			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(257);
				enumBodyDeclarations();
				}
			}

			setState(260);
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
		enterRule(_localctx, 18, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(262);
			enumConstant();
			setState(267);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(263);
					match(COMMA);
					setState(264);
					enumConstant();
					}
					} 
				}
				setState(269);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
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
		enterRule(_localctx, 20, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
			match(IDENTIFIER);
			setState(272);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(271);
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
		enterRule(_localctx, 22, RULE_enumBodyDeclarations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(SEMI);
			setState(278);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 82895963888498246L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217745L) != 0)) {
				{
				{
				setState(275);
				classBodyDeclaration();
				}
				}
				setState(280);
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
		enterRule(_localctx, 24, RULE_interfaceDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281);
			match(INTERFACE);
			setState(282);
			match(IDENTIFIER);
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(283);
				typeParameters();
				}
			}

			setState(288);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(286);
				match(EXTENDS);
				setState(287);
				typeList();
				}
			}

			setState(290);
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
		enterRule(_localctx, 26, RULE_interfaceBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(292);
			match(LBRACE);
			setState(296);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 82895963888497750L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217809L) != 0)) {
				{
				{
				setState(293);
				interfaceBodyDeclaration();
				}
				}
				setState(298);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(299);
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
		enterRule(_localctx, 28, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			setState(309);
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
				setState(304);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(301);
						modifier();
						}
						} 
					}
					setState(306);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				}
				setState(307);
				interfaceMemberDeclaration();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(308);
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
		enterRule(_localctx, 30, RULE_interfaceMemberDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
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
		enterRule(_localctx, 32, RULE_interfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1374389534738L) != 0)) {
				{
				{
				setState(313);
				interfaceMethodModifier();
				}
				}
				setState(318);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(320);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(319);
				typeParameters();
				}
			}

			setState(322);
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
		enterRule(_localctx, 34, RULE_interfaceMethodModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1374389534738L) != 0)) ) {
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
		enterRule(_localctx, 36, RULE_interfaceCommonBodyDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			typeTypeOrVoid();
			setState(327);
			match(IDENTIFIER);
			setState(328);
			formalParameters();
			setState(333);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(329);
				match(LBRACK);
				setState(330);
				match(RBRACK);
				}
				}
				setState(335);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(338);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(336);
				match(THROWS);
				setState(337);
				qualifiedNameList();
				}
			}

			setState(340);
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
		enterRule(_localctx, 38, RULE_memberDeclaration);
		try {
			setState(346);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(342);
				methodDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(343);
				fieldDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(344);
				constructorDeclaration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(345);
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
		enterRule(_localctx, 40, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			typeType(0);
			setState(349);
			match(IDENTIFIER);
			setState(350);
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
		enterRule(_localctx, 42, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(353);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(352);
				typeParameters();
				}
			}

			setState(355);
			typeTypeOrVoid();
			setState(356);
			match(IDENTIFIER);
			setState(357);
			formalParameters();
			setState(358);
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
		enterRule(_localctx, 44, RULE_constructorDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(360);
				typeParameters();
				}
			}

			setState(363);
			match(IDENTIFIER);
			setState(364);
			formalParameters();
			setState(367);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(365);
				match(THROWS);
				setState(366);
				qualifiedNameList();
				}
			}

			setState(369);
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
		enterRule(_localctx, 46, RULE_typeParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			match(LT);
			setState(372);
			typeParameter();
			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(373);
				match(COMMA);
				setState(374);
				typeParameter();
				}
				}
				setState(379);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(380);
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
		enterRule(_localctx, 48, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(382);
			qualifiedName();
			setState(387);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(383);
				match(COMMA);
				setState(384);
				qualifiedName();
				}
				}
				setState(389);
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
		enterRule(_localctx, 50, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(390);
			match(IDENTIFIER);
			setState(395);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(391);
					match(DOT);
					setState(392);
					match(IDENTIFIER);
					}
					} 
				}
				setState(397);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
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
		enterRule(_localctx, 52, RULE_typeParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(398);
			match(IDENTIFIER);
			setState(401);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(399);
				match(EXTENDS);
				setState(400);
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
		enterRule(_localctx, 54, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			match(LPAREN);
			setState(405);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1829587885699140L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217745L) != 0)) {
				{
				setState(404);
				formalParameterList();
				}
			}

			setState(407);
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
		enterRule(_localctx, 56, RULE_receiverParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
			typeType(0);
			setState(414);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(410);
				match(IDENTIFIER);
				setState(411);
				match(DOT);
				}
				}
				setState(416);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(417);
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
		enterRule(_localctx, 58, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
			formalParameter();
			setState(424);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(420);
				match(COMMA);
				setState(421);
				formalParameter();
				}
				}
				setState(426);
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
		enterRule(_localctx, 60, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(427);
			typeType(0);
			setState(428);
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
		enterRule(_localctx, 62, RULE_methodBody);
		try {
			setState(432);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(430);
				block();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(431);
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
		enterRule(_localctx, 64, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
			match(LBRACE);
			setState(438);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 439486407019339776L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299050119200783L) != 0)) {
				{
				{
				setState(435);
				labeledStatement();
				}
				}
				setState(440);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(441);
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
		enterRule(_localctx, 66, RULE_labeledStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(443);
				match(IDENTIFIER);
				setState(444);
				match(COLON);
				}
				break;
			}
			setState(447);
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
		enterRule(_localctx, 68, RULE_statement);
		int _la;
		try {
			setState(520);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(449);
				match(WHILE);
				setState(450);
				parExpression();
				setState(451);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(453);
				match(FOR);
				setState(454);
				match(LPAREN);
				setState(455);
				forControl();
				setState(456);
				match(RPAREN);
				setState(457);
				block();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(459);
				match(IF);
				setState(460);
				parExpression();
				setState(461);
				block();
				setState(464);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(462);
					match(ELSE);
					setState(463);
					block();
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(466);
				match(TRY);
				setState(467);
				block();
				setState(468);
				catchClause();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(470);
				match(SWITCH);
				setState(471);
				match(LBRACE);
				setState(475);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(472);
					branchCase();
					}
					}
					setState(477);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(478);
				match(RBRACE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(479);
				match(RETURN);
				setState(481);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
					{
					setState(480);
					expression(0);
					}
				}

				setState(483);
				match(SEMI);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(484);
				match(THROW);
				setState(485);
				expression(0);
				setState(486);
				match(SEMI);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(488);
				match(SEMI);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(489);
				methodCall();
				setState(490);
				match(SEMI);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(492);
				functionCall();
				setState(493);
				match(SEMI);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(495);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 30064771072L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(496);
				creator();
				setState(497);
				match(SEMI);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(499);
				match(ALLOCATE);
				setState(500);
				allocator();
				setState(501);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(503);
				select();
				setState(504);
				match(SEMI);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(506);
				match(LPAREN);
				setState(507);
				((StatementContext)_localctx).castType = typeType(0);
				setState(508);
				match(RPAREN);
				setState(509);
				expression(0);
				setState(510);
				match(SEMI);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(512);
				_la = _input.LA(1);
				if ( !(_la==THIS || _la==IDENTIFIER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(513);
				match(DOT);
				setState(514);
				match(IDENTIFIER);
				setState(515);
				((StatementContext)_localctx).bop = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 54)) & ~0x3f) == 0 && ((1L << (_la - 54)) & 17171480577L) != 0)) ) {
					((StatementContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(516);
				expression(0);
				setState(517);
				match(SEMI);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(519);
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
		enterRule(_localctx, 70, RULE_allocator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			classOrInterfaceType();
			setState(523);
			match(LBRACE);
			setState(525);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(524);
				allocatorFieldList();
				}
			}

			setState(527);
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
		enterRule(_localctx, 72, RULE_allocatorFieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(529);
			allocatorField();
			setState(534);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(530);
				match(COMMA);
				setState(531);
				allocatorField();
				}
				}
				setState(536);
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
		enterRule(_localctx, 74, RULE_allocatorField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(537);
			match(IDENTIFIER);
			setState(538);
			match(COLON);
			setState(539);
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
		enterRule(_localctx, 76, RULE_select);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(541);
			_la = _input.LA(1);
			if ( !(_la==SELECT || _la==SELECT_FIRST) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(542);
			match(LPAREN);
			setState(543);
			qualifiedName();
			setState(544);
			match(DOT);
			setState(545);
			match(IDENTIFIER);
			setState(550);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(546);
				match(COMMA);
				setState(547);
				expression(0);
				}
				}
				setState(552);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(553);
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
		enterRule(_localctx, 78, RULE_forControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(556);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1829587885699140L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217745L) != 0)) {
				{
				setState(555);
				loopVariableDeclarators();
				}
			}

			setState(558);
			match(SEMI);
			setState(560);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
				{
				setState(559);
				expression(0);
				}
			}

			setState(562);
			match(SEMI);
			setState(564);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(563);
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
		enterRule(_localctx, 80, RULE_loopVariableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(566);
			loopVariableDeclarator();
			setState(571);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(567);
				match(COMMA);
				setState(568);
				loopVariableDeclarator();
				}
				}
				setState(573);
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
		enterRule(_localctx, 82, RULE_loopVariableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(574);
			typeType(0);
			setState(575);
			match(IDENTIFIER);
			setState(576);
			match(ASSIGN);
			setState(577);
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
		enterRule(_localctx, 84, RULE_loopVariableUpdates);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(579);
			loopVariableUpdate();
			setState(584);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(580);
				match(COMMA);
				setState(581);
				loopVariableUpdate();
				}
				}
				setState(586);
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
		enterRule(_localctx, 86, RULE_loopVariableUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(587);
			match(IDENTIFIER);
			setState(588);
			match(ASSIGN);
			setState(589);
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
		enterRule(_localctx, 88, RULE_qualifiedFieldName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(591);
			qualifiedName();
			setState(592);
			match(DOT);
			setState(593);
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
		enterRule(_localctx, 90, RULE_creator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(596);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(595);
				typeArguments();
				}
			}

			setState(598);
			classOrInterfaceType();
			setState(599);
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
		enterRule(_localctx, 92, RULE_arrayCreatorRest);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(601);
			match(LBRACK);
			setState(602);
			match(RBRACK);
			setState(607);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(603);
				match(LBRACK);
				setState(604);
				match(RBRACK);
				}
				}
				setState(609);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(610);
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
		enterRule(_localctx, 94, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(612);
			match(LBRACE);
			setState(624);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049993371663L) != 0)) {
				{
				setState(613);
				variableInitializer();
				setState(618);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(614);
						match(COMMA);
						setState(615);
						variableInitializer();
						}
						} 
					}
					setState(620);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
				}
				setState(622);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(621);
					match(COMMA);
					}
				}

				}
			}

			setState(626);
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
		enterRule(_localctx, 96, RULE_variableInitializer);
		try {
			setState(630);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(628);
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
				setState(629);
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
		enterRule(_localctx, 98, RULE_createdName);
		int _la;
		try {
			setState(637);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(632);
				match(IDENTIFIER);
				setState(634);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(633);
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
			case VOID:
				enterOuterAlt(_localctx, 2);
				{
				setState(636);
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
		enterRule(_localctx, 100, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(639);
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
		enterRule(_localctx, 102, RULE_catchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(641);
			match(CATCH);
			setState(642);
			match(LBRACE);
			setState(644);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(643);
				catchFields();
				}
			}

			setState(646);
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
		enterRule(_localctx, 104, RULE_catchFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(648);
			catchField();
			setState(653);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(649);
				match(COMMA);
				setState(650);
				catchField();
				}
				}
				setState(655);
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
		enterRule(_localctx, 106, RULE_catchField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(656);
			match(IDENTIFIER);
			setState(657);
			match(COLON);
			setState(658);
			match(LBRACE);
			setState(664);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(659);
				catchValue();
				setState(660);
				match(COMMA);
				}
				}
				setState(666);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(667);
			match(DEFAULT);
			setState(668);
			match(COLON);
			setState(669);
			expression(0);
			setState(670);
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
		enterRule(_localctx, 108, RULE_catchValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(672);
			match(IDENTIFIER);
			setState(673);
			match(COLON);
			setState(674);
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
		enterRule(_localctx, 110, RULE_branchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(676);
			switchLabel();
			setState(677);
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
		enterRule(_localctx, 112, RULE_switchLabel);
		try {
			setState(685);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(679);
				match(CASE);
				{
				setState(680);
				expression(0);
				}
				setState(681);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(683);
				match(DEFAULT);
				setState(684);
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
		enterRule(_localctx, 114, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(687);
			match(LPAREN);
			setState(688);
			expression(0);
			setState(689);
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
		enterRule(_localctx, 116, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(691);
			expression(0);
			setState(696);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(692);
				match(COMMA);
				setState(693);
				expression(0);
				}
				}
				setState(698);
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
		int _startState = 118;
		enterRecursionRule(_localctx, 118, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(707);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				{
				setState(700);
				primary();
				}
				break;
			case 2:
				{
				setState(701);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 15L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(702);
				expression(16);
				}
				break;
			case 3:
				{
				setState(703);
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
				setState(704);
				expression(15);
				}
				break;
			case 4:
				{
				setState(705);
				match(IDENTIFIER);
				setState(706);
				arguments();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(774);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(772);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(709);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(710);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 35L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(711);
						expression(15);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(712);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(713);
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
						setState(714);
						expression(14);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(715);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(723);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
						case 1:
							{
							setState(716);
							match(LT);
							setState(717);
							match(LT);
							}
							break;
						case 2:
							{
							setState(718);
							match(GT);
							setState(719);
							match(GT);
							setState(720);
							match(GT);
							}
							break;
						case 3:
							{
							setState(721);
							match(GT);
							setState(722);
							match(GT);
							}
							break;
						}
						setState(725);
						expression(13);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(726);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(727);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & -4503599627370496000L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(728);
						expression(12);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(729);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(730);
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
						setState(731);
						expression(10);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(732);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(733);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(734);
						expression(9);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(735);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(736);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(737);
						expression(8);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(738);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(739);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(740);
						expression(7);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(741);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(742);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(743);
						expression(6);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(744);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(745);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(746);
						expression(5);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(747);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(748);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(749);
						expression(0);
						setState(750);
						match(COLON);
						setState(751);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(753);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(754);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(755);
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
						setState(756);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(757);
						match(LBRACK);
						setState(758);
						expression(0);
						setState(759);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(761);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(762);
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
						setState(763);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(764);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(765);
						typeType(0);
						}
						break;
					case 16:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(766);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(767);
						match(COLONCOLON);
						setState(769);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(768);
							typeArguments();
							}
						}

						setState(771);
						match(IDENTIFIER);
						}
						break;
					}
					} 
				}
				setState(776);
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
		enterRule(_localctx, 120, RULE_primary);
		try {
			setState(784);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(777);
				match(LPAREN);
				setState(778);
				expression(0);
				setState(779);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(781);
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
				setState(782);
				literal();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 4);
				{
				setState(783);
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
		enterRule(_localctx, 122, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(786);
			typeArguments();
			setState(787);
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
		enterRule(_localctx, 124, RULE_explicitGenericInvocationSuffix);
		try {
			setState(793);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(789);
				match(SUPER);
				setState(790);
				superSuffix();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(791);
				match(IDENTIFIER);
				setState(792);
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
		enterRule(_localctx, 126, RULE_superSuffix);
		int _la;
		try {
			setState(804);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(795);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(796);
				match(DOT);
				setState(798);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(797);
					typeArguments();
					}
				}

				setState(800);
				match(IDENTIFIER);
				setState(802);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(801);
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
		enterRule(_localctx, 128, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(806);
			match(LPAREN);
			setState(808);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
				{
				setState(807);
				expressionList();
				}
			}

			setState(810);
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
		enterRule(_localctx, 130, RULE_classType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(812);
			match(IDENTIFIER);
			setState(814);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(813);
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
		enterRule(_localctx, 132, RULE_methodCall);
		int _la;
		try {
			setState(840);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(816);
				expression(0);
				setState(817);
				match(DOT);
				setState(819);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(818);
					typeArguments();
					}
				}

				setState(821);
				match(IDENTIFIER);
				setState(822);
				match(LPAREN);
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
					{
					setState(823);
					expressionList();
					}
				}

				setState(826);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(828);
				match(THIS);
				setState(829);
				match(LPAREN);
				setState(831);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
					{
					setState(830);
					expressionList();
					}
				}

				setState(833);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(834);
				match(SUPER);
				setState(835);
				match(LPAREN);
				setState(837);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
					{
					setState(836);
					expressionList();
					}
				}

				setState(839);
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
		enterRule(_localctx, 134, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(842);
			expression(0);
			setState(843);
			match(LPAREN);
			setState(845);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 432354360320598016L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 299049984983055L) != 0)) {
				{
				setState(844);
				expressionList();
				}
			}

			setState(847);
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
			setState(856);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(849);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(850);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(851);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(852);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(853);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(854);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(855);
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
			setState(858);
			_la = _input.LA(1);
			if ( !(((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & 15L) != 0)) ) {
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
			setState(860);
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
		enterRule(_localctx, 142, RULE_typeTypeOrVoid);
		try {
			setState(864);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(862);
				typeType(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(863);
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
		int _startState = 144;
		enterRecursionRule(_localctx, 144, RULE_typeType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(891);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(867);
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
			case VOID:
				{
				setState(868);
				primitiveType();
				}
				break;
			case ANY:
				{
				setState(869);
				match(ANY);
				}
				break;
			case NEVER:
				{
				setState(870);
				match(NEVER);
				}
				break;
			case LPAREN:
				{
				setState(871);
				match(LPAREN);
				setState(880);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1829587885699140L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217745L) != 0)) {
					{
					setState(872);
					typeType(0);
					setState(877);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(873);
						match(COMMA);
						setState(874);
						typeType(0);
						}
						}
						setState(879);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(882);
				match(RPAREN);
				setState(883);
				match(ARROW);
				setState(884);
				typeType(2);
				}
				break;
			case LBRACK:
				{
				setState(885);
				match(LBRACK);
				setState(886);
				typeType(0);
				setState(887);
				match(COMMA);
				setState(888);
				typeType(0);
				setState(889);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(911);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(909);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
					case 1:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(893);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(896); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(894);
								match(BITOR);
								setState(895);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(898); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(900);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(903); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(901);
								match(BITAND);
								setState(902);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(905); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,91,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(907);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(908);
						arrayKind();
						}
						break;
					}
					} 
				}
				setState(913);
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
			setState(914);
			_la = _input.LA(1);
			if ( !(((((_la - 111)) & ~0x3f) == 0 && ((1L << (_la - 111)) & 15L) != 0)) ) {
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
			setState(916);
			qualifiedName();
			setState(918);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				{
				setState(917);
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
			setState(920);
			match(LT);
			setState(921);
			typeType(0);
			setState(926);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(922);
				match(COMMA);
				setState(923);
				typeType(0);
				}
				}
				setState(928);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(929);
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
			setState(931);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 140738025435204L) != 0)) ) {
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
			setState(940);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
				enterOuterAlt(_localctx, 1);
				{
				setState(933);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(934);
				match(NATIVE);
				}
				break;
			case READONLY:
				enterOuterAlt(_localctx, 3);
				{
				setState(935);
				match(READONLY);
				}
				break;
			case CHILD:
				enterOuterAlt(_localctx, 4);
				{
				setState(936);
				match(CHILD);
				}
				break;
			case TITLE:
				enterOuterAlt(_localctx, 5);
				{
				setState(937);
				match(TITLE);
				}
				break;
			case UNIQUE:
				enterOuterAlt(_localctx, 6);
				{
				setState(938);
				match(UNIQUE);
				}
				break;
			case DELETED:
				enterOuterAlt(_localctx, 7);
				{
				setState(939);
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
			setState(942);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1580547964930L) != 0)) ) {
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
		enterRule(_localctx, 158, RULE_lambda);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(944);
			lambdaParameters();
			setState(945);
			match(COLON);
			setState(946);
			typeTypeOrVoid();
			setState(947);
			match(ARROW);
			setState(948);
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
			setState(950);
			match(LPAREN);
			setState(952);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1829587885699140L) != 0) || ((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 134217745L) != 0)) {
				{
				setState(951);
				formalParameterList();
				}
			}

			setState(954);
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
			setState(956);
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
			setState(958);
			match(INDEX);
			setState(959);
			match(IDENTIFIER);
			setState(960);
			match(LBRACE);
			setState(964);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(961);
				indexField();
				}
				}
				setState(966);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(967);
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
			setState(969);
			match(IDENTIFIER);
			setState(970);
			match(COLON);
			setState(971);
			expression(0);
			setState(972);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 59:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 72:
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
		"\u0004\u0001v\u03cf\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0001\u0000\u0003\u0000"+
		"\u00aa\b\u0000\u0001\u0000\u0005\u0000\u00ad\b\u0000\n\u0000\f\u0000\u00b0"+
		"\t\u0000\u0001\u0000\u0004\u0000\u00b3\b\u0000\u000b\u0000\f\u0000\u00b4"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0005\u0003\u00c0\b\u0003\n\u0003"+
		"\f\u0003\u00c3\t\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"\u00c8\b\u0003\u0001\u0003\u0003\u0003\u00cb\b\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004\u00d0\b\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004\u00d4\b\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u00d8\b\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0005\u0005\u00de\b\u0005"+
		"\n\u0005\f\u0005\u00e1\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0005\u0006\u00e8\b\u0006\n\u0006\f\u0006\u00eb\t\u0006"+
		"\u0001\u0007\u0005\u0007\u00ee\b\u0007\n\u0007\f\u0007\u00f1\t\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b\u00f9\b\b\u0001"+
		"\b\u0001\b\u0003\b\u00fd\b\b\u0001\b\u0003\b\u0100\b\b\u0001\b\u0003\b"+
		"\u0103\b\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0005\t\u010a\b\t\n"+
		"\t\f\t\u010d\t\t\u0001\n\u0001\n\u0003\n\u0111\b\n\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u0115\b\u000b\n\u000b\f\u000b\u0118\t\u000b\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u011d\b\f\u0001\f\u0001\f\u0003\f\u0121\b\f\u0001\f"+
		"\u0001\f\u0001\r\u0001\r\u0005\r\u0127\b\r\n\r\f\r\u012a\t\r\u0001\r\u0001"+
		"\r\u0001\u000e\u0005\u000e\u012f\b\u000e\n\u000e\f\u000e\u0132\t\u000e"+
		"\u0001\u000e\u0001\u000e\u0003\u000e\u0136\b\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0005\u0010\u013b\b\u0010\n\u0010\f\u0010\u013e\t\u0010\u0001"+
		"\u0010\u0003\u0010\u0141\b\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0005"+
		"\u0012\u014c\b\u0012\n\u0012\f\u0012\u014f\t\u0012\u0001\u0012\u0001\u0012"+
		"\u0003\u0012\u0153\b\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u015b\b\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0015\u0003\u0015\u0162\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0003\u0016"+
		"\u016a\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016"+
		"\u0170\b\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0005\u0017\u0178\b\u0017\n\u0017\f\u0017\u017b\t\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0182"+
		"\b\u0018\n\u0018\f\u0018\u0185\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0005\u0019\u018a\b\u0019\n\u0019\f\u0019\u018d\t\u0019\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0003\u001a\u0192\b\u001a\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u0196\b\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0005\u001c\u019d\b\u001c\n\u001c\f\u001c\u01a0\t\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u01a7\b\u001d"+
		"\n\u001d\f\u001d\u01aa\t\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001f\u0001\u001f\u0003\u001f\u01b1\b\u001f\u0001 \u0001 \u0005 \u01b5"+
		"\b \n \f \u01b8\t \u0001 \u0001 \u0001!\u0001!\u0003!\u01be\b!\u0001!"+
		"\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\"\u01d1"+
		"\b\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u01da"+
		"\b\"\n\"\f\"\u01dd\t\"\u0001\"\u0001\"\u0001\"\u0003\"\u01e2\b\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0003\"\u0209\b\"\u0001#\u0001#\u0001#\u0003#\u020e\b#\u0001#\u0001"+
		"#\u0001$\u0001$\u0001$\u0005$\u0215\b$\n$\f$\u0218\t$\u0001%\u0001%\u0001"+
		"%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0005&\u0225"+
		"\b&\n&\f&\u0228\t&\u0001&\u0001&\u0001\'\u0003\'\u022d\b\'\u0001\'\u0001"+
		"\'\u0003\'\u0231\b\'\u0001\'\u0001\'\u0003\'\u0235\b\'\u0001(\u0001(\u0001"+
		"(\u0005(\u023a\b(\n(\f(\u023d\t(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001"+
		"*\u0001*\u0001*\u0005*\u0247\b*\n*\f*\u024a\t*\u0001+\u0001+\u0001+\u0001"+
		"+\u0001,\u0001,\u0001,\u0001,\u0001-\u0003-\u0255\b-\u0001-\u0001-\u0001"+
		"-\u0001.\u0001.\u0001.\u0001.\u0005.\u025e\b.\n.\f.\u0261\t.\u0001.\u0001"+
		".\u0001/\u0001/\u0001/\u0001/\u0005/\u0269\b/\n/\f/\u026c\t/\u0001/\u0003"+
		"/\u026f\b/\u0003/\u0271\b/\u0001/\u0001/\u00010\u00010\u00030\u0277\b"+
		"0\u00011\u00011\u00031\u027b\b1\u00011\u00031\u027e\b1\u00012\u00012\u0001"+
		"3\u00013\u00013\u00033\u0285\b3\u00013\u00013\u00014\u00014\u00014\u0005"+
		"4\u028c\b4\n4\f4\u028f\t4\u00015\u00015\u00015\u00015\u00015\u00015\u0005"+
		"5\u0297\b5\n5\f5\u029a\t5\u00015\u00015\u00015\u00015\u00015\u00016\u0001"+
		"6\u00016\u00016\u00017\u00017\u00017\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00038\u02ae\b8\u00019\u00019\u00019\u00019\u0001:\u0001:\u0001"+
		":\u0005:\u02b7\b:\n:\f:\u02ba\t:\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0003;\u02c4\b;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0003;\u02d4"+
		"\b;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0003;\u0302\b;\u0001;\u0005;\u0305\b;\n"+
		";\f;\u0308\t;\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<"+
		"\u0311\b<\u0001=\u0001=\u0001=\u0001>\u0001>\u0001>\u0001>\u0003>\u031a"+
		"\b>\u0001?\u0001?\u0001?\u0003?\u031f\b?\u0001?\u0001?\u0003?\u0323\b"+
		"?\u0003?\u0325\b?\u0001@\u0001@\u0003@\u0329\b@\u0001@\u0001@\u0001A\u0001"+
		"A\u0003A\u032f\bA\u0001B\u0001B\u0001B\u0003B\u0334\bB\u0001B\u0001B\u0001"+
		"B\u0003B\u0339\bB\u0001B\u0001B\u0001B\u0001B\u0001B\u0003B\u0340\bB\u0001"+
		"B\u0001B\u0001B\u0001B\u0003B\u0346\bB\u0001B\u0003B\u0349\bB\u0001C\u0001"+
		"C\u0001C\u0003C\u034e\bC\u0001C\u0001C\u0001D\u0001D\u0001D\u0001D\u0001"+
		"D\u0001D\u0001D\u0003D\u0359\bD\u0001E\u0001E\u0001F\u0001F\u0001G\u0001"+
		"G\u0003G\u0361\bG\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001"+
		"H\u0001H\u0005H\u036c\bH\nH\fH\u036f\tH\u0003H\u0371\bH\u0001H\u0001H"+
		"\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0003H\u037c\bH\u0001"+
		"H\u0001H\u0001H\u0004H\u0381\bH\u000bH\fH\u0382\u0001H\u0001H\u0001H\u0004"+
		"H\u0388\bH\u000bH\fH\u0389\u0001H\u0001H\u0005H\u038e\bH\nH\fH\u0391\t"+
		"H\u0001I\u0001I\u0001J\u0001J\u0003J\u0397\bJ\u0001K\u0001K\u0001K\u0001"+
		"K\u0005K\u039d\bK\nK\fK\u03a0\tK\u0001K\u0001K\u0001L\u0001L\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0003M\u03ad\bM\u0001N\u0001N\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001P\u0001P\u0003P\u03b9\bP\u0001"+
		"P\u0001P\u0001Q\u0001Q\u0001R\u0001R\u0001R\u0001R\u0005R\u03c3\bR\nR"+
		"\fR\u03c6\tR\u0001R\u0001R\u0001S\u0001S\u0001S\u0001S\u0001S\u0001S\u0000"+
		"\u0002v\u0090T\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprt"+
		"vxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094"+
		"\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u0000\u0012\u0002"+
		"\u0000\u0007\b\u000b\u000b\u0004\u0000\u0001\u0001\u0004\u0004&&((\u0001"+
		"\u0000 \"\u0002\u0000++ss\u0002\u000066MW\u0001\u000034\u0001\u0000CF"+
		"\u0001\u00009:\u0002\u0000GHLL\u0001\u0000EF\u0002\u000078>?\u0002\u0000"+
		"==@@\u0001\u0000CD\u0001\u0000eh\u0001\u0000ij\u0001\u0000or\u0006\u0000"+
		"\u0002\u0002\u0006\u0006\f\r\u0010\u0011\u001d\u001d//\u0003\u0000\u0001"+
		"\u0001$&((\u0411\u0000\u00a9\u0001\u0000\u0000\u0000\u0002\u00b6\u0001"+
		"\u0000\u0000\u0000\u0004\u00ba\u0001\u0000\u0000\u0000\u0006\u00ca\u0001"+
		"\u0000\u0000\u0000\b\u00cc\u0001\u0000\u0000\u0000\n\u00db\u0001\u0000"+
		"\u0000\u0000\f\u00e4\u0001\u0000\u0000\u0000\u000e\u00ef\u0001\u0000\u0000"+
		"\u0000\u0010\u00f4\u0001\u0000\u0000\u0000\u0012\u0106\u0001\u0000\u0000"+
		"\u0000\u0014\u010e\u0001\u0000\u0000\u0000\u0016\u0112\u0001\u0000\u0000"+
		"\u0000\u0018\u0119\u0001\u0000\u0000\u0000\u001a\u0124\u0001\u0000\u0000"+
		"\u0000\u001c\u0135\u0001\u0000\u0000\u0000\u001e\u0137\u0001\u0000\u0000"+
		"\u0000 \u013c\u0001\u0000\u0000\u0000\"\u0144\u0001\u0000\u0000\u0000"+
		"$\u0146\u0001\u0000\u0000\u0000&\u015a\u0001\u0000\u0000\u0000(\u015c"+
		"\u0001\u0000\u0000\u0000*\u0161\u0001\u0000\u0000\u0000,\u0169\u0001\u0000"+
		"\u0000\u0000.\u0173\u0001\u0000\u0000\u00000\u017e\u0001\u0000\u0000\u0000"+
		"2\u0186\u0001\u0000\u0000\u00004\u018e\u0001\u0000\u0000\u00006\u0193"+
		"\u0001\u0000\u0000\u00008\u0199\u0001\u0000\u0000\u0000:\u01a3\u0001\u0000"+
		"\u0000\u0000<\u01ab\u0001\u0000\u0000\u0000>\u01b0\u0001\u0000\u0000\u0000"+
		"@\u01b2\u0001\u0000\u0000\u0000B\u01bd\u0001\u0000\u0000\u0000D\u0208"+
		"\u0001\u0000\u0000\u0000F\u020a\u0001\u0000\u0000\u0000H\u0211\u0001\u0000"+
		"\u0000\u0000J\u0219\u0001\u0000\u0000\u0000L\u021d\u0001\u0000\u0000\u0000"+
		"N\u022c\u0001\u0000\u0000\u0000P\u0236\u0001\u0000\u0000\u0000R\u023e"+
		"\u0001\u0000\u0000\u0000T\u0243\u0001\u0000\u0000\u0000V\u024b\u0001\u0000"+
		"\u0000\u0000X\u024f\u0001\u0000\u0000\u0000Z\u0254\u0001\u0000\u0000\u0000"+
		"\\\u0259\u0001\u0000\u0000\u0000^\u0264\u0001\u0000\u0000\u0000`\u0276"+
		"\u0001\u0000\u0000\u0000b\u027d\u0001\u0000\u0000\u0000d\u027f\u0001\u0000"+
		"\u0000\u0000f\u0281\u0001\u0000\u0000\u0000h\u0288\u0001\u0000\u0000\u0000"+
		"j\u0290\u0001\u0000\u0000\u0000l\u02a0\u0001\u0000\u0000\u0000n\u02a4"+
		"\u0001\u0000\u0000\u0000p\u02ad\u0001\u0000\u0000\u0000r\u02af\u0001\u0000"+
		"\u0000\u0000t\u02b3\u0001\u0000\u0000\u0000v\u02c3\u0001\u0000\u0000\u0000"+
		"x\u0310\u0001\u0000\u0000\u0000z\u0312\u0001\u0000\u0000\u0000|\u0319"+
		"\u0001\u0000\u0000\u0000~\u0324\u0001\u0000\u0000\u0000\u0080\u0326\u0001"+
		"\u0000\u0000\u0000\u0082\u032c\u0001\u0000\u0000\u0000\u0084\u0348\u0001"+
		"\u0000\u0000\u0000\u0086\u034a\u0001\u0000\u0000\u0000\u0088\u0358\u0001"+
		"\u0000\u0000\u0000\u008a\u035a\u0001\u0000\u0000\u0000\u008c\u035c\u0001"+
		"\u0000\u0000\u0000\u008e\u0360\u0001\u0000\u0000\u0000\u0090\u037b\u0001"+
		"\u0000\u0000\u0000\u0092\u0392\u0001\u0000\u0000\u0000\u0094\u0394\u0001"+
		"\u0000\u0000\u0000\u0096\u0398\u0001\u0000\u0000\u0000\u0098\u03a3\u0001"+
		"\u0000\u0000\u0000\u009a\u03ac\u0001\u0000\u0000\u0000\u009c\u03ae\u0001"+
		"\u0000\u0000\u0000\u009e\u03b0\u0001\u0000\u0000\u0000\u00a0\u03b6\u0001"+
		"\u0000\u0000\u0000\u00a2\u03bc\u0001\u0000\u0000\u0000\u00a4\u03be\u0001"+
		"\u0000\u0000\u0000\u00a6\u03c9\u0001\u0000\u0000\u0000\u00a8\u00aa\u0003"+
		"\u0002\u0001\u0000\u00a9\u00a8\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001"+
		"\u0000\u0000\u0000\u00aa\u00ae\u0001\u0000\u0000\u0000\u00ab\u00ad\u0003"+
		"\u0004\u0002\u0000\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ad\u00b0\u0001"+
		"\u0000\u0000\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000\u00ae\u00af\u0001"+
		"\u0000\u0000\u0000\u00af\u00b2\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001"+
		"\u0000\u0000\u0000\u00b1\u00b3\u0003\u0006\u0003\u0000\u00b2\u00b1\u0001"+
		"\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001"+
		"\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u0001\u0001"+
		"\u0000\u0000\u0000\u00b6\u00b7\u0005\u000e\u0000\u0000\u00b7\u00b8\u0003"+
		"2\u0019\u0000\u00b8\u00b9\u0005^\u0000\u0000\u00b9\u0003\u0001\u0000\u0000"+
		"\u0000\u00ba\u00bb\u0005\u000f\u0000\u0000\u00bb\u00bc\u00032\u0019\u0000"+
		"\u00bc\u00bd\u0005^\u0000\u0000\u00bd\u0005\u0001\u0000\u0000\u0000\u00be"+
		"\u00c0\u0003\u009cN\u0000\u00bf\u00be\u0001\u0000\u0000\u0000\u00c0\u00c3"+
		"\u0001\u0000\u0000\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2"+
		"\u0001\u0000\u0000\u0000\u00c2\u00c7\u0001\u0000\u0000\u0000\u00c3\u00c1"+
		"\u0001\u0000\u0000\u0000\u00c4\u00c8\u0003\b\u0004\u0000\u00c5\u00c8\u0003"+
		"\u0010\b\u0000\u00c6\u00c8\u0003\u0018\f\u0000\u00c7\u00c4\u0001\u0000"+
		"\u0000\u0000\u00c7\u00c5\u0001\u0000\u0000\u0000\u00c7\u00c6\u0001\u0000"+
		"\u0000\u0000\u00c8\u00cb\u0001\u0000\u0000\u0000\u00c9\u00cb\u0005^\u0000"+
		"\u0000\u00ca\u00c1\u0001\u0000\u0000\u0000\u00ca\u00c9\u0001\u0000\u0000"+
		"\u0000\u00cb\u0007\u0001\u0000\u0000\u0000\u00cc\u00cd\u0007\u0000\u0000"+
		"\u0000\u00cd\u00cf\u0005s\u0000\u0000\u00ce\u00d0\u0003.\u0017\u0000\u00cf"+
		"\u00ce\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0"+
		"\u00d3\u0001\u0000\u0000\u0000\u00d1\u00d2\u0005\u0014\u0000\u0000\u00d2"+
		"\u00d4\u0003\u0090H\u0000\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d3\u00d4"+
		"\u0001\u0000\u0000\u0000\u00d4\u00d7\u0001\u0000\u0000\u0000\u00d5\u00d6"+
		"\u0005\u001b\u0000\u0000\u00d6\u00d8\u0003\f\u0006\u0000\u00d7\u00d5\u0001"+
		"\u0000\u0000\u0000\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8\u00d9\u0001"+
		"\u0000\u0000\u0000\u00d9\u00da\u0003\n\u0005\u0000\u00da\t\u0001\u0000"+
		"\u0000\u0000\u00db\u00df\u0005Z\u0000\u0000\u00dc\u00de\u0003\u000e\u0007"+
		"\u0000\u00dd\u00dc\u0001\u0000\u0000\u0000\u00de\u00e1\u0001\u0000\u0000"+
		"\u0000\u00df\u00dd\u0001\u0000\u0000\u0000\u00df\u00e0\u0001\u0000\u0000"+
		"\u0000\u00e0\u00e2\u0001\u0000\u0000\u0000\u00e1\u00df\u0001\u0000\u0000"+
		"\u0000\u00e2\u00e3\u0005[\u0000\u0000\u00e3\u000b\u0001\u0000\u0000\u0000"+
		"\u00e4\u00e9\u0003\u0090H\u0000\u00e5\u00e6\u0005_\u0000\u0000\u00e6\u00e8"+
		"\u0003\u0090H\u0000\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e8\u00eb\u0001"+
		"\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00e9\u00ea\u0001"+
		"\u0000\u0000\u0000\u00ea\r\u0001\u0000\u0000\u0000\u00eb\u00e9\u0001\u0000"+
		"\u0000\u0000\u00ec\u00ee\u0003\u009aM\u0000\u00ed\u00ec\u0001\u0000\u0000"+
		"\u0000\u00ee\u00f1\u0001\u0000\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000"+
		"\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u00f2\u0001\u0000\u0000"+
		"\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f2\u00f3\u0003&\u0013\u0000"+
		"\u00f3\u000f\u0001\u0000\u0000\u0000\u00f4\u00f5\u0005\u0013\u0000\u0000"+
		"\u00f5\u00f8\u0005s\u0000\u0000\u00f6\u00f7\u0005\u001b\u0000\u0000\u00f7"+
		"\u00f9\u0003\f\u0006\u0000\u00f8\u00f6\u0001\u0000\u0000\u0000\u00f8\u00f9"+
		"\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa\u00fc"+
		"\u0005Z\u0000\u0000\u00fb\u00fd\u0003\u0012\t\u0000\u00fc\u00fb\u0001"+
		"\u0000\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd\u00ff\u0001"+
		"\u0000\u0000\u0000\u00fe\u0100\u0005_\u0000\u0000\u00ff\u00fe\u0001\u0000"+
		"\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100\u0102\u0001\u0000"+
		"\u0000\u0000\u0101\u0103\u0003\u0016\u000b\u0000\u0102\u0101\u0001\u0000"+
		"\u0000\u0000\u0102\u0103\u0001\u0000\u0000\u0000\u0103\u0104\u0001\u0000"+
		"\u0000\u0000\u0104\u0105\u0005[\u0000\u0000\u0105\u0011\u0001\u0000\u0000"+
		"\u0000\u0106\u010b\u0003\u0014\n\u0000\u0107\u0108\u0005_\u0000\u0000"+
		"\u0108\u010a\u0003\u0014\n\u0000\u0109\u0107\u0001\u0000\u0000\u0000\u010a"+
		"\u010d\u0001\u0000\u0000\u0000\u010b\u0109\u0001\u0000\u0000\u0000\u010b"+
		"\u010c\u0001\u0000\u0000\u0000\u010c\u0013\u0001\u0000\u0000\u0000\u010d"+
		"\u010b\u0001\u0000\u0000\u0000\u010e\u0110\u0005s\u0000\u0000\u010f\u0111"+
		"\u0003\u0080@\u0000\u0110\u010f\u0001\u0000\u0000\u0000\u0110\u0111\u0001"+
		"\u0000\u0000\u0000\u0111\u0015\u0001\u0000\u0000\u0000\u0112\u0116\u0005"+
		"^\u0000\u0000\u0113\u0115\u0003\u000e\u0007\u0000\u0114\u0113\u0001\u0000"+
		"\u0000\u0000\u0115\u0118\u0001\u0000\u0000\u0000\u0116\u0114\u0001\u0000"+
		"\u0000\u0000\u0116\u0117\u0001\u0000\u0000\u0000\u0117\u0017\u0001\u0000"+
		"\u0000\u0000\u0118\u0116\u0001\u0000\u0000\u0000\u0119\u011a\u0005\u001e"+
		"\u0000\u0000\u011a\u011c\u0005s\u0000\u0000\u011b\u011d\u0003.\u0017\u0000"+
		"\u011c\u011b\u0001\u0000\u0000\u0000\u011c\u011d\u0001\u0000\u0000\u0000"+
		"\u011d\u0120\u0001\u0000\u0000\u0000\u011e\u011f\u0005\u0014\u0000\u0000"+
		"\u011f\u0121\u0003\f\u0006\u0000\u0120\u011e\u0001\u0000\u0000\u0000\u0120"+
		"\u0121\u0001\u0000\u0000\u0000\u0121\u0122\u0001\u0000\u0000\u0000\u0122"+
		"\u0123\u0003\u001a\r\u0000\u0123\u0019\u0001\u0000\u0000\u0000\u0124\u0128"+
		"\u0005Z\u0000\u0000\u0125\u0127\u0003\u001c\u000e\u0000\u0126\u0125\u0001"+
		"\u0000\u0000\u0000\u0127\u012a\u0001\u0000\u0000\u0000\u0128\u0126\u0001"+
		"\u0000\u0000\u0000\u0128\u0129\u0001\u0000\u0000\u0000\u0129\u012b\u0001"+
		"\u0000\u0000\u0000\u012a\u0128\u0001\u0000\u0000\u0000\u012b\u012c\u0005"+
		"[\u0000\u0000\u012c\u001b\u0001\u0000\u0000\u0000\u012d\u012f\u0003\u009a"+
		"M\u0000\u012e\u012d\u0001\u0000\u0000\u0000\u012f\u0132\u0001\u0000\u0000"+
		"\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0130\u0131\u0001\u0000\u0000"+
		"\u0000\u0131\u0133\u0001\u0000\u0000\u0000\u0132\u0130\u0001\u0000\u0000"+
		"\u0000\u0133\u0136\u0003\u001e\u000f\u0000\u0134\u0136\u0005^\u0000\u0000"+
		"\u0135\u0130\u0001\u0000\u0000\u0000\u0135\u0134\u0001\u0000\u0000\u0000"+
		"\u0136\u001d\u0001\u0000\u0000\u0000\u0137\u0138\u0003 \u0010\u0000\u0138"+
		"\u001f\u0001\u0000\u0000\u0000\u0139\u013b\u0003\"\u0011\u0000\u013a\u0139"+
		"\u0001\u0000\u0000\u0000\u013b\u013e\u0001\u0000\u0000\u0000\u013c\u013a"+
		"\u0001\u0000\u0000\u0000\u013c\u013d\u0001\u0000\u0000\u0000\u013d\u0140"+
		"\u0001\u0000\u0000\u0000\u013e\u013c\u0001\u0000\u0000\u0000\u013f\u0141"+
		"\u0003.\u0017\u0000\u0140\u013f\u0001\u0000\u0000\u0000\u0140\u0141\u0001"+
		"\u0000\u0000\u0000\u0141\u0142\u0001\u0000\u0000\u0000\u0142\u0143\u0003"+
		"$\u0012\u0000\u0143!\u0001\u0000\u0000\u0000\u0144\u0145\u0007\u0001\u0000"+
		"\u0000\u0145#\u0001\u0000\u0000\u0000\u0146\u0147\u0003\u008eG\u0000\u0147"+
		"\u0148\u0005s\u0000\u0000\u0148\u014d\u00036\u001b\u0000\u0149\u014a\u0005"+
		"\\\u0000\u0000\u014a\u014c\u0005]\u0000\u0000\u014b\u0149\u0001\u0000"+
		"\u0000\u0000\u014c\u014f\u0001\u0000\u0000\u0000\u014d\u014b\u0001\u0000"+
		"\u0000\u0000\u014d\u014e\u0001\u0000\u0000\u0000\u014e\u0152\u0001\u0000"+
		"\u0000\u0000\u014f\u014d\u0001\u0000\u0000\u0000\u0150\u0151\u0005-\u0000"+
		"\u0000\u0151\u0153\u00030\u0018\u0000\u0152\u0150\u0001\u0000\u0000\u0000"+
		"\u0152\u0153\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000"+
		"\u0154\u0155\u0005^\u0000\u0000\u0155%\u0001\u0000\u0000\u0000\u0156\u015b"+
		"\u0003*\u0015\u0000\u0157\u015b\u0003(\u0014\u0000\u0158\u015b\u0003,"+
		"\u0016\u0000\u0159\u015b\u0003\u00a4R\u0000\u015a\u0156\u0001\u0000\u0000"+
		"\u0000\u015a\u0157\u0001\u0000\u0000\u0000\u015a\u0158\u0001\u0000\u0000"+
		"\u0000\u015a\u0159\u0001\u0000\u0000\u0000\u015b\'\u0001\u0000\u0000\u0000"+
		"\u015c\u015d\u0003\u0090H\u0000\u015d\u015e\u0005s\u0000\u0000\u015e\u015f"+
		"\u0005^\u0000\u0000\u015f)\u0001\u0000\u0000\u0000\u0160\u0162\u0003."+
		"\u0017\u0000\u0161\u0160\u0001\u0000\u0000\u0000\u0161\u0162\u0001\u0000"+
		"\u0000\u0000\u0162\u0163\u0001\u0000\u0000\u0000\u0163\u0164\u0003\u008e"+
		"G\u0000\u0164\u0165\u0005s\u0000\u0000\u0165\u0166\u00036\u001b\u0000"+
		"\u0166\u0167\u0003>\u001f\u0000\u0167+\u0001\u0000\u0000\u0000\u0168\u016a"+
		"\u0003.\u0017\u0000\u0169\u0168\u0001\u0000\u0000\u0000\u0169\u016a\u0001"+
		"\u0000\u0000\u0000\u016a\u016b\u0001\u0000\u0000\u0000\u016b\u016c\u0005"+
		"s\u0000\u0000\u016c\u016f\u00036\u001b\u0000\u016d\u016e\u0005-\u0000"+
		"\u0000\u016e\u0170\u00030\u0018\u0000\u016f\u016d\u0001\u0000\u0000\u0000"+
		"\u016f\u0170\u0001\u0000\u0000\u0000\u0170\u0171\u0001\u0000\u0000\u0000"+
		"\u0171\u0172\u0003@ \u0000\u0172-\u0001\u0000\u0000\u0000\u0173\u0174"+
		"\u00058\u0000\u0000\u0174\u0179\u00034\u001a\u0000\u0175\u0176\u0005_"+
		"\u0000\u0000\u0176\u0178\u00034\u001a\u0000\u0177\u0175\u0001\u0000\u0000"+
		"\u0000\u0178\u017b\u0001\u0000\u0000\u0000\u0179\u0177\u0001\u0000\u0000"+
		"\u0000\u0179\u017a\u0001\u0000\u0000\u0000\u017a\u017c\u0001\u0000\u0000"+
		"\u0000\u017b\u0179\u0001\u0000\u0000\u0000\u017c\u017d\u00057\u0000\u0000"+
		"\u017d/\u0001\u0000\u0000\u0000\u017e\u0183\u00032\u0019\u0000\u017f\u0180"+
		"\u0005_\u0000\u0000\u0180\u0182\u00032\u0019\u0000\u0181\u017f\u0001\u0000"+
		"\u0000\u0000\u0182\u0185\u0001\u0000\u0000\u0000\u0183\u0181\u0001\u0000"+
		"\u0000\u0000\u0183\u0184\u0001\u0000\u0000\u0000\u01841\u0001\u0000\u0000"+
		"\u0000\u0185\u0183\u0001\u0000\u0000\u0000\u0186\u018b\u0005s\u0000\u0000"+
		"\u0187\u0188\u0005`\u0000\u0000\u0188\u018a\u0005s\u0000\u0000\u0189\u0187"+
		"\u0001\u0000\u0000\u0000\u018a\u018d\u0001\u0000\u0000\u0000\u018b\u0189"+
		"\u0001\u0000\u0000\u0000\u018b\u018c\u0001\u0000\u0000\u0000\u018c3\u0001"+
		"\u0000\u0000\u0000\u018d\u018b\u0001\u0000\u0000\u0000\u018e\u0191\u0005"+
		"s\u0000\u0000\u018f\u0190\u0005\u0014\u0000\u0000\u0190\u0192\u0003\u0090"+
		"H\u0000\u0191\u018f\u0001\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000"+
		"\u0000\u01925\u0001\u0000\u0000\u0000\u0193\u0195\u0005X\u0000\u0000\u0194"+
		"\u0196\u0003:\u001d\u0000\u0195\u0194\u0001\u0000\u0000\u0000\u0195\u0196"+
		"\u0001\u0000\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u0198"+
		"\u0005Y\u0000\u0000\u01987\u0001\u0000\u0000\u0000\u0199\u019e\u0003\u0090"+
		"H\u0000\u019a\u019b\u0005s\u0000\u0000\u019b\u019d\u0005`\u0000\u0000"+
		"\u019c\u019a\u0001\u0000\u0000\u0000\u019d\u01a0\u0001\u0000\u0000\u0000"+
		"\u019e\u019c\u0001\u0000\u0000\u0000\u019e\u019f\u0001\u0000\u0000\u0000"+
		"\u019f\u01a1\u0001\u0000\u0000\u0000\u01a0\u019e\u0001\u0000\u0000\u0000"+
		"\u01a1\u01a2\u0005+\u0000\u0000\u01a29\u0001\u0000\u0000\u0000\u01a3\u01a8"+
		"\u0003<\u001e\u0000\u01a4\u01a5\u0005_\u0000\u0000\u01a5\u01a7\u0003<"+
		"\u001e\u0000\u01a6\u01a4\u0001\u0000\u0000\u0000\u01a7\u01aa\u0001\u0000"+
		"\u0000\u0000\u01a8\u01a6\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001\u0000"+
		"\u0000\u0000\u01a9;\u0001\u0000\u0000\u0000\u01aa\u01a8\u0001\u0000\u0000"+
		"\u0000\u01ab\u01ac\u0003\u0090H\u0000\u01ac\u01ad\u0005s\u0000\u0000\u01ad"+
		"=\u0001\u0000\u0000\u0000\u01ae\u01b1\u0003@ \u0000\u01af\u01b1\u0005"+
		"^\u0000\u0000\u01b0\u01ae\u0001\u0000\u0000\u0000\u01b0\u01af\u0001\u0000"+
		"\u0000\u0000\u01b1?\u0001\u0000\u0000\u0000\u01b2\u01b6\u0005Z\u0000\u0000"+
		"\u01b3\u01b5\u0003B!\u0000\u01b4\u01b3\u0001\u0000\u0000\u0000\u01b5\u01b8"+
		"\u0001\u0000\u0000\u0000\u01b6\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b7"+
		"\u0001\u0000\u0000\u0000\u01b7\u01b9\u0001\u0000\u0000\u0000\u01b8\u01b6"+
		"\u0001\u0000\u0000\u0000\u01b9\u01ba\u0005[\u0000\u0000\u01baA\u0001\u0000"+
		"\u0000\u0000\u01bb\u01bc\u0005s\u0000\u0000\u01bc\u01be\u0005<\u0000\u0000"+
		"\u01bd\u01bb\u0001\u0000\u0000\u0000\u01bd\u01be\u0001\u0000\u0000\u0000"+
		"\u01be\u01bf\u0001\u0000\u0000\u0000\u01bf\u01c0\u0003D\"\u0000\u01c0"+
		"C\u0001\u0000\u0000\u0000\u01c1\u01c2\u00050\u0000\u0000\u01c2\u01c3\u0003"+
		"r9\u0000\u01c3\u01c4\u0003@ \u0000\u01c4\u0209\u0001\u0000\u0000\u0000"+
		"\u01c5\u01c6\u0005\u0019\u0000\u0000\u01c6\u01c7\u0005X\u0000\u0000\u01c7"+
		"\u01c8\u0003N\'\u0000\u01c8\u01c9\u0005Y\u0000\u0000\u01c9\u01ca\u0003"+
		"@ \u0000\u01ca\u0209\u0001\u0000\u0000\u0000\u01cb\u01cc\u0005\u001a\u0000"+
		"\u0000\u01cc\u01cd\u0003r9\u0000\u01cd\u01d0\u0003@ \u0000\u01ce\u01cf"+
		"\u0005\u0012\u0000\u0000\u01cf\u01d1\u0003@ \u0000\u01d0\u01ce\u0001\u0000"+
		"\u0000\u0000\u01d0\u01d1\u0001\u0000\u0000\u0000\u01d1\u0209\u0001\u0000"+
		"\u0000\u0000\u01d2\u01d3\u0005.\u0000\u0000\u01d3\u01d4\u0003@ \u0000"+
		"\u01d4\u01d5\u0003f3\u0000\u01d5\u0209\u0001\u0000\u0000\u0000\u01d6\u01d7"+
		"\u0005*\u0000\u0000\u01d7\u01db\u0005Z\u0000\u0000\u01d8\u01da\u0003n"+
		"7\u0000\u01d9\u01d8\u0001\u0000\u0000\u0000\u01da\u01dd\u0001\u0000\u0000"+
		"\u0000\u01db\u01d9\u0001\u0000\u0000\u0000\u01db\u01dc\u0001\u0000\u0000"+
		"\u0000\u01dc\u01de\u0001\u0000\u0000\u0000\u01dd\u01db\u0001\u0000\u0000"+
		"\u0000\u01de\u0209\u0005[\u0000\u0000\u01df\u01e1\u0005\'\u0000\u0000"+
		"\u01e0\u01e2\u0003v;\u0000\u01e1\u01e0\u0001\u0000\u0000\u0000\u01e1\u01e2"+
		"\u0001\u0000\u0000\u0000\u01e2\u01e3\u0001\u0000\u0000\u0000\u01e3\u0209"+
		"\u0005^\u0000\u0000\u01e4\u01e5\u0005,\u0000\u0000\u01e5\u01e6\u0003v"+
		";\u0000\u01e6\u01e7\u0005^\u0000\u0000\u01e7\u0209\u0001\u0000\u0000\u0000"+
		"\u01e8\u0209\u0005^\u0000\u0000\u01e9\u01ea\u0003\u0084B\u0000\u01ea\u01eb"+
		"\u0005^\u0000\u0000\u01eb\u0209\u0001\u0000\u0000\u0000\u01ec\u01ed\u0003"+
		"\u0086C\u0000\u01ed\u01ee\u0005^\u0000\u0000\u01ee\u0209\u0001\u0000\u0000"+
		"\u0000\u01ef\u01f0\u0007\u0002\u0000\u0000\u01f0\u01f1\u0003Z-\u0000\u01f1"+
		"\u01f2\u0005^\u0000\u0000\u01f2\u0209\u0001\u0000\u0000\u0000\u01f3\u01f4"+
		"\u0005#\u0000\u0000\u01f4\u01f5\u0003F#\u0000\u01f5\u01f6\u0005^\u0000"+
		"\u0000\u01f6\u0209\u0001\u0000\u0000\u0000\u01f7\u01f8\u0003L&\u0000\u01f8"+
		"\u01f9\u0005^\u0000\u0000\u01f9\u0209\u0001\u0000\u0000\u0000\u01fa\u01fb"+
		"\u0005X\u0000\u0000\u01fb\u01fc\u0003\u0090H\u0000\u01fc\u01fd\u0005Y"+
		"\u0000\u0000\u01fd\u01fe\u0003v;\u0000\u01fe\u01ff\u0005^\u0000\u0000"+
		"\u01ff\u0209\u0001\u0000\u0000\u0000\u0200\u0201\u0007\u0003\u0000\u0000"+
		"\u0201\u0202\u0005`\u0000\u0000\u0202\u0203\u0005s\u0000\u0000\u0203\u0204"+
		"\u0007\u0004\u0000\u0000\u0204\u0205\u0003v;\u0000\u0205\u0206\u0005^"+
		"\u0000\u0000\u0206\u0209\u0001\u0000\u0000\u0000\u0207\u0209\u0003\u009e"+
		"O\u0000\u0208\u01c1\u0001\u0000\u0000\u0000\u0208\u01c5\u0001\u0000\u0000"+
		"\u0000\u0208\u01cb\u0001\u0000\u0000\u0000\u0208\u01d2\u0001\u0000\u0000"+
		"\u0000\u0208\u01d6\u0001\u0000\u0000\u0000\u0208\u01df\u0001\u0000\u0000"+
		"\u0000\u0208\u01e4\u0001\u0000\u0000\u0000\u0208\u01e8\u0001\u0000\u0000"+
		"\u0000\u0208\u01e9\u0001\u0000\u0000\u0000\u0208\u01ec\u0001\u0000\u0000"+
		"\u0000\u0208\u01ef\u0001\u0000\u0000\u0000\u0208\u01f3\u0001\u0000\u0000"+
		"\u0000\u0208\u01f7\u0001\u0000\u0000\u0000\u0208\u01fa\u0001\u0000\u0000"+
		"\u0000\u0208\u0200\u0001\u0000\u0000\u0000\u0208\u0207\u0001\u0000\u0000"+
		"\u0000\u0209E\u0001\u0000\u0000\u0000\u020a\u020b\u0003\u0094J\u0000\u020b"+
		"\u020d\u0005Z\u0000\u0000\u020c\u020e\u0003H$\u0000\u020d\u020c\u0001"+
		"\u0000\u0000\u0000\u020d\u020e\u0001\u0000\u0000\u0000\u020e\u020f\u0001"+
		"\u0000\u0000\u0000\u020f\u0210\u0005[\u0000\u0000\u0210G\u0001\u0000\u0000"+
		"\u0000\u0211\u0216\u0003J%\u0000\u0212\u0213\u0005_\u0000\u0000\u0213"+
		"\u0215\u0003J%\u0000\u0214\u0212\u0001\u0000\u0000\u0000\u0215\u0218\u0001"+
		"\u0000\u0000\u0000\u0216\u0214\u0001\u0000\u0000\u0000\u0216\u0217\u0001"+
		"\u0000\u0000\u0000\u0217I\u0001\u0000\u0000\u0000\u0218\u0216\u0001\u0000"+
		"\u0000\u0000\u0219\u021a\u0005s\u0000\u0000\u021a\u021b\u0005<\u0000\u0000"+
		"\u021b\u021c\u0003v;\u0000\u021cK\u0001\u0000\u0000\u0000\u021d\u021e"+
		"\u0007\u0005\u0000\u0000\u021e\u021f\u0005X\u0000\u0000\u021f\u0220\u0003"+
		"2\u0019\u0000\u0220\u0221\u0005`\u0000\u0000\u0221\u0226\u0005s\u0000"+
		"\u0000\u0222\u0223\u0005_\u0000\u0000\u0223\u0225\u0003v;\u0000\u0224"+
		"\u0222\u0001\u0000\u0000\u0000\u0225\u0228\u0001\u0000\u0000\u0000\u0226"+
		"\u0224\u0001\u0000\u0000\u0000\u0226\u0227\u0001\u0000\u0000\u0000\u0227"+
		"\u0229\u0001\u0000\u0000\u0000\u0228\u0226\u0001\u0000\u0000\u0000\u0229"+
		"\u022a\u0005Y\u0000\u0000\u022aM\u0001\u0000\u0000\u0000\u022b\u022d\u0003"+
		"P(\u0000\u022c\u022b\u0001\u0000\u0000\u0000\u022c\u022d\u0001\u0000\u0000"+
		"\u0000\u022d\u022e\u0001\u0000\u0000\u0000\u022e\u0230\u0005^\u0000\u0000"+
		"\u022f\u0231\u0003v;\u0000\u0230\u022f\u0001\u0000\u0000\u0000\u0230\u0231"+
		"\u0001\u0000\u0000\u0000\u0231\u0232\u0001\u0000\u0000\u0000\u0232\u0234"+
		"\u0005^\u0000\u0000\u0233\u0235\u0003T*\u0000\u0234\u0233\u0001\u0000"+
		"\u0000\u0000\u0234\u0235\u0001\u0000\u0000\u0000\u0235O\u0001\u0000\u0000"+
		"\u0000\u0236\u023b\u0003R)\u0000\u0237\u0238\u0005_\u0000\u0000\u0238"+
		"\u023a\u0003R)\u0000\u0239\u0237\u0001\u0000\u0000\u0000\u023a\u023d\u0001"+
		"\u0000\u0000\u0000\u023b\u0239\u0001\u0000\u0000\u0000\u023b\u023c\u0001"+
		"\u0000\u0000\u0000\u023cQ\u0001\u0000\u0000\u0000\u023d\u023b\u0001\u0000"+
		"\u0000\u0000\u023e\u023f\u0003\u0090H\u0000\u023f\u0240\u0005s\u0000\u0000"+
		"\u0240\u0241\u00056\u0000\u0000\u0241\u0242\u0003v;\u0000\u0242S\u0001"+
		"\u0000\u0000\u0000\u0243\u0248\u0003V+\u0000\u0244\u0245\u0005_\u0000"+
		"\u0000\u0245\u0247\u0003V+\u0000\u0246\u0244\u0001\u0000\u0000\u0000\u0247"+
		"\u024a\u0001\u0000\u0000\u0000\u0248\u0246\u0001\u0000\u0000\u0000\u0248"+
		"\u0249\u0001\u0000\u0000\u0000\u0249U\u0001\u0000\u0000\u0000\u024a\u0248"+
		"\u0001\u0000\u0000\u0000\u024b\u024c\u0005s\u0000\u0000\u024c\u024d\u0005"+
		"6\u0000\u0000\u024d\u024e\u0003v;\u0000\u024eW\u0001\u0000\u0000\u0000"+
		"\u024f\u0250\u00032\u0019\u0000\u0250\u0251\u0005`\u0000\u0000\u0251\u0252"+
		"\u0005s\u0000\u0000\u0252Y\u0001\u0000\u0000\u0000\u0253\u0255\u0003\u0096"+
		"K\u0000\u0254\u0253\u0001\u0000\u0000\u0000\u0254\u0255\u0001\u0000\u0000"+
		"\u0000\u0255\u0256\u0001\u0000\u0000\u0000\u0256\u0257\u0003\u0094J\u0000"+
		"\u0257\u0258\u0003\u0080@\u0000\u0258[\u0001\u0000\u0000\u0000\u0259\u025a"+
		"\u0005\\\u0000\u0000\u025a\u025f\u0005]\u0000\u0000\u025b\u025c\u0005"+
		"\\\u0000\u0000\u025c\u025e\u0005]\u0000\u0000\u025d\u025b\u0001\u0000"+
		"\u0000\u0000\u025e\u0261\u0001\u0000\u0000\u0000\u025f\u025d\u0001\u0000"+
		"\u0000\u0000\u025f\u0260\u0001\u0000\u0000\u0000\u0260\u0262\u0001\u0000"+
		"\u0000\u0000\u0261\u025f\u0001\u0000\u0000\u0000\u0262\u0263\u0003^/\u0000"+
		"\u0263]\u0001\u0000\u0000\u0000\u0264\u0270\u0005Z\u0000\u0000\u0265\u026a"+
		"\u0003`0\u0000\u0266\u0267\u0005_\u0000\u0000\u0267\u0269\u0003`0\u0000"+
		"\u0268\u0266\u0001\u0000\u0000\u0000\u0269\u026c\u0001\u0000\u0000\u0000"+
		"\u026a\u0268\u0001\u0000\u0000\u0000\u026a\u026b\u0001\u0000\u0000\u0000"+
		"\u026b\u026e\u0001\u0000\u0000\u0000\u026c\u026a\u0001\u0000\u0000\u0000"+
		"\u026d\u026f\u0005_\u0000\u0000\u026e\u026d\u0001\u0000\u0000\u0000\u026e"+
		"\u026f\u0001\u0000\u0000\u0000\u026f\u0271\u0001\u0000\u0000\u0000\u0270"+
		"\u0265\u0001\u0000\u0000\u0000\u0270\u0271\u0001\u0000\u0000\u0000\u0271"+
		"\u0272\u0001\u0000\u0000\u0000\u0272\u0273\u0005[\u0000\u0000\u0273_\u0001"+
		"\u0000\u0000\u0000\u0274\u0277\u0003^/\u0000\u0275\u0277\u0003v;\u0000"+
		"\u0276\u0274\u0001\u0000\u0000\u0000\u0276\u0275\u0001\u0000\u0000\u0000"+
		"\u0277a\u0001\u0000\u0000\u0000\u0278\u027a\u0005s\u0000\u0000\u0279\u027b"+
		"\u0003\u0096K\u0000\u027a\u0279\u0001\u0000\u0000\u0000\u027a\u027b\u0001"+
		"\u0000\u0000\u0000\u027b\u027e\u0001\u0000\u0000\u0000\u027c\u027e\u0003"+
		"\u0098L\u0000\u027d\u0278\u0001\u0000\u0000\u0000\u027d\u027c\u0001\u0000"+
		"\u0000\u0000\u027ec\u0001\u0000\u0000\u0000\u027f\u0280\u0003\u0080@\u0000"+
		"\u0280e\u0001\u0000\u0000\u0000\u0281\u0282\u0005\u0005\u0000\u0000\u0282"+
		"\u0284\u0005Z\u0000\u0000\u0283\u0285\u0003h4\u0000\u0284\u0283\u0001"+
		"\u0000\u0000\u0000\u0284\u0285\u0001\u0000\u0000\u0000\u0285\u0286\u0001"+
		"\u0000\u0000\u0000\u0286\u0287\u0005[\u0000\u0000\u0287g\u0001\u0000\u0000"+
		"\u0000\u0288\u028d\u0003j5\u0000\u0289\u028a\u0005_\u0000\u0000\u028a"+
		"\u028c\u0003j5\u0000\u028b\u0289\u0001\u0000\u0000\u0000\u028c\u028f\u0001"+
		"\u0000\u0000\u0000\u028d\u028b\u0001\u0000\u0000\u0000\u028d\u028e\u0001"+
		"\u0000\u0000\u0000\u028ei\u0001\u0000\u0000\u0000\u028f\u028d\u0001\u0000"+
		"\u0000\u0000\u0290\u0291\u0005s\u0000\u0000\u0291\u0292\u0005<\u0000\u0000"+
		"\u0292\u0298\u0005Z\u0000\u0000\u0293\u0294\u0003l6\u0000\u0294\u0295"+
		"\u0005_\u0000\u0000\u0295\u0297\u0001\u0000\u0000\u0000\u0296\u0293\u0001"+
		"\u0000\u0000\u0000\u0297\u029a\u0001\u0000\u0000\u0000\u0298\u0296\u0001"+
		"\u0000\u0000\u0000\u0298\u0299\u0001\u0000\u0000\u0000\u0299\u029b\u0001"+
		"\u0000\u0000\u0000\u029a\u0298\u0001\u0000\u0000\u0000\u029b\u029c\u0005"+
		"\u0004\u0000\u0000\u029c\u029d\u0005<\u0000\u0000\u029d\u029e\u0003v;"+
		"\u0000\u029e\u029f\u0005[\u0000\u0000\u029fk\u0001\u0000\u0000\u0000\u02a0"+
		"\u02a1\u0005s\u0000\u0000\u02a1\u02a2\u0005<\u0000\u0000\u02a2\u02a3\u0003"+
		"v;\u0000\u02a3m\u0001\u0000\u0000\u0000\u02a4\u02a5\u0003p8\u0000\u02a5"+
		"\u02a6\u0003@ \u0000\u02a6o\u0001\u0000\u0000\u0000\u02a7\u02a8\u0005"+
		"\u0003\u0000\u0000\u02a8\u02a9\u0003v;\u0000\u02a9\u02aa\u0005a\u0000"+
		"\u0000\u02aa\u02ae\u0001\u0000\u0000\u0000\u02ab\u02ac\u0005\u0004\u0000"+
		"\u0000\u02ac\u02ae\u0005a\u0000\u0000\u02ad\u02a7\u0001\u0000\u0000\u0000"+
		"\u02ad\u02ab\u0001\u0000\u0000\u0000\u02aeq\u0001\u0000\u0000\u0000\u02af"+
		"\u02b0\u0005X\u0000\u0000\u02b0\u02b1\u0003v;\u0000\u02b1\u02b2\u0005"+
		"Y\u0000\u0000\u02b2s\u0001\u0000\u0000\u0000\u02b3\u02b8\u0003v;\u0000"+
		"\u02b4\u02b5\u0005_\u0000\u0000\u02b5\u02b7\u0003v;\u0000\u02b6\u02b4"+
		"\u0001\u0000\u0000\u0000\u02b7\u02ba\u0001\u0000\u0000\u0000\u02b8\u02b6"+
		"\u0001\u0000\u0000\u0000\u02b8\u02b9\u0001\u0000\u0000\u0000\u02b9u\u0001"+
		"\u0000\u0000\u0000\u02ba\u02b8\u0001\u0000\u0000\u0000\u02bb\u02bc\u0006"+
		";\uffff\uffff\u0000\u02bc\u02c4\u0003x<\u0000\u02bd\u02be\u0007\u0006"+
		"\u0000\u0000\u02be\u02c4\u0003v;\u0010\u02bf\u02c0\u0007\u0007\u0000\u0000"+
		"\u02c0\u02c4\u0003v;\u000f\u02c1\u02c2\u0005s\u0000\u0000\u02c2\u02c4"+
		"\u0003\u0080@\u0000\u02c3\u02bb\u0001\u0000\u0000\u0000\u02c3\u02bd\u0001"+
		"\u0000\u0000\u0000\u02c3\u02bf\u0001\u0000\u0000\u0000\u02c3\u02c1\u0001"+
		"\u0000\u0000\u0000\u02c4\u0306\u0001\u0000\u0000\u0000\u02c5\u02c6\n\u000e"+
		"\u0000\u0000\u02c6\u02c7\u0007\b\u0000\u0000\u02c7\u0305\u0003v;\u000f"+
		"\u02c8\u02c9\n\r\u0000\u0000\u02c9\u02ca\u0007\t\u0000\u0000\u02ca\u0305"+
		"\u0003v;\u000e\u02cb\u02d3\n\f\u0000\u0000\u02cc\u02cd\u00058\u0000\u0000"+
		"\u02cd\u02d4\u00058\u0000\u0000\u02ce\u02cf\u00057\u0000\u0000\u02cf\u02d0"+
		"\u00057\u0000\u0000\u02d0\u02d4\u00057\u0000\u0000\u02d1\u02d2\u00057"+
		"\u0000\u0000\u02d2\u02d4\u00057\u0000\u0000\u02d3\u02cc\u0001\u0000\u0000"+
		"\u0000\u02d3\u02ce\u0001\u0000\u0000\u0000\u02d3\u02d1\u0001\u0000\u0000"+
		"\u0000\u02d4\u02d5\u0001\u0000\u0000\u0000\u02d5\u0305\u0003v;\r\u02d6"+
		"\u02d7\n\u000b\u0000\u0000\u02d7\u02d8\u0007\n\u0000\u0000\u02d8\u0305"+
		"\u0003v;\f\u02d9\u02da\n\t\u0000\u0000\u02da\u02db\u0007\u000b\u0000\u0000"+
		"\u02db\u0305\u0003v;\n\u02dc\u02dd\n\b\u0000\u0000\u02dd\u02de\u0005I"+
		"\u0000\u0000\u02de\u0305\u0003v;\t\u02df\u02e0\n\u0007\u0000\u0000\u02e0"+
		"\u02e1\u0005K\u0000\u0000\u02e1\u0305\u0003v;\b\u02e2\u02e3\n\u0006\u0000"+
		"\u0000\u02e3\u02e4\u0005J\u0000\u0000\u02e4\u0305\u0003v;\u0007\u02e5"+
		"\u02e6\n\u0005\u0000\u0000\u02e6\u02e7\u0005A\u0000\u0000\u02e7\u0305"+
		"\u0003v;\u0006\u02e8\u02e9\n\u0004\u0000\u0000\u02e9\u02ea\u0005B\u0000"+
		"\u0000\u02ea\u0305\u0003v;\u0005\u02eb\u02ec\n\u0003\u0000\u0000\u02ec"+
		"\u02ed\u0005;\u0000\u0000\u02ed\u02ee\u0003v;\u0000\u02ee\u02ef\u0005"+
		"<\u0000\u0000\u02ef\u02f0\u0003v;\u0003\u02f0\u0305\u0001\u0000\u0000"+
		"\u0000\u02f1\u02f2\n\u0013\u0000\u0000\u02f2\u02f3\u0005`\u0000\u0000"+
		"\u02f3\u0305\u0007\u0003\u0000\u0000\u02f4\u02f5\n\u0012\u0000\u0000\u02f5"+
		"\u02f6\u0005\\\u0000\u0000\u02f6\u02f7\u0003v;\u0000\u02f7\u02f8\u0005"+
		"]\u0000\u0000\u02f8\u0305\u0001\u0000\u0000\u0000\u02f9\u02fa\n\u0011"+
		"\u0000\u0000\u02fa\u0305\u0007\f\u0000\u0000\u02fb\u02fc\n\n\u0000\u0000"+
		"\u02fc\u02fd\u0005\u001c\u0000\u0000\u02fd\u0305\u0003\u0090H\u0000\u02fe"+
		"\u02ff\n\u0002\u0000\u0000\u02ff\u0301\u0005b\u0000\u0000\u0300\u0302"+
		"\u0003\u0096K\u0000\u0301\u0300\u0001\u0000\u0000\u0000\u0301\u0302\u0001"+
		"\u0000\u0000\u0000\u0302\u0303\u0001\u0000\u0000\u0000\u0303\u0305\u0005"+
		"s\u0000\u0000\u0304\u02c5\u0001\u0000\u0000\u0000\u0304\u02c8\u0001\u0000"+
		"\u0000\u0000\u0304\u02cb\u0001\u0000\u0000\u0000\u0304\u02d6\u0001\u0000"+
		"\u0000\u0000\u0304\u02d9\u0001\u0000\u0000\u0000\u0304\u02dc\u0001\u0000"+
		"\u0000\u0000\u0304\u02df\u0001\u0000\u0000\u0000\u0304\u02e2\u0001\u0000"+
		"\u0000\u0000\u0304\u02e5\u0001\u0000\u0000\u0000\u0304\u02e8\u0001\u0000"+
		"\u0000\u0000\u0304\u02eb\u0001\u0000\u0000\u0000\u0304\u02f1\u0001\u0000"+
		"\u0000\u0000\u0304\u02f4\u0001\u0000\u0000\u0000\u0304\u02f9\u0001\u0000"+
		"\u0000\u0000\u0304\u02fb\u0001\u0000\u0000\u0000\u0304\u02fe\u0001\u0000"+
		"\u0000\u0000\u0305\u0308\u0001\u0000\u0000\u0000\u0306\u0304\u0001\u0000"+
		"\u0000\u0000\u0306\u0307\u0001\u0000\u0000\u0000\u0307w\u0001\u0000\u0000"+
		"\u0000\u0308\u0306\u0001\u0000\u0000\u0000\u0309\u030a\u0005X\u0000\u0000"+
		"\u030a\u030b\u0003v;\u0000\u030b\u030c\u0005Y\u0000\u0000\u030c\u0311"+
		"\u0001\u0000\u0000\u0000\u030d\u0311\u0005+\u0000\u0000\u030e\u0311\u0003"+
		"\u0088D\u0000\u030f\u0311\u0005s\u0000\u0000\u0310\u0309\u0001\u0000\u0000"+
		"\u0000\u0310\u030d\u0001\u0000\u0000\u0000\u0310\u030e\u0001\u0000\u0000"+
		"\u0000\u0310\u030f\u0001\u0000\u0000\u0000\u0311y\u0001\u0000\u0000\u0000"+
		"\u0312\u0313\u0003\u0096K\u0000\u0313\u0314\u0003|>\u0000\u0314{\u0001"+
		"\u0000\u0000\u0000\u0315\u0316\u0005)\u0000\u0000\u0316\u031a\u0003~?"+
		"\u0000\u0317\u0318\u0005s\u0000\u0000\u0318\u031a\u0003\u0080@\u0000\u0319"+
		"\u0315\u0001\u0000\u0000\u0000\u0319\u0317\u0001\u0000\u0000\u0000\u031a"+
		"}\u0001\u0000\u0000\u0000\u031b\u0325\u0003\u0080@\u0000\u031c\u031e\u0005"+
		"`\u0000\u0000\u031d\u031f\u0003\u0096K\u0000\u031e\u031d\u0001\u0000\u0000"+
		"\u0000\u031e\u031f\u0001\u0000\u0000\u0000\u031f\u0320\u0001\u0000\u0000"+
		"\u0000\u0320\u0322\u0005s\u0000\u0000\u0321\u0323\u0003\u0080@\u0000\u0322"+
		"\u0321\u0001\u0000\u0000\u0000\u0322\u0323\u0001\u0000\u0000\u0000\u0323"+
		"\u0325\u0001\u0000\u0000\u0000\u0324\u031b\u0001\u0000\u0000\u0000\u0324"+
		"\u031c\u0001\u0000\u0000\u0000\u0325\u007f\u0001\u0000\u0000\u0000\u0326"+
		"\u0328\u0005X\u0000\u0000\u0327\u0329\u0003t:\u0000\u0328\u0327\u0001"+
		"\u0000\u0000\u0000\u0328\u0329\u0001\u0000\u0000\u0000\u0329\u032a\u0001"+
		"\u0000\u0000\u0000\u032a\u032b\u0005Y\u0000\u0000\u032b\u0081\u0001\u0000"+
		"\u0000\u0000\u032c\u032e\u0005s\u0000\u0000\u032d\u032f\u0003\u0096K\u0000"+
		"\u032e\u032d\u0001\u0000\u0000\u0000\u032e\u032f\u0001\u0000\u0000\u0000"+
		"\u032f\u0083\u0001\u0000\u0000\u0000\u0330\u0331\u0003v;\u0000\u0331\u0333"+
		"\u0005`\u0000\u0000\u0332\u0334\u0003\u0096K\u0000\u0333\u0332\u0001\u0000"+
		"\u0000\u0000\u0333\u0334\u0001\u0000\u0000\u0000\u0334\u0335\u0001\u0000"+
		"\u0000\u0000\u0335\u0336\u0005s\u0000\u0000\u0336\u0338\u0005X\u0000\u0000"+
		"\u0337\u0339\u0003t:\u0000\u0338\u0337\u0001\u0000\u0000\u0000\u0338\u0339"+
		"\u0001\u0000\u0000\u0000\u0339\u033a\u0001\u0000\u0000\u0000\u033a\u033b"+
		"\u0005Y\u0000\u0000\u033b\u0349\u0001\u0000\u0000\u0000\u033c\u033d\u0005"+
		"+\u0000\u0000\u033d\u033f\u0005X\u0000\u0000\u033e\u0340\u0003t:\u0000"+
		"\u033f\u033e\u0001\u0000\u0000\u0000\u033f\u0340\u0001\u0000\u0000\u0000"+
		"\u0340\u0341\u0001\u0000\u0000\u0000\u0341\u0349\u0005Y\u0000\u0000\u0342"+
		"\u0343\u0005)\u0000\u0000\u0343\u0345\u0005X\u0000\u0000\u0344\u0346\u0003"+
		"t:\u0000\u0345\u0344\u0001\u0000\u0000\u0000\u0345\u0346\u0001\u0000\u0000"+
		"\u0000\u0346\u0347\u0001\u0000\u0000\u0000\u0347\u0349\u0005Y\u0000\u0000"+
		"\u0348\u0330\u0001\u0000\u0000\u0000\u0348\u033c\u0001\u0000\u0000\u0000"+
		"\u0348\u0342\u0001\u0000\u0000\u0000\u0349\u0085\u0001\u0000\u0000\u0000"+
		"\u034a\u034b\u0003v;\u0000\u034b\u034d\u0005X\u0000\u0000\u034c\u034e"+
		"\u0003t:\u0000\u034d\u034c\u0001\u0000\u0000\u0000\u034d\u034e\u0001\u0000"+
		"\u0000\u0000\u034e\u034f\u0001\u0000\u0000\u0000\u034f\u0350\u0005Y\u0000"+
		"\u0000\u0350\u0087\u0001\u0000\u0000\u0000\u0351\u0359\u0003\u008aE\u0000"+
		"\u0352\u0359\u0003\u008cF\u0000\u0353\u0359\u0005l\u0000\u0000\u0354\u0359"+
		"\u0005m\u0000\u0000\u0355\u0359\u0005k\u0000\u0000\u0356\u0359\u0005\r"+
		"\u0000\u0000\u0357\u0359\u0005n\u0000\u0000\u0358\u0351\u0001\u0000\u0000"+
		"\u0000\u0358\u0352\u0001\u0000\u0000\u0000\u0358\u0353\u0001\u0000\u0000"+
		"\u0000\u0358\u0354\u0001\u0000\u0000\u0000\u0358\u0355\u0001\u0000\u0000"+
		"\u0000\u0358\u0356\u0001\u0000\u0000\u0000\u0358\u0357\u0001\u0000\u0000"+
		"\u0000\u0359\u0089\u0001\u0000\u0000\u0000\u035a\u035b\u0007\r\u0000\u0000"+
		"\u035b\u008b\u0001\u0000\u0000\u0000\u035c\u035d\u0007\u000e\u0000\u0000"+
		"\u035d\u008d\u0001\u0000\u0000\u0000\u035e\u0361\u0003\u0090H\u0000\u035f"+
		"\u0361\u0005/\u0000\u0000\u0360\u035e\u0001\u0000\u0000\u0000\u0360\u035f"+
		"\u0001\u0000\u0000\u0000\u0361\u008f\u0001\u0000\u0000\u0000\u0362\u0363"+
		"\u0006H\uffff\uffff\u0000\u0363\u037c\u0003\u0094J\u0000\u0364\u037c\u0003"+
		"\u0098L\u0000\u0365\u037c\u00051\u0000\u0000\u0366\u037c\u00052\u0000"+
		"\u0000\u0367\u0370\u0005X\u0000\u0000\u0368\u036d\u0003\u0090H\u0000\u0369"+
		"\u036a\u0005_\u0000\u0000\u036a\u036c\u0003\u0090H\u0000\u036b\u0369\u0001"+
		"\u0000\u0000\u0000\u036c\u036f\u0001\u0000\u0000\u0000\u036d\u036b\u0001"+
		"\u0000\u0000\u0000\u036d\u036e\u0001\u0000\u0000\u0000\u036e\u0371\u0001"+
		"\u0000\u0000\u0000\u036f\u036d\u0001\u0000\u0000\u0000\u0370\u0368\u0001"+
		"\u0000\u0000\u0000\u0370\u0371\u0001\u0000\u0000\u0000\u0371\u0372\u0001"+
		"\u0000\u0000\u0000\u0372\u0373\u0005Y\u0000\u0000\u0373\u0374\u0005a\u0000"+
		"\u0000\u0374\u037c\u0003\u0090H\u0002\u0375\u0376\u0005\\\u0000\u0000"+
		"\u0376\u0377\u0003\u0090H\u0000\u0377\u0378\u0005_\u0000\u0000\u0378\u0379"+
		"\u0003\u0090H\u0000\u0379\u037a\u0005]\u0000\u0000\u037a\u037c\u0001\u0000"+
		"\u0000\u0000\u037b\u0362\u0001\u0000\u0000\u0000\u037b\u0364\u0001\u0000"+
		"\u0000\u0000\u037b\u0365\u0001\u0000\u0000\u0000\u037b\u0366\u0001\u0000"+
		"\u0000\u0000\u037b\u0367\u0001\u0000\u0000\u0000\u037b\u0375\u0001\u0000"+
		"\u0000\u0000\u037c\u038f\u0001\u0000\u0000\u0000\u037d\u0380\n\u0005\u0000"+
		"\u0000\u037e\u037f\u0005J\u0000\u0000\u037f\u0381\u0003\u0090H\u0000\u0380"+
		"\u037e\u0001\u0000\u0000\u0000\u0381\u0382\u0001\u0000\u0000\u0000\u0382"+
		"\u0380\u0001\u0000\u0000\u0000\u0382\u0383\u0001\u0000\u0000\u0000\u0383"+
		"\u038e\u0001\u0000\u0000\u0000\u0384\u0387\n\u0004\u0000\u0000\u0385\u0386"+
		"\u0005I\u0000\u0000\u0386\u0388\u0003\u0090H\u0000\u0387\u0385\u0001\u0000"+
		"\u0000\u0000\u0388\u0389\u0001\u0000\u0000\u0000\u0389\u0387\u0001\u0000"+
		"\u0000\u0000\u0389\u038a\u0001\u0000\u0000\u0000\u038a\u038e\u0001\u0000"+
		"\u0000\u0000\u038b\u038c\n\u0003\u0000\u0000\u038c\u038e\u0003\u0092I"+
		"\u0000\u038d\u037d\u0001\u0000\u0000\u0000\u038d\u0384\u0001\u0000\u0000"+
		"\u0000\u038d\u038b\u0001\u0000\u0000\u0000\u038e\u0391\u0001\u0000\u0000"+
		"\u0000\u038f\u038d\u0001\u0000\u0000\u0000\u038f\u0390\u0001\u0000\u0000"+
		"\u0000\u0390\u0091\u0001\u0000\u0000\u0000\u0391\u038f\u0001\u0000\u0000"+
		"\u0000\u0392\u0393\u0007\u000f\u0000\u0000\u0393\u0093\u0001\u0000\u0000"+
		"\u0000\u0394\u0396\u00032\u0019\u0000\u0395\u0397\u0003\u0096K\u0000\u0396"+
		"\u0395\u0001\u0000\u0000\u0000\u0396\u0397\u0001\u0000\u0000\u0000\u0397"+
		"\u0095\u0001\u0000\u0000\u0000\u0398\u0399\u00058\u0000\u0000\u0399\u039e"+
		"\u0003\u0090H\u0000\u039a\u039b\u0005_\u0000\u0000\u039b\u039d\u0003\u0090"+
		"H\u0000\u039c\u039a\u0001\u0000\u0000\u0000\u039d\u03a0\u0001\u0000\u0000"+
		"\u0000\u039e\u039c\u0001\u0000\u0000\u0000\u039e\u039f\u0001\u0000\u0000"+
		"\u0000\u039f\u03a1\u0001\u0000\u0000\u0000\u03a0\u039e\u0001\u0000\u0000"+
		"\u0000\u03a1\u03a2\u00057\u0000\u0000\u03a2\u0097\u0001\u0000\u0000\u0000"+
		"\u03a3\u03a4\u0007\u0010\u0000\u0000\u03a4\u0099\u0001\u0000\u0000\u0000"+
		"\u03a5\u03ad\u0003\u009cN\u0000\u03a6\u03ad\u0005\u001f\u0000\u0000\u03a7"+
		"\u03ad\u0005\u0015\u0000\u0000\u03a8\u03ad\u0005\u0016\u0000\u0000\u03a9"+
		"\u03ad\u0005\u0017\u0000\u0000\u03aa\u03ad\u0005\n\u0000\u0000\u03ab\u03ad"+
		"\u00055\u0000\u0000\u03ac\u03a5\u0001\u0000\u0000\u0000\u03ac\u03a6\u0001"+
		"\u0000\u0000\u0000\u03ac\u03a7\u0001\u0000\u0000\u0000\u03ac\u03a8\u0001"+
		"\u0000\u0000\u0000\u03ac\u03a9\u0001\u0000\u0000\u0000\u03ac\u03aa\u0001"+
		"\u0000\u0000\u0000\u03ac\u03ab\u0001\u0000\u0000\u0000\u03ad\u009b\u0001"+
		"\u0000\u0000\u0000\u03ae\u03af\u0007\u0011\u0000\u0000\u03af\u009d\u0001"+
		"\u0000\u0000\u0000\u03b0\u03b1\u0003\u00a0P\u0000\u03b1\u03b2\u0005<\u0000"+
		"\u0000\u03b2\u03b3\u0003\u008eG\u0000\u03b3\u03b4\u0005a\u0000\u0000\u03b4"+
		"\u03b5\u0003\u00a2Q\u0000\u03b5\u009f\u0001\u0000\u0000\u0000\u03b6\u03b8"+
		"\u0005X\u0000\u0000\u03b7\u03b9\u0003:\u001d\u0000\u03b8\u03b7\u0001\u0000"+
		"\u0000\u0000\u03b8\u03b9\u0001\u0000\u0000\u0000\u03b9\u03ba\u0001\u0000"+
		"\u0000\u0000\u03ba\u03bb\u0005Y\u0000\u0000\u03bb\u00a1\u0001\u0000\u0000"+
		"\u0000\u03bc\u03bd\u0003@ \u0000\u03bd\u00a3\u0001\u0000\u0000\u0000\u03be"+
		"\u03bf\u0005\t\u0000\u0000\u03bf\u03c0\u0005s\u0000\u0000\u03c0\u03c4"+
		"\u0005Z\u0000\u0000\u03c1\u03c3\u0003\u00a6S\u0000\u03c2\u03c1\u0001\u0000"+
		"\u0000\u0000\u03c3\u03c6\u0001\u0000\u0000\u0000\u03c4\u03c2\u0001\u0000"+
		"\u0000\u0000\u03c4\u03c5\u0001\u0000\u0000\u0000\u03c5\u03c7\u0001\u0000"+
		"\u0000\u0000\u03c6\u03c4\u0001\u0000\u0000\u0000\u03c7\u03c8\u0005[\u0000"+
		"\u0000\u03c8\u00a5\u0001\u0000\u0000\u0000\u03c9\u03ca\u0005s\u0000\u0000"+
		"\u03ca\u03cb\u0005<\u0000\u0000\u03cb\u03cc\u0003v;\u0000\u03cc\u03cd"+
		"\u0005^\u0000\u0000\u03cd\u00a7\u0001\u0000\u0000\u0000c\u00a9\u00ae\u00b4"+
		"\u00c1\u00c7\u00ca\u00cf\u00d3\u00d7\u00df\u00e9\u00ef\u00f8\u00fc\u00ff"+
		"\u0102\u010b\u0110\u0116\u011c\u0120\u0128\u0130\u0135\u013c\u0140\u014d"+
		"\u0152\u015a\u0161\u0169\u016f\u0179\u0183\u018b\u0191\u0195\u019e\u01a8"+
		"\u01b0\u01b6\u01bd\u01d0\u01db\u01e1\u0208\u020d\u0216\u0226\u022c\u0230"+
		"\u0234\u023b\u0248\u0254\u025f\u026a\u026e\u0270\u0276\u027a\u027d\u0284"+
		"\u028d\u0298\u02ad\u02b8\u02c3\u02d3\u0301\u0304\u0306\u0310\u0319\u031e"+
		"\u0322\u0324\u0328\u032e\u0333\u0338\u033f\u0345\u0348\u034d\u0358\u0360"+
		"\u036d\u0370\u037b\u0382\u0389\u038d\u038f\u0396\u039e\u03ac\u03b8\u03c4";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}