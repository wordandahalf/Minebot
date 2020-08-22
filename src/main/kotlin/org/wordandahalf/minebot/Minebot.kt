package org.wordandahalf.minebot

import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.wordandahalf.minebot.commands.MinebotCommandListener
import org.wordandahalf.minebot.link.MinebotLinkManager
import org.wordandahalf.minebot.link.MinebotLinkNetworkManager
import kotlin.system.exitProcess

object Minebot
{
    lateinit var API : DiscordApi
}

fun main(args: Array<String>)
{
    if(args.size != 1)
    {
        System.err.println("Please ensure that your Discord bot token is the only command line parameter")
        exitProcess(-1)
    }

    Minebot.API = DiscordApiBuilder()
        .setToken(args[0])
        .addListener(MinebotCommandListener())
        .addListener(MinebotLinkNetworkManager.MessageListener())
        .login()
        .join()

    MinebotLinkManager.init()
    MinebotLinkNetworkManager.start()
}