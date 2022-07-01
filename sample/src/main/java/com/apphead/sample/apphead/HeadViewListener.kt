package com.apphead.sample.apphead

internal interface HeadViewListener {
    fun onDismiss(view: HeadView)
    fun onClick(view: HeadView)
    fun onLongClick(view: HeadView)
}