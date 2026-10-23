FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PORT=4003

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    default-libmysqlclient-dev \
    pkg-config \
    wget \
    && rm -rf /var/lib/apt/lists/*

COPY maintenance-service/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY shared/proto /shared/proto
COPY maintenance-service/app ./app

RUN useradd -m appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 4003

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4003/api/v1/maintenance/health || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "4003"]
