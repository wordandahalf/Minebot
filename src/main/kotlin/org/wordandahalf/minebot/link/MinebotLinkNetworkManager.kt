package org.wordandahalf.minebot.link

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import org.json.JSONObject
import org.wordandahalf.minebot.MinebotLogger
import java.awt.Color
import java.net.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors

object MinebotLinkNetworkManager
{
    private val executorService = Executors.newFixedThreadPool(3)
    private lateinit var server : DatagramSocket

    @Volatile
    private var messageQueueOut = LinkedList<Message>()
    @Volatile
    private var messageQueueIn  = LinkedList<DatagramPacket>()

    fun start()
    {
        server = DatagramSocket(6969)

        executorService.execute(ReceiveWorker())
        executorService.execute(SendWorker())
        executorService.submit(MessageWorker())
    }

    fun stop()
    {
        executorService.shutdown()
        server.close()
    }

    private class ReceiveWorker : Runnable
    {
        private val buffer = ByteArray(8192)

        override fun run()
        {
            while(!Thread.currentThread().isInterrupted)
            {
                val packet = DatagramPacket(buffer, buffer.size)
                server.receive(packet)

                MinebotLinkManager.getLinkedChannels(packet.address)?.forEach {
                    MinebotLogger.debug(it, "Received packet from ${packet.address}")
                }

                messageQueueIn.add(packet)
            }
        }
    }

    private class SendWorker : Runnable
    {
        override fun run()
        {
            while(!Thread.currentThread().isInterrupted)
            {
                while(messageQueueOut.isNotEmpty())
                {
                    val message = messageQueueOut.remove()
                    val address = MinebotLinkManager.getLinkedAddress(message.channel.asServerTextChannel().get()) ?: return
                    val json = JSONObject()

                    json.put("author", message.author.name)
                    json.put("message", message.content)

                    val data = json.toString().toByteArray(StandardCharsets.UTF_8)

                    server.send(
                        DatagramPacket(
                            data,
                            data.size,
                            address,
                            6969
                        )
                    )

                    MinebotLogger.debug(message.channel as ServerTextChannel, "Sent packet {'$json'} to $address")
                }
            }
        }
    }

    private class MessageWorker : Runnable
    {
        override fun run()
        {
            while(!Thread.currentThread().isInterrupted)
            {
                while(messageQueueIn.isNotEmpty())
                {
                    val packet = messageQueueIn.remove()

                    val data = packet.data.toString(StandardCharsets.UTF_8).filter { it.toInt() != 0 }//String(packet.data.copyOfRange(0, packet.data.indexOfFirst { it == 0.toByte() }), StandardCharsets.UTF_8)
                    val json = JSONObject(data)

                    MinebotLinkManager.getLinkedChannels(packet.address)?.forEach { c ->
                        val embed = EmbedBuilder()

                        embed.setFooter(json.getString("message"))
                        embed.setDescription("**${json.getString("author")}** (${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))})")
                        embed.setColor(Color(30, 140, 80))
                        c.sendMessage(embed)
                    }
                }
            }
        }
    }

    class MessageListener : MessageCreateListener
    {
        override fun onMessageCreate(e: MessageCreateEvent)
        {
            // Return if the channel is not linked
            if(MinebotLinkManager.getLinkedAddress(e.channel.asServerTextChannel().get()) == null)
                return

            // Return if the message's author is a bot
            if(e.messageAuthor.isBotUser)
                return

            // Return if the message is empty
            if(e.messageContent.trim().isEmpty())
                return

            messageQueueOut.add(e.message)
        }
    }
}