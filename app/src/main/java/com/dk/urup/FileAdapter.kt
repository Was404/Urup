package com.dk.urup  // Проверьте, что путь к файлу совпадает с этим пакетом

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    private var items: List<File>,
    private val onItemClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {  // Наследуемся от правильного класса

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)  // Убедитесь что ID существует в item_file.xml
    }

    fun updateItems(newItems: List<File>) {
        items = newItems
        notifyDataSetChanged()  // Теперь метод используется
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)  // Проверьте существование макета
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = items[position]
        holder.fileName.text = file.name
        holder.itemView.setOnClickListener { onItemClick(file) }
    }

    override fun getItemCount(): Int = items.size
}