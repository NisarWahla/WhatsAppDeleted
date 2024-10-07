package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.aemerse.iap.BillingService
import com.aemerse.iap.DataWrappers
import com.aemerse.iap.IapConnector
import com.aemerse.iap.SubscriptionServiceListener
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.BuildConfig
import dzm.wamr.recover.deleted.messages.photo.media.FileObserve.DeleteFileReceiver
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ViewPagerAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityMainBinding
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.*
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.viewModel.SharedViewModel
import kotlinx.coroutines.*
import java.util.*

enum class FirstTourMain{PREMIUM_ACT,RATE_US,PRIVACY_POLICY,MORE_APPS,FEEDBACK,LANGUAGES}
class MainActivity : AppCompatLocaleActivity() {

    var id: String? = null
    var name: String? = null
    var appUpdateManager: AppUpdateManager? =null
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null
    private val FLEXIBLE_APP_UPDATE_REQ_CODE = 123
    // Declare the UpdateManager
    var deleteFileReceiver: DeleteFileReceiver?=null
    lateinit var sharedViewModel: SharedViewModel
    private var pos: Int = 0
    lateinit var app: MyApp
    var adapter: ViewPagerAdapter? =null
    var messageFrgInstence: MessagesFragment? =null
    var newMediaFrgInstence: NewMediaFragment? =null
    var wappStatusFrgInstence: WAppStatusFrag? =null
    var toolsFrgInstence: ToolsFragment? =null
    //Sliding Root Nav Builder And Layout Controller
    private lateinit var binding: ActivityMainBinding
    private lateinit var iapConnector: IapConnector

