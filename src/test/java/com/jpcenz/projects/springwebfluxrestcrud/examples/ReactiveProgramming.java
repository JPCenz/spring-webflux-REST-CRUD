package com.jpcenz.projects.springwebfluxrestcrud.examples;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public class ReactiveProgramming {
    public BigDecimal mapStringToBigDecimal(String value) {
        return new BigDecimal(value);
    }

    public Mono<BigDecimal> mapStringToBigDecimalMono(Mono<String> mono) {
        return mono.map(BigDecimal::new);
    }

    public Mono<Integer> monoNoEmpty(Mono<Integer> mono) {
        return mono.switchIfEmpty(Mono.just(0));
    }

    public Flux<BigDecimal> mapStringToBigDecimalFlux(Flux<String> flux) {
        return flux.map(BigDecimal::new);
    }

    public Flux<Integer> filterEvenNumbers(Flux<Integer> flux) {
        return flux.filter(i -> i % 2 == 0);
    }

    public Flux<Integer> filterPositiveNumbers(Flux<Integer> flux) {
        return flux.handle((i, sink) -> {
            if (i < 0) {
                sink.error(new IllegalArgumentException("Negative numbers are not allowed"));
            } else {
                sink.next(i);
            }
        });
    }
}
