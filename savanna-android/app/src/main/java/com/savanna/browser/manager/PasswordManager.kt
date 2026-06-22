package com.savanna.browser.manager

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.savanna.browser.model.PasswordEntry
import java.io.File
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore

class PasswordManager(private val context: Context) {
    private val gson = Gson()
    private val fileName = "passwords.enc"
    private val keyAlias = "savanna_passwords_key"
    private var cache: MutableList<PasswordEntry>? = null

    private val prefs: SharedPreferences =
        context.getSharedPreferences("savanna_passwords", Context.MODE_PRIVATE)

    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", true)
        set(v) = prefs.edit().putBoolean("biometric_enabled", v).apply()

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(keyAlias)) {
            return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
        }
        val spec = KeyGenParameterSpec.Builder(
            keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        kg.init(spec)
        return kg.generateKey()
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }

    private fun decrypt(ciphertext: String): String? {
        return try {
            val combined = android.util.Base64.decode(ciphertext, android.util.Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) { null }
    }

    fun load(): List<PasswordEntry> {
        if (cache != null) return cache!!.toList()
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) { cache = mutableListOf(); return emptyList() }
            val encrypted = file.readText()
            val json = decrypt(encrypted) ?: return emptyList()
            val type = object : TypeToken<List<PasswordEntry>>() {}.type
            val items: List<PasswordEntry> = gson.fromJson(json, type)
            cache = items.toMutableList()
            return items
        } catch (_: Exception) {
            cache = mutableListOf()
            return emptyList()
        }
    }

    private fun save(items: List<PasswordEntry>) {
        try {
            val json = gson.toJson(items)
            val encrypted = encrypt(json)
            File(context.filesDir, fileName).writeText(encrypted)
        } catch (_: Exception) { }
    }

    fun add(entry: PasswordEntry) {
        val items = load().toMutableList()
        items.add(0, entry)
        cache = items
        save(items)
    }

    fun update(entry: PasswordEntry) {
        val items = load().toMutableList()
        val idx = items.indexOfFirst { it.id == entry.id }
        if (idx >= 0) {
            items[idx] = entry
            cache = items
            save(items)
        }
    }

    fun remove(id: String) {
        val items = load().toMutableList()
        items.removeAll { it.id == id }
        cache = items
        save(items)
    }
}
