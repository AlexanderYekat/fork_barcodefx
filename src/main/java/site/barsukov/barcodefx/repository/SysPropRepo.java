package site.barsukov.barcodefx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.barsukov.barcodefx.model.dao.SysProp;
import site.barsukov.barcodefx.model.enums.SysPropType;

import java.util.Optional;
import java.util.Set;

public interface SysPropRepo extends JpaRepository<SysProp, Long> {
    Optional<SysProp> getFirstBySysKey(String propertyName);

    Set<SysProp> getByType(SysPropType type);
}
