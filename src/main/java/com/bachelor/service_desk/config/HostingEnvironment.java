package com.bachelor.service_desk.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class HostingEnvironment {

    private HostingEnvironment() {
    }

    public static void configureDatabaseUrl() {
        if (hasText(System.getProperty("spring.datasource.url")) || hasEnv("SPRING_DATASOURCE_URL")) {
            return;
        }

        String databaseUrl = firstText(System.getenv("DATABASE_URL"), System.getenv("JDBC_DATABASE_URL"));
        if (!hasText(databaseUrl)) {
            return;
        }

        if (databaseUrl.startsWith("jdbc:")) {
            System.setProperty("spring.datasource.url", databaseUrl);
            return;
        }

        DatabaseSettings databaseSettings = parseDatabaseUrl(databaseUrl);
        System.setProperty("spring.datasource.url", databaseSettings.url());
        setPropertyIfMissing(
                "spring.datasource.username",
                "SPRING_DATASOURCE_USERNAME",
                "DB_USER",
                databaseSettings.username()
        );
        setPropertyIfMissing(
                "spring.datasource.password",
                "SPRING_DATASOURCE_PASSWORD",
                "DB_PASSWORD",
                databaseSettings.password()
        );
    }

    static DatabaseSettings parseDatabaseUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL must start with postgres://, postgresql:// or jdbc:");
        }

        String databaseName = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
        String query = hasText(uri.getQuery()) ? "?" + uri.getQuery() : "";
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();

        String[] credentials = parseCredentials(uri);
        return new DatabaseSettings(
                "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + databaseName + query,
                credentials[0],
                credentials[1]
        );
    }

    private static String[] parseCredentials(URI uri) {
        String userInfo = uri.getRawUserInfo();
        if (!hasText(userInfo)) {
            return new String[]{"", ""};
        }

        String[] parts = userInfo.split(":", 2);
        String username = decode(parts[0]);
        String password = parts.length > 1 ? decode(parts[1]) : "";
        return new String[]{username, password};
    }

    private static void setPropertyIfMissing(String propertyName, String envName, String legacyEnvName, String value) {
        if (!hasText(value) || hasText(System.getProperty(propertyName)) || hasEnv(envName) || hasEnv(legacyEnvName)) {
            return;
        }

        System.setProperty(propertyName, value);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private static boolean hasEnv(String name) {
        return hasText(System.getenv(name));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    record DatabaseSettings(String url, String username, String password) {
    }
}
