package bszeti.camelspringboot.jmstest;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnExpression("'${connection.type}'!='MQTT'")
public class JMSRoutes extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .maximumRedeliveries("{{exception.maximumredeliveries}}")
            .log(LoggingLevel.ERROR,"***************************************\n********************** Camel onException: ${exception}\n***************************************")
        ;

        
        from("amq:{{receive.endpoint}}")
             .routeId("amq.receive").autoStartup("{{receive.enabled}}")
             .transacted("jmsSendTransaction")
             .to("direct:receiveMessage");
        
        from("direct:messageDelivery")
        .routeId("amq.message.delivery")
        .to("amq:{{send.endpoint}}");
        
        from("direct:messageForward")
        .routeId("amq.message.forward")
        .to("amq:{{receive.forward.endpoint}}");

    }

}
