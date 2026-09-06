import java.util.ArrayList;
import java.util.List;

public class Member {
    private String member_ID;
    private String name;
    private String mobileNum;
    private String email;
    private List<Book> borrowedBooks;

    public Member (String member_ID,String name,String mobileNum,String email) {
        this.member_ID = member_ID;
        this.name = name;
        this.mobileNum = mobileNum;
        this.email = email;
        borrowedBooks = new ArrayList<>();
    }

    public String getMember_ID() {
        return member_ID;
    }
    public String getName() {
        return name;
    }
    public String getMobileNum() {
        return mobileNum;
    }
    public String getEmail() {
        return email;
    }
    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
    return "Member{" +
            "ID='" + member_ID + '\'' +
            ", name='" + name + '\'' +
            ", mobile='" + mobileNum + '\'' +
            ", email='" + email + '\'' +
            ", borrowedBooksCount=" + borrowedBooks.size() +
            '}';
    }
}
