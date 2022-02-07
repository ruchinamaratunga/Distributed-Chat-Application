package com.ds.chatserver.chatroom;

import com.ds.chatserver.clienthandler.ClientThread;
import com.ds.chatserver.exceptions.ChatroomAlreadyExistsException;
import com.ds.chatserver.exceptions.ChatroomDoesntExistsException;
import com.ds.chatserver.exceptions.InvalidChatroomException;
import com.ds.chatserver.serverhandler.Server;

import java.util.ArrayList;

public class ChatRoomHandler {
    private ArrayList<ChatRoom> chatrooms;
    private static ChatRoomHandler chatRoomHandler = getInstance();

    private static ChatRoomHandler getInstance() {
        return new ChatRoomHandler();
    }

    private ChatRoomHandler() {
        this.chatrooms = new ArrayList<>();
    }

    private Boolean validateChatroomName(String name) {
        //TODO Implement
        return true;
    }

    public Boolean validateChatRoom(String name) {
        return (Server.validateChatroom(name));
    }

    public Boolean createChatRoom(String name, ClientThread clientThread)
            throws ChatroomAlreadyExistsException, InvalidChatroomException {
        if (!(validateChatRoom(name))) {
            throw new ChatroomAlreadyExistsException(name);
        } else if (!(validateChatroomName(name))) {
            throw new InvalidChatroomException(InvalidChatroomException.generateInvalidNameMessage(name));
        } else {
            setChatrooms(getChatrooms().add(new ChatRoom(name, clientThread)));
            return true;
        }
    }

    public void joinRoom(String name, ClientThread clientThread) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getId().equals(name) {
                chatRoom.addClient(clientThread);
                return;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }

    public Boolean removeFromPreviousRoom(String name, ClientThread clientThread) {
        Boolean isFirstJoin = true;
        ChatRoom prevChatroom = getChatroomfromClientId(clientThread.getId());
        if (prevChatroom != null) {
            prevChatroom.remove(clientThread);
            isFirstJoin = false;
        }
        return isFirstJoin;
    }

    public void changeRoom(String name, ClientThread clientThread) throws ChatroomDoesntExistsException {
        removeFromPreviousRoom(name, clientThread);
        joinRoom(name, clientThread);

    }

    public void deleteRoom(String name, ClientThread clientThread) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getOwner().equals(clientThread.getId())) {
                chatRoom.addClient(clientThread);
                return;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }

    public ChatRoom getChatroomFromName(String name) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getId().equals(name)) {
                return chatRoom;
            }
        }
        throw new ChatroomDoesntExistsException(name);
    }

    public void deleteChatroom(String name, String clientId) throws ChatroomDoesntExistsException {
        getChatroomFromName(name).delete(clientId);
    }

    public ChatRoom getChatroomfromClientId(String clientId) {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getOwner().equals(clientId)) {
                return chatRoom;
            }
        }
        return null;
    }

    public void sendMessage(String message, String clientId, String chatroomName) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getId().equals(chatroomName)) {
                return chatRoom.sendMessage(message, clientId);
            }
        }
        throw new ChatroomDoesntExistsException(chatroomName);
    }

    public void quit(String chatroomName, String ClientId) throws ChatroomDoesntExistsException {
        for (ChatRoom chatRoom: chatrooms) {
            if (chatRoom.getId().equals(chatroomName)) {
                return chatRoom.quit(clientId);
            }
        }
        throw new ChatroomDoesntExistsException(chatroomName);
    }

    public ArrayList<ChatRoom> getChatrooms() {
        return chatrooms;
    }

    public void setChatrooms(ArrayList<ChatRoom> chatrooms) {
        this.chatrooms = chatrooms;
    }

    public static ChatRoomHandler getChatRoomHandler() {
        return chatRoomHandler;
    }
}
