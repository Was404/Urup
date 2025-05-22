package com.dk.urup

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.*
import java.security.Security
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import javax.crypto.CipherOutputStream
import javax.crypto.CipherInputStream

class CryptoManager(context: Context) {
    private val provider = BouncyCastleProvider()
    private val algorithm = "GOST3412-2015" // Или "GOST28147" ??
    private val transformation = "GOST3412-2015/CBC/PKCS5Padding"
    private val keySize = 256
    private val ivSize = 16

    init {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
        //Security.addProvider(JCP()) // Если использовать CryptoPro, зависимость gradle обновлен
    }

    fun generateKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(
            "PBKDF2WithHmacGOST3411", // Используйте алгоритм ГОСТ
            Security.getProvider("BC")
        )
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

        val cipher = Cipher.getInstance("$algorithm/CBC/PKCS7Padding", provider)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        FileInputStream(inputFile).use { input ->
            FileOutputStream(outputFile).use { output ->
                output.write(iv)
                val cos = CipherOutputStream(output, cipher)
                cos.use { it.write(input.readBytes()) }
            }
        }
    }

    fun decryptFile(inputFile: File, outputFile: File, key: SecretKeySpec) {
        FileInputStream(inputFile).use { input ->
            val iv = ByteArray(ivSize)
            input.read(iv)

            val cipher = Cipher.getInstance("$algorithm/CBC/PKCS7Padding", provider)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            val cis = CipherInputStream(input, cipher)
            FileOutputStream(outputFile).use { output ->
                cis.copyTo(output)
            }
        }
    }

    fun getEncryptedFiles(directory: File): List<File> {
        return directory.listFiles { file ->
            file.extension == "enc"
        }?.toList() ?: emptyList()
    }
}