package dev.ninjacheetah.tigerdine.util

// Some handy sample code for decoding HTML character codes in text so that I don't have to
// manually handle them everywhere I might encounter them.
// Random side note: this comment above had two different typos in it when I copied it over from
// the iOS source code, somehow. It's not even that long of a comment.
fun String.decodingHTMLEntities(): String {
    // If there are no "&"s then there's no HTML stuff to decode.
    if (!this.contains("&")) return this

    var result = this
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")

    if (result.contains("&#")) {
        val regex = Regex("&#([0-9]+);")
        result = regex.replace(result) { match ->
            val codePoint = match.groupValues[1].toIntOrNull()
            if (codePoint != null && Character.isValidCodePoint(codePoint)) {
                String(Character.toChars(codePoint))
            } else {
                match.value
            }
        }
    }

    return result
}
