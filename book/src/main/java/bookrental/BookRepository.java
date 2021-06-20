package bookrental;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookRepository extends PagingAndSortingRepository<Book, Long>{

    Book findByBookId(Long bookId);

}

