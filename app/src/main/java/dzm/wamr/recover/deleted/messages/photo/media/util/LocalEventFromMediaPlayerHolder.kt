package dzm.wamr.recover.deleted.messages.photo.media.util

import dzm.wamr.recover.deleted.messages.photo.media.util.MediaPlayerHolder.PlayerState

class LocalEventFromMediaPlayerHolder {
    class UpdateLog(val formattedMessage: StringBuffer)
    class PlaybackDuration(val duration: Int)
    class PlaybackPosition(val position: Int)
    class PlaybackCompleted
    class StateChanged(val currentState: PlayerState)
}