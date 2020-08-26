package org.wordandahalf.minebot.link

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.server.Server
import org.wordandahalf.minebot.Minebot
import org.wordandahalf.minebot.MinebotConfig
import org.wordandahalf.minebot.MinebotLogger
import java.net.InetAddress

object MinebotLinkManager
{
    private val linkedChannels = HashMap<InetAddress, ArrayList<ServerTextChannel>>()

    fun init()
    {
        Minebot.API.servers.forEach outer@{ s ->
            val linkedChannelsConfig = MinebotConfig.from(s).getConfigurationSection("link.channels") ?: return@outer

            linkedChannelsConfig.getValues(false).forEach config@{
                // Linked channels are stored in the config as a list of values with the channel ID as the key and the address as the value

                // Get the TextChannel using Kotlin's fancy if expressions
                val channel =
                    if (s.getChannelById(it.key).get().asServerTextChannel().isPresent)
                        s.getChannelById(it.key).get().asServerTextChannel().get()
                    else
                        // 'return@forEach' is equivalent to 'continue' in a for loop;
                        // it skips the current iteration and moves on to the next one
                        return@config

                if(it.value !is String)
                    return@config

                val address = InetAddress.getByName(it.value as String)

                MinebotLogger.debug(channel, "Linking ${it.key} to $address")

                linkedChannels.putIfAbsent(address, ArrayList())
                linkedChannels[address]?.add(channel)
            }
        }
    }

    fun isLinked(channel: ServerTextChannel) : Boolean
    {
        val address = getLinkedAddress(channel) ?: return false

        return linkedChannels[address]?.contains(channel) ?: false
    }

    fun link(address: InetAddress, server: Server, channel: ServerTextChannel) : Boolean
    {
        if(!isLinked(channel))
        {
            linkedChannels.putIfAbsent(address, ArrayList())
            linkedChannels[address]?.add(channel) ?: return false

            MinebotConfig.from(server)
                .set("link.channels.${channel.idAsString}", address.hostAddress)

            MinebotConfig.save(server)

            return true
        }
        else
        {
            return false
        }
    }

    fun unlink(server: Server, channel: ServerTextChannel) : Boolean
    {
        // Ensure the channel is linked in memory
        val address = getLinkedAddress(channel) ?: return false

        // Ensure that the channel could be removed from the Map
        if(linkedChannels[address]?.remove(channel) == false)
        {
            return false
        }

        val config = MinebotConfig.from(server)

        // Ensure that the channel could be unlinked on disk
        return if(config.contains("link.channels.${channel.idAsString}"))
        {
            config.remove("link.channels.${channel.idAsString}")
            MinebotConfig.save(server)

            !config.contains("link.channels.${channel.idAsString}")
        }
        else
        {
            false
        }
    }

    fun getAllLinkedChannels(server: Server) : ArrayList<ServerTextChannel>
    {
        val ret = ArrayList<ServerTextChannel>()

        linkedChannels.values.forEach { channels ->
            ret.addAll(
                channels.filter {
                    (it.asServerChannel().isPresent) && (it.asServerTextChannel().get().server.id == server.id)
                }
            )
        }

        return ret
    }

    fun getLinkedChannels(address: InetAddress) : ArrayList<ServerTextChannel>?
    {
        return linkedChannels[address]
    }

    fun getLinkedAddress(channel: ServerTextChannel) : InetAddress?
    {
        val addresses = linkedChannels.filter { it.value.contains(channel) }.keys

        if (addresses.size == 1)
        {
            return addresses.first()
        }

        return null
    }

    fun getAddress(code: String) : InetAddress?
    {
        if(code.length > 14)
            return null

        val ipWithPortString = code.toBigInteger(36).toString()
        // 99.109.107.234:8000
        // 29931093107323448000
        // 6behizyg1g268

        println(code)
        println(ipWithPortString)

        var address = ""
        var groupLength = ipWithPortString[0].toInt() - 0x30
        ipWithPortString.substring(1).forEach {
            if(groupLength == 0)
            {
                address += "."
                groupLength = it.toInt() - 0x30
            }
            else
            {
                address += it
                groupLength--
            }
        }

        println(address)

        return null
    }
}