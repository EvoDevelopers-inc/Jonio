package evo.developers.ru.jpa;

import evo.developers.ru.entity.VersionControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersionControlRepository extends JpaRepository<VersionControl, Long> {
    Optional<VersionControl> findByUserId(String userId);
}
