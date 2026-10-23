import os

class Settings:
    SERVICE_NAME: str = "maintenance-service"
    PORT: int = int(os.getenv("PORT", "4003"))
    
    # MySQL Database URI
    MYSQL_HOST: str = os.getenv("MYSQL_HOST", "localhost")
    MYSQL_PORT: str = os.getenv("MYSQL_PORT", "3307")
    MYSQL_DB: str = os.getenv("MYSQL_DB", "govasset_maintenance")
    MYSQL_USER: str = os.getenv("MYSQL_USER", "maintenance_user")
    MYSQL_PASSWORD: str = os.getenv("MYSQL_PASSWORD", "maintenance_password")

    @property
    def DATABASE_URL(self) -> str:
        return f"mysql+pymysql://{self.MYSQL_USER}:{self.MYSQL_PASSWORD}@{self.MYSQL_HOST}:{self.MYSQL_PORT}/{self.MYSQL_DB}?charset=utf8mb4"

    # RabbitMQ Broker
    RABBITMQ_HOST: str = os.getenv("RABBITMQ_HOST", "localhost")
    RABBITMQ_PORT: int = int(os.getenv("RABBITMQ_PORT", "5672"))
    RABBITMQ_USER: str = os.getenv("RABBITMQ_USER", "guest")
    RABBITMQ_PASS: str = os.getenv("RABBITMQ_PASSWORD", "guest")
    RABBITMQ_EXCHANGE: str = os.getenv("RABBITMQ_EXCHANGE", "govasset.topic.exchange")

    # gRPC internal endpoints
    ASSET_GRPC_HOST: str = os.getenv("ASSET_GRPC_HOST", "localhost")
    ASSET_GRPC_PORT: int = int(os.getenv("ASSET_GRPC_PORT", "9092"))

settings = Settings()
