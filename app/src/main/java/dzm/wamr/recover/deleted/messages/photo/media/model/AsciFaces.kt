package dzm.wamr.recover.deleted.messages.photo.media.model


import com.google.gson.annotations.SerializedName

data class AsciFaces(
    @SerializedName("data")
    val `data`: ArrayList<String>,
    @SerializedName("kind")
    val kind: String
)