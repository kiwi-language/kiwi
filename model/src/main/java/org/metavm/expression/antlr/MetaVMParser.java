// Generated from MetaVMParser.g4 by ANTLR 4.13.2
package org.metavm.expression.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class MetaVMParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ALL_MATCH=1, AS=2, THIS=3, INSTANCEOF=4, ANY=5, NEVER=6, BOOLEAN=7, LONG=8, 
		CHAR=9, DOUBLE=10, STRING=11, PASSWORD=12, TIME=13, VOID=14, CLASS=15, 
		R=16, C=17, V=18, DECIMAL_LITERAL=19, HEX_LITERAL=20, OCT_LITERAL=21, 
		BINARY_LITERAL=22, FLOAT_LITERAL=23, HEX_FLOAT_LITERAL=24, BOOL_LITERAL=25, 
		CHAR_LITERAL=26, STRING_LITERAL=27, TEXT_BLOCK=28, NULL_LITERAL=29, LPAREN=30, 
		RPAREN=31, LBRACE=32, RBRACE=33, LBRACK=34, RBRACK=35, SEMI=36, COMMA=37, 
		DOT=38, EQUAL=39, GT=40, LT=41, BANG=42, TILDE=43, QUESTION=44, COLON=45, 
		LE=46, GE=47, NOTEQUAL=48, AND=49, OR=50, INC=51, DEC=52, ADD=53, SUB=54, 
		MUL=55, DIV=56, BITAND=57, BITOR=58, CARET=59, MOD=60, ARROW=61, COLONCOLON=62, 
		AT=63, ELLIPSIS=64, WS=65, COMMENT=66, LINE_COMMENT=67, IDENTIFIER=68, 
		SUPER=69, NEW=70, FINAL=71, MODULE=72, OPEN=73, REQUIRES=74, EXPORTS=75, 
		OPENS=76, TO=77, USES=78, PROVIDES=79, WITH=80, TRANSITIVE=81, YIELD=82, 
		SEALED=83, PERMITS=84, RECORD=85, VAR=86;
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
		RULE_altAnnotationQualifiedName = 32, RULE_annotation = 33, RULE_typeArguments = 34, 
		RULE_classOrInterfaceType = 35, RULE_typeType = 36, RULE_parType = 37, 
		RULE_arrayKind = 38, RULE_primitiveType = 39, RULE_nonWildcardTypeArguments = 40, 
		RULE_typeList = 41;
	private static String[] makeRuleNames() {
		return new String[] {
			"primary", "expression", "allMatch", "list", "pattern", "variableModifier", 
			"innerCreator", "creator", "nonWildcardTypeArgumentsOrDiamond", "explicitGenericInvocation", 
			"classCreatorRest", "arrayCreatorRest", "arrayInitializer", "variableInitializer", 
			"createdName", "typeArgumentsOrDiamond", "methodCall", "expressionList", 
			"explicitGenericInvocationSuffix", "superSuffix", "arguments", "elementValueArrayInitializer", 
			"literal", "integerLiteral", "floatLiteral", "identifier", "typeIdentifier", 
			"typeTypeOrVoid", "elementValuePairs", "elementValuePair", "elementValue", 
			"qualifiedName", "altAnnotationQualifiedName", "annotation", "typeArguments", 
			"classOrInterfaceType", "typeType", "parType", "arrayKind", "primitiveType", 
			"nonWildcardTypeArguments", "typeList"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'allmatch'", "'as'", "'this'", "'instanceof'", "'any'", "'never'", 
			"'boolean'", "'long'", "'char'", "'dobule'", "'string'", "'password'", 
			"'time'", "'void'", "'class'", "'[r]'", "'[c]'", "'[v]'", null, null, 
			null, null, null, null, null, null, null, null, "'null'", "'('", "')'", 
			"'{'", "'}'", "'['", "']'", "';'", "','", "'.'", "'='", "'>'", "'<'", 
			"'!'", "'~'", "'?'", "':'", "'<='", "'>='", "'!='", "'and'", "'or'", 
			"'++'", "'--'", "'+'", "'-'", "'*'", "'/'", "'&'", "'|'", "'^'", "'%'", 
			"'->'", "'::'", "'@'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ALL_MATCH", "AS", "THIS", "INSTANCEOF", "ANY", "NEVER", "BOOLEAN", 
			"LONG", "CHAR", "DOUBLE", "STRING", "PASSWORD", "TIME", "VOID", "CLASS", 
			"R", "C", "V", "DECIMAL_LITERAL", "HEX_LITERAL", "OCT_LITERAL", "BINARY_LITERAL", 
			"FLOAT_LITERAL", "HEX_FLOAT_LITERAL", "BOOL_LITERAL", "CHAR_LITERAL", 
			"STRING_LITERAL", "TEXT_BLOCK", "NULL_LITERAL", "LPAREN", "RPAREN", "LBRACE", 
			"RBRACE", "LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", "EQUAL", "GT", 
			"LT", "BANG", "TILDE", "QUESTION", "COLON", "LE", "GE", "NOTEQUAL", "AND", 
			"OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", 
			"MOD", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "WS", "COMMENT", "LINE_COMMENT", 
			"IDENTIFIER", "SUPER", "NEW", "FINAL", "MODULE", "OPEN", "REQUIRES", 
			"EXPORTS", "OPENS", "TO", "USES", "PROVIDES", "WITH", "TRANSITIVE", "YIELD", 
			"SEALED", "PERMITS", "RECORD", "VAR"
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
	public String getGrammarFileName() { return "MetaVMParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public MetaVMParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
		public TerminalNode THIS() { return getToken(MetaVMParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(MetaVMParser.SUPER, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public TerminalNode DOT() { return getToken(MetaVMParser.DOT, 0); }
		public TerminalNode CLASS() { return getToken(MetaVMParser.CLASS, 0); }
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_primary);
		try {
			setState(102);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(84);
				match(LPAREN);
				setState(85);
				expression(0);
				setState(86);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				match(THIS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(89);
				match(SUPER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(90);
				literal();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(91);
				identifier();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(92);
				typeTypeOrVoid();
				setState(93);
				match(DOT);
				setState(94);
				match(CLASS);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(96);
				nonWildcardTypeArguments();
				setState(100);
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
					setState(97);
					explicitGenericInvocationSuffix();
					}
					break;
				case THIS:
					{
					setState(98);
					match(THIS);
					setState(99);
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
		public TerminalNode DOT() { return getToken(MetaVMParser.DOT, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public AllMatchContext allMatch() {
			return getRuleContext(AllMatchContext.class,0);
		}
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public TerminalNode NEW() { return getToken(MetaVMParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
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
		public List<TerminalNode> BITAND() { return getTokens(MetaVMParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(MetaVMParser.BITAND, i);
		}
		public TerminalNode ADD() { return getToken(MetaVMParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(MetaVMParser.SUB, 0); }
		public TerminalNode INC() { return getToken(MetaVMParser.INC, 0); }
		public TerminalNode DEC() { return getToken(MetaVMParser.DEC, 0); }
		public TerminalNode TILDE() { return getToken(MetaVMParser.TILDE, 0); }
		public TerminalNode BANG() { return getToken(MetaVMParser.BANG, 0); }
		public TerminalNode MUL() { return getToken(MetaVMParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(MetaVMParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(MetaVMParser.MOD, 0); }
		public List<TerminalNode> LT() { return getTokens(MetaVMParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(MetaVMParser.LT, i);
		}
		public List<TerminalNode> GT() { return getTokens(MetaVMParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(MetaVMParser.GT, i);
		}
		public TerminalNode LE() { return getToken(MetaVMParser.LE, 0); }
		public TerminalNode GE() { return getToken(MetaVMParser.GE, 0); }
		public TerminalNode EQUAL() { return getToken(MetaVMParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(MetaVMParser.NOTEQUAL, 0); }
		public TerminalNode CARET() { return getToken(MetaVMParser.CARET, 0); }
		public TerminalNode BITOR() { return getToken(MetaVMParser.BITOR, 0); }
		public TerminalNode AND() { return getToken(MetaVMParser.AND, 0); }
		public TerminalNode OR() { return getToken(MetaVMParser.OR, 0); }
		public TerminalNode COLON() { return getToken(MetaVMParser.COLON, 0); }
		public TerminalNode QUESTION() { return getToken(MetaVMParser.QUESTION, 0); }
		public TerminalNode THIS() { return getToken(MetaVMParser.THIS, 0); }
		public InnerCreatorContext innerCreator() {
			return getRuleContext(InnerCreatorContext.class,0);
		}
		public TerminalNode SUPER() { return getToken(MetaVMParser.SUPER, 0); }
		public SuperSuffixContext superSuffix() {
			return getRuleContext(SuperSuffixContext.class,0);
		}
		public ExplicitGenericInvocationContext explicitGenericInvocation() {
			return getRuleContext(ExplicitGenericInvocationContext.class,0);
		}
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(MetaVMParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(MetaVMParser.RBRACK, 0); }
		public TerminalNode AS() { return getToken(MetaVMParser.AS, 0); }
		public TerminalNode IDENTIFIER() { return getToken(MetaVMParser.IDENTIFIER, 0); }
		public TerminalNode INSTANCEOF() { return getToken(MetaVMParser.INSTANCEOF, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitExpression(this);
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
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(105);
				primary();
				}
				break;
			case 2:
				{
				setState(106);
				typeType(0);
				setState(107);
				((ExpressionContext)_localctx).bop = match(DOT);
				setState(108);
				identifier();
				}
				break;
			case 3:
				{
				setState(110);
				list();
				}
				break;
			case 4:
				{
				setState(111);
				allMatch();
				}
				break;
			case 5:
				{
				setState(112);
				methodCall();
				}
				break;
			case 6:
				{
				setState(113);
				match(NEW);
				setState(114);
				creator();
				}
				break;
			case 7:
				{
				setState(115);
				match(LPAREN);
				setState(119);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(116);
						annotation();
						}
						} 
					}
					setState(121);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				setState(122);
				typeType(0);
				setState(127);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==BITAND) {
					{
					{
					setState(123);
					match(BITAND);
					setState(124);
					typeType(0);
					}
					}
					setState(129);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(130);
				match(RPAREN);
				setState(131);
				expression(16);
				}
				break;
			case 8:
				{
				setState(133);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 33776997205278720L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(134);
				expression(14);
				}
				break;
			case 9:
				{
				setState(135);
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
				setState(136);
				expression(13);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(216);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(214);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(139);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(140);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1261007895663738880L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(141);
						expression(13);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(142);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(143);
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
						setState(144);
						expression(12);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(145);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(153);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
						case 1:
							{
							setState(146);
							match(LT);
							setState(147);
							match(LT);
							}
							break;
						case 2:
							{
							setState(148);
							match(GT);
							setState(149);
							match(GT);
							setState(150);
							match(GT);
							}
							break;
						case 3:
							{
							setState(151);
							match(GT);
							setState(152);
							match(GT);
							}
							break;
						}
						setState(155);
						expression(11);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(156);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(157);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 214404767416320L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(158);
						expression(10);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(159);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(160);
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
						setState(161);
						expression(8);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(162);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(163);
						((ExpressionContext)_localctx).bop = match(BITAND);
						setState(164);
						expression(7);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(165);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(166);
						((ExpressionContext)_localctx).bop = match(CARET);
						setState(167);
						expression(6);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(168);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(169);
						((ExpressionContext)_localctx).bop = match(BITOR);
						setState(170);
						expression(5);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(171);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(172);
						((ExpressionContext)_localctx).bop = match(AND);
						setState(173);
						expression(4);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(174);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(175);
						((ExpressionContext)_localctx).bop = match(OR);
						setState(176);
						expression(3);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(177);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(178);
						((ExpressionContext)_localctx).bop = match(QUESTION);
						setState(179);
						expression(0);
						setState(180);
						match(COLON);
						setState(181);
						expression(1);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(183);
						if (!(precpred(_ctx, 23))) throw new FailedPredicateException(this, "precpred(_ctx, 23)");
						setState(184);
						((ExpressionContext)_localctx).bop = match(DOT);
						setState(196);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
						case 1:
							{
							setState(185);
							identifier();
							}
							break;
						case 2:
							{
							setState(186);
							methodCall();
							}
							break;
						case 3:
							{
							setState(187);
							match(THIS);
							}
							break;
						case 4:
							{
							setState(188);
							match(NEW);
							setState(190);
							_errHandler.sync(this);
							_la = _input.LA(1);
							if (_la==LT) {
								{
								setState(189);
								nonWildcardTypeArguments();
								}
							}

							setState(192);
							innerCreator();
							}
							break;
						case 5:
							{
							setState(193);
							match(SUPER);
							setState(194);
							superSuffix();
							}
							break;
						case 6:
							{
							setState(195);
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
						setState(198);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(199);
						match(LBRACK);
						setState(200);
						expression(0);
						setState(201);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(203);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(204);
						match(AS);
						setState(205);
						match(IDENTIFIER);
						}
						break;
					case 15:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(206);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(207);
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
						setState(208);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(209);
						((ExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(212);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
						case 1:
							{
							setState(210);
							typeType(0);
							}
							break;
						case 2:
							{
							setState(211);
							pattern();
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(218);
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
		public TerminalNode ALL_MATCH() { return getToken(MetaVMParser.ALL_MATCH, 0); }
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(MetaVMParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
		public AllMatchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allMatch; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterAllMatch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitAllMatch(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitAllMatch(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllMatchContext allMatch() throws RecognitionException {
		AllMatchContext _localctx = new AllMatchContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_allMatch);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			match(ALL_MATCH);
			setState(220);
			match(LPAREN);
			setState(221);
			expression(0);
			setState(222);
			match(COMMA);
			setState(223);
			expression(0);
			setState(224);
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
		public TerminalNode LBRACK() { return getToken(MetaVMParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(MetaVMParser.RBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(226);
			match(LBRACK);
			setState(228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33792409694928874L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
				{
				setState(227);
				expressionList();
				}
			}

			setState(230);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitPattern(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_pattern);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(235);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(232);
					variableModifier();
					}
					} 
				}
				setState(237);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			}
			setState(238);
			typeType(0);
			setState(242);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(239);
					annotation();
					}
					} 
				}
				setState(244);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			setState(245);
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
		public TerminalNode FINAL() { return getToken(MetaVMParser.FINAL, 0); }
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public VariableModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterVariableModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitVariableModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitVariableModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableModifierContext variableModifier() throws RecognitionException {
		VariableModifierContext _localctx = new VariableModifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_variableModifier);
		try {
			setState(249);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FINAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(247);
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
				setState(248);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterInnerCreator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitInnerCreator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitInnerCreator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InnerCreatorContext innerCreator() throws RecognitionException {
		InnerCreatorContext _localctx = new InnerCreatorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_innerCreator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251);
			identifier();
			setState(253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(252);
				nonWildcardTypeArgumentsOrDiamond();
				}
			}

			setState(255);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterCreator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitCreator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitCreator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreatorContext creator() throws RecognitionException {
		CreatorContext _localctx = new CreatorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_creator);
		try {
			setState(266);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LT:
				enterOuterAlt(_localctx, 1);
				{
				setState(257);
				nonWildcardTypeArguments();
				setState(258);
				createdName();
				setState(259);
				classCreatorRest();
				}
				break;
			case BOOLEAN:
			case LONG:
			case CHAR:
			case DOUBLE:
			case STRING:
			case PASSWORD:
			case TIME:
			case VOID:
			case NULL_LITERAL:
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
				setState(261);
				createdName();
				setState(264);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LBRACK:
					{
					setState(262);
					arrayCreatorRest();
					}
					break;
				case LPAREN:
					{
					setState(263);
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
		public TerminalNode LT() { return getToken(MetaVMParser.LT, 0); }
		public TerminalNode GT() { return getToken(MetaVMParser.GT, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public NonWildcardTypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArgumentsOrDiamond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterNonWildcardTypeArgumentsOrDiamond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitNonWildcardTypeArgumentsOrDiamond(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitNonWildcardTypeArgumentsOrDiamond(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonWildcardTypeArgumentsOrDiamondContext nonWildcardTypeArgumentsOrDiamond() throws RecognitionException {
		NonWildcardTypeArgumentsOrDiamondContext _localctx = new NonWildcardTypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_nonWildcardTypeArgumentsOrDiamond);
		try {
			setState(271);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(268);
				match(LT);
				setState(269);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(270);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterExplicitGenericInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitExplicitGenericInvocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitExplicitGenericInvocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitGenericInvocationContext explicitGenericInvocation() throws RecognitionException {
		ExplicitGenericInvocationContext _localctx = new ExplicitGenericInvocationContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273);
			nonWildcardTypeArguments();
			setState(274);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterClassCreatorRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitClassCreatorRest(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitClassCreatorRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassCreatorRestContext classCreatorRest() throws RecognitionException {
		ClassCreatorRestContext _localctx = new ClassCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
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
		public List<TerminalNode> LBRACK() { return getTokens(MetaVMParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(MetaVMParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(MetaVMParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(MetaVMParser.RBRACK, i);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterArrayCreatorRest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitArrayCreatorRest(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitArrayCreatorRest(this);
			else return visitor.visitChildren(this);
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
			setState(278);
			match(LBRACK);
			setState(306);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case RBRACK:
				{
				setState(279);
				match(RBRACK);
				setState(284);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==LBRACK) {
					{
					{
					setState(280);
					match(LBRACK);
					setState(281);
					match(RBRACK);
					}
					}
					setState(286);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(287);
				arrayInitializer();
				}
				break;
			case ALL_MATCH:
			case THIS:
			case ANY:
			case NEVER:
			case BOOLEAN:
			case LONG:
			case CHAR:
			case DOUBLE:
			case STRING:
			case PASSWORD:
			case TIME:
			case VOID:
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
			case IDENTIFIER:
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
				{
				setState(288);
				expression(0);
				setState(289);
				match(RBRACK);
				setState(296);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(290);
						match(LBRACK);
						setState(291);
						expression(0);
						setState(292);
						match(RBRACK);
						}
						} 
					}
					setState(298);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				}
				setState(303);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(299);
						match(LBRACK);
						setState(300);
						match(RBRACK);
						}
						} 
					}
					setState(305);
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
		public TerminalNode LBRACE() { return getToken(MetaVMParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MetaVMParser.RBRACE, 0); }
		public List<VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MetaVMParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MetaVMParser.COMMA, i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitArrayInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitArrayInitializer(this);
			else return visitor.visitChildren(this);
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
			setState(308);
			match(LBRACE);
			setState(320);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33792413989896170L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
				{
				setState(309);
				variableInitializer();
				setState(314);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(310);
						match(COMMA);
						setState(311);
						variableInitializer();
						}
						} 
					}
					setState(316);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
				}
				setState(318);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(317);
					match(COMMA);
					}
				}

				}
			}

			setState(322);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterVariableInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitVariableInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitVariableInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableInitializerContext variableInitializer() throws RecognitionException {
		VariableInitializerContext _localctx = new VariableInitializerContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_variableInitializer);
		try {
			setState(326);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(324);
				arrayInitializer();
				}
				break;
			case ALL_MATCH:
			case THIS:
			case ANY:
			case NEVER:
			case BOOLEAN:
			case LONG:
			case CHAR:
			case DOUBLE:
			case STRING:
			case PASSWORD:
			case TIME:
			case VOID:
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
			case IDENTIFIER:
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
				enterOuterAlt(_localctx, 2);
				{
				setState(325);
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
		public List<TerminalNode> DOT() { return getTokens(MetaVMParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MetaVMParser.DOT, i);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterCreatedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitCreatedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitCreatedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreatedNameContext createdName() throws RecognitionException {
		CreatedNameContext _localctx = new CreatedNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_createdName);
		int _la;
		try {
			setState(343);
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
				setState(328);
				identifier();
				setState(330);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(329);
					typeArgumentsOrDiamond();
					}
				}

				setState(339);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(332);
					match(DOT);
					setState(333);
					identifier();
					setState(335);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LT) {
						{
						setState(334);
						typeArgumentsOrDiamond();
						}
					}

					}
					}
					setState(341);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case BOOLEAN:
			case LONG:
			case CHAR:
			case DOUBLE:
			case STRING:
			case PASSWORD:
			case TIME:
			case VOID:
			case NULL_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(342);
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
		public TerminalNode LT() { return getToken(MetaVMParser.LT, 0); }
		public TerminalNode GT() { return getToken(MetaVMParser.GT, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public TypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgumentsOrDiamond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterTypeArgumentsOrDiamond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitTypeArgumentsOrDiamond(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitTypeArgumentsOrDiamond(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsOrDiamondContext typeArgumentsOrDiamond() throws RecognitionException {
		TypeArgumentsOrDiamondContext _localctx = new TypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_typeArgumentsOrDiamond);
		try {
			setState(348);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(345);
				match(LT);
				setState(346);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(347);
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
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode THIS() { return getToken(MetaVMParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(MetaVMParser.SUPER, 0); }
		public MethodCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_methodCall);
		int _la;
		try {
			setState(369);
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
				setState(350);
				identifier();
				setState(351);
				match(LPAREN);
				setState(353);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33792409694928874L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
					{
					setState(352);
					expressionList();
					}
				}

				setState(355);
				match(RPAREN);
				}
				break;
			case THIS:
				enterOuterAlt(_localctx, 2);
				{
				setState(357);
				match(THIS);
				setState(358);
				match(LPAREN);
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33792409694928874L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
					{
					setState(359);
					expressionList();
					}
				}

				setState(362);
				match(RPAREN);
				}
				break;
			case SUPER:
				enterOuterAlt(_localctx, 3);
				{
				setState(363);
				match(SUPER);
				setState(364);
				match(LPAREN);
				setState(366);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33792409694928874L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
					{
					setState(365);
					expressionList();
					}
				}

				setState(368);
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
		public List<TerminalNode> COMMA() { return getTokens(MetaVMParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MetaVMParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			expression(0);
			setState(376);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(372);
				match(COMMA);
				setState(373);
				expression(0);
				}
				}
				setState(378);
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
		public TerminalNode SUPER() { return getToken(MetaVMParser.SUPER, 0); }
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterExplicitGenericInvocationSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitExplicitGenericInvocationSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitExplicitGenericInvocationSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() throws RecognitionException {
		ExplicitGenericInvocationSuffixContext _localctx = new ExplicitGenericInvocationSuffixContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_explicitGenericInvocationSuffix);
		try {
			setState(384);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(379);
				match(SUPER);
				setState(380);
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
				setState(381);
				identifier();
				setState(382);
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
		public TerminalNode DOT() { return getToken(MetaVMParser.DOT, 0); }
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterSuperSuffix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitSuperSuffix(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitSuperSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SuperSuffixContext superSuffix() throws RecognitionException {
		SuperSuffixContext _localctx = new SuperSuffixContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_superSuffix);
		int _la;
		try {
			setState(395);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(386);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(387);
				match(DOT);
				setState(389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(388);
					typeArguments();
					}
				}

				setState(391);
				identifier();
				setState(393);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
				case 1:
					{
					setState(392);
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
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397);
			match(LPAREN);
			setState(399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 33792409694928874L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
				{
				setState(398);
				expressionList();
				}
			}

			setState(401);
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
		public TerminalNode LBRACE() { return getToken(MetaVMParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(MetaVMParser.RBRACE, 0); }
		public List<ElementValueContext> elementValue() {
			return getRuleContexts(ElementValueContext.class);
		}
		public ElementValueContext elementValue(int i) {
			return getRuleContext(ElementValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MetaVMParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MetaVMParser.COMMA, i);
		}
		public ElementValueArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValueArrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterElementValueArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitElementValueArrayInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitElementValueArrayInitializer(this);
			else return visitor.visitChildren(this);
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
			setState(403);
			match(LBRACE);
			setState(412);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -9189579622864879638L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524279L) != 0)) {
				{
				setState(404);
				elementValue();
				setState(409);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(405);
						match(COMMA);
						setState(406);
						elementValue();
						}
						} 
					}
					setState(411);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
				}
				}
			}

			setState(415);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(414);
				match(COMMA);
				}
			}

			setState(417);
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
		public TerminalNode CHAR_LITERAL() { return getToken(MetaVMParser.CHAR_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(MetaVMParser.STRING_LITERAL, 0); }
		public TerminalNode BOOL_LITERAL() { return getToken(MetaVMParser.BOOL_LITERAL, 0); }
		public TerminalNode NULL_LITERAL() { return getToken(MetaVMParser.NULL_LITERAL, 0); }
		public TerminalNode TEXT_BLOCK() { return getToken(MetaVMParser.TEXT_BLOCK, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_literal);
		try {
			setState(426);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(419);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(420);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(421);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(422);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(423);
				match(BOOL_LITERAL);
				}
				break;
			case NULL_LITERAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(424);
				match(NULL_LITERAL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(425);
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
		public TerminalNode DECIMAL_LITERAL() { return getToken(MetaVMParser.DECIMAL_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(MetaVMParser.HEX_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(MetaVMParser.OCT_LITERAL, 0); }
		public TerminalNode BINARY_LITERAL() { return getToken(MetaVMParser.BINARY_LITERAL, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitIntegerLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7864320L) != 0)) ) {
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
		public TerminalNode FLOAT_LITERAL() { return getToken(MetaVMParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(MetaVMParser.HEX_FLOAT_LITERAL, 0); }
		public FloatLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterFloatLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitFloatLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FloatLiteralContext floatLiteral() throws RecognitionException {
		FloatLiteralContext _localctx = new FloatLiteralContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
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
		public TerminalNode IDENTIFIER() { return getToken(MetaVMParser.IDENTIFIER, 0); }
		public TerminalNode MODULE() { return getToken(MetaVMParser.MODULE, 0); }
		public TerminalNode OPEN() { return getToken(MetaVMParser.OPEN, 0); }
		public TerminalNode REQUIRES() { return getToken(MetaVMParser.REQUIRES, 0); }
		public TerminalNode EXPORTS() { return getToken(MetaVMParser.EXPORTS, 0); }
		public TerminalNode OPENS() { return getToken(MetaVMParser.OPENS, 0); }
		public TerminalNode TO() { return getToken(MetaVMParser.TO, 0); }
		public TerminalNode USES() { return getToken(MetaVMParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(MetaVMParser.PROVIDES, 0); }
		public TerminalNode WITH() { return getToken(MetaVMParser.WITH, 0); }
		public TerminalNode TRANSITIVE() { return getToken(MetaVMParser.TRANSITIVE, 0); }
		public TerminalNode YIELD() { return getToken(MetaVMParser.YIELD, 0); }
		public TerminalNode SEALED() { return getToken(MetaVMParser.SEALED, 0); }
		public TerminalNode PERMITS() { return getToken(MetaVMParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(MetaVMParser.RECORD, 0); }
		public TerminalNode VAR() { return getToken(MetaVMParser.VAR, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(432);
			_la = _input.LA(1);
			if ( !(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524273L) != 0)) ) {
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
		public TerminalNode IDENTIFIER() { return getToken(MetaVMParser.IDENTIFIER, 0); }
		public TerminalNode MODULE() { return getToken(MetaVMParser.MODULE, 0); }
		public TerminalNode OPEN() { return getToken(MetaVMParser.OPEN, 0); }
		public TerminalNode REQUIRES() { return getToken(MetaVMParser.REQUIRES, 0); }
		public TerminalNode EXPORTS() { return getToken(MetaVMParser.EXPORTS, 0); }
		public TerminalNode OPENS() { return getToken(MetaVMParser.OPENS, 0); }
		public TerminalNode TO() { return getToken(MetaVMParser.TO, 0); }
		public TerminalNode USES() { return getToken(MetaVMParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(MetaVMParser.PROVIDES, 0); }
		public TerminalNode WITH() { return getToken(MetaVMParser.WITH, 0); }
		public TerminalNode TRANSITIVE() { return getToken(MetaVMParser.TRANSITIVE, 0); }
		public TerminalNode SEALED() { return getToken(MetaVMParser.SEALED, 0); }
		public TerminalNode PERMITS() { return getToken(MetaVMParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(MetaVMParser.RECORD, 0); }
		public TypeIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterTypeIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitTypeIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitTypeIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeIdentifierContext typeIdentifier() throws RecognitionException {
		TypeIdentifierContext _localctx = new TypeIdentifierContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_typeIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
			_la = _input.LA(1);
			if ( !(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 245745L) != 0)) ) {
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
		public TerminalNode VOID() { return getToken(MetaVMParser.VOID, 0); }
		public TypeTypeOrVoidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeTypeOrVoid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterTypeTypeOrVoid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitTypeTypeOrVoid(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitTypeTypeOrVoid(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeTypeOrVoidContext typeTypeOrVoid() throws RecognitionException {
		TypeTypeOrVoidContext _localctx = new TypeTypeOrVoidContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_typeTypeOrVoid);
		try {
			setState(438);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(436);
				typeType(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(437);
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
	public static class ElementValuePairsContext extends ParserRuleContext {
		public List<ElementValuePairContext> elementValuePair() {
			return getRuleContexts(ElementValuePairContext.class);
		}
		public ElementValuePairContext elementValuePair(int i) {
			return getRuleContext(ElementValuePairContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(MetaVMParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MetaVMParser.COMMA, i);
		}
		public ElementValuePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePairs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterElementValuePairs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitElementValuePairs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitElementValuePairs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValuePairsContext elementValuePairs() throws RecognitionException {
		ElementValuePairsContext _localctx = new ElementValuePairsContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_elementValuePairs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(440);
			elementValuePair();
			setState(445);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(441);
				match(COMMA);
				setState(442);
				elementValuePair();
				}
				}
				setState(447);
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
		public TerminalNode EQUAL() { return getToken(MetaVMParser.EQUAL, 0); }
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public ElementValuePairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValuePair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterElementValuePair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitElementValuePair(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitElementValuePair(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValuePairContext elementValuePair() throws RecognitionException {
		ElementValuePairContext _localctx = new ElementValuePairContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_elementValuePair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(448);
			identifier();
			setState(449);
			match(EQUAL);
			setState(450);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterElementValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitElementValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitElementValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValueContext elementValue() throws RecognitionException {
		ElementValueContext _localctx = new ElementValueContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_elementValue);
		try {
			setState(455);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(452);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(453);
				annotation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(454);
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
		public List<TerminalNode> DOT() { return getTokens(MetaVMParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MetaVMParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(457);
			identifier();
			setState(462);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(458);
					match(DOT);
					setState(459);
					identifier();
					}
					} 
				}
				setState(464);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
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
		public TerminalNode AT() { return getToken(MetaVMParser.AT, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(MetaVMParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(MetaVMParser.DOT, i);
		}
		public AltAnnotationQualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_altAnnotationQualifiedName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterAltAnnotationQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitAltAnnotationQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitAltAnnotationQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AltAnnotationQualifiedNameContext altAnnotationQualifiedName() throws RecognitionException {
		AltAnnotationQualifiedNameContext _localctx = new AltAnnotationQualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_altAnnotationQualifiedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(470);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524273L) != 0)) {
				{
				{
				setState(465);
				identifier();
				setState(466);
				match(DOT);
				}
				}
				setState(472);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(473);
			match(AT);
			setState(474);
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
		public TerminalNode AT() { return getToken(MetaVMParser.AT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public AltAnnotationQualifiedNameContext altAnnotationQualifiedName() {
			return getRuleContext(AltAnnotationQualifiedNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitAnnotation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_annotation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(479);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				{
				setState(476);
				match(AT);
				setState(477);
				qualifiedName();
				}
				break;
			case 2:
				{
				setState(478);
				altAnnotationQualifiedName();
				}
				break;
			}
			setState(487);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				{
				setState(481);
				match(LPAREN);
				setState(484);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
				case 1:
					{
					setState(482);
					elementValuePairs();
					}
					break;
				case 2:
					{
					setState(483);
					elementValue();
					}
					break;
				}
				setState(486);
				match(RPAREN);
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
		public TerminalNode LT() { return getToken(MetaVMParser.LT, 0); }
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(MetaVMParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(MetaVMParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MetaVMParser.COMMA, i);
		}
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitTypeArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(489);
			match(LT);
			setState(490);
			typeType(0);
			setState(495);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(491);
				match(COMMA);
				setState(492);
				typeType(0);
				}
				}
				setState(497);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(498);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterClassOrInterfaceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitClassOrInterfaceType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitClassOrInterfaceType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceTypeContext classOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(500);
			qualifiedName();
			setState(502);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(501);
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
		public TerminalNode NEVER() { return getToken(MetaVMParser.NEVER, 0); }
		public TerminalNode ANY() { return getToken(MetaVMParser.ANY, 0); }
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public ParTypeContext parType() {
			return getRuleContext(ParTypeContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(MetaVMParser.LBRACK, 0); }
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(MetaVMParser.COMMA, 0); }
		public TerminalNode RBRACK() { return getToken(MetaVMParser.RBRACK, 0); }
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(MetaVMParser.ARROW, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public List<TerminalNode> BITOR() { return getTokens(MetaVMParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(MetaVMParser.BITOR, i);
		}
		public List<TerminalNode> BITAND() { return getTokens(MetaVMParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(MetaVMParser.BITAND, i);
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
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterTypeType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitTypeType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitTypeType(this);
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
		int _startState = 72;
		enterRecursionRule(_localctx, 72, RULE_typeType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(523);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				{
				setState(505);
				match(NEVER);
				}
				break;
			case 2:
				{
				setState(506);
				match(ANY);
				}
				break;
			case 3:
				{
				setState(507);
				primitiveType();
				}
				break;
			case 4:
				{
				setState(508);
				parType();
				}
				break;
			case 5:
				{
				setState(509);
				match(LBRACK);
				setState(510);
				typeType(0);
				setState(511);
				match(COMMA);
				setState(512);
				typeType(0);
				setState(513);
				match(RBRACK);
				}
				break;
			case 6:
				{
				setState(515);
				match(LPAREN);
				setState(517);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 18790514656L) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 524273L) != 0)) {
					{
					setState(516);
					typeList();
					}
				}

				setState(519);
				match(RPAREN);
				setState(520);
				match(ARROW);
				setState(521);
				typeType(3);
				}
				break;
			case 7:
				{
				setState(522);
				classOrInterfaceType();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(543);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(541);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
					case 1:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(525);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(528); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(526);
								match(BITOR);
								setState(527);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(530); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,58,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 2:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(532);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(535); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(533);
								match(BITAND);
								setState(534);
								typeType(0);
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(537); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					case 3:
						{
						_localctx = new TypeTypeContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_typeType);
						setState(539);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(540);
						arrayKind();
						}
						break;
					}
					} 
				}
				setState(545);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
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
		public TerminalNode LPAREN() { return getToken(MetaVMParser.LPAREN, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(MetaVMParser.RPAREN, 0); }
		public ParTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterParType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitParType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitParType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParTypeContext parType() throws RecognitionException {
		ParTypeContext _localctx = new ParTypeContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_parType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(546);
			match(LPAREN);
			setState(547);
			typeType(0);
			setState(548);
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
	public static class ArrayKindContext extends ParserRuleContext {
		public TerminalNode R() { return getToken(MetaVMParser.R, 0); }
		public TerminalNode LBRACK() { return getToken(MetaVMParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(MetaVMParser.RBRACK, 0); }
		public TerminalNode C() { return getToken(MetaVMParser.C, 0); }
		public TerminalNode V() { return getToken(MetaVMParser.V, 0); }
		public ArrayKindContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayKind; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterArrayKind(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitArrayKind(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitArrayKind(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayKindContext arrayKind() throws RecognitionException {
		ArrayKindContext _localctx = new ArrayKindContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_arrayKind);
		try {
			setState(555);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case R:
				enterOuterAlt(_localctx, 1);
				{
				setState(550);
				match(R);
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 2);
				{
				setState(551);
				match(LBRACK);
				setState(552);
				match(RBRACK);
				}
				break;
			case C:
				enterOuterAlt(_localctx, 3);
				{
				setState(553);
				match(C);
				}
				break;
			case V:
				enterOuterAlt(_localctx, 4);
				{
				setState(554);
				match(V);
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
		public TerminalNode BOOLEAN() { return getToken(MetaVMParser.BOOLEAN, 0); }
		public TerminalNode STRING() { return getToken(MetaVMParser.STRING, 0); }
		public TerminalNode LONG() { return getToken(MetaVMParser.LONG, 0); }
		public TerminalNode CHAR() { return getToken(MetaVMParser.CHAR, 0); }
		public TerminalNode DOUBLE() { return getToken(MetaVMParser.DOUBLE, 0); }
		public TerminalNode TIME() { return getToken(MetaVMParser.TIME, 0); }
		public TerminalNode PASSWORD() { return getToken(MetaVMParser.PASSWORD, 0); }
		public TerminalNode VOID() { return getToken(MetaVMParser.VOID, 0); }
		public TerminalNode NULL_LITERAL() { return getToken(MetaVMParser.NULL_LITERAL, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterPrimitiveType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitPrimitiveType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(557);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 536903552L) != 0)) ) {
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
		public TerminalNode LT() { return getToken(MetaVMParser.LT, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode GT() { return getToken(MetaVMParser.GT, 0); }
		public NonWildcardTypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArguments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterNonWildcardTypeArguments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitNonWildcardTypeArguments(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitNonWildcardTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonWildcardTypeArgumentsContext nonWildcardTypeArguments() throws RecognitionException {
		NonWildcardTypeArgumentsContext _localctx = new NonWildcardTypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(559);
			match(LT);
			setState(560);
			typeList();
			setState(561);
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
		public List<TerminalNode> COMMA() { return getTokens(MetaVMParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(MetaVMParser.COMMA, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).enterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof MetaVMParserListener ) ((MetaVMParserListener)listener).exitTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof MetaVMParserVisitor ) return ((MetaVMParserVisitor<? extends T>)visitor).visitTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			typeType(0);
			setState(568);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(564);
				match(COMMA);
				setState(565);
				typeType(0);
				}
				}
				setState(570);
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
		case 36:
			return typeType_sempred((TypeTypeContext)_localctx, predIndex);
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
	private boolean typeType_sempred(TypeTypeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 16:
			return precpred(_ctx, 6);
		case 17:
			return precpred(_ctx, 5);
		case 18:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001V\u023c\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"(\u0007(\u0002)\u0007)\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0003\u0000e\b\u0000\u0003\u0000g\b\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001v\b"+
		"\u0001\n\u0001\f\u0001y\t\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005"+
		"\u0001~\b\u0001\n\u0001\f\u0001\u0081\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"\u008a\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u009a\b\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00bf\b\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00c5\b\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001\u00d5\b\u0001\u0005\u0001\u00d7\b"+
		"\u0001\n\u0001\f\u0001\u00da\t\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0003\u0003\u00e5\b\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0005\u0004"+
		"\u00ea\b\u0004\n\u0004\f\u0004\u00ed\t\u0004\u0001\u0004\u0001\u0004\u0005"+
		"\u0004\u00f1\b\u0004\n\u0004\f\u0004\u00f4\t\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0003\u0005\u00fa\b\u0005\u0001\u0006\u0001\u0006"+
		"\u0003\u0006\u00fe\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u0109\b\u0007\u0003\u0007\u010b\b\u0007\u0001\b\u0001\b\u0001\b\u0003"+
		"\b\u0110\b\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u011b\b\u000b\n\u000b\f\u000b"+
		"\u011e\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0005\u000b\u0127\b\u000b\n\u000b\f\u000b\u012a"+
		"\t\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u012e\b\u000b\n\u000b\f\u000b"+
		"\u0131\t\u000b\u0003\u000b\u0133\b\u000b\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0005\f\u0139\b\f\n\f\f\f\u013c\t\f\u0001\f\u0003\f\u013f\b\f\u0003"+
		"\f\u0141\b\f\u0001\f\u0001\f\u0001\r\u0001\r\u0003\r\u0147\b\r\u0001\u000e"+
		"\u0001\u000e\u0003\u000e\u014b\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u0150\b\u000e\u0005\u000e\u0152\b\u000e\n\u000e\f\u000e\u0155"+
		"\t\u000e\u0001\u000e\u0003\u000e\u0158\b\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0003\u000f\u015d\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0003\u0010\u0162\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u0169\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u016f\b\u0010\u0001\u0010\u0003\u0010\u0172\b"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u0177\b\u0011\n"+
		"\u0011\f\u0011\u017a\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0003\u0012\u0181\b\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0003\u0013\u0186\b\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u018a"+
		"\b\u0013\u0003\u0013\u018c\b\u0013\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u0190\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0005\u0015\u0198\b\u0015\n\u0015\f\u0015\u019b\t\u0015\u0003"+
		"\u0015\u019d\b\u0015\u0001\u0015\u0003\u0015\u01a0\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0003\u0016\u01ab\b\u0016\u0001\u0017\u0001\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a"+
		"\u0001\u001b\u0001\u001b\u0003\u001b\u01b7\b\u001b\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0005\u001c\u01bc\b\u001c\n\u001c\f\u001c\u01bf\t\u001c\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0003\u001e\u01c8\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0005"+
		"\u001f\u01cd\b\u001f\n\u001f\f\u001f\u01d0\t\u001f\u0001 \u0001 \u0001"+
		" \u0005 \u01d5\b \n \f \u01d8\t \u0001 \u0001 \u0001 \u0001!\u0001!\u0001"+
		"!\u0003!\u01e0\b!\u0001!\u0001!\u0001!\u0003!\u01e5\b!\u0001!\u0003!\u01e8"+
		"\b!\u0001\"\u0001\"\u0001\"\u0001\"\u0005\"\u01ee\b\"\n\"\f\"\u01f1\t"+
		"\"\u0001\"\u0001\"\u0001#\u0001#\u0003#\u01f7\b#\u0001$\u0001$\u0001$"+
		"\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0003$\u0206\b$\u0001$\u0001$\u0001$\u0001$\u0003$\u020c\b$\u0001$\u0001"+
		"$\u0001$\u0004$\u0211\b$\u000b$\f$\u0212\u0001$\u0001$\u0001$\u0004$\u0218"+
		"\b$\u000b$\f$\u0219\u0001$\u0001$\u0005$\u021e\b$\n$\f$\u0221\t$\u0001"+
		"%\u0001%\u0001%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001&\u0003&\u022c"+
		"\b&\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0001(\u0001)\u0001)\u0001)\u0005"+
		")\u0237\b)\n)\f)\u023a\t)\u0001)\u0000\u0002\u0002H*\u0000\u0002\u0004"+
		"\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""+
		"$&(*,.02468:<>@BDFHJLNPR\u0000\f\u0001\u000036\u0001\u0000*+\u0002\u0000"+
		"78<<\u0001\u000056\u0002\u0000()./\u0002\u0000\'\'00\u0001\u000034\u0001"+
		"\u0000\u0013\u0016\u0001\u0000\u0017\u0018\u0002\u0000DDHV\u0003\u0000"+
		"DDHQSU\u0002\u0000\u0007\u000e\u001d\u001d\u0280\u0000f\u0001\u0000\u0000"+
		"\u0000\u0002\u0089\u0001\u0000\u0000\u0000\u0004\u00db\u0001\u0000\u0000"+
		"\u0000\u0006\u00e2\u0001\u0000\u0000\u0000\b\u00eb\u0001\u0000\u0000\u0000"+
		"\n\u00f9\u0001\u0000\u0000\u0000\f\u00fb\u0001\u0000\u0000\u0000\u000e"+
		"\u010a\u0001\u0000\u0000\u0000\u0010\u010f\u0001\u0000\u0000\u0000\u0012"+
		"\u0111\u0001\u0000\u0000\u0000\u0014\u0114\u0001\u0000\u0000\u0000\u0016"+
		"\u0116\u0001\u0000\u0000\u0000\u0018\u0134\u0001\u0000\u0000\u0000\u001a"+
		"\u0146\u0001\u0000\u0000\u0000\u001c\u0157\u0001\u0000\u0000\u0000\u001e"+
		"\u015c\u0001\u0000\u0000\u0000 \u0171\u0001\u0000\u0000\u0000\"\u0173"+
		"\u0001\u0000\u0000\u0000$\u0180\u0001\u0000\u0000\u0000&\u018b\u0001\u0000"+
		"\u0000\u0000(\u018d\u0001\u0000\u0000\u0000*\u0193\u0001\u0000\u0000\u0000"+
		",\u01aa\u0001\u0000\u0000\u0000.\u01ac\u0001\u0000\u0000\u00000\u01ae"+
		"\u0001\u0000\u0000\u00002\u01b0\u0001\u0000\u0000\u00004\u01b2\u0001\u0000"+
		"\u0000\u00006\u01b6\u0001\u0000\u0000\u00008\u01b8\u0001\u0000\u0000\u0000"+
		":\u01c0\u0001\u0000\u0000\u0000<\u01c7\u0001\u0000\u0000\u0000>\u01c9"+
		"\u0001\u0000\u0000\u0000@\u01d6\u0001\u0000\u0000\u0000B\u01df\u0001\u0000"+
		"\u0000\u0000D\u01e9\u0001\u0000\u0000\u0000F\u01f4\u0001\u0000\u0000\u0000"+
		"H\u020b\u0001\u0000\u0000\u0000J\u0222\u0001\u0000\u0000\u0000L\u022b"+
		"\u0001\u0000\u0000\u0000N\u022d\u0001\u0000\u0000\u0000P\u022f\u0001\u0000"+
		"\u0000\u0000R\u0233\u0001\u0000\u0000\u0000TU\u0005\u001e\u0000\u0000"+
		"UV\u0003\u0002\u0001\u0000VW\u0005\u001f\u0000\u0000Wg\u0001\u0000\u0000"+
		"\u0000Xg\u0005\u0003\u0000\u0000Yg\u0005E\u0000\u0000Zg\u0003,\u0016\u0000"+
		"[g\u00032\u0019\u0000\\]\u00036\u001b\u0000]^\u0005&\u0000\u0000^_\u0005"+
		"\u000f\u0000\u0000_g\u0001\u0000\u0000\u0000`d\u0003P(\u0000ae\u0003$"+
		"\u0012\u0000bc\u0005\u0003\u0000\u0000ce\u0003(\u0014\u0000da\u0001\u0000"+
		"\u0000\u0000db\u0001\u0000\u0000\u0000eg\u0001\u0000\u0000\u0000fT\u0001"+
		"\u0000\u0000\u0000fX\u0001\u0000\u0000\u0000fY\u0001\u0000\u0000\u0000"+
		"fZ\u0001\u0000\u0000\u0000f[\u0001\u0000\u0000\u0000f\\\u0001\u0000\u0000"+
		"\u0000f`\u0001\u0000\u0000\u0000g\u0001\u0001\u0000\u0000\u0000hi\u0006"+
		"\u0001\uffff\uffff\u0000i\u008a\u0003\u0000\u0000\u0000jk\u0003H$\u0000"+
		"kl\u0005&\u0000\u0000lm\u00032\u0019\u0000m\u008a\u0001\u0000\u0000\u0000"+
		"n\u008a\u0003\u0006\u0003\u0000o\u008a\u0003\u0004\u0002\u0000p\u008a"+
		"\u0003 \u0010\u0000qr\u0005F\u0000\u0000r\u008a\u0003\u000e\u0007\u0000"+
		"sw\u0005\u001e\u0000\u0000tv\u0003B!\u0000ut\u0001\u0000\u0000\u0000v"+
		"y\u0001\u0000\u0000\u0000wu\u0001\u0000\u0000\u0000wx\u0001\u0000\u0000"+
		"\u0000xz\u0001\u0000\u0000\u0000yw\u0001\u0000\u0000\u0000z\u007f\u0003"+
		"H$\u0000{|\u00059\u0000\u0000|~\u0003H$\u0000}{\u0001\u0000\u0000\u0000"+
		"~\u0081\u0001\u0000\u0000\u0000\u007f}\u0001\u0000\u0000\u0000\u007f\u0080"+
		"\u0001\u0000\u0000\u0000\u0080\u0082\u0001\u0000\u0000\u0000\u0081\u007f"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0005\u001f\u0000\u0000\u0083\u0084"+
		"\u0003\u0002\u0001\u0010\u0084\u008a\u0001\u0000\u0000\u0000\u0085\u0086"+
		"\u0007\u0000\u0000\u0000\u0086\u008a\u0003\u0002\u0001\u000e\u0087\u0088"+
		"\u0007\u0001\u0000\u0000\u0088\u008a\u0003\u0002\u0001\r\u0089h\u0001"+
		"\u0000\u0000\u0000\u0089j\u0001\u0000\u0000\u0000\u0089n\u0001\u0000\u0000"+
		"\u0000\u0089o\u0001\u0000\u0000\u0000\u0089p\u0001\u0000\u0000\u0000\u0089"+
		"q\u0001\u0000\u0000\u0000\u0089s\u0001\u0000\u0000\u0000\u0089\u0085\u0001"+
		"\u0000\u0000\u0000\u0089\u0087\u0001\u0000\u0000\u0000\u008a\u00d8\u0001"+
		"\u0000\u0000\u0000\u008b\u008c\n\f\u0000\u0000\u008c\u008d\u0007\u0002"+
		"\u0000\u0000\u008d\u00d7\u0003\u0002\u0001\r\u008e\u008f\n\u000b\u0000"+
		"\u0000\u008f\u0090\u0007\u0003\u0000\u0000\u0090\u00d7\u0003\u0002\u0001"+
		"\f\u0091\u0099\n\n\u0000\u0000\u0092\u0093\u0005)\u0000\u0000\u0093\u009a"+
		"\u0005)\u0000\u0000\u0094\u0095\u0005(\u0000\u0000\u0095\u0096\u0005("+
		"\u0000\u0000\u0096\u009a\u0005(\u0000\u0000\u0097\u0098\u0005(\u0000\u0000"+
		"\u0098\u009a\u0005(\u0000\u0000\u0099\u0092\u0001\u0000\u0000\u0000\u0099"+
		"\u0094\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a"+
		"\u009b\u0001\u0000\u0000\u0000\u009b\u00d7\u0003\u0002\u0001\u000b\u009c"+
		"\u009d\n\t\u0000\u0000\u009d\u009e\u0007\u0004\u0000\u0000\u009e\u00d7"+
		"\u0003\u0002\u0001\n\u009f\u00a0\n\u0007\u0000\u0000\u00a0\u00a1\u0007"+
		"\u0005\u0000\u0000\u00a1\u00d7\u0003\u0002\u0001\b\u00a2\u00a3\n\u0006"+
		"\u0000\u0000\u00a3\u00a4\u00059\u0000\u0000\u00a4\u00d7\u0003\u0002\u0001"+
		"\u0007\u00a5\u00a6\n\u0005\u0000\u0000\u00a6\u00a7\u0005;\u0000\u0000"+
		"\u00a7\u00d7\u0003\u0002\u0001\u0006\u00a8\u00a9\n\u0004\u0000\u0000\u00a9"+
		"\u00aa\u0005:\u0000\u0000\u00aa\u00d7\u0003\u0002\u0001\u0005\u00ab\u00ac"+
		"\n\u0003\u0000\u0000\u00ac\u00ad\u00051\u0000\u0000\u00ad\u00d7\u0003"+
		"\u0002\u0001\u0004\u00ae\u00af\n\u0002\u0000\u0000\u00af\u00b0\u00052"+
		"\u0000\u0000\u00b0\u00d7\u0003\u0002\u0001\u0003\u00b1\u00b2\n\u0001\u0000"+
		"\u0000\u00b2\u00b3\u0005,\u0000\u0000\u00b3\u00b4\u0003\u0002\u0001\u0000"+
		"\u00b4\u00b5\u0005-\u0000\u0000\u00b5\u00b6\u0003\u0002\u0001\u0001\u00b6"+
		"\u00d7\u0001\u0000\u0000\u0000\u00b7\u00b8\n\u0017\u0000\u0000\u00b8\u00c4"+
		"\u0005&\u0000\u0000\u00b9\u00c5\u00032\u0019\u0000\u00ba\u00c5\u0003 "+
		"\u0010\u0000\u00bb\u00c5\u0005\u0003\u0000\u0000\u00bc\u00be\u0005F\u0000"+
		"\u0000\u00bd\u00bf\u0003P(\u0000\u00be\u00bd\u0001\u0000\u0000\u0000\u00be"+
		"\u00bf\u0001\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0"+
		"\u00c5\u0003\f\u0006\u0000\u00c1\u00c2\u0005E\u0000\u0000\u00c2\u00c5"+
		"\u0003&\u0013\u0000\u00c3\u00c5\u0003\u0012\t\u0000\u00c4\u00b9\u0001"+
		"\u0000\u0000\u0000\u00c4\u00ba\u0001\u0000\u0000\u0000\u00c4\u00bb\u0001"+
		"\u0000\u0000\u0000\u00c4\u00bc\u0001\u0000\u0000\u0000\u00c4\u00c1\u0001"+
		"\u0000\u0000\u0000\u00c4\u00c3\u0001\u0000\u0000\u0000\u00c5\u00d7\u0001"+
		"\u0000\u0000\u0000\u00c6\u00c7\n\u0016\u0000\u0000\u00c7\u00c8\u0005\""+
		"\u0000\u0000\u00c8\u00c9\u0003\u0002\u0001\u0000\u00c9\u00ca\u0005#\u0000"+
		"\u0000\u00ca\u00d7\u0001\u0000\u0000\u0000\u00cb\u00cc\n\u0013\u0000\u0000"+
		"\u00cc\u00cd\u0005\u0002\u0000\u0000\u00cd\u00d7\u0005D\u0000\u0000\u00ce"+
		"\u00cf\n\u000f\u0000\u0000\u00cf\u00d7\u0007\u0006\u0000\u0000\u00d0\u00d1"+
		"\n\b\u0000\u0000\u00d1\u00d4\u0005\u0004\u0000\u0000\u00d2\u00d5\u0003"+
		"H$\u0000\u00d3\u00d5\u0003\b\u0004\u0000\u00d4\u00d2\u0001\u0000\u0000"+
		"\u0000\u00d4\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d7\u0001\u0000\u0000"+
		"\u0000\u00d6\u008b\u0001\u0000\u0000\u0000\u00d6\u008e\u0001\u0000\u0000"+
		"\u0000\u00d6\u0091\u0001\u0000\u0000\u0000\u00d6\u009c\u0001\u0000\u0000"+
		"\u0000\u00d6\u009f\u0001\u0000\u0000\u0000\u00d6\u00a2\u0001\u0000\u0000"+
		"\u0000\u00d6\u00a5\u0001\u0000\u0000\u0000\u00d6\u00a8\u0001\u0000\u0000"+
		"\u0000\u00d6\u00ab\u0001\u0000\u0000\u0000\u00d6\u00ae\u0001\u0000\u0000"+
		"\u0000\u00d6\u00b1\u0001\u0000\u0000\u0000\u00d6\u00b7\u0001\u0000\u0000"+
		"\u0000\u00d6\u00c6\u0001\u0000\u0000\u0000\u00d6\u00cb\u0001\u0000\u0000"+
		"\u0000\u00d6\u00ce\u0001\u0000\u0000\u0000\u00d6\u00d0\u0001\u0000\u0000"+
		"\u0000\u00d7\u00da\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000\u0000"+
		"\u0000\u00d8\u00d9\u0001\u0000\u0000\u0000\u00d9\u0003\u0001\u0000\u0000"+
		"\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00db\u00dc\u0005\u0001\u0000"+
		"\u0000\u00dc\u00dd\u0005\u001e\u0000\u0000\u00dd\u00de\u0003\u0002\u0001"+
		"\u0000\u00de\u00df\u0005%\u0000\u0000\u00df\u00e0\u0003\u0002\u0001\u0000"+
		"\u00e0\u00e1\u0005\u001f\u0000\u0000\u00e1\u0005\u0001\u0000\u0000\u0000"+
		"\u00e2\u00e4\u0005\"\u0000\u0000\u00e3\u00e5\u0003\"\u0011\u0000\u00e4"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e4\u00e5\u0001\u0000\u0000\u0000\u00e5"+
		"\u00e6\u0001\u0000\u0000\u0000\u00e6\u00e7\u0005#\u0000\u0000\u00e7\u0007"+
		"\u0001\u0000\u0000\u0000\u00e8\u00ea\u0003\n\u0005\u0000\u00e9\u00e8\u0001"+
		"\u0000\u0000\u0000\u00ea\u00ed\u0001\u0000\u0000\u0000\u00eb\u00e9\u0001"+
		"\u0000\u0000\u0000\u00eb\u00ec\u0001\u0000\u0000\u0000\u00ec\u00ee\u0001"+
		"\u0000\u0000\u0000\u00ed\u00eb\u0001\u0000\u0000\u0000\u00ee\u00f2\u0003"+
		"H$\u0000\u00ef\u00f1\u0003B!\u0000\u00f0\u00ef\u0001\u0000\u0000\u0000"+
		"\u00f1\u00f4\u0001\u0000\u0000\u0000\u00f2\u00f0\u0001\u0000\u0000\u0000"+
		"\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00f5\u0001\u0000\u0000\u0000"+
		"\u00f4\u00f2\u0001\u0000\u0000\u0000\u00f5\u00f6\u00032\u0019\u0000\u00f6"+
		"\t\u0001\u0000\u0000\u0000\u00f7\u00fa\u0005G\u0000\u0000\u00f8\u00fa"+
		"\u0003B!\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00f9\u00f8\u0001\u0000"+
		"\u0000\u0000\u00fa\u000b\u0001\u0000\u0000\u0000\u00fb\u00fd\u00032\u0019"+
		"\u0000\u00fc\u00fe\u0003\u0010\b\u0000\u00fd\u00fc\u0001\u0000\u0000\u0000"+
		"\u00fd\u00fe\u0001\u0000\u0000\u0000\u00fe\u00ff\u0001\u0000\u0000\u0000"+
		"\u00ff\u0100\u0003\u0014\n\u0000\u0100\r\u0001\u0000\u0000\u0000\u0101"+
		"\u0102\u0003P(\u0000\u0102\u0103\u0003\u001c\u000e\u0000\u0103\u0104\u0003"+
		"\u0014\n\u0000\u0104\u010b\u0001\u0000\u0000\u0000\u0105\u0108\u0003\u001c"+
		"\u000e\u0000\u0106\u0109\u0003\u0016\u000b\u0000\u0107\u0109\u0003\u0014"+
		"\n\u0000\u0108\u0106\u0001\u0000\u0000\u0000\u0108\u0107\u0001\u0000\u0000"+
		"\u0000\u0109\u010b\u0001\u0000\u0000\u0000\u010a\u0101\u0001\u0000\u0000"+
		"\u0000\u010a\u0105\u0001\u0000\u0000\u0000\u010b\u000f\u0001\u0000\u0000"+
		"\u0000\u010c\u010d\u0005)\u0000\u0000\u010d\u0110\u0005(\u0000\u0000\u010e"+
		"\u0110\u0003P(\u0000\u010f\u010c\u0001\u0000\u0000\u0000\u010f\u010e\u0001"+
		"\u0000\u0000\u0000\u0110\u0011\u0001\u0000\u0000\u0000\u0111\u0112\u0003"+
		"P(\u0000\u0112\u0113\u0003$\u0012\u0000\u0113\u0013\u0001\u0000\u0000"+
		"\u0000\u0114\u0115\u0003(\u0014\u0000\u0115\u0015\u0001\u0000\u0000\u0000"+
		"\u0116\u0132\u0005\"\u0000\u0000\u0117\u011c\u0005#\u0000\u0000\u0118"+
		"\u0119\u0005\"\u0000\u0000\u0119\u011b\u0005#\u0000\u0000\u011a\u0118"+
		"\u0001\u0000\u0000\u0000\u011b\u011e\u0001\u0000\u0000\u0000\u011c\u011a"+
		"\u0001\u0000\u0000\u0000\u011c\u011d\u0001\u0000\u0000\u0000\u011d\u011f"+
		"\u0001\u0000\u0000\u0000\u011e\u011c\u0001\u0000\u0000\u0000\u011f\u0133"+
		"\u0003\u0018\f\u0000\u0120\u0121\u0003\u0002\u0001\u0000\u0121\u0128\u0005"+
		"#\u0000\u0000\u0122\u0123\u0005\"\u0000\u0000\u0123\u0124\u0003\u0002"+
		"\u0001\u0000\u0124\u0125\u0005#\u0000\u0000\u0125\u0127\u0001\u0000\u0000"+
		"\u0000\u0126\u0122\u0001\u0000\u0000\u0000\u0127\u012a\u0001\u0000\u0000"+
		"\u0000\u0128\u0126\u0001\u0000\u0000\u0000\u0128\u0129\u0001\u0000\u0000"+
		"\u0000\u0129\u012f\u0001\u0000\u0000\u0000\u012a\u0128\u0001\u0000\u0000"+
		"\u0000\u012b\u012c\u0005\"\u0000\u0000\u012c\u012e\u0005#\u0000\u0000"+
		"\u012d\u012b\u0001\u0000\u0000\u0000\u012e\u0131\u0001\u0000\u0000\u0000"+
		"\u012f\u012d\u0001\u0000\u0000\u0000\u012f\u0130\u0001\u0000\u0000\u0000"+
		"\u0130\u0133\u0001\u0000\u0000\u0000\u0131\u012f\u0001\u0000\u0000\u0000"+
		"\u0132\u0117\u0001\u0000\u0000\u0000\u0132\u0120\u0001\u0000\u0000\u0000"+
		"\u0133\u0017\u0001\u0000\u0000\u0000\u0134\u0140\u0005 \u0000\u0000\u0135"+
		"\u013a\u0003\u001a\r\u0000\u0136\u0137\u0005%\u0000\u0000\u0137\u0139"+
		"\u0003\u001a\r\u0000\u0138\u0136\u0001\u0000\u0000\u0000\u0139\u013c\u0001"+
		"\u0000\u0000\u0000\u013a\u0138\u0001\u0000\u0000\u0000\u013a\u013b\u0001"+
		"\u0000\u0000\u0000\u013b\u013e\u0001\u0000\u0000\u0000\u013c\u013a\u0001"+
		"\u0000\u0000\u0000\u013d\u013f\u0005%\u0000\u0000\u013e\u013d\u0001\u0000"+
		"\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0141\u0001\u0000"+
		"\u0000\u0000\u0140\u0135\u0001\u0000\u0000\u0000\u0140\u0141\u0001\u0000"+
		"\u0000\u0000\u0141\u0142\u0001\u0000\u0000\u0000\u0142\u0143\u0005!\u0000"+
		"\u0000\u0143\u0019\u0001\u0000\u0000\u0000\u0144\u0147\u0003\u0018\f\u0000"+
		"\u0145\u0147\u0003\u0002\u0001\u0000\u0146\u0144\u0001\u0000\u0000\u0000"+
		"\u0146\u0145\u0001\u0000\u0000\u0000\u0147\u001b\u0001\u0000\u0000\u0000"+
		"\u0148\u014a\u00032\u0019\u0000\u0149\u014b\u0003\u001e\u000f\u0000\u014a"+
		"\u0149\u0001\u0000\u0000\u0000\u014a\u014b\u0001\u0000\u0000\u0000\u014b"+
		"\u0153\u0001\u0000\u0000\u0000\u014c\u014d\u0005&\u0000\u0000\u014d\u014f"+
		"\u00032\u0019\u0000\u014e\u0150\u0003\u001e\u000f\u0000\u014f\u014e\u0001"+
		"\u0000\u0000\u0000\u014f\u0150\u0001\u0000\u0000\u0000\u0150\u0152\u0001"+
		"\u0000\u0000\u0000\u0151\u014c\u0001\u0000\u0000\u0000\u0152\u0155\u0001"+
		"\u0000\u0000\u0000\u0153\u0151\u0001\u0000\u0000\u0000\u0153\u0154\u0001"+
		"\u0000\u0000\u0000\u0154\u0158\u0001\u0000\u0000\u0000\u0155\u0153\u0001"+
		"\u0000\u0000\u0000\u0156\u0158\u0003N\'\u0000\u0157\u0148\u0001\u0000"+
		"\u0000\u0000\u0157\u0156\u0001\u0000\u0000\u0000\u0158\u001d\u0001\u0000"+
		"\u0000\u0000\u0159\u015a\u0005)\u0000\u0000\u015a\u015d\u0005(\u0000\u0000"+
		"\u015b\u015d\u0003D\"\u0000\u015c\u0159\u0001\u0000\u0000\u0000\u015c"+
		"\u015b\u0001\u0000\u0000\u0000\u015d\u001f\u0001\u0000\u0000\u0000\u015e"+
		"\u015f\u00032\u0019\u0000\u015f\u0161\u0005\u001e\u0000\u0000\u0160\u0162"+
		"\u0003\"\u0011\u0000\u0161\u0160\u0001\u0000\u0000\u0000\u0161\u0162\u0001"+
		"\u0000\u0000\u0000\u0162\u0163\u0001\u0000\u0000\u0000\u0163\u0164\u0005"+
		"\u001f\u0000\u0000\u0164\u0172\u0001\u0000\u0000\u0000\u0165\u0166\u0005"+
		"\u0003\u0000\u0000\u0166\u0168\u0005\u001e\u0000\u0000\u0167\u0169\u0003"+
		"\"\u0011\u0000\u0168\u0167\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000"+
		"\u0000\u0000\u0169\u016a\u0001\u0000\u0000\u0000\u016a\u0172\u0005\u001f"+
		"\u0000\u0000\u016b\u016c\u0005E\u0000\u0000\u016c\u016e\u0005\u001e\u0000"+
		"\u0000\u016d\u016f\u0003\"\u0011\u0000\u016e\u016d\u0001\u0000\u0000\u0000"+
		"\u016e\u016f\u0001\u0000\u0000\u0000\u016f\u0170\u0001\u0000\u0000\u0000"+
		"\u0170\u0172\u0005\u001f\u0000\u0000\u0171\u015e\u0001\u0000\u0000\u0000"+
		"\u0171\u0165\u0001\u0000\u0000\u0000\u0171\u016b\u0001\u0000\u0000\u0000"+
		"\u0172!\u0001\u0000\u0000\u0000\u0173\u0178\u0003\u0002\u0001\u0000\u0174"+
		"\u0175\u0005%\u0000\u0000\u0175\u0177\u0003\u0002\u0001\u0000\u0176\u0174"+
		"\u0001\u0000\u0000\u0000\u0177\u017a\u0001\u0000\u0000\u0000\u0178\u0176"+
		"\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000\u0000\u0179#\u0001"+
		"\u0000\u0000\u0000\u017a\u0178\u0001\u0000\u0000\u0000\u017b\u017c\u0005"+
		"E\u0000\u0000\u017c\u0181\u0003&\u0013\u0000\u017d\u017e\u00032\u0019"+
		"\u0000\u017e\u017f\u0003(\u0014\u0000\u017f\u0181\u0001\u0000\u0000\u0000"+
		"\u0180\u017b\u0001\u0000\u0000\u0000\u0180\u017d\u0001\u0000\u0000\u0000"+
		"\u0181%\u0001\u0000\u0000\u0000\u0182\u018c\u0003(\u0014\u0000\u0183\u0185"+
		"\u0005&\u0000\u0000\u0184\u0186\u0003D\"\u0000\u0185\u0184\u0001\u0000"+
		"\u0000\u0000\u0185\u0186\u0001\u0000\u0000\u0000\u0186\u0187\u0001\u0000"+
		"\u0000\u0000\u0187\u0189\u00032\u0019\u0000\u0188\u018a\u0003(\u0014\u0000"+
		"\u0189\u0188\u0001\u0000\u0000\u0000\u0189\u018a\u0001\u0000\u0000\u0000"+
		"\u018a\u018c\u0001\u0000\u0000\u0000\u018b\u0182\u0001\u0000\u0000\u0000"+
		"\u018b\u0183\u0001\u0000\u0000\u0000\u018c\'\u0001\u0000\u0000\u0000\u018d"+
		"\u018f\u0005\u001e\u0000\u0000\u018e\u0190\u0003\"\u0011\u0000\u018f\u018e"+
		"\u0001\u0000\u0000\u0000\u018f\u0190\u0001\u0000\u0000\u0000\u0190\u0191"+
		"\u0001\u0000\u0000\u0000\u0191\u0192\u0005\u001f\u0000\u0000\u0192)\u0001"+
		"\u0000\u0000\u0000\u0193\u019c\u0005 \u0000\u0000\u0194\u0199\u0003<\u001e"+
		"\u0000\u0195\u0196\u0005%\u0000\u0000\u0196\u0198\u0003<\u001e\u0000\u0197"+
		"\u0195\u0001\u0000\u0000\u0000\u0198\u019b\u0001\u0000\u0000\u0000\u0199"+
		"\u0197\u0001\u0000\u0000\u0000\u0199\u019a\u0001\u0000\u0000\u0000\u019a"+
		"\u019d\u0001\u0000\u0000\u0000\u019b\u0199\u0001\u0000\u0000\u0000\u019c"+
		"\u0194\u0001\u0000\u0000\u0000\u019c\u019d\u0001\u0000\u0000\u0000\u019d"+
		"\u019f\u0001\u0000\u0000\u0000\u019e\u01a0\u0005%\u0000\u0000\u019f\u019e"+
		"\u0001\u0000\u0000\u0000\u019f\u01a0\u0001\u0000\u0000\u0000\u01a0\u01a1"+
		"\u0001\u0000\u0000\u0000\u01a1\u01a2\u0005!\u0000\u0000\u01a2+\u0001\u0000"+
		"\u0000\u0000\u01a3\u01ab\u0003.\u0017\u0000\u01a4\u01ab\u00030\u0018\u0000"+
		"\u01a5\u01ab\u0005\u001a\u0000\u0000\u01a6\u01ab\u0005\u001b\u0000\u0000"+
		"\u01a7\u01ab\u0005\u0019\u0000\u0000\u01a8\u01ab\u0005\u001d\u0000\u0000"+
		"\u01a9\u01ab\u0005\u001c\u0000\u0000\u01aa\u01a3\u0001\u0000\u0000\u0000"+
		"\u01aa\u01a4\u0001\u0000\u0000\u0000\u01aa\u01a5\u0001\u0000\u0000\u0000"+
		"\u01aa\u01a6\u0001\u0000\u0000\u0000\u01aa\u01a7\u0001\u0000\u0000\u0000"+
		"\u01aa\u01a8\u0001\u0000\u0000\u0000\u01aa\u01a9\u0001\u0000\u0000\u0000"+
		"\u01ab-\u0001\u0000\u0000\u0000\u01ac\u01ad\u0007\u0007\u0000\u0000\u01ad"+
		"/\u0001\u0000\u0000\u0000\u01ae\u01af\u0007\b\u0000\u0000\u01af1\u0001"+
		"\u0000\u0000\u0000\u01b0\u01b1\u0007\t\u0000\u0000\u01b13\u0001\u0000"+
		"\u0000\u0000\u01b2\u01b3\u0007\n\u0000\u0000\u01b35\u0001\u0000\u0000"+
		"\u0000\u01b4\u01b7\u0003H$\u0000\u01b5\u01b7\u0005\u000e\u0000\u0000\u01b6"+
		"\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b5\u0001\u0000\u0000\u0000\u01b7"+
		"7\u0001\u0000\u0000\u0000\u01b8\u01bd\u0003:\u001d\u0000\u01b9\u01ba\u0005"+
		"%\u0000\u0000\u01ba\u01bc\u0003:\u001d\u0000\u01bb\u01b9\u0001\u0000\u0000"+
		"\u0000\u01bc\u01bf\u0001\u0000\u0000\u0000\u01bd\u01bb\u0001\u0000\u0000"+
		"\u0000\u01bd\u01be\u0001\u0000\u0000\u0000\u01be9\u0001\u0000\u0000\u0000"+
		"\u01bf\u01bd\u0001\u0000\u0000\u0000\u01c0\u01c1\u00032\u0019\u0000\u01c1"+
		"\u01c2\u0005\'\u0000\u0000\u01c2\u01c3\u0003<\u001e\u0000\u01c3;\u0001"+
		"\u0000\u0000\u0000\u01c4\u01c8\u0003\u0002\u0001\u0000\u01c5\u01c8\u0003"+
		"B!\u0000\u01c6\u01c8\u0003*\u0015\u0000\u01c7\u01c4\u0001\u0000\u0000"+
		"\u0000\u01c7\u01c5\u0001\u0000\u0000\u0000\u01c7\u01c6\u0001\u0000\u0000"+
		"\u0000\u01c8=\u0001\u0000\u0000\u0000\u01c9\u01ce\u00032\u0019\u0000\u01ca"+
		"\u01cb\u0005&\u0000\u0000\u01cb\u01cd\u00032\u0019\u0000\u01cc\u01ca\u0001"+
		"\u0000\u0000\u0000\u01cd\u01d0\u0001\u0000\u0000\u0000\u01ce\u01cc\u0001"+
		"\u0000\u0000\u0000\u01ce\u01cf\u0001\u0000\u0000\u0000\u01cf?\u0001\u0000"+
		"\u0000\u0000\u01d0\u01ce\u0001\u0000\u0000\u0000\u01d1\u01d2\u00032\u0019"+
		"\u0000\u01d2\u01d3\u0005&\u0000\u0000\u01d3\u01d5\u0001\u0000\u0000\u0000"+
		"\u01d4\u01d1\u0001\u0000\u0000\u0000\u01d5\u01d8\u0001\u0000\u0000\u0000"+
		"\u01d6\u01d4\u0001\u0000\u0000\u0000\u01d6\u01d7\u0001\u0000\u0000\u0000"+
		"\u01d7\u01d9\u0001\u0000\u0000\u0000\u01d8\u01d6\u0001\u0000\u0000\u0000"+
		"\u01d9\u01da\u0005?\u0000\u0000\u01da\u01db\u00032\u0019\u0000\u01dbA"+
		"\u0001\u0000\u0000\u0000\u01dc\u01dd\u0005?\u0000\u0000\u01dd\u01e0\u0003"+
		">\u001f\u0000\u01de\u01e0\u0003@ \u0000\u01df\u01dc\u0001\u0000\u0000"+
		"\u0000\u01df\u01de\u0001\u0000\u0000\u0000\u01e0\u01e7\u0001\u0000\u0000"+
		"\u0000\u01e1\u01e4\u0005\u001e\u0000\u0000\u01e2\u01e5\u00038\u001c\u0000"+
		"\u01e3\u01e5\u0003<\u001e\u0000\u01e4\u01e2\u0001\u0000\u0000\u0000\u01e4"+
		"\u01e3\u0001\u0000\u0000\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5"+
		"\u01e6\u0001\u0000\u0000\u0000\u01e6\u01e8\u0005\u001f\u0000\u0000\u01e7"+
		"\u01e1\u0001\u0000\u0000\u0000\u01e7\u01e8\u0001\u0000\u0000\u0000\u01e8"+
		"C\u0001\u0000\u0000\u0000\u01e9\u01ea\u0005)\u0000\u0000\u01ea\u01ef\u0003"+
		"H$\u0000\u01eb\u01ec\u0005%\u0000\u0000\u01ec\u01ee\u0003H$\u0000\u01ed"+
		"\u01eb\u0001\u0000\u0000\u0000\u01ee\u01f1\u0001\u0000\u0000\u0000\u01ef"+
		"\u01ed\u0001\u0000\u0000\u0000\u01ef\u01f0\u0001\u0000\u0000\u0000\u01f0"+
		"\u01f2\u0001\u0000\u0000\u0000\u01f1\u01ef\u0001\u0000\u0000\u0000\u01f2"+
		"\u01f3\u0005(\u0000\u0000\u01f3E\u0001\u0000\u0000\u0000\u01f4\u01f6\u0003"+
		">\u001f\u0000\u01f5\u01f7\u0003D\"\u0000\u01f6\u01f5\u0001\u0000\u0000"+
		"\u0000\u01f6\u01f7\u0001\u0000\u0000\u0000\u01f7G\u0001\u0000\u0000\u0000"+
		"\u01f8\u01f9\u0006$\uffff\uffff\u0000\u01f9\u020c\u0005\u0006\u0000\u0000"+
		"\u01fa\u020c\u0005\u0005\u0000\u0000\u01fb\u020c\u0003N\'\u0000\u01fc"+
		"\u020c\u0003J%\u0000\u01fd\u01fe\u0005\"\u0000\u0000\u01fe\u01ff\u0003"+
		"H$\u0000\u01ff\u0200\u0005%\u0000\u0000\u0200\u0201\u0003H$\u0000\u0201"+
		"\u0202\u0005#\u0000\u0000\u0202\u020c\u0001\u0000\u0000\u0000\u0203\u0205"+
		"\u0005\u001e\u0000\u0000\u0204\u0206\u0003R)\u0000\u0205\u0204\u0001\u0000"+
		"\u0000\u0000\u0205\u0206\u0001\u0000\u0000\u0000\u0206\u0207\u0001\u0000"+
		"\u0000\u0000\u0207\u0208\u0005\u001f\u0000\u0000\u0208\u0209\u0005=\u0000"+
		"\u0000\u0209\u020c\u0003H$\u0003\u020a\u020c\u0003F#\u0000\u020b\u01f8"+
		"\u0001\u0000\u0000\u0000\u020b\u01fa\u0001\u0000\u0000\u0000\u020b\u01fb"+
		"\u0001\u0000\u0000\u0000\u020b\u01fc\u0001\u0000\u0000\u0000\u020b\u01fd"+
		"\u0001\u0000\u0000\u0000\u020b\u0203\u0001\u0000\u0000\u0000\u020b\u020a"+
		"\u0001\u0000\u0000\u0000\u020c\u021f\u0001\u0000\u0000\u0000\u020d\u0210"+
		"\n\u0006\u0000\u0000\u020e\u020f\u0005:\u0000\u0000\u020f\u0211\u0003"+
		"H$\u0000\u0210\u020e\u0001\u0000\u0000\u0000\u0211\u0212\u0001\u0000\u0000"+
		"\u0000\u0212\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000\u0000"+
		"\u0000\u0213\u021e\u0001\u0000\u0000\u0000\u0214\u0217\n\u0005\u0000\u0000"+
		"\u0215\u0216\u00059\u0000\u0000\u0216\u0218\u0003H$\u0000\u0217\u0215"+
		"\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000\u0000\u0000\u0219\u0217"+
		"\u0001\u0000\u0000\u0000\u0219\u021a\u0001\u0000\u0000\u0000\u021a\u021e"+
		"\u0001\u0000\u0000\u0000\u021b\u021c\n\u0002\u0000\u0000\u021c\u021e\u0003"+
		"L&\u0000\u021d\u020d\u0001\u0000\u0000\u0000\u021d\u0214\u0001\u0000\u0000"+
		"\u0000\u021d\u021b\u0001\u0000\u0000\u0000\u021e\u0221\u0001\u0000\u0000"+
		"\u0000\u021f\u021d\u0001\u0000\u0000\u0000\u021f\u0220\u0001\u0000\u0000"+
		"\u0000\u0220I\u0001\u0000\u0000\u0000\u0221\u021f\u0001\u0000\u0000\u0000"+
		"\u0222\u0223\u0005\u001e\u0000\u0000\u0223\u0224\u0003H$\u0000\u0224\u0225"+
		"\u0005\u001f\u0000\u0000\u0225K\u0001\u0000\u0000\u0000\u0226\u022c\u0005"+
		"\u0010\u0000\u0000\u0227\u0228\u0005\"\u0000\u0000\u0228\u022c\u0005#"+
		"\u0000\u0000\u0229\u022c\u0005\u0011\u0000\u0000\u022a\u022c\u0005\u0012"+
		"\u0000\u0000\u022b\u0226\u0001\u0000\u0000\u0000\u022b\u0227\u0001\u0000"+
		"\u0000\u0000\u022b\u0229\u0001\u0000\u0000\u0000\u022b\u022a\u0001\u0000"+
		"\u0000\u0000\u022cM\u0001\u0000\u0000\u0000\u022d\u022e\u0007\u000b\u0000"+
		"\u0000\u022eO\u0001\u0000\u0000\u0000\u022f\u0230\u0005)\u0000\u0000\u0230"+
		"\u0231\u0003R)\u0000\u0231\u0232\u0005(\u0000\u0000\u0232Q\u0001\u0000"+
		"\u0000\u0000\u0233\u0238\u0003H$\u0000\u0234\u0235\u0005%\u0000\u0000"+
		"\u0235\u0237\u0003H$\u0000\u0236\u0234\u0001\u0000\u0000\u0000\u0237\u023a"+
		"\u0001\u0000\u0000\u0000\u0238\u0236\u0001\u0000\u0000\u0000\u0238\u0239"+
		"\u0001\u0000\u0000\u0000\u0239S\u0001\u0000\u0000\u0000\u023a\u0238\u0001"+
		"\u0000\u0000\u0000@dfw\u007f\u0089\u0099\u00be\u00c4\u00d4\u00d6\u00d8"+
		"\u00e4\u00eb\u00f2\u00f9\u00fd\u0108\u010a\u010f\u011c\u0128\u012f\u0132"+
		"\u013a\u013e\u0140\u0146\u014a\u014f\u0153\u0157\u015c\u0161\u0168\u016e"+
		"\u0171\u0178\u0180\u0185\u0189\u018b\u018f\u0199\u019c\u019f\u01aa\u01b6"+
		"\u01bd\u01c7\u01ce\u01d6\u01df\u01e4\u01e7\u01ef\u01f6\u0205\u020b\u0212"+
		"\u0219\u021d\u021f\u022b\u0238";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}