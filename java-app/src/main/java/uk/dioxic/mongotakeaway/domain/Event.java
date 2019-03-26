package uk.dioxic.mongotakeaway.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.dioxic.mongotakeaway.annotation.Polymorphic;

import java.awt.desktop.AppEvent;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Data
@Document
@NoArgsConstructor
public class Event {

    @Id
    private ObjectId id;
    private String type;
    private String msg;
    private LocalDateTime eventTime;
    private NodeInfo node;

    public Event(Class<? extends ApplicationEvent> type, NodeInfo nodeInfo) {
        this.type = type.getSimpleName();
        this.eventTime = LocalDateTime.now();
        this.node = nodeInfo;
    }

    public Event(Class<? extends ApplicationEvent> type, NodeInfo nodeInfo, String msg) {
        this(type, nodeInfo);
        this.msg = msg;
    }

    public ApplicationEvent getApplicationEvent() {
        String qualifiedClassName = "uk.dioxic.mongotakeaway.event." + type;
        try {
            return (ApplicationEvent)Class.forName(qualifiedClassName).getConstructor(Object.class).newInstance(this);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Event type " + qualifiedClassName + " not recognised");
        }
    }
}
