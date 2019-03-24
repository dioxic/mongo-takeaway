package uk.dioxic.mongotakeaway.event;

import org.springframework.context.ApplicationEvent;

public class CustomersReloadedEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public CustomersReloadedEvent(Object source) {
        super(source);
    }
}
