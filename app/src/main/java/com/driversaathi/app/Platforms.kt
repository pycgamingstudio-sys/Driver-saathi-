package com.driversaathi.app

object Platforms {
    /** Returns "Ola", "Uber" or "Rapido" if the package belongs to a ride app, else null. */
    fun of(pkg: String): String? {
        val seg = pkg.lowercase().split('.')
        return when {
            seg.any { it.startsWith("ola") } -> "Ola"
            seg.any { it.startsWith("uber") } -> "Uber"
            seg.any { it.contains("rapido") } -> "Rapido"
            else -> null
        }
    }
}
