package uk.dioxic.mongotakeaway.repository;

import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.AppSettings;

@Repository
public interface PropertyRepository extends BaseRepository<AppSettings, String> {

}
