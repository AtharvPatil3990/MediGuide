package com.example.core.localization

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "हिन्दी", "Hindi"),
    MARATHI("mr", "मराठी", "Marathi");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
