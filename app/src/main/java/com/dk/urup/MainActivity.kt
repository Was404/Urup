package com.dk.urup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.urup.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cryptoManager: CryptoManager
    private val encryptedDir by lazy { File(filesDir, "encrypted") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация крипто-менеджера // Исправлены опечатки
        cryptoManager = CryptoManager(this)

        // Создание директории
        if (!encryptedDir.exists()) encryptedDir.mkdirs()

        setupFileList()

        // Работа с FAB
        binding.fab.setOnClickListener { // Теперь идентификатор существует
            val originalFile = File(filesDir, "test.txt").apply {
                writeText("Secret data")
            }
            val encryptedFile = File(encryptedDir, "test.enc")

            val key = cryptoManager.generateKey("strong_password", "salt".toByteArray())
            cryptoManager.encryptFile(originalFile, encryptedFile, key)
            refreshFileList()
        }
    }

    private fun setupFileList() {
        binding.fileRecyclerView.layoutManager = LinearLayoutManager(this) // Исправлен ID
        binding.fileRecyclerView.adapter = FileAdapter(
            items = cryptoManager.getEncryptedFiles(encryptedDir),
            onItemClick = { file ->
                val decryptedFile = File(filesDir, "decrypted_${file.nameWithoutExtension}")
                val key = cryptoManager.generateKey("strong_password", "salt".toByteArray())
                cryptoManager.decryptFile(file, decryptedFile, key)
                Toast.makeText(this, "Файл расшифрован: ${decryptedFile.path}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun refreshFileList() {
        (binding.fileRecyclerView.adapter as? FileAdapter)?.updateItems(
            cryptoManager.getEncryptedFiles(encryptedDir)
        )
    }
}