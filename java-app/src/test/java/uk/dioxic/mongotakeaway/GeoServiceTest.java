package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import uk.dioxic.mongotakeaway.domain.Postcode;
import uk.dioxic.mongotakeaway.repository.PostcodeRepository;
import uk.dioxic.mongotakeaway.service.GeoService;

import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@Slf4j
@ExtendWith(SpringExtension.class)
class GeoServiceTest {

    @MockBean
    private PostcodeRepository repository;

    @Test
    void distance_fromPostcode() {
        Postcode postcode1 = new Postcode("ME16 8SH", 0.511899, 51.270395);
        Postcode postcode2 = new Postcode("DH1 3NU", -1.574591, 54.775859);

        when(repository.findAllById(anyIterable()))
                .thenReturn(Flux.just(postcode1, postcode2));

        GeoService service = new GeoService(repository);

        Assertions.assertThat(service.distanceInMiles(postcode1.getPostcode(), postcode2.getPostcode()).block()).isEqualTo(257.22879987048117d);

        verify(repository).findAllById(anyIterable());
    }

}
