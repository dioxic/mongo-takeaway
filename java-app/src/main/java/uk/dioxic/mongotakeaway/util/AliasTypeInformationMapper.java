package uk.dioxic.mongotakeaway.util;

import lombok.Builder;
import lombok.Singular;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.convert.TypeInformationMapper;
import org.springframework.data.mapping.Alias;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import uk.dioxic.mongotakeaway.annotation.Polymorphic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliasTypeInformationMapper implements TypeInformationMapper {

    private final Map<ClassTypeInformation<?>, Alias> typeToAliasMap;
    private final Map<Alias, ClassTypeInformation<?>> aliasToTypeMap;

    @Builder
    private AliasTypeInformationMapper(@Singular List<String> basePackages) {
        typeToAliasMap = new HashMap<>();
        aliasToTypeMap = new HashMap<>();

        populateTypeMap(basePackages);
    }

    private void populateTypeMap(List<String> basePackages) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(Polymorphic.class));

        for (String basePackage : basePackages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                try {
                    Class<?> clazz = Class.forName(bd.getBeanClassName());
                    Polymorphic annotation = clazz.getAnnotation(Polymorphic.class);

                    ClassTypeInformation<?> type = ClassTypeInformation.from(clazz);
                    Alias alias = Alias.of(annotation.value().isEmpty() ? clazz.getSimpleName() : annotation.value());

                    typeToAliasMap.put(type, alias);
                    aliasToTypeMap.put(alias, type);

                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(String.format("Class [%s] could not be loaded.", bd.getBeanClassName()), e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.convert.TypeInformationMapper#createAliasFor(org.springframework.data.util.TypeInformation)
     */
    @Override
    public Alias createAliasFor(TypeInformation<?> type) {
        ClassTypeInformation<?> typeClass = (ClassTypeInformation<?>) type;

        if (typeToAliasMap.containsKey(typeClass)) {
            return typeToAliasMap.get(typeClass);
        }

        return Alias.NONE;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.convert.TypeInformationMapper#resolveTypeFrom(java.lang.Object)
     */
    @Override
    public ClassTypeInformation<?> resolveTypeFrom(Alias alias) {

        if (aliasToTypeMap.containsKey(alias)) {
            return aliasToTypeMap.get(alias);
        }

        return null;
    }

}