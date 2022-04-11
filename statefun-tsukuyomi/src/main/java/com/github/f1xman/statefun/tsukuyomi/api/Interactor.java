package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.core.TsukiyomiApi;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Optional;

interface Interactor {

    Optional<TypeName> getCollaborator();

    void interact(TsukiyomiApi tsukuyomi);
}
