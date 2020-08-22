package org.wordandahalf.minebot.commands.link

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.wordandahalf.minebot.commands.MinebotCommand
import org.wordandahalf.minebot.link.MinebotLinkManager
import java.awt.Color
import java.lang.StringBuilder

class GetLinkedCommand : MinebotCommand("getlinked")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("gl")
    }

    override fun getDescription(): String?
    {
        return "Prints out a list of text channels that have been linked to a Minebot Integration-equipped game server"
    }

    override fun getUsage(): String? { return null }

    override fun onExecuted(e: MessageCreateEvent, s: Server, c: TextChannel, args: List<String>)
    {
        val linkedChannels = MinebotLinkManager.getAllLinkedChannels(s)

        if(linkedChannels.size == 0)
        {
            c.sendMessage("No linked channels.")
            return
        }

        val builder = EmbedBuilder()
        builder.setTitle("Linked channels")
        builder.setAuthor("Minebot", "https://craftbot.github.io", "https://cdn.discordapp.com/avatars/612796388694163457/f3a1b98e57b45b6389eef091c87ead7e.png")
        builder.setColor(Color(30, 140, 80))

        linkedChannels.forEach {
            builder.addField(
            "#${it.name}",
                MinebotLinkManager.getLinkedAddress(it).toString()
            )
        }

        c.sendMessage(builder)
    }
}