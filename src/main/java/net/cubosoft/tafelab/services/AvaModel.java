package net.cubosoft.tafelab.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.cubosoft.tafelab.repository.CustomNativeRepository;

@Service
public class AvaModel {
	// Connection con;
	@Autowired
	CustomNativeRepository repository;

	private static final Logger logger = LoggerFactory.getLogger(AvaModel.class);

	// TODAS TIENEN LA MISMA LOGICA POR LO QUE SE USA UNA SOLA FUNCION
	// CUIDADO LA VARIABLE PROCEDURE POR INYECCION
	public String getProcedureWithOrigin(String procedure, String origen) throws SQLException {
		String jsonResult;
		//cuando tenga este separador supongo que son origenes juntos por lo que toca enviar al procedure con comas
		if(origen.contains("-")) {
			origen=origen.replace("-", ",");
		}
		//con = Conexion.getConnection();
		String stm = "call " + procedure + "(\"@origen\"=?)";
		//NO CONVIENE DEVOLVER EL QUERY YA QUE SE QEDA ABIERTA LA CONEXION Y PUEDA QUE QUEDE EN MEMORIA DATOS
		//Query q=repository.runNativeQuery(stm);
		//q.setParameter(1, origen);
		//jsonResult=(String) q.getSingleResult();
		logger.debug("METODO "+stm + " " + origen);
		jsonResult= (String) repository.getObjectFromQuerry(stm, origen);
		
		
		return jsonResult;
		// rs.next();
	}

	// TODAS TIENEN LA MISMA LOGICA POR LO QUE SE USA UNA SOLA FUNCION
	// CUIDADO LA VARIABLE PROCEDURE POR INYECCION
	public List<String> getOrigins() throws SQLException {
		//List<String> origins = new ArrayList<>();
		String stm = "SELECT * FROM mob_tafelab_origenes";
		List<String> origins =repository.getListStringFromQuerry(stm); // La lista de Object[] que deseas transformar
	//	origins=repository.getListFromQuerry(stm);
		return origins;
		// rs.next();
	}
	
	/*
	 * public String getProcedureWithOrigin(String procedure,String origen) throws
	 * SQLException { String jsonResult; con = Conexion.getConnection();
	 * PreparedStatement pstmnt = null; String stm =
	 * "call "+procedure+"(\"@origen\"=?)"; logger.info(stm + " " + origen); pstmnt
	 * = con.prepareStatement(stm); pstmnt.setString(1, origen); ResultSet rs =
	 * pstmnt.executeQuery(); //ResultSetMetaData md = rs.getMetaData(); //int
	 * nroColumnas = md.getColumnCount(); //Map filaData = new HashMap(nroColumnas);
	 * if (!rs.next()) { logger.info("No data"); jsonResult = "{}"; } else {
	 * 
	 * for (int i = 1; i <= nroColumnas; i++) { filaData.put(md.getColumnName(i),
	 * rs.getObject(i)); }
	 * 
	 * jsonResult = rs.getString(1);// SOLO HAY UNA COLUMNA }
	 * logger.info("RESULTADO " + jsonResult); con.close(); return jsonResult; //
	 * rs.next(); }
	 * 
	 * //TODAS TIENEN LA MISMA LOGICA POR LO QUE SE USA UNA SOLA FUNCION //CUIDADO
	 * LA VARIABLE PROCEDURE POR INYECCION public List<String> getOrigins() throws
	 * SQLException { List<String> origins=new ArrayList<>(); con =
	 * Conexion.getConnection(); String stm =
	 * "SELECT cod_ori FROM \"dba\".\"ava_lisori\" where lock_ori=0"; Statement
	 * stmt=con.createStatement();
	 * 
	 * ResultSet rs = stmt.executeQuery(stm);
	 * 
	 * 
	 * while (rs.next()) { origins.add(rs.getString(1)); } con.close();
	 * 
	 * return origins; // rs.next(); }
	 */
}
