package com.apphead.sample.apphead

import android.app.Service
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.FragmentActivity

internal object SystemOverlayHelper {

    fun canDrawOverlays(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    fun checkDrawOverlayPermission(activity: FragmentActivity): Boolean {
        if (canDrawOverlays(activity)) return true

        PermissionFrag.requestForPermission(activity)
        return false
    }

}
