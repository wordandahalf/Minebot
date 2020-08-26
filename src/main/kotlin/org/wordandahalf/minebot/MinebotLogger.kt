package org.wordandahalf.minebot

import org.javacord.api.entity.channel.ServerTextChannel
import org.javacord.api.entity.server.Server
import java.lang.IllegalArgumentException

object MinebotLogger
{
    private val cachedLevels = HashMap<Server, Level>()

    enum class Level
    {
        DEBUG,
        NORMAL,
        ERROR,
    }

    fun setLevel(s: Server, level: Level)
    {
        MinebotConfig.put(s, "logging.level", level.toString())
        cachedLevels[s] = level
    }

    fun getLevel(s: Server) : Level
    {
        return cachedLevels.getOrElse(s, {
            val levelString = MinebotConfig.get(s, "logging.level")

            if(levelString is String)
            {
                try
                {
                    return Level.valueOf(levelString)
                }
                catch (e: IllegalArgumentException)
                {
                    val textChannel = s.channels.filter { it.asServerTextChannel().isPresent }

                    if(textChannel.isNotEmpty())
                    {
                        error(textChannel[0].asServerTextChannel().get(), "Found an invalid key in 'logging.level'!")
                    }
                }
            }

            MinebotConfig.put(s, "logging.level", Level.NORMAL.toString())
            cachedLevels[s] = Level.NORMAL

            return Level.NORMAL
        })
    }

    fun debug(c: ServerTextChannel, msg: String)
    {
        if(getLevel(c.server) > Level.DEBUG)
            return

        c.sendMessage("```ini\n[ $msg ]```")
    }

    fun log(c: ServerTextChannel, msg: String)
    {
        if(getLevel(c.server) > Level.NORMAL)
            return

        c.sendMessage(msg)
    }

    fun success(c: ServerTextChannel, msg: String)
    {
        c.sendMessage("```diff\n+ $msg ```")
    }

    fun error(c: ServerTextChannel, msg: String)
    {
        c.sendMessage("```diff\n- $msg ```")
    }
}