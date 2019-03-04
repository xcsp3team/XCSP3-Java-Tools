/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.modeler;

import static org.xcsp.common.Constants.ANNOTATIONS;
import static org.xcsp.common.Constants.ARGS;
import static org.xcsp.common.Constants.ARRAY;
import static org.xcsp.common.Constants.BLOCK;
import static org.xcsp.common.Constants.CONSTRAINTS;
import static org.xcsp.common.Constants.DECISION;
import static org.xcsp.common.Constants.DOMAIN;
import static org.xcsp.common.Constants.GROUP;
import static org.xcsp.common.Constants.INSTANCE;
import static org.xcsp.common.Constants.OBJECTIVES;
import static org.xcsp.common.Constants.VAR;
import static org.xcsp.common.Constants.VARIABLES;
import static org.xcsp.common.Utilities.element;
import static org.xcsp.modeler.definitions.ICtr.CONDITION;
import static org.xcsp.modeler.definitions.ICtr.EXTENSION;
import static org.xcsp.modeler.definitions.ICtr.FUNCTION;
import static org.xcsp.modeler.definitions.ICtr.INDEX;
import static org.xcsp.modeler.definitions.ICtr.INTENSION;
import static org.xcsp.modeler.definitions.ICtr.LIST;
import static org.xcsp.modeler.definitions.ICtr.SLIDE;
import static org.xcsp.modeler.definitions.ICtr.VALUE;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xcsp.common.Condition;
import org.xcsp.common.IVar;
import org.xcsp.common.Softening;
import org.xcsp.common.Softening.SofteningGlobal;
import org.xcsp.common.Softening.SofteningSimple;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Types.TypeVar;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Values.IntegerInterval;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.definitions.DefXCSP;
import org.xcsp.modeler.definitions.DefXCSP.Son;
import org.xcsp.modeler.definitions.ICtr;
import org.xcsp.modeler.definitions.ICtr.ICtrExtension;
import org.xcsp.modeler.definitions.ICtr.ICtrIntension;
import org.xcsp.modeler.definitions.ICtr.ICtrMdd;
import org.xcsp.modeler.definitions.ICtr.ICtrRegular;
import org.xcsp.modeler.definitions.ICtr.ICtrSlide;
import org.xcsp.modeler.definitions.ICtr.ICtrSmart;
import org.xcsp.modeler.definitions.ICtr.Meta;
import org.xcsp.modeler.entities.CtrEntities.CtrAlone;
import org.xcsp.modeler.entities.CtrEntities.CtrArray;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;
import org.xcsp.modeler.entities.ModelingEntity;
import org.xcsp.modeler.entities.ModelingEntity.TagDummy;
import org.xcsp.modeler.entities.ObjEntities.ObjEntity;
import org.xcsp.modeler.entities.VarEntities.VarAlone;
import org.xcsp.modeler.entities.VarEntities.VarArray;
import org.xcsp.modeler.entities.VarEntities.VarEntity;
import org.xcsp.modeler.implementation.ProblemIMP;
import org.xcsp.modeler.implementation.ProblemIMP3;
import org.xcsp.modeler.implementation.ProblemIMP3.MVariable;
import org.xcsp.modeler.problems.AllInterval;
import org.xcsp.modeler.problems.Bibd;

public class Compiler {

	/**********************************************************************************************
	 * Constants
	 *********************************************************************************************/

	public static final String FORMAT = TypeAtt.format.name();
	public static final String XCSP3 = "XCSP3";
	public static final String TYPE = TypeAtt.type.name();
	public static final String ID = TypeAtt.id.name();
	public static final String CLASS = TypeAtt.CLASS.name().toLowerCase();
	public static final String NOTE = TypeAtt.note.name();
	public static final String AS = TypeAtt.as.name();
	public static final String FOR = TypeAtt.FOR.name().toLowerCase();
	public static final String CIRCULAR = TypeAtt.circular.name();
	public static final String OFFSET = TypeAtt.offset.name();
	public static final String COLLECT = TypeAtt.collect.name();
	public static final String VIOLATION_COST = TypeAtt.violationCost.name();
	public static final String VIOLATION_MEASURE = TypeAtt.violationMeasure.name();

	public static final String SUPPORTS = TypeChild.supports.name();
	public static final String CONFLICTS = TypeChild.conflicts.name();

	public static final String VAR_ARGS = "%...";
	public static final int LIMIT_FOR_VAR_ARGS = 3;

	public static final String VARIANT = "-variant";
	public static final String DATA = "-data";
	public static final String DATA_FORMAT = "-dataFormat";
	public static final String DATA_SAVING = "-dataSaving";
	public static final String OUTPUT = "-output";
	public static final String EV = "-ev";
	public static final String IC = "-ic";

	/**********************************************************************************************
	 * Fields and Constructor
	 *********************************************************************************************/

	protected final ProblemIMP imp;
	protected Document doc;

	protected Map<String, Element> tuplesReferents = new HashMap<>();
	protected int nBuiltTuplesReferents;

	// HARD CODING/VALUES BELOW
	protected int limitForUsingAs = 12;
	protected boolean discardIntegerType = true, discardAsRelation = true, printNotes = true;
	protected boolean doubleAbstraction = true, saveImmediatelyStored = true, ignoreAutomaticGroups = true, monoformGroups = false;
	private boolean noGroupAtAllForExtension = false, noGroupAtAllForIntension = false, noGroupAtAllForGlobal = false;
	private boolean uncompactDomainFor = false;
	private boolean mustEraseIdsOfConstraints = false;
	// sometimes, for efficiency reasons, it is important to set noGroupAtAllForExtension to true and uncompactDomainFor to true

