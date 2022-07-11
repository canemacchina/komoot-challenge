package it.lorenzobugiani.domain.repository

import it.lorenzobugiani.domain.entity.User

interface UserRepository {
    fun store(user: User)
    fun getUser(id: Long): User?
    fun getLastRegisteredUsers(n: Int = 1): List<User>
}