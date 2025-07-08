# Dynamic Logging Configuration API

This document describes the REST API endpoints for dynamically changing the logging configuration of the Spring Boot application.

## Overview

The Logging API allows you to:
- Change the active log file name at runtime
- Modify the log level dynamically
- View current logging configuration
- Generate test log messages for verification

## Endpoints

### 1. Change Log File

**POST** `/v1/logging/change-file`

Changes the active log file name and optionally the log level. New logs will be written to the specified file.

#### Request Body
```json
{
  "logFileName": "new-application.log",
  "logLevel": "DEBUG"
}
```

#### Request Parameters
- `logFileName` (required): The new log file name. Must end with `.log` and contain only alphanumeric characters, dots, underscores, and hyphens.
- `logLevel` (optional): The log level. Valid values: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`. Default: `INFO`.

#### Response
```json
{
  "currentLogFileName": "new-application.log",
  "currentLogLevel": "DEBUG",
  "previousLogFileName": "application.log",
  "changedAt": "2025-01-08T14:30:00",
  "status": "SUCCESS",
  "message": "Log file changed successfully"
}
```

#### Status Codes
- `200 OK`: Log file changed successfully
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: Failed to change log file

### 2. Get Current Configuration

**GET** `/v1/logging/current`

Returns the current logging configuration.

#### Response
```json
{
  "currentLogFileName": "application.log",
  "currentLogLevel": "INFO",
  "previousLogFileName": null,
  "changedAt": "2025-01-08T14:30:00",
  "status": "SUCCESS",
  "message": "Current logging configuration retrieved"
}
```

### 3. Generate Test Logs

**POST** `/v1/logging/test?count=5`

Generates test log messages at different levels for testing purposes.

#### Query Parameters
- `count` (optional): Number of test messages to generate. Default: `5`.

#### Response
```
Generated 5 test log messages at various levels
```

## Usage Examples

### Using curl

1. **Change log file to 'debug.log' with DEBUG level:**
```bash
curl -X POST http://localhost:8085/v1/logging/change-file \
  -H "Content-Type: application/json" \
  -d '{"logFileName": "debug.log", "logLevel": "DEBUG"}'
```

2. **Get current logging configuration:**
```bash
curl -X GET http://localhost:8085/v1/logging/current
```

3. **Generate 10 test log messages:**
```bash
curl -X POST "http://localhost:8085/v1/logging/test?count=10"
```

### Using HTTPie

1. **Change log file:**
```bash
http POST localhost:8085/v1/logging/change-file \
  logFileName="production.log" \
  logLevel="WARN"
```

2. **Get current configuration:**
```bash
http GET localhost:8085/v1/logging/current
```

## Log File Location

Log files are stored in the `logs/` directory relative to the application's working directory. The directory is automatically created if it doesn't exist.

## Log File Format

The log files use the following pattern:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

Example log entry:
```
2025-01-08 14:30:15.123 [http-nio-8085-exec-1] INFO  c.e.p.controller.LoggingController - Log file changed successfully from 'application.log' to 'debug.log'
```

## Rolling Policy

- Log files are rolled daily
- Maximum of 30 historical files are kept
- Each log file can grow up to 10MB before rolling

## Error Handling

The API includes comprehensive error handling:

- **Validation errors**: Returns `400 Bad Request` with field-specific error messages
- **Configuration errors**: Returns `500 Internal Server Error` with detailed error information
- **File system errors**: Logged and returned as configuration errors

## Security Considerations

- File names are validated to prevent directory traversal attacks
- Only `.log` files are allowed
- Log files are restricted to the `logs/` directory
- No sensitive information should be logged at DEBUG or TRACE levels in production

## Integration with Spring Boot Actuator

The logging configuration changes are compatible with Spring Boot Actuator's logging endpoints. You can also use:

- `/actuator/loggers` to view and modify logger levels
- `/actuator/health` to check application health
- `/actuator/metrics` to view logging-related metrics

## Implementation Details

### Service Layer
The `LoggingConfigService` handles the dynamic reconfiguration of Logback appenders:
- Stops existing file appenders
- Creates new rolling file appenders with time-based rolling policy
- Maintains both file and console logging
- Thread-safe operations

### Validation
Request validation ensures:
- Log file names end with `.log` extension
- File names contain only safe characters
- Log levels are valid Logback levels

### Error Recovery
If log file switching fails:
- Previous configuration is maintained
- Error details are logged and returned
- Application continues with existing logging setup