	/**
	 * Builds an object that allow us to generate XCSP3 instances from the specified MCSP3 model. Data are expected to be provided at the command
	 * line.
	 * 
	 * @param api
	 *            the object denoting the model of the problem
	 */
	public Compiler(ProblemAPI api) {
		this.imp = api.imp();
	}

	protected Document buildDocument() {
		// TODO control that ids are all different
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = element(doc, INSTANCE, FORMAT, XCSP3, TYPE, imp.objEntities.active() ? TypeFramework.COP.name() : imp.typeFramework().name());
		root.appendChild(variables());
		root.appendChild(constraints());
		if (imp.objEntities.active())
			root.appendChild(objectives());
		if (imp.annotations.active())
			root.appendChild(annotations());
		doc.appendChild(root);
		doc.normalize();
		return doc;
	}

	/**********************************************************************************************
	 * Managing (groups of) predicates, relations and globals
	 *********************************************************************************************/

	private List<Predicate> storedP = new ArrayList<>();
	private List<Relation> storedR = new ArrayList<>();
	private List<Global> storedG = new ArrayList<>();

	private void saveStored(Element parent, boolean immediatly, boolean br, boolean bp, boolean bg) {
		if (!immediatly)
			return;
		if (br && storedR.size() > 0)
			parent.appendChild(buildingStoredRelations());
		if (bp && storedP.size() > 0)
			parent.appendChild(buildingStoredPredicates());
		if (bg && storedG.size() > 0)
			parent.appendChild(buildingStoredGlobals());
	}

	private void saveStored(Element parent) {
		saveStored(parent, true, true, true, true);
	}

	private abstract class Similarable<T> {
		protected abstract boolean isSimilarTo(T object);

		protected boolean haveSimilarAttributes(ICtr c1, ICtr c2) {
			CtrAlone ca1 = imp.ctrEntities.ctrToCtrAlone.get(c1), ca2 = imp.ctrEntities.ctrToCtrAlone.get(c2);
			if (ca1.id != null || ca2.id != null)
				return false;
			if (!TypeClass.equivalent(ca1.classes, ca2.classes))
				return false;
			if ((ca1.note == null) != (ca2.note == null) || (ca1.note != null && !ca1.note.equals(ca2.note)))
				return false;
			if ((ca1.softening == null) != (ca2.softening == null))
				return false;
			if (ca1.softening != null) { // and necessarily ca2.softening != null too
				if (ca1.softening.getClass() != ca2.softening.getClass())
					return false;
				if (ca1.softening.cost != null || ca2.softening.cost != null)
					return false; // relaxed constraints are considered as being not similar (do not see how it could be different)
				// we have to check cost functions now
				if (ca1.softening instanceof SofteningSimple) {
					if (((SofteningSimple) ca1.softening).violationCost != ((SofteningSimple) ca2.softening).violationCost)
						return false;
				} else if (ca1.softening instanceof SofteningGlobal) {
					if (((SofteningGlobal) ca1.softening).type != ((SofteningGlobal) ca2.softening).type)
						return false;
					if (((SofteningGlobal) ca1.softening).parameters != null || ((SofteningGlobal) ca2.softening).parameters != null)
						return false;
				} else
					return false;
			}
			return true;
		}
	}

	/**
	 * A class used to handle constraints {@code intension}, with the objective of building groups of similar intension constraints
	 */
	private class Predicate extends Similarable<Predicate> {
		private ICtrIntension c;
		private XNodeParent<?> abstractTree;
		private List<Object> args = new ArrayList<>();

		public Predicate(ICtrIntension c, boolean abstractIntegers, boolean multiOccurrences) {
			this.c = c;
			this.abstractTree = (XNodeParent<?>) ((XNodeParent<?>) c.mapXCSP().get(FUNCTION)).abstraction(args, abstractIntegers, multiOccurrences);
		}

		private Predicate(ICtrIntension c) {
			this(c, true, true);
		}

		@Override
		protected boolean isSimilarTo(Predicate p) {
			return haveSimilarAttributes(c, p.c) && abstractTree.equals(p.abstractTree);
		}
	}

	/**
	 * A class used to handle constraints {@code extension}, with the objective of building groups of similar extension constraints
	 */
	private class Relation extends Similarable<Relation> {
		private ICtrExtension c;

		private Relation(ICtrExtension c) {
			this.c = c;
		}

		@Override
		protected boolean isSimilarTo(Relation r) {
			return haveSimilarAttributes(c, r.c) && c.isSimilarTo(r.c);
		}
	}

	/**
	 * A class used to handle global constraints (i.e., constraints that are neither {@code intension} nor {@code extension}), with the objective of
	 * building groups of similar global constraints
	 */
	private class Global extends Similarable<Global> {
		private ICtr c;
		private DefXCSP def;

		private int[] recordedDiffs, recordedSizes;

		private Global(ICtr c) {
			this.c = c;
			this.def = c.defXCSP();
		}

