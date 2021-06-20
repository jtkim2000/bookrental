package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="BookRental_table")
public class BookRental {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long rentalId;
    private Long bookId;
    private Integer qty;
    private Long memberId;
    private String bookStatus = "GOOD";
    private String status;

    @PostPersist
    public void onPostPersist(){
        BookLent bookLent = new BookLent();
        BeanUtils.copyProperties(this, bookLent);
        bookLent.publishAfterCommit();


    }

    @PostUpdate
    public void onPostUpdate(){
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("** PUB :: BookReturned : status changed to " + this.status.toString());
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        BookReturned bookReturned = new BookReturned();
        this.setStatus("Book Returned");
        BeanUtils.copyProperties(this, bookReturned);
        //bookReturned.setStatus("Book Returned");
        bookReturned.publishAfterCommit();

    }

    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
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
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public String getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
