package cn.itcast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.itcast.pojo.User;
import cn.itcast.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootRabbitmqTestApplicationTests {

	@Autowired
	private RabbitTemplate template;
	
	@Autowired
	private UserService userService;
	
	// 我们想要调用service 类里面的  addUser() 方法，不能直接通过 userService.addUser() 
	// 而是要给指定的队列发送消息
	@Test
	public void contextLoads() throws InterruptedException {
		User user = new User();
		user.setUsername("eric");
		user.setPassword("123456");
		template.convertAndSend("default.exchange", "tx.add.user.key", user);
		User user2 = new User();
		user2.setUsername("rose");
		user2.setPassword("123456");
		template.convertAndSend("default.exchange", "tx.add.user.key", user2);
		User user3 = new User();
		user3.setUsername("jack");
		user3.setPassword("123456");
		template.convertAndSend("default.exchange", "tx.add.user.key", user3);
		
		// 留个 5 秒来处理任务
		Thread.sleep(5*1000);
	}
	
	// 测试一下 informAllUser() 方法
	// 这个方法没有 @RabbitListener ，所以我们可以直接通过  userService.informAllUser() 调用
	@Test
	public void test() throws InterruptedException {
		userService.informAllUser();
		// 留个 5 秒来处理任务
		Thread.sleep(5*1000);
	}

}
