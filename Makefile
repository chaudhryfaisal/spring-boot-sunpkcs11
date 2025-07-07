# Makefile for Spring Boot PKCS11 Signing Application

# Variables
MAVEN := mvn
JAVA_VERSION := 11
APP_NAME := spring-boot-sunpkcs11
JAR_FILE := target/$(APP_NAME)-*.jar
MAIN_CLASS := com.example.pkcs11.Pkcs11SigningApplication
K6_SCRIPT := k6/sign-loadtest.js

# Default target
.PHONY: help
help: ## Show this help message
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-20s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

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

.PHONY: test-coverage
test-coverage: ## Run tests with coverage report
	$(MAVEN) clean test jacoco:report
	@echo "✅ Test coverage report generated in target/site/jacoco/"

# Quality and analysis targets
.PHONY: lint
lint: ## Run code quality checks
	@echo "Running code quality checks..."
	$(MAVEN) checkstyle:check spotbugs:check || true
	@echo "✅ Code quality checks completed (see reports in target/site/)"

.PHONY: format
format: ## Format code
	$(MAVEN) spotless:apply
	@echo "✅ Code formatting completed"

.PHONY: validate
validate: ## Validate project structure and dependencies
	$(MAVEN) validate dependency:analyze
	@echo "✅ Project validation completed"

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
	java -jar $(shell ls $(JAR_FILE) | head -1)
	@echo "🚀 Application started from JAR"

# Benchmark targets
.PHONY: benchmark-check
benchmark-check: ## Check if k6 is installed
	@which k6 > /dev/null || (echo "❌ k6 is not installed. Please install k6 first." && exit 1)
	@echo "✅ k6 is available"

.PHONY: benchmark-install
benchmark-install: ## Install k6 (requires sudo)
	@echo "Installing k6..."
	@if command -v apt-get > /dev/null; then \
		sudo apt-get update && sudo apt-get install -y k6; \
	elif command -v yum > /dev/null; then \
		sudo yum install -y k6; \
	elif command -v brew > /dev/null; then \
		brew install k6; \
	else \
		echo "❌ Package manager not supported. Please install k6 manually from https://k6.io/docs/getting-started/installation/"; \
		exit 1; \
	fi
	@echo "✅ k6 installed successfully"

.PHONY: benchmark
benchmark: benchmark-check ## Run load tests with k6
	@echo "🔥 Starting load tests..."
	k6 run $(K6_SCRIPT)
	@echo "✅ Load tests completed"

.PHONY: benchmark-smoke
benchmark-smoke: benchmark-check ## Run smoke tests
	@echo "💨 Starting smoke tests..."
	k6 run --vus 1 --duration 30s $(K6_SCRIPT)
	@echo "✅ Smoke tests completed"

.PHONY: benchmark-stress
benchmark-stress: benchmark-check ## Run stress tests
	@echo "⚡ Starting stress tests..."
	k6 run --vus 50 --duration 5m $(K6_SCRIPT)
	@echo "✅ Stress tests completed"

# Development targets
.PHONY: dev-setup
dev-setup: ## Setup development environment
	@echo "🔧 Setting up development environment..."
	$(MAVEN) dependency:resolve
	@echo "✅ Development environment setup completed"

.PHONY: deps
deps: ## Download dependencies
	$(MAVEN) dependency:resolve
	@echo "✅ Dependencies downloaded"

.PHONY: deps-tree
deps-tree: ## Show dependency tree
	$(MAVEN) dependency:tree

.PHONY: deps-updates
deps-updates: ## Check for dependency updates
	$(MAVEN) versions:display-dependency-updates

# Docker targets (if needed)
.PHONY: docker-build
docker-build: build ## Build Docker image
	@if [ -f Dockerfile ]; then \
		docker build -t $(APP_NAME):latest .; \
		echo "✅ Docker image built successfully"; \
	else \
		echo "❌ Dockerfile not found"; \
		exit 1; \
	fi

.PHONY: docker-run
docker-run: ## Run application in Docker
	docker run -p 8080:8080 $(APP_NAME):latest

# CI/CD targets
.PHONY: ci
ci: clean build-with-tests ## Run CI pipeline (clean, build, test)
	@echo "✅ CI pipeline completed successfully"

.PHONY: cd
cd: ci benchmark-smoke ## Run CD pipeline (CI + smoke tests)
	@echo "✅ CD pipeline completed successfully"

# Utility targets
.PHONY: info
info: ## Show project information
	@echo "Project: $(APP_NAME)"
	@echo "Java Version: $(JAVA_VERSION)"
	@echo "Maven Version: $(shell $(MAVEN) --version | head -1)"
	@echo "JAR File: $(JAR_FILE)"
	@echo "Main Class: $(MAIN_CLASS)"

.PHONY: logs
logs: ## Show application logs (if running)
	@echo "📋 Recent application logs:"
	@tail -f logs/application.log 2>/dev/null || echo "No log file found at logs/application.log"

.PHONY: health
health: ## Check application health
	@echo "🏥 Checking application health..."
	@curl -f http://localhost:8080/actuator/health 2>/dev/null || echo "❌ Application is not running or health endpoint not available"

# Quick development workflow
.PHONY: quick-test
quick-test: compile test ## Quick test (compile + unit tests)
	@echo "✅ Quick test completed"

.PHONY: full-check
full-check: clean build-with-tests benchmark-smoke ## Full check (build, test, smoke test)
	@echo "✅ Full check completed"

# Default target when no target is specified
.DEFAULT_GOAL := help