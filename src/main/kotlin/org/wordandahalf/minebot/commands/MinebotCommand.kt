package org.wordandahalf.minebot.commands

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent

abstract class MinebotCommand(val name: String)
{
    companion object
    {
        const val PREFIX : String = "m!"
    }

    abstract fun getAliases() : Array<String>?
    abstract fun getDescription() : String?
    abstract fun getUsage() : String?
    open fun getParameterCount() : Array<Int> { return arrayOf(0) }

    fun printUsage(channel: TextChannel)
    {
        channel.sendMessage("`Usage: $PREFIX$name${if (getUsage() != null) " ${getUsage()}" else ""}`")
    }

    /**
     * Called when the command is executed.
     *
     * The first index of `args` will be the command itself (or alias, if used)
     */
    abstract fun onExecuted(e: MessageCreateEvent, s: Server, c: TextChannel, args: List<String>)
}