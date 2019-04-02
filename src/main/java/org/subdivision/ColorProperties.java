package org.subdivision;

public class ColorProperties {
	int avarageColor;

	float energyAvarage;

	long energySum;
	float maxEnergy;

	float minEnergy;
	int triangleAvarageLowerColor;
	int triangleAvarageUpperColor;

	public ColorProperties(int avarageColor, int triangleAvarageUpperColor, int triangleAvarageLowerColor,

			float energyAvarage, long energySum, float maxEnergy, float minEnergy) {
		super();
		this.avarageColor = avarageColor;
		this.triangleAvarageUpperColor = triangleAvarageUpperColor;
		this.triangleAvarageLowerColor = triangleAvarageLowerColor;
		this.energyAvarage = energyAvarage;
		this.energySum = energySum;
		this.maxEnergy = maxEnergy;
		this.minEnergy = minEnergy;
	}

	public float getMaxEnergy() {
		return maxEnergy;
	}

	public boolean hasTreshold(float treshold) {
		if (treshold == 0) {
			return true;
		}
		boolean aboveAvarage = (maxEnergy - minEnergy) > treshold;

		// System.out.format("%d > %f = %b\n", minEnergy, treshold, aboveAvarage);

		return aboveAvarage;
	}

	@Override
	public String toString() {
		return String.format(
				"ColorProperties [avarageColor=%s, triangleAvarageUpperColor=%s, triangleAvarageLowerColor=%s, energyAvarage=%s, energySum=%s, maxEnergy=%s, minEnergy=%s]",
				avarageColor, triangleAvarageUpperColor, triangleAvarageLowerColor, energyAvarage, energySum, maxEnergy,
				minEnergy);
	}

}