		@Override
		protected boolean isSimilarTo(Global g) {
			Function<Object, Integer> sizeOf = v -> v instanceof Number || v instanceof IntegerInterval || v instanceof Condition ? 1
					: Stream.of((v.toString()).trim().split("\\s+"))
							.mapToInt(tok -> Utilities.isNumeric(tok) || Utilities.isNumericInterval(tok) ? 1 : imp.varEntities.nVarsIn(tok)).sum();

			if (def.map.containsKey(ICtr.MATRIX))
				return false; // currently, forbidden to group together constraints with child MATRIX
			if (!haveSimilarAttributes(c, g.c))
				return false;
			int[] diffs = def == null || g.def == null ? null : def.differencesWith(g.def);
			if (diffs == null)
				return false;
			if (diffs.length == 0) {
				System.out.println("WARNING : Two similar constraints");
				return false; // The constraints are identical; we return false to keep both of them (may happen with some awkward instances)
			}
			if (diffs.length == 1) {
				if (def.sons.get(diffs[0]).name.equals(CONDITION)) // for the moment, problem when abstracting on conditions
					return false;
				if (storedG.size() == 1) {
					recordedDiffs = diffs;
					int s1 = sizeOf.apply(def.sons.get(diffs[0]).content), s2 = sizeOf.apply(g.def.sons.get(diffs[0]).content);
					recordedSizes = new int[] { (s1 == s2 ? s1 : -1) };
					return true;
				}
				if (recordedSizes[0] != -1 && recordedSizes[0] != sizeOf.apply(g.def.sons.get(diffs[0]).content))
					recordedSizes[0] = -1;
				return recordedDiffs.length == 1 && recordedDiffs[0] == diffs[0];
			}
			if (doubleAbstraction && diffs.length == 2 && def.sons.size() > 2 && !(c instanceof ICtrRegular) && !(c instanceof ICtrMdd)) {
				if (IntStream.of(diffs).anyMatch(i -> def.sons.get(i).name.equals(CONDITION)))
					return false; // for the moment, the parser does not manage abstraction of condition elements
				if (storedG.size() == 1) {
					int[] s1 = IntStream.of(diffs).map(i -> sizeOf.apply(def.sons.get(i).content)).toArray();
					int[] s2 = IntStream.of(diffs).map(i -> sizeOf.apply(g.def.sons.get(i).content)).toArray();
					if (IntStream.range(0, diffs.length).allMatch(i -> s1[i] == s2[i])) {
						recordedDiffs = diffs;
						recordedSizes = s1;
						return true;
					}
					return false;
				}
				if (recordedDiffs.length != 2 || recordedDiffs[0] != diffs[0] || recordedDiffs[1] != diffs[1])
					return false;
				int[] s2 = IntStream.of(diffs).map(i -> sizeOf.apply(g.def.sons.get(i).content)).toArray();
				return IntStream.range(0, diffs.length).allMatch(i -> recordedSizes[i] == s2[i]);
			}
			return false; // for the moment, only 1 or 2 differences are managed
		}

		@Override
		public String toString() {
			return def.toString();
		}
	}

	/**********************************************************************************************
	 * Auxiliary Functions
	 *********************************************************************************************/

	private String seqOfParameters(int n, int start, boolean compact) {
		return compact && n > LIMIT_FOR_VAR_ARGS ? VAR_ARGS : IntStream.range(0, n).mapToObj(i -> "%" + (start + i)).collect(Collectors.joining(" "));
	}

	private String seqOfParameters(int n, boolean compact) {
		return seqOfParameters(n, 0, compact);
	}

	private String seqOfParameters(int n) {
		return seqOfParameters(n, false);
	}

	private void sideAttributes(Element element, ModelingEntity entity) {
		if (entity == null)
			return;
		if (entity.id != null)
			element.setAttribute(ID, entity.id);
		if (entity.classes.size() > 0)
			element.setAttribute(CLASS, entity.classes.stream().map(c -> c.ccname()).collect(Collectors.joining(" ")));
		if (printNotes && entity.note != null && entity.note.length() > 0)
			element.setAttribute(NOTE, entity.note);
		if (entity instanceof CtrAlone) {
			Softening sf = ((CtrAlone) entity).softening;
			if (sf != null) {
				Utilities.control(sf.cost == null, "Cannot be managed at this place");
				if (sf instanceof SofteningSimple)
					element.setAttribute(VIOLATION_COST, ((SofteningSimple) sf).violationCost + "");
				else if (sf instanceof SofteningGlobal)
					element.setAttribute(VIOLATION_MEASURE, ((SofteningGlobal) sf).type.toString());
				else
					Utilities.control(false, "Unreachable");
			}
		}
	}

	private Element treatPossibleRecursiveSon(Son son, int sonIndex, int absIndex1, int absIndex2) {
		if (son.name.equals(ICtr.REC)) { // recursivity
			Utilities.control(absIndex1 != sonIndex && absIndex2 != sonIndex && son.content instanceof CtrAlone, "Pb");
			CtrAlone ca = (CtrAlone) son.content;
			Element sub = buildingDef(ca.ctr.defXCSP());
			sideAttributes(sub, ca);
			return sub;
		}
		return null;
	}

	private Element buildingDef(DefXCSP def, int absIndex1, String absValue1, int absIndex2, String absValue2) {
		Element elt = doc.createElement(def.name);
		def.attributes.stream().forEach(a -> elt.setAttribute(a.getKey(), a.getValue().toString()));
		if (def.sons.size() == 1 && def.sons.get(0).attributes.size() == 0 && def.possibleSimplification) {
			Element recursiveSon = treatPossibleRecursiveSon(def.sons.get(0), 0, absIndex1, absIndex2);
			if (recursiveSon != null)
				elt.appendChild(recursiveSon);
			else
				elt.setTextContent(" " + (absIndex1 == 0 ? absValue1 : def.sons.get(0).content) + " ");
		} else
			for (int i = 0; i < def.sons.size(); i++) {
				Element recursiveSon = treatPossibleRecursiveSon(def.sons.get(i), i, absIndex1, absIndex2);
				if (recursiveSon != null)
					elt.appendChild(recursiveSon);
				else {
					Element sub = element(doc, def.sons.get(i).name, i == absIndex1 ? absValue1 : i == absIndex2 ? absValue2 : def.sons.get(i).content);
					def.sons.get(i).attributes.stream().forEach(a -> sub.setAttribute(a.getKey(), a.getValue().toString()));
					elt.appendChild(sub);
				}
			}
		return elt;
	}

