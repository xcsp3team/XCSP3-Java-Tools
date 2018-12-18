package org.xcsp.modeler.entities;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.IVar.VarSymbolic;
import org.xcsp.common.Size;
import org.xcsp.common.Size.Size1D;
import org.xcsp.common.Size.Size2D;
import org.xcsp.common.Size.Size3D;
import org.xcsp.common.Size.Size4D;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeVar;
import org.xcsp.common.Utilities;
import org.xcsp.common.enumerations.EnumerationCartesian;
import org.xcsp.modeler.implementation.ProblemIMP;

public final class VarEntities {

	private ProblemIMP imp;

	public VarEntities(ProblemIMP imp) {
		this.imp = imp;
	}

	public List<VarEntity> allEntities = new ArrayList<>();

	public List<VarAlone> varAlones = new ArrayList<>();
	public List<VarArray> varArrays = new ArrayList<>();

	public Map<IVar, VarAlone> varToVarAlone = new LinkedHashMap<>();
	public Map<IVar, VarArray> varToVarArray = new LinkedHashMap<>();

	public Map<VarEntity, Integer> buildTimes = new HashMap<>();

	// public boolean isVarAlone(IVar x) { return varToVarArray.get(x) != null; }

	public void newVarAloneEntity(String id, IVar var, String note, TypeClass... classes) {
		VarAlone va = new VarAlone(id, var, note, classes);
		allEntities.add(va);
		varAlones.add(va);
		varToVarAlone.put(var, va);
		int limit = imp.stackLoops.size() > 0 ? imp.stackLoops.peek() : -1;
		if (limit != -1)
			buildTimes.put(va, limit);
	}

	public VarArray buildVarArray(String id, Size size, Object vars, String note, TypeClass... classes) {
		if (size instanceof Size1D)
			return new VarArray1D(id, (IVar[]) vars, note, classes);
		if (size instanceof Size2D)
			return new VarArray2D(id, (IVar[][]) vars, note, classes);
		if (size instanceof Size3D)
			return new VarArray3D(id, (IVar[][][]) vars, note, classes);
		if (size instanceof Size4D)
			return new VarArray4D(id, (IVar[][][][]) vars, note, classes);
		return new VarArray5D(id, (IVar[][][][][]) vars, note, classes);
	}

	public void newVarArrayEntity(String id, Size size, Object vars, String note, TypeClass... classes) {
		VarArray va = buildVarArray(id, size, vars, note, classes);
		allEntities.add(va);
		varArrays.add(va);
		if (va.flatVars != null) // null can be the case if the array has no useful variables
			Stream.of(va.flatVars).forEach(x -> varToVarArray.put(x, va));
		int limit = imp.stackLoops.size() > 0 ? imp.stackLoops.peek() : -1;
		if (limit != -1)
			buildTimes.put(va, limit);
	}

	public abstract class VarEntity extends ModelingEntity {

		protected VarEntity(String id, String note, TypeClass[] classes) {
			super(id, note, classes);
		}

		public abstract TypeVar getType();
	}

	public final class VarAlone extends VarEntity {
		public final IVar var;

		protected VarAlone(String id, IVar var, String note, TypeClass... classes) {
			super(id, note, classes);
			this.var = var;
		}

		@Override
		public TypeVar getType() {
			return var instanceof Var ? TypeVar.integer : var instanceof VarSymbolic ? TypeVar.symbolic : null;
		}
	}

	public abstract class VarArray extends VarEntity {

		public final int[] sizes;
		final int[] mins, maxs; // used for computing ranges of indexes at each dimension

		public Object vars;
		public final IVar[] flatVars;

		public String getStringSize() {
			return Arrays.stream(sizes).mapToObj(s -> "[" + s + "]").reduce("", (s, t) -> s + t);
		}

		@Override
		public TypeVar getType() {
			return flatVars[0] instanceof Var ? TypeVar.integer : flatVars[0] instanceof VarSymbolic ? TypeVar.symbolic : null;
		}

