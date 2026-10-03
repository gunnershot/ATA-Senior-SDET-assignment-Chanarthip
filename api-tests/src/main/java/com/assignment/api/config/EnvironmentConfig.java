package com.assignment.api.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({
    "classpath:config/${env}.properties",
    "classpath:config/default.properties"
})
public interface EnvironmentConfig extends Config {

    @Key("base.url")
    @DefaultValue("https://gorest.co.in/public/v2")
    String baseUrl();

    @Key("users.endpoint")
    @DefaultValue("/users")
    String usersEndpoint();
}
