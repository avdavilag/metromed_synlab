package net.cubosoft.tafelab.loop;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hibernate.exception.JDBCConnectionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.cubosoft.tafelab.model.DeviceModel;
import net.cubosoft.tafelab.services.AvaModel;
import net.cubosoft.tafelab.services.MqttService;
import net.cubosoft.tafelab.services.ThingsboardService;

public class MainThread extends Thread {

	private int sleepSeconds;

	private MqttService mqttController;
	// private ThingsboardService thingsboardService;
	private String mqttTopic;
	private AvaModel avaModel;
	private static final Logger logger = LoggerFactory.getLogger(MainThread.class);
	private volatile boolean stopped;

	public void stopThread() {
		stopped = true;
	}

	public MainThread(int sleepSeconds, MqttService mqttController, String mqttTopic, AvaModel model) {
		// ThingsboardService thingsboardService) {
		setDaemon(false);
		this.sleepSeconds = sleepSeconds;
		this.mqttController = mqttController;
		this.avaModel = model;
		this.mqttTopic = mqttTopic;
		// this.thingsboardService = thingsboardService;
	}

	@Override
	public void run() {
		logger.info("START BACKEND TAFELAB");
		JSONObject jsonResponses = new JSONObject();
		// int layers=4;
		int currentLayer = 1;// CONTADOR
		List<String> origins = null;
		// String flag = "grafica";// device
		// List<DeviceModel> listaThingsboard = null;
		try {
			origins = avaModel.getOrigins();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!stopped) {
			try {
				if(mqttController.getClient().isConnected()) {
					for (String origen : origins) {
						// while(currentLayer<layers) {
						// currentLayer = 1;// Prueba
						if (origen != null) {
							if (currentLayer == 2) {
								jsonResponses.put("tafOrdenes",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes", origen)));

								jsonResponses.put("tafOrdenes2",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_2", origen)));
								jsonResponses.put("tafPacientesGenero",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_pacientes_genero", origen)));
							}
							if (currentLayer == 3) {
								jsonResponses.put("tafSolicitadasMas",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_solicitadas_mas", origen)));
							}
							if (currentLayer == 4) {
								jsonResponses.put("tafSolicitadasMenos",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_solicitadas_menos", origen)));
							}
							if (currentLayer == 1) {
								jsonResponses.put("tafReferencias",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_referencias", origen)));
								jsonResponses.put("tafEntregaGrupo",
										new JSONObject(avaModel.getProcedureWithOrigin("taf_entrega_grupo", origen)));
							}

							// las demas van en todas
							jsonResponses.put("tafOrdenesAnterior",
									new JSONObject(avaModel.getProcedureWithOrigin("taf_ordenes_anterior", origen)));

							jsonResponses.put("tafEntregaEstado",
									new JSONObject(avaModel.getProcedureWithOrigin("taf_entrega_estado", origen)));
							/*
							 * jsonResponses.put("tipo2", flag); // varia entre graficas y dispositivos if
							 * (flag.equals("grafica")) { jsonResponses.put("tafGraficas", new
							 * JSONObject(avaModel.getProcedureWithOrigin("taf_graficas", origen))); }
							 */
							jsonResponses.put("tipo", "layer");
							jsonResponses.put("layer", currentLayer);
							jsonResponses.put("sender", "server");
							jsonResponses.put("origen", origen);
							logger.debug("publica " + mqttTopic + "/" + origen + "\t msg: " + jsonResponses.toString());
							if (mqttController.getClient().isConnected()) {// SI ESTA CONECTADO ENVIA
								String jsonString = jsonResponses.toString();
								byte[] utf8Bytes = jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
								String utf8String = new String(utf8Bytes, java.nio.charset.StandardCharsets.UTF_8);

								mqttController.publishToTopic(utf8String, mqttTopic + "/" + origen);
								// mqttController.publishToTopic("TAFPRUEBA á é ñ", mqttTopic + "/" + origen);
							} else {
								logger.info("THREAD CLIENTE NO ESTA CONECTADO..");
							}
							jsonResponses = new JSONObject();
						}

						if (currentLayer == 3)
							currentLayer = 0;

						currentLayer++;
					}
				}else {
					logger.info("No esta conectado mqtt");
				}
		
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				logger.error("ERROR CREANDO EL JSON");
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("ERROR EN LA BASE");
			} catch (JDBCConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("ERROR DESCONOCIDO BASE");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("ERROR DESCONOCIDO Hilo");
			}

			// DESPUES LO DORMIMOS
			try {
				sleep(this.sleepSeconds);
			} catch (InterruptedException e) {
				// handle exception here
				e.printStackTrace();
				logger.error("ERROR EN EL HILO");
			} catch (Exception e) {
				// handle exception here
				e.printStackTrace();
				logger.error("ERROR DESCONOCIDO PAUSANDO HILO");
			}
		}
		logger.info("HA FINALIZADO EL WHILE DEL HILO");

	}

	@PreDestroy
	public void destroy() {
		stopThread();
	}

}