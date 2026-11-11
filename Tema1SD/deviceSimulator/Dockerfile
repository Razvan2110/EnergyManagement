FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Unbuffered output for logging
ENV PYTHONUNBUFFERED=1

CMD ["python", "device_simulator.py"]
