package dzm.wamr.recover.deleted.messages.photo.media.adapter

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ChatAdapter.MyViewHoder
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ChatLayoutBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.Model
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler

class ChatAdapter(var list: List<Model>, val listener: AdapterClicklistener) :
    RecyclerView.Adapter<MyViewHoder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHoder {
        val binding:ChatLayoutBinding =DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.chat_layout, parent, false)
        return MyViewHoder(binding)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: MyViewHoder, position: Int) {
        val model = list[position]
        val sp = SpannableStringBuilder("")
        val sb = SpannableStringBuilder("")
        if (model.getIdDeleted()) {
//            holder.msg.setTextColor(holder.msg.getContext().getColor(R.color.colorAccent));
            //        holder.time.setText(model.getTime());
            if (model.getType() == "group") {
                sp.append(model.getMbrGrp())

//              Span to set text color to some RGB value
                val fcs = ForegroundColorSpan(Color.rgb(216, 27, 96))

//            Span to make text bold
                val bss = StyleSpan(Typeface.BOLD)

//               Set the text color for first 4 characters
                sp.setSpan(fcs, 0, model.getMbrGrp().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

//             make them also bold
                sp.setSpan(bss, 0, model.getMbrGrp().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                //                sp.append(": ");
                sp.append("\n")
                if (model.getMsg().contains("\uD83D\uDCF7")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_chat_img)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 40, 40)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    sp.append(" Image")
                } else if (model.getMsg().contains("\uD83C\uDFA5")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_video_chat)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 40, 40)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    val str = model.getMsg().replace("\uD83C\uDFA5", "")
                    sp.append(" $str")
                } else if (model.getMsg().contains("\uD83D\uDCC4")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_file_chat)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 30, 40)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    val str = model.getMsg().replace("\uD83D\uDCC4", "")
                    sp.append(" $str")
                } else if (model.getMsg().contains("\uD83C\uDFB5")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_audio_chat)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 20, 35)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    val str = model.getMsg().replace("\uD83C\uDFB5", "")
                    sp.append(" $str")
                } else {
                    sp.append(model.getMsg())
                }
            } else if (model.getMsg().contains("\uD83D\uDCF7")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_chat_img)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 30, 30)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                sp.append(" Image")
            } else if (model.getMsg().contains("\uD83C\uDFA5")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_video_chat)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 40, 40)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val str = model.getMsg().replace("\uD83C\uDFA5", "")
                sp.append(" $str")
            } else if (model.getMsg().contains("\uD83D\uDCC4")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_file_chat)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 30, 40)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val str = model.getMsg().replace("\uD83D\uDCC4", "")
                sp.append(" $str")
            } else if (model.getMsg().contains("\uD83C\uDFB5")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_audio_chat)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 20, 35)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val str = model.getMsg().replace("\uD83C\uDFB5", "")
                sp.append(" $str")
            } else {
                sp.append(model.getMsg())
            }
            sp.append("\n")
            holder.binding.chatNameOUserTv.text = sp
            //            String sb = "\n" +
//                    "This message was deleted" +
//                    " " +
//                    model.getTime();
            val del = "This message was deleted"
            sb.append(del)
            sb.append("  ")

//              Span to set text color to some RGB value
            val fcs = ForegroundColorSpan(Color.rgb(216, 27, 96))

//            Span to make text bold
            val bss = StyleSpan(Typeface.BOLD)

//               Set the text color for first 4 characters
            sb.setSpan(fcs, 0, del.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

//             make them also bold
            sb.setSpan(bss, 0, del.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.append(model.getTime())
            holder.binding.tvTime.text = sb
        } else {
//            holder.msg.setTextColor(holder.msg.getContext().getColor(R.color.black));
            //        holder.time.setText(model.getTime());
            if (model.getType() == "group") {
                sp.append(model.getMbrGrp())
                //                sp.append(": ");
                sp.append("\n")
                if (model.getMsg().contains("\uD83D\uDCF7")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_chat_img)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 40, 40)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    sp.append(" Image")
                } else if (model.getMsg().contains("\uD83C\uDFA5")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_video_chat)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 40, 40)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    val str = model.getMsg().replace("\uD83C\uDFA5", "")
                    sp.append(" $str")
                } else if (model.getMsg().contains("\uD83D\uDCC4")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_file_chat)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 30, 40)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    val str = model.getMsg().replace("\uD83D\uDCC4", "")
                    sp.append(" $str")
                } else if (model.getMsg().contains("\uD83C\uDFB5")) {
                    val drawables = arrayOfNulls<Drawable>(1)
                    drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_audio_chat)
                    for (i in drawables.indices) {
                        val drawable = drawables[i]
                        drawable!!.setBounds(0, 0, 20, 35)
                        val newStr = "$drawable "
                        sp.append(newStr)
                        sp.setSpan(
                            ImageSpan(drawable),
                            sp.length - newStr.length,
                            sp.length - " ".length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                    val str = model.getMsg().replace("\uD83C\uDFB5", "")
                    sp.append(" $str")
                } else {
                    sp.append(model.getMsg())
                }
            } else if (model.getMsg().contains("\uD83D\uDCF7")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_chat_img)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 30, 30)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                sp.append(" Image")
            } else if (model.getMsg().contains("\uD83C\uDFA5")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_video_chat)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 40, 40)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val str = model.getMsg().replace("\uD83C\uDFA5", "")
                sp.append(" $str")
            } else if (model.getMsg().contains("\uD83D\uDCC4")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_file_chat)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 30, 40)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val str = model.getMsg().replace("\uD83D\uDCC4", "")
                sp.append(" $str")
            } else if (model.getMsg().contains("\uD83C\uDFB5")) {
                val drawables = arrayOfNulls<Drawable>(1)
                drawables[0] = ContextCompat.getDrawable(holder.binding.root.context, R.drawable.ic_audio_chat)
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    drawable!!.setBounds(0, 0, 20, 35)
                    val newStr = "$drawable "
                    sp.append(newStr)
                    sp.setSpan(
                        ImageSpan(drawable),
                        sp.length - newStr.length,
                        sp.length - " ".length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
                val str = model.getMsg().replace("\uD83C\uDFB5", "")
                sp.append(" $str")
            } else {
                sp.append(model.getMsg())
            }
            sp.append("\n")
            holder.binding.chatNameOUserTv.text = sp
            holder.binding.tvTime.text = model.getTime()
        }

        holder.bind(listener,position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MyViewHoder(val binding:ChatLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listener: AdapterClicklistener, position: Int) {
            binding.incomingLayoutBubble.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.tvTime.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
            binding.chatNameOUserTv.setOnClickListener(DebounceClickHandler(View.OnClickListener {
                listener.onItemClick(it,position)
            }))
        }
    }
}