package com.savanna.browser.model

data class PasswordEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val url: String,
    val username: String,
    val password: String,
    val createdAt: Long = System.currentTimeMillis()
)
