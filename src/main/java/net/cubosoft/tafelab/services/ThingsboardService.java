package net.cubosoft.tafelab.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;

import net.cubosoft.tafelab.TafelabInitializer;
import net.cubosoft.tafelab.model.AssetModel;
import net.cubosoft.tafelab.model.DeviceModel;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;

@Service
public class ThingsboardService {

	@Value("${app.thingsboard.url}")
	String url = "https://app.weelab.io:8080";

	@Value("${app.thingsboard.username}")
	String username = "demo@weelab.io";

	@Value("${app.thingsboard.pwd}")
	String password = "passdemo@";
	@Value("${app.thingsboard.profiles_allowed}")
	List<String> profilesAllowed;
	// private static final RestClient client = new RestClient(url);

	private static final Logger logger = LoggerFactory.getLogger(ThingsboardService.class);

	public List<DeviceModel> getDevicesDTOFromAsset(String assets) {
		RestClient client = new RestClient(url);
		List<DeviceModel> listDevices = new ArrayList<>();
		try {
			// ThingsBoard REST API URL
			client.login(username, password);
			// https://thingsboard.io/docs/user-guide/telemetry/
			PageData<Device> pageData;
			PageLink pageLink = new PageLink(10);
			List<EntityRelation> listEntityRelation;
			List<AssetModel> assetsList = new ArrayList<AssetModel>();
			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
			// logger.info("PASO USER");
			do {
				pageData = client.getCustomerDevices(user.getCustomerId(), "", pageLink);

				java.util.List<Device> listD = pageData.getData();
				// OBTENGO LA LISTA DE DISPOSITIVOS
				for (int i = 0; listD.size() > i; i++) {
					// DATOS DISPOSITIVO
					DeviceModel deviceDTO = new DeviceModel();
					deviceDTO.setId(listD.get(i).getId().toString());
					deviceDTO.setLabel(listD.get(i).getLabel());
					deviceDTO.setName(listD.get(i).getName());
					

					// TEMPERATURA Y HUMEDAD
					java.util.List<TsKvEntry> dataTelemetry = client.getLatestTimeseries(listD.get(i).getId(),
							Arrays.asList("temperature", "humidity"));
					for (TsKvEntry tsk : dataTelemetry) {
						if (tsk.getKey() == "temperature") {
							deviceDTO.setTemperature(tsk.getValueAsString());
						} else {
							deviceDTO.setHumidity(tsk.getValueAsString());
						}
					}
					// ATRIBUTOS
					java.util.List<AttributeKvEntry> dataAtributes = client.getAttributesByScope(listD.get(i).getId(),
							"SERVER_SCOPE",
							Arrays.asList("active", "battery", "lastActivityTime", "humidityThresholdHigh",
									"humidityThresholdLow", "temperatureThresholdHigh", "temperatureThresholdLow",
									"temperatureThresholdHigh", "rssi", "maintenance"));

					for (AttributeKvEntry attr : dataAtributes) {
						String key = attr.getKey();
						switch (key) {
						case "active":
							deviceDTO.setActive(attr.getValueAsString());
							break;
						case "battery":
							deviceDTO.setBattery(attr.getValueAsString());
							break;
						case "lastActivityTime":
							deviceDTO.setLastUpdate(attr.getValueAsString());
							break;
						case "humidityThresholdHigh":
							deviceDTO.setMaxHumidity(attr.getValueAsString());
							break;
						case "humidityThresholdLow":
							deviceDTO.setMinHumidity(attr.getValueAsString());
							break;
						case "temperatureThresholdHigh":
							deviceDTO.setMaxTemperature(attr.getValueAsString());
							break;
						case "temperatureThresholdLow":
							deviceDTO.setMinTemperature(attr.getValueAsString());
							break;
						case "rssi":
							deviceDTO.setRssi(attr.getValueAsString());
							break;
						case "maintenance":
							deviceDTO.setMaintenance(attr.getValueAsString());
							break;

						// default:

						}
					}

					// BUSCO LAS RELACIONES
					listEntityRelation = client.findByTo(listD.get(i).getId(), "Contains", RelationTypeGroup.COMMON);
					assetsList.clear();
					for (EntityRelation entRelation : listEntityRelation) {
						if (entRelation.getFrom().getEntityType().name().equals("ASSET")) {
							logger.info("LLAMO ASSET " + entRelation.getFrom().getEntityType().name());
							assetsList.add(new AssetModel(entRelation.getFrom().getId().toString(), "", ""));
						}
					}
					deviceDTO.setAssetsList(assetsList);

					listEntityRelation = null;
					dataTelemetry = null;
					dataAtributes = null;

					listDevices.add(deviceDTO);
				}
				pageLink = pageLink.nextPageLink();// CAMBIO DE PAGINACION

			} while (pageData.hasNext());
			client.logout();
			client.close();

		} catch (Exception e) {
			e.printStackTrace();
			client.logout();
			client.close();
			logger.error("ERROR SERVICIO THINGSBOARD " + e.getMessage());

		}
		return listDevices;
	}

