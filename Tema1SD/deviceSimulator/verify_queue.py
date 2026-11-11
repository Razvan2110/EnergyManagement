
import os
import pika
import json
import logging
from dotenv import load_dotenv

load_dotenv()

RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
RABBITMQ_PORT = int(os.getenv('RABBITMQ_PORT', 5672))
RABBITMQ_USER = os.getenv('RABBITMQ_USER', 'guest')
RABBITMQ_PASS = os.getenv('RABBITMQ_PASSWORD', 'guest')
QUEUE_NAME = 'simulator_to_monitoring'

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("QueueVerifier")

def on_message(ch, method, properties, body):
    try:
        message = json.loads(body)
        logger.info(f"SUCCESS! Received message from '{QUEUE_NAME}':")
        logger.info(json.dumps(message, indent=2))
        ch.basic_ack(delivery_tag=method.delivery_tag)
        logger.info("Exiting verification script.")
        ch.stop_consuming()
    except Exception as e:
        logger.error(f"Error decoding message: {e}")

def main():
    logger.info(f"Connecting to RabbitMQ at {RABBITMQ_HOST}:{RABBITMQ_PORT}...")
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials
    )
    
    try:
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()
        
        channel.queue_declare(queue=QUEUE_NAME, durable=True)
        
        queue_state = channel.queue_declare(queue=QUEUE_NAME, durable=True, passive=True)
        message_count = queue_state.method.message_count
        logger.info(f"Queue '{QUEUE_NAME}' currently has {message_count} messages.")

        logger.info("Waiting for the next message...")
        channel.basic_consume(queue=QUEUE_NAME, on_message_callback=on_message)
        
        channel.start_consuming()
        
    except pika.exceptions.AMQPConnectionError:
        logger.error("Could not connect to RabbitMQ. Make sure Docker/RabbitMQ is running.")
    except KeyboardInterrupt:
        pass
    finally:
        if 'connection' in locals() and connection.is_open:
            connection.close()

if __name__ == "__main__":
    main()
