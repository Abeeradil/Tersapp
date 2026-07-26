package org.example.tears.Service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Set<String> onlineUsers =
            ConcurrentHashMap.newKeySet();

    public void online(String phone) {
        onlineUsers.add(phone);
    }

    public void offline(String phone) {
        onlineUsers.remove(phone);
    }

    public boolean isOnline(String phone) {
        return onlineUsers.contains(phone);
    }
}