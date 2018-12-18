package org.xcsp.modeler.implementation;

import static org.xcsp.modeler.definitions.ICtr.LIST;
import static org.xcsp.modeler.definitions.ICtr.LISTS;
import static org.xcsp.modeler.definitions.ICtr.MATRIX;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.IVar.VarSymbolic;
import org.xcsp.common.Range;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.Utilities.ModifiableBoolean;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.DomSymbolic;
import org.xcsp.common.domains.Domains.IDom;
import org.xcsp.common.domains.Values.IntegerEntity;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.Automaton;
import org.xcsp.common.structures.Table;
import org.xcsp.common.structures.TableSymbolic;
import org.xcsp.common.structures.Transition;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.definitions.ICtr;
import org.xcsp.modeler.definitions.ICtr.ICtrAllDifferent;
import org.xcsp.modeler.definitions.ICtr.ICtrAllEqual;
import org.xcsp.modeler.definitions.ICtr.ICtrCardinality;
import org.xcsp.modeler.definitions.ICtr.ICtrChannel;
import org.xcsp.modeler.definitions.ICtr.ICtrCircuit;
import org.xcsp.modeler.definitions.ICtr.ICtrClause;
import org.xcsp.modeler.definitions.ICtr.ICtrCount;
import org.xcsp.modeler.definitions.ICtr.ICtrCumulative;
import org.xcsp.modeler.definitions.ICtr.ICtrElement;
import org.xcsp.modeler.definitions.ICtr.ICtrExtension;
import org.xcsp.modeler.definitions.ICtr.ICtrIfThen;
import org.xcsp.modeler.definitions.ICtr.ICtrIfThenElse;
import org.xcsp.modeler.definitions.ICtr.ICtrInstantiation;
import org.xcsp.modeler.definitions.ICtr.ICtrIntension;
import org.xcsp.modeler.definitions.ICtr.ICtrMaximum;
import org.xcsp.modeler.definitions.ICtr.ICtrMdd;
import org.xcsp.modeler.definitions.ICtr.ICtrMinimum;
import org.xcsp.modeler.definitions.ICtr.ICtrNValues;
import org.xcsp.modeler.definitions.ICtr.ICtrNoOverlap;
import org.xcsp.modeler.definitions.ICtr.ICtrOrdered;
import org.xcsp.modeler.definitions.ICtr.ICtrRegular;
import org.xcsp.modeler.definitions.ICtr.ICtrSlide;
import org.xcsp.modeler.definitions.ICtr.ICtrStretch;
import org.xcsp.modeler.definitions.ICtr.ICtrSum;
import org.xcsp.modeler.definitions.IObj;
import org.xcsp.modeler.definitions.IObj.IObjFunctional;
import org.xcsp.modeler.definitions.IObj.IObjSpecialized;
import org.xcsp.modeler.entities.CtrEntities.CtrAlone;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;
import org.xcsp.modeler.entities.ObjEntities.ObjEntity;
import org.xcsp.modeler.implementation.ProblemIMP3.MVariable.MVarInteger;
import org.xcsp.modeler.implementation.ProblemIMP3.MVariable.MVarSymbolic;

public class ProblemIMP3 extends ProblemIMP {

	public static class MVariable implements IVar, Comparable<MVariable> {

		@Override
		public int compareTo(MVariable x) {
			int res = idPrefix.compareTo(x.idPrefix);
			if (res != 0)
				return res;
			if (idIndexes == null)
				return 0;
			return Utilities.lexComparatorInt.compare(idIndexes, x.idIndexes);
		}

		protected String id;
		private String idPrefix;
		private int[] idIndexes;
		public IDom dom;

