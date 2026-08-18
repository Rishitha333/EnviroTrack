import numpy as np
from sklearn.linear_model import LinearRegression

def predict_next(values):
    if len(values) < 2:
        return round(values[0], 2) if values else 0.0

    X = np.array(range(len(values))).reshape(-1, 1)
    y = np.array(values)

    model = LinearRegression()
    model.fit(X, y)

    next_index = np.array([[len(values)]])
    predicted = model.predict(next_index)[0]
    return round(float(predicted), 2)