package com.devries48.elitecommander.adapters

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ViewPagerTransformer : ViewPager2.PageTransformer {
    private  val mMinScale: Float = 0.8f
    private val mMinAlpha: Float = 0.9f

    override fun transformPage(page: View, position: Float) {
        val pageWidth = page.width
        val pageHeight = page.height

        when {
            position < -1 -> {
                page.alpha = 0f
            }
            position <= 1 -> {
                val scaleFactor = mMinScale.coerceAtLeast(1 - abs(position))
                val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                val horizontalMargin = pageWidth * (1 - scaleFactor) / 2

                if (position < 0) {
                    page.translationX = horizontalMargin - verticalMargin / 2
                } else {
                    page.translationX = -horizontalMargin + verticalMargin / 2
                }

                page.scaleX = scaleFactor
                page.scaleY = scaleFactor

                page.alpha = mMinAlpha +
                        (scaleFactor - mMinScale) /
                        (1 - mMinScale) * (1 - mMinAlpha)
            }
            else -> {
                page.alpha = 0f
            }
        }
    }

}