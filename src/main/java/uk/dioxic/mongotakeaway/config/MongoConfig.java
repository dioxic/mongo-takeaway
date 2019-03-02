package uk.dioxic.mongotakeaway.config;

import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@EnableReactiveMongoRepositories("uk.dioxic.mongotakeaway")
public class MongoConfig {

    @Autowired
    private MongoMappingContext mongoMappingContext;

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDbFactory mongoDbFactory, MongoCustomConversions mongoCustomConversions) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.setCustomConversions(mongoCustomConversions);

        return converter;
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new BigDecimalDecimal128Converter(),
                new Decimal128BigDecimalConverter()
        ));
    }

    @WritingConverter
    private static class BigDecimalDecimal128Converter implements Converter<BigDecimal, Decimal128> {

        @Override
        public Decimal128 convert(@NonNull BigDecimal source) {
            return new Decimal128(source);
        }
    }

    @ReadingConverter
    private static class Decimal128BigDecimalConverter implements Converter<Decimal128, BigDecimal> {

        @Override
        public BigDecimal convert(@NonNull Decimal128 source) {
            return source.bigDecimalValue();
        }

    }
}