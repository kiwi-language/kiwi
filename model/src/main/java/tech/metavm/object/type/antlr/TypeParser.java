// Generated from TypeParser.g4 by ANTLR 4.13.1
package tech.metavm.object.type.antlr;

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

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class TypeParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BOOLEAN=1, DOUBLE=2, LONG=3, VOID=4, NULL=5, TIME=6, PASSWORD=7, STRING=8, 
		ANY=9, NEVER=10, R=11, C=12, V=13, LPAREN=14, RPAREN=15, LBRACK=16, RBRACK=17, 
		COMMA=18, DOT=19, NUM=20, COLON=21, GT=22, LT=23, BITAND=24, BITOR=25, 
		QUESTION=26, ARROW=27, WS=28, IDENTIFIER=29, DECIMAL_LITERAL=30;
	public static final int
		RULE_type = 0, RULE_methodRef = 1, RULE_arrayKind = 2, RULE_classType = 3, 
		RULE_variableType = 4, RULE_typeArguments = 5, RULE_primitiveType = 6, 
		RULE_typeList = 7, RULE_qualifiedName = 8;
	private static String[] makeRuleNames() {
		return new String[] {
			"type", "methodRef", "arrayKind", "classType", "variableType", "typeArguments", 
			"primitiveType", "typeList", "qualifiedName"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'boolean'", "'double'", "'long'", "'void'", "'null'", "'time'", 
			"'password'", "'string'", "'any'", "'never'", "'r'", "'c'", "'v'", "'('", 
			"')'", "'['", "']'", "','", "'.'", "'#'", "':'", "'>'", "'<'", "'&'", 
			"'|'", "'?'", "'->'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BOOLEAN", "DOUBLE", "LONG", "VOID", "NULL", "TIME", "PASSWORD", 
			"STRING", "ANY", "NEVER", "R", "C", "V", "LPAREN", "RPAREN", "LBRACK", 
			"RBRACK", "COMMA", "DOT", "NUM", "COLON", "GT", "LT", "BITAND", "BITOR", 
			"QUESTION", "ARROW", "WS", "IDENTIFIER", "DECIMAL_LITERAL"
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
		int _startState = 0;
		enterRecursionRule(_localctx, 0, RULE_type, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NEVER:
				{
				setState(19);
				match(NEVER);
				}
				break;
			case ANY:
				{
				setState(20);
				match(ANY);
				}
				break;
			case BOOLEAN:
			case DOUBLE:
			case LONG:
			case VOID:
			case NULL:
			case TIME:
			case PASSWORD:
			case STRING:
				{
				setState(21);
				primitiveType();
				}
				break;
			case QUESTION:
				{
				setState(22);
				variableType();
				}
				break;
			case NUM:
				{
				setState(23);
				match(NUM);
				setState(24);
				qualifiedName();
				}
				break;
			case IDENTIFIER:
				{
				setState(25);
				classType();
				}
				break;
			case LBRACK:
				{
				setState(26);
				match(LBRACK);
				setState(27);
				type(0);
				setState(28);
				match(COMMA);
				setState(29);
				type(0);
				setState(30);
				match(RBRACK);
				}
				break;
			case LPAREN:
				{
				setState(32);
				match(LPAREN);
				setState(34);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 605112318L) != 0)) {
					{
					setState(33);
					typeList();
					}
				}

				setState(36);
				match(RPAREN);
				setState(37);
				match(ARROW);
				setState(38);
				type(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(63);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(61);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
					case 1:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						_localctx.elementType = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(41);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(42);
						match(LBRACK);
						setState(44);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 14336L) != 0)) {
							{
							setState(43);
							arrayKind();
							}
						}

						setState(46);
						match(RBRACK);
						}
						break;
					case 2:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(47);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(50); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(48);
								match(BITOR);
								setState(49);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(52); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_type);
						setState(54);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(57); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(55);
								match(BITAND);
								setState(56);
								type(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(59); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					} 
				}
				setState(65);
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
	public static class MethodRefContext extends ParserRuleContext {
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public TerminalNode DOT() { return getToken(TypeParser.DOT, 0); }
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
		enterRule(_localctx, 2, RULE_methodRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			classType();
			setState(67);
			match(DOT);
			setState(68);
			match(IDENTIFIER);
			setState(70);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(69);
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
		enterRule(_localctx, 4, RULE_arrayKind);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
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
		enterRule(_localctx, 6, RULE_classType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			qualifiedName();
			setState(76);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(75);
				typeArguments();
				}
				break;
			}
			setState(80);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(78);
				match(COLON);
				setState(79);
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
		public TerminalNode QUESTION() { return getToken(TypeParser.QUESTION, 0); }
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
		enterRule(_localctx, 8, RULE_variableType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			match(QUESTION);
			setState(83);
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
		enterRule(_localctx, 10, RULE_typeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(LT);
			setState(86);
			typeList();
			setState(87);
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
		enterRule(_localctx, 12, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
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
		enterRule(_localctx, 14, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
			type(0);
			setState(96);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(92);
				match(COMMA);
				setState(93);
				type(0);
				}
				}
				setState(98);
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
		enterRule(_localctx, 16, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			match(IDENTIFIER);
			setState(104);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(100);
					match(DOT);
					setState(101);
					match(IDENTIFIER);
					}
					} 
				}
				setState(106);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
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
		case 0:
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
		"\u0004\u0001\u001el\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000"+
		"#\b\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000(\b\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0003\u0000-\b\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0004\u00003\b\u0000\u000b\u0000\f\u0000"+
		"4\u0001\u0000\u0001\u0000\u0001\u0000\u0004\u0000:\b\u0000\u000b\u0000"+
		"\f\u0000;\u0005\u0000>\b\u0000\n\u0000\f\u0000A\t\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001G\b\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0003\u0003M\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003Q\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0005\u0007_\b\u0007\n\u0007\f\u0007b\t"+
		"\u0007\u0001\b\u0001\b\u0001\b\u0005\bg\b\b\n\b\f\bj\t\b\u0001\b\u0000"+
		"\u0001\u0000\t\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0000\u0002\u0001"+
		"\u0000\u000b\r\u0001\u0000\u0001\bu\u0000\'\u0001\u0000\u0000\u0000\u0002"+
		"B\u0001\u0000\u0000\u0000\u0004H\u0001\u0000\u0000\u0000\u0006J\u0001"+
		"\u0000\u0000\u0000\bR\u0001\u0000\u0000\u0000\nU\u0001\u0000\u0000\u0000"+
		"\fY\u0001\u0000\u0000\u0000\u000e[\u0001\u0000\u0000\u0000\u0010c\u0001"+
		"\u0000\u0000\u0000\u0012\u0013\u0006\u0000\uffff\uffff\u0000\u0013(\u0005"+
		"\n\u0000\u0000\u0014(\u0005\t\u0000\u0000\u0015(\u0003\f\u0006\u0000\u0016"+
		"(\u0003\b\u0004\u0000\u0017\u0018\u0005\u0014\u0000\u0000\u0018(\u0003"+
		"\u0010\b\u0000\u0019(\u0003\u0006\u0003\u0000\u001a\u001b\u0005\u0010"+
		"\u0000\u0000\u001b\u001c\u0003\u0000\u0000\u0000\u001c\u001d\u0005\u0012"+
		"\u0000\u0000\u001d\u001e\u0003\u0000\u0000\u0000\u001e\u001f\u0005\u0011"+
		"\u0000\u0000\u001f(\u0001\u0000\u0000\u0000 \"\u0005\u000e\u0000\u0000"+
		"!#\u0003\u000e\u0007\u0000\"!\u0001\u0000\u0000\u0000\"#\u0001\u0000\u0000"+
		"\u0000#$\u0001\u0000\u0000\u0000$%\u0005\u000f\u0000\u0000%&\u0005\u001b"+
		"\u0000\u0000&(\u0003\u0000\u0000\u0001\'\u0012\u0001\u0000\u0000\u0000"+
		"\'\u0014\u0001\u0000\u0000\u0000\'\u0015\u0001\u0000\u0000\u0000\'\u0016"+
		"\u0001\u0000\u0000\u0000\'\u0017\u0001\u0000\u0000\u0000\'\u0019\u0001"+
		"\u0000\u0000\u0000\'\u001a\u0001\u0000\u0000\u0000\' \u0001\u0000\u0000"+
		"\u0000(?\u0001\u0000\u0000\u0000)*\n\u0005\u0000\u0000*,\u0005\u0010\u0000"+
		"\u0000+-\u0003\u0004\u0002\u0000,+\u0001\u0000\u0000\u0000,-\u0001\u0000"+
		"\u0000\u0000-.\u0001\u0000\u0000\u0000.>\u0005\u0011\u0000\u0000/2\n\u0004"+
		"\u0000\u000001\u0005\u0019\u0000\u000013\u0003\u0000\u0000\u000020\u0001"+
		"\u0000\u0000\u000034\u0001\u0000\u0000\u000042\u0001\u0000\u0000\u0000"+
		"45\u0001\u0000\u0000\u00005>\u0001\u0000\u0000\u000069\n\u0003\u0000\u0000"+
		"78\u0005\u0018\u0000\u00008:\u0003\u0000\u0000\u000097\u0001\u0000\u0000"+
		"\u0000:;\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000\u0000;<\u0001\u0000"+
		"\u0000\u0000<>\u0001\u0000\u0000\u0000=)\u0001\u0000\u0000\u0000=/\u0001"+
		"\u0000\u0000\u0000=6\u0001\u0000\u0000\u0000>A\u0001\u0000\u0000\u0000"+
		"?=\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000@\u0001\u0001\u0000"+
		"\u0000\u0000A?\u0001\u0000\u0000\u0000BC\u0003\u0006\u0003\u0000CD\u0005"+
		"\u0013\u0000\u0000DF\u0005\u001d\u0000\u0000EG\u0003\n\u0005\u0000FE\u0001"+
		"\u0000\u0000\u0000FG\u0001\u0000\u0000\u0000G\u0003\u0001\u0000\u0000"+
		"\u0000HI\u0007\u0000\u0000\u0000I\u0005\u0001\u0000\u0000\u0000JL\u0003"+
		"\u0010\b\u0000KM\u0003\n\u0005\u0000LK\u0001\u0000\u0000\u0000LM\u0001"+
		"\u0000\u0000\u0000MP\u0001\u0000\u0000\u0000NO\u0005\u0015\u0000\u0000"+
		"OQ\u0005\u001e\u0000\u0000PN\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000"+
		"\u0000Q\u0007\u0001\u0000\u0000\u0000RS\u0005\u001a\u0000\u0000ST\u0005"+
		"\u001d\u0000\u0000T\t\u0001\u0000\u0000\u0000UV\u0005\u0017\u0000\u0000"+
		"VW\u0003\u000e\u0007\u0000WX\u0005\u0016\u0000\u0000X\u000b\u0001\u0000"+
		"\u0000\u0000YZ\u0007\u0001\u0000\u0000Z\r\u0001\u0000\u0000\u0000[`\u0003"+
		"\u0000\u0000\u0000\\]\u0005\u0012\u0000\u0000]_\u0003\u0000\u0000\u0000"+
		"^\\\u0001\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000"+
		"\u0000`a\u0001\u0000\u0000\u0000a\u000f\u0001\u0000\u0000\u0000b`\u0001"+
		"\u0000\u0000\u0000ch\u0005\u001d\u0000\u0000de\u0005\u0013\u0000\u0000"+
		"eg\u0005\u001d\u0000\u0000fd\u0001\u0000\u0000\u0000gj\u0001\u0000\u0000"+
		"\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000i\u0011\u0001"+
		"\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000\f\"\',4;=?FLP`h";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}