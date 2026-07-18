package com.example.ui.common

import androidx.compose.ui.graphics.Color

/** Parses a `#rrggbb` / `#aarrggbb` hex string (as returned by the backend) into a Compose Color. */
fun parseHexColor(hex: String): Color = runCatching {
    val cleaned = hex.removePrefix("#")
    val colorLong = cleaned.toLong(16)
    if (cleaned.length == 8) Color(colorLong) else Color(0xFF000000 or colorLong)
}.getOrDefault(Color.Gray)
