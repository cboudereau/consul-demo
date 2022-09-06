package com.service.app;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.OperationException;
import com.service.app.consul.session.CompositeLeaderElection;
import com.service.app.consul.session.ConsulLeaderElection;
import com.service.app.consul.session.Handler;
import com.service.app.consul.session.ManualLeaderElection;

@SpringBootApplication
public class AppApplication {

	private static final class Batch {
	
		private static final class ConsulKVSybaseConfiguration implements Supplier<Optional<String>> {
	
			private static final Logger logger = LoggerFactory.getLogger(ConsulKVSybaseConfiguration.class);
	
			private final ConsulClient cc;
	
			public ConsulKVSybaseConfiguration(ConsulClient cc) {
				this.cc = cc;
			}
	
			@Override
			public Optional<String> get() {
				try {
					String sybase_dc = cc.getKVValue("sybase/dc").getValue().getDecodedValue();
					Optional<String> connectionString = Optional.ofNullable(cc.getKVValue(String.format("sybase/%s/connectionString", sybase_dc)).getValue()).map (x -> x.getDecodedValue());
					if (connectionString.isEmpty()) {
						logger.warn("Sybase/dc {} does not match existing conf", sybase_dc);
					}
					return connectionString;
				} catch (OperationException e) {
					logger.error("consul error while getting sybase configuration", e);
					return Optional.empty();
				}
			}
		}
	
		private static final Logger logger = LoggerFactory.getLogger(Batch.class);	

		private static final String process(String connectionString) {
			logger.info("processing batch using {} connection string", connectionString);
			logger.info("batch processed");
			return "batch succeed";
		}
	
		public static final void run() throws InterruptedException {
			String service = System.getenv("SERVICE_NAME");
			logger.info("starting {}", service);
			ConsulClient cc = new ConsulClient();

			Handler<String> manualLeader = new ManualLeaderElection<>(() -> process("conf"));
			Supplier<Optional<String>> consulConf = new ConsulKVSybaseConfiguration(cc);
			
			Handler<String> consulLeader = new ConsulLeaderElection<>(cc, service, 10, () -> {
				Optional<String> connectionString = consulConf.get();
				if (connectionString.isEmpty()) {
					logger.warn("connection string is empty");
					return null;
				}
				return process(connectionString.get());
			});
			Supplier<Optional<String>> supplier = new CompositeLeaderElection<>(Arrays.asList(manualLeader, consulLeader));

			while (true) {
				logger.info("batch: {}", supplier.get() );
				TimeUnit.SECONDS.sleep(5);
			}
		}
	}

	/*
	 * This implementation handle this scenario
	 * Local consul agent failure
	 * Consul cluster failures
	 * Consul communication failures
	 * App restart/failures
	 */
	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		SpringApplication.run(AppApplication.class, args);
		Batch.run();
	}
}
