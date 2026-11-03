package com.project.tickr.ui.common

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCameraLauncher(
    onPhotoTaken: (uri: String?) -> Unit,
): () -> Unit {
    // TODO(user): UIImagePickerController (sourceType = camera) + NSCameraUsageDescription di Info.plist.
    return {}
}
