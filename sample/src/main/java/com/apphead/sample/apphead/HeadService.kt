package com.apphead.sample.apphead

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager

class HeadService : Service() {
    private var headView: HeadView? = null

    private lateinit var windowManager: WindowManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        if (Head.args == null) {
            stopSelf()
            return
        }
        windowManager = WindowManagerHelper.manager(this)
        setupHeadView()
    }

    private fun setupHeadView() {
        headView = HeadView.setup(this)

        headView!!.listener = object: HeadViewListener {

            override fun onDismiss(view: HeadView) {
                Head.headViewArgs.onDismiss?.invoke(view)
                stopSelf()
            }

            override fun onClick(view: HeadView) {
                android.widget.Toast.makeText(applicationContext,"sasa",android.widget.Toast.LENGTH_SHORT).show();
                Head.headViewArgs.onClick?.invoke(view)
                if (Head.headViewArgs.dismissOnClick) stopSelf()
            }

            override fun onLongClick(view: HeadView) {
                Head.headViewArgs.onLongClick?.invoke(view)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        headView?.run {
            cleanup()
            windowManager.removeView(this)
        }
        // clean up singleton to avoid memory leaks
        Head.args = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
