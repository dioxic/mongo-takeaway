package uk.dioxic.mongotakeaway.event;

import org.springframework.context.ApplicationEvent;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;

public class PropertiesChangedEvent extends ApplicationEvent {
    public PropertiesChangedEvent(GlobalProperties globalProperties) {
        super(globalProperties);
    }

    @Override
    public GlobalProperties getSource() {
        return (GlobalProperties) super.getSource();
    }
}