package webapi.dto;

import java.math.BigDecimal;

/**
 * Класс содержит информацию о средней величине.
 * @param average среднее значение
 * @param period количество точек, по которым расчитано среднее (1 точка = 1 минута)
 */
public record Averages(BigDecimal average, int period) {}
