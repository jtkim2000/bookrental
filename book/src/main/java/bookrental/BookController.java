package bookrental;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class BookController {
        @Autowired
        BookRepository bookRepository;

        @RequestMapping(value = "/books/checkBookQtyAndModifyQty",
                method = RequestMethod.GET,
                produces = "application/json;charset=UTF-8")

        public void checkBookQtyAndModifyQty(HttpServletRequest request, HttpServletResponse response)
                throws Exception {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                System.out.println("##### /book/checkBookQtyAndModifyQty  called #####");
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
 
                Long bookId = Long.valueOf(request.getParameter("bookId"));
                Integer qty = Integer.parseInt(request.getParameter("qty"));

                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                System.out.println("##### bookId = " + bookId);
                System.out.println("##### qty = " + qty);
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                Book book = bookRepository.findByBookId(bookId);

                // Hystrix를 통한 Circuit Breaker 동작 점검용 -- timout 발생 시험
                // 테스트를 위해 bookId = 5 인경우 5초간 sleep
                if (book.getBookId().longValue() == 5) {
                        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                        System.out.println("Hystrix를 통한 CB발생 테스트 : 5초간 sleep");
                        Thread.sleep(5000);
                        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                }

                if(book.getQty() >= qty) {
                        book.setQty(book.getQty() - qty);
                        bookRepository.save(book);
                }
        }
 }
