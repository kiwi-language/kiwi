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
		BOOLEAN=1, DOUBLE=2, LONG=3, CHAR=4, VOID=5, NULL=6, TIME=7, PASSWORD=8, 
		STRING=9, ANY=10, NEVER=11, R=12, C=13, V=14, FUNC=15, LPAREN=16, RPAREN=17, 
		LBRACK=18, RBRACK=19, COMMA=20, DOT=21, NUM=22, COLON=23, AT=24, GT=25, 
		LT=26, BITAND=27, BITOR=28, QUESTION=29, ARROW=30, COLONCOLON=31, WS=32, 
		IDENTIFIER=33, DECIMAL_LITERAL=34;
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
			null, "'boolean'", "'double'", "'long'", "'char'", "'void'", "'null'", 
			"'time'", "'password'", "'string'", "'any'", "'never'", "'[r]'", "'[c]'", 
			"'[v]'", "'func'", "'('", "')'", "'['", "']'", "','", "'.'", "'#'", "':'", 
			"'@'", "'>'", "'<'", "'&'", "'|'", "'?'", "'->'", "'::'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BOOLEAN", "DOUBLE", "LONG", "CHAR", "VOID", "NULL", "TIME", "PASSWORD", 
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
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8594493438L) != 0)) {
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
			setState(83);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(81);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
					case 1:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						_localctx.elementType = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(65);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(66);
						arrayKind();
						}
						break;
					case 2:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(67);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(70); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(68);
								match(BITOR);
								setState(69);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(72); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(74);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(77); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(75);
								match(BITAND);
								setState(76);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(79); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					} 
				}
				setState(85);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
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
			setState(86);
			match(LPAREN);
			setState(87);
			type(0);
			setState(88);
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
			setState(93);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				classType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				methodRef();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
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
			setState(95);
			classType();
			setState(96);
			match(COLONCOLON);
			setState(97);
			match(IDENTIFIER);
			setState(99);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(98);
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
			setState(101);
			match(FUNC);
			setState(102);
			match(IDENTIFIER);
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(103);
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
	public static class ArrayKindContext extends ParserRuleContext {
		public TerminalNode R() { return getToken(TypeParser.R, 0); }
		public TerminalNode C() { return getToken(TypeParser.C, 0); }
		public TerminalNode V() { return getToken(TypeParser.V, 0); }
		public TerminalNode LBRACK() { return getToken(TypeParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(TypeParser.RBRACK, 0); }
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
		try {
			setState(115);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case R:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				match(R);
				}
				break;
			case C:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
				match(C);
				}
				break;
			case V:
				enterOuterAlt(_localctx, 3);
				{
				setState(112);
				match(V);
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 4);
				{
				setState(113);
				match(LBRACK);
				setState(114);
				match(RBRACK);
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
			setState(117);
			qualifiedName();
			setState(119);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(118);
				typeArguments();
				}
				break;
			}
			setState(123);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(121);
				match(COLON);
				setState(122);
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
			setState(125);
			genericDeclarationRef();
			setState(126);
			match(AT);
			setState(127);
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
			setState(129);
			match(LT);
			setState(130);
			typeList();
			setState(131);
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
		public TerminalNode CHAR() { return getToken(TypeParser.CHAR, 0); }
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
			setState(133);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1022L) != 0)) ) {
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
			setState(135);
			type(0);
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(136);
				match(COMMA);
				setState(137);
				type(0);
				}
				}
				setState(142);
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
			setState(143);
			match(IDENTIFIER);
			setState(148);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(144);
					match(DOT);
					setState(145);
					match(IDENTIFIER);
					}
					} 
				}
				setState(150);
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
			setState(151);
			type(0);
			setState(152);
			match(IDENTIFIER);
			setState(154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(153);
				typeParameterList();
				}
			}

			setState(156);
			match(LPAREN);
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 8594493438L) != 0)) {
				{
				setState(157);
				parameterList();
				}
			}

			setState(160);
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
			setState(162);
			parameter();
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(163);
				match(COMMA);
				setState(164);
				parameter();
				}
				}
				setState(169);
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
			setState(170);
			type(0);
			setState(171);
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
			setState(173);
			match(LT);
			setState(174);
			typeParameter();
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(175);
				match(COMMA);
				setState(176);
				typeParameter();
				}
				}
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(182);
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
			setState(184);
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
		"\u0004\u0001\"\u00bb\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"\u0001\u0001\u0001\u0001\u0004\u0001G\b\u0001\u000b\u0001\f\u0001H\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0004\u0001N\b\u0001\u000b\u0001\f\u0001"+
		"O\u0005\u0001R\b\u0001\n\u0001\f\u0001U\t\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003"+
		"^\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"d\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005i\b\u0005\u0001"+
		"\u0006\u0001\u0006\u0003\u0006m\b\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007t\b\u0007\u0001\b\u0001\b\u0003"+
		"\bx\b\b\u0001\b\u0001\b\u0003\b|\b\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0005\f\u008b\b\f\n\f\f\f\u008e\t\f\u0001\r\u0001\r\u0001\r\u0005\r"+
		"\u0093\b\r\n\r\f\r\u0096\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003"+
		"\u000e\u009b\b\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u009f\b\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u00a6\b\u000f\n\u000f\f\u000f\u00a9\t\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u00b2"+
		"\b\u0011\n\u0011\f\u0011\u00b5\t\u0011\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0000\u0001\u0002\u0013\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$\u0000"+
		"\u0001\u0001\u0000\u0001\t\u00c5\u0000&\u0001\u0000\u0000\u0000\u0002"+
		"?\u0001\u0000\u0000\u0000\u0004V\u0001\u0000\u0000\u0000\u0006]\u0001"+
		"\u0000\u0000\u0000\b_\u0001\u0000\u0000\u0000\ne\u0001\u0000\u0000\u0000"+
		"\fj\u0001\u0000\u0000\u0000\u000es\u0001\u0000\u0000\u0000\u0010u\u0001"+
		"\u0000\u0000\u0000\u0012}\u0001\u0000\u0000\u0000\u0014\u0081\u0001\u0000"+
		"\u0000\u0000\u0016\u0085\u0001\u0000\u0000\u0000\u0018\u0087\u0001\u0000"+
		"\u0000\u0000\u001a\u008f\u0001\u0000\u0000\u0000\u001c\u0097\u0001\u0000"+
		"\u0000\u0000\u001e\u00a2\u0001\u0000\u0000\u0000 \u00aa\u0001\u0000\u0000"+
		"\u0000\"\u00ad\u0001\u0000\u0000\u0000$\u00b8\u0001\u0000\u0000\u0000"+
		"&\'\u0003\u0002\u0001\u0000\'(\u0005\u0000\u0000\u0001(\u0001\u0001\u0000"+
		"\u0000\u0000)*\u0006\u0001\uffff\uffff\u0000*@\u0005\u000b\u0000\u0000"+
		"+@\u0005\n\u0000\u0000,@\u0003\u0016\u000b\u0000-@\u0003\u0012\t\u0000"+
		"./\u0005\u0016\u0000\u0000/@\u0003\u001a\r\u00000@\u0003\u0010\b\u0000"+
		"1@\u0003\u0004\u0002\u000023\u0005\u0012\u0000\u000034\u0003\u0002\u0001"+
		"\u000045\u0005\u0014\u0000\u000056\u0003\u0002\u0001\u000067\u0005\u0013"+
		"\u0000\u00007@\u0001\u0000\u0000\u00008:\u0005\u0010\u0000\u00009;\u0003"+
		"\u0018\f\u0000:9\u0001\u0000\u0000\u0000:;\u0001\u0000\u0000\u0000;<\u0001"+
		"\u0000\u0000\u0000<=\u0005\u0011\u0000\u0000=>\u0005\u001e\u0000\u0000"+
		">@\u0003\u0002\u0001\u0001?)\u0001\u0000\u0000\u0000?+\u0001\u0000\u0000"+
		"\u0000?,\u0001\u0000\u0000\u0000?-\u0001\u0000\u0000\u0000?.\u0001\u0000"+
		"\u0000\u0000?0\u0001\u0000\u0000\u0000?1\u0001\u0000\u0000\u0000?2\u0001"+
		"\u0000\u0000\u0000?8\u0001\u0000\u0000\u0000@S\u0001\u0000\u0000\u0000"+
		"AB\n\u0005\u0000\u0000BR\u0003\u000e\u0007\u0000CF\n\u0004\u0000\u0000"+
		"DE\u0005\u001c\u0000\u0000EG\u0003\u0002\u0001\u0000FD\u0001\u0000\u0000"+
		"\u0000GH\u0001\u0000\u0000\u0000HF\u0001\u0000\u0000\u0000HI\u0001\u0000"+
		"\u0000\u0000IR\u0001\u0000\u0000\u0000JM\n\u0003\u0000\u0000KL\u0005\u001b"+
		"\u0000\u0000LN\u0003\u0002\u0001\u0000MK\u0001\u0000\u0000\u0000NO\u0001"+
		"\u0000\u0000\u0000OM\u0001\u0000\u0000\u0000OP\u0001\u0000\u0000\u0000"+
		"PR\u0001\u0000\u0000\u0000QA\u0001\u0000\u0000\u0000QC\u0001\u0000\u0000"+
		"\u0000QJ\u0001\u0000\u0000\u0000RU\u0001\u0000\u0000\u0000SQ\u0001\u0000"+
		"\u0000\u0000ST\u0001\u0000\u0000\u0000T\u0003\u0001\u0000\u0000\u0000"+
		"US\u0001\u0000\u0000\u0000VW\u0005\u0010\u0000\u0000WX\u0003\u0002\u0001"+
		"\u0000XY\u0005\u0011\u0000\u0000Y\u0005\u0001\u0000\u0000\u0000Z^\u0003"+
		"\u0010\b\u0000[^\u0003\b\u0004\u0000\\^\u0003\n\u0005\u0000]Z\u0001\u0000"+
		"\u0000\u0000][\u0001\u0000\u0000\u0000]\\\u0001\u0000\u0000\u0000^\u0007"+
		"\u0001\u0000\u0000\u0000_`\u0003\u0010\b\u0000`a\u0005\u001f\u0000\u0000"+
		"ac\u0005!\u0000\u0000bd\u0003\u0014\n\u0000cb\u0001\u0000\u0000\u0000"+
		"cd\u0001\u0000\u0000\u0000d\t\u0001\u0000\u0000\u0000ef\u0005\u000f\u0000"+
		"\u0000fh\u0005!\u0000\u0000gi\u0003\u0014\n\u0000hg\u0001\u0000\u0000"+
		"\u0000hi\u0001\u0000\u0000\u0000i\u000b\u0001\u0000\u0000\u0000jl\u0005"+
		"!\u0000\u0000km\u0003\u0014\n\u0000lk\u0001\u0000\u0000\u0000lm\u0001"+
		"\u0000\u0000\u0000m\r\u0001\u0000\u0000\u0000nt\u0005\f\u0000\u0000ot"+
		"\u0005\r\u0000\u0000pt\u0005\u000e\u0000\u0000qr\u0005\u0012\u0000\u0000"+
		"rt\u0005\u0013\u0000\u0000sn\u0001\u0000\u0000\u0000so\u0001\u0000\u0000"+
		"\u0000sp\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000t\u000f\u0001"+
		"\u0000\u0000\u0000uw\u0003\u001a\r\u0000vx\u0003\u0014\n\u0000wv\u0001"+
		"\u0000\u0000\u0000wx\u0001\u0000\u0000\u0000x{\u0001\u0000\u0000\u0000"+
		"yz\u0005\u0017\u0000\u0000z|\u0005\"\u0000\u0000{y\u0001\u0000\u0000\u0000"+
		"{|\u0001\u0000\u0000\u0000|\u0011\u0001\u0000\u0000\u0000}~\u0003\u0006"+
		"\u0003\u0000~\u007f\u0005\u0018\u0000\u0000\u007f\u0080\u0005!\u0000\u0000"+
		"\u0080\u0013\u0001\u0000\u0000\u0000\u0081\u0082\u0005\u001a\u0000\u0000"+
		"\u0082\u0083\u0003\u0018\f\u0000\u0083\u0084\u0005\u0019\u0000\u0000\u0084"+
		"\u0015\u0001\u0000\u0000\u0000\u0085\u0086\u0007\u0000\u0000\u0000\u0086"+
		"\u0017\u0001\u0000\u0000\u0000\u0087\u008c\u0003\u0002\u0001\u0000\u0088"+
		"\u0089\u0005\u0014\u0000\u0000\u0089\u008b\u0003\u0002\u0001\u0000\u008a"+
		"\u0088\u0001\u0000\u0000\u0000\u008b\u008e\u0001\u0000\u0000\u0000\u008c"+
		"\u008a\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000\u0000\u008d"+
		"\u0019\u0001\u0000\u0000\u0000\u008e\u008c\u0001\u0000\u0000\u0000\u008f"+
		"\u0094\u0005!\u0000\u0000\u0090\u0091\u0005\u0015\u0000\u0000\u0091\u0093"+
		"\u0005!\u0000\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0093\u0096\u0001"+
		"\u0000\u0000\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0094\u0095\u0001"+
		"\u0000\u0000\u0000\u0095\u001b\u0001\u0000\u0000\u0000\u0096\u0094\u0001"+
		"\u0000\u0000\u0000\u0097\u0098\u0003\u0002\u0001\u0000\u0098\u009a\u0005"+
		"!\u0000\u0000\u0099\u009b\u0003\"\u0011\u0000\u009a\u0099\u0001\u0000"+
		"\u0000\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b\u009c\u0001\u0000"+
		"\u0000\u0000\u009c\u009e\u0005\u0010\u0000\u0000\u009d\u009f\u0003\u001e"+
		"\u000f\u0000\u009e\u009d\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005\u0011"+
		"\u0000\u0000\u00a1\u001d\u0001\u0000\u0000\u0000\u00a2\u00a7\u0003 \u0010"+
		"\u0000\u00a3\u00a4\u0005\u0014\u0000\u0000\u00a4\u00a6\u0003 \u0010\u0000"+
		"\u00a5\u00a3\u0001\u0000\u0000\u0000\u00a6\u00a9\u0001\u0000\u0000\u0000"+
		"\u00a7\u00a5\u0001\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000"+
		"\u00a8\u001f\u0001\u0000\u0000\u0000\u00a9\u00a7\u0001\u0000\u0000\u0000"+
		"\u00aa\u00ab\u0003\u0002\u0001\u0000\u00ab\u00ac\u0005!\u0000\u0000\u00ac"+
		"!\u0001\u0000\u0000\u0000\u00ad\u00ae\u0005\u001a\u0000\u0000\u00ae\u00b3"+
		"\u0003$\u0012\u0000\u00af\u00b0\u0005\u0014\u0000\u0000\u00b0\u00b2\u0003"+
		"$\u0012\u0000\u00b1\u00af\u0001\u0000\u0000\u0000\u00b2\u00b5\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b4\u0001\u0000"+
		"\u0000\u0000\u00b4\u00b6\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001\u0000"+
		"\u0000\u0000\u00b6\u00b7\u0005\u0019\u0000\u0000\u00b7#\u0001\u0000\u0000"+
		"\u0000\u00b8\u00b9\u0005!\u0000\u0000\u00b9%\u0001\u0000\u0000\u0000\u0013"+
		":?HOQS]chlsw{\u008c\u0094\u009a\u009e\u00a7\u00b3";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}