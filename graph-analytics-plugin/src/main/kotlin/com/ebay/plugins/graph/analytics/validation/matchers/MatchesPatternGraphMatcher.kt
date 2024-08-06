package com.ebay.plugins.graph.analytics.validation.matchers

import java.util.regex.Pattern

/**
 * Matcher which matches an input string against a supplied regular expression.
 */
internal class MatchesPatternGraphMatcher(
	private val patternString: String
): GraphMatcher<String>  {
	private val pattern = Pattern.compile(patternString)

	override fun matches(value: String): DescribedMatch {
		val actual = pattern.matcher(value).matches()
		return DescribedMatch(
			actual = { value },
			description = "matches pattern '$patternString'",
			matched = actual,
		)
	}
}
