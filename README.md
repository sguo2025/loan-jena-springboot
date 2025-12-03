# loan-jena-springboot

mvn clean compile

运行方式：mvn spring-boot:run


curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{"applicantId":"Bob","age":30,"creditScore":700,"isStudent":false}'


Swagger 访问地址：http://localhost:8080/swagger-ui.html

或者 API 文档 JSON 地址：http://localhost:8080/v3/api-docs