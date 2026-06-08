package com.pelajtech.marketi.event;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LocalEventManager implements EventManager {

    private final Map<Class<? extends Event>, Set<EventSubscriber<? extends Event>>> subscribers = new ConcurrentHashMap<>();
    private final Map<Class<? extends Event>, LocalDateTime> eventTimestamps = new ConcurrentHashMap<>();

    @Override
    public <T extends Event> Optional<LocalDateTime> getLastEventTime(Class<T> eventType) {
        return Optional.ofNullable(eventTimestamps.get(eventType));
    }

    @Override
    public <T extends Event> void emit(T event) {
        eventTimestamps.put(event.getClass(), LocalDateTime.now());
        subscribers.getOrDefault(event.getClass(), Set.of()).forEach(subscriber -> subscriber
                .accept(event));
    }

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> onReceive) {
        subscribers.computeIfAbsent(eventType, _ -> ConcurrentHashMap.newKeySet())
                .add(new EventSubscriber<>(eventType, onReceive));
    }

    private record EventSubscriber<T extends Event>(Class<T> eventType, Consumer<T> onReceive) {

        void accept(Event event) {
            onReceive.accept(eventType.cast(event));
        }
    }
}
