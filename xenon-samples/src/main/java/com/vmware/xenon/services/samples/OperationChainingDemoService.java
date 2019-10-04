package com.vmware.xenon.services.samples;

import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.ServiceUriPaths;

public class OperationChainingDemoService extends StatefulService {
	
	public static final String FACTORY_LINK = ServiceUriPaths.SAMPLES + "/operation-chaining";
	
	public static Service createFactory() {
		return FactoryService.create(OperationChainingDemoService.class);
	}
	
	public static class EchoServiceState extends ServiceDocument {
		public String message;
	}
	
	public OperationChainingDemoService() {
		super(EchoServiceState.class);
	}
	
	@Override
	public void handleCreate(Operation parentPost) {
		
		SamplePreviousEchoService.EchoServiceState message1 = new SamplePreviousEchoService.EchoServiceState();
		message1.message = "message1";
		
		Operation operation1 = Operation.createPost(getHost(), SamplePreviousEchoService.FACTORY_LINK)
				.setBody(message1)
				.setCompletion(((completedOp, failure) -> {
					if(failure != null) {
						System.out.println("operation1 failed");
						logSevere("operation1 failed");
						return;
					}
					String documentLink = completedOp.getBody(EchoServiceState.class).documentSelfLink;
					System.out.println("operation1 completed");
					logInfo("operation1 completed");
				}));
		
		SamplePreviousEchoService.EchoServiceState message2 = new SamplePreviousEchoService.EchoServiceState();
		message2.message = "message2";
		
		Operation operation2 = Operation.createPost(getHost(), SamplePreviousEchoService.FACTORY_LINK)
				.setBody(message2)
				.setCompletion(((completedOp, failure) -> {
					if(failure != null) {
						System.out.println("operation2 failed");
						logSevere("operation2 failed");
						return;
					}
					
					System.out.println("operation2 completed");
					logInfo("operation2 completed");
				}));
		
		Operation operation3 = Operation.createGet(getHost(), SamplePreviousEchoService.FACTORY_LINK)
				.setCompletion(((completedOp, failure) -> {
					if(failure != null) {
						System.out.println("operation3 failed");
						logSevere("operation3 failed");
						return;
					}
					
					String result = completedOp.getBody(SamplePreviousEchoService.EchoServiceState.class).message;
					
					System.out.println("operation3 completed with message " + result);
					logInfo("operation3 completed with message {}", result);
					
				}));
		
		OperationJoin.JoinedCompletionHandler jh = (ops, failures) -> {
			if(failures != null) {
				parentPost.fail(500);
			}
			System.out.println("completed all operations");
			logInfo("completed all operations");
			parentPost.complete();
		};
		
		OperationSequence.create(operation1).next(operation2).next(operation3).setCompletion(jh).sendWith(this);
		
	}
}
