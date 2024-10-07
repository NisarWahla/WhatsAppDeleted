package dzm.wamr.recover.deleted.messages.photo.media.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.pixplicity.easyprefs.library.Prefs
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.NativeAdManager
import dzm.wamr.recover.deleted.messages.photo.media.adsmodule.AdConst
import dzm.wamr.recover.deleted.messages.photo.media.databinding.ActivityAudioPlayerBinding
import dzm.wamr.recover.deleted.messages.photo.media.model.StatusDataModel
import dzm.wamr.recover.deleted.messages.photo.media.util.*
import dzm.wamr.recover.deleted.messages.photo.media.util.LocalEventFromMainActivity.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit


class AudioPlayerA : AppCompatLocaleActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    private var selectedItem: String? = null
    private var isUserSeeking = false
    var mMediaPlayerHolder:MediaPlayerHolder ?=null
    var currentStatus: MediaPlayerHolder.PlayerState = MediaPlayerHolder.PlayerState.EMPETY
    var currentPosition:Int=0
    var statusDataList: ArrayList<StatusDataModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setLocale(Prefs.getString(Const.APP_LANGUAGE_CODE, Const.DEFAULT_LANGUAGE_CODE), this, javaClass,false)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_audio_player)
        initControl()
        actionControl()
    }

    private fun actionControl() {
        binding.ivPlay.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            if (currentStatus==MediaPlayerHolder.PlayerState.PLAYING)
            {
                pause()
            }
            else
            {
                play()
            }

        }))
        binding.btStepBack.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            moveToPreviousAudio()
        }))
        binding.btStepForward.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            moveToNextAudio()
        }))
        binding.back.setOnClickListener(DebounceClickHandler(View.OnClickListener {
            onBackPressed()
        }))
    }

    private fun moveToNextAudio() {
        if (statusDataList.size>0 && currentPosition<(statusDataList.size-1))
        {
            currentPosition=currentPosition+1
            initPlayerWithNewSelection()
        } else
        {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.no_audio_found))
        }
    }

    private fun initPlayerWithNewSelection() {
        selectCurrentAudio()
        setupAudioTitle()
        resetPlayer()
    }

    private fun moveToPreviousAudio() {
        if (statusDataList.size>0 && currentPosition>0)
        {
            currentPosition=currentPosition-1
            initPlayerWithNewSelection()
        } else
        {
            Utils.showToast(binding.root.context,binding.root.context.getString(R.string.no_audio_found))
        }
    }

    private fun initControl() {
        if (intent.hasExtra("currentPosition")) {
            currentPosition = intent.getIntExtra("currentPosition",0)
        }
        if (intent.hasExtra("audioList")) {
            val audioList:ArrayList<StatusDataModel> = intent.getSerializableExtra("audioList") as ArrayList<StatusDataModel>
            statusDataList.clear()
            statusDataList.addAll(audioList)
        }
        selectCurrentAudio()
        loadAppNative(this@AudioPlayerA)
        initPlayer()
        setupSeekbar()
    }

    private fun selectCurrentAudio() {
        if (!(statusDataList.isEmpty()))
        {
            selectedItem=statusDataList.get(currentPosition).filePath
        }
    }

    private fun initPlayer() {
        EventBus.getDefault().register(this)
        mMediaPlayerHolder=MediaPlayerHolder(binding.root.context)
        setupAudioTitle()
        mMediaPlayerHolder?.load(Uri.parse(selectedItem))
    }

    private fun resetPlayer(){
        mMediaPlayerHolder?.load(Uri.parse(selectedItem))
        reset()
    }

    private fun setupAudioTitle() {
        binding.textAudioName.text="${Utils.getFileNameWithoutExtension("${selectedItem}")}"

        Log.d(Const.tag,"selectedItem: ${selectedItem}")
    }

    fun setupSeekbar() {
        binding.mSeekbarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // This holds the progress value for onStopTrackingTouch.
            var userSelectedPosition = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Only fire seekTo() calls when the user stops the touch event.
                if (fromUser) {
                    userSelectedPosition = progress
                    isUserSeeking = true
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                EventBus.getDefault().post(LocalEventFromMainActivity.SeekTo(userSelectedPosition))
            }
        })
    }


    fun pause() {
        EventBus.getDefault().post(PausePlayback())
    }

    fun play() {
        EventBus.getDefault().post(StartPlayback())
    }

    fun reset() {
        EventBus.getDefault().post(ResetPlayback())
    }


    // Event subscribers.
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.UpdateLog) {
//        Log.d(Const.tag,"information: ${event.formattedMessage}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackDuration) {
        binding.mSeekbarAudio.max = event.duration
        val currentTime=TimeUnit.MILLISECONDS.toSeconds(event.duration.toLong())
        binding.textDuration.text="${Utils.formatTimePlayerTime(currentTime)}"
    }

    private fun updateCurrentTime(currentTime: Long) {
        binding.textElapsed.text="${Utils.formatTimePlayerTime(currentTime)}"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.PlaybackPosition) {
        if (!isUserSeeking) {
            binding.mSeekbarAudio.setProgress(event.position, true)
            val currentTime=TimeUnit.MILLISECONDS.toSeconds(event.position.toLong())
            updateCurrentTime(currentTime)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: LocalEventFromMediaPlayerHolder.StateChanged) {
        currentStatus=event.currentState
        if (event.currentState==MediaPlayerHolder.PlayerState.PLAYING)
        {
            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.pause_img))
        }
        else
        {
            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(binding.root.context,R.drawable.play_arrow))
        }
    }



    override fun onPause() {
        super.onPause()
        if (currentStatus==MediaPlayerHolder.PlayerState.PLAYING)
        {
            pause()
        }
    }


    private var nativeManger: NativeAdManager?= null
    private fun loadAppNative(activity: Activity) {
        if (AdConst.IS_AD_SHOW){
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



    override fun onBackPressed() {
        val callbackIntent= Intent()
        callbackIntent.putExtra("isShow",true)
        setResult(RESULT_OK,callbackIntent)
        finish()
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
    }


    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayerHolder?.release()
        EventBus.getDefault().unregister(this)
    }

}