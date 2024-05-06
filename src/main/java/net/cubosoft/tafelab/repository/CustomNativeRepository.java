package net.cubosoft.tafelab.repository;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public interface CustomNativeRepository {
	//public Query runNativeQuery(String query);
	public Object getObjectFromQuerry(String query,String parametro);
	public List<String> getListStringFromQuerry(String query);
	
	
	 
}
