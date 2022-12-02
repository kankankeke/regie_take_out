package com.kankankeke.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kankankeke.reggie.entity.User;
import com.kankankeke.reggie.mapper.UserMapper;
import com.kankankeke.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
