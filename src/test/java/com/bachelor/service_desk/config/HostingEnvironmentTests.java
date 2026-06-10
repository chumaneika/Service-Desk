package com.bachelor.service_desk.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HostingEnvironmentTests {

    @Test
    void parsesPostgresDatabaseUrlForHosting() {
        HostingEnvironment.DatabaseSettings settings = HostingEnvironment.parseDatabaseUrl(
                "postgres://user%40mail.com:p%40ssword@db.example.com:6543/service_desk?sslmode=require"
        );

        assertThat(settings.url())
                .isEqualTo("jdbc:postgresql://db.example.com:6543/service_desk?sslmode=require");
        assertThat(settings.username()).isEqualTo("user@mail.com");
        assertThat(settings.password()).isEqualTo("p@ssword");
    }

    @Test
    void usesDefaultPostgresPortWhenMissing() {
        HostingEnvironment.DatabaseSettings settings = HostingEnvironment.parseDatabaseUrl(
                "postgresql://user:password@db.example.com/service_desk"
        );

        assertThat(settings.url()).isEqualTo("jdbc:postgresql://db.example.com:5432/service_desk");
        assertThat(settings.username()).isEqualTo("user");
        assertThat(settings.password()).isEqualTo("password");
    }
}
