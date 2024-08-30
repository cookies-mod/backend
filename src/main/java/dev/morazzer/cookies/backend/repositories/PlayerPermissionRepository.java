package dev.morazzer.cookies.backend.repositories;

import dev.morazzer.cookies.backend.entities.db.PlayerPermissions;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerPermissionRepository extends CrudRepository<PlayerPermissions, Long> {

    List<PlayerPermissions> findAllByUuid(UUID uuid);

}
