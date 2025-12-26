package com.example.lab16_2

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // 1. 集中管理 UI 變數
    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText
    private lateinit var btnInsert: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnQuery: Button
    private lateinit var listView: ListView

    private var items: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>

    // 定義 Provider 的 Uri
    private val uri = Uri.parse("content://com.example.lab16")

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
        setListeners()
    }

    // 初始化 View，避免重複 findViewById
    private fun initViews() {
        edBook = findViewById(R.id.edBook)
        edPrice = findViewById(R.id.edPrice)
        btnInsert = findViewById(R.id.btnInsert)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnQuery = findViewById(R.id.btnQuery)
        listView = findViewById(R.id.listView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
    }

    private fun setListeners() {
        btnInsert.setOnClickListener {
            val name = edBook.text.toString()
            val price = edPrice.text.toString()

            if (name.isEmpty() || price.isEmpty()) {
                showToast("欄位請勿留空")
            } else {
                val values = ContentValues().apply {
                    put("book", name)
                    put("price", price)
                }

                // 透過 Resolver 向 Provider 新增
                val contentUri = contentResolver.insert(uri, values)

                if (contentUri != null) {
                    showToast("新增:$name, 價格:$price")
                    cleanEditText()
                } else {
                    showToast("新增失敗")
                }
            }
        }

        btnUpdate.setOnClickListener {
            val name = edBook.text.toString()
            val price = edPrice.text.toString()

            if (name.isEmpty() || price.isEmpty()) {
                showToast("欄位請勿留空")
            } else {
                val values = ContentValues().apply {
                    put("price", price)
                }

                // 根據 Lab16_1 的邏輯，我們將書名 (name) 傳入 selection 參數
                val count = contentResolver.update(uri, values, name, null)

                if (count > 0) {
                    showToast("更新:$name, 價格:$price")
                    cleanEditText()
                } else {
                    showToast("更新失敗")
                }
            }
        }

        btnDelete.setOnClickListener {
            val name = edBook.text.toString()

            if (name.isEmpty()) {
                showToast("書名請勿留空")
            } else {
                // 根據 Lab16_1 的邏輯，我們將書名 (name) 傳入 selection 參數
                val count = contentResolver.delete(uri, name, null)

                if (count > 0) {
                    showToast("刪除:$name")
                    cleanEditText()
                } else {
                    showToast("刪除失敗")
                }
            }
        }

        btnQuery.setOnClickListener {
            val name = edBook.text.toString()
            // 若無輸入書名則傳入 null，反之傳入書名
            val selection = name.ifEmpty { null }

            // 透過 Resolver 查詢
            val cursor = contentResolver.query(uri, null,
                selection,
                null,
                null)

            // 若 cursor 為 null 直接結束
            cursor ?: return@setOnClickListener

            items.clear()
            showToast("共有 ${cursor.count} 筆資料")

            // 使用 use 自動關閉 Cursor，防止記憶體洩漏
            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        // 嘗試透過欄位名稱取得 index，若找不到則使用預設值或拋出異常
                        // 為了配合 Lab16_1，這裡假設欄位名稱為 "book" 和 "price"
                        val titleIndex = c.getColumnIndex("book")
                        val priceIndex = c.getColumnIndex("price")

                        // 確保 index 有效 (大於等於 0)
                        if (titleIndex >= 0 && priceIndex >= 0) {
                            val title = c.getString(titleIndex)
                            val price = c.getInt(priceIndex)
                            items.add("書名:$title\t\t\t\t價格:$price")
                        }
                    } while (c.moveToNext())
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun cleanEditText() {
        edBook.setText("")
        edPrice.setText("")
    }
}