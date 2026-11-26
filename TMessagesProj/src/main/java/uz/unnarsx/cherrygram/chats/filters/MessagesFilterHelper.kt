/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats.filters

import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.core.configs.CherrygramNBConfig

object MessagesFilterHelper {

    @JvmStatic
    fun isFiltered(messageObject: MessageObject?): Boolean {
        if (messageObject == null) return false
        return shouldBlockMessage(messageObject)
    }

    @JvmStatic
    fun addSpoilerEntities(messageObject: MessageObject): ArrayList<TLRPC.MessageEntity>? {
        return addSpoilerEntities(messageObject, messageObject.messageOwner.entities)
    }

    @JvmStatic
    fun addSpoilerEntities(
        messageObject: MessageObject,
        original: ArrayList<TLRPC.MessageEntity>?
    ): ArrayList<TLRPC.MessageEntity>? {
        if (CherrygramNBConfig.shouldHideUnderSpoiler(messageObject)) {
            val entities = if (original != null) ArrayList(original) else ArrayList<TLRPC.MessageEntity>()
            val textLen = messageObject.messageText?.length ?: 0
            if (textLen > 0) {
                val spoiler = TLRPC.TL_messageEntitySpoiler()
                spoiler.offset = 0
                spoiler.length = textLen
                entities.add(spoiler)
            }
            return entities
        }
        return original
    }

    @JvmStatic
    fun addSpoilerEntities(originalText: String?): String? {
        return originalText
    }

    @JvmStatic
    fun shouldBlockMessage(messageObject: MessageObject?): Boolean {
        if (messageObject == null) return false
        val senderId = messageObject.senderId
        if (CherrygramNBConfig.isUserCompletelyHidden(senderId)) {
            return true
        }
        if (CherrygramNBConfig.regexFiltersEnabled && !CherrygramNBConfig.regexFilterPatterns.isNullOrEmpty()) {
            try {
                val text = messageObject.messageText?.toString() ?: messageObject.caption?.toString() ?: ""
                if (text.isNotEmpty()) {
                    val pattern = CherrygramNBConfig.regexFilterPatterns
                    if (Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(text)) {
                        return true
                    }
                }
            } catch (_: Exception) {}
        }
        return false
    }

    /** Exclusions start */
    @JvmStatic
    fun getExcludedList(): String {
        return "excluded_for_filters"
    }

    @JvmStatic
    fun getExcludedChatsCount(): Int {
        return 0
    }

    @JvmStatic
    fun saveArrayList(list: ArrayList<String>, key: String) {

    }

    @JvmStatic
    fun getArrayList(key: String): ArrayList<String> {
        return arrayListOf()
    }
    /** Exclusions finish */

}