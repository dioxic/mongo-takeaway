package uk.dioxic.mongotakeaway.config;

import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.TypeInformationMapper;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.lang.NonNull;
import uk.dioxic.mongotakeaway.TakeawayApplication;
import uk.dioxic.mongotakeaway.repository.BaseRepositoryImpl;
import uk.dioxic.mongotakeaway.util.AliasTypeInformationMapper;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = TakeawayApplication.class,
        repositoryBaseClass = BaseRepositoryImpl.class)
public class MongoConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDbFactory mongoDbFactory,
                                                       MongoCustomConversions mongoCustomConversions,
                                                       MongoMappingContext mongoMappingContext) {
        TypeInformationMapper informationMapper = AliasTypeInformationMapper.builder()
                .basePackage("uk.dioxic.mongotakeaway")
                .build();
        MongoTypeMapper typeMapper = new DefaultMongoTypeMapper(DefaultMongoTypeMapper.DEFAULT_TYPE_KEY, List.of(informationMapper));

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setTypeMapper(typeMapper);
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