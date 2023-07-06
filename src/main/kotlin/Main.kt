import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

val scores = mapOf(
    "good" to 1,
    "great" to 2,
    "bad" to -1,
    "terrible" to -2
)

fun main() = runBlocking {
    val fileUrls = listOf(
        "https://drive.google.com/uc?export=download&id=1A9lCqh5drdv8zT5CK6CrYgJD97jPu6Hq",
        "https://drive.google.com/uc?export=download&id=158dsvjuZaQaeLBzhKjTqc-DT5s_Voutw",
        "https://drive.google.com/uc?export=download&id=1BCsdTTWhWYC2rC3R2eD_bLyYafiAnwZf",
        "https://drive.google.com/uc?export=download&id=1J2yfHoXJKdjzbGrauzEWIyPXZD7Ie50o",
        "https://drive.google.com/uc?export=download&id=13ajFpY--Sia9PT--dJEDVadOOGJ0e8xt",
        "https://drive.google.com/uc?export=download&id=1fSOZT1lAlVKv1jqh3ciJz5kEEPjw-dTS",
        "https://drive.google.com/uc?export=download&id=1Qaa9UZvfDYl04A0WneJuuLHU5wn1WZPe"
    )

    val downloadedFiles = fileUrls.mapIndexed { index, url ->
        async(Dispatchers.IO) {
            val fileName = "file_$index.txt"
            downloadFile(url, fileName)
            fileName
        }
    }.awaitAll()

    val wordOccurrences = HashMap<String, Int>()

    downloadedFiles.forEachIndexed { index, fileName ->
        val file = File(fileName)
        val content = file.readText()
        val words = content.split("\\s+".toRegex())

        words.forEach { word ->
            val lowerCaseWord = word.toLowerCase()
            if (wordOccurrences.containsKey(lowerCaseWord)) {
                wordOccurrences[lowerCaseWord] = wordOccurrences[lowerCaseWord]!! + 1
            } else {
                wordOccurrences[lowerCaseWord] = 1
            }
        }

        val score = calculateScore(words)
        val newFileName = when {
            score > 0 -> getUniqueFileName("positive")
            score < 0 -> getUniqueFileName("negative")
            else -> getUniqueFileName("neutral")
        }

        file.renameTo(File(newFileName))
        println("$fileName - Score: $score, Renamed to: $newFileName")
    }

    println("Word Occurrences:")
    wordOccurrences.forEach { (word, count) ->
        println("$word: $count")
    }
}

suspend fun downloadFile(url: String, fileName: String) {
    val connection = URL(url).openConnection()
    val inputStream = connection.getInputStream()
    val file = File(fileName)
    file.outputStream().use { fileOutputStream ->
        inputStream.copyTo(fileOutputStream)
    }
}

fun calculateScore(words: List<String>): Int {
    var score = 0
    words.forEach { word ->
        val lowerCaseWord = word.lowercase(Locale.getDefault())
        score += scores[lowerCaseWord] ?: 0
    }
    return score
}

fun getUniqueFileName(prefix: String): String {
    var count = 0
    var uniqueFileName = "$prefix.txt"
    while (File(uniqueFileName).exists()) {
        count++
        uniqueFileName = "$prefix ($count).txt"
    }
    return uniqueFileName
}
