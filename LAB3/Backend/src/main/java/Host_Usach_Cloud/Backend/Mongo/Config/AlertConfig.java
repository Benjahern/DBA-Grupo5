package Host_Usach_Cloud.Backend.Mongo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;

@Configuration
public class AlertConfig {
    @Bean
    public MessageListenerContainer messageListenerContainer(MongoTemplate mongoTemplate) {
        // Crea el contenedor
        MessageListenerContainer container = new DefaultMessageListenerContainer(mongoTemplate);
        // Inicia el contenedor para que comience a escuchar eventos
        container.start();
        return container;
    }
}
