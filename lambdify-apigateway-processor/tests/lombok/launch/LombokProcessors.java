package lombok.launch;

import javax.annotation.processing.Processor;
import lombok.launch.AnnotationProcessorHider.*;

/**
 * A little hack to expose Lombok processors. I hope you can forgive me about this
 * but, you know, I have to test my Processors simulating a real world environment
 * and Lombok is extensively used on Java world. I promise I'll never use this code
 * in production. ;)
 */
public interface LombokProcessors {

	static Processor[] getProcessors(){
		return new Processor[]{
			new AnnotationProcessor(),
			new ClaimingProcessor()
		};
	}
}
