# Quick Reference: Generate TrafficLogEvent

## ⚡ TL;DR

Run this command to auto-generate the class:

```bash
mvn generate-sources
```

Then reload your IDE (IntelliJ: Maven → Reload Project).

---

## 📍 What Gets Generated

**Input (you maintain):**
```
src/main/avro/TrafficLogEvent.avsc
```

**Output (Maven generates):**
```
target/generated-sources/avro/com/example/chronos/avro/TrafficLogEvent.java
```

**Import in your code:**
```java
import com.example.chronos.avro.TrafficLogEvent;
```

---

## 🎯 Three Ways to Generate

### 1. Command Line
```bash
mvn generate-sources
```

### 2. IntelliJ IDEA
- Maven tool window → Lifecycle → double-click **generate-sources**
- Or just build the project (`Cmd+F9`)

### 3. Automatic
```bash
mvn compile        # Runs generate-sources automatically
mvn package        # Runs generate-sources automatically
mvn spring-boot:run  # Runs generate-sources automatically
```

---

## 🔄 When You Need to Regenerate

- ✅ First time cloning the project
- ✅ After changing `TrafficLogEvent.avsc`
- ✅ After `mvn clean` (deletes generated files)
- ✅ When IDE shows "Cannot resolve symbol TrafficLogEvent"

---

## 🎓 How It Works

The `avro-maven-plugin` in `pom.xml` is configured to run during the `generate-sources` Maven phase. It reads `*.avsc` schema files and generates Java classes.

**No manual coding required! 🎉**

---

See **docs/avro-code-generation-guide.md** for detailed instructions.

