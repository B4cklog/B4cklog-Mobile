package org.b4cklog.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.b4cklog.mobile.R
import org.b4cklog.mobile.models.Screenshot

class ScreenshotAdapter(
    private val screenshots: List<Screenshot>,
    private val onScreenshotClick: (Screenshot) -> Unit
) : RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_screenshot, parent, false)
        return ScreenshotViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScreenshotViewHolder, position: Int) {
        holder.bind(screenshots[position])
    }

    override fun getItemCount(): Int = screenshots.size

    inner class ScreenshotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.screenshot_image)

        fun bind(screenshot: Screenshot) {
            Glide.with(itemView.context)
                .load(screenshot.url.replace("t_thumb", "t_screenshot_big"))
                .placeholder(R.drawable.cover_placeholder)
                .into(imageView)
            itemView.setOnClickListener { onScreenshotClick(screenshot) }
        }
    }
} 