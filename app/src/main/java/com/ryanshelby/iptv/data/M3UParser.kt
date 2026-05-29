package com.ryanshelby.iptv.data

import java.io.InputStream

object M3UParser {

    fun parse(input: InputStream): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = input.bufferedReader().readLines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val name = line.substringAfter(",", "Unknown").trim()
                val logo = Regex("""tvg-logo="(.*?)"""").find(line)?.groupValues?.get(1) ?: ""
                var group = Regex("""group-title="(.*?)"""").find(line)?.groupValues?.get(1) ?: ""
                group = group.trim().split(" ").joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString()
                    }
                }
                val url = if (i + 1 < lines.size) lines[i + 1].trim() else ""
                if (url.isNotBlank() && !url.startsWith("#")) {
                    channels.add(Channel(name = name, url = url, logo = logo, group = group))
                    i += 2
                    continue
                }
            }
            i++
        }
        return channels
    }

    fun parseFromText(text: String): List<Channel> = parse(text.byteInputStream())
}
