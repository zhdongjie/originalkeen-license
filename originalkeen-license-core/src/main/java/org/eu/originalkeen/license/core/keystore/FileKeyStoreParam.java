package org.eu.originalkeen.license.core.keystore;

import de.schlichtherle.license.AbstractKeyStoreParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * File-based KeyStore parameter implementation.
 * <p>
 * Stores information about the keystore file path, alias, store password, and key password.
 * Supports loading from either the filesystem or the classpath.
 * </p>
 */
public class FileKeyStoreParam extends AbstractKeyStoreParam {

    /**
     * Path to the keystore file (can be classpath or filesystem path)
     */
    private final String storePath;

    /**
     * Alias of the key inside the keystore
     */
    private final String alias;

    /**
     * Password for the keystore
     */
    private final String storePwd;

    /**
     * Password for the key
     */
    private final String keyPwd;

    /**
     * Constructs a FileKeyStoreParam.
     *
     * @param clazz     the class for resolving classpath resources
     * @param resource  the keystore file path (supports "classpath:" prefix)
     * @param alias     key alias
     * @param storePwd  keystore password
     * @param keyPwd    key password
     */
    public FileKeyStoreParam(Class<?> clazz, String resource, String alias, String storePwd, String keyPwd) {
        super(clazz, resource);
        this.storePath = resource;
        this.alias = alias;
        this.storePwd = storePwd;
        this.keyPwd = keyPwd;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String getStorePwd() {
        return storePwd;
    }

    @Override
    public String getKeyPwd() {
        return keyPwd;
    }

    /**
     * Returns an InputStream for the keystore file.
     * <p>
     * Supports "classpath:" prefix to load from classpath. Otherwise loads from filesystem.
     * </p>
     *
     * @return InputStream of the keystore
     * @throws IOException if the file cannot be found or read
     */
    @Override
    public InputStream getStream() throws IOException {
        if (storePath.toLowerCase().startsWith("classpath:")) {
            String resourcePath = storePath.substring("classpath:".length());
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (is == null) {
                // Fallback to current thread's context class loader
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }

            if (is == null) {
                throw new IOException("Keystore file not found in classpath: " + resourcePath);
            }

            return is;
        }

        File file = new File(storePath);
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Keystore file not found or cannot be read: " + storePath);
        }

        return Files.newInputStream(file.toPath());
    }
}
