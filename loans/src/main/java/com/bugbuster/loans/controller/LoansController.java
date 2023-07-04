package com.bugbuster.loans.controller;

import com.bugbuster.loans.model.*;
import java.util.List;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.bugbuster.loans.config.LoanServiceConfig;
import com.bugbuster.loans.model.Customer;
import com.bugbuster.loans.model.Loans;
import com.bugbuster.loans.repository.LoansRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
public class LoansController {

	private static final Logger logger = LoggerFactory.getLogger(LoansController.class);

	@Autowired
	private LoansRepository loansRepository;

	@Autowired
	private LoanServiceConfig loansConfig;

	@PostMapping("/myLoans")
	public List<Loans> getLoansDetails(@RequestBody Customer customer) {
		logger.info("getLoansDetails() method started");
		System.out.println("Invoaking Loans Microservice");
		List<Loans> loans = loansRepository.findByCustomerIdOrderByStartDtDesc(customer.getCustomerId());
		if (loans != null) {
			logger.info("getLoansDetails() method ended");
			return loans;
		} else {
			logger.info("getLoansDetails() method returns null");
			return null;
		}
	}

	@PostMapping("/createLoans")
	public ResponseEntity<String> createLoans(@RequestBody Loans loans) {
		logger.info("createLoans() method started");
		Loans savedLoans = loansRepository.save(loans);
		if (savedLoans != null) {
			logger.info("createLoans() method ended");
			return new ResponseEntity<>("Loans Created Successfully", HttpStatus.OK);
		} else {
			logger.info("createLoans() method throws Something went wrong! error");
			return new ResponseEntity<>("Something went wrong!. Please try again later",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/loans/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(loansConfig.getMsg(), loansConfig.getBuildVersion(),
				loansConfig.getMailDetails(), loansConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
}
