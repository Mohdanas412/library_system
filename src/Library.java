import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books;
    private List<Member> members;

    public Library () {
        books = new ArrayList<>();
        members = new ArrayList<>();
    }
    public boolean addBook(Book newBook) {
        for (Book  b : books) {
            if(b.getISBN().equals(newBook.getISBN())) {
                System.out.println("Book with ISBN " + newBook.getISBN() + " already exists.");
                return false;
            }
        }
        books.add(newBook);
        return true;
    }
    public boolean registerMember(Member newMember) {
        for (Member m : members) {
            if(m.getMember_ID().equals(newMember.getMember_ID())) {
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

    public boolean borrowBook(String member_ID,String ISBN) {
        Member foundMember = null;
        for (Member m : members) {
            if (m.getMember_ID().equals(member_ID)) {
                foundMember = m;
                break;
            }
        }
        if (foundMember == null) {
            System.out.println("No member found with ID" + member_ID);
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
            System.out.println("No book found with ISBN" + ISBN);
            return false;
        }

        if (foundBook.getStatus() != BookStatus.AVAILABLE) {
            System.out.println("Book with ISBN" + ISBN + "is not available.");
            return false;
        }

        if (!foundMember.canBorrowMore()) {
            System.out.println("Member" + member_ID + "has reached the borrow limit");
            return false;
        }
        foundBook.setStatus(BookStatus.BoRROWED);
        foundMember.getBorrowedBooks().add(foundBook);

        System.out.println("Book " + ISBN + " borrowed successfully by member " + member_ID);
        return true;

    }

}
