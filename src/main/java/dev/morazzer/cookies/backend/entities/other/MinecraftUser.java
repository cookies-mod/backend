package dev.morazzer.cookies.backend.entities.other;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import java.util.UUID;

public record MinecraftUser(@SerializedName("id") UUID uuid, String name) {}
