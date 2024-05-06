package net.cubosoft.tafelab.controller;

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.DashboardInfo;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;

import net.cubosoft.tafelab.model.DeviceModel;

@RestController
@RequestMapping("/prueba")
public class PruebaController {

	@GetMapping
	void prueba() {
		// ThingsBoard REST API URL
		String url = "http://localhost:8080";

		// Default Tenant Administrator credentials
		String username = "tenant@thingsboard.org";
		String password = "tenant";

		// Creating new rest client and auth with credentials
		RestClient client = new RestClient(url);
		client.login(username, password);

		PageData<Device> tenantDevices;
		PageLink pageLink = new PageLink(10);
		do {
			// Fetch all tenant devices using current page link and print each of them
			tenantDevices = client.getTenantDevices("", pageLink);
			tenantDevices.getData().forEach(System.out::println);
			pageLink = pageLink.nextPageLink();
		} while (tenantDevices.hasNext());

		// Perform logout of current user and close the client
		client.logout();
		client.close();
	}

	@GetMapping("/dash")
	void dashboardTenant() {
		// Properties systemProps = System.getProperties();
		// systemProps.put("javax.net.ssl.keyStorePassword","passwordForKeystore");
		// systemProps.put("javax.net.ssl.keyStore","pathToKeystore.ks");
		// systemProps.put("javax.net.ssl.trustStore", "pathToTruststore.ts");
		// systemProps.put("javax.net.ssl.trustStorePassword","passwordForTrustStore");
		// System.setProperties(systemProps);
		// ThingsBoard REST API URL
		String url = "https://app.weelab.io:8080";

		// Default Tenant Administrator credentials
		String username = "tenant@thingsboard.org";
		String password = "veronika";

		// Creating new rest client and auth with credentials
		RestClient client = new RestClient(url);
		client.login(username, password);

		PageData<DashboardInfo> pageData;
		PageLink pageLink = new PageLink(10);
		do {
			// Fetch all tenant dashboards using current page link and print each of them
			pageData = client.getTenantDashboards(pageLink);
			pageData.getData().forEach(System.out::println);
			pageLink = pageLink.nextPageLink();
		} while (pageData.hasNext());

		// Perform logout of current user and close the client
		client.logout();
		client.close();
	}

	@GetMapping("/devcli")
	Object deviceClient() {
		// ThingsBoard REST API URL
		String url = "https://app.weelab.io:8080";
		// Perform login with default Customer User credentials
		String username = "demo@weelab.io";
		String password = "passdemo@";
		RestClient client = new RestClient(url);
		client.login(username, password);

		PageData<Device> pageData;
		PageLink pageLink = new PageLink(10);
		do {
			// Get current user
			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
			// Fetch customer devices using current page link
			pageData = client.getCustomerDevices(user.getCustomerId(), "", pageLink);
			pageData.getData().forEach(System.out::println);
			pageLink = pageLink.nextPageLink();

		} while (pageData.hasNext());

		// Perform logout of current user and close the client
		//client.logout();
		//client.close();
		return client.getAttributeKvEntries(pageData.getData().get(0).getId(),Arrays.asList("active", "battery", "lastActivityTime","rssi"));
		//return pageData;
	}

	@GetMapping("/devcliRela")
	Object deviceClientWhitRelation() {
		// ThingsBoard REST API URL
		String url = "https://app.weelab.io:8080";
		// Perform login with default Customer User credentials
		String username = "demo@weelab.io";
		String password = "passdemo@";
		RestClient client = new RestClient(url);
		client.login(username, password);

		PageData<Device> pageData;
		PageLink pageLink = new PageLink(10);
		do {
			// Get current user
			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
			// Fetch customer devices using current page link
			pageData = client.getCustomerDevices(user.getCustomerId(), "", pageLink);
			pageData.getData().forEach(System.out::println);
			pageLink = pageLink.nextPageLink();
		} while (pageData.hasNext());

		// Perform logout of current user and close the client
		//client.logout();
		//client.close();
		return client.findByTo(pageData.getData().get(0).getId(),"Contains",RelationTypeGroup.COMMON).get(0);
		//return pageData;
	}
	@GetMapping("/relAsset")
	Object relacionesAssets() {
		// ThingsBoard REST API URL
		String url = "https://app.weelab.io:8080";
		// Perform login with default Customer User credentials
		String username = "demo@weelab.io";
		String password = "passdemo@";
		RestClient client = new RestClient(url);
		client.login(username, password);

		PageData<Device> pageData;
		PageData<Asset> assets;
		
		PageLink pageLink = new PageLink(10);
		do {
			// Get current user
			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
			assets= client.getCustomerAssets(user.getCustomerId(), pageLink, "Lab");
			
			// Fetch customer devices using current page link
			//pageData = client.getCustomerDevices(user.getCustomerId(), "", pageLink);
			
			//pageData.getData().forEach(System.out::println);
			pageLink = pageLink.nextPageLink();

		} while (assets.hasNext());

		// Perform logout of current user and close the client
		//client.logout();
		//client.close();
		//return client.getAttributeKvEntries(pageData.getData().get(0).getId(),Arrays.asList("active", "battery", "lastActivityTime","rssi"));
		
		return assets.getData();
	}


