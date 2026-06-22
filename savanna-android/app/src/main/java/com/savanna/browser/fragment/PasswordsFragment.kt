package com.savanna.browser.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.savanna.browser.MainActivity
import com.savanna.browser.R
import com.savanna.browser.adapter.PasswordAdapter
import com.savanna.browser.model.PasswordEntry
import java.util.concurrent.Executors

class PasswordsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: PasswordAdapter
    private var authenticated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_passwords, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        view.setBackgroundColor(activity.themeManager.activePreset.bgColor)

        recyclerView = view.findViewById(R.id.passwords_recycler)
        emptyText = view.findViewById(R.id.empty_text)
        val btnAdd = view.findViewById<ImageView>(R.id.btn_add_password)
        val btnClose = view.findViewById<ImageView>(R.id.btn_close_passwords)

        adapter = PasswordAdapter(
            items = emptyList(),
            onToggle = {},
            onEdit = { entry -> showEditDialog(activity, entry) },
            onDelete = { entry ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Password")
                    .setMessage("Remove saved password for ${entry.url}?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        activity.passwordManager.remove(entry.id)
                        refresh()
                    }.show()
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnClose.setOnClickListener { activity.closeOverlay() }
        btnAdd.setOnClickListener { showEditDialog(activity, null) }

        authenticate(activity)
    }

    private fun authenticate(activity: MainActivity) {
        if (!activity.passwordManager.biometricEnabled) {
            authenticated = true
            refresh()
            return
        }
        val executor = Executors.newSingleThreadExecutor()
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                requireActivity().runOnUiThread {
                    authenticated = true
                    refresh()
                }
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                requireActivity().runOnUiThread { activity.closeOverlay() }
            }
            override fun onAuthenticationFailed() {}
        }
        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Passwords")
            .setSubtitle("Authenticate to view saved passwords")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
        prompt.authenticate(info)
    }

    private fun refresh() {
        if (!authenticated) return
        val a = activity as? MainActivity ?: return
        val items = a.passwordManager.load()
        adapter.updateItems(items)
        emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showEditDialog(activity: MainActivity, entry: PasswordEntry?) {
        val isNew = entry == null
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 16)
        }
        val urlInput = TextInputEditText(requireContext()).apply {
            hint = "Website URL"
            setText(entry?.url ?: "https://")
            setTextColor(activity.themeManager.activePreset.bgColor.inv())
            setSingleLine()
        }
        val userInput = TextInputEditText(requireContext()).apply {
            hint = "Username"
            setText(entry?.username ?: "")
            setSingleLine()
        }
        val passInput = TextInputEditText(requireContext()).apply {
            hint = "Password"
            setText(entry?.password ?: "")
            setSingleLine()
        }
        layout.addView(urlInput)
        layout.addView(Space(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 12)
        })
        layout.addView(userInput)
        layout.addView(Space(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 12)
        })
        layout.addView(passInput)

        AlertDialog.Builder(requireContext())
            .setTitle(if (isNew) "Add Password" else "Edit Password")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (isNew) "Save" else "Update") { _, _ ->
                val url = urlInput.text?.toString()?.trim() ?: return@setPositiveButton
                val username = userInput.text?.toString()?.trim() ?: ""
                val password = passInput.text?.toString() ?: ""
                if (url.isBlank()) return@setPositiveButton
                if (isNew) {
                    activity.passwordManager.add(PasswordEntry(url = url, username = username, password = password))
                } else {
                    activity.passwordManager.update(entry!!.copy(url = url, username = username, password = password))
                }
                refresh()
            }.show()
    }
}
