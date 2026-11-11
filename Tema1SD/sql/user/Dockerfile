FROM mysql:8.0

# Environment variables for MySQL
ENV MYSQL_ROOT_PASSWORD=parola
ENV MYSQL_DATABASE=user_db
ENV MYSQL_USER=user_user
ENV MYSQL_PASSWORD=user_pass

# Copy initialization script
COPY init.sql /docker-entrypoint-initdb.d/

# Configure default charset & collation
CMD ["mysqld", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]
