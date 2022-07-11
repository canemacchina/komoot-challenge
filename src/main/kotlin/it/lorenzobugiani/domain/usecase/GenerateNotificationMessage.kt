package it.lorenzobugiani.domain.usecase

import it.lorenzobugiani.domain.entity.User
import it.lorenzobugiani.domain.repository.UserRepository

data class NotificationMessage(val message: String, val ids: List<Long>, val receiver: Long)

class GenerateNotificationMessage(private val userRepository: UserRepository) {
    fun execute(user: User): NotificationMessage {
        val recentUsers = userRepository.getLastRegisteredUsers(3)

        return NotificationMessage(
            "Hi ${user.name}, welcome to komoot.${getRecentUserMessage(recentUsers)}",
            recentUsers.map { it.id },
            user.id
        )
    }

    private fun getRecentUserMessage(recentUsers: List<User>): String {
        if (recentUsers.isEmpty()) {
            return ""
        }

        val last = recentUsers.last()

        var others = recentUsers.subList(0, recentUsers.size - 1).joinToString(", ") { it.name }
        if (others.isNotEmpty()) {
            others = " $others and"
        }

        return "$others ${last.name} also joined recently."
    }
}
