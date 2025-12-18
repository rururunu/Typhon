package com.typhon.typhon.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.typhon.typhon.entity.User;
import com.typhon.typhon.mapper.UserMapper;
import com.typhon.typhon.service.UserService;
import org.springframework.stereotype.Component;

import static com.typhon.typhon.entity.table.UserTableDef.USER;

@Component
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public User getValidUser(String account) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(
                        USER.ACCOUNT.eq(account).or(USER.EMAIL.eq(account))
                );
        User user = mapper.selectOneWithRelationsByQuery(queryWrapper);
        //TODO:
        //  q: 为什么不内连对象
        //  a: 后续方便直接拓展 user 的信息
        return user;
    }

}
