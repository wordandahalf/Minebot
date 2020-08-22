package org.wordandahalf.minebot.games.retrievers

import org.json.JSONObject
import java.net.InetAddress
import java.net.InetSocketAddress

abstract class GameServerDataRetriever(val address: InetSocketAddress)
{
    /**
     * Connects to the retriever's [InetAddress] and returns a JSON object with the server's relevant information.
     *
     * If there is an error, set the key '`error`' in the [JSONObject] to a relevant explanation.
     */
    abstract fun retrieve(): JSONObject

    abstract fun getDefaultPort(): Int
}