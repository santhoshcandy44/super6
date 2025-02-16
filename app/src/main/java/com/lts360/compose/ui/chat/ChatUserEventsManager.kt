package com.lts360.compose.ui.chat

import io.socket.client.Socket
import io.socket.emitter.Emitter

class ChatUserEventsManager {


    fun registerListeners(
        socket: Socket,
        recipientId:Long,
        onlineStatusListener: Emitter.Listener,
        typingStatusListener: Emitter.Listener,
        profileInfoListener:Emitter.Listener,
    ) {
        socket.on("chat:onlineStatus-${recipientId}", onlineStatusListener)
        socket.on("chat:typing", typingStatusListener)
        socket.on("chat:profileInfo",profileInfoListener)
    }

    fun unregisterListeners(
        socket: Socket,
        recipientId:Long,
        onlineStatusListener: Emitter.Listener,
        typingStatusListener: Emitter.Listener,
        profileInfoListener:Emitter.Listener,

        ) {

        socket.off("chat:onlineStatus-${recipientId}", onlineStatusListener)
        socket.off("chat:isTyping", typingStatusListener)
        socket.off("chat:profileInfo",profileInfoListener)

    }
}
