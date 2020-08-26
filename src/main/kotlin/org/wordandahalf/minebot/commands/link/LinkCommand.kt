package org.wordandahalf.minebot.commands.link

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.wordandahalf.minebot.MinebotLogger
import org.wordandahalf.minebot.commands.MinebotCommand
import org.wordandahalf.minebot.link.MinebotLinkManager
import java.net.InetAddress

class LinkCommand : MinebotCommand("link")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("l")
    }

    override fun getDescription(): String?
    {
        return "Links a text channel to a Minebot-Integration instance running on a game server"
    }

    override fun getUsage(): String?
    {
        return "[text channel link] [address]"
    }

    override fun getParameterCount() : Array<Int>
    {
        return arrayOf(2)
    }

    override fun onExecuted(e: MessageCreateEvent, s: Server, c: ServerTextChannel, args: List<String>)
    {
        if(e.message.mentionedChannels.size != 1)
        {
            printUsage(c)
            return
        }

        val channelToLink = e.message.mentionedChannels[0]
        val address : InetAddress

        try
        {
            address = InetAddress.getByName(args[2])
        }
        catch (err: Exception)
        {
            MinebotLogger.error(c, "Could not link ${channelToLink.mentionTag} to `${args[2]}`: ${err.message}")
            return
        }

        if(MinebotLinkManager.link(address, s, channelToLink))
        {
            MinebotLogger.success(c, "Linked ${channelToLink.mentionTag} to `$address`")
        }
        else
        {
            MinebotLogger.error(c, "Could not link ${channelToLink.mentionTag} to `${address}`.")
        }
    }
}