#!/bin/bash
set -euo pipefail

for required_name in AUTH_MYSQL_USERNAME AUTH_MYSQL_PASSWORD USER_PROFILE_MYSQL_USERNAME USER_PROFILE_MYSQL_PASSWORD; do
  required_value="${!required_name:-}"
  if [[ -z "$required_value" || ! "$required_value" =~ ^[A-Za-z0-9_.@%+=:-]+$ ]]; then
    echo "$required_name must be set and contain only safe credential characters" >&2
    exit 1
  fi
done

mysql --protocol=socket -uroot --password="$MYSQL_ROOT_PASSWORD" <<SQL
CREATE DATABASE IF NOT EXISTS authentication_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS user_profile_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '${AUTH_MYSQL_USERNAME}'@'%' IDENTIFIED BY '${AUTH_MYSQL_PASSWORD}';
CREATE USER IF NOT EXISTS '${USER_PROFILE_MYSQL_USERNAME}'@'%' IDENTIFIED BY '${USER_PROFILE_MYSQL_PASSWORD}';
ALTER USER '${AUTH_MYSQL_USERNAME}'@'%' IDENTIFIED BY '${AUTH_MYSQL_PASSWORD}';
ALTER USER '${USER_PROFILE_MYSQL_USERNAME}'@'%' IDENTIFIED BY '${USER_PROFILE_MYSQL_PASSWORD}';
GRANT ALL PRIVILEGES ON authentication_db.* TO '${AUTH_MYSQL_USERNAME}'@'%';
GRANT ALL PRIVILEGES ON user_profile_db.* TO '${USER_PROFILE_MYSQL_USERNAME}'@'%';
FLUSH PRIVILEGES;
SQL
