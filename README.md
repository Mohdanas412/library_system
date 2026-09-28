# Library Management System

A console-based library application written in **core Java** (no frameworks, no database). Members borrow and return books under real business rules: a 14-day loan, a per-day overdue fine, and a 3-book limit. All data survives restarts through plain text files.

This is the third project in my Java Core series (Banking System → Student Management System → Library Management System). It is the first one that models a **relationship between two entities** and rebuilds that relationship when loading from disk.

---

## Features

- Add books and register members (with input validation)
- Borrow and return books, with rules enforced by custom exceptions
- 14-day loan period, automatic due dates, and a flat ₹5/day fine on late returns
- Search books by title or author (partial, case-insensitive)
- List all books, all members, currently borrowed books, and overdue books
- Automatic persistence: data is loaded at startup and saved after every change and on exit
- Malformed data lines are skipped with a message instead of crashing the program
- Bad console input (letters at a number prompt, empty fields, invalid dates) is re-asked, never a crash

## Menu

```
 1. Add book                  6. Search books by author
 2. Register member           7. List borrowed books
 3. Borrow book               8. List overdue books
 4. Return book               9. View all books
 5. Search books by title    10. View all members
                              0. Exit
```

---

## Java Concepts Demonstrated

| Concept | Where it is used |
|---|---|
| Classes, objects, encapsulation, constructors | `Book`, `Member`, `BorrowRecord` |
| Enums | `BookStatus` (AVAILABLE, BORROWED, RESERVED, LOST) instead of raw strings |
| Object relationships and nested collections | `Member` holds a `List<Book>`; `BorrowRecord` links one `Book` to one `Member` |
| Immutability with `final` | `BorrowRecord` fields (book, member, dates) cannot change after creation |
| Custom checked exception hierarchy | `LibraryException` and three subclasses |
| `java.time` | `LocalDate`, `plusDays()`, `ChronoUnit.DAYS.between()`, `isAfter()` |
| `equals()` / `hashCode()` | `Book` is compared by ISBN, not by reference |
| Streams and method references | Title/author search, borrowed list, overdue list |
| File I/O | `BufferedReader`/`BufferedWriter`, try-with-resources, `IOException` handling |
| Parsing and defensive programming | `split(",", -1)`, length checks, `LocalDate.parse`, `BookStatus.valueOf`, multi-catch |
| Varargs | `hasComma(String... fields)` helper |
| Packages and access modifiers | `model`, `exceptions`, `service`, with a `public` API across packages |
| Scanner input handling | Read whole lines and parse them, avoiding the `nextInt()` skipped-line bug |

---

## Architecture

```
Main  (Scanner + menu + display only)
  │
  ▼
Library  (all business rules, relationships, search, persistence coordination)
  │
  ├── List<Book>          ─┐
  ├── List<Member>         ├── model classes: data and their own behavior
  └── List<BorrowRecord>  ─┘
  │
  ▼
books.txt · members.txt · borrowrecords.txt   (plain text persistence)
```

### Project structure

```
library_system/
├── src/
│   ├── Main.java                  Scanner-driven menu (no business rules)
│   ├── TestRunner.java            Hardcoded regression harness (Tests 1-11 + save/reload)
│   ├── model/
│   │   ├── Book.java
│   │   ├── BookStatus.java
│   │   ├── BorrowRecord.java
│   │   └── Member.java
│   ├── exceptions/
│   │   ├── LibraryException.java
│   │   ├── BookNotAvailableException.java
│   │   ├── BorrowLimitExceededException.java
│   │   └── BookNotBorrowedException.java
│   └── service/
│       └── Library.java
├── .gitignore
└── README.md
```

### Responsibilities

| Class | Responsibility |
|---|---|
| `Book`, `Member` | Data and their own behavior (`equals`/`hashCode`/`toString`, `canBorrowMore()`). No input, output, or file code. |
| `BorrowRecord` | Records **who** borrowed **what**, **when**, **when it is due**, and whether it is closed. |
| `Library` | Every business rule, relationship management, search queries, and save/load. The only class that sees all three collections. |
| `Main` | Reads input, calls `Library`, prints results, and reports `LibraryException` messages in one place. |
| `TestRunner` | Repeatable regression checks so earlier behavior is not lost. |

---

## Class Relationships

```
Member 1 ────── * BorrowRecord * ────── 1 Book
```

- A `Member` can have many borrow records over time (limited to 3 *active* ones).
- A `Book` can appear in many records over time, but at most one *active* record at once.
- `BorrowRecord` is the **join entity**: the same shape as a foreign-key table in SQL.
- `BorrowRecord` is the **source of truth** for who has what. `Member.borrowedBooks` and `Book.status = BORROWED` are kept in sync with it.

---

## Business Rules

| Rule | Detail |
|---|---|
| Loan period | 14 days from the borrow date |
| Fine | Flat ₹5 per day late (`FINE_PER_DAY = 5.0`) |
| Overdue boundary | **Due today is NOT overdue.** A book is overdue only if today is strictly after the due date. |
| Borrow limit | Maximum 3 active borrowed books per member |
| Book identity | ISBN. Two `Book` objects with the same ISBN are equal, and duplicate ISBNs are rejected. |
| Member identity | Member ID. Duplicate IDs are rejected. |
| Availability | Only `AVAILABLE` books can be borrowed |
| `BORROWED` status | **Derived**, never trusted from disk: it is true only if an active borrow record exists |

### Error handling design

Two kinds of failure are handled differently on purpose:

