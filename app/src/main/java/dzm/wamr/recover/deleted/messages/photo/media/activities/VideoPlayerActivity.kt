package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityVideoPlayerBinding
import dzm.wamr.recover.deleted.messages.photo.media.fragments.mainFragments.StatusFragment
import dzm.wamr.recover.deleted.messages.photo.media.model.Status
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoPlayerActivity : AppCompatLocaleActivity() {
    lateinit var binding: ActivityVideoPlayerBinding
    lateinit var mediaPlayer: MediaPlayer
    lateinit var seekHandler: Handler
    private var statuslist: List<Status> = ArrayList()
    var path: String? = null
    lateinit var status: Status
    lateinit var APP_DIR11: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_video_player)

        val intent = intent
        APP_DIR11 =
            (binding.container.context.getExternalFilesDir(File("RecoveryApp${File.separator}WhatsRecovery Statuses").path)
                .toString())
        statuslist = intent.getParcelableArrayListExtra("path")!!
        val from = intent.getStringExtra("from")
        if (from.equals("status")) {
            binding.save.visibility = View.VISIBLE
        }
        for (status in statuslist) {
            this.status = status
        }
        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.R){
            binding.videoView.setVideoURI(status.fileUri)
        } else{
            binding.videoView.setVideoPath(status.path)
        }
        binding.videoView.setOnPreparedListener { mp: MediaPlayer ->
            mp.start()
//           mediaController.sh
            binding.seekBar.progress = 0
            binding.seekBar.max = mp.duration
            seekHandler.postDelayed(updateSeekBar(mp), 15)
            binding.play.setOnClickListener {
                if (mp.isPlaying) {
                    binding.play.setImageDrawable(
                        ContextCompat.getDrawable(binding.root.context,R.drawable.play_icon))
                    mp.pause()
                } else {
                    binding.play.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_pause))
                    mp.start()
                }

            }
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
//                            mp.seekTo(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    mp.seekTo(seekBar!!.progress)
                }

            })
//                    mp.isLooping = true
        }


        mediaPlayer = MediaPlayer()
        seekHandler = Handler()

        binding.videoView.start()

//        binding.videoView.setOnPreparedListener {
//
//        }
        try {
            mediaPlayer.start()
            binding.seekBar.progress = 0
            binding.seekBar.max = binding.videoView.duration

            // Updating progress bar
//            seekHandler.postDelayed(updateSeekBar, 15)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Glide.with(this)
                .asBitmap()
                .load(status.fileUri)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {

                        binding.save.setOnClickListener {


//                                Log.d("onResourceReady", "onResourceReady: running")
                            if (Common.checkImageValue(status, APP_DIR11)) {
                                Utils.showToast(binding.root.context,binding.root.context.getString(R.string.file_already_exists))
                            } else {


                                Common.copyFileFromUri(
                                    status,
                                    binding.container.context,

                                    resource,
                                    status.title!!,
                                    APP_DIR11
                                )
                                StatusFragment.RefreshDownloads()
                            }

                        }
                        binding.share.setOnClickListener {
                            val APP_DIR11: String? =
                                binding.container.context.getExternalFilesDir(File("RecoveryApp${File.separator}WhatsRecovery Statuses").path)
                                    ?.toString()
                            val shareIntent =
                                Intent(Intent.ACTION_SEND)
                            shareIntent.type = "image/jpg"
                            val uri: File = Common.getFileFromUri(
                                status,
                                binding.container.context,

                                resource,
                                status.title!!,
                                APP_DIR11!!
                            )
                            val ur: Uri =
                                FileProvider.getUriForFile(
                                    binding.container.context,
                                    binding.container.context.packageName.toString() + ".provider",
                                    uri
                                )
                            shareIntent.putExtra(Intent.EXTRA_STREAM, ur)
                            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            MyAppOpenAd.isDialogClose=false
                            resultShareCallback.launch(Intent.createChooser(
                                shareIntent,
                                binding.root.context.getString(R.string.share_image)
                            ))
                        }

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

        } else {

            binding.share.setOnClickListener {
                val shareIntent =
                    Intent(Intent.ACTION_SEND)
                if (status.isVideo){
                    val ur: Uri =
                        FileProvider.getUriForFile(
                            binding.container.context,
                            binding.container.context.packageName.toString() + ".provider",
                            status.file!!
                        )
                    shareIntent.type = "video/*"
                    shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    shareIntent.putExtra(
                        Intent.EXTRA_STREAM,
                        ur
                    )

                } else{
                    val ur: Uri =
                        FileProvider.getUriForFile(
                            binding.container.context,
                            binding.container.context.packageName.toString() + ".provider",
                            status.file!!
                        )
                    shareIntent.type = "image/jpg"
                    shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    shareIntent.putExtra(
                        Intent.EXTRA_STREAM,
                        ur
                    )

                }

                MyAppOpenAd.isDialogClose=false
                resultShareCallback.launch(Intent.createChooser(
                    shareIntent,
                    binding.root.context.getString(R.string.share)
                ))

            }
            binding.save.setOnClickListener {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "RecoveryApp${File.separator}WhatsRecovery Statuses"
                    )
                    if (!file.exists()) {
                        file.mkdirs()
                        if (!file.exists()) {
                            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.please_reload_again))
                        }
                    }
                    val fileName: String
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

                    fileName = if (status.isVideo) {
                        "${status.title}.mp4"
                    } else {
                        "${status.title}.jpg"
                    }
//                    val destFile = File(file.toString() + File.separator + fileName)
                    status.file?.let { it1 -> Common.copySingleFile(it1, file) }
//                Common.copyFile(status, container.context, container)
                    Utils.showToast(binding.root.context,binding.root.context.getString(R.string.this_is_saved))

                    StatusFragment.RefreshDownloads()
                } else {
                    Common.copyFile(status, this)
                }

            }
        }
    }

    val resultShareCallback = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        MyAppOpenAd.isDialogClose=true
        MyAppOpenAd.isAppInBackground=false
    }


    /**
     * Background Runnable thread
     */
    private fun updateSeekBar(mp: MediaPlayer): Runnable = object : Runnable {
        override fun run() {
            try {
                //            if (mp.isPlaying){
//                view1.play.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.ic_pause))
//            } else{
//                view1.play.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.play_icon))
//            }
                val totalDuration: Int = mp.duration
                val currentDuration: Int = mp.currentPosition
                // Displaying Total Duration time
                binding.duration.text = "" + milliSecondsToTimer(totalDuration - currentDuration)
                // Displaying time completed playing
//            elapsed.setText("" + milliSecondsToTimer(currentDuration))

                // Updating progress bar
                binding.seekBar.progress = currentDuration.toInt()

                // Call this thread again after 15 milliseconds => ~ 1000/60fps
                seekHandler.postDelayed(this, 15)
            } catch (e: Exception) {
                Utils.showToast(binding.root.context,"${e.message}")
            }
        }

    }

    fun milliSecondsToTimer(milliseconds: Int): String? {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }

    override fun onBackPressed() {
        if (seekHandler != null) {
            seekHandler.removeCallbacksAndMessages(null)
        }
        super.onBackPressed()
    }
}