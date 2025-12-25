package org.microserviceteam.workflow.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.microserviceteam.common.Result;
import org.microserviceteam.common.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workflow/manage")
public class WorkflowManageController {

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping("/definitions")
    public Result<List<String>> getDefinitions() {
        // 查询数据库中已部署的流程定义
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list();

        List<String> names = list.stream()
                .map(pd -> pd.getKey() + " (v" + pd.getVersion() + ")")
                .collect(Collectors.toList());

        return Result.success(names);
    }

    @PostMapping("/flush-all")
    public Result<List<String>> flushAll() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 匹配 resources/processes 及其子目录下所有 bpmn20.xml 或 xml 文件
        Resource[] resources = resolver.getResources("classpath*:processes/**/*.{bpmn20.xml,xml}");

        if (resources.length == 0) {
            return Result.error(ResultCode.SYSTEM_ERROR, "未找到流程定义文件");
        }

        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                .name("Batch_Flush_" + System.currentTimeMillis());

        for (Resource resource : resources) {
            deploymentBuilder.addClasspathResource("processes/" + resource.getFilename());
        }

        // 执行部署
        Deployment deployment = deploymentBuilder.deploy();

        // 查询最新部署的所有流程信息
        List<ProcessDefinition> deployedDefs = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .list();

        List<String> summary = deployedDefs.stream()
                .map(pd -> pd.getKey() + " (v" + pd.getVersion() + ")")
                .collect(Collectors.toList());

        return Result.success(summary);
    }
}