		public MVariable(String id, IDom dom) {
			this.id = id;
			int pos = id.indexOf('[');
			this.idPrefix = pos == -1 ? id : id.substring(0, pos);
			this.idIndexes = pos == -1 ? null : Utilities.splitToInts(id.substring(pos), "\\[|\\]");
			this.dom = dom;
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public String toString() {
			return id;
		}

		public static class MVarSymbolic extends MVariable implements VarSymbolic {
			public MVarSymbolic(String id, DomSymbolic dom) {
				super(id, dom);
			}
		}

		public static class MVarInteger extends MVariable implements Var {
			public MVarInteger(String id, Dom dom) {
				super(id, dom);
			}
		}
	}

	@Override
	public Class<MVarInteger> classVI() {
		return MVarInteger.class;
	}

	@Override
	public Class<MVarSymbolic> classVS() {
		return MVarSymbolic.class;
	}

	public ProblemIMP3(ProblemAPI api, String modelVariant, String data, String dataFormat, boolean dataSaving, String[] argsForPb) {
		super(api, modelVariant, argsForPb);
		// org.xcsp.modeler.Compiler.
		loadDataAndModel(data, dataFormat, dataSaving); // , api);
	}

	/** A map that gives access to each variable through its id. */
	public final Map<String, MVariable> mapForVars = new HashMap<>();

	/** Adds a variable that has already be built. Should not be called directly when modeling. */
	public final MVariable addVar(MVariable x) {
		Utilities.control(!mapForVars.containsKey(x.id()), x.id() + " duplicated");
		mapForVars.put(x.id(), x);
		return x;
	}

	@Override
	public MVarInteger buildVarInteger(String id, Dom dom) {
		return (MVarInteger) addVar(new MVarInteger(id, dom));
	}

	@Override
	public MVarSymbolic buildVarSymbolic(String id, DomSymbolic dom) {
		return (MVarSymbolic) addVar(new MVarSymbolic(id, dom));
	}

	public CtrAlone post(ICtr c) {
		return ctrEntities.new CtrAlone(c);
	}

	// ************************************************************************
	// ***** Constraint intension
	// ************************************************************************

	@Override
	public CtrEntity intension(XNodeParent<IVar> tree) {
		return post(ICtrIntension.buildFrom(tree.vars(), tree));
	}

	// ************************************************************************
	// ***** Converting Intension to Extension
	// ************************************************************************

	private Converter converter = new Converter() {
		@Override
		public StringBuilder signatureFor(Var[] scp) {
			StringBuilder sb = new StringBuilder();
			for (MVarInteger x : (MVarInteger[]) scp)
				sb.append(System.identityHashCode(x.dom)).append(' ');
			return sb;
		}

		@Override
		public int[][] domValuesOf(Var[] scp) {
			IntegerEntity[][] ies = Stream.of((MVarInteger[]) scp).map(x -> (IntegerEntity[]) ((Dom) x.dom).values).toArray(IntegerEntity[][]::new);
			Utilities.control(Stream.of(ies).allMatch(t -> IntegerEntity.nValues(t) != -1 && IntegerEntity.nValues(t) < 1000000), "");
			return Stream.of(ies).map(t -> IntegerEntity.toIntArray(t, 1000000)).toArray(int[][]::new);
		}

		@Override
		public ModifiableBoolean mode() {
			return new ModifiableBoolean(null);
		}
	};

	@Override
	protected Converter getConverter() {
		return converter;
	}

	// ************************************************************************
	// ***** Constraint extension
	// ************************************************************************

	@Override
	public CtrAlone extension(Var[] list, int[][] tuples, boolean positive) {
		return post(ICtrExtension.buildFrom(list, varEntities.compactOrdered(list), list.length, Table.clean(tuples), positive));
	}

	@Override
	public CtrAlone extension(VarSymbolic[] list, String[][] tuples, boolean positive) {
		return post(ICtrExtension.buildFrom(list, varEntities.compactOrdered(list), list.length, TableSymbolic.clean(tuples), positive));
	}

	// ************************************************************************
	// ***** Constraint regular
	// ***************************** *******************************************

	@Override
	public CtrAlone regular(Var[] list, Automaton automaton) {
		return post(ICtrRegular.buildFrom(list, varEntities.compactOrdered(list),
				Stream.of(automaton.transitions).map(t -> t.toString()).collect(Collectors.joining()), automaton.startState, automaton.finalStates));
	}

