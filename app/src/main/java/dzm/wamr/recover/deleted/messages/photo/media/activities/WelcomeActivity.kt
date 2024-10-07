package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.BuildConfig
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.adapter.ViewPagerAdapter
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityWelcomeBinding
import dzm.wamr.recover.deleted.messages.photo.media.firebase.NotificationListener
import dzm.wamr.recover.deleted.messages.photo.media.fragments.OnBoardingFragment
import dzm.wamr.recover.deleted.messages.photo.media.fragments.TermsAndConditionF
import dzm.wamr.recover.deleted.messages.photo.media.util.*


class WelcomeActivity :AppCompatLocaleActivity() {

    var adapter:ViewPagerAdapter? =null
    lateinit var prefManager:PrefManager

    private var onBoardingPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            showSelectedTabs(position)
        }
    }

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_welcome)
        prefManager = PrefManager(binding.root.context)
        adapter = ViewPagerAdapter(this)
        registerFragmentWithPager()
        binding.vp2Pager.offscreenPageLimit=1
        binding.vp2Pager.adapter = adapter
        binding.vp2Pager.registerOnPageChangeCallback(onBoardingPageChangeCallback)
        binding.vp2Pager.isUserInputEnabled = false
        binding.btnNext.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            applyForecePermissionLogic()
        }))
    }

    private fun registerFragmentWithPager() {
        tabSetupWithPolicy()
        if (!(prefManager.getAcceptTermOfUse(BuildConfig.VERSION_NAME)))
        {
            adapter?.addFrag(TermsAndConditionF.newInstance())
        }
        adapter?.addFrag(OnBoardingFragment.getInstance(0))
        adapter?.addFrag(OnBoardingFragment.getInstance(1))
        adapter?.addFrag(OnBoardingFragment.getInstance(2))
        adapter?.addFrag(OnBoardingFragment.getInstance(3))
    }

    private fun tabSetupWithPolicy() {
        if (!(prefManager.getAcceptTermOfUse(BuildConfig.VERSION_NAME)))
        {
            binding.ivFourCircle.visibility=View.VISIBLE
            binding.btnNext.text=binding.root.context.getString(R.string.accept)
        }
        else
        {
            binding.ivFourCircle.visibility=View.GONE
            binding.btnNext.text=binding.root.context.getString(R.string.next)
        }
    }


    override fun onDestroy() {
        binding.vp2Pager.unregisterOnPageChangeCallback(onBoardingPageChangeCallback)
        super.onDestroy()
    }


    private fun showSelectedTabs(position: Int) {
        val totalCount=binding.vp2Pager.adapter?.itemCount
        if (totalCount==4 && position==0)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.next)
        } else if (totalCount==4 && position==1)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.enable)
        } else if (totalCount==4 && position==2)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.enable)
        } else if (totalCount==4 && position==3)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.next)
        } else if (totalCount==5 && position==0)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.accept)
        } else if (totalCount==5 && position==1)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.next)
        } else if (totalCount==5 && position==2)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.enable)
        } else if (totalCount==5 && position==3)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.enable)
        } else if (totalCount==5 && position==4)
        {
            binding.btnNext.text=binding.root.context.getString(R.string.next)
        }


        if (position==0)
        {

            binding.ivZeroCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_slider_blue))
            binding.ivFirstCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivSecondCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivThirdCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFourCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
        }
        if (position==1)
        {
            binding.ivZeroCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFirstCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_slider_blue))
            binding.ivSecondCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivThirdCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFourCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
        }
        if (position==2)
        {
            binding.ivZeroCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFirstCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivSecondCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_slider_blue))
            binding.ivThirdCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFourCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
        }
        if (position==3)
        {
            binding.ivZeroCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFirstCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivSecondCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivThirdCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_slider_blue))
            binding.ivFourCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
        }
        if (position==4)
        {
            binding.ivZeroCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFirstCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivSecondCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivThirdCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_circle_gray))
            binding.ivFourCircle.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.comp_view_slider_blue))
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



    private val storagePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck = mutableListOf<String>()

            for (entry in result.entries) {
                val (key, value) = entry
                if (!value) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Utils.getPermissionStatus(this@WelcomeActivity, key))
                }
            }

            if (blockPermissionCheck.contains("blocked")) {
                Utils.showPermissionSetting(
                    binding.root.context,
                    binding.root.context.getString(R.string.storage_permission_description)
                )
            } else if (allPermissionClear) {
                //do noting action not define

            }
            moveNextTab()
            MyAppOpenAd.isDialogClose=true
            MyAppOpenAd.isAppInBackground=false
        }


    private val notificationPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck = mutableListOf<String>()

            for (entry in result.entries) {
                val (key, value) = entry
                if (!value) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Utils.getPermissionStatus(this@WelcomeActivity, key))
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


    private val foregroundPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            var allPermissionClear = true
            val blockPermissionCheck = mutableListOf<String>()

            for (entry in result.entries) {
                val (key, value) = entry
                if (!value) {
                    allPermissionClear = false
                    blockPermissionCheck.add(Utils.getPermissionStatus(this@WelcomeActivity, key))
                }
            }

            if (blockPermissionCheck.contains("blocked")) {
                Utils.showPermissionSetting(
                    binding.root.context,
                    binding.root.context.getString(R.string.please_allow_wamr_notification_permission_description)
                )
            } else if (allPermissionClear) {
                //do noting action not define
                moveNextTab()
            }
            MyAppOpenAd.isDialogClose=true
            MyAppOpenAd.isAppInBackground=false
        }


    private fun makeFirstNotificationChannel() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MyNotifications", "MyNotifications", importance)
            //            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }

        val isMyServiceRunning = Utils.isServiceRunning(binding.root.context, NotificationListener::class.java)
        if (isMyServiceRunning) {
            moveNextTab()
        }

    }


    private fun moveToFolderPermission() {
        val callingIntent=Intent(this@WelcomeActivity, FolderPermissionClass::class.java)
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun moveToMain() {
        val callingIntent=Intent(this@WelcomeActivity, MainActivity::class.java)
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }



    private fun applyForecePermissionLogic() {
        val totalCount=binding.vp2Pager.adapter?.itemCount
        Log.d(Const.tag,"totalCount: $totalCount")
        if (totalCount==5 && binding.vp2Pager.currentItem==0)
        {
            prefManager.setAcceptTermOfUse(true, BuildConfig.VERSION_NAME)
            moveNextTab()

        } else if (totalCount==5 && binding.vp2Pager.currentItem==1)
        {
            grantForegroundPermission()
        } else if (totalCount==4 && binding.vp2Pager.currentItem==0)
        {
            grantForegroundPermission()
        } else if (totalCount==5 && binding.vp2Pager.currentItem==2)
        {
            grantNotificationListenerPermission()
        } else if (totalCount==4 && binding.vp2Pager.currentItem==1)
        {
            grantNotificationListenerPermission()
        } else if (totalCount==5 && binding.vp2Pager.currentItem==3)
        {
            grantStoragePermission()
        } else if (totalCount==4 && binding.vp2Pager.currentItem==2)
        {
            grantStoragePermission()
        } else if (totalCount==5 && binding.vp2Pager.currentItem==4)
        {
            grantFolderPermission()
        } else if (totalCount==4 && binding.vp2Pager.currentItem==3)
        {
            grantFolderPermission()
        } else {
            moveNextTab()
        }
    }

    private fun grantFolderPermission() {
        MyAppOpenAd.isAppInBackground=false
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Utils.isPackageInstalled("com.whatsapp",packageManager))
            {
                moveToFolderPermission()
            } else
            {
                moveToMain()
            }

        } else {
            val prefManager = PrefManager(binding.root.context)
            prefManager.setFirstTimeLaunch(false)
            moveToMain()
        }
    }

    private fun grantStoragePermission() {
        val permissionUtil= PermissionUtils(this@WelcomeActivity,storagePermissionResult)
        if (!(permissionUtil.isStoragePermissionGranted()))
        {
            MyAppOpenAd.isDialogClose=false
            permissionUtil.takeStoragePermission()
        }
        else
        {
            moveNextTab()
        }
    }

    private fun grantForegroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            val permissionUtil= PermissionUtils(this@WelcomeActivity,foregroundPermissionResult)
            if (!(permissionUtil.isForegroundPermissionGranted()))
            {
                MyAppOpenAd.isDialogClose=false
                showForegroundDialog(permissionUtil)
            }
            else
            {
                moveNextTab()
            }
        }
        else
        {
            moveNextTab()
        }
    }

    private fun grantNotificationListenerPermission() {
        val isMyServiceRunning = Utils.isServiceRunning(binding.root.context, NotificationListener::class.java)
        if (isMyServiceRunning) {
            moveNextTab()
        } else {
            val permissionUtil= PermissionUtils(this@WelcomeActivity,notificationPermissionResult)
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

    private fun moveNextTab() {
        binding.vp2Pager.currentItem = binding.vp2Pager.currentItem + 1
    }


    private fun showForegroundDialog(permissionUtil:PermissionUtils) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.service_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val denyBtn = dialog.findViewById<Button>(R.id.deny_btn)
        val allowBtn = dialog.findViewById<Button>(R.id.allow_btn)
        denyBtn.setOnClickListener {
            serviceDenyDialog(permissionUtil)
            dialog.dismiss()
        }
        allowBtn.setOnClickListener {
            permissionUtil.takeForegroundPermission()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun serviceDenyDialog(permissionUtil:PermissionUtils) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.service_deny_dialog)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val okBtn = dialog.findViewById<Button>(R.id.ok_btn)
        okBtn.setOnClickListener {
            permissionUtil.takeForegroundPermission()
            dialog.dismiss()
        }
        dialog.show()
    }


}