package org.marioarias.monkey.ast


typealias ModifierFunction = (Node) -> Node?

fun modify(node: Node?, modifier: ModifierFunction): Node? {
    val modNote = when (node) {
        is Program -> {
            Program(node.statements.map { statement -> modify(statement, modifier) as Statement })
        }
        is ExpressionStatement -> {
            ExpressionStatement(node.token, modify(node.expression, modifier) as Expression)
        }
        is InfixExpression -> {
            InfixExpression(
                node.token,
                left = modify(node.left, modifier) as Expression,
                operator = node.operator,
                right = modify(node.right, modifier) as Expression
            )
        }
        is PrefixExpression -> {
            PrefixExpression(node.token, node.operator, modify(node.right, modifier) as Expression)
        }
        is IndexExpression -> {
            IndexExpression(
                node.token,
                left = modify(node.left, modifier) as Expression,
                index = modify(node.index, modifier) as Expression
            )
        }
        is IfExpression -> {
            IfExpression(
                node.token,
                condition = modify(node.condition, modifier) as Expression,
                consequence = modify(node.consequence, modifier) as BlockStatement,
                alternative = if (node.alternative != null) modify(
                    node.alternative,
                    modifier
                ) as BlockStatement else null
            )
        }
        is BlockStatement -> {
            BlockStatement(node.token, node.statements?.map { statement -> modify(statement!!, modifier) as Statement })
        }
        is ReturnStatement -> {
            ReturnStatement(node.token, modify(node.returnValue, modifier) as Expression)
        }
        is LetStatement -> {
            LetStatement(
                node.token,
                name = node.name,
                value = modify(node.value, modifier) as Expression
            )
        }
        is FunctionLiteral -> {
            FunctionLiteral(
                node.token,
                parameters = node.parameters?.map { identifier -> modify(identifier, modifier) as Identifier },
                body = modify(node.body, modifier) as BlockStatement
            )
        }
        is ArrayLiteral -> {
            ArrayLiteral(node.token, node.elements?.map { element -> modify(element, modifier) as Expression })
        }
        is HashLiteral -> {
            HashLiteral(
                node.token,
                node.pairs.mapKeys { (key, _) -> modify(key, modifier) as Expression }
                    .mapValues { (_, value) -> modify(value, modifier) as Expression })
        }
        else -> node!!
    }
    return modifier(modNote)
}