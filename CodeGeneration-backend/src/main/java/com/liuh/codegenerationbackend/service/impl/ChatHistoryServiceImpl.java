package com.liuh.codegenerationbackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.codegenerationbackend.constant.UserConstant;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.exception.ThrowUtils;
import com.liuh.codegenerationbackend.mapper.UserMapper;
import com.liuh.codegenerationbackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.liuh.codegenerationbackend.model.entity.App;
import com.liuh.codegenerationbackend.model.entity.User;
import com.liuh.codegenerationbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.liuh.codegenerationbackend.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.liuh.codegenerationbackend.model.entity.ChatHistory;
import com.liuh.codegenerationbackend.mapper.ChatHistoryMapper;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
@Slf4j
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, User loginUser) {
        //1.非空效验
        ThrowUtils.throwIf(ObjUtil.isNull(appId) || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "聊天消息不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "聊天消息类型不能为空");
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.PARAMS_ERROR, "用户不能为空");
        //消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(ObjUtil.isNull(messageTypeEnum), ErrorCode.PARAMS_ERROR, "聊天消息类型无效");

        //2.添加聊天记录 -- 插入数据库
        //在  ChatHistory 中 使用了 @Builder注解, 可以以构造器的形式传入次数
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(loginUser.getId())
                .build();
        return save(chatHistory);

    }

    @Override
    public boolean deleteByAppId(Long appId) {
        //1.非空效验
        ThrowUtils.throwIf(ObjUtil.isNull(appId) || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        //2.删除聊天记录
        return remove(QueryWrapper.create().eq("appId", appId));
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        //效验权限
        ThrowUtils.throwIf(ObjUtil.isNull(appId) || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize >= 50, ErrorCode.PARAMS_ERROR, "每页条数必须大于0且小于50");
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.PARAMS_ERROR, "用户不能为空");

        //只有管理员和应用所有者可以查询
        App app = appService.getById(appId);
        ThrowUtils.throwIf(ObjUtil.isNull(app), ErrorCode.PARAMS_ERROR, "应用不存在");
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NO_AUTH_ERROR, "无权限查询应用对话历史");

        //构造查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = ChatHistoryQueryRequest.builder()
                .appId(appId)
                .lastCreateTime(lastCreateTime)
                .build();

        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);

        //分页查询
        return this.page(Page.of(1,pageSize), queryWrapper);

    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        //1.非空效验
        ThrowUtils.throwIf(ObjUtil.isNull(appId) || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(ObjUtil.isNull(chatMemory), ErrorCode.PARAMS_ERROR, "对话记忆不能为空");
        ThrowUtils.throwIf(maxCount <= 0 || maxCount >= 30, ErrorCode.PARAMS_ERROR, "每页条数必须大于0且小于30");
        try {
            //查询会话记忆
            QueryWrapper limit = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .select(ChatHistory::getMessage, ChatHistory::getMessageType)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);//在ai生成前, 就已经把最先提问上传到数据库中了, 所以这里从第二条开始(调用ai后, 先获取会话记忆)
            List<ChatHistory> historyList = this.list(limit);
            if (ObjUtil.isEmpty(historyList)) {
                return 0;
            }

            //反转列表, 按照时间正序
            historyList= historyList.reversed();


            //加载成功消息的总数
            int count =  0;
            //先清理会话记忆再加载
            chatMemory.clear();
            //加载会话记忆
            for (ChatHistory chatHistory : historyList) {
                //用户消息
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                    count++;
                }else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())) {
                    //ai消息
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                    count++;
                }
            }
            log.info("加载会话记忆成功, 应用id: {}, 加载成功消息条数: {}", appId, count);
            //返回加载成功的消息条数
            return count;
        } catch (Exception e) {
            log.error("加载会话记忆失败, 应用id: {}", appId, e);
            return 0;
        }
    }


    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        //1.非空效验
        ThrowUtils.throwIf(ObjUtil.isNull(chatHistoryQueryRequest), ErrorCode.PARAMS_ERROR, "查询请求不能为空");

        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();

        //常规查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);

        //游标查询 -- 只使用 createTime 字段
        if (ObjUtil.isNotNull(lastCreateTime)) {
            queryWrapper.lt("createTime", lastCreateTime);
        }

        //排序字段
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            //默认排序
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }
}
