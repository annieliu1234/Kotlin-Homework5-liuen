package com.example.lab16_1

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
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

    // 1. 集中宣告 UI 變數
    private lateinit var edBook: EditText
    private lateinit var edPrice: EditText
    private lateinit var btnInsert: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnQuery: Button
    private lateinit var listView: ListView

    private var items: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

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
        initDatabase()
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }

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
        dbrw = MyDBHelper(this).writableDatabase
    }

    private fun setListeners() {
        btnInsert.setOnClickListener {
            if (validateInput()) {
                val book = edBook.text.toString()
                val price = edPrice.text.toString().toInt()

                try {
                    // 使用 ContentValues 取代直接拼 SQL
                    val cv = ContentValues().apply {
                        put(COL_BOOK, book)
                        put(COL_PRICE, price)
                    }

                    val result = dbrw.insert(TABLE_NAME, null, cv)
                    if (result != -1L) {
                        showToast("新增: $book, 價格: $price")
                        cleanEditText()
                    } else {
                        showToast("新增失敗 (書名可能重複)")
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

                try {
                    val cv = ContentValues().apply {
                        put(COL_PRICE, price)
                    }
                    // 安全更新
                    val count = dbrw.update(TABLE_NAME, cv, "$COL_BOOK = ?", arrayOf(book))

                    if (count > 0) {
                        showToast("更新: $book, 價格: $price")
                        cleanEditText()
                    } else {
                        showToast("更新失敗 (找不到該書名)")
                    }
                } catch (e: Exception) {
                    showToast("錯誤: ${e.message}")
                }
            }
        }

        btnDelete.setOnClickListener {
            if (edBook.length() < 1) {
                showToast("書名請勿留空")
            } else {
                val book = edBook.text.toString()
                try {
                    // 安全刪除
                    val count = dbrw.delete(TABLE_NAME, "$COL_BOOK = ?", arrayOf(book))
                    if (count > 0) {
                        showToast("刪除: $book")
                        cleanEditText()
                    } else {
                        showToast("刪除失敗 (找不到該書名)")
                    }
                } catch (e: Exception) {
                    showToast("錯誤: ${e.message}")
                }
            }
        }

        btnQuery.setOnClickListener {
            val bookName = edBook.text.toString()
            val selection = if (bookName.isEmpty()) null else "$COL_BOOK = ?"
            val selectionArgs = if (bookName.isEmpty()) null else arrayOf(bookName)

            // 使用標準 query 方法
            val cursor = dbrw.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)

            items.clear()
            showToast("共有 ${cursor.count} 筆資料")

            // 安全的 Cursor 遍歷
            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        val title = c.getString(c.getColumnIndexOrThrow(COL_BOOK))
                        val price = c.getInt(c.getColumnIndexOrThrow(COL_PRICE))
                        items.add("書名:$title\t\t\t\t價格:$price")
                    } while (c.moveToNext())
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

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