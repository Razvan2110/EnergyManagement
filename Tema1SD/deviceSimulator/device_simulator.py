import os
import threading
import json
import random
import time
import logging
import math
from datetime import datetime
import pika
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Configuration
DEVICE_ID = os.getenv('DEVICE_ID')
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
RABBITMQ_PORT = int(os.getenv('RABBITMQ_PORT', 5672))
RABBITMQ_USER = os.getenv('RABBITMQ_USER', 'guest')
RABBITMQ_PASS = os.getenv('RABBITMQ_PASSWORD', 'guest')
QUEUE_NAME = os.getenv('QUEUE_NAME', 'device_to_simulator')

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("DeviceSimulator")

def simulate_energy_consumption():
   
    current_hour = datetime.now().hour
    current_minute = datetime.now().minute
    
    time_factor = ((current_hour * 60 + current_minute) / (24 * 60)) * 2 * math.pi
    
    base_load = 5.0 
    
    variation = 2.5 * math.sin(time_factor - math.pi/2) 
    
    noise = random.uniform(-0.5, 0.5)
    
    consumption = base_load + variation + noise
    return max(0, round(consumption, 3)) 

def process_message(ch, method, properties, body):
    try:
        message = json.loads(body)
        logger.info(f"Received message: {message}")
        
        received_device_id = message.get("id") or message.get("deviceId")
        
        if str(received_device_id) == str(DEVICE_ID):
           
            consumption = simulate_energy_consumption()
            timestamp = datetime.now().isoformat()
            
            measurement_payload = {
                "timestamp": timestamp,
                "device_id": DEVICE_ID,
                "measurement_value": consumption
            }
            
            logger.info(f"Generated Measurement (Ready to Send): {json.dumps(measurement_payload)}")
            
        else:
            logger.debug(f"Ignoring message for device {received_device_id} (My ID: {DEVICE_ID})")

    except json.JSONDecodeError:
        logger.error(f"Failed to decode JSON: {body}")
    except Exception as e:
        logger.error(f"Error processing message: {e}")


def publish_measurements():
  
    SIMULATOR_QUEUE_NAME = 'simulator_to_monitoring'
    
    
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials,
        heartbeat=600,
        blocked_connection_timeout=300
    )

    while True:
        connection = None
        try:
            logger.info("Publisher thread connecting to RabbitMQ...")
            connection = pika.BlockingConnection(parameters)
            channel = connection.channel()
            channel.queue_declare(queue=SIMULATOR_QUEUE_NAME, durable=True)
            
            logger.info(f"Publisher thread started. Sending to '{SIMULATOR_QUEUE_NAME}' every 15s.")
            
            while True:
                consumption = simulate_energy_consumption()
                timestamp = int(datetime.now().timestamp() * 1000) 
                
                payload = {
                    "timestamp": timestamp,
                    "device_id": str(DEVICE_ID), 
                    "measurement_value": consumption
                }
                
                channel.basic_publish(
                    exchange='',
                    routing_key=SIMULATOR_QUEUE_NAME,
                    body=json.dumps(payload),
                    properties=pika.BasicProperties(
                        delivery_mode=2,
                    ))
                logger.info(f"Sent to {SIMULATOR_QUEUE_NAME}: {payload}")
                time.sleep(10*60)
                
        except pika.exceptions.AMQPConnectionError:
            logger.error("Publisher: Could not connect to RabbitMQ. Retrying in 5s...")
            time.sleep(5)
        except Exception as e:
            logger.error(f"Publisher error: {e}")
            time.sleep(5)
        finally:
            if connection and connection.is_open:
                connection.close()


def main():
    if not DEVICE_ID:
        logger.error("DEVICE_ID is not set in .env file. Exiting.")
        return

    logger.info(f"Starting Device Simulator for Device ID: {DEVICE_ID}")

    publisher_thread = threading.Thread(target=publish_measurements, daemon=True)
    publisher_thread.start()

    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials
    )
    
    try:
        logger.info("Connecting to RabbitMQ Consumer...")
        while True:
            try:
                connection = pika.BlockingConnection(parameters)
                channel = connection.channel()
                
                channel.queue_declare(queue=QUEUE_NAME, durable=True) 

                channel.basic_consume(
                    queue=QUEUE_NAME,
                    on_message_callback=process_message,
                    auto_ack=True
                )

                logger.info(f"Consumer listening in '{QUEUE_NAME}'. To exit press CTRL+C")
                channel.start_consuming()
                break 

            except pika.exceptions.AMQPConnectionError:
                logger.error("Consumer: Could not connect to RabbitMQ. Retrying in 5s...")
                time.sleep(5)
            except Exception as e:
                logger.error(f"Consumer error: {e}. Retrying in 5s...")
                time.sleep(5)

    except KeyboardInterrupt:
        logger.info("Stopping simulator...")
        if 'connection' in locals() and connection.is_open:
            connection.close()

if __name__ == "__main__":
    main()

