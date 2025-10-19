package com.liuh.codegenerationbackend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.liuh.codegenerationbackend.model.entity.ChatHistory;
import com.liuh.codegenerationbackend.mapper.ChatHistoryMapper;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
