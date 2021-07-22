package github.sun5066.record

import android.graphics.SurfaceTexture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CameraRecorder: CoroutineScope {

    private val job by lazy { Job() }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun startPreview(surfaceTexture: SurfaceTexture) {

    }
}