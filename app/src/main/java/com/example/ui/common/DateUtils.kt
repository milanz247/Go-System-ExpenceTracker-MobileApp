package com.example.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Uses java.text (not java.time) since minSdk 24 is below java.time's API 26 floor
// and this project doesn't enable core library desugaring.
private fun iso8601Format() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

fun nowIso8601(): String = iso8601Format().format(Date())

fun epochMillisToIso8601(millis: Long): String = iso8601Format().format(Date(millis))

/** Cheap, parser-free display: an RFC3339 timestamp always starts with `YYYY-MM-DD`. */
fun formatDisplayDate(iso: String): String = iso.take(10)
