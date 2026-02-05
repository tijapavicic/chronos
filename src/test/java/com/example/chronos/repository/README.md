# Oracle Database Integration Tests

This directory contains integration tests for the repository layer that execute conditionally when Oracle Database is available.

## Test Classes

### 1. FacilityRepositoryIntegrationTest
**Uses Testcontainers** - Automatically spins up an Oracle XE container for testing.

- **Advantages**: No manual DB setup required, isolated test environment
- **Requirements**: Docker must be running
- **Execution**: Tests automatically start an Oracle container and run against it

### 2. FacilityRepositoryOracleDbTest
**Uses External Oracle DB** - Connects to a manually provisioned Oracle database.

- **Advantages**: Faster execution (no container startup), tests against real Oracle setup
- **Requirements**: Oracle DB must be running and accessible
- **Execution**: Tests connect to existing Oracle instance via environment variables

## Running the Tests

### Prerequisites

Both test approaches require:
- Java 17
- Maven 3.6+
- Docker (for Testcontainers approach)

### Option 1: Using Testcontainers (Recommended)

Ensure Docker is running, then execute:

```bash
# Run all integration tests with Testcontainers
mvn test -Dtest=FacilityRepositoryIntegrationTest

# Or run all tests
mvn test
```

The test will:
1. Automatically pull the Oracle XE image (first run only)
2. Start an Oracle container
3. Execute tests
4. Clean up (container can be reused with `withReuse(true)`)

### Option 2: Using External Oracle Database

**Step 1: Start Oracle Database**

Using Docker:
```bash
docker run -d \
  --name oracle-test \
  -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle \
  gvenzl/oracle-xe:21-slim-faststart
```

**Step 2: Set Environment Variables**

```bash
export ORACLE_TEST_URL="jdbc:oracle:thin:@localhost:1521:xe"
export ORACLE_TEST_USERNAME="system"
export ORACLE_TEST_PASSWORD="oracle"
```

**Step 3: Run Tests**

```bash
mvn test -Dtest=FacilityRepositoryOracleDbTest
```

### Running from IntelliJ IDEA

1. **For Testcontainers**:
   - Right-click on `FacilityRepositoryIntegrationTest`
   - Select "Run 'FacilityRepositoryIntegrationTest'"
   - Ensure Docker is running

2. **For External DB**:
   - Start Oracle DB (see Step 1 above)
   - Edit Run Configuration → Environment Variables → Add:
     - `ORACLE_TEST_URL=jdbc:oracle:thin:@localhost:1521:xe`
     - `ORACLE_TEST_USERNAME=system`
     - `ORACLE_TEST_PASSWORD=oracle`
   - Right-click on `FacilityRepositoryOracleDbTest`
   - Select "Run 'FacilityRepositoryOracleDbTest'"

## Conditional Test Execution

Tests are automatically skipped if Oracle DB is not available:

```
@EnabledIfOracleAvailable - Custom JUnit 5 condition
```

This annotation:
- Checks if Oracle DB is accessible before running tests
- Skips tests gracefully with a clear message if DB is unavailable
- Prevents test failures in CI/CD when Oracle is not configured

### Test Output Examples

**When Oracle is available:**
```
✓ FacilityRepositoryIntegrationTest: All tests executed (15 tests)
```

**When Oracle is unavailable:**
```
⊘ FacilityRepositoryIntegrationTest: Oracle database is not available at jdbc:oracle:thin:@localhost:1521:xe
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ORACLE_TEST_URL` | `jdbc:oracle:thin:@localhost:1521:xe` | JDBC connection URL |
| `ORACLE_TEST_USERNAME` | `system` | Database username |
| `ORACLE_TEST_PASSWORD` | `oracle` | Database password |

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      oracle:
        image: gvenzl/oracle-xe:21-slim-faststart
        env:
          ORACLE_PASSWORD: oracle
        ports:
          - 1521:1521
        options: >-
          --health-cmd "sqlplus -L system/oracle@localhost:1521/XE @healthcheck.sql"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 10

    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Run Integration Tests
        env:
          ORACLE_TEST_URL: jdbc:oracle:thin:@localhost:1521:xe
          ORACLE_TEST_USERNAME: system
          ORACLE_TEST_PASSWORD: oracle
        run: mvn test -Dtest=FacilityRepository*Test
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    
    stages {
        stage('Start Oracle') {
            steps {
                sh '''
                    docker run -d --name oracle-test \
                        -p 1521:1521 \
                        -e ORACLE_PASSWORD=oracle \
                        gvenzl/oracle-xe:21-slim-faststart
                    
                    # Wait for Oracle to be ready
                    sleep 30
                '''
            }
        }
        
        stage('Integration Tests') {
            environment {
                ORACLE_TEST_URL = 'jdbc:oracle:thin:@localhost:1521:xe'
                ORACLE_TEST_USERNAME = 'system'
                ORACLE_TEST_PASSWORD = 'oracle'
            }
            steps {
                sh 'mvn test -Dtest=FacilityRepository*Test'
            }
        }
    }
    
    post {
        always {
            sh 'docker rm -f oracle-test || true'
        }
    }
}
```

## Test Coverage

The integration tests verify:

- ✓ Basic CRUD operations (Create, Read, Update, Delete)
- ✓ Custom query methods (`findByFacilityName`, `findByFacilityType`)
- ✓ UUID generation and persistence
- ✓ Boolean field handling
- ✓ Transaction management and rollback
- ✓ Batch operations
- ✓ Data integrity across transactions
- ✓ Empty result handling
- ✓ Entity relationships and mapping

## Troubleshooting

### Docker Issues

**Problem**: "Cannot connect to Docker daemon"
```bash
# Solution: Start Docker
open -a Docker  # macOS
sudo systemctl start docker  # Linux
```

**Problem**: "Port 1521 already in use"
```bash
# Solution: Stop existing Oracle container or use different port
docker stop oracle-test
docker rm oracle-test
```

### Oracle Connection Issues

**Problem**: "ORA-12541: TNS:no listener"
```bash
# Solution: Wait longer for Oracle to start (can take 30-60 seconds)
docker logs oracle-test  # Check startup logs
```

**Problem**: "ORA-01017: invalid username/password"
```bash
# Solution: Verify credentials match Docker container configuration
echo $ORACLE_TEST_PASSWORD
```

### Test Execution Issues

**Problem**: Tests are always skipped
```bash
# Solution: Verify Oracle is accessible
telnet localhost 1521
# or
nc -zv localhost 1521
```

**Problem**: "java.lang.ClassNotFoundException: oracle.jdbc.OracleDriver"
```bash
# Solution: Rebuild project to ensure dependencies are downloaded
mvn clean install
```

## Best Practices

1. **Use Testcontainers for CI/CD**: Provides consistent, isolated test environment
2. **Use External DB for local development**: Faster test execution during development
3. **Clean up data**: Always use `@BeforeEach` and `@AfterEach` to ensure test isolation
4. **Set realistic timeouts**: Oracle startup can take time, especially first run
5. **Monitor resource usage**: Oracle containers require significant memory (minimum 2GB)

## Additional Resources

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Oracle XE Docker Image](https://hub.docker.com/r/gvenzl/oracle-xe)
- [JUnit 5 Conditional Test Execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-conditional-execution)
- [Spring Data JPA Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.jpa-and-spring-data)
