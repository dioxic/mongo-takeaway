package uk.dioxic.mongotakeaway.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class ObjectIdJsonSerializer extends JsonSerializer<ObjectId> {
 
    @Override
    public void serialize(ObjectId objectId, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(objectId.toHexString());
    }

}