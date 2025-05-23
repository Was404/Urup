package com.dk.urup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dk.urup.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import android.content.ActivityNotFoundException
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cryptoManager: CryptoManager
    private val encryptedDir by lazy { File(filesDir, "encrypted") }
    private val requestCodePickFile = 101
    private val scope = CoroutineScope(Dispatchers.Main + Job())

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

        cryptoManager = CryptoManager(this)
        if (!encryptedDir.exists()) encryptedDir.mkdirs()

        setupFileList()

        binding.fab.setOnClickListener {
            scope.launch {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                @Suppress("DEPRECATION")
                startActivityForResult(intent, requestCodePickFile)
            }
        }
    }

    @Deprecated("Deprecated in super class")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodePickFile && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        processSelectedFile(uri)
                    }
                    refreshFileList()
                }
            }
        }
    }

    private suspend fun processSelectedFile(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"
            val originalFile = File(filesDir, fileName)
            FileOutputStream(originalFile).use { output ->
                copyFileWithBuffer(inputStream, output)
            }
            val encryptedFile = File(encryptedDir, "$fileName.enc")
            val key = cryptoManager.generateKey("password", "salt".toByteArray())
            cryptoManager.encryptFile(originalFile, encryptedFile, key)
        }
    }

    private fun getFileName(uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else null
                } else null
            }
            "file" -> uri.lastPathSegment
            else -> null
        }
    }

    private fun setupFileList() {
        binding.fileRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.fileRecyclerView.adapter = FileAdapter(
            onItemClick = { file -> showFileActionsDialog(file) }
        )
        refreshFileList()
    }

    private fun showFileActionsDialog(file: File) {
        val options = arrayOf(getString(R.string.decrypt), getString(R.string.open_in_explorer))
        AlertDialog.Builder(this)
            .setTitle(R.string.select_action)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> decryptAndOpenFile(file)
                    1 -> openInFileManager(file)
                }
            }
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
    }

    private fun refreshFileList() {
        scope.launch {
            val files = withContext(Dispatchers.IO) {
                cryptoManager.getEncryptedFiles(encryptedDir)
            }
            (binding.fileRecyclerView.adapter as? FileAdapter)?.submitList(files)
        }
    }

    private suspend fun copyFileWithBuffer(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}