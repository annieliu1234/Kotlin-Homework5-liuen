package com.example.lab16_1

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// 定義常數
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
        // 使用常數，避免 Typo
        db.execSQL("CREATE TABLE $TABLE_NAME ($COL_BOOK TEXT PRIMARY KEY, $COL_PRICE INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}