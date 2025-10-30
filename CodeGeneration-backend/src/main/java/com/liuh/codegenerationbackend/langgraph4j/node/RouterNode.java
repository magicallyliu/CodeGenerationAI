package com.liuh.codegenerationbackend.langgraph4j.node;

import com.liuh.codegenerationbackend.ai.service.AiCodeGenTypeRoutingService;
import com.liuh.codegenerationbackend.langgraph4j.state.WorkflowContext;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import com.liuh.codegenerationbackend.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 *  @Description 智能路由节点
 *
 */

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");
            
            CodeGenTypeEnum generationType;
            try {
                //获取路由ai服务
                AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                //调用ai服务进行路由决策(根据原始提示词判断)
                generationType = aiCodeGenTypeRoutingService.routeCodeGenType(context.getOriginalPrompt());
                log.info("路由决策结果: {}", generationType.getText());
            } catch (Exception e) {
                log.error("路由决策失败", e);
                //默认使用HTML类型
                generationType = CodeGenTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            log.info("路由决策完成，选择类型: {}", generationType.getText());
            return WorkflowContext.saveContext(context);
        });
    }
}
