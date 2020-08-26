package org.wordandahalf.minebot.commands

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.wordandahalf.minebot.MinebotConfig
import org.wordandahalf.minebot.MinebotLogger

class ConfigCommand : MinebotCommand("config")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("cfg")
    }

    override fun getDescription(): String?
    {
        return "For debug purposes only."
    }

    override fun getUsage(): String?
    {
        return "[operation] [name] <value>"
    }

    override fun getParameterCount() : Array<Int>
    {
        return arrayOf(2, 3)
    }

    override fun onExecuted(e: MessageCreateEvent, s: Server, c: ServerTextChannel, args: List<String>)
    {
        if(!e.message.author.isServerAdmin)
            return

        when(args[1].toLowerCase())
        {
            "get" ->
            {
                if(args.size != 3)
                {
                    printUsage(c)
                    return
                }

                MinebotLogger.log(c, "`${args[2]} = ${MinebotConfig.get(s, args[2])}`")
            }
            "remove" ->
            {
                if(args.size != 3)
                {
                    printUsage(c)
                    return
                }

                MinebotLogger.log(c, (if (MinebotConfig.remove(s, args[2])) "Successfully" else "Could not") + " removed the key '${args[2]}'")
            }
            "put" ->
            {
                if(args.size != 4)
                {
                    printUsage(c)
                    return
                }

                MinebotLogger.log(c, (if (MinebotConfig.put(s, args[2], args[3])) "Successfully" else "Could not") + " set the key '${args[2]}' to '${args[3]}")
            }
            else ->
            {
                printUsage(c)
            }
        }
    }
}