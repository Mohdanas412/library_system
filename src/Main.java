import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        Library library = new Library();

        Book book1 = new Book("978-1", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6));
        Book book2 = new Book("978-2", "Clean Code", "Robert Martin", LocalDate.of(2008, 8, 1));

        library.addBook(book1);
        library.addBook(book2);

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

        // --- Test 2: return it immediately (should be on time, fine 0.0) ---
        System.out.println("\n--- Test 2: return same day ---");
        try {
            double fine2 = library.returnBook("M001", "978-1");
            System.out.println("Fine: " + fine2);
        } catch (BookNotBorrowedException e) {
            System.out.println("Return failed: " + e.getMessage());
        }

        // --- Test 3: return a book that was never borrowed by this member ---
        System.out.println("\n--- Test 3: return without borrowing ---");
        try {
            double fine3 = library.returnBook("M002", "978-2");
            System.out.println("Fine: " + fine3);
        } catch (BookNotBorrowedException e) {
            System.out.println("Return failed: " + e.getMessage());
        }

        // --- Test 4: return a book someone already returned (should fail again) ---
        System.out.println("\n--- Test 4: return an already-returned book ---");
        try {
            double fine4 = library.returnBook("M001", "978-1");
            System.out.println("Fine: " + fine4);
        } catch (BookNotBorrowedException e) {
            System.out.println("Return failed: " + e.getMessage());
        }

        // --- Test 5: return with nonexistent member ---
        System.out.println("\n--- Test 5: nonexistent member ---");
        try {
            double fine5 = library.returnBook("M999", "978-2");
            System.out.println("Fine: " + fine5);
        } catch (BookNotBorrowedException e) {
            System.out.println("Return failed: " + e.getMessage());
        }

        // --- Test 6: return with nonexistent ISBN ---
        System.out.println("\n--- Test 6: nonexistent book ---");
        try {
            double fine6 = library.returnBook("M001", "978-999");
            System.out.println("Fine: " + fine6);
        } catch (BookNotBorrowedException e) {
            System.out.println("Return failed: " + e.getMessage());
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