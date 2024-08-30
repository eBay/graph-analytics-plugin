package com.ebay.plugins.graph.analytics.validation.matchers

import com.ebay.plugins.graph.analytics.Attributed
import com.ebay.plugins.graph.analytics.validation.RootedEdge
import com.ebay.plugins.graph.analytics.validation.RootedVertex

/**
 * DSL methods used to formulate graph validation expressions.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused") // This is public API
object GraphMatchers {
    /**
     * Matcher which extracts the specified `String` attribute, delegating the match of the
     * value to the specified matcher.
     */
    @JvmStatic
    fun stringAttribute(key: String, delegate: GraphMatcher<String?>): GraphMatcher<Attributed> {
        return AttributeStringGraphMatcher(key, delegate)
    }

    /**
     * Matcher which extracts the specified `Number` attribute, delegating the match of the
     * value to the specified matcher.
     */
    @JvmStatic
    fun numericAttribute(key: String, delegate: GraphMatcher<Number?>): GraphMatcher<Attributed> {
        return AttributeNumberGraphMatcher(key, delegate)
    }

    /**
     * Matcher which extracts the specified `Boolean` attribute, delegating the match of the
     * value to the specified matcher.
     */
    @JvmStatic
    fun booleanAttribute(key: String, delegate: GraphMatcher<Boolean>): GraphMatcher<Attributed> {
        return AttributeBooleanGraphMatcher(key, delegate)
    }

    /**
     * Matches all outgoing edges (dependencies) of a vertex.
     */
    @JvmStatic
    fun outgoingEdges(delegate: GraphMatcher<in Iterable<RootedEdge>>): GraphMatcher<RootedVertex> {
        return OutgoingEdgesGraphMatcher(delegate)
    }

    /**
     * Matches if there is an outgoing edge (dependency) of a vertex which matches the
     * supplied delegate matcher.
     */
    // Shortcut
    @JvmStatic
    fun hasOutgoingEdge(delegate: GraphMatcher<RootedEdge>): GraphMatcher<RootedVertex> {
        return outgoingEdges(hasItem(delegate))
    }

    /**
     * Matcher which extracts the edge's source vertex, delegating the match of the source
     * vertex to the specified delegate matcher.
     */
    @JvmStatic
    fun edgeSource(delegate: GraphMatcher<in RootedVertex>): GraphMatcher<in RootedEdge> {
        return EdgeSourceGraphMatcher(delegate)
    }

    /**
     * Matcher which extracts the edge's target vertex, delegating the match of the target
     * vertex to the specified delegate matcher.
     */
    @JvmStatic
    fun edgeTarget(delegate: GraphMatcher<in RootedVertex>): GraphMatcher<RootedEdge> {
        return EdgeTargetGraphMatcher(delegate)
    }

    /**
     * Matcher which extracts the vertex's path, delegating the match of the path to the
     * supplied matcher.
     */
    @JvmStatic
    fun path(delegate: GraphMatcher<String>): GraphMatcher<RootedVertex> {
        return VertexPathGraphMatcher(delegate)
    }

    /**
     * Matcher which matches only if any of the delegate rules match the input.
     */
    @JvmStatic
    fun <T> anyOf(vararg delegates: GraphMatcher<in T>): GraphMatcher<T> {
        return AnyOfGraphMatcher(delegates.toList())
    }

    /**
     * Matcher which matches only if all delegate rules match the input.
     */
    @JvmStatic
    fun <T> allOf(vararg delegates: GraphMatcher<in T>): GraphMatcher<T> {
        return AllOfGraphMatcher(delegates.toList())
    }

    /**
     * Matcher which matches only if any one of the items in the input match the delegate matcher.
     */
    @JvmStatic
    fun <T> hasItem(delegate: GraphMatcher<T>): GraphMatcher<Iterable<T>> {
        return HasItemGraphMatcher(delegate)
    }

    /**
     * Matcher which matches only if all of the items in the input match the delegate matcher.
     */
    @JvmStatic
    fun <T> everyItem(delegate: GraphMatcher<T>): GraphMatcher<Iterable<T>> {
        return EveryItemGraphMatcher(delegate)
    }

    /**
     * Matcher which inverts the result of the delegate matcher.
     */
    @JvmStatic
    fun <T> not(delegate: GraphMatcher<T>): GraphMatcher<T> {
        return NotGraphMatcher(delegate)
    }

    /**
     * Matcher which compares the input to the expected value using the equals operator.
     */
    @JvmStatic
    fun <T> equalTo(value: T): GraphMatcher<T> {
        return EqualToGraphMatcher(value)
    }

    /**
     * Matcher which compares the input to the expected value using the less than operator.
     */
    @JvmStatic
    fun <T : Number> lessThan(value: T): GraphMatcher<T?> {
        return LessThanGraphMatcher(value)
    }

    /**
     * Matcher which compares the input to the expected value using the greater than operator.
     */
    @JvmStatic
    fun <T : Number> greaterThan(value: T): GraphMatcher<T?> {
        return GreaterThanGraphMatcher(value)
    }

    /**
     * Matcher which matches an input string against a supplied regular expression.
     */
    @JvmStatic
    fun matchesPattern(value: String): GraphMatcher<String> {
        return MatchesPatternGraphMatcher(value)
    }
}