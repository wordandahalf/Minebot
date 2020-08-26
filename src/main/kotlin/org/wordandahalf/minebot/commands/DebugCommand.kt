package org.wordandahalf.minebot.commands

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.wordandahalf.minebot.MinebotConfig
import org.wordandahalf.minebot.MinebotLogger

class DebugCommand : MinebotCommand("debug")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("d")
    }

    override fun getDescription(): String?
    {
        return "Toggles the display of advanced debugging features."
    }

    override fun getUsage(): String? { return null }

    override fun onExecuted(e: MessageCreateEvent, s: Server, c: ServerTextChannel, args: List<String>)
    {
        val loggingLevel = MinebotLogger.getLevel(s)

        if(loggingLevel == MinebotLogger.Level.DEBUG)
        {
            MinebotLogger.setLevel(s, MinebotLogger.Level.NORMAL)
            MinebotLogger.success(c, "Disabled debug features")
        }
        else
        {
            MinebotLogger.setLevel(s, MinebotLogger.Level.DEBUG)
            MinebotLogger.success(c, "Enabled debug features")
        }
    }
}