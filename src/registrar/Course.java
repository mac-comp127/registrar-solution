package registrar;

import java.util.*;

/**
 * A course that can enroll students.
 */
@SuppressWarnings("WeakerAccess")
public class Course {
    public static final int UNLIMITED_ENROLLMENT = Integer.MAX_VALUE;

    private final String catalogNumber;
    private final String title;
    private Set<Student> roster = new HashSet<>();
    private List<Student> waitlist = new ArrayList<>();
    private int enrollmentLimit = UNLIMITED_ENROLLMENT;

    public Course(String catalogNumber, String title) {
        this.catalogNumber = Objects.requireNonNull(catalogNumber, "catalogNumber");
        this.title = Objects.requireNonNull(title, "title");
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public String getTitle() {
        return title;
    }

    /**
     * The maximum number of students who can be enrolled in this course.
     */
    public int getEnrollmentLimit() {
        return enrollmentLimit;
    }

    public void setEnrollmentLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("course cannot have negative enrollment limit: " + limit);
        }
        if (!getRoster().isEmpty()) {
            throw new IllegalStateException("cannot change enrollment limit once students are enrolled");
        }

        this.enrollmentLimit = limit;
    }

    /**
     * Returns all students currently enrolled in this course.
     */
    public Set<Student> getRoster() {
        return roster;
    }

    /**
     * Returns students waiting to be enrolled. If any students drop,
     * the course will automatically enroll students from the waitlist.
     */
    public List<Student> getWaitlist() {
        return waitlist;
    }

    // 👋 Note that this method isn’t public! 👋
    //
    // It does not say “public” or “private,” which makes it visible to other classes in the same
    // package (in this case, Student).
    //
    // Q: Is enroll() is part of the API or not?
    // A: It is part of the internal API that Course provides to Student, but it is not part of
    //    the public API. The Student and Course classes collaborate to preserve their invariants:
    //    only Student can call this method, and the enrollment succeeds, then the student records
    //    that they are enrolled.
    //
    boolean enroll(Student student) {
        if (roster.contains(student)) {
            return true;
        }
        if (isFull()) {
            addToWaitlist(student);
            return false;
        }
        roster.add(student);
        return true;
    }

    void drop(Student student) {
        waitlist.remove(student);
        roster.remove(student);
        if (!getWaitlist().isEmpty() && getRoster().size() < enrollmentLimit) {
            waitlist.remove(0).enrollIn(this);
        }
    }

    /**
     * Returns true if the course has reached its enrollment limit.
     */
    public boolean isFull() {
        return roster.size() >= enrollmentLimit;
    }

    private void addToWaitlist(Student s) {
        if (!waitlist.contains(s)) {
            waitlist.add(s);
        }
    }

    @Override
    public String toString() {
        return getCatalogNumber();
    }
}
