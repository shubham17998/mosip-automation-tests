package io.mosip.ivv.e2e.methods;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.testng.Reporter;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.PacketUtility;
import io.mosip.ivv.orchestrator.TestResources;
import io.mosip.testscripts.GetWithParamForDownloadCard;

public class DownloadCard extends BaseTestCaseUtil implements StepInterface {
	private static final String downLoadCard_YML = "preReg/downloadCard/downloadCard.yml";
	private static final String PDFFILEPATH = "preReg/downloadCard";
	Logger logger = Logger.getLogger(DownloadCard.class);
	String fileNameValue=null;

    @SuppressWarnings("static-access")
	@Override
    public void run() throws RigInternalError {
    	if(!step.getParameters().isEmpty() && step.getParameters().size()==1) { //"$$var=e2e_downloadCard($$requestId)"
    		String _requestId=step.getParameters().get(0);
    		if(_requestId.startsWith("$$")) {
				_requestId = step.getScenario().getVariables().get(_requestId);
//				if (step.getScenario().getUinReqIds() == null)
//					step.getScenario().getUinReqIds() = new HashMap<>();
				step.getScenario().getUinReqIds().clear();
				step.getScenario().getUinReqIds().put("requestId", _requestId);
    		}
    	}
    	String fileName = downLoadCard_YML;
    	GetWithParamForDownloadCard getWithPathParam= new GetWithParamForDownloadCard();
    	Object[] casesList = getWithPathParam.getYmlTestData(fileName);
		Object[] testCaseList = filterTestCases(casesList);
		logger.info("No. of TestCases in Yml file : " + testCaseList.length);
		
				for (Object object : testCaseList) {
					for(String requestid: this.step.getScenario().getUinReqIds().values()) {
						try {
						TestCaseDTO test = (TestCaseDTO) object;
						test.setInput(test.getInput().replace("$requestId$", requestid));
						test.setOutput(test.getOutput().replace("$requestId$", requestid));
						Reporter.log("<b><u>"+test.getTestCaseName()+ "</u></b>");
						long startTime = System.currentTimeMillis();
						logger.info(this.getClass().getSimpleName()+" starts at..."+startTime +" MilliSec");
						getWithPathParam.test(test);
						long stopTime = System.currentTimeMillis();
						long elapsedTime = stopTime - startTime;
						logger.info("Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec");
						Reporter.log("<b><u>"+"Time taken to execute "+ this.getClass().getSimpleName()+": " +elapsedTime +" MilliSec"+ "</u></b>");
						// checking pdf file size
						//assertTrue(getWithPathParam.pdf.length>0);
						if(getWithPathParam.pdf.length>0) {
							download(getWithPathParam.pdf,requestid);
						}
						else {this.hasError=true;
							throw new RigInternalError("downloaded pdf size is less than 0");
						}

					} catch (AuthenticationTestException | AdminTestException e) {
						logger.error("Failed at downloading card: "+e.getMessage());
						//assertFalse(true, "Failed at downloading card");
						this.hasError=true;throw new RigInternalError("Failed at downloading card");
					} 
						catch (Exception e) {
							logger.error("Failed at downloading card: "+e.getMessage());
							//assertFalse(true, "Failed at downloading card");
							this.hasError=true;throw new RigInternalError("Failed at downloading card");
						} 
			}
		}
		
    }
    
    
  	private  void download(byte[] pdfFile,String requestid) {
  	    FileOutputStream fos = null;
  	    try {
  	            
  	    	fos = new FileOutputStream(TestResources.getResourcePath()+PDFFILEPATH+"/"+requestid+".pdf");
  	            fos.write(pdfFile);
  	        } catch (IOException e ) {
  	        	logger.error("Failed to download the pdf Exception: "+e.getMessage());
  	        } 
  	  finally {
			PacketUtility.closeOutputStream(fos);
		}
  	    }
  	

}