		protected VarArray(String id, int[] sizes, String note, TypeClass[] classes, Object vars) {
			super(id, note, classes);
			this.sizes = sizes;
			this.mins = new int[sizes.length];
			this.maxs = new int[sizes.length];
			this.vars = vars;
			this.flatVars = Utilities.collect(IVar.class, vars);
			Utilities.control(Utilities.isRegular(vars), "Not regular arrays");
		}

		private int updateWith(int increment, int dimension, int i) {
			mins[dimension] = Math.min(mins[dimension], i);
			maxs[dimension] = Math.max(maxs[dimension], i);
			return increment;
		}

		// protected int updateRangesOLD(Object array, IVar[] t, int dimIndex) {
		// Object[] vars = (Object[]) array;
		// if (dimIndex == sizes.length - 1) {
		// int sum = 0;
		// for (int i = 0; i < vars.length; i++)
		// if (Utilities.indexOf(vars[i], t) != -1)
		// sum += updateWith(1, dimIndex, i);
		// return sum;
		// // return IntStream.range(0, vars.length).filter(i -> Utilities.indexOf(vars[i], t) != -1).map(i -> updateWith(1,
		// // dimensions[dimIndex], i)).sum();
		// } else {
		// int nFound = 0;
		// for (int i = 0; i < vars.length; i++) {
		// int nb = updateRangesOLD(vars[i], t, dimIndex + 1);
		// if (nb > 0)
		// nFound += updateWith(nb, dimIndex, i);
		// }
		// return nFound;
		// }
		// }

		protected void updateRanges(IVar[] t) {
			for (IVar x : t) {
				int[] dims = Utilities.splitToInts(x.id().substring(x.id().indexOf('[')), "\\[|\\]");
				for (int i = 0; i < dims.length; i++)
					updateWith(0, i, dims[i]);
			}
		}

		protected String compactFormOf(IVar[] t) {
			assert IntStream.range(0, t.length).noneMatch(i -> IntStream.range(i + 1, t.length).anyMatch(j -> t[i] == t[j]));
			if (Utilities.indexOf(t[0], flatVars) == -1) // quick test: is the first variable in flatVars ?
				return null;
			Arrays.fill(mins, Integer.MAX_VALUE);
			Arrays.fill(maxs, -1);
			updateRanges(t);
			// int nFound = updateRangesOLD(vars, t, 0);
			// if (nFound != t.length) return null;
			int size = 1;
			for (int i = 0; i < mins.length; i++)
				size *= (maxs[i] - mins[i] + 1);
			// System.out.println("Size" + size + " t.length " + t.length);
			if (size != t.length)
				return null;
			String s = id;
			for (int i = 0; i < mins.length; i++)
				s += "[" + (mins[i] == 0 && maxs[i] == sizes[i] - 1 ? "" : mins[i] == maxs[i] ? mins[i] + "" : mins[i] + ".." + maxs[i]) + "]";
			return s;
		}
	}

	class VarArray1D extends VarArray {
		public VarArray1D(String id, IVar[] vars, String note, TypeClass... classes) {
			super(id, new int[] { vars.length }, note, classes, vars);
		}
	}

	class VarArray2D extends VarArray {
		protected VarArray2D(String id, IVar[][] vars, String note, TypeClass... classes) {
			super(id, new int[] { vars.length, vars[0].length }, note, classes, vars);
		}
	}

	class VarArray3D extends VarArray {
		protected VarArray3D(String id, IVar[][][] vars, String note, TypeClass... classes) {
			super(id, new int[] { vars.length, vars[0].length, vars[0][0].length }, note, classes, vars);
		}
	}

	class VarArray4D extends VarArray {
		protected VarArray4D(String id, IVar[][][][] vars, String note, TypeClass... classes) {
			super(id, new int[] { vars.length, vars[0].length, vars[0][0].length, vars[0][0][0].length }, note, classes, vars);
		}
	}

	class VarArray5D extends VarArray {
		protected VarArray5D(String id, IVar[][][][][] vars, String note, TypeClass... classes) {
			super(id, new int[] { vars.length, vars[0].length, vars[0][0].length, vars[0][0][0].length, vars[0][0][0][0].length }, note, classes, vars);
		}
	}

