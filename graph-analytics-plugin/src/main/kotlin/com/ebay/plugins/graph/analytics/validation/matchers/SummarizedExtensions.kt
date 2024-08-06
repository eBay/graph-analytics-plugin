package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.validation.Summarized

internal fun Any?.quote(): String {
    return when(this) {
        null -> "<null>"
        is Number -> this.toString()
        else -> "'$this'"
    }
}

internal fun Any?.summarizedDescription(description: String): String {
    return if (this is Summarized) {
        "[${getSummary()}] $description"
    } else {
        description
    }
}