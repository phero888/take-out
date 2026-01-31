package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    AliOssUtil aliOssUtil;
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("开始上传文件:{}",file);
        try {
            String originalFilename = file.getOriginalFilename();//获取文件原始名称
            //获取原始文件后缀名
            String substring = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String objectName = UUID.randomUUID() + substring;//生成唯一文件名
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);//上传文件
            return Result.success(filePath);

        } catch (IOException e) {
            log.error(MessageConstant.UPLOAD_FAILED + "{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
