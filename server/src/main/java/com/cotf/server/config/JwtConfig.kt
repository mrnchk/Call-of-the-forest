package com.cotf.server.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtConfig(
    var secret: String = "",
    var accessExpiration: Long = 900000,
    var refreshExpiration: Long = 604800000
)