- **Input errors** (member or book not found) print a message and return `false` / `0.0`. They are not part of the exception hierarchy.
- **Business-rule violations** throw a custom checked exception:
  - `BookNotAvailableException`: the book is already out (or LOST/RESERVED)
  - `BorrowLimitExceededException`: the member already has 3 books
  - `BookNotBorrowedException`: returning a book the member has not borrowed

All three extend `LibraryException`, so `Main` handles every rule violation with a single `catch (LibraryException e)`.

---

## Persistence

Three files, one entity type each, created in the **working directory** (run from the project root):

```
books.txt          ISBN,title,author,publicationDate,status
members.txt        memberId,name,mobile,email
borrowrecords.txt  memberId,ISBN,borrowDate,dueDate,returned
```

Dates are stored in ISO format (`2026-09-24`) and read back with `LocalDate.parse()`.

### Design decisions

- **Relationships are stored as IDs, not embedded objects.** `borrowrecords.txt` holds a member ID and an ISBN, like a foreign key. Embedding a whole `Book` inside a member line would duplicate data that could silently disagree.
- **Members do not store their borrowed list.** It is derived from the borrow records.
- **Load order matters:** books → members → borrow records. A record line contains only two IDs, so the real objects must already exist to be looked up and reconnected. The **same in-memory object** is reused, never a copy.
- **Store facts, derive the rest.** `LOST` and `RESERVED` cannot be reconstructed from any record, so they are saved and trusted. `BORROWED` is **not** trusted from `books.txt`: it is reset to `AVAILABLE` on load and re-derived from active records. This prevents a "ghost loan", where a hand-edited file leaves a book marked BORROWED with no record, permanently stuck.
- **Save policy:** after every successful change and on exit, so a crash loses at most the last unsaved action.

### Malformed data policy

- Every loader checks the field count (books 5, members 4, records 5). A bad line is **skipped with a message**, and the rest of the file still loads.
- Bad dates, unknown status values, and records pointing to a missing book or member are skipped the same way.
- **Commas are forbidden in fields at entry.** With a comma-separated format, a name like `Doe, John` would silently shift the fields and produce a valid-looking but wrong record, which is worse than a loud failure. `addBook()` and `registerMember()` reject commas up front.
- A missing file on first run is not an error: the program starts empty with a short message.

---

## How to Run

Requires **Java 14 or newer** (developed and run on OpenJDK 25). Run all commands from the project root so the data files are created there.

**Compile:**
```bash
mkdir -p out
javac -d out src/model/*.java src/exceptions/*.java src/service/*.java src/Main.java src/TestRunner.java
```

**Run the application:**
```bash
java -cp out Main
```

**Run the regression harness** (Tests 1-11 plus a save/reload check; it writes the three data files, so delete them afterwards):
```bash
java -cp out TestRunner
rm -f books.txt members.txt borrowrecords.txt
```

---

## Testing

- `TestRunner` covers: a valid borrow, a same-day return, returning a never-borrowed book, returning twice, a nonexistent member, a nonexistent book, title search, author search, listing borrowed books, listing overdue books, `equals()` by ISBN, and a save-then-reload check that restores books, members, and borrow state.
- The menu application has been exercised end to end: add, register, borrow, all searches, all lists, exit confirmation, and reload after restart.
- Before any commit, the source is checked for leftover test rigs (for example, `grep -rn "minusDays" src/`) and the borrow code is confirmed to use `plusDays(14)`. Data files are deleted so no generated `.txt` files are committed.

---

## Known Limitations

These are deliberate scope decisions, not oversights:

- **Loaded data is not re-validated by the menu's input rules.** Main's checks (10-digit mobile, email shape, no future publication date) apply only at entry. Older or hand-edited data files can contain values that would be rejected today.
- **Duplicate ISBNs or member IDs in a hand-edited file both load**, since there is no de-duplication on load.
- **Setters can bypass the comma guard.** `Book.setTitle/setAuthor` and the `Member` setters can introduce a comma after `addBook()`/`registerMember()`. Such a record is skipped with a message on the next load.
- **`saveToFile()` is not atomic.** It overwrites all three files each time, so a crash mid-save could leave them inconsistent. There is no backup.
- **Files are relative to the working directory**, so the program must be run from the project root.
- **The 3-book limit is hardcoded** in `Member.canBorrowMore()`.
- **One copy per ISBN.** A book is a single physical copy.
- **No abstraction layer for other item types.** There is no `LibraryItem` parent class. The system has exactly one item type, so an abstract parent would be a guess. If Magazine or DVD is ever added, `BorrowRecord` and `Member` would be generalized to a common item type at that point.
- **No authentication and no concurrency.** It is a single-user console app.

---

## Future Improvements

Clearly out of scope for this project, and listed only as possible next steps:

- **Reservations** using a `Queue<Member>` per borrowed book
- **Multiple copies** per ISBN (a copies-available count)
- **Fine payment** linked to the Banking System project
- **Configurable rules** (loan length, fine rate, borrow limit) instead of hardcoded values
- **Atomic saves** (write to a temp file, then rename)
- **Database storage** (for example, MySQL), replacing text files and list scans with indexed lookups
- **Spring Boot + REST API** version as a new project, where `Library` becomes the service layer, the files become a repository, `BorrowRecord` becomes a join entity, and the custom exceptions map to centralized exception handling
- **Authentication** and a web frontend
- **Multiple branches** of the library

---

## Author

Built as part of a build-first Java learning path: each module started with a limitation, learned only the concept needed to fix it, then implemented, tested, broke, debugged, and committed.