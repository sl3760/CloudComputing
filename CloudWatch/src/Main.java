package assignment1;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.amazonaws.AmazonServiceException;
public class Main {
		
	public static void main(String[] args) throws Exception {
		
		boolean daytime=false;
		boolean newDay=false;
		int  instanceExist=2;
		boolean instanceExist1=true;
		boolean instanceExist2=true;
		
        try{
        	
        	Monitor monitor1=new Monitor("i-60c8731d"); 
        	Monitor monitor2=new Monitor("i-f899599e");
    	    monitor1.createIP();
    	    Thread.sleep(10000);
    	    monitor1.associateIP(monitor1.instanceId,monitor1.instanceIp);
    	    monitor1.createVolume();
    	    Thread.sleep(10000);
    	    monitor1.attachVolume(monitor1.instanceId, monitor1.createdVolumeId);
    	    Thread.sleep(10000);
    	    monitor1.createS3("do-you-know-me");
        	Thread.sleep(10000);
        	
        	monitor2.createIP();
    	    Thread.sleep(10000);
    	    monitor2.associateIP(monitor2.instanceId,monitor2.instanceIp);
    	    monitor2.createVolume();
    	    Thread.sleep(10000);
    	    monitor2.attachVolume(monitor2.instanceId, monitor2.createdVolumeId);
    	    Thread.sleep(10000);
    	    monitor1.createS3("do-you-know-him");
    	    
            while(true){
            	Date date = new Date();
        		Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        		calendar.setTime(date);   // assigns calendar to given date 
        		int hour=calendar.get(Calendar.HOUR_OF_DAY);
        		System.out.println("Now time is :"+hour+" (hr).");
        		
                if(hour>=5&&hour<17){
                	daytime=true;
                }
                else{
                	daytime=false;
                	newDay=true;
                }
                if(daytime&&(instanceExist!=0)){
                	while(true){
                		if(instanceExist1){
                		    CloudWatch cw1 = new CloudWatch(monitor1.instanceId);
						    cw1.watch();
						    if(cw1.instanceAverageCPU<0.5){
						    	monitor1.disassociateIP(monitor1.instanceIp);
						    	monitor1.detachVolume(monitor1.createdVolumeId);
						    	Thread.sleep(60000);
						    	monitor1.createSnapShot();
						    	monitor1.createAMI(monitor1.instanceId);
						    	monitor1.deleteInstance(monitor1.instanceId);
						    	instanceExist1=false;
						    	instanceExist--;
						    	if(instanceExist==0)
                                     break;						    	
						    }
                		}
                		if(instanceExist2){
					     	CloudWatch cw2 = new CloudWatch(monitor2.instanceId);
						    cw2.watch();
						    if(cw2.instanceAverageCPU<0.5){
						    	monitor2.disassociateIP(monitor2.instanceIp);
						    	monitor2.detachVolume(monitor2.createdVolumeId);
						    	Thread.sleep(60000);
						    	monitor2.createSnapShot();
						    	monitor2.createAMI(monitor2.instanceId);
						    	monitor2.deleteInstance(monitor2.instanceId);
						    	instanceExist2=false;
						    	instanceExist--;
						    	if(instanceExist==0)
                                     break;						    	
						    }
                		}
						newDay=false;							
                	}
                }
                
                else if(daytime&&(instanceExist==0)){
                	if(newDay){
                		monitor1.createInstanceFromAMI(monitor1.instanceImageId);
                		monitor1.associateIP(monitor1.instanceId,monitor1.instanceIp);   
                		Thread.sleep(10000);
                		monitor1.createVolumeFromSnapshot(monitor1.createdSnapshotId);
                		Thread.sleep(10000);
                		monitor1.attachVolume(monitor1.instanceId,monitor1.createdVolumeId); 
                		Thread.sleep(10000);
                		monitor2.createInstanceFromAMI(monitor2.instanceImageId);
                		monitor2.associateIP(monitor2.instanceId,monitor2.instanceIp);   
                		Thread.sleep(10000);
                		monitor2.createVolumeFromSnapshot(monitor2.createdSnapshotId);
                		Thread.sleep(10000);
                		monitor2.attachVolume(monitor2.instanceId,monitor2.createdVolumeId);
                		instanceExist1=true;
                		instanceExist2=true;
                		instanceExist=2;
                	}
                	else{
                		Thread.sleep(80000);
                	}
                }
                         
                else{
                	if(instanceExist!=0){
                		monitor1.disassociateIP(monitor1.instanceIp);
                    	monitor1.detachVolume(monitor1.createdVolumeId);
                    	Thread.sleep(10000);
                    	monitor1.createSnapShot();
                    	monitor1.createAMI(monitor1.instanceId);
                    	monitor1.deleteInstance(monitor1.instanceId);
                    	
                    	monitor2.disassociateIP(monitor2.instanceIp);
                    	monitor2.detachVolume(monitor2.createdVolumeId);
                    	Thread.sleep(10000);
                    	monitor2.createSnapShot();
                    	monitor2.createAMI(monitor2.instanceId);
                    	monitor2.deleteInstance(monitor2.instanceId);
                    	instanceExist=0;
                    	instanceExist1=false;
                    	instanceExist2=true;
                	}
                	else
                		Thread.sleep(80000);
                	
                }           	
            	
            }

	    }catch (AmazonServiceException ase) {
        System.out.println("Caught Exception: " + ase.getMessage());
        System.out.println("Reponse Status Code: " + ase.getStatusCode());
        System.out.println("Error Code: " + ase.getErrorCode());
        System.out.println("Request ID: " + ase.getRequestId());
        }  
        
	}
}
