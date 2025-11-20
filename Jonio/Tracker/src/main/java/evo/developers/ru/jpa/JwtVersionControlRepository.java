package evo.developers.ru.jpa;

import evo.developers.ru.entity.JwtVersionControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtVersionControlRepository extends JpaRepository<JwtVersionControl, Long> {
    Optional<JwtVersionControl> findByUserId(String userId);
}
