package com.digdes.school.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
@Data
public class Request {

    Operator operator;
    Map<String, Map<String,Object>> predicatesWithAnd = new LinkedHashMap<>();
    Map<String, Map<String,Object>> predicatesWithOr = new LinkedHashMap<>();
    Map<String,Object> values = new HashMap<>();

}
