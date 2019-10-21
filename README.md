# Registrar Simulation






----------------------------------------------------------------
Instructions
----------------------------------------------------------------




In this lab, we are practicing:

- **Object modeling**: representing concepts as object-oriented code.
- **The API concept through programming by contract**: writing code that satisfies logical constraints.
- **Defensive programming**: writing code that actively prevents other code from using it incorrectly.






Contract of the `Student` and `Course` API:

- Students know their registered courses, and courses know the list of students enrolled in them.
    > For all students and courses, `student.getCourses().contains(course)` if and only if `course.getStudents().contains(student)`.

- Courses can have a max enrollment limit on the number of students. 
    > For all courses, `course.getStudents().size()` ≤ `course.getEnrollmentLimit()`.

- Courses can have a waitlist when they go over their enrollment limit. When a student attempts to register for a course that is full, they automatically go on the waitlist. The `enroll()` method lets the caller know whether the enrollment was successful or the student was waitlisted.
    > Students appear in the waitlist in the order they attempted to register.
    
    > The waitlist never contains duplicates.
    
    > A student is never both enrolled in and wait listed for the same course.
    
    > If a course is not full, then its waitlist is empty.

- The enrollment limit cannot change if any students are already registered for the course. _(You will change this later.)_






## First task: Implement `drop()`

Add the ability for students to drop courses.

- A student can drop a class that they are enrolled in or in its waitlist
- If an enrolled student drops, then the first wait-listed student is automatically enrolled. (That’s not realistic, of course, but it makes for a better programming exercise! I guess the registrar at this college is just a little too eager.)
- Two things happen when a student drops a class (aside from the waitlist effect described above):
    - The course is no longer on the course list of that student
    - The student is no longer on the roster for the course
    - Hint:
        - public Student.drop() / internal Course.drop()
            - One calls the other
            - Don't make both public!
            - Parallel to how enroll works; study that
- Remember to test early and test often
    - What do you need to test to ensure proper working of this method? (i.e. What is the contract?) Discuss this with your partner briefly before proceeding.
    
    ...........................................................
    - We have already stated that two things happen when a student drops a course. Those seem like good candidates for testing to begin with.
        - If a student drops a course, what happens to the other courses they’re taking? 
        - If a student drops a course, what happens to the rosters of other courses they’re taking?
    - We also need to make sure that the waitlist process works right
        - If a waitlisted student drops that course, what happens to the other students in the waitlist for that course?
        - If an enrolled student drops that course, what happens to the waitlist (specifically, to the student at the top of the waitlist)?
        
- Finally, update Javadoc for getWaitList()
--- 




## Second task: Allow enrollment limit to change

You are going to replace the last item in the contract above with the following new rule:

- An existing enrollment limit now should be modifiable at eny time, regardless of whether students have already started registering.
    > (R1) The enrollment limit cannot change to be less than the number of students already registered.
    
    > (R2) If students are on the waitlist and the enrollment limit increases, the students automatically are enrolled (up to the new enrollment limit).
    
- This new contract requires us to make some changes in the setEnrollmentLimit()
    - To address (R1), you may use the following:
    `if (getStudents().size() > limit) {`
            `throw new IllegalArgumentException("cannot set limit below class size");`
`}`

    - Once you make this modification, test it 
    
    - To address (R2), you will need a way to to enroll students from the waitlist up to the new enrollment limit or until there are no students on the waitlist for that course, whichever happens first. 
    - Write a method `enrollFromWaitlist()` to implement this
    - Where do you need to call the `enrollFromWaitlist()` method? 





