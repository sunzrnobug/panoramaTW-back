package com.panorama.backend.service.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panorama.backend.model.node.LayerNode;
import com.panorama.backend.model.resource.GeneralResult;
import com.panorama.backend.model.workflow.WorkflowEdge;
import com.panorama.backend.model.workflow.WorkflowNode;
import com.panorama.backend.service.map.VectorTileService;
import com.panorama.backend.service.node.LayerNodeService;
import com.panorama.backend.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * @author: DMK
 * @description:
 * @date: 2025-03-12 22:54:47
 * @version: 1.0
 */
@Service
@Slf4j
public class WorkflowService {

    private final VectorTileService vectorTileService;
    private final LayerNodeService layerNodeService;

    @Value("${path.script}")
    private String scriptPath;

<<<<<<< Updated upstream
    @Value("${path.temp}")
    private String tempPath;

=======
>>>>>>> Stashed changes
    @Autowired
    public WorkflowService(VectorTileService vectorTileService, LayerNodeService layerNodeService) {
        this.vectorTileService = vectorTileService;
        this.layerNodeService = layerNodeService;
    }

    public Map<String, Object> executeWorkflow(String jsonInput) throws Exception {
        // 解析 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonInput);

        // 提取 node 和 edge
        List<WorkflowNode> nodes = new ArrayList<>();
        for (JsonNode node : rootNode.get("node")) {
            WorkflowNode workflowNode = objectMapper.treeToValue(node, WorkflowNode.class);
            nodes.add(workflowNode);
        }

        List<WorkflowEdge> edges = new ArrayList<>();
        for (JsonNode edge : rootNode.get("edge")) {
            WorkflowEdge workflowEdge = objectMapper.treeToValue(edge, WorkflowEdge.class);
            edges.add(workflowEdge);
        }

        // 按拓扑顺序执行
        Map<String, Object> results = new HashMap<>();
        for (WorkflowNode node : nodes) {
            executeNode(node, edges, results);
        }
        System.out.println("Workflow execution completed.");
        System.out.println("Results: " + results);
        return results;
    }

    private void executeNode(WorkflowNode node, List<WorkflowEdge> edges, Map<String, Object> results) throws Exception {
        Map<String, Object> params = node.getParams();

        // 替换输入参数
        for (WorkflowEdge edge : edges) {
            if (edge.getEnd_node().equals(node.getNode_id())) {
                for (Map.Entry<String, String> entry : edge.getMap().entrySet()) {
                    Map<String, Object> previousOutput = (Map<String, Object>) results.get(entry.getKey());
                    params.put(entry.getValue(), previousOutput);
                }
            }
        }

        // 处理数据库读取逻辑
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
                if (valueMap.containsKey("data_id")) {
                    String tableName = (String) valueMap.get("data_id");
                    LayerNode layerNode = layerNodeService.getLayerNodeByTableName(tableName); // 查询数据库
                    GeneralResult geojsonResult = vectorTileService.getGeojsonByTableName(layerNode, tableName); // 查询数据库
                    if (!geojsonResult.getStatus().equals("success")) {
                        throw new Exception("Failed to get GeoJSON from table " + tableName);
                    }
                    String geojson = (String) geojsonResult.getMessage();
                    Map<String, Object> geojsonMap = JsonUtil.jsonToMap(geojson);
                    params.put(entry.getKey(), geojsonMap); // 替换参数
                }
            }
        }

        // 处理 GeoJSON 转文件逻辑
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof Map) {
                String jsonString = JsonUtil.serializeObject(entry.getValue());
                String filePath = writeGeoJsonToFile(jsonString);
                params.put(entry.getKey(), filePath); // 替换为文件路径
            }
        }
        System.out.println(params);
        // 执行 Python 脚本
        String result = callPythonScript(node.getModel_name(), params);
        Map<String, Object> resultMap = JsonUtil.jsonToMap(result);
        // 记录执行结果
        for (String outputKey : node.getOutput()) {
            results.put(outputKey, resultMap);
        }
    }

    // 写入 GeoJSON 文件
    private String writeGeoJsonToFile(String geoJson) throws IOException {
        String filePrefix = tempPath;
        File directory = new File(filePrefix);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File tempFile = File.createTempFile("geojson_", ".geojson", directory);
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(geoJson);
        }
        return tempFile.getAbsolutePath();
    }

    // 执行Python脚本
    public String callPythonScript(String modelName, Map<String, Object> params) throws Exception {
<<<<<<< Updated upstream
=======
//        String pythonScript = "H:\\data\\panoramaTW\\script\\" + modelName + ".py";
>>>>>>> Stashed changes
        String pythonScript = scriptPath + modelName + ".py";

        // 构造命令参数列表
        List<String> command = new ArrayList<>();
//        command.add("cmd.exe");
//        command.add("/c");
//        command.add("conda activate bankModel &&");
<<<<<<< Updated upstream
        command.add("python");
//        command.add("F:\\App\\anaconda3\\envs\\bankModel\\python.exe");
=======
//        command.add("python");
//        command.add("F:\\App\\anaconda3\\envs\\bankModel\\python.exe");
        command.add("conda");
        command.add("run");
        command.add("-n");
        command.add("bankModel");
        command.add("python");
>>>>>>> Stashed changes
        command.add(pythonScript);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            command.add("--" + entry.getKey()); // 添加参数名，例如 --buffer_distance
            command.add(JsonUtil.serializeObject(entry.getValue()));
        }

        log.info("Executing command: {}", String.join(" ", command));

        // 使用 ProcessBuilder 调用 Python 脚本
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
            return output.toString();
        }
    }
}


