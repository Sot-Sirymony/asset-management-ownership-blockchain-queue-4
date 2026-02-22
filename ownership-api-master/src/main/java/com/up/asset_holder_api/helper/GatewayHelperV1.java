package com.up.asset_holder_api.helper;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class GatewayHelperV1 {

    private static final String DEFAULT_CHANNEL = "mychannel";
    private static final String DEFAULT_CONN_PROFILE = "/app/connection.yaml";
    /** Use same default as AdminServiceImp so local run (from ownership-api-master) shares one wallet. */
    private static final String DEFAULT_WALLET_DIR = "wallet";

    /** Env var to override /etc/hyperledger/fabric in connection.yaml when API runs on host (e.g. FABRIC_CRYPTO_PATH=$NETWORK_DIR/channel). */
    private static final String ENV_FABRIC_CRYPTO_PATH = "FABRIC_CRYPTO_PATH";
    private static final String DOCKER_CRYPTO_PREFIX = "/etc/hyperledger/fabric";

    private static Path resolveNetworkConfigPath() throws Exception {
        // Prefer mounted file (Docker / K8s)
        String profilePath = System.getenv().getOrDefault("CONNECTION_PROFILE", DEFAULT_CONN_PROFILE);
        Path fsPath = Paths.get(profilePath);

        String content;
        if (Files.exists(fsPath) && Files.isRegularFile(fsPath)) {
            content = Files.readString(fsPath, StandardCharsets.UTF_8);
        } else {
            // Fallback local dev: load from classpath
            try (InputStream in = GatewayHelperV1.class.getClassLoader().getResourceAsStream("connection.yaml")) {
                if (in == null) {
                    throw new IllegalStateException(
                            "connection.yaml not found. Mount it at " + profilePath +
                                    " (recommended) or include it in src/main/resources."
                    );
                }
                content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        // When running on host, connection.yaml has /etc/hyperledger/fabric (Docker path). Replace with local crypto path.
        String cryptoPath = System.getenv().get(ENV_FABRIC_CRYPTO_PATH);
        if (cryptoPath != null && !cryptoPath.isBlank()) {
            String resolved = Paths.get(cryptoPath).toAbsolutePath().normalize().toString();
            content = content.replace(DOCKER_CRYPTO_PREFIX, resolved);
        }

        // Optional: use localhost when API runs on host and peer/orderer are port-mapped (e.g. 7051, 7050).
        String peerUrl = System.getenv().get("FABRIC_PEER_URL");
        if (peerUrl != null && !peerUrl.isBlank()) {
            content = content.replace("grpcs://peer0.org1.ownify.com:7051", peerUrl);
        }
        String ordererUrl = System.getenv().get("FABRIC_ORDERER_URL");
        if (ordererUrl != null && !ordererUrl.isBlank()) {
            content = content.replace("grpcs://orderer.ownify.com:7050", ordererUrl);
        }

        // Ensure connection profile lists the channel name the app will use (e.g. channel-org).
        String channel = System.getenv().get("FABRIC_CHANNEL");
        if (channel != null && !channel.isBlank() && !"mychannel".equals(channel)) {
            content = content.replace("  mychannel:", "  " + channel + ":");
        }

        Path tmp = Files.createTempFile("fabric-connection-", ".yaml");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, content, StandardCharsets.UTF_8);
        return tmp;
    }

    private static Wallet loadWallet() throws Exception {
        String walletDir = System.getenv().getOrDefault("WALLET_PATH", DEFAULT_WALLET_DIR);
        Path walletPath = Paths.get(walletDir).toAbsolutePath().normalize();

        if (!Files.exists(walletPath)) {
            try {
                Files.createDirectories(walletPath);
            } catch (Exception e) {
                throw new IllegalStateException("Wallet path does not exist and could not be created: " + walletPath, e);
            }
        }
        return Wallets.newFileSystemWallet(walletPath);
    }

    public static Gateway connect(String username) throws Exception {
        Wallet wallet = loadWallet();

        if (wallet.get(username) == null) {
            String walletDir = System.getenv().getOrDefault("WALLET_PATH", DEFAULT_WALLET_DIR);
            throw new IllegalArgumentException(
                    "User '" + username + "' does not exist in wallet: " + Paths.get(walletDir).toAbsolutePath()
            );
        }

        Path networkConfigPath = resolveNetworkConfigPath();

        boolean discoveryEnabled = Boolean.parseBoolean(
                System.getenv().getOrDefault("FABRIC_DISCOVERY", "true")
        );

        // IMPORTANT: do NOT rely on localhost inside Docker.
        // This setting is handled by your connection.yaml (it must not contain localhost),
        // but we also keep discovery configurable.
        return Gateway.createBuilder()
                .identity(wallet, username)
                .networkConfig(networkConfigPath)
                .discovery(discoveryEnabled)
                .connect();
    }

    /** Always use channel from env so services never hardcode it */
    public static Network getNetwork(Gateway gateway) {
        String channel = System.getenv().getOrDefault("FABRIC_CHANNEL", DEFAULT_CHANNEL);
        return gateway.getNetwork(channel);
    }
}