	private Element buildingDef(DefXCSP def, int absIndex, String absValue) {
		return buildingDef(def, absIndex, absValue, -1, "");
	}

	private Element buildingDef(DefXCSP def) {
		return buildingDef(def, -1, "", -1, "");
	}

	/**********************************************************************************************
	 * Managing Variables
	 *********************************************************************************************/

	private Element baseVarEntity(Element element, VarEntity va) {
		sideAttributes(element, va);
		if (va instanceof VarArray)
			element.setAttribute(ICtr.SIZE, VarArray.class.cast(va).getStringSize());
		if (!discardIntegerType || va.getType() != TypeVar.integer)
			element.setAttribute(TYPE, va.getType().name());
		return element;
	}

	private Element var(VarAlone va, String s, boolean alias) {
		return baseVarEntity(alias ? element(doc, VAR, AS, s) : element(doc, VAR, s), va);
	}

	private Element array(VarArray va, String s, boolean alias) {
		return baseVarEntity(alias ? element(doc, ARRAY, AS, s) : element(doc, ARRAY, s), va);
	}

	private Element array(VarArray va, Map<IVar, String> varToDomText, Map<Object, List<IVar>> map) {
		Utilities.control(map.size() > 1, "The map only contains one entry");
		Element element = baseVarEntity(doc.createElement(ARRAY), va);
		for (List<IVar> list : map.values()) {
			String s = uncompactDomainFor ? list.stream().map(x -> x.id()).collect(Collectors.joining(" "))
					: imp.varEntities.compact(list.toArray(new IVar[list.size()]));
			element.appendChild(element(doc, DOMAIN, FOR, s, varToDomText.get(list.get(0))));
		}
		return element;
	}

	protected void putInMap(IVar x, Map<IVar, String> map) {
		map.put(x, ((MVariable) x).dom.toString());
	}

	protected Element variables() {
		System.out.println("  Saving variables");
		Element element = doc.createElement(VARIABLES);
		Map<IVar, String> varToDom = new HashMap<>();
		for (VarEntity ve : imp.varEntities.allEntities)
			if (ve instanceof VarAlone)
				putInMap(((VarAlone) ve).var, varToDom);
			else
				for (IVar x : ((VarArray) ve).flatVars)
					putInMap(x, varToDom);
		Map<String, String> domToVarReferent = new HashMap<>();
		for (VarEntity ve : imp.varEntities.allEntities) {
			if (ve instanceof VarAlone) {
				VarAlone va = (VarAlone) ve;
				// Utilities.control(problem.varEntities.varToVarArray.get(va.var) == null, "");
				if (imp.varEntities.varToVarArray.get(va.var) != null) // necessary for xcsp2
					continue;
				String dom = varToDom.get(va.var);
				if (domToVarReferent.get(dom) == null) {
					element.appendChild(var(va, dom, false));
					domToVarReferent.put(dom, va.id);
				} else if (dom.length() < limitForUsingAs)
					element.appendChild(var(va, dom, false));
				else
					element.appendChild(var(va, domToVarReferent.get(dom), true));
			} else {
				VarArray va = (VarArray) ve;
				Map<Object, List<IVar>> map = Stream.of(va.flatVars)
						.collect(Collectors.groupingBy(x -> varToDom.get(x), LinkedHashMap::new, Collectors.toList()));
				if (map.size() == 1) {
					String dom = varToDom.get(va.flatVars[0]);
					if (domToVarReferent.get(dom) == null) {
						element.appendChild(array(va, dom, false));
						domToVarReferent.put(dom, va.id);
					} else if (dom.length() < limitForUsingAs)
						element.appendChild(array(va, dom, false));
					else
						element.appendChild(array(va, domToVarReferent.get(dom), true));
				} else
					element.appendChild(array(va, varToDom, map));
			}
		}
		return element;
	}

	/**********************************************************************************************
	 * Managing Constraints
	 *********************************************************************************************/

	private <T extends Similarable<T>> List<Element> buildChilds(T[] t, List<T> store, Supplier<Element> spl) {
		List<Element> childs = new ArrayList<>();
		if (t[0] instanceof Predicate && noGroupAtAllForIntension || t[0] instanceof Relation && noGroupAtAllForExtension
				|| t[0] instanceof Global && noGroupAtAllForGlobal) {
			for (int i = 0; i < t.length; i++) {
				store.clear();
				store.add(t[i]);
				childs.add(spl.get());
			}
		} else if (monoformGroups) {
			store.add(t[0]);
			boolean similar = t[0].isSimilarTo(t[1]); // to record diffs
			Utilities.control(similar, "Should be similar");
			assert Stream.of(t).allMatch(p -> p.isSimilarTo(t[0]));
			IntStream.range(1, t.length).forEach(i -> store.add(t[i]));
			childs.add(spl.get());
		} else {
			boolean[] flags = new boolean[t.length];
			for (int i = 0; i < t.length; i++) {
				if (flags[i] || t[i] == null)
					continue;
				store.clear();
				store.add(t[i]);
				for (int j = i + 1; j < t.length; j++) {
					if (!flags[j] && t[i].isSimilarTo(t[j])) {
						store.add(t[j]);
						flags[j] = true;
					}
				}
				childs.add(spl.get());
			}
		}
		return childs;
	}

