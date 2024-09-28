// Generated from TypeParser.g4 by ANTLR 4.13.2
package org.metavm.object.type.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class TypeParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BOOLEAN=1, DOUBLE=2, LONG=3, VOID=4, NULL=5, TIME=6, PASSWORD=7, STRING=8, 
		ANY=9, NEVER=10, R=11, C=12, V=13, FUNC=14, LPAREN=15, RPAREN=16, LBRACK=17, 
		RBRACK=18, COMMA=19, DOT=20, NUM=21, COLON=22, AT=23, GT=24, LT=25, BITAND=26, 
		BITOR=27, QUESTION=28, ARROW=29, COLONCOLON=30, WS=31, IDENTIFIER=32, 
		DECIMAL_LITERAL=33;
	public static final int
		RULE_unit = 0, RULE_type = 1, RULE_parType = 2, RULE_genericDeclarationRef = 3, 
		RULE_methodRef = 4, RULE_functionRef = 5, RULE_simpleMethodRef = 6, RULE_arrayKind = 7, 
		RULE_classType = 8, RULE_variableType = 9, RULE_typeArguments = 10, RULE_primitiveType = 11, 
		RULE_typeList = 12, RULE_qualifiedName = 13, RULE_functionSignature = 14, 
		RULE_parameterList = 15, RULE_parameter = 16, RULE_typeParameterList = 17, 
		RULE_typeParameter = 18;
	private static String[] makeRuleNames() {
		return new String[] {
			"unit", "type", "parType", "genericDeclarationRef", "methodRef", "functionRef", 
			"simpleMethodRef", "arrayKind", "classType", "variableType", "typeArguments", 
			"primitiveType", "typeList", "qualifiedName", "functionSignature", "parameterList", 
			"parameter", "typeParameterList", "typeParameter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'boolean'", "'double'", "'long'", "'void'", "'null'", "'time'", 
			"'password'", "'string'", "'any'", "'never'", "'r'", "'c'", "'v'", "'func'", 
			"'('", "')'", "'['", "']'", "','", "'.'", "'#'", "':'", "'@'", "'>'", 
			"'<'", "'&'", "'|'", "'?'", "'->'", "'::'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BOOLEAN", "DOUBLE", "LONG", "VOID", "NULL", "TIME", "PASSWORD", 
			"STRING", "ANY", "NEVER", "R", "C", "V", "FUNC", "LPAREN", "RPAREN", 
			"LBRACK", "RBRACK", "COMMA", "DOT", "NUM", "COLON", "AT", "GT", "LT", 
			"BITAND", "BITOR", "QUESTION", "ARROW", "COLONCOLON", "WS", "IDENTIFIER", 
			"DECIMAL_LITERAL"
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
	public String getGrammarFileName() { return "TypeParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TypeParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnitContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TypeParser.EOF, 0); }
		public UnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnitContext unit() throws RecognitionException {
		UnitContext _localctx = new UnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_unit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			type(0);
			setState(39);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public TypeContext elementType;
		public TerminalNode NEVER() { return getToken(TypeParser.NEVER, 0); }
		public TerminalNode ANY() { return getToken(TypeParser.ANY, 0); }
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public VariableTypeContext variableType() {
			return getRuleContext(VariableTypeContext.class,0);
		}
		public TerminalNode NUM() { return getToken(TypeParser.NUM, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public ParTypeContext parType() {
			return getRuleContext(ParTypeContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(TypeParser.LBRACK, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(TypeParser.COMMA, 0); }
		public TerminalNode RBRACK() { return getToken(TypeParser.RBRACK, 0); }
		public TerminalNode LPAREN() { return getToken(TypeParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(TypeParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(TypeParser.ARROW, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public ArrayKindContext arrayKind() {
			return getRuleContext(ArrayKindContext.class,0);
		}
		public List<TerminalNode> BITOR() { return getTokens(TypeParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(TypeParser.BITOR, i);
		}
		public List<TerminalNode> BITAND() { return getTokens(TypeParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(TypeParser.BITAND, i);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitType(this);
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
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_type, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(42);
				match(NEVER);
				}
				break;
			case 2:
				{
				setState(43);
				match(ANY);
				}
				break;
			case 3:
				{
				setState(44);
				primitiveType();
				}
				break;
			case 4:
				{
				setState(45);
				variableType();
				}
				break;
			case 5:
				{
				setState(46);
				match(NUM);
				setState(47);
				qualifiedName();
				}
				break;
			case 6:
				{
				setState(48);
				classType();
				}
				break;
			case 7:
				{
				setState(49);
				parType();
				}
				break;
			case 8:
				{
				setState(50);
				match(LBRACK);
				setState(51);
				type(0);
				setState(52);
				match(COMMA);
				setState(53);
				type(0);
				setState(54);
				match(RBRACK);
				}
				break;
			case 9:
				{
				setState(56);
				match(LPAREN);
				setState(58);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4297246718L) != 0)) {
					{
					setState(57);
					typeList();
					}
				}

				setState(60);
				match(RPAREN);
				setState(61);
				match(ARROW);
				setState(62);
				type(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(87);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(85);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
					case 1:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						_localctx.elementType = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(65);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(66);
						match(LBRACK);
						setState(68);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 14336L) != 0)) {
							{
							setState(67);
							arrayKind();
							}
						}

						setState(70);
						match(RBRACK);
						}
						break;
					case 2:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(71);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(74); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(72);
								match(BITOR);
								setState(73);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(76); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(78);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(81); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(79);
								match(BITAND);
								setState(80);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(83); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					} 
				}
				setState(89);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
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
	public static class ParTypeContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(TypeParser.LPAREN, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(TypeParser.RPAREN, 0); }
		public ParTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterParType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitParType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitParType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParTypeContext parType() throws RecognitionException {
		ParTypeContext _localctx = new ParTypeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_parType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			match(LPAREN);
			setState(91);
			type(0);
			setState(92);
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
	public static class GenericDeclarationRefContext extends ParserRuleContext {
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public MethodRefContext methodRef() {
			return getRuleContext(MethodRefContext.class,0);
		}
		public FunctionRefContext functionRef() {
			return getRuleContext(FunctionRefContext.class,0);
		}
		public GenericDeclarationRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericDeclarationRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterGenericDeclarationRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitGenericDeclarationRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitGenericDeclarationRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericDeclarationRefContext genericDeclarationRef() throws RecognitionException {
		GenericDeclarationRefContext _localctx = new GenericDeclarationRefContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_genericDeclarationRef);
		try {
			setState(97);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(94);
				classType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(95);
				methodRef();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(96);
				functionRef();
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
	public static class MethodRefContext extends ParserRuleContext {
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public TerminalNode COLONCOLON() { return getToken(TypeParser.COLONCOLON, 0); }
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public MethodRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterMethodRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitMethodRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitMethodRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodRefContext methodRef() throws RecognitionException {
		MethodRefContext _localctx = new MethodRefContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_methodRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			classType();
			setState(100);
			match(COLONCOLON);
			setState(101);
			match(IDENTIFIER);
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(102);
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
	public static class FunctionRefContext extends ParserRuleContext {
		public TerminalNode FUNC() { return getToken(TypeParser.FUNC, 0); }
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public FunctionRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterFunctionRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitFunctionRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitFunctionRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionRefContext functionRef() throws RecognitionException {
		FunctionRefContext _localctx = new FunctionRefContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(FUNC);
			setState(106);
			match(IDENTIFIER);
			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(107);
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
	public static class SimpleMethodRefContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public SimpleMethodRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleMethodRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterSimpleMethodRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitSimpleMethodRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitSimpleMethodRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleMethodRefContext simpleMethodRef() throws RecognitionException {
		SimpleMethodRefContext _localctx = new SimpleMethodRefContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_simpleMethodRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			match(IDENTIFIER);
			setState(112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(111);
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
	public static class ArrayKindContext extends ParserRuleContext {
		public TerminalNode R() { return getToken(TypeParser.R, 0); }
		public TerminalNode C() { return getToken(TypeParser.C, 0); }
		public TerminalNode V() { return getToken(TypeParser.V, 0); }
		public ArrayKindContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayKind; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterArrayKind(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitArrayKind(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitArrayKind(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayKindContext arrayKind() throws RecognitionException {
		ArrayKindContext _localctx = new ArrayKindContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(114);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 14336L) != 0)) ) {
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
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(TypeParser.COLON, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(TypeParser.DECIMAL_LITERAL, 0); }
		public ClassTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterClassType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitClassType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitClassType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassTypeContext classType() throws RecognitionException {
		ClassTypeContext _localctx = new ClassTypeContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_classType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			qualifiedName();
			setState(118);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(117);
				typeArguments();
				}
				break;
			}
			setState(122);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(120);
				match(COLON);
				setState(121);
				match(DECIMAL_LITERAL);
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
	public static class VariableTypeContext extends ParserRuleContext {
		public GenericDeclarationRefContext genericDeclarationRef() {
			return getRuleContext(GenericDeclarationRefContext.class,0);
		}
		public TerminalNode AT() { return getToken(TypeParser.AT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public VariableTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterVariableType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitVariableType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitVariableType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableTypeContext variableType() throws RecognitionException {
		VariableTypeContext _localctx = new VariableTypeContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_variableType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			genericDeclarationRef();
			setState(125);
			match(AT);
			setState(126);
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
	public static class TypeArgumentsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(TypeParser.LT, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode GT() { return getToken(TypeParser.GT, 0); }
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitTypeArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_typeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			match(LT);
			setState(129);
			typeList();
			setState(130);
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
		public TerminalNode LONG() { return getToken(TypeParser.LONG, 0); }
		public TerminalNode DOUBLE() { return getToken(TypeParser.DOUBLE, 0); }
		public TerminalNode TIME() { return getToken(TypeParser.TIME, 0); }
		public TerminalNode STRING() { return getToken(TypeParser.STRING, 0); }
		public TerminalNode PASSWORD() { return getToken(TypeParser.PASSWORD, 0); }
		public TerminalNode NULL() { return getToken(TypeParser.NULL, 0); }
		public TerminalNode VOID() { return getToken(TypeParser.VOID, 0); }
		public TerminalNode BOOLEAN() { return getToken(TypeParser.BOOLEAN, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterPrimitiveType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitPrimitiveType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 510L) != 0)) ) {
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
	public static class TypeListContext extends ParserRuleContext {
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TypeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TypeParser.COMMA, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			type(0);
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(135);
				match(COMMA);
				setState(136);
				type(0);
				}
				}
				setState(141);
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
		public List<TerminalNode> IDENTIFIER() { return getTokens(TypeParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(TypeParser.IDENTIFIER, i);
		}
		public List<TerminalNode> DOT() { return getTokens(TypeParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(TypeParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			match(IDENTIFIER);
			setState(147);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(143);
					match(DOT);
					setState(144);
					match(IDENTIFIER);
					}
					} 
				}
				setState(149);
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
	public static class FunctionSignatureContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public TerminalNode LPAREN() { return getToken(TypeParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(TypeParser.RPAREN, 0); }
		public TypeParameterListContext typeParameterList() {
			return getRuleContext(TypeParameterListContext.class,0);
		}
		public ParameterListContext parameterList() {
			return getRuleContext(ParameterListContext.class,0);
		}
		public FunctionSignatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionSignature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterFunctionSignature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitFunctionSignature(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitFunctionSignature(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionSignatureContext functionSignature() throws RecognitionException {
		FunctionSignatureContext _localctx = new FunctionSignatureContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_functionSignature);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			type(0);
			setState(151);
			match(IDENTIFIER);
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(152);
				typeParameterList();
				}
			}

			setState(155);
			match(LPAREN);
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4297246718L) != 0)) {
				{
				setState(156);
				parameterList();
				}
			}

			setState(159);
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
	public static class ParameterListContext extends ParserRuleContext {
		public List<ParameterContext> parameter() {
			return getRuleContexts(ParameterContext.class);
		}
		public ParameterContext parameter(int i) {
			return getRuleContext(ParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TypeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TypeParser.COMMA, i);
		}
		public ParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterListContext parameterList() throws RecognitionException {
		ParameterListContext _localctx = new ParameterListContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_parameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			parameter();
			setState(166);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(162);
				match(COMMA);
				setState(163);
				parameter();
				}
				}
				setState(168);
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
	public static class ParameterContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_parameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			type(0);
			setState(170);
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
	public static class TypeParameterListContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(TypeParser.LT, 0); }
		public List<TypeParameterContext> typeParameter() {
			return getRuleContexts(TypeParameterContext.class);
		}
		public TypeParameterContext typeParameter(int i) {
			return getRuleContext(TypeParameterContext.class,i);
		}
		public TerminalNode GT() { return getToken(TypeParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(TypeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TypeParser.COMMA, i);
		}
		public TypeParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterTypeParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitTypeParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitTypeParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParameterListContext typeParameterList() throws RecognitionException {
		TypeParameterListContext _localctx = new TypeParameterListContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_typeParameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			match(LT);
			setState(173);
			typeParameter();
			setState(178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(174);
				match(COMMA);
				setState(175);
				typeParameter();
				}
				}
				setState(180);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(181);
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
	public static class TypeParameterContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(TypeParser.IDENTIFIER, 0); }
		public TypeParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).enterTypeParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TypeParserListener ) ((TypeParserListener)listener).exitTypeParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TypeParserVisitor ) return ((TypeParserVisitor<? extends T>)visitor).visitTypeParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParameterContext typeParameter() throws RecognitionException {
		TypeParameterContext _localctx = new TypeParameterContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_typeParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
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
		case 1:
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
		"\u0004\u0001!\u00ba\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001;\b\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001@\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0003\u0001E\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0004\u0001K\b\u0001\u000b\u0001\f\u0001L\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0004\u0001R\b\u0001\u000b\u0001\f\u0001S\u0005\u0001V\b\u0001"+
		"\n\u0001\f\u0001Y\t\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003b\b\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004h\b\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005m\b\u0005\u0001\u0006\u0001\u0006"+
		"\u0003\u0006q\b\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0003\b"+
		"w\b\b\u0001\b\u0001\b\u0003\b{\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0005\f\u008a\b\f\n\f\f\f\u008d\t\f\u0001\r\u0001\r\u0001\r\u0005\r"+
		"\u0092\b\r\n\r\f\r\u0095\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003"+
		"\u000e\u009a\b\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u009e\b\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u00a5\b\u000f\n\u000f\f\u000f\u00a8\t\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u00b1"+
		"\b\u0011\n\u0011\f\u0011\u00b4\t\u0011\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0000\u0001\u0002\u0013\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$\u0000"+
		"\u0002\u0001\u0000\u000b\r\u0001\u0000\u0001\b\u00c2\u0000&\u0001\u0000"+
		"\u0000\u0000\u0002?\u0001\u0000\u0000\u0000\u0004Z\u0001\u0000\u0000\u0000"+
		"\u0006a\u0001\u0000\u0000\u0000\bc\u0001\u0000\u0000\u0000\ni\u0001\u0000"+
		"\u0000\u0000\fn\u0001\u0000\u0000\u0000\u000er\u0001\u0000\u0000\u0000"+
		"\u0010t\u0001\u0000\u0000\u0000\u0012|\u0001\u0000\u0000\u0000\u0014\u0080"+
		"\u0001\u0000\u0000\u0000\u0016\u0084\u0001\u0000\u0000\u0000\u0018\u0086"+
		"\u0001\u0000\u0000\u0000\u001a\u008e\u0001\u0000\u0000\u0000\u001c\u0096"+
		"\u0001\u0000\u0000\u0000\u001e\u00a1\u0001\u0000\u0000\u0000 \u00a9\u0001"+
		"\u0000\u0000\u0000\"\u00ac\u0001\u0000\u0000\u0000$\u00b7\u0001\u0000"+
		"\u0000\u0000&\'\u0003\u0002\u0001\u0000\'(\u0005\u0000\u0000\u0001(\u0001"+
		"\u0001\u0000\u0000\u0000)*\u0006\u0001\uffff\uffff\u0000*@\u0005\n\u0000"+
		"\u0000+@\u0005\t\u0000\u0000,@\u0003\u0016\u000b\u0000-@\u0003\u0012\t"+
		"\u0000./\u0005\u0015\u0000\u0000/@\u0003\u001a\r\u00000@\u0003\u0010\b"+
		"\u00001@\u0003\u0004\u0002\u000023\u0005\u0011\u0000\u000034\u0003\u0002"+
		"\u0001\u000045\u0005\u0013\u0000\u000056\u0003\u0002\u0001\u000067\u0005"+
		"\u0012\u0000\u00007@\u0001\u0000\u0000\u00008:\u0005\u000f\u0000\u0000"+
		"9;\u0003\u0018\f\u0000:9\u0001\u0000\u0000\u0000:;\u0001\u0000\u0000\u0000"+
		";<\u0001\u0000\u0000\u0000<=\u0005\u0010\u0000\u0000=>\u0005\u001d\u0000"+
		"\u0000>@\u0003\u0002\u0001\u0001?)\u0001\u0000\u0000\u0000?+\u0001\u0000"+
		"\u0000\u0000?,\u0001\u0000\u0000\u0000?-\u0001\u0000\u0000\u0000?.\u0001"+
		"\u0000\u0000\u0000?0\u0001\u0000\u0000\u0000?1\u0001\u0000\u0000\u0000"+
		"?2\u0001\u0000\u0000\u0000?8\u0001\u0000\u0000\u0000@W\u0001\u0000\u0000"+
		"\u0000AB\n\u0005\u0000\u0000BD\u0005\u0011\u0000\u0000CE\u0003\u000e\u0007"+
		"\u0000DC\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000EF\u0001\u0000"+
		"\u0000\u0000FV\u0005\u0012\u0000\u0000GJ\n\u0004\u0000\u0000HI\u0005\u001b"+
		"\u0000\u0000IK\u0003\u0002\u0001\u0000JH\u0001\u0000\u0000\u0000KL\u0001"+
		"\u0000\u0000\u0000LJ\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000"+
		"MV\u0001\u0000\u0000\u0000NQ\n\u0003\u0000\u0000OP\u0005\u001a\u0000\u0000"+
		"PR\u0003\u0002\u0001\u0000QO\u0001\u0000\u0000\u0000RS\u0001\u0000\u0000"+
		"\u0000SQ\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000\u0000TV\u0001\u0000"+
		"\u0000\u0000UA\u0001\u0000\u0000\u0000UG\u0001\u0000\u0000\u0000UN\u0001"+
		"\u0000\u0000\u0000VY\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000"+
		"WX\u0001\u0000\u0000\u0000X\u0003\u0001\u0000\u0000\u0000YW\u0001\u0000"+
		"\u0000\u0000Z[\u0005\u000f\u0000\u0000[\\\u0003\u0002\u0001\u0000\\]\u0005"+
		"\u0010\u0000\u0000]\u0005\u0001\u0000\u0000\u0000^b\u0003\u0010\b\u0000"+
		"_b\u0003\b\u0004\u0000`b\u0003\n\u0005\u0000a^\u0001\u0000\u0000\u0000"+
		"a_\u0001\u0000\u0000\u0000a`\u0001\u0000\u0000\u0000b\u0007\u0001\u0000"+
		"\u0000\u0000cd\u0003\u0010\b\u0000de\u0005\u001e\u0000\u0000eg\u0005 "+
		"\u0000\u0000fh\u0003\u0014\n\u0000gf\u0001\u0000\u0000\u0000gh\u0001\u0000"+
		"\u0000\u0000h\t\u0001\u0000\u0000\u0000ij\u0005\u000e\u0000\u0000jl\u0005"+
		" \u0000\u0000km\u0003\u0014\n\u0000lk\u0001\u0000\u0000\u0000lm\u0001"+
		"\u0000\u0000\u0000m\u000b\u0001\u0000\u0000\u0000np\u0005 \u0000\u0000"+
		"oq\u0003\u0014\n\u0000po\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000"+
		"q\r\u0001\u0000\u0000\u0000rs\u0007\u0000\u0000\u0000s\u000f\u0001\u0000"+
		"\u0000\u0000tv\u0003\u001a\r\u0000uw\u0003\u0014\n\u0000vu\u0001\u0000"+
		"\u0000\u0000vw\u0001\u0000\u0000\u0000wz\u0001\u0000\u0000\u0000xy\u0005"+
		"\u0016\u0000\u0000y{\u0005!\u0000\u0000zx\u0001\u0000\u0000\u0000z{\u0001"+
		"\u0000\u0000\u0000{\u0011\u0001\u0000\u0000\u0000|}\u0003\u0006\u0003"+
		"\u0000}~\u0005\u0017\u0000\u0000~\u007f\u0005 \u0000\u0000\u007f\u0013"+
		"\u0001\u0000\u0000\u0000\u0080\u0081\u0005\u0019\u0000\u0000\u0081\u0082"+
		"\u0003\u0018\f\u0000\u0082\u0083\u0005\u0018\u0000\u0000\u0083\u0015\u0001"+
		"\u0000\u0000\u0000\u0084\u0085\u0007\u0001\u0000\u0000\u0085\u0017\u0001"+
		"\u0000\u0000\u0000\u0086\u008b\u0003\u0002\u0001\u0000\u0087\u0088\u0005"+
		"\u0013\u0000\u0000\u0088\u008a\u0003\u0002\u0001\u0000\u0089\u0087\u0001"+
		"\u0000\u0000\u0000\u008a\u008d\u0001\u0000\u0000\u0000\u008b\u0089\u0001"+
		"\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u0019\u0001"+
		"\u0000\u0000\u0000\u008d\u008b\u0001\u0000\u0000\u0000\u008e\u0093\u0005"+
		" \u0000\u0000\u008f\u0090\u0005\u0014\u0000\u0000\u0090\u0092\u0005 \u0000"+
		"\u0000\u0091\u008f\u0001\u0000\u0000\u0000\u0092\u0095\u0001\u0000\u0000"+
		"\u0000\u0093\u0091\u0001\u0000\u0000\u0000\u0093\u0094\u0001\u0000\u0000"+
		"\u0000\u0094\u001b\u0001\u0000\u0000\u0000\u0095\u0093\u0001\u0000\u0000"+
		"\u0000\u0096\u0097\u0003\u0002\u0001\u0000\u0097\u0099\u0005 \u0000\u0000"+
		"\u0098\u009a\u0003\"\u0011\u0000\u0099\u0098\u0001\u0000\u0000\u0000\u0099"+
		"\u009a\u0001\u0000\u0000\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b"+
		"\u009d\u0005\u000f\u0000\u0000\u009c\u009e\u0003\u001e\u000f\u0000\u009d"+
		"\u009c\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e"+
		"\u009f\u0001\u0000\u0000\u0000\u009f\u00a0\u0005\u0010\u0000\u0000\u00a0"+
		"\u001d\u0001\u0000\u0000\u0000\u00a1\u00a6\u0003 \u0010\u0000\u00a2\u00a3"+
		"\u0005\u0013\u0000\u0000\u00a3\u00a5\u0003 \u0010\u0000\u00a4\u00a2\u0001"+
		"\u0000\u0000\u0000\u00a5\u00a8\u0001\u0000\u0000\u0000\u00a6\u00a4\u0001"+
		"\u0000\u0000\u0000\u00a6\u00a7\u0001\u0000\u0000\u0000\u00a7\u001f\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000\u00a9\u00aa\u0003"+
		"\u0002\u0001\u0000\u00aa\u00ab\u0005 \u0000\u0000\u00ab!\u0001\u0000\u0000"+
		"\u0000\u00ac\u00ad\u0005\u0019\u0000\u0000\u00ad\u00b2\u0003$\u0012\u0000"+
		"\u00ae\u00af\u0005\u0013\u0000\u0000\u00af\u00b1\u0003$\u0012\u0000\u00b0"+
		"\u00ae\u0001\u0000\u0000\u0000\u00b1\u00b4\u0001\u0000\u0000\u0000\u00b2"+
		"\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000\u00b3"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b5"+
		"\u00b6\u0005\u0018\u0000\u0000\u00b6#\u0001\u0000\u0000\u0000\u00b7\u00b8"+
		"\u0005 \u0000\u0000\u00b8%\u0001\u0000\u0000\u0000\u0013:?DLSUWaglpvz"+
		"\u008b\u0093\u0099\u009d\u00a6\u00b2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}