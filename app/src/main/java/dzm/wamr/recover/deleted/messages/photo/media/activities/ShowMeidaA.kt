package dzm.wamr.recover.deleted.messages.photo.media.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ViewPagerAdapter
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityShowMeidaBinding
import dzm.wamr.recover.deleted.messages.photo.media.fragments.MediaItemF
import dzm.wamr.recover.deleted.messages.photo.media.util.AppCompatLocaleActivity
import dzm.wamr.recover.deleted.messages.photo.media.util.Const
import dzm.wamr.recover.deleted.messages.photo.media.util.DebounceClickHandler
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils
import kotlinx.coroutines.*

class ShowMeidaA : AppCompatLocaleActivity() {

    lateinit var binding:ActivityShowMeidaBinding
    var adapter:ViewPagerAdapter? =null
    var tabPositon=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_show_meida)

        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.ivBack.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
    }

    private fun initControl() {
        if (intent.hasExtra("tabPositon"))
        {
            tabPositon=intent.getIntExtra("tabPositon",0)
        }

        CoroutineScope(Dispatchers.Main).async {
            initializeNavController()
        }
    }

    private fun initializeNavController() {
        adapter = ViewPagerAdapter(this)
        registerFragmentWithPager()
        binding.viewPager.offscreenPageLimit=3
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

        CoroutineScope(Dispatchers.Main).async {
            binding.viewPager.setCurrentItem(tabPositon)
        }
    }

    private fun registerFragmentWithPager() {
        adapter?.addFrag(MediaItemF.newInstance("WhatsApp/Media/WhatsApp Images/"))
        adapter?.addFrag(MediaItemF.newInstance("WhatsApp/Media/WhatsApp Animated Gifs/"))
        adapter?.addFrag(MediaItemF.newInstance("WhatsApp/Media/WhatsApp Video/"))
        adapter?.addFrag(MediaItemF.newInstance("WhatsApp/Media/WhatsApp Audio/"))
    }



}