	private Element buildingTuples(ICtrExtension c) {
		Object tuples = c.mapXCSP().get(ICtr.TUPLES);
		String key = tuples instanceof int[][] ? ICtrExtension.tableAsString((int[][]) tuples) : ICtrExtension.tableAsString((String[][]) tuples);
		Element eltTgt = tuplesReferents.get(key), eltSrc = null;
		if (eltTgt != null && !discardAsRelation) {
			if (eltTgt.getAttribute(ID).length() == 0) {
				eltTgt.setAttribute(ID, "i" + nBuiltTuplesReferents); // we add a useful missing id
				nBuiltTuplesReferents++;
			}
			eltSrc = element(doc, (Boolean) c.mapXCSP().get(ICtr.POSITIVE) ? SUPPORTS : CONFLICTS, AS, eltTgt.getAttribute(ID));
		} else {
			eltSrc = element(doc, (Boolean) c.mapXCSP().get(ICtr.POSITIVE) ? SUPPORTS : CONFLICTS, key);
			tuplesReferents.put(key, eltSrc);
		}
		return eltSrc;
	}

	private Element buildingStoredRelations() {
		Relation r = storedR.get(0); // first relation
		Element lst = element(doc, LIST, storedR.size() == 1 ? r.c.mapXCSP().get(LIST) : seqOfParameters((Integer) r.c.mapXCSP().get(ICtr.ARITY), true));
		Element ext = element(doc, EXTENSION, lst, buildingTuples(r.c));
		Element elt = storedR.size() == 1 ? ext : element(doc, GROUP, ext, storedR.stream().map(rr -> element(doc, ARGS, rr.c.mapXCSP().get(LIST))));
		sideAttributes(elt, imp.ctrEntities.ctrToCtrAlone.get(r.c));
		// sideAttributes(elt, storedR.size() == 1 ? loader.ctrEntities.ctrToCtrAlone.get(r.c) :loader.ctrEntities?ctrToCtrArray.get(r.c));
		storedR.clear();
		return elt;
	}

	private List<Element> handleListOfExtension(Element parent, List<ICtr> ctrs) {
		saveStored(parent, true, true, false, false);
		return buildChilds(ctrs.stream().map(c -> new Relation((ICtrExtension) c)).toArray(Relation[]::new), storedR, () -> buildingStoredRelations());
	}

	private Element buildingStoredPredicates() {
		Predicate firstPredicate = storedP.get(0); // first predicate
		Utilities.control(storedP.stream().allMatch(p -> p.args.size() == firstPredicate.args.size()), "Not the same size");
		if (storedP.size() > 1) {
			Object[] similar = IntStream.range(0, firstPredicate.args.size())
					.mapToObj(i -> storedP.stream().allMatch(p -> p.args.get(i).equals(firstPredicate.args.get(i))) ? firstPredicate.args.get(i) : null)
					.toArray();
			if (Stream.of(similar).anyMatch(obj -> obj != null)) {
				// we reduce lists of arguments
				for (int i = similar.length - 1; i >= 0; i--)
					if (similar[i] != null)
						for (Predicate p : storedP)
							p.args.remove(similar[i]);
				// we modify the abstract tree
				firstPredicate.abstractTree = (XNodeParent<?>) firstPredicate.abstractTree.replacePartiallyParameters(similar);
			}
		}
		Element itn = element(doc, INTENSION, storedP.size() == 1 ? firstPredicate.c.mapXCSP().get(ICtr.FUNCTION) : firstPredicate.abstractTree);
		Element elt = null;
		if (storedP.size() == 1)
			elt = itn;
		else {
			elt = element(doc, GROUP, itn);
			for (Predicate p : storedP) {
				String s = p.args.stream().allMatch(x -> x instanceof IVar)
						? imp.varEntities.compactOrdered(p.args.stream().map(x -> (IVar) x).toArray(IVar[]::new))
						: Utilities.join(p.args);
				elt.appendChild(element(doc, ARGS, s));
			}
		}
		// Element elt = storedP.size() == 1 ? itn : element(doc, GROUP, itn, storedP.stream().map(pp -> element(doc, ARGS,Utilities.join(pp.args))));

		sideAttributes(elt, imp.ctrEntities.ctrToCtrAlone.get(firstPredicate.c));
		storedP.clear();
		return elt;
	}

	private List<Element> handleListOfIntension(Element parent, List<ICtr> ctrs) {
		saveStored(parent, true, false, true, false);
		return buildChilds(ctrs.stream().map(c -> new Predicate((ICtrIntension) c)).toArray(Predicate[]::new), storedP, () -> buildingStoredPredicates());
	}

