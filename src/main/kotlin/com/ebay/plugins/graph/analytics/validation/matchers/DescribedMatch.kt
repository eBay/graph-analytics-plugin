package com.ebay.plugins.graph.analytics.validation.matchers

/**
 * The result of evaluating a [GraphMatcher] against an input.
 */
data class DescribedMatch(
    /**
     * The actual value of the input.
     */
    val actual: () -> Any?,
    /**
     * A descriptive blurb of what the matcher operation was (e.g., `equal to`)
     */
    val description: String,
    /**
     * Flag indicating whether the match was successful.
     */
    val matched: Boolean,
    /**
     * If a matcher delegates to other matchers, this list should contain the results of each
     * of the delegate matchers' results.
     */
    val subResults: List<DescribedMatch> = emptyList(),
    /**
     * Flag indicating that the match is the result of an inversion rule (e.g., `not`)
     **/
    val inversion: Boolean = false
) {
    /**
     * Render a human-readable representation of the match result.
     */
    fun render(
        /**
         * Flag indicating that only matching results should be rendered.
         */
        onlyMatches: Boolean = false,
        indent: String = ""
    ): String {
        return buildString {
            renderInternal(
                onlyMatches = onlyMatches,
                value = this@DescribedMatch,
                builder = this,
                indent = indent)
        }.trimEnd()
    }

    private fun renderInternal(
        onlyMatches: Boolean,
        value: DescribedMatch,
        builder: StringBuilder,
        indent: String,
        invertMatch: Boolean = false,
    ) {
        val matched = if (invertMatch) {
            !value.matched
        } else {
            value.matched
        }

        val indicator = if (value.matched) {
            "\u2713 "
        } else {
            "\u2717 "
        }
        if (matched || !onlyMatches || value.inversion) {
            if (value.subResults.isEmpty()) {
                builder.append(indent)
                    .append(indicator)
                    .append(value.description)
                if (!matched) {
                    builder.append(" (was: ")
                        .append(value.actual())
                        .append(")")
                }
                builder.appendLine()
            } else {
                builder.append(indent)
                    .append(indicator)
                    .append(value.description)
                    .append(":\n")
                value.subResults.forEach { subResult ->
                    renderInternal(onlyMatches, subResult, builder, "$indent    ", invertMatch = invertMatch xor value.inversion)
                }
            }
        }
    }
}
