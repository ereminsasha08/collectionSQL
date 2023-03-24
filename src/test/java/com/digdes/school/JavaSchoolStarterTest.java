package com.digdes.school;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class JavaSchoolStarterTest {
    private static List<Map<String, Object>> maps;
    private static Map<String, Object> row1;

    private static Map<String, Object> row2;

    private static Map<String, Object> row3;

    private static Map<String, Object> row4;

    private static List<Map<String, Object>> expected;
    private JavaSchoolStarter starter = new JavaSchoolStarter(maps);

    @BeforeEach
    public void updateData() {
        row1 = new HashMap<>();

        row1.put("id", 1L);
        row1.put("lastName", "Петров");
        row1.put("age", 30L);
        row1.put("cost", 5.4);
        row1.put("active", true);

        row2 = new HashMap<>();

        row2.put("id", 2L);
        row2.put("lastName", "Иванов");
        row2.put("age", 25L);
        row2.put("cost", 4.3);
        row2.put("active", false);

        row3 = new HashMap<>();

        row3.put("id", 3L);
        row3.put("lastName", "Семен");
        row3.put("age", 30L);
        row3.put("cost", 4.3);
        row3.put("active", false);


        row4 = new HashMap<>();

        row4.put("id", 4L);
        row4.put("lastName", "Федоров");
        row4.put("age", 40L);
        row4.put("cost", null);
        row4.put("active", true);

        maps = new ArrayList<>();
        maps.add(row1);
        maps.add(row2);
        maps.add(row3);
        maps.add(row4);

        expected = new ArrayList<>();
        this.starter = new JavaSchoolStarter(maps);
    }

    //results.addAll(starter.execute("SELECT"));
    @Test
    public void selectWithWhere_Correct() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'id'!= 1");
        assertThat(actual, hasSize(3));
    }
    @Test
    public void selectWithWhere_Incorrect() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'id'!= 1.0");
        assertThat(actual, hasSize(0));
    }
    @Test
    public void selectWithWhere_Like() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'lastName' like '%ров'");
        expected.add(row1);
        expected.add(row4);
        assertThat(actual, samePropertyValuesAs(expected));
    }
    @Test
    public void selectWithWhere_and_or_and_FindThree() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'age' = 40 and 'lastName'='Федоров' or 'id' = 1 and 'cost'= null");
        expected.add(row1);
        expected.add(row4);
        assertThat(actual, samePropertyValuesAs(expected));
    }

    @Test
    public void selectWithWhere_and_or_or_FindThree() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'age' = 30 and 'lastName'='Семен' or 'id' = 1 or 'age'= 25");
        expected.add(row1);
        expected.add(row2);
        expected.add(row3);
        assertThat(actual, samePropertyValuesAs(expected));
    }

    @Test
    public void selectWithWhere_and_or_FindThree() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'age' = 30 and 'lastName'='Семен' or 'id' > 1");
        assertThat(actual, hasSize(3));
    }

    @Test
    public void selectWithWhere_and_FindOne() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'age' = 30 and 'lastName'='Семен'");
        assertThat(actual, hasSize(1));
    }

    @Test
    public void selectWithWhere_and_WithoutFind() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'id' = 1 and 'lastName'='Иванов'");
        assertThat(actual, hasSize(0));
    }

    @Test
    public void selectWithWhere_or() {
        List<Map<String, Object>> actual = starter.execute("SELECT where 'id' = 1 or 'lastName'='Иванов'");
        assertThat(actual, hasSize(2));
    }

    @Test
    public void selectWithoutWhere() {
        List<Map<String, Object>> actual = starter.execute("SELECT");
        assertThat(actual, hasSize(4));
    }

    @Test
    public void deleteWithWhere() {
        List<Map<String, Object>> actual = starter.execute("DELETE where 'id' = 1 or 'lastName'='Иванов'");
        assertThat(actual, hasSize(2));
        assertThat(starter.getMaps(), hasSize(2));
    }

    @Test
    public void deleteWithoutWhere() {
        List<Map<String, Object>> actual = starter.execute("DELETE");
        assertThat(actual, hasSize(4));
        assertThat(starter.getMaps(), hasSize(0));
    }

    @Test
    public void insertValues() {
        List<Map<String, Object>> actual = starter.execute("INSERT VALUES 'lastName' = 'Федоров' , 'id'=4, 'age'=40, 'active'=true");
        expected.add(row4);
        assertThat(expected, samePropertyValuesAs(actual));
    }

    @Test
    public void updateWithoutWhere() {
        List<Map<String, Object>> actual = starter.execute("UPDATE VALUES 'active'=true, 'cost'=10.1");
        for (Map<String, Object> map :
                actual) {
            assertThat(map, hasEntry("active", true));
            assertThat(map, hasEntry("cost", 10.1));

        }
        assertThat(actual, hasSize(4));
    }

    @Test
    public void updateWithWhere() {
        List<Map<String, Object>> actual = starter.execute("UPDATE VALUES 'active'=true, 'cost'=10.1 where 'id'=1");
        for (Map<String, Object> map :
                actual) {
            assertThat(map, hasEntry("id", 1L));
            assertThat(map, hasEntry("lastName", "Петров"));
            assertThat(map, hasEntry("active", true));
            assertThat(map, hasEntry("cost", 10.1));
            assertThat(map, hasEntry("age", 30L));
        }
        assertThat(actual, hasSize(1));
    }

}