	private Element buildingStoredGlobals() {
		Element elt = null;
		Global g = storedG.get(0);
		if (storedG.size() == 1)
			elt = buildingDef(g.def);
		else {
			Utilities.control(g.recordedDiffs.length == 1 || g.recordedDiffs.length == 2, "");
			int i = g.recordedDiffs[0];
			if (g.recordedDiffs.length == 1) {
				String name = g.def.sons.get(i).name;
				Element gbl = buildingDef(g.def, i, name.equals(INDEX) || name.equals(VALUE) || name.equals(CONDITION) ? "%0"
						: g.recordedSizes[0] == -1 ? VAR_ARGS : seqOfParameters(g.recordedSizes[0], true)); // VAR_ARGS);
				// TODO other cases with %0 ?
				elt = element(doc, GROUP, gbl, storedG.stream().map(gg -> element(doc, ARGS, gg.def.sons.get(i).content)));
			} else {
				int j = g.recordedDiffs[1];
				Element gbl = buildingDef(g.def, i, seqOfParameters(g.recordedSizes[0]), j, seqOfParameters(g.recordedSizes[1], g.recordedSizes[0], true));
				elt = element(doc, GROUP, gbl, storedG.stream().map(gg -> element(doc, ARGS, gg.def.sons.get(i).content + " " + gg.def.sons.get(j).content)));
			}
		}
		sideAttributes(elt, imp.ctrEntities.ctrToCtrAlone.get(g.c));
		storedG.clear();
		return elt;
	}

	private List<Element> handleListOfGlobal(Element parent, List<ICtr> ctrs) {
		saveStored(parent, true, false, false, true);
		return buildChilds(ctrs.stream().map(c -> new Global(c)).toArray(Global[]::new), storedG, () -> buildingStoredGlobals());
	}

	private Element buildSlide(ICtrSlide ctr) {
		Element elt = doc.createElement(SLIDE);
		Map<String, Object> map = ctr.mapXCSP();
		if (map.containsKey(CIRCULAR) && (Boolean) map.get(CIRCULAR))
			elt.setAttribute(CIRCULAR, "true");
		IVar[][] lists = (IVar[][]) map.get(ICtr.LISTS);
		int[] offsets = (int[]) map.get(ICtr.OFFSETS), collects = (int[]) map.get(ICtr.COLLECTS);
		for (int i = 0; i < lists.length; i++) {
			Element subelement = element(doc, LIST, imp.varEntities.compactOrdered(lists[i]));
			if (lists.length > 1 && collects[i] != 1)
				subelement.setAttribute(COLLECT, collects[i] + "");
			if (offsets[i] != 1)
				subelement.setAttribute(OFFSET, offsets[i] + "");
			elt.appendChild(subelement);
		}
		CtrAlone[] cas = (CtrAlone[]) map.get(ICtr.ALONES);
		ICtr c0 = cas[0].ctr;
		Utilities.control(Stream.of(cas).noneMatch(ca -> ca.ctr instanceof ICtrSlide), "Slide cannot appear in slide");
		if (c0 instanceof ICtrIntension)
			elt.appendChild(element(doc, INTENSION, new Predicate((ICtrIntension) c0, false, true).abstractTree));
		else if (c0 instanceof ICtrExtension && !(c0 instanceof ICtrMdd) && !(c0 instanceof ICtrSmart))
			elt.appendChild(element(doc, EXTENSION, element(doc, LIST, seqOfParameters(c0.scope().length)), buildingTuples((ICtrExtension) c0)));
		else {
			Global g0 = new Global(cas[0].ctr), g1 = new Global(cas[1].ctr);
			int[] diffs = g0.def.differencesWith(g1.def);
			Utilities.control(diffs.length == 1, "Bad form of slide");
			int nb = imp.varEntities.nVarsIn(g0.def.sons.get(diffs[0]).content.toString());
			elt.appendChild(buildingDef(g0.def, diffs[0], seqOfParameters(nb, true)));
		}
		sideAttributes(elt, imp.ctrEntities.ctrToCtrAlone.get(ctr));
		return elt;
	}

	private Element buildMeta(ICtr ctr) {
		Element elt = buildingDef(ctr.defXCSP());
		sideAttributes(elt, imp.ctrEntities.ctrToCtrAlone.get(ctr));
		return elt;
	}

	protected void handleCtr(Element parent, ICtr c) {
		if (c instanceof ICtrSlide) {
			saveStored(parent, saveImmediatelyStored, true, true, true);
			parent.appendChild(buildSlide((ICtrSlide) c));
		} else if (c instanceof Meta) {
			saveStored(parent, saveImmediatelyStored, true, true, true);
			parent.appendChild(buildMeta(c));
		} else if (c instanceof ICtrIntension) {
			saveStored(parent, saveImmediatelyStored, true, false, true);
			Predicate p = new Predicate((ICtrIntension) c);
			saveStored(parent, storedP.size() > 0 && (!storedP.get(0).isSimilarTo(p) || ignoreAutomaticGroups), false, true, false);
			storedP.add(p);
		} else if (c instanceof ICtrExtension && !(c instanceof ICtrMdd) && !(c instanceof ICtrSmart)) {
			saveStored(parent, saveImmediatelyStored, false, true, true);
			Relation r = new Relation((ICtrExtension) c);
			saveStored(parent, storedR.size() > 0 && (!storedR.get(0).isSimilarTo(r) || ignoreAutomaticGroups), true, false, false);
			storedR.add(r);
		} else {
			saveStored(parent, saveImmediatelyStored, true, true, false);
			Global g = new Global(c);
			saveStored(parent, storedG.size() > 0 && (!storedG.get(0).isSimilarTo(g) || ignoreAutomaticGroups), false, false, true);
			storedG.add(g);
		}
	}

	protected void setSpecificFrameworkAttributes(Element rootOfConstraints) {
		if (imp.typeFramework() == TypeFramework.WCSP) {
			// lb (lower bound) and ub (upper bound) to be managed ; TODO
		}
	}

