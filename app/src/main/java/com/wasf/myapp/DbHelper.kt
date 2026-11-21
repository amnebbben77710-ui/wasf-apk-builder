package com.wasf.myapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class DhikrItem(
    val id: Long,
    val title: String,
    val count: Int,
    val target: Int,
    val totalCount: Int
)

class DbHelper(context: Context) : SQLiteOpenHelper(context, "muslim_tasbih.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE adhkar (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "count INTEGER DEFAULT 0, " +
                "target INTEGER DEFAULT 33, " +
                "total_count INTEGER DEFAULT 0)"
        )
        val initial = listOf(
            Triple("سبحان الله", 0, 33),
            Triple("الحمد لله", 0, 33),
            Triple("الله أكبر", 0, 33),
            Triple("لا إله إلا الله", 0, 100),
            Triple("أستغفر الله العظيم", 0, 100),
            Triple("اللهم صل على محمد", 0, 100),
            Triple("سبحان الله وبحمده", 0, 100),
            Triple("لا حول ولا قوة إلا بالله", 0, 33)
        )
        for (item in initial) {
            val cv = ContentValues().apply {
                put("title", item.first)
                put("count", item.second)
                put("target", item.third)
                put("total_count", 0)
            }
            db.insert("adhkar", null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS adhkar")
        onCreate(db)
    }

    fun getAllAdhkar(query: String = ""): List<DhikrItem> {
        val list = mutableListOf<DhikrItem>()
        val db = readableDatabase
        val cursor = if (query.isBlank()) {
            db.rawQuery("SELECT id, title, count, target, total_count FROM adhkar ORDER BY id ASC", null)
        } else {
            db.rawQuery(
                "SELECT id, title, count, target, total_count FROM adhkar WHERE title LIKE ? ORDER BY id ASC",
                arrayOf("%$query%")
            )
        }
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DhikrItem(
                        id = it.getLong(0),
                        title = it.getString(1),
                        count = it.getInt(2),
                        target = it.getInt(3),
                        totalCount = it.getInt(4)
                    )
                )
            }
        }
        return list
    }

    fun getDhikrById(id: Long): DhikrItem? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, title, count, target, total_count FROM adhkar WHERE id = ?",
            arrayOf(id.toString())
        )
        cursor.use {
            if (it.moveToFirst()) {
                return DhikrItem(
                    id = it.getLong(0),
                    title = it.getString(1),
                    count = it.getInt(2),
                    target = it.getInt(3),
                    totalCount = it.getInt(4)
                )
            }
        }
        return null
    }

    fun incrementDhikr(id: Long) {
        writableDatabase.execSQL(
            "UPDATE adhkar SET count = count + 1, total_count = total_count + 1 WHERE id = ?",
            arrayOf(id.toString())
        )
    }

    fun resetDhikrCount(id: Long) {
        writableDatabase.execSQL(
            "UPDATE adhkar SET count = 0 WHERE id = ?",
            arrayOf(id.toString())
        )
    }

    fun addDhikr(title: String, target: Int): Long {
        val cv = ContentValues().apply {
            put("title", title)
            put("count", 0)
            put("target", target)
            put("total_count", 0)
        }
        return writableDatabase.insert("adhkar", null, cv)
    }

    fun updateTarget(id: Long, target: Int) {
        val cv = ContentValues().apply { put("target", target) }
        writableDatabase.update("adhkar", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getGrandTotal(): Int {
        val cursor = readableDatabase.rawQuery("SELECT SUM(total_count) FROM adhkar", null)
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

    fun resetAllTotals() {
        writableDatabase.execSQL("UPDATE adhkar SET count = 0, total_count = 0")
    }
}
