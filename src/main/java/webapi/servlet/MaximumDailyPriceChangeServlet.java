package webapi.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import webapi.dto.MaximumDailyChangeCryptocurrencyDto;
import webapi.service.CryptocurrencyWebService;

import java.io.IOException;

import static utils.ApplicationConstantHolder.defaultObjectMapper;

/**
 * Сервлет, обрабатывающий запросы на получение информации о криптовалюте с наибольшим процентным изменением цены за день.
 */
@WebServlet("/max-daily-price-change")
public class MaximumDailyPriceChangeServlet extends HttpServlet {

    private final CryptocurrencyWebService webService;

    private final ObjectMapper mapper;

    public MaximumDailyPriceChangeServlet(CryptocurrencyWebService webService) {
        this.webService = webService;
        this.mapper = defaultObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MaximumDailyChangeCryptocurrencyDto infoDto =
                webService.getCryptocurrencyWithMaxDailyPriceChange();
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getOutputStream().write(mapper.writeValueAsBytes(infoDto));
    }
}
