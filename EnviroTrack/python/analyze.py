import pandas as pd
 
from model import predict_next
from anomaly import detect_anomaly
from db_config import get_connection
 
conn = get_connection()

query = "SELECT name, value, timestamp FROM sensors ORDER BY timestamp"
df = pd.read_sql(query, conn)

cursor = conn.cursor()
cursor.execute("DELETE FROM predictions")

for sensor_name in df["name"].unique():
    sensor_df = df[df["name"] == sensor_name].reset_index(drop=True)
    values = sensor_df["value"].tolist()

    predicted = predict_next(values)
    is_anomaly, reason = detect_anomaly(values)

    cursor.execute(
        "INSERT INTO predictions (sensor_name, predicted_value, anomaly_flag, anomaly_reason) VALUES (%s, %s, %s, %s)",
        (sensor_name, predicted, is_anomaly, reason)
    )

conn.commit()
cursor.close()
conn.close()
print("Analysis complete.")