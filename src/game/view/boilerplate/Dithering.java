package game.view.boilerplate;

/**
currently just using bayer.
https://en.wikipedia.org/wiki/Ordered_dithering

@author Michael Johnston (tky886)
*/
public class Dithering {

	private static final byte[]  ditherInt   = new  byte[256];
	private static final float[] ditherFloat = new float[256];

	static {
		for (int index = 0; index < 256; index++) {
			int x = 0, y = 0;
			for (int index2 = index, add = 8; index2 != 0; index2 >>>= 2, add >>>= 1) {
				switch (index2 & 3) {
					default:
					case 0: break;
					case 1: x += add; y += add; break;
					case 2: y += add; break;
					case 3: x += add; break;
				}
			}
			int location = indexUnchecked(x, y);
			ditherInt[location] = (byte)(index);
			ditherFloat[location] = index / 255.0F;
		}
	}

	private static int indexUnchecked(int x, int y) {
		return (y << 4) | x;
	}

	private static int index(int x, int y) {
		return indexUnchecked(x & 15, y & 15);
	}

	/** returns in the [0, 255] range. */
	public static int getInt(int x, int y) {
		return ditherInt[index(x, y)] & 255;
	}

	/** returns in the [0, 1) range. */
	public static float getFloat(int x, int y) {
		return ditherFloat[index(x, y)];
	}
}