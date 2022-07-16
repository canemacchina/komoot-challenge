package it.lorenzobugiani.infrastructure.di

import it.lorenzobugiani.domain.repository.UserRepository
import it.lorenzobugiani.domain.service.NotificationService
import it.lorenzobugiani.domain.service.WelcomeMessageService
import it.lorenzobugiani.domain.usecase.GenerateNotificationMessage
import it.lorenzobugiani.domain.usecase.GetUser
import it.lorenzobugiani.domain.usecase.SaveUser
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces

@ApplicationScoped
class DomainBeanProducer {

    @Produces
    fun buildSaveUser(userRepository: UserRepository): SaveUser = SaveUser(userRepository)

    @Produces
    fun buildGetUser(userRepository: UserRepository): GetUser = GetUser(userRepository)

    @Produces
    fun buildGenerateNotificationMessage(userRepository: UserRepository): GenerateNotificationMessage =
        GenerateNotificationMessage(userRepository)

    @Produces
    fun buildWelcomeMessageService(
        generateNotificationMessage: GenerateNotificationMessage,
        saveUser: SaveUser,
        getUser: GetUser,
        notificationService: NotificationService
    ): WelcomeMessageService =
        WelcomeMessageService(generateNotificationMessage, saveUser, getUser, notificationService)
}