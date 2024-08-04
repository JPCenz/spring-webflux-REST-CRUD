package com.jpcenz.projects.springwebfluxrestcrud.examples;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

public class ReactiveProgrammingTest {

    @Test
    void testMapFromStringToBigDecimal() {
        Assertions.assertEquals(0, BigDecimal.TEN.compareTo(new ReactiveProgramming().mapStringToBigDecimal("10")));
    }
    @Test
    void testMonoNoEmptyWithMonoEmpty() {
        StepVerifier.create(new ReactiveProgramming().monoNoEmpty(Mono.empty()))
                .expectNext(0)
                .verifyComplete();

    }

    @Test
    void testMonoNoEmptyWithMono() {
        StepVerifier.create(new ReactiveProgramming().monoNoEmpty(Mono.just(2)))
                .expectNext(2)
                .verifyComplete();
    }

    @Test
    void testMapMonoFromStringToBigDecimalStepVerifier() {
        StepVerifier.create(new ReactiveProgramming().mapStringToBigDecimalMono(Mono.just("10")))
                .expectNext(BigDecimal.TEN)
                .verifyComplete();
    }
    @Test
    void testMapFluxFromStringToBigDecimalStepVerifier() {
        StepVerifier.create(new ReactiveProgramming()
                        .mapStringToBigDecimalFlux(Flux.interval(Duration.ofMillis(10)).map(String::valueOf).take(5)))
                .expectNext(BigDecimal.ZERO, BigDecimal.ONE)
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    void testfilterEvenNumbers() {
        StepVerifier.create(new ReactiveProgramming().filterEvenNumbers(Flux.just(1, 2, 3, 4, 5, 6)))
                .expectNext(2, 4, 6)
                .verifyComplete();
    }

    @Test
    void testfilterPositiveNumbers() {
        StepVerifier.create(new ReactiveProgramming().filterPositiveNumbers(Flux.just(1, 2, -3, 4, -5, 6)))
                .expectNext(1, 2)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Negative numbers are not allowed"))
                .verify();


    }
}
