package com.ebay.plugins.graph.analytics

import org.jgrapht.nio.Attribute

/**
 * An interface which is added to types which expose graph attributes.  In the base graph,
 * both [VertexInfo] and [EdgeInfo] expose attributes.
 *
 * This indirection allows both to be treated the same in the DSL.
 */
interface Attributed {
    var attributes: MutableMap<String, Attribute>
}