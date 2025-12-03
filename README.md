# loan-jena-springboot

mvn clean compile

运行方式：mvn spring-boot:run


curl -X POST http://localhost:8080/api/loan/apply \
  -H "Content-Type: application/json" \
  -d '{"applicantId":"Bob","age":30,"creditScore":700,"isStudent":false}'