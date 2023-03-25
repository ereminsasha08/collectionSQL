package com.digdes.school.service;

import com.digdes.school.exception.UncorrectedAttributeException;
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
        for (Map.Entry<String, Map<String, Object>> next : predicatesAnd.entrySet()) {
            String operatorRequest = next.getKey();
            BiFunction<Object, Object, Boolean> compareOperator = compareOperators.get(operatorRequest);
            Map<String, Object> entity = next.getValue();
            for (Map.Entry<String, Object> next1 : entity.entrySet()) {
                String keyRequest = next1.getKey();
                Object valueRequest = next1.getValue();
                if (isAnd) {
                    maps = filterByPredicate(maps, compareOperator, keyRequest, valueRequest);
                } else {
                    List<Map<String, Object>> maps1;
                    maps1 = filterByPredicate(maps, compareOperator, keyRequest, valueRequest);
                    result.addAll(maps1);
                }
            }
        }
        if (isAnd) {
            result.addAll(maps);
        }
        return result;
    }

    private static List<Map<String, Object>> filterByPredicate(List<Map<String, Object>> maps, BiFunction<Object, Object, Boolean> compareOperator, String keyRequest, Object valueRequest) {
        return maps.stream().filter(map -> applyCorrectPredicate(compareOperator, keyRequest, valueRequest, map)).toList();
    }

    private static Boolean applyCorrectPredicate(BiFunction<Object, Object, Boolean> compareOperator, String key, Object valueRequest, Map<String, Object> map) {
        String correctKey = attributeNameIgnoreCase(key, map);
        Object value = map.get(correctKey);
        if (value instanceof Boolean) {
            return compareOperator.apply(value, Boolean.valueOf((String) valueRequest));
        }
        if (value instanceof Double) {
            return compareOperator.apply(value, Double.valueOf((String) valueRequest));
        }
        if (value instanceof Long) {
            if (!valueRequest.toString().contains("."))
                return compareOperator.apply(value, Long.valueOf((String) valueRequest));
        }
        if (value instanceof String) {
            return compareOperator.apply(value, valueRequest);
        }
        return null;
    }

    private static String attributeNameIgnoreCase(String key, Map<String, Object> map) {
        Optional<String> first = map.keySet().stream().filter(k -> k.equalsIgnoreCase(key)).findFirst();
        if (first.isPresent())
            return first.get();
        throw new UncorrectedAttributeException(String.format("Некорректный атрибут %s", key));
    }
}




