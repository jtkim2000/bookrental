package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Member_table")
public class Member {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long memberId;
    private String name;
    private String grade = "AAA";
    private String status = "GOOD";

    @PostPersist
    public void onPostPersist(){
        MemberRegistered memberRegistered = new MemberRegistered();
        BeanUtils.copyProperties(this, memberRegistered);
        memberRegistered.publishAfterCommit();


        UpGraded upGraded = new UpGraded();
        BeanUtils.copyProperties(this, upGraded);
        upGraded.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        DownGraded downGraded = new DownGraded();
        BeanUtils.copyProperties(this, downGraded);
        downGraded.publishAfterCommit();


    }


    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