	public List<DeviceModel> getDevicesDTO() {
		RestClient client = new RestClient(url);
		List<DeviceModel> listDevices = new ArrayList<>();
		try {
			// ThingsBoard REST API URL
			client.login(username, password);
			// https://thingsboard.io/docs/user-guide/telemetry/
			PageData<Device> pageData;
			PageLink pageLink = new PageLink(10);

			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
			//logger.info("PASO USER");
			do {
				pageData = client.getCustomerDevices(user.getCustomerId(), "", pageLink);

				java.util.List<Device> listD = pageData.getData();
				// OBTENGO LA LISTA DE DISPOSITIVOS
				for (int i = 0; listD.size() > i; i++) {
					//VALIDAMOS SI ESTE PROFILE ESTA PERMITIDO PARA MOSTRAR
					if(!profilesAllowed.contains(listD.get(i).getDeviceProfileId().getId().toString())) {
						continue;
					}

					// DATOS DISPOSITIVO
					DeviceModel deviceDTO = new DeviceModel();
					deviceDTO.setId(listD.get(i).getId().toString());
					deviceDTO.setLabel(listD.get(i).getLabel());
					deviceDTO.setName(listD.get(i).getName());
					
					java.util.List<TsKvEntry> dataTelemetry ;
					// TEMPERATURA Y HUMEDAD
					// AQUI DA ERROR SI ESTA NULL
					try {
						// LLAMA TEMPERATURA
						dataTelemetry = client.getLatestTimeseries(listD.get(i).getId(),
								Arrays.asList("temperature"));
						if (!dataTelemetry.isEmpty())
							deviceDTO.setTemperature(dataTelemetry.get(0).getValueAsString());
					} catch (Exception e) {
						//e.printStackTrace();
						if(!e.getMessage().equals("Can't parse value: null")) {//EL ERROR DE NULL ES CUANDO NO ENCUENTRA
							logger.error("ERROR AL RECUPERAR TEMPERATURA DE " + listD.get(i).getName().toString());
							logger.error("ERROR "+e.getMessage());
						}
						//continue;
					}
					try {
						// LLAMA HUMEDAD
						 dataTelemetry = client.getLatestTimeseries(listD.get(i).getId(),
								Arrays.asList("humidity"));
						if (!dataTelemetry.isEmpty())
							deviceDTO.setHumidity(dataTelemetry.get(0)
									.getValueAsString()); /*
															 * for (TsKvEntry tsk : dataTelemetry) { if (tsk.getKey() ==
															 * "temperature") {
															 * deviceDTO.setTemperature(tsk.getValueAsString()); } else
															 * { deviceDTO.setHumidity(tsk.getValueAsString()); } }
															 */
					} catch (Exception e) {
						//e.printStackTrace();
						if(!e.getMessage().equals("Can't parse value: null")) {
							logger.error("ERROR AL RECUPERAR HUMEDAD DE " + listD.get(i).getName().toString());
							logger.error("ERROR "+e.getMessage());
						}
						//continue;
					}

					// ATRIBUTOS
					try {
						java.util.List<AttributeKvEntry> dataAtributes = client.getAttributesByScope(
								listD.get(i).getId(), "SERVER_SCOPE",
								Arrays.asList("active", "battery", "lastActivityTime", "humidityThresholdHigh",
										"humidityThresholdLow", "temperatureThresholdHigh", "temperatureThresholdLow",
										"temperatureThresholdHigh", "rssi", "maintenance"));

						for (AttributeKvEntry attr : dataAtributes) {
							String key = attr.getKey();
							switch (key) {
							case "active":
								deviceDTO.setActive(attr.getValueAsString());
								break;
							case "battery":
								deviceDTO.setBattery(attr.getValueAsString());
								break;
							case "lastActivityTime":
								deviceDTO.setLastUpdate(attr.getValueAsString());
								break;
							case "humidityThresholdHigh":
								deviceDTO.setMaxHumidity(attr.getValueAsString());
								break;
							case "humidityThresholdLow":
								deviceDTO.setMinHumidity(attr.getValueAsString());
								break;
							case "temperatureThresholdHigh":
								deviceDTO.setMaxTemperature(attr.getValueAsString());
								break;
							case "temperatureThresholdLow":
								deviceDTO.setMinTemperature(attr.getValueAsString());
								break;
							case "rssi":
								deviceDTO.setRssi(attr.getValueAsString());
								break;
							case "maintenance":
								deviceDTO.setMaintenance(attr.getValueAsString());
								break;

							// default:

							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("ERROR AL RECUPERAR ATRIBUTOS DE " + listD.get(i).getId().toString());
						logger.error("PASO ESTE DISPOSITIVO E:"+e.getMessage());
						continue;
					}

					// BUSCO LAS RELACIONES
					List<EntityRelation> listEntityRelation = client.findByTo(listD.get(i).getId(), "Contains", RelationTypeGroup.COMMON);
					logger.debug("RELACION DE "+listD.get(i).getId().toString());
					List<AssetModel> assetsList = new ArrayList<AssetModel>();
					for (EntityRelation entRelation : listEntityRelation) {
						logger.debug("RELACION FROM "+entRelation.getFrom().toString());
						if (entRelation.getFrom().getEntityType().name().equals("ASSET")) {
							
							// logger.info("LLAMO ASSET " +entRelation.getFrom().getEntityType().name());
							assetsList.add(new AssetModel(entRelation.getFrom().getId().toString(), "", ""));
						}
					}
					deviceDTO.setAssetsList(assetsList);

					listEntityRelation = null;
					// dataTelemetry = null;
					// dataAtributes = null;

					listDevices.add(deviceDTO);
				}
				pageLink = pageLink.nextPageLink();// CAMBIO DE PAGINACION

			} while (pageData.hasNext());
			client.logout();
			client.close();

		} catch (Exception e) {
			e.printStackTrace();
			client.logout();
			client.close();
			logger.error("ERROR SERVICIO THINGSBOARD " + e.getMessage());

		}
		return listDevices;
	}

	public List<AssetModel> getAssets() {
		RestClient client = new RestClient(url);
		List<AssetModel> listAssets = new ArrayList<>();
		try {
			// ThingsBoard REST API URL
			client.login(username, password);
			// https://thingsboard.io/docs/user-guide/telemetry/
			PageData<Asset> pageData;
			PageLink pageLink = new PageLink(10);
			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));

			do {
				pageData = client.getCustomerAssets(user.getCustomerId(), pageLink, "Lab");

				java.util.List<Asset> listA = pageData.getData();
				logger.info("Total Asset " + listA.size());
				// OBTENGO LA LISTA DE Assets
				for (int i = 0; listA.size() > i; i++) {
					// DATOS DISPOSITIVO
					AssetModel assetModel = new AssetModel();
					assetModel.setId(listA.get(i).getId().toString());
					assetModel.setNombre(listA.get(i).getName());
					assetModel.setTipo("Lab");

					listAssets.add(assetModel);
				}
				pageLink = pageLink.nextPageLink();// CAMBIO DE PAGINACION

			} while (pageData.hasNext());
			client.logout();
			client.close();

		} catch (Exception e) {
			e.printStackTrace();
			client.logout();
			client.close();
			logger.error("ERROR SERVICIO THINGSBOARD " + e.getMessage());

		}
		return listAssets;
	}

}
