package it.lorenzobugiani.domain.service

import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.usecase.GenerateNotificationMessage
import it.lorenzobugiani.domain.usecase.GetUser
import it.lorenzobugiani.domain.usecase.SaveUser
import org.jboss.logging.Logger

private val log: Logger = Logger.getLogger(WelcomeMessageService::class.java)

class WelcomeMessageService(
    private val generateNotificationMessage: GenerateNotificationMessage,
    private val saveUser: SaveUser,
    private val getUser: GetUser,
    private val notificationService: NotificationService
) {
    fun sendTo(newUser: User) {
        val user = getUser.execute(newUser.id)

        if (user != null) {
            log.info("Found duplicated message for user $user")
            return
        }

        val message = generateNotificationMessage.execute(newUser)
        saveUser.execute(newUser)
        notificationService.send(message)
    }
}