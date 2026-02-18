package org.manul.compiler;

import org.junit.Assert;
import org.manul.util.ApiNamedObject;

import java.util.List;

public class DualStackTest extends ManulTestBase {

    private static final String MNL = "manul/basics/dualstack/DualStackTest.mnl";

    private Object call(String method, List<Object> args) {
        return callMethod(ApiNamedObject.of("lab"), method, args);
    }

    // --- Group A: Integer arithmetic loops ---

    public void testFibonacci() {
        deploy(MNL);
        assertEquals(0, call("fibonacci", List.of(0)));
        assertEquals(1, call("fibonacci", List.of(1)));
        assertEquals(55, call("fibonacci", List.of(10)));
        assertEquals(6765, call("fibonacci", List.of(20)));
    }

    public void testSumOfSquares() {
        deploy(MNL);
        assertEquals(55, call("sumOfSquares", List.of(5)));
        assertEquals(385, call("sumOfSquares", List.of(10)));
    }

    public void testGcd() {
        deploy(MNL);
        assertEquals(6, call("gcd", List.of(48, 18)));
        assertEquals(25, call("gcd", List.of(100, 75)));
        assertEquals(1, call("gcd", List.of(17, 13)));
    }

    // --- Group B: Long arithmetic ---

    public void testLongFibonacci() {
        deploy(MNL);
        assertEquals(12586269025L, call("longFibonacci", List.of(50)));
    }

    public void testLongBitwiseOps() {
        deploy(MNL);
        // a=0xFF, b=0x0F: x=0xFF, y=0x0F, z=0xF0. (255+15)*240 = 64800
        assertEquals(64800L, call("longBitwiseOps", List.of(0xFF, 0x0F)));
    }

    public void testLongShifts() {
        deploy(MNL);
        // (1<<10) + (1>>10) + (1>>>10) = 1024 + 0 + 0 = 1024
        assertEquals(1024L, call("longShifts", List.of(1, 10)));
        // (-1<<2) + (-1>>2) + (-1>>>2)
        // -1L<<2 = -4, -1L>>2 = -1, -1L>>>2 = 4611686018427387903
        // sum = -4 + -1 + 4611686018427387903 = 4611686018427387898
        assertEquals(4611686018427387898L, call("longShifts", List.of(-1, 2)));
    }

    // --- Group C: Double arithmetic ---

    public void testNewtonsSquareRoot() {
        deploy(MNL);
        var result = (double) call("newtonsSquareRoot", List.of(2.0, 10));
        assertEquals(Math.sqrt(2.0), result, 1e-10);
    }

    public void testDotProduct() {
        deploy(MNL);
        assertEquals(11.0, call("dotProduct", List.of(1.0, 2.0, 3.0, 4.0)));
    }

    // --- Group D: Float arithmetic ---

    public void testFloatAccumulate() {
        deploy(MNL);
        // sum of i*0.5 for i=1..10 = 0.5+1.0+1.5+2.0+2.5+3.0+3.5+4.0+4.5+5.0 = 27.5
        assertEquals(27.5f, (float) call("floatAccumulate", List.of(10)), 0.01f);
    }

    // --- Group E: Type conversions ---

    public void testIntToLongToDouble() {
        deploy(MNL);
        assertEquals(42.5, call("intToLongToDouble", List.of(42)));
    }

    public void testDoubleToFloatToInt() {
        deploy(MNL);
        assertEquals(3, call("doubleToFloatToInt", List.of(3.14)));
    }

    public void testRoundTrip() {
        deploy(MNL);
        assertEquals(42, call("roundTrip", List.of(42)));
        assertEquals(0, call("roundTrip", List.of(0)));
        assertEquals(-7, call("roundTrip", List.of(-7)));
    }

    // --- Group F: Comparisons ---

    public void testIntCompareChain() {
        deploy(MNL);
        // 1<2, 2<3, 1<3 → 3
        assertEquals(3, call("intCompareChain", List.of(1, 2, 3)));
        // 3>2, 2>1, 3>1 → 0
        assertEquals(0, call("intCompareChain", List.of(3, 2, 1)));
        // 1<2, 2==2, 1<2 → 2
        assertEquals(2, call("intCompareChain", List.of(1, 2, 2)));
    }

    public void testLongCompare() {
        deploy(MNL);
        assertEquals(-1, call("longCompare", List.of(1, 2)));
        assertEquals(0, call("longCompare", List.of(2, 2)));
        assertEquals(1, call("longCompare", List.of(3, 2)));
    }

    public void testDoubleCompare() {
        deploy(MNL);
        assertEquals(-1, call("doubleCompare", List.of(1.0, 2.0)));
        assertEquals(0, call("doubleCompare", List.of(2.0, 2.0)));
        assertEquals(1, call("doubleCompare", List.of(3.0, 2.0)));
    }

    public void testFloatCompare() {
        deploy(MNL);
        assertEquals(-1, call("floatCompare", List.of(1.0, 2.0)));
        assertEquals(0, call("floatCompare", List.of(2.0, 2.0)));
        assertEquals(1, call("floatCompare", List.of(3.0, 2.0)));
    }

