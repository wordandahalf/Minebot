package org.wordandahalf.minebot.commands

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent

class HelpCommand : MinebotCommand("help")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("h")
    }

    override fun getDescription(): String?
    {
        return "Prints a list of commands, their descriptions, and their usages."
    }

    override fun getUsage(): String?
    {
        return null
    }

    override fun onExecuted(e: MessageCreateEvent, s: Server, c: ServerTextChannel, args: List<String>)
    {
        val builder = StringBuilder()
        builder.append("```md\n")

        MinebotCommandListener.getRegisteredCommands().forEach {
            val msg = "[m!${it.name}|${it.getAliases()?.joinToString(", ")}](${it.getDescription()})\n"

            if (it.getUsage() == null)
                builder.append(msg)
            else
                builder.append("$msg> m!${it.name} ${it.getUsage()}\n")
        }

        builder.append("```")
        c.sendMessage(builder.toString())
    }
}