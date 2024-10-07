package dzm.wamr.recover.deleted.messages.photo.media.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import dzm.wamr.recover.deleted.messages.photo.media.R
import dzm.wamr.recover.deleted.messages.photo.media.admanager.MyAppOpenAd

object DialogUtil {
    private var dialog: Dialog?= null

    fun showAdLoadingDialog(activity: Activity) {
        if (!activity.isFinishing && activity.window != null && dialog==null)
        {
            MyAppOpenAd.isDialogClose = false

            dialog = Dialog(activity)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setContentView(R.layout.ad_loading_dialog)
            dialog?.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
            }
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.setCancelable(false)
            dialog?.setOnDismissListener {
                MyAppOpenAd.isDialogClose = true
                MyAppOpenAd.isAppInBackground = false
            }
            dialog?.show()
        }
    }


    fun closeAdLoadingDialog()
    {
        dialog?.dismiss()
        dialog=null
    }
}