package wrappers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import exceptions.IncorrectLineFormatException;

/** 15/06/2010 Trujillo Comment
 * En este formato las longitudes y las latitudes se dan en grados decimales, o sea
 * que para calcular distancias entre puntos de este formato no es necesario hacer
 * ninguna conversion*/

/** 15/06/2010 Trujillo Comment
 * El tag <sym> es simplemente el simbolo de la coordenada que se esta representando, para nosotros son
 * puntos y ya esta*/

/** 15/06/2010 Trujillo Comment
 * El tag <ele> es la elevacion*/

/** 15/06/2010 Trujillo Comment
 * El tag <number> es el numero de la ruta*/

public class GPXFormat extends GPSFormat{

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");


		@Override
		public boolean isValidateData() {
			return true;
		}


		public void setLongitude(String lon) {
			setLongitude(Double.parseDouble(lon));
			if (getLongitude() < 0 || getLongitude() > 180) throw new IncorrectLineFormatException("La " +
					"longitude resultante es "+getLongitude()+" lo cual esta fuera de rango");
		}


		public void setLatitude(String lat) {
			setLatitude(Double.parseDouble(lat));
			if (getLatitude() < 0 || getLatitude() > 90) throw new IncorrectLineFormatException("La " +
					"latitud resultante es "+getLatitude()+" lo cual esta fuera de rango");
		}




		/** 17/06/2010 Trujillo Comment
		 * Aqui la fecha viene en el siguiente formato 2009-07-28T15:03:21Z, */
		public void setDate(String d) {
			d = d.replace("T", " ");
			d = d.replace("Z", " ");
			try {
				setTime(dateFormat.parse(d).getTime());
			} catch (ParseException e) {
				throw new IncorrectLineFormatException("La fecha dada aqui no es correcta :"+d);
			}
		}




		@Override
		public double getStandarHeight() {
			return getHeight();
		}

}
