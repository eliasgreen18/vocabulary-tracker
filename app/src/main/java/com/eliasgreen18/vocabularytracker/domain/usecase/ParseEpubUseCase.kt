package com.eliasgreen18.vocabularytracker.domain.usecase

import com.eliasgreen18.vocabularytracker.ui.reader.EpubChapter
import com.eliasgreen18.vocabularytracker.ui.reader.EpubContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipException
import java.util.zip.ZipFile
import javax.inject.Inject

class ParseEpubUseCase @Inject constructor() {
    suspend operator fun invoke(file: File, fallbackTitle: String): Result<EpubContent> = withContext(Dispatchers.IO) {
        runCatching {
            if (!file.exists()) throw FileNotFoundException("Book file not found at: ${file.absolutePath}")
            
            ZipFile(file).use { zipFile ->
                val containerEntry = zipFile.getEntry("META-INF/container.xml") 
                    ?: throw ZipException("Invalid EPUB: META-INF/container.xml missing")
                
                val containerXml = zipFile.getInputStream(containerEntry).bufferedReader().readText()
                val opfPath = Regex("full-path=\"([^\"]+)\"").find(containerXml)?.groupValues?.get(1) 
                    ?: throw ZipException("Invalid EPUB: OPF path not found in container.xml")
                
                val opfEntry = zipFile.getEntry(opfPath) 
                    ?: throw ZipException("Invalid EPUB: OPF file entry missing at $opfPath")
                
                val opfXml = zipFile.getInputStream(opfEntry).bufferedReader().readText()
                val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
                
                val manifestMap = mutableMapOf<String, String>()
                Regex("<item [^>]*id=\"([^\"]+)\" [^>]*href=\"([^\"]+)\"").findAll(opfXml).forEach { 
                    manifestMap[it.groupValues[1]] = it.groupValues[2]
                }
                
                val spineOrder = Regex("<itemref [^>]*idref=\"([^\"]+)\"").findAll(opfXml).map { it.groupValues[1] }.toList()
                if (spineOrder.isEmpty()) throw ZipException("Invalid EPUB: Spine/Reading order is empty")
                
                val chapters = mutableListOf<EpubChapter>()
                val ignoreKeywords = listOf(
                    "author", "content", "title page", "copyright", "dedication", 
                    "introduction", "about the author", "contents", "table of contents",
                    "preface", "foreword", "acknowledgments"
                )

                spineOrder.forEachIndexed { index, idref ->
                    val href = manifestMap[idref] ?: return@forEachIndexed
                    val entryPath = opfDir + href
                    val entry = zipFile.getEntry(entryPath) ?: return@forEachIndexed
                    
                    zipFile.getInputStream(entry).use { stream ->
                        val html = stream.bufferedReader().readText()
                        val doc = Jsoup.parse(html)
                        val bodyText = doc.body().text()
                        val title = doc.title().ifBlank { "Section ${index + 1}" }
                        
                        val isMeaningful = bodyText.length > 300 
                        val isMetaSection = ignoreKeywords.any { title.contains(it, ignoreCase = true) }
                        
                        if (isMeaningful && !isMetaSection) {
                            chapters.add(EpubChapter(
                                title = title,
                                plainText = bodyText,
                                originalIndex = index
                            ))
                        }
                    }
                }
                
                if (chapters.isEmpty()) throw Exception("No readable chapters found in this book.")
                
                EpubContent(title = fallbackTitle, chapters = chapters)
            }
        }
    }
}
