package github.sun5066.record.model

import android.hardware.camera2.CameraCharacteristics

enum class LensFacing(val facing: Int) {
    FRONT(CameraCharacteristics.LENS_FACING_FRONT), BACK(CameraCharacteristics.LENS_FACING_BACK);

    companion object {
        operator fun invoke(facing: Int): LensFacing = values().first { it.facing == facing }
    }
}

data class Resolution(
    val width: Int,
    val height: Int
)
