package com.kacper.compassapp.utils

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("visibility")
fun View.setVisible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}