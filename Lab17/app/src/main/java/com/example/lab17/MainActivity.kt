package com.example.lab17

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var btnQuery: Button

    // 1. 提升 OkHttpClient 為類別成員變數
    // OkHttpClient 建立成本高，應該重複使用以共用連線池 (Connection Pool)
    private val client = OkHttpClient()

    // Gson 也可以重複使用
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        btnQuery = findViewById(R.id.btnQuery)

        btnQuery.setOnClickListener {
            // 避免使用者快速重複點擊
            btnQuery.isEnabled = false
            sendRequest()
        }
    }

    private fun sendRequest() {
        val url = "https://api.italkutalk.com/api/air"

        val request = Request.Builder()
            .url(url)
            .build()

        // 使用已建立好的 client 實體
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()

                // 增加安全性檢查：確保回傳內容不為空
                if (json != null) {
                    try {
                        val myObject = gson.fromJson(json, MyObject::class.java)
                        // 切換回主執行緒顯示 UI
                        showDialog(myObject)
                    } catch (e: Exception) {
                        showError("解析失敗：${e.message}")
                    }
                } else {
                    showError("伺服器回傳空資料")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                showError("連線失敗：${e.message}")
            }
        })
    }

    private fun showDialog(myObject: MyObject) {
        // 2. 使用 Kotlin 的 map 函數簡化程式碼
        // 將 Record 物件列表直接轉換為 String 陣列
        val items = myObject.result.records.map { data ->
            "地區：${data.siteName}, 狀態：${data.status}"
        }.toTypedArray()

        runOnUiThread {
            btnQuery.isEnabled = true
            AlertDialog.Builder(this@MainActivity)
                .setTitle("臺北市空氣品質")
                .setItems(items, null)
                .show()
        }
    }

    // 抽出錯誤處理邏輯，避免重複程式碼
    private fun showError(message: String) {
        runOnUiThread {
            btnQuery.isEnabled = true
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}