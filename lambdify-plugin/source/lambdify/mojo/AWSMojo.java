package lambdify.mojo;

import com.amazonaws.auth.*;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 *
 */
public abstract class AWSMojo extends AbstractMojo {

	final AWSCredentialsProviderChain credentials = DefaultAWSCredentialsProviderChain.getInstance();

	@Override
	public void execute() throws MojoFailureException, MojoExecutionException {
		try {
			if ( !getProject().getPackaging().equals( "jar" ) || !getEnabled() ) return;
			run();
		} catch ( MojoExecutionException | MojoFailureException e ) {
			throw e;
		} catch ( RuntimeException e ) {
			throw new MojoExecutionException( e.getMessage(), e );
		} catch ( Exception e ) {
			throw new MojoFailureException( "Unhandled failure: " + e.getMessage(), e );
		}
	}

	protected abstract Boolean getEnabled();

	protected abstract MavenProject getProject();

	protected abstract void run() throws Exception;
}
