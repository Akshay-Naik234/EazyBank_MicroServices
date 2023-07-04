package com.bugbuster.cards.controller;

import java.util.Date;
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

import com.bugbuster.cards.config.CardsServiceConfig;
import com.bugbuster.cards.model.Cards;
import com.bugbuster.cards.model.Customer;
import com.bugbuster.cards.model.Properties;
import com.bugbuster.cards.repository.CardsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
public class CardsController {
	
	private static final Logger logger = LoggerFactory.getLogger(CardsController.class);


	@Autowired
	private CardsRepository cardsRepository;
	
	@Autowired
	private CardsServiceConfig cardsConfig;

	@PostMapping("/myCards")
	public List<Cards> getCardDetails(@RequestBody Customer customer) {
		logger.info("getCardDetails() method started");
		List<Cards> cards = cardsRepository.findByCustomerId(customer.getCustomerId());
		if (cards != null) {
			logger.info("getCardDetails() method ended");
			return cards;
		} else {
			logger.info("getCardDetails() method returns null value");
			return null;
		}
	}
	
	@PostMapping("/createCards")
	public ResponseEntity<String> createCards(@RequestBody Cards cards){
		logger.info("createCards() method started");
		Date date = new Date();
		cards.setCreateDt(date);
		Cards savedCards = cardsRepository.save(cards);
		if(savedCards!=null) {
			logger.info("createCards() method ended");
			return new ResponseEntity<>("Cards Created Successfully for user id "+savedCards.getCustomerId(),HttpStatus.OK);
		}
		else {
			logger.info("createCards() method throws Something went wrong! error");
			return new ResponseEntity<>("Something went wrong!. Please try again later",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping("/cards/properties")
	public String getPropertyDetails() throws JsonProcessingException{
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(cardsConfig.getMsg(),cardsConfig.getBuildVersion(),
				cardsConfig.getMailDetails(),cardsConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
	
}
