package uk.dioxic.mongotakeaway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.StateMachinePersister;
import uk.dioxic.mongotakeaway.domain.Order;

import java.util.Set;

@Configuration
@EnableStateMachine
public class StateMachineConfig  extends StateMachineConfigurerAdapter<Order.State, String> {

    @Override
    public void configure(StateMachineStateConfigurer<Order.State, String> states) throws Exception {

        states
                .withStates()
                .initial(Order.State.CREATED)
                .states(Set.of(Order.State.values()));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<Order.State, String> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(Order.State.CREATED).target(Order.State.ACCEPTED)
                .event("ACCEPTED")
                .and()
                .withExternal()
                .source(Order.State.ACCEPTED).target(Order.State.COOKING)
                .event("COOKING")
                .and()
                .withExternal()
                .source(Order.State.COOKING).target(Order.State.ONROUTE)
                .event("ONROUTE")
                .and()
                .withExternal()
                .source(Order.State.ONROUTE).target(Order.State.DELIVERED)
                .event("DELIVER");
    }

}