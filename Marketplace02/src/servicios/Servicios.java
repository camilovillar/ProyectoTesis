package servicios;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.csvreader.CsvReader;

public class Servicios {
	
	String[] servicios;
	String[][] servCQ;
	String path = "C:\\Users\\Camilo\\Desktop\\Eclipse\\JSON\\";
	String nombreArchivo = "Servicios1.csv";
	JSONObject obj = new JSONObject();	
	
	public String[] getServicios(int n, int o, int p){ // n de servicios totales, n de proveedores, n de servicios a negociar
		String[] serv = new String[n];
		JSONParser parser = new JSONParser();
		try {
			int cont = 0;
			while(cont < n){
				Object obj = parser.parse(new FileReader(path+"JSONServicios.json"));
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray servs = (JSONArray) jsonObject.get("serv"+cont);
				Iterator<String> iterator = servs.iterator();
				while (iterator.hasNext()) {
					serv[cont] = (iterator.next());
				}
				cont++;
			}
		} catch (FileNotFoundException e) {
			//manejo de error
		} catch (IOException e) {
			//manejo de error
		} catch (ParseException e) {
			//manejo de error
		}
		
		String serv1 = ";serv";
		
		String prov = ";Proveedor";
		
		for(int i = 0;i < n; i++){
			int aleatorio = (int) (Math.random() * (o));
			int aleatorio1 = (int) (Math.random() * (p));
			serv[i]+=serv1+aleatorio1;
			serv[i]+=prov+aleatorio;
		}
		return serv;
	}
	public String[][] getAtributos(String[] serv, int n){// n número de servicios
		
		String[][] atributos = new String[n][12];
		for(int i = 0; i<n ; i++){
			
			String[] atrib = serv[i].split(";");
			for(int j=0; j<12 ;j++){
				atributos[i][j]=atrib[j];
			}
		}
		return atributos;
		
	}
	
	public void creaArchivoServicios(){
		int cont = 0;
		try {
            //List<String> servicios = new ArrayList<String>();
            
            CsvReader servicios_imp = new CsvReader(path+nombreArchivo);
            servicios_imp.readHeaders();
 
            while (servicios_imp.readRecord()) {
            	JSONArray list = new JSONArray();
                String id = servicios_imp.get(0);
               
                list.add(id);
                
                obj.put("serv"+cont, list);
                cont++;
            }
            
            try {

    			FileWriter file = new FileWriter(path+"JSONServicios.json");
    			file.write(obj.toJSONString());
    			file.flush();
    			file.close();

    		} catch (IOException e) {
    			//manejar error
    		}
            
            servicios_imp.close();
             
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}// Cierra creaArchivoServicios
	public void creaArchivosProveedores(String[][] atrib, int n){
		// Arreglo de dos dimensiones con atributos 
		// Cada fila es un servicio, cada columna es un atributo, el último (10) es proveedor
		// n es el número de proveedores.
		JSONObject obj = new JSONObject();
		for(int i = 0; i < n ;i++){
			int c = 0;
			for(int j = 0;j < 2507;j++){
				if(atrib[j][11].equals("Proveedor"+i)){
					JSONArray list = new JSONArray();
					for(int k = 0;k < 11;k++){
						list.add(atrib[j][k]);
					}
					obj.put("servi"+c, list);
					c++;
				}	
			}
			// ajusto el valor que considera el 0
			obj.put("nServ", c);
			try {
				FileWriter file = new FileWriter(path+"\\Prov\\Proveedor"+i+".json");
				file.write(obj.toJSONString());
				file.flush();
				file.close();
			} catch (IOException e) {
				//manejar error
			}
		}
		
	}// Cierra método

}
