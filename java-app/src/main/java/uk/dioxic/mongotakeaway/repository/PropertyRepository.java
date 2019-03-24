package uk.dioxic.mongotakeaway.repository;

import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.GlobalProperties;

@Repository
public interface PropertyRepository extends BaseRepository<GlobalProperties, String> {

}
