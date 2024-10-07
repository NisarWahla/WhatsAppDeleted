package dzm.wamr.recover.deleted.messages.photo.media.viewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dzm.wamr.recover.deleted.messages.photo.media.model.Status

class SharedViewModel(application: Application) : BaseViewModel(application) {

    private var status = MutableLiveData<ArrayList<Status>>()
    private var downloadStatus = MutableLiveData<ArrayList<Status>>()
    private var loadStatusData = MutableLiveData<Boolean>()

    fun updateAllDocument(list: ArrayList<Status>) {
        status.postValue(list)
    }

    /**
     *
     */
    fun getDocumentedFiles(): MutableLiveData<ArrayList<Status>> {
        return status
    }

    fun updateAllDownloadsStatus(list: ArrayList<Status>) {
        downloadStatus.postValue(list)
    }

    fun getDownloadsStatus(): MutableLiveData<ArrayList<Status>> {
        return downloadStatus
    }

    fun loadStatusFiles() {

        loadStatusData.postValue(true)
    }

    fun getLoadFiles(): MutableLiveData<Boolean> {
        return loadStatusData
    }
}