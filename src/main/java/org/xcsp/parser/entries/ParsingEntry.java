package org.xcsp.parser.entries;

import static org.xcsp.common.Utilities.safeLong;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xcsp.common.IVar;
import org.xcsp.common.Types;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeVar;
import org.xcsp.common.Utilities;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XVariables.XVar;

/** The class root of any entry in variables, constraints and objectives. The basic attributes id, class and note are managed here. */
public abstract class ParsingEntry {
	/** The id (unique identifier) of the entry. */
	public String id;

	/** The classes associated with the entry. */
	public TypeClass[] classes;

	/** The note (short comment) associated with the entry. */
	public String note;

	/**
	 * The attributes that are associated with the element. Useful for storing all attributes by a simple copy. It is mainly used when dealing with
	 * special parameters of constraints (startIndex, circular, ...).
	 */
	public final Map<TypeAtt, String> attributes = new HashMap<>();

	/** The flags associated with the entry. Currently, used only for table constraints. */
	public final Set<TypeFlag> flags = new HashSet<>();

	/** Returns the Boolean value of the specified attribute, if it exists, the specified default value otherwise. */
	public final boolean getAttributeValue(TypeAtt att, boolean defaultValue) {
		return attributes.get(att) == null ? defaultValue : attributes.get(att).toLowerCase().equals("true");
	}

	/** Returns the int value of the specified attribute, if it exists, the specified default value otherwise. */
	public final int getAttributeValue(TypeAtt att, int defaultValue) {
		return attributes.get(att) == null ? defaultValue : Utilities.safeLong2Int(safeLong(attributes.get(att)), true);
	}

	/** Returns the value of the specified attribute, if it exists, the specified default value otherwise. */
	public final <T extends Enum<T>> T getAttributeValue(TypeAtt att, Class<T> clazz, T defaultValue) {
		return attributes.get(att) == null ? defaultValue : Types.valueOf(clazz, attributes.get(att));
	}

	/** Collect the XMl attributes of the specified element into a map (using an enum type for keys, and String for values). */
	public void copyAttributesOf(Element elt) {
		NamedNodeMap al = elt.getAttributes();
		IntStream.range(0, al.getLength()).forEach(i -> attributes.put(TypeAtt.valOf(al.item(i).getNodeName()), al.item(i).getNodeValue()));
		if (id == null && attributes.containsKey(TypeAtt.id))
			id = attributes.get(TypeAtt.id);
		if (attributes.containsKey(TypeAtt.CLASS))
			classes = TypeClass.classesFor(attributes.get(TypeAtt.CLASS).split("\\s+"));
		if (attributes.containsKey(TypeAtt.note))
			note = attributes.get(TypeAtt.note);
	}

	protected ParsingEntry() {}

	protected ParsingEntry(String id) {
		this.id = id;
		Utilities.control(id.matches("[a-zA-Z][_a-zA-Z0-9\\[\\]]*"), "Badly formed id : " + id + ". This does not match [a-zA-Z][_a-zA-Z0-9\\\\[\\\\]]* \n");
	}

	/** The root class used for Var and Array objects. */
	public static abstract class VEntry extends ParsingEntry {
		/** The type of the entry. */
		public final TypeVar type;

		/** Returns the type of the entry. We need an accessor for Scala. */
		public final TypeVar getType() {
			return type;
		}

		/** Builds an entry with the specified id and type. */
		protected VEntry(String id, TypeVar type) {
			super(id);
			this.type = type;
		}

		@Override
		public String toString() {
			return id + ":" + type;
		}
	}

	/**
	 * The root class of any element that is a (direct or indirect) entry in <constraints>. Also used for child elements of constraints (and
	 * constraint templates).
	 */
	public static abstract class CEntry extends ParsingEntry {

		/** The set of variables involved in this element. This is used as a cache (lazy initialization, as seen in method vars()). */
		private XVar[] vars;

		/** Returns the set of variables involved in this element. */
		public XVar[] vars() {
			if (this instanceof XCtr && ((XCtr) this).abstraction != null)
				return collectVars(new LinkedHashSet<>()).toArray(new XVar[0]);
			return vars != null ? vars : (vars = collectVars(new LinkedHashSet<>()).toArray(new XVar[0]));
		}

		/** Collect the set of variables involved in this element, and add them to the specified set. */
		public abstract LinkedHashSet<XVar> collectVars(LinkedHashSet<XVar> set);

		/** Returns true iff this element is subject to abstraction, i.e., contains parameters (tokens of the form %i or %...). */
		public abstract boolean subjectToAbstraction();

		@Override
		public String toString() {
			return "(" + (attributes == null ? "" : Utilities.join(attributes, ":", " ")) + ")";
		}
	}

	/** The root class for representing objectives. */
	public static abstract class OEntry extends ParsingEntry {

		/** Indicates whether the objective must be minimized or maximized. */
		public final boolean minimize;

		/** The type (expression, sum, minimum, ...) of the objective. */
		public final TypeObjective type;

		public abstract IVar[] vars();

		/** Returns The type (expression, sum, minimum, ...) of the objective. We need an accessor for Scala. */
		public final TypeObjective getType() {
			return type;
		}

		/** Builds an objective with the specified minimize value and type. */
		public OEntry(boolean minimize, TypeObjective type) {
			this.minimize = minimize;
			this.type = type;
		}

		@Override
		public String toString() {
			return id + " " + (minimize ? "minimize" : "maximize") + " " + type;
		}
	}

}