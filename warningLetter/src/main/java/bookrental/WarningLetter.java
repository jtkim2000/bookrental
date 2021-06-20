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
    private String status;

    @PostPersist
    public void onPostPersist(){
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




}
