package org.kasumi321.ushio.phitracker.data.parser

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 二进制读取器
 * 支持 Phigros 存档的 varshort 编码和 bit-packed booleans
 */
class BinaryReader(data: ByteArray) {
    private val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

    val position: Int get() = buffer.position()
    val remaining: Int get() = buffer.remaining()

    fun readByte(): Int = buffer.get().toInt() and 0xFF

    fun readShort(): Int = buffer.getShort().toInt() and 0xFFFF

    fun readInt(): Int = buffer.getInt()

    fun readFloat(): Float = buffer.getFloat()

    fun readBoolean(): Boolean = readByte() != 0

    /**
     * varshort 编码:
     * 0-127: 单字节
     * 128+: (0x80 | n & 0x7F), (n >> 7)
     */
    fun readVarShort(): Int {
        val first = readByte()
        return if (first < 128) first else (first and 0x7F) or (readByte() shl 7)
    }

    fun readString(): String {
        val length = readVarShort()
        val bytes = ByteArray(length)
        buffer.get(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    /**
     * 读取带末尾偏移的字符串 (gameRecord 使用 end=2)
     * C 库中 read_string(ptr, end) - 结尾截掉 end 个字节
     */
    fun readStringTrimEnd(trimBytes: Int): String {
        val length = readVarShort()
        val bytes = ByteArray(length)
        buffer.get(bytes)
        return String(bytes, 0, length - trimBytes, Charsets.UTF_8)
    }

    fun hasRemaining(): Boolean = buffer.hasRemaining()

    fun skip(n: Int) {
        buffer.position(buffer.position() + n)
    }
}
