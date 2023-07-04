package com.bugbuster.accounts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bugbuster.accounts.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
