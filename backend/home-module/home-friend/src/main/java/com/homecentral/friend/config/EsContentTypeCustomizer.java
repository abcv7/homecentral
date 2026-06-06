package com.homecentral.friend.config;

import co.elastic.clients.transport.rest5_client.Rest5ClientOptions;
import co.elastic.clients.transport.rest5_client.low_level.RequestOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EsContentTypeCustomizer {

    @Bean
    Rest5ClientOptions rest5ClientOptions() {
        return (Rest5ClientOptions) new Rest5ClientOptions.Builder(RequestOptions.DEFAULT.toBuilder())
            .setHeader("Content-Type", "application/json")
            .setHeader("Accept", "application/json")
            .build();
    }
}
