package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.dioxic.mongotakeaway.config.StateMachineConfig;
import uk.dioxic.mongotakeaway.domain.Order;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@DisplayName("state machine tests")
@SpringBootTest(classes = StateMachineConfig.class)
public class StateMachineTest {

    @Autowired
    private StateMachine<Order.State, String> stateMachine;

    @Autowired
    private StateMachinePersister<Order.State, String, String> persister;

    @BeforeEach
    public void beforeEach() throws Exception {
        persister.restore(stateMachine, "user");
    }

    @Test
    public void exists() {
        assertThat(stateMachine).isNotNull();
    }

    @Test
    public void initialState() {
        assertThat(stateMachine.getInitialState().getId()).as("initial state").isEqualTo(Order.State.CREATED);
    }

    @Test
    public void initialises() {
        stateMachine.start();
        assertThat(stateMachine.getState().getId()).as("current state").isEqualTo(Order.State.CREATED);
    }

    @Test
    public void works() {
        stateMachine.start();
        assertThat(stateMachine.sendEvent("ACCEPTED")).as("event accepted").isTrue();
        assertThat(stateMachine.getState().getId()).as("current state").isEqualTo(Order.State.ACCEPTED);
    }

}
