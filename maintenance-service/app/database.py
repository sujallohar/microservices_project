import time
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from app.config import settings

Base = declarative_base()

def get_engine():
    retries = 5
    while retries > 0:
        try:
            engine = create_engine(
                settings.DATABASE_URL,
                pool_pre_ping=True,
                pool_size=10,
                max_overflow=20,
            )
            return engine
        except Exception as e:
            retries -= 1
            print(f"[MaintenanceDB] Connecting to MySQL failed ({e}). Retries left: {retries}")
            time.sleep(3)
    return create_engine(settings.DATABASE_URL)

engine = get_engine()
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
