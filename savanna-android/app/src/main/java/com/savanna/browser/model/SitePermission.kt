package com.savanna.browser.model

data class SitePermission(
    val host: String,
    val camera: Int = 0,
    val microphone: Int = 0,
    val location: Int = 0,
    val notifications: Int = 0,
    val clipboard: Int = 0
) {
    companion object {
        const val ASK = 0
        const val ALLOW = 1
        const val DENY = 2
    }
}
