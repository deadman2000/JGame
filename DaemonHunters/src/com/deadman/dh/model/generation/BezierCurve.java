package com.deadman.dh.model.generation;

public class BezierCurve
{
	// Calculate Bernstein basis
	private static double bernstein(int n, int i, double t)
	{
		double basis;
		double ti; /* t^i */
		double tni; /* (1 - t)^i */

		/* Prevent problems with pow */

		if (t == 0.0 && i == 0)
			ti = 1.0;
		else
			ti = Math.pow(t, i);

		if (n == i && t == 1.0)
			tni = 1.0;
		else
			tni = Math.pow(1 - t, n - i);

		//Bernstein basis
		basis = Ni(n, i) * ti * tni;
		return basis;
	}

	public static void bezier2D(double[] b, double[] p)
	{
		int npts = b.length / 2;
		int jcount;
		int cpts = p.length / 2;

		int icount = 0;
		double t = 0;
		double step = 1.0 / (cpts - 1);

		for (int i1 = 0; i1 < cpts; i1++)
		{
			if ((1.0 - t) < 5e-6)
				t = 1.0;

			jcount = 0;
			p[icount] = 0.0;
			p[icount + 1] = 0.0;
			for (int i = 0; i < npts; i++)
			{
				double basis = bernstein(npts - 1, i, t);
				p[icount] += basis * b[jcount];
				p[icount + 1] += basis * b[jcount + 1];
				jcount = jcount + 2;
			}

			icount += 2;
			t += step;
		}
	}

	private static double[] F;

	private static double Ni(int n, int i)
	{
		return F[n] / (F[i] * F[n - i]);
	}

	static
	{
		F = new double[33];
		F[0] = 1.0;
		F[1] = 1.0;
		F[2] = 2.0;
		F[3] = 6.0;
		F[4] = 24.0;
		F[5] = 120.0;
		F[6] = 720.0;
		F[7] = 5040.0;
		F[8] = 40320.0;
		F[9] = 362880.0;
		F[10] = 3628800.0;
		F[11] = 39916800.0;
		F[12] = 479001600.0;
		F[13] = 6227020800.0;
		F[14] = 87178291200.0;
		F[15] = 1307674368000.0;
		F[16] = 20922789888000.0;
		F[17] = 355687428096000.0;
		F[18] = 6402373705728000.0;
		F[19] = 121645100408832000.0;
		F[20] = 2432902008176640000.0;
		F[21] = 51090942171709440000.0;
		F[22] = 1124000727777607680000.0;
		F[23] = 25852016738884976640000.0;
		F[24] = 620448401733239439360000.0;
		F[25] = 15511210043330985984000000.0;
		F[26] = 403291461126605635584000000.0;
		F[27] = 10888869450418352160768000000.0;
		F[28] = 304888344611713860501504000000.0;
		F[29] = 8841761993739701954543616000000.0;
		F[30] = 265252859812191058636308480000000.0;
		F[31] = 8222838654177922817725562880000000.0;
		F[32] = 263130836933693530167218012160000000.0;
	}
}
