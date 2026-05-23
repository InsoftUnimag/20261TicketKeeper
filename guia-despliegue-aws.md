# Guía de Despliegue en AWS — Microservicios Spring Boot + R2DBC
> Proyecto: 3 módulos en cuentas AWS independientes con conectividad cross-account

---

## Tabla de Contenidos

1. [Arquitectura General](#1-arquitectura-general)
2. [Prerrequisitos](#2-prerrequisitos)
3. [Paso 1 — Preparar la imagen Docker en ECR](#3-paso-1--preparar-la-imagen-docker-en-ecr)
4. [Paso 2 — Desplegar la base de datos con RDS](#4-paso-2--desplegar-la-base-de-datos-con-rds)
5. [Paso 3 — Crear el clúster ECS Fargate](#5-paso-3--crear-el-cluster-ecs-fargate)
6. [Paso 4 — Definir la Task Definition y el Servicio ECS](#6-paso-4--definir-la-task-definition-y-el-servicio-ecs)
7. [Paso 5 — Configurar el Application Load Balancer (ALB)](#7-paso-5--configurar-el-application-load-balancer-alb)
8. [Paso 6 — Conectividad entre módulos (cross-account)](#8-paso-6--conectividad-entre-módulos-cross-account)
9. [Paso 7 — Configurar variables de entorno entre módulos](#9-paso-7--configurar-variables-de-entorno-entre-módulos)
10. [Paso 8 — Despliegue de correo con Amazon SES](#10-paso-8--despliegue-de-correo-con-amazon-ses)
11. [Paso 9 — Verificación final](#11-paso-9--verificación-final)
12. [Resumen de URLs y variables por módulo](#12-resumen-de-urls-y-variables-por-módulo)
13. [Comandos de referencia rápida](#13-comandos-de-referencia-rápida)

---

## 1. Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│  CUENTA AWS — MÓDULO 1                                          │
│                                                                 │
│   Internet ──► ALB (público) ──► ECS Fargate (módulo-1:8080)   │
│                                        │                        │
│                                   RDS PostgreSQL (módulo 1)     │
│                                                                 │
│   ALB URL pública: https://modulo1.ejemplo.com                  │
└─────────────────────────────────────────────────────────────────┘
          ▲  GET /...            ▲  GET /...
          │                     │
┌─────────┴───────────┐   ┌─────┴───────────────────────────────┐
│  CUENTA AWS — MOD 2 │   │  CUENTA AWS — MÓDULO 3              │
│                     │   │                                      │
│  ALB ──► ECS (mod2) │   │  ALB ──► ECS (mod3)                 │
│          │          │   │          │                           │
│        RDS (mod2)   │   │        RDS (mod3)                   │
│                     │   │                                      │
│  Llama a: módulo 1  │   │  Llama a: módulo 1 y módulo 2       │
└─────────────────────┘   └──────────────────────────────────────┘
          ▲  GET /...
          │
   (módulo 3 también llama a módulo 2)
```

**Flujo de comunicación:**
- Módulo 1 → Solo **recibe** peticiones GET del módulo 2 y 3
- Módulo 2 → **Llama** al módulo 1 y **recibe** peticiones GET del módulo 3
- Módulo 3 → **Llama** al módulo 1 y al módulo 2

**Estrategia de conectividad cross-account:**
Cada módulo expone un **Application Load Balancer (ALB) público con HTTPS**. Los otros módulos se comunican a través de estas URLs públicas. Es la estrategia más simple para equipos separados en cuentas distintas.

> **¿Por qué ALB público?** VPC Peering cross-account requiere coordinación de rangos IP entre los 3 equipos y configuración de tablas de rutas en ambas cuentas. Con ALBs públicos + HTTPS, cada equipo es independiente y solo necesita compartir su URL.

---

## 2. Prerrequisitos

Cada equipo necesita lo siguiente antes de comenzar:

### Herramientas locales

```bash
# Instalar AWS CLI v2
# Linux/Mac:
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip && sudo ./aws/install

# Verificar instalación
aws --version   # aws-cli/2.x.x

# Instalar Docker
docker --version  # Docker version 24.x
```

### Configurar credenciales AWS

```bash
aws configure
# AWS Access Key ID:     <tu-access-key>
# AWS Secret Access Key: <tu-secret-key>
# Default region:        us-east-1        ← usa la misma región en todo el equipo
# Default output format: json
```

> Obtén las credenciales desde: AWS Console → IAM → Users → tu usuario → Security credentials → Create access key

### Variables que debes definir antes de empezar

Reemplaza estos valores en todos los comandos de esta guía:

| Variable | Descripción | Ejemplo |
|---|---|---|
| `<AWS_ACCOUNT_ID>` | ID de tu cuenta AWS (12 dígitos) | `123456789012` |
| `<REGION>` | Región AWS a usar | `us-east-1` |
| `<MODULO>` | Nombre de tu módulo | `modulo-1` |
| `<DB_PASSWORD>` | Contraseña segura para RDS | `MiPassword123!` |

---

## 3. Paso 1 — Preparar la imagen Docker en ECR

**ECR (Elastic Container Registry)** es el repositorio privado de Docker en AWS. Cada módulo sube su imagen aquí para que ECS pueda descargarla al desplegar.

### 3.1 Crear el repositorio ECR

```bash
aws ecr create-repository \
  --repository-name <MODULO> \
  --region <REGION>
```

La respuesta incluye el `repositoryUri`, que tiene el formato:
```
<AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>
```

Guarda ese URI, lo usarás en todos los pasos siguientes.

### 3.2 Autenticarse en ECR

```bash
aws ecr get-login-password --region <REGION> | \
  docker login --username AWS --password-stdin \
  <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com
```

### 3.3 Construir y subir la imagen

Ejecuta esto desde la raíz del proyecto (donde está el `Dockerfile`):

```bash
# Construir la imagen (igual que en local)
docker build -t <MODULO> .

# Etiquetar para ECR
docker tag <MODULO>:latest \
  <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest

# Subir a ECR
docker push \
  <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest
```

> **Tip:** Cada vez que hagas cambios en el código, repite el `docker build`, `docker tag` y `docker push`. Puedes usar tags de versión como `:v1.0`, `:v1.1` en lugar de `:latest` para llevar control de versiones.

### 3.4 Verificar que la imagen está en ECR

```bash
aws ecr list-images \
  --repository-name <MODULO> \
  --region <REGION>
```

---

## 4. Paso 2 — Desplegar la base de datos con RDS

**RDS (Relational Database Service)** es el servicio administrado de PostgreSQL en AWS. Cada módulo despliega su propia instancia independiente.

### 4.1 Crear un Security Group para RDS

Un Security Group actúa como un firewall virtual. El de RDS solo debe aceptar conexiones desde ECS.

```bash
# Primero, obtén el ID del VPC por defecto de tu cuenta
VPC_ID=$(aws ec2 describe-vpcs \
  --filters "Name=isDefault,Values=true" \
  --query "Vpcs[0].VpcId" \
  --output text \
  --region <REGION>)

echo "VPC ID: $VPC_ID"

# Crear Security Group para RDS
SG_RDS=$(aws ec2 create-security-group \
  --group-name sg-rds-<MODULO> \
  --description "Security Group para RDS del modulo" \
  --vpc-id $VPC_ID \
  --region <REGION> \
  --query "GroupId" \
  --output text)

echo "Security Group RDS: $SG_RDS"
```

> No agregues reglas de entrada al Security Group de RDS todavía — lo harás en el Paso 3 cuando tengas el Security Group de ECS.

### 4.2 Crear el Subnet Group de RDS

RDS necesita estar en al menos 2 zonas de disponibilidad (requisito de AWS):

```bash
# Obtener las subnets del VPC
aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query "Subnets[*].SubnetId" \
  --output text \
  --region <REGION>
```

Copia los IDs de las subnets (mínimo 2 de zonas distintas) y crea el grupo:

```bash
aws rds create-db-subnet-group \
  --db-subnet-group-name subnet-group-<MODULO> \
  --db-subnet-group-description "Subnet group para <MODULO>" \
  --subnet-ids subnet-AAAAAAAA subnet-BBBBBBBB \
  --region <REGION>
```

### 4.3 Crear la instancia RDS PostgreSQL

```bash
aws rds create-db-instance \
  --db-instance-identifier rds-<MODULO> \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 16.3 \
  --db-name ticketseller \
  --master-username postgres \
  --master-user-password <DB_PASSWORD> \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids $SG_RDS \
  --db-subnet-group-name subnet-group-<MODULO> \
  --backup-retention-period 7 \
  --no-multi-az \
  --no-publicly-accessible \
  --region <REGION>
```

> **Parámetros importantes:**
> - `db.t3.micro` entra en la capa gratuita de AWS (750 horas/mes)
> - `no-publicly-accessible` es crucial por seguridad — solo ECS puede llegar a esta base de datos
> - El aprovisionamiento tarda entre 5 y 10 minutos

### 4.4 Obtener el endpoint de RDS

Espera a que el estado sea `available` y luego:

```bash
aws rds describe-db-instances \
  --db-instance-identifier rds-<MODULO> \
  --query "DBInstances[0].Endpoint.Address" \
  --output text \
  --region <REGION>
```

Guarda este endpoint — se verá así:
```
rds-modulo-1.xxxxxxxxx.us-east-1.rds.amazonaws.com
```

Lo usarás en la Task Definition de ECS como valor de `SPRING_R2DBC_URL`:
```
r2dbc:postgresql://rds-modulo-1.xxxxxxxxx.us-east-1.rds.amazonaws.com:5432/ticketseller
```

### 4.5 Inicializar el esquema de la base de datos

Tu `docker-compose.yml` monta scripts SQL desde `./src/main/resources/db/` para inicializar la base de datos local. En RDS necesitas ejecutar esos scripts manualmente la primera vez.

Opción 1 — Usando una instancia EC2 temporal (recomendado):

```bash
# En una instancia EC2 dentro del mismo VPC, instala psql y ejecuta:
psql -h <RDS_ENDPOINT> -U postgres -d ticketseller \
  -f ./src/main/resources/db/001_schema.sql
```

Opción 2 — Usando Flyway o Liquibase (recomendado a largo plazo):
Agrega Flyway como dependencia en `build.gradle`. Spring Boot ejecutará las migraciones automáticamente al arrancar la aplicación.

---

## 5. Paso 3 — Crear el clúster ECS Fargate

**ECS Fargate** ejecuta los contenedores Docker sin que tengas que administrar servidores. Es equivalente a correr `docker run` pero administrado y escalable.

### 5.1 Crear el clúster ECS

```bash
aws ecs create-cluster \
  --cluster-name cluster-<MODULO> \
  --region <REGION>
```

### 5.2 Crear el Security Group para ECS

```bash
SG_ECS=$(aws ec2 create-security-group \
  --group-name sg-ecs-<MODULO> \
  --description "Security Group para ECS Fargate del modulo" \
  --vpc-id $VPC_ID \
  --region <REGION> \
  --query "GroupId" \
  --output text)

echo "Security Group ECS: $SG_ECS"
```

> Nota: agrega la regla de entrada al SG de ECS después del Paso 5 cuando tengas el `SG_ALB`.

### 5.3 Permitir que ECS acceda a RDS

Ahora que tienes el Security Group de ECS, agrega la regla de entrada al de RDS:

```bash
aws ec2 authorize-security-group-ingress \
  --group-id $SG_RDS \
  --protocol tcp \
  --port 5432 \
  --source-group $SG_ECS \
  --region <REGION>
```

Esto garantiza que **solo** los contenedores ECS pueden conectarse a la base de datos.

### 5.4 Crear el IAM Role para ECS

ECS necesita permisos para descargar imágenes de ECR y escribir logs en CloudWatch:

```bash
# Crear el rol de ejecución
aws iam create-role \
  --role-name ecsTaskExecutionRole-<MODULO> \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'

# Adjuntar la política administrada de AWS
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole-<MODULO> \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

### 5.5 Crear el grupo de logs en CloudWatch

```bash
aws logs create-log-group \
  --log-group-name /ecs/<MODULO> \
  --region <REGION>
```

---

## 6. Paso 4 — Definir la Task Definition y el Servicio ECS

La **Task Definition** es el equivalente al `docker-compose.yml`: define qué imagen correr, cuánta CPU/RAM usar y las variables de entorno.

### 6.1 Crear el archivo de Task Definition

Crea el archivo `task-definition.json` con el siguiente contenido (reemplaza todos los valores `<...>`):

```json
{
  "family": "<MODULO>",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<AWS_ACCOUNT_ID>:role/ecsTaskExecutionRole-<MODULO>",
  "containerDefinitions": [
    {
      "name": "<MODULO>",
      "image": "<AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_R2DBC_URL",
          "value": "r2dbc:postgresql://<RDS_ENDPOINT>:5432/ticketseller"
        },
        {
          "name": "SPRING_R2DBC_USERNAME",
          "value": "postgres"
        },
        {
          "name": "SPRING_R2DBC_PASSWORD",
          "value": "<DB_PASSWORD>"
        },
        {
          "name": "SPRING_MAIL_HOST",
          "value": "email-smtp.<REGION>.amazonaws.com"
        },
        {
          "name": "SPRING_MAIL_PORT",
          "value": "587"
        },
        {
          "name": "SERVER_PORT",
          "value": "8080"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/<MODULO>",
          "awslogs-region": "<REGION>",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": [
          "CMD-SHELL",
          "curl -f http://localhost:8080/actuator/health || exit 1"
        ],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

> **¿Cuánta CPU y memoria?** `512` CPU units (0.5 vCPU) y `1024` MB RAM es suficiente para empezar. Spring Boot con R2DBC arranca sin problemas con estos recursos.

> **Health Check:** Agrega `spring-boot-starter-actuator` a tu `build.gradle` si aún no lo tienes — el endpoint `/actuator/health` es necesario para que ECS sepa si el contenedor está sano.

### 6.2 Registrar la Task Definition

```bash
aws ecs register-task-definition \
  --cli-input-json file://task-definition.json \
  --region <REGION>
```

### 6.3 Crear el Servicio ECS

El servicio mantiene corriendo el número deseado de instancias del contenedor. Completa el `SG_ALB` y las subnets antes de ejecutar:

```bash
# Obtén las subnets de nuevo si las necesitas
aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" \
  --query "Subnets[*].SubnetId" \
  --output text \
  --region <REGION>

# Crear el servicio
aws ecs create-service \
  --cluster cluster-<MODULO> \
  --service-name service-<MODULO> \
  --task-definition <MODULO> \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[<SUBNET_ID_1>,<SUBNET_ID_2>],
    securityGroups=[$SG_ECS],
    assignPublicIp=ENABLED
  }" \
  --load-balancers "targetGroupArn=<TARGET_GROUP_ARN>,containerName=<MODULO>,containerPort=8080" \
  --region <REGION>
```

> El `<TARGET_GROUP_ARN>` se obtiene en el siguiente paso. Si prefieres, crea primero el ALB y luego el servicio.

---

## 7. Paso 5 — Configurar el Application Load Balancer (ALB)

El ALB es el punto de entrada público a tu módulo. Recibe peticiones en HTTPS (443) y las reenvía al contenedor en el puerto 8080.

### 7.1 Crear el Security Group del ALB

```bash
SG_ALB=$(aws ec2 create-security-group \
  --group-name sg-alb-<MODULO> \
  --description "Security Group para ALB del modulo" \
  --vpc-id $VPC_ID \
  --region <REGION> \
  --query "GroupId" \
  --output text)

echo "Security Group ALB: $SG_ALB"

# Permitir HTTP y HTTPS desde cualquier IP
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ALB \
  --protocol tcp --port 80 \
  --cidr 0.0.0.0/0 \
  --region <REGION>

aws ec2 authorize-security-group-ingress \
  --group-id $SG_ALB \
  --protocol tcp --port 443 \
  --cidr 0.0.0.0/0 \
  --region <REGION>
```

Ahora actualiza el Security Group de ECS para solo aceptar tráfico desde el ALB:

```bash
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ECS \
  --protocol tcp \
  --port 8080 \
  --source-group $SG_ALB \
  --region <REGION>
```

### 7.2 Crear el ALB

```bash
ALB_ARN=$(aws elbv2 create-load-balancer \
  --name alb-<MODULO> \
  --subnets <SUBNET_ID_1> <SUBNET_ID_2> \
  --security-groups $SG_ALB \
  --scheme internet-facing \
  --type application \
  --ip-address-type ipv4 \
  --region <REGION> \
  --query "LoadBalancers[0].LoadBalancerArn" \
  --output text)

echo "ALB ARN: $ALB_ARN"

# Obtén el DNS del ALB — comparte esta URL con los otros equipos
aws elbv2 describe-load-balancers \
  --load-balancer-arns $ALB_ARN \
  --query "LoadBalancers[0].DNSName" \
  --output text \
  --region <REGION>
```

El DNS se verá así: `alb-modulo-1-123456789.us-east-1.elb.amazonaws.com`
**Comparte esta URL con los otros equipos** — es la dirección que usarán para llamarte.

### 7.3 Crear el Target Group

El Target Group conecta el ALB con los contenedores ECS:

```bash
TG_ARN=$(aws elbv2 create-target-group \
  --name tg-<MODULO> \
  --protocol HTTP \
  --port 8080 \
  --vpc-id $VPC_ID \
  --target-type ip \
  --health-check-protocol HTTP \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --region <REGION> \
  --query "TargetGroups[0].TargetGroupArn" \
  --output text)

echo "Target Group ARN: $TG_ARN"
```

### 7.4 Crear el Listener HTTP (redirección a HTTPS)

```bash
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=redirect,RedirectConfig="{Protocol=HTTPS,Port=443,StatusCode=HTTP_301}" \
  --region <REGION>
```

### 7.5 Configurar HTTPS (Certificado SSL)

Para HTTPS necesitas un certificado. AWS Certificate Manager (ACM) los provee gratis si tienes un dominio.

**Opción A — Con dominio propio (recomendado):**

```bash
# Solicitar certificado (reemplaza con tu dominio real)
aws acm request-certificate \
  --domain-name modulo1.tudominio.com \
  --validation-method DNS \
  --region <REGION>
```

Sigue las instrucciones de validación DNS en la consola de ACM. Una vez validado:

```bash
# Crear listener HTTPS
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTPS \
  --port 443 \
  --certificates CertificateArn=<ACM_CERTIFICATE_ARN> \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN \
  --region <REGION>
```

**Opción B — Sin dominio (solo para pruebas/desarrollo):**

Usa el DNS del ALB directamente con HTTP. Crea un listener HTTP en el puerto 80 que reenvíe al Target Group:

```bash
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN \
  --region <REGION>
```

> Los otros módulos llamarán a `http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com/endpoint`

---

## 8. Paso 6 — Conectividad entre módulos (cross-account)

Dado que cada módulo está en una cuenta AWS diferente, la comunicación se hace a través de los **ALBs públicos**. No se requiere configuración adicional de red — los módulos simplemente usan las URLs HTTP/HTTPS del ALB del otro equipo.

### 8.1 Compartir URLs entre equipos

Cada equipo debe compartir con los otros la URL base de su ALB:

| Módulo | URL del ALB |
|--------|-------------|
| Módulo 1 | `http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com` |
| Módulo 2 | `http://alb-modulo-2-xxxx.us-east-1.elb.amazonaws.com` |
| Módulo 3 | `http://alb-modulo-3-xxxx.us-east-1.elb.amazonaws.com` |

### 8.2 Configurar el WebClient en Spring

En tu módulo Spring, declara un `WebClient` apuntando a la URL del módulo que necesitas llamar. Usa variables de entorno para no hardcodear las URLs:

```java
// En el módulo 2 o 3, configura el cliente para llamar al módulo 1
@Configuration
public class WebClientConfig {

    @Value("${modules.modulo1.base-url}")
    private String modulo1BaseUrl;

    @Bean
    public WebClient modulo1WebClient() {
        return WebClient.builder()
            .baseUrl(modulo1BaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
```

### 8.3 Ejemplo de llamada reactiva entre módulos

```java
@Service
public class Modulo2Service {

    private final WebClient modulo1WebClient;

    public Modulo2Service(WebClient modulo1WebClient) {
        this.modulo1WebClient = modulo1WebClient;
    }

    public Mono<MiEntidad> obtenerDatosDeModulo1(Long id) {
        return modulo1WebClient.get()
            .uri("/api/recurso/{id}", id)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError,
                resp -> Mono.error(new RuntimeException("Error 4xx del módulo 1")))
            .onStatus(HttpStatusCode::is5xxServerError,
                resp -> Mono.error(new RuntimeException("Error 5xx del módulo 1")))
            .bodyToMono(MiEntidad.class);
    }
}
```

### 8.4 Agregar variables de URL a la Task Definition

En el archivo `task-definition.json` de cada módulo, agrega las URLs de los otros módulos como variables de entorno:

**task-definition.json del módulo 2:**
```json
{
  "environment": [
    { "name": "MODULO1_BASE_URL",
      "value": "http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com" }
  ]
}
```

**task-definition.json del módulo 3:**
```json
{
  "environment": [
    { "name": "MODULO1_BASE_URL",
      "value": "http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com" },
    { "name": "MODULO2_BASE_URL",
      "value": "http://alb-modulo-2-xxxx.us-east-1.elb.amazonaws.com" }
  ]
}
```

### 8.5 Agregar las URLs al application.yml

Actualiza el `application.yml` para leer estas URLs desde variables de entorno:

```yaml
modules:
  modulo1:
    base-url: ${MODULO1_BASE_URL:http://localhost:8081}
  modulo2:
    base-url: ${MODULO2_BASE_URL:http://localhost:8082}
```

---

## 9. Paso 7 — Configurar variables de entorno entre módulos

### 9.1 Uso de AWS Secrets Manager (recomendado para producción)

En lugar de poner contraseñas directamente en la Task Definition, usa Secrets Manager:

```bash
# Guardar el password de la BD como secreto
aws secretsmanager create-secret \
  --name "/<MODULO>/db-password" \
  --secret-string "<DB_PASSWORD>" \
  --region <REGION>
```

En la Task Definition, reemplaza el valor hardcodeado por una referencia al secreto:

```json
{
  "secrets": [
    {
      "name": "SPRING_R2DBC_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:<REGION>:<AWS_ACCOUNT_ID>:secret:/<MODULO>/db-password"
    }
  ]
}
```

Agrega permisos al IAM Role de ECS para leer el secreto:

```bash
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole-<MODULO> \
  --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite
```

### 9.2 Resumen de variables de entorno por módulo

**Módulo 1** (solo recibe — no necesita URLs de otros módulos):
```
SPRING_R2DBC_URL=r2dbc:postgresql://<RDS_ENDPOINT_1>:5432/ticketseller
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=<DB_PASSWORD_1>
SPRING_MAIL_HOST=email-smtp.us-east-1.amazonaws.com
SPRING_MAIL_PORT=587
SERVER_PORT=8080
```

**Módulo 2** (llama al módulo 1):
```
SPRING_R2DBC_URL=r2dbc:postgresql://<RDS_ENDPOINT_2>:5432/ticketseller
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=<DB_PASSWORD_2>
SPRING_MAIL_HOST=email-smtp.us-east-1.amazonaws.com
SPRING_MAIL_PORT=587
SERVER_PORT=8080
MODULO1_BASE_URL=http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com
```

**Módulo 3** (llama al módulo 1 y 2):
```
SPRING_R2DBC_URL=r2dbc:postgresql://<RDS_ENDPOINT_3>:5432/ticketseller
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=<DB_PASSWORD_3>
SPRING_MAIL_HOST=email-smtp.us-east-1.amazonaws.com
SPRING_MAIL_PORT=587
SERVER_PORT=8080
MODULO1_BASE_URL=http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com
MODULO2_BASE_URL=http://alb-modulo-2-xxxx.us-east-1.elb.amazonaws.com
```

---

## 10. Paso 8 — Despliegue de correo con Amazon SES

Tu `application.yml` usa MailHog localmente. En AWS, usa **SES (Simple Email Service)** como reemplazo.

### 10.1 Verificar un email en SES

```bash
aws ses verify-email-identity \
  --email-address noreply@tudominio.com \
  --region <REGION>
```

AWS enviará un email de confirmación. Haz clic en el enlace para verificar.

### 10.2 Crear credenciales SMTP para SES

```bash
aws iam create-user --user-name ses-smtp-user-<MODULO>

aws iam attach-user-policy \
  --user-name ses-smtp-user-<MODULO> \
  --policy-arn arn:aws:iam::aws:policy/AmazonSESFullAccess

# Crear access key para el usuario SMTP
aws iam create-access-key --user-name ses-smtp-user-<MODULO>
```

### 10.3 Actualizar variables de entorno para SES

Agrega estas variables adicionales a la Task Definition:

```json
{"name": "SPRING_MAIL_USERNAME", "value": "<SMTP_USERNAME>"},
{"name": "SPRING_MAIL_PASSWORD", "value": "<SMTP_PASSWORD>"},
{"name": "SPRING_MAIL_AUTH", "value": "true"},
{"name": "SPRING_MAIL_STARTTLS", "value": "true"}
```

Y actualiza tu `application.yml` para leer estos flags desde variables:

```yaml
spring:
  mail:
    host: ${SPRING_MAIL_HOST:localhost}
    port: ${SPRING_MAIL_PORT:1025}
    username: ${SPRING_MAIL_USERNAME:}
    password: ${SPRING_MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: ${SPRING_MAIL_AUTH:false}
          starttls:
            enable: ${SPRING_MAIL_STARTTLS:false}
```

---

## 11. Paso 9 — Verificación final

### 11.1 Verificar que el contenedor está corriendo

```bash
# Listar tareas en ejecución
aws ecs list-tasks \
  --cluster cluster-<MODULO> \
  --region <REGION>

# Ver detalles de una tarea
aws ecs describe-tasks \
  --cluster cluster-<MODULO> \
  --tasks <TASK_ARN> \
  --region <REGION>
```

### 11.2 Ver los logs en CloudWatch

```bash
# Ver logs en tiempo real
aws logs tail /ecs/<MODULO> --follow --region <REGION>
```

### 11.3 Probar los endpoints

```bash
# Health check
curl http://<ALB_DNS>/actuator/health

# Swagger UI
open http://<ALB_DNS>/swagger-ui.html

# API docs
curl http://<ALB_DNS>/api-docs
```

### 11.4 Verificar conectividad entre módulos

Desde el equipo del módulo 3, prueba que puede llamar al módulo 1:

```bash
curl http://alb-modulo-1-xxxx.us-east-1.elb.amazonaws.com/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### 11.5 Actualizar el servicio después de cambios en el código

```bash
# 1. Rebuild y push
docker build -t <MODULO> .
docker tag <MODULO>:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest

# 2. Forzar nuevo despliegue
aws ecs update-service \
  --cluster cluster-<MODULO> \
  --service service-<MODULO> \
  --force-new-deployment \
  --region <REGION>
```

---

## 12. Resumen de URLs y variables por módulo

> Llena esta tabla con los valores reales y compártela con todo el equipo.

| Campo | Módulo 1 | Módulo 2 | Módulo 3 |
|-------|----------|----------|----------|
| **AWS Account ID** | | | |
| **Región** | us-east-1 | us-east-1 | us-east-1 |
| **ECR URI** | | | |
| **RDS Endpoint** | | | |
| **ALB DNS (URL pública)** | | | |

---

## 13. Comandos de referencia rápida

```bash
# ── ECR ─────────────────────────────────────────────────────────
# Login a ECR
aws ecr get-login-password --region <REGION> | docker login --username AWS \
  --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com

# Build + push en un solo bloque
docker build -t <MODULO> . && \
docker tag <MODULO>:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest && \
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/<MODULO>:latest

# ── ECS ─────────────────────────────────────────────────────────
# Ver tareas corriendo
aws ecs list-tasks --cluster cluster-<MODULO> --region <REGION>

# Forzar redeploy
aws ecs update-service --cluster cluster-<MODULO> --service service-<MODULO> \
  --force-new-deployment --region <REGION>

# ── RDS ─────────────────────────────────────────────────────────
# Obtener endpoint
aws rds describe-db-instances --db-instance-identifier rds-<MODULO> \
  --query "DBInstances[0].Endpoint.Address" --output text --region <REGION>

# ── ALB ─────────────────────────────────────────────────────────
# Obtener DNS del ALB
aws elbv2 describe-load-balancers --names alb-<MODULO> \
  --query "LoadBalancers[0].DNSName" --output text --region <REGION>

# ── LOGS ────────────────────────────────────────────────────────
# Ver logs en tiempo real
aws logs tail /ecs/<MODULO> --follow --region <REGION>
```

---

## Notas adicionales

**Costos estimados:**
- ECS Fargate: factura por vCPU y RAM usadas por segundo (~$15-20/mes con 0.5 vCPU + 1 GB)
- RDS `db.t3.micro`: 750 horas/mes gratis por 12 meses (capa gratuita)
- ALB: ~$16/mes fijo + carga procesada (no tiene capa gratuita)
- ECR: 500 MB/mes de almacenamiento gratis

**Para producción real, considera además:**
- Route 53 para dominios propios y DNS
- AWS WAF para protección del ALB contra ataques
- Auto Scaling en ECS para escalar automáticamente bajo carga
- RDS Multi-AZ para alta disponibilidad y failover automático
- CloudWatch Alarms para monitoreo y alertas por email

**El `docker-compose.yml` existente sigue siendo válido para desarrollo local** — no necesitas modificarlo. Solo asegúrate de mantener sincronizados los nombres de variables de entorno entre el `application.yml`, el `docker-compose.yml` y el `task-definition.json`.
