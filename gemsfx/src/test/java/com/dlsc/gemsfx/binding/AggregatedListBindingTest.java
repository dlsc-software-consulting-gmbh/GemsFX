package com.dlsc.gemsfx.binding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.stream.Stream;

import static org.junit.Assert.*;


public class AggregatedListBindingTest {

    private ObservableList<Classroom> classrooms;
    private AggregatedListBinding<Classroom, Student, Long> studentCountBinding;

    @Before
    public void setUp() {
        classrooms = FXCollections.observableArrayList();
        studentCountBinding = new AggregatedListBinding<>(
                classrooms,
                // Classroom::getStudents,
                classroom -> classroom == null ? null : classroom.getStudents(),
                Stream::count
        );
    }

    @After
    public void tearDown() {
        studentCountBinding.dispose();
    }

    @Test
    public void testInitialValue() {
        // Initial value should be 0 if no classrooms are present.
        assertEquals(0, studentCountBinding.get().intValue());
    }

    @Test
    public void testAddingClassrooms() {
        Classroom class1 = new Classroom();
        class1.getStudents().addAll(new Student(), new Student());

        Classroom class2 = new Classroom();
        class2.getStudents().addAll(new Student(), new Student(), new Student());

        // Outers list added classes.
        classrooms.addAll(class1, class2);
        assertEquals(5, studentCountBinding.get().intValue());

        Classroom class3 = new Classroom();
        class3.getStudents().addAll(new Student());

        // Outers list added class.
        classrooms.add(class3);
        assertEquals(6, studentCountBinding.get().intValue());

        // Inner list added students.
        class3.getStudents().addAll(new Student(), new Student());
        assertEquals(8, studentCountBinding.get().intValue());

        // Outer list added null.
        classrooms.add(null);
        assertEquals(8, studentCountBinding.get().intValue());
    }

    @Test
    public void testRemovingClassrooms() {
        Classroom class1 = new Classroom();
        class1.getStudents().addAll(new Student(), new Student());

        Classroom class2 = new Classroom();
        class2.getStudents().addAll(new Student(), new Student(), new Student());

        classrooms.addAll(class1, class2);
        assertEquals(5, studentCountBinding.get().intValue());

        // Outers list removed class.
        classrooms.remove(class1);
        assertEquals(3, studentCountBinding.get().intValue());

        // Inner list removed student.
        class2.getStudents().remove(0);
        assertEquals(2, studentCountBinding.get().intValue());
    }

    @Test
    public void testModifyingStudentListInClassroom() {
        Classroom class1 = new Classroom();
        ObservableList<Student> students = class1.getStudents();
        students.addAll(new Student(), new Student());

        classrooms.add(class1);
        students.add(new Student());

        assertEquals(3, studentCountBinding.get().intValue());

        students.remove(0);
        assertEquals(2, studentCountBinding.get().intValue());

        Classroom class2 = new Classroom();
        class2.getStudents().addAll(new Student(), new Student());
        classrooms.add(class2);

        assertEquals(4, studentCountBinding.get().intValue());

        Classroom class3 = new Classroom();
        class3.getStudents().addAll(new Student());
        classrooms.set(1, class3);
        assertEquals(3, studentCountBinding.get().intValue());

        // Inner list replaced with new elements.
        class3.getStudents().setAll(new Student(), new Student(), new Student());
        assertEquals(5, studentCountBinding.get().intValue());

        // Outer list replaced first element with null.
        classrooms.set(0, null);
        assertEquals(3, studentCountBinding.get().intValue());
    }

    private static class Classroom {

        private final ObservableList<Student> students = FXCollections.observableArrayList();

        public ObservableList<Student> getStudents() {
            return students;
        }
    }

    private static class Student {
    }

}
