package com.ebay.plugins.graph.analytics

import org.jgrapht.nio.Attribute
import java.io.Serializable

/**
 * Class which collects information about a single graph vertex.  This class must be
 * mutable due to assumptions made by the jgrapht persistence layer.  The mutability then
 * causes issues with map lookups so only the identifying fields are included in
 * [equals] and [hashCode] calculations.
 */
class VertexInfo(
    override var attributes: MutableMap<String, Attribute> = mutableMapOf(),
    var path: String,
) : Attributed, Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VertexInfo

        if (path != other.path) return false

        return true
    }

    /**
     * NOTE: The hashCode must remain constant based upon node ID only.  Ths is due to the
     * fact that the jgrapht persistence layer creates an inserts the node into a `Map` prior to
     * updating it with attribute data.
     */
    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return "VertexInfo(path=$path, attributes=$attributes)"
    }
}

