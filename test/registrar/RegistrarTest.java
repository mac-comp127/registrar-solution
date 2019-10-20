package registrar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("WeakerAccess")
class RegistrarTest {
    // ------ Setup ------

    private TestObjectFactory factory = new TestObjectFactory();
    private Course comp127, math6, basketWeaving101;
    private Student sally, fred, zongo;

    @BeforeEach
    public void createStudents() {
        sally = factory.makeStudent("Sally");
        fred = factory.makeStudent("Fred");
        zongo = factory.makeStudent("Zongo Jr.");
    }

    @BeforeEach
    public void createCourses() {
        comp127 = factory.makeCourse("COMP 127", "Software Fun Fun");
        comp127.setEnrollmentLimit(16);

        math6 = factory.makeCourse("Math 6", "All About the Number Six");
        basketWeaving101 = factory.makeCourse("Underwater Basket Weaving 101", "Senior spring semester!");
    }

    // ------ Enrolling ------

    @Test
    void studentStartsInNoCourses() {
        assertEquals(Set.of(), sally.getCourses());
    }

    @Test
    void studentCanEnroll() {
        sally.enrollIn(comp127);
        assertEquals(Set.of(comp127), sally.getCourses());
        assertEquals(Set.of(sally), comp127.getStudents());
    }

    @Test
    void doubleEnrollingHasNoEffect() {
        sally.enrollIn(comp127);
        sally.enrollIn(comp127);
        assertEquals(Set.of(comp127), sally.getCourses());
        assertEquals(Set.of(sally), comp127.getStudents());
    }


    // ------ Enrollment limits ------

    @Test
    void enrollmentLimitDefaultsToUnlimited() {
        factory.enrollMultipleStudents(math6, 1000);
        assertEquals(List.of(), math6.getWaitList());
        assertEquals(1000, math6.getStudents().size());
    }

    @Test
    void coursesHaveEnrollmentLimits() {
        comp127.setEnrollmentLimit(16);
        assertEquals(16, comp127.getEnrollmentLimit());
    }

    @Test
    void enrollingUpToLimitAllowed() {
        factory.enrollMultipleStudents(comp127, 15);
        assertTrue(sally.enrollIn(comp127));
        assertEquals(List.of(), comp127.getWaitList());
        assertTrue(comp127.getStudents().contains(sally));
    }

    @Test
    void enrollingPastLimitPushesToWaitList() {
        factory.enrollMultipleStudents(comp127, 16);
        assertFalse(sally.enrollIn(comp127));
        assertEquals(List.of(sally), comp127.getWaitList());
        assertFalse(comp127.getStudents().contains(sally));
    }

    @Test
    void waitListPreservesEnrollmentOrder() {
        factory.enrollMultipleStudents(comp127, 16);
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        zongo.enrollIn(comp127);
        assertEquals(List.of(sally, fred, zongo), comp127.getWaitList());
    }

    @Test
    void doubleEnrollingInFullCourseHasNoEffect() {
        sally.enrollIn(comp127);
        factory.enrollMultipleStudents(comp127, 20);
        assertTrue(sally.enrollIn(comp127)); // full now, but Sally was already enrolled
        assertTrue(comp127.getStudents().contains(sally));
        assertFalse(comp127.getWaitList().contains(sally));
    }

    @Test
    void doubleEnrollingAfterWaitListedHasNoEffect() {
        factory.enrollMultipleStudents(comp127, 16);
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        zongo.enrollIn(comp127);
        fred.enrollIn(comp127);
        assertFalse(sally.enrollIn(comp127));

        assertEquals(List.of(sally, fred, zongo), comp127.getWaitList());
    }

    @Test
    void cannotChangeEnrollmentLimitOnceStudentsRegister() {
        basketWeaving101.setEnrollmentLimit(10);
        fred.enrollIn(basketWeaving101);
        assertThrows(IllegalStateException.class, () -> {
            basketWeaving101.setEnrollmentLimit(11);
        });
    }

    // ------ Post-test invariant check ------
    //
    // This is a bit persnickety for day-to-day testing, but these kinds of checks are appropriate
    // for security sensitive or otherwise mission critical code. Some people even add them as
    // runtime checks in the code, instead of writing them as tests.

    @AfterEach
    public void checkInvariants() {
        for (Student s : factory.allStudents())
            checkStudentInvariants(s);
        for (Course c : factory.allCourses())
            checkCourseInvariants(c);
    }

    private void checkStudentInvariants(Student s) {
        for (Course c : s.getCourses())
            assertTrue(
                c.getStudents().contains(s),
                s + " thinks they are enrolled in " + c
                    + ", but " + c + " does not have them in the list of students");
    }

    private void checkCourseInvariants(Course c) {
        Set<Student> waitListUnique = new HashSet<>(c.getWaitList());
        assertEquals(
            waitListUnique.size(),
            c.getWaitList().size(),
            c + " wait list contains duplicates: " + c.getWaitList());

        waitListUnique.retainAll(c.getStudents());
        assertEquals(
            Set.of(),
            waitListUnique,
            c + " contains students who are both registered and waitlisted");

        for (Student s : c.getStudents()) {
            assertTrue(
                s.getCourses().contains(c),
                c + " thinks " + s + " is enrolled, but " + s + " doesn't think they're in the class");
        }

        for (Student s : c.getWaitList()) {
            assertFalse(
                s.getCourses().contains(c),
                c + " lists " + s + " as waitlisted, but " + s + " thinks they are enrolled");
        }

        assertTrue(
            c.getStudents().size() <= c.getEnrollmentLimit(),
            c + " has an enrollment limit of " + c.getEnrollmentLimit()
                + ", but has " + c.getStudents().size() + " students");

        if (c.getStudents().size() < c.getEnrollmentLimit()) {
            assertEquals(
                List.of(),
                c.getWaitList(),
                c + " is not full, but has students waitlisted");
        }
    }
}
