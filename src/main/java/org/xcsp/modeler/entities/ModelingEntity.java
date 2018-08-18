package org.xcsp.modeler.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Utilities;

public abstract class ModelingEntity {
	public interface TagDummy {
	}

	public String id, note;
	public final Set<TypeClass> classes;

	protected ModelingEntity(String id, String note, TypeClass... classes) {
		this.id = id;
		this.note = note;
		this.classes = classes == null ? new HashSet<>() : new HashSet<>(Arrays.asList(classes));
	}

	protected ModelingEntity(TypeClass... classes) {
		this(null, null, classes);
	}

	public final ModelingEntity id(String id) {
		Utilities.control(this.id == null && id != null, "Pb with the id");
		this.id = id;
		return this;
	}

	public ModelingEntity note(String note) {
		this.note = this.note == null ? note : this.note + " " + note;
		return this;
	}

	public ModelingEntity tag(TypeClass... classes) {
		this.classes.addAll(Arrays.asList(classes));
		return this;
	}

	public final ModelingEntity tag(String... classes) {
		return tag(TypeClass.classesFor(classes));
	}

	public final boolean nullBasicAttributes() {
		return (id == null || id.length() == 0) && (note == null || note.length() == 0) && classes.size() == 0;
	}
}