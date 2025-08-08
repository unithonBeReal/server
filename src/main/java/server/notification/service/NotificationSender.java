package server.notification.service;


import server.notification.dto.NotificationMessage;

public interface NotificationSender {

    void send(NotificationMessage message);
}
