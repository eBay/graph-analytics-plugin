package com.ebay.plugins.graph.analytics.validation

/**
 * Interface which is applied to objects which can be summarized into a simpler form
 * that will be more readily understood by a human in a report.
 */
interface Summarized {
    fun getSummary(): String
}