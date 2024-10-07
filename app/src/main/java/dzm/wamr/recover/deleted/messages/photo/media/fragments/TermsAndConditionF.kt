package dzm.wamr.recover.deleted.messages.photo.media.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentTermsAndConditionBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.Const


class TermsAndConditionF : Fragment() {

    companion object {
        fun newInstance(): TermsAndConditionF {
            val fragment = TermsAndConditionF()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var binding: FragmentTermsAndConditionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_terms_and_condition,container,false)
        binding.webView.loadUrl("file:///android_asset/${Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE)}/term.html")
        return binding.root
    }
}