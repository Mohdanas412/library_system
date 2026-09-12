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
        boolean t1 = library.borrowBook("M001", "978-1");
        System.out.println("Result: " + t1);

        // --- Test 2: same member, same book again ---
        System.out.println("\n--- Test 2: same book, same member, again ---");
        boolean t2 = library.borrowBook("M001", "978-1");
        System.out.println("Result: " + t2);

        // --- Test 3: different member, same (already borrowed) book ---
        System.out.println("\n--- Test 3: same book, different member ---");
        boolean t3 = library.borrowBook("M002", "978-1");
        System.out.println("Result: " + t3);

        // --- Test 4: nonexistent ISBN ---
        System.out.println("\n--- Test 4: nonexistent book ---");
        boolean t4 = library.borrowBook("M001", "978-999");
        System.out.println("Result: " + t4);

        // --- Test 5: nonexistent member ---
        System.out.println("\n--- Test 5: nonexistent member ---");
        boolean t5 = library.borrowBook("M999", "978-2");
        System.out.println("Result: " + t5);

        // --- Test 6: exceed borrow limit ---
        // M002 borrows 3 different, currently-available books first
        System.out.println("\n--- Test 6: exceed limit ---");
        System.out.println("Setup borrows for M002:");
        System.out.println("  " + library.borrowBook("M002", "978-2"));
        System.out.println("  " + library.borrowBook("M002", "978-3"));
        System.out.println("  " + library.borrowBook("M002", "978-4"));

        // Now M002 has 3 books. Add one more available book to test the LIMIT specifically.
        Book book5 = new Book("978-5", "Design Patterns", "Gang of Four", LocalDate.of(1994, 10, 21));
        library.addBook(book5);

        boolean t6 = library.borrowBook("M002", "978-5");
        System.out.println("4th borrow attempt result: " + t6);

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