package org.wordandahalf.minebot.commands

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.json.JSONObject
import org.wordandahalf.minebot.games.retrievers.MinecraftServerDataRetriever
import java.awt.Color
import java.net.InetSocketAddress
import java.util.*

class PingCommand : MinebotCommand("ping")
{
    override fun getAliases(): Array<String>?
    {
        return arrayOf("p")
    }

    override fun getDescription(): String?
    {
        return "Performs a ping at the provided Minecraft server"
    }

    override fun getUsage(): String?
    {
        return "[address] <port>"
    }

    override fun getParameterCount() : Array<Int>
    {
        return arrayOf(1, 2)
    }

    // TODO: Make this generic for future multigame compat
    override fun onExecuted(e: MessageCreateEvent, s: Server, c: ServerTextChannel, args: List<String>)
    {
        GlobalScope.launch{
            val retriever = MinecraftServerDataRetriever(InetSocketAddress(args[1], args[2].toInt()))//MinecraftServerDataRetriever(InetSocketAddress(args[1], args[2].toInt()))
            val data = retriever.retrieve()
            val builder = EmbedBuilder()

            builder.setAuthor("Minebot", "https://craftbot.github.io", "https://cdn.discordapp.com/avatars/612796388694163457/f3a1b98e57b45b6389eef091c87ead7e.png")
            builder.setTitle(retriever.address.hostString +
                if(retriever.address.port != retriever.getDefaultPort())
                    ":" + retriever.address.port
                else
                    ""
            )

            if(data.has("error"))
            {
                builder.setColor(Color.RED)
                builder.setDescription(data.getString("error"))
            }
            else
            {
                builder.setColor(Color.GREEN)
    
                if(data.has("version"))
                {
                    if(data.get("description") is JSONObject)
                        builder.setDescription(data.getJSONObject("description").getString("text"))
                    else
                        builder.setDescription(data.getString("description"))

                    builder.addField("Version", data.getJSONObject("version").getString("name"), true)
                    builder.addField("Protocol", data.getJSONObject("version").getInt("protocol").toString(), true)
    
                    builder.addField("Players",
                        if (data.getJSONObject("players").has("sample"))
                            data.getJSONObject("players").getJSONArray("sample").joinToString {
                                (it as JSONObject).getString("name") + "\n"
                            } + "(${data.getJSONObject("players").getInt("online")}/${data.getJSONObject("players").getInt("max")})"
                        else
                        "(${data.getJSONObject("players").getInt("online")}/${data.getJSONObject("players").getInt("max")})"
                    )
    
                    if(data.has("favicon"))
                        builder.setThumbnail(Base64.getDecoder().decode(data.getString("favicon").substring("data:image/png;base64,".length).replace("\n", "")))
                }
                else
                {
                    builder.setDescription(data.getString("motd"))
                    builder.addField("Players", "${data.getInt("playerCount")} / ${data.getInt("playerLimit")}")
                    builder.addField("Version", if(data.has("gameVersion")) data.getString("gameVersion") else "Legacy (b1.8 - 1.5)")
                }
            }
    
            c.sendMessage(builder)
        }
    }
}