package bookrental;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRentalMonitoringPageRepository extends CrudRepository<BookRentalMonitoringPage, Long> {

    List<BookRentalMonitoringPage> findByRentalId(Long rentalId);

}