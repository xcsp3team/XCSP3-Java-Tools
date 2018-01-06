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
import org.xcsp.modeler.ProblemAPI;

/** Data Handler, using JSON as format. */
public final class ProblemDataHandler {

	private Object load(JsonValue json, Class<?> type, Type genericType, ProblemAPI api) {
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
		if (json.toString().equals("\"null\"")) // string "null" => null
			return null;
		if (type.isArray()) {
			JsonArray jsonArray = (JsonArray) json;
			List<Object> list = jsonArray.stream().map(v -> load(v, type.getComponentType(), null, api)).collect(Collectors.toList());
			Object array = Array.newInstance(type.getComponentType(), jsonArray.size());
			IntStream.range(0, jsonArray.size()).forEach(i -> Array.set(array, i, list.get(i)));
			return array;
		}
		if (type == List.class) {
			JsonArray jsonArray = (JsonArray) json;
			Class<?> c = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
			List<Object> list = jsonArray.stream().map(v -> load(v, c, null, api)).collect(Collectors.toList());
			return list;
		}
		if (type == Map.class) {
			Class<?> c1 = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
			Class<?> c2 = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[1];
			JsonObject jsonObject = (JsonObject) json;
			Utilities.control(c1 == String.class, "Managing other types of keys ?");
			return jsonObject.entrySet().stream()
					.collect(Collectors.toMap((Entry<String, JsonValue> e) -> Integer.parseInt(e.getKey()), e -> load(e.getValue(), c2, null, api)));
		}
		// below, this is the code for loading an object
		try {
			if (json instanceof JsonString) {
				Utilities.control(json.toString().equals("\"null\""), "Pb with a JSON element");
				return null;
			}
			JsonObject jsonObject = (JsonObject) json;
			if (type == api.getClass()) {
				for (Field field : type.getDeclaredFields()) {
					if (ProblemIMP.mustBeIgnored(field))
						continue;
					field.setAccessible(true);
					String key = field.getName();
					if (jsonObject.isNull(key))
						continue;
					field.set(api, load(jsonObject.get(key), field.getType(), field.getGenericType(), api));
				}
				return api;
			} else {
				Utilities.control(type.getDeclaredConstructors().length == 1, "Only one constructor is allowed");
				Constructor<?> c = type.getDeclaredConstructors()[0];
				c.setAccessible(true);
				List<Object> list = new ArrayList<>();
				if (!Modifier.isStatic(type.getModifiers()) && type.getEnclosingClass() != null)
					list.add(api); // api first object of the constructor if the class is not static
				for (Field field : type.getDeclaredFields()) {
					if (ProblemIMP.mustBeIgnored(field))
						continue;
					field.setAccessible(true);
					String key = field.getName();
					if (jsonObject.isNull(key))
						continue;
					list.add(load(jsonObject.get(key), field.getType(), field.getGenericType(), api));
				}
				return c.newInstance(list.toArray());
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
				if (item.getClass() == Boolean.class)
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
		Map<?, ?> map = null;
		if (object instanceof Map)
			map = (Map<?, ?>) object;
		else if (object instanceof ProblemAPI) {
			List<Field> fields = ProblemIMP.problemDataFields(new ArrayList<>(), object.getClass());
			map = fields.stream().collect(Collectors.toMap(Field::getName, f -> {
				try {
					f.setAccessible(true);
					return f.get(object) == null ? "null" : f.get(object); // need to return a string "null" because null provokes an
																			// exception when merging
				} catch (Exception e) {
					// e.printStackTrace();
					return "null";
				}
			}, (v1, v2) -> v1, LinkedHashMap::new));
		} else
			map = Stream.of(object.getClass().getDeclaredFields()).filter(f -> !ProblemIMP.mustBeIgnored(f)).peek(f -> f.setAccessible(true))
					.collect(Collectors.toMap(Field::getName, f -> {
						try {
							f.setAccessible(true);
							return f.get(object);
						} catch (Exception e) {
							e.printStackTrace();
							return null;
						}
					}, (v1, v2) -> v1, LinkedHashMap::new));

		for (Entry<?, ?> e : map.entrySet()) {
			String key = e.getKey().toString();
			Object value = e.getValue();
			if (value.getClass().isArray())
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
			// Utilities.exit("Pb when saving " + e);
			e.printStackTrace();
			e.getCause().getStackTrace();
		}
		System.out.println("Finished.");
	}
}
