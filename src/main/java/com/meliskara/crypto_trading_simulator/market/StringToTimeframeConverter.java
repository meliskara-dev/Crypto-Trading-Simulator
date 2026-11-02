package com.meliskara.crypto_trading_simulator.market;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
class StringToTimeframeConverter implements Converter<String, Timeframe> {

	@Override
	public Timeframe convert(String source) {
		return Timeframe.fromCode(source);
	}
}
