package org.xcsp.modeler.definitions;

import static java.util.stream.Collectors.joining;
import static org.xcsp.modeler.definitions.IRootForCtrAndObj.map;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Constants;
import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.modeler.entities.CtrEntities.CtrAlone;

public interface ICtr extends IRootForCtrAndObj {
	String EXTENSION = TypeCtr.extension.name();
	String INTENSION = TypeCtr.intension.name();
	String SMART = TypeCtr.smart.name();
	String REGULAR = TypeCtr.regular.name();
	String GRAMMAR = TypeCtr.grammar.name();
	String MDD = TypeCtr.mdd.name();
	String ALL_DIFFERENT = TypeCtr.allDifferent.name();
	String ALL_EQUAL = TypeCtr.allEqual.name();
	String ALL_DISTANT = TypeCtr.allDistant.name();
	String ORDERED = TypeCtr.ordered.name();
	String LEX = TypeCtr.lex.name();
	String ALL_INCOMPARABLE = TypeCtr.allIncomparable.name();
	String SUM = TypeCtr.sum.name();
	String COUNT = TypeCtr.count.name();
	String NVALUES = TypeCtr.nValues.name();
	String CARDINALITY = TypeCtr.cardinality.name();
	String BALANCE = TypeCtr.balance.name();
	String SPREAD = TypeCtr.spread.name();
	String DEVIATION = TypeCtr.deviation.name();
	String SUM_COSTS = TypeCtr.sumCosts.name();
	String STRETCH = TypeCtr.stretch.name();
	String NO_OVERLAP = TypeCtr.noOverlap.name();
	String CUMULATIVE = TypeCtr.cumulative.name();
	String BIN_PACKING = TypeCtr.binPacking.name();
	String KNAPSACK = TypeCtr.knapsack.name();
	String NETWORK_FLOW = TypeCtr.networkFlow.name();
	String CIRCUIT = TypeCtr.circuit.name();
	String NCIRCUITS = TypeCtr.nCircuits.name();
	String PÄTH = TypeCtr.path.name();
	String NPATHS = TypeCtr.nPaths.name();
	String TREE = TypeCtr.tree.name();
	String NTREES = TypeCtr.nTrees.name();
	String ARBO = TypeCtr.arbo.name();
	String NARBOS = TypeCtr.nArbos.name();
	String NCLIQUES = TypeCtr.nCliques.name();
	String CLAUSE = TypeCtr.clause.name();
	String INSTANTIATION = TypeCtr.instantiation.name();
	String ALL_INTERSECTING = TypeCtr.allIntersecting.name();
	String RANGE = TypeCtr.range.name();
	String ROOTS = TypeCtr.roots.name();
	String PARTITION = TypeCtr.partition.name();
	String MINIMUM = TypeCtr.minimum.name();
	String MAXIMUM = TypeCtr.maximum.name();
	String ELEMENT = TypeCtr.element.name();
	String CHANNEL = TypeCtr.channel.name();
	String PERMUTATION = TypeCtr.permutation.name();
	String PRECEDENCE = TypeCtr.precedence.name();
	String AND = TypeCtr.and.name();
	String OR = TypeCtr.or.name();
	String NOT = TypeCtr.not.name();
	String IF_THEN = TypeCtr.ifThen.name();
	String IF_THEN_ELSE = TypeCtr.ifThenElse.name();
	String SLIDE = TypeCtr.slide.name();
	String SEQBIN = TypeCtr.seqbin.name();

