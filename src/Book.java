import java.time.LocalDate;

public class Book {
     private String ISBN;
     private String title;
     private String author;
     private LocalDate publicationDate;
     private BookStatus status;

     public Book(String ISBN, String title, String author, LocalDate publicationDate) {
          this.ISBN = ISBN;
          this.title = title;
          this.author = author;
          this.publicationDate = publicationDate;
          this.status = BookStatus.AVAILABLE;
     }

     public String getISBN() {
          return ISBN;
     }
     public String getTitle() {
          return title;
     }
     public String getAuthor() {
          return author;
     }
     public LocalDate getPublicationDate() {
          return publicationDate;
     }
     public BookStatus getStatus() {
          return status;
     }
     public void setTitle(String title) {
          this.title = title;
     }
     public void setAuthor(String author) {
          this.author = author;
     }
     public void setPublicationDate(LocalDate publicationDate) {
          this.publicationDate = publicationDate;
     }
     public void setStatus(BookStatus status) {
          this.status = status;
     }

     @Override 
     public String toString() {
          return "Book{" +
                  "ISBN='" + ISBN + '\'' +
                  ", title='" + title + '\'' +
                  ", author='" + author + '\'' +
                  ", publicationDate=" + publicationDate +
                  ", status=" + status +
                  '}';
     }
     @Override 
     public boolean equals(Object obj) {
          if (this == obj) {
               return true;
          }
          if (obj == null || !(obj instanceof Book)) {
               return false;
          }
          Book other = (Book) obj;
          return this.ISBN.equals(other.ISBN); 
     }

     @Override 
     public int hashCode() {
          return ISBN.hashCode();
     }
}
