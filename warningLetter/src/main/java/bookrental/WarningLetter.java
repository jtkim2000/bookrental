package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="WarningLetter_table")
public class WarningLetter {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long warningId;
    private Long memberId;

    // 시스템의 환경변수인 configmap의 값("PleaseCareBook")을 가져와서 warningMsg 변수에 저장
    private String warningMsg = System.getenv("configmap");
    private String status;

    @PostPersist
    public void onPostPersist(){
        // 경고장 발송 위해 시스템 환경변수인 configmap의 값을 경고장 메시지에 담아 보낸다.
        System.out.println("\n\n");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("##### get SYS ENV(configmap) -- : " + warningMsg);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("\n\n");

        WarningSent warningSent = new WarningSent();
        BeanUtils.copyProperties(this, warningSent);
        warningSent.publishAfterCommit();


    }


    public Long getWarningId() {
        return warningId;
    }

    public void setWarningId(Long warningId) {
        this.warningId = warningId;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWarningMsg() {
        return warningMsg;
    }

    public void setWarningMsg(String warningMsg) {
        this.warningMsg = warningMsg;
    }


}
