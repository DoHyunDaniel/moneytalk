package com.example.moneytalk.type;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수입/지출 타입", enumAsRef = true)
public enum LedgerType {
	INCOME, EXPENSE
}
