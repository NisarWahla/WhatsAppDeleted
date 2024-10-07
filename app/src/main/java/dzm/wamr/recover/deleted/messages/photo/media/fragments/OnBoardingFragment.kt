package dzm.wamr.recover.deleted.messages.photo.media.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentOnBoardingBinding


class OnBoardingFragment : Fragment() {

    companion object {
        private const val ARG_POSITION = "ARG_POSITION"

        fun getInstance(position: Int) = OnBoardingFragment().apply {
            arguments = bundleOf(ARG_POSITION to position)
        }
    }
    lateinit var binding: FragmentOnBoardingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_on_boarding,container,false)
        initControl()
        return binding.root
    }

    private fun initControl() {
        val position = requireArguments().getInt(ARG_POSITION)
        val onBoardingTitles = binding.root.context.resources.getStringArray(R.array.onboarding_titles)
        val onBoardingTexts = binding.root.context.resources.getStringArray(R.array.onboarding_texts)
        with(binding) {
            tvTitle.text = onBoardingTitles[position]
            tvContent.text = onBoardingTexts[position]
            when(position){
                0->Illustration.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.first_illustration))
                1->Illustration.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.second_illustration))
                2->Illustration.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.third_illustration))
                else -> Illustration.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.third_illustration))
            }

        }
    }
}