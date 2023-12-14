package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Employee;
import com.itheima.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

//        1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//        2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

//        3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("用户名不存在");
        }
//        4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("账号或密码错误");
        }

//        5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果6、登录成功，将员工id存入Session并返回登录成功结果
        if (emp.getStatus() == 0) {
            return R.error("账户已禁用!");
        }

        // 6. 登录成功！将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }


    /**
     * 员工退出
     */


    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().getAttribute("employee");
        return R.success("退出成功!");
    }


    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest req, @RequestBody Employee employee) {
        log.info("新增员工:{}", employee.toString());

        // 设置初始密码123456,进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //设置创建和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 获取当前用户的id
        Long empId = (Long) req.getSession().getAttribute("employee");

        // 设置创建和修改人
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        // 执行业务层的新增方法
        employeeService.save(employee);

        // 返回执行结果
        return R.success("新增员工成功!");
    }

    /**
     * 分页查询方法
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        // 创建分页构造器

        Page pageInfo = new Page();

        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        // 添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo, queryWrapper);

        // 返回结果
        return R.success(pageInfo);

    }

    /**
     * 根据id修改员工状态
     */


    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("修改成功!");

    }


    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询的信息...");
        Employee employee = employeeService.getById(id);

        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");

    }


}