	protected List<Element> buildChilds(Element parent, List<ICtr> ctrs) {
		ICtr c0 = ctrs.get(0);
		if (c0 instanceof ICtrSlide)
			return ctrs.stream().map(c -> buildSlide((ICtrSlide) c)).collect(Collectors.toList());
		if (c0 instanceof Meta)
			return ctrs.stream().map(c -> buildMeta(c)).collect(Collectors.toList());
		if (c0 instanceof ICtrIntension)
			return handleListOfIntension(parent, ctrs);
		if (c0 instanceof ICtrExtension && !(c0 instanceof ICtrMdd) && !(c0 instanceof ICtrSmart))
			return handleListOfExtension(parent, ctrs);
		return handleListOfGlobal(parent, ctrs);
	}

	protected Element constraints() {
		System.out.println("  Saving constraints");
		Element root = doc.createElement(CONSTRAINTS);
		setSpecificFrameworkAttributes(root);
		Utilities.control(storedP.size() == 0 && storedR.size() == 0 && storedG.size() == 0, "Storing structures are not empty");
		Stack<Element> stackOfBlocks = new Stack<>();
		stackOfBlocks.push(root); // the initial element is seen as a root block here
		for (CtrEntity ce : imp.ctrEntities.allEntities) {
			if (ce instanceof TagDummy)
				continue;
			Element currParent = stackOfBlocks.peek();
			if (ce instanceof CtrArray) {
				CtrArray ctrArray = (CtrArray) ce;
				Map<Class<?>, List<ICtr>> map = Stream.of((ctrArray).ctrs).collect(Collectors.groupingBy(c -> c.getClass())); // repartition((ctrArray).ctrs);
				if (map.size() == 1) {
					saveStored(currParent, saveImmediatelyStored, true, true, true);
					List<Element> childs = buildChilds(currParent, map.values().iterator().next());
					// if ((ctrArray.nullBasicAttributes() || childs.get(0).getAttributes().getLength() == 0)) {
					if (ctrArray.nullBasicAttributes())
						childs.stream().forEach(c -> currParent.appendChild(c));
					else if (childs.size() == 1 && childs.get(0).getAttributes().getLength() == 0) {
						sideAttributes(childs.get(0), ctrArray);
						currParent.appendChild(childs.get(0));
					} else {
						Element block = doc.createElement(BLOCK);
						sideAttributes(block, ctrArray);
						childs.stream().forEach(c -> block.appendChild(c));
						currParent.appendChild(block);
					}
				} else {
					saveStored(currParent);
					if (ctrArray.nullBasicAttributes()) { // avoiding creating a block with no attached information
						for (List<ICtr> list : map.values())
							buildChilds(currParent, list).stream().forEach(c -> currParent.appendChild(c));
					} else {
						Element block = doc.createElement(BLOCK);
						sideAttributes(block, ctrArray);
						for (List<ICtr> list : map.values())
							buildChilds(block, list).stream().forEach(c -> block.appendChild(c));
						currParent.appendChild(block);
					}
				}
			} else {
				ICtr c = ((CtrAlone) ce).ctr;
				if (mustEraseIdsOfConstraints)
					imp.ctrEntities.ctrToCtrAlone.get(c).id = null;
				if (imp.ctrEntities.ctrToCtrArray.get(c) == null)
					handleCtr(currParent, c);
			}
		}
		assert stackOfBlocks.size() == 1 && stackOfBlocks.peek() == root;
		saveStored(root);
		return root;
	}

	/**********************************************************************************************
	 * Managing Objectives
	 *********************************************************************************************/

	protected Element objectives() {
		Element root = doc.createElement(OBJECTIVES); // root.setAttribute(OPTIMIZATION, LEXICO);
		for (ObjEntity obj : imp.objEntities.allEntities) {
			Element elt = buildingDef(obj.obj.defXCSP());
			sideAttributes(elt, obj);
			root.appendChild(elt);
		}
		return root;
	}

	/**********************************************************************************************
	 * Managing Annotations
	 *********************************************************************************************/

	protected Element annotations() {
		Element root = doc.createElement(ANNOTATIONS);
		if (imp.annotations.decision != null) {
			// Element vars = doc.createElement(VARIABLES);
			root.appendChild(Utilities.element(doc, DECISION, imp.varEntities.compactOrdered(imp.annotations.decision)));
		}
		return root;
	}

	/**********************************************************************************************
	 * Static Fields and Methods
	 *********************************************************************************************/

	public static boolean ev;

