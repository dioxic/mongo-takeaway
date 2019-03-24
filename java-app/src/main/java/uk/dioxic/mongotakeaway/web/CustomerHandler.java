package uk.dioxic.mongotakeaway.web;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import uk.dioxic.mongotakeaway.domain.Customer;
import uk.dioxic.mongotakeaway.repository.CustomerRepository;

import java.net.URI;

@Slf4j
@Component
public class CustomerHandler extends AbstractRestHandler<Customer, ObjectId> {

    public CustomerHandler(CustomerRepository repository) {
        super(repository, Customer.class);
    }

    @Override
    ObjectId parseId(String id) {
        return new ObjectId(id);
    }

    @Override
    URI linkUri(Customer obj) {
        return URI.create("/customer/" + obj.getId().toHexString());
    }
}
