package br.com.assistpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.DecimalFormat
import java.util.Locale

data class FileEntry(val file: File, val diretorio: Boolean)

class FileAdapter(private val onClick: (FileEntry) -> Unit) :
    RecyclerView.Adapter<FileAdapter.Holder>() {

    private val itens = ArrayList<FileEntry>()

    fun submit(novos: List<FileEntry>) {
        itens.clear()
        itens.addAll(novos)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = itens.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(itens[position])
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val icone: ImageView = view.findViewById(R.id.file_icone)
        private val nome: TextView = view.findViewById(R.id.file_nome)
        private val meta: TextView = view.findViewById(R.id.file_meta)

        fun bind(e: FileEntry) {
            nome.text = e.file.name
            if (e.diretorio) {
                icone.setImageResource(R.drawable.ic_folder)
                meta.text = "Pasta"
            } else {
                val ext = e.file.extension.lowercase(Locale.ROOT)
                icone.setImageResource(if (ext == "pdf") R.drawable.ic_pdf else R.drawable.ic_image)
                meta.text = tamanho(e.file.length()) + "  •  " + ext.uppercase(Locale.ROOT)
            }
            itemView.setOnClickListener { onClick(e) }
        }
    }

    companion object {
        fun tamanho(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return DecimalFormat("0").format(kb) + " KB"
            val mb = kb / 1024.0
            return DecimalFormat("0.0").format(mb) + " MB"
        }

        fun imagemOuPdf(f: File): Boolean {
            if (f.isDirectory) return true
            val ext = f.extension.lowercase(Locale.ROOT)
            return ext in setOf("jpg", "jpeg", "png", "webp", "pdf")
        }
    }
}
