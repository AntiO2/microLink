package org.microserviceteam.workflow.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.microserviceteam.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}