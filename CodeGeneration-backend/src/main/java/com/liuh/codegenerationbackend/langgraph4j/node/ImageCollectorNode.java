package com.liuh.codegenerationbackend.langgraph4j.node;

import com.liuh.codegenerationbackend.langgraph4j.ai.ImageCollectionService;
import com.liuh.codegenerationbackend.langgraph4j.model.enums.ImageCategoryEnum;
import com.liuh.codegenerationbackend.langgraph4j.model.ImageResource;
import com.liuh.codegenerationbackend.langgraph4j.state.WorkflowContext;
import com.liuh.codegenerationbackend.utils.SpringContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 *     @Description 图片收集节点
 */

@Slf4j
public class ImageCollectorNode {

    @Resource


    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            
            //获取用户提示词
            String originalPrompt = context.getOriginalPrompt();
            //初始图片
            String imageListStr = "";
            try {
                //调用图片ai图片搜集服务
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                //使用ai服务智能搜集图片
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片搜集失败", e);
            }

            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
