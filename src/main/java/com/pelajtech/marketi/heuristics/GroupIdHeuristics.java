package com.pelajtech.marketi.heuristics;

import com.pelajtech.marketi.item.RawShoppingItem;

public class GroupIdHeuristics {

    public static String defaultGroupIdGenerator(RawShoppingItem item) {
        return NameHeuristics.unitsAtEnd(item.name());
    }

}
