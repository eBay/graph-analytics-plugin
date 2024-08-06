package com.ebay.plugins.graph.analytics

import org.jgrapht.nio.AttributeType

/**
 * Data class encapsulating an attribute change.  Used in [ComparisonTask].
 */
internal data class ComparisonChange(
    val project: String,
    val attributeName: String,
    val attributeType: AttributeType,
    val originalValue: String,
    val changedValue: String,
) {
    val comparisonResult by lazy {
        when(attributeType) {
            AttributeType.BOOLEAN -> {
                originalValue.toBoolean().compareTo(changedValue.toBoolean())
            }
            AttributeType.INT -> {
                originalValue.toInt().compareTo(changedValue.toInt())
            }
            AttributeType.LONG -> {
                originalValue.toLong().compareTo(changedValue.toLong())
            }
            AttributeType.DOUBLE ->  {
                originalValue.toDouble().compareTo(changedValue.toDouble())
            }
            AttributeType.FLOAT -> {
                originalValue.toFloat().compareTo(changedValue.toFloat())
            }
            else ->  {
                originalValue.compareTo(changedValue)
            }
        }
    }

    fun originalAsNumber(): Number? {
        return toNumber(originalValue)
    }

    fun changedAsNumber(): Number? {
        return toNumber(changedValue)
    }

    private fun toNumber(value: String): Number? {
        return when(attributeType) {
            AttributeType.INT -> {
                value.toInt()
            }
            AttributeType.LONG -> {
                value.toLong()
            }
            AttributeType.DOUBLE ->  {
                value.toDouble()
            }
            AttributeType.FLOAT -> {
                value.toFloat()
            }
            else -> {
                null
            }
        }
    }

    override fun toString(): String {
        return "$attributeName ${comparisonStr()} from $originalValue to $changedValue"
    }

    private fun comparisonStr(): String {
        return if (comparisonResult < 0) {
            "increased"
        } else if (comparisonResult > 0) {
            "decreased"
        } else {
            "unchanged"
        }
    }
}
