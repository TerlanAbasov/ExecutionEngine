package com.quant.finance.execution.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.enums.OrderAction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AlertMapper {

  org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AlertMapper.class);
  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Mapping(source = "ticker", target = "symbol")
  @Mapping(source = "tvAlertDto", target = "action", qualifiedByName = "determineAction")
  @Mapping(source = "strategy", target = "strategy")
  @Mapping(source = "exchange", target = "exchange")
  @Mapping(source = "interval", target = "interval", qualifiedByName = "parseInterval")
  @Mapping(source = "volume", target = "volume", qualifiedByName = "parseVolume")
  @Mapping(source = "quote", target = "quote")
  @Mapping(source = "open", target = "open", qualifiedByName = "parseBigDecimal")
  @Mapping(source = "close", target = "close", qualifiedByName = "parseBigDecimal")
  @Mapping(source = "low", target = "low", qualifiedByName = "parseBigDecimal")
  @Mapping(source = "high", target = "high", qualifiedByName = "parseBigDecimal")
  @Mapping(source = "time", target = "barTime", qualifiedByName = "parseDateTime")
  @Mapping(source = "timenow", target = "generatedTime", qualifiedByName = "parseDateTime")
  @Mapping(source = "tvAlertDto", target = "json", qualifiedByName = "buildJSON")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  AlertEntity toEntity(TVAlertDto tvAlertDto);

  @Named("determineAction")
  default OrderAction determineAction(TVAlertDto tvAlertDto) {
    if ("1".equals(tvAlertDto.getBuy()) || "true".equalsIgnoreCase(tvAlertDto.getBuy())) {
      return OrderAction.BUY;
    } else if ("1".equals(tvAlertDto.getSell()) || "true".equalsIgnoreCase(tvAlertDto.getSell())) {
      return OrderAction.SELL;
    }

    throw new IllegalArgumentException("Unknown action type");
  }

  @Named("parseInterval")
  default Integer parseInterval(String interval) {
    try {
      return Integer.parseInt(interval);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Named("parseVolume")
  default Integer parseVolume(String volume) {
    try {
      return Integer.parseInt(volume);
    } catch (NumberFormatException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Named("parseBigDecimal")
  default BigDecimal parseBigDecimal(String value) {
    try {
      return new BigDecimal(value);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Named("parseDateTime")
  default LocalDateTime parseDateTime(String dateTime) {
    try {
      // Parse ISO-8601 format: "2026-01-30T10:09:07Z"
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
      return zonedDateTime.toLocalDateTime();
    } catch (DateTimeParseException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Named("buildJSON")
  default String buildJSON(TVAlertDto tvAlertDto) {

    try {
      return OBJECT_MAPPER.writeValueAsString(tvAlertDto);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }
}