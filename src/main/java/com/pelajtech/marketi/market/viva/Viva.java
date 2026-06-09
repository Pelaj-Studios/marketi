package com.pelajtech.marketi.market.viva;

import java.net.URI;

public final class Viva {

    public static final String BASE_URL = "https://online.vivafresh.shop/";
    public static final String MARKET_ID = "viva";
    public static final String PRODUCT_LOYALTY_URL = BASE_URL
            + "lib/config/proxy.php?endpoint=shop/product-loyalty?organization_id=9"
            + "&page=%d&per_page=%d&filter%%5Btags%%5D=produktet-tona";

    private Viva() {
    }

    public static URI productLoyaltyUri(int page, int perPage) {
        return URI.create(PRODUCT_LOYALTY_URL.formatted(page, perPage));
    }

}
