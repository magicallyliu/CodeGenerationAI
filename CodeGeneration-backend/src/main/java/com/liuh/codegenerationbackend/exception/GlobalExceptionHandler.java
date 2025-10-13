package com.liuh.codegenerationbackend.exception;


import com.liuh.codegenerationbackend.common.BaseResponse;
import com.liuh.codegenerationbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description 全局异常处理器
 */

@SuppressWarnings("all")
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 发生已封装好的业务异常种类时, 进行调用, 用于返回前端
     *
     * @param e 业务异常
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("发生的业务异常: ", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 系统发生运行异常时调用
     *
     * @param e 系统运行异常
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> RuntimeExceptionHandler(RuntimeException e) {
        log.error("发生运行异常: ", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统运行异常");
    }


}
