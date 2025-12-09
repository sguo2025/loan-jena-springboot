package com.example.loanjena.service;

import com.example.loanjena.dto.ProcessFlowInfo;
import com.example.loanjena.dto.ProcessStepInfo;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于 OWL 本体的流程推理服务
 * 负责从 OWL 文件中推理业务流程步骤的结构关系
 */
@Service
public class ProcessReasoningService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessReasoningService.class);

    @Value("${ontology.file.path:owl/transfer_order_ontology.owl}")
    private String ontologyFilePath;

    private OntModel ontModel;

    private static final String TRANSFER_NS = "https://iwhalecloud.com/ontology/transfer#";
    private static final String PROCESS_STEP_CLASS = TRANSFER_NS + "ProcessStep";
    private static final String PRECEDES_PROPERTY = TRANSFER_NS + "precedes";
    private static final String REQUIRES_ENTITY_PROPERTY = TRANSFER_NS + "requiresEntity";
    private static final String PRODUCES_ENTITY_PROPERTY = TRANSFER_NS + "producesEntity";
    private static final String MAPS_TO_COMPONENT_PROPERTY = TRANSFER_NS + "mapsToComponent";
    private static final String USES_API_PROPERTY = TRANSFER_NS + "usesAPI";
    private static final String STEP_CODE_PROPERTY = TRANSFER_NS + "stepCode";
    private static final String ETOM_REF_PROPERTY = TRANSFER_NS + "etomRef";

    /**
     * 初始化本体模型
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("加载 OWL 本体文件: {}", ontologyFilePath);
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
            
            // 从类路径加载 Turtle 格式的文件
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream inputStream = classLoader.getResourceAsStream(ontologyFilePath);
            
            if (inputStream == null) {
                throw new RuntimeException("找不到本体文件: " + ontologyFilePath);
            }
            
            // 指定文件格式为 Turtle (TTL)
            ontModel.read(inputStream, null, "TURTLE");
            
            logger.info("OWL 本体模型加载成功，共 {} 个三元组", ontModel.size());
        } catch (Exception e) {
            logger.error("加载 OWL 本体文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法加载本体文件", e);
        }
    }

    /**
     * 获取完整的流程信息
     */
    public ProcessFlowInfo getProcessFlow() {
        ProcessFlowInfo flowInfo = new ProcessFlowInfo();
        
        List<ProcessStepInfo> steps = getAllProcessSteps();
        flowInfo.setSteps(steps);
        flowInfo.setTotalSteps(steps.size());
        
        // 找出开始步骤（没有前驱的步骤）
        String startStep = findStartStep(steps);
        flowInfo.setStartStepUri(startStep);
        
        // 找出结束步骤（没有后继的步骤）
        String endStep = findEndStep(steps);
        flowInfo.setEndStepUri(endStep);
        
        return flowInfo;
    }

    /**
     * 根据步骤URI获取步骤信息
     */
    public ProcessStepInfo getStepInfo(String stepUri) {
        if (!stepUri.startsWith("http")) {
            stepUri = TRANSFER_NS + stepUri;
        }
        
        OntClass stepClass = ontModel.getOntClass(stepUri);
        if (stepClass == null) {
            return null;
        }
        
        return buildStepInfo(stepClass);
    }

    /**
     * 获取所有流程步骤
     */
    private List<ProcessStepInfo> getAllProcessSteps() {
        List<ProcessStepInfo> steps = new ArrayList<>();
        
        OntClass processStepClass = ontModel.getOntClass(PROCESS_STEP_CLASS);
        if (processStepClass == null) {
            logger.warn("未找到 ProcessStep 类定义");
            return steps;
        }
        
        // 获取所有 ProcessStep 的子类
        ExtendedIterator<OntClass> subClasses = processStepClass.listSubClasses(false);
        while (subClasses.hasNext()) {
            OntClass subClass = subClasses.next();
            if (!subClass.isAnon()) {
                ProcessStepInfo stepInfo = buildStepInfo(subClass);
                // 只添加有详细定义的步骤（至少有一个限制条件或属性）
                if (hasDetailedDefinition(stepInfo)) {
                    steps.add(stepInfo);
                }
            }
        }
        
        // 按步骤编号排序
        steps.sort(Comparator.comparing(s -> s.getStepNumber() != null ? s.getStepNumber() : Integer.MAX_VALUE));
        
        return steps;
    }
    
    /**
     * 判断步骤是否有详细定义
     */
    private boolean hasDetailedDefinition(ProcessStepInfo stepInfo) {
        return (stepInfo.getStepCode() != null) ||
               (stepInfo.getEtomRef() != null) ||
               (stepInfo.getRequiredEntities() != null && !stepInfo.getRequiredEntities().isEmpty()) ||
               (stepInfo.getProducedEntities() != null && !stepInfo.getProducedEntities().isEmpty()) ||
               (stepInfo.getMappedComponent() != null) ||
               (stepInfo.getUsedAPI() != null);
    }

    /**
     * 构建步骤信息
     */
    private ProcessStepInfo buildStepInfo(OntClass stepClass) {
        ProcessStepInfo info = new ProcessStepInfo();
        
        String uri = stepClass.getURI();
        info.setStepUri(uri);
        info.setStepName(extractLocalName(uri));
        
        // 提取步骤编号
        info.setStepNumber(extractStepNumber(uri));
        
        // 获取标签和描述
        Statement labelStmt = stepClass.getProperty(RDFS.label);
        if (labelStmt != null) {
            info.setLabel(labelStmt.getString());
        }
        
        Statement commentStmt = stepClass.getProperty(RDFS.comment);
        if (commentStmt != null) {
            info.setComment(commentStmt.getString());
        }
        
        // 获取数据属性
        info.setStepCode(getDataPropertyValue(stepClass, STEP_CODE_PROPERTY));
        info.setEtomRef(getDataPropertyValue(stepClass, ETOM_REF_PROPERTY));
        
        // 获取对象属性
        info.setNextSteps(getObjectPropertyValues(stepClass, PRECEDES_PROPERTY));
        info.setPreviousSteps(findPreviousSteps(uri));
        info.setRequiredEntities(getRestrictionValues(stepClass, REQUIRES_ENTITY_PROPERTY));
        info.setProducedEntities(getRestrictionValues(stepClass, PRODUCES_ENTITY_PROPERTY));
        
        // 获取映射的组件和API
        List<String> components = getRestrictionValues(stepClass, MAPS_TO_COMPONENT_PROPERTY);
        if (!components.isEmpty()) {
            info.setMappedComponent(components.get(0));
        }
        
        List<String> apis = getRestrictionValues(stepClass, USES_API_PROPERTY);
        if (!apis.isEmpty()) {
            info.setUsedAPI(apis.get(0));
        }
        
        return info;
    }

    /**
     * 获取数据属性值
     */
    private String getDataPropertyValue(OntClass ontClass, String propertyUri) {
        Property property = ontModel.getProperty(propertyUri);
        if (property == null) {
            return null;
        }
        
        Statement stmt = ontClass.getProperty(property);
        if (stmt != null) {
            return stmt.getString();
        }
        
        return null;
    }

    /**
     * 获取对象属性值列表
     */
    private List<String> getObjectPropertyValues(OntClass ontClass, String propertyUri) {
        List<String> values = new ArrayList<>();
        Property property = ontModel.getProperty(propertyUri);
        if (property == null) {
            return values;
        }
        
        StmtIterator stmts = ontClass.listProperties(property);
        while (stmts.hasNext()) {
            Statement stmt = stmts.next();
            RDFNode node = stmt.getObject();
            if (node.isResource()) {
                values.add(node.asResource().getURI());
            }
        }
        
        return values;
    }

    /**
     * 从限制条件中提取值
     */
    private List<String> getRestrictionValues(OntClass ontClass, String propertyUri) {
        List<String> values = new ArrayList<>();
        Property property = ontModel.getProperty(propertyUri);
        if (property == null) {
            return values;
        }
        
        // 遍历父类限制
        ExtendedIterator<OntClass> superClasses = ontClass.listSuperClasses(false);
        while (superClasses.hasNext()) {
            OntClass superClass = superClasses.next();
            if (superClass.isRestriction()) {
                Restriction restriction = superClass.asRestriction();
                if (restriction.getOnProperty().equals(property)) {
                    // 处理 someValuesFrom 限制
                    if (restriction.isSomeValuesFromRestriction()) {
                        SomeValuesFromRestriction svfr = restriction.asSomeValuesFromRestriction();
                        Resource resource = svfr.getSomeValuesFrom();
                        if (resource != null && resource.isURIResource()) {
                            values.add(resource.getURI());
                        }
                    }
                    // 处理 hasValue 限制
                    else if (restriction.isHasValueRestriction()) {
                        HasValueRestriction hvr = restriction.asHasValueRestriction();
                        RDFNode value = hvr.getHasValue();
                        if (value != null && value.isResource()) {
                            values.add(value.asResource().getURI());
                        }
                    }
                }
            }
        }
        
        return values;
    }

    /**
     * 查找前驱步骤
     */
    private List<String> findPreviousSteps(String stepUri) {
        List<String> previous = new ArrayList<>();
        Property precedesProperty = ontModel.getProperty(PRECEDES_PROPERTY);
        
        if (precedesProperty == null) {
            return previous;
        }
        
        // 查找所有指向当前步骤的 precedes 属性
        ResIterator subjects = ontModel.listSubjectsWithProperty(precedesProperty);
        while (subjects.hasNext()) {
            Resource subject = subjects.next();
            if (!subject.isURIResource()) {
                continue;
            }
            
            StmtIterator stmts = subject.listProperties(precedesProperty);
            while (stmts.hasNext()) {
                Statement stmt = stmts.next();
                RDFNode obj = stmt.getObject();
                if (obj.isResource() && obj.isURIResource()) {
                    String objUri = obj.asResource().getURI();
                    if (objUri != null && objUri.equals(stepUri)) {
                        String subjectUri = subject.getURI();
                        if (subjectUri != null) {
                            previous.add(subjectUri);
                        }
                    }
                }
            }
        }
        
        return previous;
    }

    /**
     * 查找开始步骤
     */
    private String findStartStep(List<ProcessStepInfo> steps) {
        for (ProcessStepInfo step : steps) {
            if (step.getPreviousSteps() == null || step.getPreviousSteps().isEmpty()) {
                return step.getStepUri();
            }
        }
        return null;
    }

    /**
     * 查找结束步骤
     */
    private String findEndStep(List<ProcessStepInfo> steps) {
        for (ProcessStepInfo step : steps) {
            if (step.getNextSteps() == null || step.getNextSteps().isEmpty()) {
                return step.getStepUri();
            }
        }
        return null;
    }

    /**
     * 提取本地名称
     */
    private String extractLocalName(String uri) {
        if (uri == null) {
            return null;
        }
        int index = Math.max(uri.lastIndexOf('#'), uri.lastIndexOf('/'));
        return index >= 0 ? uri.substring(index + 1) : uri;
    }

    /**
     * 从URI中提取步骤编号
     */
    private Integer extractStepNumber(String uri) {
        String localName = extractLocalName(uri);
        if (localName != null && localName.matches(".*Step(\\d+).*")) {
            String numStr = localName.replaceAll(".*Step(\\d+).*", "$1");
            try {
                return Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        return null;
    }

    /**
     * 获取步骤的前驱步骤信息
     */
    public List<ProcessStepInfo> getPreviousStepInfos(String stepUri) {
        ProcessStepInfo stepInfo = getStepInfo(stepUri);
        if (stepInfo == null || stepInfo.getPreviousSteps() == null) {
            return Collections.emptyList();
        }
        
        return stepInfo.getPreviousSteps().stream()
                .map(this::getStepInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取步骤的后继步骤信息
     */
    public List<ProcessStepInfo> getNextStepInfos(String stepUri) {
        ProcessStepInfo stepInfo = getStepInfo(stepUri);
        if (stepInfo == null || stepInfo.getNextSteps() == null) {
            return Collections.emptyList();
        }
        
        return stepInfo.getNextSteps().stream()
                .map(this::getStepInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
