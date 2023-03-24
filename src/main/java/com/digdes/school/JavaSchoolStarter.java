package com.digdes.school;

import com.digdes.school.model.Request;
import com.digdes.school.service.ExecuteRequest;
import com.digdes.school.service.ParserRequest;
import lombok.Getter;

import java.util.List;
import java.util.Map;
@Getter
public class JavaSchoolStarter {
    private ParserRequest parserRequest = new ParserRequest();
    private List<Map<String, Object>> maps;
    public JavaSchoolStarter() {
    }
    public JavaSchoolStarter(List<Map<String, Object>> maps) {
        this.maps = maps;
    }
    public List<Map<String, Object>> execute(String request) {
        Request requestParse = parserRequest.parse(request);
        List<Map<String, Object>> result = ExecuteRequest.executeRequest(requestParse, maps);
        return result;
    }

}
