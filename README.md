# BankX - Ledger reactivo

## Alcance

Esta versión cubre el Track A con Java 17, Spring Boot WebFlux y MongoDB reactivo. La evaluación de riesgo se simula con WireMock y, si el servicio falla o supera el tiempo de espera configurado, se recurre al módulo legacy basado en Spring Data JPA/H2.

No se incluyen Quarkus, Azure/AKS/APIM, pruebas de rendimiento, la resiliencia completa del Track B (reintentos y circuit breaker) ni un pipeline de CI/CD avanzado.


La API, las consultas y escrituras en Mongo, la llamada HTTP y el flujo SSE se manejan de forma reactiva con Reactor y sin usar `.block()`. El acceso JPA/JDBC sí es bloqueante, por lo que `RiskEvaluationService` lo ejecuta con `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` para no bloquear el event loop de Netty.

## Flujo de la solicitud

`request → Bean Validation → account → remote risk → optional mecanismo alternativo legacy → business rules → save account → save transaction → SSE → HTTP response`

Un resultado remoto `REJECTED` se considera una respuesta válida y no activa el mecanismo alternativo. Este sólo se usa si la evaluación remota falla por un problema técnico o supera el tiempo de espera.

## Cómo ejecutarlo

Requisitos: Java 17, Docker y Docker Compose.

```bash
docker compose up -d
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Mongo escucha en `27017`, WireMock en `8089` y la aplicación en `8080`. Para detenerlos:

```bash
# Ctrl+C para la aplicación
docker compose down
```

Para eliminar también los datos Mongo de la demo: `docker compose down -v`.

Variables de configuración disponibles:

| Variable | Valor por defecto |
|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/bankx` |
| `RISK_BASE_URL` | `http://localhost:8089` |
| `RISK_TIMEOUT` | `500ms` |
| `SERVER_PORT` | `8080` |
| `LEGACY_DB_URL` | `jdbc:h2:mem:legacy;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |

Al iniciar la aplicación se crean, si todavía no existen, las cuentas `001-0001` (1000), `001-0002` (100) y `001-0003` (0).

## Escenarios de prueba

Primero abre el listener SSE en otra terminal:

```bash
curl -N -H "Accept: text/event-stream" http://localhost:8080/api/stream/transactions
```

Estado de la aplicación:

```bash
curl http://localhost:8080/actuator/health
```

Crédito correcto:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0001","type":"CREDIT","amount":10}'
```

Débito correcto:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0002","type":"DEBIT","amount":10}'
```

Riesgo rechazado (sin mecanismo alternativo):

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0001","type":"CREDIT","amount":1,"simulate":"reject"}'
```

Error HTTP 500 del servicio de riesgo → mecanismo alternativo legacy:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0001","type":"CREDIT","amount":1,"simulate":"fail"}'
```

Servicio de riesgo lento → tiempo de espera de 500 ms → mecanismo alternativo legacy:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0001","type":"CREDIT","amount":1,"simulate":"slow"}'
```

Fondos insuficientes:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0003","type":"DEBIT","amount":1}'
```

Cuenta inexistente:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"999-9999","type":"CREDIT","amount":1}'
```

Validación de entrada:

```bash
curl -i -X POST http://localhost:8080/api/transactions -H "Content-Type: application/json" -d '{"accountNumber":"001-0001","type":"DEBIT","amount":0}'
```

## Pruebas

```bash
./mvnw test
./mvnw package
```

En Windows se usa `mvnw.cmd`. Las 10 pruebas cubren crédito, débito, cuenta inexistente, fondos insuficientes, rechazo funcional sin mecanismo alternativo, fallo técnico, tiempo de espera, uso de `boundedElastic`, validación HTTP y emisión SSE. Los flujos reactivos de la aplicación y del servicio de riesgo se verifican con `StepVerifier`.

También se incluyen una colección y un entorno de Postman en `docs/postman/`, con validaciones para todos los escenarios y un listener SSE para pruebas manuales.

## Supuestos

1. No se proporcionó el contrato real del servicio de riesgo, así que se definió `POST /risk/evaluate` con una respuesta mínima.
2. `simulate` se usa únicamente para controlar WireMock durante la demo.
3. `REJECTED` se trata como un resultado funcional y no activa el mecanismo alternativo.
4. El mecanismo alternativo sólo se utiliza ante fallos técnicos del servicio de riesgo.
5. Se usan los estados 400 para `validation_error`, 404 para `account_not_found` y 422 para `risk_rejected` e `insufficient_funds`.
6. Un POST exitoso responde `201 Created` con identificador, operación, estado y saldo resultante.
7. Los 500 ms se usan para que el tiempo de espera sea fácil de reproducir en la demo; no representan un SLO real.
8. Los eventos SSE se mantienen en memoria, no se reproducen y sólo llegan a los clientes conectados en ese momento.
9. Esta versión no resuelve la concurrencia ni el doble gasto en operaciones simultáneas sobre una misma cuenta.
10. No se garantiza atomicidad entre la actualización de `Account` y la persistencia de `Transaction`.


## Limitaciones

- Las dos escrituras Mongo no forman una transacción conjunta; si falla la segunda, la cuenta ya pudo cambiar.
- No hay control de versión ni operación atómica de saldo para concurrencia productiva.
- Los eventos SSE no se almacenan ni se reintentan; un consumidor lento o desconectado puede perderlos.
- H2 es efímero y sus reglas legacy se recrean en cada arranque.
- No se incluyen reintentos, circuit breaker, métricas de negocio, seguridad, trazabilidad ni pipeline de CI/CD.
- Los datos demo se crean si faltan, pero no se restauran sus saldos en cada arranque.
