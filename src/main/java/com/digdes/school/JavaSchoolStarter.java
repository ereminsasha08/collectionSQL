package com.digdes.school;

import com.digdes.school.model.Request;
import com.digdes.school.service.ExecuteRequest;
import com.digdes.school.service.ParserRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Getter
@NoArgsConstructor
public class JavaSchoolStarter {
    private ParserRequest parserRequest = new ParserRequest();
    private List<Map<String, Object>> maps = new ArrayList<>();
    public JavaSchoolStarter(List<Map<String, Object>> maps) {
        this.maps = maps;
    }
    public List<Map<String, Object>> execute(String request) {
        Request requestParse = parserRequest.parse(request);
        List<Map<String, Object>> result = ExecuteRequest.executeRequest(requestParse, maps);
        return result;
    }

}