	@GetMapping("/devcli-test")
	Object deviceClientTest() {
		// ThingsBoard REST API URL
		String url = "https://app.weelab.io:8080";
		// Perform login with default Customer User credentials
		String username = "demo@weelab.io";
		String password = "passdemo@";
		RestClient client = new RestClient(url);
		client.login(username, password);
		java.util.List<DeviceModel> listDevices = new ArrayList<>();
//https://thingsboard.io/docs/user-guide/telemetry/
		PageData<Device> pageData;
		PageLink pageLink = new PageLink(10);

		User user = client.getUser().orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
		do {
			pageData = client.getCustomerDevices(user.getCustomerId(), "", pageLink);
			java.util.List<Device> listD=pageData.getData();
			//OBTENGO LA LISTA DE DISPOSITIVOS
			for(int i=0;listD.size()>i;i++) {
				
				//DATOS DISPOSITIVO
				DeviceModel deviceDTO = new DeviceModel();
				deviceDTO.setId(listD.get(i).getId().toString());
				deviceDTO.setLabel(listD.get(i).getLabel());
				deviceDTO.setName(listD.get(i).getName());

				// TEMPERATURA Y HUMEDAD
				java.util.List<TsKvEntry> dataTelemetry = client.getLatestTimeseries(listD.get(i).getId(),
						Arrays.asList("temperature", "humidity"));
				deviceDTO.setHumidity(dataTelemetry.get(0).getValueAsString());// temperature)
				deviceDTO.setTemperature(dataTelemetry.get(1).getValueAsString());// humidity
				
				// ATRIBUTOS
				java.util.List<AttributeKvEntry> dataAtributes = client.getAttributesByScope(listD.get(i).getId(),
						"SERVER_SCOPE", Arrays.asList("active", "battery", "lastActivityTime"));
				deviceDTO.setActive(dataAtributes.get(0).getValueAsString());
				deviceDTO.setBattery(dataAtributes.get(1).getValueAsString());
				deviceDTO.setLastUpdate(dataAtributes.get(2).getValueAsString());
				dataTelemetry=null;
				dataAtributes=null;

				listDevices.add(deviceDTO);
			}
			// Device device=pageData.getData().get(0);
			pageLink = pageLink.nextPageLink();//CAMBIO DE PAGINACION

		} while (pageData.hasNext());

		// client.getAttributesByScope(null, "SERVER_SCOPE",
		// Arrays.asList("temperatureeThressholHight"));

		//OBTENGO LOS 10 primeros
		// client.getCustomerDashboards(user.getCustomerId(),
		// pageLink).getData().get(0).
		// client.getWidgetsBundles().get(0).
		// client.plu
		// Perform logout of current user and close the client
		client.logout();
		client.close();
		return listDevices;
	}

	@GetMapping("/dashcli")
	void dashboardClient() {
		// ThingsBoard REST API URL
		String url = "https://app.weelab.io:8080";
		// Perform login with default Customer User credentials
		String username = "demo@weelab.io";
		String password = "passdemo@";
		RestClient client = new RestClient(url);
		client.login(username, password);

		PageData<DashboardInfo> pageData;
		PageLink pageLink = new PageLink(10);
		do {
			// Get current user
			User user = client.getUser()
					.orElseThrow(() -> new IllegalStateException("No logged in user has been found"));
			// Fetch customer devices using current page link
			pageData = client.getCustomerDashboards(user.getCustomerId(), pageLink);
			pageData.getData().forEach(System.out::println);
			pageLink = pageLink.nextPageLink();
		} while (pageData.hasNext());

		// Perform logout of current user and close the client
		client.logout();
		client.close();
	}
}
