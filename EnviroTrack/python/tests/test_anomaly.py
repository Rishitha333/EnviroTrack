import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from anomaly import detect_anomaly

NORMAL = [25, 26, 25, 27, 26, 25, 26, 25, 26, 26]


def test_not_enough_data():
    flagged, message = detect_anomaly([25, 26])
    assert flagged is False
    assert message == "Not enough data"


def test_normal_readings_not_flagged():
    flagged, message = detect_anomaly(NORMAL)
    assert flagged is False
    assert message == "Normal"


def test_high_spike_is_flagged():
    flagged, message = detect_anomaly(NORMAL[:-1] + [90])
    assert flagged is True
    assert "Z-score" in message


def test_sudden_drop_is_flagged():
    flagged, message = detect_anomaly(NORMAL[:-1] + [2])
    assert flagged is True


def test_returns_tuple_of_bool_and_text():
    result = detect_anomaly(NORMAL)
    assert isinstance(result, tuple)
    assert isinstance(result[0], bool)
    assert isinstance(result[1], str)