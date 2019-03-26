package uk.dioxic.mongotakeaway.event;

import org.springframework.context.ApplicationEvent;
import uk.dioxic.mongotakeaway.annotation.Polymorphic;
import uk.dioxic.mongotakeaway.domain.AppSettings;
import uk.dioxic.mongotakeaway.domain.Event;

public class PropertiesChangedEvent extends ApplicationEvent {
    public PropertiesChangedEvent(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public AppSettings getSource() {
        return (AppSettings) super.getSource();
    }
}