	String LIST = TypeChild.list.name();
	String SET = TypeChild.set.name();
	String MSET = TypeChild.mset.name();
	String MATRIX = TypeChild.matrix.name();
	String FUNCTION = TypeChild.function.name();
	String SUPPORTS = TypeChild.supports.name();
	String CONFLICTS = TypeChild.conflicts.name();
	String EXCEPT = TypeChild.except.name();
	String VALUE = TypeChild.value.name();
	String VALUES = TypeChild.values.name();
	String TOTAL = TypeChild.total.name();
	String COEFFS = TypeChild.coeffs.name();
	String CONDITION = TypeChild.condition.name();
	String COST = TypeChild.cost.name();
	String OPERATOR = TypeChild.operator.name();
	String NUMBER = TypeChild.number.name();
	String TRANSITIONS = TypeChild.transitions.name();
	String START = TypeChild.start.name();
	String FINAL = TypeChild.FINAL.name().toLowerCase(); // upper-cased because a keyword
	String TERMINAL = TypeChild.terminal.name();
	String RULES = TypeChild.rules.name();
	String INDEX = TypeChild.index.name();
	String MAPPING = TypeChild.mapping.name();
	String OCCURS = TypeChild.occurs.name();
	String ROW_OCCURS = TypeChild.rowOccurs.name();
	String COL_OCCURS = TypeChild.colOccurs.name();
	String WIDTHS = TypeChild.widths.name();
	String PATTERNS = TypeChild.patterns.name();
	String ORIGINS = TypeChild.origins.name();
	String LENGTHS = TypeChild.lengths.name();
	String ENDS = TypeChild.ends.name();
	String HEIGHTS = TypeChild.heights.name();
	String MACHINES = TypeChild.machines.name();
	String CONDITIONS = TypeChild.conditions.name();
	String SIZES = TypeChild.sizes.name();
	String WEIGHTS = TypeChild.weights.name();
	String PROFITS = TypeChild.profits.name();
	String LIMIT = TypeChild.limit.name();
	String SIZE = TypeChild.size.name();
	String ROOT = TypeChild.root.name();
	String IMAGE = TypeChild.image.name();
	String GRAPH = TypeChild.graph.name();
	String ROW = TypeChild.row.name();

	String ARITY = "arity";
	String TUPLES = "tuples";
	String POSITIVE = "positive";
	String START_INDEX = "startIndex";
	String RANK = "rank";
	String CLOSED = "closed";
	String START_INDEX2 = "startIndex2";
	String LIST2 = "list2";
	String LISTS = "lists";
	String SETS = "sets";
	String MSETS = "msets";
	String ROWS = "rows";
	String CIRCULAR = "circular";
	String OFFSETS = "offsets";
	String COLLECTS = "collects";
	String ALONES = "alones";
	String ZERO_IGNORED = "zeroIgnored";
	String REC = "rec";

	default Class<?> findInterfaceFor(Class<?> c) {
		if (c == null)
			return null;
		if (c.isInterface() && ICtr.class.isAssignableFrom(c))
			return c;
		Class<?> c1 = Stream.of(c.getInterfaces()).map(cc -> findInterfaceFor(cc)).filter(cc -> cc != null).findFirst().orElse(null);
		if (c1 != null)
			return c1;
		return findInterfaceFor(c.getSuperclass());
	}

	@Override
	default DefXCSP defXCSP() {
		Class<?> cc = findInterfaceFor(this.getClass());
		String s = cc.getSimpleName();
		String name = Character.toLowerCase(s.charAt(4)) + s.substring(5);
		DefXCSP def = def(name);
		Map<String, Object> map = mapXCSP(); // store it to avoid building it several times
		String[] keys = map.keySet().stream().filter(key -> key != null && !key.equals(SCOPE)).toArray(String[]::new);
		// System.out.println("KEYS=" + Utilities.join(keys));
		for (int i = 0; i < keys.length; i++)
			def.addOne(keys[i]);
		return def;
	}

	public interface ICtrIntension extends ICtr {

