package com.bugbuster.accounts.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.bugbuster.accounts.config.AccountsServiceConfig;
import com.bugbuster.accounts.model.Accounts;
import com.bugbuster.accounts.model.Cards;
import com.bugbuster.accounts.model.Customer;
import com.bugbuster.accounts.model.CustomerDetails;
import com.bugbuster.accounts.model.Loans;
import com.bugbuster.accounts.model.Properties;
import com.bugbuster.accounts.repository.AccountsRepository;
import com.bugbuster.accounts.repository.CustomerRepository;
import com.bugbuster.accounts.service.client.CardsFeignClient;
import com.bugbuster.accounts.service.client.LoansFeignClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;

@RestController
public class AccountsController {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountsController.class);

	@Autowired
	private AccountsRepository accountsRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private AccountsServiceConfig accountsConfig;
	
	@Autowired
	private LoansFeignClient loansFeignClient;
	
	@Autowired
	private CardsFeignClient cardsFeignClient;
	
	@PostMapping("/myAccount")
	@Timed(value = "getAccountDetails.time",description = "Time taken to return account details")
	public Accounts getAccountDetails(@RequestBody Customer customer) {
		logger.info("getAccountDetails() method started");
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		if(accounts != null) {
			logger.info("getAccountDetails() method ended");
			return accounts;
		}
		else {
			logger.info("getAccountDetails() method returns null value");
			return null;
		}
	}
	
	@PostMapping("/createCustomer")
	public ResponseEntity<String> createUser(@RequestBody Customer customer){
		logger.info("createUser() method started");
		customer.setCreateDt(LocalDate.now());
		Customer savedCustomer = customerRepository.save(customer);
		if(savedCustomer!=null) {
			logger.info("createUser() method ended");
			return new ResponseEntity<>("Customer Created Successfully",HttpStatus.OK);
		}
		else {
			logger.info("createUser() method throwing Something went wrong! error");
			return new ResponseEntity<>("Something went wrong!. Please try again later",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/createAccount")
	public ResponseEntity<String> createAccount(@RequestBody Accounts accounts){
		logger.info("createAccount() method started");
		accounts.setCreateDt(LocalDate.now());
		Accounts savedAccount = accountsRepository.save(accounts);
		if(savedAccount!=null) {
			logger.info("createAccount() method ended");
			return new ResponseEntity<>("Account Created Successfully for user id "+savedAccount.getCustomerId(),HttpStatus.OK);
		}
		else {
			logger.info("createAccount() method throwing Something went wrong! error");
			return new ResponseEntity<>("Something went wrong!. Please try again later",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/account/properties")
	public String getPropertyDetails() throws JsonProcessingException{
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountsConfig.getMsg(),accountsConfig.getBuildVersion(),
				accountsConfig.getMailDetails(),accountsConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
	
	@PostMapping("/myCustomerDetails")
//	@CircuitBreaker(name="detailsforCustomerSupportApp",fallbackMethod="myCustomerDetailsFallBack")
	@Retry(name="retryforCustomerDetails",fallbackMethod="myCustomerDetailsFallBack")
	public CustomerDetails myCustomerDetails(@RequestBody Customer customer) {
		logger.info("myCustomerDetails() method started");
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		
		List<Loans> loans = loansFeignClient.getLoansDetails(customer);
		List<Cards> cards = cardsFeignClient.getCardDetails(customer);
		
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setCards(cards);
		customerDetails.setLoans(loans);
		logger.info("myCustomerDetails() method ended");
		return customerDetails;
	}
	
	private CustomerDetails myCustomerDetailsFallBack(Customer customer,Throwable t) {
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		
		List<Loans> loans = loansFeignClient.getLoansDetails(customer);
		
		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		return customerDetails;

	}
	
	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello",fallbackMethod = "sayHellofallback")
	public String sayHello() {
		return "Hello, Welcome to EazyBank";
	}
	
	private String sayHellofallback(Throwable t) {
		return "Hi,Welcome to EazyBank";
	}
	
}
