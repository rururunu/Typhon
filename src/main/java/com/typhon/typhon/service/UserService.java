package com.typhon.typhon.service;

import com.mybatisflex.core.service.IService;
import com.typhon.typhon.entity.User;

/**
 *  服务层。
 *
 * @author 35837
 * @since 2025-12-17
 */
public interface UserService extends IService<User> {

    User getValidUser(String account);
}
