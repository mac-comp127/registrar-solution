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
        assertEquals(Set.of(sally), comp127.getRoster());
    }

    @Test
    void doubleEnrollingHasNoEffect() {
        sally.enrollIn(comp127);
        sally.enrollIn(comp127);
        assertEquals(Set.of(comp127), sally.getCourses());
        assertEquals(Set.of(sally), comp127.getRoster());
    }


    // ------ Enrollment limits ------

    @Test
    void enrollmentLimitDefaultsToUnlimited() {
        factory.enrollMultipleStudents(math6, 1000);
        assertEquals(List.of(), math6.getWaitlist());
        assertEquals(1000, math6.getRoster().size());
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
        assertEquals(List.of(), comp127.getWaitlist());
        assertTrue(comp127.getRoster().contains(sally));
    }

    @Test
    void enrollingPastLimitPushesToWaitlist() {
        factory.enrollMultipleStudents(comp127, 16);
        assertFalse(sally.enrollIn(comp127));
        assertEquals(List.of(sally), comp127.getWaitlist());
        assertFalse(comp127.getRoster().contains(sally));
    }

    @Test
    void waitlistPreservesEnrollmentOrder() {
        factory.enrollMultipleStudents(comp127, 16);
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        zongo.enrollIn(comp127);
        assertEquals(List.of(sally, fred, zongo), comp127.getWaitlist());
    }

    @Test
    void doubleEnrollingInFullCourseHasNoEffect() {
        sally.enrollIn(comp127);
        factory.enrollMultipleStudents(comp127, 20);
        assertTrue(sally.enrollIn(comp127)); // full now, but Sally was already enrolled
        assertTrue(comp127.getRoster().contains(sally));
        assertFalse(comp127.getWaitlist().contains(sally));
    }

    @Test
    void doubleEnrollingAfterWaitlistedHasNoEffect() {
        factory.enrollMultipleStudents(comp127, 16);
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        zongo.enrollIn(comp127);
        fred.enrollIn(comp127);
        assertFalse(sally.enrollIn(comp127));

        assertEquals(List.of(sally, fred, zongo), comp127.getWaitlist());
    }

    @Test
    void canReduceEnrollmentLimitOnceStudentsRegister() {
        basketWeaving101.setEnrollmentLimit(10);
        fred.enrollIn(basketWeaving101);
        basketWeaving101.setEnrollmentLimit(8);
        assertEquals(8, basketWeaving101.getEnrollmentLimit());
    }

    @Test
    void waitlistedStudentsEnrolledIfLimitIncreased() {
        factory.enrollMultipleStudents(comp127, 16);
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        zongo.enrollIn(comp127);
        comp127.setEnrollmentLimit(18);
        assertTrue(comp127.getRoster().contains(sally));
        assertTrue(comp127.getRoster().contains(fred));
        assertEquals(List.of(zongo), comp127.getWaitlist());
    }

    @Test
    void cannotLowerEnrollmentLimitBelowClassSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            factory.enrollMultipleStudents(comp127, 8);
            comp127.setEnrollmentLimit(7);
        });
    }

    // ------ Drop courses ------

    @Test
    void studentCanDrop() {
        sally.enrollIn(comp127);
        sally.drop(comp127);
        assertEquals(Set.of(), sally.getCourses());
        assertEquals(Set.of(), comp127.getRoster());
    }

    @Test
    void dropHasNoEffectOnOtherCoursesOrStudents() {
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        sally.enrollIn(math6);
        sally.drop(comp127);
        assertEquals(Set.of(math6), sally.getCourses());
        assertEquals(Set.of(fred), comp127.getRoster());
    }

    @Test
    void dropRemovesFromWaitlist() {
        factory.enrollMultipleStudents(comp127, 16);
        sally.enrollIn(comp127);
        fred.enrollIn(comp127);
        zongo.enrollIn(comp127);
        fred.drop(comp127);
        assertEquals(List.of(sally, zongo), comp127.getWaitlist());
    }

    @Test
    void dropEnrollsWaitlistedStudents() {
        sally.enrollIn(comp127);
        factory.enrollMultipleStudents(comp127, 15);
        zongo.enrollIn(comp127);
        fred.enrollIn(comp127);
        sally.drop(comp127);
        assertTrue(comp127.getRoster().contains(zongo));
        assertEquals(List.of(fred), comp127.getWaitlist());
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
                c.getRoster().contains(s),
                s + " thinks they are enrolled in " + c
                    + ", but " + c + " does not have them in the list of students");
    }

    private void checkCourseInvariants(Course c) {
        Set<Student> waitlistUnique = new HashSet<>(c.getWaitlist());
        assertEquals(
            waitlistUnique.size(),
            c.getWaitlist().size(),
            c + " waitlist contains duplicates: " + c.getWaitlist());

        waitlistUnique.retainAll(c.getRoster());
        assertEquals(
            Set.of(),
            waitlistUnique,
            c + " contains students who are both registered and waitlisted");

        for (Student s : c.getRoster()) {
            assertTrue(
                s.getCourses().contains(c),
                c + " thinks " + s + " is enrolled, but " + s + " doesn't think they're in the class");
        }

        for (Student s : c.getWaitlist()) {
            assertFalse(
                s.getCourses().contains(c),
                c + " lists " + s + " as waitlisted, but " + s + " thinks they are enrolled");
        }

        assertTrue(
            c.getRoster().size() <= c.getEnrollmentLimit(),
            c + " has an enrollment limit of " + c.getEnrollmentLimit()
                + ", but has " + c.getRoster().size() + " students");

        if (c.getRoster().size() < c.getEnrollmentLimit()) {
            assertEquals(
                List.of(),
                c.getWaitlist(),
                c + " is not full, but has students waitlisted");
        }
    }
}
