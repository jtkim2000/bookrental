package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Book_table")
public class Book {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long bookId;
    private String title;
    private Integer qty;
    private String status = "GOOD";

    @PostPersist
    public void onPostPersist(){
        BookRegistered bookRegistered = new BookRegistered();
        BeanUtils.copyProperties(this, bookRegistered);
        bookRegistered.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
        
        BookQtyModified bookQtyModified = new BookQtyModified();
        BeanUtils.copyProperties(this, bookQtyModified);
        bookQtyModified.publishAfterCommit();

        BookQtyStatusModified bookQtyStatusModified = new BookQtyStatusModified();
        BeanUtils.copyProperties(this, bookQtyStatusModified);
        bookQtyStatusModified.publishAfterCommit();

    }


    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
