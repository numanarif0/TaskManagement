# Backend setup (TaskManagement)

This backend uses Spring Boot + SQL Server.

## Quick start

1. Check your SQL Server port:
   - Open SQL Server Configuration Manager
   - TCP/IP Properties → IP Addresses → IPAll → TCP Dynamic Ports (e.g., 58182)
   - Or set static port 1433 in TCP Port field
2. Create database: `CREATE DATABASE taskmanagement;`
3. Enable SQL Server authentication and set sa password to `123456`
4. Update port in `application.properties` if different from 58182
5. Run: `mvn spring-boot:run`

## Shared config

- All settings in `application.properties` (username: sa, password: 123456, port: 58182)
- Everyone uses the same credentials for this homework project
- If your port is different, edit the URL in `application.properties` and commit it