	private final class SequenceOfSuccessiveVariables {
		private IVar firstVar; // the first variable of the sequence
		private String prefix; // the prefix of the id of the variables (in case of an array)
		private int[] starts; // the indexes of the first variable (in case of an array)
		private int posMod = -1, stopMod;

		SequenceOfSuccessiveVariables(IVar var) {
			this.firstVar = var;
			String id = var.id();
			if (id.indexOf('[') != -1) {
				this.prefix = id.substring(0, id.indexOf('['));
				this.starts = Utilities.splitToInts(id.substring(id.indexOf('[')), "\\[|\\]");
			}
		}

		int differJustAt(int[] t) {
			int pos = -1;
			for (int i = 0; i < starts.length; i++)
				if (starts[i] != t[i])
					if (pos == -1)
						pos = i;
					else
						return -1; // at least two differences
			return pos;
		}

		boolean canBeExtendedWith(String id) {
			if (id.indexOf('[') == -1 || prefix == null || !prefix.equals(id.substring(0, id.indexOf('['))))
				return false;
			int[] t = Utilities.splitToInts(id.substring(id.indexOf('[')), "\\[|\\]");
			int pos = differJustAt(t);
			if (pos == -1)
				return false;
			if (posMod == -1) {
				if (t[pos] != starts[pos] + 1)
					return false;
				posMod = pos;
				stopMod = t[pos];
			} else {
				if (pos != posMod)
					return false;
				if (t[pos] != stopMod + 1)
					return false;
				stopMod = t[pos];
			}
			return true;
		}

		@Override
		public String toString() {
			if (prefix == null)
				return firstVar.id();
			String s = prefix;
			for (int i = 0; i < starts.length; i++)
				if (posMod != i)
					s += "[" + starts[i] + "]";
				else if (starts[posMod] == 0 && varToVarArray.get(firstVar) != null && stopMod == varToVarArray.get(firstVar).sizes[posMod] - 1)
					s += "[]";
				else // if (stopMod != starts[i] + 1 || starts.length > 1)
					s += "[" + starts[i] + ".." + stopMod + "]";
			// else if (stopMod == starts[i] + 1)
			// else s = s + "[" + starts[i] + "] " + s + "[" + stopMod + "]";
			return s;
		}
	}

