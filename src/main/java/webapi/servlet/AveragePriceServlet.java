package webapi.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import webapi.dto.CryptocurrencyAverageInfoDto;
import webapi.service.CryptocurrencyWebService;

import java.io.IOException;

import static utils.ApplicationConstantHolder.defaultObjectMapper;

/**
 * Сервлет, обрабатывающий расчет среднего значения криптовалюты.
 */
@WebServlet("/average")
public class AveragePriceServlet extends HttpServlet {
    private final CryptocurrencyWebService webService;

    private final ObjectMapper mapper;

    public AveragePriceServlet(CryptocurrencyWebService webService) {
        this.webService = webService;
        this.mapper = defaultObjectMapper();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] splittedQueryString = req.getQueryString().split("=");
        String queryKey = splittedQueryString[0];

        if (queryKey.equals("symbol")) {
            String queryValue = splittedQueryString[1].toUpperCase();
            CryptocurrencyAverageInfoDto infoDto = webService.getHourAverageForSymbol(queryValue);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getOutputStream().write(mapper.writeValueAsBytes(infoDto));
        }
    }
}
