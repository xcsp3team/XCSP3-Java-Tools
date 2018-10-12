package org.xcsp.modeler.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Stack;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.xcsp.common.Condition;
import org.xcsp.common.Constants;
import org.xcsp.common.FunctionalInterfaces.IntToDom;
import org.xcsp.common.FunctionalInterfaces.IntToDomSymbolic;
import org.xcsp.common.FunctionalInterfaces.Intx2Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx2ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx2ToDomSymbolic;
import org.xcsp.common.FunctionalInterfaces.Intx3Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx3ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx3ToDomSymbolic;
import org.xcsp.common.FunctionalInterfaces.Intx4Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx4ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx5Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx5ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx6Consumer;
import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.IVar.VarSymbolic;
import org.xcsp.common.Range;
import org.xcsp.common.Range.Rangesx2;
import org.xcsp.common.Range.Rangesx3;
import org.xcsp.common.Range.Rangesx4;
import org.xcsp.common.Range.Rangesx5;
import org.xcsp.common.Range.Rangesx6;
import org.xcsp.common.Size.Size1D;
import org.xcsp.common.Size.Size2D;
import org.xcsp.common.Size.Size3D;
import org.xcsp.common.Size.Size4D;
import org.xcsp.common.Size.Size5D;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.Utilities.ModifiableBoolean;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.DomSymbolic;
import org.xcsp.common.predicates.EvaluationManager;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.Automaton;
import org.xcsp.common.structures.Table;
import org.xcsp.common.structures.Transition;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.definitions.ICtr;
import org.xcsp.modeler.entities.CtrEntities;
import org.xcsp.modeler.entities.CtrEntities.CtrAlone;
import org.xcsp.modeler.entities.CtrEntities.CtrAloneDummy;
import org.xcsp.modeler.entities.CtrEntities.CtrArray;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;
import org.xcsp.modeler.entities.ObjEntities;
import org.xcsp.modeler.entities.ObjEntities.ObjEntity;
import org.xcsp.modeler.entities.VarEntities;

public abstract class ProblemIMP {

	/**********************************************************************************************
	 * Static Methods
	 *********************************************************************************************/

