package com.kankankeke.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kankankeke.reggie.entity.Employee;
import com.kankankeke.reggie.mapper.EmployeeMapper;
import com.kankankeke.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
