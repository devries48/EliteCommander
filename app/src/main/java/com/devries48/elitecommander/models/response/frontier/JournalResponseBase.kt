@file:Suppress("unused")

package com.devries48.elitecommander.models.response.frontier

abstract class JournalResponseBase {
    lateinit var event: String
    lateinit var timestamp: String
}