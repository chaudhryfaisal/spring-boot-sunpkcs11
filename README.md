# Spring Boot PKCS#11 Signing Service

A Java Spring Boot application that provides a REST API for signing data using PKCS#11 hardware or software tokens via SunPKCS11.

## Features

- 🔐 **PKCS#11 Integration**: Connect to hardware HSMs or SoftHSM via SunPKCS11
- 🔑 **Multi-Key Support**: Configure multiple signing keys with different PINs
- 🚀 **REST API**: Simple HTTP endpoint for signing operations
- 📊 **Load Testing**: Included k6 scripts for performance testing
- 🛡️ **Security**: Secure PIN handling and comprehensive error handling
- ⚡ **Performance**: Key caching and optimized provider management

## Supported Algorithms

- **RSA**: SHA256withRSA
- **EC**: SHA256withECDSA

## Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- PKCS#11 library (HSM driver or SoftHSM2)

### Configuration

1. **Environment Variables** (recommended for production):
```bash
export PKCS11_LIBRARY=/path/to/your/pkcs11/library.so
export PKCS11_SLOT=0
export RSA_KEY_LABEL=your-rsa-key
export RSA_KEY_PIN=your-pin
export ECC_KEY_LABEL=your-ecc-key
export ECC_KEY_PIN=your-pin
```

2. **Application Configuration** (`application.yml`):
```yaml
pkcs11:
  library: /path/to/pkcs11/library.so
  slot: 0
  provider-name: HSM-Provider
  keys:
    - label: my-rsa-key
      pin: 1234
      type: RSA
    - label: my-ecc-key
      pin: 5678
      type: EC
```

### Running the Application

```bash
# Build the application
mvn clean package

# Run with default profile
java -jar target/spring-boot-sunpkcs11-0.0.1-SNAPSHOT.jar

# Run with development profile (SoftHSM2)
java -jar target/spring-boot-sunpkcs11-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## API Documentation

### Sign Data

**Endpoint**: `POST /v1/crypto/sign`

**Request**:
```json
{
  "keyLabel": "my-rsa-key",
  "algorithm": "RSA",
  "data": "SGVsbG8sIHdvcmxkIQ=="
}
```

**Response**:
```json
{
  "signature": "base64-encoded-signature"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid input data or algorithm
- `404 Not Found`: Key label not found
- `500 Internal Server Error`: Signing operation failed

### Health Check

**Endpoint**: `GET /v1/crypto/health`

**Response**:
```json
{
  "status": "UP",
  "service": "PKCS#11 Signing Service",
  "info": "PKCS#11 Signing Service - Supports RSA and EC algorithms with SHA-256"
}
```

### Service Information

**Endpoint**: `GET /v1/crypto/info`

**Response**:
```json
{
  "service": "PKCS#11 Signing Service",
  "version": "1.0.0",
  "description": "REST API for signing data using PKCS#11 tokens",
  "supportedAlgorithms": ["RSA", "EC"],
  "endpoints": {
    "sign": "POST /v1/crypto/sign",
    "health": "GET /v1/crypto/health",
    "info": "GET /v1/crypto/info"
  }
}
```

## Development Setup

### Using SoftHSM2

1. **Install SoftHSM2**:
```bash
# Ubuntu/Debian
sudo apt-get install softhsm2

# macOS
brew install softhsm

# CentOS/RHEL
sudo yum install softhsm
```

2. **Initialize SoftHSM2**:
```bash
# Initialize token
softhsm2-util --init-token --slot 0 --label "test-token" --pin 1234 --so-pin 5678

# Generate test keys
pkcs11-tool --module /usr/local/lib/softhsm/libsofthsm2.so --login --pin 1234 \
  --keypairgen --key-type rsa:2048 --label test-rsa-key

pkcs11-tool --module /usr/local/lib/softhsm/libsofthsm2.so --login --pin 1234 \
  --keypairgen --key-type EC:secp256r1 --label test-ecc-key
```

3. **Run with development profile**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Load Testing

### Using k6

1. **Install k6**:
```bash
# macOS
brew install k6

# Ubuntu/Debian
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

2. **Run load test**:
```bash
# Basic test
k6 run k6/sign-loadtest.js

# Custom parameters
k6 run --env VUS=50 --env DURATION=1m k6/sign-loadtest.js
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

## Configuration Examples

### Hardware HSM Examples

#### SafeNet Luna HSM
```yaml
pkcs11:
  library: /usr/safenet/lunaclient/lib/libCryptoki2_64.so
  slot: 0
```

#### Thales nShield
```yaml
pkcs11:
  library: /opt/nfast/toolkits/pkcs11/libcknfast.so
  slot: 0
```

#### Utimaco HSM
```yaml
pkcs11:
  library: /etc/utimaco/Software/PKCS11/lib/libcs_pkcs11_R2.so
  slot: 0
```

## Security Considerations

- **PIN Storage**: Store PINs securely using environment variables or encrypted configuration
- **Logging**: PINs and sensitive data are never logged
- **Access Control**: Implement proper authentication/authorization for production use
- **Network Security**: Use HTTPS in production environments
- **Key Management**: Follow your organization's key lifecycle management policies

## Troubleshooting

### Common Issues

1. **Library Not Found**:
   - Verify PKCS#11 library path is correct
   - Check library permissions and dependencies

2. **Slot/Token Issues**:
   - List available slots: `pkcs11-tool --module /path/to/lib --list-slots`
   - Verify token is initialized and accessible

3. **Key Not Found**:
   - List available keys: `pkcs11-tool --module /path/to/lib --list-objects --login --pin YOUR_PIN`
   - Verify key labels match configuration

4. **Authentication Failures**:
   - Verify PIN is correct
   - Check if token is locked due to failed attempts

### Debug Logging

Enable debug logging by setting:
```yaml
logging:
  level:
    com.example.pkcs11: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Check the troubleshooting section
- Review application logs
- Open an issue on GitHub