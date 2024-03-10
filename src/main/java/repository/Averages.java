package repository;

import java.math.BigDecimal;

/**
 * Класс содержит информацию о средней величине.
 * @param average среднее значение
 * @param period количество точек, по которым расчитано среднее
 */
public record Averages(BigDecimal average, int period) {}
