import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from model import predict_next


def test_rising_trend_predicts_next_value():
    assert predict_next([10, 20, 30, 40]) == 50.0


def test_falling_trend_predicts_next_value():
    assert predict_next([10, 8, 6, 4]) == 2.0


def test_constant_readings_predict_same_value():
    assert predict_next([5, 5, 5, 5]) == 5.0


def test_two_values_still_work():
    assert predict_next([1, 3]) == 5.0


def test_single_value_returns_itself():
    assert predict_next([7.5]) == 7.5


def test_empty_list_returns_zero():
    assert predict_next([]) == 0.0


def test_result_is_rounded_float():
    result = predict_next([1.111, 2.222, 3.333])
    assert isinstance(result, float)
    assert result == round(result, 2)