package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="ReasonLetter_table")
public class ReasonLetter {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long reasonId;
    private Long memberId;
    private String status = "Reason Submitted";

    @PostPersist
    public void onPostPersist(){
        ReasonSubmitted reasonSubmitted = new ReasonSubmitted();
        BeanUtils.copyProperties(this, reasonSubmitted);
        reasonSubmitted.publishAfterCommit();


    }


    public Long getReasonId() {
        return reasonId;
    }

    public void setReasonId(Long reasonId) {
        this.reasonId = reasonId;
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
