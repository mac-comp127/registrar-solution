# Registrar Simulation






----------------------------------------------------------------
Sketch of instructions + useful fragments from  old instructions
----------------------------------------------------------------




In this lab, we are practicing:

- **Object modeling**: representing concepts as object-oriented code.
- **Programming by contract**: writing code that satisfies logical constraints.
- **Defensive programming**: writing code that actively prevents other code from using it incorrectly.






Contract of the `Student` and `Course` API:

- Students know their registered courses, and courses know the list of students enrolled in them.
    > For all students and courses, `student.getCourses().contains(course)` if and only if `course.getStudents().contains(student)`.

- Courses can have a max enrollment limit on the number of students. 
    > For all courses, `course.getStudents().size()` ≤ `course.getEnrollmentLimit()`.

- Courses can have a waitlist when they go over their enrollment limit. When a student attempts to register for a course that is full, they automatically go on the wait list. The `enroll()` method lets the caller know whether the enrollment was successful or the student was waitlisted.
    > Students appear in the waitlist in the order they attempted to register.
    > The waitlist never contains duplicates.
    > A student is never both enrolled in and wait listed for the same course.
    > If a course is not full, then its wait list is empty.

- The enrollment limit cannot change if any students are already registered for the course. _(You will change this later.)_






## First task: Implement `drop()`

Add the ability for students to drop courses.

- If an enrolled student drops, then the first wait-listed student is automatically enrolled. (That’s not realistic, of course, but it makes for a better programming exercise! I guess the registrar at this college is just a little too eager.)


---
Notes for Esra

Solution here: https://github.com/mac-comp127-master/127-registrar/commit/5eea742d03c0a4889a7cc5158bdd8d7598895b88

Hints I think they’ll need:
- public Student.drop() / internal Course.drop()
    - One calls the other
    - Don't make both public!
    - Parallel to how enroll works; study that
- What tests to add (let them think about details, but some light guidance on conditions to test for)
- Remember to update Javadoc for getWaitList()
--- 




## Second task: Allow enrollment limit to change

You are going to replace the last item in the contract above with the following new rule:

- An existing enrollment limit now should be modifiable at eny time, regardless of whether students have already started registering.
    > The enrollment limit cannot change to be less than the number of students already registered.
    > If students are on the waitlist and the enrollment limit increases, the students automatically are enrolled (up to the new enrollment limit).


---
Notes for Esra

Solution here: https://github.com/mac-comp127-master/127-registrar/commit/bb8a1e7c40b8bdf35b183c340219f1525284139d

Hints I think they’ll need:
- Change IllegalStateException to IllegalArgumentException (just give them that; it’s good pratice they should see, but we haven’t talked about it at all)
- Help testing for that exception (maybe just give them the code? again has stuff we haven’t talked about)
- **After** you get it working and tests passing, see if you can remove any duplicate logic in drop() and setEnrollmentLimit() related to automatically enrolling from the waitlist. (This is a nice “aha” moment that I'd like to let them have the chance to discover: the while(...) loop that enrolls until full or waitlist is empty can cover both methods.)
--- 
