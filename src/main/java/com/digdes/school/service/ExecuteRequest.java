package com.digdes.school.service;

import com.digdes.school.model.Operator;
import com.digdes.school.model.Request;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiFunction;

public class ExecuteRequest {

    private static Map<Operator, BiFunction<Request, List<Map<String, Object>>, List<Map<String, Object>>>> operatorExecute = new HashMap<>();

    static {
        operatorExecute.put(Operator.SELECT, ExecuteRequest::executeSelect);
        operatorExecute.put(Operator.DELETE, ExecuteRequest::executeDelete);
        operatorExecute.put(Operator.INSERT, ExecuteRequest::executeInsert);
        operatorExecute.put(Operator.UPDATE, ExecuteRequest::executeUpdate);
    }

    private static Map<String, BiFunction<Object, Object, Boolean>> compareOperators = new HashMap<>();

    static {
        compareOperators.put("=", Object::equals);
        compareOperators.put("!=", (o1, o2) -> !o1.equals(o2));
        compareOperators.put("<", (o1, o2) -> {
            BigDecimal o11 = NumberUtils.createBigDecimal(o1.toString());
            BigDecimal o22 = NumberUtils.createBigDecimal(o2.toString());
            return o11.compareTo(o22) < 0;
        });
        compareOperators.put(">", (o1, o2) -> {
            BigDecimal o11 = NumberUtils.createBigDecimal(o1.toString());
            BigDecimal o22 = NumberUtils.createBigDecimal(o2.toString());
            return o11.compareTo(o22) > 0;
        });
        compareOperators.put("<=", (o1, o2) -> {
            BigDecimal o11 = NumberUtils.createBigDecimal(o1.toString());
            BigDecimal o22 = NumberUtils.createBigDecimal(o2.toString());
            return o11.compareTo(o22) <= 0;
        });
        compareOperators.put(">=", (o1, o2) -> {
            BigDecimal o11 = NumberUtils.createBigDecimal(o1.toString());
            BigDecimal o22 = NumberUtils.createBigDecimal(o2.toString());
            return o11.compareTo(o22) >= 0;
        });
        compareOperators.put("like", (o1, o2) -> {
            String o11 = (String) o1;
            String o22 = (String) o2;
            return o11.matches(o22.replace("%", "(.*)"));
        });
        compareOperators.put("ilike", (o1, o2) -> {
            String o11 = (String) o1;
            String o22 = (String) o2;
            return o11.toLowerCase().matches(o22.replace("%", "(.*)"));
        });
    }

    public static List<Map<String, Object>> executeRequest(Request request, List<Map<String, Object>> maps) {
        return operatorExecute.get(request.getOperator()).apply(request, maps);

    }

    private static List<Map<String, Object>> executeUpdate(Request request, List<Map<String, Object>> maps) {
        List<Map<String, Object>> maps1 = executeWhere(request, maps);
        Iterator<Map<String, Object>> iterator = maps1.iterator();
        Set<Map.Entry<String, Object>> entries = request.getValues().entrySet();
        Iterator<Map.Entry<String, Object>> iterator1;
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            iterator1 = entries.iterator();
            while (iterator1.hasNext()) {
                Map.Entry<String, Object> next1 = iterator1.next();
                String key = next1.getKey();
                Object value = next1.getValue();
                next.replace(key, value);
            }
        }
        return maps1;
    }

    private static List<Map<String, Object>> executeInsert(Request request, List<Map<String, Object>> maps) {
        Map<String, Object> stringObjectMap = maps.get(0);
        Set<String> keys = stringObjectMap.keySet();
        Map<String, Object> values = request.getValues();
        Map<String, Object> saveEntity = new HashMap<>();
        for (String key :
                keys) {
            try {
                saveEntity.put(key, values.get(key));
            } catch (Exception e) {
                saveEntity.put(key, null);
            }
        }
        maps.add(saveEntity);
        ArrayList<Map<String, Object>> objects = new ArrayList<>();
        objects.add(saveEntity);
        return objects;
    }

    private static List<Map<String, Object>> executeDelete(Request request, List<Map<String, Object>> maps) {
        List<Map<String, Object>> deleted = executeWhere(request, maps);
        maps.removeAll(deleted);
        return deleted;
    }

    private static List<Map<String, Object>> executeSelect(Request request, List<Map<String, Object>> maps) {
        return executeWhere(request, maps);
    }


    private static List<Map<String, Object>> executeWhere(Request request, List<Map<String, Object>> maps) {

        Set<Map<String, Object>> result = new HashSet<>();
        List<Map<String, Object>> maps1 = new ArrayList<>(maps);
        Map<String, Map<String, Object>> predicatesOr = request.getPredicatesWithOr();
        if (predicatesOr.size() != 0)
            result.addAll(getExecuteWhereWithPredicated(maps1, predicatesOr, false));

        Map<String, Map<String, Object>> predicatesAnd = request.getPredicatesWithAnd();
        result.addAll(getExecuteWhereWithPredicated(maps1, predicatesAnd, true));

        return new ArrayList<>(result);
    }

    private static Set<Map<String, Object>> getExecuteWhereWithPredicated(List<Map<String, Object>> maps, Map<String, Map<String, Object>> predicatesAnd, Boolean isAnd) {
        Set<Map<String, Object>> result = new HashSet<>();
        List<Map<String, Object>> maps1;
        for (Map.Entry<String, Map<String, Object>> next : predicatesAnd.entrySet()) {
            String key = next.getKey();
            BiFunction<Object, Object, Boolean> objectObjectBooleanBiFunction = compareOperators.get(key);
            Map<String, Object> value = next.getValue();
            for (Map.Entry<String, Object> next1 : value.entrySet()) {
                String key1 = next1.getKey();
                Object value2 = next1.getValue();
                if (isAnd) {
                    maps = filterByPredicate(maps, objectObjectBooleanBiFunction, key1, value2);
                } else {
                    maps1 = filterByPredicate(maps, objectObjectBooleanBiFunction, key1, value2);
                    result.addAll(maps1);
                }
            }
        }
        if (isAnd) {
            result.addAll(maps);
        }
        return result;
    }

    private static List<Map<String, Object>> filterByPredicate(List<Map<String, Object>> maps, BiFunction<Object, Object, Boolean> objectObjectBooleanBiFunction, String key1, Object value2) {
        return maps.stream().filter(map -> typeCasting(objectObjectBooleanBiFunction, key1, value2, map)).toList();
    }

    private static boolean typeCasting(BiFunction<Object, Object, Boolean> objectObjectBooleanBiFunction, String key1, Object value2, Map<String, Object> map) {
        try {
            Boolean value1 = (Boolean) map.get(key1);
            return objectObjectBooleanBiFunction.apply(value1, Boolean.valueOf((String) value2));
        } catch (Exception e) {
        }
        try {
            Double value1 = (Double) map.get(key1);
            return objectObjectBooleanBiFunction.apply(value1, Double.valueOf((String) value2));
        } catch (Exception e2) {
        }
        try {
            Long value1 = (Long) map.get(key1);
            if (value2.toString().contains("."))
                throw new Exception();
            return objectObjectBooleanBiFunction.apply(value1, Long.valueOf((String) value2));
        } catch (Exception e1) {
        }

        try {

            String value1 = (String) map.get(key1);
            return objectObjectBooleanBiFunction.apply(value1, value2);
        } catch (Exception e3) {
            return false;
        }
    }
}




