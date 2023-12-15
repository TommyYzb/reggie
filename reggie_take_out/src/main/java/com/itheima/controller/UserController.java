package com.itheima.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 发送邮箱短信验证码
     * @param user
     * @return
     */

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user,HttpSession session) {
        // 获取手机号
        String email = user.getPhone();
        String subject = "瑞吉外卖";

        if (StringUtils.isNotEmpty(email)) {
            // 生成6位随机的验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            String text = "[瑞吉外卖]: 您好，您的登录验证码为:"+ code + ",请保护好您的邮箱，并且尽快登录,如非本人操作，请忽略此邮件。";
            log.info("code = {}",code);

            userService.sendMsg(email,subject,text);
            session.setAttribute(email,code);

            return R.success("邮箱短信验证码发送成功!");

        }

        return R.error("邮箱短信验证码发送失败!");
    }


    //登录
    @PostMapping("/login")
    //Map存JSON数据
    public R<User> login(HttpSession session,@RequestBody Map map){
        //获取邮箱，用户输入的，这个phone就是输入的邮箱
        String phone = map.get("phone").toString();
        //获取验证码，用户输入的，这个code就是生成的验证码
        String code = map.get("code").toString();
        /**
         * 获取session中保存的验证码
         * 登录的邮箱作为session的key值，将code最为value
         * 因此邮箱和验证码可以一一对应，保证邮箱验证码数据一致完整性
         * */
        Object sessionCode = session.getAttribute(phone);
        //将session的验证码和用户输入的验证码进行比对
        if (sessionCode != null && sessionCode.equals(code)) {
            //要是User数据库没有这个邮箱则自动注册,先看看输入的邮箱是否存在数据库
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            //获得唯一的用户，因为邮箱是唯一的
            User user = userService.getOne(queryWrapper);
            //要是User数据库没有这个邮箱则自动注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                //取邮箱的前五位为用户名
                user.setName("用户"+phone.substring(0,5));
                userService.save(user);
            }
            //不保存这个用户名就登不上去，因为过滤器需要得到这个user才能放行，程序才知道你登录了
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }


    @PostMapping("/loginout")
    public R<String> loginout (HttpServletRequest request) {
        log.info("登出中。。。");
       request.getSession().removeAttribute("user");
        return R.success("登出成功!");
    }

}
