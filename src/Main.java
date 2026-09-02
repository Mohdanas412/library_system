import java.time.LocalDate;

public class Main {
    Book b = new Book("978-1", "Effective Java", "Bloch", LocalDate.of(2018, 1, 6));{
System.out.println(b.getStatus()); 
}
}