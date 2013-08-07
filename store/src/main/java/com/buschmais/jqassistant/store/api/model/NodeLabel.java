package com.buschmais.jqassistant.store.api.model;

/**
 * The node labels created by the scanner.
 */
public enum NodeLabel implements org.neo4j.graphdb.Label {
    /**
     * Artifact.
     */
    ARTIFACT,
    /**
     * Package
     */
    PACKAGE,
    /**
     * Type
     */
    TYPE,
    /**
     * Method
     */
    METHOD,
    /**
     * Field
     */
    FIELD;
}
