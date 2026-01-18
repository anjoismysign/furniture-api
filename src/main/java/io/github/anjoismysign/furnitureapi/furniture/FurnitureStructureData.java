package io.github.anjoismysign.furnitureapi.furniture;

import io.github.anjoismysign.util.Cuboid;

import java.util.UUID;

public record FurnitureStructureData(Cuboid cuboid,
                                     UUID uuid) {
}
