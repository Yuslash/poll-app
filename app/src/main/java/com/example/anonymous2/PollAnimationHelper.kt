package com.example.anonymous2

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

fun handlePollButtonClick(
    context: Context,
    pollButton: LinearLayout,
    pollTitle: TextView,
    pollIcon: ImageView,
    collapsedHeight: Int = 48,
    expandedHeight: Int = 200,
    collapsedWidth: Int = 126
) {
    val currentHeight = pollButton.height
    val density = context.resources.displayMetrics.density

    // Convert dp to px
    val collapsedHeightPx = (collapsedHeight * density).toInt()
    val expandedHeightPx = (expandedHeight * density).toInt()
    val collapsedWidthPx = (collapsedWidth * density).toInt()

    // Get parent's width and account for padding
    val parentWidth = (pollButton.parent as View).width
    val parentPadding = (pollButton.parent as ViewGroup).paddingLeft + (pollButton.parent as ViewGroup).paddingRight
    val expandedWidthPx = parentWidth - parentPadding

    // Determine current state
    val isCollapsed = currentHeight == collapsedHeightPx
    val startHeight = if (isCollapsed) collapsedHeightPx else expandedHeightPx
    val endHeight = if (isCollapsed) expandedHeightPx else collapsedHeightPx
    val startWidth = if (isCollapsed) collapsedWidthPx else expandedWidthPx
    val endWidth = if (isCollapsed) expandedWidthPx else collapsedWidthPx

    // Animate height and width
    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    valueAnimator.duration = 300
    valueAnimator.addUpdateListener { animator ->
        val fraction = animator.animatedValue as Float

        // Interpolate height and width
        val animatedHeight = (startHeight + (endHeight - startHeight) * fraction).toInt()
        val animatedWidth = (startWidth + (endWidth - startWidth) * fraction).toInt()

        // Update layout parameters
        val params = pollButton.layoutParams
        params.height = animatedHeight
        params.width = animatedWidth
        pollButton.gravity = if (isCollapsed) android.view.Gravity.START else android.view.Gravity.CENTER
        pollButton.layoutParams = params
    }

    valueAnimator.start()

    // Update pollTitle dynamically
    if (isCollapsed) {
        pollTitle.text = "What you think about the system we are gonna update that soon on so which system doy think guys it will work out the time is not good right so the time will be changed here after so tell me guys which time is best"
        pollTitle.setTextAppearance(R.style.collapsedAppearence)

        // lets create a layout
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(0,20,0,0)
            }
        }

        //adding testingTExtView
        val testingTextView = TextView(context).apply {
            text = "Im here to testing the text view"
            textSize = 12f
            setPadding(16,16,16,16)
        }

        linearLayout.addView(testingTextView)

        pollButton.removeAllViews()
        pollButton.addView(linearLayout)

    } else {
        
        pollTitle.text = "Open A Poll"
        pollTitle.setTextAppearance(R.style.collapsedAppearence_Expanded)

        pollButton.removeAllViews()
    }

    // Rotate the pollIcon
    val startRotation = if (isCollapsed) 0f else 360f
    val endRotation = if (isCollapsed) 360f else 0f

    val rotationAnimator = ObjectAnimator.ofFloat(pollIcon, "rotation", startRotation, endRotation)
    rotationAnimator.duration = 300 // Match the animation duration
    rotationAnimator.start()
}