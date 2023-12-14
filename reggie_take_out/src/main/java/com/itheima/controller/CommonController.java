package com.itheima.controller;

import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 负责文件的上传和下载
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String BasePath;


    @PostMapping("/upload")
    public R<String>  upload(MultipartFile  file) throws Exception {

        // file是一个临时文件,需要转存到指定位置,否则本次请求完成之后临时文件会被删除
        log.info(file.toString());

        // 原文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName = UUID.randomUUID().toString() + suffix;

        // 创建一个目录对象
        File dir = new File(BasePath);

        // 判断当前目录是否已存在
        if (!dir.exists()){
            // 目录不存在，创建
            dir.mkdirs();
        }


        file.transferTo(new File(BasePath + fileName ));

        return R.success(fileName);
    }


    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws Exception {
        // 通过输入流读取文件内容
        FileInputStream inputStream = new FileInputStream(new File(BasePath + name));

        // 通过输出流将文件写回浏览器(回显图片)
        ServletOutputStream outputStream = response.getOutputStream();

        response.setContentType("image/jpeg");

        int len = 0;
        byte[] bytes = new byte[1024];

        while((len  = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }

        // 释放资源
        outputStream.close();
        inputStream.close();
    }





}
