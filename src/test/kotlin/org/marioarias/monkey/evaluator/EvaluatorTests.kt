package org.marioarias.monkey.evaluator

import org.marioarias.monkey.checkType
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.objects.*
import org.marioarias.monkey.parser.Parser
import org.testng.Assert
import org.testng.annotations.Test


class EvaluatorTests {

    data class TestData<T>(val input: String, val expected: T)


    private fun List<TestData<Int>>.test() {
        forEach { (input, expected) ->
            val evaluated = testEval(input)
            testObject<MInteger, Long>(evaluated, expected.toLong())
        }
    }

    @Test
    fun `eval integer expression`() {

        listOf(
            TestData("5", 5),
            TestData("10", 10),
            TestData("-5", -5),
            TestData("-10", -10),
            TestData("5 + 5 + 5 + 5 - 10", 10),
            TestData("2 * 2 * 2 * 2 * 2", 32),
            TestData("-50 + 100 + -50", 0),
            TestData("5 * 2 + 10", 20),
            TestData("5 + 2 * 10", 25),
            TestData("20 + 2 * -10", 0),
            TestData("50 / 2 * 2 + 10", 60),
            TestData("2 * (5 + 10)", 30),
            TestData("3 * 3 * 3 + 10", 37),
            TestData("3 * (3 * 3) + 10", 37),
            TestData("(5 + 10 * 2 + 15 / 3) * 2 + -10", 50),
        ).test()
    }

    @Test
    fun `eval boolean expression`() {
        val tests = listOf(
            TestData("true", true),
            TestData("false", false),
            TestData("1 < 2", true),
            TestData("1 > 2", false),
            TestData("1 < 1", false),
            TestData("1 > 1", false),
            TestData("1 == 1", true),
            TestData("1 != 1", false),
            TestData("1 == 2", false),
            TestData("1 != 2", true),
            TestData("true == true", true),
            TestData("false == false", true),
            TestData("true == false", false),
            TestData("true != false", true),
            TestData("false != true", true),
            TestData("(1 < 2) == true", true),
            TestData("(1 < 2) == false", false),
            TestData("(1 > 2) == true", false),
            TestData("(1 > 2) == false", true),
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            testObject<MBoolean, Boolean>(evaluated, expected)
        }
    }

    @Test
    fun `bang operator`() {
        val tests = listOf(
            TestData("!true", false),
            TestData("!false", true),
            TestData("!5", false),
            TestData("!!true", true),
            TestData("!!false", false),
            TestData("!!5", true),
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            testObject<MBoolean, Boolean>(evaluated, expected)
        }
    }

