package com.buschmais.jqassistant.core.store.api;

import java.util.Map;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.jqassistant.core.shared.transaction.Transactional;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.xo.api.Example;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.buschmais.xo.api.XOManager;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * Defines the store for {@link Descriptor}s.
 */
public interface Store extends Transactional {

    /**
     * Start the store.
     *
     * This method must be called before any other method of this interface can be
     * used.
     */
    void start();

    /**
     * Stop the store.
     *
     * After calling this method no other method defined within this interface can
     * be called.
     */
    void stop();

    /**
     * Return the initialized {@link XOManager} used by this store.
     *
     * @return The {@link XOManager}.
     */
    XOManager getXOManager();

    /**
     * Clear the content of the store, i.e. delete all nodes and relationships.
     */
    void reset();

    /**
     * Begin a transaction.
     *
     * This method must be called before any write operation is performed.
     */
    void beginTransaction();

    /**
     * Commit a transaction.
     *
     * This method must be called to permanently store the changes of executed write
     * operations.
     */
    void commitTransaction();

    /**
     * Rollback a transaction.
     */
    void rollbackTransaction();

    /**
     * Checks if there is a running transaction or not.
     *
     * @return `true` if there is a running transaction, otherwise `false`.
     */
    boolean hasActiveTransaction();

    /**
     * Flush all pending data.
     *
     * This commits the current transaction and creates a new one.
     */
    void flush();

    /**
     * Creates a {@link Descriptor} of the given type.
     *
     * @param type
     *            The type.
     * @return The {@link Descriptor}.
     */
    <T extends Descriptor> T create(Class<T> type);

    /**
     * Creates a {@link Descriptor} of the given type.
     *
     * @param type
     *            The type.
     * @param example
     *            The example.
     * @return The {@link Descriptor}.
     */
    <T extends Descriptor> T create(Class<T> type, Example<T> example);

    /**
     * Creates a relation between to {@link Descriptor}s.
     *
     * @param source
     *            The source descriptor.
     * @param relationType
     *            The relationt type to create.
     * @param target
     *            The target descriptor.
     * @return The {@link Descriptor}.
     */
    <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target);

    /**
     * Creates a relation between to {@link Descriptor}s.
     *
     * @param source
     *            The source descriptor.
     * @param relationType
     *            The relationt type to create.
     * @param target
     *            The target descriptor.
     * @param example
     *            The example.
     * @return The {@link Descriptor}.
     */
    <S extends Descriptor, R extends Descriptor, T extends Descriptor> R create(S source, Class<R> relationType, T target, Example<R> example);

    /**
     * Creates a {@link Descriptor} of the given type with a full qualified name
     *
     * @param type
     *            The type.
     * @param fullQualifiedName
     *            The full qualified name of the descriptor.
     * @return The
     *         {@link com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor}.
     */
    <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName);

    /**
     * Delete a descriptor.
     *
     * @param descriptor
     *            The descriptor.
     * @param <T>
     *            The descriptor type.
     */
    <T extends Descriptor> void delete(T descriptor);

    /**
     * Add a descriptor type to an existing descriptor.
     *
     * @param descriptor
     *            The descriptor.
     * @param newDescriptorType
     *            The new descriptor type.
     * @param as
     *            The expected return type.
     * @param <T>
     *            The descriptor type.
     * @param <N>
     *            The expected return type.
     * @return The migrated descriptor.
     */
    <T extends Descriptor, N extends Descriptor> N addDescriptorType(T descriptor, Class<?> newDescriptorType, Class<N> as);

    /**
     * Add a descriptor type to an existing descriptor.
     *
     * @param descriptor
     *            The descriptor.
     * @param newDescriptorType
     *            The new descriptor type.
     * @param <T>
     *            The descriptor type.
     * @param <N>
     *            The expected return type.
     * @return The migrated descriptor.
     */
    <T extends Descriptor, N extends Descriptor> N addDescriptorType(T descriptor, Class<N> newDescriptorType);

    /**
     * Remove a descriptor type from an existing descriptor.
     *
     * @param descriptor
     *            The descriptor.
     * @param obsoleteDescriptorType
     *            The new descriptor type.
     * @param as
     *            The expected return type.
     * @param <T>
     *            The descriptor type.
     * @param <N>
     *            The expected return type.
     * @return The migrated descriptor.
     */
    <T extends Descriptor, N extends Descriptor> N removeDescriptorType(T descriptor, Class<?> obsoleteDescriptorType, Class<N> as);

    /**
     * Finds a {@link Descriptor} by an indexed property..
     *
     * @param type
     *            The type.
     * @param value
     *            The full qualified name.
     * @return The {@link Descriptor}.
     */
    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 9)
    <T extends Descriptor> T find(Class<T> type, String value);

    /**
     * Executes a CYPHER query.
     *
     * This method executes a CYPHER query.
     *
     * @param query
     *            The CYPHER query.
     * @param parameters
     *            The {@link java.util.Map} of parameters for the given query.
     * @return The {@link Result}.
     */
    Result<CompositeRowObject> executeQuery(String query, Map<String, Object> parameters);

    /**
     * Executes a CYPHER query.
     *
     * This method executes a CYPHER query.
     *
     * @param query
     *            The CYPHER query.
     * @return The {@link Result}.
     */
    Result<CompositeRowObject> executeQuery(String query);

    /**
     * Executes a typed CYPHER query.
     *
     * This method executes a CYPHER query.
     *
     * @param query
     *            The typed CYPHER query.
     * @param parameters
     *            The {@link java.util.Map} of parameters for the given query.
     * @return The {@link Result}.
     */
    <Q> Result<Q> executeQuery(Class<Q> query, Map<String, Object> parameters);

    /**
     * Get or create a {@link Cache} for the given key.
     *
     * @param cacheKey
     *            The cache key.
     * @param <K>
     *            The key type.
     * @param <V>
     *            The value type.
     * @return The {@link Cache}.
     */
    <K, V extends Descriptor> Cache<K, V> getCache(String cacheKey);

    /**
     * Invalidate the {@link Cache} for the given key.
     *
     * @param cacheKey
     *            The cache key.
     */
    void invalidateCache(String cacheKey);
}