	private static ProblemAPI usage() {
		System.out.println("\nDescription.\n  Compiler is a class that can generate XCSP3 files. You need to provide");
		System.out.println("  an MCSP3 model (Java class implementing ProblemAPI) and some effective data.");
		System.out.println("\nUsage.\n  java " + Compiler.class.getName() + " <className> [<arguments>]\n");
		System.out.println("  <className> is the name of a Java class implementing " + ProblemAPI.class.getName());
		System.out.println("  <arguments> is a possibly empty whitespace-separated list of elements among:");
		System.out.println("    -data=... ");
		System.out.println("       where ... stands for the effective data. This can be the name of a JSON");
		System.out.println("       file, a stand-alone value v or a list of values [v1,v2,...,vp]");
		System.out.println("    -dataFormat=... ");
		System.out.println("       where ... stands for the formatting instructions of data (see examples)");
		System.out.println("    -dataSaving");
		System.out.println("       which allows us to save the data in a JSON file");
		System.out.println("    -model=...");
		System.out.println("       where ... stands for the name of a model variant, which allows us to write");
		System.out.println("       code like 'if (isModel(\"basic\")) { ... }'");
		System.out.println("    -ev");
		System.out.println("       which displays the exception that has been thown, in case of a crash");
		System.out.println("    -ic");
		System.out.println("       which indents and compresses, using Linux commands 'xmlindent -i 2' and 'lzma'");
		System.out.println("    -output=...");
		System.out.println("       which ... stands for the name of the output XCSP3 file (without exetnsions)");
		System.out.println("\nExamples.");
		System.out.println("  java " + Compiler.class.getName() + " " + AllInterval.class.getName() + " -data=5");
		System.out.println("    => generates the XCSP3 file AllInterval-5.xml");
		System.out.println("  java " + Compiler.class.getName() + " " + AllInterval.class.getName() + " -data=5 -dataFormat=%03d");
		System.out.println("    => generates the XCSP3 file AllInterval-005.xml");
		System.out.println("  java " + Compiler.class.getName() + " " + Bibd.class.getName() + " -data=[6,50,25,3,10]");
		System.out.println("    => generates the XCSP3 file Bibd-6-50-25-3-10.xml");
		System.out.println("  java " + Compiler.class.getName() + " " + Bibd.class.getName() + " -data=[6,50,25,3,10] -dataFormat=[%02d,%02d,%02d,%02d,%02d]");
		System.out.println("    => generates the XCSP3 file Bibd-06-50-25-03-10.xml");
		System.out.println(
				"  java " + Compiler.class.getName() + " " + Bibd.class.getName() + " -data=[6,50,25,3,10] -dataFormat=[%02d,%02d,%02d,%02d,%02d] -dataSaving");
		System.out.println("    => generates the JSON file Bibd-06-50-25-03-10.json");
		System.out.println("    => generates the XCSP3 file Bibd-06-50-25-03-10.xml");
		System.out.println("  java " + Compiler.class.getName() + " " + Bibd.class.getName() + " -data=Bibd-06-50-25-03-10.json");
		System.out.println("    => generates the XCSP3 file Bibd-06-50-25-03-10.xml");
		System.out.println("  java " + Compiler.class.getName() + " " + Bibd.class.getName() + " -data=Bibd-06-50-25-03-10.json -ic");
		System.out.println("    => generates the indented compressed XCSP3 file Bibd-06-50-25-03-10.xml.lzma");
		System.out.println("  java " + Compiler.class.getName() + " " + AllInterval.class.getName() + " -data=5 -model=test");
		System.out.println("    => generates the XCSP3 file AllInterval-test-5.xml");
		System.out.println("       while executing any piece of code controlled by 'isModel(\"test\"))'");
		System.out.println("  java " + Compiler.class.getName() + " " + AllInterval.class.getName() + " -data=5 -output=tmp");
		System.out.println("    => generates the XCSP3 file tmp.xml");
		return null;
	}

	private static ProblemAPI buildInstanceAPI(String[] args) {
		if (args.length == 0)
			return usage();
		try {
			Constructor<?>[] cs = Class.forName(args[0]).getDeclaredConstructors();
			if (cs.length > 1 || cs[0].getParameterTypes().length > 0) {
				System.out.println("\nProblem: It is forbidden to include constructors in a class implementing " + ProblemAPI.class.getName() + "\n");
				return null;
			}
			if (!ProblemAPI.class.isAssignableFrom(cs[0].getDeclaringClass())) {
				System.out.println("\nProblem: the specified class " + args[0] + " does not implement " + ProblemAPI.class.getName() + "\n");
				return usage();
			}
			cs[0].setAccessible(true);
			ProblemAPI api = (ProblemAPI) cs[0].newInstance();

			String[] argsForPb = Stream.of(args).skip(1)
					.filter(s -> !s.startsWith(VARIANT) && !s.startsWith(DATA) && !s.startsWith(OUTPUT) && !s.equals(EV) && !s.equals(IC))
					.toArray(String[]::new);
			ev = Stream.of(args).anyMatch(s -> s.equals(EV));
			String model = Stream.of(args).filter(s -> s.startsWith(VARIANT)).map(s -> s.substring(VARIANT.length() + 1)).findFirst().orElse("");
			String data = Stream.of(args).filter(s -> s.startsWith(DATA + "=")).map(s -> s.substring(DATA.length() + 1)).findFirst().orElse("");
			String dataFormat = Stream.of(args).filter(s -> s.startsWith(DATA_FORMAT)).map(s -> s.substring(DATA_FORMAT.length() + 1)).findFirst().orElse("");
			boolean dataSaving = Stream.of(args).anyMatch(s -> s.equals(DATA_SAVING));
			new ProblemIMP3(api, model, data, dataFormat, dataSaving, argsForPb);
			return api;
		} catch (Exception e) {
			System.out.println("It was not possible to build an instance of the specified class " + args[0]);
			if (ev)
				e.printStackTrace();
			return usage();
		}
	}

	public static Document buildDocument(String[] args) {
		ProblemAPI api = buildInstanceAPI(args);
		return api == null ? null : new Compiler(api).buildDocument();
	}

	public static void main(String[] args) {
		ProblemAPI api = buildInstanceAPI(args);
		if (api == null)
			return;
		Document document = new Compiler(api).buildDocument();
		String output = Stream.of(args).filter(s -> s.startsWith(OUTPUT)).map(s -> s.substring(OUTPUT.length() + 1)).findFirst().orElse(null);
		String fileName = (output != null ? output : api.name()) + ".xml";
		ProblemAPI.api2imp.get(api).save(document, fileName);
		if (Stream.of(args).anyMatch(s -> s.equals(IC)))
			ProblemAPI.api2imp.get(api).indentAndCompressXmlUnderLinux(fileName);
	}
}
