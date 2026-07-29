FROM postgis/postgis:16-3.5

# Instalamos gdal-bin y además las librerías de cliente de postgres para gdal
RUN apt-get update && \
    apt-get install -y gdal-bin libgdal-dev postgresql-client && \
    rm -rf /var/lib/apt/lists/*