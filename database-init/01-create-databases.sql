-- Mikroservisler için ayrı veritabanları oluşturma
-- Database per Service pattern implementasyonu

-- User Management Service Database
CREATE DATABASE user_management_db;
GRANT ALL PRIVILEGES ON DATABASE user_management_db TO postgres;

-- Shipment Service Database  
CREATE DATABASE shipment_db;
GRANT ALL PRIVILEGES ON DATABASE shipment_db TO postgres;

-- Analytics Service Database
CREATE DATABASE analytics_db;
GRANT ALL PRIVILEGES ON DATABASE analytics_db TO postgres;

-- Config Server Database (eğer gerekirse)
CREATE DATABASE config_db;
GRANT ALL PRIVILEGES ON DATABASE config_db TO postgres;

-- Veritabanlarının oluşturulduğunu kontrol et
\l

-- Her veritabanı için temel extension'ları ekle
\c user_management_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c shipment_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c analytics_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c config_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Ana postgres veritabanına geri dön
\c postgres;

-- Oluşturulan veritabanlarını listele
SELECT datname FROM pg_database WHERE datistemplate = false; 