package util;

public class Converter {

	public static double minutesToDegrees(MinutesCoordinates coord){
		double result = coord.getSec()/60;
		result += coord.getMinutes();
		result /= 60;
		result += coord.getDegrees();
		return result;
	}
	
	/** 21/06/2010 Trujillo Comment
	 * Para convertir a los minutos de los grados segundos
		1. Tome simplemente el número antes del decimal como sus grados.
			Tenemos tan 78°
		2. Ahora reste eso del problema y multiplique la respuesta por 60
			Tenemos tan 78° 27 '
		3. Ahora reste 27 de la respuesta y multipliqúese por 60 otra vez.
		La respuesta final es 78° 27 ' 21,6 " (78 grados 27 minutos y 21,6 segundos)*/
	public static MinutesCoordinates degreesToMinutes(double degrees){
		int intValue = (int)degrees;
		double rest = degrees-intValue;
		int minutes = (int)(rest*60);
		rest = rest*60 - minutes;
		float sec = (float)rest*60;
		return new MinutesCoordinates(intValue, minutes, sec);
	}
	
	/*private void UTM latLongtoUTM(double lattitude, double longitude){
			double UTMNorthing; 
			double UTMEasting;
			String Zone;
			
			double a = 6378137; //WGS84
			double eccSquared = 0.00669438; //WGS84
			double k0 = 0.9996;

			double LongOrigin;
			double eccPrimeSquared;
			double N, T, C, A, M;

			//Make sure the longitude is between -180.00 .. 179.9
			double LongTemp = (longitude+180)-((int)((longitude+180)/360))*360-180; // -180.00 .. 179.9;

			double LatRad = lattitude*deg2rad;
			double LongRad = LongTemp*deg2rad;
			double LongOriginRad;
			int ZoneNumber;

			ZoneNumber = ((int)((LongTemp + 180)/6)) + 1;

			if( Lat >= 56.0 && Lat < 64.0 && LongTemp >= 3.0 && LongTemp < 12.0 )
			ZoneNumber = 32;

			// Special zones for Svalbard
			if( Lat >= 72.0 && Lat < 84.0 )
			{
			if( LongTemp >= 0.0 && LongTemp < 9.0 ) ZoneNumber = 31;
			else if( LongTemp >= 9.0 && LongTemp < 21.0 ) ZoneNumber = 33;
			else if( LongTemp >= 21.0 && LongTemp < 33.0 ) ZoneNumber = 35;
			else if( LongTemp >= 33.0 && LongTemp < 42.0 ) ZoneNumber = 37;
			}
			LongOrigin = (ZoneNumber - 1)*6 - 180 + 3; //+3 puts origin in middle of zone
			LongOriginRad = LongOrigin * deg2rad;

			//compute the UTM Zone from the latitude and longitude
			Zone = ZoneNumber.ToString() + UTMLetterDesignator(Lat);

			eccPrimeSquared = (eccSquared)/(1-eccSquared);

			N = a/Math.Sqrt(1-eccSquared*Math.Sin(LatRad)*Math.Sin(LatRad));
			T = Math.Tan(LatRad)*Math.Tan(LatRad);
			C = eccPrimeSquared*Math.Cos(LatRad)*Math.Cos(LatRad);
			A = Math.Cos(LatRad)*(LongRad-LongOriginRad);

			M = a*((1 - eccSquared/4 - 3*eccSquared*eccSquared/64 - 5*eccSquared*eccSquared*eccSquared/256)*LatRad
			- (3*eccSquared/8 + 3*eccSquared*eccSquared/32 + 45*eccSquared*eccSquared*eccSquared/1024)*Math.Sin(2*LatRad)
			+ (15*eccSquared*eccSquared/256 + 45*eccSquared*eccSquared*eccSquared/1024)*Math.Sin(4*LatRad)
			- (35*eccSquared*eccSquared*eccSquared/3072)*Math.Sin(6*LatRad));

			UTMEasting = (double)(k0*N*(A+(1-T+C)*A*A*A/6
			+ (5-18*T+T*T+72*C-58*eccPrimeSquared)*A*A*A*A*A/120)
			+ 500000.0);

			UTMNorthing = (double)(k0*(M+N*Math.Tan(LatRad)*(A*A/2+(5-T+9*C+4*C*C)*A*A*A*A/24
			+ (61-58*T+T*T+600*C-330*eccPrimeSquared)*A*A*A*A*A*A/720)));
			if(Lat < 0)
			UTMNorthing += 10000000.0; //10000000 meter offset for southern hemisphere
	}*/


}
