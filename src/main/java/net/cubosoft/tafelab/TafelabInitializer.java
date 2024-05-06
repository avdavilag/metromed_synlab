package net.cubosoft.tafelab;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import net.cubosoft.tafelab.loop.MainThread;
import net.cubosoft.tafelab.model.DeviceModel;
import net.cubosoft.tafelab.services.AdbCommandService;
import net.cubosoft.tafelab.services.AvaModel;
import net.cubosoft.tafelab.services.MqttService;
import net.cubosoft.tafelab.services.ThingsboardService;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "net.cubosoft.tafelab")
public class TafelabInitializer {
	private static final Logger logger = LoggerFactory.getLogger(TafelabInitializer.class);

	@Value("${app.loopTime}")
	private int loopTime;
	@Value("${app.mqttBroker}")
	private String mqttBroker;
	@Value("${app.mqttClientId}")
	private String mqttClientId;
	@Value("${app.mqttPwd}")
	private String mqttPwd;
	@Value("${app.mqttTopic}")
	private String mqttTopic;
	@Value("${app.thingsboard.enable}")
	private boolean enableThingsboard;
	@Autowired
	private MqttService mqttService;
	@Autowired
	private ThingsboardService thingsboardService;
	@Autowired
	public AvaModel model;
	private MainThread wt;

	@Value("${app.adbCommand.daily}")
	private boolean enableDailyTask;
	@Autowired
	private AdbCommandService adbCommandService;
	@Value("${app.adbCommand.devices}")
	private List<String> adbDevices;

	@Value("${app.task.executionHour}")
	private int executionHour;

	@Value("${app.task.executionMinute}")
	private int executionMinute;
	private ExecutorService messageProcessor = Executors.newSingleThreadExecutor();

	@Autowired
	private ApplicationContext applicationContext;

	boolean isRestart=false;
    //@Autowired
    //private ApplicationRestarter applicationRestarter;
  


	@PostConstruct
	public void init() {
		logger.info("INCIA TAFELAB");
		// SE DEBE INICIAR EL MQTT
		mqttService.connectClient(mqttBroker, mqttClientId, mqttPwd);
		this.subscribeMqtt();

		if (!enableThingsboard) {// SI ESTA DESHABILITADO
			thingsboardService = null;
		}

		wt = new MainThread(loopTime, mqttService, mqttTopic, model);
		wt.start();

		logger.info("INICIO EL HILO");
		isRestart=false;
	}

	@Scheduled(cron = "0 ${app.task.executionMinute} ${app.task.executionHour} * * *")
	public void executeDailyTask() throws SQLException {
		logger.info("Tiene Daily: " + enableDailyTask);
		if (enableDailyTask) {
			for (String ip : adbDevices) {
				logger.info("SE CONECTA A " + ip);
				adbCommandService.connect(ip);
				logger.info("INTENTA REINICIAR " + ip);
				adbCommandService.reboot();

			}
			//REINICIA EL WAR PARA LIBERAR MEMORIA
			restartApplication();
		}

	}

