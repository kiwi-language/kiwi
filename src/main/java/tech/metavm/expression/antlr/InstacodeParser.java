package tech.metavm.expression.antlr;// Generated from InstacodeParser.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class InstacodeParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ALL_MATCH=1, AS=2, DECIMAL_LITERAL=3, HEX_LITERAL=4, OCT_LITERAL=5, BINARY_LITERAL=6, 
		FLOAT_LITERAL=7, HEX_FLOAT_LITERAL=8, BOOL_LITERAL=9, SINGLE_QUOTED_STRING_LITERAL=10, 
		STRING_LITERAL=11, TEXT_BLOCK=12, NULL_LITERAL=13, LPAREN=14, RPAREN=15, 
		LBRACE=16, RBRACE=17, LBRACK=18, RBRACK=19, SEMI=20, COMMA=21, DOT=22, 
		EQUAL=23, GT=24, LT=25, BANG=26, TILDE=27, QUESTION=28, COLON=29, LE=30, 
		GE=31, NOTEQUAL=32, AND=33, OR=34, INC=35, DEC=36, ADD=37, SUB=38, MUL=39, 
		DIV=40, BITAND=41, BITOR=42, CARET=43, MOD=44, ARROW=45, COLONCOLON=46, 
		AT=47, ELLIPSIS=48, WS=49, COMMENT=50, LINE_COMMENT=51, IDENTIFIER=52, 
		THIS=53, SUPER=54, CLASS=55, NEW=56, INSTANCEOF=57, FINAL=58, MODULE=59, 
		OPEN=60, REQUIRES=61, EXPORTS=62, OPENS=63, TO=64, USES=65, PROVIDES=66, 
		WITH=67, TRANSITIVE=68, YIELD=69, SEALED=70, PERMITS=71, RECORD=72, VAR=73, 
		VOID=74, EXTENDS=75, BOOLEAN=76, CHAR=77, BYTE=78, SHORT=79, INT=80, LONG=81, 
		FLOAT=82, DOUBLE=83;
	public static final int
		RULE_primary = 0, RULE_expression = 1, RULE_allMatch = 2, RULE_list = 3, 
		RULE_pattern = 4, RULE_variableModifier = 5, RULE_innerCreator = 6, RULE_creator = 7, 
		RULE_nonWildcardTypeArgumentsOrDiamond = 8, RULE_explicitGenericInvocation = 9, 
		RULE_classCreatorRest = 10, RULE_arrayCreatorRest = 11, RULE_arrayInitializer = 12, 
		RULE_variableInitializer = 13, RULE_createdName = 14, RULE_typeArgumentsOrDiamond = 15, 
		RULE_methodCall = 16, RULE_expressionList = 17, RULE_explicitGenericInvocationSuffix = 18, 
		RULE_superSuffix = 19, RULE_arguments = 20, RULE_elementValueArrayInitializer = 21, 
		RULE_literal = 22, RULE_integerLiteral = 23, RULE_floatLiteral = 24, RULE_identifier = 25, 
		RULE_typeIdentifier = 26, RULE_typeTypeOrVoid = 27, RULE_elementValuePairs = 28, 
		RULE_elementValuePair = 29, RULE_elementValue = 30, RULE_qualifiedName = 31, 
		RULE_altAnnotationQualifiedName = 32, RULE_annotation = 33, RULE_typeArgument = 34, 
		RULE_typeArguments = 35, RULE_classOrInterfaceType = 36, RULE_typeType = 37, 
		RULE_primitiveType = 38, RULE_nonWildcardTypeArguments = 39, RULE_typeList = 40;
	private static String[] makeRuleNames() {
		return new String[] {
			"primary", "expression", "allMatch", "list", "pattern", "variableModifier", 
			"innerCreator", "creator", "nonWildcardTypeArgumentsOrDiamond", "explicitGenericInvocation", 
			"classCreatorRest", "arrayCreatorRest", "arrayInitializer", "variableInitializer", 
			"createdName", "typeArgumentsOrDiamond", "methodCall", "expressionList", 
			"explicitGenericInvocationSuffix", "superSuffix", "arguments", "elementValueArrayInitializer", 
			"literal", "integerLiteral", "floatLiteral", "identifier", "typeIdentifier", 
			"typeTypeOrVoid", "elementValuePairs", "elementValuePair", "elementValue", 
			"qualifiedName", "altAnnotationQualifiedName", "annotation", "typeArgument", 
			"typeArguments", "classOrInterfaceType", "typeType", "primitiveType", 
			"nonWildcardTypeArguments", "typeList"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'AllMatch'", "'AS'", null, null, null, null, null, null, null, 
			null, null, null, "'null'", "'('", "')'", "'{'", "'}'", "'['", "']'", 
			"';'", "','", "'.'", "'='", "'>'", "'<'", "'!'", "'~'", "'?'", "':'", 
			"'<='", "'>='", "'!='", "'AND'", "'OR'", "'++'", "'--'", "'+'", "'-'", 
			"'*'", "'/'", "'&'", "'|'", "'^'", "'%'", "'->'", "'::'", "'@'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ALL_MATCH", "AS", "DECIMAL_LITERAL", "HEX_LITERAL", "OCT_LITERAL", 
			"BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", "BOOL_LITERAL", 
			"SINGLE_QUOTED_STRING_LITERAL", "STRING_LITERAL", "TEXT_BLOCK", "NULL_LITERAL", 
			"LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", 
			"DOT", "EQUAL", "GT", "LT", "BANG", "TILDE", "QUESTION", "COLON", "LE", 
			"GE", "NOTEQUAL", "AND", "OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", 
			"BITAND", "BITOR", "CARET", "MOD", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", 
			"WS", "COMMENT", "LINE_COMMENT", "IDENTIFIER", "THIS", "SUPER", "CLASS", 
			"NEW", "INSTANCEOF", "FINAL", "MODULE", "OPEN", "REQUIRES", "EXPORTS", 
			"OPENS", "TO", "USES", "PROVIDES", "WITH", "TRANSITIVE", "YIELD", "SEALED", 
			"PERMITS", "RECORD", "VAR", "VOID", "EXTENDS", "BOOLEAN", "CHAR", "BYTE", 
			"SHORT", "INT", "LONG", "FLOAT", "DOUBLE"
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
	public String getGrammarFileName() { return "InstacodeParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public InstacodeParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(InstacodeParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(InstacodeParser.RPAREN, 0); }
		public TerminalNode THIS() { return getToken(InstacodeParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(InstacodeParser.SUPER, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public TerminalNode DOT() { return getToken(InstacodeParser.DOT, 0); }
		public TerminalNode CLASS() { return getToken(InstacodeParser.CLASS, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() {
			return getRuleContext(ExplicitGenericInvocationSuffixContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitPrimary(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_primary);
		try {
			setState(100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(82);
				match(LPAREN);
				setState(83);
				expression(0);
				setState(84);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
				match(THIS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(87);
				match(SUPER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(88);
				literal();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(89);
				identifier();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(90);
				typeTypeOrVoid();
				setState(91);
				match(DOT);
				setState(92);
				match(CLASS);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(94);
				nonWildcardTypeArguments();
				setState(98);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case IDENTIFIER:
				case SUPER:
				case MODULE:
				case OPEN:
				case REQUIRES:
				case EXPORTS:
				case OPENS:
				case TO:
				case USES:
				case PROVIDES:
				case WITH:
				case TRANSITIVE:
				case YIELD:
				case SEALED:
				case PERMITS:
				case RECORD:
				case VAR:
					{
					setState(95);
					explicitGenericInvocationSuffix();
					}
					break;
				case THIS:
					{
					setState(96);
					match(THIS);
					setState(97);
					arguments();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
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
	public static class ExpressionContext extends ParserRuleContext {
		public Token bop;
		public Token prefix;
		public Token postfix;
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode DOT() { return getToken(InstacodeParser.DOT, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public AllMatchContext allMatch() {
			return getRuleContext(AllMatchContext.class,0);
		}
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public TerminalNode NEW() { return getToken(InstacodeParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(InstacodeParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(InstacodeParser.RPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> BITAND() { return getTokens(InstacodeParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(InstacodeParser.BITAND, i);
		}
		public TerminalNode ADD() { return getToken(InstacodeParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(InstacodeParser.SUB, 0); }
		public TerminalNode INC() { return getToken(InstacodeParser.INC, 0); }
		public TerminalNode DEC() { return getToken(InstacodeParser.DEC, 0); }
		public TerminalNode TILDE() { return getToken(InstacodeParser.TILDE, 0); }
		public TerminalNode BANG() { return getToken(InstacodeParser.BANG, 0); }
		public TerminalNode MUL() { return getToken(InstacodeParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(InstacodeParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(InstacodeParser.MOD, 0); }
		public List<TerminalNode> LT() { return getTokens(InstacodeParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(InstacodeParser.LT, i);
		}
		public List<TerminalNode> GT() { return getTokens(InstacodeParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(InstacodeParser.GT, i);
		}
		public TerminalNode LE() { return getToken(InstacodeParser.LE, 0); }
		public TerminalNode GE() { return getToken(InstacodeParser.GE, 0); }
		public TerminalNode EQUAL() { return getToken(InstacodeParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(InstacodeParser.NOTEQUAL, 0); }
		public TerminalNode CARET() { return getToken(InstacodeParser.CARET, 0); }
		public TerminalNode BITOR() { return getToken(InstacodeParser.BITOR, 0); }
		public TerminalNode AND() { return getToken(InstacodeParser.AND, 0); }
		public TerminalNode OR() { return getToken(InstacodeParser.OR, 0); }
		public TerminalNode COLON() { return getToken(InstacodeParser.COLON, 0); }
		public TerminalNode QUESTION() { return getToken(InstacodeParser.QUESTION, 0); }
		public TerminalNode THIS() { return getToken(InstacodeParser.THIS, 0); }
		public InnerCreatorContext innerCreator() {
			return getRuleContext(InnerCreatorContext.class,0);
		}
		public TerminalNode SUPER() { return getToken(InstacodeParser.SUPER, 0); }
		public SuperSuffixContext superSuffix() {
			return getRuleContext(SuperSuffixContext.class,0);
		}
		public ExplicitGenericInvocationContext explicitGenericInvocation() {
			return getRuleContext(ExplicitGenericInvocationContext.class,0);
		}
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(InstacodeParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(InstacodeParser.RBRACK, 0); }
		public TerminalNode AS() { return getToken(InstacodeParser.AS, 0); }
		public TerminalNode IDENTIFIER() { return getToken(InstacodeParser.IDENTIFIER, 0); }
		public TerminalNode INSTANCEOF() { return getToken(InstacodeParser.INSTANCEOF, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitExpression(this);
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
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(103);
				primary();
				}
				break;
			case 2:
				{
				setState(104);
				typeType();
				setState(105);
				((ExpressionContext)_localctx).bop = match(DOT);
				setState(106);
				identifier();
				}
				break;
			case 3:
				{
				setState(108);
				list();
				}
				break;
			case 4:
				{
				setState(109);
				allMatch();
				}
				break;
			case 5:
				{
				setState(110);
				methodCall();
				}
				break;
			case 6:
				{
				setState(111);
				match(NEW);
				setState(112);
				creator();
				}
				break;
			case 7:
				{
				setState(113);
				match(LPAREN);
				setState(117);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(114);
						annotation();
						}
						} 
					}
					setState(119);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				setState(120);
				typeType();
				setState(125);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==BITAND) {
					{
					{
					setState(121);
					match(BITAND);
					setState(122);
					typeType();
					}
					}
					setState(127);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(128);
				match(RPAREN);
				setState(129);
				expression(16);
				}
				break;
			case 8:
				{
				setState(131);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 515396075520L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(132);
				expression(14);
				}
				break;
			case 9:
				{
				setState(133);
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
				setState(134);
				expression(13);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(214);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(212);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(137);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(138);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 19241453486080L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(139);
						expression(13);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(140);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(141);
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
						setState(142);
						expression(12);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(143);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(151);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
						case 1:
							{
							setState(144);
							match(LT);
							setState(145);
							match(LT);
							}
							break;
						case 2:
							{
							setState(146);
							match(GT);
							setState(147);
							match(GT);
							setState(148);
							match(GT);
							}
							break;
						case 3:
							{
							setState(149);
							match(GT);
							setState(150);
							match(GT);
							}
							break;
						}
						setState(153);
						expression(11);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(154);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(155);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3271557120L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(156);
						expression(10);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(157);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(158);
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
						setState(159);
						expression(8);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(160);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(161);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(162);
						expression(7);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(163);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(164);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(165);
						expression(6);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(166);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(167);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(168);
						expression(5);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(169);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(170);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(171);
						expression(4);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(172);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(173);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(174);
						expression(3);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(175);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(176);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(177);
						expression(0);
						setState(178);
						match(COLON);
						setState(179);
						expression(1);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(181);
						if (!(precpred(_ctx, 23))) throw new FailedPredicateException(this, "precpred(_ctx, 23)");
						setState(182);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(194);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
						case 1:
							{
							setState(183);
							identifier();
							}
							break;
						case 2:
							{
							setState(184);
							methodCall();
							}
							break;
						case 3:
							{
							setState(185);
							match(THIS);
							}
							break;
						case 4:
							{
							setState(186);
							match(NEW);
							setState(188);
							_errHandler.sync(this);
							_la = _input.LA(1);
							if (_la==LT) {
								{
								setState(187);
								nonWildcardTypeArguments();
								}
							}

							setState(190);
							innerCreator();
							}
							break;
						case 5:
							{
							setState(191);
							match(SUPER);
							setState(192);
							superSuffix();
							}
							break;
						case 6:
							{
							setState(193);
							explicitGenericInvocation();
							}
							break;
						}
						}
						break;
					case 13:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(196);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(197);
						match(LBRACK);
						setState(198);
						expression(0);
						setState(199);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(201);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(202);
						match(AS);
						setState(203);
						match(IDENTIFIER);
						}
						break;
					case 15:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(204);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(205);
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
					case 16:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(206);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(207);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(210);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
						case 1:
							{
							setState(208);
							typeType();
							}
							break;
						case 2:
							{
							setState(209);
							pattern();
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(216);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
	public static class AllMatchContext extends ParserRuleContext {
		public TerminalNode ALL_MATCH() { return getToken(InstacodeParser.ALL_MATCH, 0); }
		public TerminalNode LPAREN() { return getToken(InstacodeParser.LPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(InstacodeParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(InstacodeParser.RPAREN, 0); }
		public AllMatchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allMatch; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterAllMatch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitAllMatch(this);
		}
	}

	public final AllMatchContext allMatch() throws RecognitionException {
		AllMatchContext _localctx = new AllMatchContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_allMatch);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(217);
			match(ALL_MATCH);
			setState(218);
			match(LPAREN);
			setState(219);
			expression(0);
			setState(220);
			match(COMMA);
			setState(221);
			expression(0);
			setState(222);
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
	public static class ListContext extends ParserRuleContext {
		public TerminalNode LBRACK() { return getToken(InstacodeParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(InstacodeParser.RBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitList(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
			match(LBRACK);
			setState(226);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754295302L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
				{
				setState(225);
				expressionList();
				}
			}

			setState(228);
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
	public static class PatternContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public PatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitPattern(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_pattern);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(230);
					variableModifier();
					}
					} 
				}
				setState(235);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			}
			setState(236);
			typeType();
			setState(240);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(237);
					annotation();
					}
					} 
				}
				setState(242);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			setState(243);
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
	public static class VariableModifierContext extends ParserRuleContext {
		public TerminalNode FINAL() { return getToken(InstacodeParser.FINAL, 0); }
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public VariableModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterVariableModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitVariableModifier(this);
		}
	}

	public final VariableModifierContext variableModifier() throws RecognitionException {
		VariableModifierContext _localctx = new VariableModifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_variableModifier);
		try {
			setState(247);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FINAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(245);
				match(FINAL);
				}
				break;
			case AT:
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(246);
				annotation();
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
	public static class InnerCreatorContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ClassCreatorRestContext classCreatorRest() {
			return getRuleContext(ClassCreatorRestContext.class,0);
		}
		public NonWildcardTypeArgumentsOrDiamondContext nonWildcardTypeArgumentsOrDiamond() {
			return getRuleContext(NonWildcardTypeArgumentsOrDiamondContext.class,0);
		}
		public InnerCreatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_innerCreator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterInnerCreator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitInnerCreator(this);
		}
	}

	public final InnerCreatorContext innerCreator() throws RecognitionException {
		InnerCreatorContext _localctx = new InnerCreatorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_innerCreator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(249);
			identifier();
			setState(251);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(250);
				nonWildcardTypeArgumentsOrDiamond();
				}
			}

			setState(253);
			classCreatorRest();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
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
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public CreatedNameContext createdName() {
			return getRuleContext(CreatedNameContext.class,0);
		}
		public ClassCreatorRestContext classCreatorRest() {
			return getRuleContext(ClassCreatorRestContext.class,0);
		}
		public ArrayCreatorRestContext arrayCreatorRest() {
			return getRuleContext(ArrayCreatorRestContext.class,0);
		}
		public CreatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_creator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterCreator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitCreator(this);
		}
	}

	public final CreatorContext creator() throws RecognitionException {
		CreatorContext _localctx = new CreatorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_creator);
		try {
			setState(264);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LT:
				enterOuterAlt(_localctx, 1);
				{
				setState(255);
				nonWildcardTypeArguments();
				setState(256);
				createdName();
				setState(257);
				classCreatorRest();
				}
				break;
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
			case BOOLEAN:
			case CHAR:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(259);
				createdName();
				setState(262);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LBRACK:
					{
					setState(260);
					arrayCreatorRest();
					}
					break;
				case LPAREN:
					{
					setState(261);
					classCreatorRest();
					}
					break;
				default:
					throw new NoViableAltException(this);
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
	public static class NonWildcardTypeArgumentsOrDiamondContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(InstacodeParser.LT, 0); }
		public TerminalNode GT() { return getToken(InstacodeParser.GT, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public NonWildcardTypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArgumentsOrDiamond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterNonWildcardTypeArgumentsOrDiamond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitNonWildcardTypeArgumentsOrDiamond(this);
		}
	}

	public final NonWildcardTypeArgumentsOrDiamondContext nonWildcardTypeArgumentsOrDiamond() throws RecognitionException {
		NonWildcardTypeArgumentsOrDiamondContext _localctx = new NonWildcardTypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_nonWildcardTypeArgumentsOrDiamond);
		try {
			setState(269);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(266);
				match(LT);
				setState(267);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(268);
				nonWildcardTypeArguments();
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
	public static class ExplicitGenericInvocationContext extends ParserRuleContext {
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
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
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterExplicitGenericInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitExplicitGenericInvocation(this);
		}
	}

	public final ExplicitGenericInvocationContext explicitGenericInvocation() throws RecognitionException {
		ExplicitGenericInvocationContext _localctx = new ExplicitGenericInvocationContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			nonWildcardTypeArguments();
			setState(272);
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
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterClassCreatorRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitClassCreatorRest(this);
		}
	}

	public final ClassCreatorRestContext classCreatorRest() throws RecognitionException {
		ClassCreatorRestContext _localctx = new ClassCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
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
		public List<TerminalNode> LBRACK() { return getTokens(InstacodeParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(InstacodeParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(InstacodeParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(InstacodeParser.RBRACK, i);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ArrayCreatorRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayCreatorRest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterArrayCreatorRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitArrayCreatorRest(this);
		}
	}

	public final ArrayCreatorRestContext arrayCreatorRest() throws RecognitionException {
		ArrayCreatorRestContext _localctx = new ArrayCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_arrayCreatorRest);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
			match(LBRACK);
			setState(304);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case RBRACK:
				{
				setState(277);
				match(RBRACK);
				setState(282);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LBRACK) {
					{
					{
					setState(278);
					match(LBRACK);
					setState(279);
					match(RBRACK);
					}
					}
					setState(284);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(285);
				arrayInitializer();
				}
				break;
			case ALL_MATCH:
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
			case BOOL_LITERAL:
			case SINGLE_QUOTED_STRING_LITERAL:
			case STRING_LITERAL:
			case TEXT_BLOCK:
			case NULL_LITERAL:
			case LPAREN:
			case LBRACK:
			case LT:
			case BANG:
			case TILDE:
			case INC:
			case DEC:
			case ADD:
			case SUB:
			case AT:
			case IDENTIFIER:
			case THIS:
			case SUPER:
			case NEW:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
			case VOID:
			case BOOLEAN:
			case CHAR:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				{
				setState(286);
				expression(0);
				setState(287);
				match(RBRACK);
				setState(294);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(288);
						match(LBRACK);
						setState(289);
						expression(0);
						setState(290);
						match(RBRACK);
						}
						} 
					}
					setState(296);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				}
				setState(301);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(297);
						match(LBRACK);
						setState(298);
						match(RBRACK);
						}
						} 
					}
					setState(303);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				}
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
	public static class ArrayInitializerContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(InstacodeParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(InstacodeParser.RBRACE, 0); }
		public List<VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(InstacodeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(InstacodeParser.COMMA, i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitArrayInitializer(this);
		}
	}

	public final ArrayInitializerContext arrayInitializer() throws RecognitionException {
		ArrayInitializerContext _localctx = new ArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			match(LBRACE);
			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754229766L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
				{
				setState(307);
				variableInitializer();
				setState(312);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(308);
						match(COMMA);
						setState(309);
						variableInitializer();
						}
						} 
					}
					setState(314);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
				}
				setState(316);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(315);
					match(COMMA);
					}
				}

				}
			}

			setState(320);
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
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterVariableInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitVariableInitializer(this);
		}
	}

	public final VariableInitializerContext variableInitializer() throws RecognitionException {
		VariableInitializerContext _localctx = new VariableInitializerContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_variableInitializer);
		try {
			setState(324);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(322);
				arrayInitializer();
				}
				break;
			case ALL_MATCH:
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
			case BOOL_LITERAL:
			case SINGLE_QUOTED_STRING_LITERAL:
			case STRING_LITERAL:
			case TEXT_BLOCK:
			case NULL_LITERAL:
			case LPAREN:
			case LBRACK:
			case LT:
			case BANG:
			case TILDE:
			case INC:
			case DEC:
			case ADD:
			case SUB:
			case AT:
			case IDENTIFIER:
			case THIS:
			case SUPER:
			case NEW:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
			case VOID:
			case BOOLEAN:
			case CHAR:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(323);
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
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TypeArgumentsOrDiamondContext> typeArgumentsOrDiamond() {
			return getRuleContexts(TypeArgumentsOrDiamondContext.class);
		}
		public TypeArgumentsOrDiamondContext typeArgumentsOrDiamond(int i) {
			return getRuleContext(TypeArgumentsOrDiamondContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(InstacodeParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(InstacodeParser.DOT, i);
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
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterCreatedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitCreatedName(this);
		}
	}

	public final CreatedNameContext createdName() throws RecognitionException {
		CreatedNameContext _localctx = new CreatedNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_createdName);
		int _la;
		try {
			setState(341);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(326);
				identifier();
				setState(328);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(327);
					typeArgumentsOrDiamond();
					}
				}

				setState(337);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(330);
					match(DOT);
					setState(331);
					identifier();
					setState(333);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LT) {
						{
						setState(332);
						typeArgumentsOrDiamond();
						}
					}

					}
					}
					setState(339);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case BOOLEAN:
			case CHAR:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(340);
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
	public static class TypeArgumentsOrDiamondContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(InstacodeParser.LT, 0); }
		public TerminalNode GT() { return getToken(InstacodeParser.GT, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public TypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgumentsOrDiamond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeArgumentsOrDiamond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeArgumentsOrDiamond(this);
		}
	}

	public final TypeArgumentsOrDiamondContext typeArgumentsOrDiamond() throws RecognitionException {
		TypeArgumentsOrDiamondContext _localctx = new TypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_typeArgumentsOrDiamond);
		try {
			setState(346);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(343);
				match(LT);
				setState(344);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(345);
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
	public static class MethodCallContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(InstacodeParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(InstacodeParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode THIS() { return getToken(InstacodeParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(InstacodeParser.SUPER, 0); }
		public MethodCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitMethodCall(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_methodCall);
		int _la;
		try {
			setState(367);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(348);
				identifier();
				setState(349);
				match(LPAREN);
				setState(351);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754295302L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
					{
					setState(350);
					expressionList();
					}
				}

				setState(353);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(355);
				match(THIS);
				setState(356);
				match(LPAREN);
				setState(358);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754295302L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
					{
					setState(357);
					expressionList();
					}
				}

				setState(360);
				match(RPAREN);
				}
				break;
			case SUPER:
				enterOuterAlt(_localctx, 3);
				{
				setState(361);
				match(SUPER);
				setState(362);
				match(LPAREN);
				setState(364);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754295302L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
					{
					setState(363);
					expressionList();
					}
				}

				setState(366);
				match(RPAREN);
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
	public static class ExpressionListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(InstacodeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(InstacodeParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitExpressionList(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			expression(0);
			setState(374);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(370);
				match(COMMA);
				setState(371);
				expression(0);
				}
				}
				setState(376);
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
	public static class ExplicitGenericInvocationSuffixContext extends ParserRuleContext {
		public TerminalNode SUPER() { return getToken(InstacodeParser.SUPER, 0); }
		public SuperSuffixContext superSuffix() {
			return getRuleContext(SuperSuffixContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitGenericInvocationSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterExplicitGenericInvocationSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitExplicitGenericInvocationSuffix(this);
		}
	}

	public final ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() throws RecognitionException {
		ExplicitGenericInvocationSuffixContext _localctx = new ExplicitGenericInvocationSuffixContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_explicitGenericInvocationSuffix);
		try {
			setState(382);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(377);
				match(SUPER);
				setState(378);
				superSuffix();
				}
				break;
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(379);
				identifier();
				setState(380);
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
		public TerminalNode DOT() { return getToken(InstacodeParser.DOT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public SuperSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_superSuffix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterSuperSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitSuperSuffix(this);
		}
	}

	public final SuperSuffixContext superSuffix() throws RecognitionException {
		SuperSuffixContext _localctx = new SuperSuffixContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_superSuffix);
		int _la;
		try {
			setState(393);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(384);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(385);
				match(DOT);
				setState(387);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(386);
					typeArguments();
					}
				}

				setState(389);
				identifier();
				setState(391);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
				case 1:
					{
					setState(390);
					arguments();
					}
					break;
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
		public TerminalNode LPAREN() { return getToken(InstacodeParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(InstacodeParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitArguments(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			match(LPAREN);
			setState(397);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754295302L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
				{
				setState(396);
				expressionList();
				}
			}

			setState(399);
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
	public static class ElementValueArrayInitializerContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(InstacodeParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(InstacodeParser.RBRACE, 0); }
		public List<ElementValueContext> elementValue() {
			return getRuleContexts(ElementValueContext.class);
		}
		public ElementValueContext elementValue(int i) {
			return getRuleContext(ElementValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(InstacodeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(InstacodeParser.COMMA, i);
		}
		public ElementValueArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValueArrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterElementValueArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitElementValueArrayInitializer(this);
		}
	}

	public final ElementValueArrayInitializerContext elementValueArrayInitializer() throws RecognitionException {
		ElementValueArrayInitializerContext _localctx = new ElementValueArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(401);
			match(LBRACE);
			setState(410);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -472736707754229766L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1046527L) != 0)) {
				{
				setState(402);
				elementValue();
				setState(407);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
				while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(403);
						match(COMMA);
						setState(404);
						elementValue();
						}
						} 
					}
					setState(409);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
				}
				}
			}

			setState(413);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(412);
				match(COMMA);
				}
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
	public static class LiteralContext extends ParserRuleContext {
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public FloatLiteralContext floatLiteral() {
			return getRuleContext(FloatLiteralContext.class,0);
		}
		public TerminalNode SINGLE_QUOTED_STRING_LITERAL() { return getToken(InstacodeParser.SINGLE_QUOTED_STRING_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(InstacodeParser.STRING_LITERAL, 0); }
		public TerminalNode BOOL_LITERAL() { return getToken(InstacodeParser.BOOL_LITERAL, 0); }
		public TerminalNode NULL_LITERAL() { return getToken(InstacodeParser.NULL_LITERAL, 0); }
		public TerminalNode TEXT_BLOCK() { return getToken(InstacodeParser.TEXT_BLOCK, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitLiteral(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_literal);
		try {
			setState(424);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(417);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(418);
				floatLiteral();
				}
				break;
			case SINGLE_QUOTED_STRING_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(419);
				match(SINGLE_QUOTED_STRING_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(420);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(421);
				match(BOOL_LITERAL);
				}
				break;
			case NULL_LITERAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(422);
				match(NULL_LITERAL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(423);
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
		public TerminalNode DECIMAL_LITERAL() { return getToken(InstacodeParser.DECIMAL_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(InstacodeParser.HEX_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(InstacodeParser.OCT_LITERAL, 0); }
		public TerminalNode BINARY_LITERAL() { return getToken(InstacodeParser.BINARY_LITERAL, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitIntegerLiteral(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(426);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 120L) != 0)) ) {
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
		public TerminalNode FLOAT_LITERAL() { return getToken(InstacodeParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(InstacodeParser.HEX_FLOAT_LITERAL, 0); }
		public FloatLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitFloatLiteral(this);
		}
	}

	public final FloatLiteralContext floatLiteral() throws RecognitionException {
		FloatLiteralContext _localctx = new FloatLiteralContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
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
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(InstacodeParser.IDENTIFIER, 0); }
		public TerminalNode MODULE() { return getToken(InstacodeParser.MODULE, 0); }
		public TerminalNode OPEN() { return getToken(InstacodeParser.OPEN, 0); }
		public TerminalNode REQUIRES() { return getToken(InstacodeParser.REQUIRES, 0); }
		public TerminalNode EXPORTS() { return getToken(InstacodeParser.EXPORTS, 0); }
		public TerminalNode OPENS() { return getToken(InstacodeParser.OPENS, 0); }
		public TerminalNode TO() { return getToken(InstacodeParser.TO, 0); }
		public TerminalNode USES() { return getToken(InstacodeParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(InstacodeParser.PROVIDES, 0); }
		public TerminalNode WITH() { return getToken(InstacodeParser.WITH, 0); }
		public TerminalNode TRANSITIVE() { return getToken(InstacodeParser.TRANSITIVE, 0); }
		public TerminalNode YIELD() { return getToken(InstacodeParser.YIELD, 0); }
		public TerminalNode SEALED() { return getToken(InstacodeParser.SEALED, 0); }
		public TerminalNode PERMITS() { return getToken(InstacodeParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(InstacodeParser.RECORD, 0); }
		public TerminalNode VAR() { return getToken(InstacodeParser.VAR, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			_la = _input.LA(1);
			if ( !(((((_la - 52)) & ~0x3f) == 0 && ((1L << (_la - 52)) & 4194177L) != 0)) ) {
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
	public static class TypeIdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(InstacodeParser.IDENTIFIER, 0); }
		public TerminalNode MODULE() { return getToken(InstacodeParser.MODULE, 0); }
		public TerminalNode OPEN() { return getToken(InstacodeParser.OPEN, 0); }
		public TerminalNode REQUIRES() { return getToken(InstacodeParser.REQUIRES, 0); }
		public TerminalNode EXPORTS() { return getToken(InstacodeParser.EXPORTS, 0); }
		public TerminalNode OPENS() { return getToken(InstacodeParser.OPENS, 0); }
		public TerminalNode TO() { return getToken(InstacodeParser.TO, 0); }
		public TerminalNode USES() { return getToken(InstacodeParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(InstacodeParser.PROVIDES, 0); }
		public TerminalNode WITH() { return getToken(InstacodeParser.WITH, 0); }
		public TerminalNode TRANSITIVE() { return getToken(InstacodeParser.TRANSITIVE, 0); }
		public TerminalNode SEALED() { return getToken(InstacodeParser.SEALED, 0); }
		public TerminalNode PERMITS() { return getToken(InstacodeParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(InstacodeParser.RECORD, 0); }
		public TypeIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeIdentifier(this);
		}
	}

	public final TypeIdentifierContext typeIdentifier() throws RecognitionException {
		TypeIdentifierContext _localctx = new TypeIdentifierContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_typeIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(432);
			_la = _input.LA(1);
			if ( !(((((_la - 52)) & ~0x3f) == 0 && ((1L << (_la - 52)) & 1965953L) != 0)) ) {
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
		public TerminalNode VOID() { return getToken(InstacodeParser.VOID, 0); }
		public TypeTypeOrVoidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeTypeOrVoid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeTypeOrVoid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeTypeOrVoid(this);
		}
	}

	public final TypeTypeOrVoidContext typeTypeOrVoid() throws RecognitionException {
		TypeTypeOrVoidContext _localctx = new TypeTypeOrVoidContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_typeTypeOrVoid);
		try {
			setState(436);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
			case BOOLEAN:
			case CHAR:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				enterOuterAlt(_localctx, 1);
				{
				setState(434);
				typeType();
				}
				break;
			case VOID:
				enterOuterAlt(_localctx, 2);
				{
				setState(435);
				match(VOID);
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
	public static class ElementValuePairsContext extends ParserRuleContext {
		public List<ElementValuePairContext> elementValuePair() {
			return getRuleContexts(ElementValuePairContext.class);
		}
		public ElementValuePairContext elementValuePair(int i) {
			return getRuleContext(ElementValuePairContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(InstacodeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(InstacodeParser.COMMA, i);
		}
		public ElementValuePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePairs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterElementValuePairs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitElementValuePairs(this);
		}
	}

	public final ElementValuePairsContext elementValuePairs() throws RecognitionException {
		ElementValuePairsContext _localctx = new ElementValuePairsContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			elementValuePair();
			setState(443);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(439);
				match(COMMA);
				setState(440);
				elementValuePair();
				}
				}
				setState(445);
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
		public TerminalNode EQUAL() { return getToken(InstacodeParser.EQUAL, 0); }
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public ElementValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterElementValuePair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitElementValuePair(this);
		}
	}

	public final ElementValuePairContext elementValuePair() throws RecognitionException {
		ElementValuePairContext _localctx = new ElementValuePairContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(446);
			identifier();
			setState(447);
			match(EQUAL);
			setState(448);
			elementValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementValueContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public ElementValueArrayInitializerContext elementValueArrayInitializer() {
			return getRuleContext(ElementValueArrayInitializerContext.class,0);
		}
		public ElementValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterElementValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitElementValue(this);
		}
	}

	public final ElementValueContext elementValue() throws RecognitionException {
		ElementValueContext _localctx = new ElementValueContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_elementValue);
		try {
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(450);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(451);
				annotation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(452);
				elementValueArrayInitializer();
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
	public static class QualifiedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(InstacodeParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(InstacodeParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitQualifiedName(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_qualifiedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
			identifier();
			setState(460);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(456);
				match(DOT);
				setState(457);
				identifier();
				}
				}
				setState(462);
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
	public static class AltAnnotationQualifiedNameContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(InstacodeParser.AT, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(InstacodeParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(InstacodeParser.DOT, i);
		}
		public AltAnnotationQualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_altAnnotationQualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterAltAnnotationQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitAltAnnotationQualifiedName(this);
		}
	}

	public final AltAnnotationQualifiedNameContext altAnnotationQualifiedName() throws RecognitionException {
		AltAnnotationQualifiedNameContext _localctx = new AltAnnotationQualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_altAnnotationQualifiedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(468);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 52)) & ~0x3f) == 0 && ((1L << (_la - 52)) & 4194177L) != 0)) {
				{
				{
				setState(463);
				identifier();
				setState(464);
				match(DOT);
				}
				}
				setState(470);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(471);
			match(AT);
			setState(472);
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
	public static class AnnotationContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(InstacodeParser.AT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public AltAnnotationQualifiedNameContext altAnnotationQualifiedName() {
			return getRuleContext(AltAnnotationQualifiedNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(InstacodeParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(InstacodeParser.RPAREN, 0); }
		public ElementValuePairsContext elementValuePairs() {
			return getRuleContext(ElementValuePairsContext.class,0);
		}
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitAnnotation(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(477);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				{
				setState(474);
				match(AT);
				setState(475);
				qualifiedName();
				}
				break;
			case 2:
				{
				setState(476);
				altAnnotationQualifiedName();
				}
				break;
			}
			setState(485);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(479);
				match(LPAREN);
				setState(482);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
				case 1:
					{
					setState(480);
					elementValuePairs();
					}
					break;
				case 2:
					{
					setState(481);
					elementValue();
					}
					break;
				}
				setState(484);
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
	public static class TypeArgumentContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode QUESTION() { return getToken(InstacodeParser.QUESTION, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode EXTENDS() { return getToken(InstacodeParser.EXTENDS, 0); }
		public TerminalNode SUPER() { return getToken(InstacodeParser.SUPER, 0); }
		public TypeArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeArgument(this);
		}
	}

	public final TypeArgumentContext typeArgument() throws RecognitionException {
		TypeArgumentContext _localctx = new TypeArgumentContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_typeArgument);
		int _la;
		try {
			setState(499);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(487);
				typeType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(491);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((((_la - 47)) & ~0x3f) == 0 && ((1L << (_la - 47)) & 134213665L) != 0)) {
					{
					{
					setState(488);
					annotation();
					}
					}
					setState(493);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(494);
				match(QUESTION);
				setState(497);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SUPER || _la==EXTENDS) {
					{
					setState(495);
					_la = _input.LA(1);
					if ( !(_la==SUPER || _la==EXTENDS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(496);
					typeType();
					}
				}

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
	public static class TypeArgumentsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(InstacodeParser.LT, 0); }
		public List<TypeArgumentContext> typeArgument() {
			return getRuleContexts(TypeArgumentContext.class);
		}
		public TypeArgumentContext typeArgument(int i) {
			return getRuleContext(TypeArgumentContext.class,i);
		}
		public TerminalNode GT() { return getToken(InstacodeParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(InstacodeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(InstacodeParser.COMMA, i);
		}
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeArguments(this);
		}
	}

	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(501);
			match(LT);
			setState(502);
			typeArgument();
			setState(507);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(503);
				match(COMMA);
				setState(504);
				typeArgument();
				}
				}
				setState(509);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(510);
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
	public static class ClassOrInterfaceTypeContext extends ParserRuleContext {
		public TypeIdentifierContext typeIdentifier() {
			return getRuleContext(TypeIdentifierContext.class,0);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(InstacodeParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(InstacodeParser.DOT, i);
		}
		public List<TypeArgumentsContext> typeArguments() {
			return getRuleContexts(TypeArgumentsContext.class);
		}
		public TypeArgumentsContext typeArguments(int i) {
			return getRuleContext(TypeArgumentsContext.class,i);
		}
		public ClassOrInterfaceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterClassOrInterfaceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitClassOrInterfaceType(this);
		}
	}

	public final ClassOrInterfaceTypeContext classOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_classOrInterfaceType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(520);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(512);
					identifier();
					setState(514);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LT) {
						{
						setState(513);
						typeArguments();
						}
					}

					setState(516);
					match(DOT);
					}
					} 
				}
				setState(522);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			}
			setState(523);
			typeIdentifier();
			setState(525);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(524);
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
	public static class TypeTypeContext extends ParserRuleContext {
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> LBRACK() { return getTokens(InstacodeParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(InstacodeParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(InstacodeParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(InstacodeParser.RBRACK, i);
		}
		public TypeTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeType(this);
		}
	}

	public final TypeTypeContext typeType() throws RecognitionException {
		TypeTypeContext _localctx = new TypeTypeContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_typeType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(530);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(527);
					annotation();
					}
					} 
				}
				setState(532);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			}
			setState(535);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
			case MODULE:
			case OPEN:
			case REQUIRES:
			case EXPORTS:
			case OPENS:
			case TO:
			case USES:
			case PROVIDES:
			case WITH:
			case TRANSITIVE:
			case YIELD:
			case SEALED:
			case PERMITS:
			case RECORD:
			case VAR:
				{
				setState(533);
				classOrInterfaceType();
				}
				break;
			case BOOLEAN:
			case CHAR:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				{
				setState(534);
				primitiveType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(547);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,64,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(540);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (((((_la - 47)) & ~0x3f) == 0 && ((1L << (_la - 47)) & 134213665L) != 0)) {
						{
						{
						setState(537);
						annotation();
						}
						}
						setState(542);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(543);
					match(LBRACK);
					setState(544);
					match(RBRACK);
					}
					} 
				}
				setState(549);
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
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveTypeContext extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(InstacodeParser.BOOLEAN, 0); }
		public TerminalNode CHAR() { return getToken(InstacodeParser.CHAR, 0); }
		public TerminalNode BYTE() { return getToken(InstacodeParser.BYTE, 0); }
		public TerminalNode SHORT() { return getToken(InstacodeParser.SHORT, 0); }
		public TerminalNode INT() { return getToken(InstacodeParser.INT, 0); }
		public TerminalNode LONG() { return getToken(InstacodeParser.LONG, 0); }
		public TerminalNode FLOAT() { return getToken(InstacodeParser.FLOAT, 0); }
		public TerminalNode DOUBLE() { return getToken(InstacodeParser.DOUBLE, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterPrimitiveType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitPrimitiveType(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(550);
			_la = _input.LA(1);
			if ( !(((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & 255L) != 0)) ) {
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
	public static class NonWildcardTypeArgumentsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(InstacodeParser.LT, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode GT() { return getToken(InstacodeParser.GT, 0); }
		public NonWildcardTypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterNonWildcardTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitNonWildcardTypeArguments(this);
		}
	}

	public final NonWildcardTypeArgumentsContext nonWildcardTypeArguments() throws RecognitionException {
		NonWildcardTypeArgumentsContext _localctx = new NonWildcardTypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(552);
			match(LT);
			setState(553);
			typeList();
			setState(554);
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
	public static class TypeListContext extends ParserRuleContext {
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(InstacodeParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(InstacodeParser.COMMA, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).enterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof InstacodeParserListener) ((InstacodeParserListener)listener).exitTypeList(this);
		}
	}

	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(556);
			typeType();
			setState(561);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(557);
				match(COMMA);
				setState(558);
				typeType();
				}
				}
				setState(563);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 12);
		case 1:
			return precpred(_ctx, 11);
		case 2:
			return precpred(_ctx, 10);
		case 3:
			return precpred(_ctx, 9);
		case 4:
			return precpred(_ctx, 7);
		case 5:
			return precpred(_ctx, 6);
		case 6:
			return precpred(_ctx, 5);
		case 7:
			return precpred(_ctx, 4);
		case 8:
			return precpred(_ctx, 3);
		case 9:
			return precpred(_ctx, 2);
		case 10:
			return precpred(_ctx, 1);
		case 11:
			return precpred(_ctx, 23);
		case 12:
			return precpred(_ctx, 22);
		case 13:
			return precpred(_ctx, 19);
		case 14:
			return precpred(_ctx, 15);
		case 15:
			return precpred(_ctx, 8);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001S\u0235\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"(\u0007(\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0003\u0000c\b"+
		"\u0000\u0003\u0000e\b\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001t\b\u0001\n\u0001"+
		"\f\u0001w\t\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001|\b\u0001"+
		"\n\u0001\f\u0001\u007f\t\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u0088\b\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0003\u0001\u0098\b\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00bd\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00c3\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0003\u0001\u00d3\b\u0001\u0005\u0001\u00d5\b\u0001\n\u0001\f\u0001"+
		"\u00d8\t\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0003\u0003\u00e3\b\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0005\u0004\u00e8\b\u0004\n\u0004"+
		"\f\u0004\u00eb\t\u0004\u0001\u0004\u0001\u0004\u0005\u0004\u00ef\b\u0004"+
		"\n\u0004\f\u0004\u00f2\t\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0003\u0005\u00f8\b\u0005\u0001\u0006\u0001\u0006\u0003\u0006\u00fc"+
		"\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0107\b\u0007\u0003"+
		"\u0007\u0109\b\u0007\u0001\b\u0001\b\u0001\b\u0003\b\u010e\b\b\u0001\t"+
		"\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u0119\b\u000b\n\u000b\f\u000b\u011c\t\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u0125\b\u000b\n\u000b\f\u000b\u0128\t\u000b\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u012c\b\u000b\n\u000b\f\u000b\u012f\t\u000b\u0003\u000b"+
		"\u0131\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u0137\b\f\n\f\f"+
		"\f\u013a\t\f\u0001\f\u0003\f\u013d\b\f\u0003\f\u013f\b\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0003\r\u0145\b\r\u0001\u000e\u0001\u000e\u0003\u000e"+
		"\u0149\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u014e\b"+
		"\u000e\u0005\u000e\u0150\b\u000e\n\u000e\f\u000e\u0153\t\u000e\u0001\u000e"+
		"\u0003\u000e\u0156\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f"+
		"\u015b\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0160\b"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003"+
		"\u0010\u0167\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003"+
		"\u0010\u016d\b\u0010\u0001\u0010\u0003\u0010\u0170\b\u0010\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0005\u0011\u0175\b\u0011\n\u0011\f\u0011\u0178"+
		"\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003"+
		"\u0012\u017f\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0184"+
		"\b\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0188\b\u0013\u0003\u0013"+
		"\u018a\b\u0013\u0001\u0014\u0001\u0014\u0003\u0014\u018e\b\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005"+
		"\u0015\u0196\b\u0015\n\u0015\f\u0015\u0199\t\u0015\u0003\u0015\u019b\b"+
		"\u0015\u0001\u0015\u0003\u0015\u019e\b\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0003\u0016\u01a9\b\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001b\u0001"+
		"\u001b\u0003\u001b\u01b5\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0005"+
		"\u001c\u01ba\b\u001c\n\u001c\f\u001c\u01bd\t\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e"+
		"\u01c6\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u01cb\b"+
		"\u001f\n\u001f\f\u001f\u01ce\t\u001f\u0001 \u0001 \u0001 \u0005 \u01d3"+
		"\b \n \f \u01d6\t \u0001 \u0001 \u0001 \u0001!\u0001!\u0001!\u0003!\u01de"+
		"\b!\u0001!\u0001!\u0001!\u0003!\u01e3\b!\u0001!\u0003!\u01e6\b!\u0001"+
		"\"\u0001\"\u0005\"\u01ea\b\"\n\"\f\"\u01ed\t\"\u0001\"\u0001\"\u0001\""+
		"\u0003\"\u01f2\b\"\u0003\"\u01f4\b\"\u0001#\u0001#\u0001#\u0001#\u0005"+
		"#\u01fa\b#\n#\f#\u01fd\t#\u0001#\u0001#\u0001$\u0001$\u0003$\u0203\b$"+
		"\u0001$\u0001$\u0005$\u0207\b$\n$\f$\u020a\t$\u0001$\u0001$\u0003$\u020e"+
		"\b$\u0001%\u0005%\u0211\b%\n%\f%\u0214\t%\u0001%\u0001%\u0003%\u0218\b"+
		"%\u0001%\u0005%\u021b\b%\n%\f%\u021e\t%\u0001%\u0001%\u0005%\u0222\b%"+
		"\n%\f%\u0225\t%\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001\'\u0001(\u0001"+
		"(\u0001(\u0005(\u0230\b(\n(\f(\u0233\t(\u0001(\u0000\u0001\u0002)\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02468:<>@BDFHJLNP\u0000\r\u0001\u0000#&\u0001\u0000\u001a"+
		"\u001b\u0002\u0000\'(,,\u0001\u0000%&\u0002\u0000\u0018\u0019\u001e\u001f"+
		"\u0002\u0000\u0017\u0017  \u0001\u0000#$\u0001\u0000\u0003\u0006\u0001"+
		"\u0000\u0007\b\u0002\u000044;I\u0003\u000044;DFH\u0002\u000066KK\u0001"+
		"\u0000LS\u0274\u0000d\u0001\u0000\u0000\u0000\u0002\u0087\u0001\u0000"+
		"\u0000\u0000\u0004\u00d9\u0001\u0000\u0000\u0000\u0006\u00e0\u0001\u0000"+
		"\u0000\u0000\b\u00e9\u0001\u0000\u0000\u0000\n\u00f7\u0001\u0000\u0000"+
		"\u0000\f\u00f9\u0001\u0000\u0000\u0000\u000e\u0108\u0001\u0000\u0000\u0000"+
		"\u0010\u010d\u0001\u0000\u0000\u0000\u0012\u010f\u0001\u0000\u0000\u0000"+
		"\u0014\u0112\u0001\u0000\u0000\u0000\u0016\u0114\u0001\u0000\u0000\u0000"+
		"\u0018\u0132\u0001\u0000\u0000\u0000\u001a\u0144\u0001\u0000\u0000\u0000"+
		"\u001c\u0155\u0001\u0000\u0000\u0000\u001e\u015a\u0001\u0000\u0000\u0000"+
		" \u016f\u0001\u0000\u0000\u0000\"\u0171\u0001\u0000\u0000\u0000$\u017e"+
		"\u0001\u0000\u0000\u0000&\u0189\u0001\u0000\u0000\u0000(\u018b\u0001\u0000"+
		"\u0000\u0000*\u0191\u0001\u0000\u0000\u0000,\u01a8\u0001\u0000\u0000\u0000"+
		".\u01aa\u0001\u0000\u0000\u00000\u01ac\u0001\u0000\u0000\u00002\u01ae"+
		"\u0001\u0000\u0000\u00004\u01b0\u0001\u0000\u0000\u00006\u01b4\u0001\u0000"+
		"\u0000\u00008\u01b6\u0001\u0000\u0000\u0000:\u01be\u0001\u0000\u0000\u0000"+
		"<\u01c5\u0001\u0000\u0000\u0000>\u01c7\u0001\u0000\u0000\u0000@\u01d4"+
		"\u0001\u0000\u0000\u0000B\u01dd\u0001\u0000\u0000\u0000D\u01f3\u0001\u0000"+
		"\u0000\u0000F\u01f5\u0001\u0000\u0000\u0000H\u0208\u0001\u0000\u0000\u0000"+
		"J\u0212\u0001\u0000\u0000\u0000L\u0226\u0001\u0000\u0000\u0000N\u0228"+
		"\u0001\u0000\u0000\u0000P\u022c\u0001\u0000\u0000\u0000RS\u0005\u000e"+
		"\u0000\u0000ST\u0003\u0002\u0001\u0000TU\u0005\u000f\u0000\u0000Ue\u0001"+
		"\u0000\u0000\u0000Ve\u00055\u0000\u0000We\u00056\u0000\u0000Xe\u0003,"+
		"\u0016\u0000Ye\u00032\u0019\u0000Z[\u00036\u001b\u0000[\\\u0005\u0016"+
		"\u0000\u0000\\]\u00057\u0000\u0000]e\u0001\u0000\u0000\u0000^b\u0003N"+
		"\'\u0000_c\u0003$\u0012\u0000`a\u00055\u0000\u0000ac\u0003(\u0014\u0000"+
		"b_\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000ce\u0001\u0000\u0000"+
		"\u0000dR\u0001\u0000\u0000\u0000dV\u0001\u0000\u0000\u0000dW\u0001\u0000"+
		"\u0000\u0000dX\u0001\u0000\u0000\u0000dY\u0001\u0000\u0000\u0000dZ\u0001"+
		"\u0000\u0000\u0000d^\u0001\u0000\u0000\u0000e\u0001\u0001\u0000\u0000"+
		"\u0000fg\u0006\u0001\uffff\uffff\u0000g\u0088\u0003\u0000\u0000\u0000"+
		"hi\u0003J%\u0000ij\u0005\u0016\u0000\u0000jk\u00032\u0019\u0000k\u0088"+
		"\u0001\u0000\u0000\u0000l\u0088\u0003\u0006\u0003\u0000m\u0088\u0003\u0004"+
		"\u0002\u0000n\u0088\u0003 \u0010\u0000op\u00058\u0000\u0000p\u0088\u0003"+
		"\u000e\u0007\u0000qu\u0005\u000e\u0000\u0000rt\u0003B!\u0000sr\u0001\u0000"+
		"\u0000\u0000tw\u0001\u0000\u0000\u0000us\u0001\u0000\u0000\u0000uv\u0001"+
		"\u0000\u0000\u0000vx\u0001\u0000\u0000\u0000wu\u0001\u0000\u0000\u0000"+
		"x}\u0003J%\u0000yz\u0005)\u0000\u0000z|\u0003J%\u0000{y\u0001\u0000\u0000"+
		"\u0000|\u007f\u0001\u0000\u0000\u0000}{\u0001\u0000\u0000\u0000}~\u0001"+
		"\u0000\u0000\u0000~\u0080\u0001\u0000\u0000\u0000\u007f}\u0001\u0000\u0000"+
		"\u0000\u0080\u0081\u0005\u000f\u0000\u0000\u0081\u0082\u0003\u0002\u0001"+
		"\u0010\u0082\u0088\u0001\u0000\u0000\u0000\u0083\u0084\u0007\u0000\u0000"+
		"\u0000\u0084\u0088\u0003\u0002\u0001\u000e\u0085\u0086\u0007\u0001\u0000"+
		"\u0000\u0086\u0088\u0003\u0002\u0001\r\u0087f\u0001\u0000\u0000\u0000"+
		"\u0087h\u0001\u0000\u0000\u0000\u0087l\u0001\u0000\u0000\u0000\u0087m"+
		"\u0001\u0000\u0000\u0000\u0087n\u0001\u0000\u0000\u0000\u0087o\u0001\u0000"+
		"\u0000\u0000\u0087q\u0001\u0000\u0000\u0000\u0087\u0083\u0001\u0000\u0000"+
		"\u0000\u0087\u0085\u0001\u0000\u0000\u0000\u0088\u00d6\u0001\u0000\u0000"+
		"\u0000\u0089\u008a\n\f\u0000\u0000\u008a\u008b\u0007\u0002\u0000\u0000"+
		"\u008b\u00d5\u0003\u0002\u0001\r\u008c\u008d\n\u000b\u0000\u0000\u008d"+
		"\u008e\u0007\u0003\u0000\u0000\u008e\u00d5\u0003\u0002\u0001\f\u008f\u0097"+
		"\n\n\u0000\u0000\u0090\u0091\u0005\u0019\u0000\u0000\u0091\u0098\u0005"+
		"\u0019\u0000\u0000\u0092\u0093\u0005\u0018\u0000\u0000\u0093\u0094\u0005"+
		"\u0018\u0000\u0000\u0094\u0098\u0005\u0018\u0000\u0000\u0095\u0096\u0005"+
		"\u0018\u0000\u0000\u0096\u0098\u0005\u0018\u0000\u0000\u0097\u0090\u0001"+
		"\u0000\u0000\u0000\u0097\u0092\u0001\u0000\u0000\u0000\u0097\u0095\u0001"+
		"\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u00d5\u0003"+
		"\u0002\u0001\u000b\u009a\u009b\n\t\u0000\u0000\u009b\u009c\u0007\u0004"+
		"\u0000\u0000\u009c\u00d5\u0003\u0002\u0001\n\u009d\u009e\n\u0007\u0000"+
		"\u0000\u009e\u009f\u0007\u0005\u0000\u0000\u009f\u00d5\u0003\u0002\u0001"+
		"\b\u00a0\u00a1\n\u0006\u0000\u0000\u00a1\u00a2\u0005)\u0000\u0000\u00a2"+
		"\u00d5\u0003\u0002\u0001\u0007\u00a3\u00a4\n\u0005\u0000\u0000\u00a4\u00a5"+
		"\u0005+\u0000\u0000\u00a5\u00d5\u0003\u0002\u0001\u0006\u00a6\u00a7\n"+
		"\u0004\u0000\u0000\u00a7\u00a8\u0005*\u0000\u0000\u00a8\u00d5\u0003\u0002"+
		"\u0001\u0005\u00a9\u00aa\n\u0003\u0000\u0000\u00aa\u00ab\u0005!\u0000"+
		"\u0000\u00ab\u00d5\u0003\u0002\u0001\u0004\u00ac\u00ad\n\u0002\u0000\u0000"+
		"\u00ad\u00ae\u0005\"\u0000\u0000\u00ae\u00d5\u0003\u0002\u0001\u0003\u00af"+
		"\u00b0\n\u0001\u0000\u0000\u00b0\u00b1\u0005\u001c\u0000\u0000\u00b1\u00b2"+
		"\u0003\u0002\u0001\u0000\u00b2\u00b3\u0005\u001d\u0000\u0000\u00b3\u00b4"+
		"\u0003\u0002\u0001\u0001\u00b4\u00d5\u0001\u0000\u0000\u0000\u00b5\u00b6"+
		"\n\u0017\u0000\u0000\u00b6\u00c2\u0005\u0016\u0000\u0000\u00b7\u00c3\u0003"+
		"2\u0019\u0000\u00b8\u00c3\u0003 \u0010\u0000\u00b9\u00c3\u00055\u0000"+
		"\u0000\u00ba\u00bc\u00058\u0000\u0000\u00bb\u00bd\u0003N\'\u0000\u00bc"+
		"\u00bb\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd"+
		"\u00be\u0001\u0000\u0000\u0000\u00be\u00c3\u0003\f\u0006\u0000\u00bf\u00c0"+
		"\u00056\u0000\u0000\u00c0\u00c3\u0003&\u0013\u0000\u00c1\u00c3\u0003\u0012"+
		"\t\u0000\u00c2\u00b7\u0001\u0000\u0000\u0000\u00c2\u00b8\u0001\u0000\u0000"+
		"\u0000\u00c2\u00b9\u0001\u0000\u0000\u0000\u00c2\u00ba\u0001\u0000\u0000"+
		"\u0000\u00c2\u00bf\u0001\u0000\u0000\u0000\u00c2\u00c1\u0001\u0000\u0000"+
		"\u0000\u00c3\u00d5\u0001\u0000\u0000\u0000\u00c4\u00c5\n\u0016\u0000\u0000"+
		"\u00c5\u00c6\u0005\u0012\u0000\u0000\u00c6\u00c7\u0003\u0002\u0001\u0000"+
		"\u00c7\u00c8\u0005\u0013\u0000\u0000\u00c8\u00d5\u0001\u0000\u0000\u0000"+
		"\u00c9\u00ca\n\u0013\u0000\u0000\u00ca\u00cb\u0005\u0002\u0000\u0000\u00cb"+
		"\u00d5\u00054\u0000\u0000\u00cc\u00cd\n\u000f\u0000\u0000\u00cd\u00d5"+
		"\u0007\u0006\u0000\u0000\u00ce\u00cf\n\b\u0000\u0000\u00cf\u00d2\u0005"+
		"9\u0000\u0000\u00d0\u00d3\u0003J%\u0000\u00d1\u00d3\u0003\b\u0004\u0000"+
		"\u00d2\u00d0\u0001\u0000\u0000\u0000\u00d2\u00d1\u0001\u0000\u0000\u0000"+
		"\u00d3\u00d5\u0001\u0000\u0000\u0000\u00d4\u0089\u0001\u0000\u0000\u0000"+
		"\u00d4\u008c\u0001\u0000\u0000\u0000\u00d4\u008f\u0001\u0000\u0000\u0000"+
		"\u00d4\u009a\u0001\u0000\u0000\u0000\u00d4\u009d\u0001\u0000\u0000\u0000"+
		"\u00d4\u00a0\u0001\u0000\u0000\u0000\u00d4\u00a3\u0001\u0000\u0000\u0000"+
		"\u00d4\u00a6\u0001\u0000\u0000\u0000\u00d4\u00a9\u0001\u0000\u0000\u0000"+
		"\u00d4\u00ac\u0001\u0000\u0000\u0000\u00d4\u00af\u0001\u0000\u0000\u0000"+
		"\u00d4\u00b5\u0001\u0000\u0000\u0000\u00d4\u00c4\u0001\u0000\u0000\u0000"+
		"\u00d4\u00c9\u0001\u0000\u0000\u0000\u00d4\u00cc\u0001\u0000\u0000\u0000"+
		"\u00d4\u00ce\u0001\u0000\u0000\u0000\u00d5\u00d8\u0001\u0000\u0000\u0000"+
		"\u00d6\u00d4\u0001\u0000\u0000\u0000\u00d6\u00d7\u0001\u0000\u0000\u0000"+
		"\u00d7\u0003\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000\u0000\u0000"+
		"\u00d9\u00da\u0005\u0001\u0000\u0000\u00da\u00db\u0005\u000e\u0000\u0000"+
		"\u00db\u00dc\u0003\u0002\u0001\u0000\u00dc\u00dd\u0005\u0015\u0000\u0000"+
		"\u00dd\u00de\u0003\u0002\u0001\u0000\u00de\u00df\u0005\u000f\u0000\u0000"+
		"\u00df\u0005\u0001\u0000\u0000\u0000\u00e0\u00e2\u0005\u0012\u0000\u0000"+
		"\u00e1\u00e3\u0003\"\u0011\u0000\u00e2\u00e1\u0001\u0000\u0000\u0000\u00e2"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000\u0000\u00e4"+
		"\u00e5\u0005\u0013\u0000\u0000\u00e5\u0007\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e8\u0003\n\u0005\u0000\u00e7\u00e6\u0001\u0000\u0000\u0000\u00e8\u00eb"+
		"\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00e9\u00ea"+
		"\u0001\u0000\u0000\u0000\u00ea\u00ec\u0001\u0000\u0000\u0000\u00eb\u00e9"+
		"\u0001\u0000\u0000\u0000\u00ec\u00f0\u0003J%\u0000\u00ed\u00ef\u0003B"+
		"!\u0000\u00ee\u00ed\u0001\u0000\u0000\u0000\u00ef\u00f2\u0001\u0000\u0000"+
		"\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f1\u00f3\u0001\u0000\u0000\u0000\u00f2\u00f0\u0001\u0000\u0000"+
		"\u0000\u00f3\u00f4\u00032\u0019\u0000\u00f4\t\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f8\u0005:\u0000\u0000\u00f6\u00f8\u0003B!\u0000\u00f7\u00f5"+
		"\u0001\u0000\u0000\u0000\u00f7\u00f6\u0001\u0000\u0000\u0000\u00f8\u000b"+
		"\u0001\u0000\u0000\u0000\u00f9\u00fb\u00032\u0019\u0000\u00fa\u00fc\u0003"+
		"\u0010\b\u0000\u00fb\u00fa\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001\u0000"+
		"\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd\u00fe\u0003\u0014"+
		"\n\u0000\u00fe\r\u0001\u0000\u0000\u0000\u00ff\u0100\u0003N\'\u0000\u0100"+
		"\u0101\u0003\u001c\u000e\u0000\u0101\u0102\u0003\u0014\n\u0000\u0102\u0109"+
		"\u0001\u0000\u0000\u0000\u0103\u0106\u0003\u001c\u000e\u0000\u0104\u0107"+
		"\u0003\u0016\u000b\u0000\u0105\u0107\u0003\u0014\n\u0000\u0106\u0104\u0001"+
		"\u0000\u0000\u0000\u0106\u0105\u0001\u0000\u0000\u0000\u0107\u0109\u0001"+
		"\u0000\u0000\u0000\u0108\u00ff\u0001\u0000\u0000\u0000\u0108\u0103\u0001"+
		"\u0000\u0000\u0000\u0109\u000f\u0001\u0000\u0000\u0000\u010a\u010b\u0005"+
		"\u0019\u0000\u0000\u010b\u010e\u0005\u0018\u0000\u0000\u010c\u010e\u0003"+
		"N\'\u0000\u010d\u010a\u0001\u0000\u0000\u0000\u010d\u010c\u0001\u0000"+
		"\u0000\u0000\u010e\u0011\u0001\u0000\u0000\u0000\u010f\u0110\u0003N\'"+
		"\u0000\u0110\u0111\u0003$\u0012\u0000\u0111\u0013\u0001\u0000\u0000\u0000"+
		"\u0112\u0113\u0003(\u0014\u0000\u0113\u0015\u0001\u0000\u0000\u0000\u0114"+
		"\u0130\u0005\u0012\u0000\u0000\u0115\u011a\u0005\u0013\u0000\u0000\u0116"+
		"\u0117\u0005\u0012\u0000\u0000\u0117\u0119\u0005\u0013\u0000\u0000\u0118"+
		"\u0116\u0001\u0000\u0000\u0000\u0119\u011c\u0001\u0000\u0000\u0000\u011a"+
		"\u0118\u0001\u0000\u0000\u0000\u011a\u011b\u0001\u0000\u0000\u0000\u011b"+
		"\u011d\u0001\u0000\u0000\u0000\u011c\u011a\u0001\u0000\u0000\u0000\u011d"+
		"\u0131\u0003\u0018\f\u0000\u011e\u011f\u0003\u0002\u0001\u0000\u011f\u0126"+
		"\u0005\u0013\u0000\u0000\u0120\u0121\u0005\u0012\u0000\u0000\u0121\u0122"+
		"\u0003\u0002\u0001\u0000\u0122\u0123\u0005\u0013\u0000\u0000\u0123\u0125"+
		"\u0001\u0000\u0000\u0000\u0124\u0120\u0001\u0000\u0000\u0000\u0125\u0128"+
		"\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000\u0126\u0127"+
		"\u0001\u0000\u0000\u0000\u0127\u012d\u0001\u0000\u0000\u0000\u0128\u0126"+
		"\u0001\u0000\u0000\u0000\u0129\u012a\u0005\u0012\u0000\u0000\u012a\u012c"+
		"\u0005\u0013\u0000\u0000\u012b\u0129\u0001\u0000\u0000\u0000\u012c\u012f"+
		"\u0001\u0000\u0000\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012d\u012e"+
		"\u0001\u0000\u0000\u0000\u012e\u0131\u0001\u0000\u0000\u0000\u012f\u012d"+
		"\u0001\u0000\u0000\u0000\u0130\u0115\u0001\u0000\u0000\u0000\u0130\u011e"+
		"\u0001\u0000\u0000\u0000\u0131\u0017\u0001\u0000\u0000\u0000\u0132\u013e"+
		"\u0005\u0010\u0000\u0000\u0133\u0138\u0003\u001a\r\u0000\u0134\u0135\u0005"+
		"\u0015\u0000\u0000\u0135\u0137\u0003\u001a\r\u0000\u0136\u0134\u0001\u0000"+
		"\u0000\u0000\u0137\u013a\u0001\u0000\u0000\u0000\u0138\u0136\u0001\u0000"+
		"\u0000\u0000\u0138\u0139\u0001\u0000\u0000\u0000\u0139\u013c\u0001\u0000"+
		"\u0000\u0000\u013a\u0138\u0001\u0000\u0000\u0000\u013b\u013d\u0005\u0015"+
		"\u0000\u0000\u013c\u013b\u0001\u0000\u0000\u0000\u013c\u013d\u0001\u0000"+
		"\u0000\u0000\u013d\u013f\u0001\u0000\u0000\u0000\u013e\u0133\u0001\u0000"+
		"\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0140\u0001\u0000"+
		"\u0000\u0000\u0140\u0141\u0005\u0011\u0000\u0000\u0141\u0019\u0001\u0000"+
		"\u0000\u0000\u0142\u0145\u0003\u0018\f\u0000\u0143\u0145\u0003\u0002\u0001"+
		"\u0000\u0144\u0142\u0001\u0000\u0000\u0000\u0144\u0143\u0001\u0000\u0000"+
		"\u0000\u0145\u001b\u0001\u0000\u0000\u0000\u0146\u0148\u00032\u0019\u0000"+
		"\u0147\u0149\u0003\u001e\u000f\u0000\u0148\u0147\u0001\u0000\u0000\u0000"+
		"\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u0151\u0001\u0000\u0000\u0000"+
		"\u014a\u014b\u0005\u0016\u0000\u0000\u014b\u014d\u00032\u0019\u0000\u014c"+
		"\u014e\u0003\u001e\u000f\u0000\u014d\u014c\u0001\u0000\u0000\u0000\u014d"+
		"\u014e\u0001\u0000\u0000\u0000\u014e\u0150\u0001\u0000\u0000\u0000\u014f"+
		"\u014a\u0001\u0000\u0000\u0000\u0150\u0153\u0001\u0000\u0000\u0000\u0151"+
		"\u014f\u0001\u0000\u0000\u0000\u0151\u0152\u0001\u0000\u0000\u0000\u0152"+
		"\u0156\u0001\u0000\u0000\u0000\u0153\u0151\u0001\u0000\u0000\u0000\u0154"+
		"\u0156\u0003L&\u0000\u0155\u0146\u0001\u0000\u0000\u0000\u0155\u0154\u0001"+
		"\u0000\u0000\u0000\u0156\u001d\u0001\u0000\u0000\u0000\u0157\u0158\u0005"+
		"\u0019\u0000\u0000\u0158\u015b\u0005\u0018\u0000\u0000\u0159\u015b\u0003"+
		"F#\u0000\u015a\u0157\u0001\u0000\u0000\u0000\u015a\u0159\u0001\u0000\u0000"+
		"\u0000\u015b\u001f\u0001\u0000\u0000\u0000\u015c\u015d\u00032\u0019\u0000"+
		"\u015d\u015f\u0005\u000e\u0000\u0000\u015e\u0160\u0003\"\u0011\u0000\u015f"+
		"\u015e\u0001\u0000\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000\u0160"+
		"\u0161\u0001\u0000\u0000\u0000\u0161\u0162\u0005\u000f\u0000\u0000\u0162"+
		"\u0170\u0001\u0000\u0000\u0000\u0163\u0164\u00055\u0000\u0000\u0164\u0166"+
		"\u0005\u000e\u0000\u0000\u0165\u0167\u0003\"\u0011\u0000\u0166\u0165\u0001"+
		"\u0000\u0000\u0000\u0166\u0167\u0001\u0000\u0000\u0000\u0167\u0168\u0001"+
		"\u0000\u0000\u0000\u0168\u0170\u0005\u000f\u0000\u0000\u0169\u016a\u0005"+
		"6\u0000\u0000\u016a\u016c\u0005\u000e\u0000\u0000\u016b\u016d\u0003\""+
		"\u0011\u0000\u016c\u016b\u0001\u0000\u0000\u0000\u016c\u016d\u0001\u0000"+
		"\u0000\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016e\u0170\u0005\u000f"+
		"\u0000\u0000\u016f\u015c\u0001\u0000\u0000\u0000\u016f\u0163\u0001\u0000"+
		"\u0000\u0000\u016f\u0169\u0001\u0000\u0000\u0000\u0170!\u0001\u0000\u0000"+
		"\u0000\u0171\u0176\u0003\u0002\u0001\u0000\u0172\u0173\u0005\u0015\u0000"+
		"\u0000\u0173\u0175\u0003\u0002\u0001\u0000\u0174\u0172\u0001\u0000\u0000"+
		"\u0000\u0175\u0178\u0001\u0000\u0000\u0000\u0176\u0174\u0001\u0000\u0000"+
		"\u0000\u0176\u0177\u0001\u0000\u0000\u0000\u0177#\u0001\u0000\u0000\u0000"+
		"\u0178\u0176\u0001\u0000\u0000\u0000\u0179\u017a\u00056\u0000\u0000\u017a"+
		"\u017f\u0003&\u0013\u0000\u017b\u017c\u00032\u0019\u0000\u017c\u017d\u0003"+
		"(\u0014\u0000\u017d\u017f\u0001\u0000\u0000\u0000\u017e\u0179\u0001\u0000"+
		"\u0000\u0000\u017e\u017b\u0001\u0000\u0000\u0000\u017f%\u0001\u0000\u0000"+
		"\u0000\u0180\u018a\u0003(\u0014\u0000\u0181\u0183\u0005\u0016\u0000\u0000"+
		"\u0182\u0184\u0003F#\u0000\u0183\u0182\u0001\u0000\u0000\u0000\u0183\u0184"+
		"\u0001\u0000\u0000\u0000\u0184\u0185\u0001\u0000\u0000\u0000\u0185\u0187"+
		"\u00032\u0019\u0000\u0186\u0188\u0003(\u0014\u0000\u0187\u0186\u0001\u0000"+
		"\u0000\u0000\u0187\u0188\u0001\u0000\u0000\u0000\u0188\u018a\u0001\u0000"+
		"\u0000\u0000\u0189\u0180\u0001\u0000\u0000\u0000\u0189\u0181\u0001\u0000"+
		"\u0000\u0000\u018a\'\u0001\u0000\u0000\u0000\u018b\u018d\u0005\u000e\u0000"+
		"\u0000\u018c\u018e\u0003\"\u0011\u0000\u018d\u018c\u0001\u0000\u0000\u0000"+
		"\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000\u0000"+
		"\u018f\u0190\u0005\u000f\u0000\u0000\u0190)\u0001\u0000\u0000\u0000\u0191"+
		"\u019a\u0005\u0010\u0000\u0000\u0192\u0197\u0003<\u001e\u0000\u0193\u0194"+
		"\u0005\u0015\u0000\u0000\u0194\u0196\u0003<\u001e\u0000\u0195\u0193\u0001"+
		"\u0000\u0000\u0000\u0196\u0199\u0001\u0000\u0000\u0000\u0197\u0195\u0001"+
		"\u0000\u0000\u0000\u0197\u0198\u0001\u0000\u0000\u0000\u0198\u019b\u0001"+
		"\u0000\u0000\u0000\u0199\u0197\u0001\u0000\u0000\u0000\u019a\u0192\u0001"+
		"\u0000\u0000\u0000\u019a\u019b\u0001\u0000\u0000\u0000\u019b\u019d\u0001"+
		"\u0000\u0000\u0000\u019c\u019e\u0005\u0015\u0000\u0000\u019d\u019c\u0001"+
		"\u0000\u0000\u0000\u019d\u019e\u0001\u0000\u0000\u0000\u019e\u019f\u0001"+
		"\u0000\u0000\u0000\u019f\u01a0\u0005\u0011\u0000\u0000\u01a0+\u0001\u0000"+
		"\u0000\u0000\u01a1\u01a9\u0003.\u0017\u0000\u01a2\u01a9\u00030\u0018\u0000"+
		"\u01a3\u01a9\u0005\n\u0000\u0000\u01a4\u01a9\u0005\u000b\u0000\u0000\u01a5"+
		"\u01a9\u0005\t\u0000\u0000\u01a6\u01a9\u0005\r\u0000\u0000\u01a7\u01a9"+
		"\u0005\f\u0000\u0000\u01a8\u01a1\u0001\u0000\u0000\u0000\u01a8\u01a2\u0001"+
		"\u0000\u0000\u0000\u01a8\u01a3\u0001\u0000\u0000\u0000\u01a8\u01a4\u0001"+
		"\u0000\u0000\u0000\u01a8\u01a5\u0001\u0000\u0000\u0000\u01a8\u01a6\u0001"+
		"\u0000\u0000\u0000\u01a8\u01a7\u0001\u0000\u0000\u0000\u01a9-\u0001\u0000"+
		"\u0000\u0000\u01aa\u01ab\u0007\u0007\u0000\u0000\u01ab/\u0001\u0000\u0000"+
		"\u0000\u01ac\u01ad\u0007\b\u0000\u0000\u01ad1\u0001\u0000\u0000\u0000"+
		"\u01ae\u01af\u0007\t\u0000\u0000\u01af3\u0001\u0000\u0000\u0000\u01b0"+
		"\u01b1\u0007\n\u0000\u0000\u01b15\u0001\u0000\u0000\u0000\u01b2\u01b5"+
		"\u0003J%\u0000\u01b3\u01b5\u0005J\u0000\u0000\u01b4\u01b2\u0001\u0000"+
		"\u0000\u0000\u01b4\u01b3\u0001\u0000\u0000\u0000\u01b57\u0001\u0000\u0000"+
		"\u0000\u01b6\u01bb\u0003:\u001d\u0000\u01b7\u01b8\u0005\u0015\u0000\u0000"+
		"\u01b8\u01ba\u0003:\u001d\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01ba"+
		"\u01bd\u0001\u0000\u0000\u0000\u01bb\u01b9\u0001\u0000\u0000\u0000\u01bb"+
		"\u01bc\u0001\u0000\u0000\u0000\u01bc9\u0001\u0000\u0000\u0000\u01bd\u01bb"+
		"\u0001\u0000\u0000\u0000\u01be\u01bf\u00032\u0019\u0000\u01bf\u01c0\u0005"+
		"\u0017\u0000\u0000\u01c0\u01c1\u0003<\u001e\u0000\u01c1;\u0001\u0000\u0000"+
		"\u0000\u01c2\u01c6\u0003\u0002\u0001\u0000\u01c3\u01c6\u0003B!\u0000\u01c4"+
		"\u01c6\u0003*\u0015\u0000\u01c5\u01c2\u0001\u0000\u0000\u0000\u01c5\u01c3"+
		"\u0001\u0000\u0000\u0000\u01c5\u01c4\u0001\u0000\u0000\u0000\u01c6=\u0001"+
		"\u0000\u0000\u0000\u01c7\u01cc\u00032\u0019\u0000\u01c8\u01c9\u0005\u0016"+
		"\u0000\u0000\u01c9\u01cb\u00032\u0019\u0000\u01ca\u01c8\u0001\u0000\u0000"+
		"\u0000\u01cb\u01ce\u0001\u0000\u0000\u0000\u01cc\u01ca\u0001\u0000\u0000"+
		"\u0000\u01cc\u01cd\u0001\u0000\u0000\u0000\u01cd?\u0001\u0000\u0000\u0000"+
		"\u01ce\u01cc\u0001\u0000\u0000\u0000\u01cf\u01d0\u00032\u0019\u0000\u01d0"+
		"\u01d1\u0005\u0016\u0000\u0000\u01d1\u01d3\u0001\u0000\u0000\u0000\u01d2"+
		"\u01cf\u0001\u0000\u0000\u0000\u01d3\u01d6\u0001\u0000\u0000\u0000\u01d4"+
		"\u01d2\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001\u0000\u0000\u0000\u01d5"+
		"\u01d7\u0001\u0000\u0000\u0000\u01d6\u01d4\u0001\u0000\u0000\u0000\u01d7"+
		"\u01d8\u0005/\u0000\u0000\u01d8\u01d9\u00032\u0019\u0000\u01d9A\u0001"+
		"\u0000\u0000\u0000\u01da\u01db\u0005/\u0000\u0000\u01db\u01de\u0003>\u001f"+
		"\u0000\u01dc\u01de\u0003@ \u0000\u01dd\u01da\u0001\u0000\u0000\u0000\u01dd"+
		"\u01dc\u0001\u0000\u0000\u0000\u01de\u01e5\u0001\u0000\u0000\u0000\u01df"+
		"\u01e2\u0005\u000e\u0000\u0000\u01e0\u01e3\u00038\u001c\u0000\u01e1\u01e3"+
		"\u0003<\u001e\u0000\u01e2\u01e0\u0001\u0000\u0000\u0000\u01e2\u01e1\u0001"+
		"\u0000\u0000\u0000\u01e2\u01e3\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001"+
		"\u0000\u0000\u0000\u01e4\u01e6\u0005\u000f\u0000\u0000\u01e5\u01df\u0001"+
		"\u0000\u0000\u0000\u01e5\u01e6\u0001\u0000\u0000\u0000\u01e6C\u0001\u0000"+
		"\u0000\u0000\u01e7\u01f4\u0003J%\u0000\u01e8\u01ea\u0003B!\u0000\u01e9"+
		"\u01e8\u0001\u0000\u0000\u0000\u01ea\u01ed\u0001\u0000\u0000\u0000\u01eb"+
		"\u01e9\u0001\u0000\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec"+
		"\u01ee\u0001\u0000\u0000\u0000\u01ed\u01eb\u0001\u0000\u0000\u0000\u01ee"+
		"\u01f1\u0005\u001c\u0000\u0000\u01ef\u01f0\u0007\u000b\u0000\u0000\u01f0"+
		"\u01f2\u0003J%\u0000\u01f1\u01ef\u0001\u0000\u0000\u0000\u01f1\u01f2\u0001"+
		"\u0000\u0000\u0000\u01f2\u01f4\u0001\u0000\u0000\u0000\u01f3\u01e7\u0001"+
		"\u0000\u0000\u0000\u01f3\u01eb\u0001\u0000\u0000\u0000\u01f4E\u0001\u0000"+
		"\u0000\u0000\u01f5\u01f6\u0005\u0019\u0000\u0000\u01f6\u01fb\u0003D\""+
		"\u0000\u01f7\u01f8\u0005\u0015\u0000\u0000\u01f8\u01fa\u0003D\"\u0000"+
		"\u01f9\u01f7\u0001\u0000\u0000\u0000\u01fa\u01fd\u0001\u0000\u0000\u0000"+
		"\u01fb\u01f9\u0001\u0000\u0000\u0000\u01fb\u01fc\u0001\u0000\u0000\u0000"+
		"\u01fc\u01fe\u0001\u0000\u0000\u0000\u01fd\u01fb\u0001\u0000\u0000\u0000"+
		"\u01fe\u01ff\u0005\u0018\u0000\u0000\u01ffG\u0001\u0000\u0000\u0000\u0200"+
		"\u0202\u00032\u0019\u0000\u0201\u0203\u0003F#\u0000\u0202\u0201\u0001"+
		"\u0000\u0000\u0000\u0202\u0203\u0001\u0000\u0000\u0000\u0203\u0204\u0001"+
		"\u0000\u0000\u0000\u0204\u0205\u0005\u0016\u0000\u0000\u0205\u0207\u0001"+
		"\u0000\u0000\u0000\u0206\u0200\u0001\u0000\u0000\u0000\u0207\u020a\u0001"+
		"\u0000\u0000\u0000\u0208\u0206\u0001\u0000\u0000\u0000\u0208\u0209\u0001"+
		"\u0000\u0000\u0000\u0209\u020b\u0001\u0000\u0000\u0000\u020a\u0208\u0001"+
		"\u0000\u0000\u0000\u020b\u020d\u00034\u001a\u0000\u020c\u020e\u0003F#"+
		"\u0000\u020d\u020c\u0001\u0000\u0000\u0000\u020d\u020e\u0001\u0000\u0000"+
		"\u0000\u020eI\u0001\u0000\u0000\u0000\u020f\u0211\u0003B!\u0000\u0210"+
		"\u020f\u0001\u0000\u0000\u0000\u0211\u0214\u0001\u0000\u0000\u0000\u0212"+
		"\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000\u0000\u0000\u0213"+
		"\u0217\u0001\u0000\u0000\u0000\u0214\u0212\u0001\u0000\u0000\u0000\u0215"+
		"\u0218\u0003H$\u0000\u0216\u0218\u0003L&\u0000\u0217\u0215\u0001\u0000"+
		"\u0000\u0000\u0217\u0216\u0001\u0000\u0000\u0000\u0218\u0223\u0001\u0000"+
		"\u0000\u0000\u0219\u021b\u0003B!\u0000\u021a\u0219\u0001\u0000\u0000\u0000"+
		"\u021b\u021e\u0001\u0000\u0000\u0000\u021c\u021a\u0001\u0000\u0000\u0000"+
		"\u021c\u021d\u0001\u0000\u0000\u0000\u021d\u021f\u0001\u0000\u0000\u0000"+
		"\u021e\u021c\u0001\u0000\u0000\u0000\u021f\u0220\u0005\u0012\u0000\u0000"+
		"\u0220\u0222\u0005\u0013\u0000\u0000\u0221\u021c\u0001\u0000\u0000\u0000"+
		"\u0222\u0225\u0001\u0000\u0000\u0000\u0223\u0221\u0001\u0000\u0000\u0000"+
		"\u0223\u0224\u0001\u0000\u0000\u0000\u0224K\u0001\u0000\u0000\u0000\u0225"+
		"\u0223\u0001\u0000\u0000\u0000\u0226\u0227\u0007\f\u0000\u0000\u0227M"+
		"\u0001\u0000\u0000\u0000\u0228\u0229\u0005\u0019\u0000\u0000\u0229\u022a"+
		"\u0003P(\u0000\u022a\u022b\u0005\u0018\u0000\u0000\u022bO\u0001\u0000"+
		"\u0000\u0000\u022c\u0231\u0003J%\u0000\u022d\u022e\u0005\u0015\u0000\u0000"+
		"\u022e\u0230\u0003J%\u0000\u022f\u022d\u0001\u0000\u0000\u0000\u0230\u0233"+
		"\u0001\u0000\u0000\u0000\u0231\u022f\u0001\u0000\u0000\u0000\u0231\u0232"+
		"\u0001\u0000\u0000\u0000\u0232Q\u0001\u0000\u0000\u0000\u0233\u0231\u0001"+
		"\u0000\u0000\u0000Bbdu}\u0087\u0097\u00bc\u00c2\u00d2\u00d4\u00d6\u00e2"+
		"\u00e9\u00f0\u00f7\u00fb\u0106\u0108\u010d\u011a\u0126\u012d\u0130\u0138"+
		"\u013c\u013e\u0144\u0148\u014d\u0151\u0155\u015a\u015f\u0166\u016c\u016f"+
		"\u0176\u017e\u0183\u0187\u0189\u018d\u0197\u019a\u019d\u01a8\u01b4\u01bb"+
		"\u01c5\u01cc\u01d4\u01dd\u01e2\u01e5\u01eb\u01f1\u01f3\u01fb\u0202\u0208"+
		"\u020d\u0212\u0217\u021c\u0223\u0231";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}