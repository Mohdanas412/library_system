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

}
