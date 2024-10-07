package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.LanguageAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.BannerManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityLanguageBinding
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.LanguageDM
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.hideKeyboard

class LanguageA : AppCompatLocaleActivity() {

    lateinit var binding:ActivityLanguageBinding
    var dataList:ArrayList<LanguageDM> = ArrayList()
    var searchlist:ArrayList<LanguageDM> = ArrayList()
    var adapter: LanguageAdapter? =null
    var selectedLanguage:LanguageDM?=null
    var whereFrom=""
    var prefManager: PrefManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_language)

        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.ivBack.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
        binding.ivCross.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
        binding.ivDone.setOnClickListener(DebounceClickHandler(View.OnClickListener {
           if (selectedLanguage==null)
           {
               Utils.showToast(binding.root.context,binding.root.context.getString(R.string.must_select_atleast_one_country))
           }
            else
           {
               applySelectedLanguage()
           }
        }))


        binding.etSearch.addTextChangedListener(
            DelayedTextWatcher(delayMillis = 500) { text ->
                applyAdapterFilter("${text}")
            }
        )
        binding.ivCloseSearch.setOnClickListener (DebounceClickHandler(View.OnClickListener {
            clearSearch(true)
        }))

        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearSearch()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun clearSearch(isTextClear:Boolean=false) {
        if (isTextClear)
        {
            binding.etSearch.setText("")
        }
        binding.root.context?.hideKeyboard(binding.etSearch)
        binding.etSearch.clearFocus()
    }

    private fun applySelectedLanguage() {
        if (selectedLanguage!=null)
        {
            Prefs.putString(Const.APP_LANGUAGE,selectedLanguage!!.languageName)
            Prefs.putString(Const.APP_LANGUAGE_CODE,selectedLanguage!!.languageCode)
            Prefs.putBoolean(Const.KEY_IS_LANGUAGE_SET,true)

            if (whereFrom.equals("Splash"))
            {
                if (!prefManager!!.isFirstTimeLaunch())
                {
                    applyLanguageChange(MainActivity::class.java)
                }
                else
                {
                    applyLanguageChange(WelcomeActivity::class.java)
                }
            }
            else
            {
                applyLanguageChange(MainActivity::class.java)
            }

        }
        else
        {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.must_select_atleast_one_country))
        }
    }

    private fun applyLanguageChange(className: Class<*>) {
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, className,true)
    }

    private fun initControl() {
        prefManager = PrefManager(binding.root.context)
        if (intent.hasExtra("whereFrom"))
        {
            whereFrom= intent.getStringExtra("whereFrom").toString()
        }
        if (whereFrom.equals("Splash"))
        {
            binding.ivCross.visibility=View.VISIBLE
            binding.ivBack.visibility=View.GONE
        }
        else
        {
            binding.ivCross.visibility=View.GONE
            binding.ivBack.visibility=View.VISIBLE
        }
        setupAdapter()
    }

    var avoidMultipleSelection=true
    private fun setupAdapter() {
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context).apply { orientation=
            LinearLayoutManager.VERTICAL }
        adapter= LanguageAdapter(dataList, object : AdapterClicklistener {
            override fun onItemClick(view: View, position: Int) {
                selectedLanguage=dataList.get(position)
                if (avoidMultipleSelection)
                {
                    avoidMultipleSelection=false


//                    get old selected position
                    var oldPosition=-1

                    for (item in dataList)
                    {
                       if (item.selection)
                       {
                           oldPosition=dataList.indexOf(item)
                       }
                    }

                    if (oldPosition==-1)
                    {
                        Log.d(Const.tag,"No Old Position Found")
                        if ( selectedLanguage!=null)
                        {
                            selectedLanguage!!.selection=true
                            dataList.set(position,selectedLanguage!!)
                            updateSearchList(selectedLanguage!!)
                            adapter?.notifyItemChanged(position)
                        }
                    }
                    else
                    {
                        if (oldPosition==position)
                        {
                            Log.d(Const.tag,"Same Position Select")
                        }
                        else
                        {
                            val oldItem=dataList.get(oldPosition)
                            oldItem.selection=false
                            dataList.set(oldPosition,oldItem)
                            adapter?.notifyItemChanged(oldPosition)

                            if ( selectedLanguage!=null)
                            {
                                selectedLanguage!!.selection=true
                                dataList.set(position,selectedLanguage!!)
                                updateSearchList(selectedLanguage!!)
                                adapter?.notifyItemChanged(position)
                            }
                        }
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    avoidMultipleSelection=true
                },300)
            }
        })
        binding.recyclerView.adapter=adapter


        Handler(Looper.getMainLooper()).postDelayed({
            applyAdapterFilter("${binding.etSearch.text}")
        },200)
    }

    private fun updateSearchList(updatedItem: LanguageDM) {
        for (item in searchlist)
        {
            if (item.languageCode.equals(updatedItem.languageCode))
            {
                item.selection=true
                searchlist.set(searchlist.indexOf(item),item)
            }
            else
            {
                item.selection=false
                searchlist.set(searchlist.indexOf(item),item)
            }
        }
    }

    private fun populateLanguagesData() {
        if (dataList.isEmpty())
        {
            binding.progressBar.visibility=View.VISIBLE
        }
        dataList.clear()
        searchlist.clear()
        val countryCodeArray = binding.root.context.resources.getStringArray(R.array.app_language_code)
        val countryNameArray = binding.root.context.resources.getStringArray(R.array.app_language)
        val countryFlagArray = binding.root.context.resources.getStringArray(R.array.app_language_flag)
        var defaultCode= Utils.getDefaultLanguageCode()
        val selectedLanguageCode=Prefs.getString(Const.APP_LANGUAGE_CODE, "")!!
        if(!(selectedLanguageCode.isEmpty()))
        {
            defaultCode=selectedLanguageCode
        }
        Log.d(Const.tag,"defaultCode: $defaultCode")
        for (index in 0..countryNameArray.size) {
            try {

                var selection=false
                var itemPoistion=0

                if (defaultCode.equals(countryCodeArray.get(index)))
                {
                    selection=true
                    itemPoistion=index
                }
                val itemAdded=LanguageDM(
                    countryCodeArray.get(index),
                    countryNameArray.get(index),
                    countryFlagArray.get(index),
                    selection
                )
                dataList.add(itemAdded)
                searchlist.add(itemAdded)

                if (defaultCode.equals(countryCodeArray.get(index)))
                {
                    selectedLanguage=dataList.get(itemPoistion)
                    scrollToItem(binding.recyclerView,itemPoistion)
                }

            } catch (e: Exception) {
                // Handle the exception as needed
            }
        }
        updateNoDataListener()
    }

    fun scrollToItem(recyclerView: RecyclerView, position: Int) {
        recyclerView.scrollToPosition(position)
    }

    private fun applyAdapterFilter(words: String) {
        val word=words.lowercase()
        if (word.isEmpty())
        {
            binding.ivCloseSearch.visibility=View.GONE
            if (searchlist.isEmpty())
            {
                populateLanguagesData()
            }
            else
            {
                dataList.clear()
                dataList.addAll(searchlist)
                adapter?.notifyDataSetChanged()
            }
        }
        else
        {
            binding.ivCloseSearch.visibility=View.VISIBLE
            val filterList:ArrayList<LanguageDM> = ArrayList()
            for(item in searchlist)
            {
                if ((item.languageName.lowercase()).contains(word))
                {
                    filterList.add(item)
                }
            }
            adapter?.filter(filterList)
        }
        updateNoDataListener()
    }
    fun updateNoDataListener() {
        binding.progressBar.visibility=View.GONE
        if (dataList.isEmpty())
        {
            binding.relativeHide.visibility=View.VISIBLE
            binding.recyclerView.visibility=View.GONE

        }
        else
        {
            if(!(dataList.isEmpty()))
            {
                loadAppBanner(this@LanguageA)
            }
            binding.relativeHide.visibility=View.GONE
            binding.recyclerView.visibility=View.VISIBLE
        }
        adapter?.notifyDataSetChanged()
    }


    private var bannerManger: BannerManager?= null
    private fun loadAppBanner(activity: Activity) {
        if (bannerManger==null)
        {
            bannerManger= BannerManager(activity)
            val bannerId=binding.root.context.getString(R.string.admob_banner_id)
            bannerManger?.showAndLoadBannerAd(bannerId,binding.tabNative,object: AdListener(){
                override fun onAdFailedToLoad(adError : LoadAdError) {
                    Log.d(BannerManager.TAG_BANNER,"Banner Failure: $adError")
                    binding.tabNative.visibility= View.GONE
                    bannerManger=null
                }
            })
        }

        if (Common.isNetworkAvailable(activity))
        {
            if (AdConst.IS_AD_SHOW)
            {
                if (binding.tabNative.visibility==View.GONE)
                {
                    if (bannerManger!=null)
                    {
                        bannerManger=null
                        loadAppBanner(activity)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (whereFrom.equals("Splash"))
        {
            if (selectedLanguage==null)
            {
                applyDefaultLanguage()
            }
            else
            {
                applySelectedLanguage()
            }
        }
        else
        {
            super.onBackPressed()
        }
    }

    private fun applyDefaultLanguage() {
        Prefs.putString(Const.APP_LANGUAGE,Const.DEFAULT_LANGUAGE)
        Prefs.putString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE)
        Prefs.putBoolean(Const.KEY_IS_LANGUAGE_SET,true)
        if (!prefManager!!.isFirstTimeLaunch())
        {
            applyLanguageChange(MainActivity::class.java)
        }
        else
        {
            applyLanguageChange(WelcomeActivity::class.java)
        }
    }
}