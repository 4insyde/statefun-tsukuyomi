package com.github.f1xman.statefun.tsukuyomi.dispatcher.config;

import com.github.f1xman.statefun.tsukuyomi.dispatcher.config.StatefunModule;
import org.apache.flink.statefun.sdk.FunctionType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StatefunModuleTest {

    static final String FOO = "foo";
    static final String BAR = "bar";
    static final String BAZ = "baz";
    static final String FOOBARBAZ = "foo/bar;bar/baz";
    static final URI ENDPOINT = URI.create("http://foo.bar");
    static final String EGRESS_FOO_EGRESS_BAR = "egress/foo;egress/bar";

    @Test
    void buildsSetOfFunctionTypes() {
        StatefunModule module = StatefunModule.of(FOOBARBAZ, ENDPOINT, null);
        Set<FunctionType> expected = Set.of(new FunctionType(FOO, BAR), new FunctionType(BAR, BAZ));

        Set<FunctionType> actual = module.getFunctionTypes();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void buildsSetOfEgresses() {
        StatefunModule module = StatefunModule.of(FOOBARBAZ, ENDPOINT, EGRESS_FOO_EGRESS_BAR);
        Set<String> expected = Set.of("egress/foo", "egress/bar");

        Set<String> actual = module.getEgressIds();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnsEmptySetIfNoEgressesPresent() {
        StatefunModule module = StatefunModule.of(FOOBARBAZ, ENDPOINT, null);
        Set<String> expected = Set.of();

        Set<String> actual = module.getEgressIds();

        assertThat(actual).isEqualTo(expected);
    }
}