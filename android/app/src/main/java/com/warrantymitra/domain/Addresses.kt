package com.warrantymitra.domain

// Google's geocoder often returns a Plus Code such as "C9XR+8H5" as the place name, which means nothing to a person.
private val plusCode = Regex("""^[23456789CFGHJMPQRVWX]{2,8}\+[23456789CFGHJMPQRVWX]{2,3}$""", RegexOption.IGNORE_CASE)

fun isPlusCode(s: String): Boolean = plusCode.matches(s.trim())

/** Joins address parts into "12 MG Road, Madhapur", dropping blanks, repeats and Plus Codes. */
fun readableAddress(vararg parts: String?): String =
    parts.mapNotNull { it?.trim() }.filter { it.isNotEmpty() && !isPlusCode(it) }.distinct().joinToString(", ")

/** Drops a leading Plus Code from a full address line: "C9XR+8H5, Madhapur, Hyderabad" -> "Madhapur, Hyderabad". */
fun stripPlusCode(line: String): String = line.split(",").map { it.trim() }.filterNot { isPlusCode(it) }.joinToString(", ")
