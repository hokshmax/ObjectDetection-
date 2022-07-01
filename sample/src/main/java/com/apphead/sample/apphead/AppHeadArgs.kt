package com.apphead.sample.apphead



data class AppHeadArgs(
        var headViewArgs: HeadView.Args,
        var dismissViewArgs: DismissView.Args,
        var badgeViewArgs: BadgeView.Args? = null,
        var badgeView: BadgeView? = null
)