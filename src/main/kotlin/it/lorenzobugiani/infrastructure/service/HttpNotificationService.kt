package it.lorenzobugiani.infrastructure.service

import com.fasterxml.jackson.annotation.JsonProperty
import it.lorenzobugiani.domain.service.NotificationService
import it.lorenzobugiani.domain.usecase.NotificationMessage
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.POST
import javax.ws.rs.Path
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.eclipse.microprofile.rest.client.inject.RestClient


@ApplicationScoped
class HttpNotificationService(
    @ConfigProperty(name = "notification.sender") private val sender: String,
    @RestClient private val client: NotificationServiceClient
) : NotificationService {
    override fun send(message: NotificationMessage) {
        val httpNotification = HttpNotification(
            sender,
            message.receiver,
            message.message,
            message.ids
        )
        client.send(httpNotification)
    }
}

data class HttpNotification(
    val sender: String,
    val receiver: Long,
    val message: String,
    @JsonProperty("recent_user_ids")
    val recentUserIds: List<Long>
)

@Path("/")
@RegisterRestClient(configKey = "notification-service-client")
interface NotificationServiceClient {
    @POST
    fun send(notification: HttpNotification)
}