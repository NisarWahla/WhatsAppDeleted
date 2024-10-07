package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityFolderPermissionClassBinding
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.File



private const val REQUEST_ACTION_OPEN_DOCUMENT_TREE_Business: Int = 1020

class  FolderPermissionClass : AppCompatLocaleActivity() {

    private lateinit var binding: ActivityFolderPermissionClassBinding
    var prefManager: PrefManager? =null

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NewApi", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_folder_permission_class)
        prefManager = PrefManager(binding.root.context)
//        binding.cardWhatsappBusiness.setOnClickListener {
//            callWhatsappBusinessPersistantRequest()
//        }
        val file: File = File(
             Environment.getExternalStorageDirectory()
                .toString() + "/Android/media/com.whatsapp/WhatsApp/Media"
        )
        binding.cardWhatsappNormal.setOnClickListener {
            if (isPackageExisted("com.whatsapp") && file.exists())
            {
                applyFolderAccess()
            }
        }
        binding.viewWhatsappNormal.setOnClickListener {
            if (isPackageExisted("com.whatsapp") && file.exists())
            {
                applyFolderAccess()
            }
        }
        binding.btnNext.setOnClickListener {
            if ( binding.cardWhatsappNormal.visibility == View.VISIBLE)
            {
                if (binding.granted.isChecked) {
                    moveToNext()
                } else {
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.grant_permission))
                }
            } else
            {
                moveToNext()
            }
        }

        if (Utils.isPackageInstalled("com.whatsapp",packageManager)){
            binding.cardWhatsappNormal.visibility = View.VISIBLE
        }else{
            binding.cardWhatsappNormal.visibility = View.GONE
        }

//        if (Utils.isPackageInstalled("com.whatsapp.w4b",packageManager)){
//            binding.cardWhatsappBusiness.visibility =View.VISIBLE
//        }else{
            binding.cardWhatsappBusiness.visibility = View.GONE
            binding.grantedBusiness.isChecked=true
//        }

//        binding.grantedBusiness.setOnCheckedChangeListener { compoundBtn, _ ->
//            if (compoundBtn.isChecked) {
//                callWhatsappBusinessPersistantRequest()
//            }
//        }

    }

    private fun moveToNext() {
        MyAppOpenAd.isAppInBackground=false
        moveToMain()
    }


    private val launcherForOpenDocResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                try {
                    val uri = it.data
                    if (uri!=null)
                    {
                        binding.root.context.contentResolver.takePersistableUriPermission(
                            uri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                    prefManager?.setWATree(uri.toString())
                }catch (e:Exception)
                {

                }

                binding.granted.isChecked = true
                val prefManager = PrefManager(binding.root.context)
                prefManager.setFirstTimeLaunch(false)

            }
        }
    }
    private fun applyFolderAccess() {
        if (Utils.appInstalledOrNot(binding.root.context, "com.whatsapp")) {
            val sm = binding.root.context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val statusDir = whatsupFolder
            var intent: Intent? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
                var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
                var scheme = uri.toString()
                scheme = scheme.replace("/root/", "/document/")
                scheme += "%3A$statusDir"
                uri = Uri.parse(scheme)
                intent.putExtra("android.provider.extra.INITIAL_URI", uri)
            } else {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.putExtra(
                    "android.provider.extra.INITIAL_URI",
                    Uri.parse("content://com.android.externalstorage.documents/document/primary%3A$statusDir")
                )
            }
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            MyAppOpenAd.isDialogClose=false
            launcherForOpenDocResult.launch(intent)
        } else {
            Toast.makeText(
                binding.root.context, binding.root.context.getString(R.string.please_install_whatsapp_for_download_status),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val whatsupFolder: String
        get() = if (File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + ".Statuses"
            ).isDirectory
        ) {
            "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
        } else {
            "WhatsApp%2FMedia%2F.Statuses"
        }





    private fun moveToMain() {
        val callingIntent=Intent(this@FolderPermissionClass, MainActivity::class.java)
        callingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(callingIntent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }
    fun isPackageExisted(targetPackage: String?): Boolean {
        val packages: List<ApplicationInfo>
        val pm: PackageManager = packageManager
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName.equals(targetPackage)) return true
        }
        return false
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun getUri(): Uri {

        //String startDir = "Android";
        //String startDir = "Download"; // Not choosable on an Android 11 device
        //String startDir = "DCIM";
        //String startDir = "DCIM/Camera";  // replace "/", "%2F"
        //String startDir = "DCIM%2FCamera";
        // String startDir = "Documents";
        var startDir = "/Android/media/com.whatsapp/WhatsApp/Media"
        val file = File(startDir)
//        if (file.exists()) {
//            startDir = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses";
//        }
        val sm = applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        var secondDir: String
        var finalDirPath: String
        val uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString()
        Log.d("error", "INITIAL_URI scheme: $scheme")
        scheme = scheme.replace("/root/", "/document/")

        finalDirPath = "$scheme%3A$startDir";
        startDir = startDir.replace("/", "%2F")
        scheme += "%3A$startDir"
        return Uri.parse(finalDirPath)
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun getUriBusiness(): Uri {
        val sm = applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        //String startDir = "Android";
        //String startDir = "Download"; // Not choosable on an Android 11 device
        //String startDir = "DCIM";
        //String startDir = "DCIM/Camera";  // replace "/", "%2F"
        //String startDir = "DCIM%2FCamera";
        // String startDir = "Documents";
        var startDir = "Android/media/com.whatsapp.w4b/WhatsApp%20Business"
        val uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        var scheme = uri.toString()
        Log.d("error", "INITIAL_URI scheme: $scheme")
        scheme = scheme.replace("/root/", "/document/")
        startDir = startDir.replace("/", "%2F")
        scheme += "%3A$startDir"
        return Uri.parse(scheme)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun callWhatsappBusinessPersistantRequest() {
        val sm = applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
        val intent = sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        val uri = getUriBusiness()
        intent.putExtra("android.provider.extra.INITIAL_URI", uri)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        Log.d("error", "uri: $uri")
        startActivityForResult(
            intent,
            REQUEST_ACTION_OPEN_DOCUMENT_TREE_Business
        )
    }



    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            MyAppOpenAd.isDialogClose=true
        },300)
    }
}