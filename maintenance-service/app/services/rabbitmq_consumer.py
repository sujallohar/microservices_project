import json
import time
import threading
import pika
from app.config import settings
from app.database import SessionLocal
from app.services.maintenance_service import MaintenanceService

QUEUE_NAME = "queue.maintenance.lifecycle"

def publish_event(routing_key: str, payload: dict):
    try:
        credentials = pika.PlainCredentials(settings.RABBITMQ_USER, settings.RABBITMQ_PASS)
        params = pika.ConnectionParameters(
            host=settings.RABBITMQ_HOST,
            port=settings.RABBITMQ_PORT,
            credentials=credentials,
            heartbeat=600
        )
        connection = pika.BlockingConnection(params)
        channel = connection.channel()
        channel.exchange_declare(exchange=settings.RABBITMQ_EXCHANGE, exchange_type='topic', durable=True)
        
        channel.basic_publish(
            exchange=settings.RABBITMQ_EXCHANGE,
            routing_key=routing_key,
            body=json.dumps(payload),
            properties=pika.BasicProperties(
                content_type='application/json',
                delivery_mode=2
            )
        )
        connection.close()
        print(f"[MaintenanceService] Published event [{routing_key}]: {payload.get('eventId', '')}")
    except Exception as e:
        print(f"[MaintenanceService] Error publishing RabbitMQ event: {e}")

def run_consumer():
    while True:
        try:
            print(f"[MaintenanceService] Connecting to RabbitMQ at {settings.RABBITMQ_HOST}:{settings.RABBITMQ_PORT}...")
            credentials = pika.PlainCredentials(settings.RABBITMQ_USER, settings.RABBITMQ_PASS)
            parameters = pika.ConnectionParameters(
                host=settings.RABBITMQ_HOST,
                port=settings.RABBITMQ_PORT,
                credentials=credentials,
                heartbeat=60
            )
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()

            channel.exchange_declare(exchange=settings.RABBITMQ_EXCHANGE, exchange_type='topic', durable=True)
            channel.queue_declare(queue=QUEUE_NAME, durable=True)
            channel.queue_bind(exchange=settings.RABBITMQ_EXCHANGE, queue=QUEUE_NAME, routing_key="asset.registered")

            print(f"[MaintenanceService] RabbitMQ consumer listening on {QUEUE_NAME}...")

            for method_frame, properties, body in channel.consume(QUEUE_NAME):
                try:
                    payload = json.loads(body.decode("utf-8"))
                    print(f"[MaintenanceService] Saga Step 3: Consumed asset.registered event for {payload.get('assetCode')}")

                    # Check failure simulation trigger
                    if payload.get("specifications") and "TRIGGER_MAINTENANCE_FAILURE" in str(payload.get("specifications")):
                        raise ValueError("Maintenance safety compliance rejected: Structural warranty missing.")

                    # Provision MySQL baseline inspection & work order
                    db = SessionLocal()
                    try:
                        MaintenanceService.provision_saga_baseline(db, payload)
                    finally:
                        db.close()

                    # SAGA HAPPY PATH COMPLETE: Publish maintenance.scheduled
                    saga_complete_event = {
                        "eventId": f"evt-maint-{int(time.time()*1000)}",
                        "assetCode": payload.get("assetCode"),
                        "purchaseOrderId": payload.get("purchaseOrderId"),
                        "poNumber": payload.get("poNumber"),
                        "status": "PROVISIONED_AND_COMMISSIONED",
                        "timestamp": int(time.time()*1000)
                    }
                    publish_event("maintenance.scheduled", saga_complete_event)
                    channel.basic_ack(method_frame.delivery_tag)
                    print(f"[MaintenanceService] SAGA STEP 3 COMPLETE! Baseline established for {payload.get('assetCode')}")

                except Exception as ex:
                    print(f"[MaintenanceService] Saga Step 3 Error: {ex}. Triggering Saga Compensation Rollback!")
                    comp_event = {
                        "eventId": f"evt-comp-maint-{int(time.time()*1000)}",
                        "poId": payload.get("purchaseOrderId", ""),
                        "poNumber": payload.get("poNumber", ""),
                        "failedService": "maintenance-service",
                        "reason": str(ex),
                        "timestamp": int(time.time()*1000)
                    }
                    publish_event("maintenance.schedule.failed", comp_event)
                    publish_event("saga.compensate.procurement", comp_event)
                    channel.basic_ack(method_frame.delivery_tag)

        except Exception as e:
            print(f"[MaintenanceService] RabbitMQ consumer connection lost ({e}). Reconnecting in 5s...")
            time.sleep(5)

def start_background_consumer():
    t = threading.Thread(target=run_consumer, daemon=True)
    t.start()
