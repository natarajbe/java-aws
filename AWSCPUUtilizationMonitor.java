
import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

@Component
public class AWSCPUUtilizationMonitor {

	/*
	 * The method will run on every 1 min. cron format: sec min hour day mon
	 * week
	 */
	@Scheduled(cron = "0 0/1 * * * *")
	public void runTask() {
		System.out.println("***** Runing at **** " + new Date());

		AWSCredentialsProvider awsp = new AWSCredentialsProvider() {

			@Override
			public void refresh() {
				// TODO Auto-generated method stub

			}

			@Override
			public AWSCredentials getCredentials() {
				AWSCredentials awsCredentials = null;
				try {
					awsCredentials = new AWSCredentials() {

						public String getAWSSecretKey() {
							return "awssecretkey";
						}

						public String getAWSAccessKeyId() {
							return "awsaccesskey";
						}
					};
				} catch (Exception e) {
					throw new AmazonClientException(
							"can not load your aws credentials, please check your credentials !!", e);
				}
				return awsCredentials;
			}
		};
		try {

			AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().withCredentials(awsp)
					.withRegion("eu-central-1").build();

			Dimension dimension = new Dimension().withName("InstanceId").withValue("i-xxxxxxxxxxxxxxxxxx");
			long offsetInMilliseconds = 1000 * 60 * 2;

			GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
					.withStartTime(new Date(new Date().getTime() - offsetInMilliseconds)).withNamespace("AWS/EC2")
					.withPeriod(60).withMetricName("CPUUtilization").withStatistics("Maximum").withEndTime(new Date())
					.withDimensions(dimension);

			GetMetricStatisticsResult getMetricStatisticsResult = cw.getMetricStatistics(request);

			System.out.println("request StartTime : " + request.getStartTime());
			System.out.println("request EndTime   : " + request.getEndTime());

			/*
			 * System.out.println("label : " +
			 * getMetricStatisticsResult.getLabel());
			 */
			System.out.println("DataPoint Size : " + getMetricStatisticsResult.getDatapoints().size());
			double MaxcpuUtilization = 0;
			List<Datapoint> dataPoint = getMetricStatisticsResult.getDatapoints();
			for (Object aDataPoint : dataPoint) {
				Datapoint dp = (Datapoint) aDataPoint;
				MaxcpuUtilization = dp.getMaximum();
				System.out.println(dp.getTimestamp() + " is : " + MaxcpuUtilization);
			}

			/*
			 * TreeMap metricValues = new TreeMap<Long, Double>();
			 * System.out.println("Data points : "
			 * +getMetricStatisticsResult.getDatapoints().size()) ; for
			 * (Datapoint dp : getMetricStatisticsResult.getDatapoints()) {
			 * metricValues.put(dp.getTimestamp(), dp.getAverage()); }
			 * 
			 * Set set = metricValues.entrySet(); Iterator i = set.iterator();
			 * while (i.hasNext()) { Map.Entry me = (Map.Entry) i.next();
			 * System.out.print(me.getKey() + ": ");
			 * System.out.println(me.getValue()); }
			 */

		} catch (AmazonServiceException ase) {

			ase.printStackTrace();
		}

	}

}
