package net.cubosoft.tafelab.controller;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import net.cubosoft.tafelab.services.AvaModel;
import net.cubosoft.tafelab.services.MqttService;


//@RestController
public class AvaController {

@Autowired
private MqttService mqttController;

	AvaModel avaModel=new AvaModel();
	@GetMapping("/check")
	Object prueba() throws SQLException{
		JSONObject jsonResponses = new JSONObject();
		//OBTENGO TODOS LOS ORIGENES PARA PUBLICAR
		List<String> origins= avaModel.getOrigins();
		for(String origen:origins) {
			jsonResponses.put("tafOrdenes", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes", origen)));
			jsonResponses.put("tafOrdenes2", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_2", origen)));
			jsonResponses.put("tafPacientesGenero", new JSONObject(avaModel.getProcedureWithOrigin("taf_pacientes_genero", origen)));
			jsonResponses.put("tafGraficas", new JSONObject(avaModel.getProcedureWithOrigin("taf_graficas", origen)));
			jsonResponses.put("tafReferencias", new JSONObject(avaModel.getProcedureWithOrigin("taf_referencias", origen)));
			jsonResponses.put("tafEntregaEstado", new JSONObject(avaModel.getProcedureWithOrigin("taf_entrega_estado", origen)));
			jsonResponses.put("tafEntregaGrupo", new JSONObject(avaModel.getProcedureWithOrigin("taf_entrega_grupo", origen)));
			jsonResponses.put("tafOrdenesAnterior", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_anterior", origen)));
			jsonResponses.put("tafSolicitadasMas", new JSONObject(avaModel.getProcedureWithOrigin("taf_solicitadas_mas", origen)));
			jsonResponses.put("tafSolicitadasMenos", new JSONObject(avaModel.getProcedureWithOrigin("taf_solicitadas_menos", origen)));
			jsonResponses.put("tafOrdenesUrgentes", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_urgentes", origen)));
			jsonResponses.put("tafOrdenesAumentos", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_aumentos", origen)));
			jsonResponses.put("tafOrdenesHoy", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_hoy", origen)));
			jsonResponses.put("tafOrdenesPendientesAnteriores", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_pendientes_anteriores", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
		}
		return jsonResponses.toString();
	}
	

	@GetMapping("/check2")
	Object prueba3() throws SQLException{
		JSONObject jsonResponses = new JSONObject();
		//OBTENGO TODOS LOS ORIGENES PARA PUBLICAR
		List<String> origins= avaModel.getOrigins();
		for(String origen:origins) {
			jsonResponses.put("tafOrdenes", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafOrdenes2", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_2", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafPacientesGenero", new JSONObject(avaModel.getProcedureWithOrigin("taf_pacientes_genero", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafGraficas", new JSONObject(avaModel.getProcedureWithOrigin("taf_graficas", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafReferencias", new JSONObject(avaModel.getProcedureWithOrigin("taf_referencias", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafEntregaEstado", new JSONObject(avaModel.getProcedureWithOrigin("taf_entrega_estado", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafEntregaGrupo", new JSONObject(avaModel.getProcedureWithOrigin("taf_entrega_grupo", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafOrdenesAnterior", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_anterior", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafSolicitadasMas", new JSONObject(avaModel.getProcedureWithOrigin("taf_solicitadas_mas", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafSolicitadasMenos", new JSONObject(avaModel.getProcedureWithOrigin("taf_solicitadas_menos", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafOrdenesUrgentes", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_urgentes", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafOrdenesAumentos", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_aumentos", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafOrdenesHoy", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_hoy", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);
			jsonResponses.put("tafOrdenesPendientesAnteriores", new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_pendientes_anteriores", origen)));
			mqttController.publishToTopic(jsonResponses.toString(), "prueba/test/"+origen);

		}
		return jsonResponses.toString();
	}
	
	@GetMapping("/check2")
	Object prueba2() throws SQLException{
		//JSONObject jsonResponses = new JSONObject();
		return avaModel.getOrigins();
	}
}