	private static Object fatalError(Object... objects) {
		System.out.println("\nProblem: " + Stream.of(objects).filter(o -> o != null).map(o -> o.toString()).collect(Collectors.joining("\n")));
		System.out.println("\n**********************");
		StackTraceElement[] t = Thread.currentThread().getStackTrace();
		boolean notEncounteredSubclass = true, nextofControl = false;
		for (StackTraceElement s : t) {
			if (nextofControl) {
				System.out.println("  Line " + s.getLineNumber() + " in Class " + s.getClassName());
				nextofControl = false;
			}
			if (s.getMethodName().equals("control") && s.getClassName().equals(ProblemIMP.class.getName()))
				nextofControl = true;
			try {
				if (notEncounteredSubclass && ProblemAPI.class.isAssignableFrom(Class.forName(s.getClassName()))
						&& !s.getClassName().equals(ProblemAPI.class.getName())) {
					System.out.println("  Line " + s.getLineNumber() + " in Class " + s.getClassName());
					notEncounteredSubclass = false;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("**********************");
		if (org.xcsp.modeler.Compiler.ev)
			throw new RuntimeException();
		else {
			System.exit(1);
			return null;
		}
	}

	public static void control(boolean b, Object... objects) {
		if (!b)
			fatalError(objects);
	}

	// we search a void method without any parameter
	public static Method searchMethod(Class<?> cl, String name) {
		if (cl != ProblemAPI.class && ProblemAPI.class.isAssignableFrom(cl)) {
			for (Method m : cl.getDeclaredMethods()) { // all methods in the class
				m.setAccessible(true);
				if (m.getName().equals(name) && m.getGenericReturnType() == void.class && m.getGenericParameterTypes().length == 0)
					return m;
			}
			return searchMethod(cl.getSuperclass(), name);
		}
		return null;
	}

	// we search and execute a void method without any parameter
	public static boolean executeMethod(Object o, String methodName) {
		Method m = searchMethod(o.getClass(), methodName);
		if (m == null)
			return false;
		m.setAccessible(true);
		try {
			m.invoke(o);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			System.out.println("Pb when executing " + methodName);
			System.out.println(e.getCause());
			System.exit(1);
		}
		return true;
	}

	public static List<Field> problemDataFields(List<Field> list, Class<?> cl) {
		if (ProblemAPI.class.isAssignableFrom(cl)) {
			problemDataFields(list, cl.getSuperclass());
			Stream.of(cl.getDeclaredFields()).filter(f -> !ProblemIMP.mustBeIgnored(f)).forEach(f -> list.add(f));
		}
		return list;
	}

	public static boolean mustBeIgnored(Field field) {
		return Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || field.getAnnotation(NotData.class) != null;
		// because static fields are ignored (and synthetic fields include this)
	}

	private Object buildInternClassObject(Constructor<?> c, Object... fieldValues) {
		try {
			c.setAccessible(true);
			Object o = c.newInstance(api);
			Field[] fields = o.getClass().getDeclaredFields();
			for (int i = 0, j = 0; i < fieldValues.length; i++) {
				while (mustBeIgnored(fields[j]))
					j++;
				fields[j].setAccessible(true);
				fields[j++].set(o, fieldValues[i]);
			}
			return o;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Pb " + e.getCause());
			System.exit(1);
		}
		return null;
	}

	public Object buildInternClassObject(String internClass, Object... fieldValues) {
		Class<?> c = api.getClass();
		while (c.getSuperclass() != Object.class)
			c = c.getSuperclass();
		Optional<Class<?>> clazz = Stream.of(c.getDeclaredClasses()).filter(cl -> cl.getName().endsWith(internClass)).findFirst();
		control(clazz.isPresent(), "Pb with " + internClass + " as it has not been found (did you give the right name?)");
		return buildInternClassObject(clazz.get().getDeclaredConstructors()[0], fieldValues);
	}

	/**********************************************************************************************
	 * Fields and Methods
	 *********************************************************************************************/

	public final ProblemAPI api;

	public final String modelVariant; // null when no model variant

	/** User arguments given on the command for the problem (instance) */
	public final String[] argsForPb;

	protected ProblemIMP(ProblemAPI api, String modelVariant, String[] argsForPb) {
		this.api = api;
		this.modelVariant = modelVariant;
		this.argsForPb = argsForPb;
		ProblemAPI.api2imp.put(api, this);
	}

	private String[] fmt(String dataFormat) {
		if (dataFormat.length() == 0)
			return null;
		String[] fmt = dataFormat.startsWith("[") ? dataFormat.substring(1, dataFormat.length() - 1).split(",") : new String[] { dataFormat };
		return Stream.of(fmt).map(s -> s.equals("null") || s.equals("-") ? "" : s).toArray(String[]::new);
	}

	private Object prepareData(Class<?> type, String v) {
		if (type == boolean.class || type == Boolean.class)
			return Utilities.toBoolean(v);
		if (type == byte.class || type == Byte.class)
			return Byte.parseByte(v);
		if (type == short.class || type == Short.class)
			return Short.parseShort(v);
		if (type == int.class || type == Integer.class)
			return Integer.parseInt(v);
		if (type == long.class || type == Long.class)
			return Long.parseLong(v);
		if (type == float.class || type == Float.class)
			return Float.parseFloat(v);
		if (type == double.class || type == Double.class)
			return Double.parseDouble(v);
		if (type == String.class)
			return v;
		Utilities.exit("No other types for data fields currently managed " + type);
		return null;
	}

	private void setFormattedValuesOfProblemDataFields(Object[] values, String[] fmt, boolean prepare) {
		Field[] fields = problemDataFields(new ArrayList<>(), api.getClass()).toArray(new Field[0]);
		control(fields.length == values.length,
				"The number of fields is different from the number of specified data " + fields.length + " vs " + values.length + " "
						+ Stream.of(fields).map(f -> f == null ? " null" : f.toString()).collect(Collectors.joining(" ")) + " "
						+ Stream.of(values).map(f -> f == null ? "null" : f.toString()).collect(Collectors.joining(" ")));
		for (int i = 0; i < fields.length; i++) {
			try {
				fields[i].setAccessible(true);
				// System.out.println("Values=" + values[i] + " " + (prepare && values[i] != null) + " test" + (values[i].getClass()));
				Object value = values[i] instanceof String && (((String) values[i]).equals("-") || ((String) values[i]).equals("null")) ? null
						: prepare ? prepareData(fields[i].getType(), (String) values[i]) : values[i];
				fields[i].set(api, value);
				if (prepare)
					addParameter(value, fmt == null ? null : fmt[i]);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				control(false, "Problem when setting the value of field " + fields[i].getName());
				e.printStackTrace();
				System.out.println(e);
			}
		}
	}

	protected void loadDataAndModel(String data, String dataFormat, boolean dataSaving) {
		if (data.length() != 0) {
			if (data.endsWith("json")) {
				new ProblemDataHandler().load(api, data);
				String value = data.startsWith(api.getClass().getSimpleName()) ? data.substring(api.getClass().getSimpleName().length() + 1) : data;
				addParameter(value);
			} else {
				control(data.startsWith("[") == data.endsWith("]"), "Either specify a simple value (such as an integer) or an array with the form [v1,v2,..]");
				control(data.indexOf(" ") == -1, "No space is allowed in specified data");
				String[] values = data.startsWith("[") ? data.substring(1, data.length() - 1).split(",") : new String[] { data };
				setFormattedValuesOfProblemDataFields(values, fmt(dataFormat), true);
			}
		} else {
			Method m = searchMethod(api.getClass(), "data");
			if (m == null)
				control(problemDataFields(new ArrayList<>(), api.getClass()).toArray(new Field[0]).length == 0, "Data must be specified.");
			else
				executeMethod(api, "data");
			String[] fmt = fmt(dataFormat);
			if (fmt != null) {
				control(fmt.length == parameters.size(), "");
				IntStream.range(0, fmt.length).forEach(i -> parameters.get(i).setValue(fmt[i]));
			}
		}
		if (dataSaving)
			new ProblemDataHandler().save(api, api.name());
		api.model();
	}

	public void setDataValues(Object value1, Object... otherValues) {
		setFormattedValuesOfProblemDataFields(IntStream.range(0, otherValues.length + 1).mapToObj(i -> i == 0 ? value1 : otherValues[i - 1]).toArray(), null,
				false);
	}

	private String nameSimplified() {
		String sn = api.getClass().getSimpleName();
		return sn.endsWith("Reader") ? sn.substring(0, sn.lastIndexOf("Reader"))
				: sn.endsWith("ReaderZ") ? sn.substring(0, sn.lastIndexOf("ReaderZ")) : sn.endsWith("Random") ? sn.substring(0, sn.lastIndexOf("Random")) : sn;
	}

	public String name() {
		String s = nameSimplified();
		s = s + (modelVariant != null && modelVariant.length() > 0 ? "-" + modelVariant : "") + formattedPbParameters();
		return s.endsWith(".xml") ? s.substring(0, s.lastIndexOf(".xml")) : s;
	}

	public abstract Class<? extends Var> classVI();

	public abstract Class<? extends VarSymbolic> classVS();

	public final VarEntities varEntities = new VarEntities(this);
	public CtrEntities ctrEntities = new CtrEntities();
	public ObjEntities objEntities = new ObjEntities();

	public Stack<Integer> stackLoops = new Stack<>();

	private Scanner inScanner = new Scanner(System.in);

	public TypeFramework typeFramework() {
		return TypeFramework.CSP;
	}

	/**********************************************************************************************
	 * Managing Problem Parameters
	 *********************************************************************************************/

	/**
	 * The list of parameters of the problem, as given by the user (after asking them).
	 */
	public ArrayList<SimpleEntry<Object, String>> parameters = new ArrayList<>();

	/** Adds to the list of problem parameters a problem parameter with the specified value and formatting pattern. */
	public Object addParameter(Object value, String format) {
		parameters.add(new SimpleEntry<>(value, format));
		return value;
	}

	/** Adds to the list of problem parameters a problem parameter with the specified value (and no formatting pattern). */
	public Object addParameter(Object value) {
		return addParameter(value, null);
	}

	/**
	 * Gets a parameter of the problem. If the value of the parameter is not directly given on the command line, then the specified message is
	 * displayed when the method is executed, and the user is asked to enter a (String) value.
	 */
	public final String ask(String message) {
		if (parameters.size() < argsForPb.length)
			return argsForPb[parameters.size()];
		System.out.print(message + " : ");
		return inScanner.next();
	}

	public String trimParameter(String s) {
		int start = s.lastIndexOf(File.separator) == -1 ? 0 : s.lastIndexOf(File.separator) + 1;
		int end = s.lastIndexOf(".") == -1 ? s.length() : s.lastIndexOf(".");
		return s.substring(start, end);
	}

	public String formattedPbParameters() {
		String s = "";
		for (SimpleEntry<Object, String> p : parameters) {
			if (p.getKey() == null)
				continue; // since null means that the parameter has been discarded
			String t = p.getValue() != null ? String.format(p.getValue(), p.getKey()) : p.getKey().toString();
			t = trimParameter(t);
			if (t.length() != 0)
				s += "-" + t;
		}
		return s;
	}

	public boolean askBoolean(String message, Function<Boolean, String> format) {
		Boolean b = Utilities.toBoolean(ask(message + " (yes/no)"));
		Utilities.control(b != null, "A boolean value was expected when asking " + message);
		return (Boolean) addParameter(b, format == null ? null : format.apply(b));
	}

	/**
	 * Defines a parameter of the problem. If the value of the parameter is not directly given on the command line, then the specified message is
	 * displayed when the method is executed, and the user is asked to enter a Boolean value.
	 */
	public boolean askBoolean(String message) {
		return askBoolean(message, null);
	}

	/**
	 * Defines a parameter of the problem. If the value of the parameter is not directly given on the command line, then the specified message is
	 * displayed when the method is executed, and the user is asked to enter an integer value.
	 */
	public int askInt(String message, Predicate<Integer> control, IntFunction<String> format) {
		Integer v = Utilities.toInteger(ask(message));
		Utilities.control(v != null, "Value " + v + " for " + message + " is not valid (not an integer)");
		Utilities.control(control == null || control.test(v), "Value " + v + " for " + message + " does not respect the control " + control);
		return (Integer) addParameter(v, format == null ? null : format.apply(v));
	}

	public int askInt(String message, Range range, IntFunction<String> format) {
		return askInt(message, i -> range.contains(i), format);
	}

	public int askInt(String message, Predicate<Integer> control, String format) {
		return askInt(message, control, v -> format);
	}

	public int askInt(String message, Range range, String format) {
		return askInt(message, i -> range.contains(i), v -> format);
	}

	public int askInt(String message, Predicate<Integer> control) {
		return askInt(message, control, (IntFunction<String>) null);
	}

	public int askInt(String message, Range range) {
		return askInt(message, i -> range.contains(i), (IntFunction<String>) null);
	}

	public int askInt(String message, String format) {
		return askInt(message, (Predicate<Integer>) null, format);
	}

	public int askInt(String message) {
		return askInt(message, (Predicate<Integer>) null, (IntFunction<String>) null);
	}

	public double askDouble(String message, Predicate<Double> control, DoubleFunction<String> format) {
		Double d = Utilities.toDouble(ask(message));
		Utilities.control(d != null, "Value " + d + " for " + message + " is not valid (not a double)");
		Utilities.control(control == null || control.test(d), "Value " + d + " for " + message + " does not respect the control " + control);
		return (Double) addParameter(d, format == null ? null : format.apply(d));
	}

	public double askDouble(String message, Predicate<Double> control, String format) {
		return askDouble(message, control, v -> format);
	}

	public double askDouble(String message, Predicate<Double> control) {
		return askDouble(message, control, (DoubleFunction<String>) null);
	}

	public double askDouble(String message, String format) {
		return askDouble(message, null, format);
	}

	/**
	 * Defines a parameter of the problem. If the value of the parameter is not directly given on the command line, then the specified message is
	 * displayed when the method is executed, and the user is asked to enter a double value. It is also possible to indicate both minimum and maximum
	 * bounds, by using for example the method range.
	 */
	public double askDouble(String message) {
		return askDouble(message, null, (DoubleFunction<String>) null);
	}

	/**
	 * Defines a parameter of the problem. If the value of the parameter is not directly given on the command line, then the specified message is
	 * displayed when the method is executed, and the user is asked to enter a String value. The specified functional interface is used to format the
	 * value of the parameter when a file name for the problem instance is built.
	 */
	public String askString(String message, Function<String, String> format) {
		String s = ask(message);
		return (String) addParameter(s, format == null ? null : format.apply(s));
	}

	/**
	 * Defines a parameter of the problem. If the value of the parameter is not directly given on the command line, then the specified message is
	 * displayed when the method is executed, and the user is asked to enter a String value.
	 */
	public String askString(String message) {
		return askString(message, null);
	}

	private Scanner fileScanner;

	public Scanner fileScanner() {
		if (fileScanner != null)
			return fileScanner;
		String fileName = askString("Enter data filename");
		try {
			fileScanner = new Scanner(new File(fileName));
			return fileScanner;
		} catch (FileNotFoundException e) {
			System.out.println("Error with " + fileName);
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	/**********************************************************************************************
	 * Managing Variables
	 *********************************************************************************************/

	public abstract Var buildVarInteger(String id, Dom dom);

	public abstract VarSymbolic buildVarSymbolic(String id, DomSymbolic dom);

	// public <T extends IVar> T[] varsTyped(Class<T> clazz, Object first, Object... next) {
	// return Utilities.collect(clazz, first, next);
	// }

	public Var[] fill(String id, Size1D size, IntToDom f, Var[] t) {
		for (int i = 0; i < size.lengths[0]; i++) {
			Dom dom = f.apply(i);
			if (dom != null) {
				Var x = buildVarInteger(id + variableNameSuffixFor(i), dom);
				if (x != null)
					t[i] = x;
			}
		}
		return t;
	}

	public Var[][] fill(String id, Size2D size, Intx2ToDom f, Var[][] t) {
		IntStream.range(0, size.lengths[0]).forEach(i -> fill(id + "[" + i + "]", Size1D.build(size.lengths[1]), j -> f.apply(i, j), t[i]));
		return t;
	}

	public Var[][][] fill(String id, Size3D size, Intx3ToDom f, Var[][][] t) {
		IntStream.range(0, size.lengths[0])
				.forEach(i -> fill(id + "[" + i + "]", Size2D.build(size.lengths[1], size.lengths[2]), (j, k) -> f.apply(i, j, k), t[i]));
		return t;
	}

	public Var[][][][] fill(String id, Size4D size, Intx4ToDom f, Var[][][][] t) {
		IntStream.range(0, size.lengths[0]).forEach(
				i -> fill(id + "[" + i + "]", Size3D.build(size.lengths[1], size.lengths[2], size.lengths[3]), (j, k, l) -> f.apply(i, j, k, l), t[i]));
		return t;
	}

	public Var[][][][][] fill(String id, Size5D size, Intx5ToDom f, Var[][][][][] t) {
		IntStream.range(0, size.lengths[0]).forEach(i -> fill(id + "[" + i + "]",
				Size4D.build(size.lengths[1], size.lengths[2], size.lengths[3], size.lengths[4]), (j, k, l, m) -> f.apply(i, j, k, l, m), t[i]));
		return t;
	}

	public VarSymbolic[] fill(String id, Size1D size, IntToDomSymbolic f, VarSymbolic[] t) {
		for (int i = 0; i < size.lengths[0]; i++) {
			DomSymbolic dom = f.apply(i);
			if (dom != null) {
				VarSymbolic x = buildVarSymbolic(id + variableNameSuffixFor(i), dom);
				if (x != null)
					t[i] = x;
			}
		}
		return t;
	}

	public VarSymbolic[][] fill(String id, Size2D size, Intx2ToDomSymbolic f, VarSymbolic[][] t) {
		IntStream.range(0, size.lengths[0]).forEach(i -> fill(id + "[" + i + "]", Size1D.build(size.lengths[1]), j -> f.apply(i, j), t[i]));
		return t;
	}

	public VarSymbolic[][][] fill(String id, Size3D size, Intx3ToDomSymbolic f, VarSymbolic[][][] t) {
		IntStream.range(0, size.lengths[0])
				.forEach(i -> fill(id + "[" + i + "]", Size2D.build(size.lengths[1], size.lengths[2]), (j, k) -> f.apply(i, j, k), t[i]));
		return t;
	}

	/**********************************************************************************************
	 * Managing Arrays of Variables
	 *********************************************************************************************/

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified sequence of parameters. All variables encountered in the parameters,
	 * extracting them from arrays (of any dimension), collections and streams, are recursively collected in order, and concatenated to form a
	 * 1-dimensional array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param objects
	 *            a sequence of objects
	 * @return a 1-dimensional array of variables
	 */
	public <T extends IVar> T[] vars(Object... objects) {
		return (T[]) Utilities.collect(IVar.class, objects);
	}

	public <T extends IVar> T[] vars(T[][] x) {
		return vars((Object) x);
	}

	public <T extends IVar> T[] clean(T[] vars) {
		return Utilities.convert(Stream.of(vars).filter(x -> x != null).collect(Collectors.toList()));
	}

	public <T extends IVar> T[] distinct(T[] vars) {
		return Utilities.convert(Stream.of(vars).filter(x -> x != null).distinct().collect(Collectors.toList()));
	}

	public <T extends IVar> T[] distinctSorted(T[] vars) {
		return Utilities.convert(Stream.of(vars).filter(x -> x != null).distinct().sorted().collect(Collectors.toList()));
	}

	public void save(Document document, String fileName) {
		if (fileName == null)
			Utilities.save(document, new PrintWriter(System.out, true));
		else {
			System.out.print("\n   Saving XCSP File " + fileName + " ... ");
			Utilities.save(document, fileName);
			System.out.println("Finished.\n");
		}
	}

	public void indentAndCompressXmlUnderLinux(String fileName) {
		if (fileName != null) {
			System.out.print("   Indenting and Compressing File, yielding " + fileName + ".lzma ... ");
			try {
				Runtime.getRuntime().exec("xmlindent -i 2 -w " + fileName).waitFor();
				Runtime.getRuntime().exec("rm " + fileName + ".lzma").waitFor();
				Runtime.getRuntime().exec("lzma " + fileName).waitFor();
				Runtime.getRuntime().exec("rm " + fileName + "~").waitFor();
			} catch (Exception e) {
				Utilities.exit("Pb when Indenting/Compressing File " + fileName + " " + e);
			}
			System.out.println("Finished.\n");
		}
	}

	public String variableNameSuffixFor(int... vals) {
		return "[" + Utilities.join(vals, "][") + "]";
	}

	public String intervalAsString(int[] lbs, int[] ubs) {
		Utilities.control(lbs.length == ubs.length, "Bad form of intervals");
		return IntStream.range(0, lbs.length).mapToObj(i -> lbs[i] + ".." + ubs[i]).collect(Collectors.joining(" "));
	}

	public IVar[] scope(Object... objects) {
		objects = Stream.of(objects).filter(o -> o != null).map(o -> o instanceof Condition ? ((Condition) o).involvedVar() : o).toArray();
		return Utilities.convert(Stream.of(Utilities.collect(IVar.class, objects)).distinct().collect(Collectors.toList()));
	}

	public CtrEntity dummyConstraint(String message) {
		System.out.println("Dummy constraint. " + message + " Is that correct?");
		return ctrEntities.new CtrAloneDummy(message);
	}

	// ************************************************************************
	// ***** Constraint intension
	// ************************************************************************

	public abstract CtrEntity intension(XNodeParent<IVar> tree);

	public CtrEntity lessThan(Object operand1, Object operand2) {
		return intension(XNodeParent.lt(operand1, operand2));
	}

	public CtrEntity lessEqual(Object operand1, Object operand2) {
		return intension(XNodeParent.le(operand1, operand2));
	}

	public CtrEntity greaterThan(Object operand1, Object operand2) {
		return intension(XNodeParent.gt(operand1, operand2));
	}

	public CtrEntity greaterEqual(Object operand1, Object operand2) {
		return intension(XNodeParent.ge(operand1, operand2));
	}

	public CtrEntity equal(Object... operands) {
		return intension(XNodeParent.eq(operands));
	}

	public final CtrEntity different(Object... operands) {
		return intension(XNodeParent.ne(operands));
	}

	// ************************************************************************
	// ***** Converting intension to extension
	// ************************************************************************

	protected abstract class Converter {
		public Map<String, int[][]> mapT = new HashMap<>();
		public Map<String, Boolean> mapP = new HashMap<>();

		public abstract StringBuilder signatureFor(Var[] scp);

		public abstract int[][] domValuesOf(Var[] scp);

		public abstract ModifiableBoolean mode();

		public String handle(Var[] scp, XNodeParent<IVar> tree) {
			String key = signatureFor(scp).append(tree.abstraction(new ArrayList<>(), false, true).canonization().toString()).toString();
			if (mapT.containsKey(key))
				return key;
			// System.out.println("key = " + key);
			ModifiableBoolean b = mode();
			int[][] tuples = new EvaluationManager(tree).generateTuples(domValuesOf(scp), b);
			assert b.value != null;
			mapT.put(key, tuples);
			mapP.put(key, b.value);
			// System.out.println("Positive=" + b + " Tuples=" + Utilities.join(tuples));
			return key;
		}
	}

	protected abstract Converter getConverter();

	public CtrAlone extension(XNodeParent<IVar> tree) {
		Utilities.control(tree.vars() instanceof Var[], "Currently, only implemented for integer variables");
		Converter converter = getConverter();
		Var[] scp = (Var[]) tree.vars();
		String key = converter.handle(scp, tree);
		return extension(scp, converter.mapT.get(key), converter.mapP.get(key));
	}

	public final CtrAlone extensionDisjunction(List<XNodeParent<IVar>> trees) {
		Utilities.control(trees.stream().allMatch(tree -> tree.vars() instanceof Var[]), "Currently, only implemented for integer variables");
		Converter converter = getConverter();
		Var[] scp = api.singleVariablesFrom(trees, t -> t.vars());
		Table table = new Table();
		for (XNodeParent<IVar> root : trees) {
			Var[] ls = (Var[]) root.vars();
			int[][] supports = new EvaluationManager(root).generateSupports(converter.domValuesOf(ls)); // Variable.initDomainValues(ls));
			int[][] tuples = api.range(supports.length).range(scp.length).map((i, j) -> Constants.STAR);
			for (int c = 0; c < ls.length; c++) {
				int cc = Utilities.indexOf(ls[c], scp);
				for (int i = 0; i < tuples.length; i++)
					tuples[i][cc] = supports[i][c];
			}
			table.add(tuples);
		}
		// System.out.println("TUP= " + Utilities.join(table.toArray()));
		return extension(scp, table.toArray(), true);
	}

	// ************************************************************************
	// ***** Constraint extension
	// ************************************************************************

	public abstract CtrAlone extension(Var[] scp, int[][] tuples, boolean positive);

	public abstract CtrAlone extension(VarSymbolic[] scp, String[][] tuples, boolean positive);

	// ************************************************************************
	// ***** Constraints regular and mdd
	// ************************************************************************

	public abstract CtrEntity regular(Var[] list, Automaton automaton);

	public abstract CtrEntity mdd(Var[] scp, Transition[] transitions);

	// ************************************************************************
	// ***** Constraint allDifferent
	// ************************************************************************

	public abstract CtrEntity allDifferent(Var[] list);

	public abstract CtrEntity allDifferent(VarSymbolic[] list);

	public abstract CtrEntity allDifferent(Var[] list, int[] exceptValues);

	public abstract CtrEntity allDifferentList(Var[]... lists);

	public abstract CtrEntity allDifferentMatrix(Var[][] matrix);

	public abstract CtrEntity allDifferent(XNodeParent<IVar>[] trees);

	// ************************************************************************
	// ***** Constraint allEqual
	// ************************************************************************

	public abstract CtrEntity allEqual(Var... list);

	public abstract CtrEntity allEqual(VarSymbolic... list);

	public abstract CtrEntity allEqualList(Var[]... lists);

	// ************************************************************************
	// ***** Constraint ordered and lex
	// ************************************************************************

	public abstract CtrEntity ordered(Var[] list, int[] lengths, TypeOperatorRel operator);

	public abstract CtrEntity ordered(Var[] list, Var[] lengths, TypeOperatorRel operator);

	public abstract CtrEntity lex(Var[][] lists, TypeOperatorRel operator);

	public abstract CtrEntity lexMatrix(Var[][] matrix, TypeOperatorRel operator);

	// ************************************************************************
	// ***** Constraint sum
	// ************************************************************************

	public abstract CtrEntity sum(Var[] list, int[] coeffs, Condition condition);

	public abstract CtrEntity sum(Var[] list, Var[] coeffs, Condition condition);

	public abstract CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, Condition condition);

	// ************************************************************************
	// ***** Constraint count
	// ************************************************************************

	public abstract CtrEntity count(Var[] list, int[] values, Condition condition);

	public abstract CtrEntity count(Var[] list, Var[] values, Condition condition);

	// ************************************************************************
	// ***** Constraint nValues
	// ************************************************************************

	public abstract CtrEntity nValues(Var[] list, Condition condition);

	public abstract CtrEntity nValues(Var[] list, Condition condition, int[] exceptValues);

	// ************************************************************************
	// ***** Constraint cardinality
	// ************************************************************************

	public abstract CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, int[] occurs);

	public abstract CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, Var[] occurs);

	public abstract CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, int[] occursMin, int[] occursMax);

	public abstract CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, int[] occurs);