	// ************************************************************************
	// ***** Constraint mdd
	// ************************************************************************

	@Override
	public CtrAlone mdd(Var[] list, Transition[] transitions) {
		return post(ICtrMdd.buildFrom(list, varEntities.compactOrdered(list), Stream.of(transitions).map(t -> t.toString()).collect(Collectors.joining())));
	}

	// ************************************************************************
	// ***** Constraint allDifferent
	// ************************************************************************

	@Override
	public CtrEntity allDifferent(Var[] list) {
		return post(ICtrAllDifferent.buildFrom(list, LIST, varEntities.compact(list), null));
	}

	@Override
	public CtrEntity allDifferent(VarSymbolic[] list) {
		return post(ICtrAllDifferent.buildFrom(list, LIST, varEntities.compact(list), null));
	}

	@Override
	public CtrEntity allDifferent(Var[] list, int[] exceptValues) {
		return post(ICtrAllDifferent.buildFrom(list, LIST, varEntities.compact(list), Utilities.join(exceptValues)));
	}

	@Override
	public CtrEntity allDifferentList(Var[]... lists) {
		return post(ICtrAllDifferent.buildFrom(vars(lists), LISTS, varEntities.compactOrdered(lists), null));
	}

	@Override
	public CtrEntity allDifferentMatrix(Var[][] matrix) {
		return post(ICtrAllDifferent.buildFrom(vars(matrix), MATRIX, varEntities.compactMatrix(matrix), null));
	}

	@Override
	public CtrEntity allDifferent(XNodeParent<IVar>[] trees) {
		String s = Stream.of(trees).map(t -> t.toString()).collect(Collectors.joining(" "));
		return post(ICtrAllDifferent.buildFrom(scope(Stream.of(trees).map(t -> t.vars())), LIST, s, null));
	}

	// ************************************************************************
	// ***** Constraint allEqual
	// ************************************************************************

	@Override
	public CtrEntity allEqual(Var... list) {
		return post(ICtrAllEqual.buildFrom(list, LIST, varEntities.compact(list)));
	}

	@Override
	public CtrEntity allEqual(VarSymbolic... list) {
		return post(ICtrAllEqual.buildFrom(list, LIST, varEntities.compact(list)));
	}

	@Override
	public CtrEntity allEqualList(Var[]... lists) {
		return post(ICtrAllEqual.buildFrom(vars(lists), LISTS, varEntities.compactOrdered(lists)));
	}

	// ************************************************************************
	// ***** Constraint ordered and lex
	// ************************************************************************

	@Override
	public CtrEntity ordered(Var[] list, int[] lengths, TypeOperatorRel operator) {
		return post(ICtrOrdered.buildFrom(list, LIST, varEntities.compactOrdered(list),
				IntStream.of(lengths).allMatch(v -> v == 0) ? null : Utilities.join(lengths), operator));
	}

	@Override
	public CtrEntity ordered(Var[] list, Var[] lengths, TypeOperatorRel operator) {
		return post(ICtrOrdered.buildFrom(list, LIST, varEntities.compactOrdered(list), varEntities.compactOrdered(lengths), operator));
	}

	@Override
	public CtrEntity lex(Var[][] lists, TypeOperatorRel operator) {
		return post(ICtrOrdered.buildFrom(vars(lists), LISTS, varEntities.compactOrdered(lists), null, operator));
	}

	@Override
	public CtrEntity lexMatrix(Var[][] matrix, TypeOperatorRel operator) {
		return post(ICtrOrdered.buildFrom(vars(matrix), MATRIX, varEntities.compactMatrix(matrix), null, operator));
	}

	// ************************************************************************
	// ***** Constraint sum
	// ************************************************************************

