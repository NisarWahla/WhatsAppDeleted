package dzm.wamr.recover.deleted.messages.photo.media.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class Status(
    val file: File? = null, val title: String? = null, val path: String? = null,
    val isVideo: Boolean = false, val fileUri: Uri? = null
) : Parcelable
