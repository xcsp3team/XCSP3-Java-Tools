package org.xcsp.modeler.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Softening;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Utilities;
import org.xcsp.modeler.definitions.ICtr;
import org.xcsp.modeler.entities.ModelingEntity.TagDummy;
import org.xcsp.modeler.entities.VarEntities.VarEntity;

public final class CtrEntities {

	public List<CtrEntity> allEntities = new ArrayList<>();
	public Map<ICtr, CtrAlone> ctrToCtrAlone = new HashMap<>(); // useful for recording specific information (id, note, classes) about constraints
	public Map<ICtr, CtrArray> ctrToCtrArray = new HashMap<>();

	public CtrArray newCtrArrayEntity(ICtr[] ctrs, boolean dummy, TypeClass... classes) {
		return ctrs.length == 0 || dummy ? new CtrArrayDummy(ctrs, classes) : new CtrArray(ctrs, classes);
	}

	@Override
	public String toString() {
		return allEntities.stream().map(ce -> ce.getClass().getSimpleName()).collect(Collectors.joining(" "));
	}

	// ************************************************************************
	// ***** Classes for handling stand-alone and groups of constraints
	// ************************************************************************

	public abstract class CtrEntity extends ModelingEntity {
		protected CtrEntity(TypeClass... classes) {
			super(classes);
			allEntities.add(this);
		}

		public abstract CtrEntity violationCost(int violationCost);
	}

	// note a CtrAlone ca is really alone iff ctrToCtrArray.get(ca.ctr) return null;
	public class CtrAlone extends CtrEntity {
		public ICtr ctr;
		public Softening softening;

		public CtrAlone(TypeClass... classes) {
			super(classes);
		}

		public CtrAlone(ICtr ctr, TypeClass... classes) {
			this(classes);
			this.ctr = ctr;
			ctrToCtrAlone.put(ctr, this);
			Utilities.control(ctr != null && !(this instanceof CtrAloneDummy), "CtrAloneDummy possible only iff the specified constraint is null."
					+ "In some implementations other than default one, it could be different.");
		}

		@Override
		public CtrEntity violationCost(int violationCost) {
			return null; // TODO still to be implemented
			// softening = new XSofteningSimple(null, violationCost);
			// ctr.pb.framework = TypeFramework.WCSP;
			// ctr.pb.resolution.cfg.framework = TypeFramework.WCSP;
			// return this;
		}
	}

	/**
	 * Objects of this class correspond to extreme cases where the constraint is irrelevant (for example, a sum with 0 variable). Such constraint will
	 * not be recorded.
	 */
	public final class CtrAloneDummy extends CtrAlone implements TagDummy {

		public CtrAloneDummy(String note, TypeClass... classes) {
			super(classes);
			note(note); // the note indicates why the constraint is dummy
		}
	}

	public class CtrArray extends CtrEntity {
		public ICtr[] ctrs;

		/**
		 * while managing a loop (or a block), some variables and arrays of variables can be defined internally to this loop. If a tag applies to the
		 * loop, the variables and arrays of variables must also be tagged. This list stores the concerned var entities.
		 */
		public List<VarEntity> varEntitiessSubjectToTags;

		public CtrArray(ICtr[] ctrs, TypeClass... classes) {
			super(classes);
			this.ctrs = ctrs;
			if (!(this instanceof CtrArrayDummy))
				Stream.of(ctrs).forEach(c -> ctrToCtrArray.put(c, this));
		}

		@Override
		public CtrEntity violationCost(int violationCost) {
			System.out.println("Not possible to associate a violation cost with a group of constraints");
			return null;
		}

		public CtrArray append(CtrArray ca) {
			Utilities.control(ca.id == null && ca.note == null && ca.classes.size() == 0,
					"Implementation not finished yet to take into account such situations where some information must be merged");
			this.ctrs = Utilities.convert(IntStream.range(0, ctrs.length + ca.ctrs.length).mapToObj(i -> i < ctrs.length ? ctrs[i] : ca.ctrs[i - ctrs.length])
					.collect(Collectors.toList()));
			return this;
		}

		@Override
		public CtrArray tag(TypeClass... classes) {
			super.tag(classes);
			varEntitiessSubjectToTags.stream().forEach(x -> x.tag(classes));
			return this;
		}
	}

	/**
	 * Objects of this class correspond to cases where a set of constraints will have to be merged with another one. The array is dummy, but involved
	 * constraints are not.
	 */
	public final class CtrArrayDummy extends CtrArray implements TagDummy {

		protected CtrArrayDummy(ICtr[] ctrs, TypeClass... classes) {
			super(ctrs, classes);
			Stream.of(ctrs).forEach(c -> ctrToCtrAlone.get(c).tag(classes)); // because the dummy array will disappear (be merged)
		}

		@Override
		public CtrArrayDummy note(String note) {
			Stream.of(ctrs).forEach(c -> ctrToCtrAlone.get(c).note(note)); // notes are merged
			// we don't apply notes to objects in varsSubjectToTags
			return this;
		}

		@Override
		public CtrArrayDummy tag(TypeClass... classes) {
			Stream.of(ctrs).forEach(c -> ctrToCtrAlone.get(c).tag(classes));
			varEntitiessSubjectToTags.stream().forEach(x -> x.tag(classes));
			return this;
		}
	}

}
