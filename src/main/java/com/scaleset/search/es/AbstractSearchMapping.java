package com.scaleset.search.es;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.scaleset.geo.geojson.GeoJsonModule;
import com.scaleset.search.Query;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSearchMapping<T, K> implements SearchMapping<T, K> {

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new GeoJsonModule());
    private JavaType javaType;
    private String defaultIndex;
    private String defaultType;
    private JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    public AbstractSearchMapping(Class<? extends T> type, String defaultIndex, String defaultType) {
        this.javaType = objectMapper.getTypeFactory().constructType(type);
        this.defaultIndex = defaultIndex;
        this.defaultType = defaultType;
    }

    public AbstractSearchMapping(TypeReference typeReference, String defaultIndex, String defaultType) {
        this.javaType = objectMapper.getTypeFactory().constructType(typeReference);
        this.defaultIndex = defaultIndex;
        this.defaultType = defaultType;
    }

    public T fromDocument(String id, String doc) throws Exception {
        T obj = objectMapper.readValue(doc, javaType);
        return obj;
    }

    public T fromSearchHit(SearchHit searchHit) {
        ObjectNode json = nodeFactory.objectNode();
        for (SearchHitField field : searchHit.fields().values()) {
            putField(json, field);
        }
        T result = objectMapper.convertValue(json, javaType);
        return result;
    }

    void putField(ObjectNode json, SearchHitField field) {
        String fieldName = field.getName();
        String[] nameParts = fieldName.split("\\.");
        String property = nameParts[nameParts.length - 1];
        ObjectNode obj = json;
        for (int i = 0; i < nameParts.length - 1; ++i) {
            String part = nameParts[i];
            obj = obj.with(part);
        }
        List<Object> values = field.getValues();
        Object value = values.size() > 1 ? values : values.get(0);
        obj.put(property, nodeFactory.pojoNode(value));
    }

    @Override
    public abstract String id(T obj) throws Exception;

    @Override
    public abstract String idForKey(K key) throws Exception;

    @Override
    public String index(T object) throws Exception {
        return defaultIndex;
    }

    @Override
    public String indexForKey(K key) throws Exception {
        return defaultIndex;
    }

    @Override
    public String indexForQuery(Query query) throws Exception {
        return defaultIndex;
    }

    @Override
    public String toDocument(T obj) throws Exception {
        String result = objectMapper.writeValueAsString(obj);
        return result;
    }

    @Override
    public String type(T object) throws Exception {
        return defaultType;
    }

    @Override
    public String typeForKey(K key) throws Exception {
        return defaultType;
    }

    @Override
    public String typeForQuery(Query query) throws Exception {
        return defaultType;
    }
}
