package org.marioarias.monkey.vm

import org.marioarias.monkey.checkType
import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.objects.*
import org.marioarias.monkey.parse
import org.marioarias.monkey.testIntegerObject
import org.marioarias.monkey.testStringObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class VMTests {
    data class VTC<T>(val input: String, val expected: T)

    @Test
    fun `integer arithmetic`() {
        listOf(
            VTC("1", 1),
            VTC("2", 2),
            VTC("1 + 2", 3),
            VTC("1 - 2", -1),
            VTC("1 * 2", 2),
            VTC("4 / 2", 2),
            VTC("50 / 2 * 2 + 10 - 5", 55),
            VTC("5 + 5 + 5 + 5 - 10", 10),
            VTC("2 * 2 * 2 * 2 * 2", 32),
            VTC("5 * 2 + 10", 20),
            VTC("5 + 2 * 10", 25),
            VTC("5 * (2 + 10)", 60),
            VTC("-5", -5),
            VTC("-10", -10),
            VTC("-50 + 100 + - 50", 0),
            VTC("(5 + 10 * 2 + 15  / 3) * 2 + - 10", 50),
        ).runVmTests()
    }

    @Test
    fun `boolean expression`() {
        listOf(
            VTC("true", true),
            VTC("false", false),
            VTC("1 < 2", true),
            VTC("1 > 2", false),
            VTC("1 > 1", false),
            VTC("1 == 1", true),
            VTC("1 != 1", false),
            VTC("1 == 2", false),
            VTC("1 != 2", true),
            VTC("true == true", true),
            VTC("false == false", true),
            VTC("true == false", false),
            VTC("true != false", true),
            VTC("false != true", true),
            VTC("(1 < 2) == true", true),
            VTC("(1 < 2) == false", false),
            VTC("(1 > 2) == true", false),
            VTC("(1 > 2) == false", true),
            VTC("!true", false),
            VTC("!false", true),
            VTC("!5", false),
            VTC("!!true", true),
            VTC("!!false", false),
            VTC("!!5", true),
            VTC("!(if (false) { 5; })", true),
        ).runVmTests()
    }

    @Test
    fun conditional() {
        listOf<VTC<Any>>(
            VTC("if (true) {10}", 10),
            VTC("if (true) {10} else {20}", 10),
            VTC("if (false) {10} else {20}", 20),
            VTC("if (1) {10}", 10),
            VTC("if (1 < 2) {10}", 10),
            VTC("if (1 < 2) {10} else {20}", 10),
            VTC("if (1 > 2) {10} else {20}", 20),
            VTC("if (1 > 2) {10}", Null),
            VTC("if (false) {10}", Null),
            VTC("if ((if (false) {10})) {10} else {20}", 20),
        ).runVmTests()
    }

    @Test
    fun `global let statements`() {
        listOf(
            VTC("let one = 1; one;", 1),
            VTC("let one = 1; let two = 2; one + two", 3),
            VTC("let one = 1; let two = one + one; one + two", 3),
        ).runVmTests()
    }

    @Test
    fun `string expressions`() {
        listOf(
            VTC(""" "monkey" """, "monkey"),
            VTC(""" "mon" + "key" """, "monkey"),
            VTC(""" "mon" + "key" + "banana" """, "monkeybanana"),
        ).runVmTests()
    }

    @Test
    fun `array literals`() {
        listOf(
            VTC("[]", emptyList()),
            VTC("[1, 2, 3]", listOf(1L, 2L, 3L)),
            VTC("[1 + 2, 3 * 4, 5 + 6]", listOf(3L, 12L, 11L)),
        ).runVmTests()
    }

    @Test
    fun `hash literals`() {
        listOf(
            VTC("{}", emptyMap()),
            VTC(
                "{1: 2, 2: 3}", mapOf(
                    MInteger(1).hashKey() to 2L,
                    MInteger(2).hashKey() to 3L
                )
            ),
            VTC(
                "{1 + 1: 2 * 2, 3 + 3: 4 * 4}", mapOf(
                    MInteger(2).hashKey() to 4L,
                    MInteger(6).hashKey() to 16L
                )
            ),
        ).runVmTests()
    }

    @Test
    fun `index expressions`() {
        listOf(
            VTC("[1, 2, 3][1]", 2),
            VTC("[1, 2, 3][0 + 2]", 3),
            VTC("[[1, 1, 1]][0][0]", 1),
            VTC("[][0]", Null),
            VTC("[1, 2, 3][99]", Null),
            VTC("[1][-1]", Null),
            VTC("{1: 1, 2: 2}[1]", 1),
            VTC("{1: 1, 2: 2}[2]", 2),
            VTC("{1: 1}[0]", Null),
            VTC("{}[0]", Null),
        ).runVmTests()
    }

    @Test
    fun `calling functions without arguments`() {
        listOf(
            VTC(
                """
         let fivePlusTen = fn() {5 + 10; };
         fivePlusTen()
         """, 15
            ),
            VTC(
                """
         let one = fn() { 1; }
         let two = fn() { 2; }
         one() + two()
         """, 3
            ),
            VTC(
                """
                        let a = fn() { 1 };
        let b = fn () { a() + 1 };
        let c = fn () { b() + 1 };
        c();
        """,
                3,
            ),
        ).runVmTests()
    }

    @Test
    fun `functions with return statement`() {
        listOf(
            VTC(
                """
            let earlyExit = fn() { return 99; 100; };
            earlyExit();
            """,
                99,
            ),
            VTC(
                """
            let earlyExit = fn() { return 99; return 100; };
            earlyExit();
            """",
                99,
            )
        ).runVmTests()
    }

    @Test
    fun `functions without return value`() {
        listOf(
            VTC(
                """
            let noReturn = fn() {};
            noReturn();            
            """,
                Null,
            ),
            VTC(
                """
            let noReturn = fn() {};
            let noReturnTwo = fn() { noReturn(); };
            noReturn();
            noReturnTwo();            
            """,
                Null,
            ),
        ).runVmTests()
    }

    @Test
    fun `first class functions`() {
        listOf(
            VTC(
                """
            let returnsOne = fn(){ 1;};
            let returnsOneReturner = fn() {returnsOne;}
            returnsOneReturner()();
            """,
                1,
            ),
            VTC(
                """
            let returnsOneReturner = fn() {
            	let returnsOne = fn() {
            		1;
            	};
            	returnsOne;
            }
            returnsOneReturner()();
            """,

                1,
            ),
        ).runVmTests()
    }

    @Test
    fun `calling functions with bindings`() {
        listOf(
            VTC(
                """
            let one = fn() { let one = 1; one;};
            one();
            """,
                1,
            ),
            VTC(
                """
            let oneAndTwo = fn() {
            	let one = 1;
            	let two = 2;
            	one + two;
            };
            oneAndTwo();
            """,
                3,
            ),
            VTC(
                """
            let oneAndTwo = fn() {
            	let one = 1;
            	let two = 2;
            	one + two;
            };
            let threeAndFour = fn() {
            	let three = 3;
            	let four = 4;
            	three + four;
            };
            oneAndTwo() + threeAndFour();
            """,
                10,
            ),
            VTC(
                """
            let firstFoobar = fn() {
            	let foobar = 50;
            	foobar;
            };
            
            let secondFoobar = fn() {
            	let foobar = 100;
            	foobar;
            };
            firstFoobar() + secondFoobar();
            """,
                150,
            ),
            VTC(
                """
            let globalSeed = 50;
            let minusOne = fn() {
            	let num = 1;
            	globalSeed - num;
            };
            let minusTwo = fn() {
            	let num = 2;
            	globalSeed - num;
            };
            minusOne() + minusTwo();
            """,
                97,
            ),
        ).runVmTests()
    }

    @Test
    fun `calling functions with arguments and bindings`() {
        listOf(
            VTC(
                """
            let identity = fn(a) { a; };
            identity(4);
            """,
                4,
            ),
            VTC(
                """
            let sum = fn(a, b) { a + b; };
            sum(1, 2);
            """"""",
                3,
            ),
            VTC(
                """
            let sum = fn(a, b){
            	let c = a + b;
            	c;
            }
            sum(1, 2);
            """,
                3,
            ),
            VTC(
                """
            let sum = fn(a, b){
            	let c = a + b;
            	c;
            }
            sum(1, 2) + sum(3, 4);
            """,
                10,
            ),
            VTC(
                """
            let sum = fn(a, b){
            	let c = a + b;
            	c;
            }
            let outer = fn() {
            	sum(1, 2) + sum(3, 4);
            };
            outer();
            """,
                10,
            ),
            VTC(
                """
            let globalNum = 10;
            let sum = fn(a, b){
            	let c = a + b;
            	c + globalNum;
            }
            let outer = fn() {
            	sum(1, 2) + sum(3, 4) + globalNum;
            };
            outer() + globalNum;
            """,
                50,
            ),
        ).runVmTests()
    }

    @Test
    fun `calling functions with wrong arguments`() {
        listOf(
            VTC(
                "fn() {1;}(1);",
                "wrong number of arguments: want=0, got=1",
            ),
            VTC(
                "fn(a) {a;}();",
                "wrong number of arguments: want=1, got=0",
            ),
            VTC(
                "fn(a, b) {a + b;}(1);",
                "wrong number of arguments: want=2, got=1",
            ),
        ).forEach { (input, expected) ->
            val program = parse(input)
            val compiler = MCompiler()
            compiler.compile(program)

            val vm = VM(compiler.bytecode())

            try {
                vm.run()
                fail("expected VM error but resulted in none.")
            } catch (e: VMException) {
                assertEquals(expected, e.message)
            }
        }
    }

    @Test
    fun `builtins functions`() {
        listOf(
            VTC("len(\"\")", 0),
            VTC("len(\"four\")", 4),
            VTC("len(\"hello world\")", 11),
            VTC(
                "len(1)", MError(
                    "argument to `len` not supported, got MInteger",
                )
            ),
            VTC(
                "len(\"one\", \"two\")", MError(
                    "wrong number of arguments. got=2, want=1"
                )
            ),
            VTC("len([1, 2, 3])", 3),
            VTC("len([])", 0),
            VTC("puts(\"hello\", \"world!\")", Null),
            VTC("first([1, 2, 3])", 1),
            VTC("first([])", Null),
            VTC(
                "first(1)", MError(
                    "argument to `first` must be ARRAY, got MInteger"
                )
            ),
            VTC("last([1, 2, 3])", 3),
            VTC("last([])", Null),
            VTC(
                "last(1)", MError(
                    "argument to `last` must be ARRAY, got MInteger"
                )
            ),
            VTC("rest([1,2,3])", listOf(2.toLong(), 3.toLong())),
            VTC("rest([])", Null),
            VTC("push([], 1)", listOf(1.toLong())),
            VTC(
                "push(1, 1)", MError(
                    "argument to `push` must be ARRAY, got MInteger"
                )
            ),
        ).runVmTests()
    }

    @Test
    fun closures() {
        listOf(
            VTC(
                """
            let newClosure = fn(a) {
            	fn() {a; };
            };
            let closure = newClosure(99);
            closure();
            """,
                99,
            ),
            VTC(
                """
                let newAdder = fn (a, b) {
                fn(c) { a + b + c };
            };
                let adder = newAdder (1, 2);
                adder(8);
                """,
                11,
            ),
            VTC(
                """
                let newAdder = fn (a, b) {
                let c = a +b;
                fn(d) { c + d };
            };
                let adder = newAdder (1, 2);
                adder(8);
                """,
                11,
            ),
            VTC(
                """
                let newAdderOuter = fn (a, b) {
                let c = a +b;
                fn(d) {
                    let e = d +c;
                    fn(f) { e + f; };
                };
            };
                let newAdderInner = newAdderOuter (1, 2);
                let adder = newAdderInner (3);
                adder(8);
                """,
                14,
            ),
            VTC(
                """
                let a = 1;
                let newAdderOuter = fn (b) {
                    fn(c) {
                        fn(d) { a + b + c + d };
                    };
                };
                let newAdderInner = newAdderOuter (2);
                let adder = newAdderInner (3);
                adder(8);
                """,
                14,
            ),
            VTC(
                """
                let newClosure = fn (a, b) {
                let one = fn () { a; };
                let two = fn () { b; };
                fn() { one() + two(); };
            };
                let closure = newClosure (9, 90);
                closure();
                """,
                99,
            )
        ).runVmTests()
    }

    @Test
    fun `recursive functions`() {
        listOf(
            VTC(
                """
            let countDown = fn(x) {
            	if (x == 0) {
            		return 0;
            	} else {
            		countDown(x - 1);
            	};	
            }
            countDown(1);
            """,
                0,
            ),
            VTC(
                """
            let countDown = fn(x) {
            	if (x == 0) {
            		return 0;
            	} else {
            		countDown(x - 1);
            	};	
            }
            let wrapper = fn() {
            	countDown(1);
            };
            wrapper();
            """,
                0,
            ),
            VTC(
                """
            let wrapper = fn() {
            	let countDown = fn(x) {
            		if (x == 0) {
            			return 0;
            		} else {
            			countDown(x - 1);
            		};	
            	};
            	countDown(1);
            };
            wrapper();
            """,
                0
            )
        ).runVmTests()
    }

    @Test
    fun `recursive fibonacci`() {
        listOf(
            VTC(
                """
let fibonacci = fn(x) {
	if (x == 0) {
		return 0;	
	} else {
		if (x == 1) {
			return 1;
		} else {
			fibonacci(x - 1) + fibonacci(x - 2);
		}
	}
};
fibonacci(15);
                        
        """.trimIndent(), 610
            )
        ).runVmTests()
    }

    private fun <T> List<VTC<out T>>.runVmTests() {
        forEach { (input, expected) ->
            val program = parse(input)
            val compiler = MCompiler()
            compiler.compile(program)
            val vm = VM(compiler.bytecode())
            vm.run()
            val stackElem = vm.lastPoppedStackElem()!!
            testExpectObject(expected, stackElem)
        }
    }

    private fun <T> testExpectObject(expected: T, actual: MObject) {
        when (expected) {
            is Long -> testIntegerObject(expected, actual)
            is Boolean -> testBooleanObject(expected, actual)
            is MNull -> assertEquals(MNull, actual, "object is not Null")
            is String -> testStringObject(expected, actual)
            is List<*> -> checkType(actual) { array: MArray ->
                assertEquals(array.elements.size, expected.size)
                expected.forEachIndexed { i, any ->
                    testIntegerObject(any as Long, array.elements[i]!!)
                }
            }
            is Map<*, *> -> checkType(actual) { hash: MHash ->
                assertEquals(expected.size, hash.pairs.size)
                expected.forEach { (expectedKey, expectedValue) ->
                    val pair = hash.pairs[expectedKey]
                    assertNotNull(pair, "no pair for give key in pairs")
                    testIntegerObject(expectedValue as Long, pair.value)
                }
            }
            is MError -> checkType(actual) { error: MError ->
                assertEquals(error.message, expected.message)
            }
        }
    }

    private fun testBooleanObject(expected: Boolean, actual: MObject) {
        val value = (actual as MBoolean).value
        assertEquals(expected, value, "object has wrong value. got=$value, want=$expected")
    }
}