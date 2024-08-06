package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.Attributed
import org.jgrapht.nio.Attribute

/**
 * Matcher which extracts the specified `String` attribute, delegating the match of the
 * value to the specified matcher.
 */
internal class AttributeStringGraphMatcher(
	private val attributeName: String,
	private val delegate: GraphMatcher<String?>
): GraphMatcher<Attributed>  {
	override fun matches(value: Attributed): DescribedMatch {
		val attributeValue = value.attributes[attributeName]
		var delegateResult = delegate.matches(attributeValue.nullSafeValue())
		if (!delegateResult.matched) {
			// tweak the actual value to provide additional information
			delegateResult = delegateResult.copy(
				actual = { attributeValue.buildActual() }
			)
		}
		return DescribedMatch(
			actual = { attributeValue.buildActual() },
			description = value.summarizedDescription("attribute '$attributeName' string value ${attributeValue.quote()}"),
			matched = delegateResult.matched,
			subResults = listOf(delegateResult)
		)
	}

	private fun Attribute?.buildActual(): Any {
		return if (this == null) {
			"'$attributeName' attribute not found"
		} else {
			"$type attribute with value ${value.quote()}"
		}
	}

	private fun Attribute?.nullSafeValue(): String {
		return this?.value.toString()
	}
}
