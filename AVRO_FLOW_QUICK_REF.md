# Avro Schema Flow - Quick Reference

## 🎯 The 5-Step Avro Flow

### 1️⃣ Define Schema (Manual)
```json
// src/main/avro/TrafficLogEvent.avsc
{
  "namespace": "com.example.chronos.avro",
  "name": "TrafficLogEvent",
  "type": "record",
  "fields": [
    {"name": "correlationId", "type": "string"}
  ]
}
```

### 2️⃣ Generate Code (Automatic)
```bash
mvn generate-sources
```
→ Creates: `target/generated-sources/avro/.../TrafficLogEvent.java`

### 3️⃣ Build Object (Your Code)
```java
TrafficLogEvent event = TrafficLogEvent.newBuilder()
    .setCorrelationId("abc-123")
    .setMethod("GET")
    .build();
```

### 4️⃣ Serialize (Producer)
```java
SpecificDatumWriter<TrafficLogEvent> writer = 
    new SpecificDatumWriter<>(TrafficLogEvent.SCHEMA$);
BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
writer.write(event, encoder);
byte[] binary = baos.toByteArray();  // Compact binary!
```

### 5️⃣ Deserialize (Consumer)
```java
SpecificDatumReader<TrafficLogEvent> reader = 
    new SpecificDatumReader<>(TrafficLogEvent.SCHEMA$);
BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(binary, null);
TrafficLogEvent event = reader.read(null, decoder);  // Back to object!
```

---

## 📊 Key Concepts

| Concept | Explanation |
|---------|-------------|
| **Schema** | JSON definition of data structure (`.avsc` file) |
| **SCHEMA$** | Embedded schema constant in generated class |
| **SpecificDatumWriter** | Serializes Java object → Avro binary using schema |
| **SpecificDatumReader** | Deserializes Avro binary → Java object using schema |
| **BinaryEncoder** | Writes data in compact binary format |
| **BinaryDecoder** | Reads data from compact binary format |

---

## 🔄 Data Transformation

```
Java Object ──[Writer + Schema]──> Avro Binary ──[Kafka]──> Avro Binary ──[Reader + Schema]──> Java Object
   (500 bytes)                      (200 bytes)                (200 bytes)                        (500 bytes)
```

**Magic:** The schema lets you convert between human-readable objects and compact binary!

---

## ✨ Why This Works

1. **Schema defines structure** — Both sides know field order
2. **No field names in binary** — Only values, making it compact
3. **Type-safe** — Schema enforces types at compile time
4. **Evolution-friendly** — Add fields without breaking compatibility

---

## 📁 Files in Chronos

| File | Purpose | Created By |
|------|---------|------------|
| `src/main/avro/TrafficLogEvent.avsc` | Schema definition | You (manual) |
| `target/generated-sources/avro/.../TrafficLogEvent.java` | Generated class | Maven (automatic) |
| `kafka/TrafficLogProducer.java` | Serializes & publishes | You (uses generated class) |
| `kafka/TrafficLogConsumer.java` | Consumes & deserializes | You (uses generated class) |

---

## 🎓 Remember

- ✅ **Schema is the source of truth**
- ✅ **Maven generates the Java class**
- ✅ **Same schema for serialize & deserialize**
- ✅ **Binary format = compact & fast**
- ✅ **Schema evolution = add fields safely**

---

See **`docs/avro-schema-complete-flow.md`** for detailed explanation!

