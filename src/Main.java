import exceptions.LibraryException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import model.Book;
import model.Member;
import service.Library;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        try {
            library.loadFromFile();
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }

        boolean running = true;
        try {
            while (running) {
                printMenu();
                int choice = readInt("Choose an option: ");
                try {
                    switch (choice) {
                        case 1 -> addBook();
                        case 2 -> registerMember();
                        case 3 -> borrowBook();
                        case 4 -> returnBook();
                        case 5 -> searchByTitle();
                        case 6 -> searchByAuthor();
                        case 7 -> printBooks(library.listBorrowedBooks(), "No books are currently borrowed.");
                        case 8 -> printBooks(library.listOverdueBooks(), "No overdue books.");
                        case 9 -> printBooks(library.viewBooks(), "No books in the catalog.");
                        case 10 -> viewMembers();
                        case 0 -> running = !confirmExit();
                        default -> System.out.println("Invalid option. Please choose from the menu.");
                    }
                } catch (LibraryException e) {
                    // The single place where every business-rule violation is reported.
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            // Input stream closed (Ctrl+D): save and leave instead of crashing.
            System.out.println();
            System.out.println("Input closed. Saving and exiting.");
            saveData();
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Library Management System =====");
        System.out.println(" 1. Add book");
        System.out.println(" 2. Register member");
        System.out.println(" 3. Borrow book");
        System.out.println(" 4. Return book");
        System.out.println(" 5. Search books by title");
        System.out.println(" 6. Search books by author");
        System.out.println(" 7. List borrowed books");
        System.out.println(" 8. List overdue books");
        System.out.println(" 9. View all books");
        System.out.println("10. View all members");
        System.out.println(" 0. Exit");
    }

    // ---------- Actions ----------

    private static void addBook() {
        String isbn = readNonEmpty("ISBN: ");
        String title = readNonEmpty("Title: ");
        String author = readNonEmpty("Author: ");
        LocalDate publicationDate = readPublicationDate();

        if (library.addBook(new Book(isbn, title, author, publicationDate))) {
            System.out.println("Book added.");
            saveData();
        }
    }

    private static void registerMember() {
        String id = readNonEmpty("Member ID: ");
        String name = readNonEmpty("Name: ");
        String mobile = readMobile();
        String email = readEmail();

        if (library.registerMember(new Member(id, name, mobile, email))) {
            System.out.println("Member registered.");
            saveData();
        }
    }

    private static void borrowBook() throws LibraryException {
        String memberId = readNonEmpty("Member ID: ");
        String isbn = readNonEmpty("ISBN: ");
        // borrowBook prints its own success/not-found messages; we only save on success.
        if (library.borrowBook(memberId, isbn)) {
            saveData();
        }
    }

    private static void returnBook() throws LibraryException {
        String memberId = readNonEmpty("Member ID: ");
        String isbn = readNonEmpty("ISBN: ");
        // returnBook prints on-time/late and the fine itself. It throws if nothing was borrowed.
        library.returnBook(memberId, isbn);
        saveData();
    }

    private static void searchByTitle() {
        String keyword = readNonEmpty("Title keyword: ");
        printBooks(library.searchBooksByTitle(keyword), "No books match that title.");
    }

    private static void searchByAuthor() {
        String keyword = readNonEmpty("Author keyword: ");
        printBooks(library.searchBooksByAuthor(keyword), "No books match that author.");
    }

    private static void viewMembers() {
        List<Member> members = library.viewMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        for (Member m : members) {
            System.out.println(m);
        }
    }

    // ---------- Display and persistence ----------

    private static void printBooks(List<Book> books, String emptyMessage) {
        if (books.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        System.out.println(books.size() + " book(s):");
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static void saveData() {
        try {
            library.saveToFile();
        } catch (IOException e) {
            System.out.println("Warning: could not save data: " + e.getMessage());
        }
    }

    private static boolean confirmExit() {
        while (true) {
            String answer = readNonEmpty("Are you sure you want to exit? (y/n): ");
            if (answer.equalsIgnoreCase("y")) {
                saveData();
                return true;
            }
            if (answer.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Please type y or n.");
        }
    }

    // ---------- Input helpers: read a whole line, then parse ----------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static LocalDate readPublicationDate() {
        while (true) {
            System.out.print("Publication date (yyyy-mm-dd): ");
            try {
                LocalDate date = LocalDate.parse(scanner.nextLine().trim());
                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Publication date cannot be in the future.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-mm-dd, for example 2018-01-06.");
            }
        }
    }

    private static String readMobile() {
        while (true) {
            String mobile = readNonEmpty("Mobile (10 digits): ");
            if (mobile.matches("\\d{10}")) {
                return mobile;
            }
            System.out.println("Mobile must be exactly 10 digits.");
        }
    }

    private static String readEmail() {
        while (true) {
            String email = readNonEmpty("Email: ");
            if (email.contains("@") && !email.startsWith("@") && !email.endsWith("@")) {
                return email;
            }
            System.out.println("Please enter a valid email, for example name@example.com.");
        }
    }
}