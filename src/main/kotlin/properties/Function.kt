package properties

import lexer.Token

open class Function(name: String, val args: List<String>, val body: Token, parent: Type? = null) :
    Property(name, parent) {
    override fun toString(): String {
        return "$name(${args.joinToString(separator = ",")})     ${parent ?: ""}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false
        if (!super.equals(other)) return false

        if (args.size != other.args.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + args.size.hashCode()
        return result
    }
}