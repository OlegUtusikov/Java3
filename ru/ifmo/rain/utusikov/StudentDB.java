package ru.ifmo.rain.utusikov;
import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery {
    private final Student Bro = new Student(0, "", "", "");

    private <T extends Collection<String>> T mapping(List<Student> students, Function<Student, String> selector, Supplier<T> collection) {
        return students
                .stream()
                .map(selector)
                .collect(Collectors.toCollection(collection));
    }
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapping(students, Student::getFirstName, ArrayList::new);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapping(students, Student::getLastName, ArrayList::new);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapping(students, Student::getGroup, ArrayList::new);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapping(students, student -> student.getFirstName().concat(" ").concat(student.getLastName()), ArrayList::new);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mapping(students, Student::getFirstName, TreeSet::new);
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).orElse(Bro).getFirstName();
    }

    private List<Student> sort(Stream<Student> students, Comparator<Student> cmp) {
        return students.sorted(cmp).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sort(students.stream(), Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sort(students.stream(), Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName).
                thenComparing(Student::getId));
    }

    private Stream<Student> filter(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream().filter(predicate);
    }

    private List<Student> findStudentsBy(Collection<Student> students, Predicate<Student> predicate) {
        return sortStudentsByName(filter(students, predicate).collect(Collectors.toList()));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsBy(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return filter(students, student -> group.equals(student.getGroup()))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    private Stream<Entry<String, List<Student>>> getGroupsStream(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList())).entrySet().stream();
    }


    private List<Group> getGroupBy(Collection<Student> students, Function<Entry<String, List<Student>>, Group> mapFunction) {
        return getGroupsStream(students)
                .map(mapFunction)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupBy(students, (e -> new Group(e.getKey(), sortStudentsByName(e.getValue()))));
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupBy(students, (e -> new Group(e.getKey(), sortStudentsById(e.getValue()))));
    }

    private String getLargest(Collection<Student> students, Comparator<Entry<String, List<Student>>> cmp) {
        return getGroupsStream(students)
                .max(cmp.thenComparing(Entry::getKey, Collections.reverseOrder(String::compareTo)))
                .map(Entry::getKey).orElse("");
    }
    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargest(students, Comparator.comparingInt((Entry<String, List<Student>> group) -> group.getValue().size()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargest(students, Comparator.comparingInt((Entry<String, List<Student>> group) -> getDistinctFirstNames(group.getValue()).size()));
    }
}