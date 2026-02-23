package org.kasumi321.ushio.phitracker.data.parser

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import org.kasumi321.ushio.phitracker.data.api.CryptoConstants

/**
 * AES-256-CBC 解密器
 * 存档每个文件: 第 1 字节为 version, 剩余为 AES 密文
 */
@Singleton
class AesDecryptor @Inject constructor() {

    private val secretKey = SecretKeySpec(CryptoConstants.AES_KEY, "AES")
    private val ivSpec = IvParameterSpec(CryptoConstants.AES_IV)

    /**
     * 解密存档文件数据
     * @return Pair<version, decryptedData>
     */
    fun decrypt(data: ByteArray): Pair<Int, ByteArray> {
        val version = data[0].toInt() and 0xFF
        val cipherText = data.copyOfRange(1, data.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decrypted = cipher.doFinal(cipherText)
        return version to decrypted
    }

    /**
     * 加密存档文件数据 (用于回传)
     */
    fun encrypt(version: Int, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encrypted = cipher.doFinal(data)
        return byteArrayOf(version.toByte()) + encrypted
    }
}
