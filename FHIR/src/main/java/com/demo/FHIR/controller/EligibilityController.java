package com.demo.FHIR.controller;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.SSLContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

import org.springframework.stereotype.Controller;
import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.dstu3.model.EligibilityRequest;
import org.hl7.fhir.dstu3.model.EligibilityResponse;
import org.hl7.fhir.dstu3.model.Reference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;


@Controller
public class EligibilityController{
	private final IParser FhirParser = FhirContext.forDstu3().newJsonParser();
	FhirContext ctx = FhirContext.forDstu3();
	IParser parser = ctx.newJsonParser();
	
@RequestMapping(value = "/checkEligibility/{nhisNumber}", method = RequestMethod.POST)
@ResponseBody
public EligibilityResponse constructFhirEligibilityRequest(@PathVariable("nhisNumber") String nhisNumber) throws JsonProcessingException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        String chfID = nhisNumber;
        System.out.println(chfID);
		EligibilityRequest eligibilityRequest = new EligibilityRequest();
		
		//patient
		Reference patientReference = new Reference();
		patientReference.setReference("Patient/" +chfID);
		eligibilityRequest.setPatient(patientReference);
		System.out.println(eligibilityRequest.setPatient(patientReference));
		
		EligibilityResponse eligibilityResponseModel = checkEligibility(eligibilityRequest);
		System.out.println(eligibilityResponseModel);
		return eligibilityResponseModel;

	}

public EligibilityResponse checkEligibility(EligibilityRequest eligbilityRequest) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
	String jsonEligRequest = FhirParser.encodeResourceToString(eligbilityRequest);
	System.out.println("ABCD");
	System.out.println(sendPostRequest(jsonEligRequest,"https://demoimis.ssf.gov.np/api/api_fhir/EligibilityRequest/"));

	ResponseEntity<String> responseObject = sendPostRequest(jsonEligRequest,"https://demoimis.ssf.gov.np/api/api_fhir/EligibilityRequest/");
		System.out.println(responseObject);
	EligibilityResponse eligibilityResponse = parser.parseResource(EligibilityResponse.class, responseObject.getBody());
	System.out.println(eligibilityResponse);

	return null;
}



public static String mapToJson(Object obj) throws JsonProcessingException {

	ObjectMapper objectMapper = new ObjectMapper();
	return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);

}

private HttpHeaders createHeaders(String username, String password) {
	return new HttpHeaders() {
		private static final long serialVersionUID = 1L;
		{
			String auth = username + ":" + password;
			byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			String authHeader = "Basic " + new String(encodedAuth);
			set("Authorization", authHeader);
		}
	};
}

private ResponseEntity<String> sendPostRequest(String requestJson, String url) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

	HttpHeaders headers = createHeaders("fhir", "VDd4mRnk9FICpTckF9H2");
	headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	headers.add("Content-Type", "application/json");
	headers.add("remote-user", "openimis");
	HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
	return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
}
public HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
            = new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setHttpClient(httpClient());
    return clientHttpRequestFactory;
}

public HttpClient httpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException  {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials("", ""));

    TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

    SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
            .loadTrustMaterial(null, acceptingTrustStrategy)
            .build();

    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
    

    HttpClient client = HttpClientBuilder
            .create()
            .setSSLSocketFactory(csf)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setDefaultCredentialsProvider(credentialsProvider)
            .build();
    return client;
}
	

}
