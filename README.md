# Serverless Quarkus JWT Issuer

## About

A serverless microservice which issues JSON Web Tokens (JWT). Built for Google Cloud Functions.

## Setup

Built with `Java 17+` and `Quarkus`

### 1. Create `application.properties`
Create the file `src/main/resources/application.properties`

### 2. Database Configuration
Modify the following properties in `src/main/resources/application.properties` to match your selected RDBMS.

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=user
quarkus.datasource.password=password
quarkus.datasource.reactive.url=vertx-reactive:postgresql://localhost:5432/jwt_test
```

### 3. JWT Signing Configuration
After cloning the project, you must provide a key pair for signing JWT tokens.

```
openssl genrsa -out rsaPrivateKey.pem 2048
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
```

Copy the generated keys into the `resources` directory
```
cp privateKey.pem ./src/main/resources
cp publicKey.pem ./src/main/resources
```

Add the following properties in `src/main/resources/application.properties`

```properties
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.verify.key.location=publicKey.pem
```

### 4. Verify Password4J Configuration `src/main/resources/psw4j.properties`

Refer to [password4j documentation](https://github.com/Password4j/password4j)

### 5. Setup Default Admin for Testing/Development

Add the following to `src/main/resources/application.properties`

```properties
default.admin.clientid=<client id>
default.admin.secret=<secret>
```

During development and testing, a client with admin privileges is created upon startup with these credentials.

### 6. Run

Run tests
```shell script
./mvnw compile quarkus:test
```

Run in development mode
```shell script
mvn dependency:copy \
  -Dartifact='com.google.cloud.functions.invoker:java-function-invoker:1.1.1' \
  -DoutputDirectory=.
  
./scripts/local-run.sh
```

Build for Google Cloud Functions
```shell script
quarkus build
```

## HTTP Endpoints

`/admin/create (POST)`

> Endpoint for creating clients that maybe issued tokens.

**Request Body**

```json
{
  // client id of caller.
  "id": "*",
  // Secret key for client caller. 
  "secret": "*"
}
```
**Success Response Body**

```json
{
  "status": "success",
  "data" : {
    "id": "*",
    "secret": "*"
  }
}
```
**Failure Response Body**

```json
{
  "status": "fail",
  "data" : {
    "message": "*"
  }
}
```

`/credentials/issue (POST)`

> Endpoint for issuing a new JWT

**Request Body**

```json
{
  // client id of caller.
  "id": "*",
  // Secret key for client caller. 
  "secret": "*"
}
```
**Success Response Body**

```json
{
  "status": "success",
  "data" : {
    "expiresIn": 3600,
    "token": "*",
    "refresh": "*"
  }
}

```
**Failure Response Body**

```json
{
  "status": "fail",
  "data" : {
    "message": "*"
  }
}
```

`/credentials/refresh (POST)`

> Endpoint for refreshing a JWT.

**Request Body**

```json
{
  // Refresh token
  "token": "*",
}
```
**Success Response Body**

```json
{
  "status": "success",
  "data" : {
    "expiresIn": 3600,
    "token": "*",
    "refresh": "*"
  }
}
```
**Failure Response Body**

```json
{
  "status": "fail",
  "data" : {
    "message": "*"
  }
}
```

## Database Schema

The service queries a SQL database of clients to verify API requests made. The services only requires one table named `clients`.
The following is a description of this table:

| Column       | Type      | Description                                                                     |
|--------------|-----------|---------------------------------------------------------------------------------|
| id           | `String`  | The id for the client.                                                          |
| secret       | `String`  | A hash of the client's secret. By default, the secret is hashed using bcrypt.   |
| hasAdminRole | `boolean` | True if the client can create new client's using the `/admin/create` endpoint.  |

The services utilizes the `quarkus-reactive-pg-client` and `quarkus-hibernate-reactive` extensions to interface with a Postgres database. A different RDBMS can be used by changing properties in the `application.properties` file. Please refer to the documentation of those extensions for more information.