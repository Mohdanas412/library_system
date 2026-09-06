import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        // 1. Create a Library
        Library library = new Library();

        // 2. Create Book objects
        Book book1 = new Book("978-1", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
        Book book2 = new Book("978-2", "Clean Code", "Robert Martin", LocalDate.of(2008, 8, 1));
        Book book3 = new Book("978-1", "Duplicate ISBN Test", "Someone Else", LocalDate.of(2020, 1, 1));

        // 3. Add books to the library
        boolean added1 = library.addBook(book1);
        System.out.println("Added book1: " + added1);

        boolean added2 = library.addBook(book2);
        System.out.println("Added book2: " + added2);

        boolean added3 = library.addBook(book3);
        System.out.println("Added book3 (duplicate): " + added3);

        // 4. Create Member objects
        Member member1 = new Member("M001", "Alice", "9999999999", "alice@example.com");
        Member member2 = new Member("M002", "Bob", "8888888888", "bob@example.com");

        // 5. Register members
        library.registerMember(member1);
        library.registerMember(member2);

        // 6. Print all books
        System.out.println("\n--- All Books ---");
        for (Book b : library.viewBooks()) {
            System.out.println(b);
        }

        // 7. Print all members
        System.out.println("\n--- All Members ---");
        for (Member m : library.viewMembers()) {
            System.out.println(m);
        }
    }
}