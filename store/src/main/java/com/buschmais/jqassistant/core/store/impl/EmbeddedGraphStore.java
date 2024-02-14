package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.Plugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.embedded.neo4jv4.Neo4jV4ServerFactory;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jDatastoreSession;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.Files.createTempDirectory;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
@Slf4j
public class EmbeddedGraphStore extends AbstractGraphStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedGraphStore.class);

    private static final int AUTOCOMMIT_THRESHOLD = 32678;
    public static final String NEO4J_PLUGIN_DIR_PREFIX = "jqassistant-neo4j-plugins";

    private final EmbeddedNeo4jServerFactory serverFactory;

    private final EmbeddedNeo4jServer server;

    private final Embedded embedded;

    private final ArtifactProvider artifactProvider;

    private Optional<File> neo4jPluginDirectory;

    /**
     * Constructor.
     *
     * @param uri
     *     The store {@link URI}.
     * @param configuration
     *     The configuration.
     * @param storePluginRepository
     *     The {@link StorePluginRepository}.
     */
    public EmbeddedGraphStore(URI uri, com.buschmais.jqassistant.core.store.api.configuration.Store configuration, StorePluginRepository storePluginRepository,
        ArtifactProvider artifactProvider) {
        super(uri, configuration, storePluginRepository);
        this.serverFactory = getEmbeddedNeo4jServerFactory();
        this.server = serverFactory.getServer();
        this.embedded = configuration.embedded();
        this.artifactProvider = artifactProvider;
    }

    public EmbeddedNeo4jServer getServer() {
        return this.server;
    }
    @Override
    protected XOUnit configure(XOUnit.XOUnitBuilder builder) {
        this.neo4jPluginDirectory = resolveNeo4jPlugins();
        Properties properties = serverFactory.getProperties(this.embedded.connectorEnabled(), this.embedded.listenAddress(), this.embedded.boltPort(),
            this.neo4jPluginDirectory);
        builder.properties(properties);
        builder.provider(EmbeddedNeo4jXOProvider.class);
        return builder.build();
    }

    private Optional<File> resolveNeo4jPlugins() {
        List<Plugin> plugins = embedded.neo4jPlugins();
        if (plugins.isEmpty()) {
            return empty();
        }
        log.info("Resolving {} Neo4j plugins.", plugins.size());
        List<File> files = artifactProvider.resolve(plugins);
        File neo4jPluginDirectory = getNeo4jPluginDirectory();
        for (File file : files) {
            try {
                copyFileToDirectory(file, neo4jPluginDirectory);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot copy Neo4j plugins to " + neo4jPluginDirectory, e);
            }
        }
        log.info("Installed {} artifacts into Neo4j plugin directory {}.", files.size(), neo4jPluginDirectory);
        return of(neo4jPluginDirectory);
    }

    @Override
    protected final void cleanup() {
        this.neo4jPluginDirectory.ifPresent(dir -> {
            try {
                deleteDirectory(dir);
            } catch (IOException e) {
                log.warn("Cannot delete Neo4j plugin directory: " + dir, e);
            }
        });
    }

    private static File getNeo4jPluginDirectory() {
        try {
            return createTempDirectory(NEO4J_PLUGIN_DIR_PREFIX).toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create Neo4j plugin directory.", e);
        }
    }

    @Override
    protected final void initialize(XOManagerFactory xoManagerFactory) {
        LOGGER.debug("Initializing embedded Neo4j server {}.", server.getVersion());
        EmbeddedDatastore embeddedDatastore = (EmbeddedDatastore) xoManagerFactory.getDatastore(EmbeddedDatastore.class);
        server.initialize(embeddedDatastore, embedded.listenAddress(), embedded.httpPort(), storePluginRepository.getClassLoader(),
            storePluginRepository.getProcedureTypes(),
            storePluginRepository.getFunctionTypes());
        logVersion(embeddedDatastore);
    }

    private void logVersion(EmbeddedDatastore embeddedDatastore) {
        try (EmbeddedNeo4jDatastoreSession session = embeddedDatastore.createSession()) {
            String neo4jVersion = session.getNeo4jVersion();
            LOGGER.info("Initialized embedded Neo4j database '{}'.", neo4jVersion);
        }
    }

    private EmbeddedNeo4jServerFactory getEmbeddedNeo4jServerFactory() {
        return new Neo4jV4ServerFactory();
    }

    @Override
    protected int getAutocommitThreshold() {
        return AUTOCOMMIT_THRESHOLD;
    }

}
