import os
from flask import Flask, request, jsonify
import joblib
import numpy

app = Flask(__name__)

# Dùng đường dẫn tuyệt đối để luôn load đúng model.pkl trong thư mục /ml/
_dir  = os.path.dirname(os.path.abspath(__file__))
model = joblib.load(os.path.join(_dir, "model.pkl"))

@app.route("/predict", methods=["POST"])
def predict():
    data = request.json  # nhận dữ liệu JSON từ SpringBoot

    features = numpy.array([[
        data["responseTime"],
        data["correctCount"],
        data["incorrectCount"],
        data["rememberRate"],
        data["daysSinceLastLearn"]
    ]])

    prob      = model.predict_proba(features)[0]
    p_correct = float(prob[1])
    p_forget  = 1 - p_correct

    return jsonify({        # đóng gói lại thành JSON
        "p_correct": p_correct,
        "p_forget":  p_forget
    })

if __name__ == "__main__":
    app.run(port=5000)