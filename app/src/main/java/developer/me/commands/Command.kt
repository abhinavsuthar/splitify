package developer.me.commands

interface Command<out T> {
    fun execute(): T
}