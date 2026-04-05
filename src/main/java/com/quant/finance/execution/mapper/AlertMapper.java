package com.quant.finance.execution.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Types.Action;
import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.entity.AlertEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
  @Mapping(source = "peerTicker", target = "peerSymbol")
  @Mapping(source = "assetClass", target = "assetClass")
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
  @Mapping(target = "isPeer", ignore = true)
  @Mapping(target = "state", ignore = true)
  @Mapping(target = "description", ignore = true)
  AlertEntity toEntity(TVAlertDto tvAlertDto);

  @Named("determineAction")
  default Action determineAction(TVAlertDto tvAlertDto) {
    if ("1".equals(tvAlertDto.getBuy()) || "true".equalsIgnoreCase(tvAlertDto.getBuy())) {
      return Action.BUY;
    } else if ("1".equals(tvAlertDto.getSell()) || "true".equalsIgnoreCase(tvAlertDto.getSell())) {
      return Action.SELL;
    }

    throw new IllegalArgumentException("Unknown action type");
  }

/*  @Named("parseInterval")
  default Integer parseInterval(String interval) {
    try {
      return Integer.parseInt(interval);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }*/

  @Named("parseInterval")
  default Integer parseInterval(String interval) {
    if (interval == null || interval.isBlank()) {
      log.error("Interval is null or empty");
      return null;
    }

    try {
      String normalized = interval.trim().toUpperCase();

      // 🔹 Seconds (e.g. 5S)
      if (normalized.endsWith("S")) {
        int value = Integer.parseInt(normalized.substring(0, normalized.length() - 1));
        return validateInterval(value);
      }

      // 🔹 Daily / Weekly / Monthly
      switch (normalized) {
        case "D":
          return 86400;
        case "W":
          return 604800;
        case "M":
          return 2592000; // 30 days approximation
      }

      // 🔹 Minutes (default numeric)
      if (normalized.matches("\\d+")) {
        int minutes = Integer.parseInt(normalized);
        return validateInterval(minutes * 60);
      }

      throw new IllegalArgumentException("Unsupported interval: " + interval);

    } catch (Exception e) {
      log.error("Failed to parse interval: {}", interval, e);
      return null;
    }
  }

  default int validateInterval(int seconds) {
    if (seconds <= 0) {
      throw new IllegalArgumentException("Interval must be > 0");
    }

    // Optional: protect your engine from garbage / abuse
    if (seconds > 2592000) { // 30 days
      throw new IllegalArgumentException("Interval too large: " + seconds);
    }

    return seconds;
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
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
          .withZoneSameInstant(ZoneId.of("Asia/Baku"));
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