package io.github.me0wzz.music.fragments.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.me0wzz.music.activities.MainActivity
import io.github.me0wzz.music.helper.BackupContent
import io.github.me0wzz.music.helper.BackupHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess


class BackupViewModel : ViewModel() {
    private val backupsMutableLiveData = MutableLiveData<List<File>>()
    val backupsLiveData: LiveData<List<File>> = backupsMutableLiveData

    fun loadBackups(context: Context) {
        BackupHelper.getBackupRoot().listFiles { _, name ->
            return@listFiles name.endsWith(BackupHelper.BACKUP_EXTENSION)
        }?.toList()?.let {
            backupsMutableLiveData.value = it
        }
    }

    suspend fun restoreBackup(activity: Activity, inputStream: InputStream?, contents: List<BackupContent>) {
        BackupHelper.restoreBackup(activity, inputStream, contents)
        if (contents.contains(BackupContent.SETTINGS) or contents.contains(BackupContent.CUSTOM_ARTIST_IMAGES)) {
            // We have to restart App when Preferences i.e. Settings or Artist Images are to be restored
            withContext(Dispatchers.Main) {
                val intent = Intent(
                    activity,
                    MainActivity::class.java
                )
                activity.startActivity(intent)
                exitProcess(0)
            }
        }
    }
}