package uk.dioxic.mongotakeaway.util;

import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.LocalDateTimeCodec;
import org.bson.json.JsonWriterSettings;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

public class DocumentUtil {

    private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(asList(new ValueCodecProvider(),
            new BsonValueCodecProvider(),
            new ExtendedCodecProvider(new LocalDateTimeCodec()),
            new DocumentCodecProvider()));
    private static final JsonWriterSettings jws = JsonWriterSettings.builder()
            .indent(true)
            .build();
    private static final DocumentCodec documentCodec = new DocumentCodec(DEFAULT_REGISTRY);

    public static String toJson(Document doc) {
        return doc.toJson(documentCodec);
    }

    public static String toJsonPretty(Document doc) {
        return doc.toJson(jws, documentCodec);
    }

    public static DocumentCodec getDocumentCodec() {
        return documentCodec;
    }

    public static CodecRegistry getCodecRegistry() {
        return DEFAULT_REGISTRY;
    }

    public static class ExtendedCodecProvider implements CodecProvider {

        private final Map<Class<?>, Codec<?>> codecs = new HashMap<>();

        /**
         * A provider of Codecs for extended types.
         */
        public ExtendedCodecProvider(Codec... codecs) {
            for (Codec<?> codec : codecs) {
                addCodec(codec);
            }
        }

        public ExtendedCodecProvider() {}

        @Override
        @SuppressWarnings("unchecked")
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            return (Codec<T>) codecs.get(clazz);
        }

        private <T> void addCodec(final Codec<T> codec) {
            codecs.put(codec.getEncoderClass(), codec);
        }
    }
}
