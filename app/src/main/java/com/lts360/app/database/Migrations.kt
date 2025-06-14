package com.lts360.app.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_54_55 = object : Migration(54, 55) {
    override fun migrate(db: SupportSQLiteDatabase) {


        db.execSQL("DROP INDEX IF EXISTS index_messages_chat_id_id")
        db.execSQL("DROP INDEX IF EXISTS index_messages_chat_id_read_id")
        db.execSQL("DROP INDEX IF EXISTS index_messages_chat_id_read")

        // Create the new index on chat_id
        db.execSQL("CREATE INDEX IF NOT EXISTS index_messages_chat_id ON messages(chat_id)")


    }
}
