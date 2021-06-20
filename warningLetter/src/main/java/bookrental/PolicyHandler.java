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
    @Autowired WarningLetterRepository warningLetterRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBookReturned_CheckBookStatus(@Payload BookReturned bookReturned){

        if(!bookReturned.validate()) return;

        System.out.println("\n\n##### listener CheckBookStatus : " + bookReturned.toJson() + "\n\n");

        WarningLetter warningLetter = new WarningLetter();

        // 반납된 책의 상태가 BAD이면 경고장(Warning Letter)을 발송한다.
        if(bookReturned.getBookStatus().contentEquals("BAD")) {
            warningLetter.setMemberId(bookReturned.getMemberId()); 
            warningLetter.setStatus("Send Warning Letter to member : "+ bookReturned.getMemberId());            
            warningLetterRepository.save(warningLetter);
        }
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
