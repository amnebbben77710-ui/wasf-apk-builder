package com.wasf.myapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var dbHelper: DbHelper
    private lateinit var prefs: SharedPreferences
    private var vibrator: Vibrator? = null
    private var toneGen: ToneGenerator? = null

    private val colorPrimary = Color.parseColor("#0F766E")
    private val colorPrimaryDark = Color.parseColor("#0D5C56")
    private val colorAccent = Color.parseColor("#059669")
    private val colorBg = Color.parseColor("#F0FDF4")
    private val colorCard = Color.WHITE
    private val colorTextPrimary = Color.parseColor("#0F172A")
    private val colorTextSecondary = Color.parseColor("#475569")
    private val colorSoftGreen = Color.parseColor("#DCFCE7")

    private var currentTab = 0
    private var selectedDhikrId: Long = 1L
    private var isZoomed = false

    private lateinit var contentContainer: FrameLayout
    private lateinit var tabTasbihBtn: TextView
    private lateinit var tabAdhkarBtn: TextView
    private lateinit var tabSettingsBtn: TextView

    // Tasbih views
    private var tasbihTitleText: TextView? = null
    private var tasbihCountText: TextView? = null
    private var tasbihTargetText: TextView? = null
    private var tasbihTotalText: TextView? = null
    private var tasbihProgress: ProgressBar? = null
    private var tasbihCircleBtn: LinearLayout? = null
    private var zoomBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = colorPrimaryDark

        dbHelper = DbHelper(this)
        prefs = getSharedPreferences("muslim_prefs", Context.MODE_PRIVATE)
        selectedDhikrId = prefs.getLong("selected_dhikr_id", 1L)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_SYSTEM, 40)
        } catch (_: Exception) {
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(colorBg)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        contentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        root.addView(contentContainer)
        root.addView(createBottomNav())

        setContentView(root)
        switchTab(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGen?.release()
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun roundedBg(color: Int, radiusDp: Int, strokeColor: Int? = null, strokeDp: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
            if (strokeColor != null && strokeDp > 0) {
                setStroke(dp(strokeDp), strokeColor)
            }
        }
    }

    private fun createBottomNav(): LinearLayout {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            elevation = dp(8).toFloat()
            setPadding(dp(8), dp(8), dp(8), dp(10))
        }

        tabTasbihBtn = createNavItem("التسبيح") { switchTab(0) }
        tabAdhkarBtn = createNavItem("الأذكار") { switchTab(1) }
        tabSettingsBtn = createNavItem("الإعدادات") { switchTab(2) }

        nav.addView(tabTasbihBtn)
        nav.addView(tabAdhkarBtn)
        nav.addView(tabSettingsBtn)
        return nav
    }

    private fun createNavItem(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { onClick() }
        }
    }

    private fun switchTab(tab: Int) {
        currentTab = tab
        val tabs = listOf(tabTasbihBtn, tabAdhkarBtn, tabSettingsBtn)
        tabs.forEachIndexed { index, tv ->
            if (index == tab) {
                tv.setTextColor(Color.WHITE)
                tv.background = roundedBg(colorPrimary, 12)
            } else {
                tv.setTextColor(colorTextSecondary)
                tv.background = null
            }
        }
        contentContainer.removeAllViews()
        when (tab) {
            0 -> contentContainer.addView(buildTasbihScreen())
            1 -> contentContainer.addView(buildAdhkarScreen())
            2 -> contentContainer.addView(buildSettingsScreen())
        }
    }

    private fun buildTasbihScreen(): View {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(colorPrimary, colorAccent)
            ).apply {
                cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, dp(28).toFloat(), dp(28).toFloat(), dp(28).toFloat(), dp(28).toFloat())
            }
            setPadding(dp(20), dp(22), dp(20), dp(22))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val appTitle = TextView(this).apply {
            text = "مسلم - عداد التسبيح"
            setTextColor(Color.WHITE)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        tasbihTotalText = TextView(this).apply {
            setTextColor(colorSoftGreen)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        }
        header.addView(appTitle)
        header.addView(tasbihTotalText)
        container.addView(header)

        // Dhikr Card
        val dhikrCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBg(colorCard, 20, colorSoftGreen, 1)
            elevation = dp(4).toFloat()
            setPadding(dp(18), dp(16), dp(18), dp(16))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(16), dp(16), dp(16), dp(12))
            }
            setOnClickListener { switchTab(1) }
        }

        tasbihTitleText = TextView(this).apply {
            setTextColor(colorPrimary)
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        tasbihTargetText = TextView(this).apply {
            setTextColor(colorTextSecondary)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(8))
        }
        tasbihProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(8))
            progressDrawable.setTint(colorAccent)
        }

        dhikrCard.addView(tasbihTitleText)
        dhikrCard.addView(tasbihTargetText)
        dhikrCard.addView(tasbihProgress)
        container.addView(dhikrCard)

        // Main Circular Tasbih Button
        val circleWrapper = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        tasbihCircleBtn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = dp(10).toFloat()
            setOnClickListener { onTasbihTap() }
        }

        tasbihCountText = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        val tapHint = TextView(this).apply {
            text = "اضغط للتسبيح"
            setTextColor(colorSoftGreen)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        }

        tasbihCircleBtn?.addView(tasbihCountText)
        tasbihCircleBtn?.addView(tapHint)
        circleWrapper.addView(tasbihCircleBtn)
        container.addView(circleWrapper)

        // Action Buttons: Zoom & Reset
        val actionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(8), dp(16), dp(18))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        zoomBtn = Button(this).apply {
            setTextColor(colorPrimary)
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            background = roundedBg(colorSoftGreen, 14, colorPrimary, 1)
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(8) }
            setOnClickListener {
                isZoomed = !isZoomed
                applyZoomState()
            }
        }

        val resetBtn = Button(this).apply {
            text = "تصفير العداد"
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            background = roundedBg(Color.parseColor("#DC2626"), 14)
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(8) }
            setOnClickListener { confirmResetCurrent() }
        }

        actionsRow.addView(zoomBtn)
        actionsRow.addView(resetBtn)
        container.addView(actionsRow)

        scroll.addView(container)
        applyZoomState()
        refreshTasbihData()
        return scroll
    }

    private fun applyZoomState() {
        val sizeDp = if (isZoomed) 260 else 200
        val sizePx = dp(sizeDp)
        tasbihCircleBtn?.layoutParams = LinearLayout.LayoutParams(sizePx, sizePx)
        tasbihCircleBtn?.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(colorAccent, colorPrimary)).apply {
            shape = GradientDrawable.OVAL
            setStroke(dp(6), colorSoftGreen)
        }
        tasbihCountText?.textSize = if (isZoomed) 64f else 48f
        zoomBtn?.text = if (isZoomed) "تصغير العداد" else "تكبير العداد"
    }

    private fun refreshTasbihData() {
        val dhikr = dbHelper.getDhikrById(selectedDhikrId) ?: dbHelper.getAllAdhkar().firstOrNull()
        if (dhikr != null) {
            selectedDhikrId = dhikr.id
            tasbihTitleText?.text = dhikr.title
            tasbihCountText?.text = dhikr.count.toString()
            tasbihTargetText?.text = "الهدف: ${dhikr.target}  •  الإجمالي للذكر: ${dhikr.totalCount}"
            tasbihProgress?.max = if (dhikr.target > 0) dhikr.target else 33
            tasbihProgress?.progress = dhikr.count.coerceAtMost(tasbihProgress?.max ?: 33)
        }
        tasbihTotalText?.text = "إجمالي التسبيحات: ${dbHelper.getGrandTotal()}"
    }

    private fun onTasbihTap() {
        dbHelper.incrementDhikr(selectedDhikrId)
        triggerFeedback()
        tasbihCircleBtn?.animate()?.scaleX(0.94f)?.scaleY(0.94f)?.setDuration(60)?.withEndAction {
            tasbihCircleBtn?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(80)?.start()
        }?.start()
        refreshTasbihData()
    }

    private fun triggerFeedback() {
        if (prefs.getBoolean("vibrate", true)) {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(25)
                }
            }
        }
        if (prefs.getBoolean("sound", false)) {
            try {
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
            } catch (_: Exception) {
            }
        }
    }

    private fun confirmResetCurrent() {
        AlertDialog.Builder(this)
            .setTitle("تصفير العداد")
            .setMessage("هل تريد تصفير عداد الذكر الحالي؟")
            .setPositiveButton("نعم") { _, _ ->
                dbHelper.resetDhikrCount(selectedDhikrId)
                refreshTasbihData()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun buildAdhkarScreen(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(12))
        }
        val title = TextView(this).apply {
            text = "قائمة الأذكار"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorPrimary)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val addBtn = Button(this).apply {
            text = "+ ذكر جديد"
            setTextColor(Color.WHITE)
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedBg(colorAccent, 12)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setOnClickListener { showAddDhikrDialog() }
        }
        topRow.addView(title)
        topRow.addView(addBtn)
        root.addView(topRow)

        val searchBox = EditText(this).apply {
            hint = "ابحث عن ذكر..."
            background = roundedBg(Color.WHITE, 12, colorSoftGreen, 1)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            }
        }
        root.addView(searchBox)

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scroll = ScrollView(this).apply {
            addView(listContainer)
        }
        root.addView(scroll)

        fun populate(query: String) {
            listContainer.removeAllViews()
            val items = dbHelper.getAllAdhkar(query)
            for (item in items) {
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    background = roundedBg(
                        if (item.id == selectedDhikrId) colorSoftGreen else Color.WHITE,
                        16,
                        if (item.id == selectedDhikrId) colorPrimary else colorSoftGreen,
                        1
                    )
                    setPadding(dp(14), dp(14), dp(14), dp(14))
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = dp(10)
                    }
                    setOnClickListener {
                        selectedDhikrId = item.id
                        prefs.edit().putLong("selected_dhikr_id", item.id).apply()
                        switchTab(0)
                    }
                }

                val textCol = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tTitle = TextView(this).apply {
                    text = item.title
                    textSize = 17f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(colorTextPrimary)
                }
                val tSub = TextView(this).apply {
                    text = "العداد: ${item.count} / ${item.target}  •  الإجمالي: ${item.totalCount}"
                    textSize = 13f
                    setTextColor(colorTextSecondary)
                    setPadding(0, dp(4), 0, 0)
                }
                textCol.addView(tTitle)
                textCol.addView(tSub)

                val badge = TextView(this).apply {
                    text = if (item.id == selectedDhikrId) "محدد" else "اختيار"
                    setTextColor(if (item.id == selectedDhikrId) Color.WHITE else colorPrimary)
                    textSize = 12f
                    setTypeface(null, Typeface.BOLD)
                    background = roundedBg(if (item.id == selectedDhikrId) colorPrimary else colorSoftGreen, 10)
                    setPadding(dp(10), dp(6), dp(10), dp(6))
                }

                card.addView(textCol)
                card.addView(badge)
                listContainer.addView(card)
            }
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                populate(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        populate("")
        return root
    }

    private fun showAddDhikrDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(8))
        }
        val nameInput = EditText(this).apply { hint = "نص الذكر (مثال: سبحان الله وبحمده)" }
        val targetInput = EditText(this).apply {
            hint = "الهدف (مثال: 33 أو 100)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText("33")
        }
        layout.addView(nameInput)
        layout.addView(targetInput)

        AlertDialog.Builder(this)
            .setTitle("إضافة ذكر جديد")
            .setView(layout)
            .setPositiveButton("إضافة") { _, _ ->
                val title = nameInput.text.toString().trim()
                val target = targetInput.text.toString().toIntOrNull() ?: 33
                if (title.isNotEmpty()) {
                    val newId = dbHelper.addDhikr(title, if (target > 0) target else 33)
                    selectedDhikrId = newId
                    prefs.edit().putLong("selected_dhikr_id", newId).apply()
                    switchTab(1)
                } else {
                    Toast.makeText(this, "الرجاء إدخال نص الذكر", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun buildSettingsScreen(): View {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(20), dp(16), dp(20))
        }

        val title = TextView(this).apply {
            text = "الإعدادات والإحصائيات"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(colorPrimary)
            setPadding(0, 0, 0, dp(16))
        }
        root.addView(title)

        // Vibration Switch
        root.addView(createSettingSwitch("الاهتزاز عند التسبيح", prefs.getBoolean("vibrate", true)) { checked ->
            prefs.edit().putBoolean("vibrate", checked).apply()
        })

        // Sound Switch
        root.addView(createSettingSwitch("صوت النقر عند التسبيح", prefs.getBoolean("sound", false)) { checked ->
            prefs.edit().putBoolean("sound", checked).apply()
        })

        // Reset all button
        val resetAllBtn = Button(this).apply {
            text = "تصفير جميع الإحصائيات"
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
            background = roundedBg(Color.parseColor("#DC2626"), 14)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)).apply {
                topMargin = dp(20)
            }
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("تصفير الكل")
                    .setMessage("هل أنت متأكد من تصفير جميع العدادات والإحصائيات؟")
                    .setPositiveButton("نعم") { _, _ ->
                        dbHelper.resetAllTotals()
                        Toast.makeText(this@MainActivity, "تم تصفير جميع الإحصائيات", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("إلغاء", null)
                    .show()
            }
        }
        root.addView(resetAllBtn)

        scroll.addView(root)
        return scroll
    }

    private fun createSettingSwitch(label: String, initial: Boolean, onChanged: (Boolean) -> Unit): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedBg(Color.WHITE, 14, colorSoftGreen, 1)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            }
        }
        val tv = TextView(this).apply {
            text = label
            textSize = 16f
            setTextColor(colorTextPrimary)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val sw = Switch(this).apply {
            isChecked = initial
            setOnCheckedChangeListener { _, isChecked -> onChanged(isChecked) }
        }
        card.addView(tv)
        card.addView(sw)
        return card
    }
}
