package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.Attributed
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.AttributeType

/**
 * Matcher which extracts the specified `Number` attribute, delegating the match of the
 * value to the specified matcher.
 */
internal class AttributeNumberGraphMatcher(
	private val attributeName: String,
	private val delegate: GraphMatcher<Number?>
): GraphMatcher<Attributed>  {
	override fun matches(value: Attributed): DescribedMatch {
		val attributeValue = value.attributes[attributeName]
		var delegateResult = delegate.matches(attributeValue.value())
		if (!delegateResult.matched) {
			// tweak the actual value to provide additional information
			delegateResult = delegateResult.copy(
				actual = { attributeValue.buildActual() }
			)
		}
		return DescribedMatch(
			actual = { attributeValue.buildActual() },
			description = value.summarizedDescription("attribute '$attributeName' numeric value ${attributeValue.quote()}"),
			matched = delegateResult.matched,
			subResults = listOf(delegateResult)
		)
	}

	private fun Attribute?.buildActual(): Any? {
		return when (this?.type) {
			null -> {
				"no '$attributeName' attribute found"
			}
			AttributeType.INT, AttributeType.LONG, AttributeType.FLOAT, AttributeType.DOUBLE -> {
				this.value()
			}
			else -> {
				"$type attribute with value ${value.quote()}"
			}
		}
	}

	private fun Attribute?.value(): Number? {
		return when (this?.type) {
			AttributeType.INT -> value.toIntOrNull()
			AttributeType.LONG -> value.toLongOrNull()
			AttributeType.FLOAT -> value.toFloatOrNull()
			AttributeType.DOUBLE -> value.toDoubleOrNull()
			else -> null
		}
	}
}
