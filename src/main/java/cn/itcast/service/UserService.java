package cn.itcast.service;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.itcast.mapper.UserMapper;
import cn.itcast.pojo.User;

@Service
public class UserService {
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	// 从数据库中查找数据，然后发送消息到 rabbitmq 服务器
	@Transactional
	public void informAllUser() {
		List<User> list = userMapper.findAll();
		if(list != null && list.size() > 0) {
			for (User user : list) {
				rabbitTemplate.convertAndSend("default.exchange", "tx.infrom.user.key", user);
			}
		}
	}
	
	// 从rabbitmq 服务器消费消息，然后回调处理方法，在方法中把相关的数据保存到数据库中
	@Transactional
	@RabbitListeners({
		@RabbitListener(bindings= {
				@QueueBinding(
								value=@Queue("tx.add.user.queue"),
								exchange=@Exchange("default.exchange"),
								key="tx.add.user.key"
							)
		}),
		@RabbitListener(queues="tx.add.user.queue")
	})
	public void addUser(User user) {
		System.out.println("接收到一个添加用户的任务：" + user);
		userMapper.addUser(user);
	}
}
