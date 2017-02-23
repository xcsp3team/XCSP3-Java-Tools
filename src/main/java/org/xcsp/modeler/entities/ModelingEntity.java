package org.xcsp.modeler.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Utilities;
import org.xcsp.parser.entries.AnyEntry;

public abstract class ModelingEntity {
	public interface TagDummy {
	}

	public String id, note;
	public Set<TypeClass> classes;

	protected ModelingEntity(String id, String note, TypeClass... classes) {
		this.id = id;
		this.note = note;
		this.classes = new HashSet<>();
		if (classes != null)
			Stream.of(classes).forEach(c -> this.classes.add(c));
	}

	protected ModelingEntity(TypeClass... classes) {
		this(null, null, classes);
	}

	public ModelingEntity id(String id) {
		Utilities.control(this.id == null && id != null, "Pb with the id");
		this.id = id;
		return this;
	}

	public ModelingEntity note(String note) {
		this.note = this.note == null ? note : this.note + " " + note;
		return this;
	}

	public ModelingEntity tag(TypeClass... classes) {
		Stream.of(classes).forEach(c -> this.classes.add(c));
		return this;
	}

	public ModelingEntity tag(String... classes) {
		return tag(TypeClass.classesFor(classes));
	}

	public ModelingEntity setBasicAttributes(AnyEntry entry) {
		if (entry.id != null) {
			Utilities.control(this.id == null, "Two different ids for the same entity");
			this.id = entry.id;
		}
		if (entry.note != null)
			this.note = this.note == null ? entry.note : this.note + " " + entry.note;
		if (entry.classes != null)
			Stream.of(entry.classes).forEach(c -> this.classes.add(c));
		return this;
	}

	public boolean nullBasicAttributes() {
		return (id == null || id.length() == 0) && (note == null || note.length() == 0) && classes.size() == 0;
	}
}