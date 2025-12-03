package com.example.loanjena.service;

import com.example.loanjena.model.LoanApplicationRequest;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.GenericRuleReasoner;
import org.apache.jena.reasoner.InfModel;
import org.apache.jena.reasoner.Rule;
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
                dataModel.createTypedLiteral(request.getAge(), XSD.integer));
        applicant.addProperty(dataModel.createProperty(NS + "hasCreditScore"),
                dataModel.createTypedLiteral(request.getCreditScore(), XSD.integer));

        app.addProperty(RDF.type, dataModel.getResource(NS + "Application"));
        app.addProperty(dataModel.createProperty(NS + "applicant"), applicant);

        // 2. 定义规则（与你提供的完全一致）
        String rules = """
            @prefix ex: <http://example.org/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
            @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

            [acceptBasic: 
              (?app ex:applicant ?p),
              (?p ex:hasAge ?age), 
              greaterThan(?age, 17),
              (?p ex:hasCreditScore ?score),
              greaterThanEqual(?score, 600)
              ->
              (?app ex:meetsBasicCriteria "true"^^xsd:boolean)
            ]

            [rejectStudent:
              (?app ex:applicant ?p),
              (?p rdf:type ex:Student)
              ->
              (?app ex:isStudent "true"^^xsd:boolean)
            ]

            [finalDecision:
              (?app ex:meetsBasicCriteria "true"^^xsd:boolean),
              notEqual((?app ex:isStudent "true"^^xsd:boolean), true)
              ->
              (?app ex:status ex:Accepted)
            ]

            [defaultReject:
              (?app rdf:type ex:Application),
              notEqual((?app ex:status ex:Accepted), true)
              ->
              (?app ex:status ex:Rejected)
            ]
            """;

        // 3. 推理
        GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel infModel = ModelFactory.createInfModel(reasoner, dataModel);

        // 4. 查询结果
        StmtIterator iter = infModel.listStatements(
                app,
                infModel.createProperty(NS + "status"),
                (RDFNode) null
        );

        if (iter.hasNext()) {
            RDFNode status = iter.nextStatement().getObject();
            return status.getLocalName(); // "Accepted" or "Rejected"
        } else {
            return "Unknown";
        }
    }
}