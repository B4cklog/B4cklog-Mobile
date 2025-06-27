package org.b4cklog.mobile.adapters

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import org.b4cklog.mobile.R
import org.b4cklog.mobile.models.Screenshot
import kotlin.math.abs
import kotlin.math.min
import java.io.File

class ScreenshotPagerAdapter(
    private val screenshots: List<Screenshot>,
    private val onSwipeToClose: () -> Unit,
    private val viewPager: androidx.viewpager2.widget.ViewPager2
) : RecyclerView.Adapter<ScreenshotPagerAdapter.ViewHolder>() {

    companion object {
        private const val DOUBLE_TAP_ZOOM = 2.5f
    }

    init {
        // Preload all screenshots for better performance
        for (screenshot in screenshots) {
            Glide.with(viewPager.context)
                .load(screenshot.url.replace("t_thumb", "t_screenshot_huge"))
                .preload()
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fullscreen_image, parent, false) as SubsamplingScaleImageView
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screenshot = screenshots[position]
        val context = holder.imageView.context
        val url = screenshot.url.replace("t_thumb", "t_screenshot_huge").let {
            if (it.startsWith("//")) "https:$it" else it
        }
        // Load image via Glide into a temporary file
        Glide.with(context)
            .downloadOnly()
            .load(url)
            .into(object : com.bumptech.glide.request.target.CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: com.bumptech.glide.request.transition.Transition<in File>?) {
                    holder.imageView.setImage(ImageSource.uri(Uri.fromFile(resource)))
                    // After loading, set fit width
                    holder.imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM)
                    holder.imageView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
                        override fun onReady() {
                            val vWidth = holder.imageView.width.toFloat()
                            val sWidth = holder.imageView.sWidth.toFloat()
                            if (vWidth > 0 && sWidth > 0) {
                                val fitWidthScale = vWidth / sWidth
                                holder.imageView.setMinScale(fitWidthScale)
                                holder.imageView.setScaleAndCenter(fitWidthScale, null)
                                holder.imageView.invalidate()
                                holder.imageView.requestLayout()
                                holder.isZoomed = false
                                // Recreate GestureDetector to reset its internal state
                                holder.gestureDetector = holder.createGestureDetector(context, holder)
                            }
                        }
                        override fun onImageLoaded() {}
                        override fun onPreviewLoadError(e: Exception?) {}
                        override fun onImageLoadError(e: Exception?) {}
                        override fun onTileLoadError(e: Exception?) {}
                        override fun onPreviewReleased() {}
                    })
                }
                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
        // Limit maximum zoom
        holder.imageView.setMaxScale(5.0f)
        holder.imageView.setDoubleTapZoomScale(DOUBLE_TAP_ZOOM)
        holder.imageView.setDoubleTapZoomDuration(200)
        holder.imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
        // Drag-to-dismiss and double-tap
        var startY = 0f
        var startX = 0f
        var isDragging = false
        var dragTranslationY = 0f
        var isAnimating = false
        var lastTapTime = 0L
        var fitWidthScale = 1f
        var isZooming: Boolean = false
        var isZoomed = false
        holder.imageView.setOnTouchListener { _, event ->
            holder.gestureDetector?.onTouchEvent(event)
            // If zoom is active or multi-touch — block ViewPager2
            if (event.pointerCount > 1 || holder.imageView.scale > holder.imageView.minScale + 0.01f) {
                viewPager.isUserInputEnabled = false
                return@setOnTouchListener false // SubsamplingScaleImageView will handle pinch/pan itself
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
                    startX = event.x
                    isDragging = false
                    dragTranslationY = 0f
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) {
                        // double tap is already handled by gestureDetector
                        lastTapTime = 0L
                        return@setOnTouchListener true
                    }
                    lastTapTime = currentTime
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.y - startY
                    val deltaX = event.x - startX
                    if (abs(deltaY) > abs(deltaX) && abs(deltaY) > 20) {
                        isDragging = true
                        dragTranslationY = deltaY
                        holder.imageView.translationY = deltaY
                        holder.imageView.alpha = 1f - min(0.7f, abs(deltaY) / holder.imageView.height)
                        return@setOnTouchListener true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    viewPager.isUserInputEnabled = true
                    if (isDragging && abs(dragTranslationY) > 100) {
                        isAnimating = true
                        holder.imageView.animate()
                            .translationY(if (dragTranslationY > 0) holder.imageView.height.toFloat() else -holder.imageView.height.toFloat())
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction {
                                holder.imageView.translationY = 0f
                                holder.imageView.alpha = 1f
                                isAnimating = false
                                onSwipeToClose()
                            }
                            .start()
                        return@setOnTouchListener true
                    } else if (isDragging) {
                        isAnimating = true
                        holder.imageView.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .withEndAction {
                                isAnimating = false
                            }
                            .start()
                        return@setOnTouchListener true
                    }
                    isDragging = false
                }
            }
            false
        }
    }

    override fun getItemCount(): Int = screenshots.size

    class ViewHolder(val imageView: SubsamplingScaleImageView) : RecyclerView.ViewHolder(imageView) {
        var isZoomed: Boolean = false
        var gestureDetector: GestureDetector? = null
        var isZooming: Boolean = false
        fun createGestureDetector(context: android.content.Context, holder: ViewHolder): GestureDetector {
            return GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (holder.isZooming) return true
                    holder.isZooming = true
                    if (!holder.isZoomed) {
                        holder.imageView.animateScaleAndCenter(DOUBLE_TAP_ZOOM, null)
                        Handler(Looper.getMainLooper()).postDelayed({
                            holder.isZooming = false
                            holder.isZoomed = true
                        }, 200)
                    } else {
                        val minScale = holder.imageView.minScale
                        holder.imageView.setScaleAndCenter(minScale, null)
                        holder.imageView.invalidate()
                        holder.imageView.requestLayout()
                        holder.isZoomed = false
                        holder.isZooming = false
                        // Recreate GestureDetector to reset its internal state
                        holder.gestureDetector = holder.createGestureDetector(context, holder)
                    }
                    return true
                }
            })
        }
    }
} 