	private String expand(String compactForm) {
		Utilities.control(compactForm.indexOf(' ') == -1, "The specified string must correspond to a single token; bad form : " + compactForm);
		if (compactForm.endsWith(")"))
			return compactForm; // this means that we have an expression (predicate) here
		int pos = compactForm.indexOf("[");
		if (pos == -1) { // we have just a single variable
			VarAlone va = varAlones.stream().filter(a -> a.id.equals(compactForm)).findAny().orElse(null);
			Utilities.control(va != null, "An object VarAlone should have been found");
			return compactForm; // id of a single variable
		}

		// we have an array
		String prefix = compactForm.substring(0, pos), suffix = compactForm.substring(pos);
		VarArray va = varArrays.stream().filter(a -> a.id.equals(prefix)).findAny().orElse(null);
		Utilities.control(va != null, "Pb with " + compactForm);
		List<String> list = new ArrayList<>();
		while (suffix.length() > 0) {
			pos = suffix.indexOf("]");
			list.add(suffix.substring(1, pos));
			suffix = suffix.substring(pos + 1);
		}
		String[] tokens = list.toArray(new String[0]);
		Utilities.control(tokens.length == va.sizes.length, prefix + " ");
		int[] mins = new int[tokens.length], maxs = new int[tokens.length], sizes = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].length() == 0) {
				mins[i] = 0;
				maxs[i] = va.sizes[i] - 1;
			} else if (Utilities.isInteger(tokens[i]))
				mins[i] = maxs[i] = Integer.parseInt(tokens[i]);
			else {
				StringTokenizer st = new StringTokenizer(tokens[i], "..");
				mins[i] = Integer.parseInt(st.nextToken());
				maxs[i] = Integer.parseInt(st.nextToken());
			}
			sizes[i] = maxs[i] - mins[i] + 1;
		}
		String s = "";
		EnumerationCartesian ec = new EnumerationCartesian(sizes);
		while (ec.hasNext()) {
			int[] t = ec.next();
			s += prefix + IntStream.range(0, t.length).mapToObj(i -> "[" + (mins[i] + t[i]) + "]").collect(joining()) + " ";
		}
		return s.trim();

		// String s2 = ""; int[] t = new int[sizes.length]; boolean hasNext = true;
		// while (hasNext) { String ss = prefix; for (int i = 0; i < t.length; i++) ss += "[" + (mins[i] + t[i]) + "]";
		// s2 += ss + " "; hasNext = false;
		// for (int i = t.length - 1; !hasNext && i >= 0; i--) if (t[i] + 1 < sizes[i]) { t[i]++; hasNext = true; } else t[i] = 0; }
		// System.out.println("s2=" + s2);
		// return s2.trim();
	}

	private StringBuilder handlePart(StringBuilder sb, List<IVar> list, boolean preserveOrder) {
		if (list.size() == 1)
			return sb.append(" " + list.get(0).id());
		if (list.size() == 2)
			return sb.append(" " + list.get(0).id() + " " + list.get(1).id());
		IVar[] t = list.stream().toArray(IVar[]::new);
		String compactFromOneArray = varArrays.stream().map(va -> va.compactFormOf(t)).filter(s -> s != null).findFirst().orElse(null);
		if (compactFromOneArray != null && (!preserveOrder || expand(compactFromOneArray).equals(Stream.of(t).map(x -> x.id()).collect(joining(" ")))))
			return sb.append(" " + compactFromOneArray); // if perserveOrder=true, we know for sure that order is preserved because of the expand test
		// if compression from a single array (as tried above) has not succeeded, then we execute the code below
		if (!preserveOrder) {
			// we search for compact forms of the form x[][i] for a given i ; this is possible because the order is not important here
			// boolean[] bs = new boolean[t.length];
			// for (VarArray va : varArrays) {
			// if (va instanceof VarEntities.VarArray2D) {
			// IVar[][] m = (IVar[][]) VarArray2D.class.cast(va).vars;
			// for (int i = 0; i < m[0].length; i++) {
			// int j = 0;
			// while (j < m.length && Utilities.indexOf(m[j][i], t) != -1)
			// j++;
			// if (j == m.length) {
			// for (j = 0; j < m.length; j++)
			// bs[Utilities.indexOf(m[j][i], t)] = true;
			// sb.append(" " + va.id + "[][" + i + "]");
			// }
			// }
			// }
			// }
			// list = IntStream.range(0, t.length).filter(i -> !bs[i]).mapToObj(i -> t[i]).collect(toList());
		}
		if (list.size() > 0) {
			SequenceOfSuccessiveVariables sequence = null;
			for (IVar var : list)
				if (sequence == null)
					sequence = new SequenceOfSuccessiveVariables(var); // we start trying to find a sequence of successive variables
				else {
					if (sequence.canBeExtendedWith(var.id()) == false) {
						sb.append(" " + sequence.toString()); // we add the curr sequence because it was no more possible to extend it
						sequence = new SequenceOfSuccessiveVariables(var); // we restart trying to find a new sequence
					}
				}
			sb.append(" " + sequence.toString());
		}
		return sb;
	}

	private String compact(IVar[] vars, boolean preserveOrder) {
		Utilities.control(vars != null && vars.length > 0, "The array is empty");
		if (vars.length == 1)
			return vars[0].id();
		if (vars.length == 2)
			return vars[0].id() + " " + vars[1].id();
		StringBuilder sb = new StringBuilder();
		if (preserveOrder) {
			ArrayList<IVar> list = new ArrayList<>();
			list.add(vars[0]);
			String prefix = vars[0].idPrefix();
			for (int i = 1; i <= vars.length; i++) {
				if (i == vars.length || !prefix.equals(vars[i].idPrefix())) {
					handlePart(sb, list, preserveOrder);
					list.clear();
					if (i < vars.length) {
						list.add(vars[i]);
						prefix = vars[i].idPrefix();
					}
				} else
					list.add(vars[i]);
			}
		} else {
			Map<String, List<IVar>> byPrefixId = Stream.of(vars).collect(Collectors.groupingBy(x -> x.idPrefix()));
			for (List<IVar> list : byPrefixId.values())
				handlePart(sb, list, preserveOrder);
		}
		return sb.toString().trim();
	}

	// private String compact(IVar[] vars, boolean preserveOrder) {
	// if (vars.length == 2)
	// return vars[0].id() + " " + vars[1].id();
	//
	// String compactFromOneArray = varArrays.stream().map(va -> va.compactFormOf(vars)).filter(s -> s != null).findFirst().orElse(null);
	// if (compactFromOneArray != null && (!preserveOrder || expand(compactFromOneArray).equals(Stream.of(vars).map(x -> x.id()).collect(joining("
	// ")))))
	// return compactFromOneArray; // if preserveOrder is true, we know for sure that the order is preserved because we have just controlled it
	// // if compression from a single array (as tried above) has not succeeded, then we execute the code below
	// String s = "";
	// List<IVar> list = null; // contains variables remaining after trying compression on columns (when not preserving order)
	// if (!preserveOrder) {
	// // we search for compact forms of the form x[][i] for a given i ; this is possible because the order is not important here
	// boolean[] bs = new boolean[vars.length];
	// for (VarArray va : varArrays) {
	// if (va instanceof VarEntities.VarArray2D) {
	// IVar[][] m = (IVar[][]) VarArray2D.class.cast(va).vars;
	// for (int i = 0; i < m[0].length; i++) {
	// int j = 0;
	// while (j < m.length && Utilities.indexOf(m[j][i], vars) != -1)
	// j++;
	// if (j == m.length) {
	// for (j = 0; j < m.length; j++)
	// bs[Utilities.indexOf(m[j][i], vars)] = true;
	// s += " " + va.id + "[][" + i + "]";
	// }
	// }
	// }
	// }
	// list = IntStream.range(0, vars.length).filter(i -> !bs[i]).mapToObj(i -> vars[i]).collect(toList());
	// } else
	// list = Arrays.asList(vars);
	// if (list.size() > 0) {
	// SequenceOfSuccessiveVariables sequence = null;
	// for (IVar var : list)
	// if (sequence == null)
	// sequence = new SequenceOfSuccessiveVariables(var); // we start trying to find a sequence of successive variables
	// else {
	// if (sequence.canBeExtendedWith(var.id()) == false) {
	// s += " " + sequence.toString(); // we add the current sequence because it was no more possible to extend it
	// sequence = new SequenceOfSuccessiveVariables(var); // we restart trying to find a sequence of successive variables
	// }
	// }
	// s += " " + sequence.toString();
	// }
	// return s.trim(); // note that the order is always preserved in that case
	// }

	public String compact(IVar[] vars) {
		return compact(vars, false);
	}

	public String compactOrdered(IVar[] vars) {
		return compact(vars, true);
	}

	public String[] compact(IVar[][] vars) {
		return Stream.of(vars).map(t -> compact(t)).toArray(String[]::new);
	}

	public String[] compactOrdered(IVar[][] vars) {
		return Stream.of(vars).map(t -> compactOrdered(t)).toArray(String[]::new);
	}

	public String compactMatrix(IVar[][] matrix) {
		String s = compactOrdered(Utilities.collect(IVar.class, (Object) matrix));
		if (s.indexOf(" ") == -1)
			return s;
		return Stream.of(matrix).map(t -> "(" + Stream.of(t).map(x -> x.toString()).collect(Collectors.joining(",")) + ")").collect(Collectors.joining("\n"));
	}

	public int nVarsIn(String s) {
		return expand(s).split(" ").length;
	}
}
