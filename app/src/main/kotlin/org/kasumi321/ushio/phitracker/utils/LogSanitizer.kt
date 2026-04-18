package org.kasumi321.ushio.phitracker.utils

object LogSanitizer {
    private val sessionHeaderRegex = Regex("""X-LC-Session:\s*\S+""", RegexOption.IGNORE_CASE)
    private val platformIdRegex = Regex("""platform_id\s*[=:]\s*"?[^"\s,}]+""", RegexOption.IGNORE_CASE)
    private val sessionTokenFieldRegex = Regex("""("sessionToken"\s*:\s*")([^"]+)(")""", RegexOption.IGNORE_CASE)
    private val longTokenRegex = Regex("""\b[a-zA-Z0-9\-_]{24,}\b""")

    fun sanitize(message: String): String {
        return message
            .replace(sessionHeaderRegex, "X-LC-Session: ***REDACTED***")
            .replace(platformIdRegex, "platform_id=***REDACTED***")
            .replace(sessionTokenFieldRegex, "$1***REDACTED***$3")
            .replace(longTokenRegex) { token ->
                if (looksLikeTimestamp(token.value)) token.value else "***SESSION_TOKEN***"
            }
    }

    private fun looksLikeTimestamp(value: String): Boolean {
        return value.all { it.isDigit() } && value.length >= 10
    }
}
