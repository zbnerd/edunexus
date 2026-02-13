# Security Fix: Removal of Hardcoded Secrets

**Date:** 2026-02-13
**Issue:** Security-A - Hardcoded Secrets/Keys/Tokens
**Severity:** CRITICAL
**Status:** RESOLVED

## Summary

Removed all hardcoded secrets from configuration files and replaced them with environment variable placeholders. This prevents sensitive credentials from being exposed in version control.

## Files Modified

### 1. Database Passwords (6 files)
All database passwords in `application-dev.yml` files were replaced with `${DB_PASSWORD:}`:

| Service | File | Line | Changed From |
|---------|------|------|--------------|
| attendance-service | `src/main/resources/application-dev.yml` | 19 | `password: nexus` |
| course-service | `src/main/resources/application-dev.yml` | 19 | `password: nexus` |
| enrollment-service | `src/main/resources/application-dev.yml` | 9 | `password: nexus` |
| file-manage-service | `src/main/resources/application-dev.yml` | 14 | `password: nexus` |
| playback-service | `src/main/resources/application-dev.yml` | 10 | `password: nexus` |
| user-service | `src/main/resources/application-dev.yml` | 9 | `password: nexus` |

### 2. JWT Secret (2 files)
JWT secret keys were replaced with `${JWT_SECRET:}`:

| Service | File | Line | Changed From |
|---------|------|------|--------------|
| user-service | `src/main/resources/application-dev.yml` | 41 | `secret: gP1hx!82&fD4z@V9X%YqL#m6kP*o$w3B5E7Jr^N+T2a8ZyC-WxQ#vK@LdFt&R!rt` |
| user-service | `src/main/resources/application-local.yml` | 44 | `secret: gP1hx!82&fD4z@V9X%YqL#m6kP*o$w3B5E7Jr^N+T2a8ZyC-WxQ#vK@LdFt&R!rt` |

### 3. New Files Created
- **`.env.example`** - Template file showing all required environment variables
- **`.gitignore`** - Updated to ignore `.env` files but allow `.env.example`

## Changes Applied

### Before (Vulnerable)
```yaml
spring:
  datasource:
    username: edu
    password: nexus  # HARDCODED SECRET

jwt:
  secret: gP1hx!82&fD4z@V9X%YqL#m6kP*o$w3B5E7Jr^N+T2a8ZyC-WxQ#vK@LdFt&R!rt  # HARDCODED SECRET
```

### After (Secure)
```yaml
spring:
  datasource:
    username: ${DB_USERNAME:edu}
    password: ${DB_PASSWORD:}  # Environment variable

jwt:
  secret: ${JWT_SECRET:}  # Environment variable
```

## Environment Variables Now Required

All developers and deployments must provide the following environment variables:

| Variable | Purpose | Required |
|----------|---------|----------|
| `DB_USERNAME` | Database username | Optional (defaults to `edu`) |
| `DB_PASSWORD` | Database password | **REQUIRED** |
| `JWT_SECRET` | JWT signing secret | **REQUIRED** |

## Setup Instructions

### For Local Development

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Generate secure secrets:
   ```bash
   # Generate DB password
   openssl rand -base64 32

   # Generate JWT secret
   openssl rand -base64 64
   ```

3. Fill in the values in `.env` file

4. **IMPORTANT:** Never commit `.env` to version control

### For Docker Deployment

Add environment variables to your `docker-compose.yml`:
```yaml
services:
  course-service:
    environment:
      - DB_USERNAME=edu
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
```

Or use an `.env` file with docker-compose:
```bash
docker-compose up -d --env-file .env
```

### For Kubernetes

Use Kubernetes Secrets:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: edu-nexus-secrets
type: Opaque
stringData:
  db-password: YOUR_DB_PASSWORD
  jwt-secret: YOUR_JWT_SECRET
```

## Security Best Practices Implemented

1. **Dependency Inversion Principle**: Configuration now depends on environment variable abstractions, not concrete values
2. **Separation of Concerns**: Secrets separated from code
3. **Principle of Least Privilege**: Each environment can have its own secrets
4. **Secure by Default**: Empty defaults force explicit configuration

## Verification

To verify all hardcoded secrets have been removed:
```bash
git grep -nE "(password|secret|token|api_key|apikey)" -- :/**/*.yml :/**/*.yaml | grep -v "\${"
```

Expected results: Only H2 database empty passwords for local testing (which is correct).

## SOLID Principles Applied

- **Dependency Inversion**: High-level modules (services) don't depend on low-level modules (hardcoded secrets). Both depend on abstractions (environment variables).

## Additional Security Recommendations

1. **Production Secrets Management**
   - Use HashiCorp Vault, AWS Secrets Manager, or Azure Key Vault
   - Rotate secrets every 90 days
   - Use different secrets for dev/staging/production

2. **Secret Scanning**
   - Add git-secrets or truffleHog to CI/CD pipeline
   - Scan git history for exposed secrets
   - Monitor for leaked credentials

3. **Audit Trail**
   - Log all secret access attempts
   - Alert on suspicious configuration changes
   - Implement secret versioning

## Related Issues

- Security-B: Dependency vulnerabilities
- Security-C: Input validation and authorization
- Security-D: Error handling and logging

## References

- [OWASP Configuration Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Configuration_Cheat_Sheet.html)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12-Factor App: Config](https://12factor.net/config)
