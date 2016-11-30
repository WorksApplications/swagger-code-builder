# Swagger Code Builder

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/WorksApplications/swagger-code-builder.svg?branch=master)](https://travis-ci.org/WorksApplications/swagger-code-builder)

Swagger Code Builder is a project generator from [Swagger](http://swagger.io/) (or [Open API](https://www.openapis.org/)).

## Getting Started

Install Coding Starter and generate `java-service` project.

```bash
gradle installDist
gradle :api-exceptions:install
mkdir out
./build/install/swagger-code-builder/bin/swagger-code-builder \
    --structure java-services \
    --api-spec-path samples/minimum-full.yaml
```

Implement `FindUserService`.

```bash
cd out/minimum-api-services
gradle eclipse # or idea
```

Import the project and open `FindUserService`.

```java
@Slf4j
@Singleton
public class FindUserService {
    public FindUserResponse handle(FindUserRequest request) {
        log.debug("request = {}", request);
        return FindUserResponse.builder()
                .eTag("aaaaa")
                .body(FindUserResponseBody.builder()
                        .userId(request.getUserId())
                        .role("ROLE")
                        .build())
                .build();
    }
}
```

Install to local.

```bash
gradle install
```

Generate `sparkjava` project and run.

```bash
./build/install/swagger-code-builder/bin/swagger-code-builder \
    --structure sparkjava \
    --api-spec-path samples/minimum-full.yaml
cd out/minimum-api-server
gradle run
```

Already you can call the API.
Open another console.

```bash
curl "http://localhost:8080/api/1/users/test" -H "Authorization: foobar"
```

You can also generate `java-awsserverless` project.

```bash
./build/install/swagger-code-builder/bin/swagger-code-builder \
    --structure java-awsserverless \
    --api-spec-path samples/minimum-full.yaml \
    --aws-region ap-northeast-1 \
    --aws-account-id 123456789012 # Your AWS account ID
cd out/minimum-api-awsserverless
gradle buildZip
./register-lambda.sh
./register-api.sh
```

## Usage

```text
 --api-spec-path VAL : File path for Open API (Swagger) file
 --output-path VAL   : Output directory (default: out)
 --structure VAL     : Project structure
 -h (--help)         : Print usage message and exit (default: true)
```

### Avaiable Structures

<dl>
  <dt>java-services</dt>
  <dd>POJO business logic classes</dd>
  <dt>sparkjava</dt>
  <dd>Application server based on sparkjava which invoke POJO services</dd>
  <dt>java-awsserverless</dt>
  <dd>AWS Lambdas which invoke POJO services</dd>
</dl>

## License

Swagger Code Builder by Works Applications Co.,Ltd. is licensed under
the Apache License, Version 2.0.
