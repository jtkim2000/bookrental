package bookrental;

import bookrental.config.kafka.KafkaProcessor;

import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired BookRentalRepository bookRentalRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBookRequested_LendBook(@Payload BookRequested bookRequested){

        if(!bookRequested.validate()) return;

        System.out.println("\n\n##### listener LendBook : " + bookRequested.toJson() + "\n\n");

        // 책을 빌려주면 상태(status)를 "Book Lent"로 변경한다.
        BookRental bookRental = new BookRental();
        // Optional<BookRental> bookRentalOptional = bookRentalRepository.findById(bookRequested.getRequestId());
        // bookRental = bookRentalOptional.get();
        // bookRental.setStatus("Book Lent !!");
        // bookRentalRepository.save(bookRental);

        bookRental.setBookId(bookRequested.getBookId());
        bookRental.setBookStatus("GOOD");  // 책을 빌려줄때 책 상태는 "GOOD"으로 설정
        bookRental.setMemberId(bookRequested.getMemberId());
        bookRental.setQty(bookRequested.getQty());
        bookRental.setStatus("Book Lent !!");
        bookRentalRepository.save(bookRental);

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
