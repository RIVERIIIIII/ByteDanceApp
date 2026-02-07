package com.example.firsttry.ui.component

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

@Composable
fun GlideImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 0
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            var request = Glide.with(imageView.context)
                .load(imageUrl)
            
            if (cornerRadius > 0) {
                // Convert dp to px if needed, but here assuming cornerRadius is raw value or handled by caller
                // Usually RoundedCorners takes pixels.
                // For simplicity, let's assume the caller passes a reasonable pixel value or we hardcode a transformation
                request = request.transform(CenterCrop(), RoundedCorners(cornerRadius))
            } else {
                request = request.transform(CenterCrop())
            }
            
            request.into(imageView)
        },
        modifier = modifier
    )
}
