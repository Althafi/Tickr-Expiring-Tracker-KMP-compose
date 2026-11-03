package com.project.tickr.data.local.image

import kotlin.math.max
import kotlin.math.roundToInt

internal object ImageScaling {
    const val MAX_SIDE = 1280
    const val JPEG_QUALITY = 80
    const val DIRECTORY = "product_images"

    /** Ukuran akhir dengan sisi terpanjang <= [maxSide], rasio dipertahankan. Tidak pernah memperbesar. */
    fun targetSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Pair<Int, Int> {
        if (width <= 0 || height <= 0) return width to height
        val longest = max(width, height)
        if (longest <= maxSide) return width to height
        val scale = maxSide.toDouble() / longest
        return max(1, (width * scale).roundToInt()) to max(1, (height * scale).roundToInt())
    }

    /** `inSampleSize` terbesar (pangkat 2) yang masih menyisakan sisi terpanjang >= [maxSide]. */
    fun sampleSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Int {
        if (width <= 0 || height <= 0) return 1
        var sample = 1
        while (max(width, height) / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    /** True bila [path] adalah path relatif yang valid di dalam [DIRECTORY] (tanpa `..`). */
    fun isManagedPath(path: String): Boolean =
        path.startsWith("$DIRECTORY/") && ".." !in path.split('/')
}
