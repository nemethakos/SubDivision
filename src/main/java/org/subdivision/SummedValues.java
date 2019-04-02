package org.subdivision;
public class SummedValues {
	float[] energies;
	float energySum;

	public SummedValues(int length) {
		this.energies = new float[length];
	}

	public void addEnergy(float energy, int index) {

		if (index < 0 || index > energies.length) {
			throw new IllegalArgumentException("index = " + index + ", length=" + energies.length);
		}

		energySum += energy;
		energies[index] += energy;
	}
}