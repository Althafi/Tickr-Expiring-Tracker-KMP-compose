package com.project.tickr.data.local

import com.project.tickr.data.local.image.ImageScaling
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageScalingTest {

    @Test fun small_image_is_not_enlarged() =
        assertEquals(800 to 600, ImageScaling.targetSize(800, 600))

    @Test fun exact_limit_is_unchanged() =
        assertEquals(1280 to 720, ImageScaling.targetSize(1280, 720))

    @Test fun landscape_scaled_by_width() =
        assertEquals(1280 to 960, ImageScaling.targetSize(4000, 3000))

    @Test fun portrait_scaled_by_height() =
        assertEquals(960 to 1280, ImageScaling.targetSize(3000, 4000))

    @Test fun extreme_aspect_ratio_keeps_min_one_pixel() =
        assertEquals(1280 to 1, ImageScaling.targetSize(100_000, 10))

    @Test fun invalid_size_is_returned_as_is() =
        assertEquals(0 to 0, ImageScaling.targetSize(0, 0))

    @Test fun sampleSize_stays_one_when_not_much_larger() {
        assertEquals(1, ImageScaling.sampleSize(1280, 960))
        assertEquals(1, ImageScaling.sampleSize(2559, 1000))
    }

    @Test fun sampleSize_halves_while_longest_side_stays_above_target() {
        assertEquals(2, ImageScaling.sampleSize(4000, 3000))
        assertEquals(4, ImageScaling.sampleSize(8000, 6000))
    }

    @Test fun managed_path_accepts_only_relative_paths_inside_directory() {
        assertTrue(ImageScaling.isManagedPath("product_images/a.jpg"))
        assertFalse(ImageScaling.isManagedPath("/data/user/0/app/files/a.jpg"))
        assertFalse(ImageScaling.isManagedPath("https://x/y.jpg"))
        assertFalse(ImageScaling.isManagedPath("product_images/../../etc/passwd"))
        assertFalse(ImageScaling.isManagedPath("other/a.jpg"))
    }
}
