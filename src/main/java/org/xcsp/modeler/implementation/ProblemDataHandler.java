package org.xcsp.modeler.implementation;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import org.xcsp.common.Utilities;
import org.xcsp.modeler.api.ProblemAPI;

/** Data Handler, using JSON as format. */
public final class ProblemDataHandler {

	private Object handleField(Field field, JsonObject jsonObject, ProblemAPI api) {
		if (ProblemIMP.mustBeIgnored(field))
			return null;
		field.setAccessible(true);
		String key = field.getName();
		try {
			if (jsonObject.isNull(key))
				return null;
		} catch (NullPointerException e) {
			ProblemIMP.control(false, "The field " + key + " has not been found");
		}
		return load(jsonObject.get(key), field.getType(), field.getGenericType(), api);
	}

	private Object load(JsonValue json, Class<?> type, Type genericType, ProblemAPI api) {
		if (json instanceof JsonString && ((JsonString) json).getString().equals("null") && type != String.class)
			return null; // is that always the good solution ? what about String and "null ?
		if (type == boolean.class || type == Boolean.class)
			return json == JsonValue.TRUE;
		if (type == int.class || type == Integer.class || type == byte.class || type == Byte.class || type == short.class || type == Short.class)
			return ((JsonNumber) json).intValue();
		if (type == long.class || type == Long.class)
			return ((JsonNumber) json).longValue();
		if (type == double.class || type == Double.class || type == float.class || type == Float.class)
			return ((JsonNumber) json).doubleValue();
		if (type == String.class)
			return ((JsonString) json).getString();
		if (json.toString().equals("\"null\"") || json.toString().equals("null")) // null or string "null" => null
			return null;
		if (type.isArray()) {
			JsonArray jsonArray = (JsonArray) json;
			List<Object> list = jsonArray.stream().map(v -> load(v, type.getComponentType(), null, api)).collect(Collectors.toList());
			Object array = Array.newInstance(type.getComponentType(), jsonArray.size());
			IntStream.range(0, jsonArray.size()).forEach(i -> Array.set(array, i, list.get(i)));
			return array;
		}
		if (type == List.class) {
			Class<?> c = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
			return ((JsonArray) json).stream().map(v -> load(v, c, null, api)).collect(Collectors.toList());
		}
		if (type == Map.class) {
			Class<?> c1 = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
			Class<?> c2 = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[1];
			Utilities.control(c1 == Integer.class || c1 == String.class, "Managing other types of keys ?");
			return ((JsonObject) json).entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), e -> load(e.getValue(), c2, null, api)));
		}
		// below, this is the code for loading an object
		try {
			if (type.isEnum()) {
				Utilities.control(Stream.of(type.getEnumConstants()).anyMatch(c -> json.toString().equals("\"" + c.toString() + "\"")), "");
				return Stream.of(type.getEnumConstants()).filter(c -> json.toString().equals("\"" + c.toString() + "\"")).findFirst().get();
			}
			if (json instanceof JsonString) {
				Utilities.control(json.toString().equals("\"null\""), "Pb with a JSON element");
				return null;
			}
			JsonObject jsonObject = (JsonObject) json;
			if (type == api.getClass()) {
				for (Field field : type.getDeclaredFields()) {
					Object value = handleField(field, jsonObject, api);
					if (value != null)
						field.set(api, value);
				}
				return api;
			} else {
				Utilities.control(type.getDeclaredConstructors().length == 1, "Only one constructor is allowed");
				Constructor<?> c = type.getDeclaredConstructors()[0];
				c.setAccessible(true);
				boolean additionnalArgument = !Modifier.isStatic(type.getModifiers()) && type.getEnclosingClass() != null;
				boolean defaultConstructor = c.getParameterTypes().length == (additionnalArgument ? 1 : 0);
				if (defaultConstructor) {
					Object obj = additionnalArgument ? c.newInstance(api) : c.newInstance();
					for (Field field : type.getDeclaredFields()) {
						Object value = handleField(field, jsonObject, api);
						if (value != null)
							field.set(obj, value);
					}
					return obj;
				} else {
					List<Object> list = new ArrayList<>();
					if (additionnalArgument)
						list.add(api); // api first object of the constructor if the class is not static
					for (Field field : type.getDeclaredFields()) {
						Object value = handleField(field, jsonObject, api);
						if (value != null)
							list.add(value);
					}
					return c.newInstance(list.toArray());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.exit("Pb when loading ");
		}
		return null;
	}

	public void load(ProblemAPI api, String fileName) {
		try (JsonReader jsonReader = Json.createReader(new BufferedReader(new FileReader(fileName)))) {
			load(jsonReader.readObject(), api.getClass(), null, api);
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.exit("Pb when loading " + e);
		}
	}

	private JsonStructure save(Object object) throws Exception {
		if (object.getClass().isArray() || object instanceof List) {
			JsonArrayBuilder builder = Json.createArrayBuilder();
			for (Object item : object instanceof List ? (List<?>) object
					: IntStream.range(0, Array.getLength(object)).mapToObj(i -> Array.get(object, i)).collect(Collectors.toList())) {
				if (item == null)
					builder.addNull();
				else if (item.getClass() == Boolean.class)
					builder.add((boolean) item);
				else if (item.getClass() == Integer.class || item.getClass() == Byte.class || item.getClass() == Short.class)
					builder.add(((Number) item).intValue());
				else if (item.getClass() == Long.class)
					builder.add((long) item);
				else if (item.getClass() == Double.class || item.getClass() == Float.class)
					builder.add(((Number) item).doubleValue());
				else if (item.getClass() == String.class)
					builder.add((String) item);
				else
					builder.add(save(item));
			}
			return builder.build();
		}
		JsonObjectBuilder builder = Json.createObjectBuilder();
		Map<Object, Object> map = null;
		if (object instanceof Map)
			map = (Map<Object, Object>) object;
		else if (object instanceof ProblemAPI) {
			List<Field> fields = ProblemIMP.problemDataFields(new ArrayList<>(), object.getClass());
			map = fields.stream().collect(Collectors.toMap(Field::getName, f -> {
				try {
					f.setAccessible(true);
					return f.get(object) == null ? "null" : f.get(object); // "null" because null provokes an exception when merging
				} catch (Exception e) {
					return "null";
				}
			}, (v1, v2) -> v1, LinkedHashMap::new));
		} else {
			map = Stream.of(object.getClass().getDeclaredFields()).filter(f -> !ProblemIMP.mustBeIgnored(f)).peek(f -> f.setAccessible(true))
					.collect(Collectors.toMap(Field::getName, f -> {
						try {
							f.setAccessible(true);
							return f.get(object) == null ? "null" : f.get(object); // "null" because null provokes an exception when merging
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}, (v1, v2) -> v1, LinkedHashMap::new));
		}
		for (Object key : map.keySet())
			map.replace(key, "null", null);
		for (Entry<?, ?> e : map.entrySet()) {
			String key = e.getKey().toString();
			Object value = e.getValue();
			if (value == null)
				builder.addNull(key);
			else if (value.getClass().isArray())
				builder.add(key, save(value));
			else {
				if (value.getClass() == Boolean.class)
					builder.add(key, (boolean) value);
				else if (value.getClass() == Integer.class || value.getClass() == Byte.class || value.getClass() == Short.class)
					builder.add(key, ((Number) value).intValue());
				else if (value.getClass() == Long.class)
					builder.add(key, (long) value);
				else if (value.getClass() == Double.class || value.getClass() == Float.class)
					builder.add(key, ((Number) value).doubleValue());
				else if (value.getClass() == String.class)
					builder.add(key, (String) value);
				else if (value instanceof Enum)
					builder.add(key, value.toString());
				else
					builder.add(key, save(value));
			}
		}
		return builder.build();
	}

	public void save(ProblemAPI api, String fileName) {
		fileName = fileName + ".json";
		System.out.print("\n   Saving Data File " + fileName + " ... ");
		Map<String, Object> properties = new HashMap<>(1);
		// properties.put(JsonGenerator.PRETTY_PRINTING, true);
		try (JsonWriter jsonWriter = Json.createWriterFactory(properties).createWriter(new PrintWriter(new FileOutputStream(fileName)));) {
			JsonStructure js = save(api);
			jsonWriter.write(js);
		} catch (Exception e) {
			e.printStackTrace();
			e.getCause().getStackTrace();
		}
		System.out.println("Finished.");
	}
}
