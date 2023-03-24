package bszeti.camelspringboot.jmstest;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnExpression("'${connection.type}'=='MQTT'")
public class MQTTRoutes extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .maximumRedeliveries("{{exception.maximumredeliveries}}")
            .log(LoggingLevel.ERROR,"***************************************\n********************** Camel onException: ${exception}\n***************************************")
        ;

        
        from("paho:{{receive.endpoint}}")
             .routeId("mqtt.receive").autoStartup("{{receive.enabled}}")
             .to("direct:receiveMessage");
        
        from("direct:messageDelivery")
        .routeId("mqtt.message.delivery")
        .to("paho:{{send.endpoint}}");
        
        from("direct:messageForward")
        .routeId("mqtt.message.forward")
        .to("paho:{{receive.forward.endpoint}}");

    }

}
