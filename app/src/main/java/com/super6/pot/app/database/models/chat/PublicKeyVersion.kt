package com.super6.pot.app.database.models.chat

import androidx.room.ColumnInfo


data class PublicKeyVersion(
    @ColumnInfo(name = "public_key") val publicKey: String,
    @ColumnInfo(name = "key_version") val keyVersion: Long
)