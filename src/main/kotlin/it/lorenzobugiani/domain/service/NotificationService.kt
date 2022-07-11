package it.lorenzobugiani.domain.service

import it.lorenzobugiani.domain.usecase.NotificationMessage

interface NotificationService {
    fun send(message: NotificationMessage)
}