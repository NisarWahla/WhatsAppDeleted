package dzm.wamr.recover.deleted.messages.photo.media.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import dzm.wamr.recover.deleted.messages.photo.media.util.LocalEventFromMainActivity.*
import dzm.wamr.recover.deleted.messages.photo.media.util.LocalEventFromMediaPlayerHolder.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MediaPlayerHolder(private val mContext: Context) {
    private var mResourceUri: Uri? = null
    private val mMediaPlayer: MediaPlayer?
    private val mLogMessages = ArrayList<String>()
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekbarProgressUpdateTask: Runnable? = null

    enum class PlayerState {
        PLAYING, PAUSED, COMPLETED, RESET,EMPETY
    }

    init {
        EventBus.getDefault().register(this)
        mMediaPlayer = MediaPlayer()
        mMediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            stopUpdatingSeekbarWithPlaybackProgress(true)
            logToUI("MediaPlayer playback completed")
            EventBus.getDefault().post(PlaybackCompleted())
            EventBus.getDefault()
                .post(
                    StateChanged(
                        PlayerState.COMPLETED
                    )
                )
        })
        logToUI("mMediaPlayer = new MediaPlayer()")
    }

    // MediaPlayer orchestration.
    fun release() {
        logToUI("release() and mMediaPlayer = null")
        mMediaPlayer!!.release()
        EventBus.getDefault().unregister(this)
    }

    fun play() {
        if (!mMediaPlayer!!.isPlaying) {
            logToUI(
                String.format(
                    "start() %s",
                   "Audio"
                )
            )
            mMediaPlayer.start()
            startUpdatingSeekbarWithPlaybackProgress()
            EventBus.getDefault()
                .post(StateChanged(PlayerState.PLAYING))
        }
    }

    fun pause() {
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer.pause()
            logToUI("pause()")
            EventBus.getDefault()
                .post(StateChanged(PlayerState.PAUSED))
        }
    }

    fun reset() {
        logToUI("reset()")
        mMediaPlayer!!.reset()
        mResourceUri?.let { resourceUri->
            load(resourceUri)
            stopUpdatingSeekbarWithPlaybackProgress(true)
            EventBus.getDefault()
                .post(StateChanged(PlayerState.RESET))
        }

    }

    fun load(resourceUri:Uri) {
        mResourceUri = resourceUri
        try {
            logToUI("load() {1. setDataSource}")
            mMediaPlayer!!.setDataSource(mContext,mResourceUri!!)
        } catch (e: Exception) {
            logToUI(e.toString())
        }
        try {
            logToUI("load() {2. prepare}")
            mMediaPlayer!!.prepare()
        } catch (e: Exception) {
            logToUI(e.toString())
        }
        initSeekbar()
    }

    fun seekTo(duration: Int) {
        logToUI(String.format("seekTo() %d ms", duration))
        mMediaPlayer!!.seekTo(duration)
    }

    // Reporting media playback position to Seekbar in MainActivity.
    private fun stopUpdatingSeekbarWithPlaybackProgress(resetUIPlaybackPosition: Boolean) {
        if (mExecutor!=null)
        {
            mExecutor!!.shutdownNow()
            mExecutor = null
            mSeekbarProgressUpdateTask = null
            if (resetUIPlaybackPosition) {
                EventBus.getDefault().post(PlaybackPosition(0))
            }
        }
    }

    private fun startUpdatingSeekbarWithPlaybackProgress() {
        // Setup a recurring task to sync the mMediaPlayer position with the Seekbar.
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor()
        }
        if (mSeekbarProgressUpdateTask == null) {
            mSeekbarProgressUpdateTask = Runnable {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying) {
                    val currentPosition = mMediaPlayer.currentPosition
                    EventBus.getDefault().post(
                        PlaybackPosition(
                            currentPosition
                        )
                    )
                }
            }
        }
        mExecutor!!.scheduleAtFixedRate(
            mSeekbarProgressUpdateTask,
            0,
            SEEKBAR_REFRESH_INTERVAL_MS.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    fun initSeekbar() {
        // Set the duration.
        val duration = mMediaPlayer!!.duration
        EventBus.getDefault().post(
            PlaybackDuration(duration)
        )
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: SeekTo) {
        seekTo(event.position)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(
        event: StopUpdatingSeekbarWithMediaPosition?
    ) {
        stopUpdatingSeekbarWithPlaybackProgress(false)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(
        event: StartUpdatingSeekbarWithPlaybackPosition?
    ) {
        startUpdatingSeekbarWithPlaybackProgress()
    }

    // Logging to UI methods.
    fun logToUI(msg: String) {
        mLogMessages.add(msg)
        fireLogUpdate()
    }

    /**
     * update the MainActivity's UI with the debug log messages
     */
    fun fireLogUpdate() {
        val formattedLogMessages = StringBuffer()
        for (i in mLogMessages.indices) {
            formattedLogMessages.append(i)
                .append(" - ")
                .append(mLogMessages[i])
            if (i != mLogMessages.size - 1) {
                formattedLogMessages.append("\n")
            }
        }
        EventBus.getDefault().post(
            UpdateLog(formattedLogMessages)
        )
    }

    // Respond to playback localevents.
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: PausePlayback?) {
        pause()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: StartPlayback?) {
        play()
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(event: ResetPlayback?) {
        reset()
    }

    companion object {
        const val SEEKBAR_REFRESH_INTERVAL_MS = 1000
    }
}