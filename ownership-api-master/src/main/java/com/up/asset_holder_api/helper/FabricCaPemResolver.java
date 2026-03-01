package com.up.asset_holder_api.helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the Fabric CA TLS certificate (pem) file path so it works on both
 * Unix and Windows when the default path (e.g. /etc/hyperledger/fabric/...)
 * does not exist (e.g. on Windows it becomes E:\etc\...).
 * Uses FABRIC_CRYPTO_PATH as fallback to build an OS-correct path.
 */
public final class FabricCaPemResolver {

    private static final String ENV_FABRIC_CRYPTO_PATH = "FABRIC_CRYPTO_PATH";
    private static final String CA_CERT_RELATIVE = "crypto-config/peerOrganizations/org1.ownify.com/users/Admin@org1.ownify.com/tls/ca.crt";

    private FabricCaPemResolver() {
    }

    /**
     * Returns a path to the CA pem file that exists on the filesystem when possible.
     * If the configured path exists, it is returned as-is. Otherwise, if
     * FABRIC_CRYPTO_PATH is set, tries the path under it (with correct OS separators).
     *
     * @param configuredPemFile the path from configuration (e.g. application.properties)
     * @return the same path if it exists, or a resolved path under FABRIC_CRYPTO_PATH if that file exists, otherwise the original
     */
    public static String resolvePemFilePath(String configuredPemFile) {
        if (configuredPemFile == null || configuredPemFile.isBlank()) {
            return configuredPemFile;
        }
        Path configured = Paths.get(configuredPemFile);
        if (Files.exists(configured) && Files.isRegularFile(configured)) {
            return configuredPemFile;
        }
        String cryptoPath = System.getenv().get(ENV_FABRIC_CRYPTO_PATH);
        if (cryptoPath != null && !cryptoPath.isBlank()) {
            Path fallback = Paths.get(cryptoPath, CA_CERT_RELATIVE.split("/"));
            if (Files.exists(fallback) && Files.isRegularFile(fallback)) {
                return fallback.toAbsolutePath().normalize().toString();
            }
        }
        return configuredPemFile;
    }
}
