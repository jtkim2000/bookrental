package bookrental;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="bookRequests", path="bookRequests")
public interface BookRequestRepository extends PagingAndSortingRepository<BookRequest, Long>{


}
