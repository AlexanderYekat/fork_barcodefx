package site.barsukov.barcodefx.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import site.barsukov.barcodefx.model.printing.dao.PrintingProfileDao;

import java.util.Optional;

public interface PrintingProfileRepo extends PagingAndSortingRepository<PrintingProfileDao, Long> {
    Optional<PrintingProfileDao> getFirstByName(String profileName);
}
