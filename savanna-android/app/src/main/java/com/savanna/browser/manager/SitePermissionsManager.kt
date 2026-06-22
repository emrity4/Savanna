package com.savanna.browser.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.savanna.browser.model.SitePermission
import java.io.File

class SitePermissionsManager(private val context: Context) {
    private val gson = Gson()
    private val fileName = "site_permissions.json"
    private var cache: MutableMap<String, SitePermission>? = null

    private fun load(): MutableMap<String, SitePermission> {
        if (cache != null) return cache!!
        try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) { cache = mutableMapOf(); return cache!! }
            val type = object : TypeToken<Map<String, SitePermission>>() {}.type
            val map: Map<String, SitePermission> = gson.fromJson(file.readText(), type) ?: emptyMap()
            cache = map.toMutableMap()
        } catch (_: Exception) { cache = mutableMapOf() }
        return cache!!
    }

    private fun save() {
        try {
            File(context.filesDir, fileName).writeText(gson.toJson(cache ?: return))
        } catch (_: Exception) { }
    }

    fun get(host: String): SitePermission = load()[host] ?: SitePermission(host = host)

    fun set(host: String, update: (SitePermission) -> SitePermission) {
        val map = load()
        map[host] = update(map[host] ?: SitePermission(host = host))
        cache = map
        save()
    }

    fun getAll(): List<SitePermission> = load().values.sortedBy { it.host }

    fun remove(host: String) {
        load().remove(host)
        save()
    }

    fun clear() {
        cache = mutableMapOf()
        save()
    }
}
