package com.ridagames.pkgconverter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {
    private val purple = Color.rgb(137, 76, 255)
    private val bg = Color.rgb(12, 13, 22)
    private val panel = Color.rgb(24, 25, 39)
    private val muted = Color.rgb(160, 163, 183)
    private var selectedUri: Uri? = null
    private lateinit var fileName: TextView
    private lateinit var fileInfo: TextView
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var logs: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildScreen()
    }

    private fun buildScreen() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(28))
        }
        scroll.addView(root)
        setContentView(scroll)

        val brand = TextView(this).apply {
            text = "RIDA  /  PKG CONVERTER"
            textSize = 12f
            letterSpacing = .16f
            setTextColor(Color.rgb(190, 160, 255))
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(brand)

        val title = TextView(this).apply {
            text = "Convertisseur\nPKG PlayStation"
            textSize = 29f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(14), 0, dp(5))
        }
        root.addView(title)
        root.addView(label("Gestionnaire de fichiers • PS4", muted, 13f))

        val engineCard = card(root)
        val engineRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        engineCard.addView(engineRow)
        engineRow.addView(dot())
        val engineText = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        engineText.addView(label("MOTEUR DE TRAITEMENT", muted, 10f))
        engineText.addView(label("Connecteur externe en attente", Color.WHITE, 14f, true))
        engineRow.addView(engineText, LinearLayout.LayoutParams(0, -2, 1f))
        engineRow.addView(label("NON CONNECTÉ", Color.rgb(255, 190, 95), 10f, true))

        val fileCard = card(root)
        fileCard.addView(sectionTitle("FICHIER SOURCE"))
        fileName = label("Aucun fichier sélectionné", Color.WHITE, 15f, true)
        fileName.setPadding(0, dp(12), 0, dp(4))
        fileCard.addView(fileName)
        fileInfo = label("Sélectionne un fichier .pkg depuis ton appareil", muted, 12f)
        fileCard.addView(fileInfo)
        val choose = actionButton("＋  CHOISIR UN FICHIER PKG", true)
        fileCard.addView(choose)
        choose.setOnClickListener { pickPkg() }

        val processCard = card(root)
        processCard.addView(sectionTitle("TRAITEMENT"))
        status = label("En attente d'un fichier", Color.WHITE, 14f, true)
        status.setPadding(0, dp(12), 0, dp(10))
        processCard.addView(status)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            progressTintList = android.content.res.ColorStateList.valueOf(purple)
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(54, 53, 70))
        }
        processCard.addView(progress, LinearLayout.LayoutParams(-1, dp(7)))
        startButton = actionButton("DÉMARRER", false)
        startButton.isEnabled = false
        startButton.alpha = .45f
        processCard.addView(startButton)
        startButton.setOnClickListener {
            status.text = "Connecteur externe requis"
            appendLog("Traitement non démarré : aucun moteur externe connecté.")
        }

        val logCard = card(root)
        logCard.addView(sectionTitle("JOURNAL D'ACTIVITÉ"))
        logs = label("• Application initialisée\n• En attente d'une sélection de fichier", muted, 12f)
        logs.setPadding(0, dp(12), 0, dp(4))
        logCard.addView(logs)

        root.addView(label("RIDA GAMES  •  VERSION 0.1.0", muted, 10f).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, 0)
        })
    }

    private fun pickPkg() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream", "application/vnd.playstation.package"))
        }
        startActivityForResult(intent, 41)
    }

    @Deprecated("Legacy result callback retained for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            selectedUri = uri
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Fichier PKG"
            fileName.text = name
            fileInfo.text = "Fichier sélectionné • accès en lecture accordé"
            status.text = "Fichier prêt — moteur non connecté"
            startButton.isEnabled = false
            startButton.alpha = .45f
            appendLog("Fichier sélectionné : $name")
        }
    }

    private fun appendLog(line: String) {
        logs.text = logs.text.toString() + "\n• " + line
    }

    private fun card(parent: LinearLayout): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(panel, 18)
        }
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.topMargin = dp(18)
        parent.addView(box, lp)
        return box
    }

    private fun sectionTitle(text: String) = label(text, Color.rgb(190, 160, 255), 11f, true)
    private fun label(text: String, color: Int, size: Float, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }

    private fun actionButton(text: String, primary: Boolean): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            background = rounded(if (primary) purple else Color.rgb(53, 43, 82), 13)
            setPadding(dp(10), dp(7), dp(10), dp(7))
            val lp = LinearLayout.LayoutParams(-1, dp(50))
            lp.topMargin = dp(16)
            layoutParams = lp
        }

    private fun dot(): View = View(this).apply {
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.rgb(255, 190, 95))
        }
        layoutParams = LinearLayout.LayoutParams(dp(9), dp(9))
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
