package com.github.se.studentconnect.ui.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

fun DialogNotImplemented(context: Context) {
    val toast = Toast.makeText(
        context, "Not yet implemented",
        Toast.LENGTH_SHORT
    )
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}