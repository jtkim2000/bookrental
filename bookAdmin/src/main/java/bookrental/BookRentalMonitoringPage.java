package bookrental;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="BookRentalMonitoringPage_table")
public class BookRentalMonitoringPage {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;
        private Long rentalId;
        private Long bookId;
        private Integer qty;
        private String bookStatus;
        private String status;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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
