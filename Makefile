# Makefile for Spring Boot PKCS11 Signing Application

# Variables
MAVEN := mvn
JAVA_VERSION := 11
APP_NAME := spring-boot-sunpkcs11
JAR_FILE := target/$(APP_NAME)-*.jar
MAIN_CLASS := com.example.pkcs11.Pkcs11SigningApplication
K6_SCRIPT := k6/sign-loadtest.js
SOFTHSM2_LIB := $(firstword $(wildcard /usr/lib/softhsm/libsofthsm2.so /usr/lib64/softhsm/libsofthsm.so))
export PKCS11_LIBRARY?=${SOFTHSM2_LIB}
export PKCS11_SLOT?=-1
ENDPOINT_SIGN=http://localhost:8085/v1/crypto/sign
# Default target
.PHONY: help
help: ## Show this help message
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

sign: ## Test singn request with curl
	curl -X POST '${ENDPOINT_SIGN}' -H 'Content-Type: application/json' \
		-d '{"keyLabel":"rsa-2048","algorithm":"RSA","data":"SGVsbG8sIHdvcmxkIQ==","name":"curl_sign"}'
# Clean targets
.PHONY: clean
clean: ## Clean build artifacts
	$(MAVEN) clean
	@echo "✅ Cleaned build artifacts"

.PHONY: clean-all
clean-all: clean ## Clean all artifacts including dependencies
	$(MAVEN) dependency:purge-local-repository
	@echo "✅ Cleaned all artifacts and dependencies"

# Build targets
.PHONY: compile
compile: ## Compile the application
	$(MAVEN) compile
	@echo "✅ Compilation completed"

.PHONY: build
build: ## Build the application (compile + package)
	$(MAVEN) clean package -DskipTests
	@echo "✅ Build completed"

.PHONY: build-with-tests
build-with-tests: ## Build the application with tests
	$(MAVEN) clean package
	@echo "✅ Build with tests completed"

# Test targets
.PHONY: test
test: ## Run unit tests
	$(MAVEN) test
	@echo "✅ Unit tests completed"

.PHONY: test-integration
test-integration: ## Run integration tests
	$(MAVEN) test -Dtest="*IntegrationTest"
	@echo "✅ Integration tests completed"

.PHONY: test-all
test-all: ## Run all tests (unit + integration)
	$(MAVEN) verify
	@echo "✅ All tests completed"

PKCS11SPY_LIB := $(firstword $(wildcard /usr/lib64/pkcs11-spy.so /usr/lib/x86_64-linux-gnu/pkcs11-spy.so))
run-pkcs11-spy: ## Run the application
	 export PKCS11_LIBRARY=${PKCS11SPY_LIB} PKCS11SPY=${SOFTHSM2_LIB}; make run
# Run targets
.PHONY: run
run: ## Run the application
	$(MAVEN) spring-boot:run
	@echo "🚀 Application started"

.PHONY: run-dev
run-dev: ## Run the application with dev profile
	$(MAVEN) spring-boot:run -Dspring-boot.run.profiles=dev
	@echo "🚀 Application started with dev profile"

.PHONY: run-jar
run-jar: build ## Run the application from JAR
	java --add-exports=jdk.crypto.cryptoki/sun.security.pkcs11=ALL-UNNAMED -jar $(shell ls $(JAR_FILE) | head -1)
	@echo "🚀 Application started from JAR"

# Benchmark targets
.PHONY: benchmark-check
benchmark-check: ## Check if k6 is installed
	@which k6 > /dev/null || (echo "❌ k6 is not installed. Please install k6 first." && exit 1)
	@echo "✅ k6 is available"

.PHONY: benchmark-install
benchmark-install: ## Install k6 (requires sudo)
	@echo "Installing k6..."
	which k6 || curl -sSL https://github.com/grafana/k6/releases/download/v1.1.0/k6-v1.1.0-linux-amd64.tar.gz | \
		tar -xzv -C ${HOME}/.local/bin --strip-components=1

benchmark: benchmark-check ## Run load tests with k6
	@echo "🔥 Starting load tests..."
	k6 run $(K6_SCRIPT)
	@echo "✅ Load tests completed"

benchmark-smoke: benchmark-check ## Run smoke tests
	@echo "💨 Starting smoke tests..."
	k6 run --vus 1 --duration 30s $(K6_SCRIPT)
	@echo "✅ Smoke tests completed"

benchmark-stress: benchmark-check ## Run stress tests
	@echo "⚡ Starting stress tests..."
	k6 run --vus 50 --duration 5m $(K6_SCRIPT)
	@echo "✅ Stress tests completed"

health: ## Check application health
	@echo "🏥 Checking application health..."
	@curl -f http://localhost:8085/actuator/health 2>/dev/null || echo "❌ Application is not running or health endpoint not available"
