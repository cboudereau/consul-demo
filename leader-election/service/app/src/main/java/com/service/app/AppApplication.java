package com.service.app;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
import com.service.app.consul.session.HandlerStatus;
import com.service.app.consul.session.ManualLeaderElection;
import com.service.app.consul.session.UnhandledAwaiter;

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

		private static final void process(String connectionString, String input) throws InterruptedException {
			logger.info("processing batch using {} connection string and input {}", connectionString, input);
			TimeUnit.SECONDS.sleep(5);
			logger.info("batch processed");
		}
	
		public static final void run() {
			String service = System.getenv("SERVICE_NAME");
			logger.info("starting {}", service);
			ConsulClient cc = new ConsulClient();

			BiConsumer<String, String> safeProcess = (String connectionString, String input) -> {
				try {
					process(connectionString, input);
				} catch (InterruptedException e) {
					logger.error("safeProcess interrupted", e);
				}
			};

			Handler<String> manualLeader = new ManualLeaderElection<>((input) -> safeProcess.accept("conf", input));
			Supplier<Optional<String>> consulConf = new ConsulKVSybaseConfiguration(cc);
			
			Handler<String> consulLeader = new ConsulLeaderElection<>(cc, service, 10, (input) -> {
				Optional<String> connectionString = consulConf.get();
				if (connectionString.isEmpty()) {
					logger.warn("connection string is empty");
					return Optional.of(HandlerStatus.UNHANDLED);
				}
				safeProcess.accept(connectionString.get(), input);
				return Optional.of(HandlerStatus.HANDLED);
			});

			Consumer<String> runner = new CompositeLeaderElection<>(Arrays.asList(new UnhandledAwaiter<>(5, manualLeader), new UnhandledAwaiter<>(5, consulLeader)));

			while (true) {
				runner.accept("batch input");;
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
