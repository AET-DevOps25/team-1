FROM python:3.11.9-slim-bullseye AS builder

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

FROM python:3.11.9-slim-bullseye

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy all installed files from builder
COPY --from=builder /usr/local /usr/local

# Copy application code
COPY . .

EXPOSE 8079 8000

CMD ["python", "-m", "app.main"]