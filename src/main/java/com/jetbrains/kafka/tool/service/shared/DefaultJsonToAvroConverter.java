package com.jetbrains.kafka.tool.service.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.jetbrains.kafka.tool.exception.KafkaToolProducerException;
import com.jetbrains.kafka.tool.produce.avro.shared.util.Opt;
import com.jetbrains.kafka.tool.produce.avro.shared.util.StreamUtils;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Parser;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

public class DefaultJsonToAvroConverter implements JsonToAvroConverter, AvrifyConverter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public DefaultJsonToAvroConverter() {
    }

    public Object convert(String json, String schemaStr) {
        json = this.avrify(json, schemaStr);
        InputStream input = new ByteArrayInputStream(json.getBytes());
        DataInputStream din = new DataInputStream(input);
        Schema schema = (new Parser()).parse(schemaStr);

        try {
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, din);
            DatumReader<Object> reader = new GenericDatumReader(schema);
            Object datum = reader.read((Object)null, decoder);
            return datum;
        } catch (IOException var9) {
            throw new KafkaToolProducerException(var9);
        }
    }

    public String avrify(String rawJson, String rawSchema) {
        String converted = rawJson;

        try {
            JsonNode json = mapper.readTree(rawJson);
            if (json.isObject()) {
                Schema schema = (new Parser()).parse(rawSchema);
                converted = (String)Opt.of(this.avrify(json, schema)).map(jsonNode -> jsonNode.toString()).orElse(rawJson);
            }

            return converted;
        } catch (IOException var6) {
            throw new KafkaToolProducerException(var6);
        }
    }

    private JsonNode avrify(JsonNode message, Schema schema) {
        if (message == null) {
            return null;
        } else {
            Schema arrSchema;
            ObjectNode oMessage;
            if ((message).isObject() && this.isRecord(schema, true)) {
                arrSchema = this.isUnion(schema) ? this.getRecordSchema(message, schema) : schema;
                oMessage = (ObjectNode)message;
                Map<String, Field> fieldMap = this.getFieldMap(arrSchema);
                (message).fields().forEachRemaining((e) -> {
                    Field field = (Field)fieldMap.get(e.getKey());
                    Schema fs = field.schema();
                    JsonNode newField = this.avrify(e.getValue(), fs);
                    oMessage.replace((String)e.getKey(), newField);
                });
            } else if ((message).isObject() && this.isMap(schema, true)) {
                arrSchema = this.isUnion(schema) ? this.getMapSchema(message, schema) : schema;
                oMessage = (ObjectNode)message;
                Schema valueType = arrSchema.getValueType();
                (message).fields().forEachRemaining((e) -> {
                    JsonNode newField = this.avrify(e.getValue(), valueType);
                    oMessage.replace((String)e.getKey(), newField);
                });
            } else if ((message).isArray() && this.isArray(schema, true)) {
                arrSchema = this.isUnion(schema) ? this.getArraySchema(message, schema) : schema;
                arrSchema = arrSchema.getElementType();
                List<Schema> validSchemas = this.isUnion(arrSchema) ? arrSchema.getTypes() : Lists.newArrayList(new Schema[]{arrSchema});
                ArrayNode tempArrNode = mapper.createArrayNode();
                (message).elements().forEachRemaining((ele) -> {
                    Schema fs = this.guessSchema(ele, validSchemas);
                    JsonNode newField = this.avrify(ele, fs);
                    tempArrNode.add(newField);
                });
                message = tempArrNode;
            }

            if (this.isUnion(schema)) {
                message = this.convertUnion(message, schema);
            }

            return message;
        }
    }

    private JsonNode convertUnion(JsonNode message, Schema schema) {
        Set<Type> unionTypes = (Set)schema.getTypes().stream().map(Schema::getType).collect(Collectors.toSet());
        ObjectNode mappedObject = mapper.createObjectNode();
        JsonNode mapped = mappedObject;
        switch(message.getNodeType()) {
            case BOOLEAN:
                if (unionTypes.contains(Type.BOOLEAN)) {
                    mappedObject.put("boolean", message.asBoolean());
                }
                break;
            case STRING:
                if (unionTypes.contains(Type.ENUM)) {
                    String val = message.asText();
                    List<Schema> validEnumSchemas = (List)schema.getTypes().stream().filter((s) -> {
                        return this.isEnum(s, false);
                    }).filter((s) -> {
                        return s.getEnumSymbols().contains(val);
                    }).collect(Collectors.toList());
                    switch(validEnumSchemas.size()) {
                        case 0:
                        default:
                            return mapped;
                        case 1:
                            mappedObject.replace(((Schema)validEnumSchemas.get(0)).getFullName(), message);
                    }
                } else if (unionTypes.contains(Type.STRING)) {
                    mappedObject.put("string", message.asText());
                }
                break;
            case NUMBER:
                if (message.isInt() && unionTypes.contains(Type.INT)) {
                    mappedObject.put("int", message.asInt());
                } else if ((message.isInt() || message.isLong()) && unionTypes.contains(Type.LONG)) {
                    mappedObject.put("long", message.asLong());
                } else if (unionTypes.contains(Type.DOUBLE) || unionTypes.contains(Type.FLOAT)) {
                    mappedObject.put("float", message.asDouble());
                }
                break;
            case OBJECT:
                if (unionTypes.contains(Type.RECORD)) {
                    Opt.of(this.getRecordSchema(message, schema)).ifPresent((recSchema) -> {
                        mappedObject.replace(recSchema.toString(), message);
                    });
                }

                if (unionTypes.contains(Type.MAP)) {
                    mappedObject.replace("map", message);
                }
            case POJO:
            case BINARY:
            case MISSING:
            default:
                break;
            case ARRAY:
                if (unionTypes.contains(Type.ARRAY)) {
                    mappedObject.replace("array", message);
                }
                break;
            case NULL:
                if (unionTypes.contains(Type.NULL)) {
                    mapped = null;
                }
        }

        return mapped;
    }

    private Map<String, Field> getFieldMap(Schema schema) {
        return (Map)schema.getFields().stream().collect(Collectors.toMap(Field::name, (f) -> {
            return f;
        }));
    }

    private boolean isUnion(Schema schema) {
        return schema.getType().equals(Type.UNION);
    }

    private boolean isRecord(Schema schema, boolean allowUnion) {
        return this.isType(schema, allowUnion, Type.RECORD);
    }

    private boolean isMap(Schema schema, boolean allowUnion) {
        return this.isType(schema, allowUnion, Type.MAP);
    }

    private boolean isNullable(Schema schema, boolean allowUnion) {
        return this.isType(schema, allowUnion, Type.NULL);
    }

    private boolean isEnum(Schema schema, boolean allowUnion) {
        return this.isType(schema, allowUnion, Type.ENUM);
    }

    private boolean isArray(Schema schema, boolean allowUnion) {
        return this.isType(schema, allowUnion, Type.ARRAY);
    }

    private boolean isType(Schema schema, boolean allowUnion, Type type) {
        return this.isUnion(schema) && allowUnion ? ((List)schema.getTypes().stream().map(Schema::getType).collect(Collectors.toList())).contains(type) : schema.getType().equals(type);
    }

    private Schema getRecordSchema(JsonNode message, Schema unionSchema) {
        Schema recSchema = null;
        List<Schema> records = unionSchema.getTypes().stream().filter((t) -> {
            return this.isRecord(t, false);
        }).collect(Collectors.toList());
        if (records.size() == 1) {
            recSchema = (Schema)records.get(0);
        } else {
            List<Schema> matchedSchemas = records.stream().filter((r) -> {
                return this.checkSchemaMatch(message, r);
            }).collect(Collectors.toList());
            recSchema = this.getSingleFromList(matchedSchemas);
        }

        return recSchema;
    }

    private Schema getMapSchema(JsonNode message, Schema unionSchema) {
        List<Schema> maps = unionSchema.getTypes().stream().filter((t) -> {
            return this.isMap(t, false);
        }).collect(Collectors.toList());
        return this.getSingleFromList(maps);
    }

    private Schema getArraySchema(JsonNode message, Schema unionSchema) {
        List<Schema> arraySchemas = unionSchema.getTypes().stream().filter((t) -> {
            return this.isArray(t, false);
        }).collect(Collectors.toList());
        return this.getSingleFromList(arraySchemas);
    }

    private Schema getSingleFromList(List<Schema> schemas) {
        switch(schemas.size()) {
            case 0:
                throw new KafkaToolProducerException("Could not find any matching schemas");
            case 1:
                return (Schema)schemas.get(0);
            default:
                throw new KafkaToolProducerException("Found multiple matching schemas");
        }
    }

    private Schema guessSchema(JsonNode message, List<Schema> validSchemas) {
        Map<Type, List<Schema>> typeMap = validSchemas.stream().collect(Collectors.groupingBy(Schema::getType));
        Schema bestGuess = null;
        List mapSchemas;
        switch(message.getNodeType()) {
            case BOOLEAN:
                if (typeMap.containsKey(Type.BOOLEAN)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.BOOLEAN)).get(0);
                }
                break;
            case STRING:
                if (typeMap.containsKey(Type.BOOLEAN)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.BOOLEAN)).get(0);
                }

                if (bestGuess == null && typeMap.containsKey(Type.ENUM)) {
                    mapSchemas = this.guessEnumType(message.asText(), (List)typeMap.get(Type.ENUM));
                    switch(mapSchemas.size()) {
                        case 0:
                        default:
                            break;
                        case 1:
                            bestGuess = (Schema)mapSchemas.get(0);
                    }
                }

                if (bestGuess == null && typeMap.containsKey(Type.STRING)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.STRING)).get(0);
                }
                break;
            case NUMBER:
                if (message.isInt() && typeMap.containsKey(Type.INT)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.INT)).get(0);
                } else if (message.isLong() && typeMap.containsKey(Type.LONG)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.LONG)).get(0);
                } else if (message.isDouble() && typeMap.containsKey(Type.DOUBLE)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.DOUBLE)).get(0);
                } else if (message.isFloat() && typeMap.containsKey(Type.FLOAT)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.FLOAT)).get(0);
                }
                break;
            case OBJECT:
                if (typeMap.containsKey(Type.RECORD)) {
                    mapSchemas = (typeMap.get(Type.RECORD)).stream().filter((s) -> {
                        return this.checkSchemaMatch(message, s);
                    }).collect(Collectors.toList());
                    switch(mapSchemas.size()) {
                        case 0:
                            throw new KafkaToolProducerException("Could not find matching schema");
                        case 1:
                            bestGuess = (Schema)mapSchemas.get(0);
                            break;
                        default:
                            throw new KafkaToolProducerException("Found multiple matching schemas");
                    }
                } else if (typeMap.containsKey(Type.MAP)) {
                    mapSchemas = typeMap.get(Type.MAP);
                    if (mapSchemas.size() != 1) {
                        throw new KafkaToolProducerException("Found multiple matching schemas");
                    }

                    bestGuess = (Schema)mapSchemas.get(0);
                }
            case POJO:
            case BINARY:
            case MISSING:
            default:
                break;
            case ARRAY:
                if (typeMap.containsKey(Type.ARRAY)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.ARRAY)).get(0);
                }
                break;
            case NULL:
                if (typeMap.containsKey(Type.NULL)) {
                    bestGuess = (Schema)((List)typeMap.get(Type.NULL)).get(0);
                }
        }

        return bestGuess;
    }

    private List<Schema> guessEnumType(String val, List<Schema> validEnumSchemas) {
        return validEnumSchemas.stream().filter((s) -> {
            return this.isEnum(s, false);
        }).filter((s) -> {
            return s.getEnumSymbols().contains(val);
        }).collect(Collectors.toList());
    }

    private boolean checkSchemaMatch(JsonNode message, Schema recordSchema) {
        List<Field> fields = recordSchema.getFields();
        Set<String> schemaFieldNameSet = (Set)fields.stream().map(Field::name).collect(Collectors.toSet());
        long extraSchemaFields = fields.stream().filter((f) -> {
            return !this.fieldIsValid(message, f);
        }).count();
        long extraMessageFields = StreamUtils.asStream(message.fieldNames()).filter((n) -> {
            return !schemaFieldNameSet.contains(n);
        }).count();
        return extraSchemaFields == 0L && extraMessageFields == 0L;
    }

    private boolean fieldIsValid(JsonNode message, Field f) {
        String name = f.name();
        return message.has(name) || this.isNullable(f.schema(), true);
    }
}