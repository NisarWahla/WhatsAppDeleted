package dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments

import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.MainActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ViewPagerAdapter
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentNewMediaBinding
import dzm.wamr.recover.deleted.messages.photo.media.fragments.MediaItemF

class NewMediaFragment : Fragment() {

    lateinit var binding: FragmentNewMediaBinding
    var adapter:ViewPagerAdapter? =null
    var tabImages:MediaItemF? =null
    var tabGifs:MediaItemF? =null
    var tabVideos:MediaItemF? =null
    var tabAudios:MediaItemF? =null

    companion object {
        const val ACTION_NEW_MEDIA = "ACTION_NEW_MEDIA"
        fun newInstance(): NewMediaFragment {
            val fragment = NewMediaFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_new_media,container,false)
        initControl()
        return binding.root
    }

    private fun initControl() {
        initializeNavController()

    }
    private fun initializeNavController() {

        adapter = ViewPagerAdapter(this)
        registerFragmentWithPager()
        binding.viewPager.offscreenPageLimit=1
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tablayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = binding.root.context.getString(R.string.images)
                }
                1 -> {
                    tab.text = binding.root.context.getString(R.string.gif)
                }
                2 -> {
                    tab.text = binding.root.context.getString(R.string.video)
                }
                3 -> {
                    tab.text = binding.root.context.getString(R.string.audios)
                }
            }
        }.attach()
    }

    private fun registerFragmentWithPager() {
        tabImages=MediaItemF.newInstance("WhatsApp/Media/WhatsApp Images/")
        tabGifs=MediaItemF.newInstance("WhatsApp/Media/WhatsApp Animated Gifs/")
        tabVideos=MediaItemF.newInstance("WhatsApp/Media/WhatsApp Video/")
        tabAudios=MediaItemF.newInstance("WhatsApp/Media/WhatsApp Audio/")

        tabImages?.let {
            adapter?.addFrag(it)
        }
        tabGifs?.let {
            adapter?.addFrag(it)
        }
        tabVideos?.let {
            adapter?.addFrag(it)
        }
        tabAudios?.let {
            adapter?.addFrag(it)
        }
    }


    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible)
        {
            Handler(Looper.getMainLooper()).postDelayed({
                tabImages?.ReLoadList()
                tabGifs?.ReLoadList()
                tabVideos?.ReLoadList()
                tabAudios?.ReLoadList()
            },200)
        }
    }
}