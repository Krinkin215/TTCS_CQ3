"""
train_model.py
--------------
Huấn luyện mô hình Logistic Regression dự đoán xác suất quên từ vựng.
"""

import os
import pandas
import joblib

from sklearn.model_selection import train_test_split
from sklearn.linear_model   import LogisticRegression
from sklearn.metrics        import accuracy_score, classification_report

# ─────────────────────────────────────────────
# ĐỌC DỮ LIỆU
# ─────────────────────────────────────────────

current_dir = os.path.dirname(os.path.abspath(__file__))
csv_path    = os.path.join(current_dir, "train.csv")
pkl_path    = os.path.join(current_dir, "model.pkl")

print("📂 Đọc dữ liệu từ:", csv_path)
df = pandas.read_csv(csv_path)

# Features (đầu vào) – phải khớp với những gì Java gửi sang Flask
FEATURES = [
    "responseTime",
    "correctCount",
    "incorrectCount",
    "rememberRate",
    "daysSinceLastLearn",
]

X = df[FEATURES]
Y = df["isCorrect"]   # nhãn: 1 = nhớ, 0 = quên

print(f"📊 Tổng mẫu  : {len(df):,}")
print(f"   isCorrect=1: {Y.sum():,}  ({Y.mean()*100:.1f}%)")
print(f"   isCorrect=0: {(1-Y).sum():,}  ({(1-Y).mean()*100:.1f}%)")

# ─────────────────────────────────────────────
# CHIA TRAIN / TEST  (80% / 20%)
# ─────────────────────────────────────────────

X_train, X_test, Y_train, Y_test = train_test_split(
    X, Y, test_size=0.2, random_state=71, stratify=Y
)
# stratify=Y → đảm bảo tỉ lệ nhãn giống nhau ở cả train lẫn test

print(f"\n🔀 Train: {len(X_train):,} mẫu  |  Test: {len(X_test):,} mẫu")

# ─────────────────────────────────────────────
# HUẤN LUYỆN
# ─────────────────────────────────────────────

model = LogisticRegression(max_iter=1000, random_state=71)
model.fit(X_train, Y_train)

# ─────────────────────────────────────────────
# ĐÁNH GIÁ
# ─────────────────────────────────────────────

Y_pred = model.predict(X_test)
acc    = accuracy_score(Y_test, Y_pred)

print(f"\n✅ Accuracy: {acc:.4f}")
print("\n📋 Báo cáo chi tiết:")
print(classification_report(
    Y_test, Y_pred,
    target_names=["Quên (0)", "Nhớ (1)"]
))
# Precision : trong số ca máy đoán là "Quên", bao nhiêu ca thực sự là "Quên"
# Recall    : trong tất cả ca thực sự "Quên", máy tìm ra được bao nhiêu
# F1-score  : trung bình hài hòa Precision và Recall

# ─────────────────────────────────────────────
# LƯU MODEL
# ─────────────────────────────────────────────

joblib.dump(model, pkl_path)
print(f"\n💾 Đã lưu model → {pkl_path}")
