package com.example.lab16_1

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class MyContentProvider : ContentProvider() {
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(): Boolean {
        val context = context ?: return false
        dbrw = MyDBHelper(context).writableDatabase
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val cv = values ?: return null
        // 使用 TABLE_NAME 常數
        val rowId = dbrw.insert(TABLE_NAME, null, cv)

        // 若插入失敗 (rowId == -1)，回傳 null 告知呼叫者
        if (rowId == -1L) return null

        return Uri.parse("content://com.example.lab16/$rowId")
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val name = selection ?: return 0
        val cv = values ?: return 0

        // --- 修正安全性漏洞 ---
        // 原本: "book='${name}'" -> 危險！
        // 修改: 使用 whereClause + whereArgs
        return dbrw.update(TABLE_NAME, cv, "$COL_BOOK = ?", arrayOf(name))
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val name = selection ?: return 0

        // --- 修正安全性漏洞 ---
        return dbrw.delete(TABLE_NAME, "$COL_BOOK = ?", arrayOf(name))
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        // --- 修正安全性漏洞 ---
        // 若 selection 為 null，則查詢全部
        // 若有值，則當作書名查詢 (使用參數化查詢)

        val cursor = if (selection.isNullOrEmpty()) {
            dbrw.query(TABLE_NAME, null, null, null, null, null, null)
        } else {
            dbrw.query(TABLE_NAME, null, "$COL_BOOK = ?", arrayOf(selection), null, null, null)
        }

        return cursor
    }

    override fun getType(uri: Uri): String? = null
}