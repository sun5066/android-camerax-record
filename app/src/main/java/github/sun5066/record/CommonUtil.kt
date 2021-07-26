package github.sun5066.record

import android.app.Application
import java.io.File

object CommonUtil {


    fun getOutputFile(application: Application, fileName: String): File = with(application) {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        val file = if (mediaDir != null && mediaDir.exists()) mediaDir
        else filesDir

        return File(file, fileName)
    }
}