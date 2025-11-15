package evo.developers.ru.jpa;

import evo.developers.ru.entity.JonioAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JonioAddressRepository extends JpaRepository<JonioAddress, Long> {
}
