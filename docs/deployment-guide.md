#  Guía de Despliegue – API Skincare

Esta guía describe el proceso completo para desplegar la aplicación en la nube, incluyendo backend, base de datos y frontend.

---

#  Arquitectura del sistema

La aplicación está compuesta por:

- ⚙️ **Backend:** Spring Boot (Java) → Cloud Run  
- 🗄️ **Base de datos:** PostgreSQL → Cloud SQL  
- 🌐 **Frontend:** HTML/CSS/JS → Firebase Hosting  

---

#  1. Configuración de la Base de Datos (Cloud SQL)

##  Crear instancia

1. Ir a Google Cloud Console  
2. Seleccionar **Cloud SQL**  
3. Crear instancia:
   - Motor: PostgreSQL  
   - Nombre: `skincare`  
   - Región: `us-central1`  

---

##  Crear base de datos

```sql
CREATE DATABASE skincare_db;

Creación automática de tablas
spring.jpa.hibernate.ddl-auto=update

2. Configuración del Backend
application.properties
spring.application.name=skincare-api

server.port=${PORT:8080}

spring.datasource.url=jdbc:postgresql://google/skincare_db?cloudSqlInstance=${INSTANCE_CONNECTION_NAME}&socketFactory=com.google.cloud.sql.postgres.SocketFactory
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=2

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true


Dependencia Cloud SQL
<dependency>
    <groupId>com.google.cloud.sql</groupId>
    <artifactId>postgres-socket-factory</artifactId>
    <version>1.15.1</version>
</dependency>

3. Dockerización
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/api-skincare-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

4. Despliegue en Cloud Run
cloudbuild.yaml
steps:
- name: 'gcr.io/cloud-builders/docker'
  args: ['build', '-t', 'us-central1-docker.pkg.dev/api-de-skincare/cloud-run-source-deploy/skincare-api:latest', '-f', 'backend/Dockerfile', './backend']

- name: 'gcr.io/cloud-builders/docker'
  args: ['push', 'us-central1-docker.pkg.dev/api-de-skincare/cloud-run-source-deploy/skincare-api:latest']

- name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
  entrypoint: gcloud
  args:
    [
      'run', 'services', 'update', 'api-skincare-v2',
      '--image=us-central1-docker.pkg.dev/api-de-skincare/cloud-run-source-deploy/skincare-api:latest',
      '--region=us-central1',
      '--add-cloudsql-instances=api-de-skincare:us-central1:skincare',
      '--set-env-vars=DB_NAME=skincare_db,DB_USER=postgres,DB_PASSWORD=TU_PASSWORD,INSTANCE_CONNECTION_NAME=api-de-skincare:us-central1:skincare,PORT=8080',
      '--timeout=600',
      '--cpu=2',
      '--memory=2Gi',
      '--quiet'
    ]

Permisos Cloud SQL
gcloud projects add-iam-policy-binding api-de-skincare \
  --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/cloudsql.client"

Deploy
git add .
git commit -m "Deploy backend"
git push origin main