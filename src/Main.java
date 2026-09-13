import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        Library library = new Library();

        // --- Setup ---
        Book book1 = new Book("978-1", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
        Book book2 = new Book("978-2", "Clean Code", "Robert Martin", LocalDate.of(2008, 8, 1));
        Book book3 = new Book("978-3", "Head First Java", "Kathy Sierra", LocalDate.of(2005, 2, 9));
        Book book4 = new Book("978-4", "Java Concurrency in Practice", "Brian Goetz", LocalDate.of(2006, 5, 19));

        library.addBook(book1);
        library.addBook(book2);
        library.addBook(book3);
        library.addBook(book4);

        Member member1 = new Member("M001", "Alice", "9999999999", "alice@example.com");
        Member member2 = new Member("M002", "Bob", "8888888888", "bob@example.com");

        library.registerMember(member1);
        library.registerMember(member2);

        // --- Test 1: valid borrow ---
        System.out.println("\n--- Test 1: valid borrow ---");
        try {
            boolean t1 = library.borrowBook("M001", "978-1");
            System.out.println("Result: " + t1);
        } catch (LibraryException e) {
            System.out.println("Borrow failed: " + e.getMessage());
        }

        // --- Test 2: same book, same member again ---
        System.out.println("\n--- Test 2: same book, same member, again ---");
        try {
            boolean t2 = library.borrowBook("M001", "978-1");
            System.out.println("Result: " + t2);
        } catch (LibraryException e) {
            System.out.println("Borrow failed: " + e.getMessage());
        }

        // --- Test 3: different member, same (already borrowed) book ---
        System.out.println("\n--- Test 3: same book, different member ---");
        try {
            boolean t3 = library.borrowBook("M002", "978-1");
            System.out.println("Result: " + t3);
        } catch (LibraryException e) {
            System.out.println("Borrow failed: " + e.getMessage());
        }

        // --- Test 4: nonexistent ISBN ---
        System.out.println("\n--- Test 4: nonexistent book ---");
        try {
            boolean t4 = library.borrowBook("M001", "978-999");
            System.out.println("Result: " + t4);
        } catch (LibraryException e) {
            System.out.println("Borrow failed: " + e.getMessage());
        }

        // --- Test 5: nonexistent member ---
        System.out.println("\n--- Test 5: nonexistent member ---");
        try {
            boolean t5 = library.borrowBook("M999", "978-2");
            System.out.println("Result: " + t5);
        } catch (LibraryException e) {
            System.out.println("Borrow failed: " + e.getMessage());
        }

        // --- Test 6: exceed borrow limit ---
        System.out.println("\n--- Test 6: exceed limit ---");
        System.out.println("Setup borrows for M002:");
        try {
            System.out.println("  " + library.borrowBook("M002", "978-2"));
        } catch (LibraryException e) {
            System.out.println("  Borrow failed: " + e.getMessage());
        }
        try {
            System.out.println("  " + library.borrowBook("M002", "978-3"));
        } catch (LibraryException e) {
            System.out.println("  Borrow failed: " + e.getMessage());
        }
        try {
            System.out.println("  " + library.borrowBook("M002", "978-4"));
        } catch (LibraryException e) {
            System.out.println("  Borrow failed: " + e.getMessage());
        }

        Book book5 = new Book("978-5", "Design Patterns", "Gang of Four", LocalDate.of(1994, 10, 21));
        library.addBook(book5);

        try {
            boolean t6 = library.borrowBook("M002", "978-5");
            System.out.println("4th borrow attempt result: " + t6);
        } catch (LibraryException e) {
            System.out.println("4th borrow attempt failed: " + e.getMessage());
        }

        // --- Final state ---
        System.out.println("\n--- Final Book States ---");
        for (Book b : library.viewBooks()) {
            System.out.println(b);
        }

        System.out.println("\n--- Final Member States ---");
        for (Member m : library.viewMembers()) {
            System.out.println(m);
        }
    }
}