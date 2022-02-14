package cc.cloudit;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.sql.Timestamp;
import java.util.List;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.client.*;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cloud.objectstorage.services.s3.model.Bucket;
import com.ibm.cloud.objectstorage.services.s3.model.ListObjectsRequest;
import com.ibm.cloud.objectstorage.services.s3.model.ObjectListing;
import com.ibm.cloud.objectstorage.services.s3.model.S3ObjectSummary;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;

import java.io.File;

public class UploadHelper extends IntentService {

    private String bucketName = "testbucket-cc2020";
    private String api_key = "YGDmds2aAzEonjpHl-yYHBifWP-2va5fEOQ-EV08XjK2";
    private String service_instance_id = "crn:v1:bluemix:public:cloud-object-storage:global:a/57c8e2d9efbf4ee0becba868dcf62658:41bb6aa3-6f2e-40f4-8489-08234eceedc0::";
    private String endpoint_url = "s3.eu-gb.cloud-object-storage.appdomain.cloud";

    private String storageClass = "eu-gb-standard";
    private String location = "eu-gb";

    public UploadHelper() {
        super("HelloIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AWSCredentials credentials;
        if (endpoint_url.contains("objectstorage.softlayer.net")) {
            credentials = new BasicIBMOAuthCredentials(api_key, service_instance_id);
        } else {
            String access_key = api_key;
            String secret_key = service_instance_id;
            credentials = new BasicAWSCredentials(access_key, secret_key);
        }
        ClientConfiguration clientConfig = new ClientConfiguration().withRequestTimeout(5000);
        clientConfig.setUseTcpKeepAlive(true);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new EndpointConfiguration(endpoint_url, location)).withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfig)
                .build();

        s3Client.putObject(bucketName,"teste",new File(intent.getStringExtra("Path")));

    }
}