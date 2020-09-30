package registrar;

import java.util.LinkedList;
import java.util.List;

class TestObjectFactory {
    private List<Student> students = new LinkedList<Student>();
    private List<Course> courses = new LinkedList<Course>();

    Student makeStudent(String name) {
        Student s = new Student(name);
        students.add(s);
        return s;
    }

    Course makeCourse(String catalogNumber, String title) {
        Course c = new Course(catalogNumber, title);
        courses.add(c);
        return c;
    }

    void enrollMultipleStudents(Course c, int count) {
        for(; count > 0; count--)
            makeStudent("Anonymous student " + count).enrollIn(c);
    }

    List<Course> allCourses() {
        return courses;
    }

    List<Student> allStudents() {
        return students;
    }
}
