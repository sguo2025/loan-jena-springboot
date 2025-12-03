package com.example.loanjena.service;

import com.example.loanjena.model.LoanApplicationRequest;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.springframework.stereotype.Service;

@Service
public class LoanReasoningService {

    private static final String NS = "http://example.org/";

    public String evaluateApplication(LoanApplicationRequest request) {
        // 1. 创建数据模型
        Model dataModel = ModelFactory.createDefaultModel();
        dataModel.setNsPrefix("ex", NS);

        Resource applicant = dataModel.createResource(NS + request.getApplicantId());
        Resource app = dataModel.createResource(NS + "App_" + request.getApplicantId());

        // 设置类型
        if (request.isStudent()) {
            applicant.addProperty(RDF.type, dataModel.getResource(NS + "Student"));
        }

        // 添加属性
        applicant.addProperty(dataModel.createProperty(NS + "hasAge"),
                dataModel.createTypedLiteral(request.getAge()));
        applicant.addProperty(dataModel.createProperty(NS + "hasCreditScore"),
                dataModel.createTypedLiteral(request.getCreditScore()));

        app.addProperty(RDF.type, dataModel.getResource(NS + "Application"));
        app.addProperty(dataModel.createProperty(NS + "applicant"), applicant);

        // 2. 定义评估逻辑（使用 SPARQL 查询替代规则推理）
        String appId = "App_" + request.getApplicantId();
        String sparqlQuery = "PREFIX ex: <" + NS + ">\n" +
                "SELECT ?status WHERE {\n" +
                "  BIND(ex:" + appId + " as ?app)\n" +
                "  ?app ex:applicant ?p .\n" +
                "  ?p ex:hasAge ?age .\n" +
                "  ?p ex:hasCreditScore ?score .\n" +
                "  \n" +
                "  BIND(IF((?age > 17) && (?score >= 600), true, false) as ?meetsBasic)\n" +
                "  \n" +
                "  OPTIONAL { ?p a ex:Student . BIND(true as ?isStudent) }\n" +
                "  BIND(COALESCE(?isStudent, false) as ?isStu)\n" +
                "  \n" +
                "  BIND(IF(?meetsBasic && !?isStu, \"Accepted\", \"Rejected\") as ?status)\n" +
                "}";

        // 3. 执行 SPARQL 查询
        Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qe = QueryExecutionFactory.create(query, dataModel);
        
        try {
            ResultSet results = qe.execSelect();
            if (results.hasNext()) {
                QuerySolution qs = results.nextSolution();
                String status = qs.getLiteral("status").getString();
                return status;
            } else {
                return "Unknown";
            }
        } finally {
            qe.close();
        }
    }
}