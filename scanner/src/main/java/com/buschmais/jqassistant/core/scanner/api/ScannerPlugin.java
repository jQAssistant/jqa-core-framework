package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;
import java.lang.annotation.*;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.lifecycle.ContextualConfigurableLifecycleAware;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Defines the interface for a scanner plugin.
 * 
 * @param <I>
 *            The item type accepted by the plugin.
 */
public interface ScannerPlugin<I, D extends Descriptor> extends ContextualConfigurableLifecycleAware<ScannerContext, Map<String, Object>> {

    /**
     * Defines the annotation for specifying a dependency to another plugin to
     * provide an instance of the given descriptor value.
     * 
     * [source,java]
     * ----
     * @Requires(XmlDescriptor.class)
     * public class MyPlugin implements ScannerPlugin&lt;FileResource, MyDescriptor&gt; {
     * 
     * @Requires(XmlDescriptor.class)
     * public class MyPlugin implements ScannerPlugin&lt;FileResource, MyDescriptor&gt; {
     * 
     *     }
     * }
     * ----
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    @interface Requires {

        /**
         * @return The scanner plugins which must be executed first.
         */
        Class<? extends Descriptor>[] value();
    }

    @Override
    default void initialize() {
    }

    @Override
    default void configure(ScannerContext scannerContext, Map<String, Object> properties) {
    }

    @Override
    default void destroy() {
    }

    /**
     * Return the item type accepted by the plugin.
     * 
     * @return The item type.
     */
    Class<? extends I> getType();

    /**
     * Return the descriptor type produced by the plugin.
     *
     * @return The descriptor type.
     */
    Class<D> getDescriptorType();

    /**
     * Determine if the item is accepted by the plugin.
     * 
     * @param item
     *            The item.
     * @param path
     *            The path where the item is located.
     * @param scope
     *            The scope.
     * @return `true` if the plugin accepts the item.
     * @throws IOException
     *             If a problem occurs.
     */
    boolean accepts(I item, String path, Scope scope) throws IOException;

    /**
     * Scan the item.
     * 
     * @param item
     *            The item.
     * @param path
     *            The path where the item is located.
     * @param scope
     *            The scope.
     * @param scanner
     *            The scanner instance to delegate items this plugin resolves from
     *            the given item.
     * @return The {@link Descriptor} instance representing the scanned item.
     * @throws IOException
     *             If a problem occurs.
     */
    D scan(I item, String path, Scope scope, Scanner scanner) throws IOException;

    /**
     * Returns a unique name for the plugin required to identifiy the plugin by its
     * name and to assign configuration settings to it.
     *
     * The name of a plugin must be unique within a jQAssistant and should not be
     * changed if possible. The name of a scanner plugin is used for two purposes:
     *
     * . To identify the plugin in log messages and in other types of messages
     * generated by jQAssistant. . To assign configuration values to the plugin.
     *
     * It is suggested to return the full qualified classname of the plugin to
     * ensure the uniqueness of the name.
     *
     * @return The unique name of the plugin, must not be `null`.
     */
    String getName();

}
