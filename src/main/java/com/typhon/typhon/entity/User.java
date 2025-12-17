package com.typhon.typhon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.activerecord.Model;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 实体类。
 *
 * @author 35837
 * @since 2025-12-17
 */
@Accessors(chain = true)
@Data(staticConstructor = "create")
@Table("user")
public class User extends Model<User> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.uuid)
    private String id;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 名称
     */
    private String name;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String introduction;

    private String email;
}
