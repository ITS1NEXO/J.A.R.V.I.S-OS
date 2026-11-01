package com.zyvix.zorinm4

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

data class ChatMessage(val role: String, val content: String, val time: Long)

class ChatStore(context: Context) : SQLiteOpenHelper(context, "zorin_mobile.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE memories(id INTEGER PRIMARY KEY AUTOINCREMENT, fact TEXT NOT NULL UNIQUE, created_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(role: String, content: String) {
        writableDatabase.insert("messages", null, ContentValues().apply {
            put("role", role); put("content", content); put("created_at", System.currentTimeMillis())
        })
    }
    fun recent(limit: Int = 24): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        readableDatabase.rawQuery("SELECT role,content,created_at FROM messages ORDER BY id DESC LIMIT ?", arrayOf(limit.toString())).use { c ->
            while (c.moveToNext()) result.add(ChatMessage(c.getString(0), c.getString(1), c.getLong(2)))
        }
        return result.reversed()
    }
    fun remember(fact: String) {
        writableDatabase.insertWithOnConflict("memories", null, ContentValues().apply {
            put("fact", fact); put("created_at", System.currentTimeMillis())
        }, SQLiteDatabase.CONFLICT_IGNORE)
    }
    fun memories(): List<String> {
        val result = mutableListOf<String>()
        readableDatabase.rawQuery("SELECT fact FROM memories ORDER BY id DESC LIMIT 30", null).use { c ->
            while (c.moveToNext()) result.add(c.getString(0))
        }
        return result
    }
    fun clearChat() { writableDatabase.delete("messages", null, null) }
    fun clearMemories() { writableDatabase.delete("memories", null, null) }
}
