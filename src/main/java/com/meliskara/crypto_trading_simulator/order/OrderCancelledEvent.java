package com.meliskara.crypto_trading_simulator.order;

import com.meliskara.crypto_trading_simulator.market.Symbol;

import java.util.UUID;

record OrderCancelledEvent(UUID orderId, Symbol symbol) {
}
