package dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments

import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.activities.AllChat
import dzm.wamr.recover.deleted.messages.photo.media.activities.MainActivity
import dzm.wamr.recover.deleted.messages.photo.media.adapter.MessageTabAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.FragmentMessagesBinding
import dzm.wamr.recover.deleted.messages.photo.media.firebase.NotificationListener
import dzm.wamr.recover.deleted.messages.photo.media.interfaces.AdapterClicklistener
import dzm.wamr.recover.deleted.messages.photo.media.model.DataModel
import dzm.wamr.recover.deleted.messages.photo.media.sqlDb.DatabaseHelper
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.hideKeyboard
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    var mDatabaseHelper: DatabaseHelper? = null
    var deleteMsg: String? = null
    var adapter: MessageTabAdapter? = null
    val datalist:ArrayList<DataModel> =ArrayList()
    val searchlist:ArrayList<DataModel> =ArrayList()


    companion object {
        fun newInstance(): MessagesFragment {
            val fragment = MessagesFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }




    private var nativeManger: NativeAdManager?= null
    private fun loadAppNative(activity: Activity) {
        if(AdConst.IS_AD_SHOW){
            if (nativeManger == null && Common.isNetworkAvailable(activity)) {
                nativeManger = NativeAdManager(activity)
                val nativeId = binding.root.context.getString(R.string.native_ads)
                nativeManger?.showAndLoadNativeAd(
                    nativeId,
                    binding.tabNative,
                    binding.nativeTemplate,
                    object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d(NativeAdManager.TAG_NATIVE, "Native Failure: $adError")
                            binding.tabNative.visibility = View.GONE
                            nativeManger = null
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.d(NativeAdManager.TAG_NATIVE, "Native: ")
                            binding.nativeTemplate.visibility = View.VISIBLE
                        }
                    })
            }

            if (!(Common.isNetworkAvailable(activity)))
            {
                if (binding.tabNative.visibility==View.VISIBLE)
                {
                    if (binding.nativeTemplate.visibility==View.GONE)
                    {
                        binding.tabNative.visibility=View.GONE
                        nativeManger?.destroyNativeAd()
                        nativeManger=null
                    }
                }
            }
        }
        else
        {
            binding.tabNative.visibility=View.GONE
            nativeManger?.destroyNativeAd()
            nativeManger=null
        }
    }

    fun checkIsNeedToRemoveAd(){
        activity?.let {
            loadAppNative(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_messages,container,false)
        initControl()
        actionControl()

        LocalBroadcastManager.getInstance(binding.root.context)
            .registerReceiver(onNotice, IntentFilter(AllChat.ACTION_NEW_NOTIFICATION))
        fetchNotifications()
        return binding.root
    }

    private fun actionControl() {
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

    private fun applyAdapterFilter(words: String) {
        val word=words.lowercase()
        if (word.isEmpty())
        {
            fetchNotifications()
        }
        else
        {
            val filterList:ArrayList<DataModel> = ArrayList()
            for(item in searchlist)
            {
                if ((item.name.lowercase()).contains(word) || (item.message.lowercase()).contains(word))
                {
                    filterList.add(item)
                }
            }
            adapter?.filter(filterList)
        }
       updateNoDataListener()
    }

    private fun initControl() {
        mDatabaseHelper = DatabaseHelper(binding.root.context)
        setupAdapter()
    }

    var avoidMultipleSelection=true
    private fun setupAdapter() {
        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation=LinearLayoutManager.VERTICAL
        binding.recyclerView.addItemDecoration(DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL))
        binding.recyclerView.layoutManager = layoutManager
        adapter = MessageTabAdapter(datalist,object:AdapterClicklistener{
            override fun onItemClick(view: View, position: Int) {
                if (avoidMultipleSelection)
                {
                    avoidMultipleSelection=false

                    if (view.id==R.id.rl){
                        if (AdConst.IS_AD_SHOW)
                        {
                            moveToNextScreenWithFirstTourLogic(datalist.get(position))
                        }
                        else
                        {
                            moveToNextScreen(datalist.get(position))
                        }

                    }
                    if (view.id==R.id.delete_chat){
                        confirmDialog(datalist.get(position),position)
                    }
                }


                Handler(Looper.getMainLooper()).postDelayed({
                    avoidMultipleSelection=true
                },300)

            }
        })
        binding.recyclerView.adapter = adapter
    }

    private fun moveToNextScreenWithFirstTourLogic(itemModel: DataModel) {
        if (AdConst.firstTourAd)
        {
           activity?.let {
               InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(it,
                   object: AdCallback {
                       override fun onAdComplete(isCompleted: Boolean) {
                           InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(it,Const.AD_NEXT_REQUEST_DELAY)
                           AdConst.firstTourAd=false

                           moveToNextScreen(itemModel)
                       }
                   },true)
           }
        }
        else
        {
            moveToNextScreen(itemModel)
        }
    }

    private fun moveToNextScreen(itemModel: DataModel) {
        val intent = Intent(binding.root.context, AllChat::class.java)
        intent.putExtra("name", itemModel.name)
        startActivity(intent)
    }


    private fun confirmDialog(dataModel: DataModel, position: Int) {
        val dialog = Dialog(binding.root.context)
        dialog.setContentView(R.layout.delete_confirmation_dialog)
        dialog.setCancelable(true)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val yesBtn = dialog.findViewById<TextView>(R.id.txt_yes)
        val noBtn = dialog.findViewById<TextView>(R.id.txt_no)
        val fileName = dialog.findViewById<TextView>(R.id.file_name)

        fileName.isSelected = true
        fileName.visibility = View.GONE

        noBtn.setOnClickListener { dialog.dismiss() }
        yesBtn.setOnClickListener {
            try {
                searchlist.removeAt(position)
                datalist.removeAt(position)
                mDatabaseHelper?.deleteSpecificData(dataModel.name)
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.chat_deleted_successfully))
                updateNoDataListener()
            } catch (e: Exception) {
                Log.d(Const.tag,"Exception: $e")
            }
            dialog.dismiss()
        }

        dialog.show()
    }




    private val onNotice: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            Utils.readAllIntentData(intent)
            if (intent.getBooleanExtra("isShow",false))
            {
                deleteMsg = intent.getStringExtra("deleteMsgs")
                applyAdapterFilter("${binding.etSearch.text}")
            }
        }
    }

    fun fetchNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            val lis = async {
                mDatabaseHelper?.getdata()
            }
            withContext(Dispatchers.Main) {
                try {
                    val comingList=lis.await()
                    comingList?.let {
                        datalist.clear()
                        datalist.addAll(it)
                        searchlist.clear()
                        searchlist.addAll(it)
                    }
                    updateNoDataListener()
                } catch (e: Exception) {
                   Log.d(Const.tag,"Exception: $e")
                }
            }
        }
    }

    fun updateNoDataListener() {
        if (datalist.isEmpty())
        {
            binding.relativeHide.visibility=View.VISIBLE
            binding.recyclerView.visibility=View.GONE

        }
        else
        {
            activity?.let {
                loadAppNative(it)
            }
            binding.relativeHide.visibility=View.GONE
            binding.recyclerView.visibility=View.VISIBLE
        }
        adapter?.notifyDataSetChanged()
    }



    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible)
        {
            Handler(Looper.getMainLooper())
                .postDelayed({
                    applyAdapterFilter("${binding.etSearch.text}")
                    checkNotificationService()
                },200)
        }
    }
    private val notificationPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck = mutableListOf<String>()

            for (entry in result.entries) {
                val (key, value) = entry
                if (!value) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Utils.getPermissionStatus(requireActivity(), key))
                }
            }

            if (blockPermissionCheck.contains("blocked")) {
                Utils.showPermissionSetting(
                    binding.root.context,
                    binding.root.context.getString(R.string.please_allow_wamr_notification_permission_description)
                )
            } else if (allPermissionClear) {
                //do noting action not define
                getLiveNotificationPermission()
            }
            MyAppOpenAd.isDialogClose=true
            MyAppOpenAd.isAppInBackground=false
        }

    private fun checkNotificationService() {
        val isMyServiceRunning = Utils.isServiceRunning(binding.root.context, NotificationListener::class.java)
        if (isMyServiceRunning) {
//          Do Nothing
        } else {

            val permissionUtil= PermissionUtils(requireActivity(),notificationPermissionResult)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                if (permissionUtil.isNotificationPermissionGranted())
                {
                    getLiveNotificationPermission()

                }else{
                    MyAppOpenAd.isDialogClose=false
                    permissionUtil.takeNotificationPermission()
                }

            } else{
                getLiveNotificationPermission()
            }
        }
    }

    val resultLiveNotificationCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
        makeFirstNotificationChannel()
    }
    private fun getLiveNotificationPermission() {
        MyAppOpenAd.isDialogClose=false
        try {
            val intent=getIntentForNotificationAccess(binding.root.context, NotificationListener::class.java)
            resultLiveNotificationCallback.launch(intent)
        } catch (e: Exception) {
            val intent=Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            resultLiveNotificationCallback.launch(intent)
        }
    }



    fun getIntentForNotificationAccess(context: Context, notificationAccessServiceClass: Class<out NotificationListenerService>): Intent {

        val intent:Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent= Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
            intent.putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, ComponentName(context.packageName, notificationAccessServiceClass.name).flattenToString())
        }else{
            intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        val value = "${context.packageName}/${notificationAccessServiceClass.name}"
        val key = ":settings:fragment_args_key"
        intent.putExtra(key, value)
        intent.putExtra(":settings:show_fragment_args", Bundle().also { it.putString(key, value) })
        return intent
    }



    private fun makeFirstNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MyNotifications", "MyNotifications", importance)
            //            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            activity?.let {
                val notificationManager = it.getSystemService(
                    NotificationManager::class.java
                )
                notificationManager.createNotificationChannel(channel)
            }

        }
    }


}