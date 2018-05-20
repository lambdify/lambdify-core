package lambdify.mojo;

import java.io.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

/**
 *
 */
@Mojo( name = "s3-deploy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class S3UploaderMojo extends AWSMojo {

	@Parameter( defaultValue = "${project}", required = true)
	@Getter MavenProject project;

	@Parameter( defaultValue = "true", required = true )
	@Getter Boolean enabled;

	@Parameter( defaultValue = "us-east-1", required = true )
	@Getter String regionName;

	@Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}.zip", required = true )
	String zipFileName;

	@Parameter( defaultValue = "${project.groupId}-${project.artifactId}-${project.version}.zip", required = true )
	String s3Key;

	@Parameter( required = true, defaultValue = "UNDEFINED-S3-BUCKET" )
	String s3Bucket;

	@Override
	protected void run() throws Exception {
		val packageFile = new File( zipFileName );
		if ( !packageFile.exists() )
			throw new FileNotFoundException( zipFileName );
		uploadPackage( packageFile );
	}

	private void uploadPackage( File packageFile ) {
		getLog().info( "Deploying package on AWS S3: " + s3Bucket + "/" + s3Key );
		val s3 = AmazonS3Client.builder().withCredentials( credentials )
				.withRegion( Regions.fromName(regionName) ).build();
		s3.putObject( s3Bucket, s3Key, packageFile );
	}
}
