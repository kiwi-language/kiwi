// Generated from AssemblyParser.g4 by ANTLR 4.13.1
package tech.metavm.asm.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class AssemblyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ABSTRACT=1, BOOLEAN=2, CASE=3, DEFAULT=4, CATCH=5, STRING=6, CLASS=7, 
		STRUCT=8, TIME=9, NULL=10, PASSWORD=11, DOUBLE=12, ELSE=13, ENUM=14, EXTENDS=15, 
		READONLY=16, CHILD=17, TITLE=18, FINALLY=19, FOR=20, IF=21, IMPLEMENTS=22, 
		INSTANCEOF=23, INT=24, INTERFACE=25, NATIVE=26, ENEW=27, UNEW=28, NEW=29, 
		PRIVATE=30, PROTECTED=31, PUBLIC=32, RETURN=33, STATIC=34, SUPER=35, SWITCH=36, 
		THIS=37, THROW=38, THROWS=39, TRY=40, VOID=41, WHILE=42, ASSIGN=43, GT=44, 
		LT=45, BANG=46, TILDE=47, QUESTION=48, COLON=49, EQUAL=50, LE=51, GE=52, 
		NOTEQUAL=53, AND=54, OR=55, INC=56, DEC=57, ADD=58, SUB=59, MUL=60, DIV=61, 
		BITAND=62, BITOR=63, CARET=64, MOD=65, ADD_ASSIGN=66, SUB_ASSIGN=67, MUL_ASSIGN=68, 
		DIV_ASSIGN=69, AND_ASSIGN=70, OR_ASSIGN=71, XOR_ASSIGN=72, MOD_ASSIGN=73, 
		LSHIFT_ASSIGN=74, RSHIFT_ASSIGN=75, URSHIFT_ASSIGN=76, LPAREN=77, RPAREN=78, 
		LBRACE=79, RBRACE=80, LBRACK=81, RBRACK=82, SEMI=83, COMMA=84, DOT=85, 
		ARROW=86, COLONCOLON=87, AT=88, ELLIPSIS=89, DECIMAL_LITERAL=90, HEX_LITERAL=91, 
		OCT_LITERAL=92, BINARY_LITERAL=93, FLOAT_LITERAL=94, HEX_FLOAT_LITERAL=95, 
		BOOL_LITERAL=96, CHAR_LITERAL=97, STRING_LITERAL=98, TEXT_BLOCK=99, R=100, 
		RW=101, C=102, IDENTIFIER=103, WS=104, COMMENT=105, LINE_COMMENT=106;
	public static final int
		RULE_compilationUnit = 0, RULE_typeDeclaration = 1, RULE_classDeclaration = 2, 
		RULE_classBody = 3, RULE_typeList = 4, RULE_classBodyDeclaration = 5, 
		RULE_enumDeclaration = 6, RULE_enumConstants = 7, RULE_enumConstant = 8, 
		RULE_enumBodyDeclarations = 9, RULE_interfaceDeclaration = 10, RULE_interfaceBody = 11, 
		RULE_interfaceBodyDeclaration = 12, RULE_interfaceMemberDeclaration = 13, 
		RULE_interfaceMethodDeclaration = 14, RULE_interfaceMethodModifier = 15, 
		RULE_genericInterfaceMethodDeclaration = 16, RULE_interfaceCommonBodyDeclaration = 17, 
		RULE_memberDeclaration = 18, RULE_fieldDeclaration = 19, RULE_methodDeclaration = 20, 
		RULE_genericMethodDeclaration = 21, RULE_constructorDeclaration = 22, 
		RULE_genericConstructorDeclaration = 23, RULE_typeParameters = 24, RULE_qualifiedNameList = 25, 
		RULE_qualifiedName = 26, RULE_typeParameter = 27, RULE_formalParameters = 28, 
		RULE_receiverParameter = 29, RULE_formalParameterList = 30, RULE_formalParameter = 31, 
		RULE_methodBody = 32, RULE_block = 33, RULE_labeledStatement = 34, RULE_statement = 35, 
		RULE_forControl = 36, RULE_loopVariableDeclarators = 37, RULE_loopVariableDeclarator = 38, 
		RULE_loopVariableUpdates = 39, RULE_loopVariableUpdate = 40, RULE_qualifiedFieldName = 41, 
		RULE_creator = 42, RULE_arrayCreatorRest = 43, RULE_arrayInitializer = 44, 
		RULE_variableInitializer = 45, RULE_createdName = 46, RULE_classCreatorRest = 47, 
		RULE_catchClause = 48, RULE_catchFields = 49, RULE_catchField = 50, RULE_catchValue = 51, 
		RULE_branchCase = 52, RULE_switchLabel = 53, RULE_parExpression = 54, 
		RULE_expressionList = 55, RULE_expression = 56, RULE_primary = 57, RULE_explicitGenericInvocation = 58, 
		RULE_explicitGenericInvocationSuffix = 59, RULE_superSuffix = 60, RULE_arguments = 61, 
		RULE_classType = 62, RULE_methodCall = 63, RULE_literal = 64, RULE_integerLiteral = 65, 
		RULE_floatLiteral = 66, RULE_typeTypeOrVoid = 67, RULE_typeType = 68, 
		RULE_arrayKind = 69, RULE_classOrInterfaceType = 70, RULE_typeArguments = 71, 
		RULE_primitiveType = 72, RULE_modifier = 73, RULE_classOrInterfaceModifier = 74;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "typeDeclaration", "classDeclaration", "classBody", 
			"typeList", "classBodyDeclaration", "enumDeclaration", "enumConstants", 
			"enumConstant", "enumBodyDeclarations", "interfaceDeclaration", "interfaceBody", 
			"interfaceBodyDeclaration", "interfaceMemberDeclaration", "interfaceMethodDeclaration", 
			"interfaceMethodModifier", "genericInterfaceMethodDeclaration", "interfaceCommonBodyDeclaration", 
			"memberDeclaration", "fieldDeclaration", "methodDeclaration", "genericMethodDeclaration", 
			"constructorDeclaration", "genericConstructorDeclaration", "typeParameters", 
			"qualifiedNameList", "qualifiedName", "typeParameter", "formalParameters", 
			"receiverParameter", "formalParameterList", "formalParameter", "methodBody", 
			"block", "labeledStatement", "statement", "forControl", "loopVariableDeclarators", 
			"loopVariableDeclarator", "loopVariableUpdates", "loopVariableUpdate", 
			"qualifiedFieldName", "creator", "arrayCreatorRest", "arrayInitializer", 
			"variableInitializer", "createdName", "classCreatorRest", "catchClause", 
			"catchFields", "catchField", "catchValue", "branchCase", "switchLabel", 
			"parExpression", "expressionList", "expression", "primary", "explicitGenericInvocation", 
			"explicitGenericInvocationSuffix", "superSuffix", "arguments", "classType", 
			"methodCall", "literal", "integerLiteral", "floatLiteral", "typeTypeOrVoid", 
			"typeType", "arrayKind", "classOrInterfaceType", "typeArguments", "primitiveType", 
			"modifier", "classOrInterfaceModifier"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'boolean'", "'case'", "'default'", "'catch'", "'string'", 
			"'class'", "'struct'", "'time'", "'null'", "'password'", "'double'", 
			"'else'", "'enum'", "'extends'", "'readonly'", "'child'", "'title'", 
			"'finally'", "'for'", "'if'", "'implements'", "'instanceof'", "'int'", 
			"'interface'", "'native'", "'enew'", "'unew'", "'new'", "'private'", 
			"'protected'", "'public'", "'return'", "'static'", "'super'", "'switch'", 
			"'this'", "'throw'", "'throws'", "'try'", "'void'", "'while'", "'='", 
			"'>'", "'<'", "'!'", "'~'", "'?'", "':'", "'=='", "'<='", "'>='", "'!='", 
			"'&&'", "'||'", "'++'", "'--'", "'+'", "'-'", "'*'", "'/'", "'&'", "'|'", 
			"'^'", "'%'", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", 
			"'%='", "'<<='", "'>>='", "'>>>='", "'('", "')'", "'{'", "'}'", "'['", 
			"']'", "';'", "','", "'.'", "'->'", "'::'", "'@'", "'...'", null, null, 
			null, null, null, null, null, null, null, null, "'r'", "'rw'", "'c'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSTRACT", "BOOLEAN", "CASE", "DEFAULT", "CATCH", "STRING", "CLASS", 
			"STRUCT", "TIME", "NULL", "PASSWORD", "DOUBLE", "ELSE", "ENUM", "EXTENDS", 
			"READONLY", "CHILD", "TITLE", "FINALLY", "FOR", "IF", "IMPLEMENTS", "INSTANCEOF", 
			"INT", "INTERFACE", "NATIVE", "ENEW", "UNEW", "NEW", "PRIVATE", "PROTECTED", 
			"PUBLIC", "RETURN", "STATIC", "SUPER", "SWITCH", "THIS", "THROW", "THROWS", 
			"TRY", "VOID", "WHILE", "ASSIGN", "GT", "LT", "BANG", "TILDE", "QUESTION", 
			"COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", 
			"ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", "MOD", "ADD_ASSIGN", 
			"SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", 
			"XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", 
			"LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", 
			"DOT", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "DECIMAL_LITERAL", "HEX_LITERAL", 
			"OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", 
			"BOOL_LITERAL", "CHAR_LITERAL", "STRING_LITERAL", "TEXT_BLOCK", "R", 
			"RW", "C", "IDENTIFIER", "WS", "COMMENT", "LINE_COMMENT"
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
			setState(151); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(150);
				typeDeclaration();
				}
				}
				setState(153); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 24729633154L) != 0) || _la==SEMI );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		enterRule(_localctx, 2, RULE_typeDeclaration);
		int _la;
		try {
			setState(167);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case CLASS:
			case STRUCT:
			case ENUM:
			case INTERFACE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 24696061954L) != 0)) {
					{
					{
					setState(155);
					classOrInterfaceModifier();
					}
					}
					setState(160);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(164);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLASS:
				case STRUCT:
					{
					setState(161);
					classDeclaration();
					}
					break;
				case ENUM:
					{
					setState(162);
					enumDeclaration();
					}
					break;
				case INTERFACE:
					{
					setState(163);
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
				setState(166);
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
		enterRule(_localctx, 4, RULE_classDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			_la = _input.LA(1);
			if ( !(_la==CLASS || _la==STRUCT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(170);
			match(IDENTIFIER);
			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(171);
				typeParameters();
				}
			}

			setState(176);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(174);
				match(EXTENDS);
				setState(175);
				typeType(0);
				}
			}

			setState(180);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(178);
				match(IMPLEMENTS);
				setState(179);
				typeList();
				}
			}

			setState(182);
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
		enterRule(_localctx, 6, RULE_classBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(LBRACE);
			setState(188);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 37408175758918L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 67108881L) != 0)) {
				{
				{
				setState(185);
				classBodyDeclaration();
				}
				}
				setState(190);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(191);
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
		enterRule(_localctx, 8, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			typeType(0);
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(194);
				match(COMMA);
				setState(195);
				typeType(0);
				}
				}
				setState(200);
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
		enterRule(_localctx, 10, RULE_classBodyDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 24763629570L) != 0)) {
				{
				{
				setState(201);
				modifier();
				}
				}
				setState(206);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(207);
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
		enterRule(_localctx, 12, RULE_enumDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			match(ENUM);
			setState(210);
			match(IDENTIFIER);
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(211);
				match(IMPLEMENTS);
				setState(212);
				typeList();
				}
			}

			setState(215);
			match(LBRACE);
			setState(217);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(216);
				enumConstants();
				}
			}

			setState(220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(219);
				match(COMMA);
				}
			}

			setState(223);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(222);
				enumBodyDeclarations();
				}
			}

			setState(225);
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
		enterRule(_localctx, 14, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			enumConstant();
			setState(232);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(228);
					match(COMMA);
					setState(229);
					enumConstant();
					}
					} 
				}
				setState(234);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
		enterRule(_localctx, 16, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(235);
			match(IDENTIFIER);
			setState(237);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(236);
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
		enterRule(_localctx, 18, RULE_enumBodyDeclarations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			match(SEMI);
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 37408175758918L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 67108881L) != 0)) {
				{
				{
				setState(240);
				classBodyDeclaration();
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
		enterRule(_localctx, 20, RULE_interfaceDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(246);
			match(INTERFACE);
			setState(247);
			match(IDENTIFIER);
			setState(249);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(248);
				typeParameters();
				}
			}

			setState(253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(251);
				match(EXTENDS);
				setState(252);
				typeList();
				}
			}

			setState(255);
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
		enterRule(_localctx, 22, RULE_interfaceBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
			match(LBRACE);
			setState(261);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 37408175758934L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 67108945L) != 0)) {
				{
				{
				setState(258);
				interfaceBodyDeclaration();
				}
				}
				setState(263);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(264);
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
		enterRule(_localctx, 24, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			setState(274);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case BOOLEAN:
			case DEFAULT:
			case STRING:
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
			case LT:
			case LPAREN:
			case LBRACK:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(269);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(266);
						modifier();
						}
						} 
					}
					setState(271);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				}
				setState(272);
				interfaceMemberDeclaration();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(273);
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
		public GenericInterfaceMethodDeclarationContext genericInterfaceMethodDeclaration() {
			return getRuleContext(GenericInterfaceMethodDeclarationContext.class,0);
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
		enterRule(_localctx, 26, RULE_interfaceMemberDeclaration);
		try {
			setState(278);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(276);
				interfaceMethodDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(277);
				genericInterfaceMethodDeclaration();
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
		enterRule(_localctx, 28, RULE_interfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(283);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 21474836498L) != 0)) {
				{
				{
				setState(280);
				interfaceMethodModifier();
				}
				}
				setState(285);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(286);
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
		enterRule(_localctx, 30, RULE_interfaceMethodModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 21474836498L) != 0)) ) {
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
	public static class GenericInterfaceMethodDeclarationContext extends ParserRuleContext {
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public InterfaceCommonBodyDeclarationContext interfaceCommonBodyDeclaration() {
			return getRuleContext(InterfaceCommonBodyDeclarationContext.class,0);
		}
		public List<InterfaceMethodModifierContext> interfaceMethodModifier() {
			return getRuleContexts(InterfaceMethodModifierContext.class);
		}
		public InterfaceMethodModifierContext interfaceMethodModifier(int i) {
			return getRuleContext(InterfaceMethodModifierContext.class,i);
		}
		public GenericInterfaceMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericInterfaceMethodDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterGenericInterfaceMethodDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitGenericInterfaceMethodDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitGenericInterfaceMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericInterfaceMethodDeclarationContext genericInterfaceMethodDeclaration() throws RecognitionException {
		GenericInterfaceMethodDeclarationContext _localctx = new GenericInterfaceMethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_genericInterfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 21474836498L) != 0)) {
				{
				{
				setState(290);
				interfaceMethodModifier();
				}
				}
				setState(295);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(296);
			typeParameters();
			setState(297);
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
		enterRule(_localctx, 34, RULE_interfaceCommonBodyDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			typeTypeOrVoid();
			setState(300);
			match(IDENTIFIER);
			setState(301);
			formalParameters();
			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(302);
				match(LBRACK);
				setState(303);
				match(RBRACK);
				}
				}
				setState(308);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(311);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(309);
				match(THROWS);
				setState(310);
				qualifiedNameList();
				}
			}

			setState(313);
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
		public GenericMethodDeclarationContext genericMethodDeclaration() {
			return getRuleContext(GenericMethodDeclarationContext.class,0);
		}
		public FieldDeclarationContext fieldDeclaration() {
			return getRuleContext(FieldDeclarationContext.class,0);
		}
		public ConstructorDeclarationContext constructorDeclaration() {
			return getRuleContext(ConstructorDeclarationContext.class,0);
		}
		public GenericConstructorDeclarationContext genericConstructorDeclaration() {
			return getRuleContext(GenericConstructorDeclarationContext.class,0);
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
		enterRule(_localctx, 36, RULE_memberDeclaration);
		try {
			setState(320);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(315);
				methodDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(316);
				genericMethodDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(317);
				fieldDeclaration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(318);
				constructorDeclaration();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(319);
				genericConstructorDeclaration();
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
		enterRule(_localctx, 38, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(322);
			typeType(0);
			setState(323);
			match(IDENTIFIER);
			setState(324);
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
		enterRule(_localctx, 40, RULE_methodDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			typeTypeOrVoid();
			setState(327);
			match(IDENTIFIER);
			setState(328);
			formalParameters();
			setState(329);
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
	public static class GenericMethodDeclarationContext extends ParserRuleContext {
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public GenericMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericMethodDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterGenericMethodDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitGenericMethodDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitGenericMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericMethodDeclarationContext genericMethodDeclaration() throws RecognitionException {
		GenericMethodDeclarationContext _localctx = new GenericMethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_genericMethodDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(331);
			typeParameters();
			setState(332);
			methodDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
			setState(334);
			match(IDENTIFIER);
			setState(335);
			formalParameters();
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
	public static class GenericConstructorDeclarationContext extends ParserRuleContext {
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public ConstructorDeclarationContext constructorDeclaration() {
			return getRuleContext(ConstructorDeclarationContext.class,0);
		}
		public GenericConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericConstructorDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).enterGenericConstructorDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof AssemblyParserListener ) ((AssemblyParserListener)listener).exitGenericConstructorDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof AssemblyParserVisitor ) return ((AssemblyParserVisitor<? extends T>)visitor).visitGenericConstructorDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericConstructorDeclarationContext genericConstructorDeclaration() throws RecognitionException {
		GenericConstructorDeclarationContext _localctx = new GenericConstructorDeclarationContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_genericConstructorDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			typeParameters();
			setState(343);
			constructorDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
			setState(345);
			match(LT);
			setState(346);
			typeParameter();
			setState(351);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(347);
				match(COMMA);
				setState(348);
				typeParameter();
				}
				}
				setState(353);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(354);
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
			setState(356);
			qualifiedName();
			setState(361);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(357);
				match(COMMA);
				setState(358);
				qualifiedName();
				}
				}
				setState(363);
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
			setState(364);
			match(IDENTIFIER);
			setState(369);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(365);
					match(DOT);
					setState(366);
					match(IDENTIFIER);
					}
					} 
				}
				setState(371);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
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
			setState(372);
			match(IDENTIFIER);
			setState(375);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(373);
				match(EXTENDS);
				setState(374);
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
			setState(377);
			match(LPAREN);
			setState(379);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2199040040516L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 67108881L) != 0)) {
				{
				setState(378);
				formalParameterList();
				}
			}

			setState(381);
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
			setState(383);
			typeType(0);
			setState(388);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(384);
				match(IDENTIFIER);
				setState(385);
				match(DOT);
				}
				}
				setState(390);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(391);
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
			setState(393);
			formalParameter();
			setState(398);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(394);
				match(COMMA);
				setState(395);
				formalParameter();
				}
				}
				setState(400);
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
			setState(401);
			typeType(0);
			setState(402);
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
			setState(406);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(404);
				block();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(405);
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
			setState(408);
			match(LBRACE);
			setState(412);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081081039288271872L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489345L) != 0)) {
				{
				{
				setState(409);
				labeledStatement();
				}
				}
				setState(414);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(415);
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
			setState(419);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				{
				setState(417);
				match(IDENTIFIER);
				setState(418);
				match(COLON);
				}
				break;
			}
			setState(421);
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
		public ExpressionContext statementExpression;
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
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public TerminalNode NEW() { return getToken(AssemblyParser.NEW, 0); }
		public TerminalNode UNEW() { return getToken(AssemblyParser.UNEW, 0); }
		public TerminalNode ENEW() { return getToken(AssemblyParser.ENEW, 0); }
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
			setState(483);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(423);
				match(WHILE);
				setState(424);
				parExpression();
				setState(425);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(427);
				match(FOR);
				setState(428);
				match(LPAREN);
				setState(429);
				forControl();
				setState(430);
				match(RPAREN);
				setState(431);
				block();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(433);
				match(IF);
				setState(434);
				parExpression();
				setState(435);
				block();
				setState(438);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(436);
					match(ELSE);
					setState(437);
					block();
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(440);
				match(TRY);
				setState(441);
				block();
				setState(442);
				catchClause();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(444);
				match(SWITCH);
				setState(445);
				match(LBRACE);
				setState(449);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(446);
					branchCase();
					}
					}
					setState(451);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(452);
				match(RBRACE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(453);
				match(RETURN);
				setState(455);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489281L) != 0)) {
					{
					setState(454);
					expression(0);
					}
				}

				setState(457);
				match(SEMI);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(458);
				match(THROW);
				setState(459);
				expression(0);
				setState(460);
				match(SEMI);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(462);
				match(SEMI);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(463);
				((StatementContext)_localctx).statementExpression = expression(0);
				setState(464);
				match(SEMI);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(466);
				methodCall();
				setState(467);
				match(SEMI);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(469);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 939524096L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(470);
				creator();
				setState(471);
				match(SEMI);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(473);
				_la = _input.LA(1);
				if ( !(_la==THIS || _la==IDENTIFIER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(474);
				match(DOT);
				setState(475);
				match(IDENTIFIER);
				setState(476);
				((StatementContext)_localctx).bop = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 43)) & ~0x3f) == 0 && ((1L << (_la - 43)) & 17171480577L) != 0)) ) {
					((StatementContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(477);
				expression(0);
				setState(478);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(480);
				((StatementContext)_localctx).statementExpression = expression(0);
				setState(481);
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
		enterRule(_localctx, 72, RULE_forControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(486);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2199040040516L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 67108881L) != 0)) {
				{
				setState(485);
				loopVariableDeclarators();
				}
			}

			setState(488);
			match(SEMI);
			setState(490);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489281L) != 0)) {
				{
				setState(489);
				expression(0);
				}
			}

			setState(492);
			match(SEMI);
			setState(494);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(493);
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
		enterRule(_localctx, 74, RULE_loopVariableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(496);
			loopVariableDeclarator();
			setState(501);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(497);
				match(COMMA);
				setState(498);
				loopVariableDeclarator();
				}
				}
				setState(503);
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
		enterRule(_localctx, 76, RULE_loopVariableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(504);
			typeType(0);
			setState(505);
			match(IDENTIFIER);
			setState(506);
			match(ASSIGN);
			setState(507);
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
		enterRule(_localctx, 78, RULE_loopVariableUpdates);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(509);
			loopVariableUpdate();
			setState(514);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(510);
				match(COMMA);
				setState(511);
				loopVariableUpdate();
				}
				}
				setState(516);
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
		enterRule(_localctx, 80, RULE_loopVariableUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(517);
			match(IDENTIFIER);
			setState(518);
			match(ASSIGN);
			setState(519);
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
		enterRule(_localctx, 82, RULE_qualifiedFieldName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(521);
			qualifiedName();
			setState(522);
			match(DOT);
			setState(523);
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
		enterRule(_localctx, 84, RULE_creator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(525);
			classOrInterfaceType();
			setState(526);
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
		enterRule(_localctx, 86, RULE_arrayCreatorRest);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528);
			match(LBRACK);
			setState(529);
			match(RBRACK);
			setState(534);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(530);
				match(LBRACK);
				setState(531);
				match(RBRACK);
				}
				}
				setState(536);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(537);
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
		enterRule(_localctx, 88, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(539);
			match(LBRACE);
			setState(551);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489285L) != 0)) {
				{
				setState(540);
				variableInitializer();
				setState(545);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(541);
						match(COMMA);
						setState(542);
						variableInitializer();
						}
						} 
					}
					setState(547);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
				}
				setState(549);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(548);
					match(COMMA);
					}
				}

				}
			}

			setState(553);
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
		enterRule(_localctx, 90, RULE_variableInitializer);
		try {
			setState(557);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(555);
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
				setState(556);
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
		enterRule(_localctx, 92, RULE_createdName);
		int _la;
		try {
			setState(564);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(559);
				match(IDENTIFIER);
				setState(561);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(560);
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
				setState(563);
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
		enterRule(_localctx, 94, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(566);
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
		enterRule(_localctx, 96, RULE_catchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(568);
			match(CATCH);
			setState(569);
			match(LBRACE);
			setState(571);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(570);
				catchFields();
				}
			}

			setState(573);
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
		enterRule(_localctx, 98, RULE_catchFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(575);
			catchField();
			setState(580);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(576);
				match(COMMA);
				setState(577);
				catchField();
				}
				}
				setState(582);
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
		enterRule(_localctx, 100, RULE_catchField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(583);
			match(IDENTIFIER);
			setState(584);
			match(COLON);
			setState(585);
			match(LBRACE);
			setState(591);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(586);
				catchValue();
				setState(587);
				match(COMMA);
				}
				}
				setState(593);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(594);
			match(DEFAULT);
			setState(595);
			match(COLON);
			setState(596);
			expression(0);
			setState(597);
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
		enterRule(_localctx, 102, RULE_catchValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(599);
			match(IDENTIFIER);
			setState(600);
			match(COLON);
			setState(601);
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
		enterRule(_localctx, 104, RULE_branchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(603);
			switchLabel();
			setState(604);
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
		enterRule(_localctx, 106, RULE_switchLabel);
		try {
			setState(612);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(606);
				match(CASE);
				{
				setState(607);
				expression(0);
				}
				setState(608);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(610);
				match(DEFAULT);
				setState(611);
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
		enterRule(_localctx, 108, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(614);
			match(LPAREN);
			setState(615);
			expression(0);
			setState(616);
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
		enterRule(_localctx, 110, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(618);
			expression(0);
			setState(623);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(619);
				match(COMMA);
				setState(620);
				expression(0);
				}
				}
				setState(625);
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
		public TerminalNode LPAREN() { return getToken(AssemblyParser.LPAREN, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(AssemblyParser.RPAREN, 0); }
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
		int _startState = 112;
		enterRecursionRule(_localctx, 112, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(639);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(627);
				primary();
				}
				break;
			case 2:
				{
				setState(628);
				match(LPAREN);
				setState(629);
				typeType(0);
				setState(630);
				match(RPAREN);
				setState(631);
				expression(18);
				}
				break;
			case 3:
				{
				setState(633);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1080863910568919040L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(634);
				expression(16);
				}
				break;
			case 4:
				{
				setState(635);
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
				setState(636);
				expression(15);
				}
				break;
			case 5:
				{
				setState(637);
				match(IDENTIFIER);
				setState(638);
				arguments();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(706);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(704);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(641);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(642);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 60)) & ~0x3f) == 0 && ((1L << (_la - 60)) & 35L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(643);
						expression(15);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(644);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(645);
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
						setState(646);
						expression(14);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(647);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(655);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
						case 1:
							{
							setState(648);
							match(LT);
							setState(649);
							match(LT);
							}
							break;
						case 2:
							{
							setState(650);
							match(GT);
							setState(651);
							match(GT);
							setState(652);
							match(GT);
							}
							break;
						case 3:
							{
							setState(653);
							match(GT);
							setState(654);
							match(GT);
							}
							break;
						}
						setState(657);
						expression(13);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(658);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(659);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 6808175999188992L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(660);
						expression(12);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(661);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(662);
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
						setState(663);
						expression(10);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(664);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(665);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(666);
						expression(9);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(667);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(668);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(669);
						expression(8);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(670);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(671);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(672);
						expression(7);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(673);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(674);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(675);
						expression(6);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(676);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(677);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(678);
						expression(5);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(679);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(680);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(681);
						expression(0);
						setState(682);
						match(COLON);
						setState(683);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(685);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(686);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(687);
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
						setState(688);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(689);
						match(LBRACK);
						setState(690);
						expression(0);
						setState(691);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(693);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(694);
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
						setState(695);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(696);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(697);
						typeType(0);
						}
						break;
					case 16:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(698);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(699);
						match(COLONCOLON);
						setState(701);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(700);
							typeArguments();
							}
						}

						setState(703);
						match(IDENTIFIER);
						}
						break;
					}
					} 
				}
				setState(708);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
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
		enterRule(_localctx, 114, RULE_primary);
		try {
			setState(716);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(709);
				match(LPAREN);
				setState(710);
				expression(0);
				setState(711);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(713);
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
				setState(714);
				literal();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 4);
				{
				setState(715);
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
		enterRule(_localctx, 116, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(718);
			typeArguments();
			setState(719);
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
		enterRule(_localctx, 118, RULE_explicitGenericInvocationSuffix);
		try {
			setState(725);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(721);
				match(SUPER);
				setState(722);
				superSuffix();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(723);
				match(IDENTIFIER);
				setState(724);
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
		enterRule(_localctx, 120, RULE_superSuffix);
		int _la;
		try {
			setState(736);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(727);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(728);
				match(DOT);
				setState(730);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(729);
					typeArguments();
					}
				}

				setState(732);
				match(IDENTIFIER);
				setState(734);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(733);
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
		enterRule(_localctx, 122, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(738);
			match(LPAREN);
			setState(740);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489281L) != 0)) {
				{
				setState(739);
				expressionList();
				}
			}

			setState(742);
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
		enterRule(_localctx, 124, RULE_classType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(744);
			match(IDENTIFIER);
			setState(746);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(745);
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
		enterRule(_localctx, 126, RULE_methodCall);
		int _la;
		try {
			setState(769);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(748);
				expression(0);
				setState(749);
				match(DOT);
				setState(750);
				match(IDENTIFIER);
				setState(751);
				match(LPAREN);
				setState(753);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489281L) != 0)) {
					{
					setState(752);
					expressionList();
					}
				}

				setState(755);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(757);
				match(THIS);
				setState(758);
				match(LPAREN);
				setState(760);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489281L) != 0)) {
					{
					setState(759);
					expressionList();
					}
				}

				setState(762);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(763);
				match(SUPER);
				setState(764);
				match(LPAREN);
				setState(766);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1081075154240406528L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 75489281L) != 0)) {
					{
					setState(765);
					expressionList();
					}
				}

				setState(768);
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
		enterRule(_localctx, 128, RULE_literal);
		try {
			setState(778);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(771);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(772);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(773);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(774);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(775);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(776);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(777);
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
		enterRule(_localctx, 130, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(780);
			_la = _input.LA(1);
			if ( !(((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 15L) != 0)) ) {
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
		enterRule(_localctx, 132, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(782);
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
		enterRule(_localctx, 134, RULE_typeTypeOrVoid);
		try {
			setState(786);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(784);
				typeType(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(785);
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
		int _startState = 136;
		enterRecursionRule(_localctx, 136, RULE_typeType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(811);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(789);
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
				setState(790);
				primitiveType();
				}
				break;
			case LPAREN:
				{
				setState(791);
				match(LPAREN);
				setState(800);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2199040040516L) != 0) || ((((_la - 77)) & ~0x3f) == 0 && ((1L << (_la - 77)) & 67108881L) != 0)) {
					{
					setState(792);
					typeType(0);
					setState(797);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(793);
						match(COMMA);
						setState(794);
						typeType(0);
						}
						}
						setState(799);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(802);
				match(RPAREN);
				setState(803);
				match(ARROW);
				setState(804);
				typeType(2);
				}
				break;
			case LBRACK:
				{
				setState(805);
				match(LBRACK);
				setState(806);
				typeType(0);
				setState(807);
				match(COMMA);
				setState(808);
				typeType(0);
				setState(809);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(834);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,84,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(832);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
					case 1:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(813);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(816); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(814);
								match(BITOR);
								setState(815);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(818); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,81,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(820);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(823); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(821);
								match(BITAND);
								setState(822);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(825); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,82,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(827);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(828);
						match(LBRACK);
						setState(829);
						arrayKind();
						setState(830);
						match(RBRACK);
						}
						break;
					}
					} 
				}
				setState(836);
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
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayKindContext extends ParserRuleContext {
		public TerminalNode R() { return getToken(AssemblyParser.R, 0); }
		public TerminalNode RW() { return getToken(AssemblyParser.RW, 0); }
		public TerminalNode C() { return getToken(AssemblyParser.C, 0); }
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
		enterRule(_localctx, 138, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(837);
			_la = _input.LA(1);
			if ( !(((((_la - 100)) & ~0x3f) == 0 && ((1L << (_la - 100)) & 7L) != 0)) ) {
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
		enterRule(_localctx, 140, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(839);
			qualifiedName();
			setState(841);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				{
				setState(840);
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
		enterRule(_localctx, 142, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(843);
			match(LT);
			setState(844);
			typeType(0);
			setState(849);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(845);
				match(COMMA);
				setState(846);
				typeType(0);
				}
				}
				setState(851);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(852);
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
		enterRule(_localctx, 144, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(854);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2199040040516L) != 0)) ) {
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
		enterRule(_localctx, 146, RULE_modifier);
		try {
			setState(861);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
				enterOuterAlt(_localctx, 1);
				{
				setState(856);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(857);
				match(NATIVE);
				}
				break;
			case READONLY:
				enterOuterAlt(_localctx, 3);
				{
				setState(858);
				match(READONLY);
				}
				break;
			case CHILD:
				enterOuterAlt(_localctx, 4);
				{
				setState(859);
				match(CHILD);
				}
				break;
			case TITLE:
				enterOuterAlt(_localctx, 5);
				{
				setState(860);
				match(TITLE);
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
		enterRule(_localctx, 148, RULE_classOrInterfaceModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(863);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 24696061954L) != 0)) ) {
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 56:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 68:
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
			return precpred(_ctx, 20);
		case 12:
			return precpred(_ctx, 19);
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
		"\u0004\u0001j\u0362\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007J\u0001"+
		"\u0000\u0004\u0000\u0098\b\u0000\u000b\u0000\f\u0000\u0099\u0001\u0001"+
		"\u0005\u0001\u009d\b\u0001\n\u0001\f\u0001\u00a0\t\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0003\u0001\u00a5\b\u0001\u0001\u0001\u0003\u0001\u00a8"+
		"\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u00ad\b\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002\u00b1\b\u0002\u0001\u0002\u0001\u0002"+
		"\u0003\u0002\u00b5\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0005\u0003\u00bb\b\u0003\n\u0003\f\u0003\u00be\t\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004\u00c5\b\u0004\n"+
		"\u0004\f\u0004\u00c8\t\u0004\u0001\u0005\u0005\u0005\u00cb\b\u0005\n\u0005"+
		"\f\u0005\u00ce\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0003\u0006\u00d6\b\u0006\u0001\u0006\u0001\u0006"+
		"\u0003\u0006\u00da\b\u0006\u0001\u0006\u0003\u0006\u00dd\b\u0006\u0001"+
		"\u0006\u0003\u0006\u00e0\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0005\u0007\u00e7\b\u0007\n\u0007\f\u0007\u00ea\t\u0007"+
		"\u0001\b\u0001\b\u0003\b\u00ee\b\b\u0001\t\u0001\t\u0005\t\u00f2\b\t\n"+
		"\t\f\t\u00f5\t\t\u0001\n\u0001\n\u0001\n\u0003\n\u00fa\b\n\u0001\n\u0001"+
		"\n\u0003\n\u00fe\b\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0005\u000b"+
		"\u0104\b\u000b\n\u000b\f\u000b\u0107\t\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\f\u0005\f\u010c\b\f\n\f\f\f\u010f\t\f\u0001\f\u0001\f\u0003\f\u0113\b"+
		"\f\u0001\r\u0001\r\u0003\r\u0117\b\r\u0001\u000e\u0005\u000e\u011a\b\u000e"+
		"\n\u000e\f\u000e\u011d\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001"+
		"\u000f\u0001\u0010\u0005\u0010\u0124\b\u0010\n\u0010\f\u0010\u0127\t\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0005\u0011\u0131\b\u0011\n\u0011\f\u0011\u0134"+
		"\t\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0138\b\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0003\u0012\u0141\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u0153\b\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018"+
		"\u015e\b\u0018\n\u0018\f\u0018\u0161\t\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u0168\b\u0019\n\u0019\f\u0019"+
		"\u016b\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u0170\b"+
		"\u001a\n\u001a\f\u001a\u0173\t\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0003\u001b\u0178\b\u001b\u0001\u001c\u0001\u001c\u0003\u001c\u017c\b"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005"+
		"\u001d\u0183\b\u001d\n\u001d\f\u001d\u0186\t\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u018d\b\u001e\n\u001e"+
		"\f\u001e\u0190\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001 \u0001"+
		" \u0003 \u0197\b \u0001!\u0001!\u0005!\u019b\b!\n!\f!\u019e\t!\u0001!"+
		"\u0001!\u0001\"\u0001\"\u0003\"\u01a4\b\"\u0001\"\u0001\"\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0003#\u01b7\b#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0005#\u01c0\b#\n#\f#\u01c3\t#\u0001#\u0001#\u0001#\u0003"+
		"#\u01c8\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u01e4\b#\u0001"+
		"$\u0003$\u01e7\b$\u0001$\u0001$\u0003$\u01eb\b$\u0001$\u0001$\u0003$\u01ef"+
		"\b$\u0001%\u0001%\u0001%\u0005%\u01f4\b%\n%\f%\u01f7\t%\u0001&\u0001&"+
		"\u0001&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0005\'\u0201\b\'\n\'\f"+
		"\'\u0204\t\'\u0001(\u0001(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001)\u0001"+
		"*\u0001*\u0001*\u0001+\u0001+\u0001+\u0001+\u0005+\u0215\b+\n+\f+\u0218"+
		"\t+\u0001+\u0001+\u0001,\u0001,\u0001,\u0001,\u0005,\u0220\b,\n,\f,\u0223"+
		"\t,\u0001,\u0003,\u0226\b,\u0003,\u0228\b,\u0001,\u0001,\u0001-\u0001"+
		"-\u0003-\u022e\b-\u0001.\u0001.\u0003.\u0232\b.\u0001.\u0003.\u0235\b"+
		".\u0001/\u0001/\u00010\u00010\u00010\u00030\u023c\b0\u00010\u00010\u0001"+
		"1\u00011\u00011\u00051\u0243\b1\n1\f1\u0246\t1\u00012\u00012\u00012\u0001"+
		"2\u00012\u00012\u00052\u024e\b2\n2\f2\u0251\t2\u00012\u00012\u00012\u0001"+
		"2\u00012\u00013\u00013\u00013\u00013\u00014\u00014\u00014\u00015\u0001"+
		"5\u00015\u00015\u00015\u00015\u00035\u0265\b5\u00016\u00016\u00016\u0001"+
		"6\u00017\u00017\u00017\u00057\u026e\b7\n7\f7\u0271\t7\u00018\u00018\u0001"+
		"8\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00038\u0280\b8\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00018\u00018\u00018\u00018\u00018\u00038\u0290\b8\u00018\u0001"+
		"8\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u00018\u0001"+
		"8\u00018\u00018\u00038\u02be\b8\u00018\u00058\u02c1\b8\n8\f8\u02c4\t8"+
		"\u00019\u00019\u00019\u00019\u00019\u00019\u00019\u00039\u02cd\b9\u0001"+
		":\u0001:\u0001:\u0001;\u0001;\u0001;\u0001;\u0003;\u02d6\b;\u0001<\u0001"+
		"<\u0001<\u0003<\u02db\b<\u0001<\u0001<\u0003<\u02df\b<\u0003<\u02e1\b"+
		"<\u0001=\u0001=\u0003=\u02e5\b=\u0001=\u0001=\u0001>\u0001>\u0003>\u02eb"+
		"\b>\u0001?\u0001?\u0001?\u0001?\u0001?\u0003?\u02f2\b?\u0001?\u0001?\u0001"+
		"?\u0001?\u0001?\u0003?\u02f9\b?\u0001?\u0001?\u0001?\u0001?\u0003?\u02ff"+
		"\b?\u0001?\u0003?\u0302\b?\u0001@\u0001@\u0001@\u0001@\u0001@\u0001@\u0001"+
		"@\u0003@\u030b\b@\u0001A\u0001A\u0001B\u0001B\u0001C\u0001C\u0003C\u0313"+
		"\bC\u0001D\u0001D\u0001D\u0001D\u0001D\u0001D\u0001D\u0005D\u031c\bD\n"+
		"D\fD\u031f\tD\u0003D\u0321\bD\u0001D\u0001D\u0001D\u0001D\u0001D\u0001"+
		"D\u0001D\u0001D\u0001D\u0003D\u032c\bD\u0001D\u0001D\u0001D\u0004D\u0331"+
		"\bD\u000bD\fD\u0332\u0001D\u0001D\u0001D\u0004D\u0338\bD\u000bD\fD\u0339"+
		"\u0001D\u0001D\u0001D\u0001D\u0001D\u0005D\u0341\bD\nD\fD\u0344\tD\u0001"+
		"E\u0001E\u0001F\u0001F\u0003F\u034a\bF\u0001G\u0001G\u0001G\u0001G\u0005"+
		"G\u0350\bG\nG\fG\u0353\tG\u0001G\u0001G\u0001H\u0001H\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0003I\u035e\bI\u0001J\u0001J\u0001J\u0000\u0002p\u0088"+
		"K\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082"+
		"\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0000\u0011\u0001"+
		"\u0000\u0007\b\u0004\u0000\u0001\u0001\u0004\u0004  \"\"\u0001\u0000\u001b"+
		"\u001d\u0002\u0000%%gg\u0002\u0000++BL\u0001\u00008;\u0001\u0000./\u0002"+
		"\u0000<=AA\u0001\u0000:;\u0002\u0000,-34\u0002\u00002255\u0001\u00008"+
		"9\u0001\u0000Z]\u0001\u0000^_\u0001\u0000df\u0005\u0000\u0002\u0002\u0006"+
		"\u0006\t\f\u0018\u0018))\u0003\u0000\u0001\u0001\u001e \"\"\u039d\u0000"+
		"\u0097\u0001\u0000\u0000\u0000\u0002\u00a7\u0001\u0000\u0000\u0000\u0004"+
		"\u00a9\u0001\u0000\u0000\u0000\u0006\u00b8\u0001\u0000\u0000\u0000\b\u00c1"+
		"\u0001\u0000\u0000\u0000\n\u00cc\u0001\u0000\u0000\u0000\f\u00d1\u0001"+
		"\u0000\u0000\u0000\u000e\u00e3\u0001\u0000\u0000\u0000\u0010\u00eb\u0001"+
		"\u0000\u0000\u0000\u0012\u00ef\u0001\u0000\u0000\u0000\u0014\u00f6\u0001"+
		"\u0000\u0000\u0000\u0016\u0101\u0001\u0000\u0000\u0000\u0018\u0112\u0001"+
		"\u0000\u0000\u0000\u001a\u0116\u0001\u0000\u0000\u0000\u001c\u011b\u0001"+
		"\u0000\u0000\u0000\u001e\u0120\u0001\u0000\u0000\u0000 \u0125\u0001\u0000"+
		"\u0000\u0000\"\u012b\u0001\u0000\u0000\u0000$\u0140\u0001\u0000\u0000"+
		"\u0000&\u0142\u0001\u0000\u0000\u0000(\u0146\u0001\u0000\u0000\u0000*"+
		"\u014b\u0001\u0000\u0000\u0000,\u014e\u0001\u0000\u0000\u0000.\u0156\u0001"+
		"\u0000\u0000\u00000\u0159\u0001\u0000\u0000\u00002\u0164\u0001\u0000\u0000"+
		"\u00004\u016c\u0001\u0000\u0000\u00006\u0174\u0001\u0000\u0000\u00008"+
		"\u0179\u0001\u0000\u0000\u0000:\u017f\u0001\u0000\u0000\u0000<\u0189\u0001"+
		"\u0000\u0000\u0000>\u0191\u0001\u0000\u0000\u0000@\u0196\u0001\u0000\u0000"+
		"\u0000B\u0198\u0001\u0000\u0000\u0000D\u01a3\u0001\u0000\u0000\u0000F"+
		"\u01e3\u0001\u0000\u0000\u0000H\u01e6\u0001\u0000\u0000\u0000J\u01f0\u0001"+
		"\u0000\u0000\u0000L\u01f8\u0001\u0000\u0000\u0000N\u01fd\u0001\u0000\u0000"+
		"\u0000P\u0205\u0001\u0000\u0000\u0000R\u0209\u0001\u0000\u0000\u0000T"+
		"\u020d\u0001\u0000\u0000\u0000V\u0210\u0001\u0000\u0000\u0000X\u021b\u0001"+
		"\u0000\u0000\u0000Z\u022d\u0001\u0000\u0000\u0000\\\u0234\u0001\u0000"+
		"\u0000\u0000^\u0236\u0001\u0000\u0000\u0000`\u0238\u0001\u0000\u0000\u0000"+
		"b\u023f\u0001\u0000\u0000\u0000d\u0247\u0001\u0000\u0000\u0000f\u0257"+
		"\u0001\u0000\u0000\u0000h\u025b\u0001\u0000\u0000\u0000j\u0264\u0001\u0000"+
		"\u0000\u0000l\u0266\u0001\u0000\u0000\u0000n\u026a\u0001\u0000\u0000\u0000"+
		"p\u027f\u0001\u0000\u0000\u0000r\u02cc\u0001\u0000\u0000\u0000t\u02ce"+
		"\u0001\u0000\u0000\u0000v\u02d5\u0001\u0000\u0000\u0000x\u02e0\u0001\u0000"+
		"\u0000\u0000z\u02e2\u0001\u0000\u0000\u0000|\u02e8\u0001\u0000\u0000\u0000"+
		"~\u0301\u0001\u0000\u0000\u0000\u0080\u030a\u0001\u0000\u0000\u0000\u0082"+
		"\u030c\u0001\u0000\u0000\u0000\u0084\u030e\u0001\u0000\u0000\u0000\u0086"+
		"\u0312\u0001\u0000\u0000\u0000\u0088\u032b\u0001\u0000\u0000\u0000\u008a"+
		"\u0345\u0001\u0000\u0000\u0000\u008c\u0347\u0001\u0000\u0000\u0000\u008e"+
		"\u034b\u0001\u0000\u0000\u0000\u0090\u0356\u0001\u0000\u0000\u0000\u0092"+
		"\u035d\u0001\u0000\u0000\u0000\u0094\u035f\u0001\u0000\u0000\u0000\u0096"+
		"\u0098\u0003\u0002\u0001\u0000\u0097\u0096\u0001\u0000\u0000\u0000\u0098"+
		"\u0099\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u0099"+
		"\u009a\u0001\u0000\u0000\u0000\u009a\u0001\u0001\u0000\u0000\u0000\u009b"+
		"\u009d\u0003\u0094J\u0000\u009c\u009b\u0001\u0000\u0000\u0000\u009d\u00a0"+
		"\u0001\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009e\u009f"+
		"\u0001\u0000\u0000\u0000\u009f\u00a4\u0001\u0000\u0000\u0000\u00a0\u009e"+
		"\u0001\u0000\u0000\u0000\u00a1\u00a5\u0003\u0004\u0002\u0000\u00a2\u00a5"+
		"\u0003\f\u0006\u0000\u00a3\u00a5\u0003\u0014\n\u0000\u00a4\u00a1\u0001"+
		"\u0000\u0000\u0000\u00a4\u00a2\u0001\u0000\u0000\u0000\u00a4\u00a3\u0001"+
		"\u0000\u0000\u0000\u00a5\u00a8\u0001\u0000\u0000\u0000\u00a6\u00a8\u0005"+
		"S\u0000\u0000\u00a7\u009e\u0001\u0000\u0000\u0000\u00a7\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a8\u0003\u0001\u0000\u0000\u0000\u00a9\u00aa\u0007\u0000"+
		"\u0000\u0000\u00aa\u00ac\u0005g\u0000\u0000\u00ab\u00ad\u00030\u0018\u0000"+
		"\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000"+
		"\u00ad\u00b0\u0001\u0000\u0000\u0000\u00ae\u00af\u0005\u000f\u0000\u0000"+
		"\u00af\u00b1\u0003\u0088D\u0000\u00b0\u00ae\u0001\u0000\u0000\u0000\u00b0"+
		"\u00b1\u0001\u0000\u0000\u0000\u00b1\u00b4\u0001\u0000\u0000\u0000\u00b2"+
		"\u00b3\u0005\u0016\u0000\u0000\u00b3\u00b5\u0003\b\u0004\u0000\u00b4\u00b2"+
		"\u0001\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b6\u00b7\u0003\u0006\u0003\u0000\u00b7\u0005"+
		"\u0001\u0000\u0000\u0000\u00b8\u00bc\u0005O\u0000\u0000\u00b9\u00bb\u0003"+
		"\n\u0005\u0000\u00ba\u00b9\u0001\u0000\u0000\u0000\u00bb\u00be\u0001\u0000"+
		"\u0000\u0000\u00bc\u00ba\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000"+
		"\u0000\u0000\u00bd\u00bf\u0001\u0000\u0000\u0000\u00be\u00bc\u0001\u0000"+
		"\u0000\u0000\u00bf\u00c0\u0005P\u0000\u0000\u00c0\u0007\u0001\u0000\u0000"+
		"\u0000\u00c1\u00c6\u0003\u0088D\u0000\u00c2\u00c3\u0005T\u0000\u0000\u00c3"+
		"\u00c5\u0003\u0088D\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000\u00c5\u00c8"+
		"\u0001\u0000\u0000\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7"+
		"\u0001\u0000\u0000\u0000\u00c7\t\u0001\u0000\u0000\u0000\u00c8\u00c6\u0001"+
		"\u0000\u0000\u0000\u00c9\u00cb\u0003\u0092I\u0000\u00ca\u00c9\u0001\u0000"+
		"\u0000\u0000\u00cb\u00ce\u0001\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000"+
		"\u0000\u0000\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00cf\u0001\u0000"+
		"\u0000\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00cf\u00d0\u0003$\u0012"+
		"\u0000\u00d0\u000b\u0001\u0000\u0000\u0000\u00d1\u00d2\u0005\u000e\u0000"+
		"\u0000\u00d2\u00d5\u0005g\u0000\u0000\u00d3\u00d4\u0005\u0016\u0000\u0000"+
		"\u00d4\u00d6\u0003\b\u0004\u0000\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d5"+
		"\u00d6\u0001\u0000\u0000\u0000\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7"+
		"\u00d9\u0005O\u0000\u0000\u00d8\u00da\u0003\u000e\u0007\u0000\u00d9\u00d8"+
		"\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u00dc"+
		"\u0001\u0000\u0000\u0000\u00db\u00dd\u0005T\u0000\u0000\u00dc\u00db\u0001"+
		"\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00df\u0001"+
		"\u0000\u0000\u0000\u00de\u00e0\u0003\u0012\t\u0000\u00df\u00de\u0001\u0000"+
		"\u0000\u0000\u00df\u00e0\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000"+
		"\u0000\u0000\u00e1\u00e2\u0005P\u0000\u0000\u00e2\r\u0001\u0000\u0000"+
		"\u0000\u00e3\u00e8\u0003\u0010\b\u0000\u00e4\u00e5\u0005T\u0000\u0000"+
		"\u00e5\u00e7\u0003\u0010\b\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e7"+
		"\u00ea\u0001\u0000\u0000\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8"+
		"\u00e9\u0001\u0000\u0000\u0000\u00e9\u000f\u0001\u0000\u0000\u0000\u00ea"+
		"\u00e8\u0001\u0000\u0000\u0000\u00eb\u00ed\u0005g\u0000\u0000\u00ec\u00ee"+
		"\u0003z=\u0000\u00ed\u00ec\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000"+
		"\u0000\u0000\u00ee\u0011\u0001\u0000\u0000\u0000\u00ef\u00f3\u0005S\u0000"+
		"\u0000\u00f0\u00f2\u0003\n\u0005\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000"+
		"\u00f2\u00f5\u0001\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000"+
		"\u00f3\u00f4\u0001\u0000\u0000\u0000\u00f4\u0013\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f6\u00f7\u0005\u0019\u0000\u0000"+
		"\u00f7\u00f9\u0005g\u0000\u0000\u00f8\u00fa\u00030\u0018\u0000\u00f9\u00f8"+
		"\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa\u00fd"+
		"\u0001\u0000\u0000\u0000\u00fb\u00fc\u0005\u000f\u0000\u0000\u00fc\u00fe"+
		"\u0003\b\u0004\u0000\u00fd\u00fb\u0001\u0000\u0000\u0000\u00fd\u00fe\u0001"+
		"\u0000\u0000\u0000\u00fe\u00ff\u0001\u0000\u0000\u0000\u00ff\u0100\u0003"+
		"\u0016\u000b\u0000\u0100\u0015\u0001\u0000\u0000\u0000\u0101\u0105\u0005"+
		"O\u0000\u0000\u0102\u0104\u0003\u0018\f\u0000\u0103\u0102\u0001\u0000"+
		"\u0000\u0000\u0104\u0107\u0001\u0000\u0000\u0000\u0105\u0103\u0001\u0000"+
		"\u0000\u0000\u0105\u0106\u0001\u0000\u0000\u0000\u0106\u0108\u0001\u0000"+
		"\u0000\u0000\u0107\u0105\u0001\u0000\u0000\u0000\u0108\u0109\u0005P\u0000"+
		"\u0000\u0109\u0017\u0001\u0000\u0000\u0000\u010a\u010c\u0003\u0092I\u0000"+
		"\u010b\u010a\u0001\u0000\u0000\u0000\u010c\u010f\u0001\u0000\u0000\u0000"+
		"\u010d\u010b\u0001\u0000\u0000\u0000\u010d\u010e\u0001\u0000\u0000\u0000"+
		"\u010e\u0110\u0001\u0000\u0000\u0000\u010f\u010d\u0001\u0000\u0000\u0000"+
		"\u0110\u0113\u0003\u001a\r\u0000\u0111\u0113\u0005S\u0000\u0000\u0112"+
		"\u010d\u0001\u0000\u0000\u0000\u0112\u0111\u0001\u0000\u0000\u0000\u0113"+
		"\u0019\u0001\u0000\u0000\u0000\u0114\u0117\u0003\u001c\u000e\u0000\u0115"+
		"\u0117\u0003 \u0010\u0000\u0116\u0114\u0001\u0000\u0000\u0000\u0116\u0115"+
		"\u0001\u0000\u0000\u0000\u0117\u001b\u0001\u0000\u0000\u0000\u0118\u011a"+
		"\u0003\u001e\u000f\u0000\u0119\u0118\u0001\u0000\u0000\u0000\u011a\u011d"+
		"\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000\u0000\u011b\u011c"+
		"\u0001\u0000\u0000\u0000\u011c\u011e\u0001\u0000\u0000\u0000\u011d\u011b"+
		"\u0001\u0000\u0000\u0000\u011e\u011f\u0003\"\u0011\u0000\u011f\u001d\u0001"+
		"\u0000\u0000\u0000\u0120\u0121\u0007\u0001\u0000\u0000\u0121\u001f\u0001"+
		"\u0000\u0000\u0000\u0122\u0124\u0003\u001e\u000f\u0000\u0123\u0122\u0001"+
		"\u0000\u0000\u0000\u0124\u0127\u0001\u0000\u0000\u0000\u0125\u0123\u0001"+
		"\u0000\u0000\u0000\u0125\u0126\u0001\u0000\u0000\u0000\u0126\u0128\u0001"+
		"\u0000\u0000\u0000\u0127\u0125\u0001\u0000\u0000\u0000\u0128\u0129\u0003"+
		"0\u0018\u0000\u0129\u012a\u0003\"\u0011\u0000\u012a!\u0001\u0000\u0000"+
		"\u0000\u012b\u012c\u0003\u0086C\u0000\u012c\u012d\u0005g\u0000\u0000\u012d"+
		"\u0132\u00038\u001c\u0000\u012e\u012f\u0005Q\u0000\u0000\u012f\u0131\u0005"+
		"R\u0000\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0131\u0134\u0001\u0000"+
		"\u0000\u0000\u0132\u0130\u0001\u0000\u0000\u0000\u0132\u0133\u0001\u0000"+
		"\u0000\u0000\u0133\u0137\u0001\u0000\u0000\u0000\u0134\u0132\u0001\u0000"+
		"\u0000\u0000\u0135\u0136\u0005\'\u0000\u0000\u0136\u0138\u00032\u0019"+
		"\u0000\u0137\u0135\u0001\u0000\u0000\u0000\u0137\u0138\u0001\u0000\u0000"+
		"\u0000\u0138\u0139\u0001\u0000\u0000\u0000\u0139\u013a\u0005S\u0000\u0000"+
		"\u013a#\u0001\u0000\u0000\u0000\u013b\u0141\u0003(\u0014\u0000\u013c\u0141"+
		"\u0003*\u0015\u0000\u013d\u0141\u0003&\u0013\u0000\u013e\u0141\u0003,"+
		"\u0016\u0000\u013f\u0141\u0003.\u0017\u0000\u0140\u013b\u0001\u0000\u0000"+
		"\u0000\u0140\u013c\u0001\u0000\u0000\u0000\u0140\u013d\u0001\u0000\u0000"+
		"\u0000\u0140\u013e\u0001\u0000\u0000\u0000\u0140\u013f\u0001\u0000\u0000"+
		"\u0000\u0141%\u0001\u0000\u0000\u0000\u0142\u0143\u0003\u0088D\u0000\u0143"+
		"\u0144\u0005g\u0000\u0000\u0144\u0145\u0005S\u0000\u0000\u0145\'\u0001"+
		"\u0000\u0000\u0000\u0146\u0147\u0003\u0086C\u0000\u0147\u0148\u0005g\u0000"+
		"\u0000\u0148\u0149\u00038\u001c\u0000\u0149\u014a\u0003@ \u0000\u014a"+
		")\u0001\u0000\u0000\u0000\u014b\u014c\u00030\u0018\u0000\u014c\u014d\u0003"+
		"(\u0014\u0000\u014d+\u0001\u0000\u0000\u0000\u014e\u014f\u0005g\u0000"+
		"\u0000\u014f\u0152\u00038\u001c\u0000\u0150\u0151\u0005\'\u0000\u0000"+
		"\u0151\u0153\u00032\u0019\u0000\u0152\u0150\u0001\u0000\u0000\u0000\u0152"+
		"\u0153\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000\u0154"+
		"\u0155\u0003B!\u0000\u0155-\u0001\u0000\u0000\u0000\u0156\u0157\u0003"+
		"0\u0018\u0000\u0157\u0158\u0003,\u0016\u0000\u0158/\u0001\u0000\u0000"+
		"\u0000\u0159\u015a\u0005-\u0000\u0000\u015a\u015f\u00036\u001b\u0000\u015b"+
		"\u015c\u0005T\u0000\u0000\u015c\u015e\u00036\u001b\u0000\u015d\u015b\u0001"+
		"\u0000\u0000\u0000\u015e\u0161\u0001\u0000\u0000\u0000\u015f\u015d\u0001"+
		"\u0000\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000\u0160\u0162\u0001"+
		"\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000\u0000\u0162\u0163\u0005"+
		",\u0000\u0000\u01631\u0001\u0000\u0000\u0000\u0164\u0169\u00034\u001a"+
		"\u0000\u0165\u0166\u0005T\u0000\u0000\u0166\u0168\u00034\u001a\u0000\u0167"+
		"\u0165\u0001\u0000\u0000\u0000\u0168\u016b\u0001\u0000\u0000\u0000\u0169"+
		"\u0167\u0001\u0000\u0000\u0000\u0169\u016a\u0001\u0000\u0000\u0000\u016a"+
		"3\u0001\u0000\u0000\u0000\u016b\u0169\u0001\u0000\u0000\u0000\u016c\u0171"+
		"\u0005g\u0000\u0000\u016d\u016e\u0005U\u0000\u0000\u016e\u0170\u0005g"+
		"\u0000\u0000\u016f\u016d\u0001\u0000\u0000\u0000\u0170\u0173\u0001\u0000"+
		"\u0000\u0000\u0171\u016f\u0001\u0000\u0000\u0000\u0171\u0172\u0001\u0000"+
		"\u0000\u0000\u01725\u0001\u0000\u0000\u0000\u0173\u0171\u0001\u0000\u0000"+
		"\u0000\u0174\u0177\u0005g\u0000\u0000\u0175\u0176\u0005\u000f\u0000\u0000"+
		"\u0176\u0178\u0003\u0088D\u0000\u0177\u0175\u0001\u0000\u0000\u0000\u0177"+
		"\u0178\u0001\u0000\u0000\u0000\u01787\u0001\u0000\u0000\u0000\u0179\u017b"+
		"\u0005M\u0000\u0000\u017a\u017c\u0003<\u001e\u0000\u017b\u017a\u0001\u0000"+
		"\u0000\u0000\u017b\u017c\u0001\u0000\u0000\u0000\u017c\u017d\u0001\u0000"+
		"\u0000\u0000\u017d\u017e\u0005N\u0000\u0000\u017e9\u0001\u0000\u0000\u0000"+
		"\u017f\u0184\u0003\u0088D\u0000\u0180\u0181\u0005g\u0000\u0000\u0181\u0183"+
		"\u0005U\u0000\u0000\u0182\u0180\u0001\u0000\u0000\u0000\u0183\u0186\u0001"+
		"\u0000\u0000\u0000\u0184\u0182\u0001\u0000\u0000\u0000\u0184\u0185\u0001"+
		"\u0000\u0000\u0000\u0185\u0187\u0001\u0000\u0000\u0000\u0186\u0184\u0001"+
		"\u0000\u0000\u0000\u0187\u0188\u0005%\u0000\u0000\u0188;\u0001\u0000\u0000"+
		"\u0000\u0189\u018e\u0003>\u001f\u0000\u018a\u018b\u0005T\u0000\u0000\u018b"+
		"\u018d\u0003>\u001f\u0000\u018c\u018a\u0001\u0000\u0000\u0000\u018d\u0190"+
		"\u0001\u0000\u0000\u0000\u018e\u018c\u0001\u0000\u0000\u0000\u018e\u018f"+
		"\u0001\u0000\u0000\u0000\u018f=\u0001\u0000\u0000\u0000\u0190\u018e\u0001"+
		"\u0000\u0000\u0000\u0191\u0192\u0003\u0088D\u0000\u0192\u0193\u0005g\u0000"+
		"\u0000\u0193?\u0001\u0000\u0000\u0000\u0194\u0197\u0003B!\u0000\u0195"+
		"\u0197\u0005S\u0000\u0000\u0196\u0194\u0001\u0000\u0000\u0000\u0196\u0195"+
		"\u0001\u0000\u0000\u0000\u0197A\u0001\u0000\u0000\u0000\u0198\u019c\u0005"+
		"O\u0000\u0000\u0199\u019b\u0003D\"\u0000\u019a\u0199\u0001\u0000\u0000"+
		"\u0000\u019b\u019e\u0001\u0000\u0000\u0000\u019c\u019a\u0001\u0000\u0000"+
		"\u0000\u019c\u019d\u0001\u0000\u0000\u0000\u019d\u019f\u0001\u0000\u0000"+
		"\u0000\u019e\u019c\u0001\u0000\u0000\u0000\u019f\u01a0\u0005P\u0000\u0000"+
		"\u01a0C\u0001\u0000\u0000\u0000\u01a1\u01a2\u0005g\u0000\u0000\u01a2\u01a4"+
		"\u00051\u0000\u0000\u01a3\u01a1\u0001\u0000\u0000\u0000\u01a3\u01a4\u0001"+
		"\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000\u01a5\u01a6\u0003"+
		"F#\u0000\u01a6E\u0001\u0000\u0000\u0000\u01a7\u01a8\u0005*\u0000\u0000"+
		"\u01a8\u01a9\u0003l6\u0000\u01a9\u01aa\u0003B!\u0000\u01aa\u01e4\u0001"+
		"\u0000\u0000\u0000\u01ab\u01ac\u0005\u0014\u0000\u0000\u01ac\u01ad\u0005"+
		"M\u0000\u0000\u01ad\u01ae\u0003H$\u0000\u01ae\u01af\u0005N\u0000\u0000"+
		"\u01af\u01b0\u0003B!\u0000\u01b0\u01e4\u0001\u0000\u0000\u0000\u01b1\u01b2"+
		"\u0005\u0015\u0000\u0000\u01b2\u01b3\u0003l6\u0000\u01b3\u01b6\u0003B"+
		"!\u0000\u01b4\u01b5\u0005\r\u0000\u0000\u01b5\u01b7\u0003B!\u0000\u01b6"+
		"\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b7\u0001\u0000\u0000\u0000\u01b7"+
		"\u01e4\u0001\u0000\u0000\u0000\u01b8\u01b9\u0005(\u0000\u0000\u01b9\u01ba"+
		"\u0003B!\u0000\u01ba\u01bb\u0003`0\u0000\u01bb\u01e4\u0001\u0000\u0000"+
		"\u0000\u01bc\u01bd\u0005$\u0000\u0000\u01bd\u01c1\u0005O\u0000\u0000\u01be"+
		"\u01c0\u0003h4\u0000\u01bf\u01be\u0001\u0000\u0000\u0000\u01c0\u01c3\u0001"+
		"\u0000\u0000\u0000\u01c1\u01bf\u0001\u0000\u0000\u0000\u01c1\u01c2\u0001"+
		"\u0000\u0000\u0000\u01c2\u01c4\u0001\u0000\u0000\u0000\u01c3\u01c1\u0001"+
		"\u0000\u0000\u0000\u01c4\u01e4\u0005P\u0000\u0000\u01c5\u01c7\u0005!\u0000"+
		"\u0000\u01c6\u01c8\u0003p8\u0000\u01c7\u01c6\u0001\u0000\u0000\u0000\u01c7"+
		"\u01c8\u0001\u0000\u0000\u0000\u01c8\u01c9\u0001\u0000\u0000\u0000\u01c9"+
		"\u01e4\u0005S\u0000\u0000\u01ca\u01cb\u0005&\u0000\u0000\u01cb\u01cc\u0003"+
		"p8\u0000\u01cc\u01cd\u0005S\u0000\u0000\u01cd\u01e4\u0001\u0000\u0000"+
		"\u0000\u01ce\u01e4\u0005S\u0000\u0000\u01cf\u01d0\u0003p8\u0000\u01d0"+
		"\u01d1\u0005S\u0000\u0000\u01d1\u01e4\u0001\u0000\u0000\u0000\u01d2\u01d3"+
		"\u0003~?\u0000\u01d3\u01d4\u0005S\u0000\u0000\u01d4\u01e4\u0001\u0000"+
		"\u0000\u0000\u01d5\u01d6\u0007\u0002\u0000\u0000\u01d6\u01d7\u0003T*\u0000"+
		"\u01d7\u01d8\u0005S\u0000\u0000\u01d8\u01e4\u0001\u0000\u0000\u0000\u01d9"+
		"\u01da\u0007\u0003\u0000\u0000\u01da\u01db\u0005U\u0000\u0000\u01db\u01dc"+
		"\u0005g\u0000\u0000\u01dc\u01dd\u0007\u0004\u0000\u0000\u01dd\u01de\u0003"+
		"p8\u0000\u01de\u01df\u0005S\u0000\u0000\u01df\u01e4\u0001\u0000\u0000"+
		"\u0000\u01e0\u01e1\u0003p8\u0000\u01e1\u01e2\u0005S\u0000\u0000\u01e2"+
		"\u01e4\u0001\u0000\u0000\u0000\u01e3\u01a7\u0001\u0000\u0000\u0000\u01e3"+
		"\u01ab\u0001\u0000\u0000\u0000\u01e3\u01b1\u0001\u0000\u0000\u0000\u01e3"+
		"\u01b8\u0001\u0000\u0000\u0000\u01e3\u01bc\u0001\u0000\u0000\u0000\u01e3"+
		"\u01c5\u0001\u0000\u0000\u0000\u01e3\u01ca\u0001\u0000\u0000\u0000\u01e3"+
		"\u01ce\u0001\u0000\u0000\u0000\u01e3\u01cf\u0001\u0000\u0000\u0000\u01e3"+
		"\u01d2\u0001\u0000\u0000\u0000\u01e3\u01d5\u0001\u0000\u0000\u0000\u01e3"+
		"\u01d9\u0001\u0000\u0000\u0000\u01e3\u01e0\u0001\u0000\u0000\u0000\u01e4"+
		"G\u0001\u0000\u0000\u0000\u01e5\u01e7\u0003J%\u0000\u01e6\u01e5\u0001"+
		"\u0000\u0000\u0000\u01e6\u01e7\u0001\u0000\u0000\u0000\u01e7\u01e8\u0001"+
		"\u0000\u0000\u0000\u01e8\u01ea\u0005S\u0000\u0000\u01e9\u01eb\u0003p8"+
		"\u0000\u01ea\u01e9\u0001\u0000\u0000\u0000\u01ea\u01eb\u0001\u0000\u0000"+
		"\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u01ee\u0005S\u0000\u0000"+
		"\u01ed\u01ef\u0003N\'\u0000\u01ee\u01ed\u0001\u0000\u0000\u0000\u01ee"+
		"\u01ef\u0001\u0000\u0000\u0000\u01efI\u0001\u0000\u0000\u0000\u01f0\u01f5"+
		"\u0003L&\u0000\u01f1\u01f2\u0005T\u0000\u0000\u01f2\u01f4\u0003L&\u0000"+
		"\u01f3\u01f1\u0001\u0000\u0000\u0000\u01f4\u01f7\u0001\u0000\u0000\u0000"+
		"\u01f5\u01f3\u0001\u0000\u0000\u0000\u01f5\u01f6\u0001\u0000\u0000\u0000"+
		"\u01f6K\u0001\u0000\u0000\u0000\u01f7\u01f5\u0001\u0000\u0000\u0000\u01f8"+
		"\u01f9\u0003\u0088D\u0000\u01f9\u01fa\u0005g\u0000\u0000\u01fa\u01fb\u0005"+
		"+\u0000\u0000\u01fb\u01fc\u0003p8\u0000\u01fcM\u0001\u0000\u0000\u0000"+
		"\u01fd\u0202\u0003P(\u0000\u01fe\u01ff\u0005T\u0000\u0000\u01ff\u0201"+
		"\u0003P(\u0000\u0200\u01fe\u0001\u0000\u0000\u0000\u0201\u0204\u0001\u0000"+
		"\u0000\u0000\u0202\u0200\u0001\u0000\u0000\u0000\u0202\u0203\u0001\u0000"+
		"\u0000\u0000\u0203O\u0001\u0000\u0000\u0000\u0204\u0202\u0001\u0000\u0000"+
		"\u0000\u0205\u0206\u0005g\u0000\u0000\u0206\u0207\u0005+\u0000\u0000\u0207"+
		"\u0208\u0003p8\u0000\u0208Q\u0001\u0000\u0000\u0000\u0209\u020a\u0003"+
		"4\u001a\u0000\u020a\u020b\u0005U\u0000\u0000\u020b\u020c\u0005g\u0000"+
		"\u0000\u020cS\u0001\u0000\u0000\u0000\u020d\u020e\u0003\u008cF\u0000\u020e"+
		"\u020f\u0003z=\u0000\u020fU\u0001\u0000\u0000\u0000\u0210\u0211\u0005"+
		"Q\u0000\u0000\u0211\u0216\u0005R\u0000\u0000\u0212\u0213\u0005Q\u0000"+
		"\u0000\u0213\u0215\u0005R\u0000\u0000\u0214\u0212\u0001\u0000\u0000\u0000"+
		"\u0215\u0218\u0001\u0000\u0000\u0000\u0216\u0214\u0001\u0000\u0000\u0000"+
		"\u0216\u0217\u0001\u0000\u0000\u0000\u0217\u0219\u0001\u0000\u0000\u0000"+
		"\u0218\u0216\u0001\u0000\u0000\u0000\u0219\u021a\u0003X,\u0000\u021aW"+
		"\u0001\u0000\u0000\u0000\u021b\u0227\u0005O\u0000\u0000\u021c\u0221\u0003"+
		"Z-\u0000\u021d\u021e\u0005T\u0000\u0000\u021e\u0220\u0003Z-\u0000\u021f"+
		"\u021d\u0001\u0000\u0000\u0000\u0220\u0223\u0001\u0000\u0000\u0000\u0221"+
		"\u021f\u0001\u0000\u0000\u0000\u0221\u0222\u0001\u0000\u0000\u0000\u0222"+
		"\u0225\u0001\u0000\u0000\u0000\u0223\u0221\u0001\u0000\u0000\u0000\u0224"+
		"\u0226\u0005T\u0000\u0000\u0225\u0224\u0001\u0000\u0000\u0000\u0225\u0226"+
		"\u0001\u0000\u0000\u0000\u0226\u0228\u0001\u0000\u0000\u0000\u0227\u021c"+
		"\u0001\u0000\u0000\u0000\u0227\u0228\u0001\u0000\u0000\u0000\u0228\u0229"+
		"\u0001\u0000\u0000\u0000\u0229\u022a\u0005P\u0000\u0000\u022aY\u0001\u0000"+
		"\u0000\u0000\u022b\u022e\u0003X,\u0000\u022c\u022e\u0003p8\u0000\u022d"+
		"\u022b\u0001\u0000\u0000\u0000\u022d\u022c\u0001\u0000\u0000\u0000\u022e"+
		"[\u0001\u0000\u0000\u0000\u022f\u0231\u0005g\u0000\u0000\u0230\u0232\u0003"+
		"\u008eG\u0000\u0231\u0230\u0001\u0000\u0000\u0000\u0231\u0232\u0001\u0000"+
		"\u0000\u0000\u0232\u0235\u0001\u0000\u0000\u0000\u0233\u0235\u0003\u0090"+
		"H\u0000\u0234\u022f\u0001\u0000\u0000\u0000\u0234\u0233\u0001\u0000\u0000"+
		"\u0000\u0235]\u0001\u0000\u0000\u0000\u0236\u0237\u0003z=\u0000\u0237"+
		"_\u0001\u0000\u0000\u0000\u0238\u0239\u0005\u0005\u0000\u0000\u0239\u023b"+
		"\u0005O\u0000\u0000\u023a\u023c\u0003b1\u0000\u023b\u023a\u0001\u0000"+
		"\u0000\u0000\u023b\u023c\u0001\u0000\u0000\u0000\u023c\u023d\u0001\u0000"+
		"\u0000\u0000\u023d\u023e\u0005P\u0000\u0000\u023ea\u0001\u0000\u0000\u0000"+
		"\u023f\u0244\u0003d2\u0000\u0240\u0241\u0005T\u0000\u0000\u0241\u0243"+
		"\u0003d2\u0000\u0242\u0240\u0001\u0000\u0000\u0000\u0243\u0246\u0001\u0000"+
		"\u0000\u0000\u0244\u0242\u0001\u0000\u0000\u0000\u0244\u0245\u0001\u0000"+
		"\u0000\u0000\u0245c\u0001\u0000\u0000\u0000\u0246\u0244\u0001\u0000\u0000"+
		"\u0000\u0247\u0248\u0005g\u0000\u0000\u0248\u0249\u00051\u0000\u0000\u0249"+
		"\u024f\u0005O\u0000\u0000\u024a\u024b\u0003f3\u0000\u024b\u024c\u0005"+
		"T\u0000\u0000\u024c\u024e\u0001\u0000\u0000\u0000\u024d\u024a\u0001\u0000"+
		"\u0000\u0000\u024e\u0251\u0001\u0000\u0000\u0000\u024f\u024d\u0001\u0000"+
		"\u0000\u0000\u024f\u0250\u0001\u0000\u0000\u0000\u0250\u0252\u0001\u0000"+
		"\u0000\u0000\u0251\u024f\u0001\u0000\u0000\u0000\u0252\u0253\u0005\u0004"+
		"\u0000\u0000\u0253\u0254\u00051\u0000\u0000\u0254\u0255\u0003p8\u0000"+
		"\u0255\u0256\u0005P\u0000\u0000\u0256e\u0001\u0000\u0000\u0000\u0257\u0258"+
		"\u0005g\u0000\u0000\u0258\u0259\u00051\u0000\u0000\u0259\u025a\u0003p"+
		"8\u0000\u025ag\u0001\u0000\u0000\u0000\u025b\u025c\u0003j5\u0000\u025c"+
		"\u025d\u0003B!\u0000\u025di\u0001\u0000\u0000\u0000\u025e\u025f\u0005"+
		"\u0003\u0000\u0000\u025f\u0260\u0003p8\u0000\u0260\u0261\u0005V\u0000"+
		"\u0000\u0261\u0265\u0001\u0000\u0000\u0000\u0262\u0263\u0005\u0004\u0000"+
		"\u0000\u0263\u0265\u0005V\u0000\u0000\u0264\u025e\u0001\u0000\u0000\u0000"+
		"\u0264\u0262\u0001\u0000\u0000\u0000\u0265k\u0001\u0000\u0000\u0000\u0266"+
		"\u0267\u0005M\u0000\u0000\u0267\u0268\u0003p8\u0000\u0268\u0269\u0005"+
		"N\u0000\u0000\u0269m\u0001\u0000\u0000\u0000\u026a\u026f\u0003p8\u0000"+
		"\u026b\u026c\u0005T\u0000\u0000\u026c\u026e\u0003p8\u0000\u026d\u026b"+
		"\u0001\u0000\u0000\u0000\u026e\u0271\u0001\u0000\u0000\u0000\u026f\u026d"+
		"\u0001\u0000\u0000\u0000\u026f\u0270\u0001\u0000\u0000\u0000\u0270o\u0001"+
		"\u0000\u0000\u0000\u0271\u026f\u0001\u0000\u0000\u0000\u0272\u0273\u0006"+
		"8\uffff\uffff\u0000\u0273\u0280\u0003r9\u0000\u0274\u0275\u0005M\u0000"+
		"\u0000\u0275\u0276\u0003\u0088D\u0000\u0276\u0277\u0005N\u0000\u0000\u0277"+
		"\u0278\u0003p8\u0012\u0278\u0280\u0001\u0000\u0000\u0000\u0279\u027a\u0007"+
		"\u0005\u0000\u0000\u027a\u0280\u0003p8\u0010\u027b\u027c\u0007\u0006\u0000"+
		"\u0000\u027c\u0280\u0003p8\u000f\u027d\u027e\u0005g\u0000\u0000\u027e"+
		"\u0280\u0003z=\u0000\u027f\u0272\u0001\u0000\u0000\u0000\u027f\u0274\u0001"+
		"\u0000\u0000\u0000\u027f\u0279\u0001\u0000\u0000\u0000\u027f\u027b\u0001"+
		"\u0000\u0000\u0000\u027f\u027d\u0001\u0000\u0000\u0000\u0280\u02c2\u0001"+
		"\u0000\u0000\u0000\u0281\u0282\n\u000e\u0000\u0000\u0282\u0283\u0007\u0007"+
		"\u0000\u0000\u0283\u02c1\u0003p8\u000f\u0284\u0285\n\r\u0000\u0000\u0285"+
		"\u0286\u0007\b\u0000\u0000\u0286\u02c1\u0003p8\u000e\u0287\u028f\n\f\u0000"+
		"\u0000\u0288\u0289\u0005-\u0000\u0000\u0289\u0290\u0005-\u0000\u0000\u028a"+
		"\u028b\u0005,\u0000\u0000\u028b\u028c\u0005,\u0000\u0000\u028c\u0290\u0005"+
		",\u0000\u0000\u028d\u028e\u0005,\u0000\u0000\u028e\u0290\u0005,\u0000"+
		"\u0000\u028f\u0288\u0001\u0000\u0000\u0000\u028f\u028a\u0001\u0000\u0000"+
		"\u0000\u028f\u028d\u0001\u0000\u0000\u0000\u0290\u0291\u0001\u0000\u0000"+
		"\u0000\u0291\u02c1\u0003p8\r\u0292\u0293\n\u000b\u0000\u0000\u0293\u0294"+
		"\u0007\t\u0000\u0000\u0294\u02c1\u0003p8\f\u0295\u0296\n\t\u0000\u0000"+
		"\u0296\u0297\u0007\n\u0000\u0000\u0297\u02c1\u0003p8\n\u0298\u0299\n\b"+
		"\u0000\u0000\u0299\u029a\u0005>\u0000\u0000\u029a\u02c1\u0003p8\t\u029b"+
		"\u029c\n\u0007\u0000\u0000\u029c\u029d\u0005@\u0000\u0000\u029d\u02c1"+
		"\u0003p8\b\u029e\u029f\n\u0006\u0000\u0000\u029f\u02a0\u0005?\u0000\u0000"+
		"\u02a0\u02c1\u0003p8\u0007\u02a1\u02a2\n\u0005\u0000\u0000\u02a2\u02a3"+
		"\u00056\u0000\u0000\u02a3\u02c1\u0003p8\u0006\u02a4\u02a5\n\u0004\u0000"+
		"\u0000\u02a5\u02a6\u00057\u0000\u0000\u02a6\u02c1\u0003p8\u0005\u02a7"+
		"\u02a8\n\u0003\u0000\u0000\u02a8\u02a9\u00050\u0000\u0000\u02a9\u02aa"+
		"\u0003p8\u0000\u02aa\u02ab\u00051\u0000\u0000\u02ab\u02ac\u0003p8\u0003"+
		"\u02ac\u02c1\u0001\u0000\u0000\u0000\u02ad\u02ae\n\u0014\u0000\u0000\u02ae"+
		"\u02af\u0005U\u0000\u0000\u02af\u02c1\u0007\u0003\u0000\u0000\u02b0\u02b1"+
		"\n\u0013\u0000\u0000\u02b1\u02b2\u0005Q\u0000\u0000\u02b2\u02b3\u0003"+
		"p8\u0000\u02b3\u02b4\u0005R\u0000\u0000\u02b4\u02c1\u0001\u0000\u0000"+
		"\u0000\u02b5\u02b6\n\u0011\u0000\u0000\u02b6\u02c1\u0007\u000b\u0000\u0000"+
		"\u02b7\u02b8\n\n\u0000\u0000\u02b8\u02b9\u0005\u0017\u0000\u0000\u02b9"+
		"\u02c1\u0003\u0088D\u0000\u02ba\u02bb\n\u0002\u0000\u0000\u02bb\u02bd"+
		"\u0005W\u0000\u0000\u02bc\u02be\u0003\u008eG\u0000\u02bd\u02bc\u0001\u0000"+
		"\u0000\u0000\u02bd\u02be\u0001\u0000\u0000\u0000\u02be\u02bf\u0001\u0000"+
		"\u0000\u0000\u02bf\u02c1\u0005g\u0000\u0000\u02c0\u0281\u0001\u0000\u0000"+
		"\u0000\u02c0\u0284\u0001\u0000\u0000\u0000\u02c0\u0287\u0001\u0000\u0000"+
		"\u0000\u02c0\u0292\u0001\u0000\u0000\u0000\u02c0\u0295\u0001\u0000\u0000"+
		"\u0000\u02c0\u0298\u0001\u0000\u0000\u0000\u02c0\u029b\u0001\u0000\u0000"+
		"\u0000\u02c0\u029e\u0001\u0000\u0000\u0000\u02c0\u02a1\u0001\u0000\u0000"+
		"\u0000\u02c0\u02a4\u0001\u0000\u0000\u0000\u02c0\u02a7\u0001\u0000\u0000"+
		"\u0000\u02c0\u02ad\u0001\u0000\u0000\u0000\u02c0\u02b0\u0001\u0000\u0000"+
		"\u0000\u02c0\u02b5\u0001\u0000\u0000\u0000\u02c0\u02b7\u0001\u0000\u0000"+
		"\u0000\u02c0\u02ba\u0001\u0000\u0000\u0000\u02c1\u02c4\u0001\u0000\u0000"+
		"\u0000\u02c2\u02c0\u0001\u0000\u0000\u0000\u02c2\u02c3\u0001\u0000\u0000"+
		"\u0000\u02c3q\u0001\u0000\u0000\u0000\u02c4\u02c2\u0001\u0000\u0000\u0000"+
		"\u02c5\u02c6\u0005M\u0000\u0000\u02c6\u02c7\u0003p8\u0000\u02c7\u02c8"+
		"\u0005N\u0000\u0000\u02c8\u02cd\u0001\u0000\u0000\u0000\u02c9\u02cd\u0005"+
		"%\u0000\u0000\u02ca\u02cd\u0003\u0080@\u0000\u02cb\u02cd\u0005g\u0000"+
		"\u0000\u02cc\u02c5\u0001\u0000\u0000\u0000\u02cc\u02c9\u0001\u0000\u0000"+
		"\u0000\u02cc\u02ca\u0001\u0000\u0000\u0000\u02cc\u02cb\u0001\u0000\u0000"+
		"\u0000\u02cds\u0001\u0000\u0000\u0000\u02ce\u02cf\u0003\u008eG\u0000\u02cf"+
		"\u02d0\u0003v;\u0000\u02d0u\u0001\u0000\u0000\u0000\u02d1\u02d2\u0005"+
		"#\u0000\u0000\u02d2\u02d6\u0003x<\u0000\u02d3\u02d4\u0005g\u0000\u0000"+
		"\u02d4\u02d6\u0003z=\u0000\u02d5\u02d1\u0001\u0000\u0000\u0000\u02d5\u02d3"+
		"\u0001\u0000\u0000\u0000\u02d6w\u0001\u0000\u0000\u0000\u02d7\u02e1\u0003"+
		"z=\u0000\u02d8\u02da\u0005U\u0000\u0000\u02d9\u02db\u0003\u008eG\u0000"+
		"\u02da\u02d9\u0001\u0000\u0000\u0000\u02da\u02db\u0001\u0000\u0000\u0000"+
		"\u02db\u02dc\u0001\u0000\u0000\u0000\u02dc\u02de\u0005g\u0000\u0000\u02dd"+
		"\u02df\u0003z=\u0000\u02de\u02dd\u0001\u0000\u0000\u0000\u02de\u02df\u0001"+
		"\u0000\u0000\u0000\u02df\u02e1\u0001\u0000\u0000\u0000\u02e0\u02d7\u0001"+
		"\u0000\u0000\u0000\u02e0\u02d8\u0001\u0000\u0000\u0000\u02e1y\u0001\u0000"+
		"\u0000\u0000\u02e2\u02e4\u0005M\u0000\u0000\u02e3\u02e5\u0003n7\u0000"+
		"\u02e4\u02e3\u0001\u0000\u0000\u0000\u02e4\u02e5\u0001\u0000\u0000\u0000"+
		"\u02e5\u02e6\u0001\u0000\u0000\u0000\u02e6\u02e7\u0005N\u0000\u0000\u02e7"+
		"{\u0001\u0000\u0000\u0000\u02e8\u02ea\u0005g\u0000\u0000\u02e9\u02eb\u0003"+
		"\u008eG\u0000\u02ea\u02e9\u0001\u0000\u0000\u0000\u02ea\u02eb\u0001\u0000"+
		"\u0000\u0000\u02eb}\u0001\u0000\u0000\u0000\u02ec\u02ed\u0003p8\u0000"+
		"\u02ed\u02ee\u0005U\u0000\u0000\u02ee\u02ef\u0005g\u0000\u0000\u02ef\u02f1"+
		"\u0005M\u0000\u0000\u02f0\u02f2\u0003n7\u0000\u02f1\u02f0\u0001\u0000"+
		"\u0000\u0000\u02f1\u02f2\u0001\u0000\u0000\u0000\u02f2\u02f3\u0001\u0000"+
		"\u0000\u0000\u02f3\u02f4\u0005N\u0000\u0000\u02f4\u0302\u0001\u0000\u0000"+
		"\u0000\u02f5\u02f6\u0005%\u0000\u0000\u02f6\u02f8\u0005M\u0000\u0000\u02f7"+
		"\u02f9\u0003n7\u0000\u02f8\u02f7\u0001\u0000\u0000\u0000\u02f8\u02f9\u0001"+
		"\u0000\u0000\u0000\u02f9\u02fa\u0001\u0000\u0000\u0000\u02fa\u0302\u0005"+
		"N\u0000\u0000\u02fb\u02fc\u0005#\u0000\u0000\u02fc\u02fe\u0005M\u0000"+
		"\u0000\u02fd\u02ff\u0003n7\u0000\u02fe\u02fd\u0001\u0000\u0000\u0000\u02fe"+
		"\u02ff\u0001\u0000\u0000\u0000\u02ff\u0300\u0001\u0000\u0000\u0000\u0300"+
		"\u0302\u0005N\u0000\u0000\u0301\u02ec\u0001\u0000\u0000\u0000\u0301\u02f5"+
		"\u0001\u0000\u0000\u0000\u0301\u02fb\u0001\u0000\u0000\u0000\u0302\u007f"+
		"\u0001\u0000\u0000\u0000\u0303\u030b\u0003\u0082A\u0000\u0304\u030b\u0003"+
		"\u0084B\u0000\u0305\u030b\u0005a\u0000\u0000\u0306\u030b\u0005b\u0000"+
		"\u0000\u0307\u030b\u0005`\u0000\u0000\u0308\u030b\u0005\n\u0000\u0000"+
		"\u0309\u030b\u0005c\u0000\u0000\u030a\u0303\u0001\u0000\u0000\u0000\u030a"+
		"\u0304\u0001\u0000\u0000\u0000\u030a\u0305\u0001\u0000\u0000\u0000\u030a"+
		"\u0306\u0001\u0000\u0000\u0000\u030a\u0307\u0001\u0000\u0000\u0000\u030a"+
		"\u0308\u0001\u0000\u0000\u0000\u030a\u0309\u0001\u0000\u0000\u0000\u030b"+
		"\u0081\u0001\u0000\u0000\u0000\u030c\u030d\u0007\f\u0000\u0000\u030d\u0083"+
		"\u0001\u0000\u0000\u0000\u030e\u030f\u0007\r\u0000\u0000\u030f\u0085\u0001"+
		"\u0000\u0000\u0000\u0310\u0313\u0003\u0088D\u0000\u0311\u0313\u0005)\u0000"+
		"\u0000\u0312\u0310\u0001\u0000\u0000\u0000\u0312\u0311\u0001\u0000\u0000"+
		"\u0000\u0313\u0087\u0001\u0000\u0000\u0000\u0314\u0315\u0006D\uffff\uffff"+
		"\u0000\u0315\u032c\u0003\u008cF\u0000\u0316\u032c\u0003\u0090H\u0000\u0317"+
		"\u0320\u0005M\u0000\u0000\u0318\u031d\u0003\u0088D\u0000\u0319\u031a\u0005"+
		"T\u0000\u0000\u031a\u031c\u0003\u0088D\u0000\u031b\u0319\u0001\u0000\u0000"+
		"\u0000\u031c\u031f\u0001\u0000\u0000\u0000\u031d\u031b\u0001\u0000\u0000"+
		"\u0000\u031d\u031e\u0001\u0000\u0000\u0000\u031e\u0321\u0001\u0000\u0000"+
		"\u0000\u031f\u031d\u0001\u0000\u0000\u0000\u0320\u0318\u0001\u0000\u0000"+
		"\u0000\u0320\u0321\u0001\u0000\u0000\u0000\u0321\u0322\u0001\u0000\u0000"+
		"\u0000\u0322\u0323\u0005N\u0000\u0000\u0323\u0324\u0005V\u0000\u0000\u0324"+
		"\u032c\u0003\u0088D\u0002\u0325\u0326\u0005Q\u0000\u0000\u0326\u0327\u0003"+
		"\u0088D\u0000\u0327\u0328\u0005T\u0000\u0000\u0328\u0329\u0003\u0088D"+
		"\u0000\u0329\u032a\u0005R\u0000\u0000\u032a\u032c\u0001\u0000\u0000\u0000"+
		"\u032b\u0314\u0001\u0000\u0000\u0000\u032b\u0316\u0001\u0000\u0000\u0000"+
		"\u032b\u0317\u0001\u0000\u0000\u0000\u032b\u0325\u0001\u0000\u0000\u0000"+
		"\u032c\u0342\u0001\u0000\u0000\u0000\u032d\u0330\n\u0005\u0000\u0000\u032e"+
		"\u032f\u0005?\u0000\u0000\u032f\u0331\u0003\u0088D\u0000\u0330\u032e\u0001"+
		"\u0000\u0000\u0000\u0331\u0332\u0001\u0000\u0000\u0000\u0332\u0330\u0001"+
		"\u0000\u0000\u0000\u0332\u0333\u0001\u0000\u0000\u0000\u0333\u0341\u0001"+
		"\u0000\u0000\u0000\u0334\u0337\n\u0004\u0000\u0000\u0335\u0336\u0005>"+
		"\u0000\u0000\u0336\u0338\u0003\u0088D\u0000\u0337\u0335\u0001\u0000\u0000"+
		"\u0000\u0338\u0339\u0001\u0000\u0000\u0000\u0339\u0337\u0001\u0000\u0000"+
		"\u0000\u0339\u033a\u0001\u0000\u0000\u0000\u033a\u0341\u0001\u0000\u0000"+
		"\u0000\u033b\u033c\n\u0003\u0000\u0000\u033c\u033d\u0005Q\u0000\u0000"+
		"\u033d\u033e\u0003\u008aE\u0000\u033e\u033f\u0005R\u0000\u0000\u033f\u0341"+
		"\u0001\u0000\u0000\u0000\u0340\u032d\u0001\u0000\u0000\u0000\u0340\u0334"+
		"\u0001\u0000\u0000\u0000\u0340\u033b\u0001\u0000\u0000\u0000\u0341\u0344"+
		"\u0001\u0000\u0000\u0000\u0342\u0340\u0001\u0000\u0000\u0000\u0342\u0343"+
		"\u0001\u0000\u0000\u0000\u0343\u0089\u0001\u0000\u0000\u0000\u0344\u0342"+
		"\u0001\u0000\u0000\u0000\u0345\u0346\u0007\u000e\u0000\u0000\u0346\u008b"+
		"\u0001\u0000\u0000\u0000\u0347\u0349\u00034\u001a\u0000\u0348\u034a\u0003"+
		"\u008eG\u0000\u0349\u0348\u0001\u0000\u0000\u0000\u0349\u034a\u0001\u0000"+
		"\u0000\u0000\u034a\u008d\u0001\u0000\u0000\u0000\u034b\u034c\u0005-\u0000"+
		"\u0000\u034c\u0351\u0003\u0088D\u0000\u034d\u034e\u0005T\u0000\u0000\u034e"+
		"\u0350\u0003\u0088D\u0000\u034f\u034d\u0001\u0000\u0000\u0000\u0350\u0353"+
		"\u0001\u0000\u0000\u0000\u0351\u034f\u0001\u0000\u0000\u0000\u0351\u0352"+
		"\u0001\u0000\u0000\u0000\u0352\u0354\u0001\u0000\u0000\u0000\u0353\u0351"+
		"\u0001\u0000\u0000\u0000\u0354\u0355\u0005,\u0000\u0000\u0355\u008f\u0001"+
		"\u0000\u0000\u0000\u0356\u0357\u0007\u000f\u0000\u0000\u0357\u0091\u0001"+
		"\u0000\u0000\u0000\u0358\u035e\u0003\u0094J\u0000\u0359\u035e\u0005\u001a"+
		"\u0000\u0000\u035a\u035e\u0005\u0010\u0000\u0000\u035b\u035e\u0005\u0011"+
		"\u0000\u0000\u035c\u035e\u0005\u0012\u0000\u0000\u035d\u0358\u0001\u0000"+
		"\u0000\u0000\u035d\u0359\u0001\u0000\u0000\u0000\u035d\u035a\u0001\u0000"+
		"\u0000\u0000\u035d\u035b\u0001\u0000\u0000\u0000\u035d\u035c\u0001\u0000"+
		"\u0000\u0000\u035e\u0093\u0001\u0000\u0000\u0000\u035f\u0360\u0007\u0010"+
		"\u0000\u0000\u0360\u0095\u0001\u0000\u0000\u0000X\u0099\u009e\u00a4\u00a7"+
		"\u00ac\u00b0\u00b4\u00bc\u00c6\u00cc\u00d5\u00d9\u00dc\u00df\u00e8\u00ed"+
		"\u00f3\u00f9\u00fd\u0105\u010d\u0112\u0116\u011b\u0125\u0132\u0137\u0140"+
		"\u0152\u015f\u0169\u0171\u0177\u017b\u0184\u018e\u0196\u019c\u01a3\u01b6"+
		"\u01c1\u01c7\u01e3\u01e6\u01ea\u01ee\u01f5\u0202\u0216\u0221\u0225\u0227"+
		"\u022d\u0231\u0234\u023b\u0244\u024f\u0264\u026f\u027f\u028f\u02bd\u02c0"+
		"\u02c2\u02cc\u02d5\u02da\u02de\u02e0\u02e4\u02ea\u02f1\u02f8\u02fe\u0301"+
		"\u030a\u0312\u031d\u0320\u032b\u0332\u0339\u0340\u0342\u0349\u0351\u035d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}