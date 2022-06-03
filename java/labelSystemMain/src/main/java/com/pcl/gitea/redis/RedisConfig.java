package com.pcl.gitea.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.pcl.gitea.service.GiteaDataSetService;
import com.pcl.gitea.service.OBSDecompressService;

@EnableScheduling
@Configuration
public class RedisConfig extends CachingConfigurerSupport {


	@Value("${share.redis.subscribe1:LabelRedisQueue}")
	private String SUBSCRIBE_REDIS_ADD_ATTACH_TOPIC;  //启智社区数据集在minio上传完成监听通道

	@Value("${share.redis.subscribe2:LabelDecompressOBSQueue}")
	private String SUBSCRIBE_REDIS_DE_OBS_TOPIC;  //启智社区数据集在OBS上需要解压的监听通道

	@Value("${share.redis.subscribe3:LabelDatasetDeleteQueue}")
	private String SUBSCRIBE_REDIS_DELETE_TOPIC;  //启智社区数据集在OBS上需要解压的监听通道

	@Autowired
	private GiteaDataSetService datasetService;

	@Autowired
	private OBSDecompressService decompressService;

	@Bean
	public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		RedisSerializer<String> redisSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(redisSerializer);
		redisTemplate.setHashKeySerializer(redisSerializer);
		
		
		//JdkSerializationRedisSerializer序列化方式;
		JdkSerializationRedisSerializer jdkRedisSerializer=new JdkSerializationRedisSerializer();
		redisTemplate.setValueSerializer(jdkRedisSerializer);
		redisTemplate.setHashValueSerializer(jdkRedisSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(factory);
        return stringRedisTemplate;
    }

	//    /**
	//     * retemplate相关配置
	//     * @param factory
	//     * @return
	//     */
	//    @Bean
	//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
	//
	//        RedisTemplate<String, Object> template = new RedisTemplate<>();
	//        // 配置连接工厂
	//        template.setConnectionFactory(factory);
	//
	//        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
	//        Jackson2JsonRedisSerializer jacksonSeial = new Jackson2JsonRedisSerializer(Object.class);
	//
	//        ObjectMapper om = new ObjectMapper();
	//        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
	//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	//        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
	//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	//        jacksonSeial.setObjectMapper(om);
	//
	//        // 值采用json序列化
	//        template.setValueSerializer(jacksonSeial);
	//        //使用StringRedisSerializer来序列化和反序列化redis的key值
	//        template.setKeySerializer(new StringRedisSerializer());
	//
	//        // 设置hash key 和value序列化模式
	//        template.setHashKeySerializer(new StringRedisSerializer());
	//        template.setHashValueSerializer(jacksonSeial);
	//        template.afterPropertiesSet();
	//
	//        return template;
	//    }
	//
	/**
	 * 对hash类型的数据操作
	 *
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForHash();
	}

	/**
	 * 对redis字符串类型数据操作
	 *
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public ValueOperations<String, Object> valueOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForValue();
	}

	/**
	 * 对链表类型的数据操作
	 *
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForList();
	}

	/**
	 * 对无序集合类型的数据操作
	 *
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForSet();
	}

	/**
	 * 对有序集合类型的数据操作
	 *
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
		return redisTemplate.opsForZSet();
	}



	/**
	 * Redis消息监听器容器
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setTaskExecutor(RedisMsgThreadExecutor.getThreadPool());
		//订阅了一个叫dateset的通道，多通道
		container.addMessageListener(listenerAdapter(new RedisAddDataSetFileMsg(datasetService)),new PatternTopic(SUBSCRIBE_REDIS_ADD_ATTACH_TOPIC));
		container.addMessageListener(listenerAdapter(new RedisDecompressOBSFileMsg(decompressService)),new PatternTopic(SUBSCRIBE_REDIS_DE_OBS_TOPIC));
		container.addMessageListener(listenerAdapter(new RedisDeleteDataSetFileMsg(datasetService)),new PatternTopic(SUBSCRIBE_REDIS_DELETE_TOPIC));

		//这个container 可以添加多个 messageListener
		return container;
	}

	/**
	 * 配置消息接收处理类
	 * @param redisMsg  自定义消息接收类
	 * @return
	 */
	@Bean()
	@Scope("prototype")
	MessageListenerAdapter listenerAdapter(RedisMsg redisMsg) {
		//这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“receiveMessage”
		//也有好几个重载方法，这边默认调用处理器的方法 叫handleMessage 可以自己到源码里面看
		return new MessageListenerAdapter(redisMsg, "receiveMessage");//注意2个通道调用的方法都要为receiveMessage
	}

}