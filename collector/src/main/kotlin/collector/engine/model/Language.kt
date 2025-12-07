package collector.engine.model

enum class Language {
    KO, EN;

    companion object {
        fun from(value: String): Language {
            return entries.find { it.name == value } ?: KO
        }
    }
}