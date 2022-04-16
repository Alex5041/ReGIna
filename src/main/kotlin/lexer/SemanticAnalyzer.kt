package lexer

import token.Token
import token.TokenFactory.Companion.createSpecificIdentifierFromInvocation
import SymbolTable
import evaluation.Evaluation
import evaluation.FunctionEvaluation
import token.TokenArray
import token.TokenIndexing

class SemanticAnalyzer(private val fileName: String, private val tokens: List<Token>) {
    private val classes = mutableSetOf<String>()
    private val functions = SymbolTable.getEmbeddedNames()

    fun analyze(): List<Token> {
        createAssociations()
        changeIdentTokens()
        return tokens
    }

    private fun createAssociations() {
        for (token in tokens)
            when (token.value) {
                "class" -> {
                    Evaluation.globalTable.addType(token, fileName)
                    classes.add(getSupertype(getExport(token.left)).value)
                }
                "fun" -> {
                    Evaluation.globalTable.addFunction(FunctionEvaluation.createFunction(token), fileName)
                    functions.add(token.left.left.value)
                    checkParams(token.left.children.subList(1, token.left.children.size))
                }
                else -> {
                }
            }
        val intersections = classes.intersect(functions)
        if (intersections.isNotEmpty())
            throw PositionalException("$fileName contains functions and classes with same names: $intersections")
    }

    private fun changeIdentTokens() {
        for (token in tokens)
            changeTokenType(token)
    }

    private fun changeTokenType(token: Token) {
        for ((index, child) in token.children.withIndex()) {
            when (child.symbol) {
                "(" -> {
                    if (token.value != "fun")
                        token.children[index] = createSpecificIdentifierFromInvocation(child, classes, functions)
                }
                // "[]" -> token.children[index] = TokenArray(child)
                //"[" -> token.children[index] = TokenIndexing(child)
            }
            changeTokenType(token.children[index])
        }
    }

    private fun checkParams(params: List<Token>) {
        for (param in params)
            if (param.symbol != "(IDENT)") throw PositionalException("expected identifier as function parameter", param)
    }

    /**
     * constructor params are assignments, because of the dynamic structure of type
     */
    private fun checkConstructorParams(params: List<Token>) {
        for (param in params)
            if (param.value != "=") throw PositionalException(
                "expected assignment as constructor parameter",
                param
            )
    }

    private fun getExport(token: Token): Token {
        return if (token.value == "export")
            token.left
        else token
    }

    private fun getSupertype(token: Token): Token {
        return if (token.value == ":")
            token.left
        else token
    }
}