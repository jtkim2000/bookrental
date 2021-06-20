package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="BookRequest_table")
public class BookRequest {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long requestId;
    private Long memberId;
    private Long bookId;
    private Integer qty;
    private String status = "Book Requested";

    @PostPersist
    public void onPostPersist(){

        BookRentalRequestApplication.applicationContext.getBean(bookrental.external.BookService.class)
        .checkBookQtyAndModifyQty(this.getBookId(), this.getQty());

        BookRequested bookRequested = new BookRequested();
        BeanUtils.copyProperties(this, bookRequested);
        bookRequested.publishAfterCommit();
    }


    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
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
