package com.rbc.rbcone.position.dashboard.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbc.rbcone.position.dashboard.model.Account;
import com.rbc.rbcone.position.dashboard.model.Holding;
import com.rbc.rbcone.position.dashboard.repo.AccountRepository;
import com.rbc.rbcone.position.dashboard.repo.HoldingRepository;
import com.rbc.rbcone.position.dashboard.rest.AccountDTO;
import com.rbc.rbcone.position.dashboard.rest.AccountHoldingDTO;
import com.rbc.rbcone.position.dashboard.rest.HoldingDTO;

@Service
public class AccountServiceImpl implements AccountService {

	private static final String ZERO = "0.0";
	private static final String ALL_ACCOUNTS = "ALL";

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	HoldingRepository holdingRepository;
	
	@Override
	public List<AccountDTO> getUserAccounts() {
		List<Account> accounts = accountRepository.findAll();
		
		List<AccountDTO> accountDTOs = new ArrayList<>();

		for (Account account : accounts) {
			accountDTOs.add(new AccountDTO(account.getAccountNumber(), account.getAccountName()));
		}
		
		return accountDTOs;
	}

	@Override
	public AccountHoldingDTO getHoldings(String accountNumber) {
		List<Holding> holdings;
		
		if (ALL_ACCOUNTS.equals(accountNumber)) {
			holdings = holdingRepository.findAll();
		} else {
			holdings = holdingRepository.findByAccountNumber(accountNumber);
		}

        return toAccountHoldingDTO(holdings);
	}

	@Override
	public AccountHoldingDTO getHoldings(String accountNumber, String country) {
        return toAccountHoldingDTO(holdingRepository.findByAccountNumberAndCountryOfIssuer(accountNumber, country));
	}

    private AccountHoldingDTO toAccountHoldingDTO(List<Holding> holdings) {
        AccountHoldingDTO dto = new AccountHoldingDTO();

        dto.setTotalMarketValue(calculateTotalMarketValue(holdings));
        dto.setCountryTotalMarketValue(calculateTotalMarketValueByCountry(holdings));
        dto.setMajorSecurityTypeTotalMarketValue(calculateTotalMarketValueByMajorSecurityType(holdings));
        dto.setMinorSecurityTypeTotalMarketValue(calculateTotalMarketValueByMajorAndMinorSecurityType(holdings));
        dto.setHoldings(toDTOs(holdings));

        return dto;
    }

	private List<HoldingDTO> toDTOs(List<Holding> holdings) {
		List<HoldingDTO> holdingDTOs = new ArrayList<>();
		for (Holding holding : holdings) {
			holdingDTOs.add(toDTO(holding));
		}
		
		return holdingDTOs;
	}

	private HoldingDTO toDTO(Holding holding) {
		HoldingDTO dto = new HoldingDTO();

		dto.setAccountNumber(holding.getAccountNumber());
		dto.setBookBaseValue(holding.getBookBaseValue());
		dto.setCountryOfIssuer(holding.getCountryOfIssuer());
		dto.setCusip(holding.getCusip());
		dto.setIndustry(holding.getIndustry());
		dto.setIsin(holding.getIsin());
		dto.setMajorSecurityType(holding.getMajorSecurityType());
		dto.setMarketBaseValue(holding.getMarketBaseValue());
		dto.setMinorSecurityType(holding.getMinorSecurityType());
		dto.setPortfolioCurrency(holding.getPortfolioCurrency());
		dto.setPrice(holding.getPrice());
		dto.setSecurityId(holding.getSecurityId());
		dto.setSecurityDescription(holding.getSecurityDescription());
		dto.setSedol(holding.getSedol());
		dto.setUnits(holding.getUnits());

		return dto;
	}

	private Map<String, BigDecimal> calculateTotalMarketValueByCountry(List<Holding> holdings) {
		Map<String, BigDecimal> map = new HashMap<>();
		
		for (Holding holding : holdings) {
			String countryOfIssuer = holding.getCountryOfIssuer();
			BigDecimal marketBaseValue = getHoldingValue(holding.getMarketBaseValue());
			if (countryOfIssuer != null) {
				if (map.containsKey(countryOfIssuer)) {
					map.put(countryOfIssuer, map.get(countryOfIssuer).add(marketBaseValue));
				} else {
					map.put(countryOfIssuer, marketBaseValue);
				}
			}
		}
		
		return map;
	}

	private BigDecimal getHoldingValue(BigDecimal marketBaseValue) {
		return marketBaseValue != null ? marketBaseValue : new BigDecimal(ZERO);
	}

	private BigDecimal calculateTotalMarketValue(List<Holding> holdings) {
		BigDecimal totalMarketValue = new BigDecimal(ZERO);

		for (Holding holding : holdings) {
			BigDecimal marketBaseValue = getHoldingValue(holding.getMarketBaseValue());
			
			totalMarketValue = totalMarketValue.add(marketBaseValue);
		}
		
		return totalMarketValue;
	}

	Map<String, Map<String, BigDecimal>> calculateTotalMarketValueByMajorAndMinorSecurityType(List<Holding> holdings) {
		Map<String, Map<String, BigDecimal>> holdingByMajorAndMinorSecType = new HashMap<>();
		Map<String, List<Holding>> map = groupHoldingsByMajorSecurityType(holdings);
		
		Set<String> majorSecurityTypes = map.keySet();
		for (String majorSecurityType : majorSecurityTypes) {
			List<Holding> holdingsByMajorSecType = map.get(majorSecurityType);
			
			holdingByMajorAndMinorSecType.put(majorSecurityType, calculateTotalMarketValueByMinorSecurityType(holdingsByMajorSecType));
		}

		return holdingByMajorAndMinorSecType;
		
	}

	private Map<String, List<Holding>> groupHoldingsByMajorSecurityType(List<Holding> holdings) {
		Map<String, List<Holding>> map = new HashMap<>();
		for (Holding holding : holdings) {
			String majorSecurityType = holding.getMajorSecurityType();
			if (majorSecurityType != null) {
				if (!map.containsKey(majorSecurityType)) {
					List<Holding> holdingsForMajorSecurity = new ArrayList<>();
					map.put(majorSecurityType, holdingsForMajorSecurity);
				}
				map.get(majorSecurityType).add(holding);
			}
		}
		return map;
	}

	Map<String, BigDecimal> calculateTotalMarketValueByMajorSecurityType(List<Holding> holdings) {

		Map<String, BigDecimal> map = new HashMap<>();
		
		for (Holding holding : holdings) {
			String majorSecurityType = holding.getMajorSecurityType();
			if (majorSecurityType != null) {
				
				BigDecimal marketBaseValue = getHoldingValue(holding.getMarketBaseValue());
				if (map.containsKey(majorSecurityType)) {
					map.put(majorSecurityType, map.get(majorSecurityType).add(marketBaseValue));
				} else {
					map.put(majorSecurityType, marketBaseValue);
				}
			}
		}

		
		return map;
	}

	private Map<String, BigDecimal> calculateTotalMarketValueByMinorSecurityType(List<Holding> holdings) {
		
		Map<String, BigDecimal> map = new HashMap<>();
		
		for (Holding holding : holdings) {
			String minorSecurityType = holding.getMinorSecurityType();
			if (minorSecurityType != null) {
				
				BigDecimal marketBaseValue = getHoldingValue(holding.getMarketBaseValue());
				if (map.containsKey(minorSecurityType)) {
					map.put(minorSecurityType, map.get(minorSecurityType).add(marketBaseValue));
				} else {
					map.put(minorSecurityType, marketBaseValue);
				}
			}
		}
		
		
		return map;
	}
}