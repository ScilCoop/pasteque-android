package fr.pasteque.api.parser;

import fr.pasteque.api.API;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.models.TicketLineModel;
import fr.pasteque.api.models.TicketModel;
import fr.pasteque.api.utils.ApiJsonUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 11:34.
 */
public class SharedTicketParser extends Parser<JSONArray, List<TicketModel>> {

    public SharedTicketParser(API.Handler<List<TicketModel>> handler) {
        super(handler);
    }

    @Override
    protected List<TicketModel> parse(JSONArray array) {
        List<TicketModel> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            TicketModel ticketModel = new TicketModel();
            ApiJsonUtils json = new ApiJsonUtils(array.getJSONObject(i));
            ticketModel.id = json.getString("id");
            ticketModel.label = json.getString("label");
            ticketModel.lines = new ArrayList<>();
            JSONArray lines = json.getJSONArray("lines");
            for (int j = 0; j < lines.length(); j++) {
                TicketLineModel ticketLineModel = new TicketLineModel();
                ApiJsonUtils line = new ApiJsonUtils(lines.getJSONObject(j));
                ticketLineModel.product = new ProductModel();
                ticketLineModel.product.id = line.getString("productId");
                ticketLineModel.quantity = line.getString("quantity");
                ticketModel.lines.add(ticketLineModel);
            }
            result.add(ticketModel);
        }
        return result;
    }


}
