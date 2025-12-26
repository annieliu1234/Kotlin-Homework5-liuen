package com.example.lab15

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// 定義常數，避免錯字 (Typo)
const val TABLE_NAME = "myTable"
const val COL_BOOK = "book"
const val COL_PRICE = "price"

class MyDBHelper(
    context: Context,
    name: String = DB_NAME,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = VERSION
) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private const val DB_NAME = "myDatabase"
        private const val VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 使用常數定義 SQL，更安全
        val sql = "CREATE TABLE $TABLE_NAME ($COL_BOOK TEXT PRIMARY KEY, $COL_PRICE INTEGER NOT NULL)"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // --- 以下為封裝好的操作方法 (Encapsulation) ---

    // 新增資料 (回傳是否成功)
    fun insertBook(book: String, price: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_BOOK, book)
            put(COL_PRICE, price)
        }
        // insert 會回傳 row ID，若為 -1 代表失敗
        val result = db.insert(TABLE_NAME, null, values)
        return result != -1L
    }

    // 更新資料
    fun updateBook(book: String, price: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PRICE, price)
        }
        // 使用 ? 防止 SQL Injection，回傳受影響的行數
        val rows = db.update(TABLE_NAME, values, "$COL_BOOK = ?", arrayOf(book))
        return rows > 0
    }

    // 刪除資料
    fun deleteBook(book: String): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_NAME, "$COL_BOOK = ?", arrayOf(book))
        return rows > 0
    }

    // 查詢資料 (回傳 Cursor)
    fun getBooks(book: String = ""): Cursor {
        val db = readableDatabase
        return if (book.isEmpty()) {
            // 查詢全部
            db.query(TABLE_NAME, null, null, null, null, null, null)
        } else {
            // 模糊搜尋範例，或是精確搜尋
            db.query(TABLE_NAME, null, "$COL_BOOK LIKE ?", arrayOf(book), null, null, null)
        }
    }
}