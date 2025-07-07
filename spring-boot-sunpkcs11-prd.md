Here's a **Product Requirements Document (PRD)** for your **Spring Boot + Maven + SunPKCS11 signing service**, followed by a `k6` load testing script.

---

## 📝 Product Requirements Document: PKCS#11 Signing Service

### 1. **Overview**

This service is a Java Spring Boot application using Maven and JDK 11, designed to interact with PKCS#11 hardware or software tokens via SunPKCS11. It provides a REST API to sign arbitrary data using RSA or ECC keys stored in a secure backend provider.

---

### 2. **Goals**

* Securely sign data using RSA/ECC via PKCS#11 tokens.
* Dynamically configure SunPKCS11 provider at runtime using configurable library path.
* Support multiple signing keys with different PINs and labels.
* Provide REST endpoint to sign data.
* Enable performance testing using `k6` scripts.

---

### 3. **Technical Stack**

* **Language:** Java 11
* **Build Tool:** Maven
* **Framework:** Spring Boot
* **Crypto Interface:** SunPKCS11
* **Testing Tool:** k6 (JavaScript)

---

### 4. **Features**

#### 4.1. **PKCS#11 Integration**

* Use SunPKCS11 to connect to backend HSM or SoftHSM.
* Dynamically generate PKCS#11 configuration file at runtime based on:

  * `library path` (shared object `.so` or `.dll`)
  * `slot` or `slotListIndex`
  * `name`
  * `pin`
  * `keyLabel`

#### 4.2. **Dynamic Provider Registration**

* A Spring-managed `@Bean` dynamically builds the SunPKCS11 provider based on app configuration.
* Reusable across requests for performance.
* Caching support for loaded keys (optional).

#### 4.3. **Multiple Key Config Support**

Support multiple configured keys:

```yaml
pkcs11:
  library: /usr/local/lib/softhsm/libsofthsm2.so
  keys:
    - label: my-rsa-key
      pin: 1234
      type: RSA
    - label: my-ecc-key
      pin: 5678
      type: EC
```

#### 4.4. **REST API**

##### POST `/v1/crypto/sign`

* **Request Body:**

```json
{
  "keyLabel": "my-rsa-key",
  "algorithm": "RSA", // or "EC"
  "data": "base64-encoded-input"
}
```

* **Response:**

```json
{
  "signature": "base64-encoded-signature"
}
```

* **Errors:**

  * 404 if key label not found
  * 400 if input invalid
  * 500 for crypto failures

---

### 5. **Non-Functional Requirements**

* **Concurrency:** Handle concurrent signing requests efficiently.
* **Security:**

  * PINs stored securely in config.
  * No PIN exposure in logs.
* **Performance:** Measure throughput and latency via `k6`.

---

### 6. **Deployment**

* Spring Boot packaged as `jar`.
* External configuration via `application.yml` or environment variables.
* Dockerfile (optional).

---

### 7. **Testing**

#### 7.1. Unit Tests

* Signature generation
* Provider registration
* Key loading

#### 7.2. Integration Tests

* REST endpoint test with real/SoftHSM
* Error handling (invalid key, bad data)

#### 7.3. Load Testing (`k6`)

---

## 🧪 k6 Load Test Script

Save as `sign-loadtest.js`:

```js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 10,
  duration: __ENV.DURATION || '30s',
};

const payload = JSON.stringify({
  keyLabel: 'my-rsa-key',
  algorithm: 'RSA',
  data: 'SGVsbG8sIHdvcmxkIQ==' // base64 of "Hello, world!"
});

const params = {
  headers: {
    'Content-Type': 'application/json'
  }
};

export default function () {
  const res = http.post('http://localhost:8080/v1/crypto/sign', payload, params);
  check(res, {
    'status was 200': (r) => r.status === 200,
    'signature present': (r) => JSON.parse(r.body).signature !== undefined,
  });
  sleep(1);
}
```

Run it with:

```bash
k6 run --env VUS=50 --env DURATION=1m sign-loadtest.js
```

---

## 🧩 Maven Dependency Snippet

Add to `pom.xml`:

```xml
<properties>
  <java.version>11</java.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <!-- Add bouncycastle if needed for helper functions -->
</dependencies>
```