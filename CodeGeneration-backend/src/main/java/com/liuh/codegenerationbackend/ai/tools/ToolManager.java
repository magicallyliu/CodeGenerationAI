package com.liuh.codegenerationbackend.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 工具管理器, 统一的管理所有工具
 */

@SuppressWarnings("all")
@Component
@Slf4j
public class ToolManager {

    /**
     * 工具的名称对应工具的类型
     * 工具名称和工具实例的映射
     */
    private final Map<String,  BaseTool> toolMap = new HashMap<>();

    /**
     * 自动注入所有工具
     */
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化工具映射
     */
    @PostConstruct
    public void initTools(){
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {}", tool.getDisplayName());
        }
        log.info("工具注册完成, 已注册工具数量: {}", toolMap.size());
    }

    /**
     *  根据工具名称获取功能实例
     */
    public BaseTool getTool(String toolName){
        return toolMap.get(toolName);
    }

    /**
     *  获取已注册的工具集合
     */
    public BaseTool[] getAllTools(){
        return tools;
    }
}
