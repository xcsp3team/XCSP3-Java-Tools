package org.xcsp.modeler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.xcsp.modeler.implementation.ProblemIMP;

public interface ProblemAPIBase {
	/**
	 * <b>Advanced Use</b>: you shouldn't normally use this map that relates {@code ProblemAPI} objects with {@code ProblemIMP} objects.
	 */
	static Map<ProblemAPI, ProblemIMP> api2imp = new HashMap<>();

	/**
	 * <b>Advanced Use</b>: you shouldn't normally use the {@code ProblemIMP} object that offers implementation stuff for this object.
	 * 
	 * @return the {@code ProblemIMP} object that offers implementation stuff for this {@code ProblemAPI} object
	 */
	default ProblemIMP imp() {
		control(api2imp.get(this) != null, "The method has been called before the associated problem implementation object was created.");
		return api2imp.get(this);
	}

	/**
	 * Controls that the specified {@code boolean} argument is {@code true}. If it is not the case, the program will stop and specified objects will
	 * be displayed.
	 * 
	 * @param b
	 *            a {@code boolean} value to be controlled to be {@code true}
	 * @param objects
	 *            a sequence of objects used for displaying information when the specified {@code boolean} argument is {@code false}
	 */
	default void control(boolean b, Object... objects) {
		ProblemIMP.control(b, objects);
	}

	/**
	 * Returns the name of this object (i.e., the name of this problem instance). By default, this is the name of the class implementing
	 * {@code ProblemAPI} followed by the values of all parameters (separated by the symbol '-'). The parameters are the fields, used as data, which
	 * are declared in the class implementing {@code ProblemAPI}. Possibly, the name of a model variant, if used, is inserted after the name of the
	 * class.
	 */
	default String name() {
		return imp().name();
	}

	/**
	 * Returns the name of the model variant. If no model (variant) has been explicitly specified, it is {@code null}.
	 * 
	 * @return the name of the model variant, or ({@code null} is no model has been explicitly specified)
	 */
	default String modelVariant() {
		return imp().model;
	}

	/**
	 * Returns {@code true} iff the user has indicated (through the compiler by using the argument -model=) that the model variant corresponds to the
	 * value of the specified string.
	 * 
	 * @param s
	 *            a string representing the name of a model (variant)
	 * @return {@code true} iff the model (variant) corresponds to the specified string
	 */
	default boolean modelVariant(String s) {
		return s.equals(modelVariant());
	}

	@Deprecated
	/**
	 * Use {@code modelVariant} instead.
	 * 
	 * @param s
	 *            a string representing the name of a model variant
	 * @return {@code true} iff the model (variant) corresponds to the specified string
	 */
	default boolean isModel(String s) {
		return modelVariant(s);
	}

	/**
	 * Returns a stream of objects from class T, after converting each non-empty trimmed line of the specified file
	 * 
	 * @param filename
	 *            the name of a file
	 * @param f
	 *            a function mapping each line ({@code String}) into an object of class T
	 * @return a stream of objects from class T, after converting each non-empty trimmed line of the specified file
	 */
	default <T> Stream<T> readFileLines(String filename, Function<String, T> f) {
		try {
			return Files.lines(Paths.get(filename)).map(s -> s.trim()).filter(s -> s.length() > 0).map(s -> f.apply(s));
		} catch (IOException e) {
			System.out.println("Problem with file " + filename + " (or the specified function)");
			System.exit(1);
			return null;
		}
	}

	/**
	 * Returns a stream composed of the non-empty trimmed lines ({@code String}) of the specified file
	 * 
	 * @param filename
	 *            the name of a file
	 * @return a stream composed of the non-empty trimmed lines ({@code String}) of the specified file
	 */
	default Stream<String> readFileLines(String filename) {
		return readFileLines(filename, s -> s);
	}
}
