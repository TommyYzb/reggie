package com.itheima.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获sql语句错误异常
     */

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.info(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {
            // 将重复用户名取出
            String[] username = ex.getMessage().split(" ");
            // 设置错误消息
            String msg = "账户:" + username[2] + "已存在";
            // 返回错误消息
            return R.error(msg);

        }
        return R.error("未知错误");

    }


    /**
     * 捕获自定义异常
     */

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }


}
