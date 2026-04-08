package com.savanna.browser.model

data class Bookmark(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
