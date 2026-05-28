package com.isoft.consultant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isoft.consultant.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    Optional<User> findByUsername(String username);

    @Select("SELECT * FROM user WHERE email = #{email} AND deleted = 0")
    Optional<User> findByEmail(String email);

    @Select("SELECT * FROM user WHERE phone = #{phone} AND deleted = 0")
    Optional<User> findByPhone(String phone);

    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    long countActiveUsers();
}
