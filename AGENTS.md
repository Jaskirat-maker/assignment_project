# AGENTS.md

## Cursor Cloud specific instructions

### Project overview
This is a Spring Boot 3.2.3 Finance Dashboard REST API (Java 17 source level, JDK 21 compatible). The project lives in `finance-dashboard/` and uses Maven for builds. See `finance-dashboard/README.md` for feature details and API endpoints.

### Prerequisites (system dependencies)
- **JDK 21** — pre-installed on the VM
- **Maven 3.8+** — install via `sudo apt-get install -y maven`
- **MySQL 8.0** — install via `sudo apt-get install -y mysql-server mysql-client`

### Starting MySQL
```
sudo service mysql start
sudo chmod 755 /var/run/mysqld/
```
The app connects to `localhost:3306` as `root` / `your_password` (configured in `application.properties`). To set the password on a fresh MySQL install:
```
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_password'; FLUSH PRIVILEGES;"
```

### Build, test, and run
All commands from `finance-dashboard/`:
- **Compile:** `mvn clean compile`
- **Tests:** `mvn test` (35 tests; unit tests use H2 via `src/test/resources/application.properties`)
- **Run:** `mvn spring-boot:run` (starts on port 8080)
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **Health check:** `http://localhost:8080/actuator/health`

### Key gotchas discovered during setup
- The original codebase had several compilation/test issues that required fixes (missing `ModelMapper` dependency, missing `@Builder` annotations on DTOs, duplicate bean definitions, missing imports). These are now fixed in the codebase.
- `@WebMvcTest` controller tests require `@Import(SecurityConfig.class)` and `@MockBean` for `JwtTokenProvider` and `CustomUserDetailsService` to load the security context correctly.
- The `JwtAuthenticationFilter` must inject `CustomUserDetailsService` (not the `UserDetailsService` interface) to avoid `NoUniqueBeanDefinitionException` when Spring Security's `UserDetailsServiceAutoConfiguration` creates an `inMemoryUserDetailsManager`.
- `RefreshTokenServiceImpl.createRefreshToken()` must `flush()` after `deleteByUser()` before inserting a new token to avoid unique constraint violations on the `user_id` column.
- The `FinancialRecordController` CRUD endpoints allow both `ANALYST` and `ADMIN` roles (aligned with README spec). Default registered users get `ANALYST` role.
