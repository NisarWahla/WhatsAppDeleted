package dzm.wamr.recover.deleted.messages.photo.media.util

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher

class DelayedTextWatcher(private val delayMillis: Long = 1000L, private val onTextChanged: (String) -> Unit) :
    TextWatcher {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
        // No implementation needed
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        // No implementation needed
    }

    override fun afterTextChanged(editable: Editable?) {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable { onTextChanged.invoke(editable.toString()) }
        handler.postDelayed(runnable!!, delayMillis)
    }


}