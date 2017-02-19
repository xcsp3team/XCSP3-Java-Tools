package org.xcsp.common;

/**
 * This is the root abstract class of all subclasses that are useful for denoting the size (i.e., length of each dimension) of
 * multi-dimensional arrays of variables. These classes are used as syntactic sugar.
 */
public abstract class Size {
	/**
	 * The respective lengths of an array of dimension {@code lengths.length}.
	 */
	public int[] lengths;

	/**
	 * Sets the lengths of the dimensions of an array of dimension {@code lengths.length}
	 * 
	 * @param lengths
	 *            the lengths of the dimensions of an array
	 * @return this object (for method chaining)
	 */
	protected Size setLengths(int... lengths) {
		this.lengths = lengths;
		return this;
	}

	@Override
	public String toString() {
		return Utilities.join(lengths);
	}

	/**
	 * A class for representing the size (length) of a 1-dimensional array. This is mainly used as syntactic sugar.
	 */
	public static class Size1D extends Size {
		/**
		 * Builds an object that represents the size (length) of a 1-dimensional array.
		 * 
		 * @param length
		 *            the size (length) of the array
		 * @return an object that represents the size (length) of a 1-dimensional array
		 */
		public static Size1D build(int length) {
			return (Size1D) new Size1D().setLengths(length);
		}
	}

	/**
	 * A class for representing the size (i.e., length of each dimension) a 2-dimensional array. This is mainly used as syntactic sugar.
	 */
	public static class Size2D extends Size {
		/**
		 * Builds an object that represents the size (i.e., length of each dimension) of a 2-dimensional array.
		 * 
		 * @param length0
		 *            the size (length) of the first dimension of a 2-dimensional array
		 * @param length1
		 *            the size (length) of the second dimension of a 2-dimensional array
		 * @return an object that represents the size (i.e., length of each dimension) of a 2-dimensional array
		 */
		public static Size2D build(int length0, int length1) {
			return (Size2D) new Size2D().setLengths(length0, length1);
		}
	}

	/**
	 * A class for representing the size (i.e., length of each dimension) a 3-dimensional array. This is mainly used as syntactic sugar.
	 */
	public static class Size3D extends Size {
		/**
		 * Builds an object that represents the size (i.e., length of each dimension) of a 3-dimensional array.
		 * 
		 * @param length0
		 *            the size (length) of the first dimension of a 3-dimensional array
		 * @param length1
		 *            the size (length) of the second dimension of a 3-dimensional array
		 * @param length2
		 *            the size (length) of the third dimension of a 3-dimensional array
		 * @return an object that represents the size (i.e., length of each dimension) of a 3-dimensional array
		 */
		public static Size3D build(int length0, int length1, int length2) {
			return (Size3D) new Size3D().setLengths(length0, length1, length2);
		}
	}

	/**
	 * A class for representing the size (i.e., length of each dimension) a 4-dimensional array. This is mainly used as syntactic sugar.
	 */
	public static class Size4D extends Size {
		/**
		 * Builds an object that represents the size (i.e., length of each dimension) of a 4-dimensional array.
		 * 
		 * @param length0
		 *            the size (length) of the first dimension of a 4-dimensional array
		 * @param length1
		 *            the size (length) of the second dimension of a 4-dimensional array
		 * @param length2
		 *            the size (length) of the third dimension of a 4-dimensional array
		 * @param length3
		 *            the size (length) of the fourth dimension of a 4-dimensional array
		 * @return an object that represents the size (i.e., length of each dimension) of a 4-dimensional array
		 */
		public static Size4D build(int length0, int length1, int length2, int length3) {
			return (Size4D) new Size4D().setLengths(length0, length1, length2, length3);
		}
	}

	/**
	 * A class for representing the size (i.e., length of each dimension) a 5-dimensional array. This is mainly used as syntactic sugar.
	 */
	public static class Size5D extends Size {
		/**
		 * Builds an object that represents the size (i.e., length of each dimension) of a 5-dimensional array.
		 * 
		 * @param length0
		 *            the size (length) of the first dimension of a 5-dimensional array
		 * @param length1
		 *            the size (length) of the second dimension of a 5-dimensional array
		 * @param length2
		 *            the size (length) of the third dimension of a 5-dimensional array
		 * @param length3
		 *            the size (length) of the fourth dimension of a 5-dimensional array
		 * @param length4
		 *            the size (length) of the fifth dimension of a 5-dimensional array
		 * @return an object that represents the size (i.e., length of each dimension) of a 5-dimensional array
		 */
		public static Size5D build(int length0, int length1, int length2, int length3, int length4) {
			return (Size5D) new Size5D().setLengths(length0, length1, length2, length3, length4);
		}
	}

}
