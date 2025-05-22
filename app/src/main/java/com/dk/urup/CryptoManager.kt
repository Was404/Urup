package com.dk.urup

import android.content.Context
import java.io.*
import java.security.SecureRandom
import java.security.Security
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.jce.provider.BouncyCastleProvider

class CryptoManager(context: Context) {
    private val provider = BouncyCastleProvider()
    private val algorithm = "GOST3412-2015"
    private val transformation = "$algorithm/CBC/PKCS5Padding"
    private val keySize = 256
    private val ivSize = 16

    init {
        Security.removeProvider("BC")
        Security.addProvider(provider)
    }

    fun generateKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacGOST3411", provider)
        val spec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            65536,
            keySize
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, algorithm)
    }

    fun encryptFile(inputFile: File, outputFile: File, key: SecretKeySpec) {
        val iv = ByteArray(ivSize).apply {
            SecureRandom().nextBytes(this)
        }

        val cipher = Cipher.getInstance(transformation, provider)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        FileInputStream(inputFile).use { input ->
            FileOutputStream(outputFile).use { output ->
                output.write(iv)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                CipherOutputStream(output, cipher).use { cos ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        cos.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    fun decryptFile(inputFile: File, outputFile: File, key: SecretKeySpec) {
        FileInputStream(inputFile).use { input ->
            val iv = ByteArray(ivSize)
            input.read(iv)

            val cipher = Cipher.getInstance(transformation, provider)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            val buffer = ByteArray(4096)
            var bytesRead: Int
            CipherInputStream(input, cipher).use { cis ->
                FileOutputStream(outputFile).use { output ->
                    while (cis.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    fun getEncryptedFiles(directory: File): List<File> {
        return directory.listFiles { file ->
            file.extension.equals("enc", ignoreCase = true)
        }?.toList() ?: emptyList()
    }
}