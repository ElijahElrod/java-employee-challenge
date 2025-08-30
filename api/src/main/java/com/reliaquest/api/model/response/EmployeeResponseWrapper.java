package com.reliaquest.api.model.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EmployeeResponseWrapper represents the response payload for a create and read employee operation.
 * <p>
 * Contains:
 * <ul>
 *     <li><b>data</b>: List of employee details (regardless of single or multiple)</li>
 *     <li><b>status</b>: Status message of the operation</li>
 * </ul>
 * <p>
 */
public record EmployeeResponseWrapper(
        @JsonDeserialize(using = SingleOrListDeserializer.class) List<EmployeeResponse> data, String status) {

    static class SingleOrListDeserializer extends JsonDeserializer<List<EmployeeResponse>> {
        @Override
        public List<EmployeeResponse> deserialize(final JsonParser p, final DeserializationContext ctx)
                throws IOException, JsonProcessingException {
            final var mapper = (ObjectMapper) p.getCodec();
            final JsonNode node = mapper.readTree(p);
            final List<EmployeeResponse> list = new ArrayList<>();

            if (node.isArray()) {
                for (JsonNode item : node) {
                    list.add(mapper.treeToValue(item, EmployeeResponse.class));
                }
            } else if (node.isObject()) {
                list.add(mapper.treeToValue(node, EmployeeResponse.class));
            }

            return list;
        }
    }
}