    // --- Group G: Negate ---

    public void testNegateInt() {
        deploy(MNL);
        assertEquals(-42, call("negateInt", List.of(42)));
        assertEquals(0, call("negateInt", List.of(0)));
    }

    public void testNegateLong() {
        deploy(MNL);
        assertEquals(-100L, call("negateLong", List.of(100)));
    }

    public void testNegateDouble() {
        deploy(MNL);
        assertEquals(-3.14, (double) call("negateDouble", List.of(3.14)), 0.0);
    }

    public void testNegateFloat() {
        deploy(MNL);
        assertEquals(-1.5f, (float) call("negateFloat", List.of(1.5)), 0.0f);
    }

    // --- Group H: Boolean tests ---

    public void testAllComparisons() {
        deploy(MNL);
        // a<b: ne(2) + lt(4) + le(8) = 14
        assertEquals(14, call("allComparisons", List.of(3, 5)));
        // a==b: eq(1) + le(8) + ge(32) = 41
        assertEquals(41, call("allComparisons", List.of(5, 5)));
        // a>b: ne(2) + gt(16) + ge(32) = 50
        assertEquals(50, call("allComparisons", List.of(7, 5)));
    }

    // --- Group I: Closures ---

    public void testClosureCapture() {
        deploy(MNL);
        // adder(5)=15, adder(20)=30 → 45
        assertEquals(45, call("testClosureCapture", List.of()));
    }

    // --- Group J: Array operations ---

    public void testArrayIntSum() {
        deploy(MNL);
        assertEquals(15, call("arrayIntSum", List.of(List.of(1, 2, 3, 4, 5))));
        assertEquals(0, call("arrayIntSum", List.of(List.of(0, 0, 0))));
    }

    public void testBubbleSortAndSum() {
        deploy(MNL);
        // [5,3,1,4,2] → sorted [1,2,3,4,5] → 1*1+2*2+3*3+4*4+5*5 = 55
        assertEquals(55, call("bubbleSortAndSum", List.of(List.of(5, 3, 1, 4, 2))));
    }

    // --- Group K: Nested calls ---

    public void testNestedArithmetic() {
        deploy(MNL);
        // add(mul(2,3), square(4)) = 6 + 16 = 22
        assertEquals(22, call("nestedArithmetic", List.of(2, 3, 4)));
    }

    public void testPower() {
        deploy(MNL);
        assertEquals(1, call("power", List.of(2, 0)));
        assertEquals(1024, call("power", List.of(2, 10)));
        assertEquals(59049, call("power", List.of(3, 10)));
    }

    // --- Group L: Exception handling ---

    public void testDivideWithCatch() {
        deploy(MNL);
        assertEquals(3, call("divideWithCatch", List.of(10, 3)));
        assertEquals(-1, call("divideWithCatch", List.of(10, 0)));
    }

    public void testLoopWithException() {
        deploy(MNL);
        // i=0: 100/(-5)=-20; i=1: 100/(-4)=-25; i=2: 100/(-3)=-33;
        // i=3: 100/(-2)=-50; i=4: 100/(-1)=-100; i=5: exception→999;
        // i=6: 100/1=100; i=7: 100/2=50; i=8: 100/3=33; i=9: 100/4=25
        // total = -20-25-33-50-100+999+100+50+33+25 = 979
        assertEquals(979, call("loopWithException", List.of(10)));
    }

    // --- Group M: DUP patterns ---

    public void testDupPattern() {
        deploy(MNL);
        // arr=[1,2,3,4,5], n=10
        // i=0: arr[0]=1+0=1, sum=1
        // i=1: arr[1]=2+1=3, sum=4
        // i=2: arr[2]=3+2=5, sum=9
        // i=3: arr[3]=4+3=7, sum=16
        // i=4: arr[4]=5+4=9, sum=25
        // i=5: arr[0]=1+5=6, sum=31
        // i=6: arr[1]=3+6=9, sum=40
        // i=7: arr[2]=5+7=12, sum=52
        // i=8: arr[3]=7+8=15, sum=67
        // i=9: arr[4]=9+9=18, sum=85
        assertEquals(85, call("testDupPattern", List.of(10)));
    }

    // --- Group N: Mixed primitive/object ---

    public void testStringLengthSum() {
        deploy(MNL);
        assertEquals(11, call("stringLengthSum", List.of(List.of("hello", "world", "!"))));
    }

    public void testIntToString() {
        deploy(MNL);
        assertEquals("42", call("intToString", List.of(42)));
        assertEquals("-7", call("intToString", List.of(-7)));
    }

    // --- Group O: Complex control flow ---

    public void testCollatz() {
        deploy(MNL);
        assertEquals(0, call("collatz", List.of(1)));
        assertEquals(111, call("collatz", List.of(27)));
    }

    public void testCountPrimes() {
        deploy(MNL);
        assertEquals(25, call("countPrimes", List.of(100)));
        assertEquals(4, call("countPrimes", List.of(10)));
    }

}
