# Registrar Simulation

In this lab, we are practicing:

- **Object modeling**: representing concepts as object-oriented code.
- **Thinking about API contracts**: writing code that satisfies logical constraints.
- **Defensive programming**: writing code that actively prevents other code from using it incorrectly.

We give you objects used by a hypothetical college registrar system to track which students are enrolled in which courses. We also give you the API contract of these objects. Your job is to add two new features while preserving the API contract.


## Contract of the `Student` and `Course` API

- Students know their registered courses, and courses know the list of students enrolled in them.
    > For all students and courses, `student.getCourses().contains(course)` if and only if `course.getRoster().contains(student)`.

- Courses can have a max enrollment limit on the number of students. 
    > For all courses, `course.getRoster().size()` ≤ `course.getEnrollmentLimit()`.

- Courses can have a waitlist when they go over their enrollment limit. When a student attempts to register for a course that is full, they automatically go on the waitlist. The `enroll()` method lets the caller know whether the enrollment was successful or the student was waitlisted.
    > Students appear in the waitlist in the order they attempted to register.
    
    > The waitlist never contains duplicates.
    
    > A student is never both enrolled in and wait listed for the same course.
    
    > If a course is not full, then its waitlist is empty.

- The enrollment limit cannot change if any students are already registered for the course. _(You will change this later.)_

We have implemented tests that verify that the existing code satisfies this contract. Run `RegistrarTest`. All the tests should pass.

Spend a little time understanding the structure of `RegistrarTest`. Note all the specific cases it tests.


## First task: Implement `drop()`

Add the ability for students to drop courses.

- A student can ask to drop any class at any time: they can be enrolled, waitlisted, or not in the class at all.
- If an enrolled student drops, then the first wait-listed student is automatically enrolled. (That’s not realistic, of course, but it makes for a better programming exercise! I guess the registrar at this college is just a little too eager.)
- After a student drops a class:
    - The course is no longer on the course list of that student.
    - The student is no longer on the roster for the course.
    - The student is no longer on the waitlist for the course.
    - Hint:
        - Study the `public Student.enroll()` and internal `Course.enroll()` methods.
        - One calls the other. Which calls which?
        - Note that they are not both public! Why not? 
        - Create a parallel method structure for dropping courses. Remember: don’t make both methods public!
- Remember to test early and test often
    - What do you need to test to ensure proper functioning of this method? (i.e. What is its contract?) Discuss with your neighbor briefly before proceeding.
    - We have already stated that two things happen when a student drops a course. Those seem like good candidates for testing to begin with.
        - If a student drops a course, what happens to the other courses they’re taking? 
        - If a student drops a course, what happens to the rosters of other courses they’re taking?
    - We also need to make sure that the waitlist process works right
        - If a waitlisted student drops that course, what happens to the other students in the waitlist for that course?
        - If an enrolled student drops that course, what happens to the waitlist (specifically, to the student at the top of the waitlist)?
- Finally, update Javadoc for `getWaitList()` to describe how waitlisted students are automatically added.
- Commit your work so far with git.


## Second task: Allow enrollment limit to change

Replace the last item in the contract above with the following new rule:

- An existing enrollment limit now should be modifiable at eny time, regardless of whether students have already started registering.
    > (R1) The enrollment limit cannot change to be less than the number of students already registered.
    
    > (R2) If students are on the waitlist and the enrollment limit increases, the students automatically are enrolled (up to the new enrollment limit).
    
- This new contract requires us to make some changes in `setEnrollmentLimit()`.
    - To address (R1), you may use the following code:
        ```java
        if (getRoster().size() > limit) {
            throw new IllegalArgumentException("cannot set limit below class size");
        }
        ```
    - Once you make this modification, remove any old test(s) that no longer apply, and add a test that makes sure that your new modification works.
        - Hint: You can use the following code to test that an error happens when it is supposed to:
        ```java
        assertThrows(IllegalArgumentException.class, () -> {
            // line of code that is supposed to make the error happen
        });
        ```
        - Hint: Make sure the test fails if you don’t have the check, then passes when you do.
    - To address (R2), you will need a way to to enroll students from the waitlist either (a) up to the new enrollment limit or (b) until there are no students on the waitlist for that course, whichever happens first. Write a method `enrollFromWaitlist()` to implement this. Where do you need to call the `enrollFromWaitlist()` method?
    - What tests do you need to add to make sure things work? Are there any tricky cases where your logic might break, and that your tests should thus cover?
    - After you have that tested and working: Did you notice yourself duplicating any logic to enroll students from the waitlist? Is there a way to put that logic in a shared helper method so it isn't duplicated?

- Update the Javadoc for `getWaitList()` again to make sure it describes how everything behaves.
- Look over `RegistrarTest`. Are there any additional conditions you should test for? Imagine bugs the code could have. For each of those bugs, is there a test that would fail?
