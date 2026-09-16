import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books;
    private List<Member> members;
    private List<BorrowRecord> borrowRecords = new ArrayList<>();
    private static final double FINE_PER_DAY = 5.0;

    public Library() {
        books = new ArrayList<>();
        members = new ArrayList<>();
    }

    public boolean addBook(Book newBook) {
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

    public boolean borrowBook(String member_ID, String ISBN) throws BookNotAvailableException, BorrowLimitExceededException {
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
        LocalDate dueDate = borrowDate.minusDays(3);
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
}