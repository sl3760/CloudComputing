package assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.io.IOException;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.storagegateway.model.VolumeStatus;
import com.amazonaws.services.ec2.model.*;

public class Monitor {
	AmazonEC2      ec2;
    AmazonS3Client s3;
    String instanceId;
    String instanceImageId;
    String createdVolumeId;
    String createdSnapshotId;
    String instanceIp;
    AWSCredentials credentials;
    public Monitor(String instanceId) throws IOException{
    	this.credentials = new PropertiesCredentials(
    			 Main.class.getResourceAsStream("AwsCredentials.properties")); 
 		ec2 = new AmazonEC2Client(credentials);
    	this.instanceId = instanceId;
    }
    
    public void createS3(String string){
    	System.out.println("Now creating S3 ");
    	s3  = new AmazonS3Client(this.credentials);
        
        //create bucket
        String bucketName = string;
        s3.createBucket(bucketName);
        
        //set key
        String key = "object-name.txt";
        
        //set value
        File file;
		try {
			file = File.createTempFile("temp", ".txt");
		
        file.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
		writer.write("This is a sample sentence.\r\nYes!");
		
        writer.close();
        
        //put object - bucket, key, value(file)
        s3.putObject(new PutObjectRequest(bucketName, key, file));
        
        //get object
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
        BufferedReader reader = new BufferedReader(
        	    new InputStreamReader(object.getObjectContent()));
        String data = null;
        while ((data = reader.readLine()) != null) {
            System.out.println(data);
        } 
		}catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void createIP(){
    	System.out.println("Now allocat IP");
		AllocateAddressResult addressResult = ec2.allocateAddress();
		instanceIp= addressResult.getPublicIp();		
		System.out.println("New elastic IP: "+instanceIp);	
    }
    public void associateIP(String instanceId, String instanceIp){
    	//associate the ip to instance
    	 System.out.println("Now associate IP");    			 
         AssociateAddressRequest aar = new AssociateAddressRequest();   	 
    	 aar.setInstanceId(instanceId);
    	 aar.setPublicIp(instanceIp);
    	 ec2.associateAddress(aar);
    }
    
    public void disassociateIP(String instanceIp){
    	System.out.println("Now disassociate IP");
    	DisassociateAddressRequest dar = new DisassociateAddressRequest();
		dar.setPublicIp(instanceIp);
		ec2.disassociateAddress(dar);
    }
    
    public void createVolume(){			
	    try{
      	//create a volume
	    	System.out.println("Now create a volume: ");
     	    CreateVolumeRequest cvr = new CreateVolumeRequest();
	        cvr.setAvailabilityZone("us-east-1a");
	        cvr.setSize(1); 
     	    CreateVolumeResult volumeResult = ec2.createVolume(cvr);
     	    createdVolumeId = volumeResult.getVolume().getVolumeId();
     	    System.out.println("New created volume : "+createdVolumeId);
	    }catch (AmazonServiceException ase) {
         System.out.println("Caught Exception: " + ase.getMessage());
         System.out.println("Reponse Status Code: " + ase.getStatusCode());
         System.out.println("Error Code: " + ase.getErrorCode());
         System.out.println("Request ID: " + ase.getRequestId());
       }
	}
    
   
    
    public void attachVolume(String instanceId,String createdVolume){
		try{
	      	//create a volume
			System.out.println("Attach the new created volume : ");
			AttachVolumeRequest avr = new AttachVolumeRequest();
         	avr.setVolumeId(createdVolumeId);
         	avr.setInstanceId(instanceId);
         	avr.setDevice("/dev/sdf");
         	ec2.attachVolume(avr);
        	System.out.println("attaching "+createdVolumeId+" to "+ instanceId);
		    }catch (AmazonServiceException ase) {
	         System.out.println("Caught Exception: " + ase.getMessage());
	         System.out.println("Reponse Status Code: " + ase.getStatusCode());
	         System.out.println("Error Code: " + ase.getErrorCode());
	         System.out.println("Request ID: " + ase.getRequestId());
	       }
	}
	
	public void detachVolume(String createdVolumeId){
		try{
	      	//create a volume
			System.out.println("Detach the created volume : ");
			DetachVolumeRequest dvr = new DetachVolumeRequest();
	        dvr.setVolumeId(createdVolumeId);
	        dvr.setInstanceId(instanceId);
	        ec2.detachVolume(dvr);
	        System.out.println("detaching "+createdVolumeId+" from "+ instanceId);
		    }catch (AmazonServiceException ase) {
	         System.out.println("Caught Exception: " + ase.getMessage());
	         System.out.println("Reponse Status Code: " + ase.getStatusCode());
	         System.out.println("Error Code: " + ase.getErrorCode());
	         System.out.println("Request ID: " + ase.getRequestId());
	       }
	}
   
	public void createSnapShot(){
		System.out.println("Now create snapshot : ");
		CreateSnapshotRequest createSnapshotRequestInstance=new CreateSnapshotRequest();
		createSnapshotRequestInstance.setVolumeId(createdVolumeId);
		CreateSnapshotResult createSnapshotResult=ec2.createSnapshot(createSnapshotRequestInstance);
		createdSnapshotId=createSnapshotResult.getSnapshot().getSnapshotId();
		System.out.println("New snapshot ID: "+createdSnapshotId);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Now delete volume: "+createdVolumeId);
		DeleteVolumeRequest deleteVolumeRequest=new DeleteVolumeRequest(createdVolumeId);
        ec2.deleteVolume(deleteVolumeRequest);
        System.out.println("Now "+createdVolumeId+" Deleted");
	}
	
	public void createAMI(String instanceId){	
			
		System.out.println("Now create AMI : ");
     	CreateImageRequest createImageRequest=new CreateImageRequest();
     	createImageRequest.setInstanceId(instanceId);
     	createImageRequest.setName("WebServerAMI");
     	CreateImageResult imageResult=ec2.createImage(createImageRequest);
     	instanceImageId=imageResult.getImageId();
     	
     	try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
     	
     	while(true){
			DescribeImagesRequest dir = new DescribeImagesRequest();
			dir.withImageIds(instanceImageId);
			DescribeImagesResult dires = ec2.describeImages(dir);
			List<Image> ilist = dires.getImages();
			String imagestate = null;
            for (Image i : ilist){
            	imagestate = i.withImageId(instanceImageId).getState();
            }
            if (imagestate.equals("available")){
            	
            	System.out.println("AMI created, AMI id= "+instanceImageId);
            	
            	break;
            }else {
            	System.out.println("AMI is not available yet, current state: "+ imagestate);
            	try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }		
        }
     	
	}
	
    
     public void deleteInstance(String instanceId){
		System.out.println("Deleteing VM "+instanceId);
		TerminateInstancesRequest tir = new TerminateInstancesRequest().withInstanceIds(instanceId);
        ec2.terminateInstances(tir);
        System.out.println("Now "+instanceId+" deleted");        
	}
     
     public void createVolumeFromSnapshot(String createdSnapshotId){
    	 try{
    	      	//create a volume
    	         CreateVolumeRequest cvr = new CreateVolumeRequest();
    		     cvr.setAvailabilityZone("us-east-1a");
    		     cvr.setSnapshotId(createdSnapshotId);
    	         CreateVolumeResult volumeResult = ec2.createVolume(cvr);
    	         createdVolumeId = volumeResult.getVolume().getVolumeId();
    	         System.out.println("create new volume from snapshot "+createdVolumeId);
    	         Thread.sleep(10000);           
    	         System.out.println("delete snapshot: "+createdSnapshotId);
    	         DeleteSnapshotRequest deleteSnapshotRequest=new DeleteSnapshotRequest(createdSnapshotId);
    	         ec2.deleteSnapshot(deleteSnapshotRequest);
    	         System.out.println("Now "+createdSnapshotId+" Deleted");
    		    }catch (AmazonServiceException ase) {
    	         System.out.println("Caught Exception: " + ase.getMessage());
    	         System.out.println("Reponse Status Code: " + ase.getStatusCode());
    	         System.out.println("Error Code: " + ase.getErrorCode());
    	         System.out.println("Request ID: " + ase.getRequestId());
    	       } catch (InterruptedException e) {
					e.printStackTrace();
				}
    }
     
    public void createInstanceFromAMI(String instanceImageId){
    	System.out.println("create VMs from stored AMIs and also restore the data partitionss.");
        Placement pzone = new Placement();
        pzone.setAvailabilityZone("us-east-1a");
        
    	RunInstancesRequest rir = new RunInstancesRequest()
        .withPlacement(pzone)
    	.withImageId(instanceImageId)
    	.withInstanceType("t1.micro")
        .withMinCount(1)
        .withMaxCount(1)
        .withKeyName("andrewlee")
        .withSecurityGroups("launch-wizard-1");
	    RunInstancesResult result = ec2.runInstances(rir);
	    instanceId=result.getReservation().getInstances().get(0).getInstanceId();
	    boolean breakWhile=true;
	    while (breakWhile){
           	DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
           	List<Reservation> reservationResult = describeInstancesResult.getReservations();
           	for (Reservation reservation : reservationResult){
           		for(Instance instance : reservation.getInstances()){
           			if(instance.getInstanceId().equals(instanceId)){
           				if(instance.getState().getName().equals("running")&&instance.getPublicDnsName()!= null && !instance.getPublicDnsName().isEmpty()){
           	           		System.out.println("New instance public DNS is "+instance.getPublicDnsName());
           	           		breakWhile=false;
           	           		break;
                            
           	           	}else {
           	           		System.out.println("wait until instance in running state, current state: "+instance.getState().getName());
           	           		try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
           	           	}
           			}
           		}          		 
           	}             	
        }
	    System.out.println("Deleteing image "+instanceImageId);
	    DeregisterImageRequest dir=new DeregisterImageRequest(instanceImageId);
	    ec2.deregisterImage(dir);
	    System.out.println("Now "+instanceImageId+" Deleted");
    }
}
