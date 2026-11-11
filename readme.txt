Step 1: Network Configuration Create the shared Docker overlay network for container communication and Swarm replicas:



bash docker network create --driver overlay ds-network

Step 2: Build and Deploy System Deploy the microservices (including Customer Support and WebSocket services), databases, RabbitMQ, and Load Balancer using Docker Stack:


bash docker stack deploy -c docker-compose.yml ems_system

Wait for all service replicas to start. You can check status with: 'docker service ls'.

Step 3: New Assignment 3 Features

Customer Support Chat: Integrated into the frontend, allowing real-time communication between Clients and Admins.




Rule-based Chatbot: The chat automatically responds to common questions using 10+ predefined logic rules.



WebSocket Notifications: Real-time overconsumption alerts are sent from the monitoring pipeline directly to the visualization platform.


Load Balancing: Device simulator traffic is distributed across multiple Monitoring and Communication Microservice replicas.


Step 4: Run Frontend Application Navigate to the frontend directory and start the React application:

bash cd frontendsd1/frontend npm install npm run dev

Access the application at: http://localhost:5173.

Step 5: Stopping the Application To remove the stack and all associated services:

bash docker stack rm ems_system docker network rm ds-network