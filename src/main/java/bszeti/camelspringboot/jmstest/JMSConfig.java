package bszeti.camelspringboot.jmstest;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties(prefix = "connection")
@ConditionalOnExpression("'${connection.type}'!='MQTT'")
public class JMSConfig {
    private static final Logger log = LoggerFactory.getLogger(JMSConfig.class);

    private String type;
    private String remoteUrl;
    private String username;
    private String password;
    private String clientId;
    private Boolean useCachingConnectionFactory;
    private Boolean useAnonymousProducers;
    private Integer maxConnections;
    private Integer sessionCacheSize;


	 @Bean(name="jms")
	 public JmsComponent jmsComponent(@Autowired ConnectionFactory pooledConnectionFactory) {
	 	JmsComponent component = JmsComponent.jmsComponent(pooledConnectionFactory);
	 	return component;
	 }

    @Bean(name="sjms2")
    public Sjms2Component sjms2Component(@Autowired ConnectionFactory pooledConnectionFactory) {
        Sjms2Component component = new Sjms2Component();
        component.setConnectionFactory(pooledConnectionFactory);
        component.setConnectionClientId(clientId);
        return component;
    }


	@Bean
	public JmsTransactionManager myTransactionManager(@Autowired ConnectionFactory pooledConnectionFactory){
		return new JmsTransactionManager(pooledConnectionFactory);
	}

	@Bean
	public SpringTransactionPolicy jmsTransactionPolicy(@Autowired JmsTransactionManager jmsTransactionManager, @Value("${receive.forward.propagation}") String transactionPropagation){
		SpringTransactionPolicy transactionPolicy = new SpringTransactionPolicy(jmsTransactionManager);
		transactionPolicy.setPropagationBehaviorName(transactionPropagation);
		return transactionPolicy;
	}
    
    //AMQP ConnectionFactory
    public ConnectionFactory amqpConnectionFactory(){
        JmsConnectionFactory factory = new JmsConnectionFactory(remoteUrl);

        if (StringUtils.hasLength(this.getUsername())) {
            factory.setUsername(this.getUsername());
        }
        if (StringUtils.hasLength(this.getPassword())) {
            factory.setPassword(this.getPassword());
        }

        return factory;
    }

    //CORE ConnectionFactory
    public ConnectionFactory coreConnectionFactory(){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(remoteUrl);

        if (StringUtils.hasLength(this.getUsername())) {
            factory.setUser(this.getUsername());
        }
        if (StringUtils.hasLength(this.getPassword())) {
            factory.setPassword(this.getPassword());
        }

        return factory;
    }

    //Openwire ConnectionFactory
    public ConnectionFactory openwireConnectionFactory(){

        org.apache.activemq.ActiveMQConnectionFactory factory = new org.apache.activemq.ActiveMQConnectionFactory(remoteUrl);

        if (StringUtils.hasLength(this.getUsername())) {
            factory.setUserName(this.getUsername());
        }
        if (StringUtils.hasLength(this.getPassword())) {
            factory.setPassword(this.getPassword());
        }

        return factory;
    }


    //Non-pooled ConnectionFactory - used below
    public ConnectionFactory singleConnectionFactory() {
        ConnectionFactory cf = null;
        switch (this.getType()) {
            case "AMQP":
                cf = amqpConnectionFactory();
                log.info("AMQP ConnectionFactory");
                break;
            case "CORE":
                cf = coreConnectionFactory();
                log.info("CORE ConnectionFactory");
                break;
            case "OPENWIRE":
                cf = openwireConnectionFactory();
                log.info("OPENWIRE ConnectionFactory");
                break;
        }
        return cf;
    }

    //Pooled ConnectionFactory
    @Bean
    @Primary
    public ConnectionFactory pooledConnectionFactory() {
        if (useCachingConnectionFactory) {

            // Spring CachingConnectionFactory
            CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
            cachingConnectionFactory.setTargetConnectionFactory(singleConnectionFactory());
            cachingConnectionFactory.setSessionCacheSize(sessionCacheSize);
//            cachingConnectionFactory.set
            return cachingConnectionFactory;
        } else {
            // MessagingHub JmsPoolConnectionFactory
            JmsPoolConnectionFactory jmsPoolConnectionFactory = new JmsPoolConnectionFactory();
            jmsPoolConnectionFactory.setConnectionFactory(singleConnectionFactory());
            jmsPoolConnectionFactory.setMaxConnections(maxConnections);
            //setUseAnonymousProducers=true causes problems with org.messaginghub.pooled.jms.JmsPoolConnectionFactory when AMQ Address start paging
            jmsPoolConnectionFactory.setUseAnonymousProducers(useAnonymousProducers);
            return jmsPoolConnectionFactory;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Boolean getUseCachingConnectionFactory() {
        return useCachingConnectionFactory;
    }

    public void setUseCachingConnectionFactory(Boolean useCachingConnectionFactory) {
        this.useCachingConnectionFactory = useCachingConnectionFactory;
    }

    public Integer getSessionCacheSize() {
        return sessionCacheSize;
    }

    public void setSessionCacheSize(Integer sessionCacheSize) {
        this.sessionCacheSize = sessionCacheSize;
    }

    public Boolean getUseAnonymousProducers() {
        return useAnonymousProducers;
    }

    public void setUseAnonymousProducers(Boolean useAnonymousProducers) {
        this.useAnonymousProducers = useAnonymousProducers;
    }
}