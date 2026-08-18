import time
import random
import math
from datetime import datetime
 
from db_config import get_connection

# ---------- Sensor State (tracks each sensor's current value) ----------
sensor_state = {
    "Temperature Sensor": {"value": 32.0, "type": "temperature", "location": "Server Room"},
    "Humidity Sensor":    {"value": 55.0, "type": "humidity",    "location": "Warehouse"},
    "Air Quality Sensor": {"value": 45.0, "type": "airquality",  "location": "Lab"},
}

# ---------- Realistic value generator ----------
def next_value(sensor_name, state):
    hour = datetime.now().hour
    stype = state["type"]
    current = state["value"]

    if stype == "temperature":
        # Diurnal pattern — cooler at night, warmer midday
        base = 28 + 8 * math.sin((hour - 6) * math.pi / 12)
        drift = random.uniform(-0.8, 0.8)
        # Occasional spike (simulates equipment heat)
        spike = random.choice([0, 0, 0, 0, 0, random.uniform(10, 20)])
        new_val = base + drift + spike
        new_val = max(18.0, min(75.0, new_val))

    elif stype == "humidity":
        # Inversely correlated with temperature — rises at night
        base = 70 - 20 * math.sin((hour - 6) * math.pi / 12)
        drift = random.uniform(-1.5, 1.5)
        spike = random.choice([0, 0, 0, 0, 0, random.uniform(20, 35)])
        new_val = base + drift + spike
        new_val = max(20.0, min(99.0, new_val))

    elif stype == "airquality":
        # Mostly stable, sudden pollution spikes
        base = 40.0
        drift = random.uniform(-2.0, 2.0)
        # Spike happens roughly 1 in 8 readings
        spike = random.choice([0, 0, 0, 0, 0, 0, 0, random.uniform(40, 65)])
        new_val = current * 0.6 + (base + drift + spike) * 0.4
        new_val = max(10.0, min(150.0, new_val))

    state["value"] = round(new_val, 2)
    return state["value"]

# ---------- Insert reading into DB ----------
def insert_reading(conn, name, value, location):
    with conn.cursor() as cursor:
        cursor.execute(
            "INSERT INTO sensors (name, location, value, timestamp) VALUES (%s, %s, %s, NOW())",
            (name, location, value)
        )
    conn.commit()

# ---------- Run anomaly check after every insert ----------
def run_anomaly_check(conn, sensor_name):
    with conn.cursor() as cursor:
        cursor.execute(
            "SELECT value FROM sensors WHERE name=%s ORDER BY timestamp DESC LIMIT 20",
            (sensor_name,)
        )
        rows = cursor.fetchall()

    if len(rows) < 3:
        return

    values = [r[0] for r in rows]
    mean = sum(values) / len(values)
    std = (sum((v - mean) ** 2 for v in values) / len(values)) ** 0.5

    if std == 0:
        return

    latest = values[0]
    z_score = abs((latest - mean) / std)
    is_anomaly = z_score > 2.0
    reason = f"Z-score {round(z_score, 2)} — abnormal reading" if is_anomaly else "Normal"
    predicted = round(mean + (values[0] - values[-1]) / len(values), 2)

    with conn.cursor() as cursor:
        cursor.execute("DELETE FROM predictions WHERE sensor_name=%s", (sensor_name,))
        cursor.execute(
            "INSERT INTO predictions (sensor_name, predicted_value, anomaly_flag, anomaly_reason) VALUES (%s, %s, %s, %s)",
            (sensor_name, predicted, is_anomaly, reason)
        )
    conn.commit()

# ---------- Main loop ----------
print("Simulator started. Press Ctrl+C to stop.")
conn = get_connection()

try:
    while True:
        for sensor_name, state in sensor_state.items():
            value = next_value(sensor_name, state)
            insert_reading(conn, sensor_name, value, state["location"])
            run_anomaly_check(conn, sensor_name)
            print(f"[{datetime.now().strftime('%H:%M:%S')}] {sensor_name} ({state['location']}): {value}")
        time.sleep(5)

except KeyboardInterrupt:
    print("Simulator stopped.")
finally:
    conn.close()