	void subscribeToClient() {
		logger.info("TOPICO A SUBSCRIBIR " + mqttTopic);
		try {
			if (this.mqttService.getClient().isConnected()) {

				this.mqttService.getClient().subscribe(mqttTopic + "/#", new IMqttMessageListener() {
					@Override
					public void messageArrived(String topic, MqttMessage message) {
						messageProcessor.submit(new Runnable() {
							@Override
							public void run() {
								// Comprueba el estado del hilo
								compruebaEstadoHilo();
								procesarMensaje(message, topic);
							}
						});
					}
				});

				logger.info("SE CREO EL CONTROLADOR TOPICO");
			} else {
				logger.error("NO ESTA CONECTADO AL CLIENTE... INTENTE NUEVAMENTE");
			}
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("ERROR METODO SUBSCRIBE " + e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("ERROR DESCONOCIDO METODO SUBSCRIBE " + e.getMessage());
		}

	}

	public void subscribeMqtt() {
		this.mqttService.getClient().setCallback(new MqttCallbackExtended() {
			@Override
			public void connectComplete(boolean bReconnect, String host) {
				logger.info("RECONECTADO A " + host);// RECONECDO SE VUELVE A SUBSCRIBIR
				restartApplication();
				//subscribeToClient();

			}

			@Override
			public void connectionLost(Throwable thrwbl) {
				logger.error("Connection lost MQTT DEVICES");
			}

			@Override
			public void messageArrived(String string, MqttMessage mm) throws Exception {
				logger.info("Se activo message Arribed del callback " + mm.toString());
				/* Removed Code */

			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// TODO Auto-generated method stub
				logger.debug("DELEVERY COMPLETE " + token);

			}
		});
		subscribeToClient();
	}

	public void compruebaEstadoHilo() {
		try {
			if (!wt.isAlive()) {
				logger.error("EL HILO SE HA DETENIDO");
				logger.info("SE INTENTA INICIAR EL HILO NUEVAMENTE");
				wt.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR AL COMPROBAR ESTADO HILO");
		}

	}

	private void procesarMensaje(MqttMessage message, String topic) {

		JSONObject jsonResponses = new JSONObject();
		// logger.info("LLEGO MQTT " + topic + " msg " + message.toString());
		try {
			JSONObject messajeJson = new JSONObject(message.toString());
			// logger.info("PARSEADO"+ messajeJson.toString());
			String metodo = messajeJson.has("metodo") ? messajeJson.getString("metodo") : "";
			String sender = messajeJson.has("sender") ? messajeJson.getString("sender") : "";
			if (sender.equals("app")) {
				String origen = topic.split("/")[3];
				String dispositivo = topic.split("/")[4];
				logger.info("SOLICITA: " + metodo);
				// EN MESSAGE LLEGA EL TIPO
				// NO LE MANDO DIRECTO PARA EVITAR INYECCION
				switch (metodo) {
				case "urg":
					jsonResponses.put("tipo", "list");
					jsonResponses.put("list", "urg");
					jsonResponses.put("tafOrdenesUrgentes",
							new JSONObject(model.getProcedureWithOrigin("taf_ordenes_urgentes", origen)));
					break;
				case "hoy":
					jsonResponses.put("tipo", "list");
					jsonResponses.put("list", "hoy");
					jsonResponses.put("tafOrdenesHoy",
							new JSONObject(model.getProcedureWithOrigin("taf_ordenes_hoy", origen)));
					break;
				case "aum":
					jsonResponses.put("tipo", "list");
					jsonResponses.put("list", "aum");
					jsonResponses.put("tafOrdenesAumentos",
							new JSONObject(model.getProcedureWithOrigin("taf_ordenes_aumentos", origen)));
					break;
				case "pen":
					jsonResponses.put("tipo", "list");
					jsonResponses.put("list", "pen");
					jsonResponses.put("tafOrdenesPendientesAnteriores",
							new JSONObject(model.getProcedureWithOrigin("taf_ordenes_pendientes_anteriores", origen)));
					break;
				case "emer":
					jsonResponses.put("tipo", "list");
					jsonResponses.put("list", "emer");
					jsonResponses.put("tafOrdenesEmergencia",
							new JSONObject(model.getProcedureWithOrigin("taf_ordenes_emergencia", origen)));
					break;
				case "assets":
					jsonResponses.put("tipo", "devices");
					jsonResponses.put("assets", new JSONArray(thingsboardService.getAssets()));
					break;
				case "list-device":
					jsonResponses.put("tipo", "center");
					jsonResponses.put("tipo2", "device");
					// NO ES NECESARIO RECORRER LOS ORIGENES PARA EL THINGSBOARD
					List<DeviceModel> listaThingsboard = null;
					if (thingsboardService != null) {
						// THINGSBOARD
						try {
							if (listaThingsboard == null) {
								// DEVUELVO TODOS PARA QUE EL FRONT SE ENCARGE DE VER CUANTOS INDICA
								listaThingsboard = thingsboardService.getDevicesDTO();
								jsonResponses.put("thingsboardDevices", new JSONArray(listaThingsboard));
							}
						} catch (Exception e) {
							e.printStackTrace();
							logger.error("ERROR EN THINGSBOARD");
						}
					}
					break;
				case "grafica":
					jsonResponses.put("tipo", "center");
					jsonResponses.put("tipo2", "grafica");
					jsonResponses.put("tafGraficas",
							new JSONObject(model.getProcedureWithOrigin("taf_graficas", origen)));
					break;

				// default:

				}

				jsonResponses.put("sender", "server");
				jsonResponses.put("origen", origen);
				logger.debug("ENVIA " + jsonResponses.toString());
				// lo envio en un hilo aparte por que da error mandarle en el mismo hilo
				Thread thread = new Thread() {
					public void run() {
						String jsonString = jsonResponses.toString();
						byte[] utf8Bytes = jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
						String utf8String = new String(utf8Bytes, java.nio.charset.StandardCharsets.UTF_8);
						if (mqttService.getClient().isConnected()) {
							mqttService.publishToTopic(utf8String, mqttTopic + "/" + origen + "/" + dispositivo);
						}
					}
				};
				thread.start();
//MODO ADMIN ONLY
			} else if (sender.equals("admin-server")) { // ACCIONES DESDE FUERA
				logger.info("LLEGO MENSAJE ADMIN " + messajeJson.toString());
				String tipo = messajeJson.has("tipo") ? messajeJson.getString("tipo") : "";
				String dato = messajeJson.has("dato") ? messajeJson.getString("dato") : "";
				switch (tipo) {
				case "device-restart":
					logger.info("EMPIEZA A CONECTARSE A " + dato);
					adbCommandService.connect(dato);
					logger.info("REINICIA");
					adbCommandService.reboot();
					break;
				case "thread-start":
					wt.start();
					break;
				case "thread-stop":
					wt.stopThread();
					break;
				case "thread-destroy":
					wt.destroy();
					break;		
				case "restart-app":
					restartApplication();
					break;
				case "thread-check":
					jsonResponses.put("isAlive", wt.isAlive());
					jsonResponses.put("isInterrupted", wt.isInterrupted());
					jsonResponses.put("state", wt.getState());
					Thread thread = new Thread() {
						public void run() {
							String jsonString = jsonResponses.toString();
							byte[] utf8Bytes = jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8);
							String utf8String = new String(utf8Bytes, java.nio.charset.StandardCharsets.UTF_8);
							if (mqttService.getClient().isConnected()) {
								mqttService.publishToTopic(utf8String, mqttTopic + "/" + "server");
							}
						}
					};
					thread.start();
					break;

				}
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

	}

	public void restartApplication() {
		if(!isRestart) {
			isRestart=true;//PARA EVITAR QUE REINICIE DOS VECES
		logger.info("Se reinicia la applicacion");
		
		logger.info("Empieza a parar hilo");
		wt.stopThread();
		logger.info("Empieza a destruir hilo");
		wt.destroy();
		logger.info("Empieza a destruir el servicio mqtt");
		mqttService.destroy();
		
		//INICIO DE NUEVO
		init();
		
	}
		/* LO DEJO COMENTADO HASTA ENCONTRAR SOLUCION DE COMO REINICIAR TODA LA APP
		Thread thread = new Thread(() -> {
			try {
				logger.info("5 seg empieza a reiniciar");
				Thread.sleep(5000);
				//SpringApplication.exit(applicationContext, () -> 0);
				//SpringApplication.run(CsTafelabMqttApplication.class);
				CsTafelabMqttApplication.restart();
				//serviceRestart.restartApp();
				//applicationRestarter.restart();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("ERROR AL INTENTAR REINICIAR LA APPLICACION");
			}
		});
		thread.setDaemon(false);
		thread.start();
		
	*/
	}
}
