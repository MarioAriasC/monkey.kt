package org.marioarias.monkey.parser

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.token.Token
import org.marioarias.monkey.token.TokenType


typealias PrefixParse = () -> Expression?
typealias InfixParse = (Expression?) -> Expression?

class Parser(private val lexer: Lexer) {

    enum class Precedence {
        LOWEST, EQUALS, LESS_GREATER, SUM, PRODUCT, PREFIX, CALL, INDEX
    }

    private val errors: MutableList<String> = mutableListOf()

    private lateinit var curToken: Token
    private lateinit var peekToken: Token

    private fun prefixParser(tokenType: TokenType): PrefixParse? {
        return when (tokenType) {
            TokenType.INT -> ::parseIntegerLiteral
            TokenType.TRUE -> ::parseBooleanLiteral
            TokenType.FALSE -> ::parseBooleanLiteral
            TokenType.IDENT -> ::parseIdentifier
            TokenType.BANG -> ::parsePrefixExpression
            TokenType.MINUS -> ::parsePrefixExpression
            TokenType.LPAREN -> ::parseGroupExpression
            TokenType.IF -> ::parseIfExpression
            TokenType.FUNCTION -> ::parseFunctionLiteral
            TokenType.STRING -> ::parseStringLiteral
            TokenType.LBRACKET -> ::parseArrayLiteral
            TokenType.LBRACE -> ::parseHashLiteral
            else -> null
        }
    }

    private fun infixParser(tokenType: TokenType): InfixParse? {
        return when (tokenType) {
            TokenType.PLUS -> ::parseInfixExpression
            TokenType.MINUS -> ::parseInfixExpression
            TokenType.SLASH -> ::parseInfixExpression
            TokenType.ASTERISK -> ::parseInfixExpression
            TokenType.EQ -> ::parseInfixExpression
            TokenType.NOT_EQ -> ::parseInfixExpression
            TokenType.LT -> ::parseInfixExpression
            TokenType.GT -> ::parseInfixExpression
            TokenType.LPAREN -> ::parseCallExpression
            TokenType.LBRACKET -> ::parseIndexExpression
            else -> null
        }
    }

    private val precedences = mapOf(
        TokenType.EQ to Precedence.EQUALS,
        TokenType.NOT_EQ to Precedence.EQUALS,
        TokenType.LT to Precedence.LESS_GREATER,
        TokenType.GT to Precedence.LESS_GREATER,
        TokenType.PLUS to Precedence.SUM,
        TokenType.MINUS to Precedence.SUM,
        TokenType.SLASH to Precedence.PRODUCT,
        TokenType.ASTERISK to Precedence.PRODUCT,
        TokenType.LPAREN to Precedence.CALL,
        TokenType.LBRACKET to Precedence.INDEX
    )


    init {
        nextToken()
        nextToken()
    }

    private fun parseHashLiteral(): Expression? {
        val token = curToken
        val pairs = mutableMapOf<Expression, Expression>()
        while (!peekTokenIs(TokenType.RBRACE)) {
            nextToken()
            val key = parseExpression(Precedence.LOWEST)
            if (!expectPeek(TokenType.COLON)) {
                return null
            }
            nextToken()
            val value = parseExpression(Precedence.LOWEST)
            pairs[key!!] = value!!
            if (!peekTokenIs(TokenType.RBRACE) && !expectPeek(TokenType.COMMA)) {
                return null
            }
        }
        return if (!expectPeek(TokenType.RBRACE)) {
            null
        } else {
            HashLiteral(token, pairs)
        }
    }

    private fun parseIndexExpression(left: Expression?): Expression? {
        val token = curToken
        nextToken()

        val index = parseExpression(Precedence.LOWEST)

        return if (!expectPeek(TokenType.RBRACKET)) {
            null
        } else {
            IndexExpression(token, left, index)
        }
    }

    private fun parseArrayLiteral(): Expression {
        val token = curToken

        return ArrayLiteral(token, parseExpressionList(TokenType.RBRACKET))
    }

    private fun parseExpressionList(end: TokenType): List<Expression?>? {

        val arguments = mutableListOf<Expression?>()

        if (peekTokenIs(end)) {
            nextToken()
            return arguments
        }

        nextToken()
        arguments.add(parseExpression(Precedence.LOWEST))

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            arguments.add(parseExpression(Precedence.LOWEST))
        }

