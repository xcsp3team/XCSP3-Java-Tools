/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.modeler;

import static org.xcsp.common.Constants.ARGS;
import static org.xcsp.common.Constants.ARRAY;
import static org.xcsp.common.Constants.BLOCK;
import static org.xcsp.common.Constants.CONSTRAINTS;
import static org.xcsp.common.Constants.DOMAIN;
import static org.xcsp.common.Constants.GROUP;
import static org.xcsp.common.Constants.INSTANCE;
import static org.xcsp.common.Constants.OBJECTIVES;
import static org.xcsp.common.Constants.VAR;
import static org.xcsp.common.Constants.VARIABLES;
import static org.xcsp.common.Utilities.element;
import static org.xcsp.modeler.definitions.ICtr.EXTENSION;
import static org.xcsp.modeler.definitions.ICtr.FUNCTION;
import static org.xcsp.modeler.definitions.ICtr.INTENSION;
import static org.xcsp.modeler.definitions.ICtr.LIST;
import static org.xcsp.modeler.definitions.ICtr.SLIDE;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.modeler.definitions.DefXCSP;
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
import org.xcsp.modeler.entities.CtrEntities.CtrBlock;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;
import org.xcsp.modeler.entities.ModelingEntity;
import org.xcsp.modeler.entities.ModelingEntity.TagDummy;
import org.xcsp.modeler.entities.ObjEntities.ObjEntity;
import org.xcsp.modeler.entities.VarEntities.VarAlone;
import org.xcsp.modeler.entities.VarEntities.VarArray;
import org.xcsp.modeler.entities.VarEntities.VarEntity;
import org.xcsp.modeler.implementation.ProblemDataHandler;
import org.xcsp.modeler.implementation.ProblemIMP;
import org.xcsp.modeler.implementation.ProblemIMP3;
import org.xcsp.modeler.implementation.ProblemIMP3.MVariable;
import org.xcsp.parser.entries.XConstraints.XSoftening;
import org.xcsp.parser.entries.XConstraints.XSoftening.XSofteningGlobal;
import org.xcsp.parser.entries.XConstraints.XSoftening.XSofteningSimple;
import org.xcsp.parser.entries.XValues.IntegerInterval;
import org.xcsp.parser.entries.XVariables.TypeVar;

public class Compiler {

	// ************************************************************************
	// ***** Constants, Fields and Constructor
	// ************************************************************************

	public static final String FORMAT = TypeAtt.format.name();
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

	protected ProblemIMP problem;
	protected Document doc;

	protected Map<String, Element> tuplesReferents = new HashMap<>();
	protected int nbBuiltTuplesReferents;

	protected int limitForUsingAs = 10;
	protected boolean discardIntegerType = true, discardAsRelation = true, printNotes = true;

	public Compiler(ProblemAPI api) {
		this.problem = api.imp();
	}

	// ************************************************************************
	// ***** Managing (groups of) predicates, relations and globals
	// ************************************************************************

	protected List<Predicate> storedP = new ArrayList<>();
	protected List<Relation> storedR = new ArrayList<>();
	protected List<Global> storedG = new ArrayList<>();

	protected boolean doubleAbstraction = true;
	protected boolean ignoreAutomaticGroups = true, saveImmediatelyStored = true;
	protected boolean monoformGroups = false;

	protected void saveStored(Element parent, boolean immediatly, boolean br, boolean bp, boolean bg) {
		// System.out.println("saveStored" + immediatly + " " + bp + storedP.size());
		if (!immediatly)
			return;
		if (br && storedR.size() > 0)
			parent.appendChild(buildingStoredRelations());
		if (bp && storedP.size() > 0)
			parent.appendChild(buildingStoredPredicates());
		if (bg && storedG.size() > 0)
			parent.appendChild(buildingStoredGlobals());
	}

	protected void saveStored(Element parent) {
		saveStored(parent, true, true, true, true);
	}

	protected abstract class Similarable<T> {
		protected abstract boolean isSimilarTo(T object);

