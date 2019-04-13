package cn.itcast.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import cn.itcast.pojo.User;

@Mapper
public interface UserMapper {
	@Insert("insert into t_user(username, password) values(#{username}, #{password})")
	public abstract Long addUser(User user); 
	
	@Select("select * from t_user")
	public abstract List<User> findAll();
	
}
