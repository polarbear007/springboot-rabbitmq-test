package cn.itcast;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableRabbit
public class RabbitTransactionConfig {
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setUri("amqp://admin:admin@192.168.48.131:5672/transaction");
		return factory;
	}

	@Bean
	public RabbitAdmin rabbitAdmin() {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
		// 这个要设置成 true 才能有自动声明队列、交换器、绑定关系的功能。
		rabbitAdmin.setAutoStartup(true);
		return rabbitAdmin;
	}
	
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		rabbitTemplate.setMessageConverter(messageConverter());
		// 如果想要让rabbitTemplate 同步读写消息支持事务功能，那么一定要开启这个
		rabbitTemplate.setChannelTransacted(true);
		return rabbitTemplate;
	}

	@Bean("rabbitListenerContainerFactory")
	public RabbitListenerContainerFactory<SimpleMessageListenerContainer> containerFactory(@Autowired PlatformTransactionManager transactionManager){
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		factory.setMessageConverter(messageConverter());
		// 如果想要支持异步事务功能，那么一定要开启通道的事务支持
		// 如果这个设置为 false的话，那么就算事务回滚了，方法内部的操作可能也会生效
		factory.setChannelTransacted(true);
		// 同时还要给  MessageListenerContainer 设置一个事务管理器，这个事务管理器就用数据库的那个事务管理器就好了
		factory.setTransactionManager(transactionManager);
		
		// 默认情况下，事务回滚以后就会对消费的消息进行拒绝，那么我们可以通过这个设置这些拒绝的消息是否重新放回队列
		// 如果为 false 的话，那么被拒绝或者因为事务回滚的而未确认的消息会全部丢失
		// 不过如果为true 的话，很可能会变成死循环。 暂时不知道应该如何设置好一些。
		factory.setDefaultRequeueRejected(false);
		
		return factory;
	}
}
