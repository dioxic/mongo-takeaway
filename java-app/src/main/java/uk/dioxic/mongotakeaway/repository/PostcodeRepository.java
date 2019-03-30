package uk.dioxic.mongotakeaway.repository;

import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.Postcode;

@Repository
public interface PostcodeRepository extends BaseRepository<Postcode, String> {

}
