# Spring Boot configuration property loading order (cheat sheet)

This file explains, for developers, the order in which Spring Boot resolves configuration properties (highest precedence first). Use this to decide where to place overrides and how to reliably change values.

Quick checklist (highest -> lowest)

1. Command-line arguments (java -jar app.jar --my.prop=value)
2. SPRING_APPLICATION_JSON (system property or environment variable containing JSON)
3. ServletConfig init parameters (when running in a servlet container)
4. ServletContext init parameters
5. JNDI attributes (java:comp/env) — when available
6. Java System properties (-Dmy.prop=value)
7. OS environment variables (e.g., export MY_PROP=value)
8. RandomValuePropertySource (random.*)
9. Profile-specific application properties outside the jar (file:./config/, file:./, classpath:/config/, classpath:/) — application-{profile}.properties/yaml
10. Non-profile application properties in the same external/classpath locations (application.properties/yaml)
11. @PropertySource annotations on @Configuration classes
12. Default properties (SpringApplication.setDefaultProperties(...))

Notes and details

- Command line args are the single most powerful immediate override for quick local testing or scripts:
  java -jar build/libs/app.jar --server.port=9090 --my.feature.enabled=true

- SPRING_APPLICATION_JSON can be used to pass multiple properties in JSON form (useful in certain container/orchestration environments):
  export SPRING_APPLICATION_JSON='{"my":{"prop":"value"}}'

- System properties (java -D) take precedence over environment variables. Environment variables are normalized to property names (e.g., MY_PROP or MY__PROP -> my.prop depending on rules).

- Spring Boot 2.4+ uses the Config Data API. It searches for config files in the following locations (external locations have priority over classpath):
  * file:./config/
  * file:./
  * classpath:/config/
  * classpath:/

  For each location, profile-specific files (application-{profile}.properties or .yaml) are considered first and override non-profile application.properties in the same location.

- application.properties files packaged inside the JAR (classpath:/application.properties) are low precedence compared to external files in ./config or the working directory.

- @PropertySource on a @Configuration class adds a PropertySource with lower precedence than the files above (and higher than default properties).

- SpringApplication.setDefaultProperties(...) sets fallbacks and is the lowest-precedence source developers commonly use.

Runtime changes vs static binding

- The Environment is a dynamic holder of PropertySources: you can programmatically add a PropertySource at runtime and env.getProperty(...) will return the new value immediately.
  Example (programmatic override):

```java
ConfigurableEnvironment env = (ConfigurableEnvironment) applicationContext.getEnvironment();
MutablePropertySources sources = env.getPropertySources();
Map<String, Object> map = new HashMap<>();
map.put("my.feature.enabled", "true");
sources.addFirst(new MapPropertySource("runtimeOverrides", map));
```

  However: values previously injected into beans via @Value or constructor arguments are not magically re-injected. Use one of the supported patterns for refreshable configuration (see below).

Patterns for dynamic/refreshable properties

- Spring Cloud Config + @RefreshScope: annotate beans you want recreated on refresh with @RefreshScope, then trigger a refresh (previously /actuator/refresh or via the Spring Cloud Bus). This is the common approach for production runtime updates.

- @ConfigurationProperties + manual rebind: you can rebind a configuration properties bean using the binder/ConfigurationPropertiesBinding infrastructure; this is more manual but avoids bringing in the full refresh stack.

- Query a runtime-backed store: keep dynamic settings in a DB or a feature-flags service and read them at runtime (or cache + watch for changes).

Testing conveniences

- For unit/integration tests you can override properties easily:
  - @TestPropertySource
  - @SpringBootTest(properties = {"my.prop=value"})
  - System.setProperty("my.prop","value")
  - @DynamicPropertySource (JUnit 5) for programmatic property wiring

Examples / recipes

- Override with environment variable (shell):

```bash
export VXX=dev
# use in application.properties as: my.env.value=${VXX:default}
```

- Override with command-line (highest priority):

```bash
java -jar chronos.jar --spring.profiles.active=dev --management.endpoints.web.exposure.include=health,info
```

- Programmatically add a runtime override (example above) — useful for tests or management endpoints that mutate the Environment. Remember this does not rebind @Value-injected fields.

Pitfalls and "things that require restart"

- server.port and other servlet container wiring values are applied at startup and generally require an application restart to change.
- Data source connection configuration (URL, driver, credentials) often requires pool recreation or restart to apply safely.
- Many auto-configuration decisions are made at startup; changing properties later may not affect already-created beans.

Short reference: highest -> lowest (condensed)

1. Command line args
2. SPRING_APPLICATION_JSON
3. Servlet/ServletContext init params
4. JNDI
5. Java System properties (-D)
6. OS env variables
7. RandomValuePropertySource
8. External config files (file:./config/, file:./)
9. Classpath config (classpath:/config/, classpath:/)
10. @PropertySource
11. SpringApplication default properties

-----

### conclusion

Environment value already set in pipeline will take precedence over any property set in project source code.

On Dev environment exact env value exists in env variable.
Pipeline should read this value when setting spring profile instead current value from the file.
They have to pick from system environment variables or command line arguments