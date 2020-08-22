package org.wordandahalf.minebot.games.retrievers

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
        // Due to the rarity that the bot will be used to ping a legacy server,
        // this may be put after the following code eventually.
        val legacyRetriever = LegacyMinecraftServerDataRetriever(address)
        val legacyData = legacyRetriever.retrieve()

        // 'Modern' servers' responses always has 'protcolVersion'
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

        // Write the ping packet.
        // Minecraft's uncompressed packets are very, very simple, bar encoding.
        // First, the total length of the packet is encoded as a Var(iable-length)Int,
        // then the packet ID is written as a VarInt. The data is written after.

        // Here, the data is first written to a ByteArrayOutputStream before being written to the Socket
        // so that the data's length can be calculated, since VarInts take a variable length to encode.

        writeVarInt(handshake, 0)           // Packet ID
        writeVarInt(handshake, -1)          // Protocl Version (set to -1, as per wiki.vg)
                                                    // Strings are encoded as UTF-8, with their lengths written first as VarInts
        writeVarInt(handshake, hostname.length)     // The length of the server's hostname
        handshake.writeBytes(hostname)              // The server's hostname
        handshake.writeShort(this.address.port)     // The port of the server, as a short
        writeVarInt(handshake, 1)           // Next state, set to one to indicate status.

        writeVarInt(o, b.size())                    // First, write the length of the packet
        o.write(b.toByteArray())                    // Then write the data (with the ID prepended)

        // Next, a 'request' packet is written that has no data.

        o.write(1) // length
        o.write(0) // ID

        // First, read the total length of the packet as a VarInt
        val packetLength = readVarInt(i)
        if(packetLength == -1)
            return data.put("error", "Server closed the connection before data was transferred.")
        if(packetLength == 0)
            return data.put("error", "Server gave an invalid length of data to send.")

        // Then read its ID.
        val id = readVarInt(i)
        if(id != 0)
            return data.put("error", "Server provided an invalid response.")

        // The data in the response is simply JSON
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