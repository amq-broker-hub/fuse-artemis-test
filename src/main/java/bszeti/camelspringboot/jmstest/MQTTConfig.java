package bszeti.camelspringboot.jmstest;

import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.camel.CamelContext;
import org.apache.camel.component.paho.PahoComponent;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

@Configuration
@ConfigurationProperties(prefix = "connection")
@ConditionalOnExpression("'${connection.type}'=='MQTT'")
public class MQTTConfig {

	private static final Logger log = LoggerFactory.getLogger(MQTTConfig.class);

	private String type;
	private String remoteUrl;
	private String username;
	private String password;



	@Bean(name = "paho")
	public PahoComponent pahoComponent(@Autowired CamelContext camelContext) {

		log.info("MQTT protocol in use");
		
		PahoComponent pahoComponent = new PahoComponent(camelContext);

		MqttConnectOptions mqttOptions = new MqttConnectOptions();

		mqttOptions.setPassword(password.toCharArray());
		mqttOptions.setUserName(username);

		/*
		 * If CleanSession = true, all state information is discarded when connecting to, or
		 * disconnecting from, the MQTT broker
		 * 
		 * For durable subscriptions, it must be 'false'
		 */
		mqttOptions.setCleanSession(true);
		mqttOptions.setAutomaticReconnect(true);
		mqttOptions.setConnectionTimeout(30);
		mqttOptions.setKeepAliveInterval(60); // 60 seconds
		mqttOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

		// TODO create custom SSLSocketFactory instance when broker URL uses 'ssl' schema instead of 'tcp'
		//options.setSocketFactory(getSSLSocketFactory());
		
		pahoComponent.setBrokerUrl(remoteUrl);
		pahoComponent.setConnectOptions(mqttOptions);

		/* When cleanSession is set to false, an application must ensure it uses the
		 * same client identifier when it reconnects to the server to resume previous state and maintain
		 * assured message delivery.
		 */		
		// TODO when this method is set the exception 'org.eclipse.paho.client.mqttv3.MqttException: Client is not connected' is raised. 
		//pahoComponent.setClientId(MqttClient.generateClientId());
		
		return pahoComponent;
	}

	// Empty transaction manager, because there is none in case of MQTT
	@Bean
	public SpringTransactionPolicy dummyTransactionPolicy(){
		SpringTransactionPolicy transactionPolicy = new SpringTransactionPolicy();
		transactionPolicy.setTransactionManager(new AbstractPlatformTransactionManager() {
			@Override
			protected Object doGetTransaction() throws TransactionException {
				return null;
			}

			@Override
			protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
				log.debug("doBegin {}, {}", transaction, definition);
			}

			@Override
			protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
				log.debug("doCommit {}", status);
			}

			@Override
			protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
				log.debug("doRollback {}", status);
			}
		});
		return transactionPolicy;
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
}