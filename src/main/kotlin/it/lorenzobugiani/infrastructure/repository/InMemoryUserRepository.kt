package it.lorenzobugiani.infrastructure.repository

import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository

class InMemoryUserRepository : UserRepository {

    private val storage = mutableListOf<User>()

    override fun store(user: User) {
        storage.add(user)
    }

    override fun getUser(id: Long): User? = storage.firstOrNull { it.id == id }


    override fun getLastRegisteredUsers(n: Int) = storage.takeLast(n).reversed().toList()

}