	public abstract CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, Var[] occurs);

	public abstract CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, int[] occursMin, int[] occursMax);

	// ************************************************************************
	// ***** Constraint maximum
	// ************************************************************************

	public abstract CtrEntity maximum(Var[] list, Condition condition);

	public abstract CtrEntity maximum(Var[] list, int startIndex, Var index, TypeRank rank);

	public abstract CtrEntity maximum(Var[] list, int startIndex, Var index, TypeRank rank, Condition condition);

	// ************************************************************************
	// ***** Constraint minimum
	// ************************************************************************

	public abstract CtrEntity minimum(Var[] list, Condition condition);

	public abstract CtrEntity minimum(Var[] list, int startIndex, Var index, TypeRank rank);

	public abstract CtrEntity minimum(Var[] list, int startIndex, Var index, TypeRank rank, Condition condition);

	// ************************************************************************
	// ***** Constraint element
	// ************************************************************************

	public abstract CtrEntity element(Var[] list, int value);

	public abstract CtrEntity element(Var[] list, Var value);

	public abstract CtrEntity element(Var[] list, int startIndex, Var index, TypeRank rank, int value);

	public abstract CtrEntity element(Var[] list, int startIndex, Var index, TypeRank rank, Var value);

	public abstract CtrEntity element(int[] list, int startIndex, Var index, TypeRank rank, Var value);

	// ************************************************************************
	// ***** Constraint channel
	// ************************************************************************

	public abstract CtrEntity channel(Var[] list, int startIndex);

	public abstract CtrEntity channel(Var[] list1, int startIndex1, Var[] list2, int startIndex2);

	public abstract CtrEntity channel(Var[] list, int startIndex, Var value);

	// ************************************************************************
	// ***** Constraint stretch
	// ************************************************************************

	public abstract CtrEntity stretch(Var[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns);

	// ************************************************************************
	// ***** Constraint noOverlap
	// ************************************************************************

	public abstract CtrEntity noOverlap(Var[] origins, int[] lengths, boolean zeroIgnored);

	public abstract CtrEntity noOverlap(Var[] origins, Var[] lengths, boolean zeroIgnored);

	public abstract CtrEntity noOverlap(Var[][] origins, int[][] lengths, boolean zeroIgnored);

	public abstract CtrEntity noOverlap(Var[][] origins, Var[][] lengths, boolean zeroIgnored);

	// ************************************************************************
	// ***** Constraint cumulative
	// ************************************************************************

	public abstract CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, int[] heights, Condition condition);

	public abstract CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, int[] heights, Condition condition);

	public abstract CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, Var[] heights, Condition condition);

	public abstract CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, Var[] heights, Condition condition);

	// ************************************************************************
	// ***** Constraint circuit
	// ************************************************************************

	public abstract CtrEntity circuit(Var[] list, int startIndex);

	public abstract CtrEntity circuit(Var[] list, int startIndex, int size);

	public abstract CtrEntity circuit(Var[] list, int startIndex, Var size);

	// ************************************************************************
	// ***** Constraint clause
	// ************************************************************************

	public abstract CtrEntity clause(Var[] list, Boolean[] phases);

	// ************************************************************************
	// ***** Constraint instantiation
	// ************************************************************************

	public abstract CtrEntity instantiation(Var[] list, int[] values);

	// ************************************************************************
	// ***** Meta-Constraint slide
	// ************************************************************************

	public abstract CtrEntity slide(IVar[] list, Range range, IntFunction<CtrEntity> template);

	// ************************************************************************
	// ***** Meta-Constraint ifThen
	// ************************************************************************

	public abstract CtrEntity ifThen(CtrEntity c1, CtrEntity c2);

	// ************************************************************************
	// ***** Meta-Constraint ifThenElse
	// ************************************************************************

	public abstract CtrEntity ifThenElse(CtrEntity c1, CtrEntity c2, CtrEntity c3);

	// ************************************************************************
	// ***** Managing loops
	// ************************************************************************

	public CtrArray manageLoop(Runnable r) {
		stackLoops.push(ctrEntities.allEntities.size());
		r.run();
		int limit = stackLoops.pop();
		ICtr[] ctrs = IntStream.range(limit, ctrEntities.allEntities.size()).mapToObj(i -> ctrEntities.allEntities.get(i))
				.filter(e -> e instanceof CtrAlone && !(e instanceof CtrAloneDummy)).map(e -> ((CtrAlone) e).ctr).toArray(ICtr[]::new);
		CtrArray ca = ctrEntities.newCtrArrayEntity(ctrs, stackLoops.size() > 0);
		ca.varEntitiessSubjectToTags = varEntities.buildTimes.entrySet().stream().filter(e -> e.getValue() >= limit).map(e -> e.getKey())
				.collect(Collectors.toList());
		return ca;
	}

	/** Builds constraints by considering the specified range and soliciting the specified function. */
	public CtrArray forall(Range range, IntConsumer c) {
		return manageLoop(() -> range.execute(c));
	}

	/** Builds constraints by considering the specified ranges and soliciting the specified function. */
	public CtrArray forall(Rangesx2 rangesx2, Intx2Consumer c2) {
		return manageLoop(() -> rangesx2.execute(c2));
	}

	/** Builds constraints by considering the specified ranges and soliciting the specified function. */
	public CtrArray forall(Rangesx3 rangesx3, Intx3Consumer c3) {
		return manageLoop(() -> rangesx3.execute(c3));
	}

	/** Builds constraints by considering the specified ranges and soliciting the specified function. */
	public CtrArray forall(Rangesx4 rangesx4, Intx4Consumer c4) {
		return manageLoop(() -> rangesx4.execute(c4));
	}

	/** Builds constraints by considering the specified ranges and soliciting the specified function. */
	public CtrArray forall(Rangesx5 rangesx5, Intx5Consumer c5) {
		return manageLoop(() -> rangesx5.execute(c5));
	}

	/** Builds constraints by considering the specified ranges and soliciting the specified function. */
	public CtrArray forall(Rangesx6 rangesx6, Intx6Consumer c6) {
		return manageLoop(() -> rangesx6.execute(c6));
	}

	// ************************************************************************
	// ***** Managing objectives
	// ************************************************************************

	public abstract ObjEntity minimize(IVar x);

	public abstract ObjEntity maximize(IVar x);

	public abstract ObjEntity minimize(TypeObjective type, IVar[] list);

	public abstract ObjEntity maximize(TypeObjective type, IVar[] list);

	public abstract ObjEntity maximize(TypeObjective type, IVar[] list, int[] coeffs);

	public abstract ObjEntity minimize(TypeObjective type, IVar[] list, int[] coeffs);

	/**********************************************************************************************
	 * Managing Annotations
	 *********************************************************************************************/

	public Annotations annotations = new Annotations();

	public static class Annotations {
		public IVar[] decision;

		public boolean active() {
			return decision != null;
		}
	}

	public void decisionVariables(IVar[] list) {
		annotations.decision = list;
	}

}