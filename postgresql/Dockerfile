FROM pgvector/pgvector:0.8.0-pg16

# Copy initialization scripts
COPY postgresql/init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432