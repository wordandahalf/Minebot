package org.wordandahalf.minebot.games.retriever

import org.json.JSONObject
import java.io.InputStreamReader
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * Retrieves data from a Minecraft server using the legacy ping protocol.
 */
class LegacyMinecraftServerDataRetriever(address: InetSocketAddress) : GameServerDataRetriever(address)
{
    override fun retrieve(): JSONObject
    {
        val json = JSONObject()

        try
        {
            val conn = Socket()
            conn.connect(address, 5000)

            conn.getOutputStream().write(byteArrayOf(0xFE.toByte(), 0x01))
            conn.getOutputStream().flush()

            // There's got to be a better way to read a single byte...
            val id = ByteArray(1)
            conn.getInputStream().read(id, 0, 1)

            if(id.size != 1 || id[0].toInt() != -1) // Wrong ID. The first byte sent must be 0xFF
            {
                json.put("error", "Invalid ID byte!")
                return json
            }

            val i = InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_16BE)

            val length = i.read()
            if(length < 1) // Not much of input validation, but better than it succeeding with no data
            {
                json.put("error", "Could not read data!")
                return json
            }

            val chars = CharArray(length)
            i.read(chars, 0, length)

            var dataString = String(chars)
            if(dataString.length != length)
            {
                json.put("error", "Read invalid data!")
                return json
            }

            if(dataString.startsWith("§1"))
            {
                dataString = dataString.substring(3)
                val data = dataString.split(0.toChar())

                json.put("protocolVersion", data[0])
                json.put("gameVersion", data[1])
                json.put("motd", data[2])
                json.put("playerCount", data[3])
                json.put("playerLimit", data[4])
            }
            else
            {
                // We've got legacy data
                val data = dataString.split("§")

                json.put("motd", data[0])
                json.put("playerCount", data[1])
                json.put("playerLimit", data[2])
            }

            conn.close()

            return json
        }
        catch (e: Exception)
        {
            json.put("error", e.toString())
            return json
        }
    }

    override fun getDefaultPort(): Int
    {
        return 25565
    }
}