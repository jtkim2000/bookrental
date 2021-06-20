package bookrental;

import bookrental.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BookRentalMonitoringPageViewHandler {


    @Autowired
    private BookRentalMonitoringPageRepository bookRentalMonitoringPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookLent_then_CREATE_1 (@Payload BookLent bookLent) {
        try {

            if (!bookLent.validate()) return;

            // view 객체 생성
            BookRentalMonitoringPage bookRentalMonitoringPage = new BookRentalMonitoringPage();
            // view 객체에 이벤트의 Value 를 set 함
            bookRentalMonitoringPage.setRentalId(bookLent.getRentalId());
            bookRentalMonitoringPage.setBookId(bookLent.getBookId());
            bookRentalMonitoringPage.setQty(bookLent.getQty());
            bookRentalMonitoringPage.setBookStatus(bookLent.getBookStatus());
            bookRentalMonitoringPage.setStatus(bookLent.getStatus());
            // view 레파지 토리에 save
            bookRentalMonitoringPageRepository.save(bookRentalMonitoringPage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenBookReturned_then_UPDATE_1(@Payload BookReturned bookReturned) {
        try {
            if (!bookReturned.validate()) return;
                // view 객체 조회
            List<BookRentalMonitoringPage> bookRentalMonitoringPageList = bookRentalMonitoringPageRepository.findByRentalId(bookReturned.getRentalId());
            for(BookRentalMonitoringPage bookRentalMonitoringPage : bookRentalMonitoringPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                bookRentalMonitoringPage.setBookStatus(bookReturned.getBookStatus());
                bookRentalMonitoringPage.setStatus(bookReturned.getStatus());
                // view 레파지 토리에 save
                bookRentalMonitoringPageRepository.save(bookRentalMonitoringPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}