    @Test
    fun `if else expression`() {
        val tests = listOf(
            TestData("if (true) { 10 }", 10),
            TestData("if (false) { 10 }", null),
            TestData("if (1) { 10 }", 10),
            TestData("if (1 < 2) { 10 }", 10),
            TestData("if (1 > 2) { 10 }", null),
            TestData("if (1 > 2) { 10 } else { 20 }", 20),
            TestData("if (1 < 2) { 10 } else { 20 }", 10),
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)
            if (expected != null) {
                testObject<MInteger, Long>(evaluated, expected.toLong())
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun `return statement`() {

        listOf(
            TestData("return 10;", 10),
            TestData("return 10; 9;", 10),
            TestData("return 2 * 5; 9;", 10),
            TestData("9; return 2 * 5; 9;", 10),
            TestData(
                """
              if (10 > 1) {
                if (10 > 1) {
                  return 10;
                }
              
                return 1;
              }
              """, 10
            ),
            TestData(
                """
              let f = fn(x) {
                return x;
                x + 10;
              };
              f(10);""", 10
            ),
            TestData(
                """
              let f = fn(x) {
                 let result = x + 10;
                 return result;
                 return 10;
              };
              f(10);""", 20
            )
        ).test()
    }

    @Test
    fun `error handling`() {
        val tests = listOf(
            TestData(
                "5 + true;",
                "type mismatch: INTEGER + BOOLEAN",
            ),
            TestData(
                "5 + true; 5;",
                "type mismatch: INTEGER + BOOLEAN",
            ),
            TestData(
                "-true",
                "unknown operator: -BOOLEAN",
            ),
            TestData(
                "true + false;",
                "unknown operator: BOOLEAN + BOOLEAN",
            ),
            TestData(
                "true + false + true + false;",
                "unknown operator: BOOLEAN + BOOLEAN",
            ),
            TestData(
                "5; true + false; 5",
                "unknown operator: BOOLEAN + BOOLEAN",
            ),
            TestData(
                "if (10 > 1) { true + false; }",
                "unknown operator: BOOLEAN + BOOLEAN",
            ),
            TestData(
                """
            if (10 > 1) {
              if (10 > 1) {
                return true + false;
              }
            
              return 1;
            }
            """,
                "unknown operator: BOOLEAN + BOOLEAN",
            ),
            TestData(
                "foobar",
                "identifier not found: foobar",
            ),
            TestData(
                """"Hello" - "World"""",
                "unknown operator: STRING - STRING"
            ),
            TestData(
                """{"name": "Monkey"}[fn(x) {x}];""",
                "unusable as a hash key: FUNCTION"
            )
        )

        tests.forEach { (input, expected) ->
            val evaluated = testEval(input)

            checkType(evaluated) { error: MError ->
                Assert.assertEquals(
                    error.message,
                    expected,
                    "wrong error message. expected=$expected, got=${error.message}"
                )
            }
        }
    }

    @Test
    fun `let statement`() {
        listOf(
            TestData("let a = 5; a;", 5),
            TestData("let a = 5 * 5; a;", 25),
            TestData("let a = 5; let b = a; b;", 5),
            TestData("let a = 5; let b = a; let c = a + b + 5; c;", 15),
        ).test()
    }

    @Test
    fun `function object`() {
        val input = "fn(x) { x + 2; };"
        val evaluated = testEval(input)

        checkType(evaluated) { fn: MFunction ->
            Assert.assertEquals(fn.parameters?.size, 1, "function has wrong parameters. Parameters=${fn.parameters}")
            Assert.assertEquals(
                fn.parameters?.first().toString(),
                "x",
                "parameter is not 'x'. got=${fn.parameters?.first()}"
            )
            val body = "(x + 2)"
            Assert.assertEquals(fn.body?.toString(), body, "body is not $body, got=${fn.body}")
        }
    }

    @Test
    fun `function application`() {
        listOf(
            TestData("let identity = fn(x) { x; }; identity(5);", 5),
            TestData("let identity = fn(x) { return x; }; identity(5);", 5),
            TestData("let double = fn(x) { x * 2; }; double(5);", 10),
            TestData("let add = fn(x, y) { x + y; }; add(5, 5);", 10),
            TestData("let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));", 20),
            TestData("fn(x) { x; }(5)", 5),
        ).test()
    }

    @Test
    fun `enclosing environments`() {
        val input = """let first = 10;
          let second = 10;
          let third = 10;
          
          let ourFunction = fn(first) {
            let second = 20;
          
            first + second + third;
          };
          
          ourFunction(20) + first + second;"""
        testObject<MInteger, Long>(testEval(input), 70L)
    }

    @Test
    fun `string literal`() {
        val input = """"Hello World!""""
        testObject<MString, String>(testEval(input), "Hello World!")
    }

    @Test
    fun `string concatenation`() {
        val input = """"Hello" + " " + "World!""""
        testObject<MString, String>(testEval(input), "Hello World!")
    }

    @Test
    fun `builtin functions`() {
        listOf(
            TestData("""len("")""", 0),
            TestData("""len("four")""", 4),
            TestData("""len("hello world")""", 11),
            TestData("len(1)", "argument to `len` not supported, got INTEGER"),
            TestData("""len("one", "two")""", "wrong number of arguments. got=2, want=1"),
            TestData("len([1, 2, 3])", 3),
            TestData("len([])", 0),
            TestData("push([], 1)", intArrayOf(1)),
            TestData("push(1, 1)", "argument to `push` must be ARRAY, got INTEGER"),
            TestData("first([1, 2, 3])", 1),
            TestData("first([])", null),
            TestData("first(1)", "argument to `first` must be ARRAY, got INTEGER"),
            TestData("last([1, 2, 3])", 3),
            TestData("last([])", null),
            TestData("last(1)", "argument to `last` must be ARRAY, got INTEGER"),
            TestData("rest([1, 2, 3])", intArrayOf(2, 3)),
            TestData("rest([])", null),
        ).forEach { (input, expected) ->
            val evaluated = testEval(input)
            when (expected) {
                null -> testNullObject(evaluated)
                is Int -> testObject<MInteger, Long>(evaluated, expected.toLong())
                is String -> {
                    checkType(evaluated) { error: MError ->
                        Assert.assertEquals(error.message, expected)
                    }
                }
                is IntArray -> {
                    checkType(evaluated) { array: MArray ->
                        Assert.assertEquals(expected.size, array.elements.size)
                        expected.forEachIndexed { i, element ->
                            testObject<MInteger, Long>(array.elements[i], element.toLong())
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `array literal`() {
        val input = "[1, 2 * 2, 3 + 3]"

        val evaluated = testEval(input)
        val result = evaluated as MArray
        Assert.assertEquals(result.elements.size, 3, "array has wrong num of elements, got=${result.elements.size}")

        testObject<MInteger, Long>(result.elements[0], 1)
        testObject<MInteger, Long>(result.elements[1], 4)
        testObject<MInteger, Long>(result.elements[2], 6)
    }

    @Test
    fun `array index expression`() {
        listOf(
            TestData(
                "[1, 2, 3][0]",
                1,
            ),
            TestData(
                "[1, 2, 3][1]",
                2,
            ),
            TestData(
                "[1, 2, 3][2]",
                3,
            ),
            TestData(
                "let i = 0; [1][i];",
                1,
            ),
            TestData(
                "[1, 2, 3][1 + 1];",
                3,
            ),
            TestData(
                "let myArray = [1, 2, 3]; myArray[2];",
                3,
            ),
            TestData(
                "let myArray = [1, 2, 3]; myArray[0] + myArray[1] + myArray[2];",
                6,
            ),
            TestData(
                "let myArray = [1, 2, 3]; let i = myArray[0]; myArray[i]",
                2,
            ),
            TestData(
                "[1, 2, 3][3]",
                null,
            ),
            TestData(
                "[1, 2, 3][-1]",
                null,
            )
        ).forEach { (input, expected) ->
            val evaluated = testEval(input)
            when (expected) {
                is Int -> testObject<MInteger, Long>(evaluated, expected.toLong())
                else -> testNullObject(evaluated)
            }
        }
    }

    @Test
    fun `hash literals`() {
        val input = """
        let two = "two";
        	{
        		"one": 10 - 9,
        		two: 1 + 1,
        		"thr" + "ee": 6 / 2,
        		4: 4,
        		true: 5,
        		false: 6
        	}    
        """.trimIndent()
        val evaluated = testEval(input)
        checkType(evaluated) { result: MHash ->
            val expected = mapOf(
                MString("one").hashKey() to 1L,
                MString("two").hashKey() to 2,
                MString("three").hashKey() to 3,
                MInteger(4).hashKey() to 4,
                Evaluator.TRUE.hashKey() to 5,
                Evaluator.FALSE.hashKey() to 6
            )

            Assert.assertEquals(
                expected.size,
                result.pairs.size,
                "Hash has wrong number of pairs, got=${expected.size}"
            )

            expected.forEach { (expectedKey, expectedValue) ->
                val pair = result.pairs[expectedKey]
                Assert.assertNotNull(pair, "no pair for given key in pairs")
                testObject(pair!!.value, expectedValue)
            }
        }
    }

    @Test
    fun `hash index expressions`() {
        listOf(
            TestData("""{"foo": 5}["foo"]""", 5L),
            TestData("""{"foo": 5}["bar"]""", null),
            TestData("""let key = "foo";{"foo": 5}[key]""", 5L),
            TestData("""{}["foo"]""", null),
            TestData("""{5:5}[5]""", 5L),
            TestData("""{true:5}[true]""", 5L),
            TestData("""{false:5}[false]""", 5L),
        ).forEach { (input, expected) ->
            val evaluated = testEval(input)
            when (expected) {
                is Long -> testObject(evaluated, expected)
                else -> testNullObject(evaluated)
            }
        }
    }

    private fun testNullObject(obj: MObject?): Boolean {
        return if (obj != Evaluator.NULL) {
            Assert.fail("object is not NULL, got=${obj!!::class.java} ($obj)")
            false
        } else {
            true
        }
    }

    private inline fun <reified T : MValue<V>, V> testObject(obj: MObject?, expected: V): Boolean {
        return when (obj) {
            is T -> {
                when {
                    obj.value != expected -> {
                        Assert.fail("obj has wrong value, got=${obj.value}, want=$expected")
                        false
                    }
                    else -> true
                }
            }
            else -> {
                Assert.fail("obj is not ${T::class.java}, got=${if (obj != null) obj::class.java else "null"}, ($obj)")
                return false
            }
        }
    }

    companion object {
        fun testEval(input: String): MObject? {
            val lexer = Lexer(input)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            if (parser.errors().isNotEmpty()) {
                parser.errors().forEach(::println)
            }

            return Evaluator.eval(program, Environment.newEnvironment())
        }
    }


}