package com.pelajtech.marketi.event;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

public interface EventManager {

    <T extends Event> Optional<LocalDateTime> getLastEventTime(Class<T> eventType);

    <T extends Event> void emit(T event);

    <T extends Event> void subscribe(Class<T> eventType, Consumer<T> onReceive);

}
