package com.digdes.school.service;

import com.digdes.school.model.Operator;
import com.digdes.school.model.Request;

import java.util.*;

public class ParserRequest {

    public Request parse(String execute) {
        Request request = new Request();

        String operator = parseOperatePart(execute);
        request.setOperator(Operator.valueOf(operator));

        List<String> predicates = parseWherePart(execute);
        saveAllPredicate(predicates, request);

        List<String> values = parseValuesPart(execute);
        saveAllValues(values, request);

        return request;

    }

    private static String parseOperatePart(String execute) {
        String[] executeArray = execute.split(" ");
        return executeArray[0];
    }

    private List<String> parseValuesPart(String execute) {
        int whereLastIndex = execute.toUpperCase().lastIndexOf("WHERE");
        int endIndexForValues = whereLastIndex == -1 ? execute.length() : whereLastIndex - 1;
        String requestOperator = parseOperatePart(execute);
        int beginIndex = execute.toUpperCase().indexOf(requestOperator) + requestOperator.length();
        String valuesPartExecute = execute.substring(beginIndex, endIndexForValues);
        List<String> values = new ArrayList<>();
        if (valuesPartExecute.toUpperCase().contains("VALUES")) {
            valuesPartExecute = valuesPartExecute.substring(valuesPartExecute.toUpperCase().indexOf("VALUES") + 7, valuesPartExecute.length());
            values = parseAllValues(valuesPartExecute);
        }
        return values;
    }

    private List<String> parseWherePart(String execute) {
        List<String> predicates = new ArrayList<>();
        if (execute.toUpperCase().contains("WHERE")) {
            String wherePartExecute = execute.substring(execute.toUpperCase().indexOf("WHERE") + 6, execute.length());
            predicates = parseAllPredicate(wherePartExecute);
        }
        return predicates;
    }

    private List<String> parseAllPredicate(String execute) {
        return parseExecute(execute);
    }

    private List<String> parseAllValues(String execute) {
        return parseExecute(execute);
    }

    private static List<String> parseExecute(String executeWherePart) {
        Queue<String> stack = new LinkedList<>();
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = executeWherePart.toCharArray();
        Queue<Character> characters = new LinkedList<>();
        for (char c :
                chars) {
            characters.add(c);
        }
        List<String> result = new ArrayList<>();
        while (!characters.isEmpty()) {
            Character nextCharacter = characters.peek();
            boolean isSpaceOrComma = Character.isSpaceChar(nextCharacter) || nextCharacter.equals(',');
            // Проверяет запятую и пробел, если они внутри одинарных ковычек стэк заполняется дальше, если нет стэк сохраняется, а символ удаляется
            if (isSpaceOrComma && !stack.isEmpty() && stack.stream().filter((a) -> a.equals("'")).mapToInt((a) -> 1).sum() == 1) {
                stack.offer(Objects.requireNonNull(characters.poll()).toString());
                continue;
            } else if (isSpaceOrComma) {
                saveStackInResult(stack, stringBuilder, result);
                characters.poll();
                continue;
            }
            // Добовляет символ в стэк, если он относится к допустимым для атрибутов и значений
            if (nextCharacter.equals('\'') || nextCharacter.equals('%') || nextCharacter.equals('.')
                    || Character.isLetterOrDigit(nextCharacter)) {
                stack.offer(Objects.requireNonNull(characters.poll()).toString());
                continue;
            }
            // Добавляет в стэк символы сравнения и сохраняет их в результат
            if (stack.isEmpty() && !Character.isLetterOrDigit(nextCharacter) && !nextCharacter.equals('\'')) {
                while (!Character.isLetterOrDigit(nextCharacter) && !nextCharacter.equals('\'')) {
                    stack.offer(Objects.requireNonNull(characters.poll()).toString());
                    nextCharacter = characters.peek();
                }
            }
            saveStackInResult(stack, stringBuilder, result);
        }
        saveStackInResult(stack, stringBuilder, result);
        return result;
    }

    private static void saveStackInResult(Queue<String> stack, StringBuilder stringBuilder, List<String> result) {
        while (!stack.isEmpty()) {
            stringBuilder.append(stack.poll());
        }
        String token = stringBuilder.toString().trim();
        if (token.length() >= 1) {
            result.add(token);
            stringBuilder.setLength(0);
        }
    }

    private void saveAllValues(List<String> result, Request request) {
        Map<String, Object> attributeValues = new HashMap<>();
        Iterator<String> iteratorValues = result.iterator();
        while (iteratorValues.hasNext()) {
            String attribute = iteratorValues.next().replace("'", "");
            iteratorValues.next();
            String value = iteratorValues.next().replace("'", "");

            attributeValues.put(attribute, typeCasting(value));
        }
        request.setValues(attributeValues);
    }

    private static Object typeCasting(Object value2) {
        if ("null".equals(value2))
            return null;
        if ("true".equals(value2))
            return true;
        else if ("false".equals(value2))
            return false;
        try {
            String value1 = value2.toString();
            if (value1.contains("."))
                return Double.valueOf((String) value2);
        } catch (Exception e2) {
        }
        try {
            return Long.valueOf((String) value2);
        } catch (Exception e1) {
        }

        return value2;
    }


    private void saveAllPredicate(List<String> predicates, Request request) {
        Map<String, Map<String, Object>> operatorAndAttributeValueWithAnd = new HashMap<>();
        Map<String, Map<String, Object>> operatorAndAttributeValueWithOr = new HashMap<>();
        Iterator<String> iteratorPredicates = predicates.iterator();
        while (iteratorPredicates.hasNext()) {
            Map<String, Object> attributeValue = new HashMap<>();
            String logicOperator = iteratorPredicates.next();
            String attribute;
            if (logicOperator.equalsIgnoreCase("or") || logicOperator.equalsIgnoreCase("and")) {
                attribute = iteratorPredicates.next();
            } else {
                attribute = logicOperator;
            }
            attribute = attribute.replace("'", "");
            String operator = iteratorPredicates.next();
            String value = iteratorPredicates.next().replace("'", "");
            attributeValue.put(attribute, value);

            if (logicOperator.equalsIgnoreCase("or")) {
                Map<String, Object> stringObjectMapWithOr = operatorAndAttributeValueWithOr.get(operator);
                if (stringObjectMapWithOr != null) {
                    stringObjectMapWithOr.put(attribute, value);

                } else {
                    operatorAndAttributeValueWithOr.put(operator, attributeValue);
                }
            } else {
                Map<String, Object> stringObjectMapWithAnd = operatorAndAttributeValueWithAnd.get(operator);
                if (stringObjectMapWithAnd != null) {
                    stringObjectMapWithAnd.put(attribute, value);

                } else {
                    operatorAndAttributeValueWithAnd.put(operator, attributeValue);
                }
            }
            request.setPredicatesWithAnd(operatorAndAttributeValueWithAnd);
            request.setPredicatesWithOr(operatorAndAttributeValueWithOr);
        }
    }
}
