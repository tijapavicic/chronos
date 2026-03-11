# TrafficLogEvent Checker

```java
import com.example.chronos.avro.TrafficLogEvent;
```

You just need to **generate the Avro classes** by running Maven:

### Step 1: Generate Avro Sources

```bash
cd /Users/copor/IdeaProjects/JavaProjects/chronos
mvn generate-sources
```

This will create:
```
target/generated-sources/avro/com/example/chronos/avro/TrafficLogEvent.java
```

### Step 2: Refresh IDE

After Maven generates the sources:

**In IntelliJ IDEA:**
1. Right-click on the project → **Maven** → **Reload Project**
2. Or: File → **Invalidate Caches / Restart**
3. The `target/generated-sources/avro` directory should automatically be marked as "Generated Sources Root" (blue folder)

**Alternative:** Build the entire project:
```bash
mvn clean compile
```

---

## 🔍 Why This Happens

The `avro-maven-plugin` in `pom.xml` is configured to run during the `generate-sources` phase:

```xml
<plugin>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro-maven-plugin</artifactId>
    <version>1.11.3</version>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>schema</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

It reads `src/main/avro/TrafficLogEvent.avsc` and generates Java code.

---

## 🚀 Full Build Command

To generate sources and compile everything:

```bash
mvn clean compile
```

Or to build the full JAR:

```bash
mvn clean package -Dmaven.test.skip=true
```

---

## ✅ Verification

After running Maven, verify the class exists:

```bash
ls -la target/generated-sources/avro/com/example/chronos/avro/TrafficLogEvent.java
```

You should see the generated file with ~900 lines of code.

---

## 📝 Files That Use TrafficLogEvent

1. `src/main/java/com/example/chronos/web/SimulationTrafficLoggingFilter.java`
2. `src/main/java/com/example/chronos/kafka/TrafficLogProducer.java`
3. `src/main/java/com/example/chronos/kafka/TrafficLogConsumer.java`

All imports are correct - just need Maven to generate the class!

