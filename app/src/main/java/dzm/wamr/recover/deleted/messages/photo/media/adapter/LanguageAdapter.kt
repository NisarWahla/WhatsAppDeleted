package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.RowItemLanguageBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.DataModel
import dzm.wamr.recover.deleted.messages.photo.media.model.LanguageDM
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import java.util.*

class LanguageAdapter(private val list: ArrayList<LanguageDM>, val listener:AdapterClicklistener) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    class ViewHolder(val binding:RowItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.tabSelection.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: RowItemLanguageBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.row_item_language,parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun filter(updatedList: ArrayList<LanguageDM>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model: LanguageDM = list.get(position)
        holder.binding.tvTitle.setText(model.languageName)
        try {
            holder.binding.tvFlag.setText(model.languageFlag)
        } catch (e: Exception) {
            holder.binding.tvFlag.setText("")
        }
        if (model.selection)
        {
            holder.binding.tabBackground.background=ContextCompat.getDrawable(holder.binding.root.context,R.drawable.ractengle_stroke_solid_light_primary)
            holder.binding.ivSelection.setImageDrawable(ContextCompat.getDrawable(holder.binding.root.context,R.drawable.ic_selected))
        }
        else
        {
            holder.binding.tabBackground.background=ContextCompat.getDrawable(holder.binding.root.context,R.drawable.ractengle_solid_white)
            holder.binding.ivSelection.setImageDrawable(ContextCompat.getDrawable(holder.binding.root.context,R.drawable.ic_unselected))
        }

        holder.bind(listener,position)
    }

}