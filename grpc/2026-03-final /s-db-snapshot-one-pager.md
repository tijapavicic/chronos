# S-DB Snapshot Transfer Approach

## 1) Summary
Our data payload is now above **1,000,000 rows** per transfer cycle.  
At this scale, the payload is approximately **~450 MB per snapshot** 
(working estimate; depends on selected columns and encoding).

Because of this volume and usage pattern, **S-DB** is intentionally designed to 
extract data into a **separate analytics table/store**, rather than serve 
transactional request-response traffic.

## 2) Why S-DB Exists
- **Purpose-built for analytics:** S-DB isolates heavy analytical payloads from operational tables.
- **Performance protection:** Keeps transactional systems stable by offloading large reads.
- **Data usability:** Provides a clean, analysis-ready structure for downstream consumers.

## 3) Communication Model We Need
- We do **not** need real-time synchronous request-response communication.
- We need **event-triggered database snapshot transfer**.
- When the relevant business event occurs, a snapshot is extracted and transferred as a consistent data package.

## 4) Engineering Formulation
We do not need a traditional REST service for this data flow.  
We need a **snapshot extraction pipeline**:

1. Detect event trigger.
2. Extract source data to S-DB analytics structure.
3. Produce a consistent DB snapshot payload.
4. Transfer snapshot to the consuming analytics process/system.

## 5) Decision for This Meeting
Approve an **event-driven snapshot architecture** (not synchronous REST) 
for high-volume analytics payload transfer via S-DB.
