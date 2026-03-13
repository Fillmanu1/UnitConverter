package com.example.unitconverter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    private val TAG = "HistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        Log.d(TAG, "เปิดหน้าประวัติการแปลงหน่วย")

        val container = findViewById<LinearLayout>(R.id.historyContainer)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnClear = findViewById<Button>(R.id.btnClear)

        loadHistory(container)

        btnBack.setOnClickListener {
            finish()
        }

        btnClear.setOnClickListener {
            clearHistory()
            container.removeAllViews()
            val tv = TextView(this)
            tv.text = getString(R.string.no_history)
            container.addView(tv)
            Log.d(TAG, "ล้างประวัติแล้ว")
        }
    }

    private fun loadHistory(container: LinearLayout) {
        val prefs = getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
        val history = prefs.getString("history", "") ?: ""

        if (history.isEmpty()) {
            val tv = TextView(this)
            tv.text = getString(R.string.no_history)
            container.addView(tv)
            return
        }

        val lines = history.trim().split("\n")

        // ③ การวนซ้ำ (for loop) — แสดงประวัติทุกรายการ
        for (i in lines.indices) {
            if (lines[i].isNotEmpty()) {
                val tv = TextView(this)
                tv.text = "${i + 1}. ${lines[i]}"
                tv.textSize = 16f
                tv.setPadding(8, 8, 8, 8)
                container.addView(tv)
                Log.d(TAG, "แสดงประวัติ #${i + 1}: ${lines[i]}")
            }
        }
    }

    private fun clearHistory() {
        val prefs = getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("history").apply()
    }
}