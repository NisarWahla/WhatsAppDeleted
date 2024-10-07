package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import cdzm.wamr.recover.deleted.messages.photo.media.admanager.InterstitialManager
import com.aemerse.iap.*
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdCallback
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityPremiumNewBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*

class PremiumNewA : AppCompatLocaleActivity() {

    lateinit var binding:ActivityPremiumNewBinding
    private lateinit var iapConnector: IapConnector
    private val isBillingClientConnected: MutableLiveData<Boolean> = MutableLiveData()
    var weeklyPrice: String = ""
    var monthlyPrice: String = ""
    var yearlyPrice: String = ""
    var chooseString: String = ""
    var whereFrom:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE,Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_premium_new)

        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.tabSubscribe.setOnClickListener {
          purchasePackage()
        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.tabMainYear.setOnClickListener {
            selectPackage(1)
        }
        binding.tabMainMonth.setOnClickListener {
            selectPackage(2)
        }
        binding.tabMainWeek.setOnClickListener {
            selectPackage(3)
        }
    }

    private fun selectPackage(selection: Int) {

        when(selection){

            1->{
                chooseString = "yearly"
                binding.ivYear.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_select))
                binding.ivMonth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_unselect))
                binding.ivWeek.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_unselect))
                binding.tabYear.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_white_green)
                binding.tabMonth.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_transparent)
                binding.tabWeek.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_transparent)
                binding.tabPopular.visibility=View.VISIBLE
            }
            2->{
                chooseString = "monthly"
                binding.ivYear.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_unselect))
                binding.ivMonth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_select))
                binding.ivWeek.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_unselect))
                binding.tabYear.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_transparent)
                binding.tabMonth.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_white_green)
                binding.tabWeek.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_transparent)
                binding.tabPopular.visibility=View.GONE
            }
            3->{
                chooseString = "weekly"
                binding.ivYear.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_unselect))
                binding.ivMonth.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_unselect))
                binding.ivWeek.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_select))
                binding.tabYear.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_transparent)
                binding.tabMonth.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_transparent)
                binding.tabWeek.background=ContextCompat.getDrawable(binding.root.context,R.drawable.ractengle_round_solid_white_green)
                binding.tabPopular.visibility=View.GONE
            }

        }

    }

    private fun purchasePackage() {
        if (LoadingDataClass.isNetworkAvailable(this)) {
            MyAppOpenAd.isDialogClose=false
            if (chooseString.equals("weekly")) {
                //Toast.makeText(this, "Clicked 0", Toast.LENGTH_SHORT).show()
                iapConnector.subscribe(
                    this@PremiumNewA,
                    binding.root.context.getString(R.string.weekly_id)
                )
            }
            else if (chooseString.equals("monthly")) {
                //Toast.makeText(this, "Clicked 0", Toast.LENGTH_SHORT).show()
                iapConnector.subscribe(
                    this@PremiumNewA,
                    binding.root.context.getString(R.string.monthly_id)
                )
            } else if (chooseString.equals("yearly")) {
                //Toast.makeText(this, "Clicked 1", Toast.LENGTH_SHORT).show()
                iapConnector.subscribe(
                    this@PremiumNewA,
                    binding.root.context.getString(R.string.yearly_id)
                )
            }
        } else {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_check_your_internet_connection))
        }
    }

    private fun initControl() {
        whereFrom="${intent.getStringExtra("whereFrom")}"

        initBilling()
    }

    private fun initBilling() {
        chooseString = "yearly"
        isBillingClientConnected.value = false
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
            enableLogging = BuildConfig.DEBUG
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
                when (purchaseInfo.sku) {
                    binding.root.context.getString(R.string.weekly_id) -> {
                        purchaseInfo.orderId
                        Log.e(
                            BillingService.TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                    binding.root.context.getString(R.string.monthly_id) -> {
                        purchaseInfo.orderId
                        Log.e(
                            BillingService.TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                    binding.root.context.getString(R.string.yearly_id) -> {
                        purchaseInfo.orderId
                        Log.e(
                            BillingService.TAG,
                            "onSubscriptionPurchased: ${purchaseInfo.orderId + " " + purchaseInfo.sku}\n"
                        )
                    }
                }
                Prefs.putString(Const.KEY_FOR_SUBSCRIPTION, "${SubscriptionEnum.PURCHASE}")
                Prefs.putString(Const.KEY_FOR_PACKAGE, purchaseInfo.toString())
                AdConst.IS_AD_SHOW=false

                onBackPressed()
            }

            @SuppressLint("SetTextI18n", "LogNotTimber")
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.ProductDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
                Log.e(BillingService.TAG, "onPricesUpdated: ${iapKeyPrices.entries}\n")
                if (iapKeyPrices.entries.isNotEmpty()) {
                    try {
                        weeklyPrice =
                            "" + iapKeyPrices[binding.root.context.getString(R.string.weekly_id)]?.price.toString()
                        monthlyPrice =
                            "" + iapKeyPrices[binding.root.context.getString(R.string.monthly_id)]?.price.toString()
                        yearlyPrice =
                            "" + iapKeyPrices[binding.root.context.getString(R.string.yearly_id)]?.price.toString()
                        binding.tvPriceWeek.text = weeklyPrice
                        binding.tvPriceMonth.text = monthlyPrice
                        binding.tvPriceYear.text = yearlyPrice
                    } catch (e: Exception) {

                    }
                }
            }
            override fun onEmptySubscription() {
                Log.e(BillingService.TAG, "onEmptySubscription: ")
                Prefs.putString(Const.KEY_FOR_SUBSCRIPTION, "${SubscriptionEnum.NOT_PURCHASE}")
                Prefs.putString(Const.KEY_FOR_PACKAGE, "")
                AdConst.IS_AD_SHOW=true
            }
        })
    }

    private fun showInterstitialAndMoveMain() {
        if (InterstitialManager.preLoadAllScreenInterstital!=null && InterstitialManager.preLoadAllScreenInterstital!!.IsAdLoaded())
        {
            InterstitialManager.preLoadAllScreenInterstital?.showInterstitial(this@PremiumNewA,object: AdCallback {
                override fun onAdComplete(isCompleted: Boolean) {
                    InterstitialManager.preLoadAllScreenInterstital?.requestInterstitialAd(this@PremiumNewA,Const.AD_NEXT_REQUEST_DELAY)
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
        val callingIntent=Intent(binding.root.context, MainActivity::class.java)
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            MyAppOpenAd.isDialogClose=true
        },300)
    }

    override fun onBackPressed() {
        MyAppOpenAd.isAppInBackground=false
        if (whereFrom.equals("SplashA"))
        {
            if (AdConst.IS_AD_SHOW)
            {
                showInterstitialAndMoveMain()
            }else
            {
                moveToMain()
            }
        }
        else
        {
            val callbackIntent=Intent()
            callbackIntent.putExtra("isShow",true)
            setResult(RESULT_OK,callbackIntent)
            finish()
            overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom)
        }
    }
}