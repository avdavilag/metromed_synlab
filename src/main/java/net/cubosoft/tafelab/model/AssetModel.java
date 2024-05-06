package net.cubosoft.tafelab.model;

public class AssetModel {
private String id;
private String nombre;
private String tipo;
public AssetModel(String id, String nombre, String tipo) {
	this.id = id;
	this.nombre = nombre;
	this.tipo = tipo;
}

public AssetModel() {
}

public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public String getNombre() {
	return nombre;
}
public void setNombre(String nombre) {
	this.nombre = nombre;
}
public String getTipo() {
	return tipo;
}
public void setTipo(String tipo) {
	this.tipo = tipo;
}

}
