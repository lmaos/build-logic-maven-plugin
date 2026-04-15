package com.clmcat.maven.plugins.action;

/**
 * Marks group-style actions that intentionally re-parse their child DSL nodes
 * with a custom action factory at execution time instead of using the default
 * recursive parsing performed by the action factory.
 */
public interface DeferredChildrenParsingAction {
}
