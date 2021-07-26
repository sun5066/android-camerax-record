package github.sun5066.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import github.sun5066.record.databinding.ActivityMainBinding

@SuppressLint("RestrictedApi")
class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private lateinit var binding: ActivityMainBinding
    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(this) }

    private val cameraSelector: CameraSelector by lazy {
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
    }
    private lateinit var cameraProvider: ProcessCameraProvider
    private val videoCapture: VideoCapture by lazy {
        VideoCapture.Builder()
            .setTargetRotation(binding.preview.display.rotation)
            .setCameraSelector(cameraSelector)
            .setTargetRotation(Surface.ROTATION_0)
            .build()
    }
    private val contextExecutor by lazy { ContextCompat.getMainExecutor(this) }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) this.setupCamera()
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

        setClickListeners()
    }

    override fun onDestroy() {
        cameraProvider.shutdown()
        super.onDestroy()
    }

    private fun setupCamera() {
        val cameraProvider = cameraProviderFuture.get()
        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProvider
            bindPreview()
        }, contextExecutor)
    }

    private fun bindPreview() {
        val preview: Preview = Preview.Builder()
            .build()
            .apply {
                this.setSurfaceProvider(binding.preview.surfaceProvider)
            }

        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, videoCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setClickListeners() {
        binding.btnStart.setOnClickListener {
            recordStart()
        }
        binding.btnStop.setOnClickListener {
            videoCapture.stopRecording()
        }
    }


    private fun recordStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val file = CommonUtil.getOutputFile(application, "test.mp4")
            val output = VideoCapture.OutputFileOptions.Builder(file).build()
            videoCapture.startRecording(
                output,
                contextExecutor,
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        Log.d("123","저장됨 outputFileResults.savedUri: ${outputFileResults.savedUri}")
                    }

                    override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                        Log.d("123", "실패 message: $message")
                    }
                })
        }
    }


    // 권한
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) this.setupCamera()
        else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}