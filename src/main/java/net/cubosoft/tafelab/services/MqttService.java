package net.cubosoft.tafelab.services;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MqttService {
	String userName = "tafelab_main";
	private MqttClient client;
	private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

	public void connectClient(String broker, String clientId, String pwd) {
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			client = new MqttClient(broker, clientId, persistence);
			// MQTT connection option
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setUserName(userName);
			connOpts.setPassword(pwd.toCharArray());
			// retain sessionconnOpts.setKeepAliveInterval(30);
			// connOpts.setKeepAliveInterval(60);//TIEMPO QUE MANTIENE VIVA LA CONEXION0

			// connOpts.setConnectionTimeout(10);
			// connOpts.setAutomaticReconnect(true);
			// connOpts.setCleanSession(true);

			connOpts.setKeepAliveInterval(30);
			connOpts.setConnectionTimeout(10);
			connOpts.setAutomaticReconnect(true);
			connOpts.setCleanSession(false);
			// set callback
			// client.setCallback(new OnMessageCallback());

			// establish a connection
			logger.info("Connecting to broker: " + broker);
			client.connect(connOpts);

			logger.info("CONECTADO MQTT THINGSBOARD: clientid " + clientId + ", user " + userName);

		} catch (MqttException me) {
			logger.error("reason " + me.getReasonCode());
			logger.error("msg " + me.getMessage());
			logger.error("loc " + me.getLocalizedMessage());
			logger.error("cause " + me.getCause());
			logger.error("excep " + me);
			me.printStackTrace();
		}

	}

	public void publishToTopic(String message, String pubTopic) {
		try {
			MqttMessage mqttMessage = new MqttMessage();
			// MQTT NO ESTA ENVIANDO EN UTF 8
			mqttMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));
			mqttMessage.setQos(0);
			if (client.isConnected()) {
				client.publish(pubTopic, mqttMessage);
				logger.debug("Message published");
			}

			// client.disconnect();
			// System.out.println("Disconnected");
			// client.close();
		} catch (MqttException me) {
			logger.error("reason " + me.getReasonCode());
			logger.error("msg " + me.getMessage());
			logger.error("loc " + me.getLocalizedMessage());
			logger.error("cause " + me.getCause());
			logger.error("excep " + me);
			me.printStackTrace();
		}
	}

	public MqttClient getClient() {
		return client;
	}

	public void setClient(MqttClient client) {
		this.client = client;
	}

	@PreDestroy
	public void destroy3() {
		// MANDO A CERRAR LAS CONEXION DEL WAR YA QUE SE SUELE QUEDAR CONSUMIENDO
		// RECURSOS CUANDO SE DESHABILITA
		if (client != null) {
			try {
				if (client.isConnected()) {
					client.disconnectForcibly();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se logro desconectar mqtt correctamente");
			}

			try {
				client.close(true);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se logro cerrar mqtt correctamente");
			}
		}

	}

	public void destroy() {
		if (client != null) {
			try {
				if (client.isConnected()) {
					client.disconnectForcibly();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se logro desconectar mqtt correctamente");
			}

			try {
				client.close(true);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se logro cerrar mqtt correctamente");
			}
		}
	}
}
