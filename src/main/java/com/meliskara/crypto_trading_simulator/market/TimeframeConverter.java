package com.meliskara.crypto_trading_simulator.market;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
class TimeframeConverter implements AttributeConverter<Timeframe, String> {

	@Override
	public String convertToDatabaseColumn(Timeframe timeframe) {
		return timeframe == null ? null : timeframe.code();
	}

	@Override
	public Timeframe convertToEntityAttribute(String code) {
		return code == null ? null : Timeframe.fromCode(code);
	}
}
