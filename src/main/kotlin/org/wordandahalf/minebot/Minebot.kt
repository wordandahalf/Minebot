package org.wordandahalf.minebot

import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder
import org.javacord.api.entity.server.Server
import org.simpleyaml.configuration.file.YamlFile
import org.simpleyaml.exceptions.InvalidConfigurationException
import org.wordandahalf.minebot.commands.MinebotCommandListener
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.UnsupportedOperationException
import kotlin.system.exitProcess

private lateinit var API : DiscordApi

fun main(args: Array<String>)
{
    if(args.size != 1)
    {
        System.err.println("Please ensure that your Discord bot token is the only command line parameter")
        exitProcess(-1)
    }

    API = DiscordApiBuilder()
        .setToken(args[0])
        .addListener(MinebotCommandListener())
        .login()
        .join()
}

object MinebotConfig
{
    private val FOLDER = File("config")

    private val loadedConfigurations = HashMap<Long, YamlFile>()

    fun from(server: Server) : YamlFile
    {
        if(loadedConfigurations[server.id] == null)
        {
            if(!FOLDER.exists() && !FOLDER.mkdirs())
                throw UnsupportedOperationException("Unable to create configuration directory at ${FOLDER.absolutePath}!")

            val config = YamlFile(FOLDER.absolutePath + File.separator + server.id + ".yml")
            try
            {
                if(!config.exists())
                {
                    config.createNewFile(true)
                    println("Created new configuration ${config.filePath}!")
                }

                config.load()
                loadedConfigurations[server.id] = config
            }
            // Kotlin does not have multicatch......
            catch (e: Exception)
            {
                when(e)
                {
                    is IOException, is InvalidConfigurationException ->
                    {
                        System.err.println(e)
                    }
                    else -> throw e
                }
            }
        }

        return loadedConfigurations[server.id]!!
    }

    fun get(server: Server, key: String) : Any?
    {
        return from(server).get(key)
    }

    fun put(server: Server, key: String, value: Any) : Boolean
    {
        val config = from(server)
        config.set(key, value)

        try
        {
            config.save()
        }
        catch (e: IOException)
        {
            System.err.println(e)
            return false
        }

        return true
    }

    fun remove(server: Server, key: String) : Boolean
    {
        val config = from(server)
        config.remove(key)

        try
        {
            config.save()
        }
        catch (e: IOException)
        {
            System.err.println(e)
            return false
        }

        return true
    }
}