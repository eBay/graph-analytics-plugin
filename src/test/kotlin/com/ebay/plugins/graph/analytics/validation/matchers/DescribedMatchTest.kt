package com.ebay.plugins.graph.analytics.validation.matchers

import org.testng.annotations.Test

class DescribedMatchTest {

    @Test
    fun render() {
        val actual = DescribedMatch(
            actual = { "actual" },
            description = "any item",
            matched = true,
            subResults = listOf(
                DescribedMatch(
                    actual = { "subActual" },
                    description = "equal to 'nested'",
                    matched = false,
                    subResults = emptyList(),
                ),
                DescribedMatch(
                    actual = { "notActual" },
                    description = "not",
                    matched = true,
                    inversion = true,
                    subResults = listOf(
                        DescribedMatch(
                            actual = { "false" },
                            description = "equals 'true' (was: 'false')",
                            matched = false,
                            subResults = emptyList(),
                        ),
                    ),
                ),
            ),
        ).render(onlyMatches = true)
        println(actual)
    }
}