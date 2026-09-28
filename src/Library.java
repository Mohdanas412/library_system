import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Library {
    private List<Book> books;
    private List<Member> members;
    private List<BorrowRecord> borrowRecords = new ArrayList<>();
    private static final double FINE_PER_DAY = 5.0;

    public Library() {
        books = new ArrayList<>();
        members = new ArrayList<>();
    }

    // Our file format is comma-separated, so a comma inside any field would corrupt it.
    // "String..." (varargs) lets us pass any number of strings to one helper.
    private static boolean hasComma(String... fields) {
        for (String f : fields) {
            if (f != null && f.contains(",")) {
                return true;
            }
        }
        return false;
    }

    public boolean addBook(Book newBook) {
        if (hasComma(newBook.getISBN(), newBook.getTitle(), newBook.getAuthor())) {
            System.out.println("Commas are not allowed in ISBN, title or author.");
            return false;
        }
        for (Book b : books) {
            if (b.getISBN().equals(newBook.getISBN())) {
                System.out.println("Book with ISBN " + newBook.getISBN() + " already exists.");
                return false;
            }
        }
        books.add(newBook);
        return true;
    }

    public boolean registerMember(Member newMember) {
        if (hasComma(newMember.getMember_ID(), newMember.getName(),
                newMember.getMobileNum(), newMember.getEmail())) {
            System.out.println("Commas are not allowed in member ID, name, mobile or email.");
            return false;
        }
        for (Member m : members) {
            if (m.getMember_ID().equals(newMember.getMember_ID())) {
                System.out.println("Member with ID " + newMember.getMember_ID() + " already exists.");
                return false;
            }
        }
        members.add(newMember);
        return true;
    }

    public List<Book> viewBooks() {
        return books;
    }

    public List<Member> viewMembers() {
        return members;
    }

    public boolean borrowBook(String member_ID, String ISBN)
            throws BookNotAvailableException, BorrowLimitExceededException {
        Member foundMember = null;
        for (Member m : members) {
            if (m.getMember_ID().equals(member_ID)) {
                foundMember = m;
                break;
            }
        }
        if (foundMember == null) {
            System.out.println("No member found with ID " + member_ID);
            return false;
        }

        Book foundBook = null;
        for (Book b : books) {
            if (b.getISBN().equals(ISBN)) {
                foundBook = b;
                break;
            }
        }
        if (foundBook == null) {
            System.out.println("No book found with ISBN " + ISBN);
            return false;
        }

        if (foundBook.getStatus() != BookStatus.AVAILABLE) {
            throw new BookNotAvailableException("This book is unavailable!");
        }

        if (!foundMember.canBorrowMore()) {
            throw new BorrowLimitExceededException("Member has exceeded the borrow limit!");
        }

        foundBook.setStatus(BookStatus.BORROWED);
        foundMember.getBorrowedBooks().add(foundBook);

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);
        BorrowRecord record = new BorrowRecord(foundBook, foundMember, borrowDate, dueDate);
        borrowRecords.add(record);

        System.out.println("Book " + ISBN + " borrowed successfully by member " + member_ID);
        return true;
    }

    public double returnBook(String member_ID, String ISBN) throws BookNotBorrowedException {
        Member foundMember = null;
        for (Member m : members) {
            if (m.getMember_ID().equals(member_ID)) {
                foundMember = m;
                break;
            }
        }
        if (foundMember == null) {
            System.out.println("No member found with ID " + member_ID);
            return 0.0;
        }

        Book foundBook = null;
        for (Book b : books) {
            if (b.getISBN().equals(ISBN)) {
                foundBook = b;
                break;
            }
        }
        if (foundBook == null) {
            System.out.println("No book found with ISBN " + ISBN);
            return 0.0;
        }

        BorrowRecord activeRecord = null;
        for (BorrowRecord r : borrowRecords) {
            if (r.getBook().equals(foundBook) && r.getMember().equals(foundMember) && !r.isReturned()) {
                activeRecord = r;
                break;
            }
        }
        if (activeRecord == null) {
            throw new BookNotBorrowedException("Member " + member_ID + " has not borrowed " + ISBN);
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = activeRecord.getDueDate();
        double fine = 0.0;
        if (today.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, today);
            fine = daysLate * FINE_PER_DAY;
            System.out.println("Book returned " + daysLate + " day(s) late. Fine: " + fine);
        } else {
            System.out.println("Book returned on time.");
        }

        foundBook.setStatus(BookStatus.AVAILABLE);
        foundMember.getBorrowedBooks().remove(foundBook);
        activeRecord.markReturned();

        return fine;
    }

    public List<Book> searchBooksByTitle(String keyword) {
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooksByAuthor(String keyword) {
        return books.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> listBorrowedBooks() {
        return borrowRecords.stream()
                .filter(r -> !r.isReturned())
                .map(BorrowRecord::getBook)
                .collect(Collectors.toList());
    }

    public List<Book> listOverdueBooks() {
        LocalDate today = LocalDate.now();
        return borrowRecords.stream()
                .filter(r -> !r.isReturned() && today.isAfter(r.getDueDate()))
                .map(BorrowRecord::getBook)
                .collect(Collectors.toList());
    }

    // ---------------- Persistence (Module 6) ----------------

    public void saveToFile() throws IOException {
        // books.txt: ISBN,title,author,publicationDate,status
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Book b : books) {
                writer.write(b.getISBN() + "," + b.getTitle() + "," + b.getAuthor() + ","
                        + b.getPublicationDate() + "," + b.getStatus());
                writer.newLine();
            }
        }

        // members.txt: memberId,name,mobile,email (no borrowed list: derived from records)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("members.txt"))) {
            for (Member m : members) {
                writer.write(m.getMember_ID() + "," + m.getName() + ","
                        + m.getMobileNum() + "," + m.getEmail());
                writer.newLine();
            }
        }

        // borrowrecords.txt: memberId,ISBN,borrowDate,dueDate,returned
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("borrowrecords.txt"))) {
            for (BorrowRecord r : borrowRecords) {
                writer.write(r.getMember().getMember_ID() + "," + r.getBook().getISBN() + ","
                        + r.getBorrowDate() + "," + r.getDueDate() + "," + r.isReturned());
                writer.newLine();
            }
        }
    }

    public void loadFromFile() throws IOException {
        books.clear();
        members.clear();
        borrowRecords.clear();

        // 1. Load books
        File booksFile = new File("books.txt");
        if (booksFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(booksFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] parts = line.split(",", -1);
                        if (parts.length != 5) {
                            System.out.println("Skipping malformed book line: " + line);
                            continue;
                        }
                        String isbn = parts[0];
                        String title = parts[1];
                        String author = parts[2];
                        LocalDate publicationDate = LocalDate.parse(parts[3]);
                        BookStatus status = BookStatus.valueOf(parts[4]);

                        Book b = new Book(isbn, title, author, publicationDate);
                        // BORROWED is derived from active borrow records (step 3),
                        // so it is never trusted from the file. LOST/RESERVED can't be derived, so they are.
                        b.setStatus(status == BookStatus.BORROWED ? BookStatus.AVAILABLE : status);
                        books.add(b);
                    } catch (DateTimeParseException | IllegalArgumentException e) {
                        System.out.println("Skipping broken book line: " + line);
                    }
                }
            }
        } else {
            System.out.println("books.txt not found - starting with no books.");
        }

        // 2. Load members
        File membersFile = new File("members.txt");
        if (membersFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(membersFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length != 4) {
                        System.out.println("Skipping malformed member line: " + line);
                        continue;
                    }
                    members.add(new Member(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } else {
            System.out.println("members.txt not found - starting with no members.");
        }

        // 3. Load borrow records and reconnect relationships by ID
        File recordsFile = new File("borrowrecords.txt");
        if (recordsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(recordsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        String[] parts = line.split(",", -1);
                        if (parts.length != 5) {
                            System.out.println("Skipping malformed borrow record line: " + line);
                            continue;
                        }
                        String memberId = parts[0];
                        String isbn = parts[1];
                        LocalDate borrowDate = LocalDate.parse(parts[2]);
                        LocalDate dueDate = LocalDate.parse(parts[3]);
                        boolean returned = Boolean.parseBoolean(parts[4]);

                        Book foundBook = null;
                        for (Book b : books) {
                            if (b.getISBN().equals(isbn)) {
                                foundBook = b;
                                break;
                            }
                        }

                        Member foundMember = null;
                        for (Member m : members) {
                            if (m.getMember_ID().equals(memberId)) {
                                foundMember = m;
                                break;
                            }
                        }

                        if (foundBook == null || foundMember == null) {
                            System.out.println("Skipping borrow record with missing book/member: " + line);
                            continue;
                        }

                        BorrowRecord record = new BorrowRecord(foundBook, foundMember, borrowDate, dueDate);
                        if (returned) {
                            record.markReturned();
                        } else {
                            foundBook.setStatus(BookStatus.BORROWED);
                            foundMember.getBorrowedBooks().add(foundBook);
                        }
                        borrowRecords.add(record);
                    } catch (DateTimeParseException e) {
                        System.out.println("Skipping broken borrow record line: " + line);
                    }
                }
            }
        } else {
            System.out.println("borrowrecords.txt not found - starting with no borrow records.");
        }
    }
}