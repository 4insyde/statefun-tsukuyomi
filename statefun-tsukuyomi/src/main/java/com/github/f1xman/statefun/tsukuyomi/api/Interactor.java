package com.github.f1xman.statefun.tsukuyomi.api;

import com.github.f1xman.statefun.tsukuyomi.TsukiyomiApi;
import org.apache.flink.statefun.sdk.java.TypeName;

import java.util.Optional;

public interface Interactor {

    Optional<TypeName> getCollaborator();

    void interact(TsukiyomiApi tsukuyomi);
}
