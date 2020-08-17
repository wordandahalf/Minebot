package org.wordandahalf.minebot.games.retriever

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class MinecraftServerDataRetriever(address: InetSocketAddress) : GameServerDataRetriever(address) {
    private fun readVarInt(`in`: DataInputStream): Int {
        var i = 0
        var j = 0
        while (true) {
            val k: Int = `in`.readByte().toInt()
            i = i or (k and 0x7F shl j++ * 7)
            if (j > 5) throw RuntimeException("VarInt too big")
            if (k and 0x80 != 128) break
        }
        return i
    }

    private fun writeVarInt(out: DataOutputStream, paramInt: Int) {
        var value = paramInt
        while (true) {
            if (value and -0x80 == 0) {
                out.writeByte(value)
                return
            }
            out.writeByte(value and 0x7F or 0x80)
            value = value ushr 7
        }
    }
    
    override fun retrieve(): JSONObject
    {
        val legacyRetriever = LegacyMinecraftServerDataRetriever(address)
        val legacyData = legacyRetriever.retrieve()

        if(!legacyData.has("protocolVersion"))
            return legacyData

        var data = JSONObject()

        val conn = Socket()
        conn.connect(this.address)

        val i = DataInputStream(conn.getInputStream())
        val o = DataOutputStream(conn.getOutputStream())

        val b = ByteArrayOutputStream()
        val handshake = DataOutputStream(b)

        val hostname = this.address.hostString

        writeVarInt(handshake, 0)
        writeVarInt(handshake, -1)
        writeVarInt(handshake, hostname.length)
        handshake.writeBytes(hostname)
        handshake.writeShort(this.address.port)
        writeVarInt(handshake, 1)

        writeVarInt(o, b.size())
        o.write(b.toByteArray())

        // Write packet
        o.write(1) // length
        o.write(0) // ID

        val packetLength = readVarInt(i)
        if(packetLength == -1)
            return data.put("error", "Server closed the connection before data was transferred.")
        if(packetLength == 0)
            return data.put("error", "Server gave an invalid length of data to send.")

        val id = readVarInt(i)
        if(id != 0)
            return data.put("error", "Server provided an invalid response.")

        val jsonLength = readVarInt(i)
        if(jsonLength == -1)
            return data.put("error", "Server closed the connection before data was transferred.")
        if(packetLength == 0)
            return data.put("error", "Server gave an invalid length of data to send.")

        val jsonBytes = ByteArray(jsonLength)
        i.readFully(jsonBytes)

        data = JSONObject(String(jsonBytes))

        return data
    }

    override fun getDefaultPort(): Int
    {
        return 25565
    }
}