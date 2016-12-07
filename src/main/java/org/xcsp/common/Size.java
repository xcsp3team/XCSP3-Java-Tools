package org.xcsp.common;

import org.xcsp.common.Utilities;

public abstract class Size {
	public int[] lengths;

	public Size setLengths(int... lengths) {
		this.lengths = lengths;
		return this;
	}

	public String toString() {
		return Utilities.join(lengths);
	}

	public static class Size1D extends Size {
	}

	public static class Size2D extends Size {
	}

	public static class Size3D extends Size {
	}

	public static class Size4D extends Size {
	}

	public static class Size5D extends Size {
	}

	/** Builds an object that represents the size of a 1-dimensional array. */
	public static Size1D size(int length) {
		return (Size1D) new Size1D().setLengths(length);
	}

	/** Builds an object that represents the size of a 2-dimensional array. */
	public static Size2D size(int length1, int length2) {
		return (Size2D) new Size2D().setLengths(length1, length2);
	}

	/** Builds an object that represents the size of a 3-dimensional array. */
	public static Size3D size(int length1, int length2, int length3) {
		return (Size3D) new Size3D().setLengths(length1, length2, length3);
	}

	/** Builds an object that represents the size of a 4-dimensional array. */
	public static Size4D size(int length1, int length2, int length3, int length4) {
		return (Size4D) new Size4D().setLengths(length1, length2, length3, length4);
	}

	/** Builds an object that represents the size of a 5-dimensional array. */
	public static Size5D size(int length1, int length2, int length3, int length4, int length5) {
		return (Size5D) new Size5D().setLengths(length1, length2, length3, length4, length5);
	}
}
