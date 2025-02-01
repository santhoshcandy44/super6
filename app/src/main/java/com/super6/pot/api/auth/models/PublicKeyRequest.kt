package com.super6.pot.api.auth.models



data class PublicKeyRequest(
    val e2ee_public_key: String,
    val key_version: Long
)
