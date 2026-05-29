package com.ryanshelby.iptv.data

data class Channel(
    val name: String,
    val url: String,
    val logo: String,
    val group: String = "",
    val isFavorite: Boolean = false
)
