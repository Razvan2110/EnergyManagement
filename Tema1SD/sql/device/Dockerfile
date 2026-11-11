# device-db/Dockerfile

FROM mysql:8.0

# Environment variables for MySQL
ENV MYSQL_ROOT_PASSWORD=root123
ENV MYSQL_DATABASE=device_db
ENV MYSQL_USER=device_user
ENV MYSQL_PASSWORD=parola

# Copy the SQL initialization script
COPY init.sql /docker-entrypoint-initdb.d/

# Set proper charset and collation
CMD ["mysqld", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]
