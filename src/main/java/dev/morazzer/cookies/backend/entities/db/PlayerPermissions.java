package dev.morazzer.cookies.backend.entities.db;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlayerPermissions {

    @Id
    @GeneratedValue
    private Long id;
    private UUID uuid;
    private String scope;

}
