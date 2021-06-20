package bookrental;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="warningLetters", path="warningLetters")
public interface WarningLetterRepository extends PagingAndSortingRepository<WarningLetter, Long>{


}
