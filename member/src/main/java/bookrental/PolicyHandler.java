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
    @Autowired MemberRepository memberRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverWarningSent_DownGradeMember(@Payload WarningSent warningSent){

        if(!warningSent.validate()) return;

        System.out.println("\n\n##### listener DownGradeMember : " + warningSent.toJson() + "\n\n");

        // 경고장(Warning Letter)이 발송된 회원의 등급을 BBB로 강등하고, 회원상태를 BAD로 설정한다.
        Member member = new Member();        
        member = memberRepository.findByMemberId(warningSent.getMemberId());        
        member.setGrade("BBB");
        member.setStatus("BAD");
        memberRepository.save(member);
            
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReasonSubmitted_UpGradeMember(@Payload ReasonSubmitted reasonSubmitted){

        if(!reasonSubmitted.validate()) return;

        System.out.println("\n\n##### listener UpGradeMember : " + reasonSubmitted.toJson() + "\n\n");

        // 사유서(Reason Letter)를 제출한 회원의 등급을 AAA로 승급하고, 회원상태를 GOOD으로 설정한다.
        Member member = new Member();        
        member = memberRepository.findByMemberId(reasonSubmitted.getMemberId());        
        member.setGrade("AAA");
        member.setStatus("GOOD");
        memberRepository.save(member);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