		protected boolean haveSimilarAttributes(ICtr c1, ICtr c2) {
			CtrAlone ca1 = problem.ctrEntities.ctrToCtrAlone.get(c1), ca2 = problem.ctrEntities.ctrToCtrAlone.get(c2);
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
				if (ca1.softening instanceof XSofteningSimple) {
					if (((XSofteningSimple) ca1.softening).violationCost != ((XSofteningSimple) ca2.softening).violationCost)
						return false;
				} else if (ca1.softening instanceof XSofteningGlobal) {
					if (((XSofteningGlobal) ca1.softening).type != ((XSofteningGlobal) ca2.softening).type)
						return false;
					if (((XSofteningGlobal) ca1.softening).parameters != null || ((XSofteningGlobal) ca2.softening).parameters != null)
						return false;
				} else
					return false;
			}
			return true;
		}
	}

	protected class Predicate extends Similarable<Predicate> {
		public ICtrIntension c;
		public XNodeParent<IVar> abstractTree;
		public List<Object> args = new ArrayList<>();

		public Predicate(ICtrIntension c, boolean abstractIntegers, boolean multiOccurrences) {
			this.c = c;
			this.abstractTree = (XNodeParent<IVar>) ((XNodeParent<IVar>) c.mapXCSP().get(FUNCTION)).abstraction(args, abstractIntegers, multiOccurrences);
		}

		public Predicate(ICtrIntension c) {
			this(c, true, true);
		}

		@Override
		public boolean isSimilarTo(Predicate p) {
			return haveSimilarAttributes(c, p.c) && abstractTree.equals(p.abstractTree);
		}
	}

	protected class Relation extends Similarable<Relation> {
		public ICtrExtension c;

		public Relation(ICtrExtension c) {
			this.c = c;
		}

		@Override
		public boolean isSimilarTo(Relation r) {
			return haveSimilarAttributes(c, r.c) && c.isSimilarTo(r.c);
		}
	}

	protected class Global extends Similarable<Global> {
		public ICtr c;
		public DefXCSP def;

		public int[] recordedDiffs;
		public int[] recordedSizes;

		public Global(ICtr c) {
			this.c = c;
			this.def = c.defXCSP();
		}

		@Override
		public boolean isSimilarTo(Global g) {
			if (def.map.containsKey(ICtr.MATRIX))
				return false; // currently, forbidden to group together constraints with child MATRIX
			if (!haveSimilarAttributes(c, g.c))
				return false;
			int[] diffs = def == null || g.def == null ? null : def.differencesWith(g.def);
			if (diffs == null)
				return false;
			if (diffs.length == 0) {
				System.out.println("WARNING : Two similar constraints");
				return false; // The constraints are identical; we return false to keep both of them (may happen with some awkward
								// instances)
			}
			if (diffs.length == 1) {
				if (storedG.size() == 1) {
					recordedDiffs = diffs;
					return true;
				}
				return recordedDiffs.length == 1 && recordedDiffs[0] == diffs[0];
			}
			// System.out.println(" DIFFS = " + diffs.length + " " + storedG.size() + " " + Kit.join(recordedDiffs));
			if (doubleAbstraction && diffs.length == 2 && def.childs.size() > 2 && !(c instanceof ICtrRegular) && !(c instanceof ICtrMdd)) {
				Function<Object, Integer> sizeOf = v -> v instanceof Number || v instanceof IntegerInterval || v instanceof Condition ? 1
						: Stream.of((v.toString()).trim().split("\\s+"))
								.mapToInt(tok -> Utilities.isNumeric(tok) || Utilities.isNumericInterval(tok) ? 1 : problem.varEntities.nbVarsIn(tok)).sum();
				if (IntStream.of(diffs).anyMatch(i -> def.childs.get(i).name.equals("condition")))
					return false; // for the moment, the parser does not manage abstraction of condition elements
				if (storedG.size() == 1) {
					int[] s1 = IntStream.of(diffs).map(i -> sizeOf.apply(def.childs.get(i).content)).toArray();
					int[] s2 = IntStream.of(diffs).map(i -> sizeOf.apply(g.def.childs.get(i).content)).toArray();
					// System.out.println("s1 = " + Kit.join(s1) + " s2 = " + Kit.join(s2));
					if (IntStream.range(0, diffs.length).allMatch(i -> s1[i] == s2[i])) {
						recordedDiffs = diffs;
						recordedSizes = s1;
						return true;
					} else
						return false;
				}
				if (recordedDiffs.length != 2 || recordedDiffs[0] != diffs[0] || recordedDiffs[1] != diffs[1])
					return false;
				int[] s2 = IntStream.of(diffs).map(i -> sizeOf.apply(g.def.childs.get(i).content)).toArray();
				return IntStream.range(0, diffs.length).allMatch(i -> recordedSizes[i] == s2[i]);
			}
			return false; // for the moment, only 1 or 2 differences are managed
		}

		@Override
		public String toString() {
			return def.toString();
		}
	}

	// ************************************************************************
	// ***** Auxiliary Functions
	// ************************************************************************

	protected String seqOfParameters(int n, int start) {
		return n == -1 ? VAR_ARGS : IntStream.range(0, n).mapToObj(i -> "%" + (start + i)).collect(Collectors.joining(" "));
	}

	protected String seqOfParameters(int n) {
		return seqOfParameters(n, 0);
	}

	protected void sideAttributes(Element element, ModelingEntity entity) {
		if (entity == null)
			return;
		if (entity.id != null)
			element.setAttribute(ID, entity.id);
		if (entity.classes.size() > 0)
			element.setAttribute(CLASS, entity.classes.stream().map(c -> c.ccname()).collect(Collectors.joining(" ")));
		if (printNotes && entity.note != null && entity.note.length() > 0)
			element.setAttribute(NOTE, entity.note);
		if (entity instanceof CtrAlone) {
			XSoftening sf = ((CtrAlone) entity).softening;
			if (sf != null) {
				Utilities.control(sf.cost == null, "Cannot be managed at this place");
				if (sf instanceof XSofteningSimple)
					element.setAttribute(VIOLATION_COST, ((XSofteningSimple) sf).violationCost + "");
				else if (sf instanceof XSofteningGlobal)
					element.setAttribute(VIOLATION_MEASURE, ((XSofteningGlobal) sf).type.toString());
				else
					Utilities.control(false, "Unreachable");
			}
		}
	}

	protected Element buildingDef(DefXCSP def, int absIndex1, String absValue1, int absIndex2, String absValue2) {
		Element elt = doc.createElement(def.name);
		def.attributes.stream().forEach(a -> elt.setAttribute(a.getKey(), a.getValue().toString()));
		if (def.childs.size() == 1 && def.childs.get(0).attributes.size() == 0 && def.possibleSimplification)
			if (def.childs.get(0).name.equals(ICtr.REC)) { // recursivity
				Utilities.control(absIndex1 != 0 && absIndex2 != 0 && def.childs.get(0).content instanceof CtrAlone, "Pb");
				CtrAlone ca = (CtrAlone) def.childs.get(0).content;
				Element sub = buildingDef(ca.ctr.defXCSP());
				sideAttributes(sub, ca);
				elt.appendChild(sub);
			} else
				elt.setTextContent(" " + (absIndex1 == 0 ? absValue1 : def.childs.get(0).content) + " ");
		else
			for (int i = 0; i < def.childs.size(); i++) {
				if (def.childs.get(i).name.equals(ICtr.REC)) { // recursivity
					Utilities.control(absIndex1 != i && absIndex2 != i && def.childs.get(i).content instanceof CtrAlone, "Pb");
					CtrAlone ca = (CtrAlone) def.childs.get(i).content;
					Element sub = buildingDef(ca.ctr.defXCSP());
					sideAttributes(sub, ca);
					elt.appendChild(sub);
					// elt.appendChild(buildGlobal((DefXCSP) def.childs.get(i).content));
				} else {
					Element sub = element(doc, def.childs.get(i).name, i == absIndex1 ? absValue1 : i == absIndex2 ? absValue2 : def.childs.get(i).content);
					def.childs.get(i).attributes.stream().forEach(a -> sub.setAttribute(a.getKey(), a.getValue().toString()));
					elt.appendChild(sub);
				}
			}
		return elt;
	}

	protected Element buildingDef(DefXCSP def, int abstractionIndex, String abstractionValue) {
		return buildingDef(def, abstractionIndex, abstractionValue, -1, "");
	}

	protected Element buildingDef(DefXCSP def) {
		return buildingDef(def, -1, "", -1, "");
	}

	// ************************************************************************
	// ***** Managing Variables
	// ************************************************************************

	protected Element baseVarEntity(Element element, VarEntity va) {
		sideAttributes(element, va);
		if (va instanceof VarArray)
			element.setAttribute(ICtr.SIZE, VarArray.class.cast(va).getStringSize());
		if (!discardIntegerType || va.getType() != TypeVar.integer)
			element.setAttribute(TYPE, va.getType().name());
		return element;
	}

	protected Element var(VarAlone va, String s, boolean alias) {
		return baseVarEntity(alias ? element(doc, VAR, AS, s) : element(doc, VAR, s), va);
	}

	protected Element array(VarArray va, String s, boolean alias) {
		return baseVarEntity(alias ? element(doc, ARRAY, AS, s) : element(doc, ARRAY, s), va);
	}

	protected Element array(VarArray va, Map<IVar, String> varToDomText, Map<Object, List<IVar>> map) {
		Utilities.control(map.size() > 1, "The map only contains one entry");
		Element element = baseVarEntity(doc.createElement(ARRAY), va);
		for (List<IVar> list : map.values())
			element.appendChild(element(doc, DOMAIN, FOR, problem.varEntities.compact(list.toArray(new IVar[list.size()])), varToDomText.get(list.get(0))));
		return element;
	}

	protected void putInMap(IVar x, Map<IVar, String> map) {
		map.put(x, ((MVariable) x).dom.toString());
	}

	protected Element variables() {
		Element element = doc.createElement(VARIABLES);
		Map<IVar, String> varToDom = new HashMap<>();
		for (VarEntity ve : problem.varEntities.allEntities)
			if (ve instanceof VarAlone)
				putInMap(((VarAlone) ve).var, varToDom);
			else
				for (IVar x : ((VarArray) ve).flatVars)
					putInMap(x, varToDom);
		Map<String, String> domToVarReferent = new HashMap<>();
		for (VarEntity ve : problem.varEntities.allEntities) {
			if (ve instanceof VarAlone) {
				VarAlone va = (VarAlone) ve;
				// Utilities.control(problem.varEntities.varToVarArray.get(va.var) == null, "");
				if (problem.varEntities.varToVarArray.get(va.var) != null) // necessary for xcsp2
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

	// ************************************************************************
	// ***** Managing Constraints
	// ************************************************************************

	protected <T extends Similarable<T>> List<Element> buildChilds(T[] t, List<T> store, Supplier<Element> spl) {
		List<Element> childs = new ArrayList<>();
		if (monoformGroups) {
			store.add(t[0]);
			boolean similar = t[0].isSimilarTo(t[1]); // to record diffs
			Utilities.control(similar, "Should be similar");
			assert Stream.of(t).allMatch(p -> p.isSimilarTo(t[0]));
			IntStream.range(1, t.length).forEach(i -> store.add(t[i]));
			childs.add(spl.get());
		} else {
			boolean[] flags = new boolean[t.length];
			for (int i = 0; i < t.length; i++) {
				if (flags[i])
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

	protected Element buildingTuples(ICtrExtension c) {
		Object tuples = c.mapXCSP().get(ICtr.TUPLES);
		String key = tuples instanceof int[][] ? ICtrExtension.tableAsString((int[][]) tuples) : ICtrExtension.tableAsString((String[][]) tuples);
		Element eltTgt = tuplesReferents.get(key), eltSrc = null;
		if (eltTgt != null && !discardAsRelation) {
			if (eltTgt.getAttribute(ID).length() == 0) {
				eltTgt.setAttribute(ID, "i" + nbBuiltTuplesReferents); // we add a useful missing id
				nbBuiltTuplesReferents++;
			}
			eltSrc = element(doc, (Boolean) c.mapXCSP().get(ICtr.POSITIVE) ? SUPPORTS : CONFLICTS, AS, eltTgt.getAttribute(ID));
		} else {
			eltSrc = element(doc, (Boolean) c.mapXCSP().get(ICtr.POSITIVE) ? SUPPORTS : CONFLICTS, key);
			tuplesReferents.put(key, eltSrc);
		}
		return eltSrc;
	}

	protected Element buildingStoredRelations() {
		Relation r = storedR.get(0); // first relation
		Element lst = element(doc, LIST, storedR.size() == 1 ? r.c.mapXCSP().get(LIST) : seqOfParameters((Integer) r.c.mapXCSP().get(ICtr.ARITY)));
		Element ext = element(doc, EXTENSION, lst, buildingTuples(r.c));
		Element elt = storedR.size() == 1 ? ext : element(doc, GROUP, ext, storedR.stream().map(rr -> element(doc, ARGS, rr.c.mapXCSP().get(LIST))));
		sideAttributes(elt, problem.ctrEntities.ctrToCtrAlone.get(r.c));
		// sideAttributes(elt, storedR.size() == 1 ? loader.ctrEntities.ctrToCtrAlone.get(r.c) :loader.ctrEntities?ctrToCtrArray.get(r.c));
		storedR.clear();
		return elt;
	}

	protected List<Element> handleListOfExtension(Element parent, List<ICtr> ctrs) {
		saveStored(parent, true, true, false, false);
		Relation[] rels = ctrs.stream().map(c -> new Relation((ICtrExtension) c)).toArray(Relation[]::new);
		return buildChilds(rels, storedR, () -> buildingStoredRelations());
	}

	protected Element buildingStoredPredicates() {
		Predicate p = storedP.get(0); // first predicate
		Element itn = element(doc, INTENSION, storedP.size() == 1 ? p.c.mapXCSP().get(ICtr.FUNCTION) : p.abstractTree);
		Element elt = storedP.size() == 1 ? itn : element(doc, GROUP, itn, storedP.stream().map(pp -> element(doc, ARGS, Utilities.join(pp.args))));
		sideAttributes(elt, problem.ctrEntities.ctrToCtrAlone.get(p.c));
		storedP.clear();
		return elt;
	}

	protected List<Element> handleListOfIntension(Element parent, List<ICtr> ctrs) {
		saveStored(parent, true, false, true, false);
		Predicate[] preds = ctrs.stream().map(c -> new Predicate((ICtrIntension) c)).toArray(Predicate[]::new);
		return buildChilds(preds, storedP, () -> buildingStoredPredicates());
	}

	protected Element buildingStoredGlobals() {
		Element elt = null;
		Global g = storedG.get(0);
		if (storedG.size() == 1)
			elt = buildingDef(g.def);
		else {
			Utilities.control(g.recordedDiffs.length == 1 || g.recordedDiffs.length == 2, "");
			int i = g.recordedDiffs[0];
			if (g.recordedDiffs.length == 1) {
				Element gbl = buildingDef(g.def, i, g.def.childs.get(i).name.equals("index") ? "%0" : VAR_ARGS); // TODO other cases with %0
																													// ?
				elt = element(doc, GROUP, gbl, storedG.stream().map(gg -> element(doc, ARGS, gg.def.childs.get(i).content)));
			} else {
				int j = g.recordedDiffs[1];
				Element gbl = buildingDef(g.def, i, seqOfParameters(g.recordedSizes[0]), j, seqOfParameters(g.recordedSizes[1], g.recordedSizes[0]));
				elt = element(doc, GROUP, gbl,
						storedG.stream().map(gg -> element(doc, ARGS, gg.def.childs.get(i).content + " " + gg.def.childs.get(j).content)));
			}
		}
		sideAttributes(elt, problem.ctrEntities.ctrToCtrAlone.get(g.c));
		storedG.clear();
		return elt;
	}

	protected List<Element> handleListOfGlobal(Element parent, List<ICtr> ctrs) {
		saveStored(parent, true, false, false, true);
		Global[] gbls = ctrs.stream().map(c -> new Global(c)).toArray(Global[]::new);
		return buildChilds(gbls, storedG, () -> buildingStoredGlobals());
	}

	private Element buildSlide(ICtrSlide ctr) {
		Element elt = doc.createElement(SLIDE);
		Map<String, Object> map = ctr.mapXCSP();
		if (map.containsKey(CIRCULAR) && (Boolean) map.get(CIRCULAR))
			elt.setAttribute(CIRCULAR, "true");
		IVar[][] lists = (IVar[][]) map.get(ICtr.LISTS);
		int[] offsets = (int[]) map.get(ICtr.OFFSETS), collects = (int[]) map.get(ICtr.COLLECTS);
		for (int i = 0; i < lists.length; i++) {
			Element subelement = element(doc, LIST, problem.varEntities.compactOrdered(lists[i]));
			if (collects[i] != 1 || lists.length > 1)
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
			int nb = problem.varEntities.nbVarsIn(g0.def.childs.get(diffs[0]).content.toString());
			elt.appendChild(buildingDef(g0.def, diffs[0], seqOfParameters(nb, 0)));
		}
		sideAttributes(elt, problem.ctrEntities.ctrToCtrAlone.get(ctr));
		return elt;
	}

	private Element buildMeta(ICtr ctr) {
		Element elt = buildingDef(ctr.defXCSP());
		sideAttributes(elt, problem.ctrEntities.ctrToCtrAlone.get(ctr));
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
		if (problem.typeFramework() == TypeFramework.WCSP) {
			// lb (lower bound) and ub (upper bound) to be managed ; TODO
		}
	}

	// protected Map<Class<?>, List<ICtr>> repartition(ICtr[] ctrs) {
	// return Stream.of(ctrs).collect(Collectors.groupingBy(c -> c.getClass()));
	// }

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
		Element root = doc.createElement(CONSTRAINTS);
		setSpecificFrameworkAttributes(root);
		Utilities.control(storedP.size() == 0 && storedR.size() == 0 && storedG.size() == 0, "Storing structures are not empty");
		Stack<Element> stackOfBlocks = new Stack<>();
		stackOfBlocks.push(root); // the initial element is seen as a root block here
		for (CtrEntity ce : problem.ctrEntities.allEntities) {
			if (ce instanceof TagDummy)
				continue;
			Element currParent = stackOfBlocks.peek();
			// System.out.println("entity " + ce);
			if (ce instanceof CtrBlock) {
				CtrBlock ctrBlock = (CtrBlock) ce;
				saveStored(currParent);
				if (ctrBlock.opening) {
					Element block = doc.createElement(BLOCK);
					sideAttributes(block, ctrBlock);
					currParent.appendChild(block);
					stackOfBlocks.push(block);
				} else
					stackOfBlocks.pop();
			} else if (ce instanceof CtrArray) {
				CtrArray ctrArray = (CtrArray) ce;
				Map<Class<?>, List<ICtr>> map = Stream.of((ctrArray).ctrs).collect(Collectors.groupingBy(c -> c.getClass())); // repartition((ctrArray).ctrs);
				if (map.size() == 1) {
					saveStored(currParent, saveImmediatelyStored, true, true, true);
					List<Element> childs = buildChilds(currParent, map.values().iterator().next());
					if (childs.size() == 1 && (ctrArray.nullBasicAttributes() || childs.get(0).getAttributes().getLength() == 0)) {
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
					Element block = doc.createElement(BLOCK);
					sideAttributes(block, ctrArray);
					for (List<ICtr> list : map.values())
						buildChilds(block, list).stream().forEach(c -> block.appendChild(c));
					currParent.appendChild(block);
				}
			} else {
				// System.out.println("pass here " + problem.ctrEntities.ctrToCtrArray.get(((CtrAlone) ce).ctr));
				ICtr c = ((CtrAlone) ce).ctr;
				if (problem.ctrEntities.ctrToCtrArray.get(c) == null)
					handleCtr(currParent, c);
			}
		}
		assert stackOfBlocks.size() == 1 && stackOfBlocks.peek() == root;
		saveStored(root);
		return root;
	}

	// ************************************************************************
	// ***** Managing Objectives
	// ************************************************************************

	protected Element objectives() {
		Element root = doc.createElement(OBJECTIVES);// // element.setAttribute(OPTIMIZATION, LEXICO);
		for (ObjEntity obj : problem.objEntities.allEntities) {
			Element elt = buildingDef(obj.obj.defXCSP());
			sideAttributes(elt, obj);
			root.appendChild(elt);
		}
		return root;
	}

	public Document buildDocument() {
		// TODO control that ids are all different
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element root = element(doc, INSTANCE, FORMAT, "XCSP3", TYPE,
				problem.objEntities.allEntities.size() > 0 ? TypeFramework.COP.name() : problem.typeFramework().name());
		root.appendChild(variables());
		root.appendChild(constraints());
		if (problem.objEntities.allEntities.size() > 0)
			root.appendChild(objectives());
		doc.appendChild(root);
		doc.normalize();
		return doc;
	}

	/** User arguments given on the command for the problem (instance) */
	public static String[] argsForPb;

	public static boolean ev;

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

	private static Object prepareData(Class<?> type, String v) {
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

	public static List<Field> problemDataFields(List<Field> list, Class<?> cl) {
		if (ProblemAPI.class.isAssignableFrom(cl)) {
			problemDataFields(list, cl.getSuperclass());
			Stream.of(cl.getDeclaredFields()).filter(f -> !ProblemIMP.mustBeIgnored(f)).forEach(f -> list.add(f));
		}
		return list;
	}

	public static void setValuesOfProblemDataFields(ProblemAPI api, Object[] values, String[] fmt, boolean prepare) {
		Field[] fields = problemDataFields(new ArrayList<>(), api.getClass()).toArray(new Field[0]);
		ProblemIMP.control(fields.length == values.length, "The number of fields is different from the number of specified data");
		for (int i = 0; i < fields.length; i++) {
			try {
				fields[i].setAccessible(true);
				// System.out.println("Values=" + values[i] + " " + (prepare && values[i] != null) + " test" + (values[i].getClass()));
				Object value = values[i] instanceof String && ((String) values[i]).equals("-") ? null
						: prepare ? prepareData(fields[i].getType(), (String) values[i]) : values[i];
				fields[i].set(api, value);
				if (prepare)
					api.imp().addParameter(value, fmt == null ? null : fmt[i]);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				ProblemIMP.control(false, "Problem when setting the value of field " + fields[i].getName());
				if (ev)
					e.printStackTrace();
			}
		}
	}

	public static void loadDataAndModel(String data, String dataFormat, boolean dataSaving, ProblemAPI api) {
		if (data.length() != 0) {
			if (data.endsWith("json")) {
				new ProblemDataHandler().load(api, data);
				String value = data.startsWith(api.getClass().getSimpleName()) ? data.substring(api.getClass().getSimpleName().length() + 1) : data;
				api.imp().addParameter(value);
			} else {
				ProblemIMP.control(data.startsWith("[") == data.endsWith("]"),
						"Either specify a simple value (such as an integer) or an array with the form [v1,v2,..]");
				ProblemIMP.control(data.indexOf(" ") == -1, "No space is allowed in specified data");
				String[] values = data.startsWith("[") ? data.substring(1, data.length() - 1).split(",") : new String[] { data };
				String[] fmt = dataFormat.length() == 0 ? null
						: dataFormat.startsWith("[") ? dataFormat.substring(1, dataFormat.length() - 1).split(",") : new String[] { dataFormat };
				// System.out.println(values.length + " " + Kit.join(values));
				setValuesOfProblemDataFields(api, values, fmt, true);
			}
		} else {
			Method m = searchMethod(api.getClass(), "data");
			if (m == null)
				ProblemIMP.control(problemDataFields(new ArrayList<>(), api.getClass()).toArray(new Field[0]).length == 0, "Data must be specified.");
			else
				executeMethod(api, "data");
			String[] fmt = dataFormat.length() == 0 ? null
					: dataFormat.startsWith("[") ? dataFormat.substring(1, dataFormat.length() - 1).split(",") : new String[] { dataFormat };
			if (fmt != null) {
				Utilities.control(fmt.length == api.imp().parameters.size(), "");
				IntStream.range(0, fmt.length).forEach(i -> api.imp().parameters.get(i).setValue(fmt[i]));
			}
		}
		if (dataSaving)
			new ProblemDataHandler().save(api, api.name());
		api.model(); // executeMethod(api, "model");
	}

	private static void usage() {
		System.out.println("\nDescription.\n  Compiler is a class that can generate an XCSP3 file. You need to provide");
		System.out.println("  an MCSP3 model (Java class implementing ProblemAPI) and some effective data.");
		System.out.println("\nUsage.\n  java modeler.Compiler <className> [<arguments>]\n");
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
		System.out.println("       which displays the excception that has been thown, in case of a crash");
		System.out.println("    -ic");
		System.out.println("       which indents and compresses, using Linux commands 'xmlindent -i 2' and 'lzma'");
		System.out.println("\nExamples.");
		System.out.println("  java modeler.Compiler modeler.problems.AllInterval -data=5");
		System.out.println("    => generates the XCSP3 file AllInterval-5.xml");
		System.out.println("  java modeler.Compiler modeler.problems.AllInterval -data=5 -dataFormat=%3d");
		System.out.println("    => generates the XCSP3 file AllInterval-005.xml");
		System.out.println("  java modeler.Compiler modeler.problems.Bibd -data=[6,50,25,3,10]");
		System.out.println("    => generates the XCSP3 file Bibd-6-50-25-3-10.xml");
		System.out.println("  java modeler.Compiler modeler.problems.Bibd -data=[6,50,25,3,10] -dataFormat=[%02d,%02d,%02d,%02d,%02d]");
		System.out.println("    => generates the XCSP3 file Bibd-06-50-25-03-10.xml");
		System.out.println("  java modeler.Compiler modeler.problems.Bibd -data=[6,50,25,3,10] -dataFormat=[%02d,%02d,%02d,%02d,%02d] -dataSaving");
		System.out.println("    => generates the JSON file Bibd-06-50-25-03-10.json");
		System.out.println("    => generates the XCSP3 file Bibd-06-50-25-03-10.xml");
		System.out.println("  java modeler.Compiler modeler.problems.Bibd -data=Bibd-06-50-25-03-10.json");
		System.out.println("    => generates the XCSP3 file Bibd-06-50-25-03-10.xml");
		System.out.println("  java modeler.Compiler modeler.problems.Bibd -data=Bibd-06-50-25-03-10.json -ic");
		System.out.println("    => generates the indented compressed XCSP3 file Bibd-06-50-25-03-10.xml.lzma");
		System.out.println("  java modeler.Compiler modeler.problems.AllInterval -data=5 -model=test");
		System.out.println("    => generates the XCSP3 file AllInterval-5.xml");
		System.out.println("       while executing any piece of code controlled by 'isModel(\"test\"))'");
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
			return;
		}
		Object object = null;
		try {
			Constructor<?>[] cs = Class.forName(args[0]).getDeclaredConstructors();
			if (cs.length > 1 || cs[0].getParameterTypes().length > 0) {
				System.out.println("\nProblem: It is forbidden to include constructors in a class implementing " + ProblemAPI.class.getName() + "\n");
				return;
			}
			if (!ProblemAPI.class.isAssignableFrom(cs[0].getDeclaringClass())) {
				System.out.println("\nProblem: the specified class " + args[0] + " does not implement " + ProblemAPI.class.getName() + "\n");
				usage();
				return;
			}
			cs[0].setAccessible(true);
			object = cs[0].newInstance();
		} catch (Exception e) {
			System.out.println("It was not possible to build an instance of the specified class " + args[0]);
			usage();
			return;
		}

		argsForPb = Stream.of(args).skip(1).filter(s -> !s.startsWith("-data") && !s.startsWith("-model") && !s.equals("-ev") && !s.equals("-ic"))
				.toArray(String[]::new);
		String data = Stream.of(args).filter(s -> s.startsWith("-data=")).map(s -> s.substring(6)).findFirst().orElse("");
		String dataFormat = Stream.of(args).filter(s -> s.startsWith("-dataFormat=")).map(s -> s.substring(12)).findFirst().orElse("");
		boolean dataSaving = Stream.of(args).anyMatch(s -> s.equals("-dataSaving"));
		ev = Stream.of(args).anyMatch(s -> s.equals("-ev"));

		ProblemAPI api = (ProblemAPI) object;
		ProblemIMP imp = new ProblemIMP3(api);
		ProblemAPI.api2imp.put(api, imp);
		imp.model = Stream.of(args).filter(s -> s.startsWith("-model=")).map(s -> s.substring(7)).findFirst().orElse("");
		loadDataAndModel(data, dataFormat, dataSaving, api);
		Document document = new Compiler(api).buildDocument();
		String fileName = api.name() + ".xml";
		imp.save(document, fileName, "lzma ");
		if (Stream.of(args).anyMatch(s -> s.equals("-ic")))
			imp.indentAndCompressUnderLinux(fileName);
	}
}
