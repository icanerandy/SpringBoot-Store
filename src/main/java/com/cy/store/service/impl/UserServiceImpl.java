package com.cy.store.service.impl;

import com.cy.store.entity.User;
import com.cy.store.mapper.UserMapper;
import com.cy.store.service.IUserService;
import com.cy.store.service.ex.InsertException;
import com.cy.store.service.ex.UsernameDuplicateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.xml.crypto.dsig.DigestMethod;
import java.util.Date;
import java.util.UUID;

/** 处理用户数据的业务层实现类 **/
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public void reg(User user) {
        /*
        * 1. 根据参数user对象获取注册的用户名
        * 2. 调用持久层的User findByUsername(String username)方法，根据用户名查询用户数据
        * 3. 判断查询结果是否不为null
        *       是：表示用户名已被占用，则抛出UsernameDuplicateException异常
        *
        * 4.
        *   创建当前时间对象
        *   补全数据：加密后的密码
        *   补全数据：盐值
        *   补全数据：isDelete(0)
        *   补全数据：4项日志属性
        *
        * 5. 表示用户名没有被占用，则允许注册
        *       调用持久层Integer insert(User user)方法，执行注册并获取返回值（受影响的行数）
        *       判断受影响的行数是否不为1
        *           是：插入数据时出现某种错误，则抛出InsertException异常
        * */

        // reg()方法具体实现过程：
        /* 1. 根据参数user对象获取注册的用户名 */
        String username = user.getUsername();
        /* 2. 调用持久层的User findByUsername(String username)方法，根据用户名查询用户数据 */
        User result = userMapper.findByUsername(username);
        /* 3. 判断查询结果是否不为null */
        if (result != null) {
            // 是：表示用户名已被占用，则抛出UsernameDuplicateException异常
            throw new UsernameDuplicateException("尝试注册的用户名[" + username + "]已经被占用");
        }

        /* 4. */
        // 创建当前时间对象
        Date now = new Date();
        // 补全数据：加密后的密码
        String salt = UUID.randomUUID().toString().toUpperCase();
        String md5Password = getMd5Password(user.getPassword(), salt);
        user.setPassword(md5Password);
        // 补全数据：isDelete(0)
        user.setIsDelete(0);
        // 补全数据：4项日志属性
        user.setCreatedUser(username);
        user.setCreatedTime(now);
        user.setModifiedUser(username);
        user.setModifiedTime(now);

        /* 5. 表示用户名没有被占用，则允许注册 */
        // 调用持久层Integer insert(User user)方法，执行注册并获取返回值（受影响的行数）
        Integer rows = userMapper.insert(user);
        // 判断受影响的行数是否不为1
        if (rows != 1) {
            // 是：插入数据时出现某种错误，则抛出InsertException异常
            throw new InsertException("添加用户数据出现未知异常，请联系管理员");
        }
    }

    /**
     * 执行密码加密
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密文
     */
    private String getMd5Password(String password, String salt) {
        /*
        * 加密规则：
        * 1. 无视原始密码的强度
        * 2. 使用UUID作为盐值，在原始密码的两侧拼接
        * 3. 循环加密3次
        * */
        for (int i = 0; i < 3; i++) {
            password = DigestUtils.md5DigestAsHex((salt + password + salt).getBytes()).toUpperCase();
        }

        return password;
    }
}