		static ICtrIntension buildFrom(IVar[] scope, XNodeParent<IVar> tree) {
			return new ICtrIntension() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, FUNCTION, tree); // making it canonical ?
				}
			};
		}
	}

	public interface ICtrExtension extends ICtr {

		static ICtrExtension buildFrom(IVar[] scope, String list, int arity, int[][] tuples, boolean positive) {
			return new ICtrExtension() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, ARITY, arity, TUPLES, tuples, POSITIVE, positive);
				}
			};
		}

		static ICtrExtension buildFrom(IVar[] scope, String list, int arity, String[][] tuples, boolean positive) {
			return new ICtrExtension() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, ARITY, arity, TUPLES, tuples, POSITIVE, positive);
				}
			};
		}

		static String tableAsString(int[][] tuples) {
			if (tuples.length == 0)
				return "";
			if (tuples[0].length == 1)
				return Dom.compactFormOf(Stream.of(tuples).mapToInt(t -> t[0]).toArray()); // * can't be present if 1-ary
			StringBuilder sb = new StringBuilder(); // save cpu time compared to using a stream
			for (int[] t : tuples) {
				sb.append("(");
				for (int i = 0; i < t.length; i++) {
					sb.append(t[i] == Constants.STAR ? "*" : t[i]);
					sb.append(i < t.length - 1 ? "," : "");
				}
				sb.append(")");
			}
			return sb.toString();
			// return Stream.of(tuples).map(t -> "(" + IntStream.of(t).mapToObj(v -> v == Constants.STAR_INT ? "*" : v + "").collect(joining(",")) +
			// ")").collect(joining());
		}

		static String tableAsString(String[][] tuples) {
			if (tuples.length == 0)
				return "";
			if (tuples[0].length == 1)
				return Stream.of(tuples).map(t -> t[0]).collect(joining(" "));
			return Stream.of(tuples).map(t -> "(" + Stream.of(t).collect(joining(",")) + ")").collect(joining());
		}

		default boolean isSimilarTo(ICtrExtension c) {
			Map<String, Object> map = mapXCSP(), mapc = c.mapXCSP();
			if (!map.get(ARITY).equals(mapc.get(ARITY)))
				return false;
			if (!map.get(POSITIVE).equals(mapc.get(POSITIVE)))
				return false;
			if (map.get(TUPLES).getClass() != mapc.get(TUPLES).getClass())
				return false;
			if (map.get(TUPLES) instanceof int[][]) {
				int[][] t1 = (int[][]) map.get(TUPLES), t2 = (int[][]) mapc.get(TUPLES);
				if (t1.length != t2.length)
					return false;
				if (t1 == t2 || t1.length == 0)
					return true; // may be the case when the same array has been associated with several table constraints
				if (t1.length > 50000) // hard coding ; limit for search
					return false;
				// System.out.println(mapXCSP().get(ARITY) + " vs " + c.mapXCSP().get(ARITY) + "\nt1= " + Kit.join(t1) + "\nt2= " +
				// Kit.join(t2));
				int arity = t1[0].length;
				for (int i = t1.length - 1; i >= 0; i--)
					for (int j = arity - 1; j >= 0; j--)
						if (t1[i][j] != t2[i][j])
							return false;
			} else {
				String[][] t1 = (String[][]) map.get(TUPLES), t2 = (String[][]) mapc.get(TUPLES);
				if (t1.length != t2.length)
					return false;
				if (t1 == t2 || t1.length == 0)
					return true; // may be the case when the same array has been associated with several table constraints
				if (t1.length > 50000) // hard coding ; limit for search
					return false;
				// System.out.println(mapXCSP().get(ARITY) + " vs " + c.mapXCSP().get(ARITY) + "\nt1= " + Kit.join(t1) + "\nt2= " +
				// Kit.join(t2));
				int arity = t1[0].length;
				for (int i = t1.length - 1; i >= 0; i--)
					for (int j = arity - 1; j >= 0; j--)
						if (!t1[i][j].equals(t2[i][j]))
							return false;
			}
			return true;
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(EXTENSION).add(LIST);
			Object tuples = def.map.get(TUPLES) instanceof int[][] ? tableAsString((int[][]) def.map.get(TUPLES))
					: tableAsString((String[][]) def.map.get(TUPLES));
			return def.addSon(((Boolean) def.map.get(POSITIVE)) ? SUPPORTS : CONFLICTS, tuples);
		}
	}

	public interface ICtrRegular extends ICtr {
		static ICtrRegular buildFrom(IVar[] scope, String list, String transitions, String startState, String[] finalStates) {
			return new ICtrRegular() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, TRANSITIONS, transitions, START, startState, FINAL, Utilities.join(finalStates));
				}
			};
		}
	}

	public interface ICtrMdd extends ICtr {
		static ICtrMdd buildFrom(IVar[] scope, String list, String transitions) {
			return new ICtrMdd() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, TRANSITIONS, transitions);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			return def(MDD).add(LIST, TRANSITIONS);
		}
	}

	public interface ICtrAllDifferent extends ICtr {
		static ICtrAllDifferent buildFrom(IVar[] scope, String key1, Object value1, String except) {
			Utilities.control(except == null || except.length() > 0, "Pb with except values");
			return new ICtrAllDifferent() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, key1, value1, EXCEPT, except);
				}
			};
		}
	}

	public interface ICtrAllEqual extends ICtr {
		static ICtrAllEqual buildFrom(IVar[] scope, String key, Object value) {
			return new ICtrAllEqual() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, key, value);
				}
			};
		}
	}

	public interface ICtrOrdered extends ICtr {
		static ICtrOrdered buildFrom(IVar[] scope, String key1, Object value1, Object lengths, TypeOperatorRel operator) {
			return new ICtrOrdered() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, key1, value1, LENGTHS, lengths, OPERATOR, operator.name().toLowerCase());
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(mapXCSP().containsKey(LISTS) || mapXCSP().containsKey(MATRIX) ? LEX : ORDERED).addListOrLifted();
			return def.add(LENGTHS, OPERATOR);
		}
	}

	public interface ICtrSum extends ICtr {
		static ICtrSum buildFrom(IVar[] scope, String list, Object coeffs, Condition condition) {
			return new ICtrSum() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, COEFFS, coeffs, CONDITION, condition);
				}
			};
		}
	}

	public interface ICtrCount extends ICtr {
		static ICtrCount buildFrom(IVar[] scope, String list, Object values, Condition condition) {
			return new ICtrCount() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, VALUES, values, CONDITION, condition);
				}
			};
		}
	}

	public interface ICtrNValues extends ICtr {
		static ICtrNValues buildFrom(IVar[] scope, String list, String except, Condition condition) {
			return new ICtrNValues() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, EXCEPT, except, CONDITION, condition);
				}
			};
		}
	}

	public interface ICtrCardinality extends ICtr {
		static ICtrCardinality buildFrom(IVar[] scope, String list, String values, Boolean closed, String occurs) {
			return new ICtrCardinality() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, VALUES, values, CLOSED, closed, OCCURS, occurs);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(CARDINALITY).add(LIST);
			if (def.map.get(CLOSED) == null || ((Boolean) def.map.get(CLOSED)) == false)
				def.add(VALUES);
			else
				def.addSon(VALUES, def.map.get(VALUES), CLOSED, def.map.get(CLOSED));
			return def.add(OCCURS);
		}
	}

	public interface ICtrMaximum extends ICtr {
		static ICtrMaximum buildFrom(IVar[] scope, String list, Integer startIndex, Object index, TypeRank rank, Condition condition) {
			return new ICtrMaximum() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, START_INDEX, startIndex, INDEX, index, RANK, rank, CONDITION, condition);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(MAXIMUM);
			if (def.map.get(START_INDEX) == null || ((Integer) def.map.get(START_INDEX)) == 0)
				def.add(LIST);
			else
				def.addSon(LIST, def.map.get(LIST), START_INDEX, def.map.get(START_INDEX));
			if (def.map.containsKey(INDEX))
				if (def.map.get(RANK) == null || ((TypeRank) def.map.get(RANK)) == TypeRank.ANY)
					def.add(INDEX);
				else
					def.addSon(INDEX, def.map.get(INDEX), RANK, ((TypeRank) def.map.get(RANK)).name().toLowerCase());
			return def.map.containsKey(CONDITION) ? def.add(CONDITION) : def;
		}
	}

	public interface ICtrMinimum extends ICtr {
		static ICtrMinimum buildFrom(IVar[] scope, String list, Integer startIndex, Object index, TypeRank rank, Condition condition) {
			return new ICtrMinimum() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, START_INDEX, startIndex, INDEX, index, RANK, rank, CONDITION, condition);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(MINIMUM);
			if (def.map.get(START_INDEX) == null || ((Integer) def.map.get(START_INDEX)) == 0)
				def.add(LIST);
			else
				def.addSon(LIST, def.map.get(LIST), START_INDEX, def.map.get(START_INDEX));
			if (def.map.containsKey(INDEX))
				if (def.map.get(RANK) == null || ((TypeRank) def.map.get(RANK)) == TypeRank.ANY)
					def.add(INDEX);
				else
					def.addSon(INDEX, def.map.get(INDEX), RANK, ((TypeRank) def.map.get(RANK)).name().toLowerCase());
			return def.map.containsKey(CONDITION) ? def.add(CONDITION) : def;
		}
	}

	public interface ICtrElement extends ICtr {
		static ICtrElement buildFrom(IVar[] scope, String list, Integer startIndex, Object index, TypeRank rank, Object value) {
			return new ICtrElement() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, START_INDEX, startIndex, INDEX, index, RANK, rank, VALUE, value);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(ELEMENT);
			if (def.map.get(START_INDEX) == null || ((Integer) def.map.get(START_INDEX)) == 0)
				def.add(LIST);
			else
				def.addSon(LIST, def.map.get(LIST), START_INDEX, def.map.get(START_INDEX));
			if (def.map.get(RANK) == null || ((TypeRank) def.map.get(RANK)) == TypeRank.ANY)
				def.add(INDEX);
			else
				def.addSon(INDEX, def.map.get(INDEX), RANK, ((TypeRank) def.map.get(RANK)).name().toLowerCase());
			return def.add(VALUE);
		}
	}

	public interface ICtrChannel extends ICtr {
		static ICtrChannel buildFrom(IVar[] scope, String list, Integer startIndex, String list2, Integer startIndex2, Object value) {
			return new ICtrChannel() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, START_INDEX, startIndex, LIST2, list2, START_INDEX2, startIndex2, VALUE, value);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(CHANNEL);
			if (def.map.get(START_INDEX) == null || ((Integer) def.map.get(START_INDEX)) == 0)
				def.add(LIST);
			else
				def.addSon(LIST, def.map.get(LIST), START_INDEX, def.map.get(START_INDEX));
			if (def.map.containsKey(LIST2))
				if (def.map.get(START_INDEX2) == null || ((Integer) def.map.get(START_INDEX2)) == 0)
					def.addSon(LIST, def.map.get(LIST2));
				else
					def.addSon(LIST, def.map.get(LIST2), START_INDEX, def.map.get(START_INDEX2));
			if (def.map.containsKey(VALUE))
				def.add(VALUE);
			return def;
		}
	}

	public interface ICtrStretch extends ICtr {

		static ICtrStretch buildFrom(IVar[] scope, String list, String values, String widths, String patterns) {
			return new ICtrStretch() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, VALUES, values, WIDTHS, widths, PATTERNS, patterns);
				}
			};
		}
	}

	public interface ICtrNoOverlap extends ICtr {

		static ICtrNoOverlap buildFrom(IVar[] scope, String origins, String lengths, Boolean zeroIgnored) {
			return new ICtrNoOverlap() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, ORIGINS, origins, LENGTHS, lengths, ZERO_IGNORED, zeroIgnored);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(NO_OVERLAP).add(ORIGINS, LENGTHS);
			if (def.map.get(ZERO_IGNORED) != null && !(Boolean) def.map.get(ZERO_IGNORED))
				def.attributes.add(new SimpleEntry<>(ZERO_IGNORED, Boolean.FALSE));
			return def;
		}
	}

	public interface ICtrCumulative extends ICtr {

		static ICtrCumulative buildFrom(IVar[] scope, String origins, String lengths, String ends, String heights, Condition condition) {
			return new ICtrCumulative() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, ORIGINS, origins, LENGTHS, lengths, ENDS, ends, HEIGHTS, heights, CONDITION, condition);
				}
			};
		}
	}

	public interface ICtrCircuit extends ICtr {

		static ICtrCircuit buildFrom(IVar[] scope, String list, Integer startIndex, Object size) {
			return new ICtrCircuit() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, START_INDEX, startIndex, SIZE, size);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(CIRCUIT);
			if (def.map.get(START_INDEX) == null || ((Integer) def.map.get(START_INDEX)) == 0)
				def.add(LIST);
			else
				def.addSon(LIST, def.map.get(LIST), START_INDEX, def.map.get(START_INDEX));
			return def.add(SIZE);
		}
	}

	public interface ICtrClause extends ICtr {

		static ICtrClause buildFrom(IVar[] scope, String list) {
			return new ICtrClause() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list);
				}
			};
		}
	}

	public interface ICtrInstantiation extends ICtr {
		static ICtrInstantiation buildFrom(IVar[] scope, String list, String values) {
			return new ICtrInstantiation() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, VALUES, values);
				}
			};
		}
	}

	public interface ICtrSmart extends ICtr {

		static ICtrSmart buildFrom(IVar[] scope, String list, String[] rows) {
			return new ICtrSmart() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, LIST, list, ROWS, rows);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(SMART).add(LIST);
			String[] rows = (String[]) def.map.get(ROWS);
			Stream.of(rows).forEach(o -> def.addSon(ROW, o));
			return def;
		}
	}

	// ************************************************************************
	// ***** Meta-constraints
	// ************************************************************************

	public interface Meta {
	}

	public interface ICtrSlide extends ICtr, Meta {

		static ICtrSlide buildFrom(IVar[] scope, Boolean circular, IVar[][] lists, int[] offsets, int[] collects, CtrAlone[] cas) {
			return new ICtrSlide() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, CIRCULAR, circular, LISTS, lists, OFFSETS, offsets, COLLECTS, collects, ALONES, cas);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			// code managed in XCSP3Builder
			throw new RuntimeException();
		}
	}

	public interface ICtrIfThen extends ICtr, Meta {

		static ICtrIfThen buildFrom(IVar[] scope, CtrAlone ca1, CtrAlone ca2) {
			return new ICtrIfThen() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, ALONES, new CtrAlone[] { ca1, ca2 });
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(IF_THEN);
			CtrAlone[] cas = (CtrAlone[]) def.map.get(ALONES);
			def.addSon(REC, cas[0]);
			def.addSon(REC, cas[1]);
			return def;
		}
	}

	public interface ICtrIfThenElse extends ICtr, Meta {

		static ICtrIfThenElse buildFrom(IVar[] scope, CtrAlone ca1, CtrAlone ca2, CtrAlone ca3) {
			return new ICtrIfThenElse() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, ALONES, new CtrAlone[] { ca1, ca2, ca3 });
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			DefXCSP def = def(IF_THEN_ELSE);
			CtrAlone[] cas = (CtrAlone[]) def.map.get(ALONES);
			def.addSon(REC, cas[0]);
			def.addSon(REC, cas[1]);
			def.addSon(REC, cas[2]);
			return def;
		}
	}

}