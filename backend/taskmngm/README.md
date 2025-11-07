# Backend setup (TaskManagement)

This backend uses Spring Boot + SQL Server.

## Quick start

1. Ensure SQL Server is running on localhost:1433
2. Create database: `CREATE DATABASE taskmanagement;`
3. Enable SQL Server authentication and set sa password to `123456`
4. Run: `mvn spring-boot:run`

## Shared config

- All settings in `application.properties` (username: sa, password: 123456, port: 1433)
- Everyone uses the same credentials for this homework project
- If you need different settings, edit `application.properties` locally (don't commit)
