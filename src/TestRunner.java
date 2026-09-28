import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import model.Book;
import service.Library;

public class TestRunner {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        try {
            library.loadFromFile();
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addBook();
                case 2 -> viewBooks();
                case 0 -> running = !confirmExit();
                default -> System.out.println("Invalid option. Please choose from the menu.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Library Management System =====");
        System.out.println("1. Add book");
        System.out.println("2. View all books");
        System.out.println("0. Exit");
    }

    // ---------- Actions ----------

    private static void addBook() {
        String isbn = readNonEmpty("ISBN: ");
        String title = readNonEmpty("Title: ");
        String author = readNonEmpty("Author: ");
        LocalDate publicationDate = readDate("Publication date");

        if (library.addBook(new Book(isbn, title, author, publicationDate))) {
            System.out.println("Book added.");
            saveData();
        }
    }

    private static void viewBooks() {
        List<Book> books = library.viewBooks();
        if (books.isEmpty()) {
            System.out.println("No books in the catalog.");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    // ---------- Persistence ----------

    private static void saveData() {
        try {
            library.saveToFile();
        } catch (IOException e) {
            System.out.println("Warning: could not save data: " + e.getMessage());
        }
    }

    private static boolean confirmExit() {
        String answer = readNonEmpty("Are you sure you want to exit? (y/n): ");
        if (answer.equalsIgnoreCase("y")) {
            saveData();
            return true;
        }
        return false;
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

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-mm-dd): ");
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use yyyy-mm-dd, for example 2018-01-06.");
            }
        }
    }
}