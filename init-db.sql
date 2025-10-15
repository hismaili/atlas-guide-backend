-- Initialize databases for Tours Advisor application
-- This script runs automatically when PostgreSQL container starts

-- Create Keycloak database and user
CREATE DATABASE keycloak_db;
CREATE USER keycloak WITH PASSWORD 'c+cSmosyD5YsRNKIoPpUwcvt4+B2YNhsF4RZ36Bulmg=';
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak;

-- Create application database and user
CREATE DATABASE tours_advisor_db;
CREATE USER app_user WITH PASSWORD 'p2ODO9dOG0lWYu/kQs+GjZ2z8kj5g1C/GUximvC8DDg=';
GRANT ALL PRIVILEGES ON DATABASE tours_advisor_db TO app_user;

-- Connect to tours_advisor_db and set up schema
\c tours_advisor_db;

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Connect to keycloak_db and set up permissions
\c keycloak_db;

GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak;