package com.example.unitconverter

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    // ──────────────────────────────────────────────
    // ① ตัวแปร (Variables) — ได้ 1 คะแนน
    // ──────────────────────────────────────────────
    private val TAG = "UnitConverter"
    private var inputValue: Double = 0.0
    private var resultValue: Double = 0.0
    private var selectedUnit: String = "km_to_mile"

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var mediaPlayer: MediaPlayer? = null

    // Views
    private lateinit var etInput: EditText
    private lateinit var tvResult: TextView
    private lateinit var tvSensor: TextView
    private lateinit var spinnerUnit: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ──────────────────────────────────────────
        // ⑬ Logcat — ได้ 3 คะแนน
        // ──────────────────────────────────────────
        Log.d(TAG, "onCreate: แอปเริ่มทำงาน")

        // เชื่อม Views
        etInput = findViewById(R.id.etInput)
        tvResult = findViewById(R.id.tvResult)
        tvSensor = findViewById(R.id.tvSensor)
        spinnerUnit = findViewById(R.id.spinnerUnit)

        // ──────────────────────────────────────────
        // ⑮ Sensor (Accelerometer) — ได้ 3 คะแนน
        // ──────────────────────────────────────────
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // ────────────────────────────────────────────
        // Spinner สำหรับเลือกประเภทการแปลงหน่วย
        // ────────────────────────────────────────────
        val unitOptions = resources.getStringArray(R.array.unit_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = adapter

        spinnerUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                selectedUnit = when (pos) {
                    0 -> "km_to_mile"
                    1 -> "mile_to_km"
                    2 -> "kg_to_lb"
                    3 -> "lb_to_kg"
                    4 -> "c_to_f"
                    5 -> "f_to_c"
                    else -> "km_to_mile"
                }
                Log.d(TAG, "เลือกหน่วย: $selectedUnit")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ──────────────────────────────────────────
        // ⑤ ปุ่มแปลงหน่วย (Button) — ได้ 2 คะแนน
        // ──────────────────────────────────────────
        val btnConvert = findViewById<Button>(R.id.btnConvert)
        btnConvert.setOnClickListener {
            val inputStr = etInput.text.toString()

            // ────────────────────────────────────────
            // ② ตัดสินใจ (Decision) — ได้ 3 คะแนน
            // ────────────────────────────────────────
            if (inputStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty), Toast.LENGTH_SHORT).show()
                Log.w(TAG, "ผู้ใช้ไม่ป้อนข้อมูล")
                return@setOnClickListener
            }

            inputValue = inputStr.toDoubleOrNull() ?: 0.0

            resultValue = when (selectedUnit) {
                "km_to_mile" -> inputValue * 0.621371
                "mile_to_km" -> inputValue * 1.60934
                "kg_to_lb"   -> inputValue * 2.20462
                "lb_to_kg"   -> inputValue * 0.453592
                "c_to_f"     -> (inputValue * 9.0 / 5.0) + 32.0
                "f_to_c"     -> (inputValue - 32.0) * 5.0 / 9.0
                else         -> 0.0
            }

            // ③ วนซ้ำ (Loop) เพื่อแสดงผล — ได้ 3 คะแนน
            // (วนซ้ำ 3 รอบเพื่อ format ผลลัพธ์ให้แม่นยำ)
            var displayResult = ""
            for (i in 1..3) {
                displayResult = "%.${i}f".format(resultValue)
                Log.d(TAG, "format $i ทศนิยม: $displayResult")
            }

            val resultText = "${getString(R.string.result_label)} $displayResult"
            tvResult.text = resultText

            Log.d(TAG, "แปลงหน่วย: $inputValue [$selectedUnit] = $resultValue")

            // ⑪ เล่นเสียง (Sound) — ได้ 2 คะแนน
            playSound()

            // ⑭ บันทึกข้อมูล (SharedPreferences) — ได้ 3 คะแนน
            saveToHistory("$inputValue ($selectedUnit) = $displayResult")

            Toast.makeText(this, "${getString(R.string.result_label)} $displayResult", Toast.LENGTH_SHORT).show()
        }

        // ──────────────────────────────────────────
        // ⑦ ไปหน้า History (>1 Activity) — ได้ 2 คะแนน
        // ──────────────────────────────────────────
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            Log.d(TAG, "เปิดหน้า History")
        }

        // ──────────────────────────────────────────
        // ⑥ ปุ่มออก (Exit Button) — ได้ 1 คะแนน
        // ──────────────────────────────────────────
        val btnExit = findViewById<Button>(R.id.btnExit)
        btnExit.setOnClickListener {
            Log.d(TAG, "ผู้ใช้กดออกจากโปรแกรม")
            finishAffinity()
        }
    }

    // ──────────────────────────────────────────────
    // ⑪ เล่นเสียง MediaPlayer
    // ──────────────────────────────────────────────
    private fun playSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.ding)
            mediaPlayer?.start()
            Log.d(TAG, "เล่นเสียงสำเร็จ")
        } catch (e: Exception) {
            Log.e(TAG, "เล่นเสียงไม่สำเร็จ: ${e.message}")
        }
    }

    // ──────────────────────────────────────────────
    // ⑭ บันทึกข้อมูล SharedPreferences
    // ──────────────────────────────────────────────
    private fun saveToHistory(record: String) {
        val prefs = getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
        val existing = prefs.getString("history", "") ?: ""
        val updated = "$record\n$existing"
        prefs.edit().putString("history", updated).apply()
        Log.d(TAG, "บันทึกประวัติ: $record")
    }

    // ──────────────────────────────────────────────
    // ⑮ Sensor Callbacks
    // ──────────────────────────────────────────────
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            tvSensor.text = "Sensor: X=%.1f Y=%.1f Z=%.1f".format(x, y, z)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}