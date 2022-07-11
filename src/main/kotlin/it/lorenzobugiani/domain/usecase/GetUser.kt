package it.lorenzobugiani.domain.usecase

import it.lorenzobugiani.domain.repository.UserRepository

class GetUser(private val userRepository: UserRepository) {
    fun execute(id: Long) = userRepository.getUser(id)
}