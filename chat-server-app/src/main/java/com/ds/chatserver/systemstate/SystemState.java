package com.ds.chatserver.systemstate;

import com.ds.chatserver.log.Event;
import com.ds.chatserver.log.EventType;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.utils.Util;

import java.util.HashMap;

public class SystemState {
    private static HashMap<String, ClientLog> clientLists;
    private static HashMap<String, ChatroomLog> chatroomLists;
    private static HashMap<String, ClientLog> draftClientLists;
    private static HashMap<String, ChatroomLog> draftChatroomLists;

    public synchronized static void commit(Server server){
        for(int i = server.getRaftLog().getLastApplied()+1; i <= server.getRaftLog().getCommitIndex(); i++){
            Event event = server.getRaftLog().getIthEvent(i);

            EventType eventType = event.getType();

            switch (eventType){
                case NEW_IDENTITY:
                    clientLists.put(event.getClientId(), new ClientLog(event.getClientId(),
                            Util.getMainhall(event.getServerId()),
                            event.getServerId()));
                    chatroomLists.get(Util.getMainhall(event.getServerId())).addParticipant(event.getClientId());
            }

            server.getRaftLog().incrementLastApplied();

        }
    }

    public HashMap<String, ClientLog> getClientLists() {
        return clientLists;
    }

    public static void setClientLists(HashMap<String, ClientLog> clientLists) {
        SystemState.clientLists = clientLists;
    }

    public static HashMap<String, ChatroomLog> getChatroomLists() {
        return chatroomLists;
    }

    public static void setChatroomLists(HashMap<String, ChatroomLog> chatroomLists) {
        SystemState.chatroomLists = chatroomLists;
    }

    public static void commitClient(ClientLog clientLog) {
        clientLists.put(clientLog.getClientId(), clientLog);
        draftClientLists.remove(clientLog.getClientId());
    }

    public static void addDraftClient(ClientLog clientLog) {
        draftClientLists.put(clientLog.getClientId(), clientLog);
    }

    public static void removeClient(ClientLog clientLog) {
        clientLists.remove(clientLog.getClientId());
    }

    public static void commitChatroom(ChatroomLog chatroomLog) {
        chatroomLists.put(chatroomLog.getChatRoomName(), chatroomLog);
        draftChatroomLists.remove(chatroomLog.getChatRoomName());
    }

    public static void addDraftChatroom(ChatroomLog chatroomLog) {
        draftChatroomLists.put(chatroomLog.getChatRoomName(), chatroomLog);
    }

    public static void removeChatroom(ChatroomLog chatroomLog) {
        chatroomLists.remove(chatroomLog.getChatRoomName());
    }

    public static Boolean isClientCommitted(String clientId) {
        return clientLists.containsKey(clientId);
    }

    public static Boolean isClientAvailableInDraft(String chatroomName) {
        return draftClientLists.containsKey(chatroomName);
    }

    public static Boolean isChatroomCommitted(String chatroomName) {
        return chatroomLists.containsKey(chatroomName);
    }

    public static Boolean isChatroomAvailableInDraft(String clientId) {
        return draftChatroomLists.containsKey(clientId);
    }


    public static void getChatroom(String chatroomId) {

    }
}
