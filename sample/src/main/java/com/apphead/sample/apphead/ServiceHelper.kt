package com.apphead.sample.apphead


import android.content.Context
import android.content.Intent

internal object ServiceHelper {

    var  i:Intent? =null;

    fun start(clazz: Class<*>, context: Context) {
        runCatching {
            context.startService(Intent(context, clazz))
        }
    }

    fun startme(clazz: Class<*>, context: Context) {
        runCatching {
            context.startService(this.i)
        }
    }




    fun stop(clazz: Class<*>, context: Context) {
        runCatching {
            context.startService(Intent(context, clazz))
        }
    }
}
