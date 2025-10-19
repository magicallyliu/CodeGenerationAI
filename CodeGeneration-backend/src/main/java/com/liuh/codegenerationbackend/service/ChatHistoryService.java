package com.liuh.codegenerationbackend.service;

import com.liuh.codegenerationbackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.liuh.codegenerationbackend.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.liuh.codegenerationbackend.model.entity.ChatHistory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     *
     * @param appId       应用id
     * @param message     聊天消息 -- 提示词
     * @param messageType 用户/ai
     * @param loginUser   登录用户
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, User loginUser);

    /**
     * 根据应用id 关联删除 对话记录
     *
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);

    /**
     * 分页查询某app的对话记录
     * 游标查询服务
     *
     * @param appId          应用id
     * @param pageSize       每页条数
     * @param lastCreateTime 最后一条记录 的创建时间
     * @param loginUser      登录用户
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 把数据库的对话记录, 加载到对话记忆中
     *
     * @param appId      应用id
     * @param chatMemory 对话记忆 -- 目标保存的会话记忆
     * @param maxCount   对话记忆最大加载条数
     * @return 加载成功的条数
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int  maxCount);


    /**
     * 构造查询条件
     *
     * @param chatHistoryQueryRequest 对话历史查询请求 -- 使用了游标查询
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}
