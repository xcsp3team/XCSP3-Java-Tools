package org.xcsp.modeler.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Utilities;
import org.xcsp.modeler.definitions.ICtr;
import org.xcsp.modeler.entities.ModelingEntity.TagDummy;
import org.xcsp.parser.entries.XConstraints.XSoftening;

public final class CtrEntities {

	public List<CtrEntity> allEntities = new ArrayList<>();
	public Map<ICtr, CtrAlone> ctrToCtrAlone = new HashMap<>(); // useful for recording specific information (id, note, classes) about
																// constraints
	public Map<ICtr, CtrArray> ctrToCtrArray = new HashMap<>();

	public int nBuiltBlocks;

	public CtrArray newCtrArrayEntity(ICtr[] ctrs, boolean dummy, TypeClass... classes) {
		return ctrs.length == 0 || dummy ? new CtrArrayDummy(ctrs, classes) : new CtrArray(ctrs, classes);
	}

	public CtrBlock newCtrBlockEntityStart(TypeClass... classes) {
		return new CtrBlock(nBuiltBlocks++, true, classes);
	}

	public CtrBlock newCtrBlockEntityEnd() {
		return new CtrBlock(nBuiltBlocks, false);
	}

	public abstract class CtrEntity extends ModelingEntity {
		protected CtrEntity(TypeClass... classes) {
			super(classes);
			allEntities.add(this);
		}

		public CtrEntity violationCost(int violationCost) {
			System.out.println("Not possible to associate a violation cost with this class " + this.getClass());
			return null;
		}
	}

	@Override
	public String toString() {
		return allEntities.stream().map(ce -> ce.getClass().getSimpleName()).collect(Collectors.joining(" "));
	}

	// note a CtrAlone ca is really alone iff ctrToCtrArray.get(ca.ctr) return null;
	public class CtrAlone extends CtrEntity {
		public ICtr ctr;
		public XSoftening softening;

		public CtrAlone(ICtr ctr, TypeClass... classes) {
			super(classes);
			Utilities.control((ctr == null) == this instanceof CtrAloneDummy, "CtrDummy possible in some implementations (not in the default one)");
			if (!(this instanceof CtrAloneDummy)) {
				this.ctr = ctr;
				ctrToCtrAlone.put(ctr, this);
				// if (ctr instanceof CtrRaw)
				// ctrToCtrAlone.put(((CtrRaw) ctr).c, this);
			}
		}

		@Override
		public CtrEntity violationCost(int violationCost) {
			return null; // TODO XXXXXXXXXXXXXXXX
			// softening = new XSofteningSimple(null, violationCost);
			// ctr.pb.framework = TypeFramework.WCSP;
			// ctr.pb.resolution.cfg.framework = TypeFramework.WCSP;
			// return this;
		}
	}

	public final class CtrAloneDummy extends CtrAlone implements TagDummy {

		public CtrAloneDummy(String s, TypeClass... classes) {
			super(null, classes);
		}

		protected CtrAloneDummy(TypeClass... classes) {
			this(null, classes);
		}
	}

	public class CtrArray extends CtrEntity {
		public ICtr[] ctrs;

		public List<ModelingEntity> subjectToTags;

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

		public void setVarEntitiesSubjectToTags(List<ModelingEntity> subjectToTags) {
			this.subjectToTags = subjectToTags;
		}

		public CtrArray append(CtrArray ca) {
			Utilities.control(ca.id == null && ca.note == null && ca.classes.size() == 0, "Merging information to be finished");
			this.ctrs = Utilities.convert(IntStream.range(0, ctrs.length + ca.ctrs.length).mapToObj(i -> i < ctrs.length ? ctrs[i] : ca.ctrs[i - ctrs.length])
					.collect(Collectors.toList()));
			return this;
		}

		@Override
		public CtrArray tag(TypeClass... classes) {
			super.tag(classes);
			subjectToTags.stream().forEach(o -> o.tag(classes));
			return this;
		}
	}

	public final class CtrArrayDummy extends CtrArray implements TagDummy {

		protected CtrArrayDummy(ICtr[] ctrs, TypeClass... classes) {
			super(ctrs, classes);
			// Kit.log.warning("Dummy constraint array ");
			Stream.of(ctrs).forEach(c -> ctrToCtrAlone.get(c).tag(classes));
		}

		@Override
		public CtrArrayDummy note(String note) {
			Stream.of(ctrs).forEach(c -> ctrToCtrAlone.get(c).note(note)); // notes are merged
			return this;
		}

		@Override
		public CtrArrayDummy tag(TypeClass... classes) {
			Stream.of(ctrs).forEach(c -> ctrToCtrAlone.get(c).tag(classes));
			subjectToTags.stream().forEach(o -> o.tag(classes));
			return this;
		}
	}

	public final class CtrBlock extends CtrEntity {
		public int num;
		public boolean opening;

		public CtrBlock(int num, boolean opening, TypeClass... classes) {
			super(classes);
			this.num = num;
			this.opening = opening;
		}
	}
}
