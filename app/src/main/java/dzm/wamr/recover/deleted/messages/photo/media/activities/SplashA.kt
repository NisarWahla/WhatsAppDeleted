package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.aemerse.iap.BillingService
import com.aemerse.iap.DataWrappers
import com.aemerse.iap.IapConnector
import com.aemerse.iap.SubscriptionServiceListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.BuildConfig
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivitySplashBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*
class SplashA : AppCompatLocaleActivity() {

    lateinit var binding: ActivitySplashBinding
    var remoteConfig:FirebaseRemoteConfig ?=null

    var countDownTimer:CountDownTimer ?= null
    var prefManager: PrefManager? = null
    private lateinit var iapConnector: IapConnector



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_splash)
        MyApp.isNewPremiumScenarioShow=false
        MyApp.isExperimentApply=false
        initControl()
    }

    private fun initControl() {
        AdConst.MyCurrentAdLimit=-1
        prefManager = PrefManager(binding.root.context)
        MyApp.createDirs(binding.root.context)
        checkAdAndBillingAndMoveNext()
    }

    private fun applyRemoteConfig() {
        remoteConfig= FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60)
            .build()
        remoteConfig?.let {config->
            config.setConfigSettingsAsync(configSettings)
            config.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    MyApp.isExperimentApply=task.isSuccessful
                    if (MyApp.isExperimentApply) {
                        applyRemoteConfigurations()
                    } else {
                        MyApp.isNewPremiumScenarioShow=false
                    }
                    Log.d(Const.tag,"RemoteConfig: ${MyApp.isExperimentApply}")
                }
        }

    }

    private fun applyRemoteConfigurations() {
        if (remoteConfig!=null)
        {
            val remoteValue=remoteConfig!!.getString(Const.SHOW_PREMIUM_SCREEN)
            Log.d(Const.tag,"Remote Config Value: $remoteValue")
            if(remoteValue.isEmpty())
            {
                MyApp.isExperimentApply=false
            }
            MyApp.isNewPremiumScenarioShow = remoteValue.equals("ShowScreen")
        } else
        {
            MyApp.isNewPremiumScenarioShow=false
        }
        Log.d(Const.tag,"isPremiumScreenShow: ${MyApp.isNewPremiumScenarioShow}")
    }


    private fun checkAdAndBillingAndMoveNext() {
        initBilling()
        initCounter()
    }


    private fun initCounter() {
        countDownTimer=object: CountDownTimer(Const.MAX_TIME_FOR_SPLASH, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val second=(Const.MAX_TIME_FOR_SPLASH/1000)-(millisUntilFinished/1000)
                Log.d(Const.tag,"Second: ${second}  ${MyApp.isNewPremiumScenarioShow}")

                if (Common.isNetworkAvailable(binding.root.context))
                {
                    if (AdConst.IS_AD_SHOW)
                    {
                        if (MyApp.isExperimentApply)
                        {
                            if ((!MyApp.isNewPremiumScenarioShow) && second>=Const.MIN_TIME_FOR_SPLASH)
                            {
                                if (second>=Const.MAX_TIME_LIMIT_FOR_SPLASH)
                                {
                                    cancelCountdownAndMoveToNext()
                                }
                                else
                                {
                                    if (InterstitialManager.preLoadAllScreenInterstital!=null && InterstitialManager.preLoadAllScreenInterstital!!.IsAdLoaded())
                                    {
                                        cancelCountdownAndMoveToNext()
                                    }
                                }
                            }
                            else
                            {
                                if(second>=Const.MIN_TIME_FOR_SPLASH)
                                {
                                    cancelCountdownAndMoveToNext()
                                }
                            }

                        } else
                        {
                            val counter=Prefs.getInt(Const.KEY_PREMIUM_SHOW_COUNTER,0)
                            if (counter%2==0 && second>=Const.MIN_TIME_FOR_SPLASH)
                            {
                                if (second>=Const.MAX_TIME_LIMIT_FOR_SPLASH)
                                {
                                    cancelCountdownAndMoveToNext()
                                }
                                else
                                {
                                    if (InterstitialManager.preLoadAllScreenInterstital!=null && InterstitialManager.preLoadAllScreenInterstital!!.IsAdLoaded())
                                    {
                                        cancelCountdownAndMoveToNext()
                                    }
                                }
                            }
                            else
                            {
                                if(second>=Const.MIN_TIME_FOR_SPLASH)
                                {
                                    cancelCountdownAndMoveToNext()
                                }
                            }
                        }


                    }
                    else
                    {
                        if(second>=Const.MIN_TIME_FOR_SPLASH)
                        {
                            cancelCountdownAndMoveToNext()
                        }

                    }

                }
                else
                {
                    if(second>=Const.MIN_TIME_FOR_SPLASH)
                    {
                        cancelCountdownAndMoveToNext()
                    }
                }
            }

            override fun onFinish() {
                cancelCountdownAndMoveToNext()
            }
        }
        countDownTimer?.start()
    }

    private fun cancelCountdownAndMoveToNext() {
        countDownTimer?.cancel()
        countDownTimer=null
        if (Prefs.getBoolean(Const.KEY_IS_LANGUAGE_SET,false))
        {
            if (!prefManager!!.isFirstTimeLaunch())
            {
                moveToEvenOddAdFlow()
            }
            else
            {
                showInterstitialAndMoveToWelcome()
            }
        }
        else
        {
            showInterstitialAndMoveToLanguageScreen()
        }

    }

    private fun showInterstitialAndMoveToLanguageScreen() {
        if (AdConst.IS_AD_SHOW)
        {
            if (InterstitialManager.preLoadAllScreenInterstital!=null && InterstitialManager.preLoadAllScreenInterstital!!.IsAdLoaded())
            {
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@SplashA,object:AdCallback{
                    override fun onAdComplete(isCompleted: Boolean) {
                        InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@SplashA,Const.AD_NEXT_REQUEST_DELAY)
                        moveToLanguageScreen()
                    }
                })
            }
            else
            {
                moveToLanguageScreen()
            }
        }
        else
        {
            moveToLanguageScreen()
        }
    }


    private fun showInterstitialAndMoveToWelcome() {
        if (AdConst.IS_AD_SHOW)
        {
            if (InterstitialManager.preLoadAllScreenInterstital!=null && InterstitialManager.preLoadAllScreenInterstital!!.IsAdLoaded())
            {
                InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@SplashA,object:AdCallback{
                    override fun onAdComplete(isCompleted: Boolean) {
                        InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@SplashA,Const.AD_NEXT_REQUEST_DELAY)
                        moveToWelcomeScreen()
                    }
                })
            }
            else
            {
                moveToWelcomeScreen()
            }
        }
        else
        {
            moveToWelcomeScreen()
        }
    }

    private fun moveToEvenOddAdFlow() {
        if (AdConst.IS_AD_SHOW)
        {
            if (MyApp.isExperimentApply)
            {
                if (!MyApp.isNewPremiumScenarioShow)
                {
                    //even
                    showInterstitialAndMoveMain()

                }
                else
                {
                    //odd
                    moveToPremiumPro()

                }
            } else
            {
                val counter=Prefs.getInt(Const.KEY_PREMIUM_SHOW_COUNTER,0)
                if (counter%2==0)
                {
                    //even
                    showInterstitialAndMoveMain()

                }
                else
                {
                    //odd
                    moveToPremium()

                }
                Prefs.putInt(Const.KEY_PREMIUM_SHOW_COUNTER,(counter+1))
            }
        }
        else
        {
            moveToMain()
        }
    }

    private fun moveToLanguageScreen() {
        val callingIntent=Intent(this@SplashA, LanguageA::class.java)
        callingIntent.putExtra("whereFrom","Splash")
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }
    private fun moveToWelcomeScreen() {
        val callingIntent=Intent(this@SplashA, WelcomeActivity::class.java)
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
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
                InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@SplashA,1L)
                applyRemoteConfig()
            }

            @SuppressLint("SetTextI18n", "LogNotTimber")
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
                Log.e(BillingService.TAG, "onPricesUpdated: ${iapKeyPrices.entries}\n")
            }

        })
    }

    private fun moveToPremium() {
        val movingIntent=Intent(binding.root.context, PremiumNewA::class.java)
        movingIntent.putExtra("whereFrom","SplashA")
        movingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(movingIntent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun moveToPremiumPro() {
        val movingIntent=Intent(binding.root.context, PremiumProA::class.java)
        movingIntent.putExtra("whereFrom","SplashA")
        movingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(movingIntent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }


    private fun showInterstitialAndMoveMain() {

        if (InterstitialManager.preLoadAllScreenInterstital!=null && InterstitialManager.preLoadAllScreenInterstital!!.IsAdLoaded())
        {
            InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@SplashA,object:AdCallback{
                override fun onAdComplete(isCompleted: Boolean) {
                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@SplashA,Const.AD_NEXT_REQUEST_DELAY)
                    moveToMain()
                }
            },true)
        }
        else
        {
            moveToMain()
        }
    }

    private fun moveToMain() {
        val callingIntent=Intent(this@SplashA, MainActivity::class.java)
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer!=null){
            countDownTimer!!.cancel()
            countDownTimer=null
        }
    }
}