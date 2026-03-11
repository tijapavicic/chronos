# Generate TrafficLogEven

## 📦 Overview

~~The `com.example.chronos.avro.TrafficLogEvent` class is **automatically generated** by Maven from the Avro schema file. You never need to write this class manually.

---~~

## 🔄 Generation Process

### Input → Output

```
src/main/avro/TrafficLogEvent.avsc (schema)
         ↓
   avro-maven-plugin
         ↓
target/generated-sources/avro/com/example/chronos/avro/TrafficLogEvent.java (generated)
```

---

## 🛠️ Method 1: Command Line (Maven)

### Option A: Generate Sources Only

```bash
cd /Users/copor/IdeaProjects/JavaProjects/chronos
mvn generate-sources
```

This runs the `generate-sources` Maven lifecycle phase, which triggers the `avro-maven-plugin`.

### Option B: Clean and Generate

```bash
mvn clean generate-sources
```

Deletes old generated files first, then regenerates them.

### Option C: Full Compile

```bash
mvn compile
```

The `generate-sources` phase runs automatically before `compile`, so this also generates the Avro classes.

### Option D: Full Build

```bash
mvn clean package
```

Generates sources, compiles, runs tests, and creates the JAR file.

---

## 🔧 Method 2: IntelliJ IDEA (Automatic)

### Enable Auto-Generation on Build

1. **Open Maven Tool Window**
   - View → Tool Windows → Maven
   - Or: Click the **Maven** tab on the right side

2. **Execute Lifecycle Phase**
   - Expand your project in the Maven tool window
   - Expand **Lifecycle**
   - Double-click **generate-sources**
   - The class will be generated

3. **Auto-Mark as Generated Sources**
   - After generation, IntelliJ should automatically mark `target/generated-sources/avro` as a "Generated Sources Root" (blue folder icon)
   - If not, right-click on `target/generated-sources/avro` → **Mark Directory as** → **Generated Sources Root**

### Automatic Generation on Every Build

IntelliJ IDEA automatically runs Maven lifecycle phases when you:

1. **Build Project** (`Cmd+F9` / `Ctrl+F9`)
   - Build → Build Project
   - Maven runs `generate-sources` automatically

2. **Rebuild Project**
   - Build → Rebuild Project
   - Runs `clean` then `generate-sources`

3. **Run Application**
   - When you run the Spring Boot app, IntelliJ compiles first
   - Maven runs `generate-sources` before compilation

### Force Reimport

If the generated class isn't recognized:

1. Right-click on `pom.xml` → **Maven** → **Reload Project**
2. Or: **File** → **Invalidate Caches / Restart**

---

## 🔧 Method 3: IntelliJ IDEA Run Configuration

Create a Maven run configuration for easy one-click generation:

1. **Run** → **Edit Configurations**
2. Click **+** → **Maven**
3. Configure:
   - **Name:** `Generate Avro Sources`
   - **Working directory:** `$ProjectFileDir$`
   - **Command line:** `generate-sources`
4. Click **OK**
5. Now you can click the **Run** button to generate sources anytime

---

## 🔧 Method 4: Eclipse (Automatic)

### Using M2Eclipse Plugin

1. Right-click on project → **Maven** → **Update Project**
2. Check **Force Update of Snapshots/Releases**
3. Click **OK**

Maven automatically runs lifecycle phases on project updates.

### Manual Execution

1. Right-click on project → **Run As** → **Maven build...**
2. Goals: `generate-sources`
3. Click **Run**

---

## 🔧 Method 5: VS Code (Automatic)

### Using Maven for Java Extension

1. Install **Maven for Java** extension (by Microsoft)
2. Open **Maven** panel in the sidebar
3. Expand your project → **Lifecycle**
4. Click **generate-sources**

Or use the integrated terminal:

```bash
mvn generate-sources
```

---

## 🔧 Method 6: CI/CD Pipeline (Automatic)

In your CI/CD configuration (GitHub Actions, Jenkins, GitLab CI, etc.), the build command automatically generates sources:

### GitHub Actions Example

```yaml
- name: Build with Maven
  run: mvn clean package
  # generate-sources runs automatically before package
```

### Jenkins Example

```groovy
stage('Build') {
    steps {
        sh 'mvn clean compile'
        // generate-sources runs automatically before compile
    }
}
```

---

## 📋 Configuration (Already Done in Your Project)

The `pom.xml` already has the `avro-maven-plugin` configured:

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
            <configuration>
                <sourceDirectory>${project.basedir}/src/main/avro</sourceDirectory>
                <outputDirectory>${project.build.directory}/generated-sources/avro</outputDirectory>
                <stringType>String</stringType>
                <dateTimeType>jsr310</dateTimeType>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**This means:**
- ✅ Maven automatically generates classes during `generate-sources` phase
- ✅ Reads all `*.avsc` files from `src/main/avro/`
- ✅ Outputs Java classes to `target/generated-sources/avro/`
- ✅ No manual steps required once `mvn generate-sources` is run

---

## ✅ Verification

After running Maven, check that the file was generated:

```bash
ls -la target/generated-sources/avro/com/example/chronos/avro/TrafficLogEvent.java
```

You should see:
```
-rw-r--r--  1 user  staff  ~30000  Mar 11 14:30 TrafficLogEvent.java
```

---

## 🎯 Recommended Workflow

### First Time Setup

```bash
# 1. Generate sources
mvn generate-sources

# 2. Refresh IDE
# IntelliJ: Maven → Reload Project
# Eclipse: Right-click → Maven → Update Project
# VS Code: Reload window
```

### Daily Development

Just build normally - Maven handles generation automatically:

```bash
mvn compile
# or
mvn package
# or
mvn spring-boot:run
```

### When You Modify the Schema

```bash
# Regenerate after changing TrafficLogEvent.avsc
mvn clean generate-sources
```

---

## 🚫 Never Do This

❌ **Do NOT manually create** `TrafficLogEvent.java`  
❌ **Do NOT edit** generated files in `target/generated-sources/`  
❌ **Do NOT commit** `target/generated-sources/` to Git

Instead:
✅ Commit `src/main/avro/TrafficLogEvent.avsc` (the schema)  
✅ Let Maven generate the Java class  
✅ Add `target/` to `.gitignore`

---

## 🔍 Troubleshooting

### Problem: "Cannot resolve symbol TrafficLogEvent"

**Solution:** Run `mvn generate-sources` then reload your IDE project.

### Problem: Generated class not found by IDE

**Solution:** 
1. Check that `target/generated-sources/avro` is marked as "Generated Sources Root" (blue folder)
2. Right-click → **Mark Directory as** → **Generated Sources Root**

### Problem: Old version of class still in IDE

**Solution:**
```bash
mvn clean generate-sources
# Then reload IDE project
```

---

## 📚 Summary

| Method | Command | When to Use |
|--------|---------|-------------|
| **Maven CLI** | `mvn generate-sources` | First time, or after schema changes |
| **IntelliJ Build** | `Cmd+F9` / Build Project | Daily development (automatic) |
| **IntelliJ Maven** | Maven tool window → generate-sources | Manual control |
| **Full Build** | `mvn compile` or `mvn package` | Automatic during normal builds |
| **Spring Boot Run** | `mvn spring-boot:run` | Automatic before running app |

**Bottom line:** Just run `mvn generate-sources` once, and Maven will automatically regenerate the class whenever you build your project!

---

## 🎓 How It Works Under the Hood

1. Maven reads `pom.xml`
2. Finds `avro-maven-plugin` configured for `generate-sources` phase
3. Plugin scans `src/main/avro/` for `*.avsc` files
4. For each schema, generates a corresponding Java class using Avro's code generator
5. Outputs to `target/generated-sources/avro/com/example/chronos/avro/`
6. Maven adds this directory to the compilation classpath
7. Your code can now import and use `TrafficLogEvent`

**That's it - fully automatic! 🎉**

