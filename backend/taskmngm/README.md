# Backend setup (TaskManagement)

This backend uses Spring Boot + SQL Server.

## Config policy

- Shared config: `src/main/resources/application.properties`
  - Imports `optional:application-local.properties`
  - Shared defaults: `db.host=localhost`, `db.name=taskmanagement`
  - URL uses `${db.port:1433}` so the default port is 1433 unless overridden locally
  - JPA: `ddl-auto=update`, `open-in-view=false`
- Local overrides (not committed): `src/main/resources/application-local.properties`
  - Copy from `application-local.properties.example` and fill in
  - Required: `spring.datasource.username`, `spring.datasource.password`
  - Optional: `db.port` (if not 1433), `db.host`, `db.name`, or a full `spring.datasource.url`
  - Optional: `spring.jpa.properties.hibernate.default_schema=dbo` if your DB uses dbo but main sets a different schema

## Quick start

1. Copy example file:
   - `application-local.properties.example` -> `application-local.properties`
2. Fill:
   - `spring.datasource.username=...`
   - `spring.datasource.password=...`
   - `db.port=...` (only if not 1433)
3. Run the app (e.g., `mvn spring-boot:run`).

## Notes on schema

- If you want to use a custom schema (e.g., `users`), create it in SQL Server (`CREATE SCHEMA users;`) and either:
  - Set `spring.jpa.properties.hibernate.default_schema=users`, or
  - Use `@Table(schema="users")` on entities.
