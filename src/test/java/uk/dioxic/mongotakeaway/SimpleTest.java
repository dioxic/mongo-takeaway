package uk.dioxic.mongotakeaway;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("simple tests")
public class SimpleTest {

    @Test
    public void hello() {
        Assertions.assertThat("a").isEqualTo("a");
    }
}
