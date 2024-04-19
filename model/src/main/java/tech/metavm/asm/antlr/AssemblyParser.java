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
		THIS=37, THROW=38, THROWS=39, TRY=40, VOID=41, WHILE=42, ANY=43, NEVER=44, 
		ASSIGN=45, GT=46, LT=47, BANG=48, TILDE=49, QUESTION=50, COLON=51, EQUAL=52, 
		LE=53, GE=54, NOTEQUAL=55, AND=56, OR=57, INC=58, DEC=59, ADD=60, SUB=61, 
		MUL=62, DIV=63, BITAND=64, BITOR=65, CARET=66, MOD=67, ADD_ASSIGN=68, 
		SUB_ASSIGN=69, MUL_ASSIGN=70, DIV_ASSIGN=71, AND_ASSIGN=72, OR_ASSIGN=73, 
		XOR_ASSIGN=74, MOD_ASSIGN=75, LSHIFT_ASSIGN=76, RSHIFT_ASSIGN=77, URSHIFT_ASSIGN=78, 
		LPAREN=79, RPAREN=80, LBRACE=81, RBRACE=82, LBRACK=83, RBRACK=84, SEMI=85, 
		COMMA=86, DOT=87, ARROW=88, COLONCOLON=89, AT=90, ELLIPSIS=91, DECIMAL_LITERAL=92, 
		HEX_LITERAL=93, OCT_LITERAL=94, BINARY_LITERAL=95, FLOAT_LITERAL=96, HEX_FLOAT_LITERAL=97, 
		BOOL_LITERAL=98, CHAR_LITERAL=99, STRING_LITERAL=100, TEXT_BLOCK=101, 
		R=102, RW=103, C=104, IDENTIFIER=105, WS=106, COMMENT=107, LINE_COMMENT=108;
	public static final int
		RULE_compilationUnit = 0, RULE_typeDeclaration = 1, RULE_classDeclaration = 2, 
		RULE_classBody = 3, RULE_typeList = 4, RULE_classBodyDeclaration = 5, 
		RULE_enumDeclaration = 6, RULE_enumConstants = 7, RULE_enumConstant = 8, 
		RULE_enumBodyDeclarations = 9, RULE_interfaceDeclaration = 10, RULE_interfaceBody = 11, 
		RULE_interfaceBodyDeclaration = 12, RULE_interfaceMemberDeclaration = 13, 
		RULE_interfaceMethodDeclaration = 14, RULE_interfaceMethodModifier = 15, 
		RULE_interfaceCommonBodyDeclaration = 16, RULE_memberDeclaration = 17, 
		RULE_fieldDeclaration = 18, RULE_methodDeclaration = 19, RULE_constructorDeclaration = 20, 
		RULE_typeParameters = 21, RULE_qualifiedNameList = 22, RULE_qualifiedName = 23, 
		RULE_typeParameter = 24, RULE_formalParameters = 25, RULE_receiverParameter = 26, 
		RULE_formalParameterList = 27, RULE_formalParameter = 28, RULE_methodBody = 29, 
		RULE_block = 30, RULE_labeledStatement = 31, RULE_statement = 32, RULE_forControl = 33, 
		RULE_loopVariableDeclarators = 34, RULE_loopVariableDeclarator = 35, RULE_loopVariableUpdates = 36, 
		RULE_loopVariableUpdate = 37, RULE_qualifiedFieldName = 38, RULE_creator = 39, 
		RULE_arrayCreatorRest = 40, RULE_arrayInitializer = 41, RULE_variableInitializer = 42, 
		RULE_createdName = 43, RULE_classCreatorRest = 44, RULE_catchClause = 45, 
		RULE_catchFields = 46, RULE_catchField = 47, RULE_catchValue = 48, RULE_branchCase = 49, 
		RULE_switchLabel = 50, RULE_parExpression = 51, RULE_expressionList = 52, 
		RULE_expression = 53, RULE_primary = 54, RULE_explicitGenericInvocation = 55, 
		RULE_explicitGenericInvocationSuffix = 56, RULE_superSuffix = 57, RULE_arguments = 58, 
		RULE_classType = 59, RULE_methodCall = 60, RULE_functionCall = 61, RULE_literal = 62, 
		RULE_integerLiteral = 63, RULE_floatLiteral = 64, RULE_typeTypeOrVoid = 65, 
		RULE_typeType = 66, RULE_arrayKind = 67, RULE_classOrInterfaceType = 68, 
		RULE_typeArguments = 69, RULE_primitiveType = 70, RULE_modifier = 71, 
		RULE_classOrInterfaceModifier = 72, RULE_lambda = 73, RULE_lambdaParameters = 74, 
		RULE_lambdaBody = 75;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "typeDeclaration", "classDeclaration", "classBody", 
			"typeList", "classBodyDeclaration", "enumDeclaration", "enumConstants", 
			"enumConstant", "enumBodyDeclarations", "interfaceDeclaration", "interfaceBody", 
			"interfaceBodyDeclaration", "interfaceMemberDeclaration", "interfaceMethodDeclaration", 
			"interfaceMethodModifier", "interfaceCommonBodyDeclaration", "memberDeclaration", 
			"fieldDeclaration", "methodDeclaration", "constructorDeclaration", "typeParameters", 
			"qualifiedNameList", "qualifiedName", "typeParameter", "formalParameters", 
			"receiverParameter", "formalParameterList", "formalParameter", "methodBody", 
			"block", "labeledStatement", "statement", "forControl", "loopVariableDeclarators", 
			"loopVariableDeclarator", "loopVariableUpdates", "loopVariableUpdate", 
			"qualifiedFieldName", "creator", "arrayCreatorRest", "arrayInitializer", 
			"variableInitializer", "createdName", "classCreatorRest", "catchClause", 
			"catchFields", "catchField", "catchValue", "branchCase", "switchLabel", 
			"parExpression", "expressionList", "expression", "primary", "explicitGenericInvocation", 
			"explicitGenericInvocationSuffix", "superSuffix", "arguments", "classType", 
			"methodCall", "functionCall", "literal", "integerLiteral", "floatLiteral", 
			"typeTypeOrVoid", "typeType", "arrayKind", "classOrInterfaceType", "typeArguments", 
			"primitiveType", "modifier", "classOrInterfaceModifier", "lambda", "lambdaParameters", 
			"lambdaBody"
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
			"'this'", "'throw'", "'throws'", "'try'", "'void'", "'while'", "'any'", 
			"'never'", "'='", "'>'", "'<'", "'!'", "'~'", "'?'", "':'", "'=='", "'<='", 
			"'>='", "'!='", "'&&'", "'||'", "'++'", "'--'", "'+'", "'-'", "'*'", 
			"'/'", "'&'", "'|'", "'^'", "'%'", "'+='", "'-='", "'*='", "'/='", "'&='", 
			"'|='", "'^='", "'%='", "'<<='", "'>>='", "'>>>='", "'('", "')'", "'{'", 
			"'}'", "'['", "']'", "';'", "','", "'.'", "'->'", "'::'", "'@'", "'...'", 
			null, null, null, null, null, null, null, null, null, null, "'r'", "'rw'", 
			"'c'"
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
			"TRY", "VOID", "WHILE", "ANY", "NEVER", "ASSIGN", "GT", "LT", "BANG", 
			"TILDE", "QUESTION", "COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", 
			"OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", 
			"MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", 
			"OR_ASSIGN", "XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", 
			"URSHIFT_ASSIGN", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", 
			"SEMI", "COMMA", "DOT", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "DECIMAL_LITERAL", 
			"HEX_LITERAL", "OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", 
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
			setState(153); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(152);
				typeDeclaration();
				}
				}
				setState(155); 
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
			setState(169);
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
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 24696061954L) != 0)) {
					{
					{
					setState(157);
					classOrInterfaceModifier();
					}
					}
					setState(162);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(166);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CLASS:
				case STRUCT:
					{
					setState(163);
					classDeclaration();
					}
					break;
				case ENUM:
					{
					setState(164);
					enumDeclaration();
					}
					break;
				case INTERFACE:
					{
					setState(165);
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
				setState(168);
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
			setState(171);
			_la = _input.LA(1);
			if ( !(_la==CLASS || _la==STRUCT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(172);
			match(IDENTIFIER);
			setState(174);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(173);
				typeParameters();
				}
			}

			setState(178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(176);
				match(EXTENDS);
				setState(177);
				typeType(0);
				}
			}

			setState(182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(180);
				match(IMPLEMENTS);
				setState(181);
				typeList();
				}
			}

			setState(184);
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
			setState(186);
			match(LBRACE);
			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 169349571092038L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108881L) != 0)) {
				{
				{
				setState(187);
				classBodyDeclaration();
				}
				}
				setState(192);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(193);
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
			setState(195);
			typeType(0);
			setState(200);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(196);
				match(COMMA);
				setState(197);
				typeType(0);
				}
				}
				setState(202);
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
			setState(206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 24763629570L) != 0)) {
				{
				{
				setState(203);
				modifier();
				}
				}
				setState(208);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(209);
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
			setState(211);
			match(ENUM);
			setState(212);
			match(IDENTIFIER);
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
			match(LBRACE);
			setState(219);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(218);
				enumConstants();
				}
			}

			setState(222);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(221);
				match(COMMA);
				}
			}

			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(224);
				enumBodyDeclarations();
				}
			}

			setState(227);
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
			setState(229);
			enumConstant();
			setState(234);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(230);
					match(COMMA);
					setState(231);
					enumConstant();
					}
					} 
				}
				setState(236);
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
			setState(237);
			match(IDENTIFIER);
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(238);
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
			setState(241);
			match(SEMI);
			setState(245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 169349571092038L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108881L) != 0)) {
				{
				{
				setState(242);
				classBodyDeclaration();
				}
				}
				setState(247);
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
			setState(248);
			match(INTERFACE);
			setState(249);
			match(IDENTIFIER);
			setState(251);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(250);
				typeParameters();
				}
			}

			setState(255);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(253);
				match(EXTENDS);
				setState(254);
				typeList();
				}
			}

			setState(257);
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
			setState(259);
			match(LBRACE);
			setState(263);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 169349571092054L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108945L) != 0)) {
				{
				{
				setState(260);
				interfaceBodyDeclaration();
				}
				}
				setState(265);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(266);
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
			setState(276);
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
			case ANY:
			case NEVER:
			case LT:
			case LPAREN:
			case LBRACK:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(271);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(268);
						modifier();
						}
						} 
					}
					setState(273);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				}
				setState(274);
				interfaceMemberDeclaration();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(275);
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
		enterRule(_localctx, 26, RULE_interfaceMemberDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
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
			setState(287);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(286);
				typeParameters();
				}
			}

			setState(289);
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
			setState(291);
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
		enterRule(_localctx, 32, RULE_interfaceCommonBodyDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(293);
			typeTypeOrVoid();
			setState(294);
			match(IDENTIFIER);
			setState(295);
			formalParameters();
			setState(300);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(296);
				match(LBRACK);
				setState(297);
				match(RBRACK);
				}
				}
				setState(302);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(305);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(303);
				match(THROWS);
				setState(304);
				qualifiedNameList();
				}
			}

			setState(307);
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
		enterRule(_localctx, 34, RULE_memberDeclaration);
		try {
			setState(312);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(309);
				methodDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(310);
				fieldDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(311);
				constructorDeclaration();
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
		enterRule(_localctx, 36, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(314);
			typeType(0);
			setState(315);
			match(IDENTIFIER);
			setState(316);
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
		enterRule(_localctx, 38, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(319);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(318);
				typeParameters();
				}
			}

			setState(321);
			typeTypeOrVoid();
			setState(322);
			match(IDENTIFIER);
			setState(323);
			formalParameters();
			setState(324);
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
		enterRule(_localctx, 40, RULE_constructorDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(326);
				typeParameters();
				}
			}

			setState(329);
			match(IDENTIFIER);
			setState(330);
			formalParameters();
			setState(333);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(331);
				match(THROWS);
				setState(332);
				qualifiedNameList();
				}
			}

			setState(335);
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
		enterRule(_localctx, 42, RULE_typeParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			match(LT);
			setState(338);
			typeParameter();
			setState(343);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(339);
				match(COMMA);
				setState(340);
				typeParameter();
				}
				}
				setState(345);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(346);
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
		enterRule(_localctx, 44, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			qualifiedName();
			setState(353);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(349);
				match(COMMA);
				setState(350);
				qualifiedName();
				}
				}
				setState(355);
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
		enterRule(_localctx, 46, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(356);
			match(IDENTIFIER);
			setState(361);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(357);
					match(DOT);
					setState(358);
					match(IDENTIFIER);
					}
					} 
				}
				setState(363);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
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
		enterRule(_localctx, 48, RULE_typeParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(364);
			match(IDENTIFIER);
			setState(367);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(365);
				match(EXTENDS);
				setState(366);
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
		enterRule(_localctx, 50, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			match(LPAREN);
			setState(371);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 28587319107140L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108881L) != 0)) {
				{
				setState(370);
				formalParameterList();
				}
			}

			setState(373);
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
		enterRule(_localctx, 52, RULE_receiverParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(375);
			typeType(0);
			setState(380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(376);
				match(IDENTIFIER);
				setState(377);
				match(DOT);
				}
				}
				setState(382);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(383);
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
		enterRule(_localctx, 54, RULE_formalParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(385);
			formalParameter();
			setState(390);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(386);
				match(COMMA);
				setState(387);
				formalParameter();
				}
				}
				setState(392);
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
		enterRule(_localctx, 56, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			typeType(0);
			setState(394);
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
		enterRule(_localctx, 58, RULE_methodBody);
		try {
			setState(398);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(396);
				block();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(397);
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
		enterRule(_localctx, 60, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			match(LBRACE);
			setState(404);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324306089692627968L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489345L) != 0)) {
				{
				{
				setState(401);
				labeledStatement();
				}
				}
				setState(406);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(407);
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
		enterRule(_localctx, 62, RULE_labeledStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(411);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				{
				setState(409);
				match(IDENTIFIER);
				setState(410);
				match(COLON);
				}
				break;
			}
			setState(413);
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
		enterRule(_localctx, 64, RULE_statement);
		int _la;
		try {
			setState(473);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(415);
				match(WHILE);
				setState(416);
				parExpression();
				setState(417);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(419);
				match(FOR);
				setState(420);
				match(LPAREN);
				setState(421);
				forControl();
				setState(422);
				match(RPAREN);
				setState(423);
				block();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(425);
				match(IF);
				setState(426);
				parExpression();
				setState(427);
				block();
				setState(430);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(428);
					match(ELSE);
					setState(429);
					block();
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(432);
				match(TRY);
				setState(433);
				block();
				setState(434);
				catchClause();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(436);
				match(SWITCH);
				setState(437);
				match(LBRACE);
				setState(441);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(438);
					branchCase();
					}
					}
					setState(443);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(444);
				match(RBRACE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(445);
				match(RETURN);
				setState(447);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
					{
					setState(446);
					expression(0);
					}
				}

				setState(449);
				match(SEMI);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(450);
				match(THROW);
				setState(451);
				expression(0);
				setState(452);
				match(SEMI);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(454);
				match(SEMI);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(455);
				methodCall();
				setState(456);
				match(SEMI);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(458);
				functionCall();
				setState(459);
				match(SEMI);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(461);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 939524096L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(462);
				creator();
				setState(463);
				match(SEMI);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(465);
				_la = _input.LA(1);
				if ( !(_la==THIS || _la==IDENTIFIER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(466);
				match(DOT);
				setState(467);
				match(IDENTIFIER);
				setState(468);
				((StatementContext)_localctx).bop = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 45)) & ~0x3f) == 0 && ((1L << (_la - 45)) & 17171480577L) != 0)) ) {
					((StatementContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(469);
				expression(0);
				setState(470);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(472);
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
		enterRule(_localctx, 66, RULE_forControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(476);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 28587319107140L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108881L) != 0)) {
				{
				setState(475);
				loopVariableDeclarators();
				}
			}

			setState(478);
			match(SEMI);
			setState(480);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
				{
				setState(479);
				expression(0);
				}
			}

			setState(482);
			match(SEMI);
			setState(484);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(483);
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
		enterRule(_localctx, 68, RULE_loopVariableDeclarators);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(486);
			loopVariableDeclarator();
			setState(491);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(487);
				match(COMMA);
				setState(488);
				loopVariableDeclarator();
				}
				}
				setState(493);
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
		enterRule(_localctx, 70, RULE_loopVariableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(494);
			typeType(0);
			setState(495);
			match(IDENTIFIER);
			setState(496);
			match(ASSIGN);
			setState(497);
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
		enterRule(_localctx, 72, RULE_loopVariableUpdates);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(499);
			loopVariableUpdate();
			setState(504);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(500);
				match(COMMA);
				setState(501);
				loopVariableUpdate();
				}
				}
				setState(506);
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
		enterRule(_localctx, 74, RULE_loopVariableUpdate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(507);
			match(IDENTIFIER);
			setState(508);
			match(ASSIGN);
			setState(509);
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
		enterRule(_localctx, 76, RULE_qualifiedFieldName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(511);
			qualifiedName();
			setState(512);
			match(DOT);
			setState(513);
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
		enterRule(_localctx, 78, RULE_creator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(515);
				typeArguments();
				}
			}

			setState(518);
			classOrInterfaceType();
			setState(519);
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
		enterRule(_localctx, 80, RULE_arrayCreatorRest);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(521);
			match(LBRACK);
			setState(522);
			match(RBRACK);
			setState(527);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(523);
				match(LBRACK);
				setState(524);
				match(RBRACK);
				}
				}
				setState(529);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(530);
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
		enterRule(_localctx, 82, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(532);
			match(LBRACE);
			setState(544);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489285L) != 0)) {
				{
				setState(533);
				variableInitializer();
				setState(538);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(534);
						match(COMMA);
						setState(535);
						variableInitializer();
						}
						} 
					}
					setState(540);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
				}
				setState(542);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(541);
					match(COMMA);
					}
				}

				}
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
		enterRule(_localctx, 84, RULE_variableInitializer);
		try {
			setState(550);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(548);
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
				setState(549);
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
		enterRule(_localctx, 86, RULE_createdName);
		int _la;
		try {
			setState(557);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(552);
				match(IDENTIFIER);
				setState(554);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(553);
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
				setState(556);
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
		enterRule(_localctx, 88, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(559);
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
		enterRule(_localctx, 90, RULE_catchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(561);
			match(CATCH);
			setState(562);
			match(LBRACE);
			setState(564);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(563);
				catchFields();
				}
			}

			setState(566);
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
		enterRule(_localctx, 92, RULE_catchFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(568);
			catchField();
			setState(573);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(569);
				match(COMMA);
				setState(570);
				catchField();
				}
				}
				setState(575);
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
		enterRule(_localctx, 94, RULE_catchField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(576);
			match(IDENTIFIER);
			setState(577);
			match(COLON);
			setState(578);
			match(LBRACE);
			setState(584);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(579);
				catchValue();
				setState(580);
				match(COMMA);
				}
				}
				setState(586);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(587);
			match(DEFAULT);
			setState(588);
			match(COLON);
			setState(589);
			expression(0);
			setState(590);
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
		enterRule(_localctx, 96, RULE_catchValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(592);
			match(IDENTIFIER);
			setState(593);
			match(COLON);
			setState(594);
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
		enterRule(_localctx, 98, RULE_branchCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(596);
			switchLabel();
			setState(597);
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
		enterRule(_localctx, 100, RULE_switchLabel);
		try {
			setState(605);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(599);
				match(CASE);
				{
				setState(600);
				expression(0);
				}
				setState(601);
				match(ARROW);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(603);
				match(DEFAULT);
				setState(604);
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
		enterRule(_localctx, 102, RULE_parExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(607);
			match(LPAREN);
			setState(608);
			expression(0);
			setState(609);
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
		enterRule(_localctx, 104, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(611);
			expression(0);
			setState(616);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(612);
				match(COMMA);
				setState(613);
				expression(0);
				}
				}
				setState(618);
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
		int _startState = 106;
		enterRecursionRule(_localctx, 106, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(632);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				{
				setState(620);
				primary();
				}
				break;
			case 2:
				{
				setState(621);
				match(LPAREN);
				setState(622);
				typeType(0);
				setState(623);
				match(RPAREN);
				setState(624);
				expression(18);
				}
				break;
			case 3:
				{
				setState(626);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 4323455642275676160L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(627);
				expression(16);
				}
				break;
			case 4:
				{
				setState(628);
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
				setState(629);
				expression(15);
				}
				break;
			case 5:
				{
				setState(630);
				match(IDENTIFIER);
				setState(631);
				arguments();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(699);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,66,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(697);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(634);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(635);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 62)) & ~0x3f) == 0 && ((1L << (_la - 62)) & 35L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
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
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(637);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(638);
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
						setState(639);
						expression(14);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(640);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(648);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
						case 1:
							{
							setState(641);
							match(LT);
							setState(642);
							match(LT);
							}
							break;
						case 2:
							{
							setState(643);
							match(GT);
							setState(644);
							match(GT);
							setState(645);
							match(GT);
							}
							break;
						case 3:
							{
							setState(646);
							match(GT);
							setState(647);
							match(GT);
							}
							break;
						}
						setState(650);
						expression(13);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(651);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(652);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 27232703996755968L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(653);
						expression(12);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(654);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(655);
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
						setState(656);
						expression(10);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(657);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(658);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(659);
						expression(9);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(660);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(661);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(662);
						expression(8);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(663);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(664);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(665);
						expression(7);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(666);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(667);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(668);
						expression(6);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(669);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(670);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(671);
						expression(5);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(672);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(673);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(674);
						expression(0);
						setState(675);
						match(COLON);
						setState(676);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(678);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(679);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(680);
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
						setState(681);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(682);
						match(LBRACK);
						setState(683);
						expression(0);
						setState(684);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(686);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(687);
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
						setState(688);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(689);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(690);
						typeType(0);
						}
						break;
					case 16:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(691);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(692);
						match(COLONCOLON);
						setState(694);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(693);
							typeArguments();
							}
						}

						setState(696);
						match(IDENTIFIER);
						}
						break;
					}
					} 
				}
				setState(701);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,66,_ctx);
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
		enterRule(_localctx, 108, RULE_primary);
		try {
			setState(709);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(702);
				match(LPAREN);
				setState(703);
				expression(0);
				setState(704);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(706);
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
				setState(707);
				literal();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 4);
				{
				setState(708);
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
		enterRule(_localctx, 110, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(711);
			typeArguments();
			setState(712);
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
		enterRule(_localctx, 112, RULE_explicitGenericInvocationSuffix);
		try {
			setState(718);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(714);
				match(SUPER);
				setState(715);
				superSuffix();
				}
				break;
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(716);
				match(IDENTIFIER);
				setState(717);
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
		enterRule(_localctx, 114, RULE_superSuffix);
		int _la;
		try {
			setState(729);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(720);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(721);
				match(DOT);
				setState(723);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(722);
					typeArguments();
					}
				}

				setState(725);
				match(IDENTIFIER);
				setState(727);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(726);
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
		enterRule(_localctx, 116, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(731);
			match(LPAREN);
			setState(733);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
				{
				setState(732);
				expressionList();
				}
			}

			setState(735);
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
		enterRule(_localctx, 118, RULE_classType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(737);
			match(IDENTIFIER);
			setState(739);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(738);
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
		enterRule(_localctx, 120, RULE_methodCall);
		int _la;
		try {
			setState(765);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(741);
				expression(0);
				setState(742);
				match(DOT);
				setState(744);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(743);
					typeArguments();
					}
				}

				setState(746);
				match(IDENTIFIER);
				setState(747);
				match(LPAREN);
				setState(749);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
					{
					setState(748);
					expressionList();
					}
				}

				setState(751);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(753);
				match(THIS);
				setState(754);
				match(LPAREN);
				setState(756);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
					{
					setState(755);
					expressionList();
					}
				}

				setState(758);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(759);
				match(SUPER);
				setState(760);
				match(LPAREN);
				setState(762);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
					{
					setState(761);
					expressionList();
					}
				}

				setState(764);
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
		enterRule(_localctx, 122, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(767);
			expression(0);
			setState(768);
			match(LPAREN);
			setState(770);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4324300204644762624L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 75489281L) != 0)) {
				{
				setState(769);
				expressionList();
				}
			}

			setState(772);
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
		enterRule(_localctx, 124, RULE_literal);
		try {
			setState(781);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(774);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(775);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(776);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(777);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(778);
				match(BOOL_LITERAL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(779);
				match(NULL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(780);
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
		enterRule(_localctx, 126, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(783);
			_la = _input.LA(1);
			if ( !(((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & 15L) != 0)) ) {
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
		enterRule(_localctx, 128, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(785);
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
		enterRule(_localctx, 130, RULE_typeTypeOrVoid);
		try {
			setState(789);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(787);
				typeType(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(788);
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
		int _startState = 132;
		enterRecursionRule(_localctx, 132, RULE_typeType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(816);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(792);
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
				setState(793);
				primitiveType();
				}
				break;
			case ANY:
				{
				setState(794);
				match(ANY);
				}
				break;
			case NEVER:
				{
				setState(795);
				match(NEVER);
				}
				break;
			case LPAREN:
				{
				setState(796);
				match(LPAREN);
				setState(805);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 28587319107140L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108881L) != 0)) {
					{
					setState(797);
					typeType(0);
					setState(802);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(798);
						match(COMMA);
						setState(799);
						typeType(0);
						}
						}
						setState(804);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(807);
				match(RPAREN);
				setState(808);
				match(ARROW);
				setState(809);
				typeType(2);
				}
				break;
			case LBRACK:
				{
				setState(810);
				match(LBRACK);
				setState(811);
				typeType(0);
				setState(812);
				match(COMMA);
				setState(813);
				typeType(0);
				setState(814);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(839);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(837);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
					case 1:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(818);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(821); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(819);
								match(BITOR);
								setState(820);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(823); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(825);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(828); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(826);
								match(BITAND);
								setState(827);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(830); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,86,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(832);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(833);
						match(LBRACK);
						setState(834);
						arrayKind();
						setState(835);
						match(RBRACK);
						}
						break;
					}
					} 
				}
				setState(841);
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
		enterRule(_localctx, 134, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(842);
			_la = _input.LA(1);
			if ( !(((((_la - 102)) & ~0x3f) == 0 && ((1L << (_la - 102)) & 7L) != 0)) ) {
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
		enterRule(_localctx, 136, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(844);
			qualifiedName();
			setState(846);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				{
				setState(845);
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
		enterRule(_localctx, 138, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(848);
			match(LT);
			setState(849);
			typeType(0);
			setState(854);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(850);
				match(COMMA);
				setState(851);
				typeType(0);
				}
				}
				setState(856);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(857);
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
		enterRule(_localctx, 140, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(859);
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
		enterRule(_localctx, 142, RULE_modifier);
		try {
			setState(866);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
				enterOuterAlt(_localctx, 1);
				{
				setState(861);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(862);
				match(NATIVE);
				}
				break;
			case READONLY:
				enterOuterAlt(_localctx, 3);
				{
				setState(863);
				match(READONLY);
				}
				break;
			case CHILD:
				enterOuterAlt(_localctx, 4);
				{
				setState(864);
				match(CHILD);
				}
				break;
			case TITLE:
				enterOuterAlt(_localctx, 5);
				{
				setState(865);
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
		enterRule(_localctx, 144, RULE_classOrInterfaceModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(868);
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
		enterRule(_localctx, 146, RULE_lambda);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(870);
			lambdaParameters();
			setState(871);
			match(COLON);
			setState(872);
			typeTypeOrVoid();
			setState(873);
			match(ARROW);
			setState(874);
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
		enterRule(_localctx, 148, RULE_lambdaParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(876);
			match(LPAREN);
			setState(878);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 28587319107140L) != 0) || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 67108881L) != 0)) {
				{
				setState(877);
				formalParameterList();
				}
			}

			setState(880);
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
		enterRule(_localctx, 150, RULE_lambdaBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(882);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 53:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 66:
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
		"\u0004\u0001l\u0375\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"K\u0007K\u0001\u0000\u0004\u0000\u009a\b\u0000\u000b\u0000\f\u0000\u009b"+
		"\u0001\u0001\u0005\u0001\u009f\b\u0001\n\u0001\f\u0001\u00a2\t\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00a7\b\u0001\u0001\u0001\u0003"+
		"\u0001\u00aa\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u00af"+
		"\b\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u00b3\b\u0002\u0001\u0002"+
		"\u0001\u0002\u0003\u0002\u00b7\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0005\u0003\u00bd\b\u0003\n\u0003\f\u0003\u00c0\t\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004\u00c7"+
		"\b\u0004\n\u0004\f\u0004\u00ca\t\u0004\u0001\u0005\u0005\u0005\u00cd\b"+
		"\u0005\n\u0005\f\u0005\u00d0\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u00d8\b\u0006\u0001\u0006"+
		"\u0001\u0006\u0003\u0006\u00dc\b\u0006\u0001\u0006\u0003\u0006\u00df\b"+
		"\u0006\u0001\u0006\u0003\u0006\u00e2\b\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0005\u0007\u00e9\b\u0007\n\u0007\f\u0007"+
		"\u00ec\t\u0007\u0001\b\u0001\b\u0003\b\u00f0\b\b\u0001\t\u0001\t\u0005"+
		"\t\u00f4\b\t\n\t\f\t\u00f7\t\t\u0001\n\u0001\n\u0001\n\u0003\n\u00fc\b"+
		"\n\u0001\n\u0001\n\u0003\n\u0100\b\n\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u0106\b\u000b\n\u000b\f\u000b\u0109\t\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\f\u0005\f\u010e\b\f\n\f\f\f\u0111\t\f\u0001\f\u0001"+
		"\f\u0003\f\u0115\b\f\u0001\r\u0001\r\u0001\u000e\u0005\u000e\u011a\b\u000e"+
		"\n\u000e\f\u000e\u011d\t\u000e\u0001\u000e\u0003\u000e\u0120\b\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u012b\b\u0010\n\u0010\f\u0010"+
		"\u012e\t\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0132\b\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0139"+
		"\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0003"+
		"\u0013\u0140\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0014\u0003\u0014\u0148\b\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0003\u0014\u014e\b\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0156\b\u0015\n"+
		"\u0015\f\u0015\u0159\t\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0005\u0016\u0160\b\u0016\n\u0016\f\u0016\u0163\t\u0016"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u0168\b\u0017\n\u0017"+
		"\f\u0017\u016b\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018"+
		"\u0170\b\u0018\u0001\u0019\u0001\u0019\u0003\u0019\u0174\b\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a\u017b"+
		"\b\u001a\n\u001a\f\u001a\u017e\t\u001a\u0001\u001a\u0001\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0005\u001b\u0185\b\u001b\n\u001b\f\u001b\u0188"+
		"\t\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0003"+
		"\u001d\u018f\b\u001d\u0001\u001e\u0001\u001e\u0005\u001e\u0193\b\u001e"+
		"\n\u001e\f\u001e\u0196\t\u001e\u0001\u001e\u0001\u001e\u0001\u001f\u0001"+
		"\u001f\u0003\u001f\u019c\b\u001f\u0001\u001f\u0001\u001f\u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0003 \u01af\b \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0005 \u01b8\b \n \f \u01bb\t \u0001 \u0001 \u0001 \u0003"+
		" \u01c0\b \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		" \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u01da\b \u0001!\u0003!\u01dd"+
		"\b!\u0001!\u0001!\u0003!\u01e1\b!\u0001!\u0001!\u0003!\u01e5\b!\u0001"+
		"\"\u0001\"\u0001\"\u0005\"\u01ea\b\"\n\"\f\"\u01ed\t\"\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001$\u0001$\u0001$\u0005$\u01f7\b$\n$\f$\u01fa\t$\u0001"+
		"%\u0001%\u0001%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001\'\u0003\'\u0205"+
		"\b\'\u0001\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0001(\u0005(\u020e"+
		"\b(\n(\f(\u0211\t(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001)\u0005)\u0219"+
		"\b)\n)\f)\u021c\t)\u0001)\u0003)\u021f\b)\u0003)\u0221\b)\u0001)\u0001"+
		")\u0001*\u0001*\u0003*\u0227\b*\u0001+\u0001+\u0003+\u022b\b+\u0001+\u0003"+
		"+\u022e\b+\u0001,\u0001,\u0001-\u0001-\u0001-\u0003-\u0235\b-\u0001-\u0001"+
		"-\u0001.\u0001.\u0001.\u0005.\u023c\b.\n.\f.\u023f\t.\u0001/\u0001/\u0001"+
		"/\u0001/\u0001/\u0001/\u0005/\u0247\b/\n/\f/\u024a\t/\u0001/\u0001/\u0001"+
		"/\u0001/\u0001/\u00010\u00010\u00010\u00010\u00011\u00011\u00011\u0001"+
		"2\u00012\u00012\u00012\u00012\u00012\u00032\u025e\b2\u00013\u00013\u0001"+
		"3\u00013\u00014\u00014\u00014\u00054\u0267\b4\n4\f4\u026a\t4\u00015\u0001"+
		"5\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00035\u0279\b5\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00035\u0289\b5\u0001"+
		"5\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00015\u00015\u00035\u02b7\b5\u00015\u00055\u02ba\b5\n5\f5\u02bd"+
		"\t5\u00016\u00016\u00016\u00016\u00016\u00016\u00016\u00036\u02c6\b6\u0001"+
		"7\u00017\u00017\u00018\u00018\u00018\u00018\u00038\u02cf\b8\u00019\u0001"+
		"9\u00019\u00039\u02d4\b9\u00019\u00019\u00039\u02d8\b9\u00039\u02da\b"+
		"9\u0001:\u0001:\u0003:\u02de\b:\u0001:\u0001:\u0001;\u0001;\u0003;\u02e4"+
		"\b;\u0001<\u0001<\u0001<\u0003<\u02e9\b<\u0001<\u0001<\u0001<\u0003<\u02ee"+
		"\b<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<\u02f5\b<\u0001<\u0001<\u0001"+
		"<\u0001<\u0003<\u02fb\b<\u0001<\u0003<\u02fe\b<\u0001=\u0001=\u0001=\u0003"+
		"=\u0303\b=\u0001=\u0001=\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001"+
		">\u0003>\u030e\b>\u0001?\u0001?\u0001@\u0001@\u0001A\u0001A\u0003A\u0316"+
		"\bA\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0001B\u0005"+
		"B\u0321\bB\nB\fB\u0324\tB\u0003B\u0326\bB\u0001B\u0001B\u0001B\u0001B"+
		"\u0001B\u0001B\u0001B\u0001B\u0001B\u0003B\u0331\bB\u0001B\u0001B\u0001"+
		"B\u0004B\u0336\bB\u000bB\fB\u0337\u0001B\u0001B\u0001B\u0004B\u033d\b"+
		"B\u000bB\fB\u033e\u0001B\u0001B\u0001B\u0001B\u0001B\u0005B\u0346\bB\n"+
		"B\fB\u0349\tB\u0001C\u0001C\u0001D\u0001D\u0003D\u034f\bD\u0001E\u0001"+
		"E\u0001E\u0001E\u0005E\u0355\bE\nE\fE\u0358\tE\u0001E\u0001E\u0001F\u0001"+
		"F\u0001G\u0001G\u0001G\u0001G\u0001G\u0003G\u0363\bG\u0001H\u0001H\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001J\u0001J\u0003J\u036f\bJ\u0001"+
		"J\u0001J\u0001K\u0001K\u0001K\u0000\u0002j\u0084L\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088"+
		"\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0000\u0011\u0001\u0000\u0007"+
		"\b\u0004\u0000\u0001\u0001\u0004\u0004  \"\"\u0001\u0000\u001b\u001d\u0002"+
		"\u0000%%ii\u0002\u0000--DN\u0001\u0000:=\u0001\u000001\u0002\u0000>?C"+
		"C\u0001\u0000<=\u0002\u0000./56\u0002\u00004477\u0001\u0000:;\u0001\u0000"+
		"\\_\u0001\u0000`a\u0001\u0000fh\u0005\u0000\u0002\u0002\u0006\u0006\t"+
		"\f\u0018\u0018))\u0003\u0000\u0001\u0001\u001e \"\"\u03b4\u0000\u0099"+
		"\u0001\u0000\u0000\u0000\u0002\u00a9\u0001\u0000\u0000\u0000\u0004\u00ab"+
		"\u0001\u0000\u0000\u0000\u0006\u00ba\u0001\u0000\u0000\u0000\b\u00c3\u0001"+
		"\u0000\u0000\u0000\n\u00ce\u0001\u0000\u0000\u0000\f\u00d3\u0001\u0000"+
		"\u0000\u0000\u000e\u00e5\u0001\u0000\u0000\u0000\u0010\u00ed\u0001\u0000"+
		"\u0000\u0000\u0012\u00f1\u0001\u0000\u0000\u0000\u0014\u00f8\u0001\u0000"+
		"\u0000\u0000\u0016\u0103\u0001\u0000\u0000\u0000\u0018\u0114\u0001\u0000"+
		"\u0000\u0000\u001a\u0116\u0001\u0000\u0000\u0000\u001c\u011b\u0001\u0000"+
		"\u0000\u0000\u001e\u0123\u0001\u0000\u0000\u0000 \u0125\u0001\u0000\u0000"+
		"\u0000\"\u0138\u0001\u0000\u0000\u0000$\u013a\u0001\u0000\u0000\u0000"+
		"&\u013f\u0001\u0000\u0000\u0000(\u0147\u0001\u0000\u0000\u0000*\u0151"+
		"\u0001\u0000\u0000\u0000,\u015c\u0001\u0000\u0000\u0000.\u0164\u0001\u0000"+
		"\u0000\u00000\u016c\u0001\u0000\u0000\u00002\u0171\u0001\u0000\u0000\u0000"+
		"4\u0177\u0001\u0000\u0000\u00006\u0181\u0001\u0000\u0000\u00008\u0189"+
		"\u0001\u0000\u0000\u0000:\u018e\u0001\u0000\u0000\u0000<\u0190\u0001\u0000"+
		"\u0000\u0000>\u019b\u0001\u0000\u0000\u0000@\u01d9\u0001\u0000\u0000\u0000"+
		"B\u01dc\u0001\u0000\u0000\u0000D\u01e6\u0001\u0000\u0000\u0000F\u01ee"+
		"\u0001\u0000\u0000\u0000H\u01f3\u0001\u0000\u0000\u0000J\u01fb\u0001\u0000"+
		"\u0000\u0000L\u01ff\u0001\u0000\u0000\u0000N\u0204\u0001\u0000\u0000\u0000"+
		"P\u0209\u0001\u0000\u0000\u0000R\u0214\u0001\u0000\u0000\u0000T\u0226"+
		"\u0001\u0000\u0000\u0000V\u022d\u0001\u0000\u0000\u0000X\u022f\u0001\u0000"+
		"\u0000\u0000Z\u0231\u0001\u0000\u0000\u0000\\\u0238\u0001\u0000\u0000"+
		"\u0000^\u0240\u0001\u0000\u0000\u0000`\u0250\u0001\u0000\u0000\u0000b"+
		"\u0254\u0001\u0000\u0000\u0000d\u025d\u0001\u0000\u0000\u0000f\u025f\u0001"+
		"\u0000\u0000\u0000h\u0263\u0001\u0000\u0000\u0000j\u0278\u0001\u0000\u0000"+
		"\u0000l\u02c5\u0001\u0000\u0000\u0000n\u02c7\u0001\u0000\u0000\u0000p"+
		"\u02ce\u0001\u0000\u0000\u0000r\u02d9\u0001\u0000\u0000\u0000t\u02db\u0001"+
		"\u0000\u0000\u0000v\u02e1\u0001\u0000\u0000\u0000x\u02fd\u0001\u0000\u0000"+
		"\u0000z\u02ff\u0001\u0000\u0000\u0000|\u030d\u0001\u0000\u0000\u0000~"+
		"\u030f\u0001\u0000\u0000\u0000\u0080\u0311\u0001\u0000\u0000\u0000\u0082"+
		"\u0315\u0001\u0000\u0000\u0000\u0084\u0330\u0001\u0000\u0000\u0000\u0086"+
		"\u034a\u0001\u0000\u0000\u0000\u0088\u034c\u0001\u0000\u0000\u0000\u008a"+
		"\u0350\u0001\u0000\u0000\u0000\u008c\u035b\u0001\u0000\u0000\u0000\u008e"+
		"\u0362\u0001\u0000\u0000\u0000\u0090\u0364\u0001\u0000\u0000\u0000\u0092"+
		"\u0366\u0001\u0000\u0000\u0000\u0094\u036c\u0001\u0000\u0000\u0000\u0096"+
		"\u0372\u0001\u0000\u0000\u0000\u0098\u009a\u0003\u0002\u0001\u0000\u0099"+
		"\u0098\u0001\u0000\u0000\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b"+
		"\u0099\u0001\u0000\u0000\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c"+
		"\u0001\u0001\u0000\u0000\u0000\u009d\u009f\u0003\u0090H\u0000\u009e\u009d"+
		"\u0001\u0000\u0000\u0000\u009f\u00a2\u0001\u0000\u0000\u0000\u00a0\u009e"+
		"\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1\u00a6"+
		"\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000\u0000\u0000\u00a3\u00a7"+
		"\u0003\u0004\u0002\u0000\u00a4\u00a7\u0003\f\u0006\u0000\u00a5\u00a7\u0003"+
		"\u0014\n\u0000\u00a6\u00a3\u0001\u0000\u0000\u0000\u00a6\u00a4\u0001\u0000"+
		"\u0000\u0000\u00a6\u00a5\u0001\u0000\u0000\u0000\u00a7\u00aa\u0001\u0000"+
		"\u0000\u0000\u00a8\u00aa\u0005U\u0000\u0000\u00a9\u00a0\u0001\u0000\u0000"+
		"\u0000\u00a9\u00a8\u0001\u0000\u0000\u0000\u00aa\u0003\u0001\u0000\u0000"+
		"\u0000\u00ab\u00ac\u0007\u0000\u0000\u0000\u00ac\u00ae\u0005i\u0000\u0000"+
		"\u00ad\u00af\u0003*\u0015\u0000\u00ae\u00ad\u0001\u0000\u0000\u0000\u00ae"+
		"\u00af\u0001\u0000\u0000\u0000\u00af\u00b2\u0001\u0000\u0000\u0000\u00b0"+
		"\u00b1\u0005\u000f\u0000\u0000\u00b1\u00b3\u0003\u0084B\u0000\u00b2\u00b0"+
		"\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000\u00b3\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b4\u00b5\u0005\u0016\u0000\u0000\u00b5\u00b7"+
		"\u0003\b\u0004\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001"+
		"\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8\u00b9\u0003"+
		"\u0006\u0003\u0000\u00b9\u0005\u0001\u0000\u0000\u0000\u00ba\u00be\u0005"+
		"Q\u0000\u0000\u00bb\u00bd\u0003\n\u0005\u0000\u00bc\u00bb\u0001\u0000"+
		"\u0000\u0000\u00bd\u00c0\u0001\u0000\u0000\u0000\u00be\u00bc\u0001\u0000"+
		"\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00bf\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c1\u00c2\u0005R\u0000"+
		"\u0000\u00c2\u0007\u0001\u0000\u0000\u0000\u00c3\u00c8\u0003\u0084B\u0000"+
		"\u00c4\u00c5\u0005V\u0000\u0000\u00c5\u00c7\u0003\u0084B\u0000\u00c6\u00c4"+
		"\u0001\u0000\u0000\u0000\u00c7\u00ca\u0001\u0000\u0000\u0000\u00c8\u00c6"+
		"\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000\u00c9\t\u0001"+
		"\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00cb\u00cd\u0003"+
		"\u008eG\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cd\u00d0\u0001\u0000"+
		"\u0000\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000"+
		"\u0000\u0000\u00cf\u00d1\u0001\u0000\u0000\u0000\u00d0\u00ce\u0001\u0000"+
		"\u0000\u0000\u00d1\u00d2\u0003\"\u0011\u0000\u00d2\u000b\u0001\u0000\u0000"+
		"\u0000\u00d3\u00d4\u0005\u000e\u0000\u0000\u00d4\u00d7\u0005i\u0000\u0000"+
		"\u00d5\u00d6\u0005\u0016\u0000\u0000\u00d6\u00d8\u0003\b\u0004\u0000\u00d7"+
		"\u00d5\u0001\u0000\u0000\u0000\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8"+
		"\u00d9\u0001\u0000\u0000\u0000\u00d9\u00db\u0005Q\u0000\u0000\u00da\u00dc"+
		"\u0003\u000e\u0007\u0000\u00db\u00da\u0001\u0000\u0000\u0000\u00db\u00dc"+
		"\u0001\u0000\u0000\u0000\u00dc\u00de\u0001\u0000\u0000\u0000\u00dd\u00df"+
		"\u0005V\u0000\u0000\u00de\u00dd\u0001\u0000\u0000\u0000\u00de\u00df\u0001"+
		"\u0000\u0000\u0000\u00df\u00e1\u0001\u0000\u0000\u0000\u00e0\u00e2\u0003"+
		"\u0012\t\u0000\u00e1\u00e0\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000"+
		"\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000\u00e3\u00e4\u0005R\u0000"+
		"\u0000\u00e4\r\u0001\u0000\u0000\u0000\u00e5\u00ea\u0003\u0010\b\u0000"+
		"\u00e6\u00e7\u0005V\u0000\u0000\u00e7\u00e9\u0003\u0010\b\u0000\u00e8"+
		"\u00e6\u0001\u0000\u0000\u0000\u00e9\u00ec\u0001\u0000\u0000\u0000\u00ea"+
		"\u00e8\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb"+
		"\u000f\u0001\u0000\u0000\u0000\u00ec\u00ea\u0001\u0000\u0000\u0000\u00ed"+
		"\u00ef\u0005i\u0000\u0000\u00ee\u00f0\u0003t:\u0000\u00ef\u00ee\u0001"+
		"\u0000\u0000\u0000\u00ef\u00f0\u0001\u0000\u0000\u0000\u00f0\u0011\u0001"+
		"\u0000\u0000\u0000\u00f1\u00f5\u0005U\u0000\u0000\u00f2\u00f4\u0003\n"+
		"\u0005\u0000\u00f3\u00f2\u0001\u0000\u0000\u0000\u00f4\u00f7\u0001\u0000"+
		"\u0000\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f5\u00f6\u0001\u0000"+
		"\u0000\u0000\u00f6\u0013\u0001\u0000\u0000\u0000\u00f7\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f8\u00f9\u0005\u0019\u0000\u0000\u00f9\u00fb\u0005i\u0000"+
		"\u0000\u00fa\u00fc\u0003*\u0015\u0000\u00fb\u00fa\u0001\u0000\u0000\u0000"+
		"\u00fb\u00fc\u0001\u0000\u0000\u0000\u00fc\u00ff\u0001\u0000\u0000\u0000"+
		"\u00fd\u00fe\u0005\u000f\u0000\u0000\u00fe\u0100\u0003\b\u0004\u0000\u00ff"+
		"\u00fd\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100"+
		"\u0101\u0001\u0000\u0000\u0000\u0101\u0102\u0003\u0016\u000b\u0000\u0102"+
		"\u0015\u0001\u0000\u0000\u0000\u0103\u0107\u0005Q\u0000\u0000\u0104\u0106"+
		"\u0003\u0018\f\u0000\u0105\u0104\u0001\u0000\u0000\u0000\u0106\u0109\u0001"+
		"\u0000\u0000\u0000\u0107\u0105\u0001\u0000\u0000\u0000\u0107\u0108\u0001"+
		"\u0000\u0000\u0000\u0108\u010a\u0001\u0000\u0000\u0000\u0109\u0107\u0001"+
		"\u0000\u0000\u0000\u010a\u010b\u0005R\u0000\u0000\u010b\u0017\u0001\u0000"+
		"\u0000\u0000\u010c\u010e\u0003\u008eG\u0000\u010d\u010c\u0001\u0000\u0000"+
		"\u0000\u010e\u0111\u0001\u0000\u0000\u0000\u010f\u010d\u0001\u0000\u0000"+
		"\u0000\u010f\u0110\u0001\u0000\u0000\u0000\u0110\u0112\u0001\u0000\u0000"+
		"\u0000\u0111\u010f\u0001\u0000\u0000\u0000\u0112\u0115\u0003\u001a\r\u0000"+
		"\u0113\u0115\u0005U\u0000\u0000\u0114\u010f\u0001\u0000\u0000\u0000\u0114"+
		"\u0113\u0001\u0000\u0000\u0000\u0115\u0019\u0001\u0000\u0000\u0000\u0116"+
		"\u0117\u0003\u001c\u000e\u0000\u0117\u001b\u0001\u0000\u0000\u0000\u0118"+
		"\u011a\u0003\u001e\u000f\u0000\u0119\u0118\u0001\u0000\u0000\u0000\u011a"+
		"\u011d\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000\u0000\u011b"+
		"\u011c\u0001\u0000\u0000\u0000\u011c\u011f\u0001\u0000\u0000\u0000\u011d"+
		"\u011b\u0001\u0000\u0000\u0000\u011e\u0120\u0003*\u0015\u0000\u011f\u011e"+
		"\u0001\u0000\u0000\u0000\u011f\u0120\u0001\u0000\u0000\u0000\u0120\u0121"+
		"\u0001\u0000\u0000\u0000\u0121\u0122\u0003 \u0010\u0000\u0122\u001d\u0001"+
		"\u0000\u0000\u0000\u0123\u0124\u0007\u0001\u0000\u0000\u0124\u001f\u0001"+
		"\u0000\u0000\u0000\u0125\u0126\u0003\u0082A\u0000\u0126\u0127\u0005i\u0000"+
		"\u0000\u0127\u012c\u00032\u0019\u0000\u0128\u0129\u0005S\u0000\u0000\u0129"+
		"\u012b\u0005T\u0000\u0000\u012a\u0128\u0001\u0000\u0000\u0000\u012b\u012e"+
		"\u0001\u0000\u0000\u0000\u012c\u012a\u0001\u0000\u0000\u0000\u012c\u012d"+
		"\u0001\u0000\u0000\u0000\u012d\u0131\u0001\u0000\u0000\u0000\u012e\u012c"+
		"\u0001\u0000\u0000\u0000\u012f\u0130\u0005\'\u0000\u0000\u0130\u0132\u0003"+
		",\u0016\u0000\u0131\u012f\u0001\u0000\u0000\u0000\u0131\u0132\u0001\u0000"+
		"\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133\u0134\u0005U\u0000"+
		"\u0000\u0134!\u0001\u0000\u0000\u0000\u0135\u0139\u0003&\u0013\u0000\u0136"+
		"\u0139\u0003$\u0012\u0000\u0137\u0139\u0003(\u0014\u0000\u0138\u0135\u0001"+
		"\u0000\u0000\u0000\u0138\u0136\u0001\u0000\u0000\u0000\u0138\u0137\u0001"+
		"\u0000\u0000\u0000\u0139#\u0001\u0000\u0000\u0000\u013a\u013b\u0003\u0084"+
		"B\u0000\u013b\u013c\u0005i\u0000\u0000\u013c\u013d\u0005U\u0000\u0000"+
		"\u013d%\u0001\u0000\u0000\u0000\u013e\u0140\u0003*\u0015\u0000\u013f\u013e"+
		"\u0001\u0000\u0000\u0000\u013f\u0140\u0001\u0000\u0000\u0000\u0140\u0141"+
		"\u0001\u0000\u0000\u0000\u0141\u0142\u0003\u0082A\u0000\u0142\u0143\u0005"+
		"i\u0000\u0000\u0143\u0144\u00032\u0019\u0000\u0144\u0145\u0003:\u001d"+
		"\u0000\u0145\'\u0001\u0000\u0000\u0000\u0146\u0148\u0003*\u0015\u0000"+
		"\u0147\u0146\u0001\u0000\u0000\u0000\u0147\u0148\u0001\u0000\u0000\u0000"+
		"\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u014a\u0005i\u0000\u0000\u014a"+
		"\u014d\u00032\u0019\u0000\u014b\u014c\u0005\'\u0000\u0000\u014c\u014e"+
		"\u0003,\u0016\u0000\u014d\u014b\u0001\u0000\u0000\u0000\u014d\u014e\u0001"+
		"\u0000\u0000\u0000\u014e\u014f\u0001\u0000\u0000\u0000\u014f\u0150\u0003"+
		"<\u001e\u0000\u0150)\u0001\u0000\u0000\u0000\u0151\u0152\u0005/\u0000"+
		"\u0000\u0152\u0157\u00030\u0018\u0000\u0153\u0154\u0005V\u0000\u0000\u0154"+
		"\u0156\u00030\u0018\u0000\u0155\u0153\u0001\u0000\u0000\u0000\u0156\u0159"+
		"\u0001\u0000\u0000\u0000\u0157\u0155\u0001\u0000\u0000\u0000\u0157\u0158"+
		"\u0001\u0000\u0000\u0000\u0158\u015a\u0001\u0000\u0000\u0000\u0159\u0157"+
		"\u0001\u0000\u0000\u0000\u015a\u015b\u0005.\u0000\u0000\u015b+\u0001\u0000"+
		"\u0000\u0000\u015c\u0161\u0003.\u0017\u0000\u015d\u015e\u0005V\u0000\u0000"+
		"\u015e\u0160\u0003.\u0017\u0000\u015f\u015d\u0001\u0000\u0000\u0000\u0160"+
		"\u0163\u0001\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000\u0000\u0161"+
		"\u0162\u0001\u0000\u0000\u0000\u0162-\u0001\u0000\u0000\u0000\u0163\u0161"+
		"\u0001\u0000\u0000\u0000\u0164\u0169\u0005i\u0000\u0000\u0165\u0166\u0005"+
		"W\u0000\u0000\u0166\u0168\u0005i\u0000\u0000\u0167\u0165\u0001\u0000\u0000"+
		"\u0000\u0168\u016b\u0001\u0000\u0000\u0000\u0169\u0167\u0001\u0000\u0000"+
		"\u0000\u0169\u016a\u0001\u0000\u0000\u0000\u016a/\u0001\u0000\u0000\u0000"+
		"\u016b\u0169\u0001\u0000\u0000\u0000\u016c\u016f\u0005i\u0000\u0000\u016d"+
		"\u016e\u0005\u000f\u0000\u0000\u016e\u0170\u0003\u0084B\u0000\u016f\u016d"+
		"\u0001\u0000\u0000\u0000\u016f\u0170\u0001\u0000\u0000\u0000\u01701\u0001"+
		"\u0000\u0000\u0000\u0171\u0173\u0005O\u0000\u0000\u0172\u0174\u00036\u001b"+
		"\u0000\u0173\u0172\u0001\u0000\u0000\u0000\u0173\u0174\u0001\u0000\u0000"+
		"\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175\u0176\u0005P\u0000\u0000"+
		"\u01763\u0001\u0000\u0000\u0000\u0177\u017c\u0003\u0084B\u0000\u0178\u0179"+
		"\u0005i\u0000\u0000\u0179\u017b\u0005W\u0000\u0000\u017a\u0178\u0001\u0000"+
		"\u0000\u0000\u017b\u017e\u0001\u0000\u0000\u0000\u017c\u017a\u0001\u0000"+
		"\u0000\u0000\u017c\u017d\u0001\u0000\u0000\u0000\u017d\u017f\u0001\u0000"+
		"\u0000\u0000\u017e\u017c\u0001\u0000\u0000\u0000\u017f\u0180\u0005%\u0000"+
		"\u0000\u01805\u0001\u0000\u0000\u0000\u0181\u0186\u00038\u001c\u0000\u0182"+
		"\u0183\u0005V\u0000\u0000\u0183\u0185\u00038\u001c\u0000\u0184\u0182\u0001"+
		"\u0000\u0000\u0000\u0185\u0188\u0001\u0000\u0000\u0000\u0186\u0184\u0001"+
		"\u0000\u0000\u0000\u0186\u0187\u0001\u0000\u0000\u0000\u01877\u0001\u0000"+
		"\u0000\u0000\u0188\u0186\u0001\u0000\u0000\u0000\u0189\u018a\u0003\u0084"+
		"B\u0000\u018a\u018b\u0005i\u0000\u0000\u018b9\u0001\u0000\u0000\u0000"+
		"\u018c\u018f\u0003<\u001e\u0000\u018d\u018f\u0005U\u0000\u0000\u018e\u018c"+
		"\u0001\u0000\u0000\u0000\u018e\u018d\u0001\u0000\u0000\u0000\u018f;\u0001"+
		"\u0000\u0000\u0000\u0190\u0194\u0005Q\u0000\u0000\u0191\u0193\u0003>\u001f"+
		"\u0000\u0192\u0191\u0001\u0000\u0000\u0000\u0193\u0196\u0001\u0000\u0000"+
		"\u0000\u0194\u0192\u0001\u0000\u0000\u0000\u0194\u0195\u0001\u0000\u0000"+
		"\u0000\u0195\u0197\u0001\u0000\u0000\u0000\u0196\u0194\u0001\u0000\u0000"+
		"\u0000\u0197\u0198\u0005R\u0000\u0000\u0198=\u0001\u0000\u0000\u0000\u0199"+
		"\u019a\u0005i\u0000\u0000\u019a\u019c\u00053\u0000\u0000\u019b\u0199\u0001"+
		"\u0000\u0000\u0000\u019b\u019c\u0001\u0000\u0000\u0000\u019c\u019d\u0001"+
		"\u0000\u0000\u0000\u019d\u019e\u0003@ \u0000\u019e?\u0001\u0000\u0000"+
		"\u0000\u019f\u01a0\u0005*\u0000\u0000\u01a0\u01a1\u0003f3\u0000\u01a1"+
		"\u01a2\u0003<\u001e\u0000\u01a2\u01da\u0001\u0000\u0000\u0000\u01a3\u01a4"+
		"\u0005\u0014\u0000\u0000\u01a4\u01a5\u0005O\u0000\u0000\u01a5\u01a6\u0003"+
		"B!\u0000\u01a6\u01a7\u0005P\u0000\u0000\u01a7\u01a8\u0003<\u001e\u0000"+
		"\u01a8\u01da\u0001\u0000\u0000\u0000\u01a9\u01aa\u0005\u0015\u0000\u0000"+
		"\u01aa\u01ab\u0003f3\u0000\u01ab\u01ae\u0003<\u001e\u0000\u01ac\u01ad"+
		"\u0005\r\u0000\u0000\u01ad\u01af\u0003<\u001e\u0000\u01ae\u01ac\u0001"+
		"\u0000\u0000\u0000\u01ae\u01af\u0001\u0000\u0000\u0000\u01af\u01da\u0001"+
		"\u0000\u0000\u0000\u01b0\u01b1\u0005(\u0000\u0000\u01b1\u01b2\u0003<\u001e"+
		"\u0000\u01b2\u01b3\u0003Z-\u0000\u01b3\u01da\u0001\u0000\u0000\u0000\u01b4"+
		"\u01b5\u0005$\u0000\u0000\u01b5\u01b9\u0005Q\u0000\u0000\u01b6\u01b8\u0003"+
		"b1\u0000\u01b7\u01b6\u0001\u0000\u0000\u0000\u01b8\u01bb\u0001\u0000\u0000"+
		"\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001\u0000\u0000"+
		"\u0000\u01ba\u01bc\u0001\u0000\u0000\u0000\u01bb\u01b9\u0001\u0000\u0000"+
		"\u0000\u01bc\u01da\u0005R\u0000\u0000\u01bd\u01bf\u0005!\u0000\u0000\u01be"+
		"\u01c0\u0003j5\u0000\u01bf\u01be\u0001\u0000\u0000\u0000\u01bf\u01c0\u0001"+
		"\u0000\u0000\u0000\u01c0\u01c1\u0001\u0000\u0000\u0000\u01c1\u01da\u0005"+
		"U\u0000\u0000\u01c2\u01c3\u0005&\u0000\u0000\u01c3\u01c4\u0003j5\u0000"+
		"\u01c4\u01c5\u0005U\u0000\u0000\u01c5\u01da\u0001\u0000\u0000\u0000\u01c6"+
		"\u01da\u0005U\u0000\u0000\u01c7\u01c8\u0003x<\u0000\u01c8\u01c9\u0005"+
		"U\u0000\u0000\u01c9\u01da\u0001\u0000\u0000\u0000\u01ca\u01cb\u0003z="+
		"\u0000\u01cb\u01cc\u0005U\u0000\u0000\u01cc\u01da\u0001\u0000\u0000\u0000"+
		"\u01cd\u01ce\u0007\u0002\u0000\u0000\u01ce\u01cf\u0003N\'\u0000\u01cf"+
		"\u01d0\u0005U\u0000\u0000\u01d0\u01da\u0001\u0000\u0000\u0000\u01d1\u01d2"+
		"\u0007\u0003\u0000\u0000\u01d2\u01d3\u0005W\u0000\u0000\u01d3\u01d4\u0005"+
		"i\u0000\u0000\u01d4\u01d5\u0007\u0004\u0000\u0000\u01d5\u01d6\u0003j5"+
		"\u0000\u01d6\u01d7\u0005U\u0000\u0000\u01d7\u01da\u0001\u0000\u0000\u0000"+
		"\u01d8\u01da\u0003\u0092I\u0000\u01d9\u019f\u0001\u0000\u0000\u0000\u01d9"+
		"\u01a3\u0001\u0000\u0000\u0000\u01d9\u01a9\u0001\u0000\u0000\u0000\u01d9"+
		"\u01b0\u0001\u0000\u0000\u0000\u01d9\u01b4\u0001\u0000\u0000\u0000\u01d9"+
		"\u01bd\u0001\u0000\u0000\u0000\u01d9\u01c2\u0001\u0000\u0000\u0000\u01d9"+
		"\u01c6\u0001\u0000\u0000\u0000\u01d9\u01c7\u0001\u0000\u0000\u0000\u01d9"+
		"\u01ca\u0001\u0000\u0000\u0000\u01d9\u01cd\u0001\u0000\u0000\u0000\u01d9"+
		"\u01d1\u0001\u0000\u0000\u0000\u01d9\u01d8\u0001\u0000\u0000\u0000\u01da"+
		"A\u0001\u0000\u0000\u0000\u01db\u01dd\u0003D\"\u0000\u01dc\u01db\u0001"+
		"\u0000\u0000\u0000\u01dc\u01dd\u0001\u0000\u0000\u0000\u01dd\u01de\u0001"+
		"\u0000\u0000\u0000\u01de\u01e0\u0005U\u0000\u0000\u01df\u01e1\u0003j5"+
		"\u0000\u01e0\u01df\u0001\u0000\u0000\u0000\u01e0\u01e1\u0001\u0000\u0000"+
		"\u0000\u01e1\u01e2\u0001\u0000\u0000\u0000\u01e2\u01e4\u0005U\u0000\u0000"+
		"\u01e3\u01e5\u0003H$\u0000\u01e4\u01e3\u0001\u0000\u0000\u0000\u01e4\u01e5"+
		"\u0001\u0000\u0000\u0000\u01e5C\u0001\u0000\u0000\u0000\u01e6\u01eb\u0003"+
		"F#\u0000\u01e7\u01e8\u0005V\u0000\u0000\u01e8\u01ea\u0003F#\u0000\u01e9"+
		"\u01e7\u0001\u0000\u0000\u0000\u01ea\u01ed\u0001\u0000\u0000\u0000\u01eb"+
		"\u01e9\u0001\u0000\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec"+
		"E\u0001\u0000\u0000\u0000\u01ed\u01eb\u0001\u0000\u0000\u0000\u01ee\u01ef"+
		"\u0003\u0084B\u0000\u01ef\u01f0\u0005i\u0000\u0000\u01f0\u01f1\u0005-"+
		"\u0000\u0000\u01f1\u01f2\u0003j5\u0000\u01f2G\u0001\u0000\u0000\u0000"+
		"\u01f3\u01f8\u0003J%\u0000\u01f4\u01f5\u0005V\u0000\u0000\u01f5\u01f7"+
		"\u0003J%\u0000\u01f6\u01f4\u0001\u0000\u0000\u0000\u01f7\u01fa\u0001\u0000"+
		"\u0000\u0000\u01f8\u01f6\u0001\u0000\u0000\u0000\u01f8\u01f9\u0001\u0000"+
		"\u0000\u0000\u01f9I\u0001\u0000\u0000\u0000\u01fa\u01f8\u0001\u0000\u0000"+
		"\u0000\u01fb\u01fc\u0005i\u0000\u0000\u01fc\u01fd\u0005-\u0000\u0000\u01fd"+
		"\u01fe\u0003j5\u0000\u01feK\u0001\u0000\u0000\u0000\u01ff\u0200\u0003"+
		".\u0017\u0000\u0200\u0201\u0005W\u0000\u0000\u0201\u0202\u0005i\u0000"+
		"\u0000\u0202M\u0001\u0000\u0000\u0000\u0203\u0205\u0003\u008aE\u0000\u0204"+
		"\u0203\u0001\u0000\u0000\u0000\u0204\u0205\u0001\u0000\u0000\u0000\u0205"+
		"\u0206\u0001\u0000\u0000\u0000\u0206\u0207\u0003\u0088D\u0000\u0207\u0208"+
		"\u0003t:\u0000\u0208O\u0001\u0000\u0000\u0000\u0209\u020a\u0005S\u0000"+
		"\u0000\u020a\u020f\u0005T\u0000\u0000\u020b\u020c\u0005S\u0000\u0000\u020c"+
		"\u020e\u0005T\u0000\u0000\u020d\u020b\u0001\u0000\u0000\u0000\u020e\u0211"+
		"\u0001\u0000\u0000\u0000\u020f\u020d\u0001\u0000\u0000\u0000\u020f\u0210"+
		"\u0001\u0000\u0000\u0000\u0210\u0212\u0001\u0000\u0000\u0000\u0211\u020f"+
		"\u0001\u0000\u0000\u0000\u0212\u0213\u0003R)\u0000\u0213Q\u0001\u0000"+
		"\u0000\u0000\u0214\u0220\u0005Q\u0000\u0000\u0215\u021a\u0003T*\u0000"+
		"\u0216\u0217\u0005V\u0000\u0000\u0217\u0219\u0003T*\u0000\u0218\u0216"+
		"\u0001\u0000\u0000\u0000\u0219\u021c\u0001\u0000\u0000\u0000\u021a\u0218"+
		"\u0001\u0000\u0000\u0000\u021a\u021b\u0001\u0000\u0000\u0000\u021b\u021e"+
		"\u0001\u0000\u0000\u0000\u021c\u021a\u0001\u0000\u0000\u0000\u021d\u021f"+
		"\u0005V\u0000\u0000\u021e\u021d\u0001\u0000\u0000\u0000\u021e\u021f\u0001"+
		"\u0000\u0000\u0000\u021f\u0221\u0001\u0000\u0000\u0000\u0220\u0215\u0001"+
		"\u0000\u0000\u0000\u0220\u0221\u0001\u0000\u0000\u0000\u0221\u0222\u0001"+
		"\u0000\u0000\u0000\u0222\u0223\u0005R\u0000\u0000\u0223S\u0001\u0000\u0000"+
		"\u0000\u0224\u0227\u0003R)\u0000\u0225\u0227\u0003j5\u0000\u0226\u0224"+
		"\u0001\u0000\u0000\u0000\u0226\u0225\u0001\u0000\u0000\u0000\u0227U\u0001"+
		"\u0000\u0000\u0000\u0228\u022a\u0005i\u0000\u0000\u0229\u022b\u0003\u008a"+
		"E\u0000\u022a\u0229\u0001\u0000\u0000\u0000\u022a\u022b\u0001\u0000\u0000"+
		"\u0000\u022b\u022e\u0001\u0000\u0000\u0000\u022c\u022e\u0003\u008cF\u0000"+
		"\u022d\u0228\u0001\u0000\u0000\u0000\u022d\u022c\u0001\u0000\u0000\u0000"+
		"\u022eW\u0001\u0000\u0000\u0000\u022f\u0230\u0003t:\u0000\u0230Y\u0001"+
		"\u0000\u0000\u0000\u0231\u0232\u0005\u0005\u0000\u0000\u0232\u0234\u0005"+
		"Q\u0000\u0000\u0233\u0235\u0003\\.\u0000\u0234\u0233\u0001\u0000\u0000"+
		"\u0000\u0234\u0235\u0001\u0000\u0000\u0000\u0235\u0236\u0001\u0000\u0000"+
		"\u0000\u0236\u0237\u0005R\u0000\u0000\u0237[\u0001\u0000\u0000\u0000\u0238"+
		"\u023d\u0003^/\u0000\u0239\u023a\u0005V\u0000\u0000\u023a\u023c\u0003"+
		"^/\u0000\u023b\u0239\u0001\u0000\u0000\u0000\u023c\u023f\u0001\u0000\u0000"+
		"\u0000\u023d\u023b\u0001\u0000\u0000\u0000\u023d\u023e\u0001\u0000\u0000"+
		"\u0000\u023e]\u0001\u0000\u0000\u0000\u023f\u023d\u0001\u0000\u0000\u0000"+
		"\u0240\u0241\u0005i\u0000\u0000\u0241\u0242\u00053\u0000\u0000\u0242\u0248"+
		"\u0005Q\u0000\u0000\u0243\u0244\u0003`0\u0000\u0244\u0245\u0005V\u0000"+
		"\u0000\u0245\u0247\u0001\u0000\u0000\u0000\u0246\u0243\u0001\u0000\u0000"+
		"\u0000\u0247\u024a\u0001\u0000\u0000\u0000\u0248\u0246\u0001\u0000\u0000"+
		"\u0000\u0248\u0249\u0001\u0000\u0000\u0000\u0249\u024b\u0001\u0000\u0000"+
		"\u0000\u024a\u0248\u0001\u0000\u0000\u0000\u024b\u024c\u0005\u0004\u0000"+
		"\u0000\u024c\u024d\u00053\u0000\u0000\u024d\u024e\u0003j5\u0000\u024e"+
		"\u024f\u0005R\u0000\u0000\u024f_\u0001\u0000\u0000\u0000\u0250\u0251\u0005"+
		"i\u0000\u0000\u0251\u0252\u00053\u0000\u0000\u0252\u0253\u0003j5\u0000"+
		"\u0253a\u0001\u0000\u0000\u0000\u0254\u0255\u0003d2\u0000\u0255\u0256"+
		"\u0003<\u001e\u0000\u0256c\u0001\u0000\u0000\u0000\u0257\u0258\u0005\u0003"+
		"\u0000\u0000\u0258\u0259\u0003j5\u0000\u0259\u025a\u0005X\u0000\u0000"+
		"\u025a\u025e\u0001\u0000\u0000\u0000\u025b\u025c\u0005\u0004\u0000\u0000"+
		"\u025c\u025e\u0005X\u0000\u0000\u025d\u0257\u0001\u0000\u0000\u0000\u025d"+
		"\u025b\u0001\u0000\u0000\u0000\u025ee\u0001\u0000\u0000\u0000\u025f\u0260"+
		"\u0005O\u0000\u0000\u0260\u0261\u0003j5\u0000\u0261\u0262\u0005P\u0000"+
		"\u0000\u0262g\u0001\u0000\u0000\u0000\u0263\u0268\u0003j5\u0000\u0264"+
		"\u0265\u0005V\u0000\u0000\u0265\u0267\u0003j5\u0000\u0266\u0264\u0001"+
		"\u0000\u0000\u0000\u0267\u026a\u0001\u0000\u0000\u0000\u0268\u0266\u0001"+
		"\u0000\u0000\u0000\u0268\u0269\u0001\u0000\u0000\u0000\u0269i\u0001\u0000"+
		"\u0000\u0000\u026a\u0268\u0001\u0000\u0000\u0000\u026b\u026c\u00065\uffff"+
		"\uffff\u0000\u026c\u0279\u0003l6\u0000\u026d\u026e\u0005O\u0000\u0000"+
		"\u026e\u026f\u0003\u0084B\u0000\u026f\u0270\u0005P\u0000\u0000\u0270\u0271"+
		"\u0003j5\u0012\u0271\u0279\u0001\u0000\u0000\u0000\u0272\u0273\u0007\u0005"+
		"\u0000\u0000\u0273\u0279\u0003j5\u0010\u0274\u0275\u0007\u0006\u0000\u0000"+
		"\u0275\u0279\u0003j5\u000f\u0276\u0277\u0005i\u0000\u0000\u0277\u0279"+
		"\u0003t:\u0000\u0278\u026b\u0001\u0000\u0000\u0000\u0278\u026d\u0001\u0000"+
		"\u0000\u0000\u0278\u0272\u0001\u0000\u0000\u0000\u0278\u0274\u0001\u0000"+
		"\u0000\u0000\u0278\u0276\u0001\u0000\u0000\u0000\u0279\u02bb\u0001\u0000"+
		"\u0000\u0000\u027a\u027b\n\u000e\u0000\u0000\u027b\u027c\u0007\u0007\u0000"+
		"\u0000\u027c\u02ba\u0003j5\u000f\u027d\u027e\n\r\u0000\u0000\u027e\u027f"+
		"\u0007\b\u0000\u0000\u027f\u02ba\u0003j5\u000e\u0280\u0288\n\f\u0000\u0000"+
		"\u0281\u0282\u0005/\u0000\u0000\u0282\u0289\u0005/\u0000\u0000\u0283\u0284"+
		"\u0005.\u0000\u0000\u0284\u0285\u0005.\u0000\u0000\u0285\u0289\u0005."+
		"\u0000\u0000\u0286\u0287\u0005.\u0000\u0000\u0287\u0289\u0005.\u0000\u0000"+
		"\u0288\u0281\u0001\u0000\u0000\u0000\u0288\u0283\u0001\u0000\u0000\u0000"+
		"\u0288\u0286\u0001\u0000\u0000\u0000\u0289\u028a\u0001\u0000\u0000\u0000"+
		"\u028a\u02ba\u0003j5\r\u028b\u028c\n\u000b\u0000\u0000\u028c\u028d\u0007"+
		"\t\u0000\u0000\u028d\u02ba\u0003j5\f\u028e\u028f\n\t\u0000\u0000\u028f"+
		"\u0290\u0007\n\u0000\u0000\u0290\u02ba\u0003j5\n\u0291\u0292\n\b\u0000"+
		"\u0000\u0292\u0293\u0005@\u0000\u0000\u0293\u02ba\u0003j5\t\u0294\u0295"+
		"\n\u0007\u0000\u0000\u0295\u0296\u0005B\u0000\u0000\u0296\u02ba\u0003"+
		"j5\b\u0297\u0298\n\u0006\u0000\u0000\u0298\u0299\u0005A\u0000\u0000\u0299"+
		"\u02ba\u0003j5\u0007\u029a\u029b\n\u0005\u0000\u0000\u029b\u029c\u0005"+
		"8\u0000\u0000\u029c\u02ba\u0003j5\u0006\u029d\u029e\n\u0004\u0000\u0000"+
		"\u029e\u029f\u00059\u0000\u0000\u029f\u02ba\u0003j5\u0005\u02a0\u02a1"+
		"\n\u0003\u0000\u0000\u02a1\u02a2\u00052\u0000\u0000\u02a2\u02a3\u0003"+
		"j5\u0000\u02a3\u02a4\u00053\u0000\u0000\u02a4\u02a5\u0003j5\u0003\u02a5"+
		"\u02ba\u0001\u0000\u0000\u0000\u02a6\u02a7\n\u0014\u0000\u0000\u02a7\u02a8"+
		"\u0005W\u0000\u0000\u02a8\u02ba\u0007\u0003\u0000\u0000\u02a9\u02aa\n"+
		"\u0013\u0000\u0000\u02aa\u02ab\u0005S\u0000\u0000\u02ab\u02ac\u0003j5"+
		"\u0000\u02ac\u02ad\u0005T\u0000\u0000\u02ad\u02ba\u0001\u0000\u0000\u0000"+
		"\u02ae\u02af\n\u0011\u0000\u0000\u02af\u02ba\u0007\u000b\u0000\u0000\u02b0"+
		"\u02b1\n\n\u0000\u0000\u02b1\u02b2\u0005\u0017\u0000\u0000\u02b2\u02ba"+
		"\u0003\u0084B\u0000\u02b3\u02b4\n\u0002\u0000\u0000\u02b4\u02b6\u0005"+
		"Y\u0000\u0000\u02b5\u02b7\u0003\u008aE\u0000\u02b6\u02b5\u0001\u0000\u0000"+
		"\u0000\u02b6\u02b7\u0001\u0000\u0000\u0000\u02b7\u02b8\u0001\u0000\u0000"+
		"\u0000\u02b8\u02ba\u0005i\u0000\u0000\u02b9\u027a\u0001\u0000\u0000\u0000"+
		"\u02b9\u027d\u0001\u0000\u0000\u0000\u02b9\u0280\u0001\u0000\u0000\u0000"+
		"\u02b9\u028b\u0001\u0000\u0000\u0000\u02b9\u028e\u0001\u0000\u0000\u0000"+
		"\u02b9\u0291\u0001\u0000\u0000\u0000\u02b9\u0294\u0001\u0000\u0000\u0000"+
		"\u02b9\u0297\u0001\u0000\u0000\u0000\u02b9\u029a\u0001\u0000\u0000\u0000"+
		"\u02b9\u029d\u0001\u0000\u0000\u0000\u02b9\u02a0\u0001\u0000\u0000\u0000"+
		"\u02b9\u02a6\u0001\u0000\u0000\u0000\u02b9\u02a9\u0001\u0000\u0000\u0000"+
		"\u02b9\u02ae\u0001\u0000\u0000\u0000\u02b9\u02b0\u0001\u0000\u0000\u0000"+
		"\u02b9\u02b3\u0001\u0000\u0000\u0000\u02ba\u02bd\u0001\u0000\u0000\u0000"+
		"\u02bb\u02b9\u0001\u0000\u0000\u0000\u02bb\u02bc\u0001\u0000\u0000\u0000"+
		"\u02bck\u0001\u0000\u0000\u0000\u02bd\u02bb\u0001\u0000\u0000\u0000\u02be"+
		"\u02bf\u0005O\u0000\u0000\u02bf\u02c0\u0003j5\u0000\u02c0\u02c1\u0005"+
		"P\u0000\u0000\u02c1\u02c6\u0001\u0000\u0000\u0000\u02c2\u02c6\u0005%\u0000"+
		"\u0000\u02c3\u02c6\u0003|>\u0000\u02c4\u02c6\u0005i\u0000\u0000\u02c5"+
		"\u02be\u0001\u0000\u0000\u0000\u02c5\u02c2\u0001\u0000\u0000\u0000\u02c5"+
		"\u02c3\u0001\u0000\u0000\u0000\u02c5\u02c4\u0001\u0000\u0000\u0000\u02c6"+
		"m\u0001\u0000\u0000\u0000\u02c7\u02c8\u0003\u008aE\u0000\u02c8\u02c9\u0003"+
		"p8\u0000\u02c9o\u0001\u0000\u0000\u0000\u02ca\u02cb\u0005#\u0000\u0000"+
		"\u02cb\u02cf\u0003r9\u0000\u02cc\u02cd\u0005i\u0000\u0000\u02cd\u02cf"+
		"\u0003t:\u0000\u02ce\u02ca\u0001\u0000\u0000\u0000\u02ce\u02cc\u0001\u0000"+
		"\u0000\u0000\u02cfq\u0001\u0000\u0000\u0000\u02d0\u02da\u0003t:\u0000"+
		"\u02d1\u02d3\u0005W\u0000\u0000\u02d2\u02d4\u0003\u008aE\u0000\u02d3\u02d2"+
		"\u0001\u0000\u0000\u0000\u02d3\u02d4\u0001\u0000\u0000\u0000\u02d4\u02d5"+
		"\u0001\u0000\u0000\u0000\u02d5\u02d7\u0005i\u0000\u0000\u02d6\u02d8\u0003"+
		"t:\u0000\u02d7\u02d6\u0001\u0000\u0000\u0000\u02d7\u02d8\u0001\u0000\u0000"+
		"\u0000\u02d8\u02da\u0001\u0000\u0000\u0000\u02d9\u02d0\u0001\u0000\u0000"+
		"\u0000\u02d9\u02d1\u0001\u0000\u0000\u0000\u02das\u0001\u0000\u0000\u0000"+
		"\u02db\u02dd\u0005O\u0000\u0000\u02dc\u02de\u0003h4\u0000\u02dd\u02dc"+
		"\u0001\u0000\u0000\u0000\u02dd\u02de\u0001\u0000\u0000\u0000\u02de\u02df"+
		"\u0001\u0000\u0000\u0000\u02df\u02e0\u0005P\u0000\u0000\u02e0u\u0001\u0000"+
		"\u0000\u0000\u02e1\u02e3\u0005i\u0000\u0000\u02e2\u02e4\u0003\u008aE\u0000"+
		"\u02e3\u02e2\u0001\u0000\u0000\u0000\u02e3\u02e4\u0001\u0000\u0000\u0000"+
		"\u02e4w\u0001\u0000\u0000\u0000\u02e5\u02e6\u0003j5\u0000\u02e6\u02e8"+
		"\u0005W\u0000\u0000\u02e7\u02e9\u0003\u008aE\u0000\u02e8\u02e7\u0001\u0000"+
		"\u0000\u0000\u02e8\u02e9\u0001\u0000\u0000\u0000\u02e9\u02ea\u0001\u0000"+
		"\u0000\u0000\u02ea\u02eb\u0005i\u0000\u0000\u02eb\u02ed\u0005O\u0000\u0000"+
		"\u02ec\u02ee\u0003h4\u0000\u02ed\u02ec\u0001\u0000\u0000\u0000\u02ed\u02ee"+
		"\u0001\u0000\u0000\u0000\u02ee\u02ef\u0001\u0000\u0000\u0000\u02ef\u02f0"+
		"\u0005P\u0000\u0000\u02f0\u02fe\u0001\u0000\u0000\u0000\u02f1\u02f2\u0005"+
		"%\u0000\u0000\u02f2\u02f4\u0005O\u0000\u0000\u02f3\u02f5\u0003h4\u0000"+
		"\u02f4\u02f3\u0001\u0000\u0000\u0000\u02f4\u02f5\u0001\u0000\u0000\u0000"+
		"\u02f5\u02f6\u0001\u0000\u0000\u0000\u02f6\u02fe\u0005P\u0000\u0000\u02f7"+
		"\u02f8\u0005#\u0000\u0000\u02f8\u02fa\u0005O\u0000\u0000\u02f9\u02fb\u0003"+
		"h4\u0000\u02fa\u02f9\u0001\u0000\u0000\u0000\u02fa\u02fb\u0001\u0000\u0000"+
		"\u0000\u02fb\u02fc\u0001\u0000\u0000\u0000\u02fc\u02fe\u0005P\u0000\u0000"+
		"\u02fd\u02e5\u0001\u0000\u0000\u0000\u02fd\u02f1\u0001\u0000\u0000\u0000"+
		"\u02fd\u02f7\u0001\u0000\u0000\u0000\u02fey\u0001\u0000\u0000\u0000\u02ff"+
		"\u0300\u0003j5\u0000\u0300\u0302\u0005O\u0000\u0000\u0301\u0303\u0003"+
		"h4\u0000\u0302\u0301\u0001\u0000\u0000\u0000\u0302\u0303\u0001\u0000\u0000"+
		"\u0000\u0303\u0304\u0001\u0000\u0000\u0000\u0304\u0305\u0005P\u0000\u0000"+
		"\u0305{\u0001\u0000\u0000\u0000\u0306\u030e\u0003~?\u0000\u0307\u030e"+
		"\u0003\u0080@\u0000\u0308\u030e\u0005c\u0000\u0000\u0309\u030e\u0005d"+
		"\u0000\u0000\u030a\u030e\u0005b\u0000\u0000\u030b\u030e\u0005\n\u0000"+
		"\u0000\u030c\u030e\u0005e\u0000\u0000\u030d\u0306\u0001\u0000\u0000\u0000"+
		"\u030d\u0307\u0001\u0000\u0000\u0000\u030d\u0308\u0001\u0000\u0000\u0000"+
		"\u030d\u0309\u0001\u0000\u0000\u0000\u030d\u030a\u0001\u0000\u0000\u0000"+
		"\u030d\u030b\u0001\u0000\u0000\u0000\u030d\u030c\u0001\u0000\u0000\u0000"+
		"\u030e}\u0001\u0000\u0000\u0000\u030f\u0310\u0007\f\u0000\u0000\u0310"+
		"\u007f\u0001\u0000\u0000\u0000\u0311\u0312\u0007\r\u0000\u0000\u0312\u0081"+
		"\u0001\u0000\u0000\u0000\u0313\u0316\u0003\u0084B\u0000\u0314\u0316\u0005"+
		")\u0000\u0000\u0315\u0313\u0001\u0000\u0000\u0000\u0315\u0314\u0001\u0000"+
		"\u0000\u0000\u0316\u0083\u0001\u0000\u0000\u0000\u0317\u0318\u0006B\uffff"+
		"\uffff\u0000\u0318\u0331\u0003\u0088D\u0000\u0319\u0331\u0003\u008cF\u0000"+
		"\u031a\u0331\u0005+\u0000\u0000\u031b\u0331\u0005,\u0000\u0000\u031c\u0325"+
		"\u0005O\u0000\u0000\u031d\u0322\u0003\u0084B\u0000\u031e\u031f\u0005V"+
		"\u0000\u0000\u031f\u0321\u0003\u0084B\u0000\u0320\u031e\u0001\u0000\u0000"+
		"\u0000\u0321\u0324\u0001\u0000\u0000\u0000\u0322\u0320\u0001\u0000\u0000"+
		"\u0000\u0322\u0323\u0001\u0000\u0000\u0000\u0323\u0326\u0001\u0000\u0000"+
		"\u0000\u0324\u0322\u0001\u0000\u0000\u0000\u0325\u031d\u0001\u0000\u0000"+
		"\u0000\u0325\u0326\u0001\u0000\u0000\u0000\u0326\u0327\u0001\u0000\u0000"+
		"\u0000\u0327\u0328\u0005P\u0000\u0000\u0328\u0329\u0005X\u0000\u0000\u0329"+
		"\u0331\u0003\u0084B\u0002\u032a\u032b\u0005S\u0000\u0000\u032b\u032c\u0003"+
		"\u0084B\u0000\u032c\u032d\u0005V\u0000\u0000\u032d\u032e\u0003\u0084B"+
		"\u0000\u032e\u032f\u0005T\u0000\u0000\u032f\u0331\u0001\u0000\u0000\u0000"+
		"\u0330\u0317\u0001\u0000\u0000\u0000\u0330\u0319\u0001\u0000\u0000\u0000"+
		"\u0330\u031a\u0001\u0000\u0000\u0000\u0330\u031b\u0001\u0000\u0000\u0000"+
		"\u0330\u031c\u0001\u0000\u0000\u0000\u0330\u032a\u0001\u0000\u0000\u0000"+
		"\u0331\u0347\u0001\u0000\u0000\u0000\u0332\u0335\n\u0005\u0000\u0000\u0333"+
		"\u0334\u0005A\u0000\u0000\u0334\u0336\u0003\u0084B\u0000\u0335\u0333\u0001"+
		"\u0000\u0000\u0000\u0336\u0337\u0001\u0000\u0000\u0000\u0337\u0335\u0001"+
		"\u0000\u0000\u0000\u0337\u0338\u0001\u0000\u0000\u0000\u0338\u0346\u0001"+
		"\u0000\u0000\u0000\u0339\u033c\n\u0004\u0000\u0000\u033a\u033b\u0005@"+
		"\u0000\u0000\u033b\u033d\u0003\u0084B\u0000\u033c\u033a\u0001\u0000\u0000"+
		"\u0000\u033d\u033e\u0001\u0000\u0000\u0000\u033e\u033c\u0001\u0000\u0000"+
		"\u0000\u033e\u033f\u0001\u0000\u0000\u0000\u033f\u0346\u0001\u0000\u0000"+
		"\u0000\u0340\u0341\n\u0003\u0000\u0000\u0341\u0342\u0005S\u0000\u0000"+
		"\u0342\u0343\u0003\u0086C\u0000\u0343\u0344\u0005T\u0000\u0000\u0344\u0346"+
		"\u0001\u0000\u0000\u0000\u0345\u0332\u0001\u0000\u0000\u0000\u0345\u0339"+
		"\u0001\u0000\u0000\u0000\u0345\u0340\u0001\u0000\u0000\u0000\u0346\u0349"+
		"\u0001\u0000\u0000\u0000\u0347\u0345\u0001\u0000\u0000\u0000\u0347\u0348"+
		"\u0001\u0000\u0000\u0000\u0348\u0085\u0001\u0000\u0000\u0000\u0349\u0347"+
		"\u0001\u0000\u0000\u0000\u034a\u034b\u0007\u000e\u0000\u0000\u034b\u0087"+
		"\u0001\u0000\u0000\u0000\u034c\u034e\u0003.\u0017\u0000\u034d\u034f\u0003"+
		"\u008aE\u0000\u034e\u034d\u0001\u0000\u0000\u0000\u034e\u034f\u0001\u0000"+
		"\u0000\u0000\u034f\u0089\u0001\u0000\u0000\u0000\u0350\u0351\u0005/\u0000"+
		"\u0000\u0351\u0356\u0003\u0084B\u0000\u0352\u0353\u0005V\u0000\u0000\u0353"+
		"\u0355\u0003\u0084B\u0000\u0354\u0352\u0001\u0000\u0000\u0000\u0355\u0358"+
		"\u0001\u0000\u0000\u0000\u0356\u0354\u0001\u0000\u0000\u0000\u0356\u0357"+
		"\u0001\u0000\u0000\u0000\u0357\u0359\u0001\u0000\u0000\u0000\u0358\u0356"+
		"\u0001\u0000\u0000\u0000\u0359\u035a\u0005.\u0000\u0000\u035a\u008b\u0001"+
		"\u0000\u0000\u0000\u035b\u035c\u0007\u000f\u0000\u0000\u035c\u008d\u0001"+
		"\u0000\u0000\u0000\u035d\u0363\u0003\u0090H\u0000\u035e\u0363\u0005\u001a"+
		"\u0000\u0000\u035f\u0363\u0005\u0010\u0000\u0000\u0360\u0363\u0005\u0011"+
		"\u0000\u0000\u0361\u0363\u0005\u0012\u0000\u0000\u0362\u035d\u0001\u0000"+
		"\u0000\u0000\u0362\u035e\u0001\u0000\u0000\u0000\u0362\u035f\u0001\u0000"+
		"\u0000\u0000\u0362\u0360\u0001\u0000\u0000\u0000\u0362\u0361\u0001\u0000"+
		"\u0000\u0000\u0363\u008f\u0001\u0000\u0000\u0000\u0364\u0365\u0007\u0010"+
		"\u0000\u0000\u0365\u0091\u0001\u0000\u0000\u0000\u0366\u0367\u0003\u0094"+
		"J\u0000\u0367\u0368\u00053\u0000\u0000\u0368\u0369\u0003\u0082A\u0000"+
		"\u0369\u036a\u0005X\u0000\u0000\u036a\u036b\u0003\u0096K\u0000\u036b\u0093"+
		"\u0001\u0000\u0000\u0000\u036c\u036e\u0005O\u0000\u0000\u036d\u036f\u0003"+
		"6\u001b\u0000\u036e\u036d\u0001\u0000\u0000\u0000\u036e\u036f\u0001\u0000"+
		"\u0000\u0000\u036f\u0370\u0001\u0000\u0000\u0000\u0370\u0371\u0005P\u0000"+
		"\u0000\u0371\u0095\u0001\u0000\u0000\u0000\u0372\u0373\u0003<\u001e\u0000"+
		"\u0373\u0097\u0001\u0000\u0000\u0000]\u009b\u00a0\u00a6\u00a9\u00ae\u00b2"+
		"\u00b6\u00be\u00c8\u00ce\u00d7\u00db\u00de\u00e1\u00ea\u00ef\u00f5\u00fb"+
		"\u00ff\u0107\u010f\u0114\u011b\u011f\u012c\u0131\u0138\u013f\u0147\u014d"+
		"\u0157\u0161\u0169\u016f\u0173\u017c\u0186\u018e\u0194\u019b\u01ae\u01b9"+
		"\u01bf\u01d9\u01dc\u01e0\u01e4\u01eb\u01f8\u0204\u020f\u021a\u021e\u0220"+
		"\u0226\u022a\u022d\u0234\u023d\u0248\u025d\u0268\u0278\u0288\u02b6\u02b9"+
		"\u02bb\u02c5\u02ce\u02d3\u02d7\u02d9\u02dd\u02e3\u02e8\u02ed\u02f4\u02fa"+
		"\u02fd\u0302\u030d\u0315\u0322\u0325\u0330\u0337\u033e\u0345\u0347\u034e"+
		"\u0356\u0362\u036e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}