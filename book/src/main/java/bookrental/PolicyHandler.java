package bookrental;

import bookrental.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired BookRepository bookRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBookReturned_ModifyBookQtyAndStatus(@Payload BookReturned bookReturned){

        System.out.println("\n\n##### listener ModifyBookQtyAndStatus : " + bookReturned.toJson() + "\n\n");

        if(!bookReturned.validate()) {
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println("INVALID --- BookReturned Event");
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            return;
        }

        // 책 수량을 반납된 책의 수량만큼 증가 시킨다.
        Book book = bookRepository.findByBookId(Long.valueOf(bookReturned.getBookId()));
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("반납 전 책 수량 = " + book.getQty());
        book.setQty(book.getQty() + bookReturned.getQty());
        System.out.println("반납 후 책 수량 = " + book.getQty());
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        bookRepository.save(book);
    
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
