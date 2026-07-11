package com.eliasgreen18.vocabularytracker.data.remote.phonetics

import com.eliasgreen18.vocabularytracker.domain.repository.PhoneticService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPhoneticService @Inject constructor() : PhoneticService {

    private val commonIpa = mapOf(
        "jail" to "/dʒeɪl/",
        "lighthouse" to "/ˈlaɪthaʊs/",
        "dandelion" to "/ˈdændɪlaɪən/",
        "shimmer" to "/ˈʃɪmər/",
        "wilderness" to "/ˈwɪldərnəs/",
        "arcane" to "/ɑːrˈkeɪn/"
    )

    override suspend fun getIpa(text: String): String? {
        return commonIpa[text.lowercase().trim()]
    }
}
