package com.bugbuster.accounts.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Accounts {
	@Column(name="customer_id")
	private int customerId;
	
	@Column(name="account_number")
	@Id
	private long accountNumber;
	
	@Column(name="account_type")
	private String accountType;

	@Column(name="branch_address")
	private String branchAddress;

	@Column(name="create_dt")
	private LocalDate createDt;
}
