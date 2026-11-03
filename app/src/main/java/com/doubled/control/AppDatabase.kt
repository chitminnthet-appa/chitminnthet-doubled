package com.doubled.control

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "double_d_management.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE agents (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, brake REAL NOT NULL DEFAULT 0, commission REAL NOT NULL DEFAULT 10, rate REAL NOT NULL DEFAULT 80, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)""")
        db.execSQL("""CREATE TABLE entries (id INTEGER PRIMARY KEY AUTOINCREMENT, session TEXT NOT NULL, agent TEXT NOT NULL, bettor_name TEXT NOT NULL DEFAULT '', entry_kind TEXT NOT NULL DEFAULT 'ထိုး', bet_type TEXT NOT NULL, digits TEXT NOT NULL, amount REAL NOT NULL, entry_date TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)""")
        db.execSQL("""CREATE TABLE session_results (id INTEGER PRIMARY KEY AUTOINCREMENT, session TEXT NOT NULL, result_date TEXT NOT NULL, result TEXT NOT NULL, UNIQUE(session, result_date))""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE entries ADD COLUMN bettor_name TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE entries ADD COLUMN entry_kind TEXT NOT NULL DEFAULT 'ထိုး'")
        }
    }

    fun getAgents(): JSONArray {
        val out = JSONArray()
        readableDatabase.rawQuery("SELECT name, brake, commission, rate FROM agents ORDER BY id", null).use { c ->
            while (c.moveToNext()) out.put(JSONObject().put("name", c.getString(0)).put("brake", c.getDouble(1)).put("commission", c.getDouble(2)).put("rate", c.getDouble(3)))
        }
        return out
    }

    fun saveAgent(name: String, brake: Double, commission: Double, rate: Double) {
        val v = ContentValues().apply { put("name", name.trim()); put("brake", brake); put("commission", commission); put("rate", rate) }
        writableDatabase.insertWithOnConflict("agents", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun renameAgent(oldName: String, newName: String, brake: Double, commission: Double, rate: Double) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val v = ContentValues().apply { put("name", newName.trim()); put("brake", brake); put("commission", commission); put("rate", rate) }
            db.insertWithOnConflict("agents", null, v, SQLiteDatabase.CONFLICT_REPLACE)
            db.update("entries", ContentValues().apply { put("agent", newName.trim()) }, "agent=?", arrayOf(oldName))
            if (oldName != newName) db.delete("agents", "name=?", arrayOf(oldName))
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun deleteAgent(name: String) { writableDatabase.delete("agents", "name=?", arrayOf(name)) }

    fun getEntries(): JSONArray {
        val out = JSONArray()
        readableDatabase.rawQuery("SELECT id, session, agent, bettor_name, entry_kind, bet_type, digits, amount, entry_date, created_at FROM entries ORDER BY id", null).use { c ->
            while (c.moveToNext()) out.put(JSONObject()
                .put("id", c.getLong(0)).put("session", c.getString(1)).put("agent", c.getString(2))
                .put("bettor", c.getString(3)).put("kind", c.getString(4)).put("type", c.getString(5)).put("digits", c.getString(6))
                .put("amount", c.getDouble(7)).put("date", c.getString(8)).put("createdAt", c.getString(9)))
        }
        return out
    }

    fun saveEntry(session: String, agent: String, bettor: String, kind: String, type: String, digits: String, amount: Double, date: String) {
        val v = ContentValues().apply {
            put("session", session); put("agent", agent); put("bettor_name", bettor); put("entry_kind", kind)
            put("bet_type", type); put("digits", digits); put("amount", amount); put("entry_date", date)
        }
        writableDatabase.insert("entries", null, v)
    }

    fun deleteEntries(agent: String, date: String) {
        writableDatabase.delete("entries", "agent=? AND entry_date=?", arrayOf(agent, date))
    }

    fun getResults(): JSONArray {
        val out = JSONArray()
        readableDatabase.rawQuery("SELECT session, result_date, result FROM session_results ORDER BY result_date DESC", null).use { c ->
            while (c.moveToNext()) out.put(JSONObject().put("session", c.getString(0)).put("date", c.getString(1)).put("result", c.getString(2)))
        }
        return out
    }

    fun saveResult(session: String, date: String, result: String) {
        val v = ContentValues().apply { put("session", session); put("result_date", date); put("result", result) }
        writableDatabase.insertWithOnConflict("session_results", null, v, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
