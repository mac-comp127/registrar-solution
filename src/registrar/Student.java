package registrar;

import java.util.*;

/**
 * A student at a school.
 */
@SuppressWarnings("WeakerAccess")
public class Student {
    final private String name;
    private Set<Course> courses = new HashSet<Course>();

    public Student(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getName() {
        return name;
    }

    /**
     * Returns all courses this student is currently enrolled in.
     */
    public Set<Course> getCourses() {
        return courses;
    }

    /**
     * Add this student to the given course's roster.
     * Has no effect if the student is already registered.
     */
    public boolean enrollIn(Course course) {
        boolean success = course.enroll(this);
        if(success) {
            courses.add(course);
        }
        return success;
    }

    /**
     * Drops this student from the given course, or removes them from the wait list.
     * Has no effect if student is not already enrolled or waitlisted.
     */
    public void drop(Course course) {
        courses.remove(course);
        course.drop(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
