package org.wordandahalf.minebot.commands.link

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.wordandahalf.minebot.commands.MinebotCommand
import org.wordandahalf.minebot.link.MinebotLinkManager

class UnlinkCommand : MinebotCommand("unlink")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("ul")
    }

    override fun getDescription(): String?
    {
        return "Unlinks a text channel from a Minebot-Integration instance"
    }

    override fun getUsage(): String?
    {
        return "[text channel link]"
    }

    override fun getParameterCount() : Array<Int>
    {
        return arrayOf(1)
    }

    override fun onExecuted(e: MessageCreateEvent, s: Server, c: TextChannel, args: List<String>)
    {
        if(e.message.mentionedChannels.size != 1)
        {
            printUsage(c)
            return
        }

        val channelToUnlink = e.message.mentionedChannels[0]

        if(MinebotLinkManager.unlink(s, channelToUnlink))
            c.sendMessage("Unlinked ${channelToUnlink.mentionTag}.")
        else
            c.sendMessage("Could not unlink ${channelToUnlink.mentionTag}.")
    }
}