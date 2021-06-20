package bookrental;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reasonLetters", path="reasonLetters")
public interface ReasonLetterRepository extends PagingAndSortingRepository<ReasonLetter, Long>{


}
