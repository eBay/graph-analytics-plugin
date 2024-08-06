package com.ebay.plugins.graph.analytics

import org.jgrapht.graph.DefaultEdge
import org.jgrapht.nio.Attribute
import java.util.Objects

/**
 * Class which collects information about a single graph edge.  This class must be
 * mutable due to assumptions made by the jgrapht persistence layer.  The mutability then
 * causes issues with map lookups, so only the identifying fields are included in
 * [equals] and [hashCode] calculations.
 */
class EdgeInfo(
    override var attributes: MutableMap<String, Attribute> = mutableMapOf()
) : DefaultEdge(), Attributed {
    /**
     * Override to force inclusion of [source] and [target]
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EdgeInfo

        if (source != other.source) return false
        if (target != other.target) return false
        return true
    }

    /**
     * Override to force inclusion of (only) [source] and [target]
     */
    override fun hashCode(): Int {
        return Objects.hash(source, target)
    }
}
