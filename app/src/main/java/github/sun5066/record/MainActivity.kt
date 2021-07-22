package github.sun5066.record

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import github.sun5066.record.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var camera: CameraDevice? = null
    private var imgReader: ImageReader? = null
    private var session: CameraCaptureSession? = null

    private var imgBkgThread: HandlerThread? = null
    private var cameraBkgThread: HandlerThread? = null
    private var sessionBkgThread: HandlerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStop() {
        super.onStop()

        imgReader?.close()
        session?.close()
        camera?.close()

        imgBkgThread?.quit()
        sessionBkgThread?.quit()
        cameraBkgThread?.quit()
    }


    @DelicateCoroutinesApi
    fun CameraManager.openCameraChannel(cameraId: String, handler: Handler): Channel<CameraDevice> {
        val channel = Channel<CameraDevice>()

        val callback = object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                GlobalScope.launch { channel.send(camera) }
            }

            override fun onDisconnected(camera: CameraDevice) = throw Exception()

            override fun onError(camera: CameraDevice, error: Int) = throw Exception()
        }

        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            openCamera(cameraId, callback, handler)
        }
        return channel
    }

    private fun startCameraPreview() {
        try {
            val cameraHandler = Handler(mainLooper)
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

            cameraManager.cameraIdList.find {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)

                return@find cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_BACK
            }?.let {
                val cameraStateCallback = object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        val captureSessionCallback = object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                builder.addTarget(binding.preview.holder.surface)
                                session.setRepeatingRequest(builder.build(), null, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.d("123", "onConfigureFailed()")
                            }
                        }

                        val list = listOf<Surface>(binding.preview.holder.surface)
                        val sessionConfiguration = SessionConfiguration(SessionConfiguration.SESSION_REGULAR, list, cameraHandler, captureSessionCallback)
                        camera.createCaptureSession(
                            listOf(binding.preview.holder.surface),
                            captureSessionCallback,
                            cameraHandler
                        )
                    }
                    override fun onDisconnected(camera: CameraDevice) {

                    }
                    override fun onError(camera: CameraDevice, error: Int) {

                    }
                }
            }
        } catch (e: Exception) {

        }
    }
}