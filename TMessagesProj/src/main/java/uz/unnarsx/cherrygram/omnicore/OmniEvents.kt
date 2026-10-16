package uz.unnarsx.cherrygram.omnicore

sealed class OmniEvent {
    data class MessageReceived(
        val chatId: Long,
        val messageId: Int,
        val text: String,
        val isOutgoing: Boolean,
        val isChannel: Boolean,
        val senderId: Long
    ) : OmniEvent()

    data class MessageSent(
        val chatId: Long,
        val messageId: Int,
        val text: String
    ) : OmniEvent()

    data class ChatOpened(
        val chatId: Long
    ) : OmniEvent()

    data class MenuCreated(
        val messageId: Int,
        val text: String?
    ) : OmniEvent()
}

typealias OmniEventHandler = (OmniEvent) -> Boolean