	@Override
	public CtrEntity sum(Var[] list, int[] coeffs, Condition condition) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null), "A variable is null");
		Utilities.control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		Var[] newList = api.select(list, i -> coeffs[i] != 0);
		int[] newCoeffs = api.selectFromIndexing(coeffs, i -> coeffs[i] != 0);
		Utilities.control(newList.length == newCoeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return post(ICtrSum.buildFrom(scope(newList, condition), varEntities.compactOrdered(newList),
				IntStream.range(0, newCoeffs.length).allMatch(i -> newCoeffs[i] == 1) ? null : Utilities.join(newCoeffs), condition));
	}

	@Override
	public CtrEntity sum(Var[] list, Var[] coeffs, Condition condition) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null) && Stream.of(coeffs).noneMatch(x -> x == null), "A variable is null");
		Utilities.control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return post(ICtrSum.buildFrom(scope(list, coeffs, condition), varEntities.compactOrdered(list), varEntities.compactOrdered(coeffs), condition));
	}

	@Override
	public CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, Condition condition) {
		Utilities.control(trees.length == coeffs.length, "Pb because the number of trees is different form the number of coefficients");
		String s = Stream.of(trees).map(t -> t.toString()).collect(Collectors.joining(" "));
		return post(ICtrSum.buildFrom(scope(Stream.of(trees).map(t -> t.vars()), condition), s,
				IntStream.range(0, coeffs.length).allMatch(i -> coeffs[i] == 1) ? null : Utilities.join(coeffs), condition));
	}

	// ************************************************************************
	// ***** Constraint count
	// ************************************************************************

	@Override
	public CtrEntity count(Var[] list, int[] values, Condition condition) {
		return post(ICtrCount.buildFrom(scope(list, condition), varEntities.compact(clean(list)), Utilities.join(values), condition));
	}

	@Override
	public CtrEntity count(Var[] list, Var[] values, Condition condition) {
		return post(ICtrCount.buildFrom(scope(list, values, condition), varEntities.compact(clean(list)), varEntities.compact(clean(values)), condition));
	}

	// ************************************************************************
	// ***** Constraint nValues
	// ************************************************************************

	@Override
	public CtrEntity nValues(Var[] list, Condition condition) {
		return post(ICtrNValues.buildFrom(scope(list, condition), varEntities.compact(clean(list)), null, condition));
	}

	@Override
	public CtrEntity nValues(Var[] list, Condition condition, int[] exceptValues) {
		return post(ICtrNValues.buildFrom(scope(list, condition), varEntities.compact(clean(list)), Utilities.join(exceptValues), condition));
	}

	// ************************************************************************
	// ***** Constraint cardinality
	// ************************************************************************

	@Override
	public CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, int[] occurs) {
		Utilities.control(values.length == occurs.length, "Arrays values and occurs have different length.");
		return post(ICtrCardinality.buildFrom(list, varEntities.compact(clean(list)), Utilities.join(values), mustBeClosed, Utilities.join(occurs)));
	}

	@Override
	public CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, Var[] occurs) {
		Utilities.control(values.length == occurs.length, "Arrays values and occurs have different length.");
		Utilities.control(Stream.of(occurs).noneMatch(x -> x == null), "A variable in array occurs is null");
		return post(ICtrCardinality.buildFrom(scope(list, occurs), varEntities.compact(clean(list)), Utilities.join(values), mustBeClosed,
				varEntities.compactOrdered(occurs)));
	}

	@Override
	public CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, int[] minOccurs, int[] maxOccurs) {
		Utilities.control(values.length == minOccurs.length && values.length == maxOccurs.length,
				"Arrays values, minOccurs and maxOccurs have different length.");
		return post(ICtrCardinality.buildFrom(list, varEntities.compact(clean(list)), Utilities.join(values), mustBeClosed,
				intervalAsString(minOccurs, maxOccurs)));
	}

	@Override
	public CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, int[] occurs) {
		Utilities.control(values.length == occurs.length, "Arrays values and occurs have different length.");
		Utilities.control(Stream.of(values).noneMatch(x -> x == null), "A variable in array values is null");
		return post(ICtrCardinality.buildFrom(scope(list, values), varEntities.compact(clean(list)), varEntities.compactOrdered(values), mustBeClosed,
				Utilities.join(occurs)));
	}

	@Override
	public CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, Var[] occurs) {
		Utilities.control(values.length == occurs.length, "Arrays values and occurs have different length.");
		Utilities.control(Stream.of(values).noneMatch(x -> x == null) && Stream.of(occurs).noneMatch(x -> x == null),
				"A variable in array values or occurs is null");
		return post(ICtrCardinality.buildFrom(scope(list, values, occurs), varEntities.compact(clean(list)), varEntities.compactOrdered(values), mustBeClosed,
				varEntities.compactOrdered(occurs)));
	}

	@Override
	public CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, int[] minOccurs, int[] maxOccurs) {
		Utilities.control(values.length == minOccurs.length && values.length == maxOccurs.length,
				"Arrays values, minOccurs and maxOccurs have different length.");
		Utilities.control(Stream.of(values).noneMatch(x -> x == null), "A variable in array values is null");
		return post(ICtrCardinality.buildFrom(scope(list, values), varEntities.compact(clean(list)), varEntities.compactOrdered(values), mustBeClosed,
				intervalAsString(minOccurs, maxOccurs)));
	}

	// ************************************************************************
	// ***** Constraint maximum
	// ************************************************************************

	@Override
	public CtrEntity maximum(Var[] list, Condition condition) {
		return post(ICtrMaximum.buildFrom(scope(list, condition), varEntities.compact(clean(list)), null, null, null, condition));

	}

	@Override
	public CtrEntity maximum(Var[] list, int startIndex, Var index, TypeRank rank) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null), "A variable in array list is null");
		return post(ICtrMaximum.buildFrom(scope(list, index), varEntities.compactOrdered(list), startIndex, index, rank, null));
	}

	@Override
	public CtrEntity maximum(Var[] list, int startIndex, Var index, TypeRank rank, Condition condition) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null), "A variable in array list is null");
		return post(ICtrMaximum.buildFrom(scope(list, index, condition), varEntities.compactOrdered(list), startIndex, index, rank, condition));
	}

	// ************************************************************************
	// ***** Constraint minimum
	// ************************************************************************

	@Override
	public CtrEntity minimum(Var[] list, Condition condition) {
		return post(ICtrMinimum.buildFrom(scope(list, condition), varEntities.compact(clean(list)), null, null, null, condition));

	}

	@Override
	public CtrEntity minimum(Var[] list, int startIndex, Var index, TypeRank rank) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null), "A variable in array list is null");
		return post(ICtrMinimum.buildFrom(scope(list, index), varEntities.compactOrdered(list), startIndex, index, rank, null));
	}

	@Override
	public CtrEntity minimum(Var[] list, int startIndex, Var index, TypeRank rank, Condition condition) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null), "A variable in array list is null");
		return post(ICtrMinimum.buildFrom(scope(list, index, condition), varEntities.compactOrdered(list), startIndex, index, rank, condition));
	}

	// ************************************************************************
	// ***** Constraint element
	// ************************************************************************

	@Override
	public CtrEntity element(Var[] list, int value) {
		return post(ICtrElement.buildFrom(list, varEntities.compact(list), null, null, null, value));
	}

	@Override
	public CtrEntity element(Var[] list, Var value) {
		return post(ICtrElement.buildFrom(scope(list, value), varEntities.compact(list), null, null, null, value));
	}

	@Override
	public CtrEntity element(Var[] list, int startIndex, Var index, TypeRank rank, int value) {
		return post(ICtrElement.buildFrom(scope(list, index), varEntities.compactOrdered(list), startIndex, index, rank, value));
	}

	@Override
	public CtrEntity element(Var[] list, int startIndex, Var index, TypeRank rank, Var value) {
		return post(ICtrElement.buildFrom(scope(list, index, value), varEntities.compactOrdered(list), startIndex, index, rank, value));
	}

	@Override
	public CtrEntity element(int[] list, int startIndex, Var index, TypeRank rank, Var value) {
		return post(ICtrElement.buildFrom(scope(index, value), Utilities.join(list), startIndex, index, rank, value));
	}

	// ************************************************************************
	// ***** Constraint channel
	// ************************************************************************

	@Override
	public CtrEntity channel(Var[] list, int startIndex) {
		return post(ICtrChannel.buildFrom(list, varEntities.compactOrdered(list), startIndex, null, null, null));
	}

	@Override
	public CtrEntity channel(Var[] list1, int startIndex1, Var[] list2, int startIndex2) {
		return post(ICtrChannel.buildFrom(scope(list1, list2), varEntities.compactOrdered(list1), startIndex1, varEntities.compactOrdered(list2), startIndex2,
				null));
	}

	@Override
	public CtrEntity channel(Var[] list, int startIndex, Var value) {
		return post(ICtrChannel.buildFrom(list, varEntities.compactOrdered(list), startIndex, null, null, value));
	}

	// ************************************************************************
	// ***** Constraint stretch
	// ************************************************************************

	@Override
	public CtrEntity stretch(Var[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		control(values.length == widthsMin.length && values.length == widthsMax.length, "The length of the arrays are not compatible.");
		control(IntStream.range(0, values.length).allMatch(i -> widthsMin[i] <= widthsMax[i]), "a min width is greater than a max width");
		control(patterns == null || Stream.of(patterns).allMatch(t -> t.length == 2), "");
		String t = patterns == null ? null : ICtrExtension.tableAsString(Table.clean(patterns));
		return post(ICtrStretch.buildFrom(list, varEntities.compactOrdered(list), Utilities.join(values), intervalAsString(widthsMin, widthsMax), t));
	}

	// ************************************************************************
	// ***** Constraint noOverlap
	// ************************************************************************

	@Override
	public CtrEntity noOverlap(Var[] origins, int[] lengths, boolean zeroIgnored) {
		return post(ICtrNoOverlap.buildFrom(origins, varEntities.compactOrdered(origins), Utilities.join(lengths), zeroIgnored));
	}

	@Override
	public CtrEntity noOverlap(Var[] origins, Var[] lengths, boolean zeroIgnored) {
		return post(ICtrNoOverlap.buildFrom(scope(origins, lengths), varEntities.compactOrdered(origins), varEntities.compactOrdered(lengths), zeroIgnored));
	}

	@Override
	public CtrEntity noOverlap(Var[][] origins, int[][] lengths, boolean zeroIgnored) {
		return post(ICtrNoOverlap.buildFrom(scope(origins, lengths), varEntities.compactMatrix(origins), "(" + Utilities.join(lengths, ")(", ",") + ")",
				zeroIgnored));
	}

	@Override
	public CtrEntity noOverlap(Var[][] origins, Var[][] lengths, boolean zeroIgnored) {
		return post(ICtrNoOverlap.buildFrom(scope(origins, lengths), varEntities.compactMatrix(origins), "(" + Utilities.join(lengths, ")(", ",") + ")",
				zeroIgnored));
	}

	// ************************************************************************
	// ***** Constraint cumulative
	// ************************************************************************

	@Override
	public final CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, int[] heights, Condition condition) {
		return post(ICtrCumulative.buildFrom(scope(origins, ends, condition), varEntities.compactOrdered(origins), Utilities.join(lengths),
				ends == null ? null : varEntities.compactOrdered(ends), Utilities.join(heights), condition));
	}

	@Override
	public final CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, int[] heights, Condition condition) {
		return post(ICtrCumulative.buildFrom(scope(origins, lengths, ends, condition), varEntities.compactOrdered(origins), varEntities.compactOrdered(lengths),
				ends == null ? null : varEntities.compactOrdered(ends), Utilities.join(heights), condition));
	}

	@Override
	public final CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, Var[] heights, Condition condition) {
		return post(ICtrCumulative.buildFrom(scope(origins, ends, heights, condition), varEntities.compactOrdered(origins), Utilities.join(lengths),
				ends == null ? null : varEntities.compactOrdered(ends), varEntities.compactOrdered(heights), condition));
	}

	@Override
	public final CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, Var[] heights, Condition condition) {
		return post(ICtrCumulative.buildFrom(scope(origins, lengths, ends, heights, condition), varEntities.compactOrdered(origins),
				varEntities.compactOrdered(lengths), ends == null ? null : varEntities.compactOrdered(ends), varEntities.compactOrdered(heights), condition));
	}

	// ************************************************************************
	// ***** Constraint circuit
	// ************************************************************************

	@Override
	public CtrEntity circuit(Var[] list, int startIndex) {
		return post(ICtrCircuit.buildFrom(list, varEntities.compactOrdered(list), startIndex, null));
	}

	@Override
	public CtrEntity circuit(Var[] list, int startIndex, int size) {
		return post(ICtrCircuit.buildFrom(list, varEntities.compactOrdered(list), startIndex, size));
	}

	@Override
	public CtrEntity circuit(Var[] list, int startIndex, Var size) {
		return post(ICtrCircuit.buildFrom(scope(list, size), varEntities.compactOrdered(list), startIndex, size));
	}

	// ************************************************************************
	// ***** Constraint clause
	// ************************************************************************

	@Override
	public CtrEntity clause(Var[] list, Boolean[] phases) {
		Utilities.control(Stream.of(list).noneMatch(x -> x == null), "A variable in array list is null");
		Utilities.control(list.length == phases.length && list.length > 0, "Bad form of clause.");
		String s = IntStream.range(0, list.length).mapToObj(i -> phases[i] ? list[i].id() : "not(" + list[i].id() + ")").collect(Collectors.joining(" "));
		return post(ICtrClause.buildFrom(list, s));
	}

	// ************************************************************************
	// ***** Constraint instantiation
	// ************************************************************************

	@Override
	public CtrEntity instantiation(Var[] list, int[] values) {
		Utilities.control(list.length == values.length && list.length > 0, "Bad form of instantiation.");
		return post(ICtrInstantiation.buildFrom(list, varEntities.compactOrdered(list), Utilities.join(values)));
	}

	// ************************************************************************
	// ***** Meta-Constraint slide
	// ************************************************************************

	private int[] computeOffsets(IVar[][] lists, IVar[] scp0, IVar[] scp1) {
		return IntStream.range(0, lists.length).map(i -> {
			int pos0 = Stream.of(scp0).filter(x -> Utilities.indexOf(x, lists[i]) >= 0).mapToInt(x -> Utilities.indexOf(x, lists[i])).min().orElse(-1);
			int pos1 = Stream.of(scp1).filter(x -> Utilities.indexOf(x, lists[i]) >= 0).mapToInt(x -> Utilities.indexOf(x, lists[i])).min().orElse(-1);
			Utilities.control(pos0 != -1 && pos1 != -1, "");
			return pos1 - pos0;
		}).toArray();
	}

	private int[] computeCollects(IVar[][] lists, IVar[] scp) {
		return IntStream.range(0, lists.length).map(i -> (int) Stream.of(scp).filter(x -> Utilities.indexOf(x, lists[i]) >= 0).count()).toArray();
	}

	@Override
	public CtrEntity slide(IVar[] list, Range range, IntFunction<CtrEntity> template) {
		Utilities.control(range.startInclusive == 0 && range.length() > 0, "Bad form of range");
		if (range.length() == 1)
			return template.apply(0);
		CtrAlone[] cas = range.stream().mapToObj(i -> template.apply(i)).toArray(CtrAlone[]::new);
		for (int i = cas.length - 1; i >= 0; i--) { // we remove them since a slide is posted (necessary for saving into XCSP3)
			ctrEntities.allEntities.remove(cas[i]);
			ctrEntities.ctrToCtrAlone.remove(cas[i].ctr);
		}
		IVar[][] scopes = Stream.of(cas).map(ca -> ca.ctr.scope()).toArray(IVar[][]::new);
		Utilities.control(IntStream.range(1, scopes.length).noneMatch(i -> scopes[i].length != scopes[0].length), "");
		IVar[][] lists = new IVar[][] { list };
		boolean circular = Stream.of(scopes[scopes.length - 1]).anyMatch(x -> x == lists[0][0]);
		int[] offsets = computeOffsets(lists, scopes[0], scopes[1]);
		int[] collects = computeCollects(lists, scopes[0]);
		// todo many other controls to do
		return post(ICtrSlide.buildFrom(list, circular, lists, offsets, collects, cas));
	}

	// ************************************************************************
	// ***** Meta-Constraint ifThen
	// ************************************************************************

	@Override
	public final CtrEntity ifThen(CtrEntity c1, CtrEntity c2) {
		Utilities.control(c1 instanceof CtrAlone && c2 instanceof CtrAlone, "unimplemented for the moment");
		for (CtrEntity c : new CtrEntity[] { c2, c1 }) { // we remove them
			ctrEntities.allEntities.remove(c);
			ctrEntities.ctrToCtrAlone.remove(((CtrAlone) c).ctr);
		}
		return post(ICtrIfThen.buildFrom(scope(((CtrAlone) c1).ctr.scope(), ((CtrAlone) c2).ctr.scope()), (CtrAlone) c1, (CtrAlone) c2));
	}

	// ************************************************************************
	// ***** Meta-Constraint ifThenElse
	// ************************************************************************

	@Override
	public final CtrEntity ifThenElse(CtrEntity c1, CtrEntity c2, CtrEntity c3) {
		Utilities.control(c1 instanceof CtrAlone && c2 instanceof CtrAlone && c3 instanceof CtrAlone, "unimplemented for the moment");
		for (CtrEntity c : new CtrEntity[] { c3, c2, c1 }) { // we remove them
			ctrEntities.allEntities.remove(c);
			ctrEntities.ctrToCtrAlone.remove(((CtrAlone) c).ctr);
		}
		return post(ICtrIfThenElse.buildFrom(scope(((CtrAlone) c1).ctr.scope(), ((CtrAlone) c2).ctr.scope(), ((CtrAlone) c3).ctr.scope()), (CtrAlone) c1,
				(CtrAlone) c2, (CtrAlone) c3));
	}

	// ************************************************************************
	// ***** Managing objectives
	// ************************************************************************

	public ObjEntity postObj(IObj o) {
		return objEntities.new ObjEntity(o);
	}

	@Override
	public final ObjEntity minimize(IVar x) {
		return postObj(IObjFunctional.buildFrom(scope(x), true, new XNodeLeaf<>(TypeExpr.VAR, x)));
	}

	@Override
	public final ObjEntity maximize(IVar x) {
		return postObj(IObjFunctional.buildFrom(scope(x), false, new XNodeLeaf<>(TypeExpr.VAR, x)));
	}

	@Override
	public final ObjEntity minimize(TypeObjective type, IVar[] list) {
		return postObj(IObjSpecialized.buildFrom(list, true, type, varEntities.compactOrdered(list), null));
	}

	@Override
	public final ObjEntity maximize(TypeObjective type, IVar[] list) {
		return postObj(IObjSpecialized.buildFrom(list, false, type, varEntities.compactOrdered(list), null));
	}

	@Override
	public final ObjEntity minimize(TypeObjective type, IVar[] list, int[] coeffs) {
		return postObj(IObjSpecialized.buildFrom(list, true, type, varEntities.compactOrdered(list), Utilities.join(coeffs)));
	}

	@Override
	public final ObjEntity maximize(TypeObjective type, IVar[] list, int[] coeffs) {
		return postObj(IObjSpecialized.buildFrom(list, false, type, varEntities.compactOrdered(list), Utilities.join(coeffs)));
	}
}
