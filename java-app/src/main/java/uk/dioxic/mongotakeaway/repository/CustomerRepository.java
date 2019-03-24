package uk.dioxic.mongotakeaway.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import uk.dioxic.mongotakeaway.domain.Customer;

@Repository
public interface CustomerRepository extends BaseRepository<Customer, ObjectId> {

}
