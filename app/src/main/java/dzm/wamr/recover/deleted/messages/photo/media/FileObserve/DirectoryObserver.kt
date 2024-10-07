package dzm.wamr.recover.deleted.messages.photo.media.FileObserve

import android.os.FileObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class DirectoryObserver(private val directoryPath: String) : FileObserver(directoryPath) {

    private val _fileList = MutableLiveData<List<String>>()
    val fileList: LiveData<List<String>> get() = _fileList

    init {
        startWatching()
        updateFileList()
    }

    private fun updateFileList() {
        val directory = File(directoryPath)
        val files = directory.listFiles()?.map { it.name } ?: emptyList()
        _fileList.postValue(files)
    }

    override fun onEvent(event: Int, path: String?) {
        // This method will be called when there are changes in the directory
        // Update the file list whenever there is a change
        updateFileList()
    }

    override fun stopWatching() {
        stopWatching()
    }
}