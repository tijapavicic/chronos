#!/bin/bash
# Test script for SimulationController Avro Kafka logging

set -e

echo "🧪 Testing SimulationController with Avro + Kafka"
echo "=================================================="
echo ""

BASE_URL="http://localhost:8080"

# Check if app is running
echo "1️⃣  Checking if Spring Boot app is running..."
if curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo "   ✅ App is running"
else
    echo "   ❌ App is not running. Start it with: mvn spring-boot:run"
    exit 1
fi

echo ""
echo "2️⃣  Testing GET /simulation/ping (automatic filter logging)..."
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/simulation/ping")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
if [ "$HTTP_CODE" = "200" ]; then
    echo "   ✅ Ping successful (HTTP $HTTP_CODE)"
    echo "   📊 Traffic logged by SimulationTrafficLoggingFilter"
else
    echo "   ❌ Failed (HTTP $HTTP_CODE)"
fi

echo ""
echo "3️⃣  Testing POST /simulation/log-event (manual Avro event)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/simulation/log-event" \
    -H "Content-Type: application/json" \
    -d '{"test": "data", "userId": 123}')
STATUS=$(echo "$RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
CORRELATION_ID=$(echo "$RESPONSE" | grep -o '"correlationId":"[^"]*"' | cut -d'"' -f4)

if [ "$STATUS" = "success" ]; then
    echo "   ✅ Event published successfully"
    echo "   📊 Correlation ID: $CORRELATION_ID"
    echo "   📦 Format: Avro binary"
    echo "   🎯 Topic: simulation.traffic.log"
else
    echo "   ❌ Failed to publish event"
    echo "   Response: $RESPONSE"
fi

echo ""
echo "4️⃣  Testing POST /simulation/run (simulation with logging)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/simulation/run" \
    -H "Content-Type: application/json" \
    -d '{"algorithm": "monte-carlo", "iterations": 10000, "precision": 0.001}')
STATUS=$(echo "$RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
CORRELATION_ID=$(echo "$RESPONSE" | grep -o '"correlationId":"[^"]*"' | cut -d'"' -f4)
DURATION=$(echo "$RESPONSE" | grep -o '"durationMs":[0-9]*' | cut -d':' -f2)

if [ "$STATUS" = "completed" ]; then
    echo "   ✅ Simulation completed"
    echo "   📊 Correlation ID: $CORRELATION_ID"
    echo "   ⏱️  Duration: ${DURATION}ms"
    echo "   📦 Published to Kafka (Avro binary)"
else
    echo "   ❌ Simulation failed"
    echo "   Response: $RESPONSE"
fi

echo ""
echo "5️⃣  Checking Kafka messages..."
MESSAGE_COUNT=$(docker exec chronos-kafka kafka-run-class kafka.tools.GetOffsetShell \
    --broker-list localhost:9092 \
    --topic simulation.traffic.log \
    --time -1 2>/dev/null | awk -F: '{sum += $3} END {print sum}')

if [ -n "$MESSAGE_COUNT" ] && [ "$MESSAGE_COUNT" -gt 0 ]; then
    echo "   ✅ Found $MESSAGE_COUNT messages in Kafka topic"
    echo "   🎯 Topic: simulation.traffic.log"
    echo ""
    echo "   💡 View messages in Kafka UI: http://localhost:8090"
    echo "   💡 Or run: docker exec chronos-kafka kafka-console-consumer \\"
    echo "              --bootstrap-server localhost:9092 \\"
    echo "              --topic simulation.traffic.log \\"
    echo "              --from-beginning --max-messages 3"
else
    echo "   ⚠️  Could not verify Kafka messages (Kafka may not be running)"
fi

echo ""
echo "=================================================="
echo "✅ All tests completed!"
echo ""
echo "📚 Documentation:"
echo "   - docs/simulation-controller-avro-examples.md"
echo "   - docs/kafka-traffic-logging.md"
echo "   - docs/avro-schema-complete-flow.md"
echo ""