        return if (!expectPeek(end)) {
            null
        } else {
            arguments
        }
    }

    private fun parseStringLiteral(): Expression = StringLiteral(curToken, curToken.literal)

    private fun nextToken() {
        try {
            curToken = peekToken
//        } catch (e: UninitializedPropertyAccessException) { // Deprecated for common
        } catch (e: RuntimeException) {
//            Ignore it, it only happens on the first try
        }
        peekToken = lexer.nextToken()
    }

    fun parseProgram(): Program {
        val statements = mutableListOf<Statement>()
        while (curToken.type != TokenType.EOF) {
            val statement = parseStatement()
            if (statement != null) {
                statements.add(statement)
            }
            nextToken()
        }
        return Program(statements)
    }

    private fun parseStatement(): Statement? {
        return when (curToken.type) {
            TokenType.LET -> parseLetStatement()
            TokenType.RETURN -> parseReturnStatement()
            else -> parseExpressionStatement()
        }
    }

    private fun parseExpressionStatement(): Statement {
        val token = curToken
        val expression = parseExpression(Precedence.LOWEST)

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return ExpressionStatement(token, expression)

    }

    private fun parseReturnStatement(): Statement {
        val token = curToken
        nextToken()

        val returnValue = parseExpression(Precedence.LOWEST)

        while (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return ReturnStatement(token, returnValue)
    }

    private fun curTokenIs(type: TokenType): Boolean = curToken.type == type

    private fun parseLetStatement(): Statement? {
        val token = curToken
        if (!expectPeek(TokenType.IDENT)) {
            return null
        }

        val name = Identifier(curToken, curToken.literal)

        if (!expectPeek(TokenType.ASSIGN)) {
            return null
        }

        nextToken()

        val value = parseExpression(Precedence.LOWEST)

        if (value is FunctionLiteral) {
            value.name = name.value
        }

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return LetStatement(token, name, value)


    }

    private fun parseExpression(precedence: Precedence): Expression? {
        val prefix = prefixParser(curToken.type)
        if (prefix == null) {
            noPrefixParserError(curToken.type)
            return null
        }

        var left = prefix()

        while (!peekTokenIs(TokenType.SEMICOLON) && precedence < peekPrecedence()) {
            val infix = infixParser(peekToken.type) ?: return left
            nextToken()
            left = infix(left)
        }

        return left
    }


    private fun noPrefixParserError(type: TokenType) {
        errors.add("no prefix parser for $type function")
    }

    private fun expectPeek(type: TokenType): Boolean = if (peekTokenIs(type)) {
        nextToken()
        true
    } else {
        peekError(type)
        false
    }

    private fun peekError(type: TokenType) {
        errors.add("Expected next token to be $type, got ${peekToken.type} instead")
    }

    private fun peekTokenIs(type: TokenType): Boolean {
        return peekToken.type == type
    }

    fun errors(): List<String> = errors

    private fun parseIntegerLiteral(): Expression? {
        val token = curToken
        return try {
            val value = token.literal.toLong()
            IntegerLiteral(token, value)
        } catch (e: ClassCastException) {
            errors.add("could not parse ${token.literal} as integer")
            null
        }
    }

    private fun parseBooleanLiteral(): Expression = BooleanLiteral(curToken, curTokenIs(TokenType.TRUE))

    private fun parseIdentifier(): Expression = Identifier(curToken, curToken.literal)

    private fun parsePrefixExpression(): Expression {
        val token = curToken
        val operator = token.literal

        nextToken()

        val right = parseExpression(Precedence.PREFIX)

        return PrefixExpression(token, operator, right)

    }

    private fun parseInfixExpression(left: Expression?): Expression {
        val token = curToken
        val operator = token.literal

        val precedence = curPrecedence()
        nextToken()
        val right = parseExpression(precedence)

        return InfixExpression(token, left, operator, right)
    }

    private fun parseGroupExpression(): Expression? {
        nextToken()

        val exp = parseExpression(Precedence.LOWEST)

        return if (!expectPeek(TokenType.RPAREN)) {
            null
        } else {
            exp
        }
    }

    private fun parseIfExpression(): Expression? {
        val token = curToken

        if (!expectPeek(TokenType.LPAREN)) {
            return null
        }

        nextToken()

        val condition = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        if (!expectPeek(TokenType.LBRACE)) {
            return null
        }

        val consequence = parseBlockStatement()

        val alternative = if (peekTokenIs(TokenType.ELSE)) {
            nextToken()

            if (!expectPeek(TokenType.LBRACE)) {
                return null
            }

            parseBlockStatement()
        } else {
            null
        }

        return IfExpression(token, condition, consequence, alternative)
    }

    private fun parseBlockStatement(): BlockStatement {
        val token = curToken

        val statements = mutableListOf<Statement?>()

        nextToken()

        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            val statement = parseStatement()
            if (statement != null) {
                statements.add(statement)
            }
            nextToken()
        }

        return BlockStatement(token, statements)
    }

    private fun parseCallExpression(expression: Expression?): Expression {
        val token = curToken
        val arguments = parseExpressionList(TokenType.RPAREN)
        return CallExpression(token, expression, arguments)
    }

    private fun parseFunctionLiteral(): Expression? {
        val token = curToken

        if (!expectPeek(TokenType.LPAREN)) {
            return null
        }

        val parameters = parseFunctionParameters()

        if (!expectPeek(TokenType.LBRACE)) {
            return null
        }

        val body = parseBlockStatement()

        return FunctionLiteral(token, parameters, body)
    }

    private fun parseFunctionParameters(): List<Identifier>? {
        val parameters = mutableListOf<Identifier>()

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken()
            return parameters
        }

        nextToken()

        val token = curToken

        parameters.add(Identifier(token, token.literal))

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()

            val innerToken = curToken

            parameters.add(Identifier(innerToken, innerToken.literal))
        }

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        return parameters
    }

    private fun curPrecedence(): Precedence = findPrecedence(curToken.type)

    private fun peekPrecedence(): Precedence = findPrecedence(peekToken.type)

    private fun findPrecedence(type: TokenType): Precedence = precedences[type] ?: Precedence.LOWEST
}