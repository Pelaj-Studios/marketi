package com.pelajtech.marketi.pipeline;

import com.pelajtech.marketi.item.RawShoppingItem;

import java.util.function.Consumer;

public interface ItemDownloader {

    void download(Consumer<RawShoppingItem> sink);

}
