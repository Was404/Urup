package com.dk.urup

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.urup.databinding.ActivityMainBinding
import java.io.File
<<<<<<< HEAD
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import android.content.ActivityNotFoundException
import kotlinx.coroutines.cancel
import android.os.Environment
=======
>>>>>>> fcaa738 (Revert "fixed ANR +-")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cryptoManager: CryptoManager
<<<<<<< HEAD
    val externalPublicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val encryptedDir = File(externalPublicDir, "EncryptedFiles")

    private val requestCodePickFile = 101
    private val scope = CoroutineScope(Dispatchers.Main + Job())
=======
    private val encryptedDir by lazy { File(filesDir, "encrypted") }
>>>>>>> fcaa738 (Revert "fixed ANR +-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка входа пользователя перед остальным кодом:
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            // Пользователь не вошёл — запускаем AuthActivity и завершаем MainActivity:
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return // чтобы не выполнять остальной код в onCreate.
        }

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
<<<<<<< HEAD
            .show()
    }

    private fun decryptAndOpenFile(encryptedFile: File) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val decryptedFile =
                    File(filesDir, "decrypted_${encryptedFile.nameWithoutExtension}")
                val key = cryptoManager.generateKey("password", "salt".toByteArray())
                cryptoManager.decryptFile(encryptedFile, decryptedFile, key)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.file_decrypted, decryptedFile.path),
                        Toast.LENGTH_SHORT
                    ).show()
                    openInFileManager(decryptedFile)
                }
            }
        }
    }

    private fun openInFileManager(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "*/*"
        }
=======
        )
>>>>>>> fcaa738 (Revert "fixed ANR +-")
    }

    private fun refreshFileList() {
        (binding.fileRecyclerView.adapter as? FileAdapter)?.updateItems(
            cryptoManager.getEncryptedFiles(encryptedDir)
        )
    }
}