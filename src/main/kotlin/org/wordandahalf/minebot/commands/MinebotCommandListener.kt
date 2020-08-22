package org.wordandahalf.minebot.commands

import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import org.wordandahalf.minebot.commands.link.GetLinkedCommand
import org.wordandahalf.minebot.commands.link.LinkCommand
import org.wordandahalf.minebot.commands.link.UnlinkCommand

class MinebotCommandListener : MessageCreateListener
{
    companion object
    {
        // Aliases and commands are registered to the same HashMap
        private val commandRegistry = HashMap<List<String>, MinebotCommand>()

        fun register(command: MinebotCommand)
        {
            val names = ArrayList<String>()
            names.add(command.name)

            if(command.getAliases() != null)
                names.addAll(command.getAliases()!!)

            if (commandRegistry.putIfAbsent(names, command) != null) System.err.println("Tried to re-register command ${command.javaClass.simpleName}!")
        }

        fun getRegisteredCommands() : MutableCollection<MinebotCommand>
        {
            return commandRegistry.values
        }

        // A 'init' block inside of a companion object is equivalent to a static initializer in Java
        init
        {
            register(HelpCommand())
            register(ConfigCommand())
            register(PingCommand())
            register(LinkCommand())
            register(UnlinkCommand())
            register(GetLinkedCommand())
        }
    }

    override fun onMessageCreate(e: MessageCreateEvent)
    {
        val msg = e.message
        val s = if (e.server.isPresent) e.server.get() else return

        if(!msg.content.startsWith(MinebotCommand.PREFIX))
            return

        val commandWithArguments = msg.content.substring(MinebotCommand.PREFIX.length).split(" ")
        val commandName = commandWithArguments[0]

        // Get key(s) that contain the command used
        val possibleKeys = commandRegistry.filterKeys { it.contains(commandName) }.keys

        if(possibleKeys.isEmpty())
            return

        // There should only ever be one, if there isn't let someone know!
        if(possibleKeys.size > 1)
        {
            System.err.println("Found collision for command '${commandName}'!")
            return
        }

        val command = commandRegistry[possibleKeys.elementAt(0)] ?: return

        if(command.getParameterCount().contains(commandWithArguments.size - 1))
            command.onExecuted(e, s, e.channel, commandWithArguments)
        else
            command.printUsage(e.channel)
    }
}