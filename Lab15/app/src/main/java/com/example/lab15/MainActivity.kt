package com.example.lab15

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

    // 1. 宣告 UI 元件變數 (避免在 Listener 裡重複 findViewById)
    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText
    private lateinit var btnInsert: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnQuery: Button
    private lateinit var listView: ListView

    // 2. 資料變數
    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbHelper: MyDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化動作
        initViews()
        initDatabase()
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close() // 確保關閉資料庫連線
    }

    // 初始化 UI 元件 (統一管理 findViewById)
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

    private fun initDatabase() {
        dbHelper = MyDBHelper(this)
    }

    private fun setListeners() {
        btnInsert.setOnClickListener {
            if (validateInput()) {
                val book = edBook.text.toString()
                val price = edPrice.text.toString().toInt()

                try {
                    // 呼叫 Helper 方法 (邏輯與 UI 分離)
                    if (dbHelper.insertBook(book, price)) {
                        showToast("新增: $book, 價格: $price")
                        cleanEditText()
                    } else {
                        showToast("新增失敗 (可能書名重複)")
                    }
                } catch (e: Exception) {
                    showToast("錯誤: ${e.message}")
                }
            }
        }

        btnUpdate.setOnClickListener {
            if (validateInput()) {
                val book = edBook.text.toString()
                val price = edPrice.text.toString().toInt()

                if (dbHelper.updateBook(book, price)) {
                    showToast("更新: $book, 價格: $price")
                    cleanEditText()
                } else {
                    showToast("更新失敗 (找不到該書名)")
                }
            }
        }

        btnDelete.setOnClickListener {
            if (edBook.length() < 1) {
                showToast("書名請勿留空")
            } else {
                val book = edBook.text.toString()
                if (dbHelper.deleteBook(book)) {
                    showToast("刪除: $book")
                    cleanEditText()
                } else {
                    showToast("刪除失敗 (找不到該書名)")
                }
            }
        }

        btnQuery.setOnClickListener {
            val bookName = edBook.text.toString()
            val cursor = dbHelper.getBooks(bookName)

            items.clear()
            showToast("共有 ${cursor.count} 筆資料")

            // 使用 Kotlin 的 use 函式自動關閉 Cursor，防止記憶體洩漏
            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        // 使用欄位名稱取得 index，比寫死數字 (0, 1) 更安全
                        val titleIndex = c.getColumnIndexOrThrow(COL_BOOK) // 需對應 MyDBHelper 的常數
                        val priceIndex = c.getColumnIndexOrThrow(COL_PRICE) // 需對應 MyDBHelper 的常數

                        val title = c.getString(titleIndex)
                        val price = c.getInt(priceIndex)

                        items.add("書名:$title\t\t\t\t價格:$price")
                    } while (c.moveToNext())
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    // 抽出驗證邏輯，避免重複程式碼
    private fun validateInput(): Boolean {
        if (edBook.length() < 1 || edPrice.length() < 1) {
            showToast("欄位請勿留空")
            return false
        }
        return true
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun cleanEditText() {
        edBook.setText("")
        edPrice.setText("")
    }
}