package com.kacper.compassapp.utils

import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

fun ImageView.setCompassAnimation(fromValue: Float, toValue: Float) {
    val animation = RotateAnimation(
        fromValue, toValue, Animation.RELATIVE_TO_SELF,
        0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    )

    animation.duration = COMPASS_ANIM_DURATION
    animation.repeatCount = 0
    animation.fillAfter = true

    startAnimation(animation)
}