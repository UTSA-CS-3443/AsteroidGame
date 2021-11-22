package game.common;

/**
https://en.wikipedia.org/wiki/Voronoi_diagram

seed points are distributed on a square grid,
with one seed point per grid square.

@author Michael Johnston (tky886)
*/
public class VoronoiGrid {

	public final long seed;
	/** size of grid squares. */
	public final double size;
	public final double reciprocalSize;
	public final double variation;

	public VoronoiGrid(long seed, double size, double variation) {
		assert size > 0.0D;
		assert variation >= 0.0D && variation <= 1.0D;
		this.seed = seed;
		this.size = size;
		this.reciprocalSize = 1.0D / size;
		this.variation = variation;
	}

	/** returns the X coordinate of the seed point identified by its grid square's coordinates. */
	public double getSeedPointX(int gridX, int gridY) {
		long seed = this.seed ^ 0xC83D4C19D8F12EA4L;
		seed = Util.permute(seed, gridX);
		seed = Util.permute(seed, gridY);
		return (gridX + Util.nextPositiveDouble(seed) * this.variation) * this.size;
	}

	/** returns the X coordinate of the seed point identified by its grid square's coordinates. */
	public double getSeedPointY(int gridX, int gridY) {
		long seed = this.seed ^ 0x6494F4939AFEC67FL;
		seed = Util.permute(seed, gridX);
		seed = Util.permute(seed, gridY);
		return (gridY + Util.nextPositiveDouble(seed) * this.variation) * this.size;
	}

	/**
	populates the seed point with information corresponding
	to the cell which contains the provided coordinates.
	if the coordinate lie on the boundary between more than one cell,
	which cell gets chosen is not defined.
	*/
	public void getNearestSeedPoint(double x, double y, SeedPoint out) {
		assert out.getGrid() == this : "Using wrong seed point for grid";
		//out.safeDistance will be 0 if no query has been made yet.
		if (out.safeDistance > 0.0D && Util.square(x - out.centerX, y - out.centerY) < out.safeDistance) {
			return;
		}

		int centerGridX = Util.floor(x * this.reciprocalSize);
		int centerGridY = Util.floor(y * this.reciprocalSize);
		int closestGridX = centerGridX;
		int closestGridY = centerGridY;
		double closestCenterX = this.getSeedPointX(centerGridX, centerGridY);
		double closestCenterY = this.getSeedPointY(centerGridX, centerGridY);
		double closestDistance = Util.square(x - closestCenterX, y - closestCenterY);
		double secondClosestDistance = Double.POSITIVE_INFINITY;

		//the closest point could technically be outside this area,
		//but false positives are not usually noticeable in practice.
		int minGridX = centerGridX - 1;
		int maxGridX = centerGridX + 1;
		int minGridY = centerGridY - 1;
		int maxGridY = centerGridY + 1;
		for (int gridX = minGridX; gridX <= maxGridX; gridX++) {
			for (int gridY = minGridY; gridY <= maxGridY; gridY++) {
				if (gridX == centerGridX && gridY == centerGridY) continue;
				double newX = this.getSeedPointX(gridX, gridY);
				double newY = this.getSeedPointY(gridX, gridY);
				double newDistance = Util.square(x - newX, y - newY);
				if (newDistance < closestDistance) {
					closestGridX = gridX;
					closestGridY = gridY;
					closestCenterX = newX;
					closestCenterY = newY;
					secondClosestDistance = closestDistance;
					closestDistance = newDistance;
				}
				else if (newDistance < secondClosestDistance) {
					secondClosestDistance = newDistance;
				}
			}
		}

		out.gridX = closestGridX;
		out.gridY = closestGridY;
		out.centerX = closestCenterX;
		out.centerY = closestCenterY;
		out.safeDistance = secondClosestDistance;
	}

	public class SeedPoint {

		public int gridX, gridY;
		public double centerX, centerY;
		/**
		used to abort early if the requested position
		is so close to our center that there can't
		possibly be a closer seed point elsewhere.
		*/
		double safeDistance;

		public VoronoiGrid getGrid() {
			return VoronoiGrid.this;
		}

		public void setGridPosition(int gridX, int gridY) {
			this.gridX = gridX;
			this.gridY = gridY;
			this.centerX = VoronoiGrid.this.getSeedPointX(gridX, gridY);
			this.centerY = VoronoiGrid.this.getSeedPointY(gridX, gridY);
			this.safeDistance = 0.0D;
		}

		public void setPositionToClosest(double x, double y) {
			VoronoiGrid.this.getNearestSeedPoint(x, y, this);
		}
	}
}