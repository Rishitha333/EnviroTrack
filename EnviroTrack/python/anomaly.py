import numpy as np
from scipy import stats

def detect_anomaly(values):
    if len(values) < 3:
        return False, "Not enough data"

    z_scores = np.abs(stats.zscore(values))
    max_z = z_scores[-1]

    if max_z > 2.0:
        return True, f"Z-score {round(float(max_z), 2)} exceeds threshold — abnormal reading"
    return False, "Normal"