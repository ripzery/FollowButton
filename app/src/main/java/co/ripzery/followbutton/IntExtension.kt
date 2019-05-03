package co.ripzery.followbutton

import android.content.res.Resources

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.spTopx: Float
    get() = this * Resources.getSystem().displayMetrics.scaledDensity