    private val REQUEST_BATTERY_OPTIMIZATION_PERMISSION = 783
    private val REQUEST_COMPANION_RUN_IN_BACKGROUND = 781

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        initControl()
        actionControl()
    }

    private fun initControl() {
        appUpdateManager = AppUpdateManagerFactory.create(binding.root.context)
        app = application as MyApp
        deleteFileReceiver = DeleteFileReceiver()
        if (BuildConfig.DEBUG) {
            val testDeviceIds = listOf("F3B80A46D4B51F8790AC833438D7A319")
            val requestConfiguration =
                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        checkUpdate()
        setupScreenData()
        initializeNavController()
        checkBilling()
        initBilling()
        lifecycleScope.launch(Dispatchers.IO) {
            LoadingDataClass.getMedia(this@MainActivity)
            LoadingDataClass.getVideoMedia(this@MainActivity)
            LoadingDataClass.getAudioMedia(this@MainActivity)
            LoadingDataClass.getImgMedia(this@MainActivity)
        }

        installStateUpdatedListener = InstallStateUpdatedListener { state: InstallState ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                removeInstallStateUpdateListener()
            } else {
                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.install_state_)+" ${state.installStatus()}")
            }
        }
        installStateUpdatedListener?.let {
            appUpdateManager?.registerListener(it)
        }
    }



    private fun setupScreenData() {
        if (AdConst.IS_AD_SHOW)
        {
            binding.drawerNavigation.navUpgradeToPro.visibility = View.VISIBLE
            binding.drawerNavigation.navUpgradeToProLine.visibility = View.VISIBLE
        }
        else
        {
            binding.drawerNavigation.navUpgradeToPro.visibility = View.GONE
            binding.drawerNavigation.navUpgradeToProLine.visibility = View.GONE
        }
    }

    private fun actionControl() {
        binding.premiumIcon.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            MoveToNextActivityWithFirstTour(FirstTourMain.PREMIUM_ACT)
        }))
        binding.viewHideShow.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            closeNavigationDrawer()
        }))
        binding.drawerNavigation.crossImageView.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            closeNavigationDrawer()
        }))
        binding.slideIcon.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            openNavigationDrawer()
        }))

        binding.drawerNavigation.navUpgradeToPro.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            MoveToNextActivityWithFirstTour(FirstTourMain.PREMIUM_ACT)
        }))
        binding.drawerNavigation.navRating.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            MoveToNextActivityWithFirstTour(FirstTourMain.RATE_US)
        }))
        binding.drawerNavigation.navPrivacy.setOnClickListener (DebounceClickHandler(View.OnClickListener {
            MoveToNextActivityWithFirstTour(FirstTourMain.PRIVACY_POLICY)
        }))
        binding.drawerNavigation.navMoreApps.setOnClickListener (DebounceClickHandler(View.OnClickListener {
            MoveToNextActivityWithFirstTour(FirstTourMain.MORE_APPS)
        }))
        binding.drawerNavigation.navSupport.setOnClickListener(DebounceClickHandler(View.OnClickListener  {
            MoveToNextActivityWithFirstTour(FirstTourMain.FEEDBACK)
        }))
        binding.drawerNavigation.navLanguage.setOnClickListener(DebounceClickHandler(View.OnClickListener  {
            MoveToNextActivityWithFirstTour(FirstTourMain.LANGUAGES)
        }))
    }

    private fun closeNavigationDrawer() {
        binding.tabMainView.animate().translationX(0f)
            .scaleX(getResetScale())
            .scaleY(getResetScale()).setDuration(200).start()
        binding.viewHideShow.visibility=View.GONE
    }

    private fun openNavigationDrawer() {
        var directionFloat=binding.root.context.resources.getDimension(R.dimen._170sdp)
        if (Utils.isLayoutDirectionRTL(binding.tabMainView))
        {
            directionFloat=-(directionFloat)
        }
        binding.tabMainView.animate()
            .translationX(directionFloat)
            .scaleX(getScalePercentage(90f))
            .scaleY(getScalePercentage(90f)).setDuration(200).start()
        binding.viewHideShow.visibility=View.VISIBLE
    }

    fun getResetScale():Float {
        return 1.0f
    }

    fun getScalePercentage(percentage: Float):Float {
        // Ensure the percentage is within the valid range (0 to 100)
        val validPercentage = percentage.coerceIn(0f, 100f)

        // Calculate the scale factor based on the percentage
        val scaleFactor = validPercentage / 100f

        // Apply the scale directly
        return scaleFactor
    }

    private fun checkBilling() {
        if (Prefs.getString(Const.KEY_FOR_SUBSCRIPTION,"${SubscriptionEnum.DEFAULT}").equals("${SubscriptionEnum.PURCHASE}"))
        {
            AdConst.IS_AD_SHOW=false
            binding.premiumIcon.visibility=View.GONE
            binding.drawerNavigation.navUpgradeToPro.visibility = View.GONE
            binding.drawerNavigation.navUpgradeToProLine.visibility = View.GONE
            messageFrgInstence?.checkIsNeedToRemoveAd()
//            newMediaFrgInstence?.checkIsNeedToRemoveAd()
            wappStatusFrgInstence?.checkIsNeedToRemoveAd()
            toolsFrgInstence?.checkIsNeedToRemoveAd()
        }
        else
            if (Prefs.getString(Const.KEY_FOR_SUBSCRIPTION,"${SubscriptionEnum.DEFAULT}").equals("${SubscriptionEnum.NOT_PURCHASE}"))
            {
                AdConst.IS_AD_SHOW=true
                binding.premiumIcon.visibility=View.VISIBLE
                binding.drawerNavigation.navUpgradeToPro.visibility = View.VISIBLE
                binding.drawerNavigation.navUpgradeToProLine.visibility = View.VISIBLE
            }
            else
            {
                AdConst.IS_AD_SHOW=true
                binding.premiumIcon.visibility=View.VISIBLE
                binding.drawerNavigation.navUpgradeToPro.visibility = View.VISIBLE
                binding.drawerNavigation.navUpgradeToProLine.visibility = View.VISIBLE
            }

    }

    private fun initBilling() {
        val nonConsumablesList = listOf("lifetime")
        val consumablesList = listOf(
            binding.root.context.getString(R.string.weeklypro_id),
            binding.root.context.getString(R.string.weekly_id),
            binding.root.context.getString(R.string.monthly_id),
            binding.root.context.getString(R.string.yearly_id)
        )
        val subscriptionList = listOf(
            binding.root.context.getString(R.string.weeklypro_id),
            binding.root.context.getString(R.string.weekly_id),
            binding.root.context.getString(R.string.monthly_id),
            binding.root.context.getString(R.string.yearly_id)
        )

        iapConnector = IapConnector(
            context = this,
            nonConsumableKeys = nonConsumablesList,
            consumableKeys = consumablesList,
            subscriptionKeys = subscriptionList,
            key = binding.root.context.getString(R.string.wamr_key),
            enableLogging = com.aemerse.iap.BuildConfig.DEBUG
        )
        iapConnector.addSubscriptionListener(object : SubscriptionServiceListener {
            @SuppressLint("LogNotTimber")
            override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered upon fetching owned subscription upon initialization
                Log.e(BillingService.TAG, "onSubscriptionRestored: ${purchaseInfo.sku}\n")
                Prefs.putString(Const.KEY_FOR_SUBSCRIPTION, "${SubscriptionEnum.PURCHASE}")
                Prefs.putString(Const.KEY_FOR_PACKAGE, purchaseInfo.packageName)
                AdConst.IS_AD_SHOW=false
                checkBilling()
            }

            @SuppressLint("LogNotTimber")
            override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever subscription succeeded
                Log.e(BillingService.TAG, "onSubscriptionPurchased: ${purchaseInfo.sku}\n")

            }

            override fun onEmptySubscription() {
                Log.e(BillingService.TAG, "onEmptySubscription: ")
                Prefs.putString(Const.KEY_FOR_SUBSCRIPTION, "${SubscriptionEnum.NOT_PURCHASE}")
                Prefs.putString(Const.KEY_FOR_PACKAGE, "")
                AdConst.IS_AD_SHOW=true
                checkBilling()
            }

            @SuppressLint("SetTextI18n", "LogNotTimber")
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
                Log.e(BillingService.TAG, "onPricesUpdated: ${iapKeyPrices.entries}\n")
            }

        })
    }


    override fun onPause() {
        super.onPause()
        if (deleteFileReceiver!=null)
        {
            unregisterReceiver(deleteFileReceiver)
        }
    }

    override fun onResume() {
        super.onResume()
        if (deleteFileReceiver!=null)
        {
            val filter = IntentFilter()
            filter.addAction(Common.ACTION_DELETE)
            registerReceiver(deleteFileReceiver, filter)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!MyAppOpenAd.isDialogClose)
            {
                MyAppOpenAd.isAppInBackground=false
            }
            MyAppOpenAd.isDialogClose=true
        },300)
    }



    /**
     *
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_COMPANION_RUN_IN_BACKGROUND) {
            if (grantResults.isNotEmpty()) {
                val companian = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (companian) {
                    // perform action when allow permission success
                    checkBattery()
                    Log.i("Permission", "Battery Optimization Permission granted!")
                } else {
                    showSettingBatteryDialog()
                }
            }
        }

        if (requestCode == REQUEST_BATTERY_OPTIMIZATION_PERMISSION) {
            if (grantResults.isNotEmpty()) {
                val BATTERY_OPTIMIZATION = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (BATTERY_OPTIMIZATION) {
                    // perform action when allow permission success
                    Log.i("Permission", "Battery Optimization Permission granted!")
                } else {
                    showSettingBatteryDialog()
                }
            }
        }

    }

    private fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", "dzm.wamr.recover.deleted.messages.photo.media", null)
        )
        startActivityForResult(intent, 999)
    }

    private fun initializeNavController() {

        adapter = ViewPagerAdapter(this)
        registerFragmentWithPager()
        binding.viewPager.offscreenPageLimit=1
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = binding.root.context.getString(R.string.chats)
                }
                1 -> {
                    tab.text = binding.root.context.getString(R.string.media)
                }
                2 -> {
                    tab.text = binding.root.context.getString(R.string.status)
                }
                3 -> {
                    tab.text = binding.root.context.getString(R.string.tools)
                }
            }
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pos = tab.position
                when (pos) {
                    0 -> {
                        Utils.addEventToFirebase(MyApp.buttonClick, "Chats")
                        //binding.rightClickIv.visibility = View.VISIBLE
                    }
                    1 -> {
                        hideKeyboard(binding.tabLayout)
                        Utils.addEventToFirebase(MyApp.buttonClick, "Media")
                        //binding.rightClickIv.visibility = View.GONE
                    }
                    2 -> {
                        hideKeyboard(binding.tabLayout)
                        Utils.addEventToFirebase(MyApp.buttonClick, "Status")
                        //binding.rightClickIv.visibility = View.GONE
                    }
                    3 -> {
                        Utils.addEventToFirebase(MyApp.buttonClick, "Tools")
                        //binding.rightClickIv.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.view.setBackgroundColor(Color.TRANSPARENT)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }


    private fun registerFragmentWithPager() {
        messageFrgInstence=MessagesFragment.newInstance()
        newMediaFrgInstence=NewMediaFragment.newInstance()
        wappStatusFrgInstence=WAppStatusFrag.newInstance()
        toolsFrgInstence=ToolsFragment.newInstance()

        adapter?.addFrag(messageFrgInstence!!)
        adapter?.addFrag(newMediaFrgInstence!!)
        adapter?.addFrag(wappStatusFrgInstence!!)
        adapter?.addFrag(toolsFrgInstence!!)
    }






    private fun MoveToNextActivityWithFirstTour(type: FirstTourMain) {
        if (AdConst.IS_AD_SHOW) {
            if (AdConst.firstTourAd)
            {
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@MainActivity,
                    object: AdCallback {
                        override fun onAdComplete(isCompleted: Boolean) {
                            InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@MainActivity,Const.AD_NEXT_REQUEST_DELAY)
                            AdConst.firstTourAd=false
                            MoveToNextActivityAfterFirstTour(type)

                        }
                    },true)
            }
            else
            {
                MoveToNextActivityAfterFirstTour(type)
            }

        } else {
            MoveToNextActivityAfterFirstTour(type)
        }
    }

    private fun MoveToNextActivityAfterFirstTour(type: FirstTourMain) {
        if (type==FirstTourMain.PREMIUM_ACT)
        {
            if (MyApp.isExperimentApply && MyApp.isNewPremiumScenarioShow)
            {
                openPremiumProScreen()
            } else
            {
                openPremiumScreen()
            }
        } else if (type==FirstTourMain.RATE_US)
        {
            showRateUsDialouge()
        } else if (type==FirstTourMain.PRIVACY_POLICY)
        {
            moveToPrivacyPolicy()
        } else if (type==FirstTourMain.MORE_APPS)
        {
            moveToMoreApps()
        } else if (type==FirstTourMain.FEEDBACK)
        {
            FeedbackUtils.startFeedbackEmail(binding.root.context)
        } else if (type==FirstTourMain.LANGUAGES)
        {
            openLanguageScreen()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            closeNavigationDrawer()
        },200)
    }

    val resultLanguageCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
            result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {

            }
        }
    }
    }
    private fun openLanguageScreen() {
        hideKeyboard(binding.premiumIcon)
        val intent = Intent(binding.root.context, LanguageA::class.java)
        resultLanguageCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }



    private fun moveToMoreApps() {
        val url = "https://play.google.com/store/apps/dev?id=8194673071547521792"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun moveToPrivacyPolicy() {
        val url = "https://www.dzinemedia.com/privacy-policy"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    val resultPremiumCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
            result -> if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        data?.let {
            if (it.getBooleanExtra("isShow",false))
            {
                checkBilling()
            }
        }
    }
    }
    private fun openPremiumScreen() {
        hideKeyboard(binding.premiumIcon)
        val intent = Intent(binding.root.context, PremiumNewA::class.java)
        resultPremiumCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }


    private fun openPremiumProScreen() {
        hideKeyboard(binding.premiumIcon)
        val intent = Intent(binding.root.context, PremiumProA::class.java)
        resultPremiumCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }



    fun checkBattery() {
        if (!isIgnoringBatteryOptimizations(this)) {
            val name = binding.root.context.getString(R.string.app_name)
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.battery_optimization_str_one)+" $name "+binding.root.context.getString(R.string.battery_optimization_str_two))

            val intent =
                Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)//ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        return pwrm.isIgnoringBatteryOptimizations(name)

    }

    private fun showSettingBatteryDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle(binding.root.context.getString(R.string.need_battery_optimization))
        builder.setMessage(binding.root.context.getString(R.string.storage_permission_description))
        builder.setPositiveButton(binding.root.context.getString(R.string.go_to_setting)) { dialog, id ->
            dialog.dismiss()
            openSettings()
        }
        builder.setNegativeButton(binding.root.context.getString(R.string.cancel)) { dialog, id ->
            dialog.dismiss()
        }
        MyAppOpenAd.isDialogClose=false
        builder.setOnDismissListener {
            MyAppOpenAd.isDialogClose=true
        }
        builder.show()
    }




    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showRateUsDialouge() {
        var isStarSelected = 5
        val builder = android.app.AlertDialog.Builder(binding.root.context)
        val viewGroup = findViewById<ViewGroup>(android.R.id.content)
        val dialogView = LayoutInflater.from(binding.root.context)
            .inflate(R.layout.rate_us_dialgue, viewGroup, false)
        builder.setView(dialogView)
        val changeEmoji = dialogView.findViewById<ImageView>(R.id.changeEmojiIv)
        val iV = dialogView.findViewById<ImageView>(R.id.dialougeImageV)
        val first = dialogView.findViewById<ImageView>(R.id.firstStar)
        val second = dialogView.findViewById<ImageView>(R.id.secondStar)
        val third = dialogView.findViewById<ImageView>(R.id.thirdStar)
        val foruth = dialogView.findViewById<ImageView>(R.id.fourthStar)
        val fifth = dialogView.findViewById<ImageView>(R.id.fifthStar)
        val cancelDialouge = dialogView.findViewById<ImageView>(R.id.cancelDialougeiV)
        val textchangeAble = dialogView.findViewById<TextView>(R.id.changeTextView)
        val rate_us_on_google = dialogView.findViewById<TextView>(R.id.rate_us_on_googletv)
        val alertDialog = builder.create()
        alertDialog.show()
        MyAppOpenAd.isDialogClose=false
        builder.setOnDismissListener {
            MyAppOpenAd.isDialogClose=true
        }
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // set image Resource
        first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))

        cancelDialouge.setOnClickListener {
            alertDialog.dismiss()
        }

        first.setOnClickListener {
            isStarSelected = 1
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            textchangeAble.text = binding.root.context.getString(R.string.oh_no)
            changeEmoji.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.cry))

        }
        second.setOnClickListener {
            isStarSelected = 2
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            textchangeAble.text = binding.root.context.getString(R.string.oh_no)
            changeEmoji.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.cryingone))
        }


        third.setOnClickListener {
            isStarSelected = 3
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            textchangeAble.text = binding.root.context.getString(R.string.we_like_you_too)
            changeEmoji.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.happy))
        }

        foruth.setOnClickListener {
            isStarSelected = 4
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            textchangeAble.text = binding.root.context.getString(R.string.we_like_you_too)
            changeEmoji.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.smile))
        }

        fifth.setOnClickListener {
            isStarSelected = 5
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            textchangeAble.text = binding.root.context.getString(R.string.thanks_for_love)
            changeEmoji.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.love))
        }

        rate_us_on_google.setOnClickListener {
            alertDialog.dismiss()
            if (isStarSelected == 1 || isStarSelected == 2 || isStarSelected == 3) {
                FeedbackUtils.giveReview(this)
            } else {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=dzm.wamr.recover.deleted.messages.photo.media")
                openURL.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(openURL)
            }

        }

    }


    private fun checkUpdate() {
        if (!BuildConfig.DEBUG) {
            appUpdateManager?.let {
                val appUpdateInfoTask: Task<AppUpdateInfo> = it.appUpdateInfo
                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() === UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                            AppUpdateType.IMMEDIATE
                        )
                    ) {
                        startUpdateFlow(appUpdateInfo)
                    } else if (appUpdateInfo.installStatus() === InstallStatus.DOWNLOADED) {
                        popupSnackBarForCompleteUpdate()
                    }
                }
            }

        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager?.startUpdateFlowForResult(
                appUpdateInfo, AppUpdateType.IMMEDIATE, this, FLEXIBLE_APP_UPDATE_REQ_CODE
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FLEXIBLE_APP_UPDATE_REQ_CODE) {
            when (resultCode) {
                RESULT_CANCELED -> {
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.update_canceled_by_user_result_code_)+" $resultCode")
                    checkUpdate()
                }
                RESULT_OK -> {
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.update_success_result_code_)+" $resultCode")
                }
                else -> {
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.update_failed_result_code_)+" $resultCode")
                    checkUpdate()
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(
            findViewById<View>(android.R.id.content).rootView,
            binding.root.context.getString(R.string.new_app_is_ready_),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(binding.root.context.getString(R.string.install)) { view: View? ->
            appUpdateManager?.completeUpdate()
        }.setActionTextColor(ContextCompat.getColor(binding.root.context,R.color.colorPrimaryDark)).show()
    }

    private fun removeInstallStateUpdateListener() {
        installStateUpdatedListener?.let {
            appUpdateManager?.unregisterListener(it)
        }
    }

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
    }

    override fun onBackPressed() {
        if (binding.viewHideShow.visibility==View.VISIBLE)
        {
            closeNavigationDrawer()
            return
        }
        if (binding.viewPager.currentItem!=0)
        {
            binding.viewPager.setCurrentItem(0,true)
            return
        }

        backPressedDialogue()
    }

    private fun backPressedDialogue() {
        var isStarSelected = 5
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.back_pressed_diloge)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val rateUs = dialog.findViewById<TextView>(R.id.tvRateUs)
        val exit = dialog.findViewById<TextView>(R.id.exit)
        val backToApp = dialog.findViewById<TextView>(R.id.tvBack)
        val first = dialog.findViewById<ImageView>(R.id.firstStar)
        val second = dialog.findViewById<ImageView>(R.id.secondStar)
        val third = dialog.findViewById<ImageView>(R.id.thirdStar)
        val foruth = dialog.findViewById<ImageView>(R.id.fourthStar)
        val fifth = dialog.findViewById<ImageView>(R.id.fifthStar)
        // set image Resource
        first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))

        first.setOnClickListener {
            isStarSelected = 1
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))

        }
        second.setOnClickListener {
            isStarSelected = 2
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
        }


        third.setOnClickListener {
            isStarSelected = 3
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
        }

        foruth.setOnClickListener {
            isStarSelected = 4
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.star))
        }

        fifth.setOnClickListener {
            isStarSelected = 5
            first.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            second.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            third.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            foruth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
            fifth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.yellow_star))
        }


        rateUs.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            dialog.dismiss()
            if (isStarSelected == 1 || isStarSelected == 2 || isStarSelected == 3) {
                FeedbackUtils.giveReview(this)
            } else {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=dzm.wamr.recover.deleted.messages.photo.media")
                openURL.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(openURL)
            }
        }))
        backToApp.setOnClickListener (DebounceClickHandler(View.OnClickListener {
            dialog.dismiss()
        }))
        exit.setOnClickListener (DebounceClickHandler(View.OnClickListener {
            dialog.dismiss()
            System.exit(0)
        }))
        dialog.show()
    }

}

