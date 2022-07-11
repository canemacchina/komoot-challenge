package it.lorenzobugiani.domain.usecase

import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository

class SaveUser(private val userRepository: UserRepository) {
    fun execute(user: User) {
        userRepository.store(user)
    }
}