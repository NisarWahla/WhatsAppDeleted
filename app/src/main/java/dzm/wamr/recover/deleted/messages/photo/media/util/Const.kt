package dzm.wamr.recover.deleted.messages.photo.media.util

enum class SubscriptionEnum { DEFAULT, PURCHASE, NOT_PURCHASE }
object Const {
    const val tag="WRAMLOG_"
    const val AD_NEXT_REQUEST_DELAY=(16*1000).toLong()
    const val MAX_TIME_FOR_SPLASH = 16000L
    const val MIN_TIME_FOR_SPLASH = 7L
    const val MAX_TIME_LIMIT_FOR_SPLASH = 16L
    const val GIF_FIRSTPART = "https://media.giphy.com/media/"
    const val GIF_SECONDPART = "/100w.gif"
    const val GIF_DIRECTORY_NAME = ".MyGifs"
    const val KEY_PREMIUM_SHOW_COUNTER="key_premium_show_counter"
    const val KEY_IS_LANGUAGE_SET="key_is_language_set"
//    in-app billing
    const val KEY_FOR_SUBSCRIPTION="key_for_subscription"
    const val KEY_FOR_PACKAGE="key_for_package"
//    app language
    const val APP_LANGUAGE: String = "app_language"
    const val APP_LANGUAGE_CODE = "app_language_code"
    const val DEFAULT_LANGUAGE_CODE = "en"
    const val DEFAULT_LANGUAGE = "English"
//    remote config
    const val SHOW_PREMIUM_SCREEN:String="show_